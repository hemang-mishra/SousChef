package com.souschef.usecases.recipe

import com.souschef.repository.recipe.RecipeRepository
import com.souschef.service.storage.FirebaseStorageService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Deletes a recipe, its steps (in repository backend), and its associated media in Storage.
 */
class DeleteRecipeUseCase(
    private val recipeRepository: RecipeRepository,
    private val storageService: FirebaseStorageService
) {
    fun execute(recipeId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())

        try {
            // First drop media from Storage
            storageService.deleteAllRecipeMedia(recipeId)

            // Then delegate to repository, which clears steps subcollection and then the recipe document
            recipeRepository.deleteRecipe(recipeId).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(Resource.failure(com.souschef.util.ResponseError.UNKNOWN, e.localizedMessage ?: "Failed to delete recipe"))
        }
    }
}
