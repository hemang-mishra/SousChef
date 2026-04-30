package com.souschef.util

import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.model.recipe.SupportedLanguages
import kotlin.math.roundToInt

/**
 * Builds a flowing, conversational narration for a single cooking step.
 *
 * The output is meant to feel like a friendly cook talking the user through
 * the step rather than a bulleted list of facts. Sentences are stitched
 * together with natural connectors ("now", "ab", "phir", "and", "after that")
 * so a Text-to-Speech engine reads them as one breathing paragraph.
 *
 * The narrator is intentionally Hinglish-friendly for the Hindi locale —
 * everyday cooking words like "step", "minute", "second", "garlic" are kept
 * in English, while the connective tissue is in Hindi (Devanagari). This is
 * how most home cooks actually speak.
 */
object RecipeStepNarrator {

    /**
     * @param step           The step to narrate.
     * @param stepIndex      0-based index (used for "Step N").
     * @param totalSteps     Kept for API compatibility — not spoken.
     * @param ingredient     Resolved ingredient used in this step (may be null).
     * @param language       Active language code ("en" / "hi").
     * @param autoTimerStarted If true, append a sentence telling the user the
     *                         timer has started automatically. The user does
     *                         not need to press anything — they can pause it
     *                         from the screen.
     */
    fun build(
        step: RecipeStep,
        stepIndex: Int,
        @Suppress("UNUSED_PARAMETER") totalSteps: Int,
        ingredient: ResolvedIngredient?,
        language: String,
        autoTimerStarted: Boolean = false
    ): String {
        val sb = StringBuilder()

        sb.append(stepHeader(stepIndex + 1, language)).append(' ')

        // Lead-in connector ("Now, ..." / "Toh ab, ...") so the instruction
        // sentence flows out of the step header without a hard stop.
        sb.append(leadIn(language)).append(' ')
        sb.append(step.instructionIn(language).trim().stripTrailingPunct()).append('.')

        if (ingredient != null) {
            sb.append(' ').append(ingredientLine(
                quantity = ingredient.quantity,
                unit = ingredient.unitIn(language),
                name = ingredient.nameIn(language),
                language = language
            ))
        }

        step.flameLevelIn(language)?.takeIf { it.isNotBlank() }?.let { flame ->
            sb.append(' ').append(flameLine(flame, language))
        }

        step.timerSeconds?.takeIf { it > 0 }?.let { secs ->
            sb.append(' ').append(timerLine(secs, language, autoStarted = autoTimerStarted))
        }

        step.expectedVisualCueIn(language)?.takeIf { it.isNotBlank() }?.let { cue ->
            sb.append(' ').append(visualCueLine(cue, language))
        }

        return sb.toString().trim()
    }

    /**
     * Short utterance spoken when a timer auto-starts on step entry.
     * Used when the screen wants to announce only the timer (e.g. on a
     * step the user re-visited, where re-narration is suppressed).
     */
    fun timerStartedAnnouncement(seconds: Int, language: String): String =
        timerStartedSentence(seconds, language)

    // ── Localized templates ─────────────────────────────────────────────────

    private fun stepHeader(current: Int, language: String): String =
        when (language) {
            SupportedLanguages.HINDI -> "Step $current."
            else -> "Step $current."
        }

    private fun leadIn(language: String): String =
        when (language) {
            SupportedLanguages.HINDI -> "Toh ab,"
            else -> "Alright, now"
        }

    private fun ingredientLine(
        quantity: Double,
        unit: String,
        name: String,
        language: String
    ): String {
        val q = quantity.toFriendlyString()
        return when (language) {
            SupportedLanguages.HINDI -> "Iske liye aapko chahiye $q $unit $name — bas itna hi."
            else -> "For this you'll want about $q $unit of $name — that's all you need."
        }
    }

    private fun flameLine(flameLevel: String, language: String): String {
        val level = flameLevel.trim().lowercase()
        return when (language) {
            SupportedLanguages.HINDI -> when (level) {
                "low", "dheemi", "धीमी", "धीमी आँच" -> "Aanch dheemi rakhiyega, jaldi nahi karni."
                "medium", "madhyam", "मध्यम" -> "Aanch medium rakhiye, na zyada tez na bahut dheemi."
                "high", "tez", "तेज़", "तेज" -> "Aanch tez kar dijiye, hum thodi heat chahte hain."
                else -> "Aanch ko $flameLevel par rakhiye."
            }
            else -> when (level) {
                "low" -> "Keep the flame low — we're not in a rush here."
                "medium" -> "Keep the flame on medium, nothing too aggressive."
                "high" -> "Crank the flame up high, we want some real heat."
                else -> "Set the flame to $flameLevel."
            }
        }
    }

    private fun timerLine(seconds: Int, language: String, autoStarted: Boolean): String =
        if (autoStarted) {
            timerStartedSentence(seconds, language)
        } else {
            val duration = formatDuration(seconds, language)
            when (language) {
                SupportedLanguages.HINDI -> "Ise $duration ke liye pakaiye, dhyaan se."
                else -> "Let it cook for $duration, take your time."
            }
        }

    private fun timerStartedSentence(seconds: Int, language: String): String {
        val duration = formatDuration(seconds, language)
        return when (language) {
            SupportedLanguages.HINDI ->
                "Maine $duration ka timer shuru kar diya hai — aap chahein toh screen se pause kar sakte hain."
            else ->
                "I've started a $duration timer for you — you can pause it from the screen any time."
        }
    }

    private fun visualCueLine(cue: String, language: String): String {
        val cleaned = cue.trim().stripTrailingPunct()
        return when (language) {
            SupportedLanguages.HINDI -> "Aur haan, dhyaan rakhiyega — $cleaned."
            else -> "And keep an eye out — $cleaned."
        }
    }

    private fun formatDuration(seconds: Int, language: String): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return when (language) {
            SupportedLanguages.HINDI -> when {
                mins == 0 -> "$secs second"
                secs == 0 -> if (mins == 1) "ek minute" else "$mins minute"
                else -> {
                    val mPart = if (mins == 1) "ek minute" else "$mins minute"
                    "$mPart aur $secs second"
                }
            }
            else -> when {
                mins == 0 -> "$secs ${if (secs == 1) "second" else "seconds"}"
                secs == 0 -> "$mins ${if (mins == 1) "minute" else "minutes"}"
                else -> "$mins ${if (mins == 1) "minute" else "minutes"} and $secs ${if (secs == 1) "second" else "seconds"}"
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun Double.toFriendlyString(): String {
        val rounded = (this * 10).roundToInt() / 10.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    /**
     * Strips a single trailing sentence-ending mark so we can splice the
     * fragment into a longer sentence without producing double-punctuation.
     */
    private fun String.stripTrailingPunct(): String {
        if (isEmpty()) return this
        val last = last()
        return if (last == '.' || last == '!' || last == '?' || last == '।' || last == ',') {
            dropLast(1).trimEnd()
        } else this
    }
}
