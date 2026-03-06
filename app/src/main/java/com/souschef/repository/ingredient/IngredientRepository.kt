package com.souschef.repository.ingredient

import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Ingredient repository interface.
 * All one-shot methods return Flow<Resource<T>> — emit Loading first, then Success or Failure.
 */
interface IngredientRepository {
    fun createIngredient(ingredient: GlobalIngredient): Flow<Resource<String>>
    fun updateIngredient(ingredientId: String, updates: Map<String, Any>): Flow<Resource<Unit>>
    fun deleteIngredient(ingredientId: String): Flow<Resource<Unit>>
    fun getIngredient(ingredientId: String): Flow<Resource<GlobalIngredient>>
    fun getAllIngredients(): Flow<List<GlobalIngredient>>
    fun getIngredientsByIds(ids: List<String>): Flow<Resource<List<GlobalIngredient>>>
    fun existsByName(name: String): Flow<Resource<Boolean>>
}

