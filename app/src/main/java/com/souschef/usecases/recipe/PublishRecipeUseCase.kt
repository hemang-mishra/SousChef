package com.souschef.usecases.recipe

import com.google.firebase.Timestamp
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Publishes a recipe by setting isPublished = true.
 * Validates that the caller is the creator.
 */
class PublishRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {
    fun execute(recipeId: String, currentUserId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())

        // Fetch the recipe first to verify ownership
        recipeRepository.getRecipe(recipeId).collect { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data.creatorId != currentUserId) {
                        emit(Resource.failure(message = "Only the recipe creator can publish."))
                        return@collect
                    }
                    recipeRepository.updateRecipe(
                        recipeId,
                        mapOf(
                            "isPublished" to true,
                            "updatedAt" to Timestamp.now()
                        )
                    ).collect { updateResult ->
                        emit(updateResult)
                    }
                }
                is Resource.Failure -> emit(Resource.failure(result.error, result.message))
                is Resource.Loading -> { /* wait */ }
            }
        }
    }
}

