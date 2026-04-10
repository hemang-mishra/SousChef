package com.souschef.ui.screens.recipe.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.ingredient.GlobalIngredient
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.souschef.model.recipe.RecipeStep
import com.souschef.ui.components.PremiumButton
import com.souschef.ui.components.PremiumDivider
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.PremiumSectionHeader
import com.souschef.ui.components.PremiumTextButton
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.CustomShapes

// ─────────────────────────────────────────────────────────────
// Step 3: Cooking Steps (AI Generate / Manual — Optional)
// ─────────────────────────────────────────────────────────────

@Composable
internal fun Step3CookingSteps(
    uiState: CreateRecipeUiState,
    onAiDescriptionChange: (String) -> Unit,
    onGenerateSteps: () -> Unit,
    onCancelGeneration: () -> Unit,
    onEditStep: (Int, RecipeStep) -> Unit,
    onDeleteStep: (Int) -> Unit,
    onMoveStepUp: (Int) -> Unit,
    onMoveStepDown: (Int) -> Unit,
    onAddManualStep: () -> Unit,
    onRetryGeneration: () -> Unit,
    onStepMediaSelected: (Int, Uri, String) -> Unit,
    onRemoveStepMedia: (Int) -> Unit,
    onSkip: () -> Unit
) {
    val globalMap = remember(uiState.globalIngredients) {
        uiState.globalIngredients.associateBy { it.ingredientId }
    }
    AnimatedContent(
        targetState = uiState.stepsStage,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(200))
        },
        label = "steps_stage"
    ) { stage ->
        when (stage) {
            CreateRecipeUiState.StepsStage.INPUT -> StepsInputStage(
                recipeTitle = uiState.title,
                aiDescription = uiState.aiDescription,
                onAiDescriptionChange = onAiDescriptionChange,
                onGenerateSteps = onGenerateSteps,
                onAddManualStep = onAddManualStep,
                onSkip = onSkip
            )
            CreateRecipeUiState.StepsStage.LOADING -> StepsLoadingStage(
                onCancel = onCancelGeneration
            )
            CreateRecipeUiState.StepsStage.REVIEW -> StepsReviewStage(
                steps = uiState.steps,
                recipeIngredients = uiState.ingredients,
                globalIngredients = uiState.globalIngredients,
                onEditStep = onEditStep,
                onDeleteStep = onDeleteStep,
                onMoveStepUp = onMoveStepUp,
                onMoveStepDown = onMoveStepDown,
                onAddManualStep = onAddManualStep,
                onRetry = onRetryGeneration,
                onStepMediaSelected = onStepMediaSelected,
                onRemoveStepMedia = onRemoveStepMedia
            )
        }
    }
}

