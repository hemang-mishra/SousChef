package com.souschef.repository.auth

import com.google.firebase.auth.FirebaseUser
import com.souschef.model.auth.UserProfile
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Authentication repository interface.
 * All methods return Flow<Resource<T>> — emit Loading first, then Success or Failure.
 */
interface AuthRepository {
    fun signInWithGoogle(idToken: String): Flow<Resource<UserProfile>>
    fun signInWithEmail(email: String, password: String): Flow<Resource<UserProfile>>
    fun signUpWithEmail(email: String, password: String, displayName: String): Flow<Resource<UserProfile>>
    fun signOut(): Flow<Resource<Unit>>
    fun getCurrentUserProfile(): Flow<Resource<UserProfile>>
    fun observeAuthState(): Flow<FirebaseUser?>
}

