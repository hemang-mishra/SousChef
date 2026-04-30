package com.souschef.ui.screens.device.dispenser

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.souschef.model.device.BleConnectionState
import com.souschef.model.device.Compartment
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.SousChefTheme
// ── Stateful entry point ──────────────────────────────────────────────────────

@Composable
fun DispenserScreen(
    viewModel: DispenserViewModel,
    onNavigateToSettings: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSuccess() }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            viewModel.onScanConnect()
        } else {
            viewModel.onError("Bluetooth permissions are required to connect to the dispenser.")
        }
    }

    DispenserScreenLayout(
        uiState             = uiState,
        onScanConnect       = {
            if (com.souschef.permissions.BlePermissionHelper.hasAllPermissions(context)) {
                viewModel.onScanConnect()
            } else {
                permissionLauncher.launch(com.souschef.permissions.BlePermissionHelper.requiredPermissions)
            }
        },
        onDisconnect        = viewModel::onDisconnect,
        onAssignIngredient  = viewModel::onAssignIngredient,
        onClearCompartment  = viewModel::onClearCompartment,
        onRefill            = { id, qty -> viewModel.onRefill(id, qty) },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onNavigateToSettings = onNavigateToSettings,
        snackbarHostState   = snackbarHostState
    )
}

// ── Stateless layout ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispenserScreenLayout(
    uiState: DispenserUiState,
    onScanConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onAssignIngredient: (Int, GlobalIngredient) -> Unit,
    onClearCompartment: (Int) -> Unit,
    onRefill: (Int, Double?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    // Which compartment's assignment sheet is open?
    var assigningCompartmentId by remember { mutableStateOf<Int?>(null) }
    // Which compartment's refill sheet is open?
    var refillingCompartmentId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Spice Dispenser",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary()
                    )
                    Text(
                        text = "Manage your 5 compartments",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary()
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Dispenser Settings",
                        tint = AppColors.textSecondary()
                    )
                }
            }
        }
    ) { padding ->
        val assignedCount = uiState.compartments.count { !it.isEmpty }
        val lowStockCount = uiState.compartments.count { it.isLowStock }
        val emptyCount = uiState.compartments.count { it.isEmpty }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // BLE hero card
            item {
                BleStatusCard(
                    connectionState = uiState.connectionState,
                    onScanConnect   = onScanConnect,
                    onDisconnect    = onDisconnect
                )
            }

            // At-a-glance stats: assigned / low-stock / empty
            item {
                StatsRow(
                    assignedCount = assignedCount,
                    totalCount    = uiState.compartments.size,
                    lowStockCount = lowStockCount,
                    emptyCount    = emptyCount
                )
            }

            item {
                Text(
                    text = "Compartments",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary(),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // Compartment cards
            items(uiState.compartments, key = { it.compartmentId }) { compartment ->
                CompartmentCard(
                    compartment = compartment,
                    liveImageUrl = compartment.globalIngredientId
                        ?.let { uiState.ingredientImageById[it] }
                        ?: compartment.ingredientImageUrl,
                    onAssign    = { assigningCompartmentId = compartment.compartmentId },
                    onClear     = { onClearCompartment(compartment.compartmentId) },
                    onRefill    = { refillingCompartmentId = compartment.compartmentId }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Assignment bottom sheet
    if (assigningCompartmentId != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { assigningCompartmentId = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AssignCompartmentContent(
                compartmentId       = assigningCompartmentId!!,
                ingredients         = uiState.filteredIngredients,
                searchQuery         = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onIngredientSelected = { ingredient ->
                    onAssignIngredient(assigningCompartmentId!!, ingredient)
                    assigningCompartmentId = null
                }
            )
        }
    }

    // Refill bottom sheet
    if (refillingCompartmentId != null) {
        val refillSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val targetCompartment = uiState.compartments
            .firstOrNull { it.compartmentId == refillingCompartmentId }
        if (targetCompartment != null) {
            ModalBottomSheet(
                onDismissRequest = { refillingCompartmentId = null },
                sheetState = refillSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                RefillCompartmentContent(
                    compartment = targetCompartment,
                    liveImageUrl = targetCompartment.globalIngredientId
                        ?.let { uiState.ingredientImageById[it] }
                        ?: targetCompartment.ingredientImageUrl,
                    onConfirm = { quantityTsp ->
                        onRefill(targetCompartment.compartmentId, quantityTsp)
                        refillingCompartmentId = null
                    },
                    onDismiss = { refillingCompartmentId = null }
                )
            }
        } else {
            // Compartment vanished while sheet was open — bail out cleanly.
            refillingCompartmentId = null
        }
    }
}

// ── BLE Status Card ───────────────────────────────────────────────────────────

@Composable
private fun BleStatusCard(
    connectionState: BleConnectionState,
    onScanConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isConnected = connectionState is BleConnectionState.Connected
    val isScanning  = connectionState is BleConnectionState.Scanning ||
            connectionState is BleConnectionState.Connecting
    val isError     = connectionState is BleConnectionState.Error

    val dotColor by animateColorAsState(
        targetValue = when (connectionState) {
            is BleConnectionState.Connected    -> Color(0xFF4CAF50)
            is BleConnectionState.Scanning,
            is BleConnectionState.Connecting   -> Color(0xFFFFA000)
            is BleConnectionState.Error        -> Color(0xFFE53935)
            else                               -> Color(0xFF9E9E9E)
        },
        animationSpec = tween(400), label = "ble_dot"
    )

    val statusLabel = when (connectionState) {
        is BleConnectionState.Connected  -> "Connected"
        is BleConnectionState.Scanning   -> "Scanning…"
        is BleConnectionState.Connecting -> "Connecting…"
        is BleConnectionState.Error      -> "Error: ${connectionState.message}"
        else                             -> "Disconnected"
    }

    val hint = when (connectionState) {
        is BleConnectionState.Connected -> "Tap a compartment to refill or reassign."
        is BleConnectionState.Scanning,
        is BleConnectionState.Connecting -> "Make sure the dispenser is powered on and nearby."
        is BleConnectionState.Error -> "Tap Connect to retry."
        else -> "Pair with your SousChef Dispenser to start cooking."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isConnected) listOf(
                        AppColors.gold().copy(alpha = 0.20f),
                        AppColors.gold().copy(alpha = 0.06f)
                    ) else listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .border(
                width = 1.dp,
                color = if (isConnected) AppColors.gold().copy(alpha = 0.5f)
                        else if (isError) Color(0xFFE53935).copy(alpha = 0.4f)
                        else AppColors.border(),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Animated status dot inside a soft halo so it reads at a glance.
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SousChef Dispenser",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary()
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) Color(0xFFE53935) else AppColors.textSecondary()
                )
            }
            if (isConnected) {
                TextButton(onClick = onDisconnect) {
                    Text("Disconnect", color = AppColors.textSecondary())
                }
            } else {
                Button(
                    onClick = onScanConnect,
                    enabled = !isScanning,
                    colors  = ButtonDefaults.buttonColors(containerColor = AppColors.gold()),
                    shape   = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Default.BluetoothSearching,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (isScanning) "Scanning…" else "Connect")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textTertiary()
        )
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    assignedCount: Int,
    totalCount: Int,
    lowStockCount: Int,
    emptyCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatTile(
            value = "$assignedCount/$totalCount",
            label = "Assigned",
            accent = AppColors.gold(),
            modifier = Modifier.weight(1f)
        )
        StatTile(
            value = lowStockCount.toString(),
            label = "Low stock",
            accent = if (lowStockCount > 0) Color(0xFFFFA000) else AppColors.textTertiary(),
            modifier = Modifier.weight(1f)
        )
        StatTile(
            value = emptyCount.toString(),
            label = "Empty",
            accent = if (emptyCount > 0) AppColors.textSecondary() else AppColors.textTertiary(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, AppColors.border(), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.textTertiary()
        )
    }
}

// ── Compartment Card ──────────────────────────────────────────────────────────

@Composable
private fun CompartmentCard(
    compartment: Compartment,
    liveImageUrl: String?,
    onAssign: () -> Unit,
    onClear: () -> Unit,
    onRefill: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (compartment.isLowStock) 1.5.dp else 0.5.dp,
                color = if (compartment.isLowStock) Color(0xFFFFA000) else AppColors.border(),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {
        // Header row
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Numbered badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppColors.gold().copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${compartment.compartmentId}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.gold()
                )
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Compartment ${compartment.compartmentId}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary()
                )
                if (compartment.isLowStock) {
                    Text(
                        text = "⚠ Low stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFA000)
                    )
                }
            }

            // Assign / clear buttons
            if (compartment.isEmpty) {
                IconButton(onClick = onAssign) {
                    Icon(Icons.Default.Add, "Assign spice", tint = AppColors.gold())
                }
            } else {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.Close, "Clear compartment", tint = AppColors.textTertiary())
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (compartment.isEmpty) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onAssign() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Science,
                        contentDescription = null,
                        tint = AppColors.textTertiary(),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Tap to assign a spice",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textTertiary()
                    )
                }
            }
        } else {
            // Ingredient info row
            Row(verticalAlignment = Alignment.CenterVertically) {
                IngredientThumbnail(
                    imageUrl = liveImageUrl,
                    contentDescription = compartment.ingredientName
                )

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = compartment.ingredientName ?: "—",
                        style    = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color    = AppColors.textPrimary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (compartment.totalCapacityTsp > 0.0) {
                        Text(
                            text  = "~%.1f tsp remaining".format(compartment.remainingTsp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (compartment.isLowStock) Color(0xFFFFA000)
                                    else AppColors.textSecondary()
                        )
                    } else {
                        Text(
                            text = "Capacity not set",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textTertiary()
                        )
                    }
                }

                // Refill button
                IconButton(onClick = onRefill) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Mark as refilled",
                        tint = AppColors.textSecondary()
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Capacity bar
            if (compartment.totalCapacityTsp > 0.0) {
                LinearProgressIndicator(
                    progress       = { compartment.fillPercent },
                    modifier       = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color          = if (compartment.isLowStock) Color(0xFFFFA000) else AppColors.gold(),
                    trackColor     = AppColors.border(),
                    strokeCap      = StrokeCap.Round
                )
            }
        }
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Clear Compartment ${compartment.compartmentId}?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "This will remove the assigned spice. The compartment will be marked as empty.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { onClear(); showClearDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Assign Compartment Content (inside BottomSheet) ───────────────────────────

@Composable
fun AssignCompartmentContent(
    compartmentId: Int,
    ingredients: List<GlobalIngredient>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onIngredientSelected: (GlobalIngredient) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Assign to Compartment $compartmentId",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary()
        )
        Text(
            text = "Only dispensable spices are shown",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textTertiary()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text("Search spices…") },
            leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = AppColors.gold(),
                unfocusedBorderColor = AppColors.border()
            )
        )
        Spacer(Modifier.height(12.dp))

        if (ingredients.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = "No dispensable ingredients found.\nAdd some via the Ingredient Library.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = AppColors.textTertiary(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ingredients, key = { it.ingredientId }) { ingredient ->
                    IngredientPickerRow(ingredient = ingredient, onClick = { onIngredientSelected(ingredient) })
                }
            }
        }
    }
}

