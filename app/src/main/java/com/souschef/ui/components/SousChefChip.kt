package com.souschef.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.GradientGold
import com.souschef.ui.theme.SousChefTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement

// ─────────────────────────────────────────────────────────────
// SousChefFilterChip
// ─────────────────────────────────────────────────────────────

/**
 * Filter chip for category selection (e.g. Vegetarian, Spicy, Quick).
 * Color adapts to selected state using the gold primary color.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SousChefFilterChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

// ─────────────────────────────────────────────────────────────
// StatusTag
// ─────────────────────────────────────────────────────────────

/**
 * Pill tag with a colored dot indicator.
 * Use for: "Available", "Sold Out", "Limited", "Vegetarian".
 */
@Composable
fun StatusTag(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

// ─────────────────────────────────────────────────────────────
// DietaryTag
// ─────────────────────────────────────────────────────────────

/**
 * Simple colored tag for dietary indicators (Vegan, Spicy, Gluten-Free, etc.).
 */
@Composable
fun DietaryTag(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

// ─────────────────────────────────────────────────────────────
// VerifiedChefBadge
// ─────────────────────────────────────────────────────────────

/**
 * Premium gold badge for verified chefs.
 * Displayed inline next to the chef's name.
 */
@Composable
fun VerifiedChefBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Brush.linearGradient(GradientGold))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Verified,
            contentDescription = "Verified Chef",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "Verified Chef",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChipsPreview() {
    SousChefTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            // Filter chips
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var selectedVeg by remember { mutableStateOf(true) }
                var selectedSpicy by remember { mutableStateOf(false) }
                var selectedQuick by remember { mutableStateOf(false) }
                SousChefFilterChip("Vegetarian", selectedVeg, { selectedVeg = it })
                SousChefFilterChip("🌶 Spicy", selectedSpicy, { selectedSpicy = it })
                SousChefFilterChip("⚡ Quick", selectedQuick, { selectedQuick = it })
            }
            Spacer(modifier = Modifier.padding(8.dp))
            // Status tags
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusTag("Available", AppColors.success())
                StatusTag("Sold Out", AppColors.error())
                StatusTag("Limited", AppColors.warning())
            }
            Spacer(modifier = Modifier.padding(8.dp))
            // Dietary tags
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DietaryTag("🌱 Vegan", AppColors.accentGreen())
                DietaryTag("🌶 Spicy", AppColors.accentTerracotta())
                DietaryTag("🍷 Wine Pairing", AppColors.accentBurgundy())
            }
            Spacer(modifier = Modifier.padding(8.dp))
            VerifiedChefBadge()
        }
    }
}

