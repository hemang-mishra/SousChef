package com.souschef.ui.screens.recipe.cooking

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.souschef.model.recipe.RecipeStep
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.permissions.BlePermissionHelper
import com.souschef.util.AppStrings
import com.souschef.ui.components.CookingModeShimmer
import com.souschef.ui.components.GlassCard
import com.souschef.ui.components.PremiumButton
import com.souschef.ui.components.PremiumDivider
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.components.PremiumSectionHeader
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.GoldVibrant
import com.souschef.ui.theme.SousChefTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────
// Stateful Screen (wires ViewModel + keep-screen-awake)
// ─────────────────────────────────────────────────────────────

@Composable
fun CookingModeScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    viewModel: CookingModeViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Permission launcher for BLE dispensing
    var pendingDispenseData by remember { mutableStateOf<DispenseData?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            pendingDispenseData?.let { data ->
                viewModel.dispenseIngredient(data.id, data.name, data.quantity, data.unit)
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Bluetooth permissions are required to dispense.")
            }
        }
        pendingDispenseData = null
    }

    // Keep screen awake while cooking
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    if (uiState.isFinished) {
        CookingCompleteScreen(
            onBackToRecipe = onFinished,
            onShare = { /* Phase 7+ */ },
            language = uiState.language
        )
        return
    }

    CookingModeScreenLayout(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onNextStep = {
            val isLast = uiState.currentStepIndex >= uiState.steps.size - 1
            if (isLast) viewModel.finishCooking() else viewModel.nextStep()
        },
        onPreviousStep = viewModel::previousStep,
        onGoToStep = viewModel::goToStep,
        onStartTimer = viewModel::startTimer,
        onPauseTimer = viewModel::pauseTimer,
        onResetTimer = viewModel::resetTimer,
        onDispense = { globalIngredientId, name, quantity, unit ->
            if (BlePermissionHelper.hasAllPermissions(context)) {
                viewModel.dispenseIngredient(globalIngredientId, name, quantity, unit)
            } else {
                pendingDispenseData = DispenseData(globalIngredientId, name, quantity, unit)
                permissionLauncher.launch(BlePermissionHelper.requiredPermissions)
            }
        },
        onLanguageChange = viewModel::setLanguage,
        onToggleNarration = viewModel::toggleNarration
    )

    // Listen for one-shot haptic events from the ViewModel and forward them
    // to the platform Vibrator so the user gets tactile feedback on step
    // transitions and timer completion.
    val view = androidx.compose.ui.platform.LocalView.current
    LaunchedEffect(Unit) {
        viewModel.hapticEvents.collect { event ->
            when (event) {
                HapticEvent.StepAdvanced -> view.performHapticFeedback(
                    android.view.HapticFeedbackConstants.CONFIRM
                )
                HapticEvent.TimerFinished -> {
                    com.souschef.util.Haptics.timerFinished(context)
                    com.souschef.util.Beeper.timerFinished()
                }
            }
        }
    }
}

private data class DispenseData(val id: String, val name: String, val quantity: Double, val unit: String)

