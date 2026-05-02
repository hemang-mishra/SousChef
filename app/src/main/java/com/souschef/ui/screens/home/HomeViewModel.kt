package com.souschef.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.ui.components.RecipeWithMeta
import com.souschef.repository.recipe.RecipeListCache
import com.souschef.repository.recipe.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 * Loads the current user's recipes and supports search + tag filtering.
 *
 * Registered as `single` in Koin so the loaded list survives navigation between
 * tabs — combined with [RecipeListCache] for instant first paint after sign-in
 * or a cold-start.
 */
class HomeViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeListCache: RecipeListCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var currentUserId: String? = null

    /**
     * Bind the ViewModel to the currently signed-in user. Safe to call on every
     * Home composition — re-binding is a no-op when the user hasn't changed.
     */
    fun bind(userId: String, userName: String) {
        if (userId == currentUserId && userName == _uiState.value.userName) {
            return
        }
        currentUserId = userId
        _uiState.update { it.copy(userName = userName) }
        loadRecipes()
    }

    /** Clear cached state when signing out. */
    fun reset() {
        loadJob?.cancel()
        currentUserId = null
        _uiState.value = HomeUiState()
    }

    private fun loadRecipes() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            // Show shimmer only if there's nothing cached yet
            val cached = recipeListCache.allRecipes.value
            _uiState.update { it.copy(isLoading = cached.isNullOrEmpty()) }

            try {
                recipeRepository.getAllRecipes().collect { recipes ->
                    // Filter down to only those with steps
                    val recipesWithSteps = recipes.filter { it.hasSteps }
                    
                    val recipesWithMeta = recipesWithSteps.map { recipe ->
                        RecipeWithMeta(
                            recipe = recipe,
                            stepCount = recipe.stepCount,
                            hasSteps = recipe.hasSteps
                        )
                    }

                    _uiState.update { state ->
                        state.copy(
                            recipes = recipesWithMeta,
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    applyFilters()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load recipes")
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onTagSelected(tag: String?) {
        _uiState.update { it.copy(selectedTag = tag) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        var filtered = state.recipes

        // Search filter
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                val title = it.recipe.title
                title.contains(state.searchQuery, ignoreCase = true)
            }
        }

        // Tag filter
        if (state.selectedTag != null) {
            filtered = filtered.filter { rwm ->
                rwm.recipe.tags.contains(state.selectedTag)
            }
        }

        _uiState.update { it.copy(filteredRecipes = filtered) }
    }

}
