package com.souschef.di

import com.souschef.ui.screens.auth.login.LoginViewModel
import com.souschef.ui.screens.auth.signup.SignUpViewModel
import com.souschef.ui.screens.home.HomeViewModel
import com.souschef.ui.screens.ingredient.addedit.AddEditIngredientViewModel
import com.souschef.ui.screens.ingredient.library.IngredientLibraryViewModel
import com.souschef.ui.screens.recipe.aigeneration.AiStepGenerationViewModel
import com.souschef.ui.screens.recipe.cooking.CookingModeViewModel
import com.souschef.ui.screens.recipe.create.CreateRecipeViewModel
import com.souschef.ui.screens.recipe.overview.RecipeOverviewViewModel
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
    single { AppViewModel(get()) }

    // Auth screens — factory (fresh per navigation)
    factory { LoginViewModel(get()) }
    factory { SignUpViewModel(get()) }

    // Home — factory, needs userId + userName
    factory { (userId: String, userName: String) ->
        HomeViewModel(get(), userId, userName)
    }

    // Recipe creation — factory, needs currentUser from AppViewModel
    factory { (currentUser: com.souschef.model.auth.UserProfile) ->
        CreateRecipeViewModel(get(), get(), get(), get(), currentUser)
    }

    // Recipe overview — factory, needs recipeId
    factory { (recipeId: String) ->
        RecipeOverviewViewModel(get(), get(), get(), recipeId)
    }

    // Cooking mode — factory, needs recipeId + serving/flavour params
    factory { (recipeId: String, servings: Int, spice: Float, salt: Float, sweetness: Float) ->
        CookingModeViewModel(get(), get(), get(), recipeId, servings, spice, salt, sweetness)
    }

    // AI Step Generation — factory, needs recipeId
    factory { (recipeId: String) ->
        AiStepGenerationViewModel(get(), get(), get(), get(), get(), recipeId)
    }

    // Ingredient Library — factory (fresh per navigation)
    factory { IngredientLibraryViewModel(get()) }

    // Add/Edit Ingredient — factory, needs currentUser + optional ingredientId
    factory { (currentUser: com.souschef.model.auth.UserProfile, ingredientId: String?) ->
        AddEditIngredientViewModel(get(), get(), get(), currentUser, ingredientId)
    }
}

