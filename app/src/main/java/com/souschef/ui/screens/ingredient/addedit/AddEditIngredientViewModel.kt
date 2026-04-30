package com.souschef.ui.screens.ingredient.addedit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.auth.UserProfile
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.service.storage.FirebaseStorageService
import com.souschef.usecases.ingredient.AddGlobalIngredientUseCase
import com.souschef.usecases.ingredient.UpdateGlobalIngredientUseCase
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddEditIngredientViewModel(
    private val addGlobalIngredientUseCase: AddGlobalIngredientUseCase,
    private val updateGlobalIngredientUseCase: UpdateGlobalIngredientUseCase,
    private val ingredientRepository: IngredientRepository,
    private val storageService: FirebaseStorageService,
    private val currentUser: UserProfile,
    private val editIngredientId: String?
) : ViewModel() {

    private val _name = MutableStateFlow("")
    private val _defaultUnit = MutableStateFlow("grams")
    private val _isDispensable = MutableStateFlow(false)
    private val _spice = MutableStateFlow(0.0)
    private val _sweetness = MutableStateFlow(0.0)
    private val _saltness = MutableStateFlow(0.0)
    private val _imageUrl = MutableStateFlow<String?>(null)
    private val _pendingImageUri = MutableStateFlow<Uri?>(null)
    private val _isUploadingImage = MutableStateFlow(false)
    private val _imageCleared = MutableStateFlow(false)
    private val _nameError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isSaved = MutableStateFlow(false)
    private val _savedIngredientId = MutableStateFlow<String?>(null)
    private val _generalError = MutableStateFlow<String?>(null)

    private data class FlavorTriple(val spice: Double, val sweet: Double, val salt: Double)
    private data class ImageState(val url: String?, val pending: Uri?, val uploading: Boolean)
    private data class StatusBundle(val nameError: String?, val isLoading: Boolean, val generalError: String?)
    private data class SavedBundle(val isSaved: Boolean, val savedId: String?)

    val uiState: StateFlow<AddEditIngredientUiState> = combine(
        combine(_name, _defaultUnit, _isDispensable) { n, u, d -> Triple(n, u, d) },
        combine(_spice, _sweetness, _saltness) { sp, sw, sa -> FlavorTriple(sp, sw, sa) },
        combine(_imageUrl, _pendingImageUri, _isUploadingImage) { url, pending, uploading ->
            ImageState(url, pending, uploading)
        },
        combine(_nameError, _isLoading, _generalError) { ne, l, ge -> StatusBundle(ne, l, ge) },
        combine(_isSaved, _savedIngredientId) { s, id -> SavedBundle(s, id) }
    ) { basic, flavor, image, status, saved ->
        val (name, unit, dispensable) = basic
        AddEditIngredientUiState(
            isEditMode = editIngredientId != null,
            ingredientId = editIngredientId,
            name = name,
            defaultUnit = unit,
            isDispensable = dispensable,
            spiceIntensityValue = flavor.spice,
            sweetnessValue = flavor.sweet,
            saltnessValue = flavor.salt,
            imageUrl = image.url,
            pendingImageUri = image.pending,
            isUploadingImage = image.uploading,
            nameError = status.nameError,
            isLoading = status.isLoading,
            generalError = status.generalError,
            isSaved = saved.isSaved,
            savedIngredientId = saved.savedId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddEditIngredientUiState())

    init {
        if (editIngredientId != null) loadExisting(editIngredientId)
    }

    private fun loadExisting(ingredientId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            ingredientRepository.getIngredient(ingredientId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val ing = result.data
                        _name.value = ing.name
                        _defaultUnit.value = ing.defaultUnit
                        _isDispensable.value = ing.isDispensable
                        _spice.value = ing.spiceIntensityValue
                        _sweetness.value = ing.sweetnessValue
                        _saltness.value = ing.saltnessValue
                        _imageUrl.value = ing.imageUrl
                        _isLoading.value = false
                    }
                    is Resource.Failure -> {
                        _generalError.value = result.message ?: "Failed to load ingredient"
                        _isLoading.value = false
                    }
                    is Resource.Loading -> { /* wait */ }
                }
            }
        }
    }

    fun onNameChange(name: String) { _name.value = name; _nameError.value = null; _generalError.value = null }
    fun onUnitChange(unit: String) { _defaultUnit.value = unit }
    fun onDispensableChange(value: Boolean) { _isDispensable.value = value }
    fun onSpiceChange(value: Double) { _spice.value = value }
    fun onSweetnessChange(value: Double) { _sweetness.value = value }
    fun onSaltnessChange(value: Double) { _saltness.value = value }
    fun clearError() { _generalError.value = null }

    /**
     * Called when the user picks a new image from the gallery. Shows the
     * local URI as an instant preview, then uploads to Firebase Storage in
     * the background. On success the [imageUrl] is swapped in and the
     * pending preview is cleared.
     */
    fun onImageSelected(uri: Uri) {
        _pendingImageUri.value = uri
        _imageCleared.value = false
        viewModelScope.launch(Dispatchers.IO) {
            _isUploadingImage.value = true
            try {
                val url = storageService.uploadIngredientImage(
                    ingredientId = editIngredientId.orEmpty(),
                    imageUri = uri
                )
                _imageUrl.value = url
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _generalError.value = e.message ?: "Failed to upload image."
                _pendingImageUri.value = null
            } finally {
                _isUploadingImage.value = false
                if (_imageUrl.value != null && _pendingImageUri.value == uri) {
                    // Once the remote URL is in, drop the local preview so
                    // AsyncImage falls back to the canonical CDN copy.
                    _pendingImageUri.value = null
                }
            }
        }
    }

    /**
     * Removes any image associated with this ingredient. For an existing
     * ingredient the [imageUrl] field will be deleted on save; for a new
     * ingredient we just drop the local state.
     */
    fun onRemoveImage() {
        _pendingImageUri.value = null
        _imageUrl.value = null
        _imageCleared.value = true
    }

    fun onSave() {
        if (_name.value.isBlank()) {
            _nameError.value = "Ingredient name is required"
            return
        }
        if (_isUploadingImage.value) {
            // Avoid saving while the upload is still in flight — the URL
            // wouldn't be persisted otherwise.
            _generalError.value = "Please wait for the image to finish uploading."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _generalError.value = null

            if (editIngredientId != null) {
                updateGlobalIngredientUseCase.execute(
                    ingredientId = editIngredientId,
                    name = _name.value,
                    defaultUnit = _defaultUnit.value,
                    isDispensable = _isDispensable.value,
                    spiceIntensityValue = _spice.value,
                    sweetnessValue = _sweetness.value,
                    saltnessValue = _saltness.value,
                    currentUserId = currentUser.uid,
                    imageUrl = _imageUrl.value,
                    clearImage = _imageCleared.value && _imageUrl.value == null
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _savedIngredientId.value = editIngredientId
                            _isSaved.value = true
                            _isLoading.value = false
                        }
                        is Resource.Failure -> {
                            _isLoading.value = false
                            _generalError.value = result.message ?: "Failed to update ingredient."
                        }
                        is Resource.Loading -> { /* keep spinner */ }
                    }
                }
            } else {
                addGlobalIngredientUseCase.execute(
                    name = _name.value,
                    defaultUnit = _defaultUnit.value,
                    isDispensable = _isDispensable.value,
                    spiceIntensityValue = _spice.value,
                    sweetnessValue = _sweetness.value,
                    saltnessValue = _saltness.value,
                    currentUserId = currentUser.uid,
                    imageUrl = _imageUrl.value
                ).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _savedIngredientId.value = result.data
                            _isSaved.value = true
                            _isLoading.value = false
                        }
                        is Resource.Failure -> {
                            _isLoading.value = false
                            _generalError.value = result.message ?: "Failed to add ingredient."
                        }
                        is Resource.Loading -> { /* keep spinner */ }
                    }
                }
            }
        }
    }
}

