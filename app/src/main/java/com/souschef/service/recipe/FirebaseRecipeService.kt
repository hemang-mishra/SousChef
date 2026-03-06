package com.souschef.service.recipe

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.RecipeStep
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Raw Firestore operations for recipes, ingredients, and steps.
 * No business logic — just SDK calls. Repository wraps these in Resource<T>.
 */
class FirebaseRecipeService(
    private val firestore: FirebaseFirestore
) {
    private val recipesCollection = firestore.collection("recipes")

    /**
     * Creates a new recipe document. Returns the auto-generated document ID.
     */
    suspend fun createRecipe(recipe: Recipe): String {
        val docRef = recipesCollection.document()
        val recipeWithId = recipe.copy(recipeId = docRef.id)
        docRef.set(recipeWithId).await()
        return docRef.id
    }

    /**
     * Partial update on a recipe document.
     */
    suspend fun updateRecipe(recipeId: String, updates: Map<String, Any>) {
        recipesCollection.document(recipeId).update(updates).await()
    }

    /**
     * Fetches a single recipe by ID.
     */
    suspend fun getRecipe(recipeId: String): Recipe? {
        return recipesCollection.document(recipeId)
            .get().await()
            .toObject(Recipe::class.java)
    }

    /**
     * Real-time Flow of recipes created by a specific user, ordered by creation date.
     */
    fun getRecipesByCreatorFlow(creatorId: String): Flow<List<Recipe>> = callbackFlow {
        val query = recipesCollection
            .whereEqualTo("creatorId", creatorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val recipes = snapshot?.toObjects(Recipe::class.java) ?: emptyList()
            trySend(recipes)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Adds a step to the recipe's sub-collection.
     */
    suspend fun addStep(recipeId: String, step: RecipeStep) {
        val stepsCollection = recipesCollection.document(recipeId).collection("steps")
        val docRef = stepsCollection.document()
        val stepWithId = step.copy(stepId = docRef.id)
        docRef.set(stepWithId).await()
    }

    /**
     * Fetches all steps for a recipe, ordered by stepNumber.
     */
    suspend fun getSteps(recipeId: String): List<RecipeStep> {
        return recipesCollection.document(recipeId)
            .collection("steps")
            .orderBy("stepNumber")
            .get().await()
            .toObjects(RecipeStep::class.java)
    }
}

