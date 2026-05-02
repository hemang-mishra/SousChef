package com.souschef.di

import com.souschef.ui.screens.auth.login.LoginViewModel
import com.souschef.ui.screens.auth.signup.SignUpViewModel
import com.souschef.ui.screens.device.dispenser.DispenserViewModel
import com.souschef.ui.screens.device.settings.DispenserSettingsViewModel
import com.souschef.ui.screens.device.settings.HardwareTestViewModel
import com.souschef.ui.screens.home.HomeViewModel
import com.souschef.ui.screens.ingredient.addedit.AddEditIngredientViewModel
import com.souschef.ui.screens.ingredient.library.IngredientLibraryViewModel
import com.souschef.ui.screens.recipe.cooking.CookingModeViewModel
import com.souschef.ui.screens.recipe.create.CreateRecipeViewModel
import com.souschef.ui.screens.recipe.overview.RecipeOverviewViewModel
import com.souschef.ui.screens.savedrecipes.SavedRecipesViewModel
import com.souschef.ui.screens.settings.SettingsViewModel
import com.souschef.ui.viewmodels.AppViewModel
import org.koin.dsl.module

/**
 * ViewModel module.
 *
 * Rules:
 * - `single` for app-level ViewModels (e.g. AppViewModel) that persist across the activity.
 * - `factory` for screen-scoped ViewModels created fresh on each navigation entry.
 * - `factory` with parametersOf for ViewModels that need runtime parameters.
 */
val viewModelModule = module {
    // App-level — single (survives navigation)
    single { AppViewModel(get(), get(), get(), get()) }

    // Auth screens — factory (fresh per navigation)
    factory { LoginViewModel(get()) }
    factory { SignUpViewModel(get()) }

    // Home — single (cached across navigations); rebinds via .bind(userId, userName)
    single { HomeViewModel(get(), get()) }
    factory { (userId: String, userName: String) ->
        SavedRecipesViewModel(get(), userId, userName)
    }

    // Recipe creation — factory, needs currentUser from AppViewModel and optional recipeId
    factory { (currentUser: com.souschef.model.auth.UserProfile, recipeId: String?) ->
        CreateRecipeViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
            get(org.koin.core.qualifier.named("appScope")),
            currentUser,
            recipeId
        )
    }

    // Recipe overview — factory, needs recipeId and currentUser
    factory { (recipeId: String, currentUser: com.souschef.model.auth.UserProfile) ->
        RecipeOverviewViewModel(get(), get(), get(), get(), get(), get(), recipeId, currentUser.uid)
    }

    // Cooking mode — factory, needs recipeId + serving/flavour params + Phase 5 dispense deps
    factory { (recipeId: String, servings: Int, spice: Float, salt: Float, sweetness: Float) ->
        CookingModeViewModel(
            get(), get(), get(), get(), get(), get(),
            get(), get(), get(),
            recipeId, servings, spice, salt, sweetness
        )
    }

    // AI Step Generation — now integrated into CreateRecipeViewModel

    // Ingredient Library — factory (fresh per navigation)
    factory { IngredientLibraryViewModel(get()) }

    // Add/Edit Ingredient — factory, needs currentUser + optional ingredientId
    factory { (currentUser: com.souschef.model.auth.UserProfile, ingredientId: String?) ->
        AddEditIngredientViewModel(get(), get(), get(), get(), currentUser, ingredientId)
    }

    // ── Phase 5: Device ───────────────────────────────────────────────────────

    // Dispenser screen — factory (BleDeviceManager injected as single)
    factory { DispenserViewModel(get(), get(), get(), get(), get()) }

    // Dispenser settings — factory
    factory { DispenserSettingsViewModel(get(), get()) }

    // Hardware Test
    factory { HardwareTestViewModel(get()) }

    // ── Unified Settings ──────────────────────────────────────────────────────
    factory { (profile: com.souschef.model.auth.UserProfile?) ->
        SettingsViewModel(get(), get(), get(), profile)
    }

    // ── Phase 8: Admin ────────────────────────────────────────────────────────
    factory { com.souschef.ui.screens.admin.AdminViewModel(get(), get(), get()) }
}