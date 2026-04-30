package com.souschef.service.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.souschef.api.GeminiTranslationPrompt
import com.souschef.model.recipe.SupportedLanguages
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "TranslationDebug"

/**
 * Service that asks Gemini to translate the English copy of a recipe (recipe
 * metadata + steps + referenced global ingredients) into a target language.
 *
 * Pure I/O: builds the prompt, calls the model, parses the JSON. The
 * orchestration / Firestore writes live in [com.souschef.usecases.translation.TranslateRecipeUseCase].
 */
class GeminiTranslationService(
    private val model: GenerativeModel
) {

    // ── Source / Target DTOs ────────────────────────────────────────────────

    @Serializable
    data class SourceRecipe(
        val title: String = "",
        val description: String = ""
    )

    @Serializable
    data class SourceStep(
        val stepId: String = "",
        val instructionText: String = "",
        val flameLevel: String? = null,
        val expectedVisualCue: String? = null
    )

    @Serializable
    data class SourceIngredient(
        val ingredientId: String = "",
        val name: String = "",
        val defaultUnit: String = ""
    )

    @Serializable
    data class SourcePayload(
        val recipe: SourceRecipe,
        val steps: List<SourceStep>,
        val ingredients: List<SourceIngredient>
    )

    @Serializable
    data class TranslatedRecipe(
        val title: String = "",
        val description: String = ""
    )

    @Serializable
    data class TranslatedStep(
        val stepId: String = "",
        val instructionText: String = "",
        val flameLevel: String? = null,
        val expectedVisualCue: String? = null
    )

    @Serializable
    data class TranslatedIngredient(
        val ingredientId: String = "",
        val name: String = "",
        val defaultUnit: String = ""
    )

    @Serializable
    data class TranslatedPayload(
        val languageCode: String = "",
        val recipe: TranslatedRecipe = TranslatedRecipe(),
        val steps: List<TranslatedStep> = emptyList(),
        val ingredients: List<TranslatedIngredient> = emptyList()
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Translates the [source] payload into [targetLanguageCode] (e.g. "hi").
     *
     * @throws GeminiTranslationException on parse / network errors.
     */
    suspend fun translate(
        source: SourcePayload,
        targetLanguageCode: String
    ): TranslatedPayload {
        if (source.steps.isEmpty() && source.ingredients.isEmpty() &&
            source.recipe.title.isBlank() && source.recipe.description.isBlank()) {
            // Nothing to translate.
            return TranslatedPayload(languageCode = targetLanguageCode)
        }

        val sourceJson = json.encodeToString(source)
        val prompt = GeminiTranslationPrompt.buildPrompt(
            targetLanguage = SupportedLanguages.displayName(targetLanguageCode),
            targetLanguageCode = targetLanguageCode,
            sourceJson = sourceJson
        )

        Log.d(TAG, "Sending translation prompt to Gemini (${prompt.length} chars, target=$targetLanguageCode)")
        Log.d(TAG, "Source payload: $sourceJson")
        val response = model.generateContent(prompt)
        val responseText = response.text
            ?: throw GeminiTranslationException("Gemini returned an empty translation response.")

        Log.d(TAG, "Received translation (${responseText.length} chars): ${responseText.take(800)}")

        val translatedPayload = parse(responseText, targetLanguageCode)
        Log.d(TAG, "Parsed translated payload: $translatedPayload")
        return translatedPayload
    }

    private fun parse(rawText: String, fallbackLang: String): TranslatedPayload {
        val cleaned = stripFences(rawText)
        val candidate = extractFirstJsonObject(cleaned) ?: cleaned

        return try {
            val parsed: TranslatedPayload = json.decodeFromString(candidate)
            if (parsed.languageCode.isBlank()) parsed.copy(languageCode = fallbackLang) else parsed
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse translation response: ${e.message}", e)
            Log.e(TAG, "Cleaned candidate was: ${candidate.take(2000)}")
            throw GeminiTranslationException(
                "Failed to parse AI translation. Please try again.",
                e
            )
        }
    }

    /** Strips ``` / ```json fences and stray whitespace. */
    private fun stripFences(rawText: String): String {
        var s = rawText.trim()
        if (s.startsWith("```")) {
            // Remove opening fence (with optional language tag).
            val firstNewline = s.indexOf('\n')
            s = if (firstNewline >= 0) s.substring(firstNewline + 1) else s.removePrefix("```")
        }
        if (s.endsWith("```")) s = s.removeSuffix("```")
        return s.trim()
    }

    /**
     * Returns the first balanced JSON object substring of [s], or null if
     * none is found. Handles common Gemini quirks like leading prose
     * ("Sure! Here's the translation: { … }").
     */
    private fun extractFirstJsonObject(s: String): String? {
        val start = s.indexOf('{')
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escape = false
        for (i in start until s.length) {
            val c = s[i]
            if (escape) { escape = false; continue }
            if (c == '\\') { escape = true; continue }
            if (c == '"') { inString = !inString; continue }
            if (inString) continue
            when (c) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return s.substring(start, i + 1)
                }
            }
        }
        return null
    }
}

/**
 * Exception thrown when Gemini's translation response cannot be parsed as
 * valid JSON.
 */
class GeminiTranslationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
