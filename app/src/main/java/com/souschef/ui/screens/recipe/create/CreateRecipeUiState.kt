package com.souschef.ui.screens.recipe.create

import android.net.Uri
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.recipe.RecipeTag

/**
 * UI state for the multi-step Create Recipe wizard.
 */
data class CreateRecipeUiState(
    // Wizard navigation
    val currentStep: Int = 0,           // 0 = Details, 1 = Ingredients, 2 = Review
    val totalSteps: Int = 3,

    // Step 1: Recipe Details
    val title: String = "",
    val description: String = "",
    val baseServingSize: Int = 4,
    val minServingSize: Int? = null,
    val maxServingSize: Int? = null,
    val useMinServing: Boolean = false,
    val useMaxServing: Boolean = false,
    val selectedTags: List<RecipeTag> = emptyList(),
    val coverImageUri: Uri? = null,

    // Step 2: Ingredients (references to global library)
    val ingredients: List<RecipeIngredient> = emptyList(),
    val globalIngredients: List<GlobalIngredient> = emptyList(),

    // Validation
    val titleError: String? = null,
    val ingredientError: String? = null,

    // Loading / result
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isSaved: Boolean = false,
    val savedRecipeId: String? = null,
    val generalError: String? = null
) {
    val stepLabels: List<String> = listOf("Details", "Ingredients", "Review")
    val canGoNext: Boolean get() = currentStep < totalSteps - 1
    val canGoBack: Boolean get() = currentStep > 0
}
