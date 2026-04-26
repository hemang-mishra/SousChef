package com.souschef.ui.screens.recipe.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.auth.UserProfile
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.RecipeTag
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.service.storage.FirebaseStorageService
import com.souschef.usecases.ingredient.GetIngredientsUseCase
import com.souschef.usecases.recipe.CreateRecipeUseCase
import com.souschef.usecases.recipe.GenerateRecipeStepsUseCase
import com.souschef.usecases.recipe.PublishRecipeUseCase
import com.souschef.usecases.recipe.SaveRecipeStepsUseCase
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
 * Unified ViewModel for the Create / Edit Recipe wizard (4 steps).
 *
 * ## New Step Order
 *   Step 0 — Details
 *   Step 1 — Cooking Steps (AI generates steps + extracts ingredients)
 *   Step 2 — Ingredients (AI-extracted, auto-resolved, editable)
 *   Step 3 — Review & Save
 *
 * Registered as `factory` in Koin.
 */
class CreateRecipeViewModel(
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val publishRecipeUseCase: PublishRecipeUseCase,
    private val getIngredientsUseCase: GetIngredientsUseCase,
    private val generateRecipeStepsUseCase: GenerateRecipeStepsUseCase,
    private val saveRecipeStepsUseCase: SaveRecipeStepsUseCase,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val storageService: FirebaseStorageService,
    private val voiceToTextParser: com.souschef.util.VoiceToTextParser,
    private val currentUser: UserProfile,
    private val recipeId: String? = null
) : ViewModel() {

    val voiceState = voiceToTextParser.state

    fun startListening() {
        voiceToTextParser.startListening()
    }

    fun stopListening() {
        voiceToTextParser.stopListening()
    }

    // ── Step 0: Details ──────────────────────────────────────
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

    // ── Step 1: Cooking Steps ────────────────────────────────
    private val _aiDescription = MutableStateFlow("")
    private val _steps = MutableStateFlow<List<RecipeStep>>(emptyList())
    private val _stepsStage = MutableStateFlow(CreateRecipeUiState.StepsStage.INPUT)
    private val _isGeneratingSteps = MutableStateFlow(false)

    // ── Step 2: Ingredients (AI-extracted, editable) ─────────
    private val _ingredients = MutableStateFlow<List<RecipeIngredient>>(emptyList())
    private val _globalIngredients = MutableStateFlow<List<GlobalIngredient>>(emptyList())
    private val _newlyCreatedIngredientNames = MutableStateFlow<List<String>>(emptyList())

    // ── Validation / Loading ─────────────────────────────────
    private val _titleError = MutableStateFlow<String?>(null)
    private val _ingredientError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isUploadingImage = MutableStateFlow(false)
    private val _isSaved = MutableStateFlow(false)
    private val _savedRecipeId = MutableStateFlow<String?>(null)
    private val _generalError = MutableStateFlow<String?>(null)

    private val _remoteCoverImageUrl = MutableStateFlow<String?>(null)

    // ── Combined UI State ────────────────────────────────────

    val uiState: StateFlow<CreateRecipeUiState> = combine(
        listOf(
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
            combine(_isSaved, _savedRecipeId, _isUploadingImage, _aiDescription) { saved, id, uploading, aiDesc ->
                arrayOf<Any?>(saved, id, uploading, aiDesc)
            },
            combine(_steps, _stepsStage, _isGeneratingSteps, _newlyCreatedIngredientNames) { steps, stage, generating, newNames ->
                arrayOf<Any?>(steps, stage, generating, newNames)
            }
        )
    ) { arrays ->
        val details = arrays[0]
        val serving = arrays[1]
        val recipeData = arrays[2]
        val errors = arrays[3]
        val saveData = arrays[4]
        val stepsData = arrays[5]

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
            generalError = errors[3] as String?,
            isSaved = saveData[0] as Boolean,
            savedRecipeId = saveData[1] as String?,
            isUploadingImage = saveData[2] as Boolean,
            aiDescription = saveData[3] as String,
            steps = stepsData[0] as List<RecipeStep>,
            stepsStage = stepsData[1] as CreateRecipeUiState.StepsStage,
            isGeneratingSteps = stepsData[2] as Boolean,
            newlyCreatedIngredientNames = stepsData[3] as List<String>
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CreateRecipeUiState())

    init {
        loadGlobalIngredients()
        if (recipeId != null) {
            loadRecipeToEdit(recipeId)
        }
    }

    // ── Data Loading ─────────────────────────────────────────

    private fun loadRecipeToEdit(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            recipeRepository.getRecipe(id).collect { result ->
                when (result) {
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

                        // Also load existing steps
                        loadExistingSteps(id)
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

    private fun loadExistingSteps(recipeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            recipeRepository.getSteps(recipeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val existingSteps = result.data
                        if (existingSteps.isNotEmpty()) {
                            _steps.value = existingSteps
                            _stepsStage.value = CreateRecipeUiState.StepsStage.REVIEW
                        }
                        _isLoading.value = false
                    }
                    is Resource.Failure -> {
                        // Non-fatal — recipe just has no steps yet
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
        // Step 0 (Details) validation
        if (_currentStep.value == 0 && !validateDetails()) return
        // Step 1 (Steps) — no validation, it's optional
        // Step 2 (Ingredients) validation
        if (_currentStep.value == 2 && !validateIngredients()) return

        if (_currentStep.value < 3) {
            _currentStep.value = _currentStep.value + 1
        }
    }

    fun onPreviousStep() {
        if (_currentStep.value > 0) _currentStep.value--
    }

    // ── Step 0: Details ──────────────────────────────────────

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

    // ── Step 1: Cooking Steps (AI generates steps + ingredients) ──

    fun onAiDescriptionChange(text: String) {
        _aiDescription.value = text
        _generalError.value = null
    }

    /**
     * Generates both steps AND ingredients from the AI description.
     * The use case:
     * 1. Calls Gemini → gets steps + extracted ingredients
     * 2. Fuzzy-matches ingredients against global library
     * 3. Auto-creates missing ingredients in Firestore
     * 4. Returns resolved steps + RecipeIngredient list
     */
    fun onGenerateSteps() {
        val description = _aiDescription.value
        if (description.isBlank()) {
            _generalError.value = "Please describe your recipe before generating steps."
            return
        }

        _stepsStage.value = CreateRecipeUiState.StepsStage.LOADING
        _isGeneratingSteps.value = true
        _generalError.value = null

        viewModelScope.launch(Dispatchers.IO) {
            generateRecipeStepsUseCase.execute(
                description = description,
                baseServingSize = _baseServingSize.value,
                creatorUserId = currentUser.uid
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val data = result.data
                        _steps.value = data.steps
                        _ingredients.value = data.ingredients
                        _newlyCreatedIngredientNames.value = data.newlyCreatedIngredients
                        _isGeneratingSteps.value = false
                        _stepsStage.value = CreateRecipeUiState.StepsStage.REVIEW

                        // Refresh global ingredients list (includes newly created ones)
                        loadGlobalIngredients()
                    }
                    is Resource.Failure -> {
                        _isGeneratingSteps.value = false
                        _generalError.value = result.message ?: "Generation failed. Please try again."
                        _stepsStage.value = CreateRecipeUiState.StepsStage.INPUT
                    }
                    is Resource.Loading -> { /* keep spinner */ }
                }
            }
        }
    }

    fun onCancelGeneration() {
        _isGeneratingSteps.value = false
        _stepsStage.value = CreateRecipeUiState.StepsStage.INPUT
    }

    fun onEditStep(index: Int, updatedStep: RecipeStep) {
        val current = _steps.value.toMutableList()
        if (index in current.indices) {
            current[index] = updatedStep
            _steps.value = current
        }
    }

    fun onDeleteStep(index: Int) {
        val current = _steps.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _steps.value = current.mapIndexed { i, step ->
                step.copy(stepNumber = i + 1)
            }
            if (_steps.value.isEmpty()) {
                _stepsStage.value = CreateRecipeUiState.StepsStage.INPUT
            }
        }
    }

    fun onMoveStepUp(index: Int) {
        if (index <= 0) return
        val current = _steps.value.toMutableList()
        val temp = current[index]
        current[index] = current[index - 1]
        current[index - 1] = temp
        _steps.value = current.mapIndexed { i, step ->
            step.copy(stepNumber = i + 1)
        }
    }

    fun onMoveStepDown(index: Int) {
        val current = _steps.value.toMutableList()
        if (index >= current.lastIndex) return
        val temp = current[index]
        current[index] = current[index + 1]
        current[index + 1] = temp
        _steps.value = current.mapIndexed { i, step ->
            step.copy(stepNumber = i + 1)
        }
    }

    fun onAddManualStep() {
        val current = _steps.value
        val newStep = RecipeStep(
            stepNumber = current.size + 1,
            stepType = "ACTION",
            instructionText = ""
        )
        _steps.value = current + newStep
        _stepsStage.value = CreateRecipeUiState.StepsStage.REVIEW
    }

    fun onRetryGeneration() {
        _generalError.value = null
        _stepsStage.value = CreateRecipeUiState.StepsStage.INPUT
    }

    fun onStepMediaSelected(stepIndex: Int, uri: Uri, mediaType: String) {
        val uploadRecipeId = recipeId ?: "temp_${System.currentTimeMillis()}"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val downloadUrl = storageService.uploadStepMedia(
                    recipeId = uploadRecipeId,
                    stepIndex = stepIndex,
                    mediaUri = uri,
                    mediaType = mediaType
                )
                val current = _steps.value.toMutableList()
                if (stepIndex in current.indices) {
                    current[stepIndex] = current[stepIndex].copy(
                        mediaUrl = downloadUrl,
                        mediaType = mediaType
                    )
                    _steps.value = current
                }
            } catch (e: Exception) {
                _generalError.value = "Failed to upload media: ${e.message}"
            }
        }
    }

    fun onRemoveStepMedia(stepIndex: Int) {
        val current = _steps.value.toMutableList()
        if (stepIndex in current.indices) {
            current[stepIndex] = current[stepIndex].copy(
                mediaUrl = null,
                mediaType = null
            )
            _steps.value = current
        }
    }

    // ── Step 2: Ingredients (editable after AI extraction) ────

    fun onAddIngredient(ingredient: RecipeIngredient) {
        _ingredients.value = _ingredients.value + ingredient
        _ingredientError.value = null
    }

    fun onRemoveIngredient(globalIngredientId: String) {
        _ingredients.value = _ingredients.value.filter { it.globalIngredientId != globalIngredientId }
    }

    fun onCreateGlobalIngredientAndAdd(name: String, quantity: Double, unit: String) {
        val newIngredient = GlobalIngredient(
            name = name,
            defaultUnit = unit,
            createdByUserId = currentUser.uid
        )
        viewModelScope.launch(Dispatchers.IO) {
            ingredientRepository.createIngredient(newIngredient).collect { result ->
                if (result is Resource.Success) {
                    val newId = result.data
                    val ri = RecipeIngredient(newId, quantity, unit)
                    launch(Dispatchers.Main) {
                        onAddIngredient(ri)
                    }
                    // Refresh global list
                    loadGlobalIngredients()
                }
            }
        }
    }

    // ── Step 3: Save ─────────────────────────────────────────

    fun onSave(publish: Boolean) {
        if (!validateDetails() || !validateIngredients()) {
            _generalError.value = "Please complete all required fields."
            return
        }

        // Validate non-empty steps have instructions
        val nonEmptySteps = _steps.value
        if (nonEmptySteps.isNotEmpty()) {
            val emptyStep = nonEmptySteps.indexOfFirst { it.instructionText.isBlank() }
            if (emptyStep != -1) {
                _generalError.value = "Step ${emptyStep + 1} has no instructions. Please fill it in or remove it."
                return
            }
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
                // ── UPDATE existing recipe ───────────────
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
                            saveStepsAndFinish(recipeId)
                        }
                        is Resource.Failure -> {
                            _isLoading.value = false
                            _generalError.value = result.message ?: "Failed to update recipe."
                        }
                        is Resource.Loading -> { /* keep spinner */ }
                    }
                }
            } else {
                // ── CREATE new recipe ────────────────────
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
                            val newRecipeId = result.data
                            saveStepsAndFinish(newRecipeId)
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

    /** Saves steps to the recipe (if any), then marks the flow as complete. */
    private suspend fun saveStepsAndFinish(finalRecipeId: String) {
        val stepsToSave = _steps.value
        if (stepsToSave.isEmpty()) {
            _savedRecipeId.value = finalRecipeId
            _isSaved.value = true
            _isLoading.value = false
            return
        }

        saveRecipeStepsUseCase.execute(finalRecipeId, stepsToSave).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _savedRecipeId.value = finalRecipeId
                    _isSaved.value = true
                    _isLoading.value = false
                }
                is Resource.Failure -> {
                    _isLoading.value = false
                    _generalError.value = result.message ?: "Recipe saved but failed to save steps."
                }
                is Resource.Loading -> { /* wait */ }
            }
        }
    }

    fun clearError() {
        _generalError.value = null
    }

    // ── Validation ───────────────────────────────────────────

    private fun validateDetails(): Boolean {
        if (_title.value.isBlank()) {
            _titleError.value = "Recipe title is required"
            return false
        }
        return true
    }

    private fun validateIngredients(): Boolean {
        if (_ingredients.value.isEmpty()) {
            _ingredientError.value = "Add at least one ingredient"
            return false
        }
        return true
    }
}
