package com.souschef.model.auth

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Firestore document model for a user profile.
 * Document path: `users/{uid}`
 *
 * All fields have defaults so Firestore `toObject<UserProfile>()` works correctly.
 */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String? = null,
    val role: String = "user",
    @get:PropertyName("isVerifiedChef")
    @set:PropertyName("isVerifiedChef")
    var isVerifiedChef: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    /** True when this profile has admin privileges. */
    val isAdmin: Boolean get() = role == "admin"
}

