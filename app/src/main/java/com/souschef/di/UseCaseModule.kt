package com.souschef.di

import com.souschef.usecases.ingredient.AddGlobalIngredientUseCase
import com.souschef.usecases.ingredient.GetIngredientsUseCase
import com.souschef.usecases.ingredient.UpdateGlobalIngredientUseCase
import com.souschef.usecases.recipe.CreateRecipeUseCase
import com.souschef.usecases.recipe.GenerateRecipeStepsUseCase
import com.souschef.usecases.recipe.PublishRecipeUseCase
import com.souschef.usecases.recipe.RecipeCalculationUseCase
import com.souschef.usecases.recipe.SaveRecipeStepsUseCase
import org.koin.dsl.module

/**
 * Use-case module — register use-case classes as `single` (stateless).
 */
val useCaseModule = module {
    single { CreateRecipeUseCase(get()) }
    single { PublishRecipeUseCase(get()) }
    single { RecipeCalculationUseCase() }
    single { AddGlobalIngredientUseCase(get()) }
    single { UpdateGlobalIngredientUseCase(get()) }
    single { GetIngredientsUseCase(get()) }
    single { GenerateRecipeStepsUseCase(get()) }
    single { SaveRecipeStepsUseCase(get()) }
}
