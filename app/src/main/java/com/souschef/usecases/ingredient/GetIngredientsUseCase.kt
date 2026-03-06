package com.souschef.usecases.ingredient

import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Returns a Flow of all global ingredients from the library.
 * Supports optional client-side name filtering for instant search.
 */
class GetIngredientsUseCase(
    private val ingredientRepository: IngredientRepository
) {
    /**
     * Returns a real-time Flow of all ingredients.
     */
    fun execute(): Flow<List<GlobalIngredient>> {
        return ingredientRepository.getAllIngredients()
    }

    /**
     * Fetches ingredients by their IDs for resolving recipe references.
     */
    fun executeByIds(ids: List<String>): Flow<Resource<List<GlobalIngredient>>> {
        return ingredientRepository.getIngredientsByIds(ids)
    }
}

