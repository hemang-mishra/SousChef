package com.souschef.model.device

/**
 * Result of a dispense operation triggered from the cooking mode.
 */
sealed class DispenseResult {

    /** BLE command sent and hardware acknowledged dispensing. */
    data class Success(
        val compartmentId: Int,
        val countsSent: Int,
        val ingredientName: String
    ) : DispenseResult()

    /** The compartment doesn't have enough remaining capacity. */
    data class InsufficientCapacity(
        val compartmentId: Int,
        val availableTsp: Double,
        val requiredTsp: Double
    ) : DispenseResult()

    /**
     * The required ingredient isn't loaded in any compartment.
     * User must add it manually.
     */
    object NotInDevice : DispenseResult()

    /** BLE is not connected. Prompt user to connect. */
    object BleNotConnected : DispenseResult()

    /** BLE write failed at the GATT layer. */
    data class BleError(val message: String) : DispenseResult()

    /**
     * The recipe ingredient unit can't be converted to tsp automatically.
     * Show count-based dispense UI instead.
     */
    data class UnsupportedUnit(val unit: String) : DispenseResult()
}
