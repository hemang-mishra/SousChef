package com.souschef.di

import com.souschef.preferences.AppPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Preferences module — provides DataStore-based preferences.
 * Use `factory` since DataStore manages its own singleton internally.
 */
val preferencesModule = module {
    factory { AppPreferences(androidContext()) }
}

