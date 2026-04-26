package com.souschef.ui.screens.savedrecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.ui.components.RecipeWithMeta
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.ui.screens.home.HomeUiState
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 * Loads the current user's recipes and supports search + tag filtering.
 */

class SavedRecipesViewModel(
    private val recipeRepository: RecipeRepository,
    private val userId: String,
    private val userName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(userName = userName))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            try {
                recipeRepository.getRecipesByCreator(userId).collect { recipes ->
                    // For each recipe, load step count
                    val recipesWithMeta = recipes.map { recipe ->
                        var stepCount = 0
                        try {
                            recipeRepository.getSteps(recipe.recipeId).collect { result ->
                                if (result is Resource.Success) {
                                    stepCount = result.data.size
                                }
                            }
                        } catch (_: Exception) {
                            // Non-fatal — show 0 steps
                        }
                        RecipeWithMeta(
                            recipe = recipe,
                            stepCount = stepCount,
                            hasSteps = stepCount > 0
                        )
                    }

                    _uiState.update { state ->
                        state.copy(
                            recipes = recipesWithMeta,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load recipes")
                }
            }
        }
    }
}