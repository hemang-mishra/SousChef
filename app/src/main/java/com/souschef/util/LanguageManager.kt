package com.souschef.util

import com.souschef.model.recipe.SupportedLanguages
import com.souschef.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single source of truth for the user's currently active UI / narration
 * language. Backed by [AppPreferences.preferredLanguageCode] (persisted in
 * DataStore) but also provides an in-memory override for the current
 * cooking / overview session so the user can flip languages without
 * persisting that preference globally.
 *
 * The merged effective language is exposed via [currentLanguage].
 */
class LanguageManager(
    private val preferences: AppPreferences,
    private val appScope: CoroutineScope
) {
    /**
     * Per-screen override. `null` means "use the persisted preference".
     */
    private val _sessionOverride = MutableStateFlow<String?>(null)

    val currentLanguage: StateFlow<String> = combine(
        preferences.preferredLanguageCode.getFlow(),
        _sessionOverride
    ) { stored, override ->
        when {
            override != null && override in SupportedLanguages.all -> override
            stored != null && stored in SupportedLanguages.all -> stored
            else -> SupportedLanguages.ENGLISH
        }
    }.stateIn(
        scope = appScope,
        started = SharingStarted.Eagerly,
        initialValue = SupportedLanguages.ENGLISH
    )

    /**
     * Switches the active language for the current session AND persists it
     * as the user's preferred language so it sticks across launches.
     */
    fun setLanguage(code: String) {
        if (code !in SupportedLanguages.all) return
        _sessionOverride.value = code
        appScope.launch { preferences.preferredLanguageCode.set(code) }
    }

    /**
     * Toggles between English and Hindi. Convenience for the one-tap
     * switcher on recipe screens.
     */
    fun toggle() {
        val next = if (currentLanguage.value == SupportedLanguages.HINDI) {
            SupportedLanguages.ENGLISH
        } else {
            SupportedLanguages.HINDI
        }
        setLanguage(next)
    }
}
