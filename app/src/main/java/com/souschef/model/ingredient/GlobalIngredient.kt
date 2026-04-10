@file:Suppress("unused")

package com.souschef.model.ingredient

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Firestore document model for a global ingredient.
 * Document path: `ingredients/{ingredientId}`
 *
 * Represents a shared ingredient in the global library.
 * Recipes reference these via [RecipeIngredient.globalIngredientId].
 *
 * All fields have defaults for Firestore `toObject<GlobalIngredient>()`.
 */
data class GlobalIngredient(
    @DocumentId
    val ingredientId: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val defaultUnit: String = "grams",
    @get:PropertyName("isDispensable")
    @set:PropertyName("isDispensable")
    var isDispensable: Boolean = false,
    val spiceIntensityValue: Double = 0.0,
    val sweetnessValue: Double = 0.0,
    val saltnessValue: Double = 0.0,
    val createdByUserId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

