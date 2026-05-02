package com.souschef.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.souschef.model.auth.UserProfile
import com.souschef.preferences.AppPreferences
import com.souschef.repository.auth.AuthRepository
import com.souschef.repository.recipe.RecipeListCache
import com.souschef.util.ConnectivityObserver
import com.souschef.util.NetworkStatus
import com.souschef.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App-level ViewModel — registered as `single` in Koin.
 * Survives navigation and holds the current user state.
 *
 * - Observes Firebase auth state changes.
 * - Fetches the Firestore user profile when a user is detected.
 * - Exposes [isLoggedIn] and [isAdmin] for navigation routing.
 */
class AppViewModel(
    private val authRepository: AuthRepository,
    private val appPreferences: AppPreferences,
    connectivityObserver: ConnectivityObserver,
    private val recipeListCache: RecipeListCache
) : ViewModel() {

    /** True when the device is currently disconnected from the internet. */
    val isOffline: StateFlow<Boolean> = connectivityObserver.networkStatus
        .map { it == NetworkStatus.Unavailable }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser

    /** Preferred language code for recipe translation (e.g. "hi" for Hindi, "es" for Spanish). */
    val preferredLanguageCode: StateFlow<String?> = appPreferences.preferredLanguageCode.getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** True when a user is authenticated AND their profile has been loaded. */
    val isLoggedIn: StateFlow<Boolean> = _currentUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** True when the authenticated user has admin role. */
    val isAdmin: StateFlow<Boolean> = _currentUser
        .map { it?.isAdmin == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Tracks whether initial auth check has completed (prevents flash of login screen). */
    private val _authChecked = MutableStateFlow(false)
    val authChecked: StateFlow<Boolean> = _authChecked

    init {
        observeAuth()
    }

    private fun observeAuth() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.observeAuthState().collect { firebaseUser ->
                if (firebaseUser != null) {
                    fetchUserProfile(firebaseUser)
                } else {
                    _currentUser.value = null
                    _authChecked.value = true
                }
            }
        }
    }

    private fun fetchUserProfile(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.getCurrentUserProfile().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _currentUser.value = resource.data
                        _authChecked.value = true
                    }
                    is Resource.Failure -> {
                        // Firestore fetch failed (e.g. security rules, network)
                        // but Firebase Auth succeeded — create a fallback profile
                        // so the user isn't kicked back to login
                        android.util.Log.w(
                            "AppViewModel",
                            "Firestore profile fetch failed: ${resource.message}. Using Auth fallback."
                        )
                        _currentUser.value = UserProfile(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName ?: "",
                            profileImageUrl = firebaseUser.photoUrl?.toString()
                        )
                        _authChecked.value = true
                    }
                    is Resource.Loading -> { /* wait */ }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            recipeListCache.clear()
            authRepository.signOut().collect { /* result handled by auth state observer */ }
        }
    }

    /** Sets the preferred language code for recipe translation. */
    fun setPreferredLanguage(languageCode: String?) {
        viewModelScope.launch {
            appPreferences.preferredLanguageCode.set(languageCode)
        }
    }

    /**
     * Called after a successful login/signup to immediately set the user
     * without waiting for the auth state observer round-trip.
     */
    fun setCurrentUser(profile: UserProfile) {
        _currentUser.value = profile
        _authChecked.value = true
    }
}

