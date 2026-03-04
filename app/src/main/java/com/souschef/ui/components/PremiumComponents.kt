package com.souschef.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.CustomShapes
import com.souschef.ui.theme.GradientGold

// ============================================
// PREMIUM BUTTONS
// ============================================

/**
 * Primary CTA button with gold background.
 * Use for main actions: "Reserve", "Book", "Purchase", "Sign In"
 */
@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.gold(),
            contentColor = AppColors.onGold(),
            disabledContainerColor = AppColors.gold().copy(alpha = 0.5f),
            disabledContentColor = AppColors.onGold().copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppColors.onGold(),
                strokeWidth = 2.dp
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Secondary button with gold border.
 * Use for alternative actions: "View Menu", "Learn More"
 */
@Composable
fun PremiumOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, if (enabled) AppColors.gold() else AppColors.gold().copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.gold(),
            disabledContentColor = AppColors.gold().copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AppColors.gold(),
                strokeWidth = 2.dp
            )
        } else {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Text button for tertiary actions.
 * Use for: "Cancel", "Skip", "Forgot Password?"
 */
@Composable
fun PremiumTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color ?: if (enabled) AppColors.textSecondary() else AppColors.textSecondary().copy(alpha = 0.5f)
        )
    }
}

/**
 * Small compact button for inline actions.
 */
@Composable
fun PremiumSmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.gold(),
            contentColor = AppColors.onGold()
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * Gradient button for premium CTAs.
 */
@Composable
fun PremiumGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(colors = GradientGold))
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = AppColors.heroBackground(),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.heroBackground(),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================
// PREMIUM CARDS
// ============================================

/**
 * Standard flat card with no elevation.
 * Use for most content cards.
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.cardBackground()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Card with subtle border.
 * Use for secondary content sections.
 */
@Composable
fun PremiumBorderedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.cardBackground()
        ),
        border = BorderStroke(0.5.dp, AppColors.border())
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Elevated card with shadow.
 * Use for important/featured content that needs emphasis.
 */
@Composable
fun PremiumElevatedCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.cardBackground()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Glass effect card for premium content.
 * Use sparingly for featured/premium sections. Limit 1-2 per screen.
 */
@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CustomShapes.GlassCard,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.glassBackground()
        ),
        border = BorderStroke(0.5.dp, AppColors.glassBorder())
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

/**
 * Hero card with dark background for dramatic effect.
 * Use for hero sections, featured content, promotions.
 */
@Composable
fun PremiumHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AppColors.heroBackground(),
                        AppColors.heroBackgroundAlt()
                    )
                )
            )
    ) {
        // Decorative gold circle
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AppColors.gold().copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

/**
 * Card with gold accent/tint.
 * Use for reservation confirmations, premium features.
 */
@Composable
fun PremiumGoldAccentCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AppColors.gold().copy(alpha = 0.1f),
                        AppColors.goldBackground(),
                        AppColors.gold().copy(alpha = 0.1f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.gold().copy(alpha = 0.5f),
                        AppColors.gold().copy(alpha = 0.2f),
                        AppColors.gold().copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        content()
    }
}

// ============================================
// PREMIUM BADGES & TAGS
// ============================================

/**
 * Premium badge with gradient background.
 * Use for: "Michelin Star", "Top Rated", "Featured"
 */
@Composable
fun PremiumBadge(
    text: String,
    modifier: Modifier = Modifier,
    type: BadgeType = BadgeType.GOLD
) {
    val (backgroundColor, textColor) = when (type) {
        BadgeType.GOLD -> Brush.linearGradient(colors = GradientGold) to AppColors.heroBackground()
        BadgeType.DARK -> Brush.linearGradient(
            colors = listOf(AppColors.heroBackground(), AppColors.heroBackgroundAlt())
        ) to AppColors.gold()
        BadgeType.SUCCESS -> Brush.linearGradient(
            colors = listOf(AppColors.success(), AppColors.success().copy(alpha = 0.8f))
        ) to Color.White
        BadgeType.ACCENT -> Brush.linearGradient(
            colors = listOf(AppColors.accentTerracotta(), AppColors.accentBurgundy())
        ) to Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

enum class BadgeType {
    GOLD, DARK, SUCCESS, ACCENT
}

/**
 * Simple accent tag for categorization.
 * Use for: "Vegetarian", "Organic", "French"
 */
@Composable
fun PremiumTag(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

/**
 * Status tag with indicator dot.
 * Use for: "Available", "Sold Out", "Limited"
 */
@Composable
fun PremiumStatusTag(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
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

// ============================================
// PREMIUM SECTION COMPONENTS
// ============================================

/**
 * Section header with title.
 */
@Composable
fun PremiumSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.textPrimary()
        )
        action?.invoke()
    }
}

/**
 * Subsection label for content grouping.
 */
@Composable
fun PremiumSubsectionLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = AppColors.textTertiary(),
        modifier = modifier.padding(bottom = 8.dp)
    )
}

// ============================================
// PREMIUM LIST ITEMS
// ============================================

/**
 * Premium list item for menus, dishes, etc.
 */
@Composable
fun PremiumListItem(
    title: String,
    subtitle: String,
    price: String,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingContent?.invoke()

        if (leadingContent != null) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.textPrimary(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = price,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.gold(),
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ============================================
// PREMIUM DIVIDERS
// ============================================

/**
 * Standard horizontal divider.
 */
@Composable
fun PremiumDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppColors.divider())
    )
}

/**
 * Decorative dotted divider.
 */
@Composable
fun PremiumDottedDivider(
    modifier: Modifier = Modifier,
    dotCount: Int = 30
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(AppColors.gold().copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

