package com.souschef.util

import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.model.recipe.SupportedLanguages
import kotlin.math.roundToInt

/**
 * Builds a flowing, conversational narration for a single cooking step.
 *
 * Design principles:
 * - Speak like a friend talking the user through the dish, not a list of
 *   facts. Sentences are stitched together with natural connectors so the
 *   TTS engine reads them as one breathing paragraph.
 * - Avoid robotic repetition across steps. Every fragment (lead-in, flame,
 *   ingredient, timer, visual cue) has multiple variants and we pick one
 *   deterministically based on the step index — so step 1 might say
 *   "Alright, let's begin" while step 4 says "Onwards we go".
 * - Skip the "Step N" prefix entirely. The user can already see the step
 *   number on screen and it makes the narration sound mechanical.
 * - The Hindi locale is intentionally Hinglish — everyday cooking words
 *   like "minute", "second", "garlic" stay in English, while the connective
 *   tissue is in Hindi. That's how most home cooks actually speak.
 */
object RecipeStepNarrator {

    /**
     * @param step           The step to narrate.
     * @param stepIndex      0-based index. Used to seed deterministic
     *                       variant selection so a given step always
     *                       sounds the same on replay.
     * @param totalSteps     Total step count. Used to pick a "final step"
     *                       lead-in when relevant.
     * @param ingredient     Resolved ingredient used in this step (may be null).
     * @param language       Active language code ("en" / "hi").
     * @param autoTimerStarted If true, append a sentence telling the user the
     *                         timer has just started automatically.
     */
    fun build(
        step: RecipeStep,
        stepIndex: Int,
        totalSteps: Int,
        ingredient: ResolvedIngredient?,
        language: String,
        autoTimerStarted: Boolean = false
    ): String {
        val sb = StringBuilder()

        // Lead-in replaces the old "Step N." prefix. It varies per step so
        // the listener doesn't hear the same opener five times in a row.
        sb.append(leadIn(stepIndex, totalSteps, language)).append(' ')
        sb.append(step.instructionIn(language).trim().stripTrailingPunct()).append('.')

        if (ingredient != null) {
            sb.append(' ').append(ingredientLine(
                quantity = ingredient.quantity,
                unit = ingredient.unitIn(language),
                name = ingredient.nameIn(language),
                stepIndex = stepIndex,
                language = language
            ))
        }

        step.flameLevelIn(language)?.takeIf { it.isNotBlank() }?.let { flame ->
            sb.append(' ').append(flameLine(flame, stepIndex, language))
        }

        step.timerSeconds?.takeIf { it > 0 }?.let { secs ->
            sb.append(' ').append(timerLine(secs, stepIndex, language, autoStarted = autoTimerStarted))
        }

        step.expectedVisualCueIn(language)?.takeIf { it.isNotBlank() }?.let { cue ->
            sb.append(' ').append(visualCueLine(cue, stepIndex, language))
        }

        return sb.toString().trim()
    }

    /**
     * Short utterance announcing that a timer has just started. Useful when
     * the screen wants to announce only the timer (e.g. when re-narration
     * is suppressed but the auto-start should still be acknowledged).
     */
    fun timerStartedAnnouncement(seconds: Int, language: String): String =
        timerStartedSentence(seconds, 0, language)

    /**
     * Spoken when a step's countdown timer reaches zero. The cooking-mode
     * screen plays this through TTS while also vibrating the device.
     */
    fun timerFinishedAnnouncement(language: String): String =
        when (language) {
            SupportedLanguages.HINDI -> "Bas, time poora ho gaya. Ab agle step par chalein."
            else -> "Time's up — go ahead and move on whenever you're ready."
        }

    // ── Localized templates ─────────────────────────────────────────────────

    private fun leadIn(stepIndex: Int, totalSteps: Int, language: String): String {
        val isFirst = stepIndex == 0
        val isLast  = totalSteps > 0 && stepIndex == totalSteps - 1

        val variants: List<String> = when {
            language == SupportedLanguages.HINDI && isFirst -> listOf(
                "Toh chaliye shuru karte hain.",
                "Chaliye, pehla kaam.",
                "Toh ab shuru karein."
            )
            language == SupportedLanguages.HINDI && isLast -> listOf(
                "Aur ab aakhri kaam.",
                "Bas, ek aur kaam baaki hai.",
                "Chaliye, finishing touch dete hain."
            )
            language == SupportedLanguages.HINDI -> listOf(
                "Toh ab aage badhte hain.",
                "Theek hai, ab agla kaam.",
                "Chalo, ab yeh karte hain.",
                "Achha, ab dhyaan se.",
                "Toh ab,"
            )
            isFirst -> listOf(
                "Alright, let's get started.",
                "Okay, kicking things off.",
                "Let's dive in."
            )
            isLast -> listOf(
                "And here's the last bit.",
                "One last thing to do.",
                "Almost there — final move."
            )
            else -> listOf(
                "Now, moving on.",
                "Okay, next up.",
                "Alright, here we go.",
                "Onwards.",
                "Now then,"
            )
        }
        return variants.pickFor(stepIndex)
    }

