package com.souschef.ui.screens.recipe.create

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.ui.components.DietaryTag
import com.souschef.ui.components.IngredientRow
import com.souschef.ui.components.PremiumButton
import com.souschef.ui.components.PremiumDivider
import com.souschef.ui.components.PremiumDottedDivider
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.PremiumSectionHeader
import com.souschef.ui.components.PremiumTextButton
import com.souschef.ui.components.SearchField
import com.souschef.ui.components.SousChefFilterChip
import com.souschef.ui.components.SousChefTextField
import com.souschef.ui.components.StandardCard
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.CustomShapes
import com.souschef.ui.theme.SousChefTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// ── Predefined tags ─────────────────────────────────────────
val RECIPE_TAGS = listOf(
    "🌿 Vegetarian", "🌱 Vegan", "🌶 Spicy", "⚡ Quick",
    "🇮🇹 Italian", "🇮🇳 Indian", "🇲🇽 Mexican", "🇯🇵 Japanese",
    "🇫🇷 French", "🇹🇭 Thai", "🍰 Dessert", "🥗 Healthy",
    "🍖 BBQ", "🌾 Gluten-Free", "🥜 Nut-Free", "🧀 Dairy-Free"
)

val UNIT_OPTIONS_RECIPE = listOf("grams", "ml", "tsp", "tbsp", "cups", "pieces", "oz", "lbs", "kg", "L")

// ─────────────────────────────────────────────────────────────
// Stateful Screen (wires ViewModel)
// ─────────────────────────────────────────────────────────────

@Composable
fun CreateRecipeScreen(
    onBack: () -> Unit,
    onRecipeSaved: (String) -> Unit,
    viewModel: CreateRecipeViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.generalError) {
        uiState.generalError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSaved, uiState.savedRecipeId) {
        if (uiState.isSaved && uiState.savedRecipeId != null) {
            onRecipeSaved(uiState.savedRecipeId!!)
        }
    }

    CreateRecipeScreenLayout(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onNextStep = viewModel::onNextStep,
        onPreviousStep = viewModel::onPreviousStep,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onBaseServingSizeChange = viewModel::onBaseServingSizeChange,
        onUseMinServingChange = viewModel::onUseMinServingChange,
        onMinServingSizeChange = viewModel::onMinServingSizeChange,
        onUseMaxServingChange = viewModel::onUseMaxServingChange,
        onMaxServingSizeChange = viewModel::onMaxServingSizeChange,
        onToggleTag = viewModel::onToggleTag,
        onAddIngredient = viewModel::onAddIngredient,
        onRemoveIngredient = viewModel::onRemoveIngredient,
        onSave = viewModel::onSave
    )
}

