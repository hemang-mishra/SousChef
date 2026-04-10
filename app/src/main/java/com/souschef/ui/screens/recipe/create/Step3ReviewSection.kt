package com.souschef.ui.screens.recipe.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.souschef.ui.components.DietaryTag
import com.souschef.ui.components.IngredientRow
import com.souschef.ui.components.PremiumButton
import com.souschef.ui.components.PremiumDivider
import com.souschef.ui.components.PremiumDottedDivider
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.PremiumSectionHeader
import com.souschef.ui.components.StandardCard
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.CustomShapes

// ─────────────────────────────────────────────────────────────
// Step 3: Review & Save
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step3Review(
    uiState: CreateRecipeUiState,
    onSave: (Boolean) -> Unit
) {
    val globalMap = remember(uiState.globalIngredients) {
        uiState.globalIngredients.associateBy { it.ingredientId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Summary card — glass effect
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CustomShapes.GlassCard,
            colors = CardDefaults.cardColors(containerColor = AppColors.glassBackground()),
            border = BorderStroke(0.5.dp, AppColors.glassBorder())
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = uiState.title.ifBlank { "Untitled Recipe" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.textPrimary(),
                    fontWeight = FontWeight.Bold
                )
                if (uiState.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textSecondary(),
                        maxLines = 3
                    )
                }
                Spacer(Modifier.height(16.dp))
                PremiumDottedDivider()
                Spacer(Modifier.height(16.dp))

                ReviewRow("Base Servings", uiState.baseServingSize.toString())
                uiState.minServingSize?.let { ReviewRow("Min Servings", it.toString()) }
                uiState.maxServingSize?.let { ReviewRow("Max Servings", it.toString()) }
                ReviewRow("Ingredients", uiState.ingredients.size.toString())

                if (uiState.selectedTags.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.selectedTags.forEach { tag ->
                            DietaryTag(text = tag.displayLabel, color = tag.color)
                        }
                    }
                }
            }
        }

        // Ingredient list preview
        if (uiState.ingredients.isNotEmpty()) {
            PremiumSectionHeader(title = "Ingredients")
            StandardCard {
                Column {
                    uiState.ingredients.forEachIndexed { index, ingredient ->
                        val name = globalMap[ingredient.globalIngredientId]?.name ?: "Unknown"
                        IngredientRow(
                            name = name,
                            quantity = ingredient.quantity.toString(),
                            unit = ingredient.unit
                        )
                        if (index < uiState.ingredients.lastIndex) {
                            PremiumDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Save buttons
        PremiumButton(
            text = "✨ Save & Generate Steps (AI)",
            onClick = { onSave(true) },
            isLoading = uiState.isLoading
        )

        Spacer(Modifier.height(12.dp))

        PremiumOutlinedButton(
            text = "Save without Steps",
            onClick = { onSave(false) },
            isLoading = uiState.isLoading
        )

        Spacer(Modifier.height(32.dp))
    }
}


@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textTertiary())
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary())
    }
}

