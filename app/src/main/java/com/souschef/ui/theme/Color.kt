package com.souschef.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Premium Food App - Color System
 * Design Philosophy: Luxury Minimalism + Glassmorphism
 *
 * Core Principles:
 * - Elegant neutrals as foundation
 * - Muted gold accents for premium touch
 * - Deep charcoals for sophistication
 * - Soft whites for breathing room
 * - Subtle opacity for glass effects
 */

// ============================================
// PRIMARY PALETTE - Neutral Elegance
// ============================================

// Ivory & Cream Tones (Light Mode Base)
val IvoryWhite = Color(0xFFFAF9F7)           // Main background - warm white
val CreamLight = Color(0xFFF5F3F0)           // Secondary background
val PearlWhite = Color(0xFFFFFFFF)           // Pure white for cards

// Charcoal & Deep Grays (Dark Mode Base)
val CharcoalDeep = Color(0xFF1A1A1A)         // Main background dark
val CharcoalMedium = Color(0xFF2D2D2D)       // Secondary background dark
val CharcoalLight = Color(0xFF3A3A3A)        // Elevated surfaces dark

// ============================================
// ACCENT COLORS - Muted Luxury
// ============================================

// Gold Accents (Premium Touch)
val GoldMuted = Color(0xFFD4AF6A)            // Primary gold - sophisticated
val GoldDark = Color(0xFFB8935A)             // Darker gold variant
val GoldLight = Color(0xFFE5C99A)            // Light gold for highlights
val ChampagneGold = Color(0xFFF7E7CE)        // Very subtle gold tint

// Secondary Accents
val SageGreen = Color(0xFF9CAF88)            // Fresh, organic feel
val DeepOlive = Color(0xFF5C6B4A)            // Earthy depth
val TerracottaMuted = Color(0xFFD4A59A)      // Warm, appetizing
val DeepBurgundy = Color(0xFF6B3E3E)         // Rich, wine-like

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

val GlassDark = Color(0xCC1A1A1A)            // 80% dark
val GlassDarkLight = Color(0x99000000)       // 60% dark
val GlassDarkSoft = Color(0x66000000)        // 40% dark

// Frosted Glass Tints
val GlassGold = Color(0x33D4AF6A)            // 20% gold tint
val GlassCream = Color(0x4DF5F3F0)           // 30% cream tint

// ============================================
// TEXT COLORS
// ============================================

// Light Mode Text
val TextPrimaryLight = Color(0xFF1A1A1A)     // Almost black
val TextSecondaryLight = Color(0xFF666666)   // Medium gray
val TextTertiaryLight = Color(0xFF999999)    // Light gray
val TextDisabledLight = Color(0xFFCCCCCC)    // Very light gray

// Dark Mode Text
val TextPrimaryDark = Color(0xFFFAFAFA)      // Almost white
val TextSecondaryDark = Color(0xFFB3B3B3)    // Light gray
val TextTertiaryDark = Color(0xFF808080)     // Medium gray
val TextDisabledDark = Color(0xFF4D4D4D)     // Dark gray

// ============================================
// SEMANTIC COLORS
// ============================================

// Success (subtle green)
val SuccessLight = Color(0xFF7FB069)
val SuccessDark = Color(0xFF5C8F4A)

// Error (muted red, not harsh)
val ErrorLight = Color(0xFFCF6679)
val ErrorDark = Color(0xFFB00020)

// Warning (warm amber)
val WarningLight = Color(0xFFE6B566)
val WarningDark = Color(0xFFCC9933)

// Info (soft blue)
val InfoLight = Color(0xFF88B5D6)
val InfoDark = Color(0xFF5C8CAE)

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
val BorderLight = Color(0xFFE8E6E3)          // Subtle border
val DividerLight = Color(0xFFEEECE9)         // Soft divider
val OutlineLight = Color(0xFFD4D2CF)         // Visible outline

// Dark Mode
val BorderDark = Color(0xFF3A3A3A)           // Subtle border
val DividerDark = Color(0xFF2D2D2D)          // Soft divider
val OutlineDark = Color(0xFF4D4D4D)          // Visible outline

// ============================================
// SURFACE VARIATIONS
// ============================================

// Light Mode Surfaces
val SurfaceLight = Color(0xFFFFFFFF)         // Primary surface
val SurfaceVariantLight = Color(0xFFF5F3F0)  // Secondary surface
val SurfaceTintLight = Color(0xFFFAF9F7)     // Tinted surface

// Dark Mode Surfaces
val SurfaceDark = Color(0xFF1A1A1A)          // Primary surface
val SurfaceVariantDark = Color(0xFF2D2D2D)   // Secondary surface
val SurfaceTintDark = Color(0xFF252525)      // Tinted surface

// Elevated Surfaces (for cards, dialogs)
val SurfaceElevated1Light = Color(0xFFFFFFFF)
val SurfaceElevated2Light = Color(0xFFFCFBF9)
val SurfaceElevated3Light = Color(0xFFF9F8F6)

val SurfaceElevated1Dark = Color(0xFF232323)
val SurfaceElevated2Dark = Color(0xFF2A2A2A)
val SurfaceElevated3Dark = Color(0xFF313131)

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

// For premium cards and hero sections
val GradientGold = listOf(
    Color(0xFFE5C99A),
    Color(0xFFD4AF6A)
)

val GradientNeutral = listOf(
    Color(0xFFFAF9F7),
    Color(0xFFF5F3F0)
)

val GradientCharcoal = listOf(
    Color(0xFF2D2D2D),
    Color(0xFF1A1A1A)
)

// Subtle image overlays
val GradientImageOverlay = listOf(
    Color(0x00000000),  // Transparent top
    Color(0x66000000)   // 40% black bottom
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
 * 1. Glassmorphism Cards:
 *    - Use GlassWhite/GlassDark as card background
 *    - Apply blur modifier (12-24dp)
 *    - Add subtle border with BorderLight/BorderDark at 0.5dp
 *
 * 2. Food Images:
 *    - Use ImageScrimDark overlay for text readability
 *    - Apply GradientImageOverlay for bottom text areas
 *
 * 3. Premium Accents:
 *    - Use GoldMuted sparingly for CTAs and highlights
 *    - Pair with neutral backgrounds for maximum impact
 *
 * 4. Typography Pairing:
 *    - Headings: Use TextPrimary with serif fonts
 *    - Body: Use TextSecondary with sans-serif
 *    - Accents: Use GoldMuted for special emphasis
 */