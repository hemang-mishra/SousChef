package com.souschef.ui.screens.recipe.overview

import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.model.recipe.SupportedLanguages

/**
 * UI state for the Recipe Overview screen.
 *
 * [resolvedIngredients] holds the original resolved data (RecipeIngredient + GlobalIngredient).
 * [adjustedIngredients] holds the quantities after serving/flavor adjustments — this is what the UI renders.
 */
data class RecipeOverviewUiState(
    val recipe: Recipe? = null,
    val steps: List<RecipeStep> = emptyList(),
    val resolvedIngredients: List<ResolvedIngredient> = emptyList(),
    val adjustedIngredients: List<ResolvedIngredient> = emptyList(),
    val selectedServings: Int = 1,
    val spiceLevel: Float = 0f,
    val saltLevel: Float = 0f,
    val sweetnessLevel: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentUserId: String? = null,
    val isDeleted: Boolean = false,

    // ── Phase 6: Localization ────────────────────────────────────────────────
    val language: String = SupportedLanguages.ENGLISH,
    /** True while the recipe is being translated on demand for [language]. */
    val isTranslating: Boolean = false
)

