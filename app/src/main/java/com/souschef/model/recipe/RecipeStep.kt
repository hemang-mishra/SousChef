package com.souschef.model.recipe

import com.google.firebase.firestore.DocumentId

/**
 * Firestore sub-collection document for a recipe step.
 * Document path: `recipes/{recipeId}/steps/{stepId}`
 *
 * All fields have defaults for Firestore deserialization.
 */
data class RecipeStep(
    @DocumentId
    val stepId: String = "",
    val stepNumber: Int = 0,
    val instructionText: String = "",
    val timerSeconds: Int? = null,
    val flameLevel: String? = null,
    val expectedVisualCue: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "image" or "video"
    /**
     * List of **globalIngredientId** values used in this step.
     * Populated by [GenerateRecipeStepsUseCase] via fuzzy-matching AI output names
     * against the global ingredient library.
     */
    val ingredientReferences: List<String> = emptyList(),
    /**
     * Raw ingredient name strings from AI output that could NOT be matched to any
     * global ingredient (similarity < 75%). Shown as amber warnings in the review UI
     * so the user can manually resolve them.
     */
    val unresolvedIngredientNames: List<String> = emptyList()
)

