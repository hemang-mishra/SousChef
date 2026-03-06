package com.souschef.ui.screens.auth.signup

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
 * ViewModel for the Sign-Up screen. Registered as `factory` in Koin.
 */
class SignUpViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _displayName = MutableStateFlow("")
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _confirmPassword = MutableStateFlow("")
    private val _nameError = MutableStateFlow<String?>(null)
    private val _emailError = MutableStateFlow<String?>(null)
    private val _passwordError = MutableStateFlow<String?>(null)
    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _generalError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SignUpUiState> = combine(
        combine(_displayName, _email) { name, email -> Pair(name, email) },
        combine(_password, _confirmPassword) { pw, cpw -> Pair(pw, cpw) },
        combine(_nameError, _emailError) { ne, ee -> Pair(ne, ee) },
        combine(_passwordError, _confirmPasswordError) { pe, cpe -> Pair(pe, cpe) },
        combine(_isLoading, _generalError) { loading, error -> Pair(loading, error) }
    ) { (name, email), (pw, cpw), (nameErr, emailErr), (pwErr, cpwErr), (loading, error) ->
        SignUpUiState(
            displayName = name,
            email = email,
            password = pw,
            confirmPassword = cpw,
            nameError = nameErr,
            emailError = emailErr,
            passwordError = pwErr,
            confirmPasswordError = cpwErr,
            isLoading = loading,
            generalError = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SignUpUiState())

    private var _onSignUpSuccess: ((UserProfile) -> Unit)? = null

    fun setOnSignUpSuccess(callback: (UserProfile) -> Unit) {
        _onSignUpSuccess = callback
    }

    // ── User Actions ─────────────────────────────────────────

    fun onNameChange(name: String) {
        _displayName.value = name
        _nameError.value = null
        _generalError.value = null
    }

    fun onEmailChange(email: String) {
        _email.value = email
        _emailError.value = null
        _generalError.value = null
    }

    fun onPasswordChange(password: String) {
        _password.value = password
        _passwordError.value = null
        _confirmPasswordError.value = null
        _generalError.value = null
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        _confirmPasswordError.value = null
        _generalError.value = null
    }

    fun onSignUp() {
        if (!validateInputs()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _generalError.value = null

            authRepository.signUpWithEmail(
                email = _email.value.trim(),
                password = _password.value,
                displayName = _displayName.value.trim()
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _isLoading.value = false
                        _onSignUpSuccess?.invoke(resource.data)
                    }
                    is Resource.Failure -> {
                        _isLoading.value = false
                        _generalError.value = resource.message ?: "Sign up failed. Please try again."
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

    private fun validateInputs(): Boolean {
        var valid = true

        if (_displayName.value.isBlank()) {
            _nameError.value = "Name is required"
            valid = false
        } else if (_displayName.value.trim().length < 2) {
            _nameError.value = "Name must be at least 2 characters"
            valid = false
        }

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

        if (_confirmPassword.value.isBlank()) {
            _confirmPasswordError.value = "Please confirm your password"
            valid = false
        } else if (_password.value != _confirmPassword.value) {
            _confirmPasswordError.value = "Passwords do not match"
            valid = false
        }

        return valid
    }
}

