package com.souschef.api.ble

import java.util.UUID

/**
 * BLE protocol constants for the SousChef-Dispenser hardware.
 *
 * Command byte format (2 bytes):
 *   Byte 0: compartmentId (1–5)
 *   Byte 1: count        (1–255; each count ejects 0.25 tsp)
 *
 * To add real UUIDs: replace the placeholder strings with the actual UUIDs
 * printed on the hardware firmware documentation.
 */
object BleConstants {

    /** Advertised device name the app scans for. */
    const val DEVICE_NAME = "SousChef"

    /** Scan timeout in milliseconds before giving up. */
    const val SCAN_TIMEOUT_MS = 15_000L

    /**
     * Primary GATT service UUID exposed by the dispenser.
     * TODO: Replace with real UUID once hardware firmware is finalised.
     */
    val SERVICE_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")

    /**
     * Writable GATT characteristic that accepts dispense commands.
     * Write type: WRITE_TYPE_DEFAULT (expects acknowledgment).
     * TODO: Replace with real UUID once hardware firmware is finalised.
     */
    val DISPENSE_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("87654321-4321-4321-4321-cba987654321")

    // ── Unit conversion constants ─────────────────────────────────────────────

    /** Volume dispensed per single count, in teaspoons. */
    const val TSP_PER_COUNT = 0.25

    /** Approx. ml per tsp (for unit conversion). */
    const val ML_PER_TSP = 4.92892

    /** Approx. grams per tsp for typical powdered spices (density ~0.9 g/ml). */
    const val GRAMS_PER_TSP = 4.2

    // ── Supported dispense units ──────────────────────────────────────────────

    /** Units that can be automatically converted to tsp for count calculation. */
    val SUPPORTED_UNITS = setOf("tsp", "tbsp", "ml", "grams", "g")

    /**
     * Converts [quantity] in [unit] to teaspoons.
     * Returns `null` if the unit is not supported for auto-conversion.
     */
    fun toTsp(quantity: Double, unit: String): Double? {
        return when (unit.trim().lowercase()) {
            "tsp"                   -> quantity
            "tbsp"                  -> quantity * 3.0
            "ml"                    -> quantity / ML_PER_TSP
            "grams", "g", "gram"    -> quantity / GRAMS_PER_TSP
            else                    -> null
        }
    }

    /**
     * Converts [tsp] teaspoons back to [unit].
     * Returns [tsp] as fallback if unit is not supported.
     */
    fun fromTsp(tsp: Double, unit: String): Double {
        return when (unit.trim().lowercase()) {
            "tsp"                   -> tsp
            "tbsp"                  -> tsp / 3.0
            "ml"                    -> tsp * ML_PER_TSP
            "grams", "g", "gram"    -> tsp * GRAMS_PER_TSP
            else                    -> tsp
        }
    }

    /**
     * Rounds a quantity UP to the nearest multiple of 0.25 teaspoons,
     * maintaining its original unit.
     */
    fun roundUpToNearestDispenseStep(quantity: Double, unit: String): Double {
        val qtyTsp = toTsp(quantity, unit) ?: return quantity
        val count = kotlin.math.ceil(qtyTsp / TSP_PER_COUNT).coerceAtLeast(1.0)
        val roundedTsp = count * TSP_PER_COUNT
        return fromTsp(roundedTsp, unit)
    }
}
