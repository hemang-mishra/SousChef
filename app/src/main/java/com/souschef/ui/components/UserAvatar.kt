package com.souschef.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.souschef.ui.theme.GoldVibrant
import com.souschef.ui.theme.SousChefTheme

/**
 * Circular user avatar.
 * Shows profile image if [imageUrl] is provided; otherwise falls back to gold-background initials.
 *
 * @param displayName Full name used to derive initials when no image is available.
 * @param imageUrl    Remote image URL; null triggers initials fallback.
 * @param size        Diameter of the circle.
 */
@Composable
fun UserAvatar(
    displayName: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (imageUrl == null) GoldVibrant else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = displayName.initials(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.35f).sp
            )
        }
    }
}

private fun String.initials(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0][0]}${parts[1][0]}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else            -> "?"
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UserAvatarPreview() {
    SousChefTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(displayName = "Marco Rossi", size = 56.dp)
            UserAvatar(displayName = "Julia Child", size = 44.dp)
            UserAvatar(displayName = "Gordon Ramsay", size = 40.dp)
            UserAvatar(displayName = "J", size = 32.dp)
        }
    }
}

