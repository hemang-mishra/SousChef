package com.souschef.util

import com.souschef.model.ingredient.GlobalIngredient

/**
 * Pure Kotlin fuzzy-matching utility for linking AI-generated ingredient name references
 * to records in the global ingredient library.
 *
 * Algorithm: Dice coefficient on character bigrams (≥ 0.75 = match).
 * Fast, dependency-free, handles minor spelling variants and spacing differences.
 */
object IngredientMatcher {

    private const val MATCH_THRESHOLD = 0.75

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Attempts to find the best-matching [GlobalIngredient] for a raw [query] name.
     *
     * @param query     Raw ingredient name string from AI output.
     * @param candidates Full list of [GlobalIngredient] records.
     * @return Best matching ingredient if similarity ≥ [MATCH_THRESHOLD], else `null`.
     */
    fun fuzzyMatch(query: String, candidates: List<GlobalIngredient>): GlobalIngredient? {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank() || candidates.isEmpty()) return null

        var bestScore = 0.0
        var bestMatch: GlobalIngredient? = null

        for (candidate in candidates) {
            val candidateNameNorm = normalize(candidate.name)
            
            // Substring or exact match is very strong, especially for short words like 'salt' or 'turmeric'
            if (normalizedQuery.contains(candidateNameNorm) || candidateNameNorm.contains(normalizedQuery)) {
                return candidate
            }
            
            val score = diceCoefficient(normalizedQuery, candidateNameNorm)
            if (score > bestScore) {
                bestScore = score
                bestMatch = candidate
            }
        }

        return if (bestScore >= MATCH_THRESHOLD) bestMatch else null
    }

    /**
     * Resolves all raw ingredient name references in [steps] against the global library.
     *
     * @return A map of `rawName → globalIngredientId` for successfully matched names.
     *         Names that couldn't be matched are omitted from the map.
     */
    fun buildResolutionMap(
        rawNames: List<String>,
        candidates: List<GlobalIngredient>
    ): Map<String, String> {
        return rawNames
            .distinct()
            .mapNotNull { name ->
                val match = fuzzyMatch(name, candidates)
                if (match != null) name to match.ingredientId else null
            }
            .toMap()
    }

    /**
     * Returns the similarity score (0.0–1.0) between two strings using Dice coefficient.
     * Exposed for unit testing.
     */
    fun similarityScore(a: String, b: String): Double =
        diceCoefficient(normalize(a), normalize(b))

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Normalises a string: lowercase, removes non-alphanumeric chars, collapses whitespace.
     */
    private fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .trim()
            .replace(Regex("\\s+"), " ")

    /**
     * Dice coefficient on character bigrams:
     * score = 2 × |intersection| / (|bigrams(a)| + |bigrams(b)|)
     *
     * Handles single-character strings gracefully (falls back to exact equality).
     */
    private fun diceCoefficient(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.length < 2 || b.length < 2) {
            return if (a == b) 1.0 else 0.0
        }

        val bigramsA = bigrams(a)
        val bigramsB = bigrams(b)

        val intersection = bigramsA.toMutableList().let { listA ->
            var count = 0
            for (bg in bigramsB) {
                if (listA.remove(bg)) count++
            }
            count
        }

        return (2.0 * intersection) / (bigramsA.size + bigramsB.size)
    }

    /** Generates all consecutive character bigrams for [text]. */
    private fun bigrams(text: String): List<String> =
        (0 until text.length - 1).map { text.substring(it, it + 2) }
}
