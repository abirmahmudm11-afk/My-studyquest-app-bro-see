package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme — Premium Soft Minimalism & Editorial Quest Palette
val WarmParchmentCanvas = Color(0xFFF7F8F4)       // Background
val PureEditorialSurface = Color(0xFFFFFFFF)      // Primary surface
val SoftStoneSurface = Color(0xFFF1F3ED)          // Subtle secondary fill
val EditorialSurfaceVariant = Color(0xFFEDF0E8)   // Tonal card / chip fill

// Brand Forest Greens & Quest Gold
val BrandForestGreen = Color(0xFF286A50)          // Brand green primary CTA & highlights
val DeepForestGreen = Color(0xFF1F553F)           // Deep green hover / contrast hero
val SoftSageGreen = Color(0xFFE8F1EB)             // Soft green active capsules & highlights
val OnSoftSageGreen = Color(0xFF163D2D)

val QuestGold = Color(0xFFE7B95F)                 // Quest gold milestones & streak ribbons
val DeepQuestGold = Color(0xFFB38326)             // High-contrast gold ink on light
val SoftQuestGold = Color(0xFFFAF1DD)             // Soft gold streaks & positive highlights
val OnSoftQuestGold = Color(0xFF543C0A)

// Botanical & Mineral Supporting Accents (restrained, harmonious)
val MineralTeal = Color(0xFF2D7D6F)
val SoftMineralTeal = Color(0xFFE1F1EE)
val OnSoftMineralTeal = Color(0xFF12423A)

val WarmTerracotta = Color(0xFFC97343)
val SoftTerracotta = Color(0xFFFAECE4)
val OnSoftTerracotta = Color(0xFF5D2D12)

val SlateBotanical = Color(0xFF3E6373)
val SoftSlateBotanical = Color(0xFFE5EEF2)

// Editorial Ink & Hairline Borders
val DeepForestInk = Color(0xFF202C27)             // Main text
val MutedMossInk = Color(0xFF758078)              // Secondary text
val SubtleForestInk = Color(0xFF47544D)
val EditorialBorder = Color(0xFFE7EBE5)           // Primary hairline border
val StrongEditorialBorder = Color(0xFFD2D9CF)

// Semantic Status Tokens
val EditorialSuccess = Color(0xFF27865C)
val EditorialSuccessContainer = Color(0xFFE2F3EB)
val OnEditorialSuccessContainer = Color(0xFF134830)

val EditorialError = Color(0xFFC34F50)
val EditorialErrorContainer = Color(0xFFFAE6E6)
val OnEditorialErrorContainer = Color(0xFF6B1E1F)

// Selective Frosted Liquid Glass Tokens (whisper-light editorial translucency)
val GlassLightSurface = Color(0xE6FFFFFF)         // 90% frosted editorial white
val GlassLightElevated = Color(0xDDE8F1EB)        // 86% frosted sage mist
val GlassLightBorder = Color(0xD9FFFFFF)
val GlassDarkSurface = Color(0xBF17221E)          // 75% dark pine glass
val GlassDarkElevated = Color(0xCC1E2C27)         // 80% elevated dark pine glass
val GlassDarkBorder = Color(0x409AA8A0)

// Coordinated Dark Theme — Deep Charcoal-Green Night Studio
val DarkForestBackground = Color(0xFF111916)
val DarkForestSurface = Color(0xFF17221E)
val DarkForestSurfaceElevated = Color(0xFF1E2C27)
val DarkForestSurfaceHigh = Color(0xFF263731)
val DarkForestPrimary = Color(0xFF499F7C)
val DarkForestPrimaryContainer = Color(0xFF1F3B30)
val DarkForestSecondary = Color(0xFF6EC2A3)
val DarkForestSecondaryContainer = Color(0xFF183D32)
val DarkForestTertiary = Color(0xFFE7B95F)
val DarkForestTertiaryContainer = Color(0xFF362B14)
val DarkOnSurface = Color(0xFFEDF2EE)
val DarkOnSurfaceVariant = Color(0xFF9AA8A0)
val DarkOutline = Color(0xFF283932)
val DarkErrorRose = Color(0xFFE27C7D)
val DarkErrorContainer = Color(0xFF4B2122)

