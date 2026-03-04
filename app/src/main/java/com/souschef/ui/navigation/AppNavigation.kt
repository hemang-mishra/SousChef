package com.souschef.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.souschef.ui.screens.designtest.DesignTestScreen


/**
 * Root navigation host for SousChef.
 * Uses Navigation 3 (androidx.navigation3 v1.0.1).
 *
 * Rules:
 * - New destinations: add to [Screens] sealed interface, then add an `entry<>` block here.
 * - Back: `backstack.removeAt(backstack.size - 1)`
 * - Forward: `backstack.add(destination)`
 */
@Composable
fun AppNavigation() {
    val backstack = rememberNavBackStack(Screens.NavDesignTestRoute)

    NavDisplay(
        backStack = backstack,
        transitionSpec = {
            ContentTransform(
                targetContentEnter = slideInHorizontally(tween(300)) { it },
                initialContentExit = slideOutHorizontally(tween(300)) { -it }
            )
        },
        popTransitionSpec = {
            ContentTransform(
                targetContentEnter = slideInHorizontally(tween(300)) { -it },
                initialContentExit = slideOutHorizontally(tween(300)) { it }
            )
        },
        entryProvider = entryProvider {

            // ── Design Test ──────────────────────────────
            entry<Screens.NavDesignTestRoute> {
                DesignTestScreen(
                    onNavigateHome = { backstack.add(Screens.NavHomeRoute) }
                )
            }

            // ── Auth (Phase 1) ────────────────────────────
            entry<Screens.NavLoginRoute> { PlaceholderScreen("Login — Coming in Phase 1") }
            entry<Screens.NavSignUpRoute> { PlaceholderScreen("Sign Up — Coming in Phase 1") }

            // ── Home (Phase 7) ─────────────────────────────
            entry<Screens.NavHomeRoute> { PlaceholderScreen("Home Feed — Coming in Phase 7") }

            // ── Recipe (Phase 2+) ──────────────────────────
            entry<Screens.NavCreateRecipeRoute> { PlaceholderScreen("Create Recipe — Phase 2") }
            entry<Screens.NavRecipeDetailRoute> { PlaceholderScreen("Recipe Detail — Phase 3") }
            entry<Screens.NavRecipeOverviewRoute> { PlaceholderScreen("Recipe Overview — Phase 3") }
            entry<Screens.NavCookingModeRoute> { PlaceholderScreen("Cooking Mode — Phase 4") }

            // ── Saved / Profile (Phase 7) ──────────────────
            entry<Screens.NavSavedRecipesRoute> { PlaceholderScreen("Saved Recipes — Phase 7") }
            entry<Screens.NavProfileRoute> { PlaceholderScreen("Profile — Phase 7") }

            // ── Admin (Phase 8) ────────────────────────────
            entry<Screens.NavAdminRoute> { PlaceholderScreen("Admin Panel — Phase 8") }
        }
    )
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground)
    }
}
