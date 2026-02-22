package com.souschef.domain.model

/**
 * Domain model representing the current user.
 * This is a clean abstraction over FirebaseUser.
 */
data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val providerId: String
) {
    companion object {
        val EMPTY = User(
            uid = "",
            email = null,
            displayName = null,
            photoUrl = null,
            isEmailVerified = false,
            providerId = ""
        )
    }
}

