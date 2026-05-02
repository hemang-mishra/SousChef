package com.souschef.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.souschef.model.auth.UserProfile
import com.souschef.ui.components.AppNavigationShimmer
import com.souschef.ui.components.OfflineBanner
import com.souschef.ui.components.SousChefBottomNavBar
import com.souschef.ui.components.StartupPermissionGate
import com.souschef.ui.screens.admin.AdminDashboardScreen
import com.souschef.ui.screens.admin.AdminViewModel
import com.souschef.ui.screens.auth.login.LoginScreen
import com.souschef.ui.screens.auth.signup.SignUpScreen
import com.souschef.ui.screens.designtest.DesignTestScreen
import com.souschef.ui.screens.home.HomeScreen
import com.souschef.ui.screens.home.HomeViewModel
import com.souschef.ui.screens.ingredient.addedit.AddEditIngredientScreen
import com.souschef.ui.screens.ingredient.addedit.AddEditIngredientViewModel
import com.souschef.ui.screens.ingredient.library.IngredientLibraryScreen
import com.souschef.ui.screens.recipe.cooking.CookingModeScreen
import com.souschef.ui.screens.settings.SettingsScreen
import com.souschef.ui.screens.settings.SettingsViewModel
import com.souschef.ui.screens.recipe.cooking.CookingModeViewModel
import com.souschef.ui.screens.recipe.create.CreateRecipeScreen
import com.souschef.ui.screens.recipe.create.CreateRecipeViewModel
import com.souschef.ui.screens.recipe.overview.RecipeOverviewScreen
import com.souschef.ui.screens.recipe.overview.RecipeOverviewViewModel
import com.souschef.ui.screens.savedrecipes.SavedRecipesScreen
import com.souschef.ui.screens.savedrecipes.SavedRecipesViewModel
import com.souschef.ui.theme.SousChefTheme
import com.souschef.ui.viewmodels.AppViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/** Routes where the bottom nav bar is visible. */
private val BOTTOM_NAV_ROUTES = setOf(
    Screens.NavHomeRoute::class,
    Screens.NavSavedRecipesRoute::class,
    Screens.NavDispenserRoute::class,
    Screens.NavProfileRoute::class
)

/**
 * Root navigation host for SousChef.
 *
 * Auth-aware routing:
 * - Shows a splash/loading while initial auth check is in progress.
 * - If logged in → NavHomeRoute.
 * - If not logged in → NavLoginRoute.
 *
 * Bottom navigation is shown on main tab screens (Home, My Recipes, Ingredients, Profile)
 * and hidden during sub-flows (Create, Overview, Cooking, AI Generation).
 */
