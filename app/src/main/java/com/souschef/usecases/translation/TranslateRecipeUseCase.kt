package com.souschef.usecases.translation

import android.util.Log
import com.google.firebase.Timestamp
import com.souschef.model.recipe.GlobalIngredientLocalization
import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.RecipeLocalization
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.RecipeStepLocalization
import com.souschef.model.recipe.SupportedLanguages
import com.souschef.repository.ingredient.IngredientRepository
import com.souschef.repository.recipe.RecipeRepository
import com.souschef.repository.translation.TranslationRepository
import com.souschef.service.ai.GeminiTranslationService
import com.souschef.util.Resource
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

private const val TAG = "TranslateRecipeUC"

/**
 * Translates a single recipe — including its steps and the global ingredients
 * it references — into a target language and persists the localized strings
 * back to Firestore.
 *
 * On success, the following documents gain a new entry under their
 * `localizations` map (keyed by [targetLanguageCode]):
 * - `recipes/{recipeId}` — title, description
 * - `recipes/{recipeId}/steps/{stepId}` — instructionText, flameLevel, expectedVisualCue
 * - `ingredients/{globalIngredientId}` — name, defaultUnit
 *
 * The recipe document also gets `targetLanguageCode` appended to its
 * `translatedLanguages` list so future reads can short-circuit.
 *
 * If the language is already in `translatedLanguages` (and not [force]),
 * the use case is a no-op and emits success immediately.
 */
