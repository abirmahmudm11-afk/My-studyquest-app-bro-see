package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme — Cool Blue-Gray Soft Minimalism & Reference-Inspired Palette
val CoolIceGrayCanvas = Color(0xFFF3F6FA)         // Primary background canvas
val CrispCoolSurface = Color(0xFFFFFFFF)          // Crisp elevated surface cards
val SoftCoolMistSurface = Color(0xFFE9EFF6)       // Secondary cool surface fill
val CoolSurfaceVariant = Color(0xFFE1E8F2)        // Tonal card / chip fill

// Hero Stage Slate Blue-Gray & Atmospheric Accents
val HeroSlateBlueTop = Color(0xFF6F8CA9)          // Atmospheric slate blue-gray hero band top
val HeroSlateBlueBottom = Color(0xFF5C7A97)       // Atmospheric slate blue-gray hero band bottom
val HeroGlassStageLight = Color(0x38FFFFFF)       // Central frosted glass frame in hero
val HeroGlassBorderLight = Color(0x80FFFFFF)      // Crisp translucent glass edge
val HeroDarkDockSlate = Color(0xDB435568)         // Floating dark translucent 3-action pill dock

// Brand Soft Teal, Muted Mint & Powder Blue
val BrandTealPrimary = Color(0xFF237B7A)          // Primary CTA & active teal highlights
val DeepSlateTeal = Color(0xFF1A5E63)             // High-contrast deep teal
val SoftPowderBlue = Color(0xFFE0EDF7)            // Active pills & powder-blue containers
val OnSoftPowderBlue = Color(0xFF143547)

val MutedMintFocal = Color(0xFF45B3A2)            // Muted mint focal highlight
val MutedMintDarkInk = Color(0xFF196E64)          // High-contrast mint-teal ink on light
val MutedMintContainer = Color(0xFFD9F1EC)        // Soft muted mint card tint
val OnMutedMintContainer = Color(0xFF103C37)
val HeroMintCardStart = Color(0xFF9EDCCF)         // Soft muted mint focal hero card gradient start
val HeroMintCardEnd = Color(0xFF79C7BA)           // Soft muted mint focal hero card gradient end

// Cool Supporting Accents (Slate Blue, Mineral Teal, Cerulean-Indigo)
val MineralTeal = Color(0xFF2C8085)
val SoftMineralTeal = Color(0xFFDEF0F2)
val OnSoftMineralTeal = Color(0xFF113E42)

val CeruleanSlate = Color(0xFF496F96)
val SoftCeruleanSlate = Color(0xFFE3ECF6)
val OnSoftCeruleanSlate = Color(0xFF1B3450)

val SlateBotanical = Color(0xFF476988)
val SoftSlateBotanical = Color(0xFFE2ECF5)

// Deep Slate Typography & Cool Hairline Borders
val DeepSlateInk = Color(0xFF162230)              // Primary deep slate text
val MutedSlateInk = Color(0xFF5E7083)             // Secondary cool slate text
val SubtleSlateInk = Color(0xFF3A4C60)
val CoolHairlineBorder = Color(0xFFDFE6F0)        // Primary hairline border
val StrongCoolBorder = Color(0xFFC7D3E2)

// Semantic Status Tokens
val EditorialSuccess = Color(0xFF20846C)
val EditorialSuccessContainer = Color(0xFFDCF2EC)
val OnEditorialSuccessContainer = Color(0xFF0F4437)

val EditorialError = Color(0xFFC44D58)
val EditorialErrorContainer = Color(0xFFF9E5E8)
val OnEditorialErrorContainer = Color(0xFF661B23)

// Selective Frosted Liquid Glass Tokens
val GlassLightSurface = Color(0xECFFFFFF)          // 92% frosted cool white
val GlassLightElevated = Color(0xDEE0EDF7)         // 87% frosted powder-blue mist
val GlassLightBorder = Color(0xE0FFFFFF)
val GlassDarkSurface = Color(0xC715202E)           // 78% deep slate-navy glass
val GlassDarkElevated = Color(0xD41C2A3B)          // 83% elevated slate-navy glass
val GlassDarkBorder = Color(0x478CA4BE)

// Coordinated Dark Theme — Deep Slate-Navy Studio
val DarkSlateBackground = Color(0xFF0E1621)
val DarkSlateSurface = Color(0xFF15202E)
val DarkSlateSurfaceElevated = Color(0xFF1C2A3B)
val DarkSlateSurfaceHigh = Color(0xFF243549)
val DarkHeroSlateTop = Color(0xFF1E3146)
val DarkHeroSlateBottom = Color(0xFF142334)
val DarkSlatePrimary = Color(0xFF56C2B2)
val DarkSlatePrimaryContainer = Color(0xFF183842)
val DarkSlateSecondary = Color(0xFF7CA7D6)
val DarkSlateSecondaryContainer = Color(0xFF1B2E46)
val DarkSlateTertiary = Color(0xFF74D6C5)
val DarkSlateTertiaryContainer = Color(0xFF173A3A)
val DarkOnSurface = Color(0xFFEDF2F8)
val DarkOnSurfaceVariant = Color(0xFF97A9BD)
val DarkOutline = Color(0xFF283A4E)
val DarkErrorRose = Color(0xFFE57F88)
val DarkErrorContainer = Color(0xFF4D2028)

