package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.LearningRecordDoc
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.PublicProfile
import com.example.data.repository.CompetitionRepository
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.LeaderboardCategory
import com.example.data.repository.LeaderboardPeriod
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.AvatarBadgeView
import com.example.ui.components.DailyLessonsBarChart
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ExamAccuracyTrendChart
import com.example.ui.components.RankTierBadge
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.components.StudyTimeAndXpBarChart
import com.example.ui.components.SubjectXpDistributionChart
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BerryViolet
import com.example.ui.theme.BronzeAccent
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.SilverAccent
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SunsetCoral
import com.example.ui.theme.VividEmerald
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

enum class TodayOverlayDestination {
    None,
    StudyProgress,
    Leaderboard,
    AiDoubtSolver
}

enum class LeaderboardScopeFilter(val id: String, val labelEn: String, val labelBn: String) {
    CLASS_ONLY("class", "My Class", "আমার শ্রেণি"),
    NATIONAL_OPT_IN("national", "National", "জাতীয়"),
    FRIENDS("friends", "Friends", "বন্ধুরা")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyProgressDashboardScreen(
    profile: PublicProfile,
    language: AppLanguage,
    reducedMotion: Boolean,
    learningRecords: List<LearningRecordDoc>,
    dailyStudySeries: List<DailyStudyMetric>,
    subjectAnalytics: List<SubjectAnalyticsSummary>,
    weakTopics: List<WeakTopicStat>,
    onOpenQuickPractice: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val semantic = LocalStudyQuestColors.current
    val totalLessons = remember(learningRecords) { learningRecords.count { it.recordType == "lesson" } }
    val totalExams = remember(learningRecords) { learningRecords.count { it.recordType in listOf("exam", "promotion_exam") } }
    val avgAccuracyPct = remember(learningRecords) {
        val graded = learningRecords.filter { it.recordType in listOf("exam", "promotion_exam", "practice", "daily_challenge") }
        if (graded.isEmpty()) 0 else (graded.map { it.accuracy }.average() * 100).toInt().coerceIn(0, 100)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_progress_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("study_progress_back_btn")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = StudyQuestStrings.t(language, "Back", "ফিরে যাও")
                    )
                }
                Column {
                    Text(
                        text = StudyQuestStrings.t(language, "Study Progress", "অধ্যয়ন অগ্রগতি"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = StudyQuestStrings.t(language, "Daily lessons, mock exam trends & mastery", "দৈনিক পাঠ, মক টেস্ট ও দক্ষতার গ্রাফ"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                StudyQuestGradientHeroBanner(
                    eyebrow = StudyQuestStrings.t(language, "ACADEMIC SNAPSHOT", "অ্যাকাডেমিক সারসংক্ষেপ"),
                    title = "${StudyQuestStrings.formatDigits(totalLessons, language)} ${StudyQuestStrings.t(language, "Lessons", "পাঠ")} • ${StudyQuestStrings.formatDigits(totalExams, language)} ${StudyQuestStrings.t(language, "Exams", "পরীক্ষা")}",
                    subtitle = StudyQuestStrings.t(
                        language,
                        "Streak: ${profile.streakCount} days • Total XP: ${profile.totalXp}",
                        "স্ট্রিক: ${StudyQuestStrings.formatDigits(profile.streakCount, language)} দিন • মোট XP: ${StudyQuestStrings.formatDigits(profile.totalXp, language)}"
                    ),
                    reducedMotion = reducedMotion,
                    trailingSlot = {
                        AnimatedCircularMasteryRing(
                            progress = avgAccuracyPct / 100f,
                            centerValueText = "${avgAccuracyPct}%",
                            centerCaptionText = StudyQuestStrings.t(language, "Accuracy", "সঠিকতা"),
                            reducedMotion = reducedMotion
                        )
                    }
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatBadgePill(
                            icon = Icons.Filled.School,
                            label = StudyQuestStrings.t(language, "$totalLessons Lessons", "$totalLessons পাঠ"),
                            tint = Color.White,
                            containerColor = Color.White.copy(alpha = 0.18f)
                        )
                        StatBadgePill(
                            icon = Icons.Filled.CheckCircle,
                            label = StudyQuestStrings.t(language, "$totalExams Mock Exams", "$totalExams মক পরীক্ষা"),
                            tint = Color.White,
                            containerColor = Color.White.copy(alpha = 0.18f)
                        )
                        StatBadgePill(
                            icon = Icons.Filled.LocalFireDepartment,
                            label = StudyQuestStrings.t(language, "${profile.streakCount}d Streak", "${profile.streakCount} দিন"),
                            tint = Color.White,
                            containerColor = Color.White.copy(alpha = 0.18f)
                        )
                    }
                }
            }
        }

        item {
            StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                DailyLessonsBarChart(dailySeries = dailyStudySeries, language = language)
            }
        }

