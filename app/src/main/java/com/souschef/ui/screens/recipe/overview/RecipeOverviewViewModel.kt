package com.souschef.ui.screens.recipe.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.usecases.recipe.RecipeCalculationUseCase
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Recipe Overview screen.
 *
 * Loads recipe + steps from [RecipeRepository], resolves each [RecipeIngredient]
 * by joining with [GlobalIngredient] from [IngredientRepository], then delegates
 * quantity calculations to [RecipeCalculationUseCase].
 */
import com.souschef.usecases.recipe.DeleteRecipeUseCase

class RecipeOverviewViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val calculationUseCase: RecipeCalculationUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val recipeId: String,
    private val currentUserId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeOverviewUiState())
    val uiState: StateFlow<RecipeOverviewUiState> = _uiState.asStateFlow()

    init {
        loadRecipe()
    }

    // ── Data Loading ────────────────────────────────────────

    private fun loadRecipe() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null, currentUserId = currentUserId) }

            // 1. Fetch recipe
            recipeRepository.getRecipe(recipeId).collect { recipeResult ->
                when (recipeResult) {
                    is Resource.Loading -> { /* keep spinner */ }
                    is Resource.Failure -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = recipeResult.message ?: "Failed to load recipe")
                        }
                        return@collect
                    }
                    is Resource.Success -> {
                        val recipe = recipeResult.data!!

                        // 2. Fetch steps
                        var steps = emptyList<com.souschef.model.recipe.RecipeStep>()
                        recipeRepository.getSteps(recipeId).collect { stepsResult ->
                            if (stepsResult is Resource.Success) {
                                steps = stepsResult.data ?: emptyList()
                            }
                        }

                        // 3. Resolve ingredients
                        // Older recipes may lack embedded ingredients. Fallback to extracting from steps.
                        val embeddedIds = recipe.ingredients.map { it.globalIngredientId }.toSet()
                        val stepIds = steps.mapNotNull { it.effectiveIngredientId }.toSet()
                        val allIngredientIds = (embeddedIds + stepIds).filter { it.isNotBlank() }

                        if (allIngredientIds.isEmpty()) {
                            _uiState.update {
                                it.copy(
                                    recipe = recipe,
                                    steps = steps,
                                    selectedServings = recipe.baseServingSize,
                                    resolvedIngredients = emptyList(),
                                    adjustedIngredients = emptyList(),
                                    isLoading = false
                                )
                            }
                            return@collect
                        }

                        ingredientRepository.getIngredientsByIds(allIngredientIds.toList()).collect { ingResult ->
                            when (ingResult) {
                                is Resource.Loading -> { /* wait */ }
                                is Resource.Failure -> {
                                    _uiState.update {
                                        it.copy(isLoading = false, error = "Failed to load ingredients")
                                    }
                                }
                                is Resource.Success -> {
                                    val globalMap = (ingResult.data ?: emptyList()).associateBy { it.ingredientId }
                                    
                                    // Map embedded ingredients
                                    val resolvedEmbedded = recipe.ingredients.mapNotNull { ri ->
                                        val gi = globalMap[ri.globalIngredientId]
                                        if (gi != null) ResolvedIngredient.from(ri, gi) else null
                                    }

                                    // Map missing legacy ingredients from steps
                                    val fallbackResolved = stepIds
                                        .filter { id -> resolvedEmbedded.none { it.globalIngredientId == id } }
                                        .map { id ->
                                            val gi = globalMap[id] ?: ingResult.data?.find { it.name.equals(id, ignoreCase = true) }
                                            if (gi != null) {
                                                ResolvedIngredient.from(com.souschef.model.recipe.RecipeIngredient(globalIngredientId = gi.ingredientId, quantity = 1.0, unit = "unit"), gi)
                                            } else {
                                                // Ultimate fallback for unresolved string names
                                                ResolvedIngredient(globalIngredientId = id, name = id, quantity = 1.0, unit = "unit")
                                            }
                                        }

                                    val resolved = resolvedEmbedded + fallbackResolved
                                    val adjusted = calculationUseCase.calculate(
                                        resolved, recipe.baseServingSize, recipe.baseServingSize
                                    )
                                    _uiState.update {
                                        it.copy(
                                            recipe = recipe,
                                            steps = steps,
                                            selectedServings = recipe.baseServingSize,
                                            resolvedIngredients = resolved,
                                            adjustedIngredients = adjusted,
                                            isLoading = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── User Events ─────────────────────────────────────────

    fun onServingsChanged(servings: Int) {
        _uiState.update { it.copy(selectedServings = servings) }
        recalculate()
    }

    fun onSpiceLevelChanged(level: Float) {
        _uiState.update { it.copy(spiceLevel = level) }
        recalculate()
    }

    fun onSaltLevelChanged(level: Float) {
        _uiState.update { it.copy(saltLevel = level) }
        recalculate()
    }

    fun onSweetnessLevelChanged(level: Float) {
        _uiState.update { it.copy(sweetnessLevel = level) }
        recalculate()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ── Recalculation ───────────────────────────────────────

    private fun recalculate() {
        val state = _uiState.value
        val recipe = state.recipe ?: return
        val adjusted = calculationUseCase.calculate(
            ingredients = state.resolvedIngredients,
            baseServingSize = recipe.baseServingSize,
            selectedServings = state.selectedServings,
            spiceLevel = state.spiceLevel,
            saltLevel = state.saltLevel,
            sweetnessLevel = state.sweetnessLevel
        )
        _uiState.update { it.copy(adjustedIngredients = adjusted) }
    }
    // ── Actions ─────────────────────────────────────────────

    fun onDeleteRecipe() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            deleteRecipeUseCase.execute(recipeId).collect { result ->
                when (result) {
                    is Resource.Loading -> { }
                    is Resource.Failure -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to delete recipe") }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                    }
                }
            }
        }
    }
}