// Unified Compatibility Aliases (propagates the cool blue-gray / soft-teal / muted-mint system across all modules)
val WarmParchmentCanvas = CoolIceGrayCanvas
val PureEditorialSurface = CrispCoolSurface
val SoftStoneSurface = SoftCoolMistSurface
val EditorialSurfaceVariant = CoolSurfaceVariant

val BrandForestGreen = BrandTealPrimary
val DeepForestGreen = DeepSlateTeal
val SoftSageGreen = SoftPowderBlue
val OnSoftSageGreen = OnSoftPowderBlue

val QuestGold = MutedMintFocal
val DeepQuestGold = MutedMintDarkInk
val SoftQuestGold = MutedMintContainer
val OnSoftQuestGold = OnMutedMintContainer

val WarmTerracotta = CeruleanSlate
val SoftTerracotta = SoftCeruleanSlate
val OnSoftTerracotta = OnSoftCeruleanSlate

val DeepForestInk = DeepSlateInk
val MutedMossInk = MutedSlateInk
val SubtleForestInk = SubtleSlateInk
val EditorialBorder = CoolHairlineBorder
val StrongEditorialBorder = StrongCoolBorder

val DarkForestBackground = DarkSlateBackground
val DarkForestSurface = DarkSlateSurface
val DarkForestSurfaceElevated = DarkSlateSurfaceElevated
val DarkForestSurfaceHigh = DarkSlateSurfaceHigh
val DarkForestPrimary = DarkSlatePrimary
val DarkForestPrimaryContainer = DarkSlatePrimaryContainer
val DarkForestSecondary = DarkSlateSecondary
val DarkForestSecondaryContainer = DarkSlateSecondaryContainer
val DarkForestTertiary = DarkSlateTertiary
val DarkForestTertiaryContainer = DarkSlateTertiaryContainer

val PureCardWhite = CrispCoolSurface
val PureWhite = CrispCoolSurface
val VividLightCanvas = CoolIceGrayCanvas
val CrispWhiteCanvas = CoolIceGrayCanvas
val SoftSlateCanvas = CoolIceGrayCanvas
val VividSoftFill = SoftCoolMistSurface
val CoolNeutralFill = SoftCoolMistSurface
val SoftSlateContainer = SoftCoolMistSurface
val VividSurfaceVariant = CoolSurfaceVariant

val ElectricIndigo = BrandTealPrimary
val ElectricIndigoDark = DeepSlateTeal
val ForestGreenPrimary = BrandTealPrimary
val ForestGreenDark = DeepSlateTeal
val CobaltPrimary = SlateBotanical
val CobaltDark = DeepSlateTeal
val IndigoContainer = SoftPowderBlue
val SageContainer = SoftPowderBlue
val CobaltContainer = SoftPowderBlue
val OnIndigoContainer = OnSoftPowderBlue
val OnSageContainer = OnSoftPowderBlue
val OnCobaltContainer = OnSoftPowderBlue

val VividEmerald = EditorialSuccess
val VividEmeraldDark = DeepSlateTeal
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

val BerryViolet = SlateBotanical
val VioletTertiary = CeruleanSlate
val BerryVioletContainer = SoftSlateBotanical
val VioletContainer = SoftCeruleanSlate
val OnBerryVioletContainer = DeepSlateInk
val OnVioletContainer = OnSoftCeruleanSlate

val SkyCyan = MineralTeal
val SkyCyanAccent = MutedMintFocal
val SkyCyanContainer = SoftMineralTeal
val OnSkyCyanContainer = DeepSlateInk

val SunsetCoral = CeruleanSlate
val SunsetCoralContainer = SoftCeruleanSlate
val OnSunsetCoralContainer = OnSoftCeruleanSlate

val DeepInk = DeepSlateInk
val MutedSlate = MutedSlateInk
val SubtleSlateAccent = SubtleSlateInk
val CoolOutline = StrongCoolBorder
val SubtleOutline = CoolHairlineBorder

val AmberGold = MutedMintFocal
val AmberGoldContainer = MutedMintContainer
val SilverAccent = Color(0xFF8194A8)
val BronzeAccent = Color(0xFF567494)
val RoseAlert = EditorialError
val RoseAlertContainer = EditorialErrorContainer
val AccessibleRed = EditorialError
val AccessibleRedContainer = EditorialErrorContainer

val DarkMidnightBackground = DarkSlateBackground
val DarkMidnightSurface = DarkSlateSurface
val DarkMidnightSurfaceElevated = DarkSlateSurfaceElevated
val DarkForestContainer = DarkSlatePrimaryContainer
val DarkNaturalSecondary = DarkSlateSecondary
val DarkSlateContainer = DarkSlateTertiaryContainer
val DarkCobaltPrimary = DarkSlatePrimary
val DarkCobaltPrimaryContainer = DarkSlatePrimaryContainer
val DarkTealSecondary = DarkSlateSecondary
val DarkVioletTertiary = DarkSlateSecondary
val DarkVioletContainer = DarkSlateSecondaryContainer
val DarkScholarGreen = Color(0xFF53C2A8)
val DarkScholarGreenContainer = DarkSlateSecondaryContainer
val DarkAccessibleRed = DarkErrorRose
