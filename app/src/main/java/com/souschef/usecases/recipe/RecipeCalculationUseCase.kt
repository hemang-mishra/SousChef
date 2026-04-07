package com.souschef.usecases.recipe

import com.souschef.model.recipe.ResolvedIngredient
import kotlin.math.roundToInt

/**
 * Pure computation use case — no Firestore calls.
 *
 * Calculates final ingredient quantities after applying:
 * 1. Serving-size scaling (perPersonQuantity × selectedServings)
 * 2. Flavor adjustments (spice, salt, sweetness)
 *
 * Each flavor slider is -1f..+1f where 0f = original recipe.
 * Only ingredients that contribute to a given flavor dimension (value > 0) are affected.
 *
 * Formula: adjustedQty = scaledQty × (1 + level × flavorValue / 10)
 */
class RecipeCalculationUseCase {

    /**
     * @param ingredients      Resolved ingredient list (RecipeIngredient joined with GlobalIngredient).
     * @param baseServingSize  The serving size the recipe was authored for.
     * @param selectedServings User-selected serving size.
     * @param spiceLevel       User spice preference: -1f (less) to +1f (more), 0f = as-is.
     * @param saltLevel        User salt preference: -1f to +1f.
     * @param sweetnessLevel   User sweetness preference: -1f to +1f.
     * @return New list of resolved ingredients with adjusted quantities (rounded to 1 decimal).
     */
    fun calculate(
        ingredients: List<ResolvedIngredient>,
        baseServingSize: Int,
        selectedServings: Int,
        spiceLevel: Float = 0f,
        saltLevel: Float = 0f,
        sweetnessLevel: Float = 0f
    ): List<ResolvedIngredient> {
        return ingredients.map { ingredient ->
            // 1. Scale by servings
            var qty = ingredient.perPersonQuantity * selectedServings

            // 2. Apply flavour adjustments (only for ingredients that contribute)
            if (spiceLevel != 0f && ingredient.spiceIntensityValue > 0) {
                qty *= (1.0 + spiceLevel * ingredient.spiceIntensityValue / 10.0)
            }
            if (saltLevel != 0f && ingredient.saltnessValue > 0) {
                qty *= (1.0 + saltLevel * ingredient.saltnessValue / 10.0)
            }
            if (sweetnessLevel != 0f && ingredient.sweetnessValue > 0) {
                qty *= (1.0 + sweetnessLevel * ingredient.sweetnessValue / 10.0)
            }

            // 3. Round to 1 decimal place
            val rounded = (qty * 10).roundToInt() / 10.0

            ingredient.copy(quantity = rounded)
        }
    }
}

