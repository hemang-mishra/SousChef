package com.souschef.domain.repository

import com.souschef.core.util.Result
import com.souschef.domain.model.AuthState
import com.souschef.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Implementation is in the data layer using Firebase Auth.
 */
interface AuthRepository {

    /**
     * Flow of authentication state changes.
     * Emits whenever the user's auth state changes (login, logout, verification status).
     */
    val authStateFlow: Flow<AuthState>

    /**
     * Returns the currently authenticated user, or null if not authenticated.
     */
    fun getCurrentUser(): User?

    /**
     * Signs in with email and password.
     * @return Result.Success with User if successful, Result.Error otherwise.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User>

    /**
     * Creates a new account with email and password.
     * Automatically sends a verification email on success.
     * @return Result.Success with User if successful, Result.Error otherwise.
     */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String? = null): Result<User>

    /**
     * Signs in with Google credentials using Credential Manager.
     * @return Result.Success with User if successful, Result.Error otherwise.
     */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Sends a verification email to the current user's email address.
     * @return Result.Success if sent, Result.Error otherwise.
     */
    suspend fun sendVerificationEmail(): Result<Unit>

    /**
     * Reloads the current user's data from Firebase.
     * Useful after email verification to update the verification status.
     * @return Result.Success with updated User, Result.Error otherwise.
     */
    suspend fun reloadUser(): Result<User>

    /**
     * Sends a password reset email to the specified address.
     * @return Result.Success if sent, Result.Error otherwise.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Signs out the current user.
     */
    suspend fun signOut()
}

