package com.souschef.usecases.recipe

import com.souschef.model.recipe.RecipeStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Stateful use case wrapping step navigation and countdown timer logic
 * for an active cooking session.
 *
 * **Lifecycle:** One instance per cooking session. The ViewModel creates
 * this after fetching steps and disposes of it when the screen exits.
 *
 * Timer uses a coroutine-based countdown (100ms tick) — no Android
 * dependency so it's testable in pure JVM tests.
 */
class CookingSessionUseCase(
    private val steps: List<RecipeStep>,
    private val scope: CoroutineScope
) {
    // ── Step Navigation ─────────────────────────────────────

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    val totalSteps: Int get() = steps.size

    fun currentStep(): RecipeStep? = steps.getOrNull(_currentStepIndex.value)

    fun nextStep(): Boolean {
        val next = _currentStepIndex.value + 1
        if (next >= steps.size) return false
        switchToStep(next)
        return true
    }

    fun previousStep(): Boolean {
        val prev = _currentStepIndex.value - 1
        if (prev < 0) return false
        switchToStep(prev)
        return true
    }

    fun goToStep(index: Int) {
        if (index !in steps.indices) return
        switchToStep(index)
    }

    /**
     * Atomically swaps to a new step.
     *
     * Order matters: the timer's remaining millis is set BEFORE the step
     * index is updated. That way any collector observing [currentStepIndex]
     * (and synchronously calling [startTimer], like the cooking-mode
     * ViewModel does) already sees the correct millis for the new step. The
     * old order would leave a stale 0 ms in the timer flow at the moment
     * the index emitted, which made `startTimer` no-op for steps that came
     * after a step without a timer.
     */
    private fun switchToStep(index: Int) {
        cancelTimer()
        val newStep = steps.getOrNull(index)
        val seconds = newStep?.timerSeconds
        _timerMillisRemaining.value =
            if (seconds != null && seconds > 0) seconds * 1000L else 0L
        _currentStepIndex.value = index
    }

    // ── Timer ───────────────────────────────────────────────

    private val _timerMillisRemaining = MutableStateFlow(0L)
    val timerMillisRemaining: StateFlow<Long> = _timerMillisRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerFinished = MutableStateFlow(false)
    val timerFinished: StateFlow<Boolean> = _timerFinished.asStateFlow()

    private var timerJob: Job? = null

    init {
        resetTimerForCurrentStep()
    }

    fun startTimer() {
        if (_timerMillisRemaining.value <= 0L) return
        if (_isTimerRunning.value) return
        _timerFinished.value = false
        _isTimerRunning.value = true
        timerJob = scope.launch {
            while (_timerMillisRemaining.value > 0L) {
                delay(TICK_INTERVAL_MS)
                val remaining = (_timerMillisRemaining.value - TICK_INTERVAL_MS).coerceAtLeast(0L)
                _timerMillisRemaining.value = remaining
            }
            _isTimerRunning.value = false
            _timerFinished.value = true
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
    }

    fun resetTimer() {
        cancelTimer()
        resetTimerForCurrentStep()
    }

    fun clearTimerFinished() {
        _timerFinished.value = false
    }

    // ── Internal ────────────────────────────────────────────

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
        _timerFinished.value = false
    }

    private fun resetTimerForCurrentStep() {
        val step = currentStep()
        val seconds = step?.timerSeconds
        _timerMillisRemaining.value = if (seconds != null && seconds > 0) seconds * 1000L else 0L
    }

    companion object {
        private const val TICK_INTERVAL_MS = 100L
    }
}
