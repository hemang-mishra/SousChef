package com.souschef.ui.screens.auth.login

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.api.GoogleAuthProvider
import com.souschef.model.auth.UserProfile
import com.souschef.ui.components.GhostButton
import com.souschef.ui.components.PrimaryButton
import com.souschef.ui.components.SecondaryButton
import com.souschef.ui.components.SousChefTextField
import com.souschef.ui.components.StandalonePasswordField
import com.souschef.ui.theme.SousChefTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Login screen — stateful composable that wires ViewModel.
 * Follows the Two-Composable pattern from Coding Guidelines.
 */
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: (UserProfile) -> Unit,
    viewModel: LoginViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Wire success callback
    LaunchedEffect(Unit) {
        viewModel.setOnLoginSuccess(onLoginSuccess)
    }

    // Show general errors in snackbar
    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LoginScreenLayout(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = viewModel::onSignIn,
        onGoogleSignIn = {
            scope.launch {
                val provider = GoogleAuthProvider(context)
                val token = provider.getGoogleIdToken()
                if (token != null) {
                    viewModel.onGoogleSignIn(token)
                } else {
                    viewModel.onGoogleSignInFailed()
                }
            }
        },
        onNavigateToSignUp = onNavigateToSignUp
    )
}

/**
 * Login screen layout — purely presentational, no ViewModel.
 */
@Composable
fun LoginScreenLayout(
    uiState: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo / branding
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "SousChef",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Your personal AI cooking assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Email field
            SousChefTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Password field
            StandalonePasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Sign In button
            PrimaryButton(
                text = "Sign In",
                onClick = onSignIn,
                isLoading = uiState.isLoading,
                enabled = !uiState.isGoogleLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Google Sign-In button
            SecondaryButton(
                text = "Continue with Google",
                onClick = onGoogleSignIn,
                isLoading = uiState.isGoogleLoading,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Navigate to Sign Up
            GhostButton(
                text = "Don't have an account? Create one",
                onClick = onNavigateToSignUp
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ── Previews ─────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenPreview() {
    SousChefTheme {
        LoginScreenLayout(
            uiState = LoginUiState(email = "chef@souschef.com"),
            snackbarHostState = remember { SnackbarHostState() },
            onEmailChange = {}, onPasswordChange = {},
            onSignIn = {}, onGoogleSignIn = {}, onNavigateToSignUp = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenLoadingPreview() {
    SousChefTheme {
        LoginScreenLayout(
            uiState = LoginUiState(
                email = "chef@souschef.com",
                password = "secret",
                isLoading = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEmailChange = {}, onPasswordChange = {},
            onSignIn = {}, onGoogleSignIn = {}, onNavigateToSignUp = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenErrorPreview() {
    SousChefTheme {
        LoginScreenLayout(
            uiState = LoginUiState(
                email = "bad",
                emailError = "Enter a valid email address",
                passwordError = "Password is required"
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEmailChange = {}, onPasswordChange = {},
            onSignIn = {}, onGoogleSignIn = {}, onNavigateToSignUp = {}
        )
    }
}

