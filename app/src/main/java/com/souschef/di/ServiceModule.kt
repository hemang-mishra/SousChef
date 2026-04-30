package com.souschef.di

import com.souschef.api.GeminiProvider
import com.souschef.api.ble.BleDeviceManager
import com.souschef.util.VoiceToTextParser
import com.souschef.service.ai.GeminiRecipeService
import com.souschef.service.ai.GeminiTranslationService
import com.souschef.service.auth.FirebaseAuthService
import com.souschef.service.device.DevicePreferenceService
import com.souschef.service.ingredient.FirebaseIngredientService
import com.souschef.service.recipe.FirebaseRecipeService
import com.souschef.service.storage.FirebaseStorageService
import com.souschef.service.tts.TtsService
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Service module — register Firebase service classes and BLE manager here.
 * Services wrap raw Firestore / Firebase Auth / BLE calls.
 */
val serviceModule = module {
    single { FirebaseAuthService(get(), get()) }
    single { FirebaseRecipeService(get()) }
    single { FirebaseIngredientService(get()) }
    single { GeminiRecipeService(GeminiProvider.getModel()) }
    single { GeminiTranslationService(GeminiProvider.getJsonModel()) }
    single { FirebaseStorageService(get()) }
    single { VoiceToTextParser(androidApplication()) }

    // ── Phase 5: Hardware ─────────────────────────────────────────────────────
    /** BLE manager — single so the connection survives navigation. */
    single { BleDeviceManager(androidApplication()) }
    /** Preference-backed device service for compartment persistence. */
    single { DevicePreferenceService(get()) }

    // ── Phase 6: Multi-language + TTS ─────────────────────────────────────────
    /** Text-to-Speech engine — single so it can be reused across screens. */
    single { TtsService(androidApplication()) }
}
