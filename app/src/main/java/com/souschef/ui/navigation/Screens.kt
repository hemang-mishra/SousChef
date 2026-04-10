package com.souschef.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * All navigation destinations for the SousChef app.
 *
 * Rules:
 * - Routes with no params → `data object`
 * - Routes with params → `data class` with typed constructor fields (never string paths)
 * - All must be @Serializable for Navigation 3
 */
sealed interface Screens : NavKey {

    // ── Auth ──────────────────────────────────────────────
    @Serializable data object NavLoginRoute : Screens
    @Serializable data object NavSignUpRoute : Screens

    // ── Main ─────────────────────────────────────────────
    @Serializable data object NavHomeRoute : Screens

    // ── Design System ────────────────────────────────────
    @Serializable data object NavDesignTestRoute : Screens

    // ── Recipe (Phase 2+) ─────────────────────────────────
    @Serializable data object NavCreateRecipeRoute : Screens
    @Serializable data class NavRecipeDetailRoute(val recipeId: String) : Screens
    @Serializable data class NavRecipeOverviewRoute(val recipeId: String) : Screens
    @Serializable data class NavCookingModeRoute(
        val recipeId: String,
        val selectedServings: Int,
        val spiceLevel: Float,
        val saltLevel: Float,
        val sweetnessLevel: Float
    ) : Screens

    // ── Saved / Profile (Phase 7+) ────────────────────────
    @Serializable data object NavSavedRecipesRoute : Screens
    @Serializable data object NavProfileRoute : Screens

    // ── Ingredient Library (Phase 1A) ────────────────────
    @Serializable data object NavIngredientLibraryRoute : Screens
    @Serializable data class NavAddEditIngredientRoute(val ingredientId: String? = null) : Screens

    // ── AI Recipe Generation (Phase 6) ───────────────────
    @Serializable data class NavAiStepGenerationRoute(val recipeId: String) : Screens

    // ── Admin (Phase 8+) ─────────────────────────────────
    @Serializable data object NavAdminRoute : Screens
}

