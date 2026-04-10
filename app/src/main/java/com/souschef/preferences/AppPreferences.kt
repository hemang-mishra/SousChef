package com.souschef.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.souschef.model.device.Compartment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("souschef_prefs")

/**
 * Typed DataStore preference wrapper interface.
 */
interface DataStorePreference<T> {
    fun getFlow(): Flow<T>
    suspend fun set(value: T)
    suspend fun get(): T
}

/**
 * App-wide DataStore preferences backed by Jetpack DataStore.
 * Never use SharedPreferences — always use this class.
 *
 * Access via Koin: `val prefs: AppPreferences by inject()`
 */
class AppPreferences(private val context: Context) {

    // (Gson has been replaced with Kotlinx Serialization for Compartments)

    companion object {
        private val LAST_SYNC_TIME    = stringPreferencesKey("last_sync_time")
        private val CACHED_USER_UID   = stringPreferencesKey("cached_user_uid")
        private val COMPARTMENTS_JSON = stringPreferencesKey("dispenser_compartments")
    }

    // ── Auth / sync ───────────────────────────────────────────────────────────

    /** Timestamp (ISO-8601 string) of the last successful Firestore sync. */
    val lastSyncTime: DataStorePreference<String?> = object : DataStorePreference<String?> {
        override fun getFlow(): Flow<String?> =
            context.dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[LAST_SYNC_TIME] }
                .distinctUntilChanged()

        override suspend fun set(value: String?) {
            context.dataStore.edit { prefs ->
                if (value == null) prefs.remove(LAST_SYNC_TIME)
                else prefs[LAST_SYNC_TIME] = value
            }
        }

        override suspend fun get(): String? = getFlow().first()
    }

    /** UID of the currently cached signed-in user. */
    val cachedUserUid: DataStorePreference<String?> = object : DataStorePreference<String?> {
        override fun getFlow(): Flow<String?> =
            context.dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[CACHED_USER_UID] }
                .distinctUntilChanged()

        override suspend fun set(value: String?) {
            context.dataStore.edit { prefs ->
                if (value == null) prefs.remove(CACHED_USER_UID)
                else prefs[CACHED_USER_UID] = value
            }
        }

        override suspend fun get(): String? = getFlow().first()
    }

    // ── Dispenser compartments ────────────────────────────────────────────────

    /**
     * The 5 dispenser compartment configurations, persisted as JSON.
     * Returns a default list of 5 empty compartments if not yet configured.
     *
     */
    val compartments: DataStorePreference<List<Compartment>> =
        object : DataStorePreference<List<Compartment>> {

            override fun getFlow(): Flow<List<Compartment>> =
                context.dataStore.data
                    .catch { emit(emptyPreferences()) }
                    .map { prefs ->
                        val jsonStr = prefs[COMPARTMENTS_JSON]
                        if (jsonStr.isNullOrBlank()) defaultCompartments()
                        else runCatching {
                            Json.decodeFromString<List<Compartment>>(jsonStr)
                        }.getOrElse { defaultCompartments() }
                    }
                    .distinctUntilChanged()

            override suspend fun set(value: List<Compartment>) {
                context.dataStore.edit { prefs ->
                    prefs[COMPARTMENTS_JSON] = Json.encodeToString(value)
                }
            }

            override suspend fun get(): List<Compartment> = getFlow().first()
        }

    // ── Global helpers ────────────────────────────────────────────────────────

    /** Clears all stored preferences (e.g. on sign-out). */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    private fun defaultCompartments(): List<Compartment> =
        (1..5).map { id -> Compartment(compartmentId = id) }
}
