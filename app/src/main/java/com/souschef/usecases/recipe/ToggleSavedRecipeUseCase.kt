package com.souschef.usecases.recipe

import com.souschef.repository.recipe.RecipeRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Phase 7 — toggle bookmark/save state for a recipe.
 */
class ToggleSavedRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {
    fun execute(userId: String, recipeId: String, currentlySaved: Boolean): Flow<Resource<Unit>> =
        if (currentlySaved) recipeRepository.unsaveRecipe(userId, recipeId)
        else recipeRepository.saveRecipe(userId, recipeId)
}
