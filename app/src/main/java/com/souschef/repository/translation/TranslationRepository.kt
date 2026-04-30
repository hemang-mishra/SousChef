package com.souschef.repository.translation

import com.souschef.service.ai.GeminiTranslationService
import com.souschef.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for AI-powered recipe translation operations.
 * Wraps [GeminiTranslationService] in a Resource flow for consistent error handling.
 */
interface TranslationRepository {
    /**
     * Translates [source] (recipe + steps + ingredients in English) into
     * [targetLanguageCode]. Emits Loading → Success(translated) or Failure.
     */
    fun translateRecipe(
        source: GeminiTranslationService.SourcePayload,
        targetLanguageCode: String
    ): Flow<Resource<GeminiTranslationService.TranslatedPayload>>
}
