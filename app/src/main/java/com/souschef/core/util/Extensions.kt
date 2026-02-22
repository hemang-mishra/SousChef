package com.souschef.core.util

import android.util.Patterns

/**
 * Extension functions for common operations.
 */

/**
 * Validates if the string is a properly formatted email address.
 */
fun String.isValidEmail(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Validates if the string meets minimum password requirements.
 * Requirements:
 * - At least 8 characters
 * - Contains at least one digit
 * - Contains at least one letter
 */
fun String.isValidPassword(): Boolean {
    if (this.length < Constants.Auth.MIN_PASSWORD_LENGTH) return false
    val hasLetter = this.any { it.isLetter() }
    val hasDigit = this.any { it.isDigit() }
    return hasLetter && hasDigit
}

/**
 * Returns a user-friendly error message from Firebase Auth exceptions.
 */
fun Throwable.toUserMessage(): String {
    return when {
        message?.contains("email address is badly formatted", ignoreCase = true) == true ->
            "Please enter a valid email address"
        message?.contains("password is invalid", ignoreCase = true) == true ->
            "Incorrect password. Please try again"
        message?.contains("no user record", ignoreCase = true) == true ->
            "No account found with this email"
        message?.contains("email address is already in use", ignoreCase = true) == true ->
            "An account already exists with this email"
        message?.contains("network error", ignoreCase = true) == true ->
            "Network error. Please check your connection"
        message?.contains("too many requests", ignoreCase = true) == true ->
            "Too many attempts. Please try again later"
        message?.contains("weak password", ignoreCase = true) == true ->
            "Password is too weak. Use at least 8 characters with letters and numbers"
        else -> message ?: "An unexpected error occurred"
    }
}

