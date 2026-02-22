package com.souschef.feature.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.souschef.R
import com.souschef.ui.theme.BorderLight
import com.souschef.ui.theme.TextTertiaryLight

/**
 * "Or continue with" divider for social sign-in section.
 */
@Composable
fun OrDivider(
    modifier: Modifier = Modifier,
    text: String = "or continue with"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = BorderLight
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiaryLight
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = BorderLight
        )
    }
}

/**
 * Google "G" icon for sign-in button.
 * Uses a placeholder - replace with actual Google icon resource.
 */
@Composable
fun GoogleIcon(
    modifier: Modifier = Modifier
) {
    // Note: Add google_logo.xml to res/drawable
    // For now using a text placeholder
    Text(
        text = "G",
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFF4285F4), // Google Blue
        modifier = modifier
    )
}

/**
 * Row of social sign-in options.
 * Currently only Google, but extensible for Apple, Facebook, etc.
 */
@Composable
fun SocialSignInRow(
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    SecondaryButton(
        text = "Continue with Google",
        onClick = onGoogleClick,
        modifier = modifier,
        isLoading = isLoading,
        leadingIcon = { GoogleIcon(modifier = Modifier.size(20.dp)) }
    )
}

