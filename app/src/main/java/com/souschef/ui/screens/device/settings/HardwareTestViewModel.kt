package com.souschef.ui.screens.device.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.api.ble.BleDeviceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HardwareTestUiState(
    val isDispensing: Boolean = false,
    val resultMessage: String? = null
)

class HardwareTestViewModel(
    private val bleDeviceManager: BleDeviceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareTestUiState())
    val uiState: StateFlow<HardwareTestUiState> = _uiState.asStateFlow()

    fun onDispense(compartmentId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDispensing = true, resultMessage = null)
            
            // Sending exactly 1 count (0.25 tsp)
            val success = bleDeviceManager.sendDispenseCommand(compartmentId, 1)
            
            val message = if (success) {
                "Successfully dispensed from compartment $compartmentId"
            } else {
                "Failed to dispense from compartment $compartmentId. Make sure BLE is connected."
            }
            
            _uiState.value = _uiState.value.copy(
                isDispensing = false,
                resultMessage = message
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(resultMessage = null)
    }
}
