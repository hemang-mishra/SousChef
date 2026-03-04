package com.souschef.di

import org.koin.dsl.module

/**
 * Repository module — bind interfaces to Firebase implementations.
 *
 * Rule: Always bind the interface to the implementation:
 *   single<AuthRepository> { FirebaseAuthRepository(get()) }
 *
 * Example (Phase 1+):
 *   single<AuthRepository> { FirebaseAuthRepository(get()) }
 *   single<RecipeRepository> { FirestoreRecipeRepository(get()) }
 */
val repositoryModule = module {
    // Repository bindings added in Phase 1+
}