// ── Input Stage ──────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepsInputStage(
    recipeTitle: String,
    aiDescription: String,
    onAiDescriptionChange: (String) -> Unit,
    onGenerateSteps: () -> Unit,
    onAddManualStep: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Header
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
                    text = "Add Cooking Steps",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.textPrimary(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Optional — skip if you prefer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.textSecondary()
                )
            }
        }

        // AI generation card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CustomShapes.GlassCard,
            colors = CardDefaults.cardColors(containerColor = AppColors.glassBackground()),
            border = BorderStroke(0.5.dp, AppColors.glassBorder())
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "✨ Generate with AI",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.textPrimary(),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Describe how you'd cook \"$recipeTitle\" — the AI will break it into precise steps",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textTertiary()
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = aiDescription,
                    onValueChange = onAiDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = {
                        Text(
                            "e.g. Heat oil in a pan, sauté onions until golden, add tomatoes and spices, cook the gravy for 15 minutes…",
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

                Spacer(Modifier.height(16.dp))

                PremiumButton(
                    text = "✨ Generate Recipe with AI",
                    onClick = onGenerateSteps,
                    enabled = aiDescription.isNotBlank()
                )
            }
        }

        PremiumDivider()

        // Manual step button
        PremiumOutlinedButton(
            text = "➕ Add Steps Manually",
            onClick = onAddManualStep
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ── Loading Stage ────────────────────────────────────────────

@Composable
private fun StepsLoadingStage(onCancel: () -> Unit) {
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

// ── Review Stage ─────────────────────────────────────────────

@Composable
private fun StepsReviewStage(
    steps: List<RecipeStep>,
    recipeIngredients: List<RecipeIngredient>,
    globalIngredients: List<GlobalIngredient>,
    onEditStep: (Int, RecipeStep) -> Unit,
    onDeleteStep: (Int) -> Unit,
    onMoveStepUp: (Int) -> Unit,
    onMoveStepDown: (Int) -> Unit,
    onAddManualStep: () -> Unit,
    onRetry: () -> Unit,
    onStepMediaSelected: (Int, Uri, String) -> Unit,
    onRemoveStepMedia: (Int) -> Unit
) {
    val globalMap = remember(globalIngredients) {
        globalIngredients.associateBy { it.ingredientId }
    }
    // Build list of (id, name) pairs for the ingredient picker
    val ingredientOptions = remember(recipeIngredients, globalMap) {
        recipeIngredients.mapNotNull { ri ->
            globalMap[ri.globalIngredientId]?.let { gi -> gi.ingredientId to gi.name }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                        text = "Cooking Steps",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.textPrimary(),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${steps.size} step${if (steps.size != 1) "s" else ""}",
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
                ingredientOptions = ingredientOptions,
                onEditStep = { updatedStep -> onEditStep(index, updatedStep) },
                onDeleteStep = { onDeleteStep(index) },
                onMoveUp = { onMoveStepUp(index) },
                onMoveDown = { onMoveStepDown(index) },
                onMediaSelected = { uri, type -> onStepMediaSelected(index, uri, type) },
                onRemoveMedia = { onRemoveStepMedia(index) }
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
}

// ── Editable Step Card ───────────────────────────────────────

@Composable
private fun EditableStepCard(
    step: RecipeStep,
    index: Int,
    totalSteps: Int,
    ingredientOptions: List<Pair<String, String>>, // (globalIngredientId, name)
    onEditStep: (RecipeStep) -> Unit,
    onDeleteStep: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMediaSelected: (Uri, String) -> Unit,
    onRemoveMedia: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBackground()),
        border = BorderStroke(0.5.dp, AppColors.border())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: step badge + type badge + reorder + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    StepTypePill(stepType = step.stepType)
                }

                Row {
                    IconButton(onClick = onMoveUp, enabled = index > 0) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move up",
                            tint = if (index > 0) AppColors.textSecondary()
                            else AppColors.textSecondary().copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onMoveDown, enabled = index < totalSteps - 1) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move down",
                            tint = if (index < totalSteps - 1) AppColors.textSecondary()
                            else AppColors.textSecondary().copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
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

            Spacer(Modifier.height(8.dp))

            // Step Type selector
            StepTypeSelector(
                currentType = step.stepType,
                onTypeChange = { newType ->
                    val updated = if (newType == "ACTION") {
                        step.copy(stepType = newType, ingredientId = null, quantityMultiplier = 1.0)
                    } else {
                        step.copy(stepType = newType)
                    }
                    onEditStep(updated)
                }
            )

            // Ingredient picker (for INGREDIENT and PREP types)
            if (step.stepType == "INGREDIENT" || step.stepType == "PREP") {
                Spacer(Modifier.height(8.dp))
                IngredientPicker(
                    selectedIngredientId = step.ingredientId,
                    options = ingredientOptions,
                    onIngredientSelected = { id ->
                        onEditStep(step.copy(ingredientId = id))
                    }
                )

                // Quantity multiplier (only for steps with an ingredient)
                if (step.ingredientId != null) {
                    Spacer(Modifier.height(8.dp))
                    QuantityMultiplierRow(
                        multiplier = step.quantityMultiplier,
                        onMultiplierChange = { onEditStep(step.copy(quantityMultiplier = it)) }
                    )
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
                        "Enter cooking instruction (no quantities)…",
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

            // Detail chips (collapsed summary)
            val hasDetails = step.flameLevel != null ||
                    step.timerSeconds != null ||
                    step.expectedVisualCue != null

            if (hasDetails && !expanded) {
                Spacer(Modifier.height(8.dp))
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

            // Expand/collapse
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

                    FlameLevelSelector(
                        currentLevel = step.flameLevel,
                        onLevelChange = { onEditStep(step.copy(flameLevel = it)) }
                    )

                    TimerField(
                        timerSeconds = step.timerSeconds,
                        onTimerChange = { onEditStep(step.copy(timerSeconds = it)) }
                    )

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

                    // Step Media
                    PremiumDivider()
                    Text(
                        text = "Step Media",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppColors.textSecondary(),
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!step.mediaUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            if (step.mediaType == "video") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(AppColors.cardBackground()),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Videocam,
                                        contentDescription = "Video",
                                        tint = AppColors.gold(),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Video attached",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AppColors.textTertiary(),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = step.mediaUrl,
                                    contentDescription = "Step image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            IconButton(
                                onClick = onRemoveMedia,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove media",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        StepMediaPickerButtons(onMediaSelected = onMediaSelected)
                    }
                }
            }
        }
    }
}

// ── Media Picker Buttons ─────────────────────────────────────

@Composable
private fun StepMediaPickerButtons(
    onMediaSelected: (Uri, String) -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onMediaSelected(it, "image") }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onMediaSelected(it, "video") }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(
            onClick = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.gold().copy(alpha = 0.08f))
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = AppColors.gold()
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Add Image",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.gold()
            )
        }

        TextButton(
            onClick = {
                videoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.gold().copy(alpha = 0.08f))
        ) {
            Icon(
                Icons.Default.Videocam,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = AppColors.gold()
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Add Video",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.gold()
            )
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

// ── Step Component Helpers ────────────────────────────────────

@Composable
private fun StepTypePill(stepType: String) {
    val (emoji, label) = when (stepType.uppercase()) {
        "INGREDIENT" -> "🥄" to "Ingredient"
        "PREP" -> "✂️" to "Prep"
        else -> "🔧" to "Action"
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.gold().copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, style = MaterialTheme.typography.labelSmall)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.gold(),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StepTypeSelector(
    currentType: String,
    onTypeChange: (String) -> Unit
) {
    val types = listOf("ACTION", "INGREDIENT", "PREP")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.cardBackground())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        types.forEach { type ->
            val isSelected = currentType.uppercase() == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) AppColors.gold() else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onTypeChange(type) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) AppColors.onGold() else AppColors.textSecondary(),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun IngredientPicker(
    selectedIngredientId: String?,
    options: List<Pair<String, String>>, // (id, name)
    onIngredientSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.find { it.first == selectedIngredientId }?.second ?: "Select Ingredient"

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            label = { Text("Ingredient") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.gold(),
                unfocusedBorderColor = AppColors.border(),
                disabledBorderColor = AppColors.border(),
                disabledTextColor = AppColors.textPrimary(),
                disabledLabelColor = AppColors.textSecondary(),
                disabledTrailingIconColor = AppColors.textSecondary()
            ),
            enabled = false // Use clickable Box to open menu instead of focusing field
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f).background(AppColors.cardBackground())
        ) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name, color = AppColors.textPrimary()) },
                    onClick = {
                        onIngredientSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun QuantityMultiplierRow(
    multiplier: Double,
    onMultiplierChange: (Double) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Portion used in this step",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.textSecondary()
            )
            Text(
                text = "${(multiplier * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.gold(),
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = multiplier.toFloat(),
            onValueChange = { onMultiplierChange((Math.round(it * 20.0) / 20.0)) }, // step by 0.05
            valueRange = 0.05f..1f,
            colors = SliderDefaults.colors(
                thumbColor = AppColors.gold(),
                activeTrackColor = AppColors.gold(),
                inactiveTrackColor = AppColors.gold().copy(alpha = 0.2f)
            )
        )
    }
}
