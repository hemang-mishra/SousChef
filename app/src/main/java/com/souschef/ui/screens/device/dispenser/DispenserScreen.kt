package com.souschef.ui.screens.device.dispenser

import android.content.res.Configuration
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

    DispenserScreenLayout(
        uiState             = uiState,
        onScanConnect       = viewModel::onScanConnect,
        onDisconnect        = viewModel::onDisconnect,
        onAssignIngredient  = viewModel::onAssignIngredient,
        onClearCompartment  = viewModel::onClearCompartment,
        onRefill            = viewModel::onRefill,
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
    onRefill: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    // Which compartment's assignment sheet is open?
    var assigningCompartmentId by remember { mutableStateOf<Int?>(null) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // BLE status card
            item {
                BleStatusCard(
                    connectionState = uiState.connectionState,
                    onScanConnect   = onScanConnect,
                    onDisconnect    = onDisconnect
                )
                Spacer(Modifier.height(8.dp))
            }

            // Compartment cards
            items(uiState.compartments, key = { it.compartmentId }) { compartment ->
                CompartmentCard(
                    compartment = compartment,
                    onAssign    = { assigningCompartmentId = compartment.compartmentId },
                    onClear     = { onClearCompartment(compartment.compartmentId) },
                    onRefill    = { onRefill(compartment.compartmentId) }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, AppColors.border(), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "SousChef Dispenser",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary()
            )
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary()
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
                shape   = RoundedCornerShape(8.dp)
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
}

// ── Compartment Card ──────────────────────────────────────────────────────────

@Composable
private fun CompartmentCard(
    compartment: Compartment,
    onAssign: () -> Unit,
    onClear: () -> Unit,
    onRefill: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (compartment.isLowStock) 1.5.dp else 0.5.dp,
                color = if (compartment.isLowStock) Color(0xFFFFA000) else AppColors.border(),
                shape = RoundedCornerShape(16.dp)
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
                if (!compartment.ingredientImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model              = compartment.ingredientImageUrl,
                        contentDescription = compartment.ingredientName,
                        modifier           = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AppColors.gold().copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Science,
                            contentDescription = null,
                            tint     = AppColors.gold(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = compartment.ingredientName ?: "—",
                        style    = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
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

@Composable
private fun IngredientPickerRow(ingredient: GlobalIngredient, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!ingredient.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model              = ingredient.imageUrl,
                contentDescription = ingredient.name,
                modifier           = Modifier.size(40.dp).clip(CircleShape),
                contentScale       = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(AppColors.gold().copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Science, null, tint = AppColors.gold(), modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text   = ingredient.name,
                style  = MaterialTheme.typography.bodyLarge,
                color  = AppColors.textPrimary(),
                fontWeight = FontWeight.Medium
            )
            Text(
                text  = ingredient.defaultUnit,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textTertiary()
            )
        }
        Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.gold().copy(alpha = 0f))
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
            onRefill            = {},
            onSearchQueryChange = {},
            onNavigateToSettings = {},
            snackbarHostState   = remember { SnackbarHostState() }
        )
    }
}
