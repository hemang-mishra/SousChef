package com.souschef.repository.recipe

import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.RecipeStep
import com.souschef.service.recipe.FirebaseRecipeService
import com.souschef.util.Resource
import com.souschef.util.safeFirestoreCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Firebase-backed implementation of [RecipeRepository].
 * Wraps service calls in safeFirestoreCall for error mapping.
 */
class FirestoreRecipeRepository(
    private val service: FirebaseRecipeService
) : RecipeRepository {

    override fun createRecipe(recipe: Recipe): Flow<Resource<String>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.createRecipe(recipe) }
        emit(result)
    }

    override fun updateRecipe(recipeId: String, updates: Map<String, Any>): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.updateRecipe(recipeId, updates) }
        emit(result)
    }

    override fun getRecipe(recipeId: String): Flow<Resource<Recipe>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.getRecipe(recipeId) }
        when (result) {
            is Resource.Success -> {
                if (result.data != null) emit(Resource.success(result.data))
                else emit(Resource.failure(message = "Recipe not found"))
            }
            is Resource.Failure -> emit(Resource.failure(result.error, result.message))
            is Resource.Loading -> { /* already emitted */ }
        }
    }

    override fun getRecipeWithSteps(recipeId: String): Flow<Resource<Pair<Recipe, List<RecipeStep>>>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.getRecipeWithSteps(recipeId) }
        when (result) {
            is Resource.Success -> {
                val recipe = result.data?.first
                val steps = result.data?.second ?: emptyList()
                if (recipe != null) emit(Resource.success(recipe to steps))
                else emit(Resource.failure(message = "Recipe not found"))
            }
            is Resource.Failure -> emit(Resource.failure(result.error, result.message))
            is Resource.Loading -> { /* already emitted */ }
        }
    }

    override fun getRecipesByCreator(creatorId: String): Flow<List<Recipe>> {
        return service.getRecipesByCreatorFlow(creatorId)
    }

    override fun addStep(recipeId: String, step: RecipeStep): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.addStep(recipeId, step) }
        emit(result)
    }

    override fun getSteps(recipeId: String): Flow<Resource<List<RecipeStep>>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.getSteps(recipeId) }
        emit(result)
    }
}

