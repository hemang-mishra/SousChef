package com.souschef.util

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Simple alert-tone player for short, attention-grabbing chimes.
 *
 * Backed by Android's [ToneGenerator] so it works without any audio assets
 * and plays through the alarm stream — meaning the user will hear it even
 * if their media volume is muted, which is exactly what we want for a
 * cooking timer that's just elapsed.
 *
 * Each call creates a fresh generator and releases it after the tone
 * finishes; this avoids holding an audio focus / DSP slot when the user
 * isn't actively cooking.
 */
object Beeper {

    /**
     * Plays a triple "beep-beep-beep" pattern (~1.5 s total) used to signal
     * that a cooking step's countdown timer has hit zero. Safe to call from
     * any thread; failures are swallowed because audio feedback is
     * non-essential — never worth crashing the app for.
     */
    fun timerFinished() {
        Thread({
            var generator: ToneGenerator? = null
            try {
                generator = ToneGenerator(AudioManager.STREAM_ALARM, MAX_VOLUME)
                repeat(3) { index ->
                    generator.startTone(ToneGenerator.TONE_PROP_BEEP, BEEP_DURATION_MS)
                    Thread.sleep((BEEP_DURATION_MS + GAP_BETWEEN_BEEPS_MS).toLong())
                }
            } catch (e: Exception) {
                android.util.Log.w("Beeper", "timerFinished tone failed: ${e.message}")
            } finally {
                try { generator?.release() } catch (_: Exception) { /* ignore */ }
            }
        }, "souschef-beeper").apply {
            isDaemon = true
            start()
        }
    }

    private const val MAX_VOLUME = 100
    private const val BEEP_DURATION_MS = 220
    private const val GAP_BETWEEN_BEEPS_MS = 80
}
