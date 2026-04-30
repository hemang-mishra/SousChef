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
    val saltnessValue: Double = 0.0,
    /**
     * Map carried over from the underlying [GlobalIngredient] so the UI /
     * narrator can render this ingredient in the active language.
     */
    val localizations: Map<String, GlobalIngredientLocalization> = emptyMap()
) {
    /** Localized ingredient name, falling back to English. */
    fun nameIn(language: String): String =
        localizations[language]?.name?.takeIf { it.isNotBlank() } ?: name

    /**
     * Localized unit. Order:
     * 1. The static [CommonUnits] table (handles the canonical English unit
     *    strings like "pieces", "grams", "ml") so we never narrate
     *    "3 pieces tomatoes" in Hindi.
     * 2. The global ingredient's `localizations[language].defaultUnit` (only
     *    used when the recipe's [unit] equals that ingredient's default
     *    unit).
     * 3. Fall back to the canonical English [unit].
     */
    fun unitIn(language: String): String {
        val canonical = unit
        // Static dictionary lookup first — most reliable.
        CommonUnits.translate(canonical, language)?.let { return it }
        // If the recipe is using the ingredient's default unit, the global
        // ingredient's localized default is a good match.
        val globalDefault = localizations[language]?.defaultUnit
        if (!globalDefault.isNullOrBlank()) return globalDefault
        return canonical
    }

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
                saltnessValue = globalIngredient.saltnessValue,
                localizations = globalIngredient.localizations
            )
        }
    }
}

