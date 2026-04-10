package com.souschef.ui.screens.device.settings

import com.souschef.model.device.Compartment

data class DispenserSettingsUiState(
    val compartments: List<Compartment> = List(5) { Compartment(compartmentId = it + 1) },
    /** Editable capacity values (indexed by compartmentId - 1). */
    val editedCapacities: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
