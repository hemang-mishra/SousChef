package com.souschef.service.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.souschef.api.GeminiRecipePrompt
import com.souschef.model.recipe.RecipeStep
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val TAG = "GeminiRecipeService"

/**
 * Service that communicates with the Gemini API to generate a complete recipe
 * (steps + ingredients) from a free-text description.
 *
 * Handles prompt construction, API call, and JSON response parsing.
 * This service performs raw API calls — no business logic.
 */
class GeminiRecipeService(
    private val model: GenerativeModel
) {

    // ── DTOs for JSON parsing ────────────────────────────────────────────────

    /**
     * Top-level response wrapper containing both ingredients and steps.
     */
    @Serializable
    private data class GeminiRecipeResponse(
        val ingredients: List<GeminiIngredient> = emptyList(),
        val steps: List<GeminiStep> = emptyList()
    )

    /**
     * Ingredient extracted by Gemini from the recipe description.
     */
    @Serializable
    data class GeminiIngredient(
        val name: String = "",
        val quantity: Double = 0.0,
        val unit: String = "grams"
    )

    /**
     * Internal DTO matching the step JSON structure returned by Gemini.
     */
    @Serializable
    private data class GeminiStep(
        val stepNumber: Int = 0,
        val stepType: String = "ACTION",
        val instructionText: String = "",
        val ingredientName: String? = null,
        val quantityMultiplier: Double = 1.0,
        val timerSeconds: Int? = null,
        val flameLevel: String? = null,
        val expectedVisualCue: String? = null
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Result type combining both extracted ingredients and parsed steps.
     */
    data class GeneratedRecipe(
        val ingredients: List<GeminiIngredient>,
        val steps: List<RecipeStep>
    )

    /**
     * Generates a complete recipe (steps + ingredients) from a description using Gemini.
     *
     * @param recipeDescription Free-text recipe description from the creator.
     * @param baseServingSize   Number of servings for quantity calculation.
     * @return [GeneratedRecipe] containing both extracted ingredients and parsed steps.
     * @throws GeminiParseException if the response cannot be parsed as JSON.
     * @throws Exception for network or API errors.
     */
    suspend fun generateRecipe(
        recipeDescription: String,
        baseServingSize: Int
    ): GeneratedRecipe {
        val prompt = GeminiRecipePrompt.buildPrompt(recipeDescription, baseServingSize)

        Log.d(TAG, "Sending prompt to Gemini (${prompt.length} chars)")

        val response = model.generateContent(prompt)
        val responseText = response.text
            ?: throw GeminiParseException("Gemini returned an empty response.")

        Log.d(TAG, "Received response: ${responseText.take(300)}...")

        return parseRecipe(responseText)
    }

    /**
     * Parses the raw text response from Gemini into a [GeneratedRecipe].
     * Handles common Gemini quirks like markdown code fences wrapping the JSON.
     */
    private fun parseRecipe(rawText: String): GeneratedRecipe {
        // Strip markdown code fences if Gemini wraps the response
        val cleaned = rawText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val geminiResponse: GeminiRecipeResponse = json.decodeFromString(cleaned)

            val parsedSteps = geminiResponse.steps.map { step ->
                val validStepType = step.stepType.uppercase().takeIf {
                    it in listOf("INGREDIENT", "ACTION", "PREP")
                } ?: "ACTION"

                RecipeStep(
                    stepNumber = step.stepNumber,
                    stepType = validStepType,
                    instructionText = step.instructionText,
                    // ingredientName is a raw name — will be resolved to ID by use case
                    ingredientId = step.ingredientName, // temporarily stores name, resolved later
                    quantityMultiplier = step.quantityMultiplier.coerceIn(0.0, 1.0),
                    timerSeconds = step.timerSeconds,
                    flameLevel = step.flameLevel?.lowercase()?.takeIf {
                        it in listOf("low", "medium", "high")
                    },
                    expectedVisualCue = step.expectedVisualCue
                )
            }.sortedBy { it.stepNumber }

            GeneratedRecipe(
                ingredients = geminiResponse.ingredients,
                steps = parsedSteps
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response: ${e.message}", e)
            Log.e(TAG, "Raw response was: $cleaned")
            throw GeminiParseException(
                "Failed to parse AI-generated recipe. Please try again.",
                e
            )
        }
    }
}

/**
 * Exception thrown when Gemini's response cannot be parsed as valid JSON.
 */
class GeminiParseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
