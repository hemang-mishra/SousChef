package com.souschef.util

import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.model.recipe.SupportedLanguages
import kotlin.math.roundToInt

/**
 * Builds a detailed, multi-sentence narration for a single cooking step.
 *
 * Goes well beyond just reading the instruction text: it weaves in the step
 * number, the ingredient + quantity, flame level, timer, and any visual cue
 * so the user can keep their hands on the pan and their eyes off the screen.
 *
 * All template strings are localized for English and Hindi. The narrator
 * picks the localized field on each model (with English fallback) so the
 * output is always linguistically consistent with [language].
 */
object RecipeStepNarrator {

    /**
     * @param step           The step to narrate.
     * @param stepIndex      0-based index (used for "Step N").
     * @param totalSteps     Kept for API compatibility — not spoken (users
     *                       found "of N" distracting while cooking).
     * @param ingredient     Resolved ingredient used in this step (may be null).
     * @param language       Active language code ("en" / "hi").
     */
    fun build(
        step: RecipeStep,
        stepIndex: Int,
        @Suppress("UNUSED_PARAMETER") totalSteps: Int,
        ingredient: ResolvedIngredient?,
        language: String
    ): String {
        val parts = mutableListOf<String>()

        parts += stepHeader(stepIndex + 1, language)
        parts += step.instructionIn(language).ensureSentenceEnd()

        if (ingredient != null) {
            parts += ingredientLine(
                quantity = ingredient.quantity,
                unit = ingredient.unitIn(language),
                name = ingredient.nameIn(language),
                language = language
            )
        }

        step.flameLevelIn(language)?.takeIf { it.isNotBlank() }?.let { flame ->
            parts += flameLine(flame, language)
        }

        step.timerSeconds?.takeIf { it > 0 }?.let { secs ->
            parts += timerLine(secs, language)
        }

        step.expectedVisualCueIn(language)?.takeIf { it.isNotBlank() }?.let { cue ->
            parts += visualCueLine(cue, language)
        }

        return parts.joinToString(separator = " ")
    }

    // ── Localized templates ─────────────────────────────────────────────────

    private fun stepHeader(current: Int, language: String): String =
        when (language) {
            SupportedLanguages.HINDI -> "चरण $current।"
            else -> "Step $current."
        }

    private fun ingredientLine(
        quantity: Double,
        unit: String,
        name: String,
        language: String
    ): String {
        val q = quantity.toFriendlyString()
        return when (language) {
            SupportedLanguages.HINDI -> "$q $unit $name डालें।"
            else -> "Use $q $unit of $name."
        }
    }

    private fun flameLine(flameLevel: String, language: String): String =
        when (language) {
            SupportedLanguages.HINDI -> "आँच $flameLevel रखें।"
            else -> "Set the flame to $flameLevel."
        }

    private fun timerLine(seconds: Int, language: String): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return when (language) {
            SupportedLanguages.HINDI -> when {
                mins == 0 -> "$secs सेकंड के लिए पकाएँ।"
                secs == 0 -> "$mins मिनट के लिए पकाएँ।"
                else -> "$mins मिनट $secs सेकंड के लिए पकाएँ।"
            }
            else -> when {
                mins == 0 -> "Cook for $secs seconds."
                secs == 0 -> "Cook for $mins ${if (mins == 1) "minute" else "minutes"}."
                else -> "Cook for $mins ${if (mins == 1) "minute" else "minutes"} and $secs seconds."
            }
        }
    }

    private fun visualCueLine(cue: String, language: String): String =
        when (language) {
            SupportedLanguages.HINDI -> "देखें: $cue।"
            else -> "Look for: ${cue.trimEnd('.', '!', '?')}."
        }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun Double.toFriendlyString(): String {
        // Show whole numbers without trailing ".0" but keep one decimal
        // for fractional amounts like 1.5.
        val rounded = (this * 10).roundToInt() / 10.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    private fun String.ensureSentenceEnd(): String {
        val trimmed = trim()
        if (trimmed.isEmpty()) return trimmed
        val last = trimmed.last()
        return if (last == '.' || last == '!' || last == '?' || last == '।') trimmed
        else "$trimmed."
    }
}
