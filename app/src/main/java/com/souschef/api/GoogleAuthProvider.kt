package com.souschef.api

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.souschef.R

private const val TAG = "GoogleAuthProvider"

/**
 * Uses the Android Credential Manager API to get a Google ID token
 * for Firebase Authentication.
 *
 * Reads [R.string.WEB_CLIENT_ID] (set via resValue in build.gradle.kts).
 */
class GoogleAuthProvider(private val context: Context) {

    /**
     * Launches the Google One Tap / Credential Manager flow and returns the ID token.
     * Returns null if the user cancels or the flow fails.
     */
    suspend fun getGoogleIdToken(): String? {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.WEB_CLIENT_ID))
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } else {
                null
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google sign-in cancelled by user", e)
            null
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: type=${e.type}, message=${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google sign-in", e)
            null
        }
    }
}

