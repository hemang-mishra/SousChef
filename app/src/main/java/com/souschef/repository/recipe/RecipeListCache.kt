package com.souschef.repository.recipe

import com.souschef.model.recipe.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-scoped in-memory cache of recipe lists.
 *
 * The repository populates [allRecipes] from the live Firestore listener and
 * the Home/My-Recipes view models read it for instant first paint when the
 * tab is re-entered. Cleared on sign-out via [clear].
 */
class RecipeListCache {
    private val _allRecipes = MutableStateFlow<List<Recipe>?>(null)
    val allRecipes: StateFlow<List<Recipe>?> = _allRecipes

    private val _myRecipes = MutableStateFlow<Map<String, List<Recipe>>>(emptyMap())
    val myRecipes: StateFlow<Map<String, List<Recipe>>> = _myRecipes

    fun updateAll(recipes: List<Recipe>) {
        _allRecipes.value = recipes
    }

    fun updateMine(creatorId: String, recipes: List<Recipe>) {
        _myRecipes.value = _myRecipes.value.toMutableMap().apply { put(creatorId, recipes) }
    }

    fun cachedMyRecipes(creatorId: String): List<Recipe>? = _myRecipes.value[creatorId]

    fun clear() {
        _allRecipes.value = null
        _myRecipes.value = emptyMap()
    }
}
