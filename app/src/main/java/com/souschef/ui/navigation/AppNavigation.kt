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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.souschef.model.auth.UserProfile
import com.souschef.ui.components.FullScreenLoader
import com.souschef.ui.screens.auth.login.LoginScreen
import com.souschef.ui.screens.auth.signup.SignUpScreen
import com.souschef.ui.screens.designtest.DesignTestScreen
import com.souschef.ui.screens.home.HomeScreen
import com.souschef.ui.screens.ingredient.addedit.AddEditIngredientScreen
import com.souschef.ui.screens.ingredient.addedit.AddEditIngredientViewModel
import com.souschef.ui.screens.ingredient.library.IngredientLibraryScreen
import com.souschef.ui.screens.recipe.create.CreateRecipeScreen
import com.souschef.ui.screens.recipe.create.CreateRecipeViewModel
import com.souschef.ui.screens.recipe.overview.RecipeOverviewScreen
import com.souschef.ui.screens.recipe.overview.RecipeOverviewViewModel
import com.souschef.ui.theme.SousChefTheme
import com.souschef.ui.viewmodels.AppViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Root navigation host for SousChef.
 *
 * Auth-aware routing:
 * - Shows a splash/loading while initial auth check is in progress.
 * - If logged in → NavHomeRoute.
 * - If not logged in → NavLoginRoute.
 */
@Composable
fun AppNavigation() {
    val appViewModel: AppViewModel = koinInject()
    val isLoggedIn by appViewModel.isLoggedIn.collectAsState()
    val authChecked by appViewModel.authChecked.collectAsState()

    // Show loading until initial auth check completes
    if (!authChecked) {
        SousChefTheme {
            FullScreenLoader(message = "Loading…")
        }
        return
    }

    val startDestination: Screens = if (isLoggedIn) Screens.NavHomeRoute else Screens.NavLoginRoute
    val backstack = rememberNavBackStack(startDestination)

    // When auth state changes after initial load, redirect
    LaunchedEffect(isLoggedIn, authChecked) {
        if (!authChecked) return@LaunchedEffect
        val currentKey = backstack.lastOrNull()
        if (isLoggedIn && (currentKey is Screens.NavLoginRoute || currentKey is Screens.NavSignUpRoute)) {
            // Logged in but on auth screen — go to Home
            backstack.clear()
            backstack.add(Screens.NavHomeRoute)
        } else if (!isLoggedIn && currentKey !is Screens.NavLoginRoute && currentKey !is Screens.NavSignUpRoute && currentKey !is Screens.NavDesignTestRoute) {
            // Logged out — go to Login
            backstack.clear()
            backstack.add(Screens.NavLoginRoute)
        }
    }

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

            // ── Auth ─────────────────────────────────────
            entry<Screens.NavLoginRoute> {
                LoginScreen(
                    onNavigateToSignUp = { backstack.add(Screens.NavSignUpRoute) },
                    onLoginSuccess = { profile ->
                        appViewModel.setCurrentUser(profile)
                        backstack.clear()
                        backstack.add(Screens.NavHomeRoute)
                    }
                )
            }

            entry<Screens.NavSignUpRoute> {
                SignUpScreen(
                    onNavigateToLogin = {
                        if (backstack.size > 1) {
                            backstack.removeAt(backstack.size - 1)
                        }
                    },
                    onSignUpSuccess = { profile ->
                        appViewModel.setCurrentUser(profile)
                        backstack.clear()
                        backstack.add(Screens.NavHomeRoute)
                    }
                )
            }

            // ── Home ─────────────────────────────────────
            entry<Screens.NavHomeRoute> {
                val currentUser by appViewModel.currentUser.collectAsState()
                HomeScreen(
                    displayName = currentUser?.displayName,
                    onCreateRecipe = { backstack.add(Screens.NavCreateRecipeRoute) },
                    onSignOut = { appViewModel.signOut() },
                    onDesignTest = { backstack.add(Screens.NavDesignTestRoute) },
                    onIngredientLibrary = { backstack.add(Screens.NavIngredientLibraryRoute) },
                    onTestOverview = { backstack.add(Screens.NavRecipeOverviewRoute("test_recipe_id")) }
                )
            }

            // ── Create Recipe ────────────────────────────
            entry<Screens.NavCreateRecipeRoute> {
                val currentUser = appViewModel.currentUser.value ?: UserProfile()
                val viewModel: CreateRecipeViewModel = koinInject { parametersOf(currentUser) }
                CreateRecipeScreen(
                    onBack = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                    onRecipeSaved = { recipeId ->
                        if (backstack.size > 1) backstack.removeAt(backstack.size - 1)
                        backstack.add(Screens.NavRecipeOverviewRoute(recipeId))
                    },
                    viewModel = viewModel
                )
            }

            // ── Design Test ──────────────────────────────
            entry<Screens.NavDesignTestRoute> {
                DesignTestScreen(
                    onNavigateHome = { backstack.add(Screens.NavHomeRoute) }
                )
            }

            // ── Ingredient Library (Phase 1A) ────────────
            entry<Screens.NavIngredientLibraryRoute> {
                IngredientLibraryScreen(
                    onBack = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                    onAddIngredient = { backstack.add(Screens.NavAddEditIngredientRoute(ingredientId = null)) },
                    onEditIngredient = { id -> backstack.add(Screens.NavAddEditIngredientRoute(ingredientId = id)) }
                )
            }

            entry<Screens.NavAddEditIngredientRoute> { route ->
                val currentUser = appViewModel.currentUser.value ?: UserProfile()
                val viewModel: AddEditIngredientViewModel = koinInject {
                    parametersOf(currentUser, route.ingredientId)
                }
                AddEditIngredientScreen(
                    onBack = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                    onSaved = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                    viewModel = viewModel
                )
            }

            // ── Recipe Detail / Overview (Phase 3) ───────
            entry<Screens.NavRecipeDetailRoute> { PlaceholderScreen("Recipe Detail — Phase 3") }
            entry<Screens.NavRecipeOverviewRoute> { route ->
                val viewModel: RecipeOverviewViewModel = koinInject { parametersOf(route.recipeId) }
                RecipeOverviewScreen(
                    onBack = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                    onStartCooking = { servings, spice, salt, sweetness ->
                        backstack.add(
                            Screens.NavCookingModeRoute(
                                recipeId = route.recipeId,
                                selectedServings = servings,
                                spiceLevel = spice,
                                saltLevel = salt,
                                sweetnessLevel = sweetness
                            )
                        )
                    },
                    viewModel = viewModel
                )
            }
            entry<Screens.NavCookingModeRoute> { PlaceholderScreen("Cooking Mode — Phase 4") }

            // ── Saved / Profile (Phase 7) ────────────────
            entry<Screens.NavSavedRecipesRoute> { PlaceholderScreen("Saved Recipes — Phase 7") }
            entry<Screens.NavProfileRoute> { PlaceholderScreen("Profile — Phase 7") }

            // ── Admin (Phase 8) ──────────────────────────
            entry<Screens.NavAdminRoute> { PlaceholderScreen("Admin Panel — Phase 8") }
        }
    )
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
