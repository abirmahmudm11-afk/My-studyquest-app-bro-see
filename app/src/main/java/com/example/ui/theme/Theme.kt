package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StudyQuestLightScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = PureWhite,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = OnForestGreenContainer,
    secondary = NaturalGreen,
    onSecondary = PureWhite,
    secondaryContainer = NaturalGreenContainer,
    onSecondaryContainer = OnNaturalGreenContainer,
    tertiary = MangoYellow,
    onTertiary = DeepCharcoal,
    tertiaryContainer = MangoYellowContainer,
    onTertiaryContainer = OnMangoYellowContainer,
    background = WarmCream,
    onBackground = DeepCharcoal,
    surface = PureWhite,
    onSurface = DeepCharcoal,
    surfaceVariant = WarmCreamSurfaceVariant,
    onSurfaceVariant = MutedSlate,
    outline = SoftOutline,
    outlineVariant = SoftOutline.copy(alpha = 0.6f),
    error = AccessibleRed,
    onError = PureWhite,
    errorContainer = AccessibleRedContainer,
    onErrorContainer = Color(0xFF410002)
)

private val StudyQuestDarkScheme = darkColorScheme(
    primary = DarkForestPrimary,
    onPrimary = Color(0xFF062112),
    primaryContainer = DarkForestPrimaryContainer,
    onPrimaryContainer = Color(0xFFB5F2CE),
    secondary = DarkNaturalSecondary,
    onSecondary = Color(0xFF0A2616),
    secondaryContainer = Color(0xFF1F402D),
    onSecondaryContainer = Color(0xFFC5F4D8),
    tertiary = DarkMangoTertiary,
    onTertiary = Color(0xFF332500),
    tertiaryContainer = DarkMangoContainer,
    onTertiaryContainer = Color(0xFFFFE599),
    background = DarkForestBackground,
    onBackground = DarkOnSurface,
    surface = DarkForestSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkForestSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.7f),
    error = DarkAccessibleRed,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
    MaterialTheme(
        colorScheme = colorScheme,
        typography = studyQuestTypography(isBangla = isBangla),
        shapes = StudyQuestShapes,
        content = content
    )
}
