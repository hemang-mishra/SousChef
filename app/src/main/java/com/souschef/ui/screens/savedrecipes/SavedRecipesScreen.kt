package com.souschef.ui.screens.savedrecipes

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.recipe.Recipe
import com.souschef.ui.components.EmptyStateView
import com.souschef.ui.components.RecipeCard
import com.souschef.ui.components.RecipeWithMeta
import com.souschef.ui.screens.home.HomeUiState
import com.souschef.ui.screens.home.HomeViewModel
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.SousChefTheme

import com.souschef.ui.components.RecipeListShimmer

/**
 * My Recipes screen — shows all recipes created by the current user.
 *
 * Reuses [HomeViewModel] since it already loads user's recipes.
 * We just present them differently (no search/chips, simpler layout).
 */
@Composable
fun SavedRecipesScreen(
    viewModel: SavedRecipesViewModel,
    onRecipeTap: (String) -> Unit,
    onGenerateSteps: (String) -> Unit,
    onCreateRecipe: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SavedRecipesLayout(
        uiState = uiState,
        onRecipeTap = onRecipeTap,
        onGenerateSteps = onGenerateSteps,
        onCreateRecipe = onCreateRecipe
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRecipesLayout(
    uiState: HomeUiState,
    onRecipeTap: (String) -> Unit,
    onGenerateSteps: (String) -> Unit,
    onCreateRecipe: () -> Unit
) {
    if (uiState.isLoading) {
        RecipeListShimmer()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            TopAppBar(
                windowInsets = WindowInsets(top = 0.dp),
                title = {
                    Text(
                        text = "My Recipes",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.textPrimary()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }

        if (uiState.recipes.isEmpty()) {
            item {
                EmptyStateView(
                    title = "No recipes yet",
                    subtitle = "Create your first recipe to see it here.",
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    actionLabel = "Create Recipe",
                    onAction = onCreateRecipe,
                    modifier = Modifier.padding(top = 48.dp)
                )
            }
        } else {
            item {
                Text(
                    text = "${uiState.recipes.size} recipe${if (uiState.recipes.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textTertiary(),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            val recipesWithoutSteps = uiState.recipes.count { !it.hasSteps }
            if (recipesWithoutSteps > 0) {
                item {
                    AiSuggestionBanner(count = recipesWithoutSteps)
                }
            }

            items(
                items = uiState.recipes,
                key = { it.recipe.recipeId }
            ) { recipeWithMeta ->
                RecipeCard(
                    recipeWithMeta = recipeWithMeta,
                    onClick = { onRecipeTap(recipeWithMeta.recipe.recipeId) },
                    onGenerateSteps = if (!recipeWithMeta.hasSteps) {
                        { onGenerateSteps(recipeWithMeta.recipe.recipeId) }
                    } else null,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun AiSuggestionBanner(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.gold().copy(alpha = 0.08f))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AppColors.gold(),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "$count recipe${if (count > 1) "s" else ""} need${if (count == 1) "s" else ""} cooking steps — tap ✨ to generate with AI",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.gold(),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SavedRecipesPreview() {
    SousChefTheme {
        SavedRecipesLayout(
            uiState = HomeUiState(
                isLoading = false,
                recipes = listOf(
                    RecipeWithMeta(
                        recipe = Recipe(recipeId = "1", title = "Butter Chicken", creatorName = "You", baseServingSize = 4, tags = listOf("INDIAN")),
                        stepCount = 6,
                        hasSteps = true
                    ),
                    RecipeWithMeta(
                        recipe = Recipe(recipeId = "2", title = "Pasta Carbonara", creatorName = "You", baseServingSize = 2, tags = listOf("ITALIAN")),
                        stepCount = 0,
                        hasSteps = false
                    )
                )
            ),
            onRecipeTap = {},
            onGenerateSteps = {},
            onCreateRecipe = {}
        )
    }
}
