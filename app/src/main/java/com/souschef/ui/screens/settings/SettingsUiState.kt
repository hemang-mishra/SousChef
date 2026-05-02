package com.souschef.ui.screens.settings

import com.souschef.model.auth.UserProfile
import com.souschef.model.device.Compartment

/**
 * UI state for the unified Settings screen.
 *
 * Combines profile information with dispenser compartment configuration so
 * the user has a single home for all account + device controls.
 */
data class SettingsUiState(
    val userProfile: UserProfile? = null,
    val compartments: List<Compartment> = emptyList(),
    /** Edited capacity value per compartment id (string while typing). */
    val editedCapacities: Map<Int, String> = emptyMap(),
    val preferredLanguageCode: String? = null,
    val isLoading: Boolean = true,
    val isSavingDispenser: Boolean = false,
    val savedMessage: String? = null,
    val error: String? = null
)
