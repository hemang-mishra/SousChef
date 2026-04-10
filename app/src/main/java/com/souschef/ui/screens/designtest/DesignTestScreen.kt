package com.souschef.ui.screens.designtest

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.souschef.ui.components.DietaryTag
import com.souschef.ui.components.ElevatedCard
import com.souschef.ui.components.EmptyStateView
import com.souschef.ui.components.FlameLevel
import com.souschef.ui.components.GhostButton
import com.souschef.ui.components.GlassCard
import com.souschef.ui.components.GoldAccentCard
import com.souschef.ui.components.ImageCard
import com.souschef.ui.components.IngredientRow
import com.souschef.ui.components.PrimaryButton
import com.souschef.ui.components.RatingDisplay
import com.souschef.ui.components.SearchField
import com.souschef.ui.components.SecondaryButton
import com.souschef.ui.components.SectionHeader
import com.souschef.ui.components.SousChefExtendedFAB
import com.souschef.ui.components.SousChefFAB
import com.souschef.ui.components.SousChefFilterChip
import com.souschef.ui.components.SousChefTextField
import com.souschef.ui.components.StandalonePasswordField
import com.souschef.ui.components.StandardCard
import com.souschef.ui.components.StatusTag
import com.souschef.ui.components.StepRow
import com.souschef.ui.components.UserAvatar
import com.souschef.ui.components.VerifiedChefBadge
import com.souschef.ui.theme.AppColors
import com.souschef.ui.theme.CharcoalDeep
import com.souschef.ui.theme.CharcoalLight
import com.souschef.ui.theme.CharcoalMedium
import com.souschef.ui.theme.CreamLight
import com.souschef.ui.theme.ErrorLight
import com.souschef.ui.theme.GlassDark
import com.souschef.ui.theme.GlassGold
import com.souschef.ui.theme.GlassWhite
import com.souschef.ui.theme.GoldLight
import com.souschef.ui.theme.GoldRich
import com.souschef.ui.theme.GoldVibrant
import com.souschef.ui.theme.IvoryWhite
import com.souschef.ui.theme.PearlWhite
import com.souschef.ui.theme.SageGreen
import com.souschef.ui.theme.SousChefTheme
import com.souschef.ui.theme.SuccessLight
import com.souschef.ui.theme.TerracottaVibrant
import com.souschef.ui.theme.WarningLight
import com.souschef.ui.theme.DeepBurgundy
import kotlinx.coroutines.launch

/**
 * Design System showcase screen.
 *
 * Features:
 * - Top toggle to switch light / dark theme live.
 * - All 14 component categories on a single scrollable canvas.
 * - Zero hardcoded colors — all via MaterialTheme.colorScheme or AppColors.
 */
