package com.souschef.model.device

import kotlinx.serialization.Serializable

/**
 * Represents a single spice compartment in the physical SousChef dispenser.
 *
 * - The dispenser has 5 compartments total (numbered 1–5).
 * - Each count ejected = ¼ teaspoon (0.25 tsp ≈ 1.23 ml ≈ ~1.2 g for typical powdered spices).
 * - Remaining capacity = totalCapacityTsp - (dispensedCounts * 0.25)
 *
 * Persisted locally in DataStore (JSON) — no Firestore needed for compartment state.
 */
@Serializable
data class Compartment(
    /** Compartment number 1–5 (matches the physical slot on the hardware). */
    val compartmentId: Int = 0,

    /** References [GlobalIngredient.ingredientId] from the global library. Null if slot is empty. */
    val globalIngredientId: String? = null,

    /** Denormalised display name (e.g. "Red Chili Powder"). Null if empty. */
    val ingredientName: String? = null,

    /** Optional thumbnail URL from the global ingredient record. */
    val ingredientImageUrl: String? = null,

    /**
     * User-configured total capacity of this compartment in teaspoons (tsp).
     * 0.0 = not configured yet.
     */
    val totalCapacityTsp: Double = 0.0,

    /**
     * Cumulative number of counts already dispensed from this compartment since last refill.
     * Each count = 0.25 tsp.
     */
    val dispensedCounts: Int = 0,

    /** Epoch-millis timestamp of the last time this compartment was refilled. Null if never. */
    val lastRefillAt: Long? = null
) {
    // ── Computed helpers ──────────────────────────────────────────────────────

    /** Total volume already dispensed, in tsp. */
    val dispensedTsp: Double get() = dispensedCounts * 0.25

    /** Remaining usable volume, in tsp. Returns 0.0 if capacity not configured. */
    val remainingTsp: Double
        get() = if (totalCapacityTsp <= 0.0) 0.0
                else maxOf(0.0, totalCapacityTsp - dispensedTsp)

    /** Fill percentage 0–100. 0 if capacity not configured. */
    val fillPercent: Float
        get() = if (totalCapacityTsp <= 0.0) 0f
                else (remainingTsp / totalCapacityTsp).toFloat().coerceIn(0f, 1f)

    /** True when remaining capacity is below 20% of total. */
    val isLowStock: Boolean
        get() = totalCapacityTsp > 0.0 && fillPercent <= 0.20f

    /** True when no ingredient is assigned to this compartment. */
    val isEmpty: Boolean get() = globalIngredientId == null
}
