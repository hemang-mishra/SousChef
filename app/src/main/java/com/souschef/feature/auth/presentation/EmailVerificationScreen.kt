package com.souschef.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.souschef.domain.model.User
import com.souschef.feature.auth.presentation.components.PrimaryButton
import com.souschef.feature.auth.presentation.components.SecondaryButton
import com.souschef.feature.auth.presentation.components.TertiaryButton
import com.souschef.ui.theme.GoldMuted
import kotlinx.coroutines.flow.collectLatest

/**
 * Email verification screen shown after sign up.
 * User must verify email before accessing the app.
 */
@Composable
fun EmailVerificationScreen(
    viewModel: AuthViewModel,
    user: User,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AuthEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email icon
            Icon(
                imageVector = Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = GoldMuted
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text(
                text = "Verify Your Email",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "We've sent a verification link to",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email address
            Text(
                text = user.email ?: "your email",
                style = MaterialTheme.typography.titleMedium,
                color = GoldMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please check your inbox and click the verification link to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Check verification button
            PrimaryButton(
                text = "I've Verified My Email",
                onClick = { viewModel.checkEmailVerification() },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Resend email button
            SecondaryButton(
                text = "Resend Verification Email",
                onClick = { viewModel.resendVerificationEmail() },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign out / use different account
            TertiaryButton(
                text = "Use a different account",
                onClick = {
                    viewModel.signOut()
                    onSignOut()
                },
                enabled = !uiState.isLoading
            )
        }
    }
}

