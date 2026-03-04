package com.souschef.di

import org.koin.dsl.module

/**
 * ViewModel module.
 *
 * Rules:
 * - `single` for app-level ViewModels (e.g. AppViewModel) that persist across the activity.
 * - `factory` for screen-scoped ViewModels created fresh on each navigation entry.
 * - `factory` with parametersOf for ViewModels that need runtime parameters.
 *
 * Example (Phase 1+):
 *   single { AppViewModel(get()) }
 *   factory { LoginViewModel(get()) }
 */
val viewModelModule = module {
    // ViewModels added in Phase 1+
}

