package com.souschef.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.RecipeTag
import com.souschef.ui.components.EmptyStateView
import com.souschef.ui.components.FullScreenLoader
import com.souschef.ui.components.RecipeCard
import com.souschef.ui.components.RecipeWithMeta
import com.souschef.ui.components.SousChefFilterChip
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.GradientGold
import com.souschef.ui.theme.SousChefTheme

/**
 * Stateful composable — wires ViewModel.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRecipeTap: (String) -> Unit,
    onGenerateSteps: (String) -> Unit,
    onCreateRecipe: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenLayout(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onTagSelected = viewModel::onTagSelected,
        onRecipeTap = onRecipeTap,
        onGenerateSteps = onGenerateSteps,
        onCreateRecipe = onCreateRecipe
    )
}

/**
 * Stateless layout composable — purely presentational.
 */
@Composable
fun HomeScreenLayout(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onTagSelected: (String?) -> Unit,
    onRecipeTap: (String) -> Unit,
    onGenerateSteps: (String) -> Unit,
    onCreateRecipe: () -> Unit
) {
    if (uiState.isLoading) {
        FullScreenLoader(message = "Loading your recipes…")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp) // space for bottom nav
    ) {
        // ── Greeting Header ──────────────────────────────
        item {
            GreetingHeader(userName = uiState.userName)
        }

        // ── Search Bar ───────────────────────────────────
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                placeholder = {
                    Text(
                        "Search your recipes…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textTertiary()
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = AppColors.textTertiary()
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.gold(),
                    unfocusedBorderColor = AppColors.border(),
                    cursorColor = AppColors.gold()
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true
            )
        }

        // ── Category Chips ───────────────────────────────
        item {
            CategoryChipRow(
                selectedTag = uiState.selectedTag,
                onTagSelected = onTagSelected
            )
            Spacer(Modifier.height(12.dp))
        }

        // ── AI Feature Banner (show if user has recipes without steps) ─
        val recipesWithoutSteps = uiState.filteredRecipes.count { !it.hasSteps }
        if (recipesWithoutSteps > 0) {
            item {
                AiSuggestionBanner(count = recipesWithoutSteps)
            }
        }

        // ── Recipe Feed ──────────────────────────────────
        if (uiState.filteredRecipes.isEmpty()) {
            item {
                if (uiState.recipes.isEmpty()) {
                    EmptyStateView(
                        title = "No recipes yet",
                        subtitle = "Create your first recipe and let AI generate the cooking steps!",
                        icon = Icons.Outlined.Restaurant,
                        actionLabel = "Create Recipe",
                        onAction = onCreateRecipe,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                } else {
                    EmptyStateView(
                        title = "No matches",
                        subtitle = "Try a different search or category filter.",
                        icon = Icons.Default.Search,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            }
        } else {
            items(
                items = uiState.filteredRecipes,
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
        }

        // Footer space
        item { Spacer(Modifier.height(20.dp)) }
    }
}

// ── Greeting Header ──────────────────────────────────────

@Composable
private fun GreetingHeader(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // App icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientGold)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Restaurant,
                    contentDescription = null,
                    tint = AppColors.heroBackground(),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (userName.isNotBlank()) "Welcome back," else "Welcome!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary()
                )
                if (userName.isNotBlank()) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary()
                    )
                }
            }
        }
    }
}

// ── Category Chip Row ────────────────────────────────────

@Composable
private fun CategoryChipRow(
    selectedTag: String?,
    onTagSelected: (String?) -> Unit
) {
    val tags = listOf(null) + RecipeTag.entries.map { it.name }
    val labels = listOf("All") + RecipeTag.entries.map { it.displayLabel }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEachIndexed { index, tag ->
            SousChefFilterChip(
                label = labels[index],
                selected = selectedTag == tag,
                onSelectedChange = { onTagSelected(tag) }
            )
        }
    }
}

// ── AI Suggestion Banner ─────────────────────────────────

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
private fun HomeScreenPreview() {
    SousChefTheme {
        HomeScreenLayout(
            uiState = HomeUiState(
                userName = "Hemang",
                isLoading = false,
                recipes = listOf(
                    RecipeWithMeta(
                        recipe = Recipe(
                            recipeId = "1",
                            title = "Butter Chicken Masala",
                            creatorName = "Chef Hemang",
                            baseServingSize = 4,
                            isVerifiedChefRecipe = true,
                            tags = listOf("INDIAN", "SPICY")
                        ),
                        stepCount = 8,
                        hasSteps = true
                    ),
                    RecipeWithMeta(
                        recipe = Recipe(
                            recipeId = "2",
                            title = "Quick Pasta Aglio e Olio",
                            creatorName = "Hemang",
                            baseServingSize = 2,
                            tags = listOf("ITALIAN", "QUICK")
                        ),
                        stepCount = 0,
                        hasSteps = false
                    ),
                    RecipeWithMeta(
                        recipe = Recipe(
                            recipeId = "3",
                            title = "Chocolate Lava Cake",
                            creatorName = "Hemang",
                            baseServingSize = 6,
                            tags = listOf("DESSERT")
                        ),
                        stepCount = 5,
                        hasSteps = true
                    )
                ),
                filteredRecipes = listOf(
                    RecipeWithMeta(
                        recipe = Recipe(
                            recipeId = "1",
                            title = "Butter Chicken Masala",
                            creatorName = "Chef Hemang",
                            baseServingSize = 4,
                            isVerifiedChefRecipe = true,
                            tags = listOf("INDIAN", "SPICY")
                        ),
                        stepCount = 8,
                        hasSteps = true
                    ),
                    RecipeWithMeta(
                        recipe = Recipe(
                            recipeId = "2",
                            title = "Quick Pasta Aglio e Olio",
                            creatorName = "Hemang",
                            baseServingSize = 2,
                            tags = listOf("ITALIAN", "QUICK")
                        ),
                        stepCount = 0,
                        hasSteps = false
                    ),
                    RecipeWithMeta(
                        recipe = Recipe(
                            recipeId = "3",
                            title = "Chocolate Lava Cake",
                            creatorName = "Hemang",
                            baseServingSize = 6,
                            tags = listOf("DESSERT")
                        ),
                        stepCount = 5,
                        hasSteps = true
                    )
                )
            ),
            onSearchQueryChange = {},
            onTagSelected = {},
            onRecipeTap = {},
            onGenerateSteps = {},
            onCreateRecipe = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenEmptyPreview() {
    SousChefTheme {
        HomeScreenLayout(
            uiState = HomeUiState(
                userName = "Hemang",
                isLoading = false,
                recipes = emptyList(),
                filteredRecipes = emptyList()
            ),
            onSearchQueryChange = {},
            onTagSelected = {},
            onRecipeTap = {},
            onGenerateSteps = {},
            onCreateRecipe = {}
        )
    }
}
