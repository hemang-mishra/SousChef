package com.souschef.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.ui.components.RecipeWithMeta
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.ui.screens.profile.translateText
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Home screen.
 * Loads the current user's recipes and supports search + tag filtering.
 */
class HomeViewModel(
    private val recipeRepository: RecipeRepository,
    private val userId: String,
    private val userName: String,
    private val preferredLanguageCode: String? = null
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
                // Fetch all recipes using the real-time listener
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
                    
                    // Trigger translation if language is set
                    if (!preferredLanguageCode.isNullOrBlank()) {
                        translateRecipes(preferredLanguageCode)
                    } else {
                        applyFilters()
                    }
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

    private fun translateRecipes(langCode: String) {
        viewModelScope.launch {
            // Translate UI Strings
            val greeting = translateText("Welcome back,", langCode) ?: "Welcome back,"
            val searchPlaceholder = translateText("Search your recipes…", langCode) ?: "Search your recipes…"
            val emptyTitle = translateText("No recipes yet", langCode) ?: "No recipes yet"
            val emptySubtitle = translateText("Create your first recipe and let AI generate the cooking steps!", langCode) ?: "Create your first recipe and let AI generate the cooking steps!"

            _uiState.update { state ->
                state.copy(
                    translatedGreeting = greeting,
                    translatedSearchPlaceholder = searchPlaceholder,
                    translatedEmptyTitle = emptyTitle,
                    translatedEmptySubtitle = emptySubtitle
                )
            }

            // Translate Recipes
            val currentRecipes = _uiState.value.recipes
            val translatedRecipes = currentRecipes.map { rwm ->
                val translatedTitle = translateText(rwm.recipe.title, langCode) ?: rwm.recipe.title
                
                // Translate the description as well
                val translatedDescription = translateText(rwm.recipe.description, langCode) ?: rwm.recipe.description

                val translatedRecipe = rwm.recipe.copy(
                    title = translatedTitle,
                    description = translatedDescription
                )
                rwm.copy(recipe = translatedRecipe)
            }
            
            _uiState.update { it.copy(recipes = translatedRecipes) }
            applyFilters()
        }
    }
}
