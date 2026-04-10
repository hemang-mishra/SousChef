package com.souschef.usecases.device

import com.souschef.service.device.DevicePreferenceService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Resets a compartment's [dispensedCounts] to 0 and records the current time as
 * [lastRefillAt]. Call this when the user physically refills the compartment.
 */
class RefillCompartmentUseCase(
    private val deviceService: DevicePreferenceService
) {
    fun execute(compartmentId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        try {
            val current = deviceService.getCompartmentsFlow()
                .let { flow ->
                    var result: com.souschef.model.device.Compartment? = null
                    flow.collect { list ->
                        result = list.firstOrNull { it.compartmentId == compartmentId }
                        return@collect
                    }
                    result
                } ?: run {
                emit(Resource.failure(message = "Compartment $compartmentId not found"))
                return@flow
            }

            val refilled = current.copy(
                dispensedCounts = 0,
                lastRefillAt = System.currentTimeMillis()
            )
            deviceService.updateCompartment(refilled)
            emit(Resource.success(Unit))
        } catch (e: Exception) {
            emit(Resource.failure(message = "Failed to refill compartment: ${e.message}"))
        }
    }
}
