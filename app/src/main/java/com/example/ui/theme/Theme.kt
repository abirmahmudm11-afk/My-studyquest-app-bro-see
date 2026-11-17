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
    val onWarningContainer: Color = OnSoftQuestGold,
    val coral: Color = WarmTerracotta,
    val coralContainer: Color = SoftTerracotta,
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
        warning = DeepQuestGold,
        warningContainer = SoftQuestGold,
        onWarningContainer = OnSoftQuestGold
    )
}

val LocalStudyQuestSurfaceStyle = staticCompositionLocalOf {
    StudyQuestVisualConfig()
}

private val StudyQuestLightScheme = lightColorScheme(
    primary = BrandForestGreen,
    onPrimary = PureEditorialSurface,
    primaryContainer = SoftSageGreen,
    onPrimaryContainer = OnSoftSageGreen,
    secondary = MineralTeal,
    onSecondary = PureEditorialSurface,
    secondaryContainer = SoftMineralTeal,
    onSecondaryContainer = OnSoftMineralTeal,
    tertiary = DeepQuestGold,
    onTertiary = PureEditorialSurface,
    tertiaryContainer = SoftQuestGold,
    onTertiaryContainer = OnSoftQuestGold,
    background = WarmParchmentCanvas,
    onBackground = DeepForestInk,
    surface = PureEditorialSurface,
    onSurface = DeepForestInk,
    surfaceVariant = SoftStoneSurface,
    onSurfaceVariant = MutedMossInk,
    surfaceContainerLowest = PureEditorialSurface,
    surfaceContainerLow = Color(0xFFFBFBF8),
    surfaceContainer = SoftStoneSurface,
    surfaceContainerHigh = EditorialSurfaceVariant,
    outline = StrongEditorialBorder,
    outlineVariant = EditorialBorder,
    error = EditorialError,
    onError = PureEditorialSurface,
    errorContainer = EditorialErrorContainer,
    onErrorContainer = OnEditorialErrorContainer
)

private val StudyQuestDarkScheme = darkColorScheme(
    primary = DarkForestPrimary,
    onPrimary = Color(0xFF0A2419),
    primaryContainer = DarkForestPrimaryContainer,
    onPrimaryContainer = Color(0xFFD6EFE3),
    secondary = DarkForestSecondary,
    onSecondary = Color(0xFF07251C),
    secondaryContainer = DarkForestSecondaryContainer,
    onSecondaryContainer = Color(0xFFCDEEE2),
    tertiary = DarkForestTertiary,
    onTertiary = Color(0xFF332609),
    tertiaryContainer = DarkForestTertiaryContainer,
    onTertiaryContainer = Color(0xFFF9E6BB),
    background = DarkForestBackground,
    onBackground = DarkOnSurface,
    surface = DarkForestSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkForestSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkForestBackground,
    surfaceContainerLow = DarkForestSurface,
    surfaceContainer = DarkForestSurfaceElevated,
    surfaceContainerHigh = DarkForestSurfaceHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.9f),
    error = DarkErrorRose,
    onError = Color(0xFF471617),
    errorContainer = DarkErrorContainer,
    onErrorContainer = Color(0xFFFAD7D7)
)

val StudyQuestShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
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
            successContainer = DarkForestSecondaryContainer,
            onSuccessContainer = Color(0xFFD1EFE0),
            warning = DarkForestTertiary,
            warningContainer = DarkForestTertiaryContainer,
            onWarningContainer = Color(0xFFF9E6BB),
            coral = Color(0xFFE5956B),
            coralContainer = Color(0xFF492917),
            cyan = Color(0xFF7CB4C9),
            cyanContainer = Color(0xFF1D353F),
            violet = DarkForestSecondary,
            violetContainer = DarkForestSecondaryContainer
        )
    } else {
        StudyQuestSemanticColors(
            success = EditorialSuccess,
            successContainer = EditorialSuccessContainer,
            onSuccessContainer = OnEditorialSuccessContainer,
            warning = QuestGold,
            warningContainer = SoftQuestGold,
            onWarningContainer = OnSoftQuestGold,
            coral = WarmTerracotta,
            coralContainer = SoftTerracotta,
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
        baseColor.copy(alpha = if (config.isDark) 0.78f else 0.88f)
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
        accent?.copy(alpha = 0.42f) ?: Color.White.copy(alpha = 0.20f)
    } else {
        Color.White.copy(alpha = 0.90f)
    }
    val bottomEdge = if (config.isDark) {
        DarkForestPrimary.copy(alpha = 0.30f)
    } else {
        accent?.copy(alpha = 0.24f) ?: BrandForestGreen.copy(alpha = 0.14f)
    }
    return BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(listOf(topHighlight, bottomEdge))
    )
}
