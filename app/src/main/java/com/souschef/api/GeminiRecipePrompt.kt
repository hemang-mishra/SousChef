package com.souschef.api

import com.souschef.model.recipe.ResolvedIngredient

/**
 * Prompt engineering for Gemini recipe step generation.
 *
 * Produces a structured system prompt that instructs Gemini to return
 * a JSON array of atomic cooking steps matching the RecipeStep schema.
 */
object GeminiRecipePrompt {

    /**
     * Builds the full prompt for Gemini, injecting the recipe description
     * and ingredient list into the template.
     *
     * @param recipeDescription Free-text description of the recipe by the creator.
     * @param ingredients Resolved ingredient list with names and quantities.
     * @return The complete prompt string to send to the Gemini model.
     */
    fun buildPrompt(
        recipeDescription: String,
        ingredients: List<ResolvedIngredient>
    ): String {
        val ingredientList = ingredients.joinToString("\n") { ingredient ->
            "- ${ingredient.name}: ${ingredient.quantity} ${ingredient.unit}"
        }

        return """
You are a professional chef and recipe writer. Your task is to take a recipe description and convert it into a structured list of atomic cooking steps.

Each step must:
- Be a single, specific, executable action.
- Never assume prior preparation unless explicitly stated.
- Include all implicit steps (washing, chopping, preheating, etc.).
- Be written in plain, clear language.
- Be sequenced in the correct cooking order.

Return a JSON array. Each object must have these fields:
- stepNumber: Int (starting from 1)
- instructionText: String
- timerSeconds: Int or null (only if a specific time is mentioned or commonly needed)
- flameLevel: "low" or "medium" or "high" or null (only for stovetop steps)
- expectedVisualCue: String or null (what to look for to know this step is done)
- ingredientReferences: String[] (ingredient names used in this step, empty array if none)

Recipe Description:
$recipeDescription

Ingredients:
$ingredientList

Return ONLY the JSON array. No explanation. No markdown. No code fences. Just the raw JSON array.
        """.trimIndent()
    }
}
