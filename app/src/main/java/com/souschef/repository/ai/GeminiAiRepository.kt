package com.souschef.repository.ai

import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.service.ai.GeminiRecipeService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Gemini-backed implementation of [AiRepository].
 * Wraps service calls in Resource flow for consistent error handling.
 */
class GeminiAiRepository(
    private val service: GeminiRecipeService
) : AiRepository {

    override fun generateRecipeSteps(
        description: String,
        ingredients: List<ResolvedIngredient>
    ): Flow<Resource<List<RecipeStep>>> = flow {
        emit(Resource.loading())
        try {
            // Use the new combined method, but only return steps
            val result = service.generateRecipe(description, baseServingSize = 4)
            emit(Resource.success(result.steps))
        } catch (e: Exception) {
            emit(Resource.failure(message = e.message ?: "AI generation failed. Please try again."))
        }
    }

    override fun generateRecipeWithIngredients(
        description: String,
        baseServingSize: Int
    ): Flow<Resource<GeminiRecipeService.GeneratedRecipe>> = flow {
        emit(Resource.loading())
        try {
            val result = service.generateRecipe(description, baseServingSize)
            emit(Resource.success(result))
        } catch (e: Exception) {
            emit(Resource.failure(message = e.message ?: "AI generation failed. Please try again."))
        }
    }
}
