package com.souschef.ui.screens.recipe.create

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.model.ingredient.GlobalIngredient
import com.souschef.model.recipe.RecipeIngredient
import com.souschef.model.recipe.RecipeTag
import com.souschef.ui.components.PremiumButton
import com.souschef.ui.components.PremiumDivider
import com.souschef.ui.components.PremiumOutlinedButton
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.SousChefTheme
import org.koin.compose.koinInject


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
        onCoverImageSelected = viewModel::onCoverImageSelected,
        onRemoveCoverImage = viewModel::onRemoveCoverImage,
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
    onToggleTag: (RecipeTag) -> Unit,
    onCoverImageSelected: (Uri) -> Unit,
    onRemoveCoverImage: () -> Unit,
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
                        onToggleTag = onToggleTag,
                        onCoverImageSelected = onCoverImageSelected,
                        onRemoveCoverImage = onRemoveCoverImage
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
                )
            }
        }
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
            onCoverImageSelected = {}, onRemoveCoverImage = {},
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
            onCoverImageSelected = {}, onRemoveCoverImage = {},
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
                selectedTags = listOf(RecipeTag.ITALIAN, RecipeTag.VEGETARIAN),
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
            onCoverImageSelected = {}, onRemoveCoverImage = {},
            onAddIngredient = {}, onRemoveIngredient = {}, onSave = {}
        )
    }
}
