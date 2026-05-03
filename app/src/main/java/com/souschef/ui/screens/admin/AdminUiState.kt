package com.souschef.ui.screens.admin

import com.souschef.model.auth.UserProfile

data class AdminUiState(
    val users: List<UserProfile> = emptyList(),
    val searchQuery: String = "",
    val totalUsers: Int = 0,
    val totalRecipes: Int = 0,
    val totalVerifiedChefs: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val message: String? = null
) {
    val filteredUsers: List<UserProfile>
        get() = if (searchQuery.isBlank()) users
        else users.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
}
