package com.souschef.service.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.souschef.model.recipe.SupportedLanguages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

private const val TAG = "TtsService"

/**
 * Lightweight wrapper around Android's [TextToSpeech] engine.
 *
 * Lifecycle:
 * - The service is registered as a Koin `single` and lazily initialised on
 *   the first [speak] call. It survives navigation but should be released via
 *   [shutdown] when the app process exits.
 *
 * Multi-language:
 * - [speak] takes a language code ("en" / "hi") and switches the engine's
 *   locale on demand. If the requested language isn't installed on the device
 *   it falls back to English and surfaces a flag via [missingLanguagePack].
 */
class TtsService(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var initialized = false
    private val pendingUtterances = mutableListOf<PendingUtterance>()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _missingLanguagePack = MutableStateFlow<String?>(null)
    /** Set to a language code when that locale isn't installed on the device. */
    val missingLanguagePack: StateFlow<String?> = _missingLanguagePack

    private data class PendingUtterance(val text: String, val languageCode: String)

    private fun ensureInit() {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            initialized = (status == TextToSpeech.SUCCESS)
            if (!initialized) {
                Log.e(TAG, "TTS engine init failed (status=$status)")
                return@TextToSpeech
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { _isSpeaking.value = true }
                override fun onDone(utteranceId: String?) { _isSpeaking.value = false }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) { _isSpeaking.value = false }
            })

            // Drain anything queued while we were initialising.
            val queued = pendingUtterances.toList()
            pendingUtterances.clear()
            queued.forEach { speak(it.text, it.languageCode) }
        }
    }

    /**
     * Speaks [text] in [languageCode] ("en" / "hi"). Cancels anything currently
     * being spoken so step transitions don't pile up.
     */
    fun speak(text: String, languageCode: String) {
        if (text.isBlank()) return
        ensureInit()
        if (!initialized) {
            pendingUtterances += PendingUtterance(text, languageCode)
            return
        }

        val locale = Locale.forLanguageTag(SupportedLanguages.bcp47(languageCode))
        val supportResult = tts?.setLanguage(locale)
        if (supportResult == TextToSpeech.LANG_MISSING_DATA ||
            supportResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language pack missing for $languageCode — falling back to English.")
            _missingLanguagePack.value = languageCode
            tts?.setLanguage(Locale.US)
        } else {
            _missingLanguagePack.value = null

            try {
                val availableVoices = tts?.voices
                if (availableVoices != null) {
                    val localeVoices = availableVoices.filter { it.locale.language == locale.language }
                    val bestVoice = localeVoices.find { it.isNetworkConnectionRequired || it.name.contains("network", ignoreCase = true) }
                        ?: localeVoices.firstOrNull()

                    if (bestVoice != null) {
                        tts?.voice = bestVoice
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set custom voice", e)
            }

            tts?.setSpeechRate(0.95f)
            tts?.setPitch(0.9f)
        }

        val utteranceId = "souschef_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /** Stops any in-progress utterance immediately. */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /** Releases the underlying engine. Called when the process is going away. */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
        _isSpeaking.value = false
    }
}
