package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.ThemeMode

// ==========================================
// BLACK & WHITE MINIMALIST THEME
// Pure Monochrome. Zero Gradients. Ultra Clean.
// ==========================================

private val DarkColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoDarkCardElevated,
    onPrimaryContainer = MonoWhite,
    secondary = MonoDarkTextSecondary,
    onSecondary = MonoBlack,
    secondaryContainer = MonoDarkCard,
    onSecondaryContainer = MonoWhite,
    tertiary = MonoWhite,
    onTertiary = MonoBlack,
    background = MonoDarkBackground,
    onBackground = MonoDarkTextPrimary,
    surface = MonoDarkSurface,
    onSurface = MonoDarkTextPrimary,
    surfaceVariant = MonoDarkCard,
    onSurfaceVariant = MonoDarkTextSecondary,
    outline = MonoDarkBorder,
    outlineVariant = MonoDarkBorderLight,
    error = MonoWhite,
    onError = MonoBlack,
    errorContainer = MonoDarkCardElevated,
    onErrorContainer = MonoWhite
)

private val LightColorScheme = lightColorScheme(
    primary = MonoBlack,
    onPrimary = MonoWhite,
    primaryContainer = MonoLightCardElevated,
    onPrimaryContainer = MonoBlack,
    secondary = MonoLightTextSecondary,
    onSecondary = MonoWhite,
    secondaryContainer = MonoLightSurface,
    onSecondaryContainer = MonoBlack,
    tertiary = MonoBlack,
    onTertiary = MonoWhite,
    background = MonoLightBackground,
    onBackground = MonoLightTextPrimary,
    surface = MonoLightSurface,
    onSurface = MonoLightTextPrimary,
    surfaceVariant = MonoLightCard,
    onSurfaceVariant = MonoLightTextSecondary,
    outline = MonoLightBorder,
    outlineVariant = MonoLightBorderDark,
    error = MonoBlack,
    onError = MonoWhite,
    errorContainer = MonoLightCardElevated,
    onErrorContainer = MonoBlack
)

@Composable
fun RoutineMateTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
