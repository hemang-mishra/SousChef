package com.souschef.service.recipe

import com.google.firebase.firestore.FieldValue
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
     * Real-time Flow of all recipes, ordered by creation date.
     */
    fun getAllRecipesFlow(): Flow<List<Recipe>> = callbackFlow {
        val query = recipesCollection
//            .whereEqualTo("hasSteps",true)
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
     * Adds a step to the recipe's sub-collection and updates parent recipe metadata.
     */
    suspend fun addStep(recipeId: String, step: RecipeStep) {
        val stepsCollection = recipesCollection.document(recipeId).collection("steps")
        val docRef = stepsCollection.document()
        val stepWithId = step.copy(stepId = docRef.id)
        
        val batch = firestore.batch()
        batch.set(docRef, stepWithId)
        batch.update(recipesCollection.document(recipeId), mapOf(
            "hasSteps" to true,
            "stepCount" to FieldValue.increment(1)
        ))
        batch.commit().await()
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

    /**
     * Fetches recipe document and steps in one call path for overview screens.
     */
    suspend fun getRecipeWithSteps(recipeId: String): Pair<Recipe?, List<RecipeStep>> {
        val recipe = getRecipe(recipeId)
        val steps = getSteps(recipeId)
        return recipe to steps
    }

    /**
     * Deletes all steps in a recipe's steps sub-collection and updates parent recipe metadata.
     * Used before re-generating steps via AI to avoid duplicates.
     */
    suspend fun deleteAllSteps(recipeId: String) {
        val stepsCollection = recipesCollection.document(recipeId).collection("steps")
        val existingSteps = stepsCollection.get().await()
        val batch = firestore.batch()
        existingSteps.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.update(recipesCollection.document(recipeId), mapOf(
            "hasSteps" to false,
            "stepCount" to 0
        ))
        batch.commit().await()
    }

    /**
     * Writes multiple steps to a recipe's sub-collection in a single batch and updates parent recipe metadata.
     * Each step gets an auto-generated document ID and its stepNumber is preserved.
     */
    suspend fun batchAddSteps(recipeId: String, steps: List<RecipeStep>) {
        val stepsCollection = recipesCollection.document(recipeId).collection("steps")
        val batch = firestore.batch()
        steps.forEach { step ->
            val docRef = stepsCollection.document()
            val stepWithId = step.copy(stepId = docRef.id)
            batch.set(docRef, stepWithId)
        }
        if (steps.isNotEmpty()) {
            batch.update(recipesCollection.document(recipeId), mapOf(
                "hasSteps" to true,
                "stepCount" to FieldValue.increment(steps.size.toLong())
            ))
        }
        batch.commit().await()
    }

    /**
     * Deletes a recipe document and completely drops its steps sub-collection.
     */
    suspend fun deleteRecipe(recipeId: String) {
        // 1. Delete all steps inside the sub-collection
        deleteAllSteps(recipeId)

        // 2. Delete the recipe document itself
        recipesCollection.document(recipeId).delete().await()
    }
}
