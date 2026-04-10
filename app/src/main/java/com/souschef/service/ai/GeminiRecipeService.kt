package com.souschef.service.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.souschef.api.GeminiRecipePrompt
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val TAG = "GeminiRecipeService"

/**
 * Service that communicates with the Gemini API to generate recipe steps.
 * Handles prompt construction, API call, and JSON response parsing.
 *
 * This service performs raw API calls — no business logic.
 * Repository wraps these calls in Resource<T>.
 */
class GeminiRecipeService(
    private val model: GenerativeModel
) {

    /**
     * Internal DTO matching the JSON structure returned by Gemini.
     * Used only for parsing; mapped to [RecipeStep] afterwards.
     */
    @Serializable
    private data class GeminiStep(
        val stepNumber: Int = 0,
        val instructionText: String = "",
        val timerSeconds: Int? = null,
        val flameLevel: String? = null,
        val expectedVisualCue: String? = null,
        val ingredientReferences: List<String> = emptyList()
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Generates atomic cooking steps from a recipe description using Gemini.
     *
     * @param recipeDescription Free-text recipe instructions from the creator.
     * @param ingredients Resolved ingredient list for context.
     * @return Parsed list of [RecipeStep] objects.
     * @throws GeminiParseException if the response cannot be parsed as JSON.
     * @throws Exception for network or API errors.
     */
    suspend fun generateSteps(
        recipeDescription: String,
        ingredients: List<ResolvedIngredient>
    ): List<RecipeStep> {
        val prompt = GeminiRecipePrompt.buildPrompt(recipeDescription, ingredients)

        Log.d(TAG, "Sending prompt to Gemini (${prompt.length} chars)")

        val response = model.generateContent(prompt)
        val responseText = response.text
            ?: throw GeminiParseException("Gemini returned an empty response.")

        Log.d(TAG, "Received response: ${responseText.take(200)}...")

        return parseSteps(responseText)
    }

    /**
     * Parses the raw text response from Gemini into a list of [RecipeStep].
     * Handles common Gemini quirks like markdown code fences wrapping the JSON.
     */
    private fun parseSteps(rawText: String): List<RecipeStep> {
        // Strip markdown code fences if Gemini wraps the response
        val cleaned = rawText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val geminiSteps: List<GeminiStep> = json.decodeFromString(cleaned)

            geminiSteps.map { step ->
                RecipeStep(
                    stepNumber = step.stepNumber,
                    instructionText = step.instructionText,
                    timerSeconds = step.timerSeconds,
                    flameLevel = step.flameLevel?.lowercase()?.takeIf {
                        it in listOf("low", "medium", "high")
                    },
                    expectedVisualCue = step.expectedVisualCue,
                    ingredientReferences = step.ingredientReferences
                )
            }.sortedBy { it.stepNumber }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response: ${e.message}", e)
            Log.e(TAG, "Raw response was: $cleaned")
            throw GeminiParseException(
                "Failed to parse AI-generated steps. Please try again.",
                e
            )
        }
    }
}

/**
 * Exception thrown when Gemini's response cannot be parsed as valid step JSON.
 */
class GeminiParseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
