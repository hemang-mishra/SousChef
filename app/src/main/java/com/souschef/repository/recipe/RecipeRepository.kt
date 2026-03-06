package com.souschef.repository.recipe

import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.RecipeStep
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Recipe repository interface.
 * All one-shot methods return Flow<Resource<T>> — emit Loading first, then Success or Failure.
 */
interface RecipeRepository {
    fun createRecipe(recipe: Recipe): Flow<Resource<String>>
    fun updateRecipe(recipeId: String, updates: Map<String, Any>): Flow<Resource<Unit>>
    fun getRecipe(recipeId: String): Flow<Resource<Recipe>>
    fun getRecipesByCreator(creatorId: String): Flow<List<Recipe>>
    fun addStep(recipeId: String, step: RecipeStep): Flow<Resource<Unit>>
    fun getSteps(recipeId: String): Flow<Resource<List<RecipeStep>>>
}

