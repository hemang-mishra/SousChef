package com.souschef.api

/**
 * Prompt engineering for Gemini recipe generation.
 *
 * Produces a structured system prompt that instructs Gemini to return
 * a JSON object containing:
 * - `ingredients[]` — complete ingredient list with quantities for the base serving size.
 * - `steps[]` — atomic, single-ingredient cooking steps.
 *
 * The AI is expected to **infer** any missing details from culinary knowledge.
 * No pre-built ingredient list is required — the model figures it all out.
 */
object GeminiRecipePrompt {

    /**
     * Builds the full prompt for Gemini.
     *
     * @param recipeDescription Free-text description of the recipe by the creator.
     * @param baseServingSize   Number of servings the quantities should be for.
     * @return The complete prompt string to send to the Gemini model.
     */
    fun buildPrompt(
        recipeDescription: String,
        baseServingSize: Int
    ): String {
        return """
You are a professional chef and recipe writer. Your task is to take a recipe description and produce a COMPLETE recipe with ingredients and atomic cooking steps.

IMPORTANT: The user may give a brief description like "Kadhai Paneer" or a detailed walkthrough. Either way, you MUST:
1. Identify ALL ingredients needed (including implicit ones like oil, water, salt, etc.)
2. Provide realistic quantities for $baseServingSize servings
3. Break the cooking process into precise, atomic steps

OUTPUT FORMAT — Return a single JSON object with two arrays:

{
  "ingredients": [ ... ],
  "steps": [ ... ]
}

═══════════════════════════════════════
INGREDIENTS ARRAY
═══════════════════════════════════════
Each ingredient object:
- name: String (common English name, e.g. "Paneer", "Red Chili Powder")
- quantity: Double (amount for $baseServingSize servings)
- unit: String (one of: "grams", "ml", "tsp", "tbsp", "cups", "pieces", "pinch", "to taste")

Rules:
- Include EVERY ingredient, even obvious ones (oil, water, salt).
- Use realistic quantities a home cook would actually use.
- If the description doesn't specify exact amounts, use your culinary expertise to estimate.

═══════════════════════════════════════
STEPS ARRAY
═══════════════════════════════════════
Each step object:
- stepNumber: Int (starting from 1)
- stepType: String ("INGREDIENT", "ACTION", or "PREP")
- instructionText: String (NO quantities — only the action and ingredient name)
- ingredientName: String or null (EXACT name from the ingredients array above, null for ACTION steps)
- quantityMultiplier: Double (0.0–1.0, what fraction of total quantity is used here)
- timerSeconds: Int or null
- flameLevel: "low" or "medium" or "high" or null
- expectedVisualCue: String or null

STEP TYPE RULES:
- "INGREDIENT" — Adding a single ingredient. Must have ingredientName and quantityMultiplier.
- "ACTION" — Cooking action (stir, heat, simmer). No ingredient.
- "PREP" — Preparation (wash, chop, preheat). May reference one ingredient being prepared.

CRITICAL STEP RULES:
1. Every ingredient addition MUST be its own step. NEVER combine "add A, B, and C".
2. NEVER put quantities in instructionText. Write "Add the garlic" not "Add 4 cloves of garlic".
3. Each step = one specific action.
4. Include all implicit prep steps (preheating, chopping, etc.)
5. Steps must be in correct cooking order.
6. quantityMultiplier defaults to 1.0. If an ingredient is split (e.g. "half now, half later"), use 0.5 each.
7. The sum of quantityMultiplier for the same ingredient across all steps should equal 1.0.

Recipe Description:
$recipeDescription

Base Serving Size: $baseServingSize servings

Return ONLY the JSON object. No explanation. No markdown. No code fences. Just raw JSON.
        """.trimIndent()
    }
}