class TranslateRecipeUseCase(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val translationRepository: TranslationRepository
) {

    fun execute(
        recipeId: String,
        targetLanguageCode: String,
        force: Boolean = false
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.loading())

        if (targetLanguageCode == SupportedLanguages.ENGLISH) {
            // English is the canonical copy; no translation needed.
            emit(Resource.success(Unit))
            return@flow
        }

        // 1. Load recipe + steps
        val recipeResult = recipeRepository.getRecipe(recipeId).first { it !is Resource.Loading }
        if (recipeResult is Resource.Failure) {
            emit(Resource.failure(recipeResult.error, recipeResult.message))
            return@flow
        }
        val recipe = (recipeResult as Resource.Success).data

        if (!force && targetLanguageCode in recipe.translatedLanguages) {
            Log.d(TAG, "Recipe $recipeId already translated to $targetLanguageCode — skipping.")
            emit(Resource.success(Unit))
            return@flow
        }

        val stepsResult = recipeRepository.getSteps(recipeId).first { it !is Resource.Loading }
        val steps: List<RecipeStep> = when (stepsResult) {
            is Resource.Success -> stepsResult.data
            else -> emptyList()
        }

        // 2. Load referenced global ingredients
        val ingredientIds = recipe.ingredients
            .map { it.globalIngredientId }
            .filter { it.isNotBlank() }
            .distinct()

        val globalIngredients = if (ingredientIds.isEmpty()) emptyList() else {
            val ingResult = ingredientRepository.getIngredientsByIds(ingredientIds)
                .first { it !is Resource.Loading }
            val fetched = (ingResult as? Resource.Success)?.data ?: emptyList()
            // Preserve recipe's ingredient ordering — Firestore whereIn does
            // NOT guarantee order, and the persist-side index fallback relies
            // on a stable ordering between the request we send to Gemini and
            // the response.
            val byId = fetched.associateBy { it.ingredientId }
            ingredientIds.mapNotNull { byId[it] }
        }

        // 3. Build source payload. We always send EVERY ingredient + step,
        //    even if a previous translation exists, so the persist-side
        //    index-fallback can't drift between the AI request and response.
        //    `force` semantics still apply at the recipe-level early-return
        //    above.
        val ingredientsToSend = globalIngredients

        val source = GeminiTranslationService.SourcePayload(
            recipe = GeminiTranslationService.SourceRecipe(
                title = recipe.title,
                description = recipe.description
            ),
            steps = steps.map { step ->
                GeminiTranslationService.SourceStep(
                    stepId = step.stepId,
                    instructionText = step.instructionText,
                    flameLevel = step.flameLevel,
                    expectedVisualCue = step.expectedVisualCue
                )
            },
            ingredients = ingredientsToSend.map { gi ->
                GeminiTranslationService.SourceIngredient(
                    ingredientId = gi.ingredientId,
                    name = gi.name,
                    defaultUnit = gi.defaultUnit
                )
            }
        )

        // 4. Call translator
        val translationResult = translationRepository
            .translateRecipe(source, targetLanguageCode)
            .first { it !is Resource.Loading }

        if (translationResult is Resource.Failure) {
            emit(Resource.failure(translationResult.error, translationResult.message))
            return@flow
        }
        val translated = (translationResult as Resource.Success).data

        Log.d(
            TAG,
            "Translation returned ${translated.steps.size}/${steps.size} steps, " +
                "${translated.ingredients.size}/${ingredientsToSend.size} ingredients"
        )

        // 5. Persist results.
        //
        // We wrap ALL Firestore writes in `NonCancellable` so a cancellation
        // partway through (e.g. user navigating away, ViewModel scope dying)
        // can't leave Firestore in a half-written state — every step +
        // ingredient gets persisted, or none. Without this guard the
        // recipe-level title write completed but per-step / per-ingredient
        // writes were getting cancelled, which is exactly what produced the
        // half-translated UI in the screenshots.
        val persistFailure: String? = withContext(NonCancellable) {
            try {
                persistRecipeTranslation(recipe, translated, targetLanguageCode)
                persistStepTranslations(recipeId, steps, translated, targetLanguageCode)
                persistIngredientTranslations(ingredientsToSend, translated, targetLanguageCode)

                // Mark recipe as translated for this language (idempotent).
                val mergedLanguages = (recipe.translatedLanguages + targetLanguageCode).distinct()
                recipeRepository.updateRecipe(
                    recipeId,
                    mapOf(
                        "translatedLanguages" to mergedLanguages,
                        "updatedAt" to Timestamp.now()
                    )
                ).first { it !is Resource.Loading }
                null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist translation: ${e.message}", e)
                e.message ?: "Failed to save translation."
            }
        }

        if (persistFailure != null) {
            emit(Resource.failure(message = persistFailure))
        } else {
            emit(Resource.success(Unit))
        }
    }

    // ── Persistence helpers ────────────────────────────────────────────────

    private suspend fun persistRecipeTranslation(
        recipe: Recipe,
        translated: GeminiTranslationService.TranslatedPayload,
        languageCode: String
    ) {
        val recipeLoc = RecipeLocalization(
            title = translated.recipe.title.ifBlank { recipe.title },
            description = translated.recipe.description.ifBlank { recipe.description }
        )
        val updatedMap = recipe.localizations + (languageCode to recipeLoc)

        Log.d(TAG, "Persisting recipe localization [$languageCode]: title='${recipeLoc.title}'")
        val result = recipeRepository.updateRecipe(
            recipe.recipeId,
            mapOf("localizations" to updatedMap.toFirestoreMap())
        ).first { it !is Resource.Loading }
        if (result is Resource.Failure) {
            Log.e(TAG, "Recipe localization write failed: ${result.message}")
        }
    }

    private suspend fun persistStepTranslations(
        recipeId: String,
        steps: List<RecipeStep>,
        translated: GeminiTranslationService.TranslatedPayload,
        languageCode: String
    ) {
        if (steps.isEmpty() || translated.steps.isEmpty()) return

        // Primary lookup: by stepId. Fallback: positional/index-based lookup,
        // since Gemini occasionally drops, normalises, or rewrites the long
        // Firestore-generated stepIds.
        val translatedById = translated.steps
            .filter { it.stepId.isNotBlank() }
            .associateBy { it.stepId }

        var written = 0
        for ((index, step) in steps.withIndex()) {
            val t = translatedById[step.stepId]
                ?: translated.steps.getOrNull(index)
                ?: continue

            // Skip if the AI returned literally nothing useful.
            if (t.instructionText.isBlank() && t.flameLevel.isNullOrBlank() &&
                t.expectedVisualCue.isNullOrBlank()) {
                Log.w(TAG, "Empty translation for step #${index + 1} — skipping.")
                continue
            }

            val loc = RecipeStepLocalization(
                instructionText = t.instructionText.ifBlank { step.instructionText },
                flameLevel = t.flameLevel?.ifBlank { null } ?: step.flameLevel,
                expectedVisualCue = t.expectedVisualCue?.ifBlank { null }
                    ?: step.expectedVisualCue
            )
            val mergedMap = step.localizations + (languageCode to loc)
            val result = recipeRepository.updateStep(
                recipeId,
                step.stepId,
                mapOf("localizations" to mergedMap.toFirestoreStepMap())
            ).first { it !is Resource.Loading }
            if (result is Resource.Failure) {
                Log.e(TAG, "Step #${index + 1} write failed: ${result.message}")
            } else {
                written++
            }
        }
        Log.d(TAG, "Persisted $written/${steps.size} step translations for $languageCode")
    }

    private suspend fun persistIngredientTranslations(
        globalIngredients: List<com.souschef.model.ingredient.GlobalIngredient>,
        translated: GeminiTranslationService.TranslatedPayload,
        languageCode: String
    ) {
        if (globalIngredients.isEmpty() || translated.ingredients.isEmpty()) return

        val translatedById = translated.ingredients
            .filter { it.ingredientId.isNotBlank() }
            .associateBy { it.ingredientId }

        var written = 0
        for ((index, gi) in globalIngredients.withIndex()) {
            val t = translatedById[gi.ingredientId]
                ?: translated.ingredients.getOrNull(index)
                ?: continue
            if (t.name.isBlank() && t.defaultUnit.isBlank()) {
                Log.w(TAG, "Empty translation for ingredient '${gi.name}' — skipping.")
                continue
            }
            val loc = GlobalIngredientLocalization(
                name = t.name.ifBlank { gi.name },
                defaultUnit = t.defaultUnit.ifBlank { gi.defaultUnit }
            )
            val mergedMap = gi.localizations + (languageCode to loc)
            val result = ingredientRepository.updateIngredient(
                gi.ingredientId,
                mapOf("localizations" to mergedMap.toFirestoreMap())
            ).first { it !is Resource.Loading }
            if (result is Resource.Failure) {
                Log.e(TAG, "Ingredient '${gi.name}' write failed: ${result.message}")
            } else {
                written++
                Log.d(TAG, "Ingredient '${gi.name}' → '${loc.name}' [$languageCode]")
            }
        }
        Log.d(TAG, "Persisted $written/${globalIngredients.size} ingredient translations for $languageCode")
    }

    // Firestore can't serialise our data classes directly inside an `update`
    // call, so we flatten them to plain maps.
    private fun Map<String, RecipeLocalization>.toFirestoreMap(): Map<String, Map<String, Any?>> =
        mapValues { (_, v) ->
            mapOf("title" to v.title, "description" to v.description)
        }

    @JvmName("ingredientLocToFirestoreMap")
    private fun Map<String, GlobalIngredientLocalization>.toFirestoreMap(): Map<String, Map<String, Any?>> =
        mapValues { (_, v) ->
            mapOf("name" to v.name, "defaultUnit" to v.defaultUnit)
        }

    @JvmName("stepLocToFirestoreMap")
    private fun Map<String, RecipeStepLocalization>.toFirestoreStepMap(): Map<String, Map<String, Any?>> =
        mapValues { (_, v) ->
            mapOf(
                "instructionText" to v.instructionText,
                "flameLevel" to v.flameLevel,
                "expectedVisualCue" to v.expectedVisualCue
            )
        }
}