        item {
            StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                ExamAccuracyTrendChart(dailySeries = dailyStudySeries, language = language)
            }
        }

        item {
            StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                StudyTimeAndXpBarChart(dailySeries = dailyStudySeries, language = language)
            }
        }

        item {
            StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                SubjectXpDistributionChart(subjectAnalytics = subjectAnalytics, language = language)
            }
        }

        if (weakTopics.isNotEmpty()) {
            item {
                Text(
                    text = StudyQuestStrings.t(language, "Recommended Review Topics", "পুনরালোচনার বিষয়সমূহ"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(weakTopics.take(4), key = { "${it.subjectId}_${it.topicId}" }) { wt ->
                StudyQuestCard {
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
                            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = RoseAlert)
                            Column {
                                Text(
                                    text = StudyQuestStrings.t(language, wt.topicLabelEn, wt.topicLabelBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "${(wt.averageAccuracy * 100).toInt()}% accuracy over ${wt.attempts} attempts",
                                        "${wt.attempts} বার চেষ্টায় ${(wt.averageAccuracy * 100).toInt()}% সঠিকতা"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = onOpenQuickPractice) {
                            Text(StudyQuestStrings.t(language, "Practice", "অনুশীলন"))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeaderboardScreen(
    myProfile: PublicProfile,
    language: AppLanguage,
    reducedMotion: Boolean,
    leaderboard: List<LeaderboardEntryDoc>,
    selectedScope: LeaderboardScopeFilter,
    selectedPeriod: LeaderboardPeriod,
    selectedCategory: LeaderboardCategory,
    selectedSubjectId: String = "all",
    friendUids: Set<String> = emptySet(),
    isGuestMode: Boolean = false,
    onSelectScope: (LeaderboardScopeFilter) -> Unit,
    onSelectPeriod: (LeaderboardPeriod) -> Unit,
    onSelectCategory: (LeaderboardCategory) -> Unit,
    onSelectSubject: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onRequestGoogleSignIn: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    fun metricScore(entry: LeaderboardEntryDoc): Long =
        CompetitionRepository.scoreFor(entry, selectedCategory, selectedPeriod)

    fun scoreUnitLabel(): String = when (selectedCategory) {
        LeaderboardCategory.STUDY_TIME -> StudyQuestStrings.t(language, "min", "মি.")
        LeaderboardCategory.XP -> "XP"
        LeaderboardCategory.EXAM -> StudyQuestStrings.t(language, "pts", "নম্বর")
        LeaderboardCategory.ASSIGNMENT -> StudyQuestStrings.t(language, "tasks", "টাস্ক")
        LeaderboardCategory.CHALLENGE -> "pts"
    }

    val rankedEntries = remember(leaderboard, selectedScope, selectedPeriod, selectedCategory, selectedSubjectId, myProfile, friendUids, isGuestMode) {
        leaderboard
            .filter { entry ->
                val matchesScope = when (selectedScope) {
                    LeaderboardScopeFilter.CLASS_ONLY -> entry.classId == myProfile.classId
                    LeaderboardScopeFilter.FRIENDS -> entry.userId == myProfile.userId || entry.userId in friendUids
                    LeaderboardScopeFilter.NATIONAL_OPT_IN -> if (isGuestMode) true else entry.optInNational
                }
                val matchesSubject = selectedSubjectId.isBlank() ||
                    selectedSubjectId == "all" ||
                    entry.subjectId == selectedSubjectId
                matchesScope && matchesSubject
            }
            .sortedByDescending { metricScore(it) }
    }
    val topThree = remember(rankedEntries) { rankedEntries.take(3) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("leaderboard_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = StudyQuestStrings.t(language, "StudyQuest Leaderboard", "StudyQuest লিডারবোর্ড"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = StudyQuestStrings.t(language, "Live rankings from Firestore", "ফায়ারবেস থেকে লাইভ র‍্যাংকিং"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("leaderboard_refresh_btn")
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = StudyQuestStrings.t(language, "Refresh", "রিফ্রেশ"))
                }
            }
        }

        if (isGuestMode) {
            item {
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(language, "Join National & Class Rankings", "জাতীয় ও শ্রেণি র‍্যাংকিংয়ে যোগ দাও"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(language, "Sign in with Google to sync your score to the Firestore leaderboard.", "আপনার পয়েন্ট লিডারবোর্ডে যুক্ত করতে গুগল সাইন-ইন করুন।"),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(onClick = onRequestGoogleSignIn) {
                            Text(StudyQuestStrings.t(language, "Sign In", "সাইন-ইন"))
                        }
                    }
                }
            }
        }

        // Top 3 Podium Card
        if (topThree.isNotEmpty()) {
            item {
                StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                    StudyQuestGradientHeroBanner(
                        eyebrow = StudyQuestStrings.t(language, "TOP SCHOLARS PODIUM", "শীর্ষ ৩ মেধাবী"),
                        title = StudyQuestStrings.t(language, selectedPeriod.labelEn, selectedPeriod.labelBn),
                        subtitle = StudyQuestStrings.t(language, selectedCategory.labelEn, selectedCategory.labelBn),
                        reducedMotion = reducedMotion
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            topThree.forEachIndexed { idx, entry ->
                                val medalColor = when (idx) {
                                    0 -> AmberGold
                                    1 -> SilverAccent
                                    else -> BronzeAccent
                                }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, medalColor.copy(alpha = 0.7f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(shape = CircleShape, color = medalColor, modifier = Modifier.size(24.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("#${idx + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                            }
                                        }
                                        AvatarBadgeView(
                                            avatarId = entry.avatarId ?: "crest_indigo",
                                            frameId = entry.frameId,
                                            displayName = entry.displayName,
                                            size = 40.dp
                                        )
                                        Text(
                                            text = entry.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${metricScore(entry)} ${scoreUnitLabel()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips
        item {
            StudyQuestCard {
                Text(
                    text = StudyQuestStrings.t(language, "Period & Category", "সময়কাল ও ধরন"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeaderboardPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { onSelectPeriod(period) },
                            label = { Text(StudyQuestStrings.t(language, period.labelEn, period.labelBn)) },
                            modifier = Modifier.testTag("lb_period_${period.name.lowercase()}")
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeaderboardCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(StudyQuestStrings.t(language, cat.labelEn, cat.labelBn)) },
                            modifier = Modifier.testTag("lb_cat_${cat.name.lowercase()}")
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeaderboardScopeFilter.entries.forEach { scope ->
                        FilterChip(
                            selected = selectedScope == scope,
                            onClick = { onSelectScope(scope) },
                            label = { Text(StudyQuestStrings.t(language, scope.labelEn, scope.labelBn)) },
                            modifier = Modifier.testTag("lb_scope_${scope.name.lowercase()}")
                        )
                    }
                }
                if (selectedSubjectId.isNotBlank() && selectedSubjectId != "all") {
                    Spacer(Modifier.height(4.dp))
                    AssistChip(
                        onClick = { onSelectSubject("all") },
                        label = { Text(StudyQuestStrings.t(language, "All Subjects", "সকল বিষয়")) }
                    )
                }
            }
        }

        if (rankedEntries.isEmpty()) {
            item {
                EmptyStateView(
                    titleEn = "No Leaderboard Entries Yet",
                    titleBn = "এখনো কোনো র‍্যাংকিং নেই",
                    subtitleEn = "Complete lessons, quizzes, or focus sessions to claim the #1 spot!",
                    subtitleBn = "র‍্যাংকিংয়ে প্রথম হতে পাঠ, কুইজ বা ফোকাস সেশন সম্পন্ন করো!",
                    language = language
                )
            }
        } else {
            itemsIndexed(rankedEntries, key = { idx, item -> "${idx}_${item.userId}" }) { idx, entry ->
                val isMe = entry.userId == myProfile.userId
                val rankColor = when (idx) {
                    0 -> AmberGold
                    1 -> SilverAccent
                    2 -> BronzeAccent
                    else -> ElectricIndigo
                }
                StudyQuestCard(
                    containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("lb_row_${entry.userId}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = rankColor.copy(alpha = 0.16f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${idx + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = rankColor
                                )
                            }
                        }
                        AvatarBadgeView(
                            avatarId = entry.avatarId ?: "crest_indigo",
                            frameId = entry.frameId,
                            displayName = entry.displayName,
                            size = 44.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = entry.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isMe) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "(You)", "(তুমি)"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricIndigo,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                RankTierBadge(tier = entry.rankTier, language = language)
                                Text(
                                    text = "${entry.streakCount}d 🔥",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${metricScore(entry)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ElectricIndigo
                            )
                            Text(
                                text = scoreUnitLabel(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

data class DoubtSolverMessage(
    val id: String,
    val question: String,
    val subjectLabel: String,
    val answer: String,
    val isError: Boolean = false
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiDoubtSolverScreen(
    profile: PublicProfile,
    language: AppLanguage,
    reducedMotion: Boolean,
    initialQuestionPrompt: String = "",
    onAskGeminiDoubt: (questionPrompt: String, studentQuestion: String, subjectName: String) -> Flow<String>,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val scope = rememberCoroutineScope()
    val subjects = remember(profile.classId) {
        BangladeshCurriculumCatalog.subjectsForClass(profile.classId)
    }
    var selectedSubject by remember(subjects) {
        mutableStateOf(subjects.firstOrNull())
    }
    var questionText by remember(initialQuestionPrompt) { mutableStateOf(initialQuestionPrompt) }
    var isStreaming by remember { mutableStateOf(false) }
    var streamingAnswer by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val history = remember { mutableStateListOf<DoubtSolverMessage>() }

    val quickPrompts = remember(language) {
        listOf(
            StudyQuestStrings.t(language, "Prove Pythagoras Theorem step by step", "পিথাগোরাসের উপপাদ্য ধাপে ধাপে প্রমাণ করো"),
            StudyQuestStrings.t(language, "Explain Newton's 2nd Law with examples", "উদাহরণসহ নিউটনের দ্বিতীয় সূত্র বুঝিয়ে দাও"),
            StudyQuestStrings.t(language, "How to solve quadratic equations easily?", "দ্বিঘাত সমীকরণ সমাধানের সহজ নিয়ম কী?"),
            StudyQuestStrings.t(language, "Key grammar rules for SSC English", "এসএসসি ইংরেজির গুরুত্বপূর্ণ গ্রামার নিয়ম")
        )
    }

    fun submitQuestion(rawPrompt: String) {
        val clean = rawPrompt.trim()
        if (clean.isBlank() || isStreaming) return
        val subjName = selectedSubject?.let { StudyQuestStrings.t(language, it.titleEn, it.titleBn) } ?: "General"
        isStreaming = true
        streamingAnswer = ""
        errorText = null
        scope.launch {
            var latest = ""
            try {
                onAskGeminiDoubt(initialQuestionPrompt, clean, subjName).collect { partial ->
                    latest = partial
                    streamingAnswer = partial
                }
                history.add(
                    0,
                    DoubtSolverMessage(
                        id = "doubt_${System.currentTimeMillis()}",
                        question = clean,
                        subjectLabel = subjName,
                        answer = latest
                    )
                )
                streamingAnswer = ""
                questionText = ""
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorText = e.localizedMessage ?: StudyQuestStrings.t(
                    language,
                    "Could not get response from Gemini. Please try again.",
                    "জেমিনি থেকে উত্তর পাওয়া যায়নি। আবার চেষ্টা করো।"
                )
            } finally {
                isStreaming = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_doubt_solver_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("ai_doubt_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text(
                        text = StudyQuestStrings.t(language, "AI Doubt Solver", "এআই ডাউট সলভার"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = StudyQuestStrings.t(language, "Powered by Firebase AI Logic Gemini", "Firebase AI Logic Gemini দ্বারা চালিত"),
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricIndigo
                    )
                }
            }
        }

        item {
            StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                StudyQuestGradientHeroBanner(
                    eyebrow = StudyQuestStrings.t(language, "INSTANT GEMINI TUTOR", "তাৎক্ষণিক জেমিনি টিউটর"),
                    title = StudyQuestStrings.t(language, "Ask Any Academic Doubt", "পড়াশোনার যেকোনো প্রশ্ন করো"),
                    subtitle = StudyQuestStrings.t(
                        language,
                        "Get instant NCTB step-by-step explanations in Bangla or English.",
                        "বাংলা বা ইংরেজিতে এনসিটিবি পাঠ্যক্রম অনুযায়ী ধাপে ধাপে ব্যাখ্যা পাও।"
                    ),
                    reducedMotion = reducedMotion
                ) {}
            }
        }

        item {
            StudyQuestCard {
                Text(
                    text = StudyQuestStrings.t(language, "Choose Subject", "বিষয় নির্বাচন করো"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    subjects.take(8).forEach { subj ->
                        FilterChip(
                            selected = selectedSubject?.id == subj.id,
                            onClick = { selectedSubject = subj },
                            label = { Text(StudyQuestStrings.t(language, subj.titleEn, subj.titleBn)) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text(StudyQuestStrings.t(language, "Type your question or math problem...", "তোমার প্রশ্ন বা অংক লিখো...")) },
                    minLines = 2,
                    maxLines = 5,
                    enabled = !isStreaming,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_doubt_input")
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { submitQuestion(questionText) },
                    enabled = !isStreaming && questionText.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("ai_doubt_ask_btn")
                ) {
                    if (isStreaming) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(StudyQuestStrings.t(language, "Thinking with Gemini...", "জেমিনি সমাধান তৈরি করছে..."))
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(StudyQuestStrings.t(language, "Solve with Gemini AI", "জেমিনি দিয়ে সমাধান করো"), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    quickPrompts.forEach { sample ->
                        AssistChip(
                            onClick = {
                                questionText = sample
                                submitQuestion(sample)
                            },
                            label = { Text(sample, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            }
        }

        if (!errorText.isNullOrBlank()) {
            item {
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (isStreaming || streamingAnswer.isNotBlank()) {
            item {
                StudyQuestCard(modifier = Modifier.testTag("ai_doubt_streaming_card")) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = BerryViolet)
                        Text(
                            text = StudyQuestStrings.t(language, "Gemini Explanation", "জেমিনির ব্যাখ্যা"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = streamingAnswer.ifBlank { StudyQuestStrings.t(language, "Generating step-by-step solution...", "ধাপে ধাপে সমাধান লেখা হচ্ছে...") },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        items(history, key = { it.id }) { item ->
            StudyQuestCard(modifier = Modifier.testTag("ai_doubt_history_${item.id}")) {
                Text(
                    text = item.subjectLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricIndigo,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
