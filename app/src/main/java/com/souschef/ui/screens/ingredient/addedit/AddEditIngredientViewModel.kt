package com.souschef.ui.screens.ingredient.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.auth.UserProfile
import com.souschef.repository.ingredient.IngredientRepository
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
    private val _nameError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isSaved = MutableStateFlow(false)
    private val _savedIngredientId = MutableStateFlow<String?>(null)
    private val _generalError = MutableStateFlow<String?>(null)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<AddEditIngredientUiState> = combine(
        combine(_name, _defaultUnit, _isDispensable) { n, u, d -> Triple(n, u, d) },
        combine(_spice, _sweetness, _saltness) { sp, sw, sa -> Triple(sp, sw, sa) },
        combine(_imageUrl, _nameError, _isLoading, _generalError) { img, ne, l, ge -> arrayOf(img, ne, l, ge) },
        combine(_isSaved, _savedIngredientId) { s, id -> Pair(s, id) }
    ) { basic, flavor, misc, saved ->
        val (name, unit, dispensable) = basic
        val (spice, sweetness, saltness) = flavor
        val (isSaved, savedId) = saved
        AddEditIngredientUiState(
            isEditMode = editIngredientId != null,
            ingredientId = editIngredientId,
            name = name,
            defaultUnit = unit,
            isDispensable = dispensable,
            spiceIntensityValue = spice,
            sweetnessValue = sweetness,
            saltnessValue = saltness,
            imageUrl = misc[0] as String?,
            nameError = misc[1] as String?,
            isLoading = misc[2] as Boolean,
            generalError = misc[3] as String?,
            isSaved = isSaved,
            savedIngredientId = savedId
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

    fun onSave() {
        if (_name.value.isBlank()) {
            _nameError.value = "Ingredient name is required"
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
                    currentUserId = currentUser.uid
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
                    currentUserId = currentUser.uid
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

