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
    val ingredientReferences: List<String> = emptyList()
)