// ─────────────────────────────────────────────────────────────
// Stateless Layout
// ─────────────────────────────────────────────────────────────


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeScreenLayout(
    uiState: CookingModeUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onGoToStep: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onDispense: (String, String, Double, String) -> Unit,
    onLanguageChange: (String) -> Unit = {},
    onToggleNarration: () -> Unit = {}
) {
    if (uiState.isLoading) {
        CookingModeShimmer()
        return
    }

    if (uiState.error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val currentStep = uiState.steps.getOrNull(uiState.currentStepIndex) ?: return
    val isLastStep = uiState.currentStepIndex >= uiState.steps.size - 1
    val isFirstStep = uiState.currentStepIndex == 0
    val lang = uiState.language
    val stepHeader = AppStrings.step(lang, uiState.currentStepIndex + 1, uiState.steps.size)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = 0.dp),
                title = {
                    Text(
                        text = stepHeader,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.textPrimary()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Exit cooking mode",
                            tint = AppColors.textPrimary()
                        )
                    }
                },
                actions = {
                    com.souschef.ui.components.LanguageToggle(
                        currentLanguage = uiState.language,
                        onLanguageChange = onLanguageChange,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    // Narration auto-plays when entering each step. This button
                    // acts as Stop while speaking, and as Replay when idle so
                    // the user can hear the current step again hands-free.
                    IconButton(onClick = onToggleNarration) {
                        Icon(
                            imageVector = if (uiState.isSpeaking)
                                androidx.compose.material.icons.Icons.AutoMirrored.Filled.VolumeOff
                            else
                                androidx.compose.material.icons.Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (uiState.isSpeaking) "Stop narration" else "Replay narration",
                            tint = AppColors.gold()
                        )
                    }
                    // Translation progress is the only thing that surfaces
                    // here now — retranslate has been moved to the recipe
                    // overview overflow menu so cooking mode stays focused.
                    if (uiState.isTranslating) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                            color = AppColors.gold(),
                            strokeWidth = 2.dp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Column {
                PremiumDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isFirstStep) {
                        PremiumOutlinedButton(
                            text = AppStrings.previousStep(lang),
                            onClick = onPreviousStep,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    PremiumButton(
                        text = if (isLastStep) AppStrings.finishCooking(lang) else AppStrings.nextStep(lang),
                        onClick = onNextStep,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress bar
            SegmentedProgressBar(
                totalSteps = uiState.steps.size,
                currentStep = uiState.currentStepIndex,
                onStepClick = onGoToStep,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Step content with slide animation
            AnimatedContent(
                targetState = uiState.currentStepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "step_transition",
                modifier = Modifier.weight(1f)
            ) { stepIndex ->
                val step = uiState.steps.getOrNull(stepIndex) ?: return@AnimatedContent
                StepContent(
                    step = step,
                    stepIndex = stepIndex,
                    stepIngredient = uiState.stepIngredientMap[stepIndex],
                    timerMillisRemaining = uiState.timerMillisRemaining,
                    isTimerRunning = uiState.isTimerRunning,
                    timerFinished = uiState.timerFinished,
                    loadedCompartmentIngredientIds = uiState.loadedCompartmentIngredientIds,
                    loadedCompartmentIngredientNames = uiState.loadedCompartmentIngredientNames,
                    dispensingIds = uiState.dispensingIngredientIds,
                    language = uiState.language,
                    onStartTimer = onStartTimer,
                    onPauseTimer = onPauseTimer,
                    onResetTimer = onResetTimer,
                    onDispense = onDispense
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Segmented Progress Bar
// ─────────────────────────────────────────────────────────────

@Composable
private fun SegmentedProgressBar(
    totalSteps: Int,
    currentStep: Int,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isCompleted = index < currentStep
            val isCurrent = index == currentStep

            val animatedHeight by animateFloatAsState(
                targetValue = if (isCurrent) 6f else 4f,
                animationSpec = tween(300),
                label = "segmentHeight"
            )

            val segmentColor = when {
                isCompleted -> AppColors.gold()
                isCurrent -> AppColors.gold()
                else -> AppColors.border()
            }

            val alpha by animateFloatAsState(
                targetValue = when {
                    isCompleted -> 1f
                    isCurrent -> 1f
                    else -> 0.5f
                },
                animationSpec = tween(300),
                label = "segmentAlpha"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .alpha(alpha)
                    .background(segmentColor)
                    .clickable { onStepClick(index) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step Content Card
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepContent(
    step: RecipeStep,
    stepIndex: Int,
    stepIngredient: ResolvedIngredient?,
    timerMillisRemaining: Long,
    isTimerRunning: Boolean,
    timerFinished: Boolean,
    loadedCompartmentIngredientIds: Set<String>,
    loadedCompartmentIngredientNames: Set<String>,
    dispensingIds: Set<String>,
    language: String = com.souschef.model.recipe.SupportedLanguages.ENGLISH,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onDispense: (String, String, Double, String) -> Unit
) {
    val hasTimer = step.timerSeconds != null && step.timerSeconds > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Step number + type pill — kept tiny and above everything so the
        // user always knows where they are without it stealing space.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StepNumberBadge(stepNumber = stepIndex + 1)
            StepTypeBadge(stepType = step.stepType)
        }


        // ── Instruction zone ───────────────────────────────────────────
        // The long-form copy lives below. The user can scroll into this if
        // they want to re-read it; otherwise they hear it via narration
        // and operate the timer / dispense from above-the-fold.
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = step.instructionIn(language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3f
                )

                step.flameLevel?.takeIf { it.isNotBlank() }?.let { canonical ->
                    FlameLevelIndicator(
                        level = canonical,
                        displayLabel = step.flameLevelIn(language)
                    )
                }

                step.expectedVisualCueIn(language)?.takeIf { it.isNotBlank() }?.let { cue ->
                    VisualCueRow(cue = cue, language = language)
                }

                if (!step.mediaUrl.isNullOrBlank()) {
                    MediaSection(mediaUrl = step.mediaUrl, mediaType = step.mediaType)
                }
            }
        }

        // ── Action zone ────────────────────────────────────────────────
        // Timer + ingredient/dispense are the *interactive* parts of the
        // screen and need to be reachable without scrolling. We render
        // them BEFORE the long instruction card so they always sit above
        // the fold even when the recipe instruction wraps to many lines.
        if (hasTimer) {
            TimerSection(
                timerMillisRemaining = timerMillisRemaining,
                isTimerRunning = isTimerRunning,
                timerFinished = timerFinished,
                language = language,
                onStart = onStartTimer,
                onPause = onPauseTimer,
                onReset = onResetTimer
            )
        }

        if (stepIngredient != null) {
            val isLoadedById = loadedCompartmentIngredientIds.contains(stepIngredient.globalIngredientId)
            val isLoadedByName = loadedCompartmentIngredientNames.any {
                stepIngredient.name.lowercase().contains(it) || it.contains(stepIngredient.name.lowercase())
            }
            val isLoaded = isLoadedById || isLoadedByName

            val debugText = StringBuilder().apply {
                append("ID: ${stepIngredient.globalIngredientId}\n")
                append("Name: ${stepIngredient.name}\n")
                append("isDispensable: ${stepIngredient.isDispensable}\n")
                append("isLoadedById: $isLoadedById\n")
                append("isLoadedByName: $isLoadedByName\n")
                append("Loaded Names Available: $loadedCompartmentIngredientNames")
            }.toString()

            StepIngredientCard(
                ingredient = stepIngredient,
                isDispensing = dispensingIds.contains(stepIngredient.globalIngredientId),
                isLoaded = isLoaded,
                debugInfo = debugText,
                language = language,
                onDispense = {
                    onDispense(
                        stepIngredient.globalIngredientId,
                        stepIngredient.name,
                        stepIngredient.quantity,
                        stepIngredient.unit
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Step Number Badge
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepNumberBadge(stepNumber: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AppColors.gold(),
                        AppColors.goldSecondary()
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stepNumber.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.onGold()
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Flame Level Indicator
// ─────────────────────────────────────────────────────────────

@Composable
private fun FlameLevelIndicator(level: String, displayLabel: String? = null) {
    val flameCount = when (level.lowercase()) {
        "low" -> 1
        "medium" -> 2
        "high" -> 3
        else -> 0
    }
    val label = (displayLabel ?: level).replaceFirstChar { it.uppercase() }

    if (flameCount == 0) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            Text(
                text = "🔥",
                modifier = Modifier.alpha(if (index < flameCount) 1f else 0.2f)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.textTertiary()
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Visual Cue
// ─────────────────────────────────────────────────────────────

@Composable
private fun VisualCueRow(
    cue: String,
    language: String = com.souschef.model.recipe.SupportedLanguages.ENGLISH
) {
    val prefix = AppStrings.lookFor(language)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "👁 ",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "$prefix$cue",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = AppColors.textSecondary()
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Media Section
// ─────────────────────────────────────────────────────────────

@Composable
private fun MediaSection(mediaUrl: String, mediaType: String? = null) {
    val isVideo = mediaType == "video" ||
            mediaUrl.contains(".mp4", ignoreCase = true) ||
            mediaUrl.contains("video", ignoreCase = true)

    if (isVideo) {
        VideoPlayerSection(videoUrl = mediaUrl)
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = mediaUrl,
                contentDescription = "Step media",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Lightweight video player using Android's VideoView wrapped in AndroidView.
 * Loops automatically and provides play/pause + mute controls.
 */
@Composable
private fun VideoPlayerSection(videoUrl: String) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val context = LocalContext.current

            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    android.widget.VideoView(ctx).apply {
                        setVideoURI(android.net.Uri.parse(videoUrl))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            mp.setVolume(0f, 0f) // Muted by default
                        }
                    }
                },
                update = { videoView ->
                    if (isPlaying && !videoView.isPlaying) {
                        videoView.start()
                    } else if (!isPlaying && videoView.isPlaying) {
                        videoView.pause()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Play/Pause overlay
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { isPlaying = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = "Play video",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // Tap to pause
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { isPlaying = false }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Step Type Badge
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepTypeBadge(stepType: String) {
    val (emoji, label) = when (stepType.uppercase()) {
        "INGREDIENT" -> "🥄" to "Add Ingredient"
        "PREP" -> "✂️" to "Preparation"
        else -> "🔧" to "Action"
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.gold().copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
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

// ─────────────────────────────────────────────────────────────
// Single Ingredient Card for Step
// ─────────────────────────────────────────────────────────────

@Composable
private fun StepIngredientCard(
    ingredient: ResolvedIngredient,
    isDispensing: Boolean,
    isLoaded: Boolean,
    debugInfo: String = "",
    language: String = com.souschef.model.recipe.SupportedLanguages.ENGLISH,
    onDispense: () -> Unit
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PremiumSectionHeader(title = AppStrings.ingredient(language))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (ingredient.imageUrl != null) {
                        AsyncImage(
                            model = ingredient.imageUrl,
                            contentDescription = ingredient.name,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Text(
                            text = ingredient.nameIn(language),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${ingredient.quantity.toOneDecimalString()} ${ingredient.unitIn(language)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.gold()
                        )
                    }
                }

                // Dispense button logic: if it's explicitly marked dispensable, or if it's currently loaded in a compartment
                if (ingredient.isDispensable || isLoaded) {
                    Spacer(modifier = Modifier.width(12.dp))
                    androidx.compose.material3.Button(
                        onClick = onDispense,
                        enabled = !isDispensing && isLoaded,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (isLoaded) AppColors.gold().copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                            contentColor = if (isLoaded) AppColors.gold() else Color.Gray,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.1f),
                            disabledContentColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp, vertical = 8.dp
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isDispensing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AppColors.gold(),
                                strokeWidth = 2.dp
                            )
                        } else {
                            if (!isLoaded) {
                                Text(
                                    text = AppStrings.notLoaded(language),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Science,
                                    contentDescription = "Auto-dispense",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.dispense(language),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Timer Section
// ─────────────────────────────────────────────────────────────

@Composable
private fun TimerSection(
    timerMillisRemaining: Long,
    isTimerRunning: Boolean,
    timerFinished: Boolean,
    language: String = com.souschef.model.recipe.SupportedLanguages.ENGLISH,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = AppStrings.timer(language),
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.textSecondary()
            )

            // Countdown display with pulse animation when finished
            val pulseScale = if (timerFinished) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "timerPulse"
                )
                scale
            } else {
                1f
            }

            val timerColor = when {
                timerFinished -> AppColors.success()
                isTimerRunning -> AppColors.gold()
                else -> AppColors.textPrimary()
            }

            Text(
                text = timerMillisRemaining.formatTimerDisplay(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = timerColor,
                modifier = Modifier.scale(pulseScale)
            )

            if (timerFinished) {
                Text(
                    text = AppStrings.timesUp(language),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.success(),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!timerFinished) {
                    // Start / Pause
                    TimerControlButton(
                        icon = if (isTimerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        label = if (isTimerRunning) AppStrings.timerPause(language) else AppStrings.timerStart(language),
                        isPrimary = true,
                        onClick = if (isTimerRunning) onPause else onStart
                    )
                }

                // Reset
                TimerControlButton(
                    icon = Icons.Outlined.Refresh,
                    label = AppStrings.timerReset(language),
                    isPrimary = false,
                    onClick = onReset
                )
            }
        }
    }
}

@Composable
private fun TimerControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isPrimary) AppColors.gold()
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) AppColors.onGold()
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.textTertiary()
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Cooking Complete Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun CookingCompleteScreen(
    onBackToRecipe: () -> Unit,
    onShare: () -> Unit,
    language: String = com.souschef.model.recipe.SupportedLanguages.ENGLISH
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.heroBackground(),
                        AppColors.heroBackgroundAlt()
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated checkmark
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = tween(600)) + fadeIn(animationSpec = tween(600))
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    AppColors.gold(),
                                    AppColors.goldSecondary()
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Complete",
                        tint = AppColors.onGold(),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                text = AppStrings.greatWork(language),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = AppStrings.dishReady(language),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            PremiumButton(
                text = AppStrings.backToRecipe(language),
                onClick = onBackToRecipe,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            PremiumOutlinedButton(
                text = AppStrings.shareRecipe(language),
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Utility
// ─────────────────────────────────────────────────────────────

private fun Long.formatTimerDisplay(): String {
    val totalSeconds = (this / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun Double.toOneDecimalString(): String =
    ((this * 10.0).roundToInt() / 10.0).toString()

// ─────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CookingModePreview() {
    SousChefTheme {
        CookingModeScreenLayout(
            uiState = CookingModeUiState(
                steps = listOf(
                    RecipeStep(
                        stepId = "s1",
                        stepNumber = 1,
                        instructionText = "Heat olive oil in a large pan over medium heat until the oil begins to shimmer.",
                        flameLevel = "medium",
                        expectedVisualCue = "Oil shimmering on surface",
                        timerSeconds = 120,
                        ingredientReferences = listOf("g1")
                    ),
                    RecipeStep(
                        stepId = "s2",
                        stepNumber = 2,
                        instructionText = "Add garlic and sauté until fragrant, about 1 minute.",
                        flameLevel = "low",
                        timerSeconds = 60,
                        ingredientReferences = listOf("g2")
                    ),
                    RecipeStep(
                        stepId = "s3",
                        stepNumber = 3,
                        instructionText = "Pour in the cream and stir gently. Season with salt and pepper.",
                        expectedVisualCue = "Sauce begins to thicken",
                        ingredientReferences = listOf("g3")
                    )
                ),
                adjustedIngredients = listOf(
                    ResolvedIngredient(globalIngredientId = "g1", name = "Olive Oil", quantity = 30.0, unit = "ml"),
                    ResolvedIngredient(globalIngredientId = "g2", name = "Garlic", quantity = 4.0, unit = "cloves"),
                    ResolvedIngredient(globalIngredientId = "g3", name = "Heavy Cream", quantity = 220.0, unit = "ml")
                ),
                currentStepIndex = 0,
                timerMillisRemaining = 120_000L,
                isTimerRunning = false,
                isLoading = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onNextStep = {},
            onPreviousStep = {},
            onGoToStep = {},
            onStartTimer = {},
            onPauseTimer = {},
            onResetTimer = {},
            onDispense = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CookingModeLastStepPreview() {
    SousChefTheme {
        CookingModeScreenLayout(
            uiState = CookingModeUiState(
                steps = listOf(
                    RecipeStep(stepId = "s1", stepNumber = 1, instructionText = "Step 1"),
                    RecipeStep(
                        stepId = "s2",
                        stepNumber = 2,
                        instructionText = "Plate the dish and garnish with fresh herbs. Serve immediately.",
                        flameLevel = null,
                        expectedVisualCue = "Golden brown edges on the pasta"
                    )
                ),
                adjustedIngredients = emptyList(),
                currentStepIndex = 1,
                isLoading = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onNextStep = {},
            onPreviousStep = {},
            onGoToStep = {},
            onStartTimer = {},
            onPauseTimer = {},
            onResetTimer = {},
            onDispense = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CookingCompletePreview() {
    SousChefTheme {
        CookingCompleteScreen(
            onBackToRecipe = {},
            onShare = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CookingModeLoadingPreview() {
    SousChefTheme {
        CookingModeScreenLayout(
            uiState = CookingModeUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onNextStep = {},
            onPreviousStep = {},
            onGoToStep = {},
            onStartTimer = {},
            onPauseTimer = {},
            onResetTimer = {},
            onDispense = { _, _, _, _ -> }
        )
    }
}
