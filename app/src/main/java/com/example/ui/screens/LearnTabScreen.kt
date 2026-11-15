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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.local.CachedLessonEntity
import com.example.data.local.LocalBookmarkEntity
import com.example.data.local.LocalRevisionEntity
import com.example.data.model.AcademicChapter
import com.example.data.model.AcademicSubject
import com.example.data.model.ExamTemplate
import com.example.data.model.PublicProfile
import com.example.data.model.QuestionItem
import com.example.data.model.StudyLesson
import com.example.data.model.StudyPlanDoc
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.R
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.components.SubjectCard
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.viewmodel.AiDoubtUiState
import com.example.ui.viewmodel.AssessmentResultSummary

sealed interface LearnDestination {
    data object Home : LearnDestination
    data class SubjectDetail(val subject: AcademicSubject) : LearnDestination
    data class LessonDetail(val lesson: StudyLesson) : LearnDestination
    data object QuestionBank : LearnDestination
    data class PracticeSession(val title: String, val questions: List<QuestionItem>) : LearnDestination
    data class MockExamSession(val template: ExamTemplate, val questions: List<QuestionItem>) : LearnDestination
    data object RevisionAndPlanner : LearnDestination
    data object FlashcardsAndVault : LearnDestination
    data object PomodoroFocus : LearnDestination
    data object DailyChallenge : LearnDestination
    data object MasteryAnalytics : LearnDestination
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearnTabScreen(
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    initialClassId: String,
    isOnline: Boolean,
    allQuestions: List<QuestionItem>,
    cachedLessons: List<CachedLessonEntity>,
    bookmarks: List<LocalBookmarkEntity>,
    revisions: List<LocalRevisionEntity>,
    studyPlans: List<StudyPlanDoc>,
    examRemainingSeconds: Int,
    lastAssessmentResult: AssessmentResultSummary?,
    aiState: AiDoubtUiState,
    externalDestination: LearnDestination?,
    onConsumeExternalDestination: () -> Unit,
    onDownloadChapter: (String) -> Unit,
    onCompleteLesson: (StudyLesson) -> Unit,
    onToggleBookmark: (QuestionItem) -> Unit,
    onAddRevision: (QuestionItem) -> Unit,
    onReviewRevision: (String, Int) -> Unit,
    onCreateStudyPlan: (String, String, String, Int, Long) -> Unit,
    onToggleStudyPlan: (StudyPlanDoc) -> Unit,
    onDeleteStudyPlan: (String) -> Unit,
    onStartExamTimer: (Int, () -> Unit) -> Unit,
    onSubmitAssessment: (
        title: String,
        questions: List<QuestionItem>,
        selected: Map<String, Set<Int>>,
        textAnswers: Map<String, String>,
        cqSelfScores: Map<String, Double>,
        template: ExamTemplate?
    ) -> Unit,
    onClearAssessmentResult: () -> Unit,
    onReportQuestion: (QuestionItem) -> Unit,
    onAskAi: (questionPrompt: String, studentQuestion: String, classId: String, subjectName: String) -> Unit,
    onResetAi: () -> Unit,
    reducedMotion: Boolean = false,
    profile: PublicProfile = PublicProfile(),
    subjectAnalytics: List<SubjectAnalyticsSummary> = emptyList(),
    weakTopics: List<WeakTopicStat> = emptyList(),
    dailyChallengeQuestions: List<QuestionItem> = emptyList(),
    dailyChallengeDoneToday: Boolean = false,
    dailyChallengeDateKey: String = "",
    onCompletePomodoro: (subjectId: String, focusedMinutes: Int) -> Unit = { _, _ -> },
    onCompleteFlashcardDeck: (subjectId: String, reviewedCount: Int, masteredCount: Int) -> Unit = { _, _, _ -> },
    onSubmitDailyChallenge: (List<QuestionItem>, Map<String, Set<Int>>, Map<String, String>) -> Unit = { _, _, _ -> }
) {
    var destination by remember { mutableStateOf<LearnDestination>(LearnDestination.Home) }
    var selectedClassId by remember(initialClassId) { mutableStateOf(initialClassId) }

    LaunchedEffect(externalDestination) {
        if (externalDestination != null) {
            destination = externalDestination
            onConsumeExternalDestination()
        }
    }

    var aiSheetContext by remember { mutableStateOf<Pair<String, String>?>(null) } // prompt to subjectName

    if (destination != LearnDestination.Home) {
        BackHandler {
            destination = LearnDestination.Home
        }
    }

    when (val dest = destination) {
        LearnDestination.Home -> {
            val subjects = remember(selectedClassId) {
                BangladeshCurriculumCatalog.subjectsForClass(selectedClassId)
            }
            val classLessonIds = remember(selectedClassId) {
                val subjIds = subjects.map { it.id }.toSet()
                BangladeshCurriculumCatalog.lessons.filter { it.subjectId in subjIds }.map { it.id }.toSet()
            }
            val completedLessonCount = remember(cachedLessons, classLessonIds) {
                cachedLessons.count { it.id in classLessonIds && it.completed }
            }
            val classQuestionsCount = remember(allQuestions, selectedClassId) {
                allQuestions.count { it.classId == selectedClassId }.coerceAtLeast(allQuestions.size)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                        val cls = BangladeshCurriculumCatalog.classes.find { it.id == selectedClassId }
                        StudyQuestGradientHeroBanner(
                            eyebrow = StudyQuestStrings.t(uiLanguage, "NCTB CURRICULUM HUB", "এনসিটিবি পাঠ্যক্রম"),
                            title = StudyQuestStrings.t(uiLanguage, "Academic Learning Hub", "অ্যাকাডেমিক লার্নিং হাব"),
                            subtitle = StudyQuestStrings.t(
                                uiLanguage,
                                "${cls?.labelEn ?: selectedClassId} • $classQuestionsCount questions • ${cachedLessons.size} offline cached",
                                "${cls?.labelBn ?: selectedClassId} • ${StudyQuestStrings.formatDigits(classQuestionsCount, uiLanguage)} প্রশ্ন • ${StudyQuestStrings.formatDigits(cachedLessons.size, uiLanguage)}টি অফলাইন"
                            ),
                            reducedMotion = reducedMotion,
                            backgroundResId = R.drawable.img_hero_study,
                            trailingSlot = {
                                val denom = classLessonIds.size.coerceAtLeast(1)
                                AnimatedCircularMasteryRing(
                                    progress = completedLessonCount.toFloat() / denom.toFloat(),
                                    centerValueText = "${StudyQuestStrings.formatDigits(completedLessonCount, uiLanguage)}/${StudyQuestStrings.formatDigits(denom, uiLanguage)}",
                                    centerCaptionText = StudyQuestStrings.t(uiLanguage, "Lessons", "পাঠ"),
                                    reducedMotion = reducedMotion,
                                    size = 72.dp
                                )
                            }
                        )
                    }
                }

                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BangladeshCurriculumCatalog.classes.forEach { cls ->
                            FilterChip(
                                selected = selectedClassId == cls.id,
                                onClick = { if (cls.futureReadyTag == null) selectedClassId = cls.id },
                                enabled = cls.futureReadyTag == null,
                                label = { Text(StudyQuestStrings.t(uiLanguage, cls.labelEn, cls.labelBn)) },
                                modifier = Modifier.testTag("learn_class_${cls.id}")
                            )
                        }
                    }
                }

