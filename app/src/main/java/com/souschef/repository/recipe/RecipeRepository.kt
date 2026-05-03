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
    fun getRecipeWithSteps(recipeId: String): Flow<Resource<Pair<Recipe, List<RecipeStep>>>>
    fun getRecipesByCreator(creatorId: String): Flow<List<Recipe>>
    fun getAllRecipes(): Flow<List<Recipe>>
    fun addStep(recipeId: String, step: RecipeStep): Flow<Resource<Unit>>
    fun updateStep(recipeId: String, stepId: String, updates: Map<String, Any>): Flow<Resource<Unit>>
    fun getSteps(recipeId: String): Flow<Resource<List<RecipeStep>>>
    fun deleteAllSteps(recipeId: String): Flow<Resource<Unit>>
    fun batchAddSteps(recipeId: String, steps: List<RecipeStep>): Flow<Resource<Unit>>
    fun deleteRecipe(recipeId: String): Flow<Resource<Unit>>

    // ── Phase 7: Fork & Save ──────────────────────────────────────────────────
    fun forkRecipe(
        original: Recipe,
        newCreatorId: String,
        newCreatorName: String,
        newCreatorIsVerifiedChef: Boolean
    ): Flow<Resource<String>>

    fun saveRecipe(userId: String, recipeId: String): Flow<Resource<Unit>>
    fun unsaveRecipe(userId: String, recipeId: String): Flow<Resource<Unit>>
    fun isRecipeSaved(userId: String, recipeId: String): Flow<Resource<Boolean>>
    fun getSavedRecipes(userId: String): Flow<List<Recipe>>
}

