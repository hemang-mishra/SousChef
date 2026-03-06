package com.souschef.service.auth

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.souschef.model.auth.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Raw Firebase Auth + Firestore operations for user management.
 * No business logic — just SDK calls. Repository wraps these in Resource<T>.
 */
class FirebaseAuthService(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    // ── Auth operations ──────────────────────────────────────

    suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user ?: throw IllegalStateException("Google sign-in returned null user")
    }

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("Email sign-in returned null user")
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Email sign-up returned null user")
        // Set display name on the Firebase Auth profile
        val profileUpdates = userProfileChangeRequest { this.displayName = displayName }
        user.updateProfile(profileUpdates).await()
        return user
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Emits the current [FirebaseUser] (or null) whenever auth state changes.
     * Uses callbackFlow + awaitClose for proper cleanup.
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // ── Firestore profile operations ─────────────────────────

    suspend fun getUserProfile(uid: String): UserProfile? {
        return usersCollection.document(uid).get().await()
            .toObject(UserProfile::class.java)
    }

    suspend fun createUserProfile(profile: UserProfile) {
        usersCollection.document(profile.uid).set(profile).await()
    }

    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>) {
        val timestamped = updates.toMutableMap().apply {
            put("updatedAt", Timestamp.now())
        }
        usersCollection.document(uid).update(timestamped).await()
    }
}

