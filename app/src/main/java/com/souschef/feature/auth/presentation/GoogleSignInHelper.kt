package com.souschef.feature.auth.presentation

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.souschef.core.util.Result
import java.security.MessageDigest
import java.util.UUID

/**
 * Helper class for Google Sign-In using Credential Manager API.
 *
 * Setup requirements:
 * 1. Add your app's SHA-1 fingerprint to Firebase Console
 * 2. Enable Google Sign-In in Firebase Console > Authentication > Sign-in method
 * 3. Download updated google-services.json
 * 4. Add the Web Client ID from Firebase Console
 */
class GoogleSignInHelper(
    private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    /**
     * Initiates Google Sign-In flow and returns the ID token.
     *
     * @param webClientId The Web Client ID from Firebase Console (OAuth 2.0 Client IDs)
     * @return Result containing the ID token on success, or error on failure
     */
    suspend fun signIn(webClientId: String): Result<String> {
        return try {
            // Generate a nonce for security
            val nonce = generateNonce()

            // Configure Google ID option
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Allow any Google account
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true) // Auto-select if only one account
                .setNonce(nonce)
                .build()

            // Build the credential request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Launch the credential picker
            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            // Extract the ID token from the response
            handleSignInResponse(response)

        } catch (e: GetCredentialCancellationException) {
            Result.Error(e, "Sign-in was cancelled")
        } catch (e: NoCredentialException) {
            Result.Error(e, "No Google accounts found on this device")
        } catch (e: GetCredentialException) {
            Result.Error(e, "Failed to get credentials: ${e.message}")
        } catch (e: Exception) {
            Result.Error(e, "An unexpected error occurred: ${e.message}")
        }
    }

    /**
     * Processes the credential response and extracts the Google ID token.
     */
    private fun handleSignInResponse(response: GetCredentialResponse): Result<String> {
        val credential = response.credential

        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        Result.Success(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Result.Error(e, "Failed to parse Google ID token")
                    }
                } else {
                    Result.Error(
                        IllegalStateException("Unexpected credential type"),
                        "Unexpected credential type: ${credential.type}"
                    )
                }
            }
            else -> {
                Result.Error(
                    IllegalStateException("Unexpected credential type"),
                    "Unexpected credential type"
                )
            }
        }
    }

    /**
     * Generates a secure nonce for the Google Sign-In request.
     */
    private fun generateNonce(): String {
        val ranNonce = UUID.randomUUID().toString()
        val bytes = ranNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

