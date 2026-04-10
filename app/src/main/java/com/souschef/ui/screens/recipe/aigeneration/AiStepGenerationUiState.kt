package com.souschef.ui.screens.recipe.aigeneration

import com.souschef.model.recipe.RecipeStep

/**
 * UI state for the AI Step Generation screen.
 * All fields have sensible defaults for an initial/empty state.
 */
data class AiStepGenerationUiState(
    /** Free-text recipe description entered by the creator. */
    val recipeDescription: String = "",

    /** Ingredient names (resolved from global library) shown as read-only chips. */
    val ingredientChips: List<String> = emptyList(),

    /** AI-generated (and possibly user-edited) steps. */
    val generatedSteps: List<RecipeStep> = emptyList(),

    /** Current stage of the generation flow. */
    val stage: Stage = Stage.INPUT,

    /** True while AI is generating or steps are being saved. */
    val isLoading: Boolean = false,

    /** True while saving steps to Firestore. */
    val isSaving: Boolean = false,

    /** Error message to display, null if none. */
    val error: String? = null,

    /** True after steps have been successfully saved. */
    val isSaved: Boolean = false,

    /** True while the initial recipe data is loading. */
    val isRecipeLoading: Boolean = true,

    /** Recipe title for display. */
    val recipeTitle: String = ""
) {
    /**
     * The three stages of the AI step generation flow.
     */
    enum class Stage {
        /** Stage 1: Creator enters a recipe description. */
        INPUT,
        /** Stage 2: AI is generating steps (loading). */
        LOADING,
        /** Stage 3: Creator reviews, edits, and saves generated steps. */
        REVIEW
    }
}
