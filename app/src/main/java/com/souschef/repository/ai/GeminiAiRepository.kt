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
            val steps = service.generateSteps(description, ingredients)
            emit(Resource.success(steps))
        } catch (e: Exception) {
            emit(Resource.failure(message = e.message ?: "AI generation failed. Please try again."))
        }
    }
}
