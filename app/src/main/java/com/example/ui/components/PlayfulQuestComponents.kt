package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.MutedMintFocal
import com.example.ui.theme.QuestOnboardingOrchid
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor

/**
 * Playful pastel bento tile inspired by Reference Photo 1 & Photo 4.
 * Automatically adapts to Light, Dark, and Glass surface preferences.
 */
@Composable
fun PastelQuestBentoTile(
    title: String,
    valueText: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeLabel: String? = null
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val resolvedBg = if (visualConfig.isGlass) {
        containerColor.copy(alpha = if (visualConfig.isDark) 0.78f else 0.88f)
    } else {
        containerColor
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(),
        shadowElevation = 0.dp,
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (!badgeLabel.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * "Map the Path" Journey Roadmap Card inspired by Reference Photo 1.
 * Connects the student's profile avatar, locked textbook milestone, step dots, and badges count.
 */
@Composable
fun MapThePathCard(
    avatarId: String,
    frameId: String?,
    displayName: String,
    pathTitle: String,
    pathSubtitle: String,
    stepCurrent: Int,
    stepTotal: Int,
    unlockedBadgesCount: Int,
    language: AppLanguage,
    onContinuePath: () -> Unit,
    onOpenBadges: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalStudyQuestColors.current
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val safeTotal = stepTotal.coerceAtLeast(1)
    val safeDone = stepCurrent.coerceIn(0, safeTotal)
    val progressRatio = (safeDone.toFloat() / safeTotal.toFloat()).coerceIn(0.06f, 1f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(),
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("map_the_path_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = StudyQuestStrings.t(language, "Map the Path", "শেখার পথরেখা"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    onClick = onOpenBadges,
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                language,
                                "$unlockedBadgesCount Badges",
                                "${StudyQuestStrings.formatDigits(unlockedBadgesCount, language)}টি ব্যাজ"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(62.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AvatarBadgeView(
                            avatarId = avatarId,
                            frameId = frameId,
                            displayName = displayName,
                            size = 46.dp
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = pathTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pathSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = StudyQuestStrings.t(
                                language,
                                "$safeDone / $safeTotal steps",
                                "${StudyQuestStrings.formatDigits(safeDone, language)} / ${StudyQuestStrings.formatDigits(safeTotal, language)} ধাপ"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            val earnedStars = ((progressRatio * 5).toInt()).coerceIn(1, 5)
                            repeat(5) { idx ->
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (idx < earnedStars) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Surface(
                    onClick = onContinuePath,
                    shape = CircleShape,
                    color = semantic.charcoalMilestone,
                    modifier = Modifier
                        .size(44.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("map_path_continue_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = StudyQuestStrings.t(language, "Continue path", "এগিয়ে যান"),
                            tint = semantic.onCharcoalMilestone,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

data class ActivityRingSlice(
    val label: String,
    val count: Int,
    val color: Color
)

/**
 * "My Activity" Segmented Donut Ring Card inspired by Reference Photo 1.
 * Displays real study hours/minutes + colored donut breakdown (no XP numbers).
 */
@Composable
fun ActivityDonutBreakdownCard(
    totalMinutes: Int,
    slices: List<ActivityRingSlice>,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    val durationText = StudyQuestStrings.t(
        language,
        if (hours > 0) "${hours}h ${mins}m" else "${mins}m",
        if (hours > 0) "${StudyQuestStrings.formatDigits(hours, language)}ঘ ${StudyQuestStrings.formatDigits(mins, language)}মি"
        else "${StudyQuestStrings.formatDigits(mins, language)} মিনিট"
    )
    val totalCount = slices.sumOf { it.count }.coerceAtLeast(1)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(),
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_donut_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = StudyQuestStrings.t(language, "My activity", "আমার অধ্যয়ন"),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = StudyQuestStrings.t(language, "7 Days", "৭ দিন"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                val hasActivity = slices.any { it.count > 0 }
                Box(
                    modifier = Modifier.size(104.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokePx = 15.dp.toPx()
                        val pad = strokePx / 2f
                        val arcSize = Size(size.width - strokePx, size.height - strokePx)
                        val topLeft = Offset(pad, pad)
                        if (!hasActivity) {
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        } else {
                            var startAngle = -90f
                            val gap = if (slices.count { it.count > 0 } > 1) 6f else 0f
                            slices.filter { it.count > 0 }.forEach { slice ->
                                val rawSweep = 360f * (slice.count.toFloat() / totalCount.toFloat())
                                val drawSweep = (rawSweep - gap).coerceAtLeast(10f)
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = drawSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                                )
                                startAngle += rawSweep
                            }
                        }
                    }
                    Text(
                        text = "${StudyQuestStrings.formatDigits(slices.sumOf { it.count }, language)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slices.forEach { slice ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Text(
                                    text = slice.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = StudyQuestStrings.formatDigits(slice.count, language),
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-contrast dark charcoal milestone card inspired by Reference Photo 1's bottom milestone bar.
 * Spotlights the next achievable Badge or Trophy instead of XP levels.
 */
@Composable
fun CharcoalMilestoneCard(
    eyebrow: String,
    title: String,
    progressLabel: String,
    progress: Float,
    unlockedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalStudyQuestColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = semantic.charcoalMilestone,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("charcoal_milestone_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eyebrow,
                            style = MaterialTheme.typography.labelSmall,
                            color = semantic.onCharcoalMilestone.copy(alpha = 0.68f),
                            maxLines = 1
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = semantic.onCharcoalMilestone,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "$unlockedCount/$totalCount",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        fontWeight = FontWeight.Bold,
                        color = semantic.onCharcoalMilestone,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0.05f, 1f) },
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.18f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progressLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.onCharcoalMilestone.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Playful scholar-on-stacked-books illustration inspired by Reference Photo 1 & Photo 3.
 * Vector-drawn cleanly in Compose Canvas so it adapts crisply to Light and Dark themes.
 */
@Composable
fun ScholarOnBooksIllustration(
    modifier: Modifier = Modifier
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val semantic = LocalStudyQuestColors.current
    val bookSpine1 = if (visualConfig.isDark) Color(0xFF28415C) else semantic.pastelSky
    val bookSpine2 = if (visualConfig.isDark) Color(0xFF33294E) else semantic.pastelLavender
    val bookSpine3 = if (visualConfig.isDark) Color(0xFF2B4639) else semantic.pastelMint
    val outlineColor = if (visualConfig.isDark) Color(0xFF9FB8D4) else Color(0xFF1F2B38)
    val sweaterColor = if (visualConfig.isDark) Color(0xFFB566E6) else QuestOnboardingOrchid
    val pantsColor = MutedMintFocal
    val skinColor = Color(0xFFF6CBB2)
    val sparkleColor = semantic.heroLimeStart

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft halo behind illustration
        drawCircle(
            color = semantic.pastelLavender.copy(alpha = if (visualConfig.isDark) 0.25f else 0.55f),
            radius = minOf(w, h) * 0.40f,
            center = Offset(w * 0.50f, h * 0.48f)
        )

        // Sparkles
        listOf(
            Offset(w * 0.20f, h * 0.22f) to 5.dp.toPx(),
            Offset(w * 0.82f, h * 0.26f) to 6.dp.toPx(),
            Offset(w * 0.16f, h * 0.56f) to 4.dp.toPx()
        ).forEach { (pt, r) ->
            drawCircle(color = sparkleColor, radius = r, center = pt)
        }

        // Stacked NCTB Textbooks (bottom 3 layers)
        val books = listOf(
            Triple(0.74f, bookSpine1, 0.18f),
            Triple(0.64f, bookSpine2, 0.21f),
            Triple(0.54f, bookSpine3, 0.23f)
        )
        books.forEachIndexed { idx, (topFrac, fill, leftFrac) ->
            val bw = w * (0.64f - idx * 0.04f)
            val bh = h * 0.095f
            val bx = w * leftFrac
            val by = h * topFrac
            drawRoundRect(
                color = fill,
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = 1.6.dp.toPx())
            )
            // Page lines on book edge
            drawLine(
                color = outlineColor.copy(alpha = 0.5f),
                start = Offset(bx + bw * 0.12f, by + bh * 0.5f),
                end = Offset(bx + bw * 0.88f, by + bh * 0.5f),
                strokeWidth = 1.2.dp.toPx()
            )
        }

        // Legs sitting comfortably across the top book
        val legs = Path().apply {
            moveTo(w * 0.48f, h * 0.46f)
            lineTo(w * 0.34f, h * 0.53f)
            lineTo(w * 0.27f, h * 0.67f)
            lineTo(w * 0.33f, h * 0.69f)
            lineTo(w * 0.40f, h * 0.56f)
            lineTo(w * 0.55f, h * 0.54f)
            close()
        }
        drawPath(legs, color = pantsColor)
        drawPath(legs, color = outlineColor, style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round))

        // Torso sweater
        drawRoundRect(
            color = sweaterColor,
            topLeft = Offset(w * 0.45f, h * 0.31f),
            size = Size(w * 0.16f, h * 0.18f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(w * 0.45f, h * 0.31f),
            size = Size(w * 0.16f, h * 0.18f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = 1.6.dp.toPx())
        )

        // Laptop resting on knees
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.27f, h * 0.36f),
            size = Size(w * 0.14f, h * 0.10f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(w * 0.27f, h * 0.36f),
            size = Size(w * 0.14f, h * 0.10f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Head
        drawCircle(
            color = skinColor,
            radius = minOf(w, h) * 0.068f,
            center = Offset(w * 0.52f, h * 0.23f)
        )
        drawCircle(
            color = outlineColor,
            radius = minOf(w, h) * 0.068f,
            center = Offset(w * 0.52f, h * 0.23f),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Graduation Cap (Mortarboard)
        val cap = Path().apply {
            moveTo(w * 0.40f, h * 0.16f)
            lineTo(w * 0.53f, h * 0.11f)
            lineTo(w * 0.66f, h * 0.16f)
            lineTo(w * 0.53f, h * 0.21f)
            close()
        }
        drawPath(cap, color = BrandTealPrimary)
        drawPath(cap, color = outlineColor, style = Stroke(width = 1.4.dp.toPx(), join = StrokeJoin.Round))
    }
}

/**
 * Playful 3D geometric learning cluster inspired by Reference Photo 4 ("Ready to learn?").
 */
@Composable
fun GeometricStudyArtwork(
    modifier: Modifier = Modifier
) {
    val isDark = LocalStudyQuestSurfaceStyle.current.isDark
    val fillPrimary = if (isDark) Color(0xFF283244) else Color(0xFFE2E8F0)
    val fillSecondary = if (isDark) Color(0xFF1F2735) else Color(0xFFF1F5F9)
    val inkStroke = if (isDark) Color(0xFFCBD5E1) else Color(0xFF0F172A)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = fillSecondary,
            radius = minOf(w, h) * 0.36f,
            center = Offset(w * 0.56f, h * 0.48f)
        )
        drawCircle(
            color = fillPrimary,
            radius = minOf(w, h) * 0.22f,
            center = Offset(w * 0.34f, h * 0.60f)
        )
        drawCircle(
            color = inkStroke,
            radius = minOf(w, h) * 0.14f,
            center = Offset(w * 0.64f, h * 0.66f),
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = inkStroke,
            radius = minOf(w, h) * 0.06f,
            center = Offset(w * 0.30f, h * 0.34f)
        )
    }
}
