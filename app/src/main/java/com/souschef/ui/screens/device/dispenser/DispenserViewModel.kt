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
    /** id → imageUrl lookup for the entire library (not just dispensable ones). */
    private val _imageById       = MutableStateFlow<Map<String, String?>>(emptyMap())

    private data class Extras(
        val isLoading: Boolean,
        val isSaving: Boolean,
        val error: String?,
        val successMessage: String?,
        val imageById: Map<String, String?>
    )

    val uiState: StateFlow<DispenserUiState> = combine(
        getCompartmentsUseCase.execute(),
        bleDeviceManager.connectionState,
        _dispensable,
        _searchQuery,
        combine(_isLoading, _isSaving, _error, _successMessage, _imageById) { l, s, e, m, img ->
            Extras(l, s, e, m, img)
        }
    ) { compartmentsRes, bleState, dispensable, query, extras ->
        val compartments = (compartmentsRes as? Resource.Success)?.data
            ?: List(5) { com.souschef.model.device.Compartment(compartmentId = it + 1) }

        val filtered = if (query.isBlank()) dispensable
        else dispensable.filter { it.name.contains(query, ignoreCase = true) }

        DispenserUiState(
            compartments          = compartments,
            connectionState       = bleState,
            dispensableIngredients = dispensable,
            filteredIngredients   = filtered,
            ingredientImageById   = extras.imageById,
            searchQuery           = query,
            isLoading             = extras.isLoading,
            isSaving              = extras.isSaving,
            error                 = extras.error,
            successMessage        = extras.successMessage
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

    fun onError(message: String) {
        _error.value = message
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

    /**
     * Records a physical refill. If [newCapacityTsp] is provided, the
     * compartment's total capacity is updated to that exact amount — i.e.
     * "I just poured in 6 tsp" snaps the gauge to 6 tsp full. Pass null to
     * top the existing capacity back up without changing it.
     */
    fun onRefill(compartmentId: Int, newCapacityTsp: Double? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            refillCompartmentUseCase.execute(compartmentId, newCapacityTsp).collect { result ->
                if (result is Resource.Success) {
                    _successMessage.value = if (newCapacityTsp != null) {
                        "Compartment $compartmentId refilled to %.1f tsp".format(newCapacityTsp)
                    } else {
                        "Compartment $compartmentId marked as refilled"
                    }
                } else if (result is Resource.Failure) {
                    _error.value = result.message ?: "Failed to record refill"
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
                // Index every ingredient (not just dispensables) so the
                // dispenser screen can render the latest imageUrl even when
                // the compartment's denormalised copy is stale.
                _imageById.value = result.associate { it.ingredientId to it.imageUrl }
                _isLoading.update { false }
            }
        }
    }
}
