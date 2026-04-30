@file:Suppress("unused")

package com.souschef.model.ingredient

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.souschef.model.recipe.GlobalIngredientLocalization

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
    val updatedAt: Timestamp = Timestamp.now(),

    /**
     * Map of languageCode → translated [GlobalIngredientLocalization].
     * Canonical [name] / [defaultUnit] always hold the English copy.
     * Use [nameIn] / [defaultUnitIn] for safe localized reads.
     */
    val localizations: Map<String, GlobalIngredientLocalization> = emptyMap()
) {
    /** Localized ingredient name, falling back to English. */
    fun nameIn(language: String): String =
        localizations[language]?.name?.takeIf { it.isNotBlank() } ?: name

    /** Localized default unit, falling back to English. */
    fun defaultUnitIn(language: String): String =
        localizations[language]?.defaultUnit?.takeIf { it.isNotBlank() } ?: defaultUnit
}

