package com.souschef.di

import com.souschef.repository.auth.AuthRepository
import com.souschef.repository.auth.FirebaseAuthRepository
import com.souschef.repository.ingredient.FirestoreIngredientRepository
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.repository.recipe.FirestoreRecipeRepository
import com.souschef.repository.recipe.RecipeRepository
import org.koin.dsl.module

/**
 * Repository module — bind interfaces to Firebase implementations.
 */
val repositoryModule = module {
    single<AuthRepository> { FirebaseAuthRepository(get()) }
    single<RecipeRepository> { FirestoreRecipeRepository(get()) }
    single<IngredientRepository> { FirestoreIngredientRepository(get()) }
}
