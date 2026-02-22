package com.souschef.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * Premium Food App - Theme Configuration
 *
 * Design System:
 * - Luxury minimalism with glassmorphism
 * - Elegant typography pairing (Serif + Sans)
 * - Refined shapes and spacing
 * - Premium color palette
 */

// ============================================
// TYPOGRAPHY - Premium Pairing
// ============================================

/**
 * Font Families
 *
 * For production, replace with actual font files:
 * - Playfair Display / Cormorant Garamond (Serif - Headings)
 * - Inter / SF Pro / Avenir (Sans - Body)
 *
 * Add to res/font/ folder:
 * - playfair_display_regular.ttf
 * - playfair_display_medium.ttf
 * - playfair_display_semibold.ttf
 * - playfair_display_bold.ttf
 * - inter_regular.ttf
 * - inter_medium.ttf
 * - inter_semibold.ttf
 * - inter_bold.ttf
 */

// Serif Font Family (For elegant headings)
// val SerifFontFamily = FontFamily(
//     Font(R.font.playfair_display_regular, FontWeight.Normal),
//     Font(R.font.playfair_display_medium, FontWeight.Medium),
//     Font(R.font.playfair_display_semibold, FontWeight.SemiBold),
//     Font(R.font.playfair_display_bold, FontWeight.Bold)
// )

// Sans Font Family (For clean body text)
// val SansFontFamily = FontFamily(
//     Font(R.font.inter_regular, FontWeight.Normal),
//     Font(R.font.inter_medium, FontWeight.Medium),
//     Font(R.font.inter_semibold, FontWeight.SemiBold),
//     Font(R.font.inter_bold, FontWeight.Bold)
// )

// Fallback to default fonts (replace with custom fonts in production)
private val SerifFontFamily = FontFamily.Serif
private val SansFontFamily = FontFamily.SansSerif

/**
 * Typography Scale - Premium Food App
 *
 * Hierarchy:
 * - Display: Hero sections, splash screens (Serif)
 * - Headline: Section titles, card headers (Serif)
 * - Title: Subsections, prominent labels (Serif)
 * - Body: Main content, descriptions (Sans)
 * - Label: Buttons, tags, metadata (Sans)
 */
val PremiumTypography = Typography(

    // Display - Extra large, for hero sections
    displayLarge = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline - Large section headers
    headlineLarge = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title - Medium emphasis headers
    titleLarge = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SansFontFamily,  // Sans for medium titles
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body - Main content text
    bodyLarge = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label - UI elements, buttons, tags
    labelLarge = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ============================================
// SHAPES - Refined Corners
// ============================================

/**
 * Shape System
 *
 * Premium feel with subtle rounded corners:
 * - None: Sharp edges for minimal elements
 * - ExtraSmall: Slight softness (4dp)
 * - Small: Gentle curves (8dp)
 * - Medium: Balanced roundness (12dp)
 * - Large: Prominent curves (16dp)
 * - ExtraLarge: Statement pieces (24dp)
 */
val PremiumShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),      // Tags, small chips
    small = RoundedCornerShape(8.dp),            // Buttons, small cards
    medium = RoundedCornerShape(12.dp),          // Standard cards, inputs
    large = RoundedCornerShape(16.dp),           // Featured cards, images
    extraLarge = RoundedCornerShape(24.dp)       // Hero cards, dialogs
)

// Custom shape variants for specific use cases
object CustomShapes {
    val GlassCard = RoundedCornerShape(16.dp)           // Glassmorphism cards
    val ImageCard = RoundedCornerShape(12.dp)           // Food photo cards
    val Pill = RoundedCornerShape(50)                   // Fully rounded (tags, filters)
    val TopRounded = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )  // Bottom sheets
    val BottomRounded = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )  // Top bars with rounded bottom
}

// ============================================
// COLOR SCHEMES - Light & Dark
// ============================================

/**
 * Light Theme Color Scheme
 * Premium, airy, and sophisticated
 */
private val LightColorScheme = lightColorScheme(
    // Primary - Gold accents
    primary = GoldMuted,
    onPrimary = Color.White,
    primaryContainer = ChampagneGold,
    onPrimaryContainer = CharcoalDeep,

    // Secondary - Sage green
    secondary = SageGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F0E3),
    onSecondaryContainer = DeepOlive,

    // Tertiary - Terracotta
    tertiary = TerracottaMuted,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5E8E3),
    onTertiaryContainer = DeepBurgundy,

    // Error
    error = ErrorLight,
    onError = Color.White,
    errorContainer = Color(0xFFFCE4E4),
    onErrorContainer = ErrorDark,

    // Background & Surface
    background = IvoryWhite,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = GoldMuted,

    // Outline & Border
    outline = OutlineLight,
    outlineVariant = BorderLight,

    // Inverse (for snackbars, etc.)
    inverseSurface = CharcoalDeep,
    inverseOnSurface = TextPrimaryDark,
    inversePrimary = GoldLight,

    // Scrim
    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * Dark Theme Color Scheme
 * Sophisticated, dramatic, and premium
 */
