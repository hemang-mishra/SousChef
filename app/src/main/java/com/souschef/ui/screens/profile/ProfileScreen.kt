package com.souschef.ui.screens.profile

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.auth.UserProfile
import com.souschef.ui.components.PremiumButton
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.VerifiedChefBadge
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.GradientGold
import com.souschef.ui.theme.SousChefTheme

/**
 * Minimal profile screen with user info and sign-out.
 * Full profile features (edit, settings) planned for Phase 9.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.textPrimary()
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientGold)),
                contentAlignment = Alignment.Center
            ) {
                val initials = userProfile?.displayName
                    ?.split(" ")
                    ?.take(2)
                    ?.mapNotNull { it.firstOrNull()?.uppercase() }
                    ?.joinToString("")
                    ?: "?"

                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.heroBackground()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Name
            Text(
                text = userProfile?.displayName ?: "User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary()
            )

            // Email
            Text(
                text = userProfile?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary()
            )

            if (userProfile?.isVerifiedChef == true) {
                Spacer(Modifier.height(8.dp))
                VerifiedChefBadge()
            }

            Spacer(Modifier.height(12.dp))

            // Role badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppColors.gold().copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = (userProfile?.role ?: "user").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.gold()
                )
            }

            Spacer(Modifier.weight(1f))

            // Sign Out button
            PremiumOutlinedButton(
                text = "Sign Out",
                onClick = onSignOut,
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Previews ─────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenPreview() {
    SousChefTheme {
        ProfileScreen(
            userProfile = UserProfile(
                displayName = "Hemang Mishra",
                email = "hemang@souschef.com",
                role = "user"
            ),
            onSignOut = {}
        )
    }
}
