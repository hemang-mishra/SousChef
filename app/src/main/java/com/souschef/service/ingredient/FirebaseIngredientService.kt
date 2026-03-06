package com.souschef.service.ingredient

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.souschef.model.ingredient.GlobalIngredient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Raw Firestore operations for the global ingredient library.
 * No business logic — just SDK calls. Repository wraps these in Resource<T>.
 */
class FirebaseIngredientService(
    private val firestore: FirebaseFirestore
) {
    private val ingredientsCollection = firestore.collection("ingredients")

    /**
     * Creates a new ingredient document. Returns the auto-generated document ID.
     */
    suspend fun createIngredient(ingredient: GlobalIngredient): String {
        val docRef = ingredientsCollection.document()
        val ingredientWithId = ingredient.copy(ingredientId = docRef.id)
        docRef.set(ingredientWithId).await()
        return docRef.id
    }

    /**
     * Partial update on an ingredient document.
     */
    suspend fun updateIngredient(ingredientId: String, updates: Map<String, Any>) {
        ingredientsCollection.document(ingredientId).update(updates).await()
    }

    /**
     * Deletes an ingredient document.
     */
    suspend fun deleteIngredient(ingredientId: String) {
        ingredientsCollection.document(ingredientId).delete().await()
    }

    /**
     * Fetches a single ingredient by ID.
     */
    suspend fun getIngredient(ingredientId: String): GlobalIngredient? {
        return ingredientsCollection.document(ingredientId)
            .get().await()
            .toObject(GlobalIngredient::class.java)
    }

    /**
     * Real-time Flow of all ingredients, ordered by name.
     */
    fun getAllIngredientsFlow(): Flow<List<GlobalIngredient>> = callbackFlow {
        val query = ingredientsCollection.orderBy("name", Query.Direction.ASCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val ingredients = snapshot?.toObjects(GlobalIngredient::class.java) ?: emptyList()
            trySend(ingredients)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Fetches multiple ingredients by their IDs.
     * Useful for resolving RecipeIngredient references.
     */
    suspend fun getIngredientsByIds(ids: List<String>): List<GlobalIngredient> {
        if (ids.isEmpty()) return emptyList()
        // Firestore whereIn supports max 30 items per query
        return ids.chunked(30).flatMap { chunk ->
            ingredientsCollection
                .whereIn("ingredientId", chunk)
                .get().await()
                .toObjects(GlobalIngredient::class.java)
        }
    }

    /**
     * Checks if an ingredient with the given name already exists (case-insensitive).
     * Returns true if a duplicate exists.
     */
    suspend fun existsByName(name: String): Boolean {
        // Firestore doesn't support case-insensitive queries natively,
        // so we store and query using lowercase comparison
        val results = ingredientsCollection
            .whereEqualTo("name", name.trim())
            .limit(1)
            .get().await()
        return !results.isEmpty
    }
}

