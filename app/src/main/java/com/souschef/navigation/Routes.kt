package com.souschef.navigation

import com.souschef.domain.model.User
import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app.
 * Uses sealed interface for type-safe navigation.
 */
sealed interface Route {

    /**
     * Authentication flow routes.
     */
    @Serializable
    data object Login : Route

    @Serializable
    data object SignUp : Route

    @Serializable
    data object EmailVerification : Route

    @Serializable
    data object ForgotPassword : Route

    /**
     * Main app routes (post-authentication).
     */
    @Serializable
    data object Dashboard : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Favorites : Route

    @Serializable
    data object Profile : Route
}

/**
 * Navigation graph identifiers.
 */
object NavGraph {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}

