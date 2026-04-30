package com.souschef.api

/**
 * Prompt engineering for Gemini recipe translation.
 *
 * Takes a JSON payload that bundles the English copy of a single recipe
 * (recipe metadata + steps + referenced global ingredients) and instructs
 * Gemini to return the same JSON shape with all user-facing fields translated
 * into the target language.
 *
 * Numeric / structural fields (timerSeconds, quantityMultiplier, stepType, ids)
 * are preserved verbatim.
 */
object GeminiTranslationPrompt {

    /**
     * @param targetLanguage human-readable language name shown to the model
     *                       (e.g. "Hindi"). Use [targetLanguageCode] to drive
     *                       this — see [com.souschef.model.recipe.SupportedLanguages].
     * @param targetLanguageCode ISO-639-1 code (e.g. "hi") — shown to the model
     *                           and embedded into output keys.
     * @param sourceJson         JSON payload (see schema below) containing the
     *                           recipe + steps + ingredients that need to be
     *                           translated.
     */
    fun buildPrompt(
        targetLanguage: String,
        targetLanguageCode: String,
        sourceJson: String
    ): String {
        return """
You are a professional culinary translator. You translate cooking recipes — including step instructions, ingredient names, and culinary units — into $targetLanguage ($targetLanguageCode) while keeping the meaning, tone, and cooking precision intact.

You will be given a JSON object describing ONE recipe in English. You must return the same JSON shape, but with all user-facing TEXT fields translated into $targetLanguage. **Do NOT translate or modify any IDs, numbers, booleans, enum codes, or structural keys.**

═══════════════════════════════════════
INPUT SCHEMA (English)
═══════════════════════════════════════
{
  "recipe": { "title": "...", "description": "..." },
  "steps": [
    {
      "stepId": "...",                 // do NOT translate
      "instructionText": "...",        // TRANSLATE
      "flameLevel": "low|medium|high|null", // TRANSLATE to natural $targetLanguage equivalent
      "expectedVisualCue": "..."       // TRANSLATE (or null)
    }
  ],
  "ingredients": [
    {
      "ingredientId": "...",           // do NOT translate
      "name": "...",                   // TRANSLATE — use natural local culinary names where they exist (e.g. "Coriander" → "धनिया")
      "defaultUnit": "grams|ml|tsp|..." // TRANSLATE to natural unit word in $targetLanguage
    }
  ]
}

═══════════════════════════════════════
OUTPUT SCHEMA (translated)
═══════════════════════════════════════
Return EXACTLY this shape:
{
  "languageCode": "$targetLanguageCode",
  "recipe": { "title": "...", "description": "..." },
  "steps": [
    { "stepId": "<copy>", "instructionText": "...", "flameLevel": "...", "expectedVisualCue": "..." }
  ],
  "ingredients": [
    { "ingredientId": "<copy>", "name": "...", "defaultUnit": "..." }
  ]
}

═══════════════════════════════════════
TRANSLATION RULES (READ CAREFULLY)
═══════════════════════════════════════
1. **CRITICAL — Preserve every `stepId` and `ingredientId` EXACTLY as given (byte-for-byte, including casing and any random-looking characters). These are database keys; the app cannot match translations back to records if they change.**
2. **CRITICAL — Output the SAME number of steps and ingredients in the SAME ORDER as the input. Do not merge, drop, reorder, or insert items. If the input has 18 steps, output 18 step objects.**
3. EVERY step MUST appear in the output with a non-empty `instructionText`. Do not leave any step un-translated.
4. EVERY ingredient MUST appear in the output with a non-empty `name`. Do not leave any ingredient un-translated.
5. Translate naturally — not literally. Use the cooking vocabulary a native $targetLanguage cook would actually use.
6. Use Devanagari script for Hindi (no transliteration). Use the script native to the target language.
7. Numbers / quantities NEVER appear in instructionText (the source obeys this rule). Do not invent quantities.
8. Keep flameLevel terse (1–3 words). Examples for Hindi: "low" → "धीमी आँच", "medium" → "मध्यम आँच", "high" → "तेज़ आँच".
9. If a field is null in the input, return null (or omit it) in the output.
10. defaultUnit should be a single short noun. Examples for Hindi: "grams" → "ग्राम", "ml" → "मिली", "tsp" → "छोटा चम्मच", "tbsp" → "बड़ा चम्मच", "cups" → "कप", "pieces" → "पीस", "pinch" → "चुटकी", "to taste" → "स्वादानुसार".
11. Ingredient names should use the most common $targetLanguage culinary name. If no native term exists, transliterate.

═══════════════════════════════════════
INPUT JSON
═══════════════════════════════════════
$sourceJson

Return ONLY the translated JSON object. No explanation. No markdown. No code fences. Just raw JSON.
        """.trimIndent()
    }
}
