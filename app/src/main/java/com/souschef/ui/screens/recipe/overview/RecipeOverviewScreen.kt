package com.souschef.ui.screens.recipe.overview

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.souschef.model.recipe.Recipe
import com.souschef.model.recipe.ResolvedIngredient
import com.souschef.ui.components.FullScreenLoader
import com.souschef.ui.components.GlassCard
import com.souschef.ui.components.PrimaryButton
import com.souschef.ui.components.SectionHeader
import com.souschef.ui.components.VerifiedChefBadge
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.SousChefTheme
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun RecipeOverviewScreen(
	onBack: () -> Unit,
	onStartCooking: (selectedServings: Int, spiceLevel: Float, saltLevel: Float, sweetnessLevel: Float) -> Unit,
	onEditRecipe: (String) -> Unit,
	viewModel: RecipeOverviewViewModel = koinInject()
) {
	val uiState by viewModel.uiState.collectAsState()

	LaunchedEffect(uiState.isDeleted) {
		if (uiState.isDeleted) {
			onBack()
		}
	}

	RecipeOverviewContent(
		uiState = uiState,
		onBack = onBack,
		onServingsChanged = viewModel::onServingsChanged,
		onSpiceLevelChanged = viewModel::onSpiceLevelChanged,
		onSaltLevelChanged = viewModel::onSaltLevelChanged,
		onSweetnessLevelChanged = viewModel::onSweetnessLevelChanged,
		onStartCooking = {
			onStartCooking(
				uiState.selectedServings,
				uiState.spiceLevel,
				uiState.saltLevel,
				uiState.sweetnessLevel
			)
		},
		onEditRecipe = { uiState.recipe?.let { onEditRecipe(it.recipeId) } },
		onDeleteRecipe = viewModel::onDeleteRecipe
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeOverviewContent(
	uiState: RecipeOverviewUiState,
	onBack: () -> Unit,
	onServingsChanged: (Int) -> Unit,
	onSpiceLevelChanged: (Float) -> Unit,
	onSaltLevelChanged: (Float) -> Unit,
	onSweetnessLevelChanged: (Float) -> Unit,
	onStartCooking: () -> Unit,
	onEditRecipe: () -> Unit,
	onDeleteRecipe: () -> Unit
) {
	val recipe = uiState.recipe

	if (uiState.isLoading) {
		FullScreenLoader(message = "Preparing your recipe...")
		return
	}

	if (recipe == null) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = uiState.error ?: "Recipe not available",
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.bodyLarge
			)
		}
		return
	}

	val minServings = recipe.minServingSize ?: 1
	val maxServings = recipe.maxServingSize ?: (recipe.baseServingSize * 4).coerceAtLeast(minServings)

	var showMenu by remember { mutableStateOf(false) }
	var showDeleteDialog by remember { mutableStateOf(false) }
	val showOptions = uiState.currentUserId != null && uiState.currentUserId == recipe.creatorId

	if (showDeleteDialog) {
		AlertDialog(
			onDismissRequest = { showDeleteDialog = false },
			title = { Text("Delete Recipe") },
			text = { Text("Are you sure you want to delete this recipe? This action cannot be undone.") },
			confirmButton = {
				Button(
					onClick = {
						showDeleteDialog = false
						onDeleteRecipe()
					},
					colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
				) {
					Text("Delete")
				}
			},
			dismissButton = {
				TextButton(onClick = { showDeleteDialog = false }) {
					Text("Cancel")
				}
			}
		)
	}

	Scaffold(
		topBar = {
			TopAppBar(
				windowInsets = WindowInsets(top = 0.dp),
				title = { Text(text = "Recipe Overview") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
					}
				},
				actions = {
					if (showOptions) {
						IconButton(onClick = { showMenu = true }) {
							Icon(Icons.Default.MoreVert, contentDescription = "Options")
						}
						DropdownMenu(
							expanded = showMenu,
							onDismissRequest = { showMenu = false }
						) {
							DropdownMenuItem(
								text = { Text("Edit Recipe") },
								onClick = {
									showMenu = false
									onEditRecipe()
								}
							)
							DropdownMenuItem(
								text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
								onClick = {
									showMenu = false
									showDeleteDialog = true
								}
							)
						}
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
			)
		},
		bottomBar = {
			PrimaryButton(
				text = "Start Cooking",
				onClick = onStartCooking,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 20.dp, vertical = 14.dp)
			)
		},
		containerColor = MaterialTheme.colorScheme.background
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.verticalScroll(rememberScrollState())
				.padding(bottom = 12.dp)
		) {
			HeroSection(recipe = recipe)

			ServingSelectorCard(
				selectedServings = uiState.selectedServings,
				minServings = minServings,
				maxServings = maxServings,
				onDecrease = { onServingsChanged((uiState.selectedServings - 1).coerceAtLeast(minServings)) },
				onIncrease = { onServingsChanged((uiState.selectedServings + 1).coerceAtMost(maxServings)) }
			)

			FlavorCustomizationCard(
				spiceLevel = uiState.spiceLevel,
				saltLevel = uiState.saltLevel,
				sweetnessLevel = uiState.sweetnessLevel,
				onSpiceLevelChanged = onSpiceLevelChanged,
				onSaltLevelChanged = onSaltLevelChanged,
				onSweetnessLevelChanged = onSweetnessLevelChanged
			)

			val dispensable = uiState.adjustedIngredients.filter { it.isDispensable }
			val manual = uiState.adjustedIngredients.filter { !it.isDispensable }

			if (dispensable.isNotEmpty()) {
				SectionHeader(title = "Auto-Dispensable")
				Text(
					text = "These ingredients will be automatically handled by your SousChef dispenser.",
					style = MaterialTheme.typography.bodySmall,
					color = AppColors.gold(),
					modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
				)
				Spacer(modifier = Modifier.height(8.dp))
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp)
				) {
					dispensable.forEach { ingredient ->
						IngredientQuantityRow(ingredient = ingredient)
					}
				}
			}

			if (manual.isNotEmpty()) {
				SectionHeader(title = "Manual Ingredients")
				Text(
					text = "Quantities update instantly with serving size changes.",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
				)
				Spacer(modifier = Modifier.height(8.dp))
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp)
				) {
					manual.forEach { ingredient ->
						IngredientQuantityRow(ingredient = ingredient)
					}
				}
			}
		}
	}
}

