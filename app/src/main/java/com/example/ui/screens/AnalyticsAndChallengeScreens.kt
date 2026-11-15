package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.local.LocalBookmarkEntity
import com.example.data.model.PublicProfile
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import com.example.ui.components.EmptyStateView
import com.example.ui.components.QuestionPracticeCard
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.RoseAlert
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.VioletTertiary
import com.example.ui.viewmodel.AssessmentResultSummary

@Composable
fun DailyChallengeScreen(
    profile: PublicProfile,
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    reducedMotion: Boolean,
    dateKey: String,
    alreadyDoneToday: Boolean,
    questions: List<QuestionItem>,
    bookmarks: List<LocalBookmarkEntity>,
    assessmentResult: AssessmentResultSummary?,
    onBack: () -> Unit,
    onToggleBookmark: (QuestionItem) -> Unit,
    onAddRevision: (QuestionItem) -> Unit,
    onReportQuestion: (QuestionItem) -> Unit,
    onAskAi: (QuestionItem) -> Unit,
    onSubmitChallenge: (Map<String, Set<Int>>, Map<String, String>) -> Unit
) {
    BackHandler { onBack() }

    val selectedMap = remember(dateKey) { mutableStateMapOf<String, Set<Int>>() }
    val textMap = remember(dateKey) { mutableStateMapOf<String, String>() }
    var submittedNow by remember { mutableStateOf(false) }

    val showAnswers = alreadyDoneToday || submittedNow || assessmentResult != null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("daily_challenge_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Daily Streak Challenge", "দৈনিক বোর্ড চ্যালেঞ্জ"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            StudyQuestGradientHeroBanner(
                eyebrow = StudyQuestStrings.t(uiLanguage, "DAILY CHALLENGE • $dateKey", "আজকের চ্যালেঞ্জ • $dateKey"),
                title = StudyQuestStrings.t(
                    uiLanguage,
                    "${profile.streakCount}-Day Study Streak",
                    "${StudyQuestStrings.formatDigits(profile.streakCount, uiLanguage)} দিনের স্ট্রিক"
                ),
                subtitle = StudyQuestStrings.t(
                    uiLanguage,
                    if (alreadyDoneToday) "Completed today! Review answers below and come back tomorrow."
                    else "Answer today's 5 board-style questions to extend your streak & earn bonus XP.",
                    if (alreadyDoneToday) "আজকের চ্যালেঞ্জ সম্পন্ন হয়েছে! নিচে উত্তরগুলো দেখে নিতে পারো।"
                    else "স্ট্রিক বাড়াতে ও বোনাস XP পেতে আজকের ৫টি প্রশ্নের উত্তর দাও।"
                ),
                reducedMotion = reducedMotion,
                trailingSlot = {
                    StatBadgePill(
                        icon = Icons.Filled.LocalFireDepartment,
                        label = "${StudyQuestStrings.formatDigits(profile.streakCount, uiLanguage)}d",
                        tint = CobaltPrimary,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            )
        }

        if (assessmentResult != null) {
            item {
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = TealSecondary, modifier = Modifier.size(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Score: ${assessmentResult.correctCount}/${questions.size} • +${assessmentResult.xpEarned} XP",
                                    "স্কোর: ${StudyQuestStrings.formatDigits(assessmentResult.correctCount, uiLanguage)}/${StudyQuestStrings.formatDigits(questions.size, uiLanguage)} • +${StudyQuestStrings.formatDigits(assessmentResult.xpEarned, uiLanguage)} XP"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Your streak and leaderboard have been updated!", "তোমার স্ট্রিক ও লিডারবোর্ড আপডেট হয়েছে!"),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        if (questions.isEmpty()) {
            item {
                EmptyStateView(
                    titleEn = "No questions available",
                    titleBn = "কোনো প্রশ্ন পাওয়া যায়নি",
                    subtitleEn = "Please check back after syncing curriculum content.",
                    subtitleBn = "পাঠ্যক্রম সিঙ্ক হওয়ার পর আবার চেষ্টা করো।",
                    language = uiLanguage
                )
            }
        } else {
            items(questions, key = { it.id }) { q ->
                QuestionPracticeCard(
                    question = q,
                    questionLanguage = questionLanguage,
                    uiLanguage = uiLanguage,
                    selectedIndices = selectedMap[q.id] ?: emptySet(),
                    textAnswer = textMap[q.id] ?: "",
                    cqSelfScore = null,
                    isBookmarked = bookmarks.any { it.questionId == q.id },
                    showFeedback = showAnswers,
                    onToggleOption = { idx ->
                        if (!showAnswers) {
                            val current = selectedMap[q.id] ?: emptySet()
                            selectedMap[q.id] = if (q.type == QuestionType.MULTI_MCQ) {
                                if (idx in current) current - idx else current + idx
                            } else setOf(idx)
                        }
                    },
                    onTextAnswerChange = { if (!showAnswers) textMap[q.id] = it },
                    onCqSelfScoreChange = {},
                    onToggleBookmark = { onToggleBookmark(q) },
                    onAddRevision = { onAddRevision(q) },
                    onReportQuestion = { onReportQuestion(q) },
                    onAskAi = { onAskAi(q) }
                )
            }

            if (!alreadyDoneToday && !submittedNow) {
                item {
                    Button(
                        onClick = {
                            submittedNow = true
                            onSubmitChallenge(selectedMap.toMap(), textMap.toMap())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_daily_challenge_btn")
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Submit & Extend Streak", "সাবমিট করো ও স্ট্রিক বাড়াও"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MasteryAnalyticsScreen(
    profile: PublicProfile,
    uiLanguage: AppLanguage,
    reducedMotion: Boolean,
    subjectAnalytics: List<SubjectAnalyticsSummary>,
    weakTopics: List<WeakTopicStat>,
    onBack: () -> Unit,
    onPracticeWeakTopic: (WeakTopicStat) -> Unit
) {
    BackHandler { onBack() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("analytics_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Mastery & Weak-Topic Analytics", "দক্ষতা ও দুর্বল অধ্যায় বিশ্লেষণ"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            StudyQuestGradientHeroBanner(
                eyebrow = StudyQuestStrings.t(uiLanguage, "PROGRESS OVERVIEW", "অগ্রগতি বিশ্লেষণ"),
                title = "${StudyQuestStrings.formatDigits(profile.totalXp, uiLanguage)} XP • Lv.${StudyQuestStrings.formatDigits(profile.overallLevel, uiLanguage)}",
                subtitle = StudyQuestStrings.t(
                    uiLanguage,
                    "${subjectAnalytics.size} active subjects • ${profile.streakCount}-day streak",
                    "${StudyQuestStrings.formatDigits(subjectAnalytics.size, uiLanguage)}টি সক্রিয় বিষয় • ${StudyQuestStrings.formatDigits(profile.streakCount, uiLanguage)} দিনের স্ট্রিক"
                ),
                reducedMotion = reducedMotion,
                trailingSlot = {
                    StatBadgePill(
                        icon = Icons.Filled.Star,
                        label = profile.rankTier,
                        tint = VioletTertiary,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }
            )
        }

        item {
            Text(
                text = StudyQuestStrings.t(uiLanguage, "Weak Topics to Practice", "অনুশীলন প্রয়োজন এমন বিষয়"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (weakTopics.isEmpty()) {
            item {
                StudyQuestCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.Analytics, contentDescription = null, tint = CobaltPrimary)
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "No weak topics flagged yet! Topics appear here when 2+ attempts average under 65%.",
                                "এখনো কোনো দুর্বল অধ্যায় নেই! কোনো অধ্যায়ে ২+ চেষ্টায় ৬৫%-এর নিচে স্কোর হলে এখানে দেখাবে।"
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else {
            items(weakTopics, key = { "${it.subjectId}_${it.topicId}" }) { wt ->
                val pct = (wt.averageAccuracy * 100).toInt()
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = RoseAlert)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, wt.topicLabelEn, wt.topicLabelBn),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    uiLanguage,
                                    "$pct% accuracy over ${wt.attempts} attempts",
                                    "${StudyQuestStrings.formatDigits(wt.attempts, uiLanguage)}টি চেষ্টায় ${StudyQuestStrings.formatDigits(pct, uiLanguage)}% সঠিকতা"
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        FilledTonalButton(
                            onClick = { onPracticeWeakTopic(wt) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("drill_weak_${wt.topicId}")
                        ) {
                            Text(StudyQuestStrings.t(uiLanguage, "Drill", "অনুশীলন"))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = StudyQuestStrings.t(uiLanguage, "Subject Breakdown", "বিষয়ভিত্তিক দক্ষতা"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (subjectAnalytics.isEmpty()) {
            item {
                EmptyStateView(
                    titleEn = "No activity recorded yet",
                    titleBn = "এখনো কোনো অনুশীলন রেকর্ড নেই",
                    subtitleEn = "Complete a lesson, flashcard deck, Pomodoro session, or quiz to populate your subject mastery.",
                    subtitleBn = "পাঠ, ফ্ল্যাশকার্ড, পোমোডোরো বা কুইজ সম্পন্ন করলে এখানে বিষয়ভিত্তিক অগ্রগতি দেখাবে।",
                    language = uiLanguage
                )
            }
        } else {
            items(subjectAnalytics, key = { it.subjectId }) { item ->
                val subj = BangladeshCurriculumCatalog.subjects.find { it.id == item.subjectId }
                StudyQuestCard(modifier = Modifier.testTag("analytics_subj_${item.subjectId}")) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, subj?.titleEn ?: item.subjectId, subj?.titleBn ?: item.subjectId),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lv.${item.subjectLevel} • ${item.totalXp} XP",
                            color = CobaltPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.averageAccuracy.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = StudyQuestStrings.t(
                            uiLanguage,
                            "Lessons: ${item.lessonsCompleted} • Practice/Focus: ${item.practiceSessions} • Exams: ${item.examSessions} • Accuracy: ${(item.averageAccuracy * 100).toInt()}%",
                            "পাঠ: ${StudyQuestStrings.formatDigits(item.lessonsCompleted, uiLanguage)} • অনুশীলন: ${StudyQuestStrings.formatDigits(item.practiceSessions, uiLanguage)} • পরীক্ষা: ${StudyQuestStrings.formatDigits(item.examSessions, uiLanguage)} • সঠিকতা: ${StudyQuestStrings.formatDigits((item.averageAccuracy * 100).toInt(), uiLanguage)}%"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
