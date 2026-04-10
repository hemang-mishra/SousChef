package com.souschef.ui.screens.recipe.aigeneration

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.recipe.RecipeStep
import com.souschef.ui.components.FullScreenLoader
import com.souschef.ui.components.PremiumButton
import com.souschef.ui.components.PremiumDivider
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.PremiumSectionHeader
import com.souschef.ui.components.PremiumTextButton
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.CustomShapes
import com.souschef.ui.theme.SousChefTheme

/**
 * Stateful composable — wires ViewModel.
 * Entry point from navigation.
 */
@Composable
fun AiStepGenerationScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AiStepGenerationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show errors via Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Navigate after successful save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaved()
        }
    }

    if (uiState.isRecipeLoading) {
        FullScreenLoader(message = "Loading recipe…")
        return
    }

    AiStepGenerationScreenLayout(
        uiState = uiState,
        onBack = onBack,
        onDescriptionChange = viewModel::onDescriptionChange,
        onGenerateSteps = viewModel::onGenerateSteps,
        onCancelGeneration = viewModel::onCancelGeneration,
        onEditStep = viewModel::onEditStep,
        onDeleteStep = viewModel::onDeleteStep,
        onMoveStepUp = viewModel::onMoveStepUp,
        onMoveStepDown = viewModel::onMoveStepDown,
        onAddManualStep = viewModel::onAddManualStep,
        onSaveSteps = viewModel::onSaveSteps,
        onRetry = viewModel::onRetry,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Stateless layout composable — purely presentational.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStepGenerationScreenLayout(
    uiState: AiStepGenerationUiState,
    onBack: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onGenerateSteps: () -> Unit,
    onCancelGeneration: () -> Unit,
    onEditStep: (Int, RecipeStep) -> Unit,
    onDeleteStep: (Int) -> Unit,
    onMoveStepUp: (Int) -> Unit,
    onMoveStepDown: (Int) -> Unit,
    onAddManualStep: () -> Unit,
    onSaveSteps: () -> Unit,
    onRetry: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AI Step Generator",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.textPrimary()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.textPrimary()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        AnimatedContent(
            targetState = uiState.stage,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
            },
            label = "stage_transition"
        ) { stage ->
            when (stage) {
                AiStepGenerationUiState.Stage.INPUT -> InputStage(
                    recipeTitle = uiState.recipeTitle,
                    description = uiState.recipeDescription,
                    ingredientChips = uiState.ingredientChips,
                    onDescriptionChange = onDescriptionChange,
                    onGenerateSteps = onGenerateSteps
                )
                AiStepGenerationUiState.Stage.LOADING -> LoadingStage(
                    onCancel = onCancelGeneration
                )
                AiStepGenerationUiState.Stage.REVIEW -> ReviewStage(
                    steps = uiState.generatedSteps,
                    isSaving = uiState.isSaving,
                    onEditStep = onEditStep,
                    onDeleteStep = onDeleteStep,
                    onMoveStepUp = onMoveStepUp,
                    onMoveStepDown = onMoveStepDown,
                    onAddManualStep = onAddManualStep,
                    onSaveSteps = onSaveSteps,
                    onRetry = onRetry
                )
            }
        }
    }
}

