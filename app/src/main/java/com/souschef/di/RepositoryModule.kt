package com.souschef.di

import com.souschef.repository.ai.AiRepository
import com.souschef.repository.ai.GeminiAiRepository
import com.souschef.repository.auth.AuthRepository
import com.souschef.repository.auth.FirebaseAuthRepository
import com.souschef.repository.ingredient.FirestoreIngredientRepository
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.repository.recipe.FirestoreRecipeRepository
import com.souschef.repository.recipe.RecipeListCache
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.repository.translation.GeminiTranslationRepository
import com.souschef.repository.translation.TranslationRepository
import org.koin.dsl.module

/**
 * Repository module — bind interfaces to Firebase implementations.
 */
val repositoryModule = module {
    single<AuthRepository> { FirebaseAuthRepository(get()) }
    single { RecipeListCache() }
    single<RecipeRepository> { FirestoreRecipeRepository(get(), get()) }
    single<IngredientRepository> { FirestoreIngredientRepository(get()) }
    single<AiRepository> { GeminiAiRepository(get()) }
    single<TranslationRepository> { GeminiTranslationRepository(get()) }
}
