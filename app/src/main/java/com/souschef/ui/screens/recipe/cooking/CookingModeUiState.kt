package com.souschef.ui.screens.recipe.cooking

import com.souschef.model.device.BleConnectionState
import com.souschef.model.device.DispenseResult
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.model.recipe.SupportedLanguages

/**
 * UI state for the step-by-step cooking mode screen.
 *
 * [adjustedIngredients] are the fully scaled + flavor-adjusted ingredient list
 * carried forward from phase 3 (RecipeOverview). Each step can reference a
 * single ingredient via [RecipeStep.ingredientId].
 *
 * [stepIngredientMap] pre-resolves each step's ingredient with its
 * step-specific quantity already multiplied by [RecipeStep.quantityMultiplier].
 */
data class CookingModeUiState(
    val steps: List<RecipeStep> = emptyList(),
    val adjustedIngredients: List<ResolvedIngredient> = emptyList(),

    /**
     * Maps stepIndex → ResolvedIngredient with step-adjusted quantity.
     * Only populated for steps that have an ingredientId.
     * The quantity in the ResolvedIngredient is already multiplied by
     * the step's quantityMultiplier.
     */
    val stepIngredientMap: Map<Int, ResolvedIngredient> = emptyMap(),

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
     * Set of globalIngredientIds currently loaded in physical compartments.
     * Used to determine if the dispense button should be visible.
     */
    val loadedCompartmentIngredientIds: Set<String> = emptySet(),

    /** Set of lowercase names for loaded compartments (fallback matching). */
    val loadedCompartmentIngredientNames: Set<String> = emptySet(),

    /**
     * Set of globalIngredientIds currently being dispensed (shows spinner on that row).
     * Multiple concurrent dispenses are supported.
     */
    val dispensingIngredientIds: Set<String> = emptySet(),

    /** Most recent dispense result — shown as snackbar / inline feedback. */
    val lastDispenseResult: DispenseResult? = null,

    // ── Phase 6: Localization + Narration ────────────────────────────────────

    /** Active language code ("en" / "hi") for instructions + narration. */
    val language: String = SupportedLanguages.ENGLISH,

    /**
     * True while a missing translation is being fetched (after the user taps
     * the Hindi toggle on a recipe that hasn't been translated yet).
     */
    val isTranslating: Boolean = false,

    /** True while TTS is actively speaking. */
    val isSpeaking: Boolean = false,

    /** Set when TTS can't render the requested language locally. */
    val missingLanguagePack: String? = null
)
