package com.souschef.usecases.ingredient

import com.google.firebase.Timestamp
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Updates an existing global ingredient.
 * Validates ownership: only the creator or an admin can edit.
 */
class UpdateGlobalIngredientUseCase(
    private val ingredientRepository: IngredientRepository
) {
    fun execute(
        ingredientId: String,
        name: String?,
        defaultUnit: String?,
        isDispensable: Boolean?,
        spiceIntensityValue: Double?,
        sweetnessValue: Double?,
        saltnessValue: Double?,
        currentUserId: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())

        // First fetch the ingredient to verify ownership
        ingredientRepository.getIngredient(ingredientId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    val existing = result.data
                    if (existing.createdByUserId != currentUserId) {
                        emit(Resource.failure(message = "Only the creator can edit this ingredient."))
                        return@collect
                    }

                    val updates = mutableMapOf<String, Any>("updatedAt" to Timestamp.now())
                    name?.trim()?.takeIf { it.isNotBlank() }?.let { updates["name"] = it }
                    defaultUnit?.let { updates["defaultUnit"] = it }
                    isDispensable?.let { updates["isDispensable"] = it }
                    spiceIntensityValue?.let { updates["spiceIntensityValue"] = it }
                    sweetnessValue?.let { updates["sweetnessValue"] = it }
                    saltnessValue?.let { updates["saltnessValue"] = it }

                    ingredientRepository.updateIngredient(ingredientId, updates).collect { updateResult ->
                        emit(updateResult)
                    }
                }
                is Resource.Failure -> emit(Resource.failure(result.error, result.message))
                is Resource.Loading -> { /* wait */ }
            }
        }
    }
}

