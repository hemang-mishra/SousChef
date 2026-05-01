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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    /**
     * One-shot haptic events the screen should react to. These are NOT part of
     * [uiState] because re-collecting state shouldn't replay vibrations on
     * configuration changes (e.g. screen rotation).
     */
    private val _hapticEvents = MutableSharedFlow<HapticEvent>(extraBufferCapacity = 4)
    val hapticEvents: SharedFlow<HapticEvent> = _hapticEvents.asSharedFlow()

    private var session: CookingSessionUseCase? = null
    private var lastTimerFinished = false

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
        viewModelScope.launch {
            s.currentStepIndex.collect { idx ->
                _currentStepIndex.value = idx
                onStepEntered(idx)
            }
        }
        viewModelScope.launch { s.timerMillisRemaining.collect { _timerMillisRemaining.value = it } }
        viewModelScope.launch { s.isTimerRunning.collect       { _isTimerRunning.value       = it } }
        viewModelScope.launch {
            s.timerFinished.collect { finished ->
                _timerFinished.value = finished
                // Edge-trigger: only act on the false → true transition so we
                // don't re-fire haptics or TTS every time the flow re-emits.
                if (finished && !lastTimerFinished) {
                    onTimerFinished()
                }
                lastTimerFinished = finished
            }
        }
    }

    /**
     * Called when the active step's countdown timer reaches zero. Plays a
     * spoken alert via TTS and dispatches a strong haptic so the user knows
     * to come back to the pan even if their phone is across the kitchen.
     */
    private fun onTimerFinished() {
        val language = languageManager.currentLanguage.value
        ttsService.stop()
        ttsService.speak(RecipeStepNarrator.timerFinishedAnnouncement(language), language)
        viewModelScope.launch { _hapticEvents.emit(HapticEvent.TimerFinished) }
    }

    /**
     * Called whenever the active step changes (including the very first step
     * when the session starts). Drives the hands-free experience:
     *
     *  1. Speaks a humanized narration of the new step in the active language.
     *  2. If the step has a timer, schedules an auto-start with a small
     *     delay so the user has a beat to register the new step on screen
     *     before the countdown begins ticking.
     *
     * The user can still pause / reset the timer or replay the narration
     * from the screen at any time.
     */
    private var pendingTimerStart: kotlinx.coroutines.Job? = null

    private fun onStepEntered(index: Int) {
        // Cancel any timer auto-start scheduled for the previous step — if
        // the user advanced before that delay fired, we don't want it
        // starting a timer on the wrong step.
        pendingTimerStart?.cancel()
        pendingTimerStart = null

        val steps = _steps.value
        val step = steps.getOrNull(index) ?: return
        val ingredient = _stepIngredientMap.value[index]
        val language = languageManager.currentLanguage.value
        val timerSecs = step.timerSeconds
        val willAutoStartTimer = timerSecs != null && timerSecs > 0
        val ingredientLoadedInDispenser = ingredient != null && isIngredientLoaded(ingredient)

        val narration = RecipeStepNarrator.build(
            step = step,
            stepIndex = index,
            totalSteps = steps.size,
            ingredient = ingredient,
            language = language,
            autoTimerStarted = willAutoStartTimer,
            dispensableAvailable = ingredientLoadedInDispenser
        )
        ttsService.stop()
        ttsService.speak(narration, language)

        if (willAutoStartTimer) {
            // Slight delay before kicking off the countdown — gives the user
            // a moment to look at the step and lets the narrator say the
            // first sentence before they hear the seconds tick down. Captured
            // so we can cancel it if the user moves on quickly.
            pendingTimerStart = viewModelScope.launch {
                kotlinx.coroutines.delay(AUTO_TIMER_START_DELAY_MS)
                // Re-check that we're still on the same step in case the
                // user navigated during the delay.
                if (_currentStepIndex.value == index) {
                    session?.startTimer()
                }
            }
        }
    }

    // ── User Actions ────────────────────────────────────────────────────────

    override fun onCleared() {
        ttsService.stop()
        super.onCleared()
    }

    fun nextStep() {
        val advanced = session?.nextStep() == true
        if (advanced) viewModelScope.launch { _hapticEvents.emit(HapticEvent.StepAdvanced) }
    }
    fun previousStep() {
        val moved = session?.previousStep() == true
        if (moved) viewModelScope.launch { _hapticEvents.emit(HapticEvent.StepAdvanced) }
    }
    fun goToStep(index: Int) {
        val before = _currentStepIndex.value
        session?.goToStep(index)
        if (before != _currentStepIndex.value) {
            viewModelScope.launch { _hapticEvents.emit(HapticEvent.StepAdvanced) }
        }
    }
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
        // Speak a confirmation as soon as the user taps Dispense. Prefer the
        // localized ingredient name from the active step's resolved
        // ingredient when we have it, otherwise fall back to the canonical
        // English name passed by the caller.
        val language = languageManager.currentLanguage.value
        val spokenName = _stepIngredientMap.value.values
            .firstOrNull { it.globalIngredientId == globalIngredientId }
            ?.nameIn(language)
            ?: ingredientName
        ttsService.stop()
        ttsService.speak(
            RecipeStepNarrator.dispensingAnnouncement(spokenName, language),
            language
        )

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
     * Replays / silences the narration for the current step.
     *
     * - If TTS is currently speaking, this stops it (acts as a mute toggle).
     * - Otherwise re-speaks the current step's narration. The timer is NOT
     *   re-started here — replay is a passive action — so we always pass
     *   `autoTimerStarted = false` to avoid the narrator falsely announcing
     *   that it just started a timer that's already mid-countdown.
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
            language = state.language,
            autoTimerStarted = false,
            dispensableAvailable = ingredient != null && isIngredientLoaded(ingredient)
        )
        ttsService.speak(text, state.language)
    }

    /**
     * Mirrors the logic the cooking-mode screen uses to decide whether to
     * show the Dispense button — matches by global ingredient ID first, and
     * falls back to a fuzzy lowercase name match for legacy compartments
     * that don't have an ID populated.
     */
    private fun isIngredientLoaded(ingredient: ResolvedIngredient): Boolean {
        val id = ingredient.globalIngredientId
        if (id.isNotBlank() && _loadedCompartmentIngredientIds.value.contains(id)) return true
        val lowered = ingredient.name.lowercase()
        return _loadedCompartmentIngredientNames.value.any { loaded ->
            lowered.contains(loaded) || loaded.contains(lowered)
        }
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

    private companion object {
        /**
         * Grace period between landing on a step with a timer and actually
         * starting the countdown. Keeps the UX feeling unhurried — the user
         * sees the step, hears the narrator finish their sentence, and only
         * then does the on-screen counter begin to tick. Five seconds gives
         * the TTS engine room to deliver the full step intro plus the
         * "I've started a timer" line before the count begins.
         */
        private const val AUTO_TIMER_START_DELAY_MS = 12000L
    }

    private suspend fun reloadAfterTranslation() {
        android.util.Log.d("TranslationDebug", "Reloading steps and ingredients after translation")
        val recipeResult = recipeRepository.getRecipeWithSteps(recipeId)
            .first { it !is Resource.Loading }
        val payload = (recipeResult as? Resource.Success)?.data ?: return
        val recipe = payload.first
        val steps = payload.second.sortedBy { it.stepNumber }
        android.util.Log.d("TranslationDebug", "Fetched ${steps.size} steps. Step 0 localizations: ${steps.firstOrNull()?.localizations}")

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

/**
 * One-shot UI feedback events emitted by the cooking-mode flow. The screen
 * collects these to drive haptics so the ViewModel stays platform-agnostic.
 */
sealed class HapticEvent {
    /** Light tick when the user moves between steps. */
    data object StepAdvanced : HapticEvent()
    /** Strong, longer pattern when a step's countdown finishes. */
    data object TimerFinished : HapticEvent()
}
