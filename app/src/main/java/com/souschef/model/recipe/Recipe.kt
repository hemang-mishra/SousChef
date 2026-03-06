@file:Suppress("unused")

package com.souschef.model.recipe

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Firestore document model for a recipe.
 * Document path: `recipes/{recipeId}`
 *
 * Ingredients are stored as embedded [RecipeIngredient] sub-documents that
 * reference the global ingredient library via [RecipeIngredient.globalIngredientId].
 *
 * All fields have defaults for Firestore `toObject<Recipe>()`.
 */
data class Recipe(
    @DocumentId
    val recipeId: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val isVerifiedChefRecipe: Boolean = false,
    val baseServingSize: Int = 4,
    val minServingSize: Int? = null,
    val maxServingSize: Int? = null,
    val coverImageUrl: String? = null,
    val isPublished: Boolean = false,
    val originalRecipeId: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val tags: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList()
)

