package com.souschef.usecases.recipe

import com.souschef.model.auth.UserProfile
import com.souschef.model.recipe.Recipe
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Phase 7 — fork (deep copy) a recipe under the current user's account.
 */
class ForkRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {
    fun execute(original: Recipe, currentUser: UserProfile?): Flow<Resource<String>> {
        if (currentUser == null || currentUser.uid.isBlank()) {
            return flowOf(Resource.failure(message = "You need to sign in to fork a recipe"))
        }
        return recipeRepository.forkRecipe(
            original = original,
            newCreatorId = currentUser.uid,
            newCreatorName = currentUser.displayName,
            newCreatorIsVerifiedChef = currentUser.isVerifiedChef
        )
    }
}
