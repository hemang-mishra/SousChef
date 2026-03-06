package com.souschef.usecases.ingredient

import com.google.firebase.Timestamp
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Adds a new ingredient to the global library.
 * Validates that the ingredient name is not empty and not a duplicate.
 * Returns the new ingredientId on success.
 */
class AddGlobalIngredientUseCase(
    private val ingredientRepository: IngredientRepository
) {
    fun execute(
        name: String,
        defaultUnit: String,
        isDispensable: Boolean,
        spiceIntensityValue: Double,
        sweetnessValue: Double,
        saltnessValue: Double,
        currentUserId: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.loading())

        // Validate name
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            emit(Resource.failure(message = "Ingredient name is required."))
            return@flow
        }

        // Check for duplicate name
        ingredientRepository.existsByName(trimmedName).collect { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data) {
                        emit(Resource.failure(message = "An ingredient with this name already exists."))
                        return@collect
                    }

                    // Create the ingredient
                    val ingredient = GlobalIngredient(
                        name = trimmedName,
                        defaultUnit = defaultUnit,
                        isDispensable = isDispensable,
                        spiceIntensityValue = spiceIntensityValue,
                        sweetnessValue = sweetnessValue,
                        saltnessValue = saltnessValue,
                        createdByUserId = currentUserId,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )

                    ingredientRepository.createIngredient(ingredient).collect { createResult ->
                        emit(createResult)
                    }
                }
                is Resource.Failure -> emit(Resource.failure(result.error, result.message))
                is Resource.Loading -> { /* wait */ }
            }
        }
    }
}

