package com.souschef.model.recipe

/**
 * Supported language codes used as keys in `localizations` maps stored on
 * Recipe / RecipeStep / GlobalIngredient documents.
 *
 * The canonical fields (title, description, instructionText, name, …) on each
 * model continue to hold the **English** copy for backward compatibility.
 * Hindi (and any future languages) are stored under the corresponding map entry.
 */
object SupportedLanguages {
    const val ENGLISH = "en"
    const val HINDI = "hi"

    val all: List<String> = listOf(ENGLISH, HINDI)

    fun displayName(code: String): String = when (code) {
        ENGLISH -> "English"
        HINDI -> "हिन्दी"
        else -> code.uppercase()
    }

    /** BCP-47 locale tag for TTS / Locale APIs. */
    fun bcp47(code: String): String = when (code) {
        HINDI -> "hi-IN"
        else -> "en-US"
    }
}

/**
 * Translated copy of recipe-level fields.
 * Stored as a map entry on [Recipe.localizations] (key = language code).
 *
 * Default-value constructor required for Firestore deserialization.
 */
data class RecipeLocalization(
    val title: String = "",
    val description: String = ""
)

/**
 * Translated copy of step-level fields.
 * Stored as a map entry on [RecipeStep.localizations].
 *
 * Numeric / structural fields (timerSeconds, quantityMultiplier, stepType) are
 * **not** translated — they are language-agnostic.
 */
data class RecipeStepLocalization(
    val instructionText: String = "",
    val flameLevel: String? = null,
    val expectedVisualCue: String? = null
)

/**
 * Translated copy of a global ingredient's user-facing fields.
 * Stored as a map entry on [com.souschef.model.ingredient.GlobalIngredient.localizations].
 *
 * `defaultUnit` translation lets us narrate "ग्राम" vs "grams" naturally.
 */
data class GlobalIngredientLocalization(
    val name: String = "",
    val defaultUnit: String = ""
)
