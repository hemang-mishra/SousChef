package com.souschef.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.auth.UserProfile
import com.souschef.preferences.AppPreferences
import com.souschef.service.device.DevicePreferenceService
import com.souschef.usecases.device.RefillCompartmentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the unified Settings screen. Owns profile + dispenser compartment
 * data and reuses the existing dispenser persistence layer.
 */
class SettingsViewModel(
    private val deviceService: DevicePreferenceService,
    private val refillCompartmentUseCase: RefillCompartmentUseCase,
    private val appPreferences: AppPreferences,
    private val initialProfile: UserProfile?
) : ViewModel() {

    private val _userProfile = MutableStateFlow(initialProfile)
    private val _editedCapacities = MutableStateFlow<Map<Int, String>>(emptyMap())
    private val _isSaving = MutableStateFlow(false)
    private val _savedMessage = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        _userProfile,
        deviceService.getCompartmentsFlow(),
        _editedCapacities,
        appPreferences.preferredLanguageCode.getFlow(),
        combine(_isSaving, _savedMessage, _error) { saving, msg, err ->
            Triple(saving, msg, err)
        }
    ) { profile, compartments, edited, lang, flags ->
        SettingsUiState(
            userProfile = profile,
            compartments = compartments,
            editedCapacities = edited.ifEmpty {
                compartments.associate { it.compartmentId to it.totalCapacityTsp.toString() }
            },
            preferredLanguageCode = lang,
            isLoading = false,
            isSavingDispenser = flags.first,
            savedMessage = flags.second,
            error = flags.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(userProfile = initialProfile, isLoading = true)
    )

    fun setProfile(profile: UserProfile?) {
        _userProfile.value = profile
    }

    fun onCapacityChange(compartmentId: Int, value: String) {
        _editedCapacities.value = _editedCapacities.value.toMutableMap().also {
            it[compartmentId] = value
        }
    }

    fun onSaveDispenser() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            _error.value = null
            try {
                val current = deviceService.getCompartmentsFlow().first()
                val edited = _editedCapacities.value
                val updated = current.map { comp ->
                    val raw = edited[comp.compartmentId]
                    val newCapacity = raw?.toDoubleOrNull() ?: comp.totalCapacityTsp
                    comp.copy(totalCapacityTsp = newCapacity.coerceAtLeast(0.0))
                }
                deviceService.saveCompartments(updated)
                _savedMessage.value = "Dispenser settings saved"
            } catch (e: Exception) {
                _error.value = "Failed to save: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun onResetAllCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            (1..5).forEach { id -> refillCompartmentUseCase.execute(id).collect {} }
            _savedMessage.value = "All compartments marked as refilled"
        }
    }

    fun setPreferredLanguage(code: String?) {
        viewModelScope.launch { appPreferences.preferredLanguageCode.set(code) }
    }

    fun clearMessage() { _savedMessage.value = null }
    fun clearError() { _error.value = null }
}
