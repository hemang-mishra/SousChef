package com.souschef.ui.screens.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.auth.UserProfile
import com.souschef.repository.auth.AuthRepository
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Login screen. Registered as `factory` in Koin.
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _emailError = MutableStateFlow<String?>(null)
    private val _passwordError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isGoogleLoading = MutableStateFlow(false)
    private val _generalError = MutableStateFlow<String?>(null)

    /** Single observable UI state derived from all internal flows. */
    val uiState: StateFlow<LoginUiState> = combine(
        _email, _password, _emailError, _passwordError,
        combine(_isLoading, _isGoogleLoading, _generalError) { a, b, c -> Triple(a, b, c) }
    ) { email, password, emailErr, pwErr, (loading, googleLoading, generalErr) ->
        LoginUiState(
            email = email,
            password = password,
            emailError = emailErr,
            passwordError = pwErr,
            isLoading = loading,
            isGoogleLoading = googleLoading,
            generalError = generalErr
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoginUiState())

    // Callback on success — set by the Screen composable to notify AppViewModel
    private var _onLoginSuccess: ((UserProfile) -> Unit)? = null

    fun setOnLoginSuccess(callback: (UserProfile) -> Unit) {
        _onLoginSuccess = callback
    }

    // ── User Actions ─────────────────────────────────────────

    fun onEmailChange(email: String) {
        _email.value = email
        _emailError.value = null
        _generalError.value = null
    }

    fun onPasswordChange(password: String) {
        _password.value = password
        _passwordError.value = null
        _generalError.value = null
    }

    fun onSignIn() {
        if (!validateInputs()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _generalError.value = null

            authRepository.signInWithEmail(_email.value.trim(), _password.value).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _onLoginSuccess?.invoke(resource.data)
                    }
                    is Resource.Failure -> {
                        _isLoading.value = false
                        _generalError.value = resource.message ?: "Sign in failed. Please try again."
                    }
                    is Resource.Loading -> { /* keep spinner */ }
                }
            }
        }
    }

    fun onGoogleSignIn(idToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGoogleLoading.value = true
            _generalError.value = null

            authRepository.signInWithGoogle(idToken).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _isGoogleLoading.value = false
                        _onLoginSuccess?.invoke(resource.data)
                    }
                    is Resource.Failure -> {
                        _isGoogleLoading.value = false
                        _generalError.value = resource.message ?: "Google sign-in failed."
                    }
                    is Resource.Loading -> { /* keep spinner */ }
                }
            }
        }
    }

    fun onGoogleSignInFailed() {
        _isGoogleLoading.value = false
        _generalError.value = "Google sign-in was cancelled or failed."
    }

    fun clearError() {
        _generalError.value = null
    }

    // ── Validation ───────────────────────────────────────────

    private fun validateInputs(): Boolean {
        var valid = true
        if (_email.value.isBlank()) {
            _emailError.value = "Email is required"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(_email.value.trim()).matches()) {
            _emailError.value = "Enter a valid email address"
            valid = false
        }
        if (_password.value.isBlank()) {
            _passwordError.value = "Password is required"
            valid = false
        } else if (_password.value.length < 6) {
            _passwordError.value = "Password must be at least 6 characters"
            valid = false
        }
        return valid
    }
}

