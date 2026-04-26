package com.souschef.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.RecipeTag
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.GradientGold
import com.souschef.ui.theme.SousChefTheme

/**
 * Data class bundling a recipe with its computed metadata for display.
 */
data class RecipeWithMeta(
    val recipe: Recipe,
    val stepCount: Int = 0,
    val hasSteps: Boolean = false
)

/**
 * Premium horizontal recipe card for feeds and lists.
 *
 * Shows recipe image (or initials fallback), title, chef name,
 * tag chips, step count, and action icons.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCard(
    recipeWithMeta: RecipeWithMeta,
    onClick: () -> Unit,
    onGenerateSteps: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val recipe = recipeWithMeta.recipe
    val tags = recipe.tags.mapNotNull { RecipeTag.fromName(it) }.take(4)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.cardBackground()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // ── Top: Image + Info + Actions ──────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.Top
            ) {
                // Left: Image / Initials
                RecipeImageBlock(
                    imageUrl = recipe.coverImageUrl,
                    title = recipe.title
                )

                Spacer(Modifier.width(14.dp))

                // Center: Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title.ifBlank { "Untitled Recipe" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(2.dp))

                    // Chef name + verified badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = recipe.creatorName.ifBlank { "You" },
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary()
                        )
                        if (recipe.isVerifiedChef) {
                            Spacer(Modifier.width(6.dp))
                            VerifiedChefBadge()
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Metadata: servings + steps
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetaChip(
                            icon = Icons.Default.Group,
                            text = "${recipe.baseServingSize} servings"
                        )
                        if (recipeWithMeta.hasSteps) {
                            MetaChip(
                                icon = Icons.Default.Restaurant,
                                text = "${recipeWithMeta.stepCount} steps"
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(AppColors.warning().copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "No steps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.warning()
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(4.dp))

                // Right: Action icons
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!recipeWithMeta.hasSteps && onGenerateSteps != null) {
                        IconButton(
                            onClick = onGenerateSteps,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Generate Steps",
                                tint = AppColors.gold(),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Spacer(Modifier.size(36.dp))
                    }

                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View recipe",
                            tint = AppColors.textTertiary(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Bottom: Tags row (below everything) ──────
            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))

                HorizontalDivider(
                    color = AppColors.textTertiary().copy(alpha = 0.08f),
                    thickness = 1.dp
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val visibleTags = tags.take(2)
                    val overflowCount = tags.size - visibleTags.size

                    visibleTags.forEach { tag ->
                        DietaryTag(text = tag.displayLabel, color = tag.color)
                    }

                    if (overflowCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(AppColors.textTertiary().copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "+$overflowCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.textTertiary()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeImageBlock(
    imageUrl: String?,
    title: String
) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        // Initials fallback with gold gradient
        val initials = title
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifEmpty { "?" }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(GradientGold)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.heroBackground()
            )
        }
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = AppColors.textTertiary()
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.textTertiary()
        )
    }
}

// ── Previews ─────────────────────────────────────────────

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecipeCardPreview() {
    SousChefTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RecipeCard(
                recipeWithMeta = RecipeWithMeta(
                    recipe = Recipe(
                        title = "Butter Chicken Masala",
                        creatorName = "Chef Hemang",
                        baseServingSize = 4,
                        isVerifiedChef = true,
                        tags = listOf("INDIAN", "SPICY")
                    ),
                    stepCount = 8,
                    hasSteps = true
                ),
                onClick = {},
                onGenerateSteps = {}
            )
            RecipeCard(
                recipeWithMeta = RecipeWithMeta(
                    recipe = Recipe(
                        title = "Quick Pasta Aglio e Olio",
                        creatorName = "You",
                        baseServingSize = 2,
                        tags = listOf("ITALIAN", "QUICK")
                    ),
                    stepCount = 0,
                    hasSteps = false
                ),
                onClick = {},
                onGenerateSteps = {}
            )
        }
    }
}
