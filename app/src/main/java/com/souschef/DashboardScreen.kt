package com.souschef

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.souschef.ui.components.SecondaryButton
import com.souschef.ui.theme.GoldVibrant

/**
 * Dashboard screen — main landing page after authentication (Phase 1+).
 * This is a placeholder that will be replaced with the full Home screen in Phase 7.
 */
@Composable
fun DashboardScreen(
    displayName: String? = null,
    email: String? = null,
    onSignOut: () -> Unit,
    onNavigateToDesignTest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GoldVibrant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to SousChef",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayName?.let { "Hello, $it!" }
                ?: email?.let { "Signed in as $it" }
                ?: "You're signed in!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        SecondaryButton(text = "View Design System", onClick = onNavigateToDesignTest)

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryButton(text = "Sign Out", onClick = onSignOut)
    }
}