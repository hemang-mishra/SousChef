package com.souschef.model.recipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.souschef.ui.theme.AppColors

/**
 * Predefined recipe tags with display labels and associated accent colors.
 *
 * Stored in Firestore as the enum [name] (e.g. "VEGETARIAN").
 * Displayed in the UI via [displayLabel] (e.g. "🌿 Vegetarian").
 */
enum class RecipeTag(val displayLabel: String) {

    // ── Diet ────────────────────────────────
    VEGETARIAN("🌿 Vegetarian"),
    VEGAN("🌱 Vegan"),
    HEALTHY("🥗 Healthy"),

    // ── Flavor ──────────────────────────────
    SPICY("🌶 Spicy"),
    DESSERT("🍰 Dessert"),
    BBQ("🍖 BBQ"),

    // ── Speed ───────────────────────────────
    QUICK("⚡ Quick"),

    // ── Cuisine ─────────────────────────────
    ITALIAN("🇮🇹 Italian"),
    INDIAN("🇮🇳 Indian"),
    MEXICAN("🇲🇽 Mexican"),
    JAPANESE("🇯🇵 Japanese"),
    FRENCH("🇫🇷 French"),
    THAI("🇹🇭 Thai"),

    // ── Allergen-free ───────────────────────
    GLUTEN_FREE("🌾 Gluten-Free"),
    NUT_FREE("🥜 Nut-Free"),
    DAIRY_FREE("🧀 Dairy-Free");

    /**
     * Returns the accent [Color] for this tag's chip / badge.
     * Must be called inside a @Composable scope because it reads from [AppColors].
     */
    val color: Color
        @Composable get() = when (this) {
            VEGETARIAN, VEGAN, HEALTHY -> AppColors.accentGreen()
            SPICY, BBQ, DESSERT        -> AppColors.accentTerracotta()
            QUICK                       -> AppColors.gold()
            ITALIAN, FRENCH             -> AppColors.accentBurgundy()
            INDIAN, THAI, MEXICAN, JAPANESE -> AppColors.accentTeal()
            GLUTEN_FREE, NUT_FREE, DAIRY_FREE -> AppColors.accentOlive()
        }

    companion object {
        /** Look up a [RecipeTag] by its stored [name], returning null for unknown values. */
        fun fromName(name: String): RecipeTag? = entries.find { it.name == name }
    }
}

