package com.souschef.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.souschef.ui.navigation.Screens
import com.souschef.ui.theme.AppColors

/**
 * Destinations for the bottom navigation bar.
 */
enum class BottomNavDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Screens
) {
    HOME(
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        route = Screens.NavHomeRoute
    ),
    MY_RECIPES(
        label = "My Recipes",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        route = Screens.NavSavedRecipesRoute
    ),
    PROFILE(
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        route = Screens.NavProfileRoute
    )
}

/**
 * Bottom navigation bar with premium styling.
 *
 * Shows Home, My Recipes, a center Create FAB, Ingredients, and Profile.
 */
@Composable
fun SousChefBottomNavBar(
    currentRoute: Screens?,
    onNavigate: (Screens) -> Unit,
    onCreateRecipe: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        // Home
        val homeSelected = currentRoute is Screens.NavHomeRoute
        NavigationBarItem(
            selected = homeSelected,
            onClick = { onNavigate(Screens.NavHomeRoute) },
            icon = {
                Icon(
                    if (homeSelected) BottomNavDestination.HOME.selectedIcon
                    else BottomNavDestination.HOME.unselectedIcon,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors()
        )

        // My Recipes
        val recipesSelected = currentRoute is Screens.NavSavedRecipesRoute
        NavigationBarItem(
            selected = recipesSelected,
            onClick = { onNavigate(Screens.NavSavedRecipesRoute) },
            icon = {
                Icon(
                    if (recipesSelected) BottomNavDestination.MY_RECIPES.selectedIcon
                    else BottomNavDestination.MY_RECIPES.unselectedIcon,
                    contentDescription = "My Recipes"
                )
            },
            label = { Text("Recipes", style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors()
        )

        // Center FAB — Create
        NavigationBarItem(
            selected = false,
            onClick = onCreateRecipe,
            icon = {
                FloatingActionButton(
                    onClick = onCreateRecipe,
                    containerColor = AppColors.gold(),
                    contentColor = AppColors.onGold(),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create Recipe",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text("Create", style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors()
        )

        // Dispenser
        val dispenserSelected = currentRoute is Screens.NavDispenserRoute
        NavigationBarItem(
            selected = dispenserSelected,
            onClick = { onNavigate(Screens.NavDispenserRoute) },
            icon = {
                Icon(
                    Icons.Outlined.Science,
                    contentDescription = "Dispenser",
                    tint = if (dispenserSelected) AppColors.gold()
                    else AppColors.textTertiary()
                )
            },
            label = { Text("Dispenser", style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors()
        )

        // Profile
        val profileSelected = currentRoute is Screens.NavProfileRoute
        NavigationBarItem(
            selected = profileSelected,
            onClick = { onNavigate(Screens.NavProfileRoute) },
            icon = {
                Icon(
                    if (profileSelected) BottomNavDestination.PROFILE.selectedIcon
                    else BottomNavDestination.PROFILE.unselectedIcon,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", style = MaterialTheme.typography.labelSmall) },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppColors.gold(),
    selectedTextColor = AppColors.gold(),
    unselectedIconColor = AppColors.textTertiary(),
    unselectedTextColor = AppColors.textTertiary(),
    indicatorColor = AppColors.gold().copy(alpha = 0.12f)
)
