package com.souschef.repository.auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.souschef.model.auth.UserProfile
import com.souschef.service.auth.FirebaseAuthService
import com.souschef.util.Resource
import com.souschef.util.safeFirestoreCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Firebase-backed implementation of [AuthRepository].
 *
 * Rules:
 * - Every method emits Resource.Loading first.
 * - Service calls wrapped in safeFirestoreCall for error mapping.
 * - After sign-in/sign-up, ensures a Firestore user profile exists.
 */
class FirebaseAuthRepository(
    private val service: FirebaseAuthService
) : AuthRepository {

    override fun signInWithGoogle(idToken: String): Flow<Resource<UserProfile>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.signInWithGoogle(idToken) }
        when (result) {
            is Resource.Success -> emit(ensureUserProfile(result.data))
            is Resource.Failure -> emit(Resource.failure(result.error, result.message))
            is Resource.Loading -> { /* already emitted */ }
        }
    }

    override fun signInWithEmail(email: String, password: String): Flow<Resource<UserProfile>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.signInWithEmail(email, password) }
        when (result) {
            is Resource.Success -> emit(ensureUserProfile(result.data))
            is Resource.Failure -> emit(Resource.failure(result.error, result.message))
            is Resource.Loading -> { /* already emitted */ }
        }
    }

    override fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Flow<Resource<UserProfile>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.signUpWithEmail(email, password, displayName) }
        when (result) {
            is Resource.Success -> {
                val user = result.data
                val profile = UserProfile(
                    uid = user.uid,
                    email = user.email ?: email,
                    displayName = user.displayName ?: displayName,
                    profileImageUrl = user.photoUrl?.toString(),
                    role = "user",
                    isVerifiedChef = false,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                val createResult = safeFirestoreCall { service.createUserProfile(profile) }
                when (createResult) {
                    is Resource.Success -> emit(Resource.success(profile))
                    is Resource.Failure -> emit(Resource.failure(createResult.error, createResult.message))
                    is Resource.Loading -> { /* already emitted */ }
                }
            }
            is Resource.Failure -> emit(Resource.failure(result.error, result.message))
            is Resource.Loading -> { /* already emitted */ }
        }
    }

    override fun signOut(): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        val result = safeFirestoreCall { service.signOut() }
        emit(result)
    }

    override fun getCurrentUserProfile(): Flow<Resource<UserProfile>> = flow {
        emit(Resource.loading())
        val firebaseUser = service.getCurrentUser()
        if (firebaseUser == null) {
            emit(Resource.failure(message = "No authenticated user"))
            return@flow
        }
        val result = safeFirestoreCall { service.getUserProfile(firebaseUser.uid) }
        when (result) {
            is Resource.Success -> {
                val profile = result.data
                if (profile != null) {
                    emit(Resource.success(profile))
                } else {
                    // Profile doesn't exist yet — create one from FirebaseUser data
                    val newProfile = UserProfile(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        profileImageUrl = firebaseUser.photoUrl?.toString(),
                        role = "user",
                        isVerifiedChef = false,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                    safeFirestoreCall { service.createUserProfile(newProfile) }
                    emit(Resource.success(newProfile))
                }
            }
            is Resource.Failure -> emit(Resource.failure(result.error, result.message))
            is Resource.Loading -> { /* already emitted */ }
        }
    }

    override fun observeAuthState(): Flow<FirebaseUser?> = service.observeAuthState()

    // ── Phase 8: Admin ────────────────────────────────────────

    override fun getAllUsers(): Flow<List<UserProfile>> = service.getAllUsersFlow()

    override fun setVerifiedChef(uid: String, isVerified: Boolean): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        emit(safeFirestoreCall { service.setVerifiedChef(uid, isVerified) })
    }

    override fun getUserCount(): Flow<Resource<Int>> = flow {
        emit(Resource.loading())
        emit(safeFirestoreCall { service.getUserCount() })
    }

    override fun getRecipeCount(): Flow<Resource<Int>> = flow {
        emit(Resource.loading())
        emit(safeFirestoreCall { service.getRecipeCount() })
    }

    // ── Private helpers ──────────────────────────────────────

    /**
     * After successful Firebase Auth, fetch or create the Firestore profile.
     */
    private suspend fun ensureUserProfile(firebaseUser: FirebaseUser): Resource<UserProfile> {
        val profileResult = safeFirestoreCall { service.getUserProfile(firebaseUser.uid) }
        return when (profileResult) {
            is Resource.Success -> {
                val existing = profileResult.data
                if (existing != null) {
                    Resource.success(existing)
                } else {
                    // First sign-in — create profile
                    val newProfile = UserProfile(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: "",
                        profileImageUrl = firebaseUser.photoUrl?.toString(),
                        role = "user",
                        isVerifiedChef = false,
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                    safeFirestoreCall { service.createUserProfile(newProfile) }
                    Resource.success(newProfile)
                }
            }
            is Resource.Failure -> Resource.failure(profileResult.error, profileResult.message)
            is Resource.Loading -> Resource.loading()
        }
    }
}

