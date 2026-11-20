package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.OfflineVaultManager
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.FormulaEntry
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.BookChapterEntity
import com.example.data.local.BookPageNoteEntity
import com.example.data.local.CachedBookEntity
import com.example.data.local.CachedLessonEntity
import com.example.data.local.DownloadPackEntity
import com.example.data.local.ExamCountdownEntity
import com.example.data.local.FormulaBookmarkEntity
import com.example.data.local.LocalBookmarkEntity
import com.example.data.local.LocalRevisionEntity
import com.example.data.local.LocalSoloMatchEntity
import com.example.data.local.MistakeNoteEntity
import com.example.data.model.AcademicSubject
import com.example.data.model.AiChatMessageDoc
import com.example.data.model.AiConversationDoc
import com.example.data.model.AssignmentDefinitionDoc
import com.example.data.model.AssignmentSubmissionDoc
import com.example.data.model.ExamTemplate
import com.example.data.model.LearningRecordDoc
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
import com.example.data.repository.DownloadPackCategory
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import androidx.compose.foundation.layout.widthIn
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GeometricStudyArtwork
import com.example.ui.components.LessonSuccessLottieDialog
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.SubjectCard
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.CeruleanSlate
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.MineralTeal
import com.example.ui.theme.SlateBotanical
import com.example.ui.theme.SoftPowderBlue
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
    data class InteractiveStemLab(val initialLabId: String = "trig") : LearnDestination
    data object OfflineDownloads : LearnDestination
    data object FormulaAtlas : LearnDestination
    data object MistakeNotebook : LearnDestination
    data object ExamCountdown : LearnDestination
    data object PocketAudioRevision : LearnDestination
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
    onReportBookIssue: (String, String, String) -> Unit = { _, _, _ -> },
    downloadPacks: List<DownloadPackEntity> = emptyList(),
    unresolvedMistakes: List<MistakeNoteEntity> = emptyList(),
    examCountdowns: List<ExamCountdownEntity> = emptyList(),
    formulaBookmarks: List<FormulaBookmarkEntity> = emptyList(),
    learningRecords: List<LearningRecordDoc> = emptyList(),
    soloMatches: List<LocalSoloMatchEntity> = emptyList(),
    onSeedDownloads: () -> Unit = {},
    onDownloadPack: (DownloadPackEntity) -> Unit = {},
    onDownloadClassBundle: () -> Unit = {},
    onRemoveDownloadPack: (String) -> Unit = {},
    onToggleFormulaBookmark: (FormulaEntry) -> Unit = {},
    onResolveMistake: (String) -> Unit = {},
    onAddExamCountdown: (String, String, Int, Int) -> Unit = { _, _, _, _ -> },
    onIncrementExamTopic: (String) -> Unit = {},
    onDeleteExamCountdown: (String) -> Unit = {}
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
    val semantic = LocalStudyQuestColors.current
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

            val activeCache = activeLockedBook?.let { b -> cachedBooks.firstOrNull { it.bookId == b.id } }
            val installedOffline = activeCache?.downloadStatus == TextbookDownloadStatus.INSTALLED.id
            val denom = classLessonIds.size.coerceAtLeast(1)
            val lessonProgress = (completedLessonCount.toFloat() / denom.toFloat()).coerceIn(0f, 1f)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("learn_hub_list"),
                contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Clean Swiss Monochrome Header, Search & Active Textbook Card
                item {
                    val cls = BangladeshCurriculumCatalog.classes.find { it.id == selectedClassId }
                    Column(
                        modifier = Modifier
                            .widthIn(max = 980.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, "CURRICULUM LIBRARY", "পাঠ্যক্রম ও কোর্সসমূহ"),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, "Learn Hub", "শেখার বিশ্ব"),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = studyQuestGlassAwareBorder()
                            ) {
                                Text(
                                    text = StudyQuestStrings.t(
                                        uiLanguage,
                                        "${cls?.labelEn ?: selectedClassId} • $classQuestionsCount Qs",
                                        "${cls?.labelBn ?: selectedClassId} • ${StudyQuestStrings.formatDigits(classQuestionsCount, uiLanguage)} প্রশ্ন"
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
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
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.testTag("learn_search_clear_btn")
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = StudyQuestStrings.t(uiLanguage, "Clear search", "খোঁজ মুছুন")
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("learn_search_input")
                        )

                        if (activeLockedBook != null) {
                            val focalBg = if (visualConfig.isDark) Color(0xFF161D2A) else Color(0xFF0F172A)
                            val focalBorder = if (visualConfig.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFF1E293B)
                            Surface(
                                onClick = { pushDest(LearnDestination.TextbookReader) },
                                shape = RoundedCornerShape(16.dp),
                                color = focalBg,
                                border = BorderStroke(1.dp, focalBorder),
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("learn_active_book_card")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = StudyQuestStrings.t(
                                                    uiLanguage,
                                                    "ACTIVE TEXTBOOK • ${activeLockedBook.academicYear}" + if (installedOffline) " • OFFLINE" else "",
                                                    "সক্রিয় পাঠ্যবই • ${activeLockedBook.academicYear}" + if (installedOffline) " • অফলাইন" else ""
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White.copy(alpha = 0.65f)
                                            )
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, activeLockedBook.titleEn, activeLockedBook.titleBn),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.10f)
                                        ) {
                                            Text(
                                                text = "${StudyQuestStrings.formatDigits(completedLessonCount, uiLanguage)}/${StudyQuestStrings.formatDigits(denom, uiLanguage)}",
                                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "${activeLockedBook.bookCode} • ${cachedLessons.size} offline lessons • $classQuestionsCount scoped questions",
                                            "${activeLockedBook.bookCode} • ${StudyQuestStrings.formatDigits(cachedLessons.size, uiLanguage)}টি পাঠ • ${StudyQuestStrings.formatDigits(classQuestionsCount, uiLanguage)} প্রশ্ন"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.78f)
                                    )
                                    LinearProgressIndicator(
                                        progress = { lessonProgress.coerceIn(0.04f, 1f) },
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
                                            onClick = { pushDest(LearnDestination.TextbookReader) },
                                            shape = RoundedCornerShape(999.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = Color(0xFF0F172A)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .testTag("learn_open_reader_btn")
                                        ) {
                                            Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, "Read Textbook", "বই পড়ুন"),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        TextButton(
                                            onClick = { showSwitchBookSheet = true },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.85f)),
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("learn_switch_book_btn")
                                        ) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Switch", "পরিবর্তন"), fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Search & Class Filters Bar below Hero
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 980.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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

                // Empty / No-Match Search Recovery State
                if (filteredSubjects.isEmpty() && filteredLessons.isEmpty()) {
                    item {
                        Box(modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth().padding(horizontal = 16.dp)) {
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
                }

                // 2. Layered Subjects & Courses Grid
                if (filteredSubjects.isNotEmpty()) {
                    item {
                        StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 980.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                EditorialSectionHeader(
                                    eyebrow = StudyQuestStrings.t(uiLanguage, "CURRICULUM SUBJECTS", "বিষয়সমূহ"),
                                    title = StudyQuestStrings.t(uiLanguage, "Featured Courses", "কোর্স ও বিষয়"),
                                    actionLabel = StudyQuestStrings.t(uiLanguage, "Question Bank", "প্রশ্নব্যাংক"),
                                    onAction = { pushDest(LearnDestination.QuestionBank) }
                                )

                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val columns = when {
                                        maxWidth >= 820.dp -> 3
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

                // 3. Structured Learning Path Strip
                item {
                    StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                        val completedChapters = bookChapters.count { it.completed }
                        val firstSubj = scopedSubjects.firstOrNull()
                        val pathNodes = listOf(
                            Triple(
                                StudyQuestStrings.t(uiLanguage, "1. Foundation", "১. মূল পাঠ"),
                                if (activeLockedBook != null) StudyQuestStrings.t(uiLanguage, "Read chapter", "অধ্যায় পড়ুন")
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
                                StudyQuestStrings.t(uiLanguage, "Daily check", "দৈনিক পরীক্ষা"),
                                dailyChallengeDoneToday
                            ) to { pushDest(LearnDestination.DailyChallenge) }
                        )

                        Column(
                            modifier = Modifier
                                .widthIn(max = 980.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EditorialSectionHeader(
                                eyebrow = StudyQuestStrings.t(uiLanguage, "STRUCTURED PROGRESSION", "ধারাবাহিক শেখা"),
                                title = StudyQuestStrings.t(uiLanguage, "Learning Sequence", "শেখার ধাপ")
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
                                                if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftPowderBlue
                                            } else {
                                                studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface)
                                            },
                                            border = BorderStroke(
                                                1.2.dp,
                                                if (done) BrandTealPrimary else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier
                                                .width(142.dp)
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
                                                    color = if (done) BrandTealPrimary else CeruleanSlate.copy(alpha = 0.35f),
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

                // 4. Discover Lessons Horizontal Shelf
                if (filteredLessons.isNotEmpty()) {
                    item {
                        StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 980.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
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
                                        val accent = parentSubj?.let { Color(it.accentHex) } ?: BrandTealPrimary
                                        Surface(
                                            onClick = { pushDest(LearnDestination.LessonDetail(lesson)) },
                                            shape = RoundedCornerShape(18.dp),
                                            color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                            border = studyQuestGlassAwareBorder(if (isDone) EditorialSuccess else accent.copy(alpha = 0.35f)),
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
                                                        color = accent.copy(alpha = 0.13f)
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
                                                        "${lesson.estimatedMinutes} min • Guided lesson",
                                                        "${StudyQuestStrings.formatDigits(lesson.estimatedMinutes, uiLanguage)} মি. • পাঠ"
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

                // 5. Organized Learning Collections Grid (Compact Multi-Column Cards)
                item {
                    StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                        val toolShortcuts = listOf(
                            LearnToolShortcut(
                                tag = "open_question_bank",
                                label = StudyQuestStrings.t(uiLanguage, "Question Bank", "প্রশ্নব্যাংক"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "MCQ, CQ & Board Qs", "বোর্ড ও সৃজনশীল"),
                                icon = Icons.Filled.Quiz,
                                accent = BrandTealPrimary,
                                onClick = { pushDest(LearnDestination.QuestionBank) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_quick_practice",
                                label = StudyQuestStrings.t(uiLanguage, "Quick Practice", "দ্রুত অনুশীলন"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "6-question drill", "৬ প্রশ্নের চর্চা"),
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
                                accent = SlateBotanical,
                                onClick = { pushDest(LearnDestination.FlashcardsAndVault) }
                            ),
                            LearnToolShortcut(
                                tag = "open_revision_planner",
                                label = StudyQuestStrings.t(uiLanguage, "Revision & Planner", "রিভিশন ও প্ল্যানার"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Spaced review", "স্মার্ট রিভিশন"),
                                icon = Icons.Filled.Replay,
                                accent = CeruleanSlate,
                                onClick = { pushDest(LearnDestination.RevisionAndPlanner) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_pomodoro",
                                label = StudyQuestStrings.t(uiLanguage, "Focus Timer", "ফোকাস টাইমার"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Timer & study audio", "ফোকাস ও অডিও"),
                                icon = Icons.Filled.Timer,
                                accent = BrandTealPrimary,
                                onClick = { pushDest(LearnDestination.PomodoroFocus) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_daily_challenge",
                                label = StudyQuestStrings.t(uiLanguage, "Daily Quiz", "দৈনিক কুইজ"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Daily check-in set", "দৈনিক মূল্যায়ন"),
                                icon = Icons.Filled.CheckCircle,
                                accent = MineralTeal,
                                onClick = { pushDest(LearnDestination.DailyChallenge) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_mastery_analytics",
                                label = StudyQuestStrings.t(uiLanguage, "Mastery Analytics", "দক্ষতা বিশ্লেষণ"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Accuracy & trends", "সঠিকতা ও ধারা"),
                                icon = Icons.Filled.Analytics,
                                accent = SlateBotanical,
                                onClick = { pushDest(LearnDestination.MasteryAnalytics) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_ai_lab",
                                label = StudyQuestStrings.t(uiLanguage, "AI Study Lab", "AI স্টাডি ল্যাব"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Interactive AI tutor", "বিস্তারিত AI টিউটর"),
                                icon = Icons.Filled.AutoAwesome,
                                accent = CeruleanSlate,
                                onClick = { pushDest(LearnDestination.AiLab) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_quick_doubt",
                                label = StudyQuestStrings.t(uiLanguage, "Quick Doubt", "দ্রুত সমাধান"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Instant Q&A help", "তাৎক্ষণিক প্রশ্নোত্তর"),
                                icon = Icons.Filled.AutoAwesome,
                                accent = BrandTealPrimary,
                                onClick = {
                                    aiSheetContext = "" to StudyQuestStrings.t(uiLanguage, "General Study", "সাধারণ পড়াশোনা")
                                }
                            ),
                            LearnToolShortcut(
                                tag = "learn_assignments_goals",
                                label = StudyQuestStrings.t(uiLanguage, "Goals & Tasks", "লক্ষ্য ও কাজ"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Track weekly plans", "সাপ্তাহিক লক্ষ্য"),
                                icon = Icons.Filled.Assignment,
                                accent = MineralTeal,
                                onClick = { pushDest(LearnDestination.AssignmentsAndGoals) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_ai_video_studio",
                                label = StudyQuestStrings.t(uiLanguage, "Video Library", "ভিডিও লাইব্রেরি"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Concept explainers", "ধারণা ভিডিও"),
                                icon = Icons.Filled.VideoLibrary,
                                accent = SlateBotanical,
                                onClick = { pushDest(LearnDestination.AiVideoStudio) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_stem_lab",
                                label = StudyQuestStrings.t(uiLanguage, "STEM Lab", "STEM ল্যাব"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Interactive offline sims", "অফলাইন সিমুলেশন"),
                                icon = Icons.Filled.Science,
                                accent = MineralTeal,
                                onClick = {
                                    val defaultLab = when (activeLockedBook?.subjectId) {
                                        "physics" -> "physics"
                                        "chemistry" -> "chem"
                                        "biology", "gen_science" -> "bio"
                                        else -> "trig"
                                    }
                                    pushDest(LearnDestination.InteractiveStemLab(defaultLab))
                                }
                            ),
                            LearnToolShortcut(
                                tag = "learn_downloads_center",
                                label = StudyQuestStrings.t(uiLanguage, "Downloads Hub", "ডাউনলোড হাব"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Books, audio & labs", "বই, অডিও ও ল্যাব প্যাক"),
                                icon = Icons.Filled.DownloadForOffline,
                                accent = BrandTealPrimary,
                                onClick = { pushDest(LearnDestination.OfflineDownloads) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_formula_atlas",
                                label = StudyQuestStrings.t(uiLanguage, "Formula Atlas", "সূত্র অ্যাটলাস"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Equations & constants", "সূত্র ও ধ্রুবক"),
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                accent = MineralTeal,
                                onClick = { pushDest(LearnDestination.FormulaAtlas) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_mistake_bank",
                                label = StudyQuestStrings.t(uiLanguage, "Mistake Bank", "ভুলের খাতা"),
                                subtitle = StudyQuestStrings.t(
                                    uiLanguage,
                                    "${unresolvedMistakes.size} items to fix",
                                    "${StudyQuestStrings.formatDigits(unresolvedMistakes.size, uiLanguage)}টি সংশোধন"
                                ),
                                icon = Icons.Filled.Build,
                                accent = CeruleanSlate,
                                onClick = { pushDest(LearnDestination.MistakeNotebook) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_exam_countdown",
                                label = StudyQuestStrings.t(uiLanguage, "Exam Timer", "পরীক্ষা লক্ষ্য"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Countdowns & heatmap", "কাউন্টডাউন ও হিটম্যাপ"),
                                icon = Icons.Filled.Event,
                                accent = SlateBotanical,
                                onClick = { pushDest(LearnDestination.ExamCountdown) }
                            ),
                            LearnToolShortcut(
                                tag = "learn_audio_briefs",
                                label = StudyQuestStrings.t(uiLanguage, "Audio Briefs", "অডিও ব্রিফ"),
                                subtitle = StudyQuestStrings.t(uiLanguage, "Studio WAV player", "অফলাইন অডিও"),
                                icon = Icons.Filled.Headphones,
                                accent = BrandTealPrimary,
                                onClick = { pushDest(LearnDestination.PocketAudioRevision) }
                            )
                        )

                        Column(
                            modifier = Modifier
                                .widthIn(max = 980.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = { pushDest(LearnDestination.FlashcardsAndVault) },
                                shape = RoundedCornerShape(16.dp),
                                color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                border = studyQuestGlassAwareBorder(),
                                shadowElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    GeometricStudyArtwork(modifier = Modifier.size(56.dp))
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = StudyQuestStrings.t(uiLanguage, "Ready to learn?", "শেখার জন্য প্রস্তুত?"),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = StudyQuestStrings.t(
                                                uiLanguage,
                                                "Flip active-recall decks, drills & smart AI notes to unlock scholar badges.",
                                                "ফ্ল্যাশকার্ড, অনুশীলন ও AI নোটের মাধ্যমে ব্যাজ অর্জন করুন।"
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = StudyQuestStrings.t(uiLanguage, "Start", "শুরু"),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            EditorialSectionHeader(
                                eyebrow = StudyQuestStrings.t(uiLanguage, "ORGANIZED COLLECTIONS", "শেখার সংকলন"),
                                title = StudyQuestStrings.t(uiLanguage, "Study Tools & Collections", "অধ্যয়ন সহায়িকা")
                            )
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("learn_tools_shelf")
                            ) {
                                val cols = if (maxWidth >= 760.dp) 3 else 2
                                val toolRows = toolShortcuts.chunked(cols)
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    toolRows.forEach { rowTools ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowTools.forEach { tool ->
                                                Surface(
                                                    onClick = tool.onClick,
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                                                    border = studyQuestGlassAwareBorder(),
                                                    shadowElevation = 0.dp,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .minimumInteractiveComponentSize()
                                                        .testTag(tool.tag)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(14.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                                            modifier = Modifier.size(38.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(19.dp))
                                                            }
                                                        }
                                                        Column(modifier = Modifier.weight(1f)) {
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
                                            repeat(cols - rowTools.size) {
                                                Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Timed Mock Exams & Promotion Assessment Shelf
                item {
                    StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
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
                                "Required retake (60%+ passes & advances your class)."
                            } else {
                                "Pass 60%+ to advance in the annual academic cycle."
                            },
                            descriptionBn = "বার্ষিক উত্তীর্ণ হতে ৬০%+ নম্বর অর্জন করুন।"
                        )

                        Column(
                            modifier = Modifier
                                .widthIn(max = 980.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (profile.annualExamStatus == "failed") {
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                                        } else {
                                            studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface)
                                        },
                                        border = studyQuestGlassAwareBorder(),
                                        shadowElevation = 0.dp,
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
                                                Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                                                Text(
                                                    text = StudyQuestStrings.t(uiLanguage, "PROMOTION EXAM", "প্রোমোশন পরীক্ষা"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
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
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
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
            val ctx = LocalContext.current
            val subjectPlate by produceState<Bitmap?>(initialValue = null, key1 = subject.id) {
                value = OfflineVaultManager.loadSubjectPlate(ctx, subject.id, sampleSize = 2)
            }
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
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        if (subject.id in listOf("math", "physics", "chemistry", "biology", "gen_science")) {
                            FilledTonalButton(
                                onClick = {
                                    val labId = when (subject.id) {
                                        "physics" -> "physics"
                                        "chemistry" -> "chem"
                                        "biology", "gen_science" -> "bio"
                                        else -> "trig"
                                    }
                                    pushDest(LearnDestination.InteractiveStemLab(labId))
                                },
                                modifier = Modifier.testTag("subject_open_stem_lab_${subject.id}")
                            ) {
                                Icon(Icons.Filled.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Lab", "ল্যাব"))
                            }
                        }
                    }
                }

                if (subjectPlate != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                bitmap = subjectPlate!!.asImageBitmap(),
                                contentDescription = StudyQuestStrings.t(
                                    uiLanguage,
                                    "${subject.titleEn} Concept Diagram Plate",
                                    "${subject.titleBn} ধারণা ডায়াগ্রাম"
                                ),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(172.dp)
                            )
                        }
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
                                        tint = BrandTealPrimary
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
                                                tint = if (completed) EditorialSuccess else BrandTealPrimary,
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
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
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
                onSwitchBook = { showSwitchBookSheet = true },
                onOpenStemLab = { labId -> pushDest(LearnDestination.InteractiveStemLab(labId)) }
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
                onBack = { popDest() },
                isOnline = isOnline
            )
        }

        is LearnDestination.InteractiveStemLab -> {
            InteractiveStemLabScreen(
                uiLanguage = uiLanguage,
                initialLabId = dest.initialLabId,
                onBack = { popDest() }
            )
        }

        LearnDestination.OfflineDownloads -> {
            OfflineDownloadsScreen(
                uiLanguage = uiLanguage,
                isOnline = isOnline,
                classId = selectedClassId,
                packs = downloadPacks,
                onSeedCatalog = onSeedDownloads,
                onDownloadPack = onDownloadPack,
                onDownloadClassBundle = onDownloadClassBundle,
                onRemovePack = onRemoveDownloadPack,
                onOpenPack = { pack ->
                    when (pack.category) {
                        DownloadPackCategory.TEXTBOOK -> {
                            val matchBook = availableBooksForLock.firstOrNull { it.subjectId == pack.subjectId }
                            if (matchBook != null) onSwitchActiveBook(matchBook)
                            pushDest(LearnDestination.TextbookReader)
                        }
                        DownloadPackCategory.AUDIO_BRIEFING -> pushDest(LearnDestination.PocketAudioRevision)
                        DownloadPackCategory.FOCUS_SOUNDSCAPE -> pushDest(LearnDestination.PomodoroFocus)
                        DownloadPackCategory.STEM_LAB_PACK -> {
                            val labId = when (pack.subjectId) {
                                "physics" -> "physics"
                                "chemistry" -> "chem"
                                "biology", "gen_science" -> "bio"
                                else -> "trig"
                            }
                            pushDest(LearnDestination.InteractiveStemLab(labId))
                        }
                        else -> pushDest(LearnDestination.TextbookReader)
                    }
                },
                onBack = { popDest() }
            )
        }

        LearnDestination.FormulaAtlas -> {
            FormulaAtlasScreen(
                uiLanguage = uiLanguage,
                bookmarks = formulaBookmarks,
                onToggleBookmark = onToggleFormulaBookmark,
                onBack = { popDest() }
            )
        }

        LearnDestination.MistakeNotebook -> {
            MistakeNotebookScreen(
                uiLanguage = uiLanguage,
                mistakes = unresolvedMistakes,
                allQuestions = allQuestions,
                onStartRepairDrill = { qs ->
                    pushDest(
                        LearnDestination.PracticeSession(
                            StudyQuestStrings.t(uiLanguage, "Mistake Repair Drill", "ভুল সংশোধন অনুশীলন"),
                            qs
                        )
                    )
                },
                onResolveMistake = onResolveMistake,
                onBack = { popDest() }
            )
        }

        LearnDestination.ExamCountdown -> {
            ExamCountdownAndHeatmapScreen(
                uiLanguage = uiLanguage,
                countdowns = examCountdowns,
                learningRecords = learningRecords,
                soloMatches = soloMatches,
                onAddExam = onAddExamCountdown,
                onIncrementTopic = onIncrementExamTopic,
                onDeleteExam = onDeleteExamCountdown,
                onBack = { popDest() }
            )
        }

        LearnDestination.PocketAudioRevision -> {
            PocketAudioRevisionScreen(
                uiLanguage = uiLanguage,
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
            },
            isOnline = isOnline
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
                        accentColor = if (selected) BrandTealPrimary else SlateBotanical,
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
                        if (isCompleted) StudyQuestStrings.t(uiLanguage, "Completed", "সম্পন্ন হয়েছে")
                        else StudyQuestStrings.t(uiLanguage, "Mark Lesson Complete", "পাঠ সম্পন্ন করুন")
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
