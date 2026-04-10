package com.souschef.usecases.recipe

import com.souschef.model.recipe.RecipeStep
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Saves generated/edited recipe steps to Firestore.
 *
 * Clears any existing steps for the recipe, then batch-writes the
 * provided steps with sequential stepNumbers.
 */
class SaveRecipeStepsUseCase(
    private val recipeRepository: RecipeRepository
) {

    /**
     * @param recipeId The recipe to save steps for.
     * @param steps The list of steps to save (ordering is preserved).
     * @return Flow emitting Loading → Success or Failure.
     */
    fun execute(
        recipeId: String,
        steps: List<RecipeStep>
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())

        // Re-number steps sequentially
        val numberedSteps = steps.mapIndexed { index, step ->
            step.copy(
                stepNumber = index + 1,
                stepId = "" // clear any existing IDs — Firestore will assign new ones
            )
        }

        // Delete existing steps first
        recipeRepository.deleteAllSteps(recipeId).collect { result ->
            when (result) {
                is Resource.Failure -> {
                    emit(Resource.failure(result.error, result.message))
                    return@collect
                }
                is Resource.Loading -> { /* wait */ }
                is Resource.Success -> {
                    // Now batch-add the new steps
                    recipeRepository.batchAddSteps(recipeId, numberedSteps).collect { addResult ->
                        when (addResult) {
                            is Resource.Failure -> emit(Resource.failure(addResult.error, addResult.message))
                            is Resource.Loading -> { /* wait */ }
                            is Resource.Success -> emit(Resource.success(Unit))
                        }
                    }
                }
            }
        }
    }
}
