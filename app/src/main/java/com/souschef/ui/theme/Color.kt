package com.souschef.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Premium Food App - Color System
 * Design Philosophy: Vibrant Luxury + Glassmorphism
 *
 * Core Principles:
 * - Rich, vibrant gold as the hero color
 * - Deep, sophisticated neutrals
 * - Vibrant accent colors that pop
 * - Premium glass effects
 */

// ============================================
// PRIMARY PALETTE - Neutral Elegance
// ============================================

// Ivory & Cream Tones (Light Mode Base)
val IvoryWhite = Color(0xFFFAF9F7)           // Main background - warm white
val CreamLight = Color(0xFFF5F3F0)           // Secondary background
val PearlWhite = Color(0xFFFFFFFF)           // Pure white for cards

// Charcoal & Deep Grays (Dark Mode Base)
val CharcoalDeep = Color(0xFF121212)         // Main background dark - deeper
val CharcoalMedium = Color(0xFF1E1E1E)       // Secondary background dark
val CharcoalLight = Color(0xFF2C2C2C)        // Elevated surfaces dark

// ============================================
// ACCENT COLORS - Vibrant Luxury
// ============================================

// Gold Accents (Premium Touch) - MORE VIBRANT
val GoldVibrant = Color(0xFFFFB800)          // Primary vibrant gold - hero color
val GoldRich = Color(0xFFE5A600)             // Rich gold for pressed states
val GoldLight = Color(0xFFFFD54F)            // Light gold for highlights
val GoldPale = Color(0xFFFFE082)             // Pale gold for backgrounds
val ChampagneGold = Color(0xFFFFF3D6)        // Very subtle gold tint

// Legacy gold (keeping for compatibility, but prefer vibrant)
val GoldMuted = Color(0xFFD4AF37)            // Classic gold - slightly more saturated

// Amber/Bronze Alternative
val AmberRich = Color(0xFFFFAB00)            // Rich amber
val BronzeDeep = Color(0xFFCD7F32)           // Deep bronze

// Secondary Accents - MORE VIBRANT
val SageGreen = Color(0xFF66BB6A)            // Fresh, vibrant green
val DeepOlive = Color(0xFF558B2F)            // Earthy but vibrant
val TerracottaVibrant = Color(0xFFE57373)    // Warm, appetizing coral
val TerracottaMuted = Color(0xFFEF5350)      // Rich terracotta
val DeepBurgundy = Color(0xFF8E2441)         // Rich, wine-like - more saturated
val RubyRed = Color(0xFFC62828)              // Deep ruby

// Teal/Aqua for variety
val TealVibrant = Color(0xFF26A69A)          // Fresh teal
val TealDeep = Color(0xFF00897B)             // Deep teal

// ============================================
// NEUTRAL GRAYS - Full Spectrum
// ============================================

val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)

// ============================================
// GLASSMORPHISM EFFECTS
// ============================================

// Glass Overlay Colors (use with blur effects)
val GlassWhite = Color(0xCCFFFFFF)           // 80% white
val GlassWhiteLight = Color(0xE6FFFFFF)      // 90% white
val GlassWhiteSoft = Color(0xB3FFFFFF)       // 70% white

val GlassDark = Color(0xCC121212)            // 80% dark
val GlassDarkLight = Color(0x99000000)       // 60% dark
val GlassDarkSoft = Color(0x66000000)        // 40% dark

// Frosted Glass Tints
val GlassGold = Color(0x40FFB800)            // 25% vibrant gold tint
val GlassCream = Color(0x4DF5F3F0)           // 30% cream tint

// ============================================
// TEXT COLORS
// ============================================

// Light Mode Text
val TextPrimaryLight = Color(0xFF1A1A1A)     // Almost black
val TextSecondaryLight = Color(0xFF5C5C5C)   // Medium gray - slightly darker
val TextTertiaryLight = Color(0xFF8A8A8A)    // Light gray
val TextDisabledLight = Color(0xFFBDBDBD)    // Disabled gray

// Dark Mode Text
val TextPrimaryDark = Color(0xFFFAFAFA)      // Almost white
val TextSecondaryDark = Color(0xFFB8B8B8)    // Light gray
val TextTertiaryDark = Color(0xFF787878)     // Medium gray
val TextDisabledDark = Color(0xFF505050)     // Dark gray

// ============================================
// SEMANTIC COLORS - MORE VIBRANT
// ============================================

// Success (vibrant green)
val SuccessLight = Color(0xFF4CAF50)         // Material green
val SuccessDark = Color(0xFF81C784)          // Lighter for dark mode
val SuccessVibrant = Color(0xFF00C853)       // Extra vibrant

// Error (vibrant red)
val ErrorLight = Color(0xFFE53935)           // Vibrant red
val ErrorDark = Color(0xFFEF5350)            // Lighter for dark mode
val ErrorVibrant = Color(0xFFFF1744)         // Extra vibrant

// Warning (vibrant amber)
val WarningLight = Color(0xFFFF9800)         // Vibrant orange
val WarningDark = Color(0xFFFFB74D)          // Lighter for dark mode
val WarningVibrant = Color(0xFFFFAB00)       // Extra vibrant

// Info (vibrant blue)
val InfoLight = Color(0xFF2196F3)            // Vibrant blue
val InfoDark = Color(0xFF64B5F6)             // Lighter for dark mode
val InfoVibrant = Color(0xFF00B0FF)          // Extra vibrant

// ============================================
// INTERACTIVE STATES
// ============================================

// Hover & Press States (Light Mode)
val HoverLight = Color(0x0A000000)           // 4% black overlay
val PressedLight = Color(0x1A000000)         // 10% black overlay
val RippleLight = Color(0x1F000000)          // 12% black overlay

