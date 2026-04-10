package com.souschef.di

import com.souschef.api.GeminiProvider
import com.souschef.service.ai.GeminiRecipeService
import com.souschef.service.auth.FirebaseAuthService
import com.souschef.service.ingredient.FirebaseIngredientService
import com.souschef.service.recipe.FirebaseRecipeService
import org.koin.dsl.module

/**
 * Service module — register Firebase service classes here.
 * Services wrap raw Firestore / Firebase Auth calls.
 */
val serviceModule = module {
    single { FirebaseAuthService(get(), get()) }
    single { FirebaseRecipeService(get()) }
    single { FirebaseIngredientService(get()) }
    single { GeminiRecipeService(GeminiProvider.getModel()) }
}
