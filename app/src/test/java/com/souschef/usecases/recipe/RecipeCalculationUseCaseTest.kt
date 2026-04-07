package com.souschef.usecases.recipe

import com.souschef.model.recipe.ResolvedIngredient
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeCalculationUseCaseTest {

    private val useCase = RecipeCalculationUseCase()

    @Test
    fun calculate_scalesBySelectedServings() {
        val ingredients = listOf(
            ResolvedIngredient(
                globalIngredientId = "ing_1",
                name = "Rice",
                quantity = 0.0,
                unit = "grams",
                perPersonQuantity = 50.0
            )
        )

        val result = useCase.calculate(
            ingredients = ingredients,
            baseServingSize = 2,
            selectedServings = 4,
            spiceLevel = 0f,
            saltLevel = 0f,
            sweetnessLevel = 0f
        )

        assertEquals(200.0, result.first().quantity, 0.0)
    }

    @Test
    fun calculate_appliesOnlyRelevantFlavorAdjustment() {
        val ingredients = listOf(
            ResolvedIngredient(
                globalIngredientId = "ing_1",
                name = "Chili",
                unit = "grams",
                perPersonQuantity = 10.0,
                spiceIntensityValue = 8.0
            ),
            ResolvedIngredient(
                globalIngredientId = "ing_2",
                name = "Sugar",
                unit = "grams",
                perPersonQuantity = 10.0,
                sweetnessValue = 8.0
            )
        )

        val result = useCase.calculate(
            ingredients = ingredients,
            baseServingSize = 1,
            selectedServings = 2,
            spiceLevel = 0.5f,
            saltLevel = 0f,
            sweetnessLevel = 0f
        )

        assertEquals(28.0, result[0].quantity, 0.0)
        assertEquals(20.0, result[1].quantity, 0.0)
    }

    @Test
    fun calculate_appliesMultipleFlavorAdjustmentsForSingleIngredient() {
        val ingredients = listOf(
            ResolvedIngredient(
                globalIngredientId = "ing_1",
                name = "Soy Sauce",
                unit = "ml",
                perPersonQuantity = 15.0,
                spiceIntensityValue = 2.0,
                saltnessValue = 8.0
            )
        )

        val result = useCase.calculate(
            ingredients = ingredients,
            baseServingSize = 1,
            selectedServings = 2,
            spiceLevel = 0.5f,
            saltLevel = 0.5f,
            sweetnessLevel = 0f
        )

        assertEquals(46.2, result.first().quantity, 0.0)
    }

    @Test
    fun calculate_roundsToSingleDecimal() {
        val ingredients = listOf(
            ResolvedIngredient(
                globalIngredientId = "ing_1",
                name = "Pepper",
                unit = "grams",
                perPersonQuantity = 3.333,
                spiceIntensityValue = 7.0
            )
        )

        val result = useCase.calculate(
            ingredients = ingredients,
            baseServingSize = 1,
            selectedServings = 3,
            spiceLevel = 0.3f,
            saltLevel = 0f,
            sweetnessLevel = 0f
        )

        assertEquals(12.1, result.first().quantity, 0.0)
    }
}

