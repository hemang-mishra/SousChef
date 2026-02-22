package com.souschef.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.core.util.Result
import com.souschef.core.util.isValidEmail
import com.souschef.core.util.isValidPassword
import com.souschef.core.util.toUserMessage
import com.souschef.domain.model.AuthState
import com.souschef.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication screens.
 * Manages login, sign up, and email verification flows.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Auth state from repository - drives navigation.
     */
    val authState: StateFlow<AuthState> = authRepository.authStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )

    /**
     * UI state for form inputs and loading.
     */
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * One-time events (snackbars, navigation).
     */
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    // ==================== Form Input Handlers ====================

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun onDisplayNameChange(displayName: String) {
        _uiState.update { it.copy(displayName = displayName) }
    }

    // ==================== Authentication Actions ====================

    /**
     * Sign in with email and password.
     */
    fun signIn() {
        val state = _uiState.value

        // Validate inputs
        if (!validateSignInInputs(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = authRepository.signInWithEmail(state.email, state.password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    // Navigation is handled by authState observer
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowError(result.exception.toUserMessage()))
                }
                is Result.Loading -> {
                    // Already handled by isLoading state
                }
            }
        }
    }

    /**
     * Sign up with email and password.
     */
    fun signUp() {
        val state = _uiState.value

        // Validate inputs
        if (!validateSignUpInputs(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = authRepository.signUpWithEmail(
                email = state.email,
                password = state.password,
                displayName = state.displayName.takeIf { it.isNotBlank() }
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowMessage("Verification email sent. Please check your inbox."))
                    // Navigation to verification screen handled by authState
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowError(result.exception.toUserMessage()))
                }
                is Result.Loading -> {
                    // Already handled by isLoading state
                }
            }
        }
    }

    /**
     * Sign in with Google ID token.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    // Navigation handled by authState
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowError(result.exception.toUserMessage()))
                }
                is Result.Loading -> {
                    // Already handled by isLoading state
                }
            }
        }
    }

    /**
     * Resend verification email.
     */
    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = authRepository.sendVerificationEmail()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowMessage("Verification email sent"))
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowError(result.exception.toUserMessage()))
                }
                is Result.Loading -> {
                    // Already handled by isLoading state
                }
            }
        }
    }

    /**
     * Check if email has been verified.
     * Called when user taps "I've Verified" button.
     */
    fun checkEmailVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = authRepository.reloadUser()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    if (result.data.isEmailVerified) {
                        _events.emit(AuthEvent.ShowMessage("Email verified successfully!"))
                        // authState will update automatically
                    } else {
                        _events.emit(AuthEvent.ShowError("Email not verified yet. Please check your inbox."))
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowError(result.exception.toUserMessage()))
                }
                is Result.Loading -> {
                    // Already handled by isLoading state
                }
            }
        }
    }

    /**
     * Send password reset email.
     */
    fun sendPasswordResetEmail() {
        val email = _uiState.value.email

        if (!email.isValidEmail()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowMessage("Password reset email sent"))
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.ShowError(result.exception.toUserMessage()))
                }
                is Result.Loading -> {
                    // Already handled by isLoading state
                }
            }
        }
    }

    /**
     * Sign out the current user.
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    /**
     * Clear form state.
     */
    fun clearForm() {
        _uiState.update { AuthUiState() }
    }

    // ==================== Validation ====================

    private fun validateSignInInputs(state: AuthUiState): Boolean {
        var isValid = true

        if (!state.email.isValidEmail()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            isValid = false
        }

        if (state.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
            isValid = false
        }

        return isValid
    }

    private fun validateSignUpInputs(state: AuthUiState): Boolean {
        var isValid = true

        if (!state.email.isValidEmail()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            isValid = false
        }

        if (!state.password.isValidPassword()) {
            _uiState.update {
                it.copy(passwordError = "Password must be at least 8 characters with letters and numbers")
            }
            isValid = false
        }

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords don't match") }
            isValid = false
        }

        return isValid
    }
}

/**
 * UI state for authentication screens.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false
)

/**
 * One-time events for authentication screens.
 */
sealed class AuthEvent {
    data class ShowMessage(val message: String) : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

