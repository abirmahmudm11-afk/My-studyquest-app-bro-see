package com.example.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.core.StudyActivityMonitor
import com.example.core.unwrapFirestoreMessage
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.BookChapterEntity
import com.example.data.local.BookPageNoteEntity
import com.example.data.local.CachedBookEntity
import com.example.data.local.CachedLessonEntity
import com.example.data.local.LocalBookmarkEntity
import com.example.data.local.LocalRevisionEntity
import com.example.data.model.ExamTemplate
import com.example.data.model.LearningRecordDoc
import com.example.data.model.PublicProfile
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.StudyLesson
import com.example.data.model.StudyPlanDoc
import com.example.data.model.TextbookCatalogItem
import com.example.data.repository.AcademicProgressRepository
import com.example.data.repository.AiDoubtSolverRepository
import com.example.data.repository.AuthAndProfileRepository
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.GuestDataRepository
import com.example.data.repository.SocialRepository
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.TextbookDownloadRepository
import com.example.data.repository.WeakTopicStat
import kotlin.random.Random
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.model.AiChatMessageDoc
import com.example.data.model.AiConversationDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.repository.StudyQuestAiToolCatalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

sealed interface AiDoubtUiState {
    data object Idle : AiDoubtUiState
    data object Loading : AiDoubtUiState
    data class Streaming(val text: String, val modelUsed: String = "") : AiDoubtUiState
    data class Success(val text: String, val modelUsed: String = "") : AiDoubtUiState
    data class Error(val message: String, val lastPrompt: String? = null) : AiDoubtUiState
}

data class AssessmentResultSummary(
    val title: String,
    val isExam: Boolean,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val score: Double,
    val totalMarks: Double,
    val negativeDeduction: Double,
    val xpEarned: Int,
    val includesCqSelfAssessment: Boolean
)

