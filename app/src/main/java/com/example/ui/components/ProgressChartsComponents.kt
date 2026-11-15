package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.GamificationCatalog
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.VioletTertiary

@Composable
fun CompactWeeklyStreakStrip(
    streakCount: Long,
    historyKeys: List<String>,
    freezeCount: Long,
    language: AppLanguage,
    onBuyFreeze: (() -> Unit)? = null,
    showBuyFreeze: Boolean = true
) {
    val semantic = LocalStudyQuestColors.current
    val weekKeys = remember { GamificationCatalog.recentSevenDayKeys() }
    val historySet = remember(historyKeys) { historyKeys.toSet() }
    val todayKey = remember { GamificationCatalog.currentDayKey() }

    StudyQuestCard(modifier = Modifier.testTag("weekly_streak_strip_card")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = semantic.successContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = StudyQuestStrings.t(language, "Study Streak", "অধ্যয়ন স্ট্রিক"),
                        tint = semantic.success,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column {
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "$streakCount-Day Study Streak",
                            "${StudyQuestStrings.formatDigits(streakCount, language)} দিনের স্ট্রিক"
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "Complete a daily question set or lesson to extend • $freezeCount Freeze(s)",
                            "স্ট্রিক বাড়াতে দৈনিক কুইজ বা পাঠ শেষ করো • ফ্রিজ: ${StudyQuestStrings.formatDigits(freezeCount, language)}"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekKeys.forEachIndexed { idx, key ->
                val done = key in historySet
                val isToday = key == todayKey
                val label = key.takeLast(2)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            done -> semantic.success
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (done) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = StudyQuestStrings.t(language, "Completed day $label", "সম্পন্ন"),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = StudyQuestStrings.formatDigits(label, language),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isToday) StudyQuestStrings.t(language, "Today", "আজ") else "D${idx + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (done) semantic.success else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showBuyFreeze && onBuyFreeze != null) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.AcUnit, contentDescription = null, tint = CobaltPrimary, modifier = Modifier.size(16.dp))
                    Text(
                        text = StudyQuestStrings.t(language, "Streak Freeze ($freezeCount/5)", "স্ট্রিক ফ্রিজ ($freezeCount/5)"),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                FilledTonalButton(
                    onClick = onBuyFreeze,
                    enabled = freezeCount < 5L,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("buy_streak_freeze_btn")
                ) {
                    Text(StudyQuestStrings.t(language, "Buy Freeze (40 Coins)", "ফ্রিজ কিনুন (৪০ কয়েন)"))
                }
            }
        }
    }
}

