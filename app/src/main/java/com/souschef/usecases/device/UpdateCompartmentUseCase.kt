package com.souschef.usecases.device

import com.souschef.model.device.Compartment
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.service.device.DevicePreferenceService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Assigns or updates the ingredient and capacity settings for a single compartment.
 *
 * @param compartmentId       Compartment to update (1–5).
 * @param globalIngredient    Global ingredient to assign, or `null` to clear.
 * @param totalCapacityTsp    New total capacity in tsp (pass current value to leave unchanged).
 */
class UpdateCompartmentUseCase(
    private val deviceService: DevicePreferenceService
) {
    fun execute(
        compartmentId: Int,
        globalIngredient: GlobalIngredient?,
        totalCapacityTsp: Double
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())
        try {
            val updated = Compartment(
                compartmentId = compartmentId,
                globalIngredientId = globalIngredient?.ingredientId,
                ingredientName = globalIngredient?.name,
                ingredientImageUrl = globalIngredient?.imageUrl,
                totalCapacityTsp = totalCapacityTsp,
                dispensedCounts = 0 // reset counts when reassigning
            )
            deviceService.updateCompartment(updated)
            emit(Resource.success(Unit))
        } catch (e: Exception) {
            emit(Resource.failure(message = "Failed to update compartment: ${e.message}"))
        }
    }
}
