package com.souschef.api

import com.google.ai.client.generativeai.GenerativeModel
import com.souschef.BuildConfig

/**
 * Singleton provider for the Google Generative AI (Gemini) model.
 *
 * Uses the API key from BuildConfig (sourced from local.properties).
 * Model: gemini-2.0-flash — latest fast model for low-latency generation.
 */
object GeminiProvider {

    private var model: GenerativeModel? = null

    fun getModel(): GenerativeModel {
        return model ?: GenerativeModel(
            modelName = "gemini-3.1-flash-lite-preview",
            apiKey = BuildConfig.GEMINI_API_KEY
        ).also { model = it }
    }
}
