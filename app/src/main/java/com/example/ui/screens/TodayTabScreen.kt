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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Style
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.GamificationCatalog
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
import com.example.data.model.UserBadgeDoc
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import com.example.ui.components.ActivityDonutBreakdownCard
import com.example.ui.components.ActivityRingSlice
import com.example.ui.components.CharcoalMilestoneCard
import com.example.ui.components.CompactWeeklyStreakStrip
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.GuestUpgradeBannerCard
import com.example.ui.components.MapThePathCard
import com.example.ui.components.ModeStatusBottomSheet
import com.example.ui.components.PastelQuestBentoTile
import com.example.ui.components.ReferenceActivityRow
import com.example.ui.components.ReferenceSummaryPanel
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyTimeAndXpBarChart
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.CeruleanSlate
import com.example.ui.theme.EditorialError
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.MineralTeal
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
    onSwitchActiveBook: (TextbookCatalogItem) -> Unit = {},
    userBadges: List<UserBadgeDoc> = emptyList(),
    onOpenStemLab: () -> Unit = {},
    onOpenDownloadManager: () -> Unit = {},
    onOpenFormulaAtlas: () -> Unit = {},
    onOpenMistakeNotebook: () -> Unit = {},
    onOpenExamCountdown: () -> Unit = {},
    onOpenPocketAudio: () -> Unit = {},
    unresolvedMistakesCount: Int = 0,
    offlineModeEnabled: Boolean = false,
    isNetworkAvailable: Boolean = isOnline,
    onToggleOfflineMode: ((Boolean) -> Unit)? = null
) {
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showSwitchBookSheet by remember { mutableStateOf(false) }
    var showModeStatusSheet by remember { mutableStateOf(false) }
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

    val primaryTitle = when {
        activeLockedBook != null -> StudyQuestStrings.t(language, activeLockedBook.titleEn, activeLockedBook.titleBn)
        spotlightLesson != null -> StudyQuestStrings.t(language, spotlightLesson.titleEn, spotlightLesson.titleBn)
        else -> StudyQuestStrings.t(language, "Continue Today's Course", "আজকের পাঠ চালিয়ে যান")
    }
    val primaryEyebrow = if (activeLockedBook != null) {
        val offlineTag = if (isInstalledOffline) StudyQuestStrings.t(language, " • OFFLINE", " • অফলাইন") else ""
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
            "Up next: ${spotlightLesson.titleEn} • ${spotlightLesson.estimatedMinutes} min",
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("today_home_list"),
        contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Clean Swiss Monochrome Editorial Header + Primary Focal Study Card
        item {
            Column(
                modifier = Modifier
                    .widthIn(max = 980.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_personal_header"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formattedDate.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = greetingTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        SyncStatusPill(
                            isOnline = isOnline,
                            pendingSyncCount = pendingSyncCount,
                            language = language,
                            onClick = { showModeStatusSheet = true }
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
                                tint = MaterialTheme.colorScheme.onBackground
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
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text("$unreadNotificationCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = StudyQuestStrings.t(language, "Notifications", "নোটিফিকেশন"),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                // Single High-Contrast Monochrome Focal Card (Active Textbook / Recommended Lesson)
                val focalBg = if (visualConfig.isDark) Color(0xFF161D2A) else Color(0xFF0F172A)
                val focalBorder = if (visualConfig.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFF1E293B)
                val readinessPct = (subjectAccuracy * 100).toInt().coerceIn(0, 100)
                Surface(
                    onClick = if (activeLockedBook != null) onOpenTextbookReader else onOpenQuickPractice,
                    shape = RoundedCornerShape(16.dp),
                    color = focalBg,
                    border = BorderStroke(1.dp, focalBorder),
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_active_book_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = primaryEyebrow,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.65f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = primaryTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
                            ) {
                                Text(
                                    text = "${StudyQuestStrings.formatDigits(readinessPct, language)}%",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Text(
                            text = primarySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        LinearProgressIndicator(
                            progress = { subjectAccuracy.coerceIn(0.05f, 1f) },
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = if (activeLockedBook != null) onOpenTextbookReader else onOpenQuickPractice,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag(if (activeLockedBook != null) "today_open_reader_btn" else "today_start_quest_btn")
                            ) {
                                Icon(
                                    imageVector = if (activeLockedBook != null) Icons.Filled.MenuBook else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = StudyQuestStrings.t(language, "Continue Learning", "পড়া শুরু করুন"),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            if (activeLockedBook != null) {
                                if (!isInstalledOffline) {
                                    TextButton(
                                        onClick = onOpenDownloadManager,
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("today_download_book_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DownloadForOffline,
                                            contentDescription = StudyQuestStrings.t(language, "Download offline", "ডাউনলোড"),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = StudyQuestStrings.t(language, "Save", "সেভ"),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                TextButton(
                                    onClick = { showSwitchBookSheet = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.85f)),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("today_switch_book_btn")
                                ) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Switch", "বদলাও"),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!isOnline) {
            item {
                Box(modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth().padding(horizontal = 16.dp)) {
                    StudyQuestCard(modifier = Modifier.testTag("today_offline_vault_banner")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "Offline Study Mode",
                                        "অফলাইন স্টাডি মোড"
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "Textbooks, audio briefs, STEM labs & 6,000-entry solver work offline. Live 1v1, chat & cloud sync wait for Online Mode.",
                                        "পাঠ্যবই, অডিও, STEM ল্যাব ও সমাধান অফলাইনে চালু; লাইভ ১v১, চ্যাট ও সিঙ্ক অনলাইনে চালু হবে।"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                FilledTonalButton(
                                    onClick = onOpenDownloadManager,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("offline_banner_downloads_btn")
                                ) {
                                    Text(StudyQuestStrings.t(language, "Books", "বই ও প্যাক"))
                                }
                                if (offlineModeEnabled && isNetworkAvailable && onToggleOfflineMode != null) {
                                    TextButton(
                                        onClick = { onToggleOfflineMode(false) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("offline_banner_go_online_btn")
                                    ) {
                                        Text(StudyQuestStrings.t(language, "Go Online", "অনলাইনে যান"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
                        accentColor = MaterialTheme.colorScheme.primary,
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
                        accentColor = semantic.success,
                        onClick = onOpenStudyProgress,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2B. Playful Quest Section (Photo 1 Hybrid): Streak Calendar, Map the Path, Pastel Bento, Charcoal Badge Spotlight & My Activity Ring
        item {
            val activeHistoryKeys = remember(dailyStudySeries) {
                dailyStudySeries.filter { it.studyMinutes > 0 || it.lessonsCompleted > 0 || it.examsCompleted > 0 }.map { it.dayKey }
            }
            val unlockedBadgeDocs = remember(userBadges) { userBadges.filter { it.unlocked } }
            val unlockedCount = unlockedBadgeDocs.size
            val totalBadgesCount = GamificationCatalog.badgesCatalog.size
            val badgeMap = remember(userBadges) { userBadges.associateBy { it.badgeId } }
            val nextBadgeDef = remember(badgeMap) {
                GamificationCatalog.badgesCatalog
                    .filter { badgeMap[it.id]?.unlocked != true }
                    .maxByOrNull { def ->
                        val p = (badgeMap[def.id]?.progress ?: 0L).toFloat()
                        p / def.target.coerceAtLeast(1L).toFloat()
                    }
                    ?: GamificationCatalog.badgesCatalog.first()
            }
            val nextBadgeProg = (badgeMap[nextBadgeDef.id]?.progress ?: 0L).coerceAtMost(nextBadgeDef.target)
            val totalWeekMinutes = remember(dailyStudySeries, todayMinutes) {
                dailyStudySeries.sumOf { it.studyMinutes }.coerceAtLeast(todayMinutes)
            }
            val practiceCount = remember(recentRecords) {
                recentRecords.count { it.recordType !in listOf("lesson", "exam", "promotion_exam") }
            }
            val pathTitle = activeLockedBook?.let {
                StudyQuestStrings.t(language, it.titleEn, it.titleBn)
            } ?: spotlightSubject?.let {
                StudyQuestStrings.t(language, it.titleEn, it.titleBn)
            } ?: StudyQuestStrings.t(language, "NCTB Study Path", "এনসিটিবি পাঠ্যক্রম")
            val totalClassLessons = remember(profile.classId, activeLockedBook) {
                val subjId = activeLockedBook?.subjectId
                if (subjId != null) {
                    BangladeshCurriculumCatalog.lessons.count { it.subjectId == subjId }.coerceAtLeast(4)
                } else {
                    BangladeshCurriculumCatalog.lessons.size.coerceAtLeast(6)
                }
            }
            val activitySlices = remember(completedLessonsCount, completedExamsCount, practiceCount, unlockedCount, language, visualConfig.isDark) {
                val s1 = if (visualConfig.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                val s2 = if (visualConfig.isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                val s3 = if (visualConfig.isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                listOf(
                    ActivityRingSlice(
                        label = StudyQuestStrings.t(language, "Lessons", "পাঠ"),
                        count = completedLessonsCount,
                        color = s1
                    ),
                    ActivityRingSlice(
                        label = StudyQuestStrings.t(language, "Practice", "অনুশীলন"),
                        count = practiceCount,
                        color = s2
                    ),
                    ActivityRingSlice(
                        label = StudyQuestStrings.t(language, "Mock Exams", "পরীক্ষা"),
                        count = completedExamsCount,
                        color = s3
                    ),
                    ActivityRingSlice(
                        label = StudyQuestStrings.t(language, "Badges", "ব্যাজ"),
                        count = unlockedCount,
                        color = EditorialSuccess
                    )
                )
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 980.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactWeeklyStreakStrip(
                    streakCount = profile.streakCount,
                    historyKeys = activeHistoryKeys,
                    freezeCount = profile.streakFreezeCount,
                    language = language,
                    onBuyFreeze = onBuyStreakFreeze,
                    showBuyFreeze = !isGuestMode
                )

                MapThePathCard(
                    avatarId = profile.avatarId,
                    frameId = profile.frameId,
                    displayName = profile.displayName,
                    pathTitle = pathTitle,
                    pathSubtitle = StudyQuestStrings.t(
                        language,
                        "${StudyQuestStrings.classLabel(profile.classId, language)} • Guided curriculum steps",
                        "${StudyQuestStrings.classLabel(profile.classId, language)} • ধারাবাহিক শেখা"
                    ),
                    stepCurrent = completedLessonsCount,
                    stepTotal = totalClassLessons,
                    unlockedBadgesCount = unlockedCount,
                    language = language,
                    onContinuePath = if (activeLockedBook != null) onOpenTextbookReader else onOpenQuickPractice,
                    onOpenBadges = onOpenBadgesAndXp
                )

                // 2x2 Pastel Bento Cards (Reference Photo 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PastelQuestBentoTile(
                        title = StudyQuestStrings.t(language, "Flashcards", "ফ্ল্যাশকার্ড"),
                        valueText = StudyQuestStrings.formatDigits(dueRevisionCount, language),
                        subtitle = StudyQuestStrings.t(language, "due for recall", "রিভিশন বাকি"),
                        icon = Icons.Filled.Style,
                        containerColor = semantic.pastelLavender,
                        contentColor = semantic.onPastelLavender,
                        badgeLabel = StudyQuestStrings.t(language, "Deck", "ডেক"),
                        onClick = onOpenFlashcardsVault,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("today_bento_flashcards")
                    )
                    PastelQuestBentoTile(
                        title = StudyQuestStrings.t(language, "Daily Quiz", "দৈনিক কুইজ"),
                        valueText = if (dailyChallengeDoneToday) "1/1" else "0/1",
                        subtitle = if (dailyChallengeDoneToday) {
                            StudyQuestStrings.t(language, "Completed today", "আজ সম্পন্ন")
                        } else {
                            StudyQuestStrings.t(language, "Ready to play", "খেলতে প্রস্তুত")
                        },
                        icon = Icons.Filled.Quiz,
                        containerColor = semantic.pastelPeach,
                        contentColor = semantic.onPastelPeach,
                        badgeLabel = if (dailyChallengeDoneToday) StudyQuestStrings.t(language, "Done", "সম্পন্ন") else StudyQuestStrings.t(language, "Daily", "দৈনিক"),
                        onClick = onOpenDailyChallenge,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("today_bento_daily_quiz")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PastelQuestBentoTile(
                        title = StudyQuestStrings.t(language, "Focus Goal", "ফোকাস লক্ষ্য"),
                        valueText = "${StudyQuestStrings.formatDigits(todayMinutes, language)}m",
                        subtitle = StudyQuestStrings.t(
                            language,
                            "of ${dailyGoalMinutes}m daily",
                            "দৈনিক ${StudyQuestStrings.formatDigits(dailyGoalMinutes, language)}মি."
                        ),
                        icon = Icons.Filled.Timer,
                        containerColor = semantic.pastelSky,
                        contentColor = semantic.onPastelSky,
                        badgeLabel = "${StudyQuestStrings.formatDigits((goalRatio * 100).toInt(), language)}%",
                        onClick = onOpenPomodoro,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("today_bento_focus")
                    )
                    PastelQuestBentoTile(
                        title = StudyQuestStrings.t(language, "Badges Earned", "অর্জিত ব্যাজ"),
                        valueText = StudyQuestStrings.formatDigits(unlockedCount, language),
                        subtitle = StudyQuestStrings.t(
                            language,
                            "of $totalBadgesCount achievements",
                            "মোট ${StudyQuestStrings.formatDigits(totalBadgesCount, language)}টি"
                        ),
                        icon = Icons.Filled.WorkspacePremium,
                        containerColor = semantic.pastelMint,
                        contentColor = semantic.onPastelMint,
                        badgeLabel = StudyQuestStrings.t(language, "Cabinet", "সংগ্রহ"),
                        onClick = onOpenBadgesAndXp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("today_bento_badges")
                    )
                }

                CharcoalMilestoneCard(
                    eyebrow = StudyQuestStrings.t(language, "NEXT BADGE MILESTONE", "পরবর্তী ব্যাজ অর্জন"),
                    title = StudyQuestStrings.t(language, nextBadgeDef.titleEn, nextBadgeDef.titleBn),
                    progressLabel = StudyQuestStrings.t(
                        language,
                        "$nextBadgeProg / ${nextBadgeDef.target} • ${nextBadgeDef.rarity.labelEn} Achievement",
                        "${StudyQuestStrings.formatDigits(nextBadgeProg, language)} / ${StudyQuestStrings.formatDigits(nextBadgeDef.target, language)} • ${nextBadgeDef.rarity.labelBn}"
                    ),
                    progress = nextBadgeProg.toFloat() / nextBadgeDef.target.coerceAtLeast(1L).toFloat(),
                    unlockedCount = unlockedCount,
                    totalCount = totalBadgesCount,
                    onClick = onOpenBadgesAndXp
                )

                ActivityDonutBreakdownCard(
                    totalMinutes = totalWeekMinutes,
                    slices = activitySlices,
                    language = language,
                    onClick = onOpenStudyProgress
                )
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
                                if (isOnline) "Classrooms & peer study" else "Offline limited • Online rooms",
                                if (isOnline) "লাইভ রুম ও সহপাঠী" else "অফলাইনে সীমিত • অনলাইন রুম"
                            ),
                            badge = if (isOnline) StudyQuestStrings.t(language, "Rooms", "রুম")
                            else StudyQuestStrings.t(language, "Online", "অনলাইন"),
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
                                if (isOnline) "Compare class accuracy" else "Local record only offline",
                                if (isOnline) "শ্রেণিভিত্তিক অবস্থান" else "অফলাইনে লোকাল স্কোর"
                            ),
                            badge = if (isOnline) StudyQuestStrings.t(language, "Board", "বোর্ড")
                            else StudyQuestStrings.t(language, "Online", "অনলাইন"),
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
                                if (isOnline) "Step-by-step NCTB help" else "Offline 6k solver atlas",
                                if (isOnline) "ধাপে ধাপে উত্তর" else "অফলাইন ৬০০০ গাইড"
                            ),
                            badge = if (isOnline) "AI" else StudyQuestStrings.t(language, "Offline", "অফলাইন"),
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
                        Triple(StudyQuestStrings.t(language, "Video Studio", "ভিডিও স্টুডিও"), Icons.Filled.VideoLibrary, "today_ai_video_studio" to onOpenAiVideoStudio),
                        Triple(StudyQuestStrings.t(language, "STEM Lab", "STEM ল্যাব"), Icons.Filled.Science, "today_stem_lab" to onOpenStemLab),
                        Triple(StudyQuestStrings.t(language, "Downloads Hub", "ডাউনলোড হাব"), Icons.Filled.DownloadForOffline, "today_downloads_hub" to onOpenDownloadManager),
                        Triple(StudyQuestStrings.t(language, "Formula Atlas", "সূত্র অ্যাটলাস"), Icons.AutoMirrored.Filled.MenuBook, "today_formula_atlas" to onOpenFormulaAtlas),
                        Triple(StudyQuestStrings.t(language, "Mistakes ($unresolvedMistakesCount)", "ভুলের খাতা ($unresolvedMistakesCount)"), Icons.Filled.Build, "today_mistake_bank" to onOpenMistakeNotebook),
                        Triple(StudyQuestStrings.t(language, "Exam Timer", "পরীক্ষা লক্ষ্য"), Icons.Filled.Event, "today_exam_countdown" to onOpenExamCountdown),
                        Triple(StudyQuestStrings.t(language, "Audio Briefs", "অডিও ব্রিফ"), Icons.Filled.Headphones, "today_audio_briefs" to onOpenPocketAudio)
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

    if (showModeStatusSheet) {
        ModeStatusBottomSheet(
            isOnline = isOnline,
            pendingSyncCount = pendingSyncCount,
            language = language,
            onDismiss = { showModeStatusSheet = false },
            offlineModeEnabled = offlineModeEnabled,
            isNetworkAvailable = isNetworkAvailable,
            onToggleOfflineMode = onToggleOfflineMode,
            onOpenDownloadManager = {
                showModeStatusSheet = false
                onOpenDownloadManager()
            }
        )
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
        shape = RoundedCornerShape(16.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(),
        shadowElevation = 0.dp,
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
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
