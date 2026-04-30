package com.souschef.model.recipe

import com.google.firebase.firestore.DocumentId

/**
 * Firestore sub-collection document for a recipe step.
 * Document path: `recipes/{recipeId}/steps/{stepId}`
 *
 * ## Step Types
 * - **INGREDIENT** — Adding a single ingredient. [ingredientId] is required.
 *   Quantity is **never** hard-coded in [instructionText]; instead it is computed
 *   at runtime as `perPersonQuantity × servings × flavorAdjustment × quantityMultiplier`.
 * - **ACTION** — A cooking action (stir, sauté, heat). No ingredient reference.
 * - **PREP** — Preparation (wash, chop, preheat). May optionally reference one ingredient.
 *
 * ## Quantity Multiplier
 * When an ingredient is used across multiple steps (e.g. "add half the onions now,
 * rest later"), each step carries a [quantityMultiplier] between 0.0 and 1.0.
 * The cooking mode UI multiplies the adjusted quantity by this value.
 *
 * All fields have defaults for Firestore deserialization.
 */
data class RecipeStep(
    @DocumentId
    val stepId: String = "",
    val stepNumber: Int = 0,
    val instructionText: String = "",

    /**
     * Categorises the purpose of this step.
     * One of: `"INGREDIENT"`, `"ACTION"`, `"PREP"`.
     */
    val stepType: String = "ACTION",

    /**
     * Single **globalIngredientId** used in this step.
     * Required for `INGREDIENT` steps, optional for `PREP`, null for `ACTION`.
     *
     * Populated by [GenerateRecipeStepsUseCase] via fuzzy-matching AI output names
     * against the global ingredient library.
     */
    val ingredientId: String? = null,

    /**
     * Fraction of the ingredient's total recipe quantity used in this step.
     * Defaults to 1.0 (use the full amount). Use 0.5 when the ingredient is
     * split across two steps, etc.
     *
     * Only meaningful when [ingredientId] is non-null.
     */
    val quantityMultiplier: Double = 1.0,

    val timerSeconds: Int? = null,
    val flameLevel: String? = null,
    val expectedVisualCue: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "image" or "video"

    // ── Backward compatibility ───────────────────────────────────────────────
    /**
     * **Deprecated** — kept for reading old Firestore documents.
     * New steps use [ingredientId] (single). The cooking mode falls back to
     * `ingredientReferences[0]` when [ingredientId] is null and this list is
     * non-empty.
     */
    @Deprecated("Use ingredientId instead. Kept for Firestore backward compat.")
    val ingredientReferences: List<String>? = emptyList(),

    /**
     * **Deprecated** — raw AI names that couldn't be matched.
     * New flow resolves everything during generation; unresolved names are
     * dropped or surfaced as warnings in the wizard.
     */
    @Deprecated("No longer stored for new steps.")
    val unresolvedIngredientNames: List<String>? = emptyList(),

    /**
     * Map of languageCode → translated copy of this step's user-facing fields.
     * The canonical [instructionText] / [flameLevel] / [expectedVisualCue]
     * always hold the English copy. Use [instructionIn] / [flameLevelIn] /
     * [expectedVisualCueIn] for safe localized reads with an English fallback.
     */
    val localizations: Map<String, RecipeStepLocalization> = emptyMap()
) {
    /** Resolved effective ingredient ID, with backward-compat fallback. */
    @Suppress("DEPRECATION")
    val effectiveIngredientId: String?
        get() = ingredientId ?: ingredientReferences?.firstOrNull()

    /** Localized instruction text, falling back to English. */
    fun instructionIn(language: String): String =
        localizations[language]?.instructionText?.takeIf { it.isNotBlank() }
            ?: instructionText

    /** Localized flame level (e.g. "low" / "धीमी"), falling back to English. */
    fun flameLevelIn(language: String): String? =
        localizations[language]?.flameLevel?.takeIf { it.isNotBlank() } ?: flameLevel

    /** Localized expected visual cue, falling back to English. */
    fun expectedVisualCueIn(language: String): String? =
        localizations[language]?.expectedVisualCue?.takeIf { it.isNotBlank() }
            ?: expectedVisualCue
}