// Unified Compatibility Aliases (ensures all existing modules inherit the editorial forest palette)
val PureCardWhite = PureEditorialSurface
val PureWhite = PureEditorialSurface
val VividLightCanvas = WarmParchmentCanvas
val CrispWhiteCanvas = WarmParchmentCanvas
val SoftSlateCanvas = WarmParchmentCanvas
val VividSoftFill = SoftStoneSurface
val CoolNeutralFill = SoftStoneSurface
val SoftSlateContainer = SoftStoneSurface
val VividSurfaceVariant = EditorialSurfaceVariant
val CoolSurfaceVariant = EditorialSurfaceVariant

val ElectricIndigo = BrandForestGreen
val ElectricIndigoDark = DeepForestGreen
val ForestGreenPrimary = BrandForestGreen
val ForestGreenDark = DeepForestGreen
val CobaltPrimary = BrandForestGreen
val CobaltDark = DeepForestGreen
val IndigoContainer = SoftSageGreen
val SageContainer = SoftSageGreen
val CobaltContainer = SoftSageGreen
val OnIndigoContainer = OnSoftSageGreen
val OnSageContainer = OnSoftSageGreen
val OnCobaltContainer = OnSoftSageGreen

val VividEmerald = EditorialSuccess
val VividEmeraldDark = DeepForestGreen
val NaturalGreen = EditorialSuccess
val ScholarGreen = EditorialSuccess
val TealSecondary = MineralTeal
val EmeraldContainer = EditorialSuccessContainer
val NaturalGreenContainer = EditorialSuccessContainer
val ScholarGreenContainer = EditorialSuccessContainer
val TealContainer = SoftMineralTeal
val OnEmeraldContainer = OnEditorialSuccessContainer
val OnNaturalGreenContainer = OnEditorialSuccessContainer
val OnScholarGreenContainer = OnEditorialSuccessContainer
val OnTealContainer = OnSoftMineralTeal

val BerryViolet = MineralTeal
val VioletTertiary = MineralTeal
val BerryVioletContainer = SoftMineralTeal
val VioletContainer = SoftMineralTeal
val OnBerryVioletContainer = OnSoftMineralTeal
val OnVioletContainer = OnSoftMineralTeal

val SkyCyan = SlateBotanical
val SkyCyanAccent = SlateBotanical
val SkyCyanContainer = SoftSlateBotanical
val OnSkyCyanContainer = DeepForestInk

val SunsetCoral = WarmTerracotta
val SunsetCoralContainer = SoftTerracotta
val OnSunsetCoralContainer = OnSoftTerracotta

val DeepInk = DeepForestInk
val MutedSlate = MutedMossInk
val SubtleSlateAccent = SubtleForestInk
val CoolOutline = StrongEditorialBorder
val SubtleOutline = EditorialBorder

val AmberGold = QuestGold
val AmberGoldContainer = SoftQuestGold
val SilverAccent = Color(0xFF8C9790)
val BronzeAccent = Color(0xFFB77B42)
val RoseAlert = EditorialError
val RoseAlertContainer = EditorialErrorContainer
val AccessibleRed = EditorialError
val AccessibleRedContainer = EditorialErrorContainer

val DarkMidnightBackground = DarkForestBackground
val DarkMidnightSurface = DarkForestSurface
val DarkMidnightSurfaceElevated = DarkForestSurfaceElevated
val DarkForestContainer = DarkForestPrimaryContainer
val DarkNaturalSecondary = DarkForestSecondary
val DarkSlateTertiary = DarkForestTertiary
val DarkSlateContainer = DarkForestTertiaryContainer
val DarkCobaltPrimary = DarkForestPrimary
val DarkCobaltPrimaryContainer = DarkForestPrimaryContainer
val DarkTealSecondary = DarkForestSecondary
val DarkVioletTertiary = DarkForestTertiary
val DarkVioletContainer = DarkForestTertiaryContainer
val DarkScholarGreen = Color(0xFF4DB380)
val DarkScholarGreenContainer = DarkForestSecondaryContainer
val DarkAccessibleRed = DarkErrorRose
