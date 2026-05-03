package com.souschef.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.souschef.model.auth.UserProfile
import com.souschef.ui.components.EmptyStateView
import com.souschef.ui.components.VerifiedChefBadge
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.GradientGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    isAdmin: Boolean,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var pendingToggle by remember { mutableStateOf<UserProfile?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Admin Panel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = "Access Denied",
                    subtitle = "Only admins can view this page.",
                    icon = Icons.Outlined.AdminPanelSettings
                )
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = AppColors.gold(),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .height(3.dp),
                        color = AppColors.gold()
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Users", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Statistics", fontWeight = FontWeight.SemiBold) }
                )
            }

            when (selectedTab) {
                0 -> UsersTab(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onToggleClick = { user -> pendingToggle = user }
                )
                1 -> StatsTab(uiState = uiState)
            }
        }
    }

    pendingToggle?.let { user ->
        AlertDialog(
            onDismissRequest = { pendingToggle = null },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onToggleVerified(user.uid, user.isVerifiedChef)
                    pendingToggle = null
                }) {
                    Text(
                        if (user.isVerifiedChef) "Revoke" else "Verify",
                        color = AppColors.gold(),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingToggle = null }) {
                    Text("Cancel", color = AppColors.textSecondary())
                }
            },
            title = {
                Text(
                    if (user.isVerifiedChef) "Revoke Verified Chef?" else "Grant Verified Chef?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (user.isVerifiedChef)
                        "Remove the verified chef badge from ${user.displayName}?"
                    else
                        "Mark ${user.displayName} as a verified chef? They'll get the badge across the app."
                )
            }
        )
    }
}

@Composable
private fun UsersTab(
    uiState: AdminUiState,
    onSearchQueryChange: (String) -> Unit,
    onToggleClick: (UserProfile) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            placeholder = {
                Text("Search by name or email", color = AppColors.textTertiary())
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = AppColors.textTertiary())
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.gold(),
                unfocusedBorderColor = AppColors.border()
            )
        )

        if (uiState.isLoading && uiState.users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = AppColors.textSecondary())
            }
        } else if (uiState.filteredUsers.isEmpty()) {
            EmptyStateView(
                title = "No users",
                subtitle = "Try a different search.",
                icon = Icons.Default.Search,
                modifier = Modifier.padding(top = 48.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredUsers, key = { it.uid }) { user ->
                    UserAdminRow(user = user, onToggleClick = { onToggleClick(user) })
                }
            }
        }
    }
}

@Composable
private fun UserAdminRow(user: UserProfile, onToggleClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AppColors.cardBackground(),
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AppColors.border())
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientGold)),
                contentAlignment = Alignment.Center
            ) {
                val initials = user.displayName
                    .split(" ").take(2)
                    .mapNotNull { it.firstOrNull()?.uppercase() }
                    .joinToString("")
                    .ifBlank { "?" }
                Text(
                    initials,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.heroBackground()
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.displayName.ifBlank { "(no name)" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary()
                    )
                    if (user.isVerifiedChef) {
                        Spacer(Modifier.width(8.dp))
                        VerifiedChefBadge()
                    }
                }
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary()
                )
                if (user.isAdmin) {
                    Text(
                        "Admin",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.gold(),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Button(
                onClick = onToggleClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (user.isVerifiedChef)
                        Color(0xFFE53935).copy(alpha = 0.12f)
                    else AppColors.gold()
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    if (user.isVerifiedChef) Icons.Default.Block else Icons.Default.CheckCircle,
                    null,
                    tint = if (user.isVerifiedChef) Color(0xFFE53935) else AppColors.onGold(),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (user.isVerifiedChef) "Revoke" else "Verify",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (user.isVerifiedChef) Color(0xFFE53935) else AppColors.onGold()
                )
            }
        }
    }
}

@Composable
private fun StatsTab(uiState: AdminUiState) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { StatTile(label = "Total Users", value = uiState.totalUsers.toString()) }
        item { StatTile(label = "Total Recipes", value = uiState.totalRecipes.toString()) }
        item { StatTile(label = "Verified Chefs", value = uiState.totalVerifiedChefs.toString()) }
    }
}

@Composable
private fun StatTile(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.cardBackground(),
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AppColors.border())
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.gold()
                )
            }
        }
    }
}
