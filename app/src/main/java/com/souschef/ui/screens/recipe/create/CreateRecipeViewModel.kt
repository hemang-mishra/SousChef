package com.souschef.ui.screens.recipe.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.auth.UserProfile
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.recipe.RecipeTag
import com.souschef.usecases.ingredient.GetIngredientsUseCase
import com.souschef.usecases.recipe.CreateRecipeUseCase
import com.souschef.usecases.recipe.PublishRecipeUseCase
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Create Recipe wizard. Registered as `factory` in Koin.
 */
class CreateRecipeViewModel(
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val publishRecipeUseCase: PublishRecipeUseCase,
    private val getIngredientsUseCase: GetIngredientsUseCase,
    private val currentUser: UserProfile
) : ViewModel() {

    // ── Internal state flows ─────────────────────────────────
    private val _currentStep = MutableStateFlow(0)
    private val _title = MutableStateFlow("")
    private val _description = MutableStateFlow("")
    private val _baseServingSize = MutableStateFlow(4)
    private val _minServingSize = MutableStateFlow<Int?>(null)
    private val _maxServingSize = MutableStateFlow<Int?>(null)
    private val _useMinServing = MutableStateFlow(false)
    private val _useMaxServing = MutableStateFlow(false)
    private val _selectedTags = MutableStateFlow<List<RecipeTag>>(emptyList())
    private val _ingredients = MutableStateFlow<List<RecipeIngredient>>(emptyList())
    private val _globalIngredients = MutableStateFlow<List<GlobalIngredient>>(emptyList())
    private val _titleError = MutableStateFlow<String?>(null)
    private val _ingredientError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isSaved = MutableStateFlow(false)
    private val _savedRecipeId = MutableStateFlow<String?>(null)
    private val _generalError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CreateRecipeUiState> = combine(
        combine(_currentStep, _title, _description, _baseServingSize) { step, title, desc, base ->
            arrayOf<Any?>(step, title, desc, base)
        },
        combine(_minServingSize, _maxServingSize, _useMinServing, _useMaxServing) { min, max, useMin, useMax ->
            arrayOf<Any?>(min, max, useMin, useMax)
        },
        combine(_selectedTags, _ingredients, _globalIngredients) { tags, ingredients, globalIngredients ->
            Triple(tags, ingredients, globalIngredients)
        },
        combine(_titleError, _ingredientError, _isLoading, _generalError) { tErr, iErr, loading, gErr ->
            arrayOf<Any?>(tErr, iErr, loading, gErr)
        },
        combine(_isSaved, _savedRecipeId) { saved, id -> Pair(saved, id) }
    ) { details, serving, (tags, ingredients, globalIngredients), errors, (saved, savedId) ->
        CreateRecipeUiState(
            currentStep = details[0] as Int,
            title = details[1] as String,
            description = details[2] as String,
            baseServingSize = details[3] as Int,
            minServingSize = serving[0] as Int?,
            maxServingSize = serving[1] as Int?,
            useMinServing = serving[2] as Boolean,
            useMaxServing = serving[3] as Boolean,
            selectedTags = tags,
            ingredients = ingredients,
            globalIngredients = globalIngredients,
            titleError = errors[0] as String?,
            ingredientError = errors[1] as String?,
            isLoading = errors[2] as Boolean,
            generalError = errors[3] as String?,
            isSaved = saved,
            savedRecipeId = savedId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CreateRecipeUiState())

    init {
        loadGlobalIngredients()
    }

    private fun loadGlobalIngredients() {
        viewModelScope.launch {
            try {
                getIngredientsUseCase.execute().collect { ingredients ->
                    _globalIngredients.value = ingredients
                }
            } catch (_: Exception) {
                // Non-fatal — picker will just show empty
            }
        }
    }

    // ── Step Navigation ──────────────────────────────────────

    fun onNextStep() {
        if (_currentStep.value == 0 && !validateStep1()) return
        if (_currentStep.value == 1 && !validateStep2()) return
        if (_currentStep.value < 2) _currentStep.value++
    }

    fun onPreviousStep() {
        if (_currentStep.value > 0) _currentStep.value--
    }

    // ── Step 1: Details ──────────────────────────────────────

    fun onTitleChange(title: String) {
        _title.value = title
        _titleError.value = null
        _generalError.value = null
    }

    fun onDescriptionChange(description: String) {
        _description.value = description
    }

    fun onBaseServingSizeChange(size: Int) {
        _baseServingSize.value = size.coerceIn(1, 50)
    }

    fun onUseMinServingChange(use: Boolean) {
        _useMinServing.value = use
        if (!use) _minServingSize.value = null
        else _minServingSize.value = 1
    }

    fun onMinServingSizeChange(size: Int) {
        _minServingSize.value = size.coerceIn(1, _baseServingSize.value)
    }

    fun onUseMaxServingChange(use: Boolean) {
        _useMaxServing.value = use
        if (!use) _maxServingSize.value = null
        else _maxServingSize.value = _baseServingSize.value * 2
    }

    fun onMaxServingSizeChange(size: Int) {
        _maxServingSize.value = size.coerceIn(_baseServingSize.value, 100)
    }

    fun onToggleTag(tag: RecipeTag) {
        val current = _selectedTags.value
        _selectedTags.value = if (tag in current) current - tag else current + tag
    }

    // ── Step 2: Ingredients ──────────────────────────────────

    fun onAddIngredient(ingredient: RecipeIngredient) {
        _ingredients.value = _ingredients.value + ingredient
        _ingredientError.value = null
    }

    fun onRemoveIngredient(globalIngredientId: String) {
        _ingredients.value = _ingredients.value.filter { it.globalIngredientId != globalIngredientId }
    }

    // ── Step 3: Save ─────────────────────────────────────────

    fun onSave(publish: Boolean) {
        if (!validateStep1() || !validateStep2()) {
            _generalError.value = "Please complete all required fields."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _generalError.value = null

            createRecipeUseCase.execute(
                title = _title.value,
                description = _description.value,
                baseServingSize = _baseServingSize.value,
                minServingSize = _minServingSize.value,
                maxServingSize = _maxServingSize.value,
                tags = _selectedTags.value.map { it.name },
                ingredients = _ingredients.value,
                currentUser = currentUser,
                publish = publish
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _savedRecipeId.value = result.data
                        _isSaved.value = true
                        _isLoading.value = false
                    }
                    is Resource.Failure -> {
                        _isLoading.value = false
                        _generalError.value = result.message ?: "Failed to save recipe."
                    }
                    is Resource.Loading -> { /* keep spinner */ }
                }
            }
        }
    }

    fun clearError() {
        _generalError.value = null
    }

    // ── Validation ───────────────────────────────────────────

    private fun validateStep1(): Boolean {
        if (_title.value.isBlank()) {
            _titleError.value = "Recipe title is required"
            return false
        }
        return true
    }

    private fun validateStep2(): Boolean {
        if (_ingredients.value.isEmpty()) {
            _ingredientError.value = "Add at least one ingredient"
            return false
        }
        return true
    }
}
