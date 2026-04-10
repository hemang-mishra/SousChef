package com.souschef.usecases.recipe

import com.google.firebase.Timestamp
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Updates an existing recipe with new metadata and ingredients.
 * - Recalculates perPersonQuantity.
 * - Updates the timestamp.
 */
class UpdateRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {
    fun execute(
        recipeId: String,
        title: String,
        description: String,
        baseServingSize: Int,
        minServingSize: Int?,
        maxServingSize: Int?,
        tags: List<String>,
        ingredients: List<RecipeIngredient>,
        publish: Boolean,
        coverImageUrl: String?
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())

        // Calculate perPersonQuantity for each ingredient
        val processedIngredients = ingredients.map { ingredient ->
            ingredient.copy(
                perPersonQuantity = if (baseServingSize > 0) {
                    ingredient.quantity / baseServingSize
                } else {
                    ingredient.quantity
                }
            )
        }

        val updates = mutableMapOf<String, Any>(
            "title" to title.trim(),
            "description" to description.trim(),
            "baseServingSize" to baseServingSize,
            "isPublished" to publish,
            "tags" to tags,
            "ingredients" to processedIngredients,
            "updatedAt" to Timestamp.now()
        )

        // Only add nullable fields if they are explicitly changing or being cleared
        // We can just set them since Firestore handles null values.
        updates["minServingSize"] = minServingSize ?: com.google.firebase.firestore.FieldValue.delete()
        updates["maxServingSize"] = maxServingSize ?: com.google.firebase.firestore.FieldValue.delete()
        
        if (coverImageUrl != null) {
             updates["coverImageUrl"] = coverImageUrl
        }

        recipeRepository.updateRecipe(recipeId, updates).collect { result ->
            emit(result)
        }
    }
}
