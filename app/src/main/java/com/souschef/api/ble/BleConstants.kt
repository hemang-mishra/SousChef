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
}
