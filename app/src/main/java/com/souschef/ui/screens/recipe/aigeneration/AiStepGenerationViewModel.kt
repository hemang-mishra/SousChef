package com.souschef.ui.screens.recipe.aigeneration

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.service.storage.FirebaseStorageService
import com.souschef.usecases.recipe.GenerateRecipeStepsUseCase
import com.souschef.usecases.recipe.SaveRecipeStepsUseCase
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the AI Step Generation screen.
 * Handles recipe loading, AI generation, step editing, and saving.
 *
 * Registered as factory in Koin with recipeId parameter.
 */
class AiStepGenerationViewModel(
    private val generateRecipeStepsUseCase: GenerateRecipeStepsUseCase,
    private val saveRecipeStepsUseCase: SaveRecipeStepsUseCase,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val storageService: FirebaseStorageService,
    private val recipeId: String
) : ViewModel() {

    // ── Internal state flows ─────────────────────────────────
    private val _recipeDescription = MutableStateFlow("")
    private val _ingredientChips = MutableStateFlow<List<String>>(emptyList())
    private val _generatedSteps = MutableStateFlow<List<RecipeStep>>(emptyList())
    private val _stage = MutableStateFlow(AiStepGenerationUiState.Stage.INPUT)
    private val _isLoading = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isSaved = MutableStateFlow(false)
    private val _isRecipeLoading = MutableStateFlow(true)
    private val _recipeTitle = MutableStateFlow("")

    // Cached resolved ingredients for the AI prompt
    private var resolvedIngredients: List<ResolvedIngredient> = emptyList()

    val uiState: StateFlow<AiStepGenerationUiState> = combine(
        combine(_recipeDescription, _ingredientChips, _generatedSteps, _stage) { desc, chips, steps, stage ->
            arrayOf<Any?>(desc, chips, steps, stage)
        },
        combine(_isLoading, _isSaving, _error, _isSaved) { loading, saving, error, saved ->
            arrayOf<Any?>(loading, saving, error, saved)
        },
        combine(_isRecipeLoading, _recipeTitle) { recipeLoading, title ->
            Pair(recipeLoading, title)
        }
    ) { inputs, states, (recipeLoading, title) ->
        @Suppress("UNCHECKED_CAST")
        AiStepGenerationUiState(
            recipeDescription = inputs[0] as String,
            ingredientChips = inputs[1] as List<String>,
            generatedSteps = inputs[2] as List<RecipeStep>,
            stage = inputs[3] as AiStepGenerationUiState.Stage,
            isLoading = states[0] as Boolean,
            isSaving = states[1] as Boolean,
            error = states[2] as String?,
            isSaved = states[3] as Boolean,
            isRecipeLoading = recipeLoading,
            recipeTitle = title
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiStepGenerationUiState())

    init {
        loadRecipeData()
    }

    // ── Data Loading ─────────────────────────────────────────

    /**
     * Loads the recipe and its ingredients, resolves ingredient names for display.
     */
    private fun loadRecipeData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRecipeLoading.value = true

            // Load recipe
            recipeRepository.getRecipe(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val recipe = result.data
                        _recipeTitle.value = recipe.title

                        // Load all global ingredients to resolve names
                        try {
                            ingredientRepository.getAllIngredients().collect { allIngredients ->
                                val ingredientMap = allIngredients.associateBy { it.ingredientId }

                                // Resolve ingredients
                                resolvedIngredients = recipe.ingredients.mapNotNull { recipeIngredient ->
                                    ingredientMap[recipeIngredient.globalIngredientId]?.let { global ->
                                        ResolvedIngredient.from(recipeIngredient, global)
                                    }
                                }

                                _ingredientChips.value = resolvedIngredients.map { it.name }
                                _isRecipeLoading.value = false
                            }
                        } catch (e: Exception) {
                            _error.value = "Failed to load ingredients: ${e.message}"
                            _isRecipeLoading.value = false
                        }
                    }
                    is Resource.Failure -> {
                        _error.value = result.message ?: "Failed to load recipe."
                        _isRecipeLoading.value = false
                    }
                    is Resource.Loading -> { /* wait */ }
                }
            }
        }
    }

    // ── Stage 1: Input ───────────────────────────────────────

    fun onDescriptionChange(text: String) {
        _recipeDescription.value = text
        _error.value = null
    }

    fun onGenerateSteps() {
        val description = _recipeDescription.value
        if (description.isBlank()) {
            _error.value = "Please describe your recipe before generating steps."
            return
        }

        _stage.value = AiStepGenerationUiState.Stage.LOADING
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            generateRecipeStepsUseCase.execute(description, resolvedIngredients).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _generatedSteps.value = result.data
                        _isLoading.value = false
                        _stage.value = AiStepGenerationUiState.Stage.REVIEW
                    }
                    is Resource.Failure -> {
                        _isLoading.value = false
                        _error.value = result.message ?: "Generation failed. Please try again."
                        _stage.value = AiStepGenerationUiState.Stage.INPUT
                    }
                    is Resource.Loading -> { /* keep spinner */ }
                }
            }
        }
    }

    fun onCancelGeneration() {
        _isLoading.value = false
        _stage.value = AiStepGenerationUiState.Stage.INPUT
    }

    // ── Stage 3: Review & Edit ───────────────────────────────

    fun onEditStep(index: Int, updatedStep: RecipeStep) {
        val current = _generatedSteps.value.toMutableList()
        if (index in current.indices) {
            current[index] = updatedStep
            _generatedSteps.value = current
        }
    }

    fun onDeleteStep(index: Int) {
        val current = _generatedSteps.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            // Re-number
            _generatedSteps.value = current.mapIndexed { i, step ->
                step.copy(stepNumber = i + 1)
            }
        }
    }

    fun onMoveStepUp(index: Int) {
        if (index <= 0) return
        val current = _generatedSteps.value.toMutableList()
        val temp = current[index]
        current[index] = current[index - 1]
        current[index - 1] = temp
        // Re-number
        _generatedSteps.value = current.mapIndexed { i, step ->
            step.copy(stepNumber = i + 1)
        }
    }

    fun onMoveStepDown(index: Int) {
        val current = _generatedSteps.value.toMutableList()
        if (index >= current.lastIndex) return
        val temp = current[index]
        current[index] = current[index + 1]
        current[index + 1] = temp
        // Re-number
        _generatedSteps.value = current.mapIndexed { i, step ->
            step.copy(stepNumber = i + 1)
        }
    }

    fun onAddManualStep() {
        val current = _generatedSteps.value
        val newStep = RecipeStep(
            stepNumber = current.size + 1,
            instructionText = ""
        )
        _generatedSteps.value = current + newStep
    }

    fun onSaveSteps() {
        val steps = _generatedSteps.value
        if (steps.isEmpty()) {
            _error.value = "Add at least one step before saving."
            return
        }

        // Validate: all steps must have non-blank instructions
        val emptyStep = steps.indexOfFirst { it.instructionText.isBlank() }
        if (emptyStep != -1) {
            _error.value = "Step ${emptyStep + 1} has no instructions. Please fill it in or remove it."
            return
        }

        _isSaving.value = true
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            saveRecipeStepsUseCase.execute(recipeId, steps).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _isSaving.value = false
                        _isSaved.value = true
                    }
                    is Resource.Failure -> {
                        _isSaving.value = false
                        _error.value = result.message ?: "Failed to save steps."
                    }
                    is Resource.Loading -> { /* wait */ }
                }
            }
        }
    }

    fun onRetry() {
        _error.value = null
        _stage.value = AiStepGenerationUiState.Stage.INPUT
    }

    fun clearError() {
        _error.value = null
    }

    // ── Step Media ───────────────────────────────────────────

    /**
     * Uploads media (image or video) for a specific step.
     * Updates the step with the download URL and media type.
     */
    fun onStepMediaSelected(stepIndex: Int, uri: Uri, mediaType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadUrl = storageService.uploadStepMedia(
                    recipeId = recipeId,
                    stepIndex = stepIndex,
                    mediaUri = uri,
                    mediaType = mediaType
                )
                val current = _generatedSteps.value.toMutableList()
                if (stepIndex in current.indices) {
                    current[stepIndex] = current[stepIndex].copy(
                        mediaUrl = downloadUrl,
                        mediaType = mediaType
                    )
                    _generatedSteps.value = current
                }
            } catch (e: Exception) {
                _error.value = "Failed to upload media: ${e.message}"
            }
        }
    }

    /**
     * Removes media from a step (clears URL and type).
     */
    fun onRemoveStepMedia(stepIndex: Int) {
        val current = _generatedSteps.value.toMutableList()
        if (stepIndex in current.indices) {
            current[stepIndex] = current[stepIndex].copy(
                mediaUrl = null,
                mediaType = null
            )
            _generatedSteps.value = current
        }
    }
}
