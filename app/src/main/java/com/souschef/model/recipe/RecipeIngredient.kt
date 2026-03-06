package com.souschef.model.recipe

/**
 * Embedded sub-document within a Recipe.
 * References a [com.souschef.model.ingredient.GlobalIngredient] via [globalIngredientId]
 * and stores only recipe-specific quantity data.
 *
 * Flavor attributes and dispensability are resolved at runtime by joining
 * with the referenced GlobalIngredient.
 *
 * All fields have defaults for Firestore deserialization.
 */
data class RecipeIngredient(
    val globalIngredientId: String = "",
    val quantity: Double = 0.0,
    val unit: String = "grams",
    val perPersonQuantity: Double = 0.0
)

