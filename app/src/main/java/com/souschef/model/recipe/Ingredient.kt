package com.souschef.model.recipe

/**
 * **DEPRECATED** — Legacy embedded ingredient model.
 *
 * Use [RecipeIngredient] (references global library via globalIngredientId) +
 * [com.souschef.model.ingredient.GlobalIngredient] instead.
 *
 * Kept for backward compatibility with any existing Firestore data.
 */
@Deprecated(
    message = "Use RecipeIngredient + GlobalIngredient instead",
    replaceWith = ReplaceWith("RecipeIngredient")
)
data class Ingredient(
    val ingredientId: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "grams",
    val perPersonQuantity: Double = 0.0,
    val isDispensable: Boolean = false,
    val spiceIntensityValue: Double = 0.0,
    val sweetnessValue: Double = 0.0,
    val saltnessValue: Double = 0.0
)