// ─────────────────────────────────────────────────────────────
// Stateless Layout
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreenLayout(
    uiState: CreateRecipeUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBaseServingSizeChange: (Int) -> Unit,
    onUseMinServingChange: (Boolean) -> Unit,
    onMinServingSizeChange: (Int) -> Unit,
    onUseMaxServingChange: (Boolean) -> Unit,
    onMaxServingSizeChange: (Int) -> Unit,
    onToggleTag: (String) -> Unit,
    onAddIngredient: (RecipeIngredient) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onSave: (Boolean) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Recipe",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.textPrimary()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = if (uiState.canGoBack) onPreviousStep else onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.textPrimary()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Step indicator
            PremiumStepIndicator(
                currentStep = uiState.currentStep,
                labels = uiState.stepLabels,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            // Content — animated transition between steps
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "wizard_step",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    0 -> Step1Details(
                        uiState = uiState,
                        onTitleChange = onTitleChange,
                        onDescriptionChange = onDescriptionChange,
                        onBaseServingSizeChange = onBaseServingSizeChange,
                        onUseMinServingChange = onUseMinServingChange,
                        onMinServingSizeChange = onMinServingSizeChange,
                        onUseMaxServingChange = onUseMaxServingChange,
                        onMaxServingSizeChange = onMaxServingSizeChange,
                        onToggleTag = onToggleTag
                    )
                    1 -> Step2Ingredients(
                        ingredients = uiState.ingredients,
                        globalIngredients = uiState.globalIngredients,
                        ingredientError = uiState.ingredientError,
                        onAddIngredient = onAddIngredient,
                        onRemoveIngredient = onRemoveIngredient
                    )
                    2 -> Step3Review(
                        uiState = uiState,
                        onSave = onSave
                    )
                }
            }

            // Bottom navigation buttons
            if (uiState.canGoBack || uiState.canGoNext) {
                PremiumDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.canGoBack) {
                        PremiumOutlinedButton(
                            text = "Back",
                            onClick = onPreviousStep,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (uiState.canGoNext) {
                        PremiumButton(
                            text = "Next",
                            onClick = onNextStep,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Premium Step Indicator
// ─────────────────────────────────────────────────────────────

@Composable
private fun PremiumStepIndicator(
    currentStep: Int,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Dots + connecting lines
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            labels.forEachIndexed { index, _ ->
                val isCompleted = index < currentStep
                val isCurrent = index == currentStep

                val dotColor by animateColorAsState(
                    targetValue = when {
                        isCompleted -> AppColors.gold()
                        isCurrent -> AppColors.gold()
                        else -> AppColors.border()
                    },
                    animationSpec = tween(300),
                    label = "stepDotColor"
                )

                // Step dot
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 36.dp else 28.dp)
                        .clip(CircleShape)
                        .then(
                            if (isCurrent) Modifier
                                .border(2.dp, AppColors.gold(), CircleShape)
                                .background(AppColors.gold().copy(alpha = 0.12f))
                            else if (isCompleted) Modifier.background(dotColor)
                            else Modifier
                                .border(1.5.dp, AppColors.border(), CircleShape)
                                .background(Color.Transparent)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Completed",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) AppColors.gold() else AppColors.textTertiary()
                        )
                    }
                }

                // Connecting line (between dots)
                if (index < labels.lastIndex) {
                    val lineColor by animateColorAsState(
                        targetValue = if (index < currentStep) AppColors.gold() else AppColors.border(),
                        animationSpec = tween(300),
                        label = "stepLineColor"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 8.dp)
                            .background(lineColor, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        index < currentStep -> AppColors.gold()
                        index == currentStep -> AppColors.textPrimary()
                        else -> AppColors.textTertiary()
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step 1: Recipe Details
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step1Details(
    uiState: CreateRecipeUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBaseServingSizeChange: (Int) -> Unit,
    onUseMinServingChange: (Boolean) -> Unit,
    onMinServingSizeChange: (Int) -> Unit,
    onUseMaxServingChange: (Boolean) -> Unit,
    onMaxServingSizeChange: (Int) -> Unit,
    onToggleTag: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        SousChefTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = "Recipe Title *",
            isError = uiState.titleError != null,
            errorMessage = uiState.titleError
        )

        SousChefTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = "Description",
            singleLine = false,
            minLines = 3,
            maxLines = 5
        )

        PremiumDivider()

        // Serving Size
        PremiumSectionHeader(title = "Serving Size")
        ServingStepper(
            label = "Base servings",
            value = uiState.baseServingSize,
            onValueChange = onBaseServingSizeChange,
            min = 1,
            max = 50
        )

        // Min/Max restrictions
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Minimum servings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textPrimary()
                )
                Text(
                    "Set a lower limit for this recipe",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
            }
            Switch(
                checked = uiState.useMinServing,
                onCheckedChange = onUseMinServingChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.gold(),
                    checkedTrackColor = AppColors.gold().copy(alpha = 0.3f),
                    uncheckedThumbColor = AppColors.textTertiary(),
                    uncheckedTrackColor = AppColors.border()
                )
            )
        }
        if (uiState.useMinServing) {
            ServingStepper(
                label = "Min",
                value = uiState.minServingSize ?: 1,
                onValueChange = onMinServingSizeChange,
                min = 1,
                max = uiState.baseServingSize
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Maximum servings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textPrimary()
                )
                Text(
                    "Set an upper limit for this recipe",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
            }
            Switch(
                checked = uiState.useMaxServing,
                onCheckedChange = onUseMaxServingChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.gold(),
                    checkedTrackColor = AppColors.gold().copy(alpha = 0.3f),
                    uncheckedThumbColor = AppColors.textTertiary(),
                    uncheckedTrackColor = AppColors.border()
                )
            )
        }
        if (uiState.useMaxServing) {
            ServingStepper(
                label = "Max",
                value = uiState.maxServingSize ?: (uiState.baseServingSize * 2),
                onValueChange = onMaxServingSizeChange,
                min = uiState.baseServingSize,
                max = 100
            )
        }

        PremiumDivider()

        // Tags
        PremiumSectionHeader(title = "Tags")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RECIPE_TAGS.forEach { tag ->
                SousChefFilterChip(
                    label = tag,
                    selected = tag in uiState.selectedTags,
                    onSelectedChange = { onToggleTag(tag) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Serving Stepper
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServingStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                enabled = value > min
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(
                            1.dp,
                            if (value > min) AppColors.gold() else AppColors.border(),
                            CircleShape
                        )
                        .background(
                            if (value > min) AppColors.gold().copy(alpha = 0.08f) else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "\u2212",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (value > min) AppColors.gold() else AppColors.textTertiary()
                    )
                }
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary(),
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                enabled = value < max
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(
                            1.dp,
                            if (value < max) AppColors.gold() else AppColors.border(),
                            CircleShape
                        )
                        .background(
                            if (value < max) AppColors.gold().copy(alpha = 0.08f) else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (value < max) AppColors.gold() else AppColors.textTertiary()
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step 2: Ingredients
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2Ingredients(
    ingredients: List<RecipeIngredient>,
    globalIngredients: List<GlobalIngredient>,
    ingredientError: String?,
    onAddIngredient: (RecipeIngredient) -> Unit,
    onRemoveIngredient: (String) -> Unit
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
                        text = if (globalIngredients.isEmpty()) "No ingredients in the library yet.\nAdd some from the Ingredient Library screen."
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
                        onSave(
                            RecipeIngredient(
                                globalIngredientId = sel.ingredientId,
                                quantity = qty,
                                unit = selectedUnit
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step 3: Review & Save
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3Review(
    uiState: CreateRecipeUiState,
    onSave: (Boolean) -> Unit
) {
    val globalMap = remember(uiState.globalIngredients) {
        uiState.globalIngredients.associateBy { it.ingredientId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Summary card — glass effect
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CustomShapes.GlassCard,
            colors = CardDefaults.cardColors(containerColor = AppColors.glassBackground()),
            border = BorderStroke(0.5.dp, AppColors.glassBorder())
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = uiState.title.ifBlank { "Untitled Recipe" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.textPrimary(),
                    fontWeight = FontWeight.Bold
                )
                if (uiState.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textSecondary(),
                        maxLines = 3
                    )
                }
                Spacer(Modifier.height(16.dp))
                PremiumDottedDivider()
                Spacer(Modifier.height(16.dp))

                ReviewRow("Base Servings", uiState.baseServingSize.toString())
                uiState.minServingSize?.let { ReviewRow("Min Servings", it.toString()) }
                uiState.maxServingSize?.let { ReviewRow("Max Servings", it.toString()) }
                ReviewRow("Ingredients", uiState.ingredients.size.toString())

                if (uiState.selectedTags.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.selectedTags.forEach { tag ->
                            DietaryTag(text = tag, color = tagColor(tag))
                        }
                    }
                }
            }
        }

        // Ingredient list preview
        if (uiState.ingredients.isNotEmpty()) {
            PremiumSectionHeader(title = "Ingredients")
            StandardCard {
                Column {
                    uiState.ingredients.forEachIndexed { index, ingredient ->
                        val name = globalMap[ingredient.globalIngredientId]?.name ?: "Unknown"
                        IngredientRow(
                            name = name,
                            quantity = ingredient.quantity.toString(),
                            unit = ingredient.unit
                        )
                        if (index < uiState.ingredients.lastIndex) {
                            PremiumDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Save buttons
        PremiumOutlinedButton(
            text = "Save for Later",
            onClick = { onSave(false) },
            isLoading = uiState.isLoading
        )

        PremiumButton(
            text = "Publish Recipe",
            onClick = { onSave(true) },
            isLoading = uiState.isLoading
        )

        Spacer(Modifier.height(32.dp))
    }
}

/** Maps recipe tags to appropriate accent colors */
@Composable
private fun tagColor(tag: String): Color = when {
    tag.contains("Vegetarian") || tag.contains("Vegan") || tag.contains("Healthy") -> AppColors.accentGreen()
    tag.contains("Spicy") || tag.contains("BBQ") -> AppColors.accentTerracotta()
    tag.contains("Italian") || tag.contains("French") -> AppColors.accentBurgundy()
    tag.contains("Indian") || tag.contains("Thai") || tag.contains("Mexican") || tag.contains("Japanese") -> AppColors.accentTeal()
    tag.contains("Quick") -> AppColors.gold()
    tag.contains("Gluten-Free") || tag.contains("Nut-Free") || tag.contains("Dairy-Free") -> AppColors.accentOlive()
    tag.contains("Dessert") -> AppColors.accentTerracotta()
    else -> AppColors.textTertiary()
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textTertiary())
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary())
    }
}

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateRecipeStep1Preview() {
    SousChefTheme {
        CreateRecipeScreenLayout(
            uiState = CreateRecipeUiState(currentStep = 0, title = "Truffle Risotto"),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onNextStep = {}, onPreviousStep = {},
            onTitleChange = {}, onDescriptionChange = {},
            onBaseServingSizeChange = {}, onUseMinServingChange = {},
            onMinServingSizeChange = {}, onUseMaxServingChange = {},
            onMaxServingSizeChange = {}, onToggleTag = {},
            onAddIngredient = {}, onRemoveIngredient = {}, onSave = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateRecipeStep2Preview() {
    SousChefTheme {
        CreateRecipeScreenLayout(
            uiState = CreateRecipeUiState(
                currentStep = 1,
                ingredients = listOf(
                    RecipeIngredient("g1", 200.0, "grams"),
                    RecipeIngredient("g2", 80.0, "grams"),
                    RecipeIngredient("g3", 2.0, "tbsp")
                ),
                globalIngredients = listOf(
                    GlobalIngredient(ingredientId = "g1", name = "Arborio Rice", defaultUnit = "grams"),
                    GlobalIngredient(ingredientId = "g2", name = "Parmesan Cheese", defaultUnit = "grams"),
                    GlobalIngredient(ingredientId = "g3", name = "White Truffle Oil", defaultUnit = "tbsp")
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onNextStep = {}, onPreviousStep = {},
            onTitleChange = {}, onDescriptionChange = {},
            onBaseServingSizeChange = {}, onUseMinServingChange = {},
            onMinServingSizeChange = {}, onUseMaxServingChange = {},
            onMaxServingSizeChange = {}, onToggleTag = {},
            onAddIngredient = {}, onRemoveIngredient = {}, onSave = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CreateRecipeStep3Preview() {
    SousChefTheme {
        CreateRecipeScreenLayout(
            uiState = CreateRecipeUiState(
                currentStep = 2,
                title = "Truffle Risotto alla Milanese",
                description = "A rich and creamy Italian risotto with truffle oil and Parmesan.",
                baseServingSize = 4,
                selectedTags = listOf("🇮🇹 Italian", "🌿 Vegetarian"),
                ingredients = listOf(
                    RecipeIngredient("g1", 200.0, "grams"),
                    RecipeIngredient("g2", 80.0, "grams"),
                    RecipeIngredient("g3", 2.0, "tbsp")
                ),
                globalIngredients = listOf(
                    GlobalIngredient(ingredientId = "g1", name = "Arborio Rice", defaultUnit = "grams"),
                    GlobalIngredient(ingredientId = "g2", name = "Parmesan Cheese", defaultUnit = "grams"),
                    GlobalIngredient(ingredientId = "g3", name = "White Truffle Oil", defaultUnit = "tbsp")
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onNextStep = {}, onPreviousStep = {},
            onTitleChange = {}, onDescriptionChange = {},
            onBaseServingSizeChange = {}, onUseMinServingChange = {},
            onMinServingSizeChange = {}, onUseMaxServingChange = {},
            onMaxServingSizeChange = {}, onToggleTag = {},
            onAddIngredient = {}, onRemoveIngredient = {}, onSave = {}
        )
    }
}

