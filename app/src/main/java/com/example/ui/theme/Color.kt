package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme — Crisp Neutral White & Selective Natural Green (Chorcha-beating editorial clarity)
// Zero yellowish tint, zero blinding pure-white canvas glare, zero AI-slop neon/purple.
val CrispWhiteCanvas = Color(0xFFF6F7F9)      // Calm neutral soft-white window canvas
val PureCardWhite = Color(0xFFFFFFFF)         // Crisp white elevated cards & sheets
val CoolNeutralFill = Color(0xFFEEF1F5)       // Neutral cool-gray container fill
val CoolSurfaceVariant = Color(0xFFE5E9F0)    // Subtle tonal surface variant

// Selective Natural Botanical Green — reserved strictly for primary CTAs, active tabs, & verified success
val ForestGreenPrimary = Color(0xFF166534)    // Rich natural forest green (non-neon)
val ForestGreenDark = Color(0xFF14532D)
val SageContainer = Color(0xFFEAF5EE)         // Very restrained soft sage tint for active pills
val OnSageContainer = Color(0xFF14532D)

val NaturalGreen = Color(0xFF15803D)          // Progress bars, mastery rings & verified right answers
val NaturalGreenContainer = Color(0xFFDCFCE7)
val OnNaturalGreenContainer = Color(0xFF14532D)

// Neutral Charcoal & Slate Hierarchy (carries 85%+ of the interface)
val DeepInk = Color(0xFF111827)               // Crisp charcoal body & heading ink
val MutedSlate = Color(0xFF4B5563)            // Readable secondary ink
val SubtleSlateAccent = Color(0xFF334155)     // Neutral dark slate for secondary badges/headers
val CoolOutline = Color(0xFFD1D5DB)
val SubtleOutline = Color(0xFFE5E7EB)         // Crisp 1dp card & divider hairline

// Strictly Functional Semantic Accents (never decorative)
val AmberGold = Color(0xFFB45309)             // Muted warm amber for streaks & XP warnings
val AmberGoldContainer = Color(0xFFFEF3C7)
val RoseAlert = Color(0xFFDC2626)             // Accessible red for errors & wrong answers
val RoseAlertContainer = Color(0xFFFEE2E2)
val AccessibleRed = Color(0xFFDC2626)
val AccessibleRedContainer = Color(0xFFFEE2E2)

// Semantic Compatibility Aliases (ensures zero neon/purple/cobalt AI-slop anywhere in legacy call sites)
val PureWhite = PureCardWhite
val SoftSlateCanvas = CrispWhiteCanvas
val SoftSlateContainer = CoolNeutralFill
val ScholarGreen = NaturalGreen
val ScholarGreenContainer = NaturalGreenContainer
val OnScholarGreenContainer = OnNaturalGreenContainer
val CobaltPrimary = ForestGreenPrimary
val CobaltDark = Color(0xFF1F2937)            // Calm dark charcoal for contrast headers
val CobaltContainer = SageContainer
val OnCobaltContainer = OnSageContainer
val TealSecondary = NaturalGreen
val TealContainer = SageContainer
val OnTealContainer = OnSageContainer
val VioletTertiary = SubtleSlateAccent
val VioletContainer = CoolNeutralFill
val OnVioletContainer = DeepInk
val SkyCyanAccent = Color(0xFF22C55E)         // Muted leaf green highlight on dark surfaces

// Accessible Dark Theme — Clean Deep Charcoal (Settings Toggle)
val DarkMidnightBackground = Color(0xFF0F1419)
val DarkMidnightSurface = Color(0xFF171D24)
val DarkMidnightSurfaceElevated = Color(0xFF1F2730)
val DarkForestPrimary = Color(0xFF4ADE80)
val DarkForestContainer = Color(0xFF14532D)
val DarkNaturalSecondary = Color(0xFF34D399)
val DarkSlateTertiary = Color(0xFF94A3B8)
val DarkSlateContainer = Color(0xFF26313E)
val DarkCobaltPrimary = DarkForestPrimary
val DarkCobaltPrimaryContainer = DarkForestContainer
val DarkTealSecondary = DarkNaturalSecondary
val DarkVioletTertiary = DarkSlateTertiary
val DarkVioletContainer = DarkSlateContainer
val DarkScholarGreen = Color(0xFF4ADE80)
val DarkScholarGreenContainer = Color(0xFF14532D)
val DarkOnSurface = Color(0xFFF3F4F6)
val DarkOnSurfaceVariant = Color(0xFF9CA3AF)
val DarkOutline = Color(0xFF2D3748)
val DarkAccessibleRed = Color(0xFFFCA5A5)
