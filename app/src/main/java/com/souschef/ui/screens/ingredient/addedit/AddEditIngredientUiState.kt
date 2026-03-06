package com.souschef.ui.screens.ingredient.addedit

/**
 * UI state for the Add/Edit Ingredient screen.
 */
data class AddEditIngredientUiState(
    val isEditMode: Boolean = false,
    val ingredientId: String? = null,
    val name: String = "",
    val defaultUnit: String = "grams",
    val isDispensable: Boolean = false,
    val spiceIntensityValue: Double = 0.0,
    val sweetnessValue: Double = 0.0,
    val saltnessValue: Double = 0.0,
    val imageUrl: String? = null,
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val savedIngredientId: String? = null,
    val generalError: String? = null
)

