package com.souschef.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.souschef.core.util.Result
import com.souschef.domain.model.AuthState
import com.souschef.domain.model.User
import com.souschef.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of [AuthRepository].
 * Handles all authentication operations using Firebase Auth.
 */
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val authStateFlow: Flow<AuthState> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val state = when {
                firebaseUser == null -> AuthState.Unauthenticated
                !firebaseUser.isEmailVerified &&
                    firebaseUser.providerData.any { it.providerId == "password" } ->
                    AuthState.Unverified(firebaseUser.toDomainUser())
                else -> AuthState.Authenticated(firebaseUser.toDomainUser())
            }
            trySend(state)
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toDomainUser()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.Success(user.toDomainUser())
            } else {
                Result.Error(Exception("Sign in failed"), "Unable to sign in")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                // Update display name if provided
                if (!displayName.isNullOrBlank()) {
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = displayName
                    }
                    user.updateProfile(profileUpdates).await()
                }

                // Send verification email
                user.sendEmailVerification().await()

                Result.Success(user.toDomainUser())
            } else {
                Result.Error(Exception("Sign up failed"), "Unable to create account")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user

            if (user != null) {
                Result.Success(user.toDomainUser())
            } else {
                Result.Error(Exception("Google sign in failed"), "Unable to sign in with Google")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Result.Success(Unit)
            } else {
                Result.Error(Exception("No user signed in"), "Please sign in first")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun reloadUser(): Result<User> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.reload().await()
                // Re-fetch the user after reload
                val refreshedUser = firebaseAuth.currentUser
                if (refreshedUser != null) {
                    Result.Success(refreshedUser.toDomainUser())
                } else {
                    Result.Error(Exception("User not found after reload"), "Session expired")
                }
            } else {
                Result.Error(Exception("No user signed in"), "Please sign in first")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * Extension function to convert FirebaseUser to domain User model.
     */
    private fun FirebaseUser.toDomainUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isEmailVerified = isEmailVerified,
            providerId = providerData.firstOrNull()?.providerId ?: "unknown"
        )
    }
}

