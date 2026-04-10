package com.souschef.ui.screens.device.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.service.device.DevicePreferenceService
import com.souschef.usecases.device.RefillCompartmentUseCase
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DispenserSettingsViewModel(
    private val deviceService: DevicePreferenceService,
    private val refillCompartmentUseCase: RefillCompartmentUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isSaving  = MutableStateFlow(false)
    private val _isSaved   = MutableStateFlow(false)
    private val _error     = MutableStateFlow<String?>(null)
    private val _editedCapacities = MutableStateFlow<Map<Int, String>>(emptyMap())

    val uiState: StateFlow<DispenserSettingsUiState> = combine(
        deviceService.getCompartmentsFlow(),
        _editedCapacities,
        combine(_isLoading, _isSaving, _isSaved, _error) { l, sv, sd, e ->
            arrayOf<Any?>(l, sv, sd, e)
        }
    ) { compartments, edited, extras ->
        @Suppress("UNCHECKED_CAST")
        DispenserSettingsUiState(
            compartments     = compartments,
            editedCapacities = edited.ifEmpty {
                compartments.associate { it.compartmentId to it.totalCapacityTsp.toString() }
            },
            isLoading = extras[0] as Boolean,
            isSaving  = extras[1] as Boolean,
            isSaved   = extras[2] as Boolean,
            error     = extras[3] as String?
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = DispenserSettingsUiState(isLoading = true)
    )

    fun onCapacityChange(compartmentId: Int, value: String) {
        _editedCapacities.value = _editedCapacities.value.toMutableMap().also {
            it[compartmentId] = value
        }
    }

    fun onSave() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            _error.value    = null
            try {
                val current = deviceService.getCompartmentsFlow().let { flow ->
                    var list = emptyList<com.souschef.model.device.Compartment>()
                    flow.collect { list = it; return@collect }
                    list
                }
                val edited = _editedCapacities.value
                val updated = current.map { comp ->
                    val rawCapacity = edited[comp.compartmentId]
                    val newCapacity = rawCapacity?.toDoubleOrNull() ?: comp.totalCapacityTsp
                    comp.copy(totalCapacityTsp = newCapacity.coerceAtLeast(0.0))
                }
                deviceService.saveCompartments(updated)
                _isSaving.value = false
                _isSaved.value  = true
            } catch (e: Exception) {
                _isSaving.value = false
                _error.value    = "Failed to save settings: ${e.message}"
            }
        }
    }

    fun onResetAllCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            (1..5).forEach { id ->
                refillCompartmentUseCase.execute(id).collect {}
            }
        }
    }

    fun clearError() { _error.value = null }
}
