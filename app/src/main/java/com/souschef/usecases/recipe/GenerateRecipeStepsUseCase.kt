package com.souschef.usecases.recipe

import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.repository.ai.AiRepository
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.util.IngredientMatcher
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Generates atomic recipe steps from a description using AI, then resolves every
 * AI-generated ingredient name reference to a [GlobalIngredient.ingredientId] using
 * [IngredientMatcher] (Dice-coefficient fuzzy match, ≥ 75% similarity).
 *
 * Unresolved names are preserved in [RecipeStep.unresolvedIngredientNames] so the
 * review UI can surface them as amber warnings for manual linking.
 */
class GenerateRecipeStepsUseCase(
    private val aiRepository: AiRepository,
    private val ingredientRepository: IngredientRepository
) {

    /**
     * @param description  Free-text recipe description from the creator.
     * @param ingredients  Resolved ingredients for this recipe (for building the AI prompt).
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
                        val allGlobalIngredients: List<GlobalIngredient> = try {
                            ingredientRepository.getAllIngredients()
                                .first()
                        } catch (_: Exception) {
                            emptyList()
                        }

                        val resolvedSteps = resolveIngredientReferences(
                            steps = resource.data,
                            globalIngredients = allGlobalIngredients
                        )
                        Resource.success(resolvedSteps)
                    }
                    is Resource.Failure -> resource
                    is Resource.Loading -> resource
                }
            }
    }

    /**
     * For each step, splits [RecipeStep.ingredientReferences] (raw AI names) into:
     * - **resolved** → matched → stored as `globalIngredientId` in [RecipeStep.ingredientReferences].
     * - **unresolved** → no match → stored in [RecipeStep.unresolvedIngredientNames].
     */
    private fun resolveIngredientReferences(
        steps: List<RecipeStep>,
        globalIngredients: List<GlobalIngredient>
    ): List<RecipeStep> {
        return steps.map { step ->
            val resolvedIds = mutableListOf<String>()
            val unresolved = mutableListOf<String>()

            for (rawName in step.ingredientReferences) {
                val match = IngredientMatcher.fuzzyMatch(rawName, globalIngredients)
                if (match != null) {
                    resolvedIds += match.ingredientId
                } else {
                    unresolved += rawName
                }
            }

            step.copy(
                ingredientReferences = resolvedIds,
                unresolvedIngredientNames = unresolved
            )
        }
    }
}
