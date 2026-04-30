package com.souschef.ui.screens.recipe.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.model.recipe.SupportedLanguages
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.usecases.recipe.RecipeCalculationUseCase
import com.souschef.usecases.translation.TranslateRecipeUseCase
import com.souschef.util.LanguageManager
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
import com.souschef.usecases.recipe.DeleteRecipeUseCase

class RecipeOverviewViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val calculationUseCase: RecipeCalculationUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val translateRecipeUseCase: TranslateRecipeUseCase,
    private val languageManager: LanguageManager,
    private val recipeId: String,
    private val currentUserId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeOverviewUiState())
    val uiState: StateFlow<RecipeOverviewUiState> = _uiState.asStateFlow()

    init {
        observeLanguage()
        loadRecipe()
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            languageManager.currentLanguage.collect { lang ->
                _uiState.update { it.copy(language = lang) }
                ensureRecipeTranslated(lang)
            }
        }
    }

    /**
     * One-tap language switcher used by [com.souschef.ui.components.LanguageToggle].
     */
    fun setLanguage(code: String) {
        languageManager.setLanguage(code)
    }

    /**
     * Forces a fresh AI translation for the active (non-English) language,
     * overwriting any existing localizations.
     */
    fun retranslate() {
        val target = _uiState.value.language
        if (target == SupportedLanguages.ENGLISH) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isTranslating = true) }
            try {
                translateRecipeUseCase.execute(recipeId, target, force = true)
                    .first { it !is Resource.Loading }
                loadRecipe()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.w("RecipeOverviewVM", "Retranslate failed: ${e.message}")
            } finally {
                _uiState.update { it.copy(isTranslating = false) }
            }
        }
    }

    /**
     * Lazily translates the current recipe to [targetLanguage] if needed.
     * No-op for English (canonical) and for languages already in
     * `recipe.translatedLanguages`.
     */
    private fun ensureRecipeTranslated(targetLanguage: String) {
        if (targetLanguage == SupportedLanguages.ENGLISH) return
        val recipe = _uiState.value.recipe ?: return
        if (targetLanguage in recipe.translatedLanguages) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isTranslating = true) }
            try {
                translateRecipeUseCase.execute(recipeId, targetLanguage)
                    .first { it !is Resource.Loading }
                // Reload to pick up the new localizations from Firestore.
                loadRecipe()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.w("RecipeOverviewVM", "Translation failed: ${e.message}")
            } finally {
                _uiState.update { it.copy(isTranslating = false) }
            }
        }
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
                        val recipe = recipeResult.data

                        // 2. Fetch steps
                        var steps = emptyList<com.souschef.model.recipe.RecipeStep>()
                        recipeRepository.getSteps(recipeId).collect { stepsResult ->
                                if (stepsResult is Resource.Success) {
                                    steps = stepsResult.data
                                }
                        }

                        // 3. Resolve ingredients by globalIngredientId for display/calculation
                        val recipeIngredients = recipe.ingredients.filter { it.globalIngredientId.isNotBlank() }
                        val ingredientIds = recipeIngredients.map { it.globalIngredientId }.distinct()

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
                            return@collect
                        }

                        ingredientRepository.getIngredientsByIds(ingredientIds).collect { ingResult ->
                            when (ingResult) {
                                is Resource.Loading -> { /* wait */ }
                                is Resource.Failure -> {
                                    _uiState.update {
                                        it.copy(isLoading = false, error = "Failed to load ingredients")
                                    }
                                }
                                is Resource.Success -> {
                                    val globalMap = ingResult.data.associateBy { it.ingredientId }

                                    val resolved = recipeIngredients.map { recipeIngredient ->
                                        val globalIngredient = globalMap[recipeIngredient.globalIngredientId]
                                        if (globalIngredient != null) {
                                            ResolvedIngredient.from(recipeIngredient, globalIngredient)
                                        } else {
                                            fallbackResolvedIngredient(recipeIngredient, recipeIngredients, globalMap)
                                        }
                                    }

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
                                    // After loading, kick off translation if the
                                    // active language isn't covered yet.
                                    ensureRecipeTranslated(_uiState.value.language)
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

    private fun fallbackResolvedIngredient(
        recipeIngredient: RecipeIngredient,
        allRecipeIngredients: List<RecipeIngredient>,
        globalMap: Map<String, GlobalIngredient>
    ): ResolvedIngredient {
        val nearbyName = globalMap[recipeIngredient.globalIngredientId]?.name
            ?: allRecipeIngredients.firstOrNull { it.globalIngredientId == recipeIngredient.globalIngredientId }
                ?.globalIngredientId
            ?: recipeIngredient.globalIngredientId

        return ResolvedIngredient(
            globalIngredientId = recipeIngredient.globalIngredientId,
            name = nearbyName,
            imageUrl = null,
            quantity = recipeIngredient.quantity,
            unit = recipeIngredient.unit,
            perPersonQuantity = recipeIngredient.perPersonQuantity
        )
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

