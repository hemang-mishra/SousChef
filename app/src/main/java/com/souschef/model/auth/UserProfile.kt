package com.souschef.model.auth

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
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
    /**
     * Optional explicit admin flag stored in Firestore.
     * When `true`, the user is treated as admin regardless of the [role] field.
     * Allows admin status to be granted directly from the Firebase console
     * without requiring the legacy `role: "admin"` value.
     */
    @get:PropertyName("isAdmin")
    @set:PropertyName("isAdmin")
    var isAdminFlag: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    @get:Exclude
    /** True when this profile has admin privileges. */
    val isAdmin: Boolean get() = isAdminFlag || role == "admin"
}

