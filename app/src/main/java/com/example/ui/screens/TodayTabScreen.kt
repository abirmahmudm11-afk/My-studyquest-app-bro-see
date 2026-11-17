package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.local.LocalRevisionEntity
import com.example.data.model.AcademicSubject
import com.example.data.model.LearningRecordDoc
import com.example.data.model.MascotMood
import com.example.data.model.PublicProfile
import com.example.data.model.StudyPlanDoc
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WorkspacePremium
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.AnimatedCounterText
import com.example.ui.components.CompactWeeklyStreakStrip
import com.example.ui.components.GuestUpgradeBannerCard
import com.example.ui.components.MascotTituCard
import com.example.ui.components.QuestCard
import com.example.ui.components.RankTierBadge
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.components.StudyTimeAndXpBarChart
import com.example.ui.components.SubjectCard
import com.example.ui.components.SyncStatusPill
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.UserSubscriptionDoc
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BerryViolet
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.VividEmerald
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    onRequestGoogleSignIn: () -> Unit = {}
) {
    var showNotificationsSheet by remember { mutableStateOf(false) }
    val semantic = LocalStudyQuestColors.current

    val dueRevisionCount = remember(revisions) {
        val now = System.currentTimeMillis()
        revisions.count { it.nextDueAtMs <= now }
    }
    val (xpWithinLevel, xpRequiredForNext) = remember(profile.totalXp) {
        StudyQuestStrings.xpIntoCurrentLevel(profile.totalXp)
    }
    val dailyGoalProgress = (recentRecords.take(3).sumOf { it.xpAwarded.toInt() }).coerceAtMost(60)
    val categorySubjects = remember(profile.classId, profile.selectedSubjects) {
        val allForClass = BangladeshCurriculumCatalog.subjectsForClass(profile.classId)
        val filtered = allForClass.filter { it.id in profile.selectedSubjects }
        filtered.ifEmpty { allForClass }
    }
    val spotlightLesson = remember(profile.classId, profile.selectedSubjects) {
        val subjs = profile.selectedSubjects.ifEmpty {
            BangladeshCurriculumCatalog.subjectsForClass(profile.classId).map { it.id }
        }
        BangladeshCurriculumCatalog.lessons.firstOrNull { it.subjectId in subjs }
            ?: BangladeshCurriculumCatalog.lessons.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("today_home_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
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
                    SyncStatusPill(
                        isOnline = isOnline,
                        pendingSyncCount = pendingSyncCount,
                        language = language
                    )
                    RankTierBadge(tier = profile.rankTier, language = language)
                }
                Row {
                    IconButton(
                        onClick = onOpenMessages,
                        modifier = Modifier.testTag("today_open_messages")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = StudyQuestStrings.t(language, "Messages", "মেসেজ")
                        )
                    }
                    IconButton(
                        onClick = { showNotificationsSheet = true },
                        modifier = Modifier.testTag("today_open_notifications")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge { Text("$unreadNotificationCount") }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = StudyQuestStrings.t(language, "Notifications", "নোটিফিকেশন")
                            )
                        }
                    }
                }
            }
        }

        if (isGuestMode) {
            item {
                StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                    GuestUpgradeBannerCard(
                        language = language,
                        totalXp = profile.totalXp,
                        coins = profile.coins,
                        onSignInClick = onRequestGoogleSignIn
                    )
                }
            }
        }

        // Rich Hero Dashboard Card with Animated Mastery Ring & Counters
        item {
            StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                val cls = BangladeshCurriculumCatalog.classes.find { it.id == profile.classId }
                StudyQuestGradientHeroBanner(
                    eyebrow = StudyQuestStrings.t(
                        language,
                        "LEVEL ${profile.overallLevel} • ${StudyQuestStrings.levelTitle(profile.overallLevel.toInt(), language).uppercase()}",
                        "লেভেল ${StudyQuestStrings.formatDigits(profile.overallLevel, language)} • ${StudyQuestStrings.levelTitle(profile.overallLevel.toInt(), language)}"
                    ),
                    title = StudyQuestStrings.t(language, "Hello, ${profile.displayName}", "স্বাগতম, ${profile.displayName}"),
                    subtitle = StudyQuestStrings.t(
                        language,
                        "${cls?.labelEn ?: profile.classId} • ${profile.studyQuestId}",
                        "${cls?.labelBn ?: profile.classId} • ${profile.studyQuestId}"
                    ),
                    reducedMotion = reducedMotion,
                    backgroundResId = R.drawable.img_hero_study,
                    trailingSlot = {
                        AnimatedCircularMasteryRing(
                            progress = dailyGoalProgress / 60f,
                            centerValueText = "${StudyQuestStrings.formatDigits(dailyGoalProgress, language)}/60",
                            centerCaptionText = StudyQuestStrings.t(language, "Goal XP", "দৈনিক"),
                            reducedMotion = reducedMotion
                        )
                    }
                ) {
                    LinearProgressIndicator(
                        progress = { (xpWithinLevel.toFloat() / xpRequiredForNext.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.24f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "$xpWithinLevel / $xpRequiredForNext XP to Lv.${profile.overallLevel + 1}",
                            "পরবর্তী লেভেলের জন্য ${StudyQuestStrings.formatDigits(xpWithinLevel, language)} / ${StudyQuestStrings.formatDigits(xpRequiredForNext, language)} XP"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                AnimatedCounterText(
                                    targetValue = profile.totalXp.toInt(),
                                    language = language,
                                    suffix = " XP",
                                    reducedMotion = reducedMotion,
                                    color = Color.White
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                AnimatedCounterText(
                                    targetValue = profile.coins.toInt(),
                                    language = language,
                                    suffix = " ${StudyQuestStrings.t(language, "Coins", "কয়েন")}",
                                    reducedMotion = reducedMotion,
                                    color = Color.White
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "${profile.streakCount}d Streak",
                                        "${StudyQuestStrings.formatDigits(profile.streakCount, language)} দিন স্ট্রিক"
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Annual Class Holdback or Jan 1 Promotion Banner
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
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = RoseAlert)
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
                            Text(StudyQuestStrings.t(language, "Retake Exam", "পুনঃপরীক্ষা"))
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

        // Duolingo-style 7-Day Daily Streak Strip + Streak Freeze
        item {
            StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                CompactWeeklyStreakStrip(
                    streakCount = profile.streakCount,
                    historyKeys = profile.streakHistoryKeys,
                    freezeCount = profile.streakFreezeCount,
                    language = language,
                    onBuyFreeze = if (!isGuestMode) onBuyStreakFreeze else null,
                    showBuyFreeze = !isGuestMode
                )
            }
        }

        // 4 Vivid Playful Quest Feature Hero Cards (Study Progress, Study Groups, Leaderboard, AI Doubt Solver)
        item {
            val completedLessonsCount = remember(recentRecords) { recentRecords.count { it.recordType == "lesson" } }
            val completedExamsCount = remember(recentRecords) { recentRecords.count { it.recordType in listOf("exam", "promotion_exam") } }
            StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = StudyQuestStrings.t(language, "Quest Destinations", "কুইক ডেস্টিনেশন"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TodayFeatureHeroTile(
                            title = StudyQuestStrings.t(language, "Study Progress", "অধ্যয়ন অগ্রগতি"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "${completedLessonsCount} lessons • ${completedExamsCount} exams",
                                "${StudyQuestStrings.formatDigits(completedLessonsCount, language)} পাঠ • ${StudyQuestStrings.formatDigits(completedExamsCount, language)} পরীক্ষা"
                            ),
                            badge = StudyQuestStrings.t(language, "Charts", "চার্ট"),
                            icon = Icons.Filled.TrendingUp,
                            accentColor = ElectricIndigo,
                            onClick = onOpenStudyProgress,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_hero_study_progress")
                        )
                        TodayFeatureHeroTile(
                            title = StudyQuestStrings.t(language, "Study Groups", "স্টাডি গ্রুপ"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "Live rooms & problem board",
                                "লাইভ রুম ও প্রশ্ন বোর্ড"
                            ),
                            badge = StudyQuestStrings.t(language, "Live", "লাইভ"),
                            icon = Icons.Filled.Groups,
                            accentColor = VividEmerald,
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
                            accentColor = AmberGold,
                            onClick = onOpenLeaderboard,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_hero_leaderboard")
                        )
                        TodayFeatureHeroTile(
                            title = StudyQuestStrings.t(language, "AI Doubt Solver", "এআই সমাধান"),
                            subtitle = StudyQuestStrings.t(
                                language,
                                "Instant Gemini NCTB answers",
                                "তাৎক্ষণিক জেমিনি উত্তর"
                            ),
                            badge = "Gemini",
                            icon = Icons.Filled.AutoAwesome,
                            accentColor = BerryViolet,
                            onClick = onOpenAiDoubtSolver,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("today_hero_ai_solver")
                        )
                    }
                }
            }
        }

        item {
            StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                val anyClaimable = quests.any { it.completed && !it.claimed }
                MascotTituCard(
                    mood = if (anyClaimable) MascotMood.QUEST_COMPLETION else MascotMood.WELCOME,
                    messageEn = if (anyClaimable) {
                        "You finished a quest! Tap Claim below to collect your Coins and XP."
                    } else {
                        "Ready for today's study sprint? Even 15 focused minutes sharpen your SSC readiness."
                    },
                    messageBn = if (anyClaimable) {
                        "তুমি একটি কোয়েস্ট সম্পন্ন করেছ! নিচে থেকে তোমার কয়েন ও XP সংগ্রহ করো।"
                    } else {
                        "আজকের পড়ার স্প্রিন্টের জন্য প্রস্তুত? প্রতিদিন ১৫ মিনিটের মনোযোগী অনুশীলন বড় পরিবর্তন আনে!"
                    },
                    language = language,
                    reducedMotion = reducedMotion,
                    showCelebrationLottie = anyClaimable
                )
            }
        }

        // Quick-Access Practice Modules
        item {
            StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                Column {
                    Text(
                        text = StudyQuestStrings.t(language, "Practice Modules", "অনুশীলন মডিউলসমূহ"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onOpenQuickPractice,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_quick_practice")
                        ) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Quick Practice", "কুইক প্র্যাকটিস"))
                        }
                        FilledTonalButton(
                            onClick = onOpenDailyChallenge,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_daily_challenge")
                        ) {
                            Icon(
                                if (dailyChallengeDoneToday) Icons.Filled.CheckCircle else Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = if (dailyChallengeDoneToday) semantic.success else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (dailyChallengeDoneToday) {
                                    StudyQuestStrings.t(language, "Daily Done ✓", "দৈনিক সম্পন্ন ✓")
                                } else {
                                    StudyQuestStrings.t(language, "Daily Streak", "দৈনিক চ্যালেঞ্জ")
                                }
                            )
                        }
                        FilledTonalButton(
                            onClick = onOpenFlashcardsVault,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_flashcards_vault")
                        ) {
                            Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Flashcards & Formulas", "ফ্ল্যাশকার্ড ও সূত্র"))
                        }
                        FilledTonalButton(
                            onClick = onOpenPomodoro,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_pomodoro")
                        ) {
                            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Pomodoro Focus", "পমোডোরো ফোকাস"))
                        }
                        FilledTonalButton(
                            onClick = onOpenMasteryAnalytics,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_mastery_analytics")
                        ) {
                            Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Mastery Analytics", "দক্ষতা বিশ্লেষণ"))
                        }
                        FilledTonalButton(
                            onClick = onOpenMockExam,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_mock_exam")
                        ) {
                            Icon(Icons.Filled.HistoryEdu, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Mock Exam", "মডেল টেস্ট"))
                        }
                        FilledTonalButton(
                            onClick = onOpenRevision,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_revision")
                        ) {
                            Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                StudyQuestStrings.t(
                                    language,
                                    "Revision ($dueRevisionCount)",
                                    "রিভিশন (${StudyQuestStrings.formatDigits(dueRevisionCount, language)})"
                                )
                            )
                        }
                        FilledTonalButton(
                            onClick = onOpenAiLab,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_ai_lab")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "StudyQuest AI Lab", "StudyQuest AI ল্যাব"))
                        }
                        FilledTonalButton(
                            onClick = onOpenAiDoubtSolver,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_ai_solver")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Quick Doubt", "তাৎক্ষণিক সমাধান"))
                        }
                        FilledTonalButton(
                            onClick = onOpenAssignmentsAndGoals,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_assignments_goals")
                        ) {
                            Icon(Icons.Filled.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Goals & Tasks", "লক্ষ্য ও কাজ"))
                        }
                        FilledTonalButton(
                            onClick = onOpenAiVideoStudio,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_ai_video_studio")
                        ) {
                            Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Video Library", "ভিডিও লাইব্রেরি"))
                        }
                        FilledTonalButton(
                            onClick = onOpenBadgesAndXp,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_badges_xp")
                        ) {
                            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "Badges & XP", "ব্যাজ ও XP"))
                        }
                    }
                }
            }
        }

        if (!isGuestMode) {
            item {
                val activePlan = remember(subscription.tier) { QuestPlanCatalog.planById(subscription.tier) }
                val aiLimit = subscription.monthlyAiLimit.toInt().coerceAtLeast(activePlan.monthlyAiCredits)
                val aiLeft = (aiLimit - subscription.aiCreditsUsed.toInt()).coerceAtLeast(0)
                val vidLimit = subscription.monthlyVideoLimit.toInt().coerceAtLeast(activePlan.monthlyVideoCredits)
                val vidLeft = ((vidLimit - subscription.videoCreditsUsed.toInt()).coerceAtLeast(0) + subscription.bonusVideoCredits.toInt().coerceAtLeast(0))
                StudyQuestCard(
                    onClick = onOpenPlanStore,
                    modifier = Modifier.testTag("today_plan_strip")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(language, "${activePlan.titleEn} • StudyQuest Helper", "${activePlan.titleBn} • StudyQuest Helper"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "$aiLeft AI credits left • $vidLeft video credits left • Puter.js ready",
                                    "${aiLeft}টি AI ক্রেডিট • ${vidLeft}টি ভিডিও ক্রেডিট বাকি"
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

        // Native Compose Canvas 7-Day Study & XP Progress Chart
        item {
            StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                StudyTimeAndXpBarChart(
                    dailySeries = dailyStudySeries,
                    language = language
                )
            }
        }

        // Study Categories Section
        if (categorySubjects.isNotEmpty()) {
            item {
                Text(
                    text = StudyQuestStrings.t(language, "Study Categories", "অধ্যয়নের বিষয়সমূহ"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(categorySubjects, key = { "cat_${it.id}" }) { subject ->
                val chCount = BangladeshCurriculumCatalog.chapters.count { it.subjectId == subject.id }
                val summary = subjectAnalytics.find { it.subjectId == subject.id }
                val mastery = summary?.averageAccuracy?.toFloat()?.coerceIn(0f, 1f) ?: 0f
                SubjectCard(
                    subject = subject,
                    language = language,
                    chapterCount = chCount,
                    masteryProgress = mastery,
                    onClick = { onOpenSubjectCategory(subject) },
                    modifier = Modifier.testTag("today_category_${subject.id}")
                )
            }
        }

        // Curated Daily Study Spotlight so Home never feels bare on Day 1
        if (spotlightLesson != null) {
            item {
                StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                    StudyQuestCard(
                        containerColor = MaterialTheme.colorScheme.surface,
                        onClick = onOpenQuickPractice
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(language, "Today's NCTB Spotlight", "আজকের এনসিটিবি পাঠ"),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = StudyQuestStrings.t(language, spotlightLesson.titleEn, spotlightLesson.titleBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "${spotlightLesson.estimatedMinutes} min study + instant MCQ drill",
                                        "${StudyQuestStrings.formatDigits(spotlightLesson.estimatedMinutes, language)} মিনিট পাঠ ও তাৎক্ষণিক অনুশীলন"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatBadgePill(
                                icon = Icons.Filled.Bolt,
                                label = "+20 XP",
                                tint = semantic.success,
                                containerColor = semantic.successContainer
                            )
                        }
                    }
                }
            }
        }

        // Weak topic nudge (only shown when >= 2 attempts show < 65% accuracy)
        if (weakTopics.isNotEmpty()) {
            item {
                val topWeak = weakTopics.first()
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.surface) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = RoseAlert)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(language, "Weak Topic Focus", "দুর্বল বিষয়ে মনোযোগ"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val pct = (topWeak.averageAccuracy * 100).toInt()
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "${topWeak.topicLabelEn}: $pct% across ${topWeak.attempts} attempts",
                                    "${topWeak.topicLabelBn}: ${StudyQuestStrings.formatDigits(pct, language)}% (${StudyQuestStrings.formatDigits(topWeak.attempts, language)}টি চেষ্টায়)"
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        FilledTonalButton(onClick = onOpenQuickPractice) {
                            Text(StudyQuestStrings.t(language, "Practice", "অনুশীলন"))
                        }
                    }
                }
            }
        }

        // Daily & Weekly Quests
        item {
            Text(
                text = StudyQuestStrings.t(language, "Daily & Weekly Quests", "দৈনিক ও সাপ্তাহিক কোয়েস্ট"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(quests, key = { it.id }) { qDoc ->
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

        // Exam Countdowns & Study Plans
        if (studyPlans.isNotEmpty()) {
            item {
                Text(
                    text = StudyQuestStrings.t(language, "Upcoming Study Goals", "আসন্ন পড়ার লক্ষ্য"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(studyPlans.take(3), key = { it.id }) { plan ->
                val daysLeft = (((plan.targetDateEpochMs - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0L)).toInt()
                StudyQuestCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plan.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "$daysLeft days remaining • ${plan.minutesPerDay} min/day",
                                    "${StudyQuestStrings.formatDigits(daysLeft, language)} দিন বাকি • দৈনিক ${StudyQuestStrings.formatDigits(plan.minutesPerDay, language)} মিনিট"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Recent Activity
        if (recentRecords.isNotEmpty()) {
            item {
                Text(
                    text = StudyQuestStrings.t(language, "Recent Activity", "সাম্প্রতিক অনুশীলন"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(recentRecords.take(3), key = { it.id }) { rec ->
                StudyQuestCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rec.summary ?: rec.refId, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${rec.recordType.uppercase()} • +${StudyQuestStrings.formatDigits(rec.xpAwarded, language)} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
}

@Composable
private fun TodayFeatureHeroTile(
    title: String,
    subtitle: String,
    badge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(accentColor.copy(alpha = 0.55f)),
        shadowElevation = 2.dp,
        modifier = modifier
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
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor.copy(alpha = 0.16f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accentColor.copy(alpha = 0.15f)
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
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