@Composable
private fun HeroSection(recipe: Recipe) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(250.dp)
	) {
		if (!recipe.coverImageUrl.isNullOrBlank()) {
			AsyncImage(
				model = recipe.coverImageUrl,
				contentDescription = recipe.title,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
		} else {
			// Gradient fallback when no cover image
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(
						Brush.linearGradient(
							colors = listOf(
								AppColors.gold().copy(alpha = 0.6f),
								AppColors.gold().copy(alpha = 0.2f),
								MaterialTheme.colorScheme.surface
							)
						)
					)
			)
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					Brush.verticalGradient(
						colors = listOf(
							Color.Transparent,
							AppColors.heroBackground().copy(alpha = 0.8f)
						)
					)
				)
		)

		Column(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(16.dp)
		) {
			Text(
				text = recipe.title,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
				color = Color.White,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
			Spacer(modifier = Modifier.height(6.dp))
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = "by ${recipe.creatorName}",
					style = MaterialTheme.typography.bodyMedium,
					color = Color.White.copy(alpha = 0.92f)
				)
				if (recipe.isVerifiedChefRecipe) {
					Spacer(modifier = Modifier.width(8.dp))
					VerifiedChefBadge()
				}
			}
		}
	}
}

@Composable
private fun ServingSelectorCard(
	selectedServings: Int,
	minServings: Int,
	maxServings: Int,
	onDecrease: () -> Unit,
	onIncrease: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 14.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
		shape = RoundedCornerShape(20.dp)
	) {
		Column(modifier = Modifier.padding(18.dp)) {
			Text(
				text = "How many are you cooking for?",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
			Spacer(modifier = Modifier.height(10.dp))
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				StepperButton(
					icon = Icons.Outlined.Remove,
					enabled = selectedServings > minServings,
					onClick = onDecrease
				)
				Text(
					text = selectedServings.toString(),
					style = MaterialTheme.typography.headlineSmall,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurface
				)
				StepperButton(
					icon = Icons.Outlined.Add,
					enabled = selectedServings < maxServings,
					onClick = onIncrease
				)
			}
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "Serves $minServings-$maxServings",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun StepperButton(
	icon: ImageVector,
	enabled: Boolean,
	onClick: () -> Unit
) {
	IconButton(
		onClick = onClick,
		enabled = enabled,
		modifier = Modifier
			.size(40.dp)
			.clip(CircleShape)
			.background(MaterialTheme.colorScheme.surfaceVariant)
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
private fun FlavorCustomizationCard(
	spiceLevel: Float,
	saltLevel: Float,
	sweetnessLevel: Float,
	onSpiceLevelChanged: (Float) -> Unit,
	onSaltLevelChanged: (Float) -> Unit,
	onSweetnessLevelChanged: (Float) -> Unit
) {
	GlassCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
		Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
			Text(
				text = "Flavor customization",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
			FlavorSliderRow(
				label = "Spice",
				emoji = "🌶",
				level = spiceLevel,
				onLevelChanged = onSpiceLevelChanged
			)
			FlavorSliderRow(
				label = "Salt",
				emoji = "🧂",
				level = saltLevel,
				onLevelChanged = onSaltLevelChanged
			)
			FlavorSliderRow(
				label = "Sweetness",
				emoji = "🍯",
				level = sweetnessLevel,
				onLevelChanged = onSweetnessLevelChanged
			)
		}
	}
}

@Composable
private fun FlavorSliderRow(
	label: String,
	emoji: String,
	level: Float,
	onLevelChanged: (Float) -> Unit
) {
	Column {
		Text(
			text = "$emoji $label",
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onSurface
		)
		Slider(
			value = level.toSliderValue(),
			onValueChange = { slider -> onLevelChanged(slider.toFlavorLevel()) },
			valueRange = 0f..1f
		)
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(text = "Less", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
			Text(text = "Original", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
			Text(text = "More", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
		}
	}
}

@Composable
private fun IngredientQuantityRow(ingredient: ResolvedIngredient) {
	val animatedQuantity by animateFloatAsState(
		targetValue = ingredient.quantity.toFloat(),
		label = "ingredient_${ingredient.globalIngredientId}"
	)

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 10.dp),
		colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
		shape = RoundedCornerShape(14.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp, vertical = 10.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
				AsyncImage(
					model = ingredient.imageUrl,
					contentDescription = ingredient.name,
					modifier = Modifier
						.size(42.dp)
						.clip(RoundedCornerShape(10.dp)),
					contentScale = ContentScale.Crop
				)
				Spacer(modifier = Modifier.width(10.dp))
				Text(
					text = ingredient.name,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = "${animatedQuantity.toOneDecimalString()} ${ingredient.unit}",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}

private fun Float.toOneDecimalString(): String = ((this * 10f).roundToInt() / 10f).toString()

private fun Float.toSliderValue(): Float = ((this + 1f) / 2f).coerceIn(0f, 1f)

private fun Float.toFlavorLevel(): Float {
	val raw = (this * 2f) - 1f
	return ((raw * 10f).roundToInt() / 10f).coerceIn(-1f, 1f)
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecipeOverviewContentPreview() {
	SousChefTheme {
		RecipeOverviewContent(
            uiState = RecipeOverviewUiState(
                recipe = Recipe(
                    recipeId = "r1",
                    title = "Creamy Garlic Pasta",
                    creatorName = "Chef Hemang",
                    baseServingSize = 2,
                    minServingSize = 1,
                    maxServingSize = 6,
                    isVerifiedChefRecipe = true,
                    ingredients = emptyList()
                ),
                selectedServings = 3,
                adjustedIngredients = listOf(
                    ResolvedIngredient(
                        globalIngredientId = "1",
                        name = "Garlic",
                        quantity = 4.2,
                        unit = "cloves"
                    ),
                    ResolvedIngredient(
                        globalIngredientId = "2",
                        name = "Cream",
                        quantity = 220.0,
                        unit = "ml"
                    ),
                    ResolvedIngredient(
                        globalIngredientId = "3",
                        name = "Parmesan",
                        quantity = 35.0,
                        unit = "g"
                    )
                ),
                isLoading = false
            ),
            onBack = {},
            onServingsChanged = {},
            onSpiceLevelChanged = {},
            onSaltLevelChanged = {},
            onStartCooking = {},
            onEditRecipe = {},
            onDeleteRecipe = {},
            onSweetnessLevelChanged = {}
        )
	}
}



