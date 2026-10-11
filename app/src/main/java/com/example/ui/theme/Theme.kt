package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.ThemeMode

// ==========================================
// WHATSAPP SOLID GREEN THEME
// Clean, Solid, High-Performance, Compact
// ==========================================

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppGreenPrimary,
    onPrimary = Color(0xFF111B21),
    primaryContainer = WhatsAppGreenContainerDark,
    onPrimaryContainer = WhatsAppGreenLight,
    secondary = WhatsAppGreenBright,
    onSecondary = Color(0xFF0B141A),
    secondaryContainer = Color(0xFF1F2C34),
    onSecondaryContainer = WhatsAppGreenBright,
    tertiary = WhatsAppGreenPrimary,
    onTertiary = Color.White,
    background = WhatsAppDarkBackground,
    onBackground = WhatsAppDarkTextPrimary,
    surface = WhatsAppDarkSurface,
    onSurface = WhatsAppDarkTextPrimary,
    surfaceVariant = WhatsAppDarkCard,
    onSurfaceVariant = WhatsAppDarkTextSecondary,
    outline = WhatsAppDarkBorder,
    error = WhatsAppRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppGreenDark,
    onPrimary = Color.White,
    primaryContainer = WhatsAppGreenLight,
    onPrimaryContainer = WhatsAppGreenDeep,
    secondary = WhatsAppGreenPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7FCE9),
    onSecondaryContainer = WhatsAppGreenDark,
    tertiary = WhatsAppGreenPrimary,
    onTertiary = Color.White,
    background = WhatsAppLightBackground,
    onBackground = WhatsAppLightTextPrimary,
    surface = WhatsAppLightSurface,
    onSurface = WhatsAppLightTextPrimary,
    surfaceVariant = WhatsAppLightCard,
    onSurfaceVariant = WhatsAppLightTextSecondary,
    outline = WhatsAppLightBorder,
    error = WhatsAppRed,
    onError = Color.White
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
