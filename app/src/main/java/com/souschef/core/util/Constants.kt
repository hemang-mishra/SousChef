package com.souschef.core.util

/**
 * Application-wide constants.
 * Keep all magic strings and values here for consistency.
 */
object Constants {

    // Authentication
    object Auth {
        const val MIN_PASSWORD_LENGTH = 8
        const val EMAIL_VERIFICATION_TIMEOUT_SECONDS = 60L
    }

    // Timeouts
    object Timeout {
        const val DEFAULT_NETWORK_TIMEOUT_MS = 30_000L
        const val AUTH_OPERATION_TIMEOUT_MS = 15_000L
    }

    // SharedPreferences Keys
    object Prefs {
        const val PREFS_NAME = "souschef_prefs"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}