@Composable
fun AppNavigation() {
    val appViewModel: AppViewModel = koinInject()
    val isLoggedIn by appViewModel.isLoggedIn.collectAsState()
    val authChecked by appViewModel.authChecked.collectAsState()

    // Show loading until initial auth check completes
    if (!authChecked) {
        SousChefTheme {
            AppNavigationShimmer()
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
            backstack.clear()
            backstack.add(Screens.NavHomeRoute)
        } else if (!isLoggedIn && currentKey !is Screens.NavLoginRoute && currentKey !is Screens.NavSignUpRoute && currentKey !is Screens.NavDesignTestRoute) {
            backstack.clear()
            backstack.add(Screens.NavLoginRoute)
        }
    }

    // Determine if bottom nav should be visible
    val showBottomNav by remember {
        derivedStateOf {
            isLoggedIn && backstack.lastOrNull()?.let { it::class in BOTTOM_NAV_ROUTES } == true
        }
    }

    val currentRoute = backstack.lastOrNull() as? Screens
    val isOffline by appViewModel.isOffline.collectAsState()

    StartupPermissionGate(enabled = isLoggedIn) {
    Scaffold(
        topBar = { OfflineBanner(isOffline = isOffline) },
        bottomBar = {
            if (showBottomNav) {
                SousChefBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        // Replace current tab — don't stack tabs
                        if (currentRoute != destination) {
                            if (backstack.size > 1) {
                                // Pop back to root & add new tab
                                while (backstack.size > 1) {
                                    backstack.removeAt(backstack.size - 1)
                                }
                            }
                            if (backstack.lastOrNull() != destination) {
                                backstack[0] = destination
                            }
                        }
                    },
                    onCreateRecipe = {
                        backstack.add(Screens.NavCreateRecipeRoute())
                    }
                )
            }
        }
    ) { scaffoldPadding ->
        NavDisplay(
            backStack = backstack,
            modifier = Modifier.padding(scaffoldPadding),
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

                // ── Home (Phase 7) ───────────────────────────
                entry<Screens.NavHomeRoute> {
                    val currentUser by appViewModel.currentUser.collectAsState()
                    val user = currentUser ?: UserProfile()
                    val homeViewModel: HomeViewModel = koinInject()
                    LaunchedEffect(user.uid, user.displayName) {
                        homeViewModel.bind(user.uid, user.displayName)
                    }
                    HomeScreen(
                        viewModel = homeViewModel,
                        onRecipeTap = { recipeId ->
                            backstack.add(Screens.NavRecipeOverviewRoute(recipeId))
                        },
                        onGenerateSteps = { recipeId ->
                            backstack.add(Screens.NavCreateRecipeRoute(recipeId = recipeId))
                        },
                        onCreateRecipe = {
                            backstack.add(Screens.NavCreateRecipeRoute())
                        }
                    )
                }

                // ── My Recipes (Phase 7) ─────────────────────
                entry<Screens.NavSavedRecipesRoute> {
                    val currentUser by appViewModel.currentUser.collectAsState()
                    val user = currentUser ?: UserProfile()
                    val savedRecipesViewModel: SavedRecipesViewModel = koinInject {
                        parametersOf(user.uid, user.displayName)
                    }
                    SavedRecipesScreen(
                        viewModel = savedRecipesViewModel,
                        onRecipeTap = { recipeId ->
                            backstack.add(Screens.NavRecipeOverviewRoute(recipeId))
                        },
                        onGenerateSteps = { recipeId ->
                            backstack.add(Screens.NavCreateRecipeRoute(recipeId = recipeId))
                        },
                        onCreateRecipe = {
                            backstack.add(Screens.NavCreateRecipeRoute())
                        }
                    )
                }

                // ── Unified Settings (replaces Profile) ─────────
                entry<Screens.NavProfileRoute> {
                    val currentUser by appViewModel.currentUser.collectAsState()
                    val isAdmin by appViewModel.isAdmin.collectAsState()
                    val viewModel: SettingsViewModel = koinInject { parametersOf(currentUser) }
                    LaunchedEffect(currentUser) { viewModel.setProfile(currentUser) }
                    SettingsScreen(
                        viewModel = viewModel,
                        isAdmin = isAdmin,
                        onSignOut = { appViewModel.signOut() },
                        onOpenIngredientLibrary = { backstack.add(Screens.NavIngredientLibraryRoute) },
                        onOpenHardwareTest = { backstack.add(Screens.NavHardwareTestRoute) },
                        onOpenAdmin = { backstack.add(Screens.NavAdminRoute) }
                    )
                }

                // ── Create Recipe ────────────────────────────
                entry<Screens.NavCreateRecipeRoute> { route ->
                    val currentUser = appViewModel.currentUser.value ?: UserProfile()
                    val viewModel: CreateRecipeViewModel = koinInject { parametersOf(currentUser, route.recipeId) }
                    CreateRecipeScreen(
                        isEditMode = route.recipeId != null,
                        onBack = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                        onRecipeSaved = { recipeId ->
                            if (backstack.size > 1) backstack.removeAt(backstack.size - 1)
                            // Navigate directly to recipe overview (steps are saved as part of the wizard)
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

                // ── Phase 5: Hardware Integration (Dispenser) ────────────────
                entry<Screens.NavDispenserRoute> {
                    val viewModel: com.souschef.ui.screens.device.dispenser.DispenserViewModel = koinInject()
                    com.souschef.ui.screens.device.dispenser.DispenserScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { backstack.add(Screens.NavDispenserSettingsRoute) }
                    )
                }

                entry<Screens.NavDispenserSettingsRoute> {
                    val viewModel: com.souschef.ui.screens.device.settings.DispenserSettingsViewModel = koinInject()
                    com.souschef.ui.screens.device.settings.DispenserSettingsScreen(
                        viewModel = viewModel,
                        onBackPress = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                        onNavigateToGlobalIngredients = { backstack.add(Screens.NavIngredientLibraryRoute) },
                        onNavigateToHardwareTest = { backstack.add(Screens.NavHardwareTestRoute) }
                    )
                }

                entry<Screens.NavHardwareTestRoute> {
                    val viewModel: com.souschef.ui.screens.device.settings.HardwareTestViewModel = koinInject()
                    com.souschef.ui.screens.device.settings.HardwareTestScreen(
                        viewModel = viewModel,
                        onBackPress = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) }
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

                // ── Recipe Overview ──────────────────────────
                entry<Screens.NavRecipeOverviewRoute> { route ->
                    val currentUser = appViewModel.currentUser.value ?: UserProfile()
                    val viewModel: RecipeOverviewViewModel = koinInject { parametersOf(route.recipeId, currentUser) }
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
                        onEditRecipe = { recipeId ->
                            backstack.add(Screens.NavCreateRecipeRoute(recipeId = recipeId))
                        },
                        viewModel = viewModel
                    )
                }
                entry<Screens.NavCookingModeRoute> { route ->
                    val viewModel: CookingModeViewModel = koinInject {
                        parametersOf(
                            route.recipeId,
                            route.selectedServings,
                            route.spiceLevel,
                            route.saltLevel,
                            route.sweetnessLevel
                        )
                    }
                    CookingModeScreen(
                        onBack = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                        onFinished = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) },
                        viewModel = viewModel
                    )
                }

                // ── AI Step Generation (Phase 6) — now integrated into CreateRecipeRoute ──

                // ── Admin (Phase 8) ──────────────────────────
                entry<Screens.NavAdminRoute> {
                    val isAdmin by appViewModel.isAdmin.collectAsState()
                    val viewModel: AdminViewModel = koinInject()
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        isAdmin = isAdmin,
                        onBack = { if (backstack.size > 1) backstack.removeAt(backstack.size - 1) }
                    )
                }
            }
        )
    }
    } // close StartupPermissionGate
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