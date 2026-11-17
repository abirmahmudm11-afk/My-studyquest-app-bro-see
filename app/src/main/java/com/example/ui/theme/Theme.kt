package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.SurfaceStyle

@Immutable
data class StudyQuestSemanticColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color = OnMutedMintContainer,
    val coral: Color = CeruleanSlate,
    val coralContainer: Color = SoftCeruleanSlate,
    val cyan: Color = SlateBotanical,
    val cyanContainer: Color = SoftSlateBotanical,
    val violet: Color = MineralTeal,
    val violetContainer: Color = SoftMineralTeal
)

@Immutable
data class StudyQuestVisualConfig(
    val surfaceStyle: SurfaceStyle = SurfaceStyle.NORMAL,
    val isDark: Boolean = false
) {
    val isGlass: Boolean get() = surfaceStyle == SurfaceStyle.GLASSMORPHISM
}

val LocalStudyQuestColors = staticCompositionLocalOf {
    StudyQuestSemanticColors(
        success = EditorialSuccess,
        successContainer = EditorialSuccessContainer,
        onSuccessContainer = OnEditorialSuccessContainer,
        warning = MutedMintDarkInk,
        warningContainer = MutedMintContainer,
        onWarningContainer = OnMutedMintContainer
    )
}

val LocalStudyQuestSurfaceStyle = staticCompositionLocalOf {
    StudyQuestVisualConfig()
}

private val StudyQuestLightScheme = lightColorScheme(
    primary = BrandTealPrimary,
    onPrimary = CrispCoolSurface,
    primaryContainer = SoftPowderBlue,
    onPrimaryContainer = OnSoftPowderBlue,
    secondary = MineralTeal,
    onSecondary = CrispCoolSurface,
    secondaryContainer = SoftMineralTeal,
    onSecondaryContainer = OnSoftMineralTeal,
    tertiary = SlateBotanical,
    onTertiary = CrispCoolSurface,
    tertiaryContainer = MutedMintContainer,
    onTertiaryContainer = OnMutedMintContainer,
    background = CoolIceGrayCanvas,
    onBackground = DeepSlateInk,
    surface = CrispCoolSurface,
    onSurface = DeepSlateInk,
    surfaceVariant = SoftCoolMistSurface,
    onSurfaceVariant = MutedSlateInk,
    surfaceContainerLowest = CrispCoolSurface,
    surfaceContainerLow = Color(0xFFF8FAFD),
    surfaceContainer = SoftCoolMistSurface,
    surfaceContainerHigh = CoolSurfaceVariant,
    outline = StrongCoolBorder,
    outlineVariant = CoolHairlineBorder,
    error = EditorialError,
    onError = CrispCoolSurface,
    errorContainer = EditorialErrorContainer,
    onErrorContainer = OnEditorialErrorContainer
)

private val StudyQuestDarkScheme = darkColorScheme(
    primary = DarkSlatePrimary,
    onPrimary = Color(0xFF0A2426),
    primaryContainer = DarkSlatePrimaryContainer,
    onPrimaryContainer = Color(0xFFCDEEE9),
    secondary = DarkSlateSecondary,
    onSecondary = Color(0xFF0B2036),
    secondaryContainer = DarkSlateSecondaryContainer,
    onSecondaryContainer = Color(0xFFD5E6F8),
    tertiary = DarkSlateTertiary,
    onTertiary = Color(0xFF0A2623),
    tertiaryContainer = DarkSlateTertiaryContainer,
    onTertiaryContainer = Color(0xFFCDF1EA),
    background = DarkSlateBackground,
    onBackground = DarkOnSurface,
    surface = DarkSlateSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSlateSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSlateBackground,
    surfaceContainerLow = DarkSlateSurface,
    surfaceContainer = DarkSlateSurfaceElevated,
    surfaceContainerHigh = DarkSlateSurfaceHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.9f),
    error = DarkErrorRose,
    onError = Color(0xFF47161B),
    errorContainer = DarkErrorContainer,
    onErrorContainer = Color(0xFFFAD7DC)
)

val StudyQuestShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun StudyQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isBangla: Boolean = false,
    surfaceStyle: SurfaceStyle = SurfaceStyle.NORMAL,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudyQuestDarkScheme else StudyQuestLightScheme
    val semanticColors = if (darkTheme) {
        StudyQuestSemanticColors(
            success = DarkScholarGreen,
            successContainer = DarkSlateTertiaryContainer,
            onSuccessContainer = Color(0xFFCFF2EA),
            warning = DarkSlateSecondary,
            warningContainer = DarkSlateSecondaryContainer,
            onWarningContainer = Color(0xFFD7E8FA),
            coral = DarkSlateSecondary,
            coralContainer = DarkSlateSecondaryContainer,
            cyan = Color(0xFF7CB9D4),
            cyanContainer = Color(0xFF1A3344),
            violet = DarkSlatePrimary,
            violetContainer = DarkSlatePrimaryContainer
        )
    } else {
        StudyQuestSemanticColors(
            success = EditorialSuccess,
            successContainer = EditorialSuccessContainer,
            onSuccessContainer = OnEditorialSuccessContainer,
            warning = MutedMintDarkInk,
            warningContainer = MutedMintContainer,
            onWarningContainer = OnMutedMintContainer,
            coral = CeruleanSlate,
            coralContainer = SoftCeruleanSlate,
            cyan = SlateBotanical,
            cyanContainer = SoftSlateBotanical,
            violet = MineralTeal,
            violetContainer = SoftMineralTeal
        )
    }

    val visualConfig = StudyQuestVisualConfig(surfaceStyle = surfaceStyle, isDark = darkTheme)

    CompositionLocalProvider(
        LocalStudyQuestColors provides semanticColors,
        LocalStudyQuestSurfaceStyle provides visualConfig
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = studyQuestTypography(isBangla = isBangla),
            shapes = StudyQuestShapes,
            content = content
        )
    }
}

@Composable
fun studyQuestGlassAwareSurfaceColor(baseColor: Color = MaterialTheme.colorScheme.surface): Color {
    val config = LocalStudyQuestSurfaceStyle.current
    if (!config.isGlass) return baseColor
    return if (baseColor == MaterialTheme.colorScheme.surface) {
        if (config.isDark) GlassDarkSurface else GlassLightSurface
    } else {
        baseColor.copy(alpha = if (config.isDark) 0.80f else 0.90f)
    }
}

@Composable
fun studyQuestGlassAwareBorder(accent: Color? = null): BorderStroke {
    val config = LocalStudyQuestSurfaceStyle.current
    if (!config.isGlass) {
        return BorderStroke(
            width = 1.dp,
            color = accent?.copy(alpha = 0.24f) ?: MaterialTheme.colorScheme.outlineVariant
        )
    }
    val topHighlight = if (config.isDark) {
        accent?.copy(alpha = 0.42f) ?: Color.White.copy(alpha = 0.22f)
    } else {
        Color.White.copy(alpha = 0.92f)
    }
    val bottomEdge = if (config.isDark) {
        DarkSlatePrimary.copy(alpha = 0.30f)
    } else {
        accent?.copy(alpha = 0.24f) ?: BrandTealPrimary.copy(alpha = 0.16f)
    }
    return BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(listOf(topHighlight, bottomEdge))
    )
}
