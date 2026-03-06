package com.souschef.ui.screens.ingredient.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.usecases.ingredient.GetIngredientsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Ingredient Library screen.
 * Loads all global ingredients and provides client-side search filtering.
 */
class IngredientLibraryViewModel(
    private val getIngredientsUseCase: GetIngredientsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _allIngredients = MutableStateFlow<List<com.souschef.model.ingredient.GlobalIngredient>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<IngredientLibraryUiState> = combine(
        _allIngredients, _searchQuery, _isLoading, _error
    ) { all, query, loading, error ->
        val filtered = if (query.isBlank()) all
        else all.filter { it.name.contains(query, ignoreCase = true) }
        IngredientLibraryUiState(
            allIngredients = all,
            filteredIngredients = filtered,
            searchQuery = query,
            isLoading = loading,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IngredientLibraryUiState())

    init {
        loadIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getIngredientsUseCase.execute().collect { ingredients ->
                    _allIngredients.value = ingredients
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load ingredients"
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}

