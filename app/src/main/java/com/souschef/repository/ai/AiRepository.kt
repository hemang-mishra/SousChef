package com.souschef.repository.ai

import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI-powered recipe operations.
 * Abstracts the AI service (Gemini) behind a standard repository pattern.
 */
interface AiRepository {

    /**
     * Generates atomic cooking steps from a recipe description using AI.
     *
     * @param description Free-text recipe description from the creator.
     * @param ingredients Resolved ingredient list for context.
     * @return Flow emitting Loading → Success(steps) or Failure.
     */
    fun generateRecipeSteps(
        description: String,
        ingredients: List<ResolvedIngredient>
    ): Flow<Resource<List<RecipeStep>>>
}
