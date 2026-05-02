package com.souschef.ui.screens.settings

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.auth.UserProfile
import com.souschef.model.device.Compartment
import com.souschef.permissions.BlePermissionHelper
import com.souschef.permissions.NotificationPermissionHelper
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.VerifiedChefBadge
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.GradientGold
import com.souschef.ui.theme.SousChefTheme

// ── Stateful entry ────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    isAdmin: Boolean,
    onSignOut: () -> Unit,
    onOpenIngredientLibrary: () -> Unit,
    onOpenHardwareTest: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedMessage) {
        uiState.savedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    SettingsScreenLayout(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        isAdmin = isAdmin,
        onCapacityChange = viewModel::onCapacityChange,
        onSaveDispenser = viewModel::onSaveDispenser,
        onResetCounts = viewModel::onResetAllCounts,
        onSignOut = onSignOut,
        onOpenIngredientLibrary = onOpenIngredientLibrary,
        onOpenHardwareTest = onOpenHardwareTest,
        onOpenAdmin = onOpenAdmin
    )
}

// ── Stateless layout ──────────────────────────────────────────────────────────

@Composable
fun SettingsScreenLayout(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    isAdmin: Boolean,
    onCapacityChange: (Int, String) -> Unit,
    onSaveDispenser: () -> Unit,
    onResetCounts: () -> Unit,
    onSignOut: () -> Unit,
    onOpenIngredientLibrary: () -> Unit,
    onOpenHardwareTest: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { ProfileHeader(uiState.userProfile) }

            item {
                PermissionsSection(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            item {
                DispenserSection(
                    compartments = uiState.compartments,
                    editedCapacities = uiState.editedCapacities,
                    isSaving = uiState.isSavingDispenser,
                    onCapacityChange = onCapacityChange,
                    onSaveDispenser = onSaveDispenser,
                    onResetCounts = onResetCounts,
                    onOpenHardwareTest = onOpenHardwareTest,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            item {
                NavRowCard(
                    icon = Icons.Outlined.Eco,
                    title = "Ingredient Library",
                    subtitle = "Manage spices, herbs and pantry items",
                    onClick = onOpenIngredientLibrary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            if (isAdmin) {
                item {
                    NavRowCard(
                        icon = Icons.Outlined.AdminPanelSettings,
                        title = "Admin Panel",
                        subtitle = "Manage users and verified chefs",
                        onClick = onOpenAdmin,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                PremiumOutlinedButton(
                    text = "Sign Out",
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}

// ── Profile Header ────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(profile: UserProfile?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(GradientGold))
                .padding(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(3.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(GradientGold)),
            contentAlignment = Alignment.Center
        ) {
            val initials = profile?.displayName
                ?.split(" ")
                ?.take(2)
                ?.mapNotNull { it.firstOrNull()?.uppercase() }
                ?.joinToString("") ?: "?"
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.heroBackground()
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = profile?.displayName.takeUnless { it.isNullOrBlank() } ?: "Guest",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary()
        )
        if (!profile?.email.isNullOrBlank()) {
            Text(
                text = profile?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary()
            )
        }
        if (profile?.isVerifiedChef == true) {
            Spacer(Modifier.height(10.dp))
            VerifiedChefBadge()
        }
    }
}

// ── Permissions Section ───────────────────────────────────────────────────────

@Composable
private fun PermissionsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bleGranted by remember { mutableStateOf(BlePermissionHelper.hasAllPermissions(context)) }
    var notifGranted by remember { mutableStateOf(NotificationPermissionHelper.hasPermission(context)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        bleGranted = BlePermissionHelper.hasAllPermissions(context)
        notifGranted = NotificationPermissionHelper.hasPermission(context)
    }

    SectionContainer(
        title = "Permissions",
        subtitle = "Required for hardware and refill alerts",
        icon = Icons.Outlined.Tune,
        initiallyExpanded = !(bleGranted && notifGranted),
        modifier = modifier
    ) {
        PermissionStatusRow(
            icon = Icons.Default.Bluetooth,
            label = "Bluetooth",
            description = "Pair with your dispenser",
            granted = bleGranted,
            onGrant = {
                launcher.launch(BlePermissionHelper.requiredPermissions)
            }
        )
        Spacer(Modifier.height(8.dp))
        PermissionStatusRow(
            icon = Icons.Default.Notifications,
            label = "Notifications",
            description = "Get refill alerts",
            granted = notifGranted,
            onGrant = {
                if (NotificationPermissionHelper.requiredPermissions.isNotEmpty()) {
                    launcher.launch(NotificationPermissionHelper.requiredPermissions)
                }
            }
        )
    }
}

@Composable
private fun PermissionStatusRow(
    icon: ImageVector,
    label: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(AppColors.gold().copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = AppColors.gold(), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary()
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary()
            )
        }
        if (granted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Granted",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            TextButton(onClick = onGrant) {
                Text("Grant", color = AppColors.gold(), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Dispenser Section ─────────────────────────────────────────────────────────

@Composable
private fun DispenserSection(
    compartments: List<Compartment>,
    editedCapacities: Map<Int, String>,
    isSaving: Boolean,
    onCapacityChange: (Int, String) -> Unit,
    onSaveDispenser: () -> Unit,
    onResetCounts: () -> Unit,
    onOpenHardwareTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionContainer(
        title = "Dispenser",
        subtitle = "Configure compartment capacities",
        icon = Icons.Outlined.Science,
        initiallyExpanded = false,
        modifier = modifier
    ) {
        if (compartments.isEmpty()) {
            Text(
                text = "Connect your dispenser from the Dispenser tab to configure compartments.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary()
            )
            return@SectionContainer
        }

        compartments.forEach { compartment ->
            CompartmentCapacityRow(
                compartment = compartment,
                edited = editedCapacities[compartment.compartmentId] ?: "0.0",
                onCapacityChange = { onCapacityChange(compartment.compartmentId, it) }
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onSaveDispenser,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.gold())
        ) {
            Text(
                if (isSaving) "Saving…" else "Save Capacities",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onResetCounts,
            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
        ) {
            Text("Reset all dispensed counts")
        }
        Text(
            text = "Use after physically refilling all compartments.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textTertiary()
        )

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onOpenHardwareTest,
            colors = ButtonDefaults.textButtonColors(contentColor = AppColors.gold())
        ) {
            Text("Hardware Test Mode")
        }
        Text(
            text = "Send raw commands to test the physical dispenser.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textTertiary()
        )
    }
}

@Composable
private fun CompartmentCapacityRow(
    compartment: Compartment,
    edited: String,
    onCapacityChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.size(width = 44.dp, height = 52.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.Science, null,
                tint = if (compartment.isEmpty) AppColors.textTertiary() else AppColors.gold(),
                modifier = Modifier.size(18.dp)
            )
            Text(
                "#${compartment.compartmentId}",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textSecondary(),
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = compartment.ingredientName ?: "Empty",
                style = MaterialTheme.typography.bodyMedium,
                color = if (compartment.isEmpty) AppColors.textTertiary() else AppColors.textPrimary(),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Dispensed: ~%.1f tsp".format(compartment.dispensedTsp),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textTertiary()
            )
        }

        OutlinedTextField(
            value = edited,
            onValueChange = onCapacityChange,
            modifier = Modifier.size(width = 96.dp, height = 56.dp),
            suffix = { Text("tsp", style = MaterialTheme.typography.labelSmall) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.gold(),
                unfocusedBorderColor = AppColors.border()
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

// ── Reusable Section Container ────────────────────────────────────────────────

@Composable
private fun SectionContainer(
    title: String,
    subtitle: String,
    icon: ImageVector,
    initiallyExpanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val arrowRot by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AppColors.cardBackground(),
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AppColors.border())
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppColors.gold().copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = AppColors.gold(), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary()
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary()
                    )
                }
                Icon(
                    Icons.Outlined.ExpandMore,
                    null,
                    tint = AppColors.textSecondary(),
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(arrowRot)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun NavRowCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = AppColors.cardBackground(),
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AppColors.border())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(AppColors.gold().copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AppColors.gold(), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary()
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary()
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = AppColors.textTertiary(),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    SousChefTheme {
        SettingsScreenLayout(
            uiState = SettingsUiState(
                userProfile = UserProfile(
                    displayName = "Hemang Mishra",
                    email = "hemang@souschef.com",
                    role = "user"
                ),
                compartments = (1..5).map {
                    Compartment(
                        compartmentId = it,
                        ingredientName = if (it < 4) "Spice $it" else null,
                        totalCapacityTsp = 12.0,
                        dispensedCounts = it * 3
                    )
                },
                editedCapacities = (1..5).associate { it to "12.0" },
                isLoading = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            isAdmin = false,
            onCapacityChange = { _, _ -> },
            onSaveDispenser = {},
            onResetCounts = {},
            onSignOut = {},
            onOpenIngredientLibrary = {},
            onOpenHardwareTest = {},
            onOpenAdmin = {}
        )
    }
}
