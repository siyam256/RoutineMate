package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// RESTORED PREVIOUS MODERN THEME:
// INDIGO • VIOLET • EMERALD • SLATE
// Premium, minimalist, sleek & soothing aesthetic
// ==========================================

// --- Primary Indigo & Violet Spectrum ---
val Indigo600 = Color(0xFF4F46E5)              // Vibrant Indigo Primary
val Indigo500 = Color(0xFF6366F1)              // Soft Indigo
val Indigo400 = Color(0xFF818CF8)              // Electric Lavender Indigo
val Indigo100 = Color(0xFFE0E7FF)              // Very light indigo tint
val Indigo50 = Color(0xFFEEF2FF)               // Light indigo surface
val Violet500 = Color(0xFF8B5CF6)              // Rich Violet Accent
val Violet400 = Color(0xFFA78BFA)              // Light Violet

// --- Accent Emerald & Teal Spectrum ---
val Emerald500 = Color(0xFF10B981)             // Vibrant Emerald for completions
val Emerald400 = Color(0xFF34D399)             // Mint Emerald
val Emerald600 = Color(0xFF059669)             // Deep Emerald
val Teal500 = Color(0xFF14B8A6)                // Calming Teal

// --- Warm Amber Accent ---
val Amber500 = Color(0xFFF59E0B)               // Warm Amber for warning / due
val Amber400 = Color(0xFFFBBF24)               // Soft Amber

// --- Signature Gradients ---
val BrandGradient = Brush.horizontalGradient(
    listOf(Indigo600, Violet500)
)

val EmeraldGradient = Brush.horizontalGradient(
    listOf(Emerald400, Emerald600)
)

val AlertGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEF4444), Color(0xFFDC2626))
)

// --- Midnight Slate Dark Mode (Linear / GitHub Dark style) ---
val DarkBackground = Color(0xFF0B0F19)         // Deep midnight slate
val DarkSurface = Color(0xFF111827)            // Dark slate surface
val DarkCard = Color(0xFF1F2937)               // Elevated card background
val DarkCardBorder = Color(0xFF374151)         // Subtle slate border
val DarkTextPrimary = Color(0xFFF9FAFB)        // Crisp white text
val DarkTextSecondary = Color(0xFF9CA3AF)      // Muted slate secondary text
val DarkTextMuted = Color(0xFF6B7280)          // Tertiary subtle text

// --- Crisp Clean Light Mode ---
val LightBackground = Color(0xFFF8FAFC)        // Soft slate background
val LightSurface = Color(0xFFFFFFFF)           // Pure white surface
val LightCard = Color(0xFFFFFFFF)              // Pure white card
val LightCardBorder = Color(0xFFE2E8F0)        // Clean subtle border
val LightTextPrimary = Color(0xFF0F172A)       // Deep slate text
val LightTextSecondary = Color(0xFF475569)     // Secondary slate text
val LightTextMuted = Color(0xFF94A3B8)         // Muted placeholder text
