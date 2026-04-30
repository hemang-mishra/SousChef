package com.souschef.service.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Firebase Storage service for uploading recipe media (images & videos).
 *
 * Storage paths:
 * - Recipe cover: `recipes/{recipeId}/cover_{uuid}.jpg`
 * - Step media:   `recipes/{recipeId}/steps/step_{index}_{uuid}.{ext}`
 */
class FirebaseStorageService(
    private val context: Context
) {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    /**
     * Uploads a recipe cover image. Compresses to max 1024px and JPEG 85%.
     * Returns the download URL as a String.
     */
    suspend fun uploadRecipeCoverImage(recipeId: String, imageUri: Uri): String {
        val compressed = compressImage(imageUri, maxDimension = 1024, quality = 85)
        val fileName = "cover_${UUID.randomUUID()}.jpg"
        val ref = storageRef.child("recipes/$recipeId/$fileName")
        ref.putBytes(compressed).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Uploads a global-ingredient image. Compresses to max 512px and JPEG 85%
     * (ingredients render small so a smaller file is plenty).
     *
     * Storage path: `ingredients/{ingredientId}/image_{uuid}.jpg`.
     *
     * If [ingredientId] is blank (e.g. the user is uploading before the
     * ingredient document has been created) the image is parked under
     * `ingredients/_drafts/{uuid}.jpg` so the URL is still resolvable.
     */
    suspend fun uploadIngredientImage(ingredientId: String, imageUri: Uri): String {
        val compressed = compressImage(imageUri, maxDimension = 512, quality = 85)
        val fileName = "image_${UUID.randomUUID()}.jpg"
        val folder = if (ingredientId.isBlank()) "ingredients/_drafts" else "ingredients/$ingredientId"
        val ref = storageRef.child("$folder/$fileName")
        ref.putBytes(compressed).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Uploads step media (image or video).
     * Images are compressed; videos are uploaded as-is.
     *
     * @param mediaUri  URI from the device gallery
     * @param mediaType "image" or "video"
     * @return download URL
     */
    suspend fun uploadStepMedia(
        recipeId: String,
        stepIndex: Int,
        mediaUri: Uri,
        mediaType: String
    ): String {
        val uuid = UUID.randomUUID()

        return if (mediaType == "image") {
            val compressed = compressImage(mediaUri, maxDimension = 1024, quality = 85)
            val ref = storageRef.child("recipes/$recipeId/steps/step_${stepIndex}_${uuid}.jpg")
            ref.putBytes(compressed).await()
            ref.downloadUrl.await().toString()
        } else {
            // Video — upload raw bytes from URI
            val ref = storageRef.child("recipes/$recipeId/steps/step_${stepIndex}_${uuid}.mp4")
            ref.putFile(mediaUri).await()
            ref.downloadUrl.await().toString()
        }
    }

    /**
     * Compresses an image URI to a JPEG byte array, scaling down to [maxDimension].
     */
    private fun compressImage(uri: Uri, maxDimension: Int, quality: Int): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")

        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val scaled = scaleDown(original, maxDimension)

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        if (scaled != original) scaled.recycle()
        original.recycle()

        return outputStream.toByteArray()
    }

    /**
     * Scales a bitmap so that neither dimension exceeds [maxDimension],
     * preserving the aspect ratio.
     */
    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Recursively deletes all media associated with a recipe.
     * Uses listAll to clear the "steps/" directory and then the main cover image.
     */
    suspend fun deleteAllRecipeMedia(recipeId: String) {
        try {
            // Delete steps directory items
            val stepsDirRef = storageRef.child("recipes/$recipeId/steps")
            val stepsList = stepsDirRef.listAll().await()
            stepsList.items.forEach { fileRef ->
                fileRef.delete().await()
            }

            // Delete root directory items (like cover image)
            val rootDirRef = storageRef.child("recipes/$recipeId")
            val rootList = rootDirRef.listAll().await()
            rootList.items.forEach { fileRef ->
                fileRef.delete().await()
            }
        } catch (e: Exception) {
            // In case directories don't exist, we don't want to crash.
        }
    }
}
