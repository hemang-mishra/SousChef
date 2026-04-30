package com.souschef.ui.screens.ingredient.library

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.ui.components.EmptyStateView
import com.souschef.ui.components.IngredientListShimmer
import com.souschef.ui.components.SousChefTextField
import com.souschef.ui.theme.SousChefTheme
import org.koin.compose.koinInject

@Composable
fun IngredientLibraryScreen(
    onBack: () -> Unit,
    onAddIngredient: () -> Unit,
    onEditIngredient: (String) -> Unit,
    viewModel: IngredientLibraryViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    IngredientLibraryLayout(uiState, onBack, viewModel::onSearchQueryChange, onAddIngredient, onEditIngredient)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientLibraryLayout(
    uiState: IngredientLibraryUiState,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddIngredient: () -> Unit,
    onIngredientClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingredient Library", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(0.dp) ,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddIngredient,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Ingredient")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SousChefTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                label = "Search ingredients",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                uiState.isLoading -> {
                    IngredientListShimmer()
                }
                uiState.error != null -> {
                    EmptyStateView(
                        icon = Icons.Outlined.Restaurant,
                        title = "Something went wrong",
                        subtitle = uiState.error
                    )
                }
                uiState.filteredIngredients.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.Restaurant,
                        title = if (uiState.searchQuery.isNotBlank()) "No results" else "No ingredients yet",
                        subtitle = if (uiState.searchQuery.isNotBlank()) "Try a different search term" else "Tap + to add your first ingredient"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.filteredIngredients, key = { it.ingredientId }) { ingredient ->
                            IngredientLibraryRow(ingredient) { onIngredientClick(ingredient.ingredientId) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientLibraryRow(ingredient: GlobalIngredient, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (ingredient.imageUrl != null) {
            AsyncImage(
                model = ingredient.imageUrl,
                contentDescription = ingredient.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Restaurant, null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(ingredient.defaultUnit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (ingredient.isDispensable) {
                    Text("Dispensable", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (ingredient.isDispensable) {
            Icon(Icons.Outlined.SmartToy, "Dispensable", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IngredientLibraryPreview() {
    SousChefTheme {
        IngredientLibraryLayout(
            uiState = IngredientLibraryUiState(
                isLoading = false,
                filteredIngredients = listOf(
                    GlobalIngredient(ingredientId = "1", name = "Red Chili Powder", defaultUnit = "tsp", isDispensable = true),
                    GlobalIngredient(ingredientId = "2", name = "Salt", defaultUnit = "grams", isDispensable = true),
                    GlobalIngredient(ingredientId = "3", name = "Onion", defaultUnit = "pieces")
                )
            ),
            onBack = {},
            onSearchQueryChange = {},
            onAddIngredient = {},
            onIngredientClick = {}
        )
    }
}