private val DarkColorScheme = darkColorScheme(
    // Primary - Gold accents (lighter in dark mode)
    primary = GoldLight,
    onPrimary = CharcoalDeep,
    primaryContainer = GoldDark,
    onPrimaryContainer = ChampagneGold,

    // Secondary - Sage green
    secondary = SageGreen,
    onSecondary = CharcoalDeep,
    secondaryContainer = DeepOlive,
    onSecondaryContainer = Color(0xFFE8F0E3),

    // Tertiary - Terracotta
    tertiary = TerracottaMuted,
    onTertiary = CharcoalDeep,
    tertiaryContainer = DeepBurgundy,
    onTertiaryContainer = Color(0xFFF5E8E3),

    // Error
    error = ErrorLight,
    onError = CharcoalDeep,
    errorContainer = ErrorDark,
    onErrorContainer = Color(0xFFFCE4E4),

    // Background & Surface
    background = CharcoalDeep,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = GoldLight,

    // Outline & Border
    outline = OutlineDark,
    outlineVariant = BorderDark,

    // Inverse
    inverseSurface = IvoryWhite,
    inverseOnSurface = TextPrimaryLight,
    inversePrimary = GoldMuted,

    // Scrim
    scrim = Color.Black.copy(alpha = 0.5f)
)

// ============================================
// THEME COMPOSABLE
// ============================================

/**
 * Premium Food App Theme
 *
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use dynamic color (Android 12+)
 * @param content The composable content
 */
@Composable
fun SousChefTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // For a premium brand, you might want to disable this to maintain brand consistency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PremiumTypography,
        shapes = PremiumShapes,
        content = content
    )
}

// ============================================
// THEME EXTENSIONS
// ============================================

/**
 * Extended color properties for premium features
 * Access via MaterialTheme.colorScheme.extension
 */
object ThemeExtensions {

    /**
     * Glass effect colors
     */
    @Composable
    fun glassBackground(darkTheme: Boolean = isSystemInDarkTheme()): Color {
        return if (darkTheme) GlassDark else GlassWhite
    }

    @Composable
    fun glassBorder(darkTheme: Boolean = isSystemInDarkTheme()): Color {
        return if (darkTheme) BorderDark else BorderLight
    }

    /**
     * Image overlay colors
     */
    @Composable
    fun imageScrim(darkTheme: Boolean = isSystemInDarkTheme()): Color {
        return if (darkTheme) ImageScrimDark else ImageScrimLight
    }

    /**
     * Elevated surface colors (for cards at different elevations)
     */
    @Composable
    fun surfaceElevated1(darkTheme: Boolean = isSystemInDarkTheme()): Color {
        return if (darkTheme) SurfaceElevated1Dark else SurfaceElevated1Light
    }

    @Composable
    fun surfaceElevated2(darkTheme: Boolean = isSystemInDarkTheme()): Color {
        return if (darkTheme) SurfaceElevated2Dark else SurfaceElevated2Light
    }

    @Composable
    fun surfaceElevated3(darkTheme: Boolean = isSystemInDarkTheme()): Color {
        return if (darkTheme) SurfaceElevated3Dark else SurfaceElevated3Light
    }

    /**
     * Hover and interactive states
     */
    @Composable
    fun hover(darkTheme: Boolean = isSystemInDarkTheme()): Color {
        return if (darkTheme) HoverDark else HoverLight
    }

    @Composable
    fun pressed(darkTheme: Boolean = isSystemInDarkTheme()): Color {
        return if (darkTheme) PressedDark else PressedLight
    }
}

// ============================================
// USAGE EXAMPLES
// ============================================

/**
 * Example: Glass Card
 *
 * Card(
 *     modifier = Modifier
 *         .blur(12.dp)  // Requires experimental API
 *         .background(ThemeExtensions.glassBackground())
 *         .border(
 *             width = 0.5.dp,
 *             color = ThemeExtensions.glassBorder(),
 *             shape = CustomShapes.GlassCard
 *         ),
 *     shape = CustomShapes.GlassCard,
 *     colors = CardDefaults.cardColors(
 *         containerColor = Color.Transparent
 *     )
 * ) {
 *     // Content
 * }
 */

/**
 * Example: Premium Button
 *
 * Button(
 *     onClick = { },
 *     colors = ButtonDefaults.buttonColors(
 *         containerColor = MaterialTheme.colorScheme.primary,
 *         contentColor = MaterialTheme.colorScheme.onPrimary
 *     ),
 *     shape = PremiumShapes.small,
 *     elevation = ButtonDefaults.buttonElevation(
 *         defaultElevation = 0.dp,
 *         pressedElevation = 2.dp
 *     )
 * ) {
 *     Text(
 *         text = "Reserve Table",
 *         style = MaterialTheme.typography.labelLarge
 *     )
 * }
 */

/**
 * Example: Food Image Card
 *
 * Card(
 *     modifier = Modifier.fillMaxWidth(),
 *     shape = CustomShapes.ImageCard
 * ) {
 *     Box {
 *         AsyncImage(
 *             model = imageUrl,
 *             contentDescription = null,
 *             modifier = Modifier.fillMaxWidth()
 *         )
 *         // Gradient overlay for text readability
 *         Box(
 *             modifier = Modifier
 *                 .fillMaxWidth()
 *                 .height(120.dp)
 *                 .align(Alignment.BottomCenter)
 *                 .background(
 *                     Brush.verticalGradient(
 *                         colors = GradientImageOverlay
 *                     )
 *                 )
 *         )
 *         Text(
 *             text = "Truffle Risotto",
 *             style = MaterialTheme.typography.headlineSmall,
 *             color = Color.White,
 *             modifier = Modifier
 *                 .align(Alignment.BottomStart)
 *                 .padding(16.dp)
 *         )
 *     }
 * }
 */