package com.souschef.ui.screens.ingredient.addedit

import android.net.Uri

/**
 * UI state for the Add/Edit Ingredient screen.
 *
 * Image fields:
 * - [imageUrl]        Remotely-hosted Firebase Storage URL. Set when an
 *                     existing ingredient with an image is loaded, or after
 *                     a freshly-picked photo finishes uploading.
 * - [pendingImageUri] Local `content://` URI just picked from the gallery,
 *                     used to show an instant preview while the upload is
 *                     still in flight.
 * - [isUploadingImage] True while the picked photo is being uploaded.
 */
data class AddEditIngredientUiState(
    val isEditMode: Boolean = false,
    val ingredientId: String? = null,
    val name: String = "",
    val defaultUnit: String = "grams",
    val isDispensable: Boolean = false,
    val spiceIntensityValue: Double = 0.0,
    val sweetnessValue: Double = 0.0,
    val saltnessValue: Double = 0.0,
    val imageUrl: String? = null,
    val pendingImageUri: Uri? = null,
    val isUploadingImage: Boolean = false,
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val savedIngredientId: String? = null,
    val generalError: String? = null
)

