package com.souschef.usecases.device

import com.souschef.model.device.Compartment
import com.souschef.service.device.DevicePreferenceService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Returns the current list of 5 dispenser compartments (reactive flow).
 */
class GetCompartmentsUseCase(
    private val deviceService: DevicePreferenceService
) {
    fun execute(): Flow<Resource<List<Compartment>>> =
        deviceService.getCompartmentsFlow()
            .map { Resource.success(it) }
}
