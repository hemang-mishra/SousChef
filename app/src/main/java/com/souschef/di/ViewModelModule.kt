package com.souschef.di

import com.souschef.ui.screens.auth.login.LoginViewModel
import com.souschef.ui.screens.auth.signup.SignUpViewModel
import com.souschef.ui.screens.ingredient.addedit.AddEditIngredientViewModel
import com.souschef.ui.screens.ingredient.library.IngredientLibraryViewModel
import com.souschef.ui.screens.recipe.create.CreateRecipeViewModel
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

    // Recipe creation — factory, needs currentUser from AppViewModel
    factory { (currentUser: com.souschef.model.auth.UserProfile) ->
        CreateRecipeViewModel(get(), get(), get(), currentUser)
    }

    // Ingredient Library — factory (fresh per navigation)
    factory { IngredientLibraryViewModel(get()) }

    // Add/Edit Ingredient — factory, needs currentUser + optional ingredientId
    factory { (currentUser: com.souschef.model.auth.UserProfile, ingredientId: String?) ->
        AddEditIngredientViewModel(get(), get(), get(), currentUser, ingredientId)
    }
}
