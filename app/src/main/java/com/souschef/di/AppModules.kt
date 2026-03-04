package com.souschef.di

/**
 * Aggregated list of all Koin modules for the app.
 * Pass this list to `modules(appModules)` in [SousChefApplication].
 */
val appModules = listOf(
    networkingModule,
    databaseModule,
    serviceModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
    preferencesModule
)

