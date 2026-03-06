package com.souschef.repository.ingredient

import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.service.ingredient.FirebaseIngredientService
import com.souschef.util.Resource
import com.souschef.util.safeFirestoreCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Firebase-backed implementation of [IngredientRepository].
 * Wraps service calls in safeFirestoreCall for error mapping.
 */
class FirestoreIngredientRepository(
    private val service: FirebaseIngredientService
) : IngredientRepository {

    override fun createIngredient(ingredient: GlobalIngredient): Flow<Resource<String>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.createIngredient(ingredient) }
        emit(result)
    }

    override fun updateIngredient(ingredientId: String, updates: Map<String, Any>): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.updateIngredient(ingredientId, updates) }
        emit(result)
    }

    override fun deleteIngredient(ingredientId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.deleteIngredient(ingredientId) }
        emit(result)
    }

    override fun getIngredient(ingredientId: String): Flow<Resource<GlobalIngredient>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.getIngredient(ingredientId) }
        when (result) {
            is Resource.Success -> {
                if (result.data != null) emit(Resource.success(result.data))
                else emit(Resource.failure(message = "Ingredient not found"))
            }
            is Resource.Failure -> emit(Resource.failure(result.error, result.message))
            is Resource.Loading -> { /* already emitted */ }
        }
    }

    override fun getAllIngredients(): Flow<List<GlobalIngredient>> {
        return service.getAllIngredientsFlow()
    }

    override fun getIngredientsByIds(ids: List<String>): Flow<Resource<List<GlobalIngredient>>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.getIngredientsByIds(ids) }
        emit(result)
    }

    override fun existsByName(name: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.existsByName(name) }
        emit(result)
    }
}

