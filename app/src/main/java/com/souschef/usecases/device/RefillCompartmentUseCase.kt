package com.souschef.usecases.device

import com.souschef.service.device.DevicePreferenceService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Records that the user has physically refilled a compartment.
 *
 * - Sets [dispensedCounts] back to 0 so the remaining-capacity gauge starts
 *   from "full".
 * - Optionally overrides [totalCapacityTsp] to whatever amount the user just
 *   poured in. When [newCapacityTsp] is null, the existing capacity is left
 *   untouched (useful for the legacy "I just topped it back up to full"
 *   shortcut).
 * - Stamps [lastRefillAt] with the current time.
 */
class RefillCompartmentUseCase(
    private val deviceService: DevicePreferenceService
) {
    fun execute(
        compartmentId: Int,
        newCapacityTsp: Double? = null
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        try {
            val current = deviceService.getCompartmentsFlow()
                .first()
                .firstOrNull { it.compartmentId == compartmentId }
                ?: run {
                    emit(Resource.failure(message = "Compartment $compartmentId not found"))
                    return@flow
                }

            val refilled = current.copy(
                dispensedCounts = 0,
                totalCapacityTsp = newCapacityTsp?.coerceAtLeast(0.0)
                    ?: current.totalCapacityTsp,
                lastRefillAt = System.currentTimeMillis()
            )
            deviceService.updateCompartment(refilled)
            emit(Resource.success(Unit))
        } catch (e: Exception) {
            emit(Resource.failure(message = "Failed to refill compartment: ${e.message}"))
        }
    }
}
