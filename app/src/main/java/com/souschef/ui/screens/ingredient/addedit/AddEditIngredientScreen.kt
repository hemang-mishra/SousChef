package com.souschef.ui.screens.ingredient.addedit

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.ui.components.PrimaryButton
import com.souschef.ui.components.SectionHeader
import com.souschef.ui.components.SousChefTextField
import com.souschef.ui.theme.SousChefTheme

val UNIT_OPTIONS = listOf("grams", "ml", "tsp", "tbsp", "cups", "pieces", "oz", "lbs", "kg", "L")

@Composable
fun AddEditIngredientScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: AddEditIngredientViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSaved, uiState.savedIngredientId) {
        if (uiState.isSaved && uiState.savedIngredientId != null) {
            onSaved(uiState.savedIngredientId!!)
        }
    }

    AddEditIngredientLayout(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onUnitChange = viewModel::onUnitChange,
        onDispensableChange = viewModel::onDispensableChange,
        onSpiceChange = viewModel::onSpiceChange,
        onSweetnessChange = viewModel::onSweetnessChange,
        onSaltnessChange = viewModel::onSaltnessChange,
        onSave = viewModel::onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIngredientLayout(
    uiState: AddEditIngredientUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onDispensableChange: (Boolean) -> Unit,
    onSpiceChange: (Double) -> Unit,
    onSweetnessChange: (Double) -> Unit,
    onSaltnessChange: (Double) -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) "Edit Ingredient" else "Add Ingredient",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SousChefTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = "Ingredient Name *",
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError
            )

            var unitExpanded by remember { mutableStateOf(false) }
            Box {
                SousChefTextField(
                    value = uiState.defaultUnit,
                    onValueChange = {},
                    label = "Default Unit",
                    trailingIcon = {
                        IconButton(onClick = { unitExpanded = !unitExpanded }) {
                            Icon(
                                if (unitExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = "Select unit"
                            )
                        }
                    },
                    modifier = Modifier.clickable { unitExpanded = true }
                )
                DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    UNIT_OPTIONS.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = { onUnitChange(unit); unitExpanded = false }
                        )
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Hardware dispensable", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Can the smart dispenser handle this?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = uiState.isDispensable,
                    onCheckedChange = onDispensableChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            SectionHeader(title = "Flavor Attributes")
            FlavorSlider("Spice Intensity", uiState.spiceIntensityValue) { onSpiceChange(it) }
            FlavorSlider("Sweetness", uiState.sweetnessValue) { onSweetnessChange(it) }
            FlavorSlider("Saltiness", uiState.saltnessValue) { onSaltnessChange(it) }

            Spacer(Modifier.height(8.dp))

            PrimaryButton(
                text = if (uiState.isEditMode) "Save Changes" else "Add Ingredient",
                onClick = onSave,
                isLoading = uiState.isLoading
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FlavorSlider(label: String, value: Double, onValueChange: (Double) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(value.toInt().toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddIngredientPreview() {
    SousChefTheme {
        AddEditIngredientLayout(
            uiState = AddEditIngredientUiState(name = "Red Chili Powder", spiceIntensityValue = 8.0),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onNameChange = {}, onUnitChange = {}, onDispensableChange = {},
            onSpiceChange = {}, onSweetnessChange = {}, onSaltnessChange = {}, onSave = {}
        )
    }
}

