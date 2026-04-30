package com.souschef.model.recipe

/**
 * Static fallback dictionary for the well-known English culinary units the
 * AI generation prompt is allowed to emit ("grams", "ml", "tsp", …).
 *
 * Used by [ResolvedIngredient.unitIn] so we always have a localized unit
 * string even when the per-ingredient `localizations` map doesn't contain
 * an entry that matches the recipe's actual unit (e.g. recipe stores
 * "pieces" but the ingredient's default is "kg").
 */
object CommonUnits {

    private val hindi: Map<String, String> = mapOf(
        "grams" to "ग्राम",
        "gram" to "ग्राम",
        "g" to "ग्राम",
        "kg" to "किलो",
        "ml" to "मिली",
        "milliliters" to "मिली",
        "millilitres" to "मिली",
        "l" to "लीटर",
        "liter" to "लीटर",
        "litre" to "लीटर",
        "tsp" to "छोटा चम्मच",
        "teaspoon" to "छोटा चम्मच",
        "teaspoons" to "छोटा चम्मच",
        "tbsp" to "बड़ा चम्मच",
        "tablespoon" to "बड़ा चम्मच",
        "tablespoons" to "बड़ा चम्मच",
        "cup" to "कप",
        "cups" to "कप",
        "piece" to "पीस",
        "pieces" to "पीस",
        "pinch" to "चुटकी",
        "to taste" to "स्वादानुसार",
        "cloves" to "कलियाँ",
        "clove" to "कली",
        "slice" to "टुकड़ा",
        "slices" to "टुकड़े",
        "stick" to "डंडा",
        "sticks" to "डंडे",
        "drop" to "बूँद",
        "drops" to "बूँदें",
        "leaf" to "पत्ता",
        "leaves" to "पत्ते"
    )

    /**
     * Returns the localized unit string for [canonical] (e.g. "pieces") in
     * [language]. Returns null when no entry exists or for English (which
     * is itself the canonical form).
     */
    fun translate(canonical: String, language: String): String? {
        val key = canonical.trim().lowercase()
        if (key.isEmpty()) return null
        return when (language) {
            SupportedLanguages.HINDI -> hindi[key]
            else -> null
        }
    }
}
