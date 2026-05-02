package com.souschef.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.repository.auth.AuthRepository
import com.souschef.usecases.admin.ChefVerificationUseCase
import com.souschef.ui.viewmodels.AppViewModel
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminViewModel(
    private val authRepository: AuthRepository,
    private val chefVerificationUseCase: ChefVerificationUseCase,
    private val appViewModel: AppViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        observeUsers()
        loadCounts()
    }

    private fun observeUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                authRepository.getAllUsers().collect { users ->
                    _uiState.update {
                        it.copy(
                            users = users,
                            totalVerifiedChefs = users.count { u -> u.isVerifiedChef },
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.getUserCount().collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(totalUsers = res.data) }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.getRecipeCount().collect { res ->
                if (res is Resource.Success) {
                    _uiState.update { it.copy(totalRecipes = res.data) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onToggleVerified(targetUid: String, currentlyVerified: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            chefVerificationUseCase
                .execute(appViewModel.currentUser.value, targetUid, !currentlyVerified)
                .collect { res ->
                    when (res) {
                        is Resource.Success -> _uiState.update {
                            it.copy(message = if (currentlyVerified) "Verification revoked" else "User verified")
                        }
                        is Resource.Failure -> _uiState.update { it.copy(error = res.message) }
                        is Resource.Loading -> Unit
                    }
                }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
    fun clearMessage() { _uiState.update { it.copy(message = null) } }
}
