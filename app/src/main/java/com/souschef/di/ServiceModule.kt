package com.souschef.di

import org.koin.dsl.module

/**
 * Service module — register Firebase service classes here.
 * Services wrap raw Firestore / Firebase Auth calls.
 *
 * Example (Phase 1+):
 *   single { FirebaseAuthService(get(), get()) }
 *   single { FirebaseRecipeService(get()) }
 */
val serviceModule = module {
    // Firebase service implementations added in Phase 1+
}

