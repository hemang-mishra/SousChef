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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Recipe Overview screen.
 *
 * Loads recipe + steps from [RecipeRepository], resolves each [RecipeIngredient]
 * by joining with [GlobalIngredient] from [IngredientRepository], then delegates
 * quantity calculations to [RecipeCalculationUseCase].
 */
class RecipeOverviewViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val calculationUseCase: RecipeCalculationUseCase,
    private val recipeId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeOverviewUiState())
    val uiState: StateFlow<RecipeOverviewUiState> = _uiState.asStateFlow()

    init {
        loadRecipe()
    }

    // ── Data Loading ────────────────────────────────────────

    private fun loadRecipe() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val recipeResult = recipeRepository.getRecipeWithSteps(recipeId)
                .first { it !is Resource.Loading }

            if (recipeResult is Resource.Failure) {
                _uiState.update {
                    it.copy(isLoading = false, error = recipeResult.message ?: "Failed to load recipe")
                }
                return@launch
            }

            val payload = (recipeResult as? Resource.Success)?.data
            val recipe = payload?.first
            val steps = payload?.second ?: emptyList()

            if (recipe == null) {
                _uiState.update { it.copy(isLoading = false, error = "Recipe not found") }
                return@launch
            }

            val ingredientIds = recipe.ingredients.map { it.globalIngredientId }.distinct()
            if (ingredientIds.isEmpty()) {
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
                return@launch
            }

            val ingredientResult = ingredientRepository.getIngredientsByIds(ingredientIds)
                .first { it !is Resource.Loading }

            if (ingredientResult is Resource.Failure) {
                _uiState.update {
                    it.copy(isLoading = false, error = ingredientResult.message ?: "Failed to load ingredients")
                }
                return@launch
            }

            val globalMap = ((ingredientResult as? Resource.Success)?.data ?: emptyList())
                .associateBy { it.ingredientId }
            val resolved = recipe.ingredients.mapNotNull { ri ->
                globalMap[ri.globalIngredientId]?.let { gi ->
                    ResolvedIngredient.from(ri, gi)
                }
            }

            val adjusted = calculationUseCase.calculate(
                ingredients = resolved,
                baseServingSize = recipe.baseServingSize,
                selectedServings = recipe.baseServingSize,
                spiceLevel = 0f,
                saltLevel = 0f,
                sweetnessLevel = 0f
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
}

