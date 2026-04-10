package com.souschef.usecases.device

import com.souschef.api.ble.BleConstants
import com.souschef.api.ble.BleDeviceManager
import com.souschef.model.device.BleConnectionState
import com.souschef.model.device.DispenseResult
import com.souschef.service.device.DevicePreferenceService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.math.ceil

/**
 * Orchestrates a full dispense operation for a single ingredient during cooking.
 *
 * ## Flow
 * 1. Check BLE is connected.
 * 2. Find the compartment holding [globalIngredientId].
 * 3. Convert [quantity] + [unit] → counts (1 count = 0.25 tsp).
 * 4. Verify sufficient remaining capacity.
 * 5. Send BLE dispense command.
 * 6. Decrement [dispensedCounts] in DataStore.
 * 7. Return [DispenseResult].
 *
 * ## Unit conversion
 * - tsp     → count = ceil(qty / 0.25)
 * - tbsp    → convert to tsp (* 3) then calculate
 * - ml      → convert to tsp (/ 4.92892) then calculate
 * - grams/g → convert to tsp (/ 4.2) then calculate (powder density assumption)
 * - others  → [DispenseResult.UnsupportedUnit]
 */
class DispenseSpiceUseCase(
    private val deviceService: DevicePreferenceService,
    private val bleDeviceManager: BleDeviceManager
) {

    fun execute(
        globalIngredientId: String,
        ingredientName: String,
        quantity: Double,
        unit: String
    ): Flow<Resource<DispenseResult>> = flow {
        emit(Resource.loading())

        // 1. BLE connected?
        val connectionState = bleDeviceManager.connectionState.first()
        if (connectionState !is BleConnectionState.Connected) {
            emit(Resource.success(DispenseResult.BleNotConnected))
            return@flow
        }

        // 2. Find matching compartment
        val compartments = deviceService.getCompartmentsFlow().first()
        val compartment = compartments.firstOrNull { it.globalIngredientId == globalIngredientId }
        if (compartment == null) {
            emit(Resource.success(DispenseResult.NotInDevice))
            return@flow
        }

        // 3. Convert quantity to tsp
        val qtyTsp = toTsp(quantity, unit)
        if (qtyTsp == null) {
            emit(Resource.success(DispenseResult.UnsupportedUnit(unit)))
            return@flow
        }

        val count = ceil(qtyTsp / BleConstants.TSP_PER_COUNT).toInt().coerceAtLeast(1)

        // 4. Capacity check
        if (compartment.remainingTsp < qtyTsp) {
            emit(Resource.success(
                DispenseResult.InsufficientCapacity(
                    compartmentId = compartment.compartmentId,
                    availableTsp = compartment.remainingTsp,
                    requiredTsp = qtyTsp
                )
            ))
            return@flow
        }

        // 5. Send BLE command
        val sent = bleDeviceManager.sendDispenseCommand(
            compartmentId = compartment.compartmentId,
            count = count
        )

        if (!sent) {
            emit(Resource.success(DispenseResult.BleError("BLE write failed")))
            return@flow
        }

        // 6. Persist updated counts
        val updated = compartment.copy(dispensedCounts = compartment.dispensedCounts + count)
        deviceService.updateCompartment(updated)

        // 7. Return success
        emit(Resource.success(
            DispenseResult.Success(
                compartmentId = compartment.compartmentId,
                countsSent = count,
                ingredientName = ingredientName
            )
        ))
    }

    // ── Unit conversion ───────────────────────────────────────────────────────

    /**
     * Converts [quantity] in [unit] to teaspoons.
     * Returns `null` if the unit is not supported for auto-conversion.
     */
    private fun toTsp(quantity: Double, unit: String): Double? {
        return when (unit.trim().lowercase()) {
            "tsp"                   -> quantity
            "tbsp"                  -> quantity * 3.0
            "ml"                    -> quantity / BleConstants.ML_PER_TSP
            "grams", "g", "gram"    -> quantity / BleConstants.GRAMS_PER_TSP
            else                    -> null
        }
    }
}
