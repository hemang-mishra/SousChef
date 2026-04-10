package com.souschef.ui.screens.recipe.cooking

import com.souschef.model.device.BleConnectionState
import com.souschef.model.device.DispenseResult
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient

/**
 * UI state for the step-by-step cooking mode screen.
 *
 * [adjustedIngredients] are the fully scaled + flavor-adjusted ingredient list
 * carried forward from phase 3 (RecipeOverview). Each step can reference a
 * subset of these via [RecipeStep.ingredientReferences].
 */
data class CookingModeUiState(
    val steps: List<RecipeStep> = emptyList(),
    val adjustedIngredients: List<ResolvedIngredient> = emptyList(),
    val currentStepIndex: Int = 0,
    val timerMillisRemaining: Long = 0L,
    val isTimerRunning: Boolean = false,
    val timerFinished: Boolean = false,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val error: String? = null,

    // ── Phase 5 hardware additions ────────────────────────────────────────────

    /** Current BLE connection state with the dispenser. */
    val connectionState: BleConnectionState = BleConnectionState.Disconnected,

    /**
     * Set of globalIngredientIds currently being dispensed (shows spinner on that row).
     * Multiple concurrent dispenses are supported.
     */
    val dispensingIngredientIds: Set<String> = emptySet(),

    /** Most recent dispense result — shown as snackbar / inline feedback. */
    val lastDispenseResult: DispenseResult? = null
)
