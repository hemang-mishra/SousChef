package com.souschef.ui.screens.savedrecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.ui.components.RecipeWithMeta
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.ui.screens.home.HomeUiState
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
            _uiState.update { it.copy(isLoading = it.recipes.isEmpty()) }

            try {
                recipeRepository.getRecipesByCreator(userId).collect { recipes ->
                    // Use the denormalized stepCount/hasSteps fields on the
                    // Recipe document; avoids one Firestore query per recipe.
                    val recipesWithMeta = recipes.map { recipe ->
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
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load recipes")
                }
            }
        }
    }
}