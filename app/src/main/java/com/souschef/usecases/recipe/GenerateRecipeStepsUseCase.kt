package com.souschef.usecases.recipe

import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.repository.ai.AiRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Generates atomic recipe steps from a description using AI.
 *
 * Also resolves ingredient name references in the generated steps
 * back to globalIngredientIds by matching against the provided
 * ResolvedIngredient list.
 */
class GenerateRecipeStepsUseCase(
    private val aiRepository: AiRepository
) {

    /**
     * @param description Free-text recipe description from the creator.
     * @param ingredients Resolved ingredient list (joined RecipeIngredient + GlobalIngredient).
     * @return Flow emitting Loading → Success(steps with resolved ingredient IDs) or Failure.
     */
    fun execute(
        description: String,
        ingredients: List<ResolvedIngredient>
    ): Flow<Resource<List<RecipeStep>>> {
        return aiRepository.generateRecipeSteps(description, ingredients)
            .map { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val resolvedSteps = resolveIngredientReferences(
                            resource.data,
                            ingredients
                        )
                        Resource.success(resolvedSteps)
                    }
                    is Resource.Failure -> resource
                    is Resource.Loading -> resource
                }
            }
    }

    /**
     * Maps ingredient name references from AI output back to globalIngredientIds.
     *
     * Gemini returns ingredient names (e.g. "Red Chili Powder") in ingredientReferences.
     * We match these against our ResolvedIngredient list to get the actual IDs.
     * Uses case-insensitive substring matching for robustness.
     */
    private fun resolveIngredientReferences(
        steps: List<RecipeStep>,
        ingredients: List<ResolvedIngredient>
    ): List<RecipeStep> {
        return steps.map { step ->
            val resolvedRefs = step.ingredientReferences.mapNotNull { refName ->
                // Try exact match first, then fuzzy
                ingredients.find {
                    it.name.equals(refName, ignoreCase = true)
                }?.globalIngredientId
                    ?: ingredients.find {
                        it.name.contains(refName, ignoreCase = true) ||
                                refName.contains(it.name, ignoreCase = true)
                    }?.globalIngredientId
                    ?: refName // Keep the name as-is if no match found
            }
            step.copy(ingredientReferences = resolvedRefs)
        }
    }
}
