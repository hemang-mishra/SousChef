package com.souschef.service.recipe

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.RecipeStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Raw Firestore operations for recipes, ingredients, and steps.
 * No business logic — just SDK calls. Repository wraps these in Resource<T>.
 */
class FirebaseRecipeService(
    private val firestore: FirebaseFirestore
) {
    private val recipesCollection = firestore.collection("recipes")
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
    suspend fun getRecipe(recipeId: String, source: Source = Source.DEFAULT): Recipe? {
        return recipesCollection.document(recipeId)
            .get(source).await()
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
     * Partial update on a single step document. Used to patch localizations
     * after AI translation without rewriting the whole sub-collection.
     */
    suspend fun updateStep(recipeId: String, stepId: String, updates: Map<String, Any>) {
        recipesCollection.document(recipeId)
            .collection("steps")
            .document(stepId)
            .update(updates).await()
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
    suspend fun getSteps(recipeId: String, source: Source = Source.DEFAULT): List<RecipeStep> {
        return recipesCollection.document(recipeId)
            .collection("steps")
            .orderBy("stepNumber")
            .get(source).await()
            .toObjects(RecipeStep::class.java)
    }

    /**
     * Fetches recipe document and steps in one call path for overview screens.
     */
    suspend fun getRecipeWithSteps(recipeId: String, source: Source = Source.DEFAULT): Pair<Recipe?, List<RecipeStep>> {
        val recipe = getRecipe(recipeId, source)
        val steps = getSteps(recipeId, source)
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

    // ── Phase 7: Fork & Save ──────────────────────────────────────────────────

    /**
     * Creates a forked copy of [original] for [newCreator]. The copy keeps the
     * same ingredient references and step content but resets ownership and
     * counters. The original recipe's [Recipe.forkCount] is incremented.
     *
     * @return the newly created recipe id.
     */
    suspend fun forkRecipe(
        original: Recipe,
        newCreatorId: String,
        newCreatorName: String,
        newCreatorIsVerifiedChef: Boolean
    ): String {
        // 1) Read original steps
        val originalSteps = getSteps(original.recipeId)

        // 2) Allocate ids
        val newDocRef = recipesCollection.document()
        val newRecipeId = newDocRef.id

        // 3) Build the forked recipe
        val now = com.google.firebase.Timestamp.now()
        val forkedRecipe = original.copy(
            recipeId = newRecipeId,
            creatorId = newCreatorId,
            creatorName = newCreatorName,
            isVerifiedChef = newCreatorIsVerifiedChef,
            isPublished = false,
            originalRecipeId = original.recipeId,
            originalRecipeTitle = original.title,
            forkCount = 0,
            savedByCount = 0,
            createdAt = now,
            updatedAt = now,
            stepCount = originalSteps.size,
            hasSteps = originalSteps.isNotEmpty()
        )

        // 4) Batch: write recipe, copy steps, increment original.forkCount
        val batch = firestore.batch()
        batch.set(newDocRef, forkedRecipe)

        val stepsCollection = newDocRef.collection("steps")
        originalSteps.forEach { step ->
            val stepDocRef = stepsCollection.document()
            batch.set(stepDocRef, step.copy(stepId = stepDocRef.id))
        }

        batch.update(
            recipesCollection.document(original.recipeId),
            mapOf("forkCount" to FieldValue.increment(1))
        )

        batch.commit().await()
        return newRecipeId
    }

    private fun savedRecipesCollection(userId: String) =
        firestore.collection("savedRecipes").document(userId).collection("recipes")

    /**
     * Adds a recipe to a user's saved/bookmarked list. Increments the parent
     * recipe's [Recipe.savedByCount].
     */
    suspend fun saveRecipe(userId: String, recipeId: String) {
        val batch = firestore.batch()
        batch.set(
            savedRecipesCollection(userId).document(recipeId),
            mapOf(
                "recipeId" to recipeId,
                "savedAt" to com.google.firebase.Timestamp.now()
            )
        )
        batch.update(
            recipesCollection.document(recipeId),
            mapOf("savedByCount" to FieldValue.increment(1))
        )
        batch.commit().await()
    }

    /**
     * Removes a recipe from a user's saved list. Decrements [Recipe.savedByCount].
     */
    suspend fun unsaveRecipe(userId: String, recipeId: String) {
        val batch = firestore.batch()
        batch.delete(savedRecipesCollection(userId).document(recipeId))
        batch.update(
            recipesCollection.document(recipeId),
            mapOf("savedByCount" to FieldValue.increment(-1))
        )
        batch.commit().await()
    }

    /** True if the user has saved this recipe. */
    suspend fun isRecipeSaved(userId: String, recipeId: String): Boolean {
        return savedRecipesCollection(userId).document(recipeId).get().await().exists()
    }

    /**
     * Real-time list of recipes saved by [userId]. The list is built by reading
     * the saved-recipes index then fetching each referenced recipe document.
     */
    fun getSavedRecipesFlow(userId: String): Flow<List<Recipe>> = callbackFlow {
        val registration = savedRecipesCollection(userId)
            .orderBy("savedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents?.map { it.id } ?: emptyList()
                if (ids.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                // Fetch each referenced recipe (sequentially is fine — typically small)
                ioScope.launch {
                    val recipes = ids.mapNotNull { id ->
                        try {
                            recipesCollection.document(id).get().await()
                                .toObject(Recipe::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    trySend(recipes)
                }
            }
        awaitClose { registration.remove() }
    }
}