// ── Stage 1: Input ───────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InputStage(
    recipeTitle: String,
    description: String,
    ingredientChips: List<String>,
    onDescriptionChange: (String) -> Unit,
    onGenerateSteps: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Header with sparkle icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.gold().copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AppColors.gold(),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "Generate Cooking Steps",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.textPrimary(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "for \"$recipeTitle\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary()
                )
            }
        }

        // Description input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CustomShapes.GlassCard,
            colors = CardDefaults.cardColors(containerColor = AppColors.glassBackground()),
            border = BorderStroke(0.5.dp, AppColors.glassBorder())
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Describe Your Recipe",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.textPrimary(),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Write how you'd cook this dish — the AI will break it into precise steps",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = {
                        Text(
                            "e.g. Heat oil in a pan, sauté onions until golden, add tomatoes and spices, cook the gravy for 15 minutes, then add the marinated chicken pieces and simmer on low flame for 20 minutes…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textTertiary()
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.gold(),
                        unfocusedBorderColor = AppColors.border(),
                        focusedLabelColor = AppColors.gold(),
                        cursorColor = AppColors.gold()
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Ingredient chips
        if (ingredientChips.isNotEmpty()) {
            PremiumSectionHeader(title = "Ingredients")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ingredientChips.forEach { name ->
                    IngredientChip(name = name)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Generate button
        PremiumButton(
            text = "✨ Generate Steps with AI",
            onClick = onGenerateSteps,
            enabled = description.isNotBlank()
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun IngredientChip(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.gold().copy(alpha = 0.1f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.gold()
        )
    }
}

// ── Stage 2: Loading ─────────────────────────────────────────

@Composable
private fun LoadingStage(onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AppColors.gold(),
            modifier = Modifier
                .size(64.dp)
                .alpha(alpha)
        )

        Spacer(Modifier.height(24.dp))

        CircularProgressIndicator(
            color = AppColors.gold(),
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Chef AI is crafting your recipe…",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.textPrimary(),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.alpha(alpha)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Breaking down each step for precise cooking",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textTertiary(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        PremiumTextButton(text = "Cancel", onClick = onCancel)
    }
}

// ── Stage 3: Review & Edit ───────────────────────────────────

@Composable
private fun ReviewStage(
    steps: List<RecipeStep>,
    isSaving: Boolean,
    onEditStep: (Int, RecipeStep) -> Unit,
    onDeleteStep: (Int) -> Unit,
    onMoveStepUp: (Int) -> Unit,
    onMoveStepDown: (Int) -> Unit,
    onAddManualStep: () -> Unit,
    onSaveSteps: () -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Steps list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Review Steps",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AppColors.textPrimary(),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${steps.size} steps generated",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.textTertiary()
                        )
                    }
                    PremiumTextButton(
                        text = "Regenerate",
                        onClick = onRetry,
                        color = AppColors.gold()
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            itemsIndexed(steps) { index, step ->
                EditableStepCard(
                    step = step,
                    index = index,
                    totalSteps = steps.size,
                    onEditStep = { updatedStep -> onEditStep(index, updatedStep) },
                    onDeleteStep = { onDeleteStep(index) },
                    onMoveUp = { onMoveStepUp(index) },
                    onMoveDown = { onMoveStepDown(index) }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                PremiumOutlinedButton(
                    text = "➕ Add Step Manually",
                    onClick = onAddManualStep
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Fixed bottom action bar
        PremiumDivider()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            PremiumButton(
                text = "Save Steps",
                onClick = onSaveSteps,
                isLoading = isSaving,
                enabled = steps.isNotEmpty()
            )
        }
    }
}

// ── Editable Step Card ───────────────────────────────────────

@Composable
private fun EditableStepCard(
    step: RecipeStep,
    index: Int,
    totalSteps: Int,
    onEditStep: (RecipeStep) -> Unit,
    onDeleteStep: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground()),
        border = BorderStroke(0.5.dp, AppColors.border())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: step badge + reorder + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Step number badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AppColors.gold()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step.stepNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppColors.onGold(),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    // Move up
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move up",
                            tint = if (index > 0) AppColors.textSecondary()
                            else AppColors.textSecondary().copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Move down
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalSteps - 1
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move down",
                            tint = if (index < totalSteps - 1) AppColors.textSecondary()
                            else AppColors.textSecondary().copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete
                    IconButton(onClick = onDeleteStep) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete step",
                            tint = AppColors.error(),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Instruction text (editable)
            OutlinedTextField(
                value = step.instructionText,
                onValueChange = { onEditStep(step.copy(instructionText = it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Enter cooking instruction…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textTertiary()
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.gold(),
                    unfocusedBorderColor = AppColors.border(),
                    cursorColor = AppColors.gold()
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                minLines = 2,
                maxLines = 6
            )

            // Show details summary (flame, timer, cue)
            val hasDetails = step.flameLevel != null ||
                    step.timerSeconds != null ||
                    step.expectedVisualCue != null

            if (hasDetails || expanded) {
                Spacer(Modifier.height(8.dp))

                if (!expanded) {
                    // Collapsed: show summary chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        step.flameLevel?.let { flame ->
                            DetailChip(
                                icon = Icons.Default.LocalFireDepartment,
                                text = flame.replaceFirstChar { it.uppercase() },
                                onClick = { expanded = true }
                            )
                        }
                        step.timerSeconds?.let { seconds ->
                            val mins = seconds / 60
                            val secs = seconds % 60
                            val timeText = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                            DetailChip(
                                icon = Icons.Default.Timer,
                                text = timeText,
                                onClick = { expanded = true }
                            )
                        }
                        step.expectedVisualCue?.let {
                            DetailChip(
                                icon = Icons.Default.Visibility,
                                text = "Visual cue",
                                onClick = { expanded = true }
                            )
                        }
                    }
                }
            }

            // Expand/collapse button
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    text = if (expanded) "Hide details" else "Edit details",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.gold()
                )
            }

            // Expanded detail editing
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumDivider()
                    Spacer(Modifier.height(4.dp))

                    // Flame level dropdown
                    FlameLevelSelector(
                        currentLevel = step.flameLevel,
                        onLevelChange = { onEditStep(step.copy(flameLevel = it)) }
                    )

                    // Timer field
                    TimerField(
                        timerSeconds = step.timerSeconds,
                        onTimerChange = { onEditStep(step.copy(timerSeconds = it)) }
                    )

                    // Visual cue field
                    OutlinedTextField(
                        value = step.expectedVisualCue ?: "",
                        onValueChange = {
                            onEditStep(step.copy(expectedVisualCue = it.ifBlank { null }))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Visual cue (optional)") },
                        placeholder = {
                            Text(
                                "e.g. golden brown edges",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.textTertiary(),
                                fontStyle = FontStyle.Italic
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.gold(),
                            unfocusedBorderColor = AppColors.border(),
                            focusedLabelColor = AppColors.gold(),
                            cursorColor = AppColors.gold()
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        singleLine = true
                    )
                }
            }
        }
    }
}

// ── Helper Components ────────────────────────────────────────

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.gold().copy(alpha = 0.08f))
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = AppColors.gold()
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.gold()
        )
    }
}

