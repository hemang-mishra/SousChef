package com.souschef

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.souschef.core.util.Result
import com.souschef.domain.model.AuthState
import com.souschef.feature.auth.presentation.AuthViewModel
import com.souschef.feature.auth.presentation.EmailVerificationScreen
import com.souschef.feature.auth.presentation.GoogleSignInHelper
import com.souschef.feature.auth.presentation.LoginScreen
import com.souschef.feature.auth.presentation.SignUpScreen
import com.souschef.navigation.Route
import com.souschef.ui.theme.GoldMuted
import com.souschef.ui.theme.SousChefTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SousChefTheme {
                SousChefApp()
            }
        }
    }
}

/**
 * Main app composable that handles auth state and navigation.
 * Routes users based on their authentication status.
 */
@Composable
fun SousChefApp() {
    val viewModel: AuthViewModel = koinViewModel()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Google Sign-In helper
    val googleSignInHelper = GoogleSignInHelper(context)

    // TODO: Replace with your Web Client ID from Firebase Console
    // Go to Firebase Console > Authentication > Sign-in method > Google > Web SDK configuration
    val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    /**
     * Handle Google Sign-In flow.
     * Launches credential manager and signs in with Firebase.
     */
    fun handleGoogleSignIn() {
        scope.launch {
            when (val result = googleSignInHelper.signIn(webClientId)) {
                is Result.Success -> {
                    viewModel.signInWithGoogle(result.data)
                }
                is Result.Error -> {
                    Toast.makeText(
                        context,
                        result.message ?: "Google Sign-In failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Result.Loading -> {
                    // Loading state handled by UI
                }
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (authState) {
                is AuthState.Loading -> {
                    // Show loading indicator while checking auth state
                    LoadingScreen()
                }

                is AuthState.Unauthenticated -> {
                    // Show auth flow using internal navigation state
                    AuthNavigation(
                        viewModel = viewModel,
                        onGoogleSignIn = { handleGoogleSignIn() }
                    )
                }

                is AuthState.Unverified -> {
                    val user = (authState as AuthState.Unverified).user
                    EmailVerificationScreen(
                        viewModel = viewModel,
                        user = user,
                        onSignOut = { viewModel.signOut() }
                    )
                }

                is AuthState.Authenticated -> {
                    val user = (authState as AuthState.Authenticated).user
                    DashboardScreen(
                        user = user,
                        onSignOut = { viewModel.signOut() }
                    )
                }
            }
        }
    }
}

/**
 * Internal navigation for authentication flow.
 * Handles navigation between Login, SignUp, and ForgotPassword screens.
 */
@Composable
private fun AuthNavigation(
    viewModel: AuthViewModel,
    onGoogleSignIn: () -> Unit
) {
    // Simple navigation state for auth screens
    val navigationState = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<Route>(Route.Login)
    }

    // Clear form when navigating
    LaunchedEffect(navigationState.value) {
        viewModel.clearForm()
    }

    when (navigationState.value) {
        Route.Login -> {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navigationState.value = Route.SignUp },
                onForgotPassword = {
                    // For now, show a toast. Can be expanded to a full screen later.
                    viewModel.sendPasswordResetEmail()
                },
                onGoogleSignIn = onGoogleSignIn
            )
        }

        Route.SignUp -> {
            SignUpScreen(
                viewModel = viewModel,
                onNavigateBack = { navigationState.value = Route.Login },
                onGoogleSignIn = onGoogleSignIn
            )
        }

        else -> {
            // Default to login for any other routes in auth flow
            LoginScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navigationState.value = Route.SignUp },
                onForgotPassword = { viewModel.sendPasswordResetEmail() },
                onGoogleSignIn = onGoogleSignIn
            )
        }
    }
}

/**
 * Loading screen shown while checking authentication state.
 */
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = GoldMuted,
            strokeWidth = 4.dp
        )
    }
}