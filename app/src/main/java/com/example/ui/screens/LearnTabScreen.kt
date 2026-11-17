package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.BookChapterEntity
import com.example.data.local.BookPageNoteEntity
import com.example.data.local.CachedBookEntity
import com.example.data.local.CachedLessonEntity
import com.example.data.local.LocalBookmarkEntity
import com.example.data.local.LocalRevisionEntity
import com.example.data.model.AcademicSubject
import com.example.data.model.AiChatMessageDoc
import com.example.data.model.AiConversationDoc
import com.example.data.model.AssignmentDefinitionDoc
import com.example.data.model.AssignmentSubmissionDoc
import com.example.data.model.ExamTemplate
import com.example.data.model.PublicProfile
import com.example.data.model.QuestionItem
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.StudyGoalDoc
import com.example.data.model.StudyLesson
import com.example.data.model.StudyPlanDoc
import com.example.data.model.TextbookCatalogItem
import com.example.data.model.TextbookDownloadStatus
import com.example.data.model.UserBadgeDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.model.XpTransactionDoc
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LessonSuccessLottieDialog
import com.example.ui.components.OpenBookQuestIllustration
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.SubjectCard
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.DeepQuestGold
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
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
import com.example.ui.viewmodel.AiDoubtUiState
import com.example.ui.viewmodel.AiVideoViewModel
import com.example.ui.viewmodel.AssessmentResultSummary
import com.example.ui.viewmodel.FocusTimerViewModel

sealed interface LearnDestination {
    data object Home : LearnDestination
    data object TextbookReader : LearnDestination
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
    data object AssignmentsAndGoals : LearnDestination
    data object AiVideoStudio : LearnDestination
    data object BadgesAndXp : LearnDestination
    data object AiLab : LearnDestination
}

private enum class LearnStatusFilter {
    ALL,
    IN_PROGRESS,
    NOT_STARTED,
    COMPLETED
}

