package com.souschef.model.recipe

import com.souschef.model.ingredient.GlobalIngredient

/**
 * Combined view of [RecipeIngredient] + [GlobalIngredient] for display and calculation purposes.
 * Created by joining a recipe's ingredient reference with the global ingredient library.
 */
data class ResolvedIngredient(
    val globalIngredientId: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val quantity: Double = 0.0,
    val unit: String = "grams",
    val perPersonQuantity: Double = 0.0,
    val isDispensable: Boolean = false,
    val spiceIntensityValue: Double = 0.0,
    val sweetnessValue: Double = 0.0,
    val saltnessValue: Double = 0.0
) {
    companion object {
        /**
         * Resolves a [RecipeIngredient] by joining it with the corresponding [GlobalIngredient].
         */
        fun from(recipeIngredient: RecipeIngredient, globalIngredient: GlobalIngredient): ResolvedIngredient {
            return ResolvedIngredient(
                globalIngredientId = globalIngredient.ingredientId,
                name = globalIngredient.name,
                imageUrl = globalIngredient.imageUrl,
                quantity = recipeIngredient.quantity,
                unit = recipeIngredient.unit,
                perPersonQuantity = recipeIngredient.perPersonQuantity,
                isDispensable = globalIngredient.isDispensable,
                spiceIntensityValue = globalIngredient.spiceIntensityValue,
                sweetnessValue = globalIngredient.sweetnessValue,
                saltnessValue = globalIngredient.saltnessValue
            )
        }
    }
}

