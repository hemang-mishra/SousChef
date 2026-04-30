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
    /**
     * Latest globalIngredientId → imageUrl lookup for ALL ingredients in the
     * library (not just dispensable ones). The compartment record stores a
     * denormalised copy of the image URL at assignment time, so without this
     * lookup the dispenser wouldn't pick up images added to ingredients
     * later. The screen prefers this map over [Compartment.ingredientImageUrl].
     */
    val ingredientImageById: Map<String, String?> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