@Composable
fun DesignTestScreen(onNavigateHome: () -> Unit = {}) {
    var isDark by remember { mutableStateOf(false) }
    SousChefTheme(darkTheme = isDark) {
        DesignTestScreenLayout(
            isDark = isDark,
            onToggleDark = { isDark = !isDark },
            onNavigateHome = onNavigateHome
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DesignTestScreenLayout(
    isDark: Boolean,
    onToggleDark: () -> Unit,
    onNavigateHome: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Design System", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                },
                actions = {
                    Text(
                        text = if (isDark) "☀️ Light" else "🌙 Dark",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = isDark,
                        onCheckedChange = { onToggleDark() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── 1. COLOR PALETTE ──────────────────────────────────────────
            DemoSection("1. Color Palette") {
                ColorSwatchRow("Gold: Vibrant / Light / Rich", listOf(GoldVibrant, GoldLight, GoldRich))
                ColorSwatchRow("Surfaces Light: Ivory / Pearl / Cream", listOf(IvoryWhite, PearlWhite, CreamLight))
                ColorSwatchRow("Surfaces Dark: Deep / Medium / Light", listOf(CharcoalDeep, CharcoalMedium, CharcoalLight))
                ColorSwatchRow("Accents: Sage / Terracotta / Burgundy", listOf(SageGreen, TerracottaVibrant, DeepBurgundy))
                ColorSwatchRow("Semantic: Success / Error / Warning", listOf(SuccessLight, ErrorLight, WarningLight))
                ColorSwatchRow("Glass: White / Dark / Gold", listOf(GlassWhite, GlassDark, GlassGold))
            }

            // ── 2. TYPOGRAPHY ─────────────────────────────────────────────
            DemoSection("2. Typography Scale") {
                Text("displaySmall — 36sp Bold Serif", style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground)
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text("headlineLarge — 32sp Bold Serif", style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("headlineMedium — 28sp SemiBold Serif", style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("headlineSmall — 24sp SemiBold Serif", style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground)
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text("titleLarge — 22sp Medium Serif", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("titleMedium — 16sp SemiBold Sans", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("titleSmall — 14sp Medium Sans", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground)
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text("bodyLarge — 16sp Normal Sans. Use for primary descriptions and content.",
                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                Text("bodyMedium — 14sp Normal Sans. Use for secondary information.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("bodySmall — 12sp Normal Sans. Use for captions and metadata.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text("labelLarge — 14sp Medium Sans — Buttons",
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
                Text("labelMedium — 12sp Medium Sans — Tags, Chips",
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("labelSmall — 11sp Medium Sans — Metadata",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── 3. STANDARD CARDS ─────────────────────────────────────────
            DemoSection("3. Standard Cards") {
                StandardCard {
                    Column {
                        Text("Standard Card", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Flat, outlined, no elevation. Default for most content. Surface color adapts to light/dark automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(10.dp))
                ElevatedCard {
                    Column {
                        Text("Elevated Card", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("8dp shadow elevation for emphasis. Use for featured or important content.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(10.dp))
                GoldAccentCard {
                    Column {
                        Text("Gold Accent Card", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Premium gold gradient border. For verified chef recipes or premium feature highlights.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── 4. GLASS CARDS ────────────────────────────────────────────
            DemoSection("4. Glassmorphism Cards") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(CharcoalDeep, Color(0xFF2D1B00))),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    GlassCard {
                        Column {
                            Text("Glass Card", style = MaterialTheme.typography.titleMedium,
                                color = Color.White)
                            Spacer(Modifier.height(6.dp))
                            Text("Semi-transparent with frosted border. Limit to 1–2 per screen. Best on dark/gradient backgrounds.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f))
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(displayName = "Marco Rossi", size = 32.dp)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Marco Rossi", style = MaterialTheme.typography.labelMedium,
                                        color = Color.White)
                                    VerifiedChefBadge()
                                }
                                Spacer(Modifier.weight(1f))
                                RatingDisplay(rating = 4.8f, count = 523, starSize = 14.dp)
                            }
                        }
                    }
                }
            }

            // ── 5. IMAGE CARDS ────────────────────────────────────────────
            DemoSection("5. Image Cards") {
                ImageCard(imageUrl = null, title = "Truffle Risotto alla Milanese")
                Spacer(Modifier.height(10.dp))
                ImageCard(imageUrl = null, title = "Beef Wellington", aspectRatio = 16f / 9f)
            }

            // ── 6. BUTTONS ────────────────────────────────────────────────
            DemoSection("6. Buttons") {
                PrimaryButton(text = "Start Cooking", onClick = {})
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    text = "Continue with Google",
                    onClick = {},
                    leadingIcon = {
                        androidx.compose.material3.Icon(
                            Icons.Outlined.AccountCircle, null,
                            Modifier.size(18.dp)
                        )
                    }
                )
                Spacer(Modifier.height(8.dp))
                GhostButton(text = "Forgot Password?", onClick = {})
                Spacer(Modifier.height(8.dp))
                PrimaryButton(text = "Loading State…", onClick = {}, isLoading = true)
                Spacer(Modifier.height(8.dp))
                PrimaryButton(text = "Disabled Button", onClick = {}, enabled = false)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SousChefFAB(icon = Icons.Outlined.Add, contentDescription = "Add", onClick = {})
                    SousChefExtendedFAB(text = "Create Recipe", icon = Icons.Outlined.Add, onClick = {})
                }
            }

            // ── 7. INPUT FIELDS ───────────────────────────────────────────
            DemoSection("7. Input Fields") {
                SousChefTextField(value = "Pasta Carbonara", onValueChange = {}, label = "Recipe Title")
                Spacer(Modifier.height(8.dp))
                SearchField(value = "", onValueChange = {}, placeholder = "Search 1,200+ recipes…")
                Spacer(Modifier.height(8.dp))
                StandalonePasswordField(value = "secret123", onValueChange = {}, label = "Password")
                Spacer(Modifier.height(8.dp))
                SousChefTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email",
                    isError = true,
                    errorMessage = "Please enter a valid email address"
                )
                Spacer(Modifier.height(8.dp))
                SousChefTextField(
                    value = "A rich and velvety pasta made with eggs, Pecorino Romano, guanciale and freshly cracked black pepper.",
                    onValueChange = {},
                    label = "Description",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5
                )
            }

            // ── 8. CHIPS & TAGS ───────────────────────────────────────────
            DemoSection("8. Chips & Tags") {
                var vegSelected by remember { mutableStateOf(true) }
                var spicySelected by remember { mutableStateOf(false) }
                var quickSelected by remember { mutableStateOf(true) }
                var italianSelected by remember { mutableStateOf(false) }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SousChefFilterChip("🌿 Vegetarian", vegSelected, { vegSelected = it })
                    SousChefFilterChip("🌶 Spicy", spicySelected, { spicySelected = it })
                    SousChefFilterChip("⚡ Quick (<30 min)", quickSelected, { quickSelected = it })
                    SousChefFilterChip("🇮🇹 Italian", italianSelected, { italianSelected = it })
                }
                Spacer(Modifier.height(12.dp))
                Text("Status Tags", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusTag("Available", AppColors.success())
                    StatusTag("Sold Out", AppColors.error())
                    StatusTag("Limited", AppColors.warning())
                    StatusTag("Featured", AppColors.gold())
                }
                Spacer(Modifier.height(12.dp))
                Text("Dietary Tags", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DietaryTag("🌱 Vegan", AppColors.accentGreen())
                    DietaryTag("🌾 Gluten-Free", AppColors.accentTeal())
                    DietaryTag("🌶 Spicy", AppColors.accentTerracotta())
                    DietaryTag("🍷 Wine Pairing", AppColors.accentBurgundy())
                    DietaryTag("🫒 Organic", AppColors.accentOlive())
                }
                Spacer(Modifier.height(12.dp))
                VerifiedChefBadge()
            }

            // ── 9. LIST ITEMS ─────────────────────────────────────────────
            DemoSection("9. List Items") {
                SectionHeader(title = "Ingredients")
                StandardCard {
                    Column {
                        IngredientRow(name = "Arborio Rice", quantity = "200", unit = "g")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        IngredientRow(name = "Parmesan Cheese (Parmigiano Reggiano)", quantity = "80", unit = "g",
                            isHighlighted = true)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        IngredientRow(name = "White Truffle Oil", quantity = "2", unit = "tbsp")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        IngredientRow(name = "Dry White Wine", quantity = "120", unit = "ml")
                    }
                }
                Spacer(Modifier.height(12.dp))
                SectionHeader(title = "Steps")
                StandardCard {
                    Column {
                        StepRow(1, "Bring a large pot of salted water to boil over high heat.",
                            FlameLevel.HIGH, isActive = true)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        StepRow(2, "Toast arborio rice in butter, stirring continuously for 2 minutes.",
                            FlameLevel.MEDIUM)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        StepRow(3, "Add wine and stir until fully absorbed.", FlameLevel.MEDIUM)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        StepRow(4, "Remove from heat. Fold in truffle oil and cold butter.", FlameLevel.NONE)
                    }
                }
            }

            // ── 10. LOADING STATES ────────────────────────────────────────
            DemoSection("10. Loading States") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Text(
                            "Chef AI is crafting your recipe…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── 11. EMPTY STATE ───────────────────────────────────────────
            DemoSection("11. Empty State") {
                StandardCard {
                    EmptyStateView(
                        title = "No saved recipes",
                        subtitle = "Recipes you bookmark will appear here. Start exploring!",
                        icon = Icons.Outlined.BookmarkBorder,
                        actionLabel = "Browse Recipes",
                        onAction = {}
                    )
                }
            }

            // ── 12. SNACKBAR ──────────────────────────────────────────────
            DemoSection("12. Snackbar") {
                SecondaryButton(
                    text = "Trigger Snackbar",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Recipe saved successfully! ✓")
                        }
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Snackbar appears at the bottom via Scaffold's snackbarHost.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── 13. RATING ROW ────────────────────────────────────────────
            DemoSection("13. Rating Display") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RatingDisplay(rating = 5.0f, count = 1284)
                    RatingDisplay(rating = 4.5f, count = 372)
                    RatingDisplay(rating = 3.5f, count = 89)
                    RatingDisplay(rating = 2.0f)
                }
            }

            // ── 14. USER AVATARS ──────────────────────────────────────────
            DemoSection("14. User Avatars") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(displayName = "Marco Rossi", size = 56.dp)
                    UserAvatar(displayName = "Julia Child", size = 48.dp)
                    UserAvatar(displayName = "Gordon Ramsay", size = 40.dp)
                    UserAvatar(displayName = "J", size = 32.dp)
                    UserAvatar(displayName = "AB", size = 32.dp)
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(displayName = "Marco Rossi", size = 44.dp)
                    Column {
                        Text("Marco Rossi", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(2.dp))
                        VerifiedChefBadge()
                    }
                }
            }

            // Bottom padding
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun DemoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun ColorSwatchRow(label: String, colors: List<Color>) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 44.dp)
                        .background(color, RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DesignTestLightPreview() {
    SousChefTheme(darkTheme = false) {
        DesignTestScreenLayout(isDark = false, onToggleDark = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DesignTestDarkPreview() {
    SousChefTheme(darkTheme = true) {
        DesignTestScreenLayout(isDark = true, onToggleDark = {})
    }
}




