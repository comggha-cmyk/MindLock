package com.disciplinex.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
// Color Palette — Dark, electric, focused
// ─────────────────────────────────────────────
object DXColors {
    // Backgrounds
    val Background = Color(0xFF080A0F)
    val Surface = Color(0xFF0F1219)
    val SurfaceVariant = Color(0xFF161B27)
    val CardSurface = Color(0xFF1A2030)

    // Accent — Electric cyan/blue
    val Primary = Color(0xFF00E5FF)
    val PrimaryDim = Color(0xFF0099BB)
    val PrimaryContainer = Color(0xFF003344)

    // Secondary — Lime green for "go" / success
    val Secondary = Color(0xFF39FF14)
    val SecondaryDim = Color(0xFF2ACC10)

    // Danger — Alarm / block red
    val Danger = Color(0xFFFF2244)
    val DangerDim = Color(0xFFCC1133)

    // Warning — Orange for timers
    val Warning = Color(0xFFFF8800)
    val WarningDim = Color(0xFFCC6600)

    // Text
    val OnBackground = Color(0xFFEEF2FF)
    val OnBackgroundMuted = Color(0xFF8892AA)
    val OnBackgroundFaint = Color(0xFF404860)

    // Overlay
    val Overlay = Color(0xAA000000)
    val GlassOverlay = Color(0x22FFFFFF)

    // Focus gradient colors
    val GradientStart = Color(0xFF0A0E18)
    val GradientEnd = Color(0xFF0F1820)
}

private val DarkColorScheme = darkColorScheme(
    primary = DXColors.Primary,
    onPrimary = DXColors.Background,
    primaryContainer = DXColors.PrimaryContainer,
    secondary = DXColors.Secondary,
    background = DXColors.Background,
    surface = DXColors.Surface,
    surfaceVariant = DXColors.SurfaceVariant,
    onBackground = DXColors.OnBackground,
    onSurface = DXColors.OnBackground,
    onSurfaceVariant = DXColors.OnBackgroundMuted,
    error = DXColors.Danger
)

@Composable
fun DisciplineXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = DXTypography,
        content = content
    )
}

val DXTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 1.25.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    )
)
