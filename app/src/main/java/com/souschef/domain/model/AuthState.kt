package com.souschef.domain.model

/**
 * Represents the authentication state of the user.
 * Used to drive navigation decisions.
 */
sealed class AuthState {
    /**
     * Initial state - auth status not yet determined.
     */
    data object Loading : AuthState()

    /**
     * User is not authenticated.
     */
    data object Unauthenticated : AuthState()

    /**
     * User is authenticated but email is not verified.
     */
    data class Unverified(val user: User) : AuthState()

    /**
     * User is fully authenticated and email is verified.
     */
    data class Authenticated(val user: User) : AuthState()
}

