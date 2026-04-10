package com.souschef.usecases.recipe

import android.util.Log
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.recipe.RecipeStep
import com.souschef.repository.ai.AiRepository
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.service.ai.GeminiRecipeService
import com.souschef.util.IngredientMatcher
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG = "GenerateRecipeUseCase"

/**
 * Generates a complete recipe from a free-text description using AI.
 *
 * This use case orchestrates:
 * 1. Calling Gemini to generate both **steps** and **ingredients**.
 * 2. Fuzzy-matching each AI-extracted ingredient name against the global library.
 * 3. **Auto-creating** new [GlobalIngredient] entries for unmatched ingredients.
 * 4. Building [RecipeIngredient] list with resolved global IDs and AI-provided quantities.
 * 5. Resolving each step's `ingredientId` from raw name → globalIngredientId.
 *
 * Returns a [GeneratedRecipeData] containing both resolved steps and ingredients.
 */
class GenerateRecipeStepsUseCase(
    private val aiRepository: AiRepository,
    private val ingredientRepository: IngredientRepository
) {

    /**
     * Combined result of AI generation + ingredient resolution.
     */
    data class GeneratedRecipeData(
        val steps: List<RecipeStep>,
        val ingredients: List<RecipeIngredient>,
        /** Names of newly created global ingredients (for user feedback). */
        val newlyCreatedIngredients: List<String> = emptyList()
    )

    /**
     * Executes the full generation + resolution pipeline.
     *
     * @param description     Free-text recipe description.
     * @param baseServingSize Number of servings for quantity calculation.
     * @param creatorUserId   UID of the current user (for auto-created ingredients).
     * @return Flow emitting Loading → Success([GeneratedRecipeData]) or Failure.
     */
    fun execute(
        description: String,
        baseServingSize: Int,
        creatorUserId: String
    ): Flow<Resource<GeneratedRecipeData>> {
        return aiRepository.generateRecipeWithIngredients(description, baseServingSize)
            .map { resource ->
                when (resource) {
                    is Resource.Success -> {
                        try {
                            val generatedRecipe = resource.data
                            val resolved = resolveAndCreateIngredients(
                                generatedRecipe = generatedRecipe,
                                creatorUserId = creatorUserId
                            )
                            Resource.success(resolved)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to resolve ingredients", e)
                            Resource.failure(
                                message = e.message ?: "Failed to process ingredients."
                            )
                        }
                    }
                    is Resource.Failure -> resource
                    is Resource.Loading -> resource
                }
            }
    }

    /**
     * For backward compatibility — generates steps only (used by old callers).
     */
    fun execute(
        description: String,
        ingredients: List<com.souschef.model.recipe.ResolvedIngredient>
    ): Flow<Resource<List<RecipeStep>>> {
        return aiRepository.generateRecipeSteps(description, ingredients)
            .map { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val allGlobal = try {
                            ingredientRepository.getAllIngredients().first()
                        } catch (_: Exception) { emptyList() }
                        val resolved = resolveStepIngredientIds(resource.data, allGlobal)
                        Resource.success(resolved)
                    }
                    is Resource.Failure -> resource
                    is Resource.Loading -> resource
                }
            }
    }

    // ── Core resolution logic ──────────────────────────────────────────────

    /**
     * Takes the raw AI output and:
     * 1. Fetches all existing global ingredients.
     * 2. For each AI-extracted ingredient, fuzzy-matches or auto-creates.
     * 3. Builds RecipeIngredient list.
     * 4. Resolves step ingredientId fields.
     */
    private suspend fun resolveAndCreateIngredients(
        generatedRecipe: GeminiRecipeService.GeneratedRecipe,
        creatorUserId: String
    ): GeneratedRecipeData {
        // 1. Fetch current global ingredients
        val allGlobalIngredients: List<GlobalIngredient> = try {
            ingredientRepository.getAllIngredients().first()
        } catch (_: Exception) {
            emptyList()
        }

        // 2. Build resolution map: aiName → globalIngredientId
        //    Auto-create missing ingredients.
        val nameToIdMap = mutableMapOf<String, String>()
        val newlyCreated = mutableListOf<String>()
        // Keep a mutable copy to include newly created ingredients for subsequent matches
        val allGlobalsMutable = allGlobalIngredients.toMutableList()

        for (aiIngredient in generatedRecipe.ingredients) {
            val name = aiIngredient.name.trim()
            if (name.isBlank()) continue

            // Try fuzzy match first
            val match = IngredientMatcher.fuzzyMatch(name, allGlobalsMutable)
            if (match != null) {
                nameToIdMap[name] = match.ingredientId
                Log.d(TAG, "Matched '$name' → '${match.name}' (${match.ingredientId})")
            } else {
                // Auto-create in global library
                Log.d(TAG, "No match for '$name' — auto-creating global ingredient")
                val newGlobal = GlobalIngredient(
                    name = name,
                    defaultUnit = aiIngredient.unit,
                    createdByUserId = creatorUserId
                )
                val createResult = ingredientRepository.createIngredient(newGlobal)
                    .first { it !is Resource.Loading }

                if (createResult is Resource.Success) {
                    val newId = createResult.data
                    nameToIdMap[name] = newId
                    newlyCreated.add(name)
                    // Add to local mutable list so subsequent fuzzy matches can find it
                    allGlobalsMutable.add(newGlobal.copy(ingredientId = newId))
                    Log.d(TAG, "Created global ingredient '$name' with ID $newId")
                } else {
                    Log.w(TAG, "Failed to create ingredient '$name', skipping")
                }
            }
        }

        // 3. Build RecipeIngredient list
        val recipeIngredients = generatedRecipe.ingredients.mapNotNull { aiIngredient ->
            val globalId = nameToIdMap[aiIngredient.name.trim()] ?: return@mapNotNull null
            RecipeIngredient(
                globalIngredientId = globalId,
                quantity = aiIngredient.quantity,
                unit = aiIngredient.unit
            )
        }

        // 4. Resolve step ingredientId from raw name → globalIngredientId
        val resolvedSteps = generatedRecipe.steps.map { step ->
            val rawName = step.ingredientId // temporarily holds the AI name
            if (rawName.isNullOrBlank()) {
                step
            } else {
                val normalizedName = rawName.trim()
                // Try exact map first, then fuzzy match against our extended list
                val resolvedId = nameToIdMap[normalizedName]
                    ?: nameToIdMap.entries.find {
                        it.key.equals(normalizedName, ignoreCase = true)
                    }?.value
                    ?: IngredientMatcher.fuzzyMatch(normalizedName, allGlobalsMutable)
                        ?.ingredientId

                step.copy(ingredientId = resolvedId)
            }
        }

        return GeneratedRecipeData(
            steps = resolvedSteps,
            ingredients = recipeIngredients,
            newlyCreatedIngredients = newlyCreated
        )
    }

    /**
     * Legacy helper: resolves step ingredient names → IDs for old callers.
     */
    private fun resolveStepIngredientIds(
        steps: List<RecipeStep>,
        globalIngredients: List<GlobalIngredient>
    ): List<RecipeStep> {
        return steps.map { step ->
            val rawName = step.ingredientId
            if (rawName.isNullOrBlank()) {
                step
            } else {
                val match = IngredientMatcher.fuzzyMatch(rawName, globalIngredients)
                step.copy(ingredientId = match?.ingredientId)
            }
        }
    }
}
