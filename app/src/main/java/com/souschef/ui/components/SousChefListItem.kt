package com.souschef.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.SousChefTheme

// ─────────────────────────────────────────────────────────────
// IngredientRow
// ─────────────────────────────────────────────────────────────

/**
 * Displays a single ingredient with quantity and unit.
 * Used in recipe overview, step detail, and ingredient lists.
 */
@Composable
fun IngredientRow(
    name: String,
    quantity: String,
    unit: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$quantity $unit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────
// StepRow
// ─────────────────────────────────────────────────────────────

enum class FlameLevel { LOW, MEDIUM, HIGH, NONE }

/**
 * Displays a single cooking step with a numbered badge, instruction text,
 * and optional flame-level indicator.
 */
@Composable
fun StepRow(
    stepNumber: Int,
    instruction: String,
    flameLevel: FlameLevel = FlameLevel.NONE,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step number badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (flameLevel != FlameLevel.NONE) {
                val filledFlames = when (flameLevel) {
                    FlameLevel.LOW    -> 1
                    FlameLevel.MEDIUM -> 2
                    FlameLevel.HIGH   -> 3
                    FlameLevel.NONE   -> 0
                }
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { i ->
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (i < filledFlames) AppColors.accentTerracotta()
                                   else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (flameLevel) {
                            FlameLevel.LOW    -> "Low heat"
                            FlameLevel.MEDIUM -> "Medium heat"
                            FlameLevel.HIGH   -> "High heat"
                            FlameLevel.NONE   -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ListItemsPreview() {
    SousChefTheme {
        Column {
            SectionHeader(title = "Ingredients")
            IngredientRow(name = "Arborio Rice", quantity = "200", unit = "g")
            IngredientRow(name = "Parmesan Cheese", quantity = "50", unit = "g", isHighlighted = true)
            IngredientRow(name = "White Truffle Oil", quantity = "2", unit = "tbsp")
            SectionHeader(title = "Steps")
            StepRow(1, "Bring a large pot of salted water to a boil.", FlameLevel.HIGH, isActive = true)
            StepRow(2, "Add rice and stir continuously for 2 minutes.", FlameLevel.MEDIUM)
            StepRow(3, "Remove from heat and fold in cold butter.", FlameLevel.NONE)
        }
    }
}
