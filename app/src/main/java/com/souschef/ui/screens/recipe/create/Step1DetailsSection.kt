package com.souschef.ui.screens.recipe.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.souschef.model.recipe.RecipeTag
import com.souschef.ui.components.PremiumDivider
import com.souschef.ui.components.PremiumSectionHeader
import com.souschef.ui.components.SousChefFilterChip
import com.souschef.ui.components.SousChefTextField
import com.souschef.ui.theme.AppColors

// ─────────────────────────────────────────────────────────────
// Step 1: Recipe Details
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step1Details(
    uiState: CreateRecipeUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBaseServingSizeChange: (Int) -> Unit,
    onUseMinServingChange: (Boolean) -> Unit,
    onMinServingSizeChange: (Int) -> Unit,
    onUseMaxServingChange: (Boolean) -> Unit,
    onMaxServingSizeChange: (Int) -> Unit,
    onToggleTag: (RecipeTag) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        SousChefTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = "Recipe Title *",
            isError = uiState.titleError != null,
            errorMessage = uiState.titleError
        )

        SousChefTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = "Description",
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        PremiumDivider()

        // Serving Size
        PremiumSectionHeader(title = "Serving Size")
        ServingStepper(
            label = "Base servings",
            value = uiState.baseServingSize,
            onValueChange = onBaseServingSizeChange,
            min = 1,
            max = 50
        )

        // Min/Max restrictions
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Minimum servings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textPrimary()
                )
                Text(
                    "Set a lower limit for this recipe",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
            }
            Switch(
                checked = uiState.useMinServing,
                onCheckedChange = onUseMinServingChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.gold(),
                    checkedTrackColor = AppColors.gold().copy(alpha = 0.3f),
                    uncheckedThumbColor = AppColors.textTertiary(),
                    uncheckedTrackColor = AppColors.border()
                )
            )
        }
        if (uiState.useMinServing) {
            ServingStepper(
                label = "Min",
                value = uiState.minServingSize ?: 1,
                onValueChange = onMinServingSizeChange,
                min = 1,
                max = uiState.baseServingSize
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Maximum servings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textPrimary()
                )
                Text(
                    "Set an upper limit for this recipe",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
            }
            Switch(
                checked = uiState.useMaxServing,
                onCheckedChange = onUseMaxServingChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.gold(),
                    checkedTrackColor = AppColors.gold().copy(alpha = 0.3f),
                    uncheckedThumbColor = AppColors.textTertiary(),
                    uncheckedTrackColor = AppColors.border()
                )
            )
        }
        if (uiState.useMaxServing) {
            ServingStepper(
                label = "Max",
                value = uiState.maxServingSize ?: (uiState.baseServingSize * 2),
                onValueChange = onMaxServingSizeChange,
                min = uiState.baseServingSize,
                max = 100
            )
        }

        PremiumDivider()

        // Tags
        PremiumSectionHeader(title = "Tags")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecipeTag.entries.forEach { tag ->
                SousChefFilterChip(
                    label = tag.displayLabel,
                    selected = tag in uiState.selectedTags,
                    onSelectedChange = { onToggleTag(tag) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Serving Stepper
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServingStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                enabled = value > min
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(
                            1.dp,
                            if (value > min) AppColors.gold() else AppColors.border(),
                            CircleShape
                        )
                        .background(
                            if (value > min) AppColors.gold().copy(alpha = 0.08f) else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "\u2212",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (value > min) AppColors.gold() else AppColors.textTertiary()
                    )
                }
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary(),
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                enabled = value < max
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(
                            1.dp,
                            if (value < max) AppColors.gold() else AppColors.border(),
                            CircleShape
                        )
                        .background(
                            if (value < max) AppColors.gold().copy(alpha = 0.08f) else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (value < max) AppColors.gold() else AppColors.textTertiary()
                    )
                }
            }
        }
    }
}

