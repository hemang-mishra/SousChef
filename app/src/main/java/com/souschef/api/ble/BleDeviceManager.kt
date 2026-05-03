package com.souschef.api.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.souschef.model.device.BleConnectionState
import com.souschef.model.device.HardwareButtonEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages BLE scanning, connection, and GATT communication with the SousChef dispenser.
 *
 * ## Protocol
 * - Target device: advertised name = [BleConstants.DEVICE_NAME]
 * - Service UUID:  [BleConstants.SERVICE_UUID]
 * - Dispense char: [BleConstants.DISPENSE_CHARACTERISTIC_UUID]
 * - Command payload: 2 bytes → [compartmentId (Byte), count (Byte)]
 *
 * ## Threading
 * BLE callbacks fire on the BLE thread; state is pushed into [StateFlow] which is
 * coroutine-safe. Never call `sendDispenseCommand` from the main thread while a
 * GATT write is already pending.
 */
@SuppressLint("MissingPermission")  // Caller must check permissions before any call
class BleDeviceManager(private val context: Context) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter get() = bluetoothManager.adapter

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    /**
     * One-shot stream of hardware button presses forwarded by the dispenser.
     * `extraBufferCapacity = 4` prevents missed events if the collector is
     * momentarily off while the kitchen-side user mashes a button.
     */
    private val _buttonEvents = MutableSharedFlow<HardwareButtonEvent>(extraBufferCapacity = 4)
    val buttonEvents: SharedFlow<HardwareButtonEvent> = _buttonEvents.asSharedFlow()

    private var gatt: BluetoothGatt? = null
    private var dispenseCharacteristic: BluetoothGattCharacteristic? = null
    private var buttonCharacteristic: BluetoothGattCharacteristic? = null

    private val scanTimeoutHandler = Handler(Looper.getMainLooper())

    // ── Scanning ──────────────────────────────────────────────────────────────

    /**
     * Starts a BLE scan for [BleConstants.DEVICE_NAME] and connects when found.
     * Automatically times out after [BleConstants.SCAN_TIMEOUT_MS].
     */
    fun scanAndConnect() {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _connectionState.value = BleConnectionState.Error("Bluetooth is disabled")
            return
        }

        _connectionState.value = BleConnectionState.Scanning

        val scanner = adapter.bluetoothLeScanner ?: run {
            _connectionState.value = BleConnectionState.Error("BLE scanner unavailable")
            return
        }

        val filter = ScanFilter.Builder()
            .setDeviceName(BleConstants.DEVICE_NAME)
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(listOf(filter), settings, scanCallback)

        // Stop scanning after timeout
        scanTimeoutHandler.postDelayed({
            scanner.stopScan(scanCallback)
            if (_connectionState.value is BleConnectionState.Scanning) {
                _connectionState.value =
                    BleConnectionState.Error("Device not found. Is it powered on?")
            }
        }, BleConstants.SCAN_TIMEOUT_MS)
    }

    /** Disconnects the current GATT connection and resets state. */
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        dispenseCharacteristic = null
        buttonCharacteristic = null
        _connectionState.value = BleConnectionState.Disconnected
    }

    // ── Scan callback ─────────────────────────────────────────────────────────

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // Stop scanning — device found
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(this)
            scanTimeoutHandler.removeCallbacksAndMessages(null)

            _connectionState.value = BleConnectionState.Connecting
            gatt = result.device.connectGatt(context, false, gattCallback)
        }

        override fun onScanFailed(errorCode: Int) {
            _connectionState.value =
                BleConnectionState.Error("Scan failed (error $errorCode)")
        }
    }

    // ── GATT callback ─────────────────────────────────────────────────────────

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when {
                newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS -> {
                    Log.d("BleDeviceManager", "GATT Connected! Discovering services...")
                    _connectionState.value = BleConnectionState.Connecting
                    gatt.discoverServices()
                }
                newState == BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BleDeviceManager", "GATT Disconnected.")
                    this@BleDeviceManager.gatt?.close()
                    this@BleDeviceManager.gatt = null
                    dispenseCharacteristic = null
                    buttonCharacteristic = null
                    _connectionState.value = BleConnectionState.Disconnected
                }
                else -> {
                    _connectionState.value =
                        BleConnectionState.Error("GATT error (status $status)")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value =
                    BleConnectionState.Error("Service discovery failed (status $status)")
                return
            }
            val service = gatt.getService(BleConstants.SERVICE_UUID)
            if (service == null) {
                _connectionState.value =
                    BleConnectionState.Error("SousChef service not found on device")
                return
            }
            dispenseCharacteristic =
                service.getCharacteristic(BleConstants.DISPENSE_CHARACTERISTIC_UUID)
            if (dispenseCharacteristic == null) {
                _connectionState.value =
                    BleConnectionState.Error("Dispense characteristic not found")
                return
            }

            // Hardware button forwarding is optional — older firmware may not
            // expose this characteristic. Subscribe only if present so the
            // app degrades gracefully on legacy dispensers.
            buttonCharacteristic =
                service.getCharacteristic(BleConstants.BUTTON_CHARACTERISTIC_UUID)
            buttonCharacteristic?.let { subscribeToButtonNotifications(gatt, it) }

            Log.d("BleDeviceManager", "✅ Device fully connected and characteristic assigned!")
            _connectionState.value = BleConnectionState.Connected
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.d("BleDeviceManager", "onCharacteristicWrite callback fired. Status = $status (0=Success)")
            // Future: notify a pending callback/channel on write completion
        }

        // Pre-Tiramisu callback: payload arrives via characteristic.value.
        @Deprecated("Used on API < 33; the new overload below handles 33+.")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            handleButtonNotification(characteristic.uuid, characteristic.value)
        }

        // API 33+ overload: payload is delivered as a separate parameter so
        // we don't depend on the characteristic's mutable value field.
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleButtonNotification(characteristic.uuid, value)
        }
    }

    private fun handleButtonNotification(charUuid: java.util.UUID?, payload: ByteArray?) {
        if (charUuid != BleConstants.BUTTON_CHARACTERISTIC_UUID) return
        val byte = payload?.firstOrNull() ?: return
        val event = when (byte) {
            BleConstants.BUTTON_PAYLOAD_PREVIOUS -> HardwareButtonEvent.PREVIOUS
            BleConstants.BUTTON_PAYLOAD_NEXT     -> HardwareButtonEvent.NEXT
            BleConstants.BUTTON_PAYLOAD_DISPENSE -> HardwareButtonEvent.DISPENSE
            else -> {
                Log.w("BleDeviceManager", "Ignoring unknown button payload: $byte")
                return
            }
        }
        Log.d("BleDeviceManager", "Hardware button: $event")
        _buttonEvents.tryEmit(event)
    }

    /**
     * Enables notifications on the button characteristic — both locally
     * (via [BluetoothGatt.setCharacteristicNotification]) and on the peer by
     * writing 0x0001 to the standard CCC descriptor. Without the descriptor
     * write the ESP32 will never actually push notifications.
     */
    @Suppress("DEPRECATION")
    private fun subscribeToButtonNotifications(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val ok = gatt.setCharacteristicNotification(characteristic, true)
        if (!ok) {
            Log.w("BleDeviceManager", "setCharacteristicNotification returned false")
            return
        }
        val descriptor = characteristic.getDescriptor(BleConstants.CCC_DESCRIPTOR_UUID) ?: run {
            Log.w("BleDeviceManager", "CCC descriptor missing on button characteristic")
            return
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            descriptor.value = android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }

    // ── Dispense command ──────────────────────────────────────────────────────

    /**
     * Sends a dispense command to the hardware.
     *
     * @param compartmentId  Compartment number (1–5).
     * @param count          Number of quarter-teaspoon ejections to perform.
     * @return `true` if the GATT write was initiated, `false` otherwise.
     */
    @Suppress("DEPRECATION")
    fun sendDispenseCommand(compartmentId: Int, count: Int): Boolean {
        val characteristic = dispenseCharacteristic ?: return false
        val currentGatt = gatt ?: return false

        require(compartmentId in 1..5) { "compartmentId must be 1–5" }
        require(count in 1..255) { "count must be 1–255" }

        // 2-byte payload: [compartmentId, count]
        val payload = byteArrayOf(compartmentId.toByte(), count.toByte())

        Log.d("BleDeviceManager", "Preparing to write [${payload[0]}, ${payload[1]}] to characteristic...")

        // Use modern API if available, else fallback
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val result = currentGatt.writeCharacteristic(
                characteristic,
                payload,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            )
            Log.d("BleDeviceManager", "TIRAMISU writeCharacteristic returned status code: $result (0=Success)")
            return result == BluetoothGatt.GATT_SUCCESS
        } else {
            characteristic.value = payload
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            val initiated = currentGatt.writeCharacteristic(characteristic)
            Log.d("BleDeviceManager", "Legacy writeCharacteristic initiated: $initiated")
            return initiated
        }
    }
}
