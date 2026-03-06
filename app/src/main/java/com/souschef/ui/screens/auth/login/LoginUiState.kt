package com.souschef.ui.screens.auth.login

/**
 * Login screen UI state — single immutable data class.
 * All fields have sensible defaults.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val generalError: String? = null
)

