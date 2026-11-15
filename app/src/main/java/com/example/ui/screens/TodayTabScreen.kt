package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
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
import com.example.data.model.LearningRecordDoc
import com.example.data.model.MascotMood
import com.example.data.model.PublicProfile
import com.example.data.model.StudyPlanDoc
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import com.example.data.repository.WeakTopicStat
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.AnimatedCounterText
import com.example.ui.components.GuestUpgradeBannerCard
import com.example.ui.components.MascotTituCard
import com.example.ui.components.QuestCard
import com.example.ui.components.RankTierBadge
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MangoYellow
import com.example.ui.theme.WarmOrange

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
    isGuestMode: Boolean = false,
    onRequestGoogleSignIn: () -> Unit = {}
) {
    var showNotificationsSheet by remember { mutableStateOf(false) }

    val dueRevisionCount = remember(revisions) {
        val now = System.currentTimeMillis()
        revisions.count { it.nextDueAtMs <= now }
    }
    val xpWithinLevel = (profile.totalXp % 100L).toInt()
    val dailyGoalProgress = (recentRecords.take(3).sumOf { it.xpAwarded.toInt() }).coerceAtMost(60)
    val spotlightLesson = remember(profile.classId, profile.selectedSubjects) {
        val subjs = profile.selectedSubjects.ifEmpty {
            BangladeshCurriculumCatalog.subjectsForClass(profile.classId).map { it.id }
        }
        BangladeshCurriculumCatalog.lessons.firstOrNull { it.subjectId in subjs }
            ?: BangladeshCurriculumCatalog.lessons.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                        progress = { xpWithinLevel / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = MangoYellow,
                        trackColor = Color.White.copy(alpha = 0.24f)
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
                                Icon(Icons.Filled.Star, contentDescription = null, tint = MangoYellow, modifier = Modifier.size(16.dp))
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
                                Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = MangoYellow, modifier = Modifier.size(16.dp))
                                AnimatedCounterText(
                                    targetValue = profile.coins.toInt(),
                                    language = language,
                                    suffix = " ${StudyQuestStrings.t(language, "Coins", "কয়েন")}",
                                    reducedMotion = reducedMotion,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
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

        // Quick Shortcuts Row
        item {
            StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                Column {
                    Text(
                        text = StudyQuestStrings.t(language, "Quick Actions", "দ্রুত শুরু"),
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
                            onClick = onOpenMockExam,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_mock_exam")
                        ) {
                            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
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
                            onClick = onOpenAiDoubtSolver,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("today_ai_solver")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(language, "AI Doubt Solver", "AI সমাধান"))
                        }
                    }
                }
            }
        }

        // Curated Daily Study Spotlight so Home never feels bare on Day 1
        if (spotlightLesson != null) {
            item {
                StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                    StudyQuestCard(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                        onClick = onOpenQuickPractice
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = ForestGreen.copy(alpha = 0.14f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(language, "Today's NCTB Spotlight", "আজকের এনসিটিবি পাঠ"),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
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
                                tint = WarmOrange,
                                containerColor = MaterialTheme.colorScheme.surface
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
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = WarmOrange)
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
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = ForestGreen)
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
                        Icon(Icons.Filled.HistoryEdu, contentDescription = null, tint = ForestGreen)
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
