package com.souschef.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.ui.components.GhostButton
import com.souschef.ui.components.PrimaryButton
import com.souschef.ui.components.SecondaryButton
import com.souschef.ui.components.SousChefExtendedFAB
import com.souschef.ui.theme.SousChefTheme

/**
 * Minimal Home screen placeholder.
 * Shows the user's name, a "Create Recipe" CTA, and navigation helpers.
 * Full home screen with recipe feed built in Phase 7.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    displayName: String?,
    onCreateRecipe: () -> Unit,
    onSignOut: () -> Unit,
    onDesignTest: () -> Unit,
    onIngredientLibrary: () -> Unit = {},
    onTestOverview: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SousChef",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            SousChefExtendedFAB(
                text = "Create Recipe",
                icon = Icons.Outlined.Add,
                onClick = onCreateRecipe
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = displayName?.let { "Welcome, $it!" } ?: "Welcome!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your recipe feed will appear here.\nStart by creating your first recipe!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            PrimaryButton(
                text = "Create Your First Recipe",
                onClick = onCreateRecipe,
                leadingIcon = Icons.Outlined.Add
            )

            Spacer(Modifier.height(16.dp))

            SecondaryButton(
                text = "Ingredient Library",
                onClick = onIngredientLibrary,
                leadingIcon = {
                    Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )

            Spacer(Modifier.height(16.dp))

            SecondaryButton(
                text = "Design System",
                onClick = onDesignTest,
                leadingIcon = {
                    Icon(Icons.Outlined.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )

            Spacer(Modifier.height(16.dp))

            GhostButton(text = "Sign Out", onClick = onSignOut)

            Spacer(Modifier.height(16.dp))

            GhostButton(text = "Test Overview (DEBUG)", onClick = onTestOverview)
        }
    }
}

// ── Previews ─────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    SousChefTheme {
        HomeScreen(
            displayName = "Marco Rossi",
            onCreateRecipe = {},
            onSignOut = {},
            onDesignTest = {},
                onIngredientLibrary = {},
                onTestOverview = {}
        )
    }
}

