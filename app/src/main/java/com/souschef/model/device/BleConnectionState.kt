package com.souschef.model.device

/**
 * Represents the BLE connection state of the SousChef dispenser device.
 */
sealed class BleConnectionState {
    /** No active connection attempt. */
    object Disconnected : BleConnectionState()

    /** Currently scanning for the "SousChef-Dispenser" device. */
    object Scanning : BleConnectionState()

    /** Device found; GATT connection in progress. */
    object Connecting : BleConnectionState()

    /** GATT connected and service/characteristic discovered; ready to send commands. */
    object Connected : BleConnectionState()

    /** An error occurred (scan failure, GATT error, permission denied, etc.). */
    data class Error(val message: String) : BleConnectionState()

    /** Whether the device is currently usable for dispensing. */
    val isReady: Boolean get() = this is Connected
}
