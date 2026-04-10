package com.souschef.service.device

import com.souschef.model.device.Compartment
import com.souschef.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Service layer for reading and persisting dispenser compartment configuration.
 * Backed by [AppPreferences] (DataStore) — no Firestore needed for device state.
 */
class DevicePreferenceService(private val prefs: AppPreferences) {

    /**
     * Reactive flow of the current compartment list.
     * Emits immediately with the persisted value (or 5 empty defaults).
     */
    fun getCompartmentsFlow(): Flow<List<Compartment>> =
        prefs.compartments.getFlow()

    /**
     * Persists the full compartment list.
     * Always pass all 5 compartments — this replaces the entire stored list.
     */
    suspend fun saveCompartments(compartments: List<Compartment>) {
        prefs.compartments.set(compartments)
    }

    /**
     * Updates a single compartment by [Compartment.compartmentId].
     * No-op if the compartment ID is not found.
     */
    suspend fun updateCompartment(updated: Compartment) {
        val current = prefs.compartments.get().toMutableList()
        val index = current.indexOfFirst { it.compartmentId == updated.compartmentId }
        if (index != -1) {
            current[index] = updated
            prefs.compartments.set(current)
        }
    }
}
