package com.souschef.api

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.souschef.BuildConfig

/**
 * Singleton provider for the Google Generative AI (Gemini) model.
 *
 * Uses the API key from BuildConfig (sourced from local.properties).
 *
 * Two model handles are exposed:
 * - [getModel] — generic recipe generation (loose JSON, occasional prose).
 * - [getJsonModel] — translation / structured tasks. Configured with
 *   `responseMimeType = "application/json"` so Gemini does not wrap output
 *   in prose or markdown fences.
 */
object GeminiProvider {

    private const val MODEL_NAME = "gemini-3.1-flash-lite-preview"

    private var model: GenerativeModel? = null
    private var jsonModel: GenerativeModel? = null

    fun getModel(): GenerativeModel {
        return model ?: GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = BuildConfig.GEMINI_API_KEY
        ).also { model = it }
    }

    fun getJsonModel(): GenerativeModel {
        return jsonModel ?: GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.2f
            }
        ).also { jsonModel = it }
    }
}