    private fun ingredientLine(
        quantity: Double,
        unit: String,
        name: String,
        stepIndex: Int,
        language: String
    ): String {
        val q = quantity.toFriendlyString()
        val variants = when (language) {
            SupportedLanguages.HINDI -> listOf(
                "Iske liye chahiye $q $unit $name.",
                "$q $unit $name le lijiye.",
                "Saath mein $q $unit $name daaliye.",
                "Bas $q $unit $name, aur kuch nahi."
            )
            else -> listOf(
                "You'll want about $q $unit of $name.",
                "Grab $q $unit of $name for this.",
                "Add in $q $unit of $name.",
                "Just $q $unit of $name, that's it."
            )
        }
        return variants.pickFor(stepIndex)
    }

    private fun flameLine(flameLevel: String, stepIndex: Int, language: String): String {
        val level = flameLevel.trim().lowercase()
        val variants: List<String> = when (language) {
            SupportedLanguages.HINDI -> when (level) {
                "low", "dheemi", "धीमी", "धीमी आँच" -> listOf(
                    "Aanch dheemi rakhiyega.",
                    "Flame ko low par rakhiye, jaldi nahi.",
                    "Dheemi aanch — slow aur steady."
                )
                "medium", "madhyam", "मध्यम" -> listOf(
                    "Aanch medium par rakhiye.",
                    "Flame medium — na bahut tez, na bahut dheemi.",
                    "Medium heat ideal rahegi."
                )
                "high", "tez", "तेज़", "तेज" -> listOf(
                    "Aanch tez kar dijiye.",
                    "Flame high — thodi heat chahiye.",
                    "Tez aanch par jaaiye."
                )
                else -> listOf("Aanch ko $flameLevel par rakhiye.")
            }
            else -> when (level) {
                "low" -> listOf(
                    "Keep the flame low.",
                    "A gentle low flame works best here.",
                    "Low heat — slow and steady."
                )
                "medium" -> listOf(
                    "Medium flame, nothing too aggressive.",
                    "Keep it on medium.",
                    "Medium heat is the sweet spot."
                )
                "high" -> listOf(
                    "Crank the flame up high.",
                    "High heat — we want some sizzle.",
                    "Go high on the flame."
                )
                else -> listOf("Set the flame to $flameLevel.")
            }
        }
        return variants.pickFor(stepIndex)
    }

    private fun timerLine(
        seconds: Int,
        stepIndex: Int,
        language: String,
        autoStarted: Boolean
    ): String =
        if (autoStarted) {
            timerStartedSentence(seconds, stepIndex, language)
        } else {
            val duration = formatDuration(seconds, language)
            val variants = when (language) {
                SupportedLanguages.HINDI -> listOf(
                    "Ise $duration ke liye pakaiye.",
                    "$duration ka time chahiye hoga.",
                    "$duration ke liye chodd dijiye, bas."
                )
                else -> listOf(
                    "Let it cook for $duration.",
                    "Give it $duration.",
                    "About $duration should do it."
                )
            }
            variants.pickFor(stepIndex)
        }

    private fun timerStartedSentence(seconds: Int, stepIndex: Int, language: String): String {
        val duration = formatDuration(seconds, language)
        val variants = when (language) {
            SupportedLanguages.HINDI -> listOf(
                "Maine $duration ka timer shuru kar diya hai — pause karna ho toh screen se kar sakte hain.",
                "Timer chal gaya hai, $duration ka — beech mein pause kar sakte hain.",
                "$duration ka timer set ho chuka hai — screen se manage kar lijiye."
            )
            else -> listOf(
                "I've started a $duration timer for you — pause it from the screen any time.",
                "Timer's running — $duration on the clock. Pause from the screen if needed.",
                "Setting a $duration timer for this — feel free to pause from the screen."
            )
        }
        return variants.pickFor(stepIndex)
    }

    private fun visualCueLine(cue: String, stepIndex: Int, language: String): String {
        val cleaned = cue.trim().stripTrailingPunct()
        val variants = when (language) {
            SupportedLanguages.HINDI -> listOf(
                "Dhyaan rakhiyega — $cleaned.",
                "Aap dekhenge — $cleaned.",
                "Iska sign hai — $cleaned."
            )
            else -> listOf(
                "Keep an eye out — $cleaned.",
                "You'll know it's ready when $cleaned.",
                "Watch for this: $cleaned."
            )
        }
        return variants.pickFor(stepIndex)
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

    /**
     * Picks a stable variant for [seed] from this list. The same seed always
     * picks the same entry, so a step's narration is consistent on replay
     * but adjacent steps get different phrasings.
     */
    private fun <T> List<T>.pickFor(seed: Int): T {
        if (isEmpty()) error("pickFor called on empty list")
        val idx = ((seed % size) + size) % size
        return this[idx]
    }

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
