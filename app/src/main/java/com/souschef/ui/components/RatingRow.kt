package com.souschef.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.souschef.ui.theme.GoldVibrant
import com.souschef.ui.theme.SousChefTheme

/**
 * Displays a 5-star rating row with a numeric label.
 *
 * @param rating  Value from 0.0 to 5.0. Half-star precision supported.
 * @param count   Optional review count displayed after the star row.
 * @param starSize Diameter of each star icon.
 */
@Composable
fun RatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier,
    count: Int? = null,
    starSize: Dp = 16.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val fill = rating - index
            Icon(
                imageVector = when {
                    fill >= 1f   -> Icons.Filled.Star
                    fill >= 0.5f -> Icons.Outlined.StarHalf
                    else         -> Icons.Outlined.StarBorder
                },
                contentDescription = null,
                tint = GoldVibrant,
                modifier = Modifier.size(starSize)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (count != null) "%.1f (%,d)".format(rating, count)
                   else "%.1f".format(rating),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RatingDisplayPreview() {
    SousChefTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RatingDisplay(rating = 5.0f, count = 1284)
            RatingDisplay(rating = 4.5f, count = 372)
            RatingDisplay(rating = 3.5f, count = 89)
            RatingDisplay(rating = 2.0f)
        }
    }
}