class LearnAndExamViewModel(
    private val currentUserId: String,
    private val academicRepo: AcademicProgressRepository,
    private val aiRepo: AiDoubtSolverRepository,
    private val textbookRepo: TextbookDownloadRepository? = null,
    private val socialRepo: SocialRepository? = null
) : ViewModel() {

    val learningRecords: StateFlow<List<LearningRecordDoc>> = academicRepo.observeLearningRecords(currentUserId)
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            Log.w(TAG, "Learning records stream error", fsErr ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val studyPlans: StateFlow<List<StudyPlanDoc>> = academicRepo.observeStudyPlans(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val mergedQuestions: StateFlow<List<QuestionItem>> = if (currentUserId == GuestDataRepository.GUEST_UID) {
        flowOf(BangladeshCurriculumCatalog.questions)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), BangladeshCurriculumCatalog.questions)
    } else {
        academicRepo.observePublishedCloudContent()
            .map { academicRepo.mergeQuestionBank(it) }
            .catch { emit(BangladeshCurriculumCatalog.questions) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), BangladeshCurriculumCatalog.questions)
    }

    val cachedLessons: StateFlow<List<CachedLessonEntity>> = academicRepo.observeCachedLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cloudTextbooks: StateFlow<List<TextbookCatalogItem>> = (textbookRepo?.observeCloudTextbookCatalog()
        ?: flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cachedBooks: StateFlow<List<CachedBookEntity>> = (textbookRepo?.observeCachedBooks()
        ?: flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun observeBookChapters(bookId: String): kotlinx.coroutines.flow.Flow<List<BookChapterEntity>> =
        textbookRepo?.observeBookChapters(bookId) ?: flowOf(emptyList())

    fun observeBookNotes(bookId: String): kotlinx.coroutines.flow.Flow<List<BookPageNoteEntity>> =
        textbookRepo?.observeBookNotes(bookId) ?: flowOf(emptyList())

    fun questionsForActiveBook(allQuestions: List<QuestionItem>, activeBook: TextbookCatalogItem): List<QuestionItem> =
        TextbookCatalog.questionsForActiveBook(allQuestions, activeBook)

    val bookmarks: StateFlow<List<LocalBookmarkEntity>> = academicRepo.observeBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val revisions: StateFlow<List<LocalRevisionEntity>> = academicRepo.observeRevisions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val pendingSyncCount: StateFlow<Int> = academicRepo.observePendingSyncCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    val weakTopics: StateFlow<List<WeakTopicStat>> = learningRecords
        .map { academicRepo.computeWeakTopics(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val subjectAnalytics: StateFlow<List<SubjectAnalyticsSummary>> = learningRecords
        .map { academicRepo.computeSubjectAnalytics(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val dailyStudySeries: StateFlow<List<DailyStudyMetric>> = learningRecords
        .map { academicRepo.computeDailyStudySeries(it, emptyList(), daysCount = 7) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _aiState = MutableStateFlow<AiDoubtUiState>(AiDoubtUiState.Idle)
    val aiState: StateFlow<AiDoubtUiState> = _aiState.asStateFlow()

    val aiConversations: StateFlow<List<AiConversationDoc>> = if (currentUserId == GuestDataRepository.GUEST_UID) {
        flowOf(emptyList())
    } else {
        aiRepo.getAIConversations(currentUserId).catch { emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeAiMessages: StateFlow<List<AiChatMessageDoc>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId.isNullOrBlank() || currentUserId == GuestDataRepository.GUEST_UID) {
                flowOf(emptyList())
            } else {
                aiRepo.getAIConversationHistory(currentUserId, convId).catch { emit(emptyList()) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _activeToolId = MutableStateFlow("QuestTutor")
    val activeToolId: StateFlow<String> = _activeToolId.asStateFlow()

    private var aiStreamJob: Job? = null
    private var lastRetryParams: TupleAiRetry? = null

    private data class TupleAiRetry(
        val conversationId: String,
        val toolId: String,
        val prompt: String,
        val classId: String,
        val subjectName: String,
        val language: AppLanguage,
        val subscription: UserSubscriptionDoc,
        val activeBookTitle: String = ""
    )

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _lastAssessmentResult = MutableStateFlow<AssessmentResultSummary?>(null)
    val lastAssessmentResult: StateFlow<AssessmentResultSummary?> = _lastAssessmentResult.asStateFlow()

    private val _examRemainingSeconds = MutableStateFlow(0)
    val examRemainingSeconds: StateFlow<Int> = _examRemainingSeconds.asStateFlow()
    private var examTimerJob: Job? = null

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun clearAssessmentResult() {
        _lastAssessmentResult.value = null
    }

    fun resetAiDoubtState() {
        aiStreamJob?.cancel()
        _aiState.value = AiDoubtUiState.Idle
    }

    fun selectAiTool(toolId: String) {
        _activeToolId.value = StudyQuestAiToolCatalog.byId(toolId).id
    }

    fun openAIConversation(conversationId: String?) {
        _activeConversationId.value = conversationId
    }

    fun createAIConversation(
        title: String,
        toolId: String,
        classId: String,
        subjectId: String
    ) {
        viewModelScope.launch {
            val res = aiRepo.createAIConversation(
                userId = currentUserId,
                title = title,
                toolId = toolId,
                providerMode = "studyquest_ai",
                classId = classId,
                subjectId = subjectId
            )
            res.onSuccess { conv ->
                _activeConversationId.value = conv.id
                _activeToolId.value = toolId
            }.onFailure { err ->
                _statusBanner.value = unwrapFirestoreMessage(err, "Could not start chat")
            }
        }
    }

    fun deleteAIConversation(conversationId: String) {
        viewModelScope.launch {
            if (_activeConversationId.value == conversationId) {
                _activeConversationId.value = null
            }
            aiRepo.deleteAIConversation(currentUserId, conversationId)
        }
    }

    fun cancelAIRequest() {
        aiStreamJob?.cancel()
        aiStreamJob = null
        _aiState.value = AiDoubtUiState.Idle
    }

    fun downloadChapterOffline(chapterId: String, bookId: String = "") {
        viewModelScope.launch {
            val res = academicRepo.downloadChapterForOffline(chapterId, bookId = bookId)
            res.onSuccess { count ->
                _statusBanner.value = "Downloaded $count items for offline study"
            }
            res.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Download failed"
            }
        }
    }

    fun installBookOffline(book: TextbookCatalogItem, language: AppLanguage, forceRedownload: Boolean = false) {
        val repo = textbookRepo ?: return
        viewModelScope.launch {
            val res = repo.ensureBookInstalledOffline(book, forceRedownloadPdf = forceRedownload, language = language)
            res.onSuccess {
                _statusBanner.value = if (language == AppLanguage.BANGLA) {
                    "${book.titleBn} অফলাইনে সংরক্ষিত হয়েছে (${it.totalPages} পৃষ্ঠা)"
                } else {
                    "${book.titleEn} installed offline (${it.totalPages} pages)"
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Offline install failed"
            }
        }
    }

    fun importPdfForBook(book: TextbookCatalogItem, uri: Uri, language: AppLanguage) {
        val repo = textbookRepo ?: return
        viewModelScope.launch {
            val res = repo.importLocalPdfForBook(book, uri)
            res.onSuccess {
                _statusBanner.value = if (language == AppLanguage.BANGLA) {
                    "অফিসিয়াল PDF সংযুক্ত ও অফলাইনে সেভ হয়েছে (${it.totalPages} পৃষ্ঠা)"
                } else {
                    "PDF attached & saved offline (${it.totalPages} pages)"
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Unable to import PDF"
            }
        }
    }

    fun deleteOfflineBookCache(bookId: String) {
        viewModelScope.launch {
            textbookRepo?.deleteOfflineBookCache(bookId)
            _statusBanner.value = "Removed offline textbook cache"
        }
    }

    fun saveLastReadPage(bookId: String, pageIndex: Int) {
        viewModelScope.launch {
            textbookRepo?.saveLastReadPage(bookId, pageIndex)
        }
    }

    fun markBookChapterCompleted(chapterEntityId: String) {
        viewModelScope.launch {
            textbookRepo?.markChapterCompleted(chapterEntityId)
        }
    }

    fun saveBookPageNote(
        bookId: String,
        pageNumber: Int,
        chapterId: String?,
        title: String,
        noteText: String,
        isBookmark: Boolean
    ) {
        viewModelScope.launch {
            textbookRepo?.savePageNote(bookId, pageNumber, chapterId, title, noteText, isBookmark)
            _statusBanner.value = if (isBookmark && noteText.isBlank()) "Page ${pageNumber + 1} bookmarked" else "Note saved for page ${pageNumber + 1}"
        }
    }

    fun deleteBookPageNote(noteId: String) {
        viewModelScope.launch {
            textbookRepo?.deletePageNote(noteId)
        }
    }

    fun reportQuestionOrBook(targetType: String, targetId: String, reason: String, details: String) {
        if (currentUserId == GuestDataRepository.GUEST_UID) {
            _statusBanner.value = "Sign in to submit moderation reports to StudyQuest reviewers."
            return
        }
        val repo = socialRepo ?: return
        viewModelScope.launch {
            val res = repo.reportIssue(currentUserId, targetType, targetId, reason, details)
            res.onSuccess {
                _statusBanner.value = "Report submitted for academic review"
            }.onFailure {
                _statusBanner.value = unwrapFirestoreMessage(it, "Report failed")
            }
        }
    }

    fun completeLesson(lesson: StudyLesson, isOnline: Boolean, bookId: String = "", onXpEarned: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val res = academicRepo.recordLessonCompleted(currentUserId, lesson, isOnline, bookId = bookId)
            res.onSuccess { xp ->
                if (xp > 0) onXpEarned(xp)
            }
            res.onFailure {
                _statusBanner.value = unwrapFirestoreMessage(it, "Could not save lesson")
            }
        }
    }

    fun toggleBookmark(question: QuestionItem, isMistake: Boolean = false, bookId: String = "") {
        viewModelScope.launch {
            val existing = bookmarks.value.any { it.questionId == question.id }
            if (existing) {
                academicRepo.removeBookmark(question.id)
            } else {
                academicRepo.toggleBookmark(question, isMistake = isMistake, bookId = bookId)
            }
        }
    }

    fun addToRevision(question: QuestionItem) {
        viewModelScope.launch {
            academicRepo.addToRevisionQueue(question, markWrong = false)
            _statusBanner.value = "Added to Spaced Repetition queue"
        }
    }

    fun reviewRevisionItem(itemId: String, quality: Int) {
        viewModelScope.launch {
            academicRepo.reviewSpacedRepetitionItem(itemId, quality)
        }
    }

    fun setExamOrQuizActive(active: Boolean) {
        StudyActivityMonitor.isExamOrQuizActive.value = active
    }

    fun startExamTimer(durationSeconds: Int, onTimeExpired: () -> Unit) {
        examTimerJob?.cancel()
        StudyActivityMonitor.isExamOrQuizActive.value = true
        _examRemainingSeconds.value = durationSeconds
        examTimerJob = viewModelScope.launch {
            while (_examRemainingSeconds.value > 0) {
                delay(1000L)
                _examRemainingSeconds.value = _examRemainingSeconds.value - 1
            }
            onTimeExpired()
        }
    }

    fun stopExamTimer() {
        examTimerJob?.cancel()
        examTimerJob = null
        StudyActivityMonitor.isExamOrQuizActive.value = false
    }

    fun submitPracticeOrExam(
        title: String,
        questions: List<QuestionItem>,
        selectedIndicesByQuestion: Map<String, Set<Int>>,
        textAnswersByQuestion: Map<String, String>,
        cqSelfScoresByQuestion: Map<String, Double>,
        template: ExamTemplate?,
        isOnline: Boolean,
        bookId: String = "",
        onXpEarned: (Int) -> Unit = {}
    ) {
        stopExamTimer()
        viewModelScope.launch {
            var correctCount = 0
            var wrongCount = 0
            var skippedCount = 0
            var earnedMarks = 0.0
            var totalMarks = 0.0
            var hasCq = false

            for (q in questions) {
                totalMarks += q.marks.toDouble()
                when (q.type) {
                    QuestionType.SINGLE_MCQ, QuestionType.TRUE_FALSE, QuestionType.MULTI_MCQ -> {
                        val picked = selectedIndicesByQuestion[q.id] ?: emptySet()
                        if (picked.isEmpty()) {
                            skippedCount++
                        } else if (picked == q.correctOptionIndices.toSet()) {
                            correctCount++
                            earnedMarks += q.marks.toDouble()
                        } else {
                            wrongCount++
                            academicRepo.addToRevisionQueue(q, markWrong = true)
                            academicRepo.toggleBookmark(q, isMistake = true)
                        }
                    }
                    QuestionType.CREATIVE_CQ -> {
                        hasCq = true
                        val selfScore = cqSelfScoresByQuestion[q.id]
                        if (selfScore == null) {
                            skippedCount++
                        } else {
                            earnedMarks += selfScore.coerceIn(0.0, q.marks.toDouble())
                            if (selfScore >= q.marks * 0.5) correctCount++ else wrongCount++
                        }
                    }
                    QuestionType.FLASHCARD -> {
                        val flipped = selectedIndicesByQuestion[q.id]?.isNotEmpty() == true
                        if (flipped) {
                            correctCount++
                            earnedMarks += q.marks.toDouble()
                        } else skippedCount++
                    }
                    else -> {
                        val txt = textAnswersByQuestion[q.id]?.trim().orEmpty()
                        if (txt.isBlank()) {
                            skippedCount++
                        } else if (q.acceptableTextAnswers.any { it.equals(txt, ignoreCase = true) } ||
                            q.correctOptionIndices.isEmpty() && txt.length >= 2
                        ) {
                            correctCount++
                            earnedMarks += q.marks.toDouble()
                        } else {
                            wrongCount++
                            academicRepo.addToRevisionQueue(q, markWrong = true)
                        }
                    }
                }
            }

            val negRate = template?.negativeMarkingPerWrong ?: 0.0
            val negDeduction = wrongCount * negRate
            val finalScore = (earnedMarks - negDeduction).coerceAtLeast(0.0)
            val firstQ = questions.firstOrNull()
            val subjectId = firstQ?.subjectId ?: template?.subjectIds?.firstOrNull() ?: "math"

            val res = academicRepo.recordPracticeOrExamSession(
                userId = currentUserId,
                recordType = if (template != null) "exam" else "practice",
                refId = template?.id ?: "practice_${subjectId}_${questions.size}",
                subjectId = subjectId,
                chapterId = firstQ?.chapterId,
                topicId = firstQ?.topicId,
                correctCount = correctCount,
                totalQuestions = questions.size,
                score = finalScore,
                totalMarks = totalMarks.coerceAtLeast(1.0),
                summary = title,
                isOnline = isOnline,
                isPromotionExam = (template?.id?.startsWith("promotion_") == true),
                bookId = bookId.ifBlank { firstQ?.bookId.orEmpty() }
            )
            val xpEarned = res.getOrNull() ?: 0
            if (xpEarned > 0) onXpEarned(xpEarned)

            _lastAssessmentResult.value = AssessmentResultSummary(
                title = title,
                isExam = template != null,
                correctCount = correctCount,
                wrongCount = wrongCount,
                skippedCount = skippedCount,
                score = finalScore,
                totalMarks = totalMarks,
                negativeDeduction = negDeduction,
                xpEarned = xpEarned,
                includesCqSelfAssessment = hasCq
            )
        }
    }

    fun todayChallengeDateKey(): String =
        AuthAndProfileRepository.currentDailyChallengeDateKey()

    fun isDailyChallengeDoneToday(profile: PublicProfile): Boolean =
        profile.lastChallengeDateKey == todayChallengeDateKey()

    fun dailyChallengeQuestions(allQuestions: List<QuestionItem>, classId: String): List<QuestionItem> {
        val gradable = allQuestions.filter {
            it.type in listOf(
                QuestionType.SINGLE_MCQ,
                QuestionType.TRUE_FALSE,
                QuestionType.MULTI_MCQ,
                QuestionType.NUMERICAL,
                QuestionType.FILL_BLANK,
                QuestionType.MATCHING
            )
        }
        val pool = gradable.filter { it.classId == classId }.ifEmpty { gradable }
        val seed = todayChallengeDateKey().hashCode().toLong()
        return pool.shuffled(Random(seed)).take(5)
    }

    fun completePomodoroSession(
        subjectId: String,
        focusedMinutes: Int,
        isOnline: Boolean,
        bookId: String = "",
        onXpEarned: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = academicRepo.recordPomodoroSession(currentUserId, subjectId, focusedMinutes, isOnline, bookId = bookId)
            res.onSuccess { xp ->
                if (xp > 0) {
                    _statusBanner.value = "Focus session saved! +$xp XP earned"
                    onXpEarned(xp)
                }
            }
            res.onFailure {
                _statusBanner.value = unwrapFirestoreMessage(it, "Could not save focus session")
            }
        }
    }

    fun recordFlashcardSession(
        subjectId: String,
        reviewedCount: Int,
        masteredCount: Int,
        isOnline: Boolean,
        bookId: String = "",
        onXpEarned: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = academicRepo.recordFlashcardDeckSession(
                userId = currentUserId,
                subjectId = subjectId,
                reviewedCount = reviewedCount,
                masteredCount = masteredCount,
                isOnline = isOnline,
                bookId = bookId
            )
            res.onSuccess { xp ->
                if (xp > 0) {
                    _statusBanner.value = "Flashcards completed! +$xp XP earned"
                    onXpEarned(xp)
                }
            }
            res.onFailure {
                _statusBanner.value = unwrapFirestoreMessage(it, "Could not save flashcard progress")
            }
        }
    }

    fun completeDailyChallenge(
        questions: List<QuestionItem>,
        selectedIndicesByQuestion: Map<String, Set<Int>>,
        textAnswersByQuestion: Map<String, String>,
        isOnline: Boolean,
        bookId: String = "",
        onXpEarned: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            var correctCount = 0
            var wrongCount = 0
            var skippedCount = 0
            var earnedMarks = 0.0
            var totalMarks = 0.0

            for (q in questions) {
                totalMarks += q.marks.toDouble()
                when (q.type) {
                    QuestionType.SINGLE_MCQ, QuestionType.TRUE_FALSE, QuestionType.MULTI_MCQ, QuestionType.MATCHING -> {
                        val picked = selectedIndicesByQuestion[q.id] ?: emptySet()
                        if (picked.isEmpty()) {
                            skippedCount++
                        } else if (picked == q.correctOptionIndices.toSet()) {
                            correctCount++
                            earnedMarks += q.marks.toDouble()
                        } else {
                            wrongCount++
                            academicRepo.addToRevisionQueue(q, markWrong = true)
                            academicRepo.toggleBookmark(q, isMistake = true)
                        }
                    }
                    else -> {
                        val txt = textAnswersByQuestion[q.id]?.trim().orEmpty()
                        if (txt.isBlank()) {
                            skippedCount++
                        } else if (q.acceptableTextAnswers.any { it.equals(txt, ignoreCase = true) }) {
                            correctCount++
                            earnedMarks += q.marks.toDouble()
                        } else {
                            wrongCount++
                            academicRepo.addToRevisionQueue(q, markWrong = true)
                        }
                    }
                }
            }

            val firstSubject = questions.firstOrNull()?.subjectId ?: "math"
            val dateKey = todayChallengeDateKey()
            val res = academicRepo.completeDailyChallenge(
                userId = currentUserId,
                subjectId = firstSubject,
                correctCount = correctCount,
                totalQuestions = questions.size,
                score = earnedMarks,
                totalMarks = totalMarks.coerceAtLeast(1.0),
                summary = "Daily Challenge ($dateKey): $correctCount/${questions.size}",
                isOnline = isOnline,
                bookId = bookId
            )
            val xpEarned = res.getOrNull() ?: 0
            if (xpEarned > 0) {
                _statusBanner.value = "Daily Streak updated! +$xpEarned XP"
                onXpEarned(xpEarned)
            }
            _lastAssessmentResult.value = AssessmentResultSummary(
                title = "Daily Challenge ($dateKey)",
                isExam = true,
                correctCount = correctCount,
                wrongCount = wrongCount,
                skippedCount = skippedCount,
                score = earnedMarks,
                totalMarks = totalMarks,
                negativeDeduction = 0.0,
                xpEarned = xpEarned,
                includesCqSelfAssessment = false
            )
        }
    }

    fun createStudyPlan(
        title: String,
        subjectId: String,
        planType: String,
        daysAhead: Int,
        minutesPerDay: Long,
        bookId: String = ""
    ) {
        viewModelScope.launch {
            val targetMs = System.currentTimeMillis() + daysAhead.coerceAtLeast(1) * 86_400_000L
            val res = academicRepo.createStudyPlan(
                userId = currentUserId,
                title = title,
                subjectId = subjectId,
                planType = planType,
                targetDateEpochMs = targetMs,
                minutesPerDay = minutesPerDay,
                bookId = bookId
            )
            res.onFailure { _statusBanner.value = unwrapFirestoreMessage(it, "Could not create plan") }
        }
    }

    fun toggleStudyPlan(plan: StudyPlanDoc) {
        viewModelScope.launch {
            academicRepo.toggleStudyPlanCompleted(currentUserId, plan)
        }
    }

    fun deleteStudyPlan(planId: String) {
        viewModelScope.launch {
            academicRepo.deleteStudyPlan(currentUserId, planId)
        }
    }

    fun sendStudyQuestMessage(
        userText: String,
        classId: String,
        subjectName: String,
        language: AppLanguage,
        subscription: UserSubscriptionDoc = UserSubscriptionDoc(),
        toolId: String = _activeToolId.value,
        activeBookTitle: String = ""
    ) {
        aiStreamJob?.cancel()
        aiStreamJob = viewModelScope.launch {
            _aiState.value = AiDoubtUiState.Loading
            var convId = _activeConversationId.value.orEmpty()
            if (convId.isBlank() && currentUserId != GuestDataRepository.GUEST_UID) {
                val created = aiRepo.createAIConversation(
                    userId = currentUserId,
                    title = userText.take(42),
                    toolId = toolId,
                    providerMode = "studyquest_ai",
                    classId = classId,
                    subjectId = subjectName
                )
                created.onSuccess {
                    convId = it.id
                    _activeConversationId.value = it.id
                }
            }
            lastRetryParams = TupleAiRetry(convId, toolId, userText, classId, subjectName, language, subscription, activeBookTitle)
            try {
                val enabled = aiRepo.isAiSolverEnabled()
                if (!enabled) {
                    _aiState.value = AiDoubtUiState.Error(
                        if (language == AppLanguage.BANGLA) "অ্যাডমিন দ্বারা StudyQuest Helper সাময়িকভাবে বন্ধ আছে।"
                        else "StudyQuest Helper is currently paused by StudyQuest Admins."
                    )
                    return@launch
                }
                var latest = ""
                var latestModel = ""
                aiRepo.sendStudyQuestMessage(
                    userId = currentUserId,
                    conversationId = convId,
                    toolId = toolId,
                    userText = userText,
                    classId = classId,
                    subjectName = subjectName,
                    language = language,
                    subscription = subscription,
                    history = activeAiMessages.value,
                    activeBookTitle = activeBookTitle
                ).collect { update ->
                    latest = update.text
                    latestModel = update.modelUsed
                    _aiState.value = AiDoubtUiState.Streaming(update.text, update.modelUsed)
                }
                if (latest.isNotBlank()) {
                    _aiState.value = AiDoubtUiState.Success(latest, latestModel)
                } else {
                    _aiState.value = AiDoubtUiState.Error("Empty response from StudyQuest Helper.", userText)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "StudyQuest Helper ViewModel error", e)
                _aiState.value = AiDoubtUiState.Error(e.localizedMessage ?: "AI tutor error.", userText)
            }
        }
    }

    fun retryFailedAIRequest() {
        val last = lastRetryParams ?: return
        sendStudyQuestMessage(
            userText = last.prompt,
            classId = last.classId,
            subjectName = last.subjectName,
            language = last.language,
            subscription = last.subscription,
            toolId = last.toolId,
            activeBookTitle = last.activeBookTitle
        )
    }

    fun askAiDoubtSolver(
        questionPrompt: String,
        studentQuestion: String,
        classId: String,
        subjectName: String,
        language: AppLanguage,
        subscription: UserSubscriptionDoc = UserSubscriptionDoc(),
        activeBookTitle: String = ""
    ) {
        aiStreamJob?.cancel()
        aiStreamJob = viewModelScope.launch {
            _aiState.value = AiDoubtUiState.Loading
            try {
                val enabled = aiRepo.isAiSolverEnabled()
                if (!enabled) {
                    _aiState.value = AiDoubtUiState.Error(
                        if (language == AppLanguage.BANGLA) "অ্যাডমিন দ্বারা AI Doubt Solver সাময়িকভাবে বন্ধ আছে।"
                        else "AI Doubt Solver is currently paused by StudyQuest Admins."
                    )
                    return@launch
                }
                var latest = ""
                aiRepo.streamDoubtExplanation(
                    questionPrompt = questionPrompt,
                    studentQuestion = studentQuestion,
                    classId = classId,
                    subjectName = subjectName,
                    language = language,
                    subscription = subscription,
                    activeBookTitle = activeBookTitle
                ).collect { chunk ->
                    latest = chunk
                    _aiState.value = AiDoubtUiState.Streaming(chunk)
                }
                if (latest.isNotBlank()) {
                    _aiState.value = AiDoubtUiState.Success(latest)
                } else {
                    _aiState.value = AiDoubtUiState.Error("Empty response from AI tutor.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Doubt Solver ViewModel error", e)
                _aiState.value = AiDoubtUiState.Error(e.localizedMessage ?: "AI tutor error.")
            }
        }
    }

    fun streamDirectGeminiDoubt(
        questionPrompt: String,
        studentQuestion: String,
        classId: String,
        subjectName: String,
        language: AppLanguage,
        activeBookTitle: String = ""
    ): kotlinx.coroutines.flow.Flow<String> = aiRepo.streamDirectGeminiDoubt(
        questionPrompt = questionPrompt,
        studentQuestion = studentQuestion,
        classId = classId,
        subjectName = subjectName,
        language = language,
        activeBookTitle = activeBookTitle
    )

    override fun onCleared() {
        super.onCleared()
        examTimerJob?.cancel()
        StudyActivityMonitor.isExamOrQuizActive.value = false
    }

    companion object {
        private const val TAG = "LearnExamVM"
    }
}