                // Core tools strip
                item {
                    StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { destination = LearnDestination.QuestionBank },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_question_bank")
                            ) {
                                Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Question Bank", "প্রশ্নব্যাংক"))
                            }
                            FilledTonalButton(
                                onClick = {
                                    val qs = allQuestions.filter { it.classId == selectedClassId }.ifEmpty { allQuestions }.take(6)
                                    destination = LearnDestination.PracticeSession(
                                        StudyQuestStrings.t(uiLanguage, "Quick Practice", "কুইক প্র্যাকটিস"),
                                        qs
                                    )
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("learn_quick_practice")
                            ) {
                                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Quick Practice", "দ্রুত অনুশীলন"))
                            }
                            FilledTonalButton(
                                onClick = { destination = LearnDestination.RevisionAndPlanner },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_revision_planner")
                            ) {
                                Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Revision & Planner", "রিভিশন ও প্ল্যানার"))
                            }
                            FilledTonalButton(
                                onClick = { destination = LearnDestination.FlashcardsAndVault },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("learn_flashcards_vault")
                            ) {
                                Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Flashcards & Vault", "ফ্ল্যাশকার্ড ও সূত্র"))
                            }
                            FilledTonalButton(
                                onClick = { destination = LearnDestination.PomodoroFocus },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("learn_pomodoro")
                            ) {
                                Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Pomodoro Focus", "পমোডোরো ফোকাস"))
                            }
                            FilledTonalButton(
                                onClick = { destination = LearnDestination.DailyChallenge },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("learn_daily_challenge")
                            ) {
                                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Daily Streak", "দৈনিক চ্যালেঞ্জ"))
                            }
                            FilledTonalButton(
                                onClick = { destination = LearnDestination.MasteryAnalytics },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("learn_mastery_analytics")
                            ) {
                                Icon(Icons.Filled.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Mastery Analytics", "দক্ষতা বিশ্লেষণ"))
                            }
                            OutlinedButton(
                                onClick = {
                                    aiSheetContext = "" to StudyQuestStrings.t(uiLanguage, "General Study", "সাধারণ পড়াশোনা")
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "AI Tutor", "AI টিউটর"))
                            }
                        }
                    }
                }

                // Subjects
                item {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Subjects (NCTB Curriculum)", "বিষয়সমূহ (এনসিটিবি পাঠ্যক্রম)"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                itemsIndexed(subjects, key = { _, s -> s.id }) { idx, subject ->
                    val chCount = BangladeshCurriculumCatalog.chapters.count { it.subjectId == subject.id }
                    val subjLessons = BangladeshCurriculumCatalog.lessons.filter { it.subjectId == subject.id }
                    val doneCount = subjLessons.count { l -> cachedLessons.any { it.id == l.id && it.completed } }
                    val mastery = if (subjLessons.isEmpty()) 0f else doneCount.toFloat() / subjLessons.size.toFloat()
                    StaggeredEntrance(index = idx + 2, reducedMotion = reducedMotion) {
                        SubjectCard(
                            subject = subject,
                            language = uiLanguage,
                            chapterCount = chCount,
                            masteryProgress = mastery,
                            onClick = { destination = LearnDestination.SubjectDetail(subject) }
                        )
                    }
                }

                // Timed Mock Exams
                item {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Timed Mock Exams", "সময়নির্ভর মডেল টেস্ট"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(BangladeshCurriculumCatalog.examTemplates, key = { it.id }) { tpl ->
                    StudyQuestCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, tpl.titleEn, tpl.titleBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, tpl.descriptionEn, tpl.descriptionBn),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = StudyQuestStrings.t(
                                        uiLanguage,
                                        "${tpl.durationSeconds / 60} min • ${tpl.totalMarks} marks • Neg: -${tpl.negativeMarkingPerWrong}",
                                        "${StudyQuestStrings.formatDigits(tpl.durationSeconds / 60, uiLanguage)} মিনিট • ${StudyQuestStrings.formatDigits(tpl.totalMarks, uiLanguage)} নম্বর"
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ForestGreen
                                )
                            }
                            Button(
                                onClick = {
                                    val qs = allQuestions.filter { it.subjectId in tpl.subjectIds }.ifEmpty { allQuestions }.take(6)
                                    destination = LearnDestination.MockExamSession(tpl, qs)
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("start_mock_${tpl.id}")
                            ) {
                                Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Start", "শুরু"))
                            }
                        }
                    }
                }
            }
        }

        is LearnDestination.SubjectDetail -> {
            val subject = dest.subject
            val chapters = remember(subject.id) {
                BangladeshCurriculumCatalog.chapters.filter { it.subjectId == subject.id }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { destination = LearnDestination.Home }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, subject.titleEn, subject.titleBn),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (chapters.isEmpty()) {
                    item {
                        EmptyStateView(
                            titleEn = "No chapters yet",
                            titleBn = "এখনো অধ্যায় যোগ হয়নি",
                            subtitleEn = "Try practice questions in Question Bank.",
                            subtitleBn = "প্রশ্নব্যাংকে গিয়ে অনুশীলন করো।",
                            language = uiLanguage
                        )
                    }
                } else {
                    items(chapters, key = { it.id }) { chapter ->
                        val chapterLessons = BangladeshCurriculumCatalog.lessons.filter { it.chapterId == chapter.id }
                        val chapterQuestions = allQuestions.filter { it.chapterId == chapter.id }
                        val isCached = chapterLessons.isNotEmpty() && chapterLessons.all { l -> cachedLessons.any { it.id == l.id } }

                        StudyQuestCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, chapter.titleEn, chapter.titleBn),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, chapter.summaryEn, chapter.summaryBn),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDownloadChapter(chapter.id) },
                                    modifier = Modifier.testTag("download_ch_${chapter.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isCached) Icons.Filled.DownloadDone else Icons.Filled.DownloadForOffline,
                                        contentDescription = StudyQuestStrings.t(uiLanguage, "Download offline", "অফলাইন ডাউনলোড"),
                                        tint = ForestGreen
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            chapterLessons.forEach { lesson ->
                                val completed = cachedLessons.any { it.id == lesson.id && it.completed }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                if (completed) Icons.Filled.CheckCircle else Icons.AutoMirrored.Filled.MenuBook,
                                                contentDescription = null,
                                                tint = ForestGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, lesson.titleEn, lesson.titleBn),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = { destination = LearnDestination.LessonDetail(lesson) },
                                            modifier = Modifier.testTag("open_lesson_${lesson.id}")
                                        ) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Study", "পড়ো"))
                                        }
                                    }
                                }
                            }
                            if (chapterQuestions.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        destination = LearnDestination.PracticeSession(
                                            StudyQuestStrings.t(uiLanguage, chapter.titleEn, chapter.titleBn),
                                            chapterQuestions
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        StudyQuestStrings.t(
                                            uiLanguage,
                                            "Practice ${chapterQuestions.size} Questions",
                                            "${StudyQuestStrings.formatDigits(chapterQuestions.size, uiLanguage)}টি প্রশ্ন অনুশীলন"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        is LearnDestination.LessonDetail -> {
            LessonDetailScreen(
                lesson = dest.lesson,
                uiLanguage = uiLanguage,
                isCompleted = cachedLessons.any { it.id == dest.lesson.id && it.completed },
                onBack = { destination = LearnDestination.Home },
                onCompleteLesson = { onCompleteLesson(dest.lesson) },
                onPracticeTopic = {
                    val topicQs = allQuestions.filter { it.chapterId == dest.lesson.chapterId }
                        .ifEmpty { allQuestions.filter { it.subjectId == dest.lesson.subjectId } }
                    destination = LearnDestination.PracticeSession(
                        StudyQuestStrings.t(uiLanguage, dest.lesson.titleEn, dest.lesson.titleBn),
                        topicQs
                    )
                },
                onAskAi = {
                    aiSheetContext = StudyQuestStrings.t(uiLanguage, dest.lesson.titleEn, dest.lesson.titleBn) to dest.lesson.subjectId
                }
            )
        }

        LearnDestination.QuestionBank -> {
            QuestionBankScreen(
                uiLanguage = uiLanguage,
                questionLanguage = questionLanguage,
                allQuestions = allQuestions,
                bookmarks = bookmarks,
                onBack = { destination = LearnDestination.Home },
                onToggleBookmark = onToggleBookmark,
                onAddRevision = onAddRevision,
                onReportQuestion = onReportQuestion,
                onAskAi = { q ->
                    aiSheetContext = StudyQuestStrings.t(questionLanguage, q.promptEn, q.promptBn) to q.subjectId
                },
                onStartFilteredPractice = { title, qs ->
                    destination = LearnDestination.PracticeSession(title, qs)
                }
            )
        }

        is LearnDestination.PracticeSession -> {
            PracticeAndExamRunnerScreen(
                title = dest.title,
                questions = dest.questions,
                template = null,
                uiLanguage = uiLanguage,
                questionLanguage = questionLanguage,
                bookmarks = bookmarks,
                examRemainingSeconds = 0,
                assessmentResult = lastAssessmentResult,
                onBack = {
                    onClearAssessmentResult()
                    destination = LearnDestination.Home
                },
                onStartExamTimer = { _, _ -> },
                onToggleBookmark = onToggleBookmark,
                onAddRevision = onAddRevision,
                onReportQuestion = onReportQuestion,
                onAskAi = { q ->
                    aiSheetContext = StudyQuestStrings.t(questionLanguage, q.promptEn, q.promptBn) to q.subjectId
                },
                onSubmit = { sel, txt, cq ->
                    onSubmitAssessment(dest.title, dest.questions, sel, txt, cq, null)
                }
            )
        }

        is LearnDestination.MockExamSession -> {
            PracticeAndExamRunnerScreen(
                title = StudyQuestStrings.t(uiLanguage, dest.template.titleEn, dest.template.titleBn),
                questions = dest.questions,
                template = dest.template,
                uiLanguage = uiLanguage,
                questionLanguage = questionLanguage,
                bookmarks = bookmarks,
                examRemainingSeconds = examRemainingSeconds,
                assessmentResult = lastAssessmentResult,
                onBack = {
                    onClearAssessmentResult()
                    destination = LearnDestination.Home
                },
                onStartExamTimer = onStartExamTimer,
                onToggleBookmark = onToggleBookmark,
                onAddRevision = onAddRevision,
                onReportQuestion = onReportQuestion,
                onAskAi = { q ->
                    aiSheetContext = StudyQuestStrings.t(questionLanguage, q.promptEn, q.promptBn) to q.subjectId
                },
                onSubmit = { sel, txt, cq ->
                    onSubmitAssessment(
                        StudyQuestStrings.t(uiLanguage, dest.template.titleEn, dest.template.titleBn),
                        dest.questions,
                        sel,
                        txt,
                        cq,
                        dest.template
                    )
                }
            )
        }

        LearnDestination.RevisionAndPlanner -> {
            RevisionAndPlannerScreen(
                uiLanguage = uiLanguage,
                questionLanguage = questionLanguage,
                revisions = revisions,
                allQuestions = allQuestions,
                studyPlans = studyPlans,
                onBack = { destination = LearnDestination.Home },
                onReviewItem = onReviewRevision,
                onCreatePlan = onCreateStudyPlan,
                onTogglePlan = onToggleStudyPlan,
                onDeletePlan = onDeleteStudyPlan
            )
        }

        LearnDestination.FlashcardsAndVault -> {
            FlashcardsAndVaultScreen(
                uiLanguage = uiLanguage,
                questionLanguage = questionLanguage,
                classId = selectedClassId,
                allQuestions = allQuestions,
                reducedMotion = reducedMotion,
                onBack = { destination = LearnDestination.Home },
                onAddRevision = onAddRevision,
                onCompleteFlashcardDeck = onCompleteFlashcardDeck
            )
        }

        LearnDestination.PomodoroFocus -> {
            PomodoroFocusScreen(
                uiLanguage = uiLanguage,
                classId = selectedClassId,
                onBack = { destination = LearnDestination.Home },
                onCompleteFocusSession = onCompletePomodoro
            )
        }

        LearnDestination.DailyChallenge -> {
            DailyChallengeScreen(
                profile = profile,
                uiLanguage = uiLanguage,
                questionLanguage = questionLanguage,
                reducedMotion = reducedMotion,
                dateKey = dailyChallengeDateKey,
                alreadyDoneToday = dailyChallengeDoneToday,
                questions = dailyChallengeQuestions,
                bookmarks = bookmarks,
                assessmentResult = lastAssessmentResult,
                onBack = {
                    onClearAssessmentResult()
                    destination = LearnDestination.Home
                },
                onToggleBookmark = onToggleBookmark,
                onAddRevision = onAddRevision,
                onReportQuestion = onReportQuestion,
                onAskAi = { q ->
                    aiSheetContext = StudyQuestStrings.t(questionLanguage, q.promptEn, q.promptBn) to q.subjectId
                },
                onSubmitChallenge = { sel, txt ->
                    onSubmitDailyChallenge(dailyChallengeQuestions, sel, txt)
                }
            )
        }

        LearnDestination.MasteryAnalytics -> {
            MasteryAnalyticsScreen(
                profile = profile,
                uiLanguage = uiLanguage,
                reducedMotion = reducedMotion,
                subjectAnalytics = subjectAnalytics,
                weakTopics = weakTopics,
                onBack = { destination = LearnDestination.Home },
                onPracticeWeakTopic = { wt ->
                    val topicQs = allQuestions.filter { it.topicId == wt.topicId }
                        .ifEmpty { allQuestions.filter { it.subjectId == wt.subjectId } }
                        .ifEmpty { allQuestions.take(5) }
                    destination = LearnDestination.PracticeSession(
                        StudyQuestStrings.t(uiLanguage, wt.topicLabelEn, wt.topicLabelBn),
                        topicQs
                    )
                }
            )
        }
    }

    aiSheetContext?.let { (promptContext, subjectId) ->
        AiDoubtSolverSheet(
            initialContext = promptContext,
            classId = selectedClassId,
            subjectName = subjectId,
            language = uiLanguage,
            aiState = aiState,
            onSubmitDoubt = { studentQ ->
                onAskAi(promptContext, studentQ, selectedClassId, subjectId)
            },
            onDismiss = {
                onResetAi()
                aiSheetContext = null
            }
        )
    }
}

@Composable
private fun LessonDetailScreen(
    lesson: StudyLesson,
    uiLanguage: AppLanguage,
    isCompleted: Boolean,
    onBack: () -> Unit,
    onCompleteLesson: () -> Unit,
    onPracticeTopic: () -> Unit,
    onAskAi: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, lesson.titleEn, lesson.titleBn),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lesson.estimatedMinutes} min • ${lesson.sourceAttribution}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            StudyQuestCard {
                Text(StudyQuestStrings.t(uiLanguage, "Learning Objectives", "শিখনফল"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                val objs = if (uiLanguage == AppLanguage.BANGLA) lesson.objectivesBn else lesson.objectivesEn
                objs.forEach { obj ->
                    Text("• $obj", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }

        item {
            StudyQuestCard {
                Text(StudyQuestStrings.t(uiLanguage, "Concept Explanation", "মূল ধারণা ও ব্যাখ্যা"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = StudyQuestStrings.t(uiLanguage, lesson.explanationEn, lesson.explanationBn),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (lesson.formulas.isNotEmpty() || lesson.diagramAscii.isNotBlank()) {
            item {
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) {
                    if (lesson.formulas.isNotEmpty()) {
                        Text(StudyQuestStrings.t(uiLanguage, "Key Formulas", "গুরুত্বপূর্ণ সূত্র"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        lesson.formulas.forEach { f ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(
                                    text = f,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                    if (lesson.diagramAscii.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(StudyQuestStrings.t(uiLanguage, "Visual Diagram", "চিত্রায়িত ব্যাখ্যা"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = lesson.diagramAscii,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        item {
            StudyQuestCard {
                Text(StudyQuestStrings.t(uiLanguage, "Worked Examples & Key Points", "উদাহরণ ও মূল পয়েন্ট"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                val examples = if (uiLanguage == AppLanguage.BANGLA) lesson.examplesBn else lesson.examplesEn
                val points = if (uiLanguage == AppLanguage.BANGLA) lesson.keyPointsBn else lesson.keyPointsEn
                examples.forEach { Text("Ex: $it", style = MaterialTheme.typography.bodySmall) }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                points.forEach { Text("✓ $it", style = MaterialTheme.typography.bodySmall, color = ForestGreen) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCompleteLesson,
                    enabled = !isCompleted,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("complete_lesson_${lesson.id}")
                ) {
                    Text(
                        if (isCompleted) StudyQuestStrings.t(uiLanguage, "Completed (+15 XP)", "সম্পন্ন হয়েছে")
                        else StudyQuestStrings.t(uiLanguage, "Mark Complete (+15 XP)", "সম্পন্ন করুন (+১৫ XP)")
                    )
                }
                FilledTonalButton(
                    onClick = onPracticeTopic,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Practice", "অনুশীলন"))
                }
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = onAskAi,
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(StudyQuestStrings.t(uiLanguage, "Ask AI Doubt Solver about this lesson", "এই পাঠ নিয়ে AI-কে প্রশ্ন করো"))
            }
        }
    }
}
