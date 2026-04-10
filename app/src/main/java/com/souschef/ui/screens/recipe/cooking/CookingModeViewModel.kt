package com.souschef.ui.screens.recipe.cooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.api.ble.BleDeviceManager
import com.souschef.model.device.DispenseResult
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.usecases.device.DispenseSpiceUseCase
import com.souschef.usecases.recipe.CookingSessionUseCase
import com.souschef.usecases.recipe.RecipeCalculationUseCase
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the step-by-step cooking mode screen.
 *
 * Receives nav params (recipeId + flavour adjustments), loads steps & ingredients,
 * then delegates step navigation / timer to [CookingSessionUseCase].
 *
 * Phase 5 additions:
 * - Exposes BLE [connectionState] from [BleDeviceManager].
 * - [dispenseIngredient] triggers [DispenseSpiceUseCase] and tracks in-flight ids.
 */
class CookingModeViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val calculationUseCase: RecipeCalculationUseCase,
    private val dispenseSpiceUseCase: DispenseSpiceUseCase,
    private val bleDeviceManager: BleDeviceManager,
    private val recipeId: String,
    private val selectedServings: Int,
    private val spiceLevel: Float,
    private val saltLevel: Float,
    private val sweetnessLevel: Float
) : ViewModel() {

    // ── Internal mutable state ──────────────────────────────────────────────

    private val _isLoading              = MutableStateFlow(true)
    private val _error                  = MutableStateFlow<String?>(null)
    private val _steps                  = MutableStateFlow<List<RecipeStep>>(emptyList())
    private val _adjustedIngredients    = MutableStateFlow<List<ResolvedIngredient>>(emptyList())
    private val _isFinished             = MutableStateFlow(false)
    private val _dispensingIds          = MutableStateFlow<Set<String>>(emptySet())
    private val _lastDispenseResult     = MutableStateFlow<DispenseResult?>(null)

    // Session state proxied through MutableStateFlows
    private val _currentStepIndex       = MutableStateFlow(0)
    private val _timerMillisRemaining   = MutableStateFlow(0L)
    private val _isTimerRunning         = MutableStateFlow(false)
    private val _timerFinished          = MutableStateFlow(false)

    private var session: CookingSessionUseCase? = null

    // ── Public UiState ──────────────────────────────────────────────────────

    val uiState: StateFlow<CookingModeUiState> = combine(
        combine(_isLoading, _error, _steps, _adjustedIngredients, _isFinished) {
            isLoading, error, steps, ingredients, isFinished ->
            CookingModeUiState(
                steps               = steps,
                adjustedIngredients = ingredients,
                isLoading           = isLoading,
                isFinished          = isFinished,
                error               = error
            )
        },
        combine(_currentStepIndex, _timerMillisRemaining, _isTimerRunning, _timerFinished) {
            stepIndex, timer, running, finished ->
            TimerSnapshot(stepIndex, timer, running, finished)
        },
        bleDeviceManager.connectionState,
        _dispensingIds,
        _lastDispenseResult
    ) { base, timer, bleState, dispensingIds, lastResult ->
        base.copy(
            currentStepIndex    = timer.stepIndex,
            timerMillisRemaining = timer.millis,
            isTimerRunning      = timer.running,
            timerFinished       = timer.finished,
            connectionState     = bleState,
            dispensingIngredientIds = dispensingIds,
            lastDispenseResult  = lastResult
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = CookingModeUiState()
    )

    init {
        loadData()
    }

    // ── Data Loading ────────────────────────────────────────────────────────

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value     = null

            val recipeResult = recipeRepository.getRecipeWithSteps(recipeId)
                .first { it !is Resource.Loading }

            if (recipeResult is Resource.Failure) {
                _isLoading.value = false
                _error.value     = recipeResult.message ?: "Failed to load recipe"
                return@launch
            }

            val payload = (recipeResult as? Resource.Success)?.data
            val recipe  = payload?.first
            val steps   = payload?.second ?: emptyList()

            if (recipe == null || steps.isEmpty()) {
                _isLoading.value = false
                _error.value     = "Recipe or steps not found"
                return@launch
            }

            val ingredientIds = recipe.ingredients.map { it.globalIngredientId }.distinct()
            var adjusted      = emptyList<ResolvedIngredient>()

            if (ingredientIds.isNotEmpty()) {
                val ingResult = ingredientRepository.getIngredientsByIds(ingredientIds)
                    .first { it !is Resource.Loading }

                if (ingResult is Resource.Success) {
                    val globalMap = ingResult.data.associateBy { it.ingredientId }
                    val resolved  = recipe.ingredients.mapNotNull { ri ->
                        globalMap[ri.globalIngredientId]?.let { gi ->
                            ResolvedIngredient.from(ri, gi)
                        }
                    }
                    adjusted = calculationUseCase.calculate(
                        ingredients    = resolved,
                        baseServingSize = recipe.baseServingSize,
                        selectedServings = selectedServings,
                        spiceLevel     = spiceLevel,
                        saltLevel      = saltLevel,
                        sweetnessLevel = sweetnessLevel
                    )
                }
            }

            val sortedSteps  = steps.sortedBy { it.stepNumber }
            _steps.value     = sortedSteps
            _adjustedIngredients.value = adjusted

            val cookingSession = CookingSessionUseCase(sortedSteps, viewModelScope)
            session = cookingSession
            observeSessionFlows(cookingSession)

            _isLoading.value = false
        }
    }

    private fun observeSessionFlows(s: CookingSessionUseCase) {
        viewModelScope.launch { s.currentStepIndex.collect    { _currentStepIndex.value     = it } }
        viewModelScope.launch { s.timerMillisRemaining.collect { _timerMillisRemaining.value = it } }
        viewModelScope.launch { s.isTimerRunning.collect       { _isTimerRunning.value       = it } }
        viewModelScope.launch { s.timerFinished.collect        { _timerFinished.value        = it } }
    }

    // ── User Actions ────────────────────────────────────────────────────────

    fun nextStep()     { session?.nextStep() }
    fun previousStep() { session?.previousStep() }
    fun goToStep(index: Int) { session?.goToStep(index) }
    fun startTimer()   { session?.startTimer() }
    fun pauseTimer()   { session?.pauseTimer() }
    fun resetTimer()   { session?.resetTimer() }
    fun clearTimerFinished() { session?.clearTimerFinished() }
    fun finishCooking() { _isFinished.value = true }
    fun clearError()   { _error.value = null }

    // ── Phase 5: Dispense ───────────────────────────────────────────────────

    /**
     * Triggers a dispense operation for [globalIngredientId].
     * Marks the ingredient as 'dispensing' to show a spinner in the UI.
     * [lastDispenseResult] is updated with the outcome.
     */
    fun dispenseIngredient(
        globalIngredientId: String,
        ingredientName: String,
        quantity: Double,
        unit: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _dispensingIds.update { it + globalIngredientId }

            dispenseSpiceUseCase.execute(
                globalIngredientId = globalIngredientId,
                ingredientName     = ingredientName,
                quantity           = quantity,
                unit               = unit
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _lastDispenseResult.value = result.data
                    }
                    is Resource.Failure -> {
                        _lastDispenseResult.value =
                            DispenseResult.BleError(result.message ?: "Unknown error")
                    }
                    is Resource.Loading -> { /* spinner already active */ }
                }
            }

            _dispensingIds.update { it - globalIngredientId }
        }
    }

    /** Clears the last dispense result after the UI has consumed it. */
    fun clearDispenseResult() { _lastDispenseResult.value = null }

    // ── Internal helpers ────────────────────────────────────────────────────

    private data class TimerSnapshot(
        val stepIndex: Int,
        val millis: Long,
        val running: Boolean,
        val finished: Boolean
    )
}
