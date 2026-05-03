@file:Suppress("unused")

package com.souschef.model.recipe

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Firestore document model for a recipe.
 * Document path: `recipes/{recipeId}`
 *
 * Ingredients are stored as embedded [RecipeIngredient] sub-documents that
 * reference the global ingredient library via [RecipeIngredient.globalIngredientId].
 *
 * All fields have defaults for Firestore `toObject<Recipe>()`.
 */
data class Recipe(
    @DocumentId
    val recipeId: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    @get:PropertyName("verifiedChefRecipe")
    @set:PropertyName("verifiedChefRecipe")
    var isVerifiedChef: Boolean = false,
    val baseServingSize: Int = 4,
    val minServingSize: Int? = null,
    val maxServingSize: Int? = null,
    val coverImageUrl: String? = null,
    val isPublished: Boolean = false,
    val originalRecipeId: String? = null,
    val originalRecipeTitle: String? = null,
    /** Number of times this recipe has been forked. Maintained via FieldValue.increment. */
    val forkCount: Int = 0,
    /** Number of users that have saved/bookmarked this recipe. */
    val savedByCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val stepCount: Int = 0,
    val hasSteps: Boolean = false,
    val tags: List<String> = emptyList(),
    val ingredients: List<RecipeIngredient> = emptyList(),

    /**
     * Map of languageCode → translated [RecipeLocalization].
     * The canonical [title] / [description] fields above always hold the
     * English copy. Use [titleIn] / [descriptionIn] to read in any language
     * with a safe English fallback.
     */
    val localizations: Map<String, RecipeLocalization> = emptyMap(),

    /**
     * Set of language codes for which this recipe (and all its steps +
     * referenced ingredients) has been fully translated. Used by the lazy
     * translation flow to avoid re-translating on every view.
     */
    val translatedLanguages: List<String> = emptyList()
) {
    /** Returns the title in [language], falling back to the English [title]. */
    fun titleIn(language: String): String =
        localizations[language]?.title?.takeIf { it.isNotBlank() } ?: title

    /** Returns the description in [language], falling back to English. */
    fun descriptionIn(language: String): String =
        localizations[language]?.description?.takeIf { it.isNotBlank() } ?: description
}