private data class LearnToolShortcut(
    val tag: String,
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    onSubmitDailyChallenge: (List<QuestionItem>, Map<String, Set<Int>>, Map<String, String>) -> Unit = { _, _, _ -> },
    focusVm: FocusTimerViewModel? = null,
    aiVideoVm: AiVideoViewModel? = null,
    dailyStudySeries: List<DailyStudyMetric> = emptyList(),
    studyGoals: List<StudyGoalDoc> = emptyList(),
    assignments: List<AssignmentDefinitionDoc> = emptyList(),
    assignmentSubmissions: List<AssignmentSubmissionDoc> = emptyList(),
    userBadges: List<UserBadgeDoc> = emptyList(),
    xpLedger: List<XpTransactionDoc> = emptyList(),
    externalVideoRendererConfigured: Boolean = false,
    isGuestMode: Boolean = false,
    onRequestGoogleSignIn: () -> Unit = {},
    onCreateStudyGoal: (String, String, String, String, Long, String) -> Unit = { _, _, _, _, _, _ -> },
    onClaimStudyGoal: (StudyGoalDoc) -> Unit = {},
    onDeleteStudyGoal: (String) -> Unit = {},
    onSubmitAssignment: (AssignmentDefinitionDoc) -> Unit = {},
    onToggleBadgeFavorite: (String) -> Unit = {},
    onToggleBadgePin: (String) -> Unit = {},
    onSetExamOrQuizActive: (Boolean) -> Unit = {},
    subscription: UserSubscriptionDoc = UserSubscriptionDoc(),
    aiConversations: List<AiConversationDoc> = emptyList(),
    activeAiConversationId: String? = null,
    activeAiMessages: List<AiChatMessageDoc> = emptyList(),
    activeAiToolId: String = "QuestTutor",
    onSelectAiTool: (String) -> Unit = {},
    onSelectAiConversation: (String?) -> Unit = {},
    onCreateAiConversation: (String, String) -> Unit = { _, _ -> },
    onDeleteAiConversation: (String) -> Unit = {},
    onSendAiLabMessage: (String) -> Unit = {},
    onCancelAiRequest: () -> Unit = {},
    onRetryAiRequest: () -> Unit = {},
    onOpenPlanStore: () -> Unit = {},
    activeLockedBook: TextbookCatalogItem? = null,
    availableBooksForLock: List<TextbookCatalogItem> = emptyList(),
    cachedBooks: List<CachedBookEntity> = emptyList(),
    bookChapters: List<BookChapterEntity> = emptyList(),
    bookNotes: List<BookPageNoteEntity> = emptyList(),
    onInstallBookOffline: (TextbookCatalogItem, Boolean) -> Unit = { _, _ -> },
    onImportPdfForBook: (TextbookCatalogItem, Uri) -> Unit = { _, _ -> },
    onSaveLastReadPage: (String, Int) -> Unit = { _, _ -> },
    onMarkBookChapterCompleted: (String) -> Unit = {},
    onSaveBookPageNote: (String, Int, String?, String, String, Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteBookPageNote: (String) -> Unit = {},
    onSwitchActiveBook: (TextbookCatalogItem) -> Unit = {},
    onReportBookIssue: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val destinationStack = remember { mutableStateListOf<LearnDestination>(LearnDestination.Home) }
    val destination = destinationStack.lastOrNull() ?: LearnDestination.Home
    var selectedClassId by remember(initialClassId) { mutableStateOf(initialClassId) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(LearnStatusFilter.ALL) }
    var showSwitchBookSheet by remember { mutableStateOf(false) }
    var switchYearFilter by remember(activeLockedBook?.academicYear) {
        mutableStateOf(activeLockedBook?.academicYear ?: "2026")
    }
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val bookScopedQuestions = remember(allQuestions, activeLockedBook) {
        if (activeLockedBook == null) allQuestions
        else TextbookCatalog.questionsForActiveBook(allQuestions, activeLockedBook)
    }

    fun pushDest(next: LearnDestination) {
        if (next == LearnDestination.Home) {
            while (destinationStack.size > 1) {
                destinationStack.removeAt(destinationStack.lastIndex)
            }
        } else if (destinationStack.lastOrNull() != next) {
            destinationStack.add(next)
        }
    }

    fun popDest() {
        if (destinationStack.size > 1) {
            destinationStack.removeAt(destinationStack.lastIndex)
        }
    }

    LaunchedEffect(externalDestination) {
        if (externalDestination != null) {
            pushDest(externalDestination)
            onConsumeExternalDestination()
        }
    }

    LaunchedEffect(destination) {
        val inQuizOrExam = destination is LearnDestination.PracticeSession ||
            destination is LearnDestination.MockExamSession ||
            destination is LearnDestination.DailyChallenge
        onSetExamOrQuizActive(inQuizOrExam)
    }

    DisposableEffect(Unit) {
        onDispose { onSetExamOrQuizActive(false) }
    }

    var aiSheetContext by remember { mutableStateOf<Pair<String, String>?>(null) }

    BackHandler(enabled = aiSheetContext != null) {
        onResetAi()
        aiSheetContext = null
    }

    BackHandler(enabled = aiSheetContext == null && destinationStack.size > 1) {
        val top = destinationStack.lastOrNull()
        if (top is LearnDestination.PracticeSession ||
            top is LearnDestination.MockExamSession ||
            top is LearnDestination.DailyChallenge
        ) {
            onClearAssessmentResult()
        }
        popDest()
    }

    when (val dest = destination) {
        LearnDestination.Home -> {
            val allClassSubjects = remember(selectedClassId) {
                BangladeshCurriculumCatalog.subjectsForClass(selectedClassId)
            }
            val scopedSubjects = remember(allClassSubjects, activeLockedBook) {
                if (activeLockedBook == null) allClassSubjects
                else allClassSubjects.filter { it.id == activeLockedBook.subjectId }.ifEmpty { allClassSubjects }
            }
            val subjectMasteryMap = remember(scopedSubjects, cachedLessons) {
                scopedSubjects.associate { subject ->
                    val subjLessons = BangladeshCurriculumCatalog.lessons.filter { it.subjectId == subject.id }
                    val doneCount = subjLessons.count { l -> cachedLessons.any { it.id == l.id && it.completed } }
                    val ratio = if (subjLessons.isEmpty()) 0f else doneCount.toFloat() / subjLessons.size.toFloat()
                    subject.id to ratio
                }
            }
            val trimmedQuery = searchQuery.trim()
            val filteredSubjects = remember(scopedSubjects, trimmedQuery, statusFilter, subjectMasteryMap) {
                scopedSubjects.filter { subject ->
                    val matchesQuery = trimmedQuery.isEmpty() ||
                        subject.titleEn.contains(trimmedQuery, ignoreCase = true) ||
                        subject.titleBn.contains(trimmedQuery, ignoreCase = true) ||
                        subject.code.contains(trimmedQuery, ignoreCase = true)
                    val mastery = subjectMasteryMap[subject.id] ?: 0f
                    val matchesStatus = when (statusFilter) {
                        LearnStatusFilter.ALL -> true
                        LearnStatusFilter.IN_PROGRESS -> mastery > 0f && mastery < 1f
                        LearnStatusFilter.NOT_STARTED -> mastery == 0f
                        LearnStatusFilter.COMPLETED -> mastery >= 1f
                    }
                    matchesQuery && matchesStatus
                }
            }

            val scopedLessons = remember(scopedSubjects, cachedLessons) {
                val subjIds = scopedSubjects.map { it.id }.toSet()
                BangladeshCurriculumCatalog.lessons.filter { it.subjectId in subjIds }
            }
            val filteredLessons = remember(scopedLessons, trimmedQuery, statusFilter, cachedLessons) {
                scopedLessons.filter { lesson ->
                    val matchesQuery = trimmedQuery.isEmpty() ||
                        lesson.titleEn.contains(trimmedQuery, ignoreCase = true) ||
                        lesson.titleBn.contains(trimmedQuery, ignoreCase = true)
                    val isDone = cachedLessons.any { it.id == lesson.id && it.completed }
                    val isCached = cachedLessons.any { it.id == lesson.id }
                    val matchesStatus = when (statusFilter) {
                        LearnStatusFilter.ALL -> true
                        LearnStatusFilter.IN_PROGRESS -> isCached && !isDone
                        LearnStatusFilter.NOT_STARTED -> !isDone
                        LearnStatusFilter.COMPLETED -> isDone
                    }
                    matchesQuery && matchesStatus
                }
            }

            val classLessonIds = remember(scopedLessons) { scopedLessons.map { it.id }.toSet() }
            val completedLessonCount = remember(cachedLessons, classLessonIds) {
                cachedLessons.count { it.id in classLessonIds && it.completed }
            }
            val classQuestionsCount = remember(bookScopedQuestions, selectedClassId) {
                bookScopedQuestions.count { it.classId == selectedClassId }.coerceAtLeast(bookScopedQuestions.size)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("learn_hub_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Learn Hub Editorial Header + Live Search & Status Filter Chips
                item {
                    StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                        val cls = BangladeshCurriculumCatalog.classes.find { it.id == selectedClassId }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, "NCTB WORLD OF KNOWLEDGE", "এনসিটিবি শিক্ষার বিশ্ব"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, "Learn Hub", "শেখার বিশ্ব"),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftSageGreen
                                ) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "${cls?.labelEn ?: selectedClassId} • $classQuestionsCount Qs",
                                            "${cls?.labelBn ?: selectedClassId} • ${StudyQuestStrings.formatDigits(classQuestionsCount, uiLanguage)} প্রশ্ন"
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        StudyQuestStrings.t(
                                            uiLanguage,
                                            "Search subjects, lessons, or codes...",
                                            "বিষয় বা পাঠ খুঁজুন..."
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.testTag("learn_search_clear_btn")
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = StudyQuestStrings.t(uiLanguage, "Clear search", "খোঁজ মুছুন"))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("learn_search_input")
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val filters = listOf(
                                    Triple(LearnStatusFilter.ALL, "learn_filter_all", StudyQuestStrings.t(uiLanguage, "All", "সব")),
                                    Triple(LearnStatusFilter.IN_PROGRESS, "learn_filter_in_progress", StudyQuestStrings.t(uiLanguage, "In Progress", "চলমান")),
                                    Triple(LearnStatusFilter.NOT_STARTED, "learn_filter_not_started", StudyQuestStrings.t(uiLanguage, "Not Started", "শুরু হয়নি")),
                                    Triple(LearnStatusFilter.COMPLETED, "learn_filter_completed", StudyQuestStrings.t(uiLanguage, "Completed", "সম্পন্ন"))
                                )
                                items(filters, key = { it.second }) { (filter, tag, label) ->
                                    FilterChip(
                                        selected = statusFilter == filter,
                                        onClick = { statusFilter = filter },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag(tag)
                                    )
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(BangladeshCurriculumCatalog.classes, key = { it.id }) { clsItem ->
                                    FilterChip(
                                        selected = selectedClassId == clsItem.id,
                                        onClick = { if (clsItem.futureReadyTag == null) selectedClassId = clsItem.id },
                                        enabled = clsItem.futureReadyTag == null,
                                        label = { Text(StudyQuestStrings.t(uiLanguage, clsItem.labelEn, clsItem.labelBn)) },
                                        modifier = Modifier.testTag("learn_class_${clsItem.id}")
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Featured Learning Spotlight (Active Locked Textbook or Curriculum Focus)
                if (activeLockedBook != null) {
                    item {
                        val activeCache = cachedBooks.firstOrNull { it.bookId == activeLockedBook.id }
                        val installedOffline = activeCache?.downloadStatus == TextbookDownloadStatus.INSTALLED.id
                        val denom = classLessonIds.size.coerceAtLeast(1)
                        val lessonProgress = (completedLessonCount.toFloat() / denom.toFloat()).coerceIn(0f, 1f)
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                            border = studyQuestGlassAwareBorder(BrandForestGreen.copy(alpha = 0.28f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("learn_active_book_card")
                        ) {
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                                val wide = maxWidth >= 500.dp
                                val details: @Composable (Modifier) -> Unit = { mod ->
                                    Column(
                                        modifier = mod,
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
                                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                                    Text(
                                                        text = StudyQuestStrings.t(
                                                            uiLanguage,
                                                            "ACTIVE LOCKED TEXTBOOK • ${activeLockedBook.academicYear}",
                                                            "লককৃত সক্রিয় বই • ${activeLockedBook.academicYear}"
                                                        ),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            if (installedOffline) {
                                                Icon(
                                                    Icons.Filled.CloudDone,
                                                    contentDescription = StudyQuestStrings.t(uiLanguage, "Installed Offline", "অফলাইন প্রস্তুত"),
                                                    tint = EditorialSuccess,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = StudyQuestStrings.t(uiLanguage, activeLockedBook.titleEn, activeLockedBook.titleBn),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Text(
                                            text = StudyQuestStrings.t(
                                                uiLanguage,
                                                "${activeLockedBook.bookCode} • ${cachedLessons.size} offline lessons • $classQuestionsCount scoped questions",
                                                "${activeLockedBook.bookCode} • ${StudyQuestStrings.formatDigits(cachedLessons.size, uiLanguage)}টি অফলাইন পাঠ • ${StudyQuestStrings.formatDigits(classQuestionsCount, uiLanguage)} প্রশ্ন"
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { pushDest(LearnDestination.TextbookReader) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = BrandForestGreen,
                                                    contentColor = PureEditorialSurface
                                                ),
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("learn_open_reader_btn")
                                            ) {
                                                Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(StudyQuestStrings.t(uiLanguage, "Read Textbook", "বই পড়ো"))
                                            }
                                            FilledTonalButton(
                                                onClick = { showSwitchBookSheet = true },
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("learn_switch_book_btn")
                                            ) {
                                                Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(StudyQuestStrings.t(uiLanguage, "Switch", "পরিবর্তন"))
                                            }
                                        }
                                    }
                                }

                                if (wide) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        details(Modifier.weight(1.4f))
                                        Column(
                                            modifier = Modifier.weight(0.6f),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            AnimatedCircularMasteryRing(
                                                progress = lessonProgress,
                                                centerValueText = "${StudyQuestStrings.formatDigits(completedLessonCount, uiLanguage)}/${StudyQuestStrings.formatDigits(denom, uiLanguage)}",
                                                centerCaptionText = StudyQuestStrings.t(uiLanguage, "Lessons", "পাঠ"),
                                                reducedMotion = reducedMotion,
                                                size = 82.dp,
                                                ringColor = BrandForestGreen,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                valueColor = MaterialTheme.colorScheme.onSurface,
                                                captionColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        details(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Empty / No-Match Search Recovery State
                if (filteredSubjects.isEmpty() && filteredLessons.isEmpty()) {
                    item {
                        EmptyStateView(
                            titleEn = "No matching lessons or subjects",
                            titleBn = "কোনো মিল পাওয়া যায়নি",
                            subtitleEn = "Try clearing your search or status filter to explore all NCTB subjects.",
                            subtitleBn = "সব বিষয় ও পাঠ দেখতে ফিল্টার বা খোঁজ পরিষ্কার করুন।",
                            language = uiLanguage,
                            actionLabelEn = "Clear Filters",
                            actionLabelBn = "ফিল্টার মুছুন",
                            onAction = {
                                searchQuery = ""
                                statusFilter = LearnStatusFilter.ALL
                            },
                            actionTestTag = "learn_clear_filters_btn",
                            modifier = Modifier.testTag("learn_empty_search_state")
                        )
                    }
                }

                // 3. Explore Learning Worlds — Responsive Subject Mosaic
                if (filteredSubjects.isNotEmpty()) {
                    item {
                        StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                EditorialSectionHeader(
                                    eyebrow = StudyQuestStrings.t(uiLanguage, "EXPLORE WORLDS", "বিষয় জগৎ"),
                                    title = StudyQuestStrings.t(uiLanguage, "Subjects Mosaic", "বিষয়সমূহ"),
                                    actionLabel = StudyQuestStrings.t(uiLanguage, "Question Bank", "প্রশ্নব্যাংক"),
                                    onAction = { pushDest(LearnDestination.QuestionBank) }
                                )

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val columns = when {
                                        maxWidth >= 880.dp -> 3
                                        maxWidth >= 560.dp -> 2
                                        else -> 2
                                    }
                                    val rows = filteredSubjects.chunked(columns)
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        rows.forEach { rowSubjects ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                rowSubjects.forEach { subject ->
                                                    val chCount = BangladeshCurriculumCatalog.chapters.count { it.subjectId == subject.id }
                                                    val mastery = subjectMasteryMap[subject.id] ?: 0f
                                                    SubjectCard(
                                                        subject = subject,
                                                        language = uiLanguage,
                                                        chapterCount = chCount,
                                                        masteryProgress = mastery,
                                                        onClick = { pushDest(LearnDestination.SubjectDetail(subject)) },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                repeat(columns - rowSubjects.size) {
                                                    Spacer(Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Visual Learning Paths Strip (Dynamic Progression Nodes)
                item {
                    StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                        val completedChapters = bookChapters.count { it.completed }
                        val firstSubj = scopedSubjects.firstOrNull()
                        val pathNodes = listOf(
                            Triple(
                                StudyQuestStrings.t(uiLanguage, "1. Foundation", "১. মূল পাঠ"),
                                if (activeLockedBook != null) StudyQuestStrings.t(uiLanguage, "Read chapter", "অধ্যায় পড়ো")
                                else StudyQuestStrings.t(uiLanguage, "Pick topic", "বিষয় নির্বাচন"),
                                completedChapters > 0
                            ) to {
                                if (activeLockedBook != null) pushDest(LearnDestination.TextbookReader)
                                else if (firstSubj != null) pushDest(LearnDestination.SubjectDetail(firstSubj))
                            },
                            Triple(
                                StudyQuestStrings.t(uiLanguage, "2. Guided Lesson", "২. পাঠ অনুশীলন"),
                                "${StudyQuestStrings.formatDigits(completedLessonCount, uiLanguage)}/${StudyQuestStrings.formatDigits(classLessonIds.size.coerceAtLeast(1), uiLanguage)}",
                                completedLessonCount > 0
                            ) to {
                                val nextLesson = scopedLessons.firstOrNull { l -> cachedLessons.none { it.id == l.id && it.completed } }
                                    ?: scopedLessons.firstOrNull()
                                if (nextLesson != null) pushDest(LearnDestination.LessonDetail(nextLesson))
                                else pushDest(LearnDestination.QuestionBank)
                            },
                            Triple(
                                StudyQuestStrings.t(uiLanguage, "3. Topic Drills", "৩. প্রশ্ন চর্চা"),
                                StudyQuestStrings.t(uiLanguage, "$classQuestionsCount Qs", "${StudyQuestStrings.formatDigits(classQuestionsCount, uiLanguage)} প্রশ্ন"),
                                revisions.isNotEmpty()
                            ) to { pushDest(LearnDestination.QuestionBank) },
                            Triple(
                                StudyQuestStrings.t(uiLanguage, "4. Board Check", "৪. পরীক্ষা প্রস্তুতি"),
                                StudyQuestStrings.t(uiLanguage, "Mock exam", "মডেল টেস্ট"),
                                dailyChallengeDoneToday
                            ) to { pushDest(LearnDestination.DailyChallenge) }
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            EditorialSectionHeader(
                                eyebrow = StudyQuestStrings.t(uiLanguage, "STRUCTURED PROGRESSION", "ধারাবাহিক শেখা"),
                                title = StudyQuestStrings.t(uiLanguage, "Learning Paths", "শেখার ধাপ")
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("learn_paths_track")
                            ) {
                                items(pathNodes.size) { idx ->
                                    val (meta, onClick) = pathNodes[idx]
                                    val (title, subtitle, done) = meta
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            onClick = onClick,
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (done) {
                                                if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftSageGreen
                                            } else {
                                                studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface)
                                            },
                                            border = BorderStroke(
                                                1.2.dp,
                                                if (done) BrandForestGreen else MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier
                                                .width(136.dp)
                                                .minimumInteractiveComponentSize()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = subtitle,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                        if (idx < pathNodes.lastIndex) {
                                            Canvas(modifier = Modifier.width(22.dp).height(10.dp)) {
                                                drawLine(
                                                    color = if (done) QuestGold else BrandForestGreen.copy(alpha = 0.40f),
                                                    start = Offset(2.dp.toPx(), size.height / 2f),
                                                    end = Offset(size.width - 2.dp.toPx(), size.height / 2f),
                                                    strokeWidth = 2.dp.toPx(),
                                                    cap = StrokeCap.Round,
                                                    pathEffect = if (done) null else PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Discover Lessons Horizontal Shelf
                if (filteredLessons.isNotEmpty()) {
                    item {
                        StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                EditorialSectionHeader(
                                    eyebrow = StudyQuestStrings.t(uiLanguage, "CURATED CHAPTER LESSONS", "নির্বাচিত পাঠ"),
                                    title = StudyQuestStrings.t(uiLanguage, "Discover Lessons", "পাঠ অন্বেষণ করুন")
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("learn_discover_lessons_shelf")
                                ) {
                                    items(filteredLessons.take(12), key = { it.id }) { lesson ->
                                        val isDone = cachedLessons.any { it.id == lesson.id && it.completed }
                                        val parentSubj = BangladeshCurriculumCatalog.subjects.find { it.id == lesson.subjectId }
                                        val accent = parentSubj?.let { Color(it.accentHex) } ?: BrandForestGreen
                                        Surface(
                                            onClick = { pushDest(LearnDestination.LessonDetail(lesson)) },
                                            shape = RoundedCornerShape(18.dp),
                                            color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                            border = studyQuestGlassAwareBorder(if (isDone) EditorialSuccess else accent.copy(alpha = 0.45f)),
                                            modifier = Modifier
                                                .width(224.dp)
                                                .minimumInteractiveComponentSize()
                                                .testTag("discover_lesson_${lesson.id}")
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
                                                            text = parentSubj?.let { StudyQuestStrings.t(uiLanguage, it.titleEn, it.titleBn) }
                                                                ?: lesson.subjectId.uppercase(),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = accent,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                    if (isDone) {
                                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EditorialSuccess, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                Text(
                                                    text = StudyQuestStrings.t(uiLanguage, lesson.titleEn, lesson.titleBn),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = StudyQuestStrings.t(
                                                        uiLanguage,
                                                        "${lesson.estimatedMinutes} min • +15 XP",
                                                        "${StudyQuestStrings.formatDigits(lesson.estimatedMinutes, uiLanguage)} মি. • +১৫ XP"
                                                    ),
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
                }

                // 6. Study Tools & AI Shelf
                item {
                    StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                        val toolShortcuts = listOf(
                            LearnToolShortcut(
                                tag = "open_question_bank",
                                label = StudyQuestStrings.t(uiLanguage, "Question Bank", "প্রশ্নব্যাংক"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "MCQ, CQ & Board Qs", "বোর্ড ও সৃজনশীল"),
                                icon = Icons.Filled.Quiz,
                                accent = BrandForestGreen,
                                onClick = { pushDest(LearnDestination.QuestionBank) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_quick_practice",
                                label = StudyQuestStrings.t(uiLanguage, "Quick Practice", "দ্রুত অনুশীলন"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "6-question sprint", "৬ প্রশ্নের স্প্রিন্ট"),
                                icon = Icons.Filled.Bolt,
                                accent = MineralTeal,
                                onClick = {
                                    val qs = bookScopedQuestions.filter { it.classId == selectedClassId }.ifEmpty { bookScopedQuestions }.take(6)
                                    pushDest(
                                        LearnDestination.PracticeSession(
                                            StudyQuestStrings.t(uiLanguage, "Quick Practice", "কুইক প্র্যাকটিস"),
                                            qs
                                        )
                                    )
                                }
                            ),
                            LearnToolShortcut(
                                tag = "learn_flashcards_vault",
                                label = StudyQuestStrings.t(uiLanguage, "Flashcards & Vault", "ফ্ল্যাশকার্ড ও সূত্র"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Active recall deck", "দ্রুত পুনরাবৃত্তি"),
                                icon = Icons.Filled.Style,
                                accent = DeepQuestGold,
                                onClick = { pushDest(LearnDestination.FlashcardsAndVault) }
                            ),
                            LearnToolShortcut(
                                tag = "open_revision_planner",
                                label = StudyQuestStrings.t(uiLanguage, "Revision & Planner", "রিভিশন ও প্ল্যানার"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Spaced review", "স্মার্ট রিভিশন"),
                                icon = Icons.Filled.Replay,
                                accent = SlateBotanical,
                                onClick = { pushDest(LearnDestination.RevisionAndPlanner) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_pomodoro",
                                label = StudyQuestStrings.t(uiLanguage, "Pomodoro Focus", "পমোডোরো ফোকাস"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Timer & ambient audio", "ফোকাস টাইমার"),
                                icon = Icons.Filled.Timer,
                                accent = WarmTerracotta,
                                onClick = { pushDest(LearnDestination.PomodoroFocus) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_daily_challenge",
                                label = StudyQuestStrings.t(uiLanguage, "Daily Streak", "দৈনিক চ্যালেঞ্জ"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Keep your momentum", "ধারাবাহিকতা ধরে রাখো"),
                                icon = Icons.Filled.LocalFireDepartment,
                                accent = DeepQuestGold,
                                onClick = { pushDest(LearnDestination.DailyChallenge) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_mastery_analytics",
                                label = StudyQuestStrings.t(uiLanguage, "Mastery Analytics", "দক্ষতা বিশ্লেষণ"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Accuracy & trends", "সঠিকতা ও ধারা"),
                                icon = Icons.Filled.Analytics,
                                accent = BrandForestGreen,
                                onClick = { pushDest(LearnDestination.MasteryAnalytics) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_ai_lab",
                                label = StudyQuestStrings.t(uiLanguage, "StudyQuest AI Lab", "StudyQuest AI ল্যাব"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Multi-turn AI tutor", "বিস্তারিত AI টিউটর"),
                                icon = Icons.Filled.AutoAwesome,
                                accent = MineralTeal,
                                onClick = { pushDest(LearnDestination.AiLab) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_quick_doubt",
                                label = StudyQuestStrings.t(uiLanguage, "Quick Doubt", "দ্রুত সমাধান"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Instant Gemini Q&A", "তাৎক্ষণিক প্রশ্নোত্তর"),
                                icon = Icons.Filled.AutoAwesome,
                                accent = WarmTerracotta,
                                onClick = {
                                    aiSheetContext = "" to StudyQuestStrings.t(uiLanguage, "General Study", "সাধারণ পড়াশোনা")
                                }
                            ),
                            LearnToolShortcut(
                                tag = "learn_assignments_goals",
                                label = StudyQuestStrings.t(uiLanguage, "Goals & Tasks", "লক্ষ্য ও কাজ"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Track weekly plans", "সাপ্তাহিক লক্ষ্য"),
                                icon = Icons.Filled.Assignment,
                                accent = SlateBotanical,
                                onClick = { pushDest(LearnDestination.AssignmentsAndGoals) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_ai_video_studio",
                                label = StudyQuestStrings.t(uiLanguage, "Video Library", "ভিডিও লাইব্রেরি"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Concept explainers", "ধারণা ভিডিও"),
                                icon = Icons.Filled.VideoLibrary,
                                accent = BrandForestGreen,
                                onClick = { pushDest(LearnDestination.AiVideoStudio) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_badges_xp",
                                label = StudyQuestStrings.t(uiLanguage, "Badges & XP", "ব্যাজ ও XP"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Earned milestones", "অর্জিত সম্মাননা"),
                                icon = Icons.Filled.WorkspacePremium,
                                accent = DeepQuestGold,
                                onClick = { pushDest(LearnDestination.BadgesAndXp) }
                            )
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            EditorialSectionHeader(
                                eyebrow = StudyQuestStrings.t(uiLanguage, "PRACTICE & AI WORKSPACE", "অনুশীলন ও টুলস"),
                                title = StudyQuestStrings.t(uiLanguage, "Study Tools", "শেখার সহায়িকা")
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("learn_tools_shelf")
                            ) {
                                items(toolShortcuts, key = { it.tag }) { tool ->
                                    Surface(
                                        onClick = tool.onClick,
                                        shape = RoundedCornerShape(18.dp),
                                        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                        border = studyQuestGlassAwareBorder(tool.accent.copy(alpha = 0.40f)),
                                        modifier = Modifier
                                            .width(172.dp)
                                            .minimumInteractiveComponentSize()
                                            .testTag(tool.tag)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = tool.accent.copy(alpha = 0.14f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(tool.icon, contentDescription = null, tint = tool.accent, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Text(
                                                text = tool.label,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = tool.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Timed Mock Exams & Promotion Track Shelf
                item {
                    StaggeredEntrance(index = 5, reducedMotion = reducedMotion) {
                        val promoTemplate = ExamTemplate(
                            id = "promotion_${profile.classId}",
                            titleEn = "Annual Class Promotion Exam (${StudyQuestStrings.classLabel(profile.classId, uiLanguage)})",
                            titleBn = "বার্ষিক শ্রেণি প্রোমোশন পরীক্ষা (${StudyQuestStrings.classLabel(profile.classId, uiLanguage)})",
                            classId = profile.classId,
                            subjectIds = BangladeshCurriculumCatalog.subjectsForClass(profile.classId).map { it.id },
                            durationSeconds = 900,
                            totalMarks = 8,
                            negativeMarkingPerWrong = 0.0,
                            descriptionEn = if (profile.annualExamStatus == "failed") {
                                "Required retake (60%+ passes & advances your locked class)."
                            } else {
                                "Pass 60%+ to secure Jan 1 academic year promotion."
                            },
                            descriptionBn = "বার্ষিক উত্তীর্ণ হতে ৬০%+ নম্বর অর্জন করুন।"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            EditorialSectionHeader(
                                eyebrow = StudyQuestStrings.t(uiLanguage, "ASSESSMENTS", "মূল্যায়ন ও মডেল টেস্ট"),
                                title = StudyQuestStrings.t(uiLanguage, "Timed Mock Exams", "সময়নির্ভর মডেল টেস্ট")
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item(key = "promo_exam") {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (profile.annualExamStatus == "failed") {
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                                        } else if (visualConfig.isDark) {
                                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.50f)
                                        } else {
                                            SoftQuestGold
                                        },
                                        border = BorderStroke(1.dp, QuestGold.copy(alpha = 0.55f)),
                                        modifier = Modifier
                                            .width(276.dp)
                                            .testTag("promotion_exam_card")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Filled.School, contentDescription = null, tint = BrandForestGreen, modifier = Modifier.size(18.dp))
                                                Text(
                                                    text = StudyQuestStrings.t(uiLanguage, "PROMOTION EXAM", "প্রোমোশন পরীক্ষা"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepQuestGold
                                                )
                                            }
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, promoTemplate.titleEn, promoTemplate.titleBn),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2
                                            )
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, promoTemplate.descriptionEn, promoTemplate.descriptionBn),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2
                                            )
                                            Button(
                                                onClick = {
                                                    val qs = allQuestions.filter { it.classId == profile.classId }.ifEmpty { allQuestions }.take(8)
                                                    pushDest(LearnDestination.MockExamSession(promoTemplate, qs))
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = BrandForestGreen,
                                                    contentColor = PureEditorialSurface
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("start_promotion_exam_btn")
                                            ) {
                                                Text(StudyQuestStrings.t(uiLanguage, "Take Exam", "পরীক্ষা দিন"))
                                            }
                                        }
                                    }
                                }

                                items(BangladeshCurriculumCatalog.examTemplates, key = { it.id }) { tpl ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                        border = studyQuestGlassAwareBorder(),
                                        modifier = Modifier.width(268.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = StudyQuestStrings.t(
                                                    uiLanguage,
                                                    "${tpl.durationSeconds / 60} min • ${tpl.totalMarks} marks",
                                                    "${StudyQuestStrings.formatDigits(tpl.durationSeconds / 60, uiLanguage)} মিনিট • ${StudyQuestStrings.formatDigits(tpl.totalMarks, uiLanguage)} নম্বর"
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, tpl.titleEn, tpl.titleBn),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, tpl.descriptionEn, tpl.descriptionBn),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Button(
                                                onClick = {
                                                    val qs = allQuestions.filter { it.subjectId in tpl.subjectIds }.ifEmpty { allQuestions }.take(6)
                                                    pushDest(LearnDestination.MockExamSession(tpl, qs))
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("start_mock_${tpl.id}")
                                            ) {
                                                Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(StudyQuestStrings.t(uiLanguage, "Start Exam", "শুরু করুন"))
                                            }
                                        }
                                    }
                                }
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
                        IconButton(onClick = { popDest() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, subject.titleEn, subject.titleBn),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
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
                                        tint = BrandForestGreen
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
                                                tint = if (completed) EditorialSuccess else BrandForestGreen,
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
                                            onClick = { pushDest(LearnDestination.LessonDetail(lesson)) },
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
                                        pushDest(
                                            LearnDestination.PracticeSession(
                                                StudyQuestStrings.t(uiLanguage, chapter.titleEn, chapter.titleBn),
                                                chapterQuestions
                                            )
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
                reducedMotion = reducedMotion,
                onBack = { popDest() },
                onCompleteLesson = { onCompleteLesson(dest.lesson) },
                onPracticeTopic = {
                    val topicQs = allQuestions.filter { it.chapterId == dest.lesson.chapterId }
                        .ifEmpty { allQuestions.filter { it.subjectId == dest.lesson.subjectId } }
                    pushDest(
                        LearnDestination.PracticeSession(
                            StudyQuestStrings.t(uiLanguage, dest.lesson.titleEn, dest.lesson.titleBn),
                            topicQs
                        )
                    )
                },
                onAskAi = {
                    aiSheetContext = StudyQuestStrings.t(uiLanguage, dest.lesson.titleEn, dest.lesson.titleBn) to dest.lesson.subjectId
                }
            )
        }

        LearnDestination.TextbookReader -> {
            val activeBook = activeLockedBook ?: TextbookCatalog.books.first()
            TextbookReaderScreen(
                book = activeBook,
                cachedBook = cachedBooks.firstOrNull { it.bookId == activeBook.id },
                chapters = bookChapters,
                notes = bookNotes,
                uiLanguage = uiLanguage,
                onBack = { popDest() },
                onInstallOffline = { force -> onInstallBookOffline(activeBook, force) },
                onImportLocalPdf = { uri -> onImportPdfForBook(activeBook, uri) },
                onSaveLastReadPage = { page -> onSaveLastReadPage(activeBook.id, page) },
                onMarkChapterCompleted = onMarkBookChapterCompleted,
                onSavePageNote = { p, chId, t, txt, bm ->
                    onSaveBookPageNote(activeBook.id, p, chId, t, txt, bm)
                },
                onDeletePageNote = onDeleteBookPageNote,
                onAskAiAboutPage = { prompt ->
                    aiSheetContext = prompt to activeBook.subjectId
                },
                onPracticeBookQuestions = {
                    val qs = bookScopedQuestions.take(8)
                    pushDest(
                        LearnDestination.PracticeSession(
                            StudyQuestStrings.t(uiLanguage, activeBook.titleEn, activeBook.titleBn),
                            qs
                        )
                    )
                },
                onReportBookIssue = { reason, details ->
                    onReportBookIssue("textbook", activeBook.id, "$reason: $details")
                },
                onSwitchBook = { showSwitchBookSheet = true }
            )
        }

        LearnDestination.QuestionBank -> {
            QuestionBankScreen(
                uiLanguage = uiLanguage,
                questionLanguage = questionLanguage,
                allQuestions = bookScopedQuestions,
                bookmarks = bookmarks,
                onBack = { popDest() },
                onToggleBookmark = onToggleBookmark,
                onAddRevision = onAddRevision,
                onReportQuestion = onReportQuestion,
                onAskAi = { q ->
                    aiSheetContext = StudyQuestStrings.t(questionLanguage, q.promptEn, q.promptBn) to q.subjectId
                },
                onStartFilteredPractice = { title, qs ->
                    pushDest(LearnDestination.PracticeSession(title, qs))
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
                reducedMotion = reducedMotion,
                onBack = {
                    onClearAssessmentResult()
                    popDest()
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
                reducedMotion = reducedMotion,
                onBack = {
                    onClearAssessmentResult()
                    popDest()
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
                onBack = { popDest() },
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
                allQuestions = bookScopedQuestions,
                reducedMotion = reducedMotion,
                onBack = { popDest() },
                onAddRevision = onAddRevision,
                onCompleteFlashcardDeck = onCompleteFlashcardDeck
            )
        }

        LearnDestination.PomodoroFocus -> {
            if (focusVm != null) {
                FocusAndMusicScreen(
                    uiLanguage = uiLanguage,
                    classId = selectedClassId,
                    focusVm = focusVm,
                    onBack = { popDest() }
                )
            } else {
                PomodoroFocusScreen(
                    uiLanguage = uiLanguage,
                    classId = selectedClassId,
                    onBack = { popDest() },
                    onCompleteFocusSession = onCompletePomodoro
                )
            }
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
                    popDest()
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
                dailyStudySeries = dailyStudySeries,
                onBack = { popDest() },
                onPracticeWeakTopic = { wt ->
                    val topicQs = allQuestions.filter { it.topicId == wt.topicId }
                        .ifEmpty { allQuestions.filter { it.subjectId == wt.subjectId } }
                        .ifEmpty { allQuestions.take(5) }
                    pushDest(
                        LearnDestination.PracticeSession(
                            StudyQuestStrings.t(uiLanguage, wt.topicLabelEn, wt.topicLabelBn),
                            topicQs
                        )
                    )
                }
            )
        }

        LearnDestination.AssignmentsAndGoals -> {
            AssignmentsAndGoalsScreen(
                uiLanguage = uiLanguage,
                classId = profile.classId,
                studyGoals = studyGoals,
                assignments = assignments,
                submissions = assignmentSubmissions,
                isGuestMode = isGuestMode,
                onCreateGoal = onCreateStudyGoal,
                onClaimGoal = onClaimStudyGoal,
                onDeleteGoal = onDeleteStudyGoal,
                onSubmitAssignment = onSubmitAssignment,
                onBack = { popDest() }
            )
        }

        LearnDestination.AiVideoStudio -> {
            if (aiVideoVm != null) {
                AiVideoStudioScreen(
                    uiLanguage = uiLanguage,
                    classId = profile.classId,
                    ownerName = profile.displayName,
                    currentUserId = profile.userId,
                    subscription = subscription,
                    aiVideoVm = aiVideoVm,
                    isOnline = isOnline,
                    externalVideoRendererConfigured = externalVideoRendererConfigured,
                    isGuestMode = isGuestMode,
                    onOpenVideoUpgrade = onOpenPlanStore,
                    onRequestGoogleSignIn = onRequestGoogleSignIn,
                    onBack = { popDest() }
                )
            } else {
                LaunchedEffect(Unit) { popDest() }
            }
        }

        LearnDestination.BadgesAndXp -> {
            BadgesAndXpScreen(
                profile = profile,
                uiLanguage = uiLanguage,
                userBadges = userBadges,
                xpLedger = xpLedger,
                isGuestMode = isGuestMode,
                onToggleFavorite = onToggleBadgeFavorite,
                onTogglePin = onToggleBadgePin,
                onBack = { popDest() }
            )
        }

        LearnDestination.AiLab -> {
            AILabScreen(
                language = uiLanguage,
                classId = selectedClassId,
                subjectName = "General Study",
                subscription = subscription,
                activeToolId = activeAiToolId,
                conversations = aiConversations,
                activeConversationId = activeAiConversationId,
                messages = activeAiMessages,
                aiState = aiState,
                isGuestMode = isGuestMode,
                onSelectTool = onSelectAiTool,
                onSelectConversation = onSelectAiConversation,
                onCreateConversation = onCreateAiConversation,
                onDeleteConversation = onDeleteAiConversation,
                onSendMessage = onSendAiLabMessage,
                onCancelRequest = onCancelAiRequest,
                onRetryRequest = onRetryAiRequest,
                onOpenPlanStore = onOpenPlanStore,
                onRequestGoogleSignIn = onRequestGoogleSignIn,
                onBack = { popDest() }
            )
        }
    }

    aiSheetContext?.let { (promptContext, subjectId) ->
        val activePlan = QuestPlanCatalog.planById(subscription.tier)
        val aiLimit = subscription.monthlyAiLimit.toInt().coerceAtLeast(activePlan.monthlyAiCredits)
        val aiLeft = (aiLimit - subscription.aiCreditsUsed.toInt()).coerceAtLeast(0)
        AiDoubtSolverSheet(
            initialContext = promptContext,
            classId = selectedClassId,
            subjectName = subjectId,
            language = uiLanguage,
            aiState = aiState,
            remainingAiCredits = if (isGuestMode) null else aiLeft,
            onOpenAiLab = {
                aiSheetContext = null
                pushDest(LearnDestination.AiLab)
            },
            onSubmitDoubt = { studentQ ->
                onAskAi(promptContext, studentQ, selectedClassId, subjectId)
            },
            onDismiss = {
                onResetAi()
                aiSheetContext = null
            }
        )
    }

    if (showSwitchBookSheet) {
        BackHandler { showSwitchBookSheet = false }
        ModalBottomSheet(onDismissRequest = { showSwitchBookSheet = false }) {
            val choices = remember(availableBooksForLock, switchYearFilter, selectedClassId, profile.curriculumVersion, profile.academicGroup) {
                val base = availableBooksForLock.ifEmpty {
                    TextbookCatalog.booksFor(
                        academicYear = switchYearFilter,
                        classId = selectedClassId,
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
                    .testTag("learn_switch_book_sheet"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Switch Locked Active Textbook", "সক্রিয় পাঠ্যবই পরিবর্তন করো"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
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
                choices.take(12).forEach { choice ->
                    val selected = choice.id == activeLockedBook?.id
                    StudyQuestCard(
                        accentColor = if (selected) BrandForestGreen else SlateBotanical,
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
                                    text = StudyQuestStrings.t(uiLanguage, choice.titleEn, choice.titleBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${choice.bookCode} • ${StudyQuestStrings.t(uiLanguage, choice.editionLabelEn, choice.editionLabelBn)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selected) {
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
private fun LessonDetailScreen(
    lesson: StudyLesson,
    uiLanguage: AppLanguage,
    reducedMotion: Boolean = false,
    isCompleted: Boolean,
    onBack: () -> Unit,
    onCompleteLesson: () -> Unit,
    onPracticeTopic: () -> Unit,
    onAskAi: () -> Unit
) {
    var showSuccessLottie by remember { mutableStateOf(false) }

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
                points.forEach { Text("✓ $it", style = MaterialTheme.typography.bodySmall, color = EditorialSuccess) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onCompleteLesson()
                        showSuccessLottie = true
                    },
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

    if (showSuccessLottie) {
        LessonSuccessLottieDialog(
            lessonTitle = StudyQuestStrings.t(uiLanguage, lesson.titleEn, lesson.titleBn),
            xpEarned = 15,
            language = uiLanguage,
            reducedMotion = reducedMotion,
            onDismiss = { showSuccessLottie = false }
        )
    }
}