// ── Refill Bottom Sheet ───────────────────────────────────────────────────────

/**
 * Bottom sheet for recording a physical refill of a compartment.
 *
 * The user can either:
 * - Tap a quick preset (2, 4, 6, 8, 10 tsp) — covers the typical store-bought
 *   spice jar sizes.
 * - Tap "Custom amount" to type an exact tsp value.
 *
 * On confirm we hand the chosen tsp value back to the ViewModel which both
 * resets `dispensedCounts` to 0 AND overwrites `totalCapacityTsp` with the
 * supplied amount, so the on-card progress bar snaps to a fresh full
 * compartment of exactly that size.
 */
@Composable
private fun RefillCompartmentContent(
    compartment: Compartment,
    liveImageUrl: String?,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    // Preset tsp values cover most common single-jar refill sizes.
    val presets = listOf(2.0, 4.0, 6.0, 8.0, 10.0)

    // Sensible default: whatever was previously configured, falling back to
    // the first preset for an uninitialised compartment.
    val initialPreset = presets.firstOrNull { it == compartment.totalCapacityTsp }
    var selectedPreset by remember(compartment.compartmentId) { mutableStateOf(initialPreset) }
    var customMode by remember(compartment.compartmentId) {
        mutableStateOf(initialPreset == null && compartment.totalCapacityTsp > 0.0)
    }
    var customText by remember(compartment.compartmentId) {
        mutableStateOf(
            if (compartment.totalCapacityTsp > 0.0 && initialPreset == null)
                "%.1f".format(compartment.totalCapacityTsp)
            else ""
        )
    }

    val customValueTsp = customText.replace(',', '.').toDoubleOrNull()
    val chosenAmount: Double? = when {
        customMode -> customValueTsp?.takeIf { it > 0.0 }
        else -> selectedPreset
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        // Header row with thumbnail
        Row(verticalAlignment = Alignment.CenterVertically) {
            IngredientThumbnail(
                imageUrl = liveImageUrl,
                contentDescription = compartment.ingredientName,
                size = 48.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Refill Compartment ${compartment.compartmentId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary()
                )
                Text(
                    text = compartment.ingredientName ?: "Unassigned",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "How much did you pour in?",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textPrimary(),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "1 tsp ≈ 5 ml. Pick the closest amount or enter a custom value.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textTertiary()
        )

        Spacer(Modifier.height(14.dp))

        // Preset chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = !customMode && selectedPreset == preset
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) AppColors.gold().copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) AppColors.gold() else AppColors.border(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            customMode = false
                            selectedPreset = preset
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${preset.toInt()} tsp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) AppColors.gold() else AppColors.textPrimary()
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Custom amount toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (customMode) AppColors.gold().copy(alpha = 0.10f)
                    else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = if (customMode) 1.5.dp else 0.5.dp,
                    color = if (customMode) AppColors.gold() else AppColors.border(),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { customMode = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Custom amount (tsp)",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textPrimary(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = customText,
                onValueChange = { raw ->
                    // Only allow digits + a single decimal separator. Keeps
                    // the field tidy on phones that don't honour
                    // KeyboardType.Decimal strictly.
                    val sanitized = raw.filter { it.isDigit() || it == '.' || it == ',' }
                    customText = sanitized
                    customMode = true
                },
                placeholder = { Text("e.g. 5.5") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.gold(),
                    unfocusedBorderColor = AppColors.border()
                ),
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .width(120.dp)
            )
        }

        if (customMode && customText.isNotBlank() && customValueTsp == null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Enter a positive number, e.g. 5 or 5.5",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE53935)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = AppColors.textSecondary())
            }
            Button(
                onClick = { chosenAmount?.let(onConfirm) },
                enabled = chosenAmount != null,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.gold()),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.4f),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = chosenAmount?.let { "Mark refilled • %.1f tsp".format(it) }
                        ?: "Mark refilled",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun IngredientPickerRow(ingredient: GlobalIngredient, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, AppColors.border(), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IngredientThumbnail(
            imageUrl = ingredient.imageUrl,
            contentDescription = ingredient.name,
            size = 48.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text   = ingredient.name,
                style  = MaterialTheme.typography.bodyLarge,
                color  = AppColors.textPrimary(),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text  = ingredient.defaultUnit,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textTertiary()
            )
        }
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = AppColors.gold().copy(alpha = 0f)
        )
    }
}

