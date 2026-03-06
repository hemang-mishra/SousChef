package com.souschef.ui.screens.auth.signup

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.auth.UserProfile
import com.souschef.ui.components.GhostButton
import com.souschef.ui.components.PrimaryButton
import com.souschef.ui.components.SousChefTextField
import com.souschef.ui.components.StandalonePasswordField
import com.souschef.ui.theme.SousChefTheme
import org.koin.compose.koinInject

/**
 * Sign-up screen — stateful composable that wires ViewModel.
 */
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (UserProfile) -> Unit,
    viewModel: SignUpViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.setOnSignUpSuccess(onSignUpSuccess)
    }

    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    SignUpScreenLayout(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSignUp = viewModel::onSignUp,
        onNavigateToLogin = onNavigateToLogin
    )
}

/**
 * Sign-up screen layout — purely presentational, no ViewModel.
 */
@Composable
fun SignUpScreenLayout(
    uiState: SignUpUiState,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSignUp: () -> Unit,
    onNavigateToLogin: () -> Unit
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
            Spacer(Modifier.height(40.dp))

            // Branding
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Join the SousChef community",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Display Name
            SousChefTextField(
                value = uiState.displayName,
                onValueChange = onNameChange,
                label = "Full Name",
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Email
            SousChefTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Password
            StandalonePasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Confirm Password
            StandalonePasswordField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm Password",
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Create Account button
            PrimaryButton(
                text = "Create Account",
                onClick = onSignUp,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Back to Login
            GhostButton(
                text = "Already have an account? Sign In",
                onClick = onNavigateToLogin
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ── Previews ─────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignUpScreenPreview() {
    SousChefTheme {
        SignUpScreenLayout(
            uiState = SignUpUiState(displayName = "Marco Rossi", email = "marco@chef.com"),
            snackbarHostState = remember { SnackbarHostState() },
            onNameChange = {}, onEmailChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onSignUp = {}, onNavigateToLogin = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenErrorPreview() {
    SousChefTheme {
        SignUpScreenLayout(
            uiState = SignUpUiState(
                displayName = "M",
                nameError = "Name must be at least 2 characters",
                email = "bad-email",
                emailError = "Enter a valid email address",
                password = "123",
                passwordError = "Password must be at least 6 characters",
                confirmPassword = "456",
                confirmPasswordError = "Passwords do not match"
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNameChange = {}, onEmailChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onSignUp = {}, onNavigateToLogin = {}
        )
    }
}

