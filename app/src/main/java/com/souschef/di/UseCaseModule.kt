package com.souschef.di

import com.souschef.usecases.device.DispenseSpiceUseCase
import com.souschef.usecases.device.GetCompartmentsUseCase
import com.souschef.usecases.device.RefillCompartmentUseCase
import com.souschef.usecases.device.UpdateCompartmentUseCase
import com.souschef.usecases.ingredient.AddGlobalIngredientUseCase
import com.souschef.usecases.ingredient.GetIngredientsUseCase
import com.souschef.usecases.ingredient.UpdateGlobalIngredientUseCase
import com.souschef.usecases.recipe.CreateRecipeUseCase
import com.souschef.usecases.recipe.DeleteRecipeUseCase
import com.souschef.usecases.recipe.GenerateRecipeStepsUseCase
import com.souschef.usecases.recipe.PublishRecipeUseCase
import com.souschef.usecases.recipe.RecipeCalculationUseCase
import com.souschef.usecases.recipe.SaveRecipeStepsUseCase
import com.souschef.usecases.recipe.UpdateRecipeUseCase
import com.souschef.usecases.translation.TranslateRecipeUseCase
import org.koin.dsl.module

/**
 * Use-case module — register use-case classes as `single` (stateless).
 */
val useCaseModule = module {
    // ── Recipe ────────────────────────────────────────────────────────────────
    single { CreateRecipeUseCase(get()) }
    single { PublishRecipeUseCase(get()) }
    single { RecipeCalculationUseCase() }
    single { SaveRecipeStepsUseCase(get()) }
    single { UpdateRecipeUseCase(get()) }
    single { DeleteRecipeUseCase(get(), get()) }
    /** Now takes IngredientRepository as 2nd arg for fuzzy matching. */
    single { GenerateRecipeStepsUseCase(get(), get()) }

    // ── Translation (multi-language) ──────────────────────────────────────────
    single { TranslateRecipeUseCase(get(), get(), get()) }

    // ── Ingredient ────────────────────────────────────────────────────────────
    single { AddGlobalIngredientUseCase(get()) }
    single { UpdateGlobalIngredientUseCase(get()) }
    single { GetIngredientsUseCase(get()) }

    // ── Phase 5: Device ───────────────────────────────────────────────────────
    single { GetCompartmentsUseCase(get()) }
    single { UpdateCompartmentUseCase(get()) }
    single { RefillCompartmentUseCase(get()) }
    single { DispenseSpiceUseCase(get(), get()) }
}
