package com.souschef.di

import org.koin.dsl.module

/**
 * Database module - register Room DAOs here as phases add entities.
 * Example:
 *   single { get<PrimaryDatabase>().recipeDao() }
 */
val databaseModule = module {
    // Room database and DAOs will be added in Phase 2+
}

