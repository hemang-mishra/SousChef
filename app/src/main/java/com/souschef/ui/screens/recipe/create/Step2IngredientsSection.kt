package com.souschef.ui.screens.recipe.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.ui.components.PremiumButton
import com.souschef.ui.components.PremiumDivider
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.PremiumSectionHeader
import com.souschef.ui.components.PremiumTextButton
import com.souschef.ui.components.SearchField
import com.souschef.ui.components.SousChefTextField
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.CustomShapes
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// Step 2: Ingredients
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Step2Ingredients(
    ingredients: List<RecipeIngredient>,
    globalIngredients: List<GlobalIngredient>,
    newlyCreatedIngredients: List<String>,
    ingredientError: String?,
    onAddIngredient: (RecipeIngredient) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onCreateGlobalIngredient: (String, Double, String) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Build a lookup map for display
    val globalMap = remember(globalIngredients) { globalIngredients.associateBy { it.ingredientId } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        PremiumSectionHeader(
            title = "Ingredients",
            action = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ingredients.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.gold().copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${ingredients.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.gold()
                            )
                        }
                    }
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Add ingredient",
                            tint = AppColors.gold()
                        )
                    }
                }
            }
        )

        if (ingredientError != null) {
            Text(
                text = ingredientError,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.error(),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (newlyCreatedIngredients.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.gold().copy(alpha = 0.1f))
                    .border(0.5.dp, AppColors.gold(), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "✨ AI automatically added new ingredients to your global library: ${newlyCreatedIngredients.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.gold()
                )
            }
        }

        if (ingredients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AppColors.gold().copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = AppColors.gold().copy(alpha = 0.5f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No ingredients yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.textSecondary()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Add ingredients from the library",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textTertiary()
                    )
                    Spacer(Modifier.height(16.dp))
                    PremiumTextButton(text = "Tap + to add", onClick = { showBottomSheet = true })
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(ingredients, key = { _, it -> it.globalIngredientId }) { _, ingredient ->
                    val globalIngredient = globalMap[ingredient.globalIngredientId]
                    RecipeIngredientCard(
                        ingredient = ingredient,
                        ingredientName = globalIngredient?.name ?: "Unknown",
                        onDelete = { onRemoveIngredient(ingredient.globalIngredientId) }
                    )
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    PremiumOutlinedButton(
                        text = "Add Another Ingredient",
                        onClick = { showBottomSheet = true },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = AppColors.cardBackground(),
            shape = CustomShapes.TopRounded,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            PickIngredientBottomSheet(
                globalIngredients = globalIngredients,
                alreadyAdded = ingredients.map { it.globalIngredientId }.toSet(),
                onSave = { recipeIngredient ->
                    onAddIngredient(recipeIngredient)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showBottomSheet = false
                    }
                },
                onCreateGlobal = { name, quantity, unit ->
                    onCreateGlobalIngredient(name, quantity, unit)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showBottomSheet = false
                    }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showBottomSheet = false
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Recipe Ingredient Card (shows name from global library)
// ─────────────────────────────────────────────────────────────

@Composable
private fun RecipeIngredientCard(
    ingredient: RecipeIngredient,
    ingredientName: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, AppColors.border())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Left gold accent line
                    drawLine(
                        color = Color(0xFFFFB800),
                        start = Offset(0f, size.height * 0.2f),
                        end = Offset(0f, size.height * 0.8f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredientName,
                    style = MaterialTheme.typography.titleSmall,
                    color = AppColors.textPrimary(),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${ingredient.quantity} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textTertiary()
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Remove",
                    tint = AppColors.error(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Pick Ingredient Bottom Sheet (from Global Library)
// ─────────────────────────────────────────────────────────────

@Composable
private fun PickIngredientBottomSheet(
    globalIngredients: List<GlobalIngredient>,
    alreadyAdded: Set<String>,
    onSave: (RecipeIngredient) -> Unit,
    onCreateGlobal: (String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIngredient by remember { mutableStateOf<GlobalIngredient?>(null) }
    var quantityText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("") }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var unitExpanded by remember { mutableStateOf(false) }

    val filtered = remember(searchQuery, globalIngredients, alreadyAdded) {
        globalIngredients
            .filter { it.ingredientId !in alreadyAdded }
            .filter { searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectedIngredient == null) {
            // ── Stage 1: Search & Pick ──
            PremiumSectionHeader(title = "Select Ingredient")

            SearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search ingredients..."
            )

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (globalIngredients.isEmpty()) "No ingredients in the library yet.\nType a name to add it globally."
                               else "No matching ingredients found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textTertiary(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filtered, key = { it.ingredientId }) { ingredient ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    selectedIngredient = ingredient
                                    selectedUnit = ingredient.defaultUnit
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.gold().copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Restaurant,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AppColors.gold().copy(alpha = 0.7f)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ingredient.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AppColors.textPrimary()
                                )
                                Text(
                                    text = ingredient.defaultUnit,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.textTertiary()
                                )
                            }
                        }
                    }
                }
            }

            if (searchQuery.isNotBlank() && filtered.none { it.name.equals(searchQuery, ignoreCase = true) }) {
                PremiumOutlinedButton(
                    text = "Add \"$searchQuery\" as new ingredient",
                    onClick = {
                        selectedIngredient = GlobalIngredient(ingredientId = "\$NEW\$", name = searchQuery, defaultUnit = "grams")
                        selectedUnit = "grams"
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            PremiumTextButton(text = "Cancel", onClick = onDismiss)
            Spacer(Modifier.height(16.dp))

        } else {
            // ── Stage 2: Enter Quantity ──
            val sel = selectedIngredient!!

            PremiumSectionHeader(title = "Add ${sel.name}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SousChefTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it; quantityError = null },
                    label = "Quantity *",
                    isError = quantityError != null,
                    errorMessage = quantityError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                Box(modifier = Modifier.weight(1f)) {
                    SousChefTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        label = "Unit",
                        trailingIcon = {
                            IconButton(onClick = { unitExpanded = !unitExpanded }) {
                                Icon(
                                    if (unitExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = "Select unit",
                                    tint = AppColors.textSecondary()
                                )
                            }
                        },
                        modifier = Modifier.clickable { unitExpanded = true }
                    )
                    DropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        UNIT_OPTIONS_RECIPE.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = { selectedUnit = unit; unitExpanded = false }
                            )
                        }
                    }
                }
            }

            PremiumDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumOutlinedButton(
                    text = "Back",
                    onClick = { selectedIngredient = null; quantityText = ""; quantityError = null },
                    modifier = Modifier.weight(1f)
                )
                PremiumButton(
                    text = "Add",
                    onClick = {
                        val qty = quantityText.toDoubleOrNull()
                        if (qty == null || qty <= 0) {
                            quantityError = "Enter a valid quantity"
                            return@PremiumButton
                        }
                        if (sel.ingredientId == "\$NEW\$") {
                            onCreateGlobal(sel.name, qty, selectedUnit)
                        } else {
                            onSave(
                                RecipeIngredient(
                                    globalIngredientId = sel.ingredientId,
                                    quantity = qty,
                                    unit = selectedUnit
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

