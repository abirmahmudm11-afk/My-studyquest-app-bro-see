package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.CachedBookEntity
import com.example.data.local.LocalRevisionEntity
import com.example.data.model.AcademicSubject
import com.example.data.model.LearningRecordDoc
import com.example.data.model.PublicProfile
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.StudyPlanDoc
import com.example.data.model.TextbookCatalogItem
import com.example.data.model.TextbookDownloadStatus
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.CompactWeeklyStreakStrip
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.GuestUpgradeBannerCard
import com.example.ui.components.OpenBookQuestIllustration
import com.example.ui.components.QuestCard
import com.example.ui.components.RankTierBadge
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyTimeAndXpBarChart
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepQuestGold
import com.example.ui.theme.EditorialError
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.MineralTeal
import com.example.ui.theme.PureEditorialSurface
import com.example.ui.theme.QuestGold
import com.example.ui.theme.SlateBotanical
import com.example.ui.theme.SoftQuestGold
import com.example.ui.theme.SoftSageGreen
import com.example.ui.theme.WarmTerracotta
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class QuestStepState {
    COMPLETED,
    IN_PROGRESS,
    AVAILABLE,
    LOCKED
}

private data class TodayQuestStep(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val state: QuestStepState,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTabScreen(
    profile: PublicProfile,
    language: AppLanguage,
    reducedMotion: Boolean,
    isOnline: Boolean,
    pendingSyncCount: Int,
    quests: List<UserQuestDoc>,
    notifications: List<UserNotificationDoc>,
    unreadNotificationCount: Int,
    weakTopics: List<WeakTopicStat>,
    revisions: List<LocalRevisionEntity>,
    studyPlans: List<StudyPlanDoc>,
    recentRecords: List<LearningRecordDoc>,
    onClaimQuest: (UserQuestDoc) -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onOpenQuickPractice: () -> Unit,
    onOpenMockExam: () -> Unit,
    onOpenRevision: () -> Unit,
    onOpenAiDoubtSolver: () -> Unit,
    onOpenMessages: () -> Unit,
    subjectAnalytics: List<SubjectAnalyticsSummary> = emptyList(),
    dailyStudySeries: List<DailyStudyMetric> = emptyList(),
    dailyChallengeDoneToday: Boolean = false,
    onOpenSubjectCategory: (AcademicSubject) -> Unit = {},
    onOpenFlashcardsVault: () -> Unit = {},
    onOpenPomodoro: () -> Unit = {},
    onOpenDailyChallenge: () -> Unit = {},
    onOpenMasteryAnalytics: () -> Unit = {},
    onOpenAssignmentsAndGoals: () -> Unit = {},
    onOpenAiVideoStudio: () -> Unit = {},
    onOpenBadgesAndXp: () -> Unit = {},
    onBuyStreakFreeze: () -> Unit = {},
    onRetakePromotionExam: () -> Unit = {},
    subscription: UserSubscriptionDoc = UserSubscriptionDoc(),
    onOpenAiLab: () -> Unit = {},
    onOpenPlanStore: () -> Unit = {},
    onOpenStudyProgress: () -> Unit = {},
    onOpenStudyGroups: () -> Unit = {},
    onOpenLeaderboard: () -> Unit = {},
    isGuestMode: Boolean = false,
    onRequestGoogleSignIn: () -> Unit = {},
    activeLockedBook: TextbookCatalogItem? = null,
    availableBooksForLock: List<TextbookCatalogItem> = emptyList(),
    cachedBooks: List<CachedBookEntity> = emptyList(),
    onOpenTextbookReader: () -> Unit = {},
    onSwitchActiveBook: (TextbookCatalogItem) -> Unit = {}
) {
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showSwitchBookSheet by remember { mutableStateOf(false) }
    var switchYearFilter by remember { mutableStateOf(activeLockedBook?.academicYear ?: "2026") }
    val semantic = LocalStudyQuestColors.current
    val visualConfig = LocalStudyQuestSurfaceStyle.current

    val dueRevisionCount = remember(revisions) {
        val now = System.currentTimeMillis()
        revisions.count { it.nextDueAtMs <= now }
    }
    val (xpWithinLevel, xpRequiredForNext) = remember(profile.totalXp) {
        StudyQuestStrings.xpIntoCurrentLevel(profile.totalXp)
    }
    val dailyGoalXpTarget = (profile.dailyGoalMinutes.coerceAtLeast(15L) * 2L).toInt().coerceAtLeast(60)
    val dailyGoalProgress = remember(recentRecords, dailyGoalXpTarget) {
        recentRecords.take(4).sumOf { it.xpAwarded.toInt() }.coerceAtMost(dailyGoalXpTarget)
    }
    val categorySubjects = remember(profile.classId, profile.selectedSubjects, activeLockedBook) {
        val allForClass = BangladeshCurriculumCatalog.subjectsForClass(profile.classId)
        val filtered = allForClass.filter { it.id in profile.selectedSubjects }
        val base = filtered.ifEmpty { allForClass }
        if (activeLockedBook == null) base
        else base.sortedByDescending { it.id == activeLockedBook.subjectId }
    }
    val spotlightSubject = remember(activeLockedBook, categorySubjects) {
        categorySubjects.firstOrNull { it.id == activeLockedBook?.subjectId }
            ?: categorySubjects.firstOrNull()
    }
    val spotlightLesson = remember(profile.classId, profile.selectedSubjects, activeLockedBook) {
        val bookMatch = activeLockedBook?.let { book ->
            BangladeshCurriculumCatalog.lessons.firstOrNull { it.subjectId == book.subjectId }
        }
        val subjs = profile.selectedSubjects.ifEmpty {
            BangladeshCurriculumCatalog.subjectsForClass(profile.classId).map { it.id }
        }
        bookMatch
            ?: BangladeshCurriculumCatalog.lessons.firstOrNull { it.subjectId in subjs }
            ?: BangladeshCurriculumCatalog.lessons.firstOrNull()
    }
    val completedLessonsCount = remember(recentRecords) {
        recentRecords.count { it.recordType == "lesson" }
    }
    val completedExamsCount = remember(recentRecords) {
        recentRecords.count { it.recordType in listOf("exam", "promotion_exam") }
    }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingTitle = remember(currentHour, profile.displayName, language) {
        val name = profile.displayName.ifBlank { StudyQuestStrings.t(language, "Scholar", "শিক্ষার্থী") }
        when {
            currentHour < 12 -> StudyQuestStrings.t(language, "Good Morning, $name", "সুপ্রভাত, $name")
            currentHour < 17 -> StudyQuestStrings.t(language, "Good Afternoon, $name", "শুভ অপরাহ্ণ, $name")
            else -> StudyQuestStrings.t(language, "Good Evening, $name", "শুভ সন্ধ্যা, $name")
        }
    }
    val formattedDate = remember(language) {
        val locale = if (language == AppLanguage.BANGLA) Locale("bn", "BD") else Locale.ENGLISH
        SimpleDateFormat("EEEE, d MMM yyyy", locale).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("today_home_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Personal Header
        item {
            StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("today_personal_header"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = greetingTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SyncStatusPill(
                                    isOnline = isOnline,
                                    pendingSyncCount = pendingSyncCount,
                                    language = language
                                )
                                RankTierBadge(tier = profile.rankTier, language = language)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenMessages,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("today_open_messages")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = StudyQuestStrings.t(language, "Messages", "মেসেজ"),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { showNotificationsSheet = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("today_open_notifications")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationCount > 0) {
                                            Badge(containerColor = BrandForestGreen, contentColor = PureEditorialSurface) {
                                                Text("$unreadNotificationCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Notifications,
                                        contentDescription = StudyQuestStrings.t(language, "Notifications", "নোটিফিকেশন"),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (isGuestMode) {
                        GuestUpgradeBannerCard(
                            language = language,
                            totalXp = profile.totalXp,
                            coins = profile.coins,
                            onSignInClick = onRequestGoogleSignIn
                        )
                    }
                }
            }
        }

        // Holdback or Promotion Status Strip (real account academic state)
        if (profile.annualExamStatus == "failed") {
            item {
                StudyQuestCard(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                    modifier = Modifier.testTag("annual_exam_holdback_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = EditorialError)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Class Holdback: Annual Exam Incomplete",
                                    "শ্রেণি উন্নয়ন স্থগিত: বার্ষিক পরীক্ষা অসম্পূর্ণ"
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "You remain in ${StudyQuestStrings.classLabel(profile.classId, language)}. Pass the Promotion Exam (60%+) to advance.",
                                    "আপনি বর্তমানে ${StudyQuestStrings.classLabel(profile.classId, language)}-এ আছেন। প্রোমোশন পরীক্ষায় ৬০%+ পেলে পরবর্তী শ্রেণিতে যাবেন।"
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(
                            onClick = onRetakePromotionExam,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("retake_promotion_exam_btn")
                        ) {
                            Text(StudyQuestStrings.t(language, "Retake", "পুনঃপরীক্ষা"))
                        }
                    }
                }
            }
        } else if (!profile.promotedFromClassId.isNullOrBlank()) {
            item {
                StudyQuestCard(
                    containerColor = semantic.successContainer,
                    modifier = Modifier.testTag("annual_promotion_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = semantic.success)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Promoted to ${StudyQuestStrings.classLabel(profile.classId, language)}!",
                                    "${StudyQuestStrings.classLabel(profile.classId, language)}-এ উন্নীত হয়েছেন!"
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = semantic.onSuccessContainer
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Jan 1 academic year promotion active (${profile.classLockedAcademicYear ?: ""}).",
                                    "১ জানুয়ারি শিক্ষাবর্ষ অনুযায়ী স্বয়ংক্রিয় শ্রেণি উন্নয়ন সম্পন্ন হয়েছে।"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = semantic.onSuccessContainer
                            )
                        }
                    }
                }
            }
        }

        // 2. Your Next Quest — Asymmetric Editorial Hero Section
        item {
            StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                val cachedEntry = activeLockedBook?.let { book ->
                    cachedBooks.firstOrNull { it.bookId == book.id }
                }
                val isInstalledOffline = cachedEntry?.downloadStatus == TextbookDownloadStatus.INSTALLED.id
                val subjectAccuracy = spotlightSubject?.let { subj ->
                    subjectAnalytics.firstOrNull { it.subjectId == subj.id }?.averageAccuracy?.toFloat()
                } ?: (dailyGoalProgress.toFloat() / dailyGoalXpTarget.toFloat()).coerceIn(0f, 1f)
                val heroTitle = when {
                    activeLockedBook != null -> StudyQuestStrings.t(language, activeLockedBook.titleEn, activeLockedBook.titleBn)
                    spotlightLesson != null -> StudyQuestStrings.t(language, spotlightLesson.titleEn, spotlightLesson.titleBn)
                    else -> StudyQuestStrings.t(language, "Today's NCTB Quest", "আজকের এনসিটিবি কোয়েস্ট")
                }
                val heroDescription = when {
                    spotlightLesson != null -> StudyQuestStrings.t(
                        language,
                        "Up next: ${spotlightLesson.titleEn} • Focused ${spotlightLesson.estimatedMinutes}m session aligned with your NCTB syllabus.",
                        "পরবর্তী পাঠ: ${spotlightLesson.titleBn} • আপনার এনসিটিবি পাঠ্যক্রম অনুযায়ী ${StudyQuestStrings.formatDigits(spotlightLesson.estimatedMinutes, language)} মিনিটের সেশন।"
                    )
                    activeLockedBook != null -> StudyQuestStrings.t(
                        language,
                        "${StudyQuestStrings.classLabel(activeLockedBook.classId, language)} • ${activeLockedBook.bookCode} active textbook ready for guided study.",
                        "${StudyQuestStrings.classLabel(activeLockedBook.classId, language)} • ${activeLockedBook.bookCode} সক্রিয় বইটি পড়ার জন্য প্রস্তুত।"
                    )
                    else -> StudyQuestStrings.t(
                        language,
                        "Start a focused 15-minute sprint tailored to your class curriculum.",
                        "আপনার শ্রেণি অনুযায়ী ১৫ মিনিটের একটি মনোযোগী অনুশীলন শুরু করুন।"
                    )
                }

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                    border = studyQuestGlassAwareBorder(BrandForestGreen.copy(alpha = 0.25f)),
                    tonalElevation = if (visualConfig.isGlass) 0.dp else 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_active_book_card")
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        val wideHero = maxWidth >= 480.dp
                        val heroCopy: @Composable (Modifier) -> Unit = { copyMod ->
                            Column(
                                modifier = copyMod,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftSageGreen
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (activeLockedBook != null) {
                                                Icon(
                                                    Icons.Filled.Lock,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Text(
                                                text = if (activeLockedBook != null) {
                                                    StudyQuestStrings.t(
                                                        language,
                                                        "NEXT QUEST • ${activeLockedBook.academicYear}",
                                                        "পরবর্তী কোয়েস্ট • ${activeLockedBook.academicYear}"
                                                    )
                                                } else {
                                                    StudyQuestStrings.t(language, "YOUR NEXT QUEST", "আপনার পরবর্তী কোয়েস্ট")
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    if (activeLockedBook != null && isInstalledOffline) {
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = EditorialSuccess.copy(alpha = 0.14f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.CloudDone,
                                                    contentDescription = null,
                                                    tint = EditorialSuccess,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = StudyQuestStrings.t(language, "Offline", "অফলাইন"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EditorialSuccess
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = heroTitle,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = heroDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Actual learning progress + estimated duration
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = StudyQuestStrings.t(
                                                language,
                                                "Daily readiness: ${(subjectAccuracy * 100).toInt()}%",
                                                "দৈনিক প্রস্তুতি: ${StudyQuestStrings.formatDigits((subjectAccuracy * 100).toInt(), language)}%"
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = StudyQuestStrings.t(
                                                    language,
                                                    "${spotlightLesson?.estimatedMinutes ?: 15} min",
                                                    "${StudyQuestStrings.formatDigits(spotlightLesson?.estimatedMinutes ?: 15, language)} মি."
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    LinearProgressIndicator(
                                        progress = { subjectAccuracy.coerceIn(0.04f, 1f) },
                                        color = BrandForestGreen,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (activeLockedBook != null) {
                                        Button(
                                            onClick = onOpenTextbookReader,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = BrandForestGreen,
                                                contentColor = PureEditorialSurface
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .minimumInteractiveComponentSize()
                                                .testTag("today_open_reader_btn")
                                        ) {
                                            Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = StudyQuestStrings.t(language, "Start Quest", "কোয়েস্ট শুরু"),
                                                maxLines = 1
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = { showSwitchBookSheet = true },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("today_switch_book_btn")
                                        ) {
                                            Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(StudyQuestStrings.t(language, "Switch", "বদলাও"), maxLines = 1)
                                        }
                                    } else {
                                        Button(
                                            onClick = onOpenQuickPractice,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = BrandForestGreen,
                                                contentColor = PureEditorialSurface
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .minimumInteractiveComponentSize()
                                                .testTag("today_start_quest_btn")
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(StudyQuestStrings.t(language, "Start Quest", "কোয়েস্ট শুরু"))
                                        }
                                    }
                                }
                            }
                        }

                        if (wideHero) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                heroCopy(Modifier.weight(1.3f))
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (visualConfig.isDark) MaterialTheme.colorScheme.surfaceVariant else SoftSageGreen.copy(alpha = 0.55f),
                                    modifier = Modifier.weight(0.7f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        OpenBookQuestIllustration(
                                            modifier = Modifier.size(width = 148.dp, height = 112.dp),
                                            reducedMotion = reducedMotion
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OpenBookQuestIllustration(
                                        modifier = Modifier.size(width = 164.dp, height = 104.dp),
                                        reducedMotion = reducedMotion
                                    )
                                }
                                heroCopy(Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }

        // 3. Today's Quest Path (Horizontal Milestone Trail: Start -> Learn -> Practice -> Complete)
        item {
            StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                val claimableQuest = quests.firstOrNull { it.completed && !it.claimed }
                val allQuestsClaimed = quests.isNotEmpty() && quests.all { it.claimed }
                val hasActivityToday = dailyGoalProgress > 0 || dailyChallengeDoneToday || completedLessonsCount > 0 || quests.any { it.progress > 0 }
                val pathSteps = listOf(
                    TodayQuestStep(
                        id = "start",
                        title = StudyQuestStrings.t(language, "Start", "শুরু"),
                        subtitle = if (activeLockedBook != null) {
                            StudyQuestStrings.t(language, "Open book", "বই খোলো")
                        } else {
                            StudyQuestStrings.t(language, "Set focus", "লক্ষ্য ঠিক করো")
                        },
                        icon = Icons.Filled.Flag,
                        state = if (activeLockedBook != null || dailyGoalProgress > 0) QuestStepState.COMPLETED else QuestStepState.AVAILABLE,
                        onClick = { if (activeLockedBook != null) onOpenTextbookReader() else onOpenPomodoro() }
                    ),
                    TodayQuestStep(
                        id = "learn",
                        title = StudyQuestStrings.t(language, "Learn", "শেখা"),
                        subtitle = StudyQuestStrings.t(language, "Read topic", "পাঠ পড়ো"),
                        icon = Icons.Filled.MenuBook,
                        state = when {
                            completedLessonsCount > 0 -> QuestStepState.COMPLETED
                            recentRecords.isNotEmpty() -> QuestStepState.IN_PROGRESS
                            else -> QuestStepState.AVAILABLE
                        },
                        onClick = {
                            if (spotlightSubject != null) onOpenSubjectCategory(spotlightSubject)
                            else onOpenTextbookReader()
                        }
                    ),
                    TodayQuestStep(
                        id = "practice",
                        title = StudyQuestStrings.t(language, "Practice", "অনুশীলন"),
                        subtitle = if (dueRevisionCount > 0) {
                            StudyQuestStrings.t(language, "$dueRevisionCount due", "${StudyQuestStrings.formatDigits(dueRevisionCount, language)} বাকি")
                        } else {
                            StudyQuestStrings.t(language, "Daily sprint", "দৈনিক অনুশীলন")
                        },
                        icon = Icons.Filled.Bolt,
                        state = when {
                            dailyChallengeDoneToday -> QuestStepState.COMPLETED
                            dueRevisionCount > 0 -> QuestStepState.IN_PROGRESS
                            else -> QuestStepState.AVAILABLE
                        },
                        onClick = onOpenDailyChallenge
                    ),
                    TodayQuestStep(
                        id = "complete",
                        title = StudyQuestStrings.t(language, "Complete", "সম্পন্ন"),
                        subtitle = when {
                            claimableQuest != null -> StudyQuestStrings.t(language, "Claim reward", "পুরস্কার নাও")
                            allQuestsClaimed -> StudyQuestStrings.t(language, "All claimed", "সংগৃহীত")
                            !hasActivityToday -> StudyQuestStrings.t(language, "Finish a step", "ধাপ সম্পন্ন করো")
                            else -> StudyQuestStrings.t(language, "Take exam", "পরীক্ষা দাও")
                        },
                        icon = Icons.Filled.Star,
                        state = when {
                            allQuestsClaimed -> QuestStepState.COMPLETED
                            claimableQuest != null -> QuestStepState.AVAILABLE
                            !hasActivityToday -> QuestStepState.LOCKED
                            else -> QuestStepState.AVAILABLE
                        },
                        onClick = {
                            if (claimableQuest != null) onClaimQuest(claimableQuest)
                            else onOpenMockExam()
                        }
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EditorialSectionHeader(
                        eyebrow = StudyQuestStrings.t(language, "DAILY MILESTONES", "দৈনিক ধাপসমূহ"),
                        title = StudyQuestStrings.t(language, "Today's Quest Path", "আজকের কোয়েস্ট পথ"),
                        actionLabel = StudyQuestStrings.t(language, "Daily Quiz", "দৈনিক কুইজ"),
                        onAction = onOpenDailyChallenge
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("today_quest_path")
                    ) {
                        items(pathSteps.size) { index ->
                            val step = pathSteps[index]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                QuestPathMilestoneNode(
                                    step = step,
                                    language = language
                                )
                                if (index < pathSteps.lastIndex) {
                                    val completedLink = step.state == QuestStepState.COMPLETED
                                    val linkColor = if (completedLink) QuestGold else MaterialTheme.colorScheme.outline
                                    Canvas(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height(12.dp)
                                    ) {
                                        drawLine(
                                            color = linkColor,
                                            start = Offset(2.dp.toPx(), size.height / 2f),
                                            end = Offset(size.width - 2.dp.toPx(), size.height / 2f),
                                            strokeWidth = 2.5.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            pathEffect = if (completedLink) null else PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Active quests ready to claim or track directly below milestone track
                    if (quests.isNotEmpty()) {
                        quests.take(2).forEach { qDoc ->
                            val cat = BangladeshCurriculumCatalog.questsCatalog.find { it.id == qDoc.questId }
                            QuestCard(
                                quest = qDoc,
                                titleEn = cat?.titleEn ?: qDoc.questId,
                                titleBn = cat?.titleBn ?: qDoc.questId,
                                descriptionEn = cat?.descriptionEn ?: "",
                                descriptionBn = cat?.descriptionBn ?: "",
                                language = language,
                                onClaim = { onClaimQuest(qDoc) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Continue Your Journey (Horizontal Shelf of Active Subjects, Plans, Recent Records)
        item {
            StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EditorialSectionHeader(
                        eyebrow = StudyQuestStrings.t(language, "RESUME WHERE YOU LEFT OFF", "যেখান থেকে থেমেছিলেন"),
                        title = StudyQuestStrings.t(language, "Continue Your Journey", "শেখা চালিয়ে যান"),
                        actionLabel = StudyQuestStrings.t(language, "Revision ($dueRevisionCount)", "রিভিশন ($dueRevisionCount)"),
                        onAction = onOpenRevision
                    )

                    if (categorySubjects.isEmpty() && studyPlans.isEmpty() && recentRecords.isEmpty()) {
                        StudyQuestCard(onClick = onOpenQuickPractice) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Filled.School, contentDescription = null, tint = BrandForestGreen)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Begin your first session", "প্রথম অনুশীলন শুরু করুন"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Complete any lesson or drill and it will appear right here.",
                                            "যেকোনো পাঠ বা অনুশীলন শেষ করলেই এখানে ফিরে আসার কার্ড দেখাবে।"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BrandForestGreen)
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("today_continue_shelf")
                        ) {
                            items(categorySubjects.take(5), key = { "shelf_subj_${it.id}" }) { subject ->
                                val summary = subjectAnalytics.find { it.subjectId == subject.id }
                                val mastery = summary?.averageAccuracy?.toFloat()?.coerceIn(0f, 1f) ?: 0f
                                val chCount = BangladeshCurriculumCatalog.chapters.count { it.subjectId == subject.id }
                                val accent = Color(subject.accentHex)
                                Surface(
                                    onClick = { onOpenSubjectCategory(subject) },
                                    shape = RoundedCornerShape(18.dp),
                                    color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                    border = studyQuestGlassAwareBorder(accent.copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .width(214.dp)
                                        .minimumInteractiveComponentSize()
                                        .testTag("today_category_${subject.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = accent.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = subject.code,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accent,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Text(
                                                text = StudyQuestStrings.t(
                                                    language,
                                                    "$chCount ch",
                                                    "${StudyQuestStrings.formatDigits(chCount, language)} অধ্যায়"
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = StudyQuestStrings.t(language, subject.titleEn, subject.titleBn),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        LinearProgressIndicator(
                                            progress = { mastery.coerceIn(0.05f, 1f) },
                                            color = accent,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(999.dp))
                                        )
                                        Text(
                                            text = if (mastery > 0f) {
                                                StudyQuestStrings.t(language, "${(mastery * 100).toInt()}% mastery • Resume", "${StudyQuestStrings.formatDigits((mastery * 100).toInt(), language)}% দক্ষতা")
                                            } else {
                                                StudyQuestStrings.t(language, "Tap to continue", "চালিয়ে যান")
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            items(studyPlans.take(2), key = { "shelf_plan_${it.id}" }) { plan ->
                                val daysLeft = (((plan.targetDateEpochMs - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0L)).toInt()
                                Surface(
                                    onClick = onOpenAssignmentsAndGoals,
                                    shape = RoundedCornerShape(18.dp),
                                    color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                    border = studyQuestGlassAwareBorder(QuestGold.copy(alpha = 0.55f)),
                                    modifier = Modifier
                                        .width(214.dp)
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = DeepQuestGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = StudyQuestStrings.t(language, "STUDY PLAN", "পড়ার লক্ষ্য"),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepQuestGold
                                            )
                                        }
                                        Text(
                                            text = plan.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = StudyQuestStrings.t(
                                                language,
                                                "$daysLeft days left • ${plan.minutesPerDay}m/day",
                                                "${StudyQuestStrings.formatDigits(daysLeft, language)} দিন বাকি"
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            items(recentRecords.take(3), key = { "shelf_rec_${it.id}" }) { rec ->
                                Surface(
                                    onClick = onOpenStudyProgress,
                                    shape = RoundedCornerShape(18.dp),
                                    color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                    border = studyQuestGlassAwareBorder(),
                                    modifier = Modifier
                                        .width(204.dp)
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = rec.recordType.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = rec.summary ?: rec.refId,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "+${StudyQuestStrings.formatDigits(rec.xpAwarded, language)} XP",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EditorialSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Your Progress Bento Cluster + Quest Destinations + 7-Day Chart
        item {
            StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EditorialSectionHeader(
                        eyebrow = StudyQuestStrings.t(language, "LEVEL ${profile.overallLevel}", "লেভেল ${StudyQuestStrings.formatDigits(profile.overallLevel, language)}"),
                        title = StudyQuestStrings.t(language, "Your Progress", "আপনার অগ্রগতি"),
                        actionLabel = StudyQuestStrings.t(language, "Full Analytics", "বিস্তারিত"),
                        onAction = onOpenMasteryAnalytics
                    )

                    // Daily Mastery + Level Progress Card
                    StudyQuestCard(accentColor = BrandForestGreen.copy(alpha = 0.35f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AnimatedCircularMasteryRing(
                                progress = dailyGoalProgress.toFloat() / dailyGoalXpTarget.toFloat(),
                                centerValueText = "${StudyQuestStrings.formatDigits(dailyGoalProgress, language)}/$dailyGoalXpTarget",
                                centerCaptionText = StudyQuestStrings.t(language, "Daily XP", "দৈনিক"),
                                reducedMotion = reducedMotion,
                                size = 82.dp,
                                ringColor = BrandForestGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                valueColor = MaterialTheme.colorScheme.onSurface,
                                captionColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = StudyQuestStrings.levelTitle(profile.overallLevel.toInt(), language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                LinearProgressIndicator(
                                    progress = { (xpWithinLevel.toFloat() / xpRequiredForNext.toFloat()).coerceIn(0f, 1f) },
                                    color = QuestGold,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "$xpWithinLevel / $xpRequiredForNext XP to Lv.${profile.overallLevel + 1} • ${profile.totalXp} Total XP",
                                        "${StudyQuestStrings.formatDigits(xpWithinLevel, language)} / ${StudyQuestStrings.formatDigits(xpRequiredForNext, language)} XP (মোট ${StudyQuestStrings.formatDigits(profile.totalXp, language)} XP)"
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4 Editorial Bento Destination Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TodayFeatureHeroTile(
                            title = StudyQuestStrings.t(language, "Study Progress", "অধ্যয়ন অগ্রগতি"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "$completedLessonsCount lessons • $completedExamsCount exams",
                                "${StudyQuestStrings.formatDigits(completedLessonsCount, language)} পাঠ • ${StudyQuestStrings.formatDigits(completedExamsCount, language)} পরীক্ষা"
                            ),
                            badge = StudyQuestStrings.t(language, "Charts", "চার্ট"),
                            icon = Icons.Filled.TrendingUp,
                            accentColor = BrandForestGreen,
                            onClick = onOpenStudyProgress,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_hero_study_progress")
                        )
                        TodayFeatureHeroTile(
                            title = StudyQuestStrings.t(language, "Study Groups", "স্টাডি গ্রুপ"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "Live rooms & board",
                                "লাইভ রুম ও বোর্ড"
                            ),
                            badge = StudyQuestStrings.t(language, "Live", "লাইভ"),
                            icon = Icons.Filled.Groups,
                            accentColor = MineralTeal,
                            onClick = onOpenStudyGroups,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_hero_study_groups")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TodayFeatureHeroTile(
                            title = StudyQuestStrings.t(language, "Leaderboard", "লিডারবোর্ড"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "${profile.rankTier} • ${profile.totalXp} XP",
                                "${profile.rankTier} • ${StudyQuestStrings.formatDigits(profile.totalXp, language)} XP"
                            ),
                            badge = StudyQuestStrings.t(language, "Top 50", "শীর্ষ ৫০"),
                            icon = Icons.Filled.EmojiEvents,
                            accentColor = DeepQuestGold,
                            onClick = onOpenLeaderboard,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_hero_leaderboard")
                        )
                        TodayFeatureHeroTile(
                            title = StudyQuestStrings.t(language, "AI Doubt Solver", "এআই সমাধান"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "Step-by-step NCTB help",
                                "ধাপে ধাপে উত্তর"
                            ),
                            badge = "AI",
                            icon = Icons.Filled.AutoAwesome,
                            accentColor = WarmTerracotta,
                            onClick = onOpenAiDoubtSolver,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_hero_ai_solver")
                        )
                    }

                    StudyTimeAndXpBarChart(
                        dailySeries = dailyStudySeries,
                        language = language
                    )

                    if (weakTopics.isNotEmpty()) {
                        val topWeak = weakTopics.first()
                        val pct = (topWeak.averageAccuracy * 100).toInt()
                        StudyQuestCard(accentColor = WarmTerracotta.copy(alpha = 0.55f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = WarmTerracotta)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Focus Suggestion", "বিশেষ অনুশীলন"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "${topWeak.topicLabelEn}: $pct% across ${topWeak.attempts} attempts",
                                            "${topWeak.topicLabelBn}: ${StudyQuestStrings.formatDigits(pct, language)}% (${StudyQuestStrings.formatDigits(topWeak.attempts, language)} চেষ্টায়)"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FilledTonalButton(onClick = onOpenQuickPractice) {
                                    Text(StudyQuestStrings.t(language, "Practice", "অনুশীলন"))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Streaks & Achievements Band (Soft Gold positive celebration)
        item {
            StaggeredEntrance(index = 5, reducedMotion = reducedMotion) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (visualConfig.isDark) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f) else SoftQuestGold,
                    border = BorderStroke(1.dp, QuestGold.copy(alpha = 0.50f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(language, "CONSISTENCY & REWARDS", "ধারাবাহিকতা ও অর্জন"),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepQuestGold
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "${profile.streakCount}-Day Momentum • ${profile.coins} Coins",
                                        "${StudyQuestStrings.formatDigits(profile.streakCount, language)} দিনের ধারাবাহিকতা • ${StudyQuestStrings.formatDigits(profile.coins, language)} কয়েন"
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenBadgesAndXp,
                                border = BorderStroke(1.dp, QuestGold),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("today_badges_xp")
                            ) {
                                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(StudyQuestStrings.t(language, "Badges", "ব্যাজ"))
                            }
                        }

                        CompactWeeklyStreakStrip(
                            streakCount = profile.streakCount,
                            historyKeys = profile.streakHistoryKeys,
                            freezeCount = profile.streakFreezeCount,
                            language = language,
                            onBuyFreeze = if (!isGuestMode) onBuyStreakFreeze else null,
                            showBuyFreeze = !isGuestMode
                        )

                        if (!isGuestMode) {
                            val activePlan = remember(subscription.tier) { QuestPlanCatalog.planById(subscription.tier) }
                            val aiLimit = subscription.monthlyAiLimit.toInt().coerceAtLeast(activePlan.monthlyAiCredits)
                            val aiLeft = (aiLimit - subscription.aiCreditsUsed.toInt()).coerceAtLeast(0)
                            Surface(
                                onClick = onOpenPlanStore,
                                shape = RoundedCornerShape(14.dp),
                                color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                border = studyQuestGlassAwareBorder(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("today_plan_strip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = StudyQuestStrings.t(language, "${activePlan.titleEn} Plan", "${activePlan.titleBn} প্ল্যান"),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = StudyQuestStrings.t(
                                                language,
                                                "$aiLeft AI credits available",
                                                "${StudyQuestStrings.formatDigits(aiLeft, language)}টি AI ক্রেডিট রয়েছে"
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    FilledTonalButton(onClick = onOpenPlanStore) {
                                        Text(StudyQuestStrings.t(language, "Plans", "প্ল্যান"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Compact Quick Actions Strip
        item {
            StaggeredEntrance(index = 6, reducedMotion = reducedMotion) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EditorialSectionHeader(
                        eyebrow = StudyQuestStrings.t(language, "SHORTCUTS", "দ্রুত শর্টকাট"),
                        title = StudyQuestStrings.t(language, "Quick Study Tools", "দ্রুত অনুশীলন ও টুলস")
                    )

                    val quickTools = listOf(
                        Triple(StudyQuestStrings.t(language, "Quick Practice", "কুইক প্র্যাকটিস"), Icons.Filled.Bolt, "today_quick_practice" to onOpenQuickPractice),
                        Triple(
                            if (dailyChallengeDoneToday) StudyQuestStrings.t(language, "Daily Done", "দৈনিক সম্পন্ন")
                            else StudyQuestStrings.t(language, "Daily Streak", "দৈনিক চ্যালেঞ্জ"),
                            if (dailyChallengeDoneToday) Icons.Filled.CheckCircle else Icons.Filled.LocalFireDepartment,
                            "today_daily_challenge" to onOpenDailyChallenge
                        ),
                        Triple(StudyQuestStrings.t(language, "Mock Exam", "মডেল টেস্ট"), Icons.Filled.HistoryEdu, "today_mock_exam" to onOpenMockExam),
                        Triple(StudyQuestStrings.t(language, "Flashcards", "ফ্ল্যাশকার্ড"), Icons.Filled.Style, "today_flashcards_vault" to onOpenFlashcardsVault),
                        Triple(StudyQuestStrings.t(language, "Pomodoro Focus", "পমোডোরো"), Icons.Filled.Timer, "today_pomodoro" to onOpenPomodoro),
                        Triple(StudyQuestStrings.t(language, "Revision ($dueRevisionCount)", "রিভিশন ($dueRevisionCount)"), Icons.Filled.Replay, "today_revision" to onOpenRevision),
                        Triple(StudyQuestStrings.t(language, "AI Lab", "AI ল্যাব"), Icons.Filled.AutoAwesome, "today_ai_lab" to onOpenAiLab),
                        Triple(StudyQuestStrings.t(language, "Quick Doubt", "দ্রুত সমাধান"), Icons.Filled.AutoAwesome, "today_ai_solver" to onOpenAiDoubtSolver),
                        Triple(StudyQuestStrings.t(language, "Goals & Tasks", "লক্ষ্য ও কাজ"), Icons.Filled.Assignment, "today_assignments_goals" to onOpenAssignmentsAndGoals),
                        Triple(StudyQuestStrings.t(language, "Analytics", "বিশ্লেষণ"), Icons.Filled.Analytics, "today_mastery_analytics" to onOpenMasteryAnalytics),
                        Triple(StudyQuestStrings.t(language, "Video Studio", "ভিডিও স্টুডিও"), Icons.Filled.VideoLibrary, "today_ai_video_studio" to onOpenAiVideoStudio)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickTools, key = { it.third.first }) { (label, icon, tagAndClick) ->
                            val (tag, onClick) = tagAndClick
                            Surface(
                                onClick = onClick,
                                shape = RoundedCornerShape(999.dp),
                                color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                border = studyQuestGlassAwareBorder(),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag(tag)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsSheet) {
        BackHandler { showNotificationsSheet = false }
        ModalBottomSheet(onDismissRequest = { showNotificationsSheet = false }) {
            BackHandler { showNotificationsSheet = false }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = StudyQuestStrings.t(language, "Notifications", "নোটিফিকেশন"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (notifications.isEmpty()) {
                    Text(
                        text = StudyQuestStrings.t(language, "You're all caught up!", "কোনো নতুন নোটিফিকেশন নেই!"),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    notifications.take(15).forEach { notif ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (notif.read) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(notif.body, style = MaterialTheme.typography.bodySmall)
                                }
                                if (!notif.read) {
                                    FilledTonalButton(onClick = { onMarkNotificationRead(notif.id) }) {
                                        Text(StudyQuestStrings.t(language, "Mark read", "পঠিত"))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showSwitchBookSheet) {
        BackHandler { showSwitchBookSheet = false }
        ModalBottomSheet(onDismissRequest = { showSwitchBookSheet = false }) {
            val filteredChoices = remember(availableBooksForLock, switchYearFilter, profile.classId, profile.curriculumVersion, profile.academicGroup) {
                val base = availableBooksForLock.ifEmpty {
                    TextbookCatalog.booksFor(
                        academicYear = switchYearFilter,
                        classId = profile.classId,
                        curriculumVersion = profile.curriculumVersion,
                        academicGroup = profile.academicGroup
                    )
                }
                base.filter { it.academicYear == switchYearFilter }.ifEmpty { base }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("today_switch_book_sheet"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = StudyQuestStrings.t(language, "Switch Locked Active Textbook", "লককৃত সক্রিয় পাঠ্যবই পরিবর্তন"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "One textbook stays locked across lessons, quizzes, flashcards & AI Tutor. Selecting a book installs it offline.",
                        "পাঠ, কুইজ, ফ্ল্যাশকার্ড ও AI টিউটর নির্বাচিত একটি বইয়েই লক থাকবে এবং স্বয়ংক্রিয়ভাবে অফলাইনে সংরক্ষিত হবে।"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("2026" to "2026 NCTB Official", "2027" to "2027 NCTB Track").forEach { (yr, label) ->
                        FilterChip(
                            selected = switchYearFilter == yr,
                            onClick = { switchYearFilter = yr },
                            label = { Text(label) }
                        )
                    }
                }
                filteredChoices.take(12).forEach { choice ->
                    val isSelected = choice.id == activeLockedBook?.id
                    StudyQuestCard(
                        accentColor = if (isSelected) BrandForestGreen else SlateBotanical,
                        onClick = {
                            onSwitchActiveBook(choice)
                            showSwitchBookSheet = false
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(language, choice.titleEn, choice.titleBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${choice.bookCode} • ${StudyQuestStrings.t(language, choice.editionLabelEn, choice.editionLabelBn)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = BrandForestGreen)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuestPathMilestoneNode(
    step: TodayQuestStep,
    language: AppLanguage
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val (bgColor, borderColor, iconTint, stateBadge) = when (step.state) {
        QuestStepState.COMPLETED -> listOf(
            if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftSageGreen,
            BrandForestGreen,
            BrandForestGreen,
            StudyQuestStrings.t(language, "Done", "সম্পন্ন")
        )
        QuestStepState.IN_PROGRESS -> listOf(
            if (visualConfig.isDark) MaterialTheme.colorScheme.tertiaryContainer else SoftQuestGold,
            QuestGold,
            DeepQuestGold,
            StudyQuestStrings.t(language, "Active", "চলমান")
        )
        QuestStepState.AVAILABLE -> listOf(
            studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.primary,
            StudyQuestStrings.t(language, "Ready", "প্রস্তুত")
        )
        QuestStepState.LOCKED -> listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            MaterialTheme.colorScheme.outlineVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            StudyQuestStrings.t(language, "Locked", "লক")
        )
    }

    @Suppress("UNCHECKED_CAST")
    val resolvedBg = bgColor as Color
    val resolvedBorder = borderColor as Color
    val resolvedIconTint = iconTint as Color
    val resolvedBadge = stateBadge as String

    Surface(
        onClick = step.onClick,
        enabled = step.state != QuestStepState.LOCKED,
        shape = RoundedCornerShape(18.dp),
        color = resolvedBg,
        border = BorderStroke(1.2.dp, resolvedBorder),
        modifier = Modifier
            .width(126.dp)
            .minimumInteractiveComponentSize()
            .testTag("quest_path_step_${step.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = resolvedIconTint.copy(alpha = 0.16f),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (step.state) {
                                QuestStepState.COMPLETED -> Icons.Filled.CheckCircle
                                QuestStepState.LOCKED -> Icons.Filled.Lock
                                else -> step.icon
                            },
                            contentDescription = step.title,
                            tint = resolvedIconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = resolvedBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = resolvedIconTint
                )
            }
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = step.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TodayFeatureHeroTile(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(accentColor.copy(alpha = 0.40f)),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
