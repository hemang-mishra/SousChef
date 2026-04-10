package com.souschef.ui.screens.home

import com.souschef.ui.components.RecipeWithMeta

/**
 * UI state for the Home screen.
 */
data class HomeUiState(
    /** All recipes created by the current user. */
    val recipes: List<RecipeWithMeta> = emptyList(),
    /** Filtered recipes (by search + tag). */
    val filteredRecipes: List<RecipeWithMeta> = emptyList(),
    /** Current search query. */
    val searchQuery: String = "",
    /** Currently selected tag filter (null = "All"). */
    val selectedTag: String? = null,
    /** True while loading recipes. */
    val isLoading: Boolean = true,
    /** User's display name. */
    val userName: String = "",
    /** Error message, null if none. */
    val error: String? = null
)