/**
 * Reusable ingredient image bubble. Falls back to a gold-tinted Science
 * icon when no remote image is available so the layout doesn't shift
 * between assigned slots.
 */
@Composable
private fun IngredientThumbnail(
    imageUrl: String?,
    contentDescription: String?,
    size: androidx.compose.ui.unit.Dp = 56.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.gold().copy(alpha = 0.10f))
            .border(0.5.dp, AppColors.gold().copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Outlined.Science,
                contentDescription = null,
                tint = AppColors.gold(),
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DispenserScreenPreview() {
    SousChefTheme {
        DispenserScreenLayout(
            uiState = DispenserUiState(
                compartments = listOf(
                    Compartment(1, "id1", "Red Chili Powder", null, 10.0, 12),
                    Compartment(2, "id2", "Turmeric", null, 8.0, 4),
                    Compartment(3, null,  null, null, 0.0, 0),
                    Compartment(4, "id3", "Cumin", null, 5.0, 18),
                    Compartment(5, null,  null, null, 0.0, 0)
                ),
                connectionState = BleConnectionState.Connected
            ),
            onScanConnect       = {},
            onDisconnect        = {},
            onAssignIngredient  = { _, _ -> },
            onClearCompartment  = {},
            onRefill            = { _, _ -> },
            onSearchQueryChange = {},
            onNavigateToSettings = {},
            snackbarHostState   = remember { SnackbarHostState() }
        )
    }
}
