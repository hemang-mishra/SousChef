package com.souschef.ui.screens.recipe.cooking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.api.ble.BleDeviceManager
import com.souschef.model.device.DispenseResult
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.model.recipe.SupportedLanguages
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.service.tts.TtsService
import com.souschef.usecases.device.DispenseSpiceUseCase
import com.souschef.usecases.device.GetCompartmentsUseCase
import com.souschef.usecases.recipe.CookingSessionUseCase
import com.souschef.usecases.recipe.RecipeCalculationUseCase
import com.souschef.usecases.translation.TranslateRecipeUseCase
import com.souschef.util.LanguageManager
import com.souschef.util.RecipeStepNarrator
import com.souschef.util.Resource
import kotlin.math.roundToInt
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
    private val getCompartmentsUseCase: GetCompartmentsUseCase,
    private val calculationUseCase: RecipeCalculationUseCase,
    private val dispenseSpiceUseCase: DispenseSpiceUseCase,
    private val bleDeviceManager: BleDeviceManager,
    private val translateRecipeUseCase: TranslateRecipeUseCase,
    private val languageManager: LanguageManager,
    private val ttsService: TtsService,
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
    private val _stepIngredientMap      = MutableStateFlow<Map<Int, ResolvedIngredient>>(emptyMap())
    private val _isFinished             = MutableStateFlow(false)
    private val _loadedCompartmentIngredientIds = MutableStateFlow<Set<String>>(emptySet())
    private val _loadedCompartmentIngredientNames = MutableStateFlow<Set<String>>(emptySet())
    private val _dispensingIds          = MutableStateFlow<Set<String>>(emptySet())
    private val _lastDispenseResult     = MutableStateFlow<DispenseResult?>(null)

    // Session state proxied through MutableStateFlows
    private val _currentStepIndex       = MutableStateFlow(0)
    private val _timerMillisRemaining   = MutableStateFlow(0L)
    private val _isTimerRunning         = MutableStateFlow(false)
    private val _timerFinished          = MutableStateFlow(false)

    // Phase 6: language + narration
    private val _isTranslating          = MutableStateFlow(false)

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
        combine(
            _dispensingIds,
            _loadedCompartmentIngredientIds,
            _loadedCompartmentIngredientNames,
            _lastDispenseResult,
            _stepIngredientMap
        ) { dispensingIds, loadedIds, loadedNames, result, map ->
            DispenseState(dispensingIds, loadedIds, loadedNames, result, map)
        },
        combine(
            languageManager.currentLanguage,
            _isTranslating,
            ttsService.isSpeaking,
            ttsService.missingLanguagePack
        ) { lang, translating, speaking, missingPack ->
            LanguageState(lang, translating, speaking, missingPack)
        }
    ) { base, timer, bleState, dispenseState, langState ->
        base.copy(
            currentStepIndex    = timer.stepIndex,
            timerMillisRemaining = timer.millis,
            isTimerRunning      = timer.running,
            timerFinished       = timer.finished,
            connectionState     = bleState,
            loadedCompartmentIngredientIds = dispenseState.loadedIds,
            loadedCompartmentIngredientNames = dispenseState.loadedNames,
            dispensingIngredientIds = dispenseState.dispensingIds,
            lastDispenseResult  = dispenseState.result,
            stepIngredientMap   = dispenseState.map,
            language            = langState.code,
            isTranslating       = langState.translating,
            isSpeaking          = langState.speaking,
            missingLanguagePack = langState.missingPack
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = CookingModeUiState()
    )

    init {
        loadData()
        loadCompartments()
        observeLanguageForTranslation()
    }

    /**
     * If the active language flips to one we don't have on the recipe yet
     * (e.g. user signed in with Hindi as their default), kick off the
     * translation. The use case itself short-circuits if already done.
     */
    private fun observeLanguageForTranslation() {
        viewModelScope.launch {
            languageManager.currentLanguage.collect { lang ->
                if (lang != SupportedLanguages.ENGLISH) {
                    ensureRecipeTranslated(lang)
                }
            }
        }
    }

    // ── Data Loading ────────────────────────────────────────────────────────

    private fun loadCompartments() {
        viewModelScope.launch(Dispatchers.IO) {
            getCompartmentsUseCase.execute().collect { resource ->
                if (resource is Resource.Success) {
                    val compartments = resource.data
                    val loadedIds = compartments.mapNotNull { it.globalIngredientId }.toSet()
                    val loadedNames = compartments.mapNotNull { it.ingredientName?.lowercase() }.toSet()
                    _loadedCompartmentIngredientIds.value = loadedIds
                    _loadedCompartmentIngredientNames.value = loadedNames
                }
            }
        }
    }

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

            // Build per-step ingredient map with multiplied quantities
            _stepIngredientMap.value = buildStepIngredientMap(sortedSteps, adjusted)

            val cookingSession = CookingSessionUseCase(sortedSteps, viewModelScope)
            session = cookingSession
            observeSessionFlows(cookingSession)

            _isLoading.value = false
        }
    }

    /**
     * Builds a map from stepIndex → ResolvedIngredient for each step that
     * references an ingredient. The quantity is already multiplied by the
     * step's [RecipeStep.quantityMultiplier].
     */
    private fun buildStepIngredientMap(
        steps: List<RecipeStep>,
        adjustedIngredients: List<ResolvedIngredient>
    ): Map<Int, ResolvedIngredient> {
        val ingredientById = adjustedIngredients.associateBy { it.globalIngredientId }
        val map = mutableMapOf<Int, ResolvedIngredient>()
        steps.forEachIndexed { index, step ->
            val ingIdOrName = step.effectiveIngredientId
            if (ingIdOrName != null) {
                // Try to find by explicit global ID first
                var ingredient = ingredientById[ingIdOrName]

                // Fallback for legacy recipes where effectiveIngredientId holds a raw name
                if (ingredient == null) {
                    ingredient = adjustedIngredients.find { it.name.equals(ingIdOrName, ignoreCase = true) }
                }

                // Ultimate fallback: if the ingredient is entirely missing from the recipe payload, mock it so the UI doesn't crash/hide
                if (ingredient == null && ingIdOrName.isNotBlank()) {
                    ingredient = ResolvedIngredient(
                        globalIngredientId = ingIdOrName,
                        name = step.ingredientId ?: ingIdOrName,
                        quantity = 1.0,
                        unit = "unit"
                    )
                }

                if (ingredient != null) {
                    // Apply quantityMultiplier for step-specific amount
                    var stepQty = ingredient.quantity * step.quantityMultiplier
                    if (ingredient.isDispensable) {
                        stepQty = com.souschef.api.ble.BleConstants.roundUpToNearestDispenseStep(stepQty, ingredient.unit)
                    }
                    stepQty = (stepQty * 10).roundToInt() / 10.0
                    map[index] = ingredient.copy(quantity = stepQty)
                }
            }
        }
        return map
    }

    private fun observeSessionFlows(s: CookingSessionUseCase) {
        viewModelScope.launch { s.currentStepIndex.collect    { _currentStepIndex.value     = it } }
        viewModelScope.launch { s.timerMillisRemaining.collect { _timerMillisRemaining.value = it } }
        viewModelScope.launch { s.isTimerRunning.collect       { _isTimerRunning.value       = it } }
        viewModelScope.launch { s.timerFinished.collect        { _timerFinished.value        = it } }
    }

    // ── User Actions ────────────────────────────────────────────────────────

    override fun onCleared() {
        ttsService.stop()
        super.onCleared()
    }

    fun nextStep()     { session?.nextStep(); ttsService.stop() }
    fun previousStep() { session?.previousStep(); ttsService.stop() }
    fun goToStep(index: Int) { session?.goToStep(index); ttsService.stop() }
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

    private data class DispenseState(
        val dispensingIds: Set<String>,
        val loadedIds: Set<String>,
        val loadedNames: Set<String>,
        val result: DispenseResult?,
        val map: Map<Int, ResolvedIngredient>
    )

    private data class LanguageState(
        val code: String,
        val translating: Boolean,
        val speaking: Boolean,
        val missingPack: String?
    )

    // ── Phase 6: Language / Narration ──────────────────────────────────────

    /**
     * Switches the active language. The translation kicks off via the
     * [observeLanguageForTranslation] flow listener so we don't double-fire.
     */
    fun setLanguage(code: String) {
        ttsService.stop()
        languageManager.setLanguage(code)
    }

    /**
     * Forces a fresh AI translation for the active (non-English) language,
     * overwriting any existing localizations on the recipe / steps /
     * ingredients. Useful when an earlier translation was incomplete or
     * incorrect.
     */
    fun retranslate() {
        val target = uiState.value.language
        if (target == SupportedLanguages.ENGLISH) return
        ttsService.stop()
        viewModelScope.launch(Dispatchers.IO) {
            _isTranslating.value = true
            try {
                translateRecipeUseCase.execute(
                    recipeId = recipeId,
                    targetLanguageCode = target,
                    force = true
                ).collect { result ->
                    if (result is Resource.Success) reloadAfterTranslation()
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.w("CookingModeVM", "Retranslate failed: ${e.message}")
            } finally {
                _isTranslating.value = false
            }
        }
    }

    /**
     * Builds a detailed narration for the current step and speaks it via TTS.
     * If TTS is already speaking, this stops it (so the user can tap once
     * to start, tap again to silence).
     */
    fun toggleNarration() {
        if (ttsService.isSpeaking.value) {
            ttsService.stop()
            return
        }
        val state = uiState.value
        val step = state.steps.getOrNull(state.currentStepIndex) ?: return
        val ingredient = state.stepIngredientMap[state.currentStepIndex]
        val text = RecipeStepNarrator.build(
            step = step,
            stepIndex = state.currentStepIndex,
            totalSteps = state.steps.size,
            ingredient = ingredient,
            language = state.language
        )
        ttsService.speak(text, state.language)
    }

    private fun ensureRecipeTranslated(targetLanguage: String) {
        if (targetLanguage == SupportedLanguages.ENGLISH) return
        viewModelScope.launch(Dispatchers.IO) {
            _isTranslating.value = true
            try {
                translateRecipeUseCase.execute(
                    recipeId = recipeId,
                    targetLanguageCode = targetLanguage
                ).collect { result ->
                    if (result is Resource.Success) {
                        // Reload steps + ingredients so the new localizations
                        // become visible without forcing the user to leave
                        // and re-enter the screen.
                        reloadAfterTranslation()
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.w("CookingModeVM", "Translation failed: ${e.message}")
            } finally {
                _isTranslating.value = false
            }
        }
    }

    private suspend fun reloadAfterTranslation() {
        val recipeResult = recipeRepository.getRecipeWithSteps(recipeId)
            .first { it !is Resource.Loading }
        val payload = (recipeResult as? Resource.Success)?.data ?: return
        val recipe = payload.first
        val steps = payload.second.sortedBy { it.stepNumber }

        val ingredientIds = recipe.ingredients.map { it.globalIngredientId }.distinct()
        val resolved = if (ingredientIds.isEmpty()) emptyList() else {
            val ingResult = ingredientRepository.getIngredientsByIds(ingredientIds)
                .first { it !is Resource.Loading }
            if (ingResult is Resource.Success) {
                val map = ingResult.data.associateBy { it.ingredientId }
                recipe.ingredients.mapNotNull { ri ->
                    map[ri.globalIngredientId]?.let { gi -> ResolvedIngredient.from(ri, gi) }
                }
            } else emptyList()
        }
        val adjusted = calculationUseCase.calculate(
            ingredients = resolved,
            baseServingSize = recipe.baseServingSize,
            selectedServings = selectedServings,
            spiceLevel = spiceLevel,
            saltLevel = saltLevel,
            sweetnessLevel = sweetnessLevel
        )
        _steps.value = steps
        _adjustedIngredients.value = adjusted
        _stepIngredientMap.value = buildStepIngredientMap(steps, adjusted)
    }
}
