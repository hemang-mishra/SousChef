package com.souschef.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Small utility around the platform [Vibrator] for cooking-mode haptic
 * feedback. We keep this off the View / HapticFeedbackConstants path for the
 * stronger patterns because those are quite subtle on most devices, and a
 * timer finishing across a noisy kitchen really benefits from something the
 * user can feel through their pocket.
 */
object Haptics {

    /**
     * Plays a triple-pulse pattern (≈ "buzz-buzz-buzz") to signal that a
     * cooking step's countdown timer has just hit zero. Falls back silently
     * on devices that don't expose a [Vibrator].
     */
    fun timerFinished(context: Context) {
        // Wrap everything in try/catch — on some OEM ROMs, calling vibrate()
        // even with the VIBRATE permission can throw (e.g. when the OS is
        // shutting down the activity). A buzz is non-essential feedback;
        // a crash absolutely is not.
        try {
            val vibrator = vibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Pattern: wait 0ms, vibrate 250ms, off 120ms, vibrate 250ms,
                //          off 120ms, vibrate 400ms.
                val timings = longArrayOf(0L, 250L, 120L, 250L, 120L, 400L)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0L, 250L, 120L, 250L, 120L, 400L), -1)
            }
        } catch (e: Exception) {
            android.util.Log.w("Haptics", "timerFinished vibrate failed: ${e.message}")
        }
    }

    private fun vibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}
