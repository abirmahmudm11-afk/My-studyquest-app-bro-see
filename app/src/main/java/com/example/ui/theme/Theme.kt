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
        success = NaturalGreen,
        successContainer = NaturalGreenContainer,
        onSuccessContainer = OnNaturalGreenContainer,
        warning = AmberGold,
        warningContainer = AmberGoldContainer
    )
}

private val StudyQuestLightScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = PureCardWhite,
    primaryContainer = SageContainer,
    onPrimaryContainer = OnSageContainer,
    secondary = SubtleSlateAccent,
    onSecondary = PureCardWhite,
    secondaryContainer = CoolNeutralFill,
    onSecondaryContainer = DeepInk,
    tertiary = NaturalGreen,
    onTertiary = PureCardWhite,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = OnSageContainer,
    background = CrispWhiteCanvas,
    onBackground = DeepInk,
    surface = PureCardWhite,
    onSurface = DeepInk,
    surfaceVariant = CoolNeutralFill,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = PureCardWhite,
    surfaceContainerLow = Color(0xFFFAFAFC),
    surfaceContainer = CoolNeutralFill,
    surfaceContainerHigh = CoolSurfaceVariant,
    outline = CoolOutline,
    outlineVariant = SubtleOutline,
    error = AccessibleRed,
    onError = PureCardWhite,
    errorContainer = AccessibleRedContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val StudyQuestDarkScheme = darkColorScheme(
    primary = DarkForestPrimary,
    onPrimary = Color(0xFF052E16),
    primaryContainer = DarkForestContainer,
    onPrimaryContainer = Color(0xFFDCFCE7),
    secondary = DarkSlateTertiary,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = DarkSlateContainer,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = DarkNaturalSecondary,
    onTertiary = Color(0xFF064E3B),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = DarkMidnightBackground,
    onBackground = DarkOnSurface,
    surface = DarkMidnightSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkMidnightSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkMidnightBackground,
    surfaceContainerLow = DarkMidnightSurface,
    surfaceContainer = DarkMidnightSurfaceElevated,
    surfaceContainerHigh = Color(0xFF28323D),
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
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
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
            success = NaturalGreen,
            successContainer = NaturalGreenContainer,
            onSuccessContainer = OnNaturalGreenContainer,
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
