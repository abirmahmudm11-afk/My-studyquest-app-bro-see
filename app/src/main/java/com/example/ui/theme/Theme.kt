package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class StudyQuestSemanticColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color
)

val LocalStudyQuestColors = staticCompositionLocalOf {
    StudyQuestSemanticColors(
        success = ScholarGreen,
        successContainer = ScholarGreenContainer,
        onSuccessContainer = OnScholarGreenContainer,
        warning = AmberGold,
        warningContainer = AmberGoldContainer
    )
}

private val StudyQuestLightScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = PureWhite,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = OnCobaltContainer,
    secondary = TealSecondary,
    onSecondary = PureWhite,
    secondaryContainer = TealContainer,
    onSecondaryContainer = OnTealContainer,
    tertiary = VioletTertiary,
    onTertiary = PureWhite,
    tertiaryContainer = VioletContainer,
    onTertiaryContainer = OnVioletContainer,
    background = SoftSlateCanvas,
    onBackground = DeepInk,
    surface = PureWhite,
    onSurface = DeepInk,
    surfaceVariant = SoftSlateContainer,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = SoftSlateContainer,
    surfaceContainerHigh = CoolSurfaceVariant,
    outline = CoolOutline,
    outlineVariant = SubtleOutline,
    error = AccessibleRed,
    onError = PureWhite,
    errorContainer = AccessibleRedContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val StudyQuestDarkScheme = darkColorScheme(
    primary = DarkCobaltPrimary,
    onPrimary = Color(0xFF0A1D3B),
    primaryContainer = DarkCobaltPrimaryContainer,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = DarkTealSecondary,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = DarkVioletTertiary,
    onTertiary = Color(0xFF2E1065),
    tertiaryContainer = DarkVioletContainer,
    onTertiaryContainer = Color(0xFFEDE9FE),
    background = DarkMidnightBackground,
    onBackground = DarkOnSurface,
    surface = DarkMidnightSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkMidnightSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkMidnightBackground,
    surfaceContainerLow = DarkMidnightSurface,
    surfaceContainer = DarkMidnightSurfaceElevated,
    surfaceContainerHigh = Color(0xFF263349),
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.75f),
    error = DarkAccessibleRed,
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2)
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
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudyQuestDarkScheme else StudyQuestLightScheme
    val semanticColors = if (darkTheme) {
        StudyQuestSemanticColors(
            success = DarkScholarGreen,
            successContainer = DarkScholarGreenContainer,
            onSuccessContainer = Color(0xFFDCFCE7),
            warning = Color(0xFFFBBF24),
            warningContainer = Color(0xFF78350F)
        )
    } else {
        StudyQuestSemanticColors(
            success = ScholarGreen,
            successContainer = ScholarGreenContainer,
            onSuccessContainer = OnScholarGreenContainer,
            warning = AmberGold,
            warningContainer = AmberGoldContainer
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalStudyQuestColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = studyQuestTypography(isBangla = isBangla),
            shapes = StudyQuestShapes,
            content = content
        )
    }
}
