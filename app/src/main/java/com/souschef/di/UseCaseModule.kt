package com.souschef.di

import org.koin.dsl.module

/**
 * Use-case module — register use-case classes as `single`.
 *
 * Example (Phase 1+):
 *   single { CreateRecipeUseCase(get(), get()) }
 *   single { GenerateRecipeStepsUseCase(get()) }
 */
val useCaseModule = module {
    // Use cases added in Phase 1+
}

