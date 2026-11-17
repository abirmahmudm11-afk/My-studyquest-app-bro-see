package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.GuestUpgradeBannerCard
import com.example.ui.components.HeroCarouselSlide
import com.example.ui.components.HeroDockAction
import com.example.ui.components.ReferenceActivityRow
import com.example.ui.components.ReferenceHeroCarouselStage
import com.example.ui.components.ReferenceSummaryPanel
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyTimeAndXpBarChart
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.CeruleanSlate
import com.example.ui.theme.CrispCoolSurface
import com.example.ui.theme.DeepSlateInk
import com.example.ui.theme.EditorialError
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.HeroMintCardEnd
import com.example.ui.theme.HeroMintCardStart
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.MineralTeal
import com.example.ui.theme.MutedMintFocal
import com.example.ui.theme.SlateBotanical
import com.example.ui.theme.SoftPowderBlue
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val todayMetric = remember(dailyStudySeries) {
        dailyStudySeries.lastOrNull()
    }
    val todayMinutes = todayMetric?.studyMinutes ?: (completedLessonsCount * 12)
    val dailyGoalMinutes = profile.dailyGoalMinutes.coerceAtLeast(15L).toInt()
    val goalRatio = (todayMinutes.toFloat() / dailyGoalMinutes.toFloat()).coerceIn(0f, 1f)
    val avgAccuracyPct = remember(subjectAnalytics, recentRecords) {
        val fromSubjects = subjectAnalytics.mapNotNull { it.averageAccuracy.takeIf { acc -> acc > 0.0 } }
        val fromRecent = recentRecords.mapNotNull { it.accuracy?.takeIf { acc -> acc > 0.0 } }
        val avg = (fromSubjects + fromRecent).average()
        if (avg.isNaN()) 0 else (avg * 100).toInt().coerceIn(0, 100)
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

    val cachedEntry = activeLockedBook?.let { book ->
        cachedBooks.firstOrNull { it.bookId == book.id }
    }
    val isInstalledOffline = cachedEntry?.downloadStatus == TextbookDownloadStatus.INSTALLED.id
    val subjectAccuracy = spotlightSubject?.let { subj ->
        subjectAnalytics.firstOrNull { it.subjectId == subj.id }?.averageAccuracy?.toFloat()
    } ?: goalRatio

    val heroSlides = remember(
        activeLockedBook,
        spotlightLesson,
        categorySubjects,
        subjectAnalytics,
        language,
        isInstalledOffline,
        subjectAccuracy
    ) {
        buildList {
            val primaryTitle = when {
                activeLockedBook != null -> StudyQuestStrings.t(language, activeLockedBook.titleEn, activeLockedBook.titleBn)
                spotlightLesson != null -> StudyQuestStrings.t(language, spotlightLesson.titleEn, spotlightLesson.titleBn)
                else -> StudyQuestStrings.t(language, "Continue Today's Course", "আজকের পাঠ চালিয়ে যান")
            }
            val primaryEyebrow = if (activeLockedBook != null) {
                val offlineTag = if (isInstalledOffline) {
                    StudyQuestStrings.t(language, " • Offline", " • অফলাইন")
                } else ""
                StudyQuestStrings.t(
                    language,
                    "ACTIVE TEXTBOOK • ${activeLockedBook.academicYear}$offlineTag",
                    "সক্রিয় পাঠ্যবই • ${activeLockedBook.academicYear}$offlineTag"
                )
            } else {
                StudyQuestStrings.t(language, "RECOMMENDED LESSON", "পরবর্তী পাঠ")
            }
            val primarySubtitle = when {
                spotlightLesson != null -> StudyQuestStrings.t(
                    language,
                    "Up next: ${spotlightLesson.titleEn} • ${spotlightLesson.estimatedMinutes} min guided session",
                    "পরবর্তী পাঠ: ${spotlightLesson.titleBn} • ${StudyQuestStrings.formatDigits(spotlightLesson.estimatedMinutes, language)} মিনিট"
                )
                activeLockedBook != null -> StudyQuestStrings.t(
                    language,
                    "${StudyQuestStrings.classLabel(activeLockedBook.classId, language)} • ${activeLockedBook.bookCode}",
                    "${StudyQuestStrings.classLabel(activeLockedBook.classId, language)} • ${activeLockedBook.bookCode}"
                )
                else -> StudyQuestStrings.t(
                    language,
                    "Focused syllabus drill aligned with your NCTB class.",
                    "আপনার শ্রেণির এনসিটিবি সিলেবাস অনুযায়ী অনুশীলন।"
                )
            }

            add(
                HeroCarouselSlide(
                    id = "primary_course",
                    eyebrow = primaryEyebrow,
                    title = primaryTitle,
                    subtitle = primarySubtitle,
                    metricValue = "${StudyQuestStrings.formatDigits((subjectAccuracy * 100).toInt(), language)}%",
                    metricCaption = StudyQuestStrings.t(language, "Readiness", "প্রস্তুতি"),
                    progress = subjectAccuracy,
                    actionLabel = StudyQuestStrings.t(language, "Continue Learning", "পড়া শুরু করুন"),
                    actionIcon = if (activeLockedBook != null) Icons.Filled.MenuBook else Icons.Filled.PlayArrow,
                    gradientColors = listOf(HeroMintCardStart, HeroMintCardEnd),
                    contentOnCard = DeepSlateInk,
                    secondaryActionLabel = if (activeLockedBook != null) StudyQuestStrings.t(language, "Switch", "বদলাও") else null,
                    secondaryActionTestTag = if (activeLockedBook != null) "today_switch_book_btn" else null,
                    onSecondaryAction = if (activeLockedBook != null) ({ showSwitchBookSheet = true }) else null,
                    cardTestTag = "today_active_book_card",
                    primaryActionTestTag = if (activeLockedBook != null) "today_open_reader_btn" else "today_start_quest_btn",
                    onClick = if (activeLockedBook != null) onOpenTextbookReader else onOpenQuickPractice
                )
            )

            categorySubjects.take(4).forEachIndexed { idx, subj ->
                val summary = subjectAnalytics.find { it.subjectId == subj.id }
                val mastery = summary?.averageAccuracy?.toFloat()?.coerceIn(0f, 1f) ?: 0f
                val chCount = BangladeshCurriculumCatalog.chapters.count { it.subjectId == subj.id }
                val cardGradient = when (idx % 3) {
                    0 -> listOf(Color(0xFFD7E6F5), Color(0xFFB5CFE8))
                    1 -> listOf(Color(0xFF426C91), Color(0xFF2D5375))
                    else -> listOf(Color(0xFF318482), Color(0xFF236668))
                }
                val inkColor = if (idx % 3 == 0) DeepSlateInk else Color.White
                add(
                    HeroCarouselSlide(
                        id = "subj_${subj.id}",
                        eyebrow = "${subj.code} • ${StudyQuestStrings.classLabel(profile.classId, language)}",
                        title = StudyQuestStrings.t(language, subj.titleEn, subj.titleBn),
                        subtitle = StudyQuestStrings.t(
                            language,
                            "$chCount structured chapters & board drills",
                            "${StudyQuestStrings.formatDigits(chCount, language)}টি অধ্যায় ও বোর্ড অনুশীলন"
                        ),
                        metricValue = "${StudyQuestStrings.formatDigits((mastery * 100).toInt(), language)}%",
                        metricCaption = StudyQuestStrings.t(language, "Mastery", "দক্ষতা"),
                        progress = mastery,
                        actionLabel = StudyQuestStrings.t(language, "Open Subject", "বিষয় খুলুন"),
                        actionIcon = Icons.AutoMirrored.Filled.MenuBook,
                        gradientColors = cardGradient,
                        contentOnCard = inkColor,
                        onClick = { onOpenSubjectCategory(subj) }
                    )
                )
            }
        }
    }

    val dockActions = remember(activeLockedBook, language) {
        listOf(
            HeroDockAction(
                id = "dock_continue",
                label = StudyQuestStrings.t(language, "Continue", "পড়ুন"),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = if (activeLockedBook != null) onOpenTextbookReader else onOpenQuickPractice
            ),
            HeroDockAction(
                id = "dock_practice",
                label = StudyQuestStrings.t(language, "Practice", "অনুশীলন"),
                icon = Icons.Filled.Bolt,
                onClick = onOpenQuickPractice
            ),
            HeroDockAction(
                id = "dock_ai",
                label = StudyQuestStrings.t(language, "AI Tutor", "AI টিউটর"),
                icon = Icons.Filled.AutoAwesome,
                onClick = onOpenAiDoubtSolver
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("today_home_list"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Wide Slate Blue-Gray Atmospheric Hero Stage with 3D Carousel & Glass Dock
        item {
            ReferenceHeroCarouselStage(
                slides = heroSlides,
                dockActions = dockActions,
                reducedMotion = reducedMotion,
                headerSlot = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("today_personal_header"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.80f)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = greetingTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            SyncStatusPill(
                                isOnline = isOnline,
                                pendingSyncCount = pendingSyncCount,
                                language = language
                            )
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
                                    tint = Color.White
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
                                            Badge(containerColor = MutedMintFocal, contentColor = DeepSlateInk) {
                                                Text("$unreadNotificationCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Notifications,
                                        contentDescription = StudyQuestStrings.t(language, "Notifications", "নোটিফিকেশন"),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        // Guest Sync or Academic Status Banners (when applicable)
        if (isGuestMode) {
            item {
                Box(modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth().padding(horizontal = 16.dp)) {
                    GuestUpgradeBannerCard(
                        language = language,
                        totalXp = profile.totalXp,
                        coins = profile.coins,
                        onSignInClick = onRequestGoogleSignIn
                    )
                }
            }
        }

        if (profile.annualExamStatus == "failed") {
            item {
                Box(modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth().padding(horizontal = 16.dp)) {
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
            }
        } else if (!profile.promotedFromClassId.isNullOrBlank()) {
            item {
                Box(modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth().padding(horizontal = 16.dp)) {
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
                                        "Promoted to ${StudyQuestStrings.classLabel(profile.classId, language)}",
                                        "${StudyQuestStrings.classLabel(profile.classId, language)}-এ উন্নীত হয়েছেন"
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = semantic.onSuccessContainer
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "Academic year ${profile.classLockedAcademicYear ?: ""} promotion is active.",
                                        "শিক্ষাবর্ষ ${profile.classLockedAcademicYear ?: ""} অনুযায়ী শ্রেণি উন্নয়ন সম্পন্ন হয়েছে।"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = semantic.onSuccessContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Two Compact Summary Panels Underneath the Hero (Reference-Inspired)
        item {
            StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 980.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReferenceSummaryPanel(
                        label = StudyQuestStrings.t(language, "Study time today", "আজকের অধ্যয়ন সময়"),
                        primaryValue = StudyQuestStrings.t(
                            language,
                            "${todayMinutes}m / ${dailyGoalMinutes}m",
                            "${StudyQuestStrings.formatDigits(todayMinutes, language)} / ${StudyQuestStrings.formatDigits(dailyGoalMinutes, language)} মি."
                        ),
                        supportingText = StudyQuestStrings.t(
                            language,
                            "${(goalRatio * 100).toInt()}% of daily target",
                            "দৈনিক লক্ষ্যের ${StudyQuestStrings.formatDigits((goalRatio * 100).toInt(), language)}%"
                        ),
                        progress = goalRatio,
                        accentColor = BrandTealPrimary,
                        onClick = onOpenPomodoro,
                        modifier = Modifier.weight(1f)
                    )
                    ReferenceSummaryPanel(
                        label = StudyQuestStrings.t(language, "Course accuracy", "গড় সঠিকতা"),
                        primaryValue = if (avgAccuracyPct > 0) {
                            "${StudyQuestStrings.formatDigits(avgAccuracyPct, language)}%"
                        } else {
                            StudyQuestStrings.t(language, "$completedLessonsCount done", "${StudyQuestStrings.formatDigits(completedLessonsCount, language)} সম্পন্ন")
                        },
                        supportingText = StudyQuestStrings.t(
                            language,
                            "$completedLessonsCount lessons • $completedExamsCount exams",
                            "${StudyQuestStrings.formatDigits(completedLessonsCount, language)} পাঠ • ${StudyQuestStrings.formatDigits(completedExamsCount, language)} পরীক্ষা"
                        ),
                        progress = if (avgAccuracyPct > 0) avgAccuracyPct / 100f else 0.15f,
                        accentColor = MutedMintFocal,
                        onClick = onOpenStudyProgress,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Clean Activity History Section (Reference-Inspired List Card)
        item {
            StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 980.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditorialSectionHeader(
                        title = StudyQuestStrings.t(language, "Recent Learning Activity", "সাম্প্রতিক অধ্যয়ন ইতিহাস"),
                        actionLabel = StudyQuestStrings.t(language, "Progress", "বিস্তারিত"),
                        onAction = onOpenStudyProgress
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                        border = studyQuestGlassAwareBorder(),
                        shadowElevation = if (visualConfig.isGlass) 0.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("today_continue_shelf")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (recentRecords.isNotEmpty()) {
                                recentRecords.take(4).forEachIndexed { idx, rec ->
                                    if (idx > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                    val subj = BangladeshCurriculumCatalog.subjects.find { it.id == rec.subjectId }
                                    val subjName = subj?.let { StudyQuestStrings.t(language, it.titleEn, it.titleBn) }
                                        ?: rec.subjectId.replaceFirstChar { it.uppercase() }
                                    val recIcon = when (rec.recordType) {
                                        "lesson" -> Icons.AutoMirrored.Filled.MenuBook
                                        "exam", "promotion_exam" -> Icons.Filled.HistoryEdu
                                        else -> Icons.Filled.Quiz
                                    }
                                    val recTint = subj?.accentHex?.let { Color(it) } ?: BrandTealPrimary
                                    val accText = rec.accuracy?.let {
                                        "${StudyQuestStrings.formatDigits((it * 100).toInt(), language)}%"
                                    } ?: StudyQuestStrings.t(language, "Done", "সম্পন্ন")
                                    val typeLabel = when (rec.recordType) {
                                        "lesson" -> StudyQuestStrings.t(language, "Lesson", "পাঠ")
                                        "exam", "promotion_exam" -> StudyQuestStrings.t(language, "Exam", "পরীক্ষা")
                                        else -> StudyQuestStrings.t(language, "Practice", "অনুশীলন")
                                    }
                                    ReferenceActivityRow(
                                        title = rec.summary ?: rec.refId,
                                        subtitle = subjName,
                                        trailingPrimary = accText,
                                        trailingSecondary = typeLabel,
                                        icon = recIcon,
                                        tint = recTint,
                                        onClick = onOpenStudyProgress
                                    )
                                }
                            } else if (studyPlans.isNotEmpty()) {
                                studyPlans.take(3).forEachIndexed { idx, plan ->
                                    if (idx > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                    val daysLeft = (((plan.targetDateEpochMs - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0L)).toInt()
                                    ReferenceActivityRow(
                                        title = plan.title,
                                        subtitle = StudyQuestStrings.t(language, "Active study plan", "সক্রিয় অধ্যয়ন পরিকল্পনা"),
                                        trailingPrimary = "${StudyQuestStrings.formatDigits(plan.minutesPerDay, language)}m",
                                        trailingSecondary = StudyQuestStrings.t(
                                            language,
                                            "$daysLeft days left",
                                            "${StudyQuestStrings.formatDigits(daysLeft, language)} দিন বাকি"
                                        ),
                                        icon = Icons.Filled.CalendarMonth,
                                        tint = CeruleanSlate,
                                        onClick = onOpenAssignmentsAndGoals
                                    )
                                }
                            } else {
                                categorySubjects.take(3).forEachIndexed { idx, subject ->
                                    if (idx > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                    val chCount = BangladeshCurriculumCatalog.chapters.count { it.subjectId == subject.id }
                                    val accent = Color(subject.accentHex)
                                    ReferenceActivityRow(
                                        title = StudyQuestStrings.t(language, subject.titleEn, subject.titleBn),
                                        subtitle = StudyQuestStrings.t(
                                            language,
                                            "$chCount chapters ready to study",
                                            "${StudyQuestStrings.formatDigits(chCount, language)}টি অধ্যায় প্রস্তুত"
                                        ),
                                        trailingPrimary = subject.code,
                                        trailingSecondary = StudyQuestStrings.t(language, "Open", "খুলুন"),
                                        icon = Icons.AutoMirrored.Filled.MenuBook,
                                        tint = accent,
                                        onClick = { onOpenSubjectCategory(subject) },
                                        modifier = Modifier.testTag("today_category_${subject.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Workspace Shortcuts Bento (Study Progress, Study Groups, Class Rankings, AI Doubt Solver)
        item {
            StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 980.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditorialSectionHeader(
                        eyebrow = StudyQuestStrings.t(language, "LEARNING WORKSPACE", "শেখার সহায়িকা"),
                        title = StudyQuestStrings.t(language, "Study Overview", "অধ্যয়ন ও বিশ্লেষণ"),
                        actionLabel = StudyQuestStrings.t(language, "Full Analytics", "বিস্তারিত"),
                        onAction = onOpenMasteryAnalytics
                    )

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
                            accentColor = BrandTealPrimary,
                            onClick = onOpenStudyProgress,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_hero_study_progress")
                        )
                        TodayFeatureHeroTile(
                            title = StudyQuestStrings.t(language, "Study Groups", "স্টাডি গ্রুপ"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "Classrooms & peer study",
                                "লাইভ রুম ও সহপাঠী"
                            ),
                            badge = StudyQuestStrings.t(language, "Rooms", "রুম"),
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
                            title = StudyQuestStrings.t(language, "Class Rankings", "শ্রেণি র‌্যাংকিং"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "Compare class accuracy",
                                "শ্রেণিভিত্তিক অবস্থান"
                            ),
                            badge = StudyQuestStrings.t(language, "Board", "বোর্ড"),
                            icon = Icons.Filled.EmojiEvents,
                            accentColor = SlateBotanical,
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
                            accentColor = CeruleanSlate,
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
                        StudyQuestCard(accentColor = CeruleanSlate.copy(alpha = 0.45f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = CeruleanSlate)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Recommended Review", "বিশেষ অনুশীলন"),
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

                    if (!isGuestMode) {
                        val activePlan = remember(subscription.tier) { QuestPlanCatalog.planById(subscription.tier) }
                        val aiLimit = subscription.monthlyAiLimit.toInt().coerceAtLeast(activePlan.monthlyAiCredits)
                        val aiLeft = (aiLimit - subscription.aiCreditsUsed.toInt()).coerceAtLeast(0)
                        Surface(
                            onClick = onOpenPlanStore,
                            shape = RoundedCornerShape(16.dp),
                            color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                            border = studyQuestGlassAwareBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("today_plan_strip")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
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
                                            "$aiLeft AI tutor credits available",
                                            "${StudyQuestStrings.formatDigits(aiLeft, language)}টি AI ক্রেডিট রয়েছে"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FilledTonalButton(onClick = onOpenPlanStore) {
                                    Text(StudyQuestStrings.t(language, "Manage", "প্ল্যান"))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Compact Quick Study Tools Shelf
        item {
            StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 980.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EditorialSectionHeader(
                        eyebrow = StudyQuestStrings.t(language, "TOOLS & PRACTICE", "দ্রুত টুলস"),
                        title = StudyQuestStrings.t(language, "Quick Study Tools", "দ্রুত অনুশীলন ও টুলস")
                    )

                    val quickTools = listOf(
                        Triple(StudyQuestStrings.t(language, "Quick Practice", "কুইক প্র্যাকটিস"), Icons.Filled.Bolt, "today_quick_practice" to onOpenQuickPractice),
                        Triple(
                            if (dailyChallengeDoneToday) StudyQuestStrings.t(language, "Daily Quiz Done", "দৈনিক সম্পন্ন")
                            else StudyQuestStrings.t(language, "Daily Quiz", "দৈনিক কুইজ"),
                            Icons.Filled.CheckCircle,
                            "today_daily_challenge" to onOpenDailyChallenge
                        ),
                        Triple(StudyQuestStrings.t(language, "Mock Exam", "মডেল টেস্ট"), Icons.Filled.HistoryEdu, "today_mock_exam" to onOpenMockExam),
                        Triple(StudyQuestStrings.t(language, "Flashcards", "ফ্ল্যাশকার্ড"), Icons.Filled.Style, "today_flashcards_vault" to onOpenFlashcardsVault),
                        Triple(StudyQuestStrings.t(language, "Focus Timer", "ফোকাস টাইমার"), Icons.Filled.Timer, "today_pomodoro" to onOpenPomodoro),
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
                            color = if (notif.read) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            else SoftPowderBlue.copy(alpha = if (visualConfig.isDark) 0.20f else 0.70f),
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
                    text = StudyQuestStrings.t(language, "Switch Active Textbook", "সক্রিয় পাঠ্যবই পরিবর্তন"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "Select a textbook to synchronize lessons, practice questions, and AI tutoring.",
                        "পাঠ, অনুশীলনী ও AI সহায়তার জন্য একটি পাঠ্যবই নির্বাচন করুন।"
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
                        accentColor = if (isSelected) BrandTealPrimary else SlateBotanical,
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
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = BrandTealPrimary)
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
        shape = RoundedCornerShape(18.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(accentColor.copy(alpha = 0.28f)),
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
                    color = accentColor.copy(alpha = 0.13f),
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