@Composable
private fun FlameLevelSelector(
    currentLevel: String?,
    onLevelChange: (String?) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    val options = listOf(null, "low", "medium", "high")
    val displayText = when (currentLevel) {
        "low" -> "🔥 Low"
        "medium" -> "🔥🔥 Medium"
        "high" -> "🔥🔥🔥 High"
        else -> "No flame"
    }

    Box {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Flame level") },
            readOnly = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.gold(),
                unfocusedBorderColor = AppColors.border(),
                focusedLabelColor = AppColors.gold(),
                cursorColor = AppColors.gold()
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { showDropdown = true }) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select flame level",
                        tint = AppColors.textSecondary()
                    )
                }
            }
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            options.forEach { level ->
                val label = when (level) {
                    "low" -> "🔥 Low"
                    "medium" -> "🔥🔥 Medium"
                    "high" -> "🔥🔥🔥 High"
                    else -> "No flame"
                }
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onLevelChange(level)
                        showDropdown = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TimerField(
    timerSeconds: Int?,
    onTimerChange: (Int?) -> Unit
) {
    val minutes = timerSeconds?.div(60) ?: 0
    val seconds = timerSeconds?.rem(60) ?: 0
    var minuteText by remember(timerSeconds) { mutableStateOf(if (minutes > 0) minutes.toString() else "") }
    var secondText by remember(timerSeconds) { mutableStateOf(if (seconds > 0) seconds.toString() else "") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = minuteText,
            onValueChange = { input ->
                minuteText = input.filter { it.isDigit() }.take(3)
                val mins = minuteText.toIntOrNull() ?: 0
                val secs = secondText.toIntOrNull() ?: 0
                val total = mins * 60 + secs
                onTimerChange(if (total > 0) total else null)
            },
            modifier = Modifier.weight(1f),
            label = { Text("Minutes") },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.gold(),
                unfocusedBorderColor = AppColors.border(),
                focusedLabelColor = AppColors.gold(),
                cursorColor = AppColors.gold()
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true
        )

        Text(":", style = MaterialTheme.typography.titleLarge, color = AppColors.textTertiary())

        OutlinedTextField(
            value = secondText,
            onValueChange = { input ->
                secondText = input.filter { it.isDigit() }.take(2)
                val mins = minuteText.toIntOrNull() ?: 0
                val secs = secondText.toIntOrNull() ?: 0
                val total = mins * 60 + secs
                onTimerChange(if (total > 0) total else null)
            },
            modifier = Modifier.weight(1f),
            label = { Text("Seconds") },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.gold(),
                unfocusedBorderColor = AppColors.border(),
                focusedLabelColor = AppColors.gold(),
                cursorColor = AppColors.gold()
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true
        )
    }
}

