package com.souschef.ui.screens.device.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Science
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.device.Compartment
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.SousChefTheme

// ── Stateful screen ───────────────────────────────────────────────────────────

@Composable
fun DispenserSettingsScreen(
    viewModel: DispenserSettingsViewModel,
    onBackPress: () -> Unit,
    onNavigateToGlobalIngredients: () -> Unit,
    onNavigateToHardwareTest: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Settings saved")
            onBackPress()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    DispenserSettingsScreenLayout(
        uiState           = uiState,
        onCapacityChange  = viewModel::onCapacityChange,
        onSave            = viewModel::onSave,
        onResetAllCounts  = viewModel::onResetAllCounts,
        onNavigateToGlobalIngredients = onNavigateToGlobalIngredients,
        onNavigateToHardwareTest = onNavigateToHardwareTest,
        onBackPress       = onBackPress,
        snackbarHostState = snackbarHostState
    )
}

// ── Stateless layout ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispenserSettingsScreenLayout(
    uiState: DispenserSettingsUiState,
    onCapacityChange: (Int, String) -> Unit,
    onSave: () -> Unit,
    onResetAllCounts: () -> Unit,
    onNavigateToGlobalIngredients: () -> Unit,
    onNavigateToHardwareTest: () -> Unit,
    onBackPress: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dispenser Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Compartment Capacities Section ────────────────────────────────
            item {
                SectionHeader(title = "Compartment Capacities")
                Text(
                    text = "Set how many teaspoons (tsp) each compartment can hold. " +
                            "This is used to track remaining stock.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary()
                )
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.compartments.forEach { compartment ->
                        CompartmentCapacityRow(
                            compartment      = compartment,
                            editedCapacityTsp = uiState.editedCapacities[compartment.compartmentId] ?: "0.0",
                            onCapacityChange = { onCapacityChange(compartment.compartmentId, it) }
                        )
                    }
                }
            }

            // ── Info tile ─────────────────────────────────────────────────────
            item {
                InfoTile(
                    text = "1 count = ¼ tsp ejected by the hardware. " +
                            "Counts are tracked automatically when you dispense during cooking."
                )
            }

            // ── Device section ────────────────────────────────────────────────
            item {
                SectionHeader(title = "Device")
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onResetAllCounts,
                    colors  = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                ) {
                    Text("Reset all dispensed counts")
                }
                Text(
                    text = "Use this after you've physically refilled all compartments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
                
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onNavigateToHardwareTest,
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.gold())
                ) {
                    Text("Hardware Test Mode")
                }
                Text(
                    text = "Directly trigger dispenser commands to test physical hardware.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
                Spacer(Modifier.height(16.dp))
                
                SectionHeader(title = "Ingredients")
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onNavigateToGlobalIngredients,
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.gold())
                ) {
                    Text("Manage Global Ingredients Library")
                }
                Text(
                    text = "Add or edit spices in your library to assign them to compartments later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
            }

            // ── Save button ───────────────────────────────────────────────────
            item {
                Button(
                    onClick  = onSave,
                    enabled  = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AppColors.gold()),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text  = if (uiState.isSaving) "Saving…" else "Save Settings",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun CompartmentCapacityRow(
    compartment: Compartment,
    editedCapacityTsp: String,
    onCapacityChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Compartment badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.size(width = 48.dp, height = 56.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.Science, null,
                tint     = if (compartment.isEmpty) AppColors.textTertiary() else AppColors.gold(),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text   = "#${compartment.compartmentId}",
                style  = MaterialTheme.typography.labelSmall,
                color  = AppColors.textSecondary(),
                fontWeight = FontWeight.SemiBold
            )
        }

        // Label
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = compartment.ingredientName ?: "Empty",
                style = MaterialTheme.typography.bodyMedium,
                color = if (compartment.isEmpty) AppColors.textTertiary() else AppColors.textPrimary(),
                fontWeight = FontWeight.Medium
            )
            Text(
                text  = "Dispensed: ~%.1f tsp".format(compartment.dispensedTsp),
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textTertiary()
            )
        }

        // Capacity input
        OutlinedTextField(
            value         = editedCapacityTsp,
            onValueChange = onCapacityChange,
            modifier      = Modifier.size(width = 100.dp, height = 56.dp),
            suffix        = { Text("tsp", style = MaterialTheme.typography.labelSmall) },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape  = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = AppColors.gold(),
                unfocusedBorderColor = AppColors.border()
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text   = title,
        style  = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color  = AppColors.textPrimary()
    )
}

@Composable
private fun InfoTile(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.gold().copy(alpha = 0.07f), RoundedCornerShape(10.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("ⓘ", style = MaterialTheme.typography.labelLarge, color = AppColors.gold())
        Text(text, style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary())
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DispenserSettingsPreview() {
    SousChefTheme {
        DispenserSettingsScreenLayout(
            uiState = DispenserSettingsUiState(
                compartments = (1..5).map { Compartment(it, "id$it", "Spice $it", null, 12.0, it * 3) },
                editedCapacities = (1..5).associate { it to "12.0" }
            ),
            onCapacityChange = { _, _ -> },
            onSave           = {},
            onResetAllCounts = {},
            onNavigateToGlobalIngredients = {},
            onNavigateToHardwareTest = {},
            onBackPress      = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