@Composable
fun StudyTimeAndXpBarChart(
    dailySeries: List<DailyStudyMetric>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val semantic = LocalStudyQuestColors.current
    var selectedIdx by remember(dailySeries) {
        mutableIntStateOf((dailySeries.size - 1).coerceAtLeast(0))
    }

    StudyQuestCard(modifier = modifier.testTag("study_xp_bar_chart")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = StudyQuestStrings.t(language, "7-Day Study & XP Trend", "৭ দিনের অধ্যয়ন ও XP গ্রাফ"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = StudyQuestStrings.t(language, "Tap bar to inspect", "বিস্তারিত দেখতে ট্যাপ করো"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (dailySeries.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = StudyQuestStrings.t(language, "Complete a lesson, quiz, or focus timer to see daily progress.", "দৈনিক গ্রাফ দেখতে একটি পাঠ, কুইজ বা ফোকাস সেশন সম্পন্ন করো।"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@StudyQuestCard
        }

        val maxVal = dailySeries.maxOf { maxOf(it.xpEarned.toInt(), it.studyMinutes) }.coerceAtLeast(20)
        val barPrimary = MaterialTheme.colorScheme.primary
        val barSelected = semantic.success
        val gridLineColor = MaterialTheme.colorScheme.outlineVariant

        Spacer(Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .pointerInput(dailySeries) {
                    detectTapGestures { offset ->
                        val count = dailySeries.size.coerceAtLeast(1)
                        val step = size.width.toFloat() / count
                        selectedIdx = (offset.x / step).toInt().coerceIn(0, count - 1)
                    }
                }
        ) {
            val h = size.height
            val w = size.width
            // Horizontal gridlines
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                val y = h * (1f - frac)
                drawLine(gridLineColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }
            val count = dailySeries.size.coerceAtLeast(1)
            val slotW = w / count
            val barW = (slotW * 0.48f).coerceAtLeast(12.dp.toPx())
            dailySeries.forEachIndexed { idx, item ->
                val value = maxOf(item.xpEarned.toInt(), item.studyMinutes)
                val barH = ((value.toFloat() / maxVal.toFloat()) * (h * 0.88f)).coerceAtLeast(6.dp.toPx())
                val left = idx * slotW + (slotW - barW) / 2f
                drawRoundRect(
                    color = if (idx == selectedIdx) barSelected else barPrimary.copy(alpha = 0.75f),
                    topLeft = Offset(left, h - barH),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dailySeries.forEachIndexed { idx, item ->
                Text(
                    text = item.shortLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal,
                    color = if (idx == selectedIdx) semantic.success else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val inspected = dailySeries.getOrNull(selectedIdx)
        if (inspected != null) {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "${inspected.dayKey}: ${inspected.xpEarned} XP • ${inspected.studyMinutes} focus min • ${inspected.sessionsCount} activities",
                        "${inspected.dayKey}: ${StudyQuestStrings.formatDigits(inspected.xpEarned, language)} XP • ${StudyQuestStrings.formatDigits(inspected.studyMinutes, language)} মিনিট • ${StudyQuestStrings.formatDigits(inspected.sessionsCount, language)} সেশন"
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun SubjectXpDistributionChart(
    subjectAnalytics: List<SubjectAnalyticsSummary>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    StudyQuestCard(modifier = modifier.testTag("subject_xp_distribution_chart")) {
        Text(
            text = StudyQuestStrings.t(language, "XP Across Subjects", "বিষয়ভিত্তিক XP বণ্টন"),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        if (subjectAnalytics.isEmpty()) {
            Text(
                text = StudyQuestStrings.t(language, "No subject XP recorded yet.", "এখনো কোনো বিষয়ে XP জমা হয়নি।"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@StudyQuestCard
        }

        val maxXp = subjectAnalytics.maxOf { it.totalXp }.coerceAtLeast(1L)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            subjectAnalytics.take(6).forEach { summary ->
                val subj = BangladeshCurriculumCatalog.subjects.find { it.id == summary.subjectId }
                val label = StudyQuestStrings.t(language, subj?.titleEn ?: summary.subjectId, subj?.titleBn ?: summary.subjectId)
                val barColor = subj?.accentHex?.let { Color(it) } ?: CobaltPrimary
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${StudyQuestStrings.formatDigits(summary.totalXp, language)} XP • ${(summary.averageAccuracy * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (summary.totalXp.toFloat() / maxXp.toFloat()).coerceIn(0.05f, 1f) },
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun ExamAccuracyTrendChart(
    dailySeries: List<DailyStudyMetric>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val activePoints = remember(dailySeries) {
        dailySeries.filter { it.sessionsCount > 0 || it.examAccuracy > 0.0 }
    }
    StudyQuestCard(modifier = modifier.testTag("exam_accuracy_trend_chart")) {
        Text(
            text = StudyQuestStrings.t(language, "Accuracy Over Time", "সময়ের সাথে সঠিকতার হার"),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        if (activePoints.isEmpty()) {
            Text(
                text = StudyQuestStrings.t(language, "Complete quizzes or mock exams to track accuracy trends.", "সঠিকতার ধারা দেখতে কুইজ বা মডেল টেস্ট দাও।"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@StudyQuestCard
        }

        val lineColor = TealSecondary
        val pointBg = MaterialTheme.colorScheme.surface
        val gridColor = MaterialTheme.colorScheme.outlineVariant

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val w = size.width
            val h = size.height
            listOf(0.5f, 1.0f).forEach { frac ->
                val y = h * (1f - frac * 0.85f)
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }
            val pts = activePoints.mapIndexed { idx, m ->
                val x = if (activePoints.size == 1) w / 2f else (idx.toFloat() / (activePoints.size - 1)) * (w - 24.dp.toPx()) + 12.dp.toPx()
                val acc = m.examAccuracy.toFloat().coerceIn(0f, 1f)
                val y = h - 10.dp.toPx() - acc * (h - 20.dp.toPx())
                Offset(x, y)
            }
            if (pts.size > 1) {
                val path = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x, pts[i].y)
                    }
                }
                drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }
            pts.forEach { p ->
                drawCircle(color = pointBg, radius = 6.dp.toPx(), center = p)
                drawCircle(color = lineColor, radius = 4.dp.toPx(), center = p)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            activePoints.forEach { m ->
                Text(
                    text = "${m.shortLabel} (${(m.examAccuracy * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