// ── Previews ───────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InputStagePreview() {
    SousChefTheme {
        AiStepGenerationScreenLayout(
            uiState = AiStepGenerationUiState(
                recipeTitle = "Butter Chicken",
                ingredientChips = listOf("Chicken", "Butter", "Tomatoes", "Cream", "Garam Masala", "Kasuri Methi"),
                stage = AiStepGenerationUiState.Stage.INPUT
            ),
            onBack = {},
            onDescriptionChange = {},
            onGenerateSteps = {},
            onCancelGeneration = {},
            onEditStep = { _, _ -> },
            onDeleteStep = {},
            onMoveStepUp = {},
            onMoveStepDown = {},
            onAddManualStep = {},
            onSaveSteps = {},
            onRetry = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingStagePreview() {
    SousChefTheme {
        AiStepGenerationScreenLayout(
            uiState = AiStepGenerationUiState(
                stage = AiStepGenerationUiState.Stage.LOADING,
                isLoading = true
            ),
            onBack = {},
            onDescriptionChange = {},
            onGenerateSteps = {},
            onCancelGeneration = {},
            onEditStep = { _, _ -> },
            onDeleteStep = {},
            onMoveStepUp = {},
            onMoveStepDown = {},
            onAddManualStep = {},
            onSaveSteps = {},
            onRetry = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReviewStagePreview() {
    SousChefTheme {
        AiStepGenerationScreenLayout(
            uiState = AiStepGenerationUiState(
                stage = AiStepGenerationUiState.Stage.REVIEW,
                generatedSteps = listOf(
                    RecipeStep(
                        stepNumber = 1,
                        instructionText = "Wash and pat dry the chicken thighs. Cut into bite-sized pieces.",
                        flameLevel = null,
                        timerSeconds = null
                    ),
                    RecipeStep(
                        stepNumber = 2,
                        instructionText = "In a large bowl, marinate the chicken with yogurt, turmeric, and red chili powder for at least 30 minutes.",
                        flameLevel = null,
                        timerSeconds = 1800,
                        expectedVisualCue = "Chicken evenly coated with marinade"
                    ),
                    RecipeStep(
                        stepNumber = 3,
                        instructionText = "Heat butter in a heavy-bottomed pan over medium flame.",
                        flameLevel = "medium",
                        timerSeconds = 120,
                        expectedVisualCue = "Butter melted and slightly foamy"
                    ),
                    RecipeStep(
                        stepNumber = 4,
                        instructionText = "Add the marinated chicken pieces and sear on high heat until golden brown.",
                        flameLevel = "high",
                        timerSeconds = 300,
                        expectedVisualCue = "Golden brown crust on chicken"
                    )
                )
            ),
            onBack = {},
            onDescriptionChange = {},
            onGenerateSteps = {},
            onCancelGeneration = {},
            onEditStep = { _, _ -> },
            onDeleteStep = {},
            onMoveStepUp = {},
            onMoveStepDown = {},
            onAddManualStep = {},
            onSaveSteps = {},
            onRetry = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
