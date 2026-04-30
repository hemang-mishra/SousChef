package com.souschef.di

import com.souschef.preferences.AppPreferences
import com.souschef.util.LanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Preferences module — provides DataStore-based preferences and the
 * [LanguageManager] that wraps the persisted preferred-language code.
 */
val preferencesModule = module {
    factory { AppPreferences(androidContext()) }

    /** Long-lived coroutine scope for app-level singletons (e.g. LanguageManager). */
    single<CoroutineScope>(named("appScope")) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /** Active UI / narration language — single so all screens observe the same flow. */
    single { LanguageManager(get(), get(named("appScope"))) }
}

