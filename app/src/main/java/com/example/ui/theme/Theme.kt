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
    val coral: Color = SunsetCoral,
    val coralContainer: Color = SunsetCoralContainer,
    val cyan: Color = SkyCyan,
    val cyanContainer: Color = SkyCyanContainer,
    val violet: Color = BerryViolet,
    val violetContainer: Color = BerryVioletContainer
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
        success = VividEmerald,
        successContainer = EmeraldContainer,
        onSuccessContainer = OnEmeraldContainer,
        warning = AmberGold,
        warningContainer = AmberGoldContainer
    )
}

val LocalStudyQuestSurfaceStyle = staticCompositionLocalOf {
    StudyQuestVisualConfig()
}

private val StudyQuestLightScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = PureCardWhite,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = OnIndigoContainer,
    secondary = VividEmerald,
    onSecondary = PureCardWhite,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = OnEmeraldContainer,
    tertiary = BerryViolet,
    onTertiary = PureCardWhite,
    tertiaryContainer = BerryVioletContainer,
    onTertiaryContainer = OnBerryVioletContainer,
    background = VividLightCanvas,
    onBackground = DeepInk,
    surface = PureCardWhite,
    onSurface = DeepInk,
    surfaceVariant = VividSoftFill,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = PureCardWhite,
    surfaceContainerLow = Color(0xFFF8FAFF),
    surfaceContainer = VividSoftFill,
    surfaceContainerHigh = VividSurfaceVariant,
    outline = CoolOutline,
    outlineVariant = SubtleOutline,
    error = AccessibleRed,
    onError = PureCardWhite,
    errorContainer = AccessibleRedContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val StudyQuestDarkScheme = darkColorScheme(
    primary = DarkForestPrimary,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = DarkForestContainer,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = DarkNaturalSecondary,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = DarkScholarGreenContainer,
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = DarkSlateTertiary,
    onTertiary = Color(0xFF2E1065),
    tertiaryContainer = DarkVioletContainer,
    onTertiaryContainer = Color(0xFFEDE9FE),
    background = DarkMidnightBackground,
    onBackground = DarkOnSurface,
    surface = DarkMidnightSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkMidnightSurfaceElevated,
    onSurfaceVariant = Color(0xFFCBD5E1),
    surfaceContainerLowest = DarkMidnightBackground,
    surfaceContainerLow = DarkMidnightSurface,
    surfaceContainer = DarkMidnightSurfaceElevated,
    surfaceContainerHigh = Color(0xFF25325B),
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.8f),
    error = DarkAccessibleRed,
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2)
)

val StudyQuestShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
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
            successContainer = DarkScholarGreenContainer,
            onSuccessContainer = Color(0xFFD1FAE5),
            warning = Color(0xFFFBBF24),
            warningContainer = Color(0xFF78350F),
            coral = Color(0xFFFB923C),
            coralContainer = Color(0xFF7C2D12),
            cyan = Color(0xFF22D3EE),
            cyanContainer = Color(0xFF164E63),
            violet = DarkSlateTertiary,
            violetContainer = DarkVioletContainer
        )
    } else {
        StudyQuestSemanticColors(
            success = VividEmerald,
            successContainer = EmeraldContainer,
            onSuccessContainer = OnEmeraldContainer,
            warning = AmberGold,
            warningContainer = AmberGoldContainer,
            coral = SunsetCoral,
            coralContainer = SunsetCoralContainer,
            cyan = SkyCyan,
            cyanContainer = SkyCyanContainer,
            violet = BerryViolet,
            violetContainer = BerryVioletContainer
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
        baseColor.copy(alpha = if (config.isDark) 0.72f else 0.84f)
    }
}

@Composable
fun studyQuestGlassAwareBorder(accent: Color? = null): BorderStroke {
    val config = LocalStudyQuestSurfaceStyle.current
    if (!config.isGlass) {
        return BorderStroke(
            width = 1.dp,
            color = accent?.copy(alpha = 0.22f) ?: MaterialTheme.colorScheme.outlineVariant
        )
    }
    val topHighlight = if (config.isDark) {
        accent?.copy(alpha = 0.50f) ?: Color.White.copy(alpha = 0.24f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }
    val bottomEdge = if (config.isDark) {
        ElectricIndigo.copy(alpha = 0.30f)
    } else {
        accent?.copy(alpha = 0.28f) ?: ElectricIndigo.copy(alpha = 0.16f)
    }
    return BorderStroke(
        width = 1.25.dp,
        brush = Brush.linearGradient(listOf(topHighlight, bottomEdge))
    )
}
