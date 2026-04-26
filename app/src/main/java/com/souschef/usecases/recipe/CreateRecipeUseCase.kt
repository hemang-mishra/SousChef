package com.souschef.usecases.recipe

import com.google.firebase.Timestamp
import com.souschef.model.auth.UserProfile
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.recipe.Recipe
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Creates a recipe with metadata and ingredients.
 * - Calculates perPersonQuantity for each RecipeIngredient before saving.
 * - Sets creator info from the current user.
 * - Returns the new recipeId.
 */
class CreateRecipeUseCase(
    private val recipeRepository: RecipeRepository
) {
    fun execute(
        title: String,
        description: String,
        baseServingSize: Int,
        minServingSize: Int?,
        maxServingSize: Int?,
        tags: List<String>,
        ingredients: List<RecipeIngredient>,
        currentUser: UserProfile,
        publish: Boolean = false,
        coverImageUrl: String? = null
    ): Flow<Resource<String>> = flow {
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

        val recipe = Recipe(
            title = title.trim(),
            description = description.trim(),
            creatorId = currentUser.uid,
            creatorName = currentUser.displayName,
            isVerifiedChef = currentUser.isVerifiedChef,
            baseServingSize = baseServingSize,
            minServingSize = minServingSize,
            maxServingSize = maxServingSize,
            coverImageUrl = coverImageUrl,
            isPublished = publish,
            tags = tags,
            ingredients = processedIngredients,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        recipeRepository.createRecipe(recipe).collect { result ->
            emit(result)
        }
    }
}
