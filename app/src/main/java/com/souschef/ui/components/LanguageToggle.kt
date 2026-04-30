package com.souschef.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.souschef.model.recipe.SupportedLanguages
import com.souschef.ui.theme.AppColors

/**
 * Compact one-tap toggle that switches the active language between
 * English and Hindi. The currently active code is highlighted; tapping
 * either label invokes [onLanguageChange].
 */
@Composable
fun LanguageToggle(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, AppColors.gold().copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LanguagePill(
            label = "EN",
            isActive = currentLanguage == SupportedLanguages.ENGLISH,
            onClick = { onLanguageChange(SupportedLanguages.ENGLISH) }
        )
        LanguagePill(
            label = "हिं",
            isActive = currentLanguage == SupportedLanguages.HINDI,
            onClick = { onLanguageChange(SupportedLanguages.HINDI) }
        )
    }
}

@Composable
private fun LanguagePill(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (isActive) AppColors.gold() else Color.Transparent,
        label = "langPillBg"
    )
    val fg by animateColorAsState(
        targetValue = if (isActive) AppColors.onGold() else AppColors.textSecondary(),
        label = "langPillFg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = fg
        )
    }
}
