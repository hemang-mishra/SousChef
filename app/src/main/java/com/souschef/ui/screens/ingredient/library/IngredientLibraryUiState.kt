package com.souschef.ui.screens.ingredient.library

import com.souschef.model.ingredient.GlobalIngredient

/**
 * UI state for the Ingredient Library screen.
 */
data class IngredientLibraryUiState(
    val allIngredients: List<GlobalIngredient> = emptyList(),
    val filteredIngredients: List<GlobalIngredient> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

