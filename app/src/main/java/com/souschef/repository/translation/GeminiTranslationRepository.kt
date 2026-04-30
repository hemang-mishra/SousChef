package com.souschef.repository.translation

import com.souschef.service.ai.GeminiTranslationService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Gemini-backed implementation of [TranslationRepository].
 */
class GeminiTranslationRepository(
    private val service: GeminiTranslationService
) : TranslationRepository {

    override fun translateRecipe(
        source: GeminiTranslationService.SourcePayload,
        targetLanguageCode: String
    ): Flow<Resource<GeminiTranslationService.TranslatedPayload>> = flow {
        emit(Resource.loading())
        try {
            val result = service.translate(source, targetLanguageCode)
            emit(Resource.success(result))
        } catch (e: Exception) {
            emit(Resource.failure(message = e.message ?: "Translation failed. Please try again."))
        }
    }
}
