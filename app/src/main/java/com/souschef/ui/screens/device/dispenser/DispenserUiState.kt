package com.souschef.ui.screens.device.dispenser

import com.souschef.model.device.BleConnectionState
import com.souschef.model.device.Compartment
import com.souschef.model.ingredient.GlobalIngredient

data class DispenserUiState(
    val compartments: List<Compartment> = List(5) { Compartment(compartmentId = it + 1) },
    val connectionState: BleConnectionState = BleConnectionState.Disconnected,
    /** Full list of dispensable global ingredients for the assignment picker. */
    val dispensableIngredients: List<GlobalIngredient> = emptyList(),
    /** Filtered view of [dispensableIngredients] based on [searchQuery]. */
    val filteredIngredients: List<GlobalIngredient> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
