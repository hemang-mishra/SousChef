package com.souschef.usecases.ingredient

import com.google.firebase.Timestamp
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Updates an existing global ingredient.
 * Any user can edit a global ingredient.
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
        currentUserId: String,
        imageUrl: String? = null,
        clearImage: Boolean = false
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())

        // First fetch the ingredient to verify ownership
        ingredientRepository.getIngredient(ingredientId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    val existing = result.data

                    val updates = mutableMapOf<String, Any>("updatedAt" to Timestamp.now())
                    name?.trim()?.takeIf { it.isNotBlank() }?.let { updates["name"] = it }
                    defaultUnit?.let { updates["defaultUnit"] = it }
                    isDispensable?.let { updates["isDispensable"] = it }
                    spiceIntensityValue?.let { updates["spiceIntensityValue"] = it }
                    sweetnessValue?.let { updates["sweetnessValue"] = it }
                    saltnessValue?.let { updates["saltnessValue"] = it }
                    when {
                        clearImage -> updates["imageUrl"] = com.google.firebase.firestore.FieldValue.delete()
                        imageUrl != null && imageUrl.isNotBlank() -> updates["imageUrl"] = imageUrl
                    }

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

