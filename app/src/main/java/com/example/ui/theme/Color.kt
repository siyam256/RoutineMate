package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// PURE BLACK & WHITE MINIMALIST COLOR SYSTEM
// Strictly Monochrome. Pure Black, Pure White,
// Refined Neutral Grayscale. Crisp Hairlines.
// Zero Gradients. Zero Colors. High Luxury Aesthetic.
// ==========================================

// --- Monochrome Pure Accents ---
val MonoBlack = Color(0xFF000000)
val MonoWhite = Color(0xFFFFFFFF)

// --- Monochrome Dark Theme (OLED Black Minimalist) ---
val MonoDarkBackground = Color(0xFF000000)       // Pure OLED Deep Black
val MonoDarkSurface = Color(0xFF0D0D0E)          // Deep clean dark surface
val MonoDarkCard = Color(0xFF141416)             // Refined card surface
val MonoDarkCardElevated = Color(0xFF1B1B1E)     // Elevated hover / selected card
val MonoDarkBorder = Color(0xFF27272A)           // Crisp hairline border
val MonoDarkBorderLight = Color(0xFF3F3F46)      // Active / highlighted border
val MonoDarkTextPrimary = Color(0xFFFAFAFA)      // Pure crisp white text
val MonoDarkTextSecondary = Color(0xFFA1A1AA)    // Balanced neutral gray
val MonoDarkTextMuted = Color(0xFF71717A)        // Subdued hint / helper text
val MonoDarkNodeRing = Color(0xFFE4E4E7)         // Timeline active node ring
val MonoDarkLine = Color(0xFF27272A)             // Timeline connecting line

// --- Monochrome Light Theme (Clean Minimalist White) ---
val MonoLightBackground = Color(0xFFFFFFFF)      // Pure clean white
val MonoLightSurface = Color(0xFFF7F7F8)         // Subtle off-white surface
val MonoLightCard = Color(0xFFFFFFFF)            // Clean white card
val MonoLightCardElevated = Color(0xFFF4F4F5)    // Slightly shaded surface
val MonoLightBorder = Color(0xFFE4E4E7)          // Subtle clean divider / border
val MonoLightBorderDark = Color(0xFF27272A)      // Active / highlighted border
val MonoLightTextPrimary = Color(0xFF09090B)     // Jet black typography
val MonoLightTextSecondary = Color(0xFF52525B)   // Neutral dark gray
val MonoLightTextMuted = Color(0xFFA1A1AA)       // Muted text
val MonoLightNodeRing = Color(0xFF18181B)        // Timeline active node ring
val MonoLightLine = Color(0xFFE4E4E7)            // Timeline connecting line

// --- Backward-Compatible Monochrome Aliases ---
// Any component referencing these will automatically render in pure monochrome
val WhatsAppGreenPrimary = MonoWhite
val WhatsAppGreenDark = MonoBlack
val WhatsAppGreenDeep = MonoBlack
val WhatsAppGreenBright = MonoWhite
val WhatsAppGreenLight = Color(0xFF27272A)
val WhatsAppGreenContainerDark = Color(0xFF27272A)

val WhatsAppRed = MonoWhite
val WhatsAppRedContainer = Color(0xFF27272A)
val WhatsAppRedDarkContainer = Color(0xFF18181B)

val WhatsAppAmber = MonoWhite
val WhatsAppAmberContainer = Color(0xFF27272A)
val WhatsAppAmberLight = Color(0xFFF4F4F5)

val WhatsAppDarkBackground = MonoDarkBackground
val WhatsAppDarkSurface = MonoDarkSurface
val WhatsAppDarkCard = MonoDarkCard
val WhatsAppDarkBorder = MonoDarkBorder
val WhatsAppDarkTextPrimary = MonoDarkTextPrimary
val WhatsAppDarkTextSecondary = MonoDarkTextSecondary
val WhatsAppDarkTextMuted = MonoDarkTextMuted

val WhatsAppLightBackground = MonoLightBackground
val WhatsAppLightSurface = MonoLightSurface
val WhatsAppLightCard = MonoLightCard
val WhatsAppLightBorder = MonoLightBorder
val WhatsAppLightTextPrimary = MonoLightTextPrimary
val WhatsAppLightTextSecondary = MonoLightTextSecondary
val WhatsAppLightTextMuted = MonoLightTextMuted
