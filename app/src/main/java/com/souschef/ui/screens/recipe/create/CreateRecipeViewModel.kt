package com.souschef.ui.screens.recipe.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.auth.UserProfile
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.recipe.RecipeTag
import com.souschef.service.storage.FirebaseStorageService
import com.souschef.usecases.ingredient.GetIngredientsUseCase
import com.souschef.usecases.recipe.CreateRecipeUseCase
import com.souschef.usecases.recipe.PublishRecipeUseCase
import com.souschef.usecases.recipe.UpdateRecipeUseCase
import com.souschef.repository.recipe.RecipeRepository
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
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val publishRecipeUseCase: PublishRecipeUseCase,
    private val getIngredientsUseCase: GetIngredientsUseCase,
    private val recipeRepository: RecipeRepository,
    private val storageService: FirebaseStorageService,
    private val currentUser: UserProfile,
    private val recipeId: String? = null
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
    private val _coverImageUri = MutableStateFlow<Uri?>(null)
    private val _ingredients = MutableStateFlow<List<RecipeIngredient>>(emptyList())
    private val _globalIngredients = MutableStateFlow<List<GlobalIngredient>>(emptyList())
    private val _titleError = MutableStateFlow<String?>(null)
    private val _ingredientError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isUploadingImage = MutableStateFlow(false)
    private val _isSaved = MutableStateFlow(false)
    private val _savedRecipeId = MutableStateFlow<String?>(null)
    private val _generalError = MutableStateFlow<String?>(null)
    
    private val _remoteCoverImageUrl = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CreateRecipeUiState> = combine(
        combine(_currentStep, _title, _description, _baseServingSize) { step, title, desc, base ->
            arrayOf<Any?>(step, title, desc, base)
        },
        combine(_minServingSize, _maxServingSize, _useMinServing, _useMaxServing) { min, max, useMin, useMax ->
            arrayOf<Any?>(min, max, useMin, useMax)
        },
        combine(_selectedTags, _ingredients, _globalIngredients, _coverImageUri) { tags, ingredients, globalIngredients, coverUri ->
            arrayOf<Any?>(tags, ingredients, globalIngredients, coverUri)
        },
        combine(_titleError, _ingredientError, _isLoading, _generalError) { tErr, iErr, loading, gErr ->
            arrayOf<Any?>(tErr, iErr, loading, gErr)
        },
        combine(_isSaved, _savedRecipeId, _isUploadingImage) { saved, id, uploading ->
            Triple(saved, id, uploading)
        }
    ) { details, serving, recipeData, errors, (saved, savedId, uploading) ->
        @Suppress("UNCHECKED_CAST")
        CreateRecipeUiState(
            currentStep = details[0] as Int,
            title = details[1] as String,
            description = details[2] as String,
            baseServingSize = details[3] as Int,
            minServingSize = serving[0] as Int?,
            maxServingSize = serving[1] as Int?,
            useMinServing = serving[2] as Boolean,
            useMaxServing = serving[3] as Boolean,
            selectedTags = recipeData[0] as List<RecipeTag>,
            ingredients = recipeData[1] as List<RecipeIngredient>,
            globalIngredients = recipeData[2] as List<GlobalIngredient>,
            coverImageUri = recipeData[3] as Uri?,
            titleError = errors[0] as String?,
            ingredientError = errors[1] as String?,
            isLoading = errors[2] as Boolean,
            isUploadingImage = uploading,
            generalError = errors[3] as String?,
            isSaved = saved,
            savedRecipeId = savedId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CreateRecipeUiState())

    init {
        loadGlobalIngredients()
        if (recipeId != null) {
            loadRecipeToEdit(recipeId)
        }
    }

    private fun loadRecipeToEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            recipeRepository.getRecipe(id).collect { result ->
                when(result) {
                    is Resource.Success -> {
                        val recipe = result.data
                        _title.value = recipe.title
                        _description.value = recipe.description
                        _baseServingSize.value = recipe.baseServingSize
                        if (recipe.minServingSize != null) {
                            _minServingSize.value = recipe.minServingSize
                            _useMinServing.value = true
                        }
                        if (recipe.maxServingSize != null) {
                            _maxServingSize.value = recipe.maxServingSize
                            _useMaxServing.value = true
                        }
                        _selectedTags.value = recipe.tags.mapNotNull { tagName ->
                            RecipeTag.entries.find { it.name == tagName }
                        }
                        _ingredients.value = recipe.ingredients
                        _remoteCoverImageUrl.value = recipe.coverImageUrl
                        _isLoading.value = false
                    }
                    is Resource.Failure -> {
                        _generalError.value = "Failed to load recipe for editing"
                        _isLoading.value = false
                    }
                    is Resource.Loading -> { }
                }
            }
        }
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

    fun onCoverImageSelected(uri: Uri) {
        _coverImageUri.value = uri
    }

    fun onRemoveCoverImage() {
        _coverImageUri.value = null
        _remoteCoverImageUrl.value = null
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

            // Upload cover image if selected
            var coverImageUrl: String? = _remoteCoverImageUrl.value
            val imageUri = _coverImageUri.value
            if (imageUri != null) {
                _isUploadingImage.value = true
                try {
                    val uploadId = recipeId ?: "temp_${System.currentTimeMillis()}"
                    coverImageUrl = storageService.uploadRecipeCoverImage(
                        recipeId = uploadId,
                        imageUri = imageUri
                    )
                } catch (e: Exception) {
                    _generalError.value = "Failed to upload image: ${e.message}"
                    _isLoading.value = false
                    _isUploadingImage.value = false
                    return@launch
                }
                _isUploadingImage.value = false
            }

            if (recipeId != null) {
                updateRecipeUseCase.execute(
                    recipeId = recipeId,
                    title = _title.value,
                    description = _description.value,
                    baseServingSize = _baseServingSize.value,
                    minServingSize = _minServingSize.value,
                    maxServingSize = _maxServingSize.value,
                    tags = _selectedTags.value.map { it.name },
                    ingredients = _ingredients.value,
                    publish = publish,
                    coverImageUrl = coverImageUrl
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _savedRecipeId.value = recipeId
                            _isSaved.value = true
                            _isLoading.value = false
                        }
                        is Resource.Failure -> {
                            _isLoading.value = false
                            _generalError.value = result.message ?: "Failed to update recipe."
                        }
                        is Resource.Loading -> { /* keep spinner */ }
                    }
                }
            } else {
                createRecipeUseCase.execute(
                    title = _title.value,
                    description = _description.value,
                    baseServingSize = _baseServingSize.value,
                    minServingSize = _minServingSize.value,
                    maxServingSize = _maxServingSize.value,
                    tags = _selectedTags.value.map { it.name },
                    ingredients = _ingredients.value,
                    currentUser = currentUser,
                    publish = publish,
                    coverImageUrl = coverImageUrl
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
