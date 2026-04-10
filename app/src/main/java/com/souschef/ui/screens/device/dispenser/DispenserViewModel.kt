package com.souschef.ui.screens.device.dispenser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.api.ble.BleDeviceManager
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.usecases.device.GetCompartmentsUseCase
import com.souschef.usecases.device.RefillCompartmentUseCase
import com.souschef.usecases.device.UpdateCompartmentUseCase
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DispenserViewModel(
    private val getCompartmentsUseCase: GetCompartmentsUseCase,
    private val updateCompartmentUseCase: UpdateCompartmentUseCase,
    private val refillCompartmentUseCase: RefillCompartmentUseCase,
    private val ingredientRepository: IngredientRepository,
    private val bleDeviceManager: BleDeviceManager
) : ViewModel() {

    private val _isLoading       = MutableStateFlow(false)
    private val _isSaving        = MutableStateFlow(false)
    private val _error           = MutableStateFlow<String?>(null)
    private val _successMessage  = MutableStateFlow<String?>(null)
    private val _searchQuery     = MutableStateFlow("")
    private val _dispensable     = MutableStateFlow<List<GlobalIngredient>>(emptyList())

    val uiState: StateFlow<DispenserUiState> = combine(
        getCompartmentsUseCase.execute(),
        bleDeviceManager.connectionState,
        _dispensable,
        _searchQuery,
        combine(_isLoading, _isSaving, _error, _successMessage) { l, s, e, m ->
            arrayOf<Any?>(l, s, e, m)
        }
    ) { compartmentsRes, bleState, dispensable, query, extras ->
        val compartments = (compartmentsRes as? Resource.Success)?.data
            ?: List(5) { com.souschef.model.device.Compartment(compartmentId = it + 1) }

        val filtered = if (query.isBlank()) dispensable
        else dispensable.filter { it.name.contains(query, ignoreCase = true) }

        @Suppress("UNCHECKED_CAST")
        DispenserUiState(
            compartments          = compartments,
            connectionState       = bleState,
            dispensableIngredients = dispensable,
            filteredIngredients   = filtered,
            searchQuery           = query,
            isLoading             = extras[0] as Boolean,
            isSaving              = extras[1] as Boolean,
            error                 = extras[2] as String?,
            successMessage        = extras[3] as String?
        )
    }.stateIn(
        scope         = viewModelScope,
        started       = SharingStarted.WhileSubscribed(5_000),
        initialValue  = DispenserUiState(isLoading = true)
    )

    init {
        loadDispensableIngredients()
    }

    // ── BLE ───────────────────────────────────────────────────────────────────

    fun onScanConnect() {
        bleDeviceManager.scanAndConnect()
    }

    fun onDisconnect() {
        bleDeviceManager.disconnect()
    }

    // ── Compartment management ────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onAssignIngredient(compartmentId: Int, ingredient: GlobalIngredient) {
        val currentCapacity = uiState.value.compartments
            .firstOrNull { it.compartmentId == compartmentId }?.totalCapacityTsp ?: 0.0

        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            updateCompartmentUseCase
                .execute(compartmentId, ingredient, currentCapacity)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _isSaving.value     = false
                            _successMessage.value = "Spice assigned to Compartment $compartmentId"
                        }
                        is Resource.Failure -> {
                            _isSaving.value = false
                            _error.value    = result.message ?: "Failed to assign ingredient"
                        }
                        is Resource.Loading -> { /* spinner already shown */ }
                    }
                }
        }
    }

    fun onClearCompartment(compartmentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            updateCompartmentUseCase.execute(compartmentId, null, 0.0).collect {}
        }
    }

    fun onRefill(compartmentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            refillCompartmentUseCase.execute(compartmentId).collect { result ->
                if (result is Resource.Success) {
                    _successMessage.value = "Compartment $compartmentId marked as refilled"
                }
            }
        }
    }

    fun clearError()   { _error.value = null }
    fun clearSuccess() { _successMessage.value = null }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun loadDispensableIngredients() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            ingredientRepository.getAllIngredients().collect { result ->
                _dispensable.value = result.filter { it.isDispensable }
                _isLoading.update { false }
            }
        }
    }
}