// Hover & Press States (Dark Mode)
val HoverDark = Color(0x0AFFFFFF)            // 4% white overlay
val PressedDark = Color(0x1AFFFFFF)          // 10% white overlay
val RippleDark = Color(0x1FFFFFFF)           // 12% white overlay

// ============================================
// BORDERS & DIVIDERS
// ============================================

// Light Mode
val BorderLight = Color(0xFFE0E0E0)          // Subtle border
val DividerLight = Color(0xFFEEEEEE)         // Soft divider
val OutlineLight = Color(0xFFBDBDBD)         // Visible outline

// Dark Mode
val BorderDark = Color(0xFF3A3A3A)           // Subtle border
val DividerDark = Color(0xFF2A2A2A)          // Soft divider
val OutlineDark = Color(0xFF505050)          // Visible outline

// ============================================
// SURFACE VARIATIONS
// ============================================

// Light Mode Surfaces
val SurfaceLight = Color(0xFFFFFFFF)         // Primary surface
val SurfaceVariantLight = Color(0xFFF5F5F5)  // Secondary surface
val SurfaceTintLight = Color(0xFFFAF9F7)     // Tinted surface

// Dark Mode Surfaces
val SurfaceDark = Color(0xFF121212)          // Primary surface
val SurfaceVariantDark = Color(0xFF1E1E1E)   // Secondary surface
val SurfaceTintDark = Color(0xFF1A1A1A)      // Tinted surface

// Elevated Surfaces (for cards, dialogs)
val SurfaceElevated1Light = Color(0xFFFFFFFF)
val SurfaceElevated2Light = Color(0xFFFCFCFC)
val SurfaceElevated3Light = Color(0xFFF8F8F8)

val SurfaceElevated1Dark = Color(0xFF1E1E1E)
val SurfaceElevated2Dark = Color(0xFF252525)
val SurfaceElevated3Dark = Color(0xFF2C2C2C)

// ============================================
// FOOD PHOTOGRAPHY OVERLAY
// ============================================

// Use these for scrim overlays on food images
val ImageScrimLight = Color(0x33000000)      // 20% dark for readability
val ImageScrimDark = Color(0x4D000000)       // 30% dark for contrast
val ImageGradientStart = Color(0x00000000)   // Transparent
val ImageGradientEnd = Color(0xCC000000)     // 80% black

// ============================================
// SPECIAL EFFECTS
// ============================================

// Shimmer effect for loading states
val ShimmerHighlight = Color(0x33FFFFFF)     // Subtle white shimmer
val ShimmerBase = Color(0x1AFFFFFF)          // Base shimmer layer

// Shadow colors
val ShadowLight = Color(0x0D000000)          // 5% black shadow
val ShadowMedium = Color(0x1A000000)         // 10% black shadow
val ShadowStrong = Color(0x33000000)         // 20% black shadow

// ============================================
// PREMIUM GRADIENT COMBINATIONS
// ============================================

// Vibrant gold gradients
val GradientGoldVibrant = listOf(
    Color(0xFFFFD54F),
    Color(0xFFFFB800),
    Color(0xFFE5A600)
)

val GradientGold = listOf(
    Color(0xFFFFD54F),
    Color(0xFFFFB800)
)

val GradientGoldSubtle = listOf(
    Color(0xFFFFF8E1),
    Color(0xFFFFE082)
)

val GradientNeutral = listOf(
    Color(0xFFFAFAFA),
    Color(0xFFF5F5F5)
)

val GradientCharcoal = listOf(
    Color(0xFF1E1E1E),
    Color(0xFF121212)
)

val GradientDarkElegant = listOf(
    Color(0xFF2C2C2C),
    Color(0xFF1A1A1A),
    Color(0xFF121212)
)

// Accent gradients
val GradientTerracotta = listOf(
    Color(0xFFEF5350),
    Color(0xFFC62828)
)

val GradientTeal = listOf(
    Color(0xFF26A69A),
    Color(0xFF00897B)
)

val GradientSuccess = listOf(
    Color(0xFF66BB6A),
    Color(0xFF43A047)
)

// Subtle image overlays
val GradientImageOverlay = listOf(
    Color(0x00000000),  // Transparent top
    Color(0x80000000)   // 50% black bottom
)

val GradientImageOverlayStrong = listOf(
    Color(0x00000000),
    Color(0xB3000000)   // 70% black bottom
)

// ============================================
// ACCESSIBILITY HELPERS
// ============================================

// High contrast versions for accessibility mode
val HighContrastTextLight = Color(0xFF000000)
val HighContrastTextDark = Color(0xFFFFFFFF)
val HighContrastBorderLight = Color(0xFF000000)
val HighContrastBorderDark = Color(0xFFFFFFFF)

/**
 * Usage Notes:
 *
 * 1. Primary Actions:
 *    - Use GoldVibrant for CTAs, buttons, and key highlights
 *    - GoldRich for pressed/hover states
 *    - GoldLight for secondary highlights
 *
 * 2. Glassmorphism Cards:
 *    - Use GlassWhite/GlassDark as card background
 *    - Apply blur modifier (12-24dp)
 *    - Add subtle border with BorderLight/BorderDark at 0.5dp
 *
 * 3. Food Images:
 *    - Use ImageScrimDark overlay for text readability
 *    - Apply GradientImageOverlay for bottom text areas
 *
 * 4. Dark Mode:
 *    - Use GoldVibrant (same) - it pops nicely on dark
 *    - Surfaces should be CharcoalDeep/Medium/Light
 *    - Text uses TextPrimaryDark/SecondaryDark/TertiaryDark
 */