package com.souschef.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.CustomShapes
import com.souschef.ui.theme.GradientImageOverlay
import com.souschef.ui.theme.SousChefTheme

// ─────────────────────────────────────────────────────────────
// StandardCard
// ─────────────────────────────────────────────────────────────

/**
 * Flat card with no elevation. Default for most content.
 * Background adapts to light/dark theme automatically via MaterialTheme.colorScheme.surface.
 */
@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ElevatedCard
// ─────────────────────────────────────────────────────────────

/**
 * Card with shadow elevation for emphasis on important content.
 */
@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────
// GlassCard
// ─────────────────────────────────────────────────────────────

/**
 * Glassmorphism card for premium/featured content.
 * Limit to 1–2 per screen. Best placed over a gradient or image background.
 */
@Composable
fun GlassCard(
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

// ─────────────────────────────────────────────────────────────
// GoldAccentCard
// ─────────────────────────────────────────────────────────────

/**
 * Card with a gold gradient border and tinted background.
 * Use for premium feature call-outs or confirmed reservations.
 */
@Composable
fun GoldAccentCard(
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
                        AppColors.gold().copy(alpha = 0.08f),
                        AppColors.goldBackground(),
                        AppColors.gold().copy(alpha = 0.08f)
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
            .padding(20.dp)
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────
// ImageCard
// ─────────────────────────────────────────────────────────────

/**
 * Food photo card with a gradient overlay at the bottom for text readability.
 * Uses a 4:3 aspect ratio by default.
 *
 * @param imageUrl  URL of the food image (can be null — shows placeholder background).
 * @param title     Text overlaid at the bottom of the image.
 * @param aspectRatio Width / height ratio. Default 4:3.
 */
@Composable
fun ImageCard(
    imageUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 4f / 3f,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio),
        shape = CustomShapes.ImageCard,
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(colors = GradientImageOverlay as List<Color>))
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StandardCardPreview() {
    SousChefTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            StandardCard {
                Text("Standard flat card — default for most content")
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GlassCardPreview() {
    SousChefTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF1A1A1A), Color(0xFF2D2D2D))))
                .padding(16.dp)
        ) {
            GlassCard {
                Text("Glass card — for premium sections", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ImageCardPreview() {
    SousChefTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ImageCard(imageUrl = null, title = "Truffle Risotto")
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GoldAccentCardPreview() {
    SousChefTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GoldAccentCard {
                Text("Gold accent card — premium feature highlight")
            }
        }
    }
}

