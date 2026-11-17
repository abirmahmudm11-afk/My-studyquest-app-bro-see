package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.AppLanguage
import com.example.core.OperationType
import com.example.core.StudyQuestPreferences
import com.example.core.StudyQuestStrings
import com.example.core.handleFirestoreError
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.local.CachedLessonEntity
import com.example.data.local.CachedQuestionEntity
import com.example.data.local.LocalBookmarkEntity
import com.example.data.local.LocalRevisionEntity
import com.example.data.local.LocalStudyPlanEntity
import com.example.data.local.PendingSyncActionEntity
import com.example.data.local.StudyQuestDao
import com.example.data.local.StudyQuestWorkScheduler
import com.example.data.model.AcademicContentDoc
import com.example.data.model.LearningRecordDoc
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionProvenance
import com.example.data.model.QuestionType
import com.example.data.model.StudyLesson
import com.example.data.model.StudyPlanDoc
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.example.data.curriculum.GamificationCatalog
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

data class WeakTopicStat(
    val subjectId: String,
    val topicId: String,
    val topicLabelEn: String,
    val topicLabelBn: String,
    val attempts: Int,
    val averageAccuracy: Double
)

data class SubjectAnalyticsSummary(
    val subjectId: String,
    val lessonsCompleted: Int,
    val practiceSessions: Int,
    val examSessions: Int,
    val totalXp: Long,
    val averageAccuracy: Double,
    val subjectLevel: Int
)

data class DailyStudyMetric(
    val dayKey: String,
    val shortLabel: String,
    val xpEarned: Long,
    val studyMinutes: Int,
    val examAccuracy: Double,
    val sessionsCount: Int,
    val lessonsCompleted: Int = 0,
    val examsCompleted: Int = 0
)

class AcademicProgressRepository(
    private val db: FirebaseFirestore,
    private val dao: StudyQuestDao,
    private val appContext: Context? = null,
    private val preferences: StudyQuestPreferences? = null,
    private val guestRepo: GuestDataRepository? = null,
    private val gamificationEngine: GamificationEngine? = null
) {
    constructor(
        context: Context,
        dao: StudyQuestDao,
        preferences: StudyQuestPreferences? = null,
        guestRepo: GuestDataRepository? = null,
        gamificationEngine: GamificationEngine? = null
    ) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        dao,
        context.applicationContext,
        preferences,
        guestRepo,
        gamificationEngine
    )

    private val resolvedEngine: GamificationEngine by lazy {
        gamificationEngine ?: GamificationEngine(db, dao, preferences)
    }

    fun observeLearningRecords(userId: String): Flow<List<LearningRecordDoc>> {
        if (userId == GuestDataRepository.GUEST_UID) {
            return dao.observeAllActionsForUser(GuestDataRepository.GUEST_UID).map { list ->
                list.map { item ->
                    LearningRecordDoc(
                        id = item.idempotencyKey,
                        userId = GuestDataRepository.GUEST_UID,
                        recordType = item.actionType,
                        refId = item.refId,
                        bookId = item.bookId.ifBlank { null },
                        subjectId = item.subjectId,
                        chapterId = item.chapterId.ifBlank { null },
                        topicId = item.topicId.ifBlank { null },
                        score = item.score,
                        totalMarks = item.totalMarks,
                        accuracy = item.accuracy,
                        xpAwarded = item.xpAwarded.toLong(),
                        idempotencyKey = item.idempotencyKey,
                        summary = item.summary,
                        createdAt = Timestamp(item.createdAtMs / 1000L, 0)
                    )
                }
            }
        }
        return db.collection("users").document(userId).collection("learning_records")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    LearningRecordDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
    }

    fun observeStudyPlans(userId: String): Flow<List<StudyPlanDoc>> {
        if (userId == GuestDataRepository.GUEST_UID) {
            return dao.observeStudyPlansForUser(GuestDataRepository.GUEST_UID).map { list ->
                list.map { plan ->
                    StudyPlanDoc(
                        id = plan.id,
                        userId = plan.userId,
                        title = plan.title,
                        bookId = plan.bookId.ifBlank { null },
                        subjectId = plan.subjectId,
                        planType = plan.planType,
                        targetDateEpochMs = plan.targetDateEpochMs,
                        minutesPerDay = plan.minutesPerDay,
                        completed = plan.completed
                    )
                }.sortedBy { it.targetDateEpochMs }
            }
        }
        return db.collection("users").document(userId).collection("study_plans")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    StudyPlanDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.targetDateEpochMs }
            }
    }

    fun observePublishedCloudContent(): Flow<List<AcademicContentDoc>> =
        db.collection("academic_content")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    AcademicContentDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    fun observeCachedLessons(): Flow<List<CachedLessonEntity>> = dao.observeCachedLessons()

    fun observeBookmarks(): Flow<List<LocalBookmarkEntity>> = dao.observeBookmarks()

    fun observeRevisions(): Flow<List<LocalRevisionEntity>> = dao.observeRevisions()

    fun observePendingSyncCount(): Flow<Int> =
        dao.observePendingSyncActions().map { it.size }

    suspend fun downloadChapterForOffline(chapterId: String, bookId: String = ""): Result<Int> {
        return try {
            val lessons = BangladeshCurriculumCatalog.lessons.filter { it.chapterId == chapterId }
            val questions = BangladeshCurriculumCatalog.questions.filter { it.chapterId == chapterId }
            dao.upsertLessons(lessons.map { lesson ->
                CachedLessonEntity(
                    id = lesson.id,
                    classId = lesson.classId,
                    subjectId = lesson.subjectId,
                    chapterId = lesson.chapterId,
                    bookId = bookId,
                    titleEn = lesson.titleEn,
                    titleBn = lesson.titleBn,
                    explanationEn = lesson.explanationEn,
                    explanationBn = lesson.explanationBn,
                    formulasCsv = lesson.formulas.joinToString("|"),
                    keyPointsEnCsv = lesson.keyPointsEn.joinToString("|"),
                    keyPointsBnCsv = lesson.keyPointsBn.joinToString("|")
                )
            })
            dao.upsertQuestions(questions.map { q ->
                CachedQuestionEntity(
                    id = q.id,
                    classId = q.classId,
                    subjectId = q.subjectId,
                    chapterId = q.chapterId,
                    topicId = q.topicId,
                    bookId = bookId.ifBlank { q.bookId },
                    questionType = q.type.id,
                    difficulty = q.difficulty.id,
                    promptEn = q.promptEn,
                    promptBn = q.promptBn,
                    optionsEnPipe = q.optionsEn.joinToString("|"),
                    optionsBnPipe = q.optionsBn.joinToString("|"),
                    correctIndicesCsv = q.correctOptionIndices.joinToString(","),
                    explanationEn = q.explanationEn,
                    explanationBn = q.explanationBn,
                    marks = q.marks,
                    provenance = q.provenance.id,
                    sourceAttribution = q.sourceAttribution
                )
            })
            Result.success(lessons.size + questions.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleBookmark(
        question: QuestionItem,
        isMistake: Boolean = false,
        note: String = "",
        bookId: String = ""
    ) {
        dao.upsertBookmark(
            LocalBookmarkEntity(
                questionId = question.id,
                bookId = bookId.ifBlank { question.bookId },
                subjectId = question.subjectId,
                chapterId = question.chapterId,
                topicId = question.topicId,
                isMistake = isMistake,
                note = note
            )
        )
    }

    suspend fun removeBookmark(questionId: String) {
        dao.deleteBookmark(questionId)
    }

    suspend fun addToRevisionQueue(question: QuestionItem, markWrong: Boolean = false, bookId: String = "") {
        val existing = dao.getRevision(question.id)
        if (existing == null) {
            dao.upsertRevision(
                LocalRevisionEntity(
                    itemId = question.id,
                    bookId = bookId.ifBlank { question.bookId },
                    subjectId = question.subjectId,
                    chapterId = question.chapterId,
                    topicId = question.topicId,
                    wrongCount = if (markWrong) 1 else 0,
                    nextDueAtMs = System.currentTimeMillis()
                )
            )
        } else if (markWrong) {
            dao.upsertRevision(
                existing.copy(
                    wrongCount = existing.wrongCount + 1,
                    intervalDays = 1,
                    repetitions = 0,
                    nextDueAtMs = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun reviewSpacedRepetitionItem(itemId: String, quality: Int) {
        // SM-2 spaced repetition update (quality 0..5, >= 3 is recall success)
        val existing = dao.getRevision(itemId) ?: return
        val q = quality.coerceIn(0, 5)
        val newEase = (existing.easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))).coerceAtLeast(1.3)
        val (newReps, newInterval) = if (q < 3) {
            0 to 1
        } else {
            val reps = existing.repetitions + 1
            val interval = when (reps) {
                1 -> 1
                2 -> 6
                else -> (existing.intervalDays * newEase).roundToInt().coerceAtLeast(2)
            }
            reps to interval
        }
        val nextDue = System.currentTimeMillis() + newInterval * 86_400_000L
        dao.upsertRevision(
            existing.copy(
                easeFactor = newEase,
                intervalDays = newInterval,
                repetitions = newReps,
                correctCount = if (q >= 3) existing.correctCount + 1 else existing.correctCount,
                wrongCount = if (q < 3) existing.wrongCount + 1 else existing.wrongCount,
                lastReviewedAtMs = System.currentTimeMillis(),
                nextDueAtMs = nextDue
            )
        )
    }

    suspend fun recordLessonCompleted(
        userId: String,
        lesson: StudyLesson,
        isOnline: Boolean,
        bookId: String = ""
    ): Result<Int> {
        val xp = 20
        val idempotencyKey = "lesson_${lesson.id}"
        dao.markLessonCompleted(lesson.id)
        return recordActivityWithIdempotency(
            userId = userId,
            recordType = "lesson",
            refId = lesson.id,
            bookId = bookId,
            subjectId = lesson.subjectId,
            chapterId = lesson.chapterId,
            topicId = lesson.topicId,
            score = 1.0,
            totalMarks = 1.0,
            accuracy = 1.0,
            xpAwarded = xp,
            idempotencyKey = idempotencyKey,
            summary = lesson.titleEn,
            isOnline = isOnline,
            qualifiesForStreak = true,
            lessonsDelta = 1L
        )
    }

    suspend fun recordPracticeOrExamSession(
        userId: String,
        recordType: String, // "practice" or "exam"
        refId: String,
        subjectId: String,
        chapterId: String?,
        topicId: String?,
        correctCount: Int,
        totalQuestions: Int,
        score: Double,
        totalMarks: Double,
        summary: String,
        isOnline: Boolean,
        isPromotionExam: Boolean = false,
        bookId: String = ""
    ): Result<Int> {
        val baseXp = if (recordType == "exam") 30 else 10
        val bonusXp = correctCount * 3
        val totalXp = (baseXp + bonusXp).coerceAtMost(250)
        val accuracy = if (totalQuestions > 0) (correctCount.toDouble() / totalQuestions.toDouble()).coerceIn(0.0, 1.0) else 0.0
        if (isPromotionExam && userId != GuestDataRepository.GUEST_UID) {
            val passed = accuracy >= 0.40
            resolvedEngine.recordAnnualPromotionExamOutcome(userId, passed, score, totalMarks)
            return Result.success(if (passed) 80 else 20)
        }
        val idempotencyKey = "${recordType}_${refId}_${System.currentTimeMillis()}"
        return recordActivityWithIdempotency(
            userId = userId,
            recordType = recordType,
            refId = refId,
            bookId = bookId,
            subjectId = subjectId,
            chapterId = chapterId,
            topicId = topicId,
            score = score,
            totalMarks = totalMarks,
            accuracy = accuracy,
            xpAwarded = totalXp,
            idempotencyKey = idempotencyKey,
            summary = summary,
            isOnline = isOnline,
            questionsSolvedDelta = totalQuestions.toLong(),
            examScoreDelta = if (recordType == "exam") score.toLong().coerceAtLeast(1L) else 0L
        )
    }

    suspend fun recordPomodoroSession(
        userId: String,
        subjectId: String,
        focusedMinutes: Int,
        isOnline: Boolean,
        bookId: String = ""
    ): Result<Int> {
        val mins = focusedMinutes.coerceAtLeast(1)
        val xp = (mins * 2).coerceIn(10, 120)
        val idempotencyKey = "pomodoro_${subjectId}_${mins}m_${System.currentTimeMillis()}"
        return recordActivityWithIdempotency(
            userId = userId,
            recordType = "pomodoro",
            refId = "pomodoro_${subjectId}_${mins}m",
            bookId = bookId,
            subjectId = subjectId,
            chapterId = null,
            topicId = null,
            score = mins.toDouble(),
            totalMarks = mins.toDouble(),
            accuracy = 1.0,
            xpAwarded = xp,
            idempotencyKey = idempotencyKey,
            summary = "Focused Study ($mins min)",
            isOnline = isOnline,
            studyMinutesDelta = mins.toLong(),
            sessionsDelta = 1L
        )
    }

    suspend fun recordFlashcardDeckSession(
        userId: String,
        subjectId: String,
        reviewedCount: Int,
        masteredCount: Int,
        isOnline: Boolean,
        bookId: String = ""
    ): Result<Int> {
        val reviewed = reviewedCount.coerceAtLeast(1)
        val mastered = masteredCount.coerceIn(0, reviewed)
        val xp = (10 + mastered * 3).coerceIn(10, 90)
        val accuracy = (mastered.toDouble() / reviewed.toDouble()).coerceIn(0.0, 1.0)
        val idempotencyKey = "flashcard_${subjectId}_${System.currentTimeMillis()}"
        return recordActivityWithIdempotency(
            userId = userId,
            recordType = "flashcard",
            refId = "flashcards_$subjectId",
            bookId = bookId,
            subjectId = subjectId,
            chapterId = null,
            topicId = null,
            score = mastered.toDouble(),
            totalMarks = reviewed.toDouble(),
            accuracy = accuracy,
            xpAwarded = xp,
            idempotencyKey = idempotencyKey,
            summary = "Flashcard Deck ($mastered/$reviewed mastered)",
            isOnline = isOnline,
            questionsSolvedDelta = reviewed.toLong()
        )
    }

    suspend fun completeDailyChallenge(
        userId: String,
        subjectId: String,
        correctCount: Int,
        totalQuestions: Int,
        score: Double,
        totalMarks: Double,
        summary: String,
        isOnline: Boolean,
        bookId: String = ""
    ): Result<Int> {
        val todayKey = GamificationCatalog.currentDayKey()
        val xp = (40 + correctCount * 5).coerceIn(40, 150)
        val accuracy = if (totalQuestions > 0) (correctCount.toDouble() / totalQuestions.toDouble()).coerceIn(0.0, 1.0) else 0.0
        val idempotencyKey = "daily_challenge_$todayKey"
        return recordActivityWithIdempotency(
            userId = userId,
            recordType = "daily_challenge",
            refId = idempotencyKey,
            bookId = bookId,
            subjectId = subjectId,
            chapterId = null,
            topicId = null,
            score = score,
            totalMarks = totalMarks,
            accuracy = accuracy,
            xpAwarded = xp,
            idempotencyKey = idempotencyKey,
            summary = summary,
            isOnline = isOnline,
            qualifiesForStreak = true,
            questionsSolvedDelta = totalQuestions.toLong(),
            challengeScoreDelta = score.toLong().coerceAtLeast(10L)
        )
    }

    private suspend fun recordActivityWithIdempotency(
        userId: String,
        recordType: String,
        refId: String,
        bookId: String,
        subjectId: String,
        chapterId: String?,
        topicId: String?,
        score: Double,
        totalMarks: Double,
        accuracy: Double,
        xpAwarded: Int,
        idempotencyKey: String,
        summary: String,
        isOnline: Boolean,
        qualifiesForStreak: Boolean = false,
        studyMinutesDelta: Long = 0L,
        questionsSolvedDelta: Long = 0L,
        lessonsDelta: Long = 0L,
        sessionsDelta: Long = 0L,
        examScoreDelta: Long = 0L,
        challengeScoreDelta: Long = 0L
    ): Result<Int> {
        val safeDocId = idempotencyKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
        if (userId == GuestDataRepository.GUEST_UID) {
            val existingGuest = dao.getActionsForUser(GuestDataRepository.GUEST_UID)
                .any { it.idempotencyKey == idempotencyKey }
            if (existingGuest) return Result.success(0)

            val coinBonus = (xpAwarded / 3).coerceAtLeast(1)
            dao.enqueueSyncAction(
                PendingSyncActionEntity(
                    idempotencyKey = idempotencyKey,
                    userId = GuestDataRepository.GUEST_UID,
                    actionType = recordType,
                    refId = refId,
                    bookId = bookId,
                    subjectId = subjectId,
                    chapterId = chapterId.orEmpty(),
                    topicId = topicId.orEmpty(),
                    score = score,
                    totalMarks = totalMarks,
                    accuracy = accuracy,
                    xpAwarded = xpAwarded,
                    summary = summary,
                    synced = false
                )
            )
            resolvedEngine.awardVerifiedActivity(
                userId = GuestDataRepository.GUEST_UID,
                sourceType = recordType,
                sourceId = refId,
                idempotencyKey = "xp_$safeDocId",
                rawXpDelta = xpAwarded.toLong(),
                coinDelta = coinBonus.toLong(),
                description = summary,
                qualifiesForStreak = qualifiesForStreak,
                studyMinutesDelta = studyMinutesDelta,
                questionsSolvedDelta = questionsSolvedDelta,
                lessonsDelta = lessonsDelta,
                sessionsDelta = sessionsDelta,
                subjectId = subjectId
            )
            guestRepo?.incrementGuestQuest(recordType)
            return Result.success(xpAwarded)
        }

        val recordRef = db.collection("users").document(userId)
            .collection("learning_records").document(safeDocId)

        if (!isOnline) {
            enqueuePendingSync(
                idempotencyKey = idempotencyKey,
                userId = userId,
                actionType = recordType,
                refId = refId,
                bookId = bookId,
                subjectId = subjectId,
                chapterId = chapterId.orEmpty(),
                topicId = topicId.orEmpty(),
                score = score,
                totalMarks = totalMarks,
                accuracy = accuracy,
                xpAwarded = xpAwarded,
                summary = summary
            )
            return Result.success(xpAwarded)
        }

        return try {
            val existing = recordRef.get().await()
            if (existing.exists()) {
                return Result.success(0)
            }

            recordRef.set(
                mapOf(
                    "userId" to userId,
                    "recordType" to recordType,
                    "refId" to refId,
                    "bookId" to bookId.ifBlank { null },
                    "subjectId" to subjectId,
                    "chapterId" to chapterId,
                    "topicId" to topicId,
                    "score" to score,
                    "totalMarks" to totalMarks,
                    "accuracy" to accuracy,
                    "xpAwarded" to xpAwarded.toLong(),
                    "idempotencyKey" to idempotencyKey,
                    "summary" to summary.take(480),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            ).await()

            val coinBonus = (xpAwarded / 3).coerceAtLeast(1)
            val awardRes = resolvedEngine.awardVerifiedActivity(
                userId = userId,
                sourceType = recordType,
                sourceId = refId,
                idempotencyKey = "xp_$safeDocId",
                rawXpDelta = xpAwarded.toLong(),
                coinDelta = coinBonus.toLong(),
                description = summary,
                qualifiesForStreak = qualifiesForStreak,
                studyMinutesDelta = studyMinutesDelta,
                examScoreDelta = examScoreDelta,
                challengeScoreDelta = challengeScoreDelta,
                questionsSolvedDelta = questionsSolvedDelta,
                lessonsDelta = lessonsDelta,
                sessionsDelta = sessionsDelta,
                perfectExam = (recordType == "exam" && accuracy >= 0.999),
                highScoreExam = (recordType == "exam" && accuracy >= 0.85),
                subjectId = subjectId
            )
            if (awardRes.isFailure) {
                return Result.failure(awardRes.exceptionOrNull() ?: IllegalStateException("Activity award failed"))
            }
            val finalAwarded = awardRes.getOrDefault(xpAwarded.toLong()).toInt()

            incrementQuestProgress(userId, recordType)
            Result.success(finalAwarded)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, recordRef.path)
            enqueuePendingSync(
                idempotencyKey = idempotencyKey,
                userId = userId,
                actionType = recordType,
                refId = refId,
                bookId = bookId,
                subjectId = subjectId,
                chapterId = chapterId.orEmpty(),
                topicId = topicId.orEmpty(),
                score = score,
                totalMarks = totalMarks,
                accuracy = accuracy,
                xpAwarded = xpAwarded,
                summary = summary
            )
            Result.success(xpAwarded)
        }
    }

    private suspend fun incrementQuestProgress(userId: String, recordType: String) {
        val questId = when (recordType) {
            "lesson" -> "q_daily_lesson"
            "practice", "flashcard", "pomodoro" -> "q_daily_practice"
            "exam", "daily_challenge" -> "q_weekly_exam"
            else -> return
        }
        val ref = db.collection("users").document(userId).collection("quests").document(questId)
        try {
            val snap = ref.get().await()
            if (!snap.exists()) return
            val target = snap.getLong("target") ?: 1L
            val nextProgress = ((snap.getLong("progress") ?: 0L) + 1L).coerceAtMost(target)
            ref.update(
                mapOf(
                    "progress" to nextProgress,
                    "completed" to (nextProgress >= target),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Non-fatal quest increment failure", e)
        }
    }

    private suspend fun enqueuePendingSync(
        idempotencyKey: String,
        userId: String,
        actionType: String,
        refId: String,
        bookId: String,
        subjectId: String,
        chapterId: String,
        topicId: String,
        score: Double,
        totalMarks: Double,
        accuracy: Double,
        xpAwarded: Int,
        summary: String
    ) {
        dao.enqueueSyncAction(
            PendingSyncActionEntity(
                idempotencyKey = idempotencyKey,
                userId = userId,
                actionType = actionType,
                refId = refId,
                bookId = bookId,
                subjectId = subjectId,
                chapterId = chapterId,
                topicId = topicId,
                score = score,
                totalMarks = totalMarks,
                accuracy = accuracy,
                xpAwarded = xpAwarded,
                summary = summary
            )
        )
        appContext?.let { StudyQuestWorkScheduler.enqueueImmediateSync(it) }
    }

    suspend fun createStudyPlan(
        userId: String,
        title: String,
        subjectId: String,
        planType: String,
        targetDateEpochMs: Long,
        minutesPerDay: Long,
        bookId: String = ""
    ): Result<String> {
        val planId = "plan_${UUID.randomUUID().toString().take(12)}"
        if (userId == GuestDataRepository.GUEST_UID) {
            dao.upsertStudyPlan(
                LocalStudyPlanEntity(
                    id = planId,
                    userId = GuestDataRepository.GUEST_UID,
                    bookId = bookId,
                    title = title.trim().take(118),
                    subjectId = subjectId,
                    planType = planType,
                    targetDateEpochMs = targetDateEpochMs,
                    minutesPerDay = minutesPerDay.coerceIn(5L, 720L),
                    completed = false,
                    syncedToCloud = false
                )
            )
            return Result.success(planId)
        }
        val ref = db.collection("users").document(userId).collection("study_plans").document(planId)
        return try {
            ref.set(
                mapOf(
                    "userId" to userId,
                    "title" to title.trim().take(118),
                    "bookId" to bookId.ifBlank { null },
                    "subjectId" to subjectId,
                    "planType" to planType,
                    "targetDateEpochMs" to targetDateEpochMs,
                    "minutesPerDay" to minutesPerDay.coerceIn(5L, 720L),
                    "completed" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            ).await()
            Result.success(planId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun toggleStudyPlanCompleted(userId: String, plan: StudyPlanDoc): Result<Unit> {
        if (userId == GuestDataRepository.GUEST_UID) {
            dao.updateStudyPlanCompleted(plan.id, !plan.completed)
            return Result.success(Unit)
        }
        val ref = db.collection("users").document(userId).collection("study_plans").document(plan.id)
        return try {
            ref.update(
                mapOf(
                    "completed" to !plan.completed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun deleteStudyPlan(userId: String, planId: String): Result<Unit> {
        if (userId == GuestDataRepository.GUEST_UID) {
            dao.deleteStudyPlan(planId)
            return Result.success(Unit)
        }
        val ref = db.collection("users").document(userId).collection("study_plans").document(planId)
        return try {
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            Result.failure(e)
        }
    }

    fun mergeQuestionBank(cloudDocs: List<AcademicContentDoc>): List<QuestionItem> {
        val cloudQuestions = cloudDocs
            .filter { it.contentType == "question" }
            .mapNotNull { doc -> parseCloudQuestion(doc) }
        val merged = LinkedHashMap<String, QuestionItem>()
        BangladeshCurriculumCatalog.questions.forEach { merged[it.id] = it }
        cloudQuestions.forEach { merged[it.id] = it }
        return merged.values.toList()
    }

    private fun parseCloudQuestion(doc: AcademicContentDoc): QuestionItem? {
        return try {
            val json = doc.payloadJson?.takeIf { it.isNotBlank() }?.let { JSONObject(it) }
            val optionsEnArr = json?.optJSONArray("optionsEn")
            val optionsBnArr = json?.optJSONArray("optionsBn")
            val optionsEn = if (optionsEnArr != null) {
                (0 until optionsEnArr.length()).map { optionsEnArr.getString(it) }
            } else listOf("Option A", "Option B", "Option C", "Option D")
            val optionsBn = if (optionsBnArr != null) {
                (0 until optionsBnArr.length()).map { optionsBnArr.getString(it) }
            } else optionsEn

            val provenanceStr = json?.optString("provenance")
            val provenance = QuestionProvenance.entries.find { it.id == provenanceStr }
                ?: QuestionProvenance.STUDYQUEST_ORIGINAL
            QuestionItem(
                id = doc.id,
                curriculumVersion = doc.curriculumVersion,
                classId = doc.classId,
                subjectId = doc.subjectId,
                chapterId = doc.chapterId ?: "ch_custom",
                topicId = doc.topicId ?: "topic_custom",
                bookId = json?.optString("bookId").orEmpty(),
                board = doc.sourceAttribution ?: "StudyQuest Cloud Editorial",
                type = QuestionType.entries.find { it.id == json?.optString("type") } ?: QuestionType.SINGLE_MCQ,
                difficulty = QuestionDifficulty.entries.find { it.id == json?.optString("difficulty") } ?: QuestionDifficulty.MEDIUM,
                promptEn = doc.titleEn,
                promptBn = doc.titleBn,
                optionsEn = optionsEn,
                optionsBn = optionsBn,
                correctOptionIndices = listOf(json?.optInt("correctIndex", 0) ?: 0),
                explanationEn = json?.optString("explanationEn")?.ifBlank { doc.titleEn } ?: doc.titleEn,
                explanationBn = json?.optString("explanationBn")?.ifBlank { doc.titleBn } ?: doc.titleBn,
                provenance = provenance,
                sourceAttribution = doc.sourceAttribution ?: "StudyQuest Published Content",
                publicationStatus = doc.status,
                version = doc.version.toInt()
            )
        } catch (_: Exception) {
            null
        }
    }

    fun filterRecordsForActiveBook(
        records: List<LearningRecordDoc>,
        activeBookId: String
    ): List<LearningRecordDoc> {
        if (activeBookId.isBlank()) return records
        return records.filter { it.bookId.isNullOrBlank() || it.bookId == activeBookId }
    }

    fun computeWeakTopics(records: List<LearningRecordDoc>): List<WeakTopicStat> {
        val grouped = records
            .filter { !it.topicId.isNullOrBlank() && it.recordType in listOf("practice", "exam", "daily_challenge") }
            .groupBy { it.subjectId to it.topicId!! }

        return grouped.mapNotNull { (key, list) ->
            // Do not flag weak topics from 1 lucky or unlucky attempt — require >= 2 attempts
            if (list.size < 2) return@mapNotNull null
            val avg = list.map { it.accuracy }.average()
            if (avg >= 0.65) return@mapNotNull null
            val chapter = BangladeshCurriculumCatalog.chapters.find { it.subjectId == key.first }
            WeakTopicStat(
                subjectId = key.first,
                topicId = key.second,
                topicLabelEn = chapter?.titleEn ?: key.second,
                topicLabelBn = chapter?.titleBn ?: key.second,
                attempts = list.size,
                averageAccuracy = avg
            )
        }.sortedBy { it.averageAccuracy }
    }

    fun computeSubjectAnalytics(records: List<LearningRecordDoc>): List<SubjectAnalyticsSummary> {
        return records.groupBy { it.subjectId }.map { (subjectId, list) ->
            val lessons = list.count { it.recordType == "lesson" }
            val practices = list.count { it.recordType in listOf("practice", "flashcard", "pomodoro") }
            val exams = list.count { it.recordType in listOf("exam", "daily_challenge") }
            val xp = list.sumOf { it.xpAwarded }
            val graded = list.filter { it.recordType in listOf("practice", "exam", "daily_challenge", "flashcard") }
            val avgAcc = if (graded.isNotEmpty()) graded.map { it.accuracy }.average() else 0.0
            SubjectAnalyticsSummary(
                subjectId = subjectId,
                lessonsCompleted = lessons,
                practiceSessions = practices,
                examSessions = exams,
                totalXp = xp,
                averageAccuracy = avgAcc,
                subjectLevel = ((xp / 50L) + 1L).toInt()
            )
        }.sortedByDescending { it.totalXp }
    }

    fun computeDailyStudySeries(
        records: List<LearningRecordDoc>,
        focusSessions: List<com.example.data.model.StudySessionDoc> = emptyList(),
        daysCount: Int = 7
    ): List<DailyStudyMetric> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = GamificationCatalog.dhakaTimeZone }
        val shortFmt = SimpleDateFormat("EEE", Locale.US).apply { timeZone = GamificationCatalog.dhakaTimeZone }
        val nowMs = System.currentTimeMillis()
        val cal = Calendar.getInstance(GamificationCatalog.dhakaTimeZone).apply {
            timeInMillis = nowMs
            add(Calendar.DAY_OF_YEAR, -(daysCount - 1))
        }
        val keys = (0 until daysCount).map {
            val k = fmt.format(cal.timeInMillis)
            val l = shortFmt.format(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            k to l
        }

        val xpByDay = mutableMapOf<String, Long>()
        val countByDay = mutableMapOf<String, Int>()
        val lessonsByDay = mutableMapOf<String, Int>()
        val examsByDay = mutableMapOf<String, Int>()
        val examAccByDay = mutableMapOf<String, MutableList<Double>>()
        for (rec in records) {
            val rawSec = rec.createdAt?.seconds ?: 0L
            val ts = if (rawSec > 0L) rawSec * 1000L else nowMs
            val day = fmt.format(ts)
            xpByDay[day] = (xpByDay[day] ?: 0L) + rec.xpAwarded
            countByDay[day] = (countByDay[day] ?: 0) + 1
            if (rec.recordType == "lesson") {
                lessonsByDay[day] = (lessonsByDay[day] ?: 0) + 1
            }
            if (rec.recordType in listOf("exam", "promotion_exam")) {
                examsByDay[day] = (examsByDay[day] ?: 0) + 1
            }
            if (rec.recordType in listOf("exam", "promotion_exam", "practice", "daily_challenge")) {
                examAccByDay.getOrPut(day) { mutableListOf() }.add(rec.accuracy)
            }
        }
        val minsByDay = mutableMapOf<String, Long>()
        for (s in focusSessions) {
            val ts = s.endedAtEpochMs.takeIf { it > 0L }
                ?: ((s.createdAt?.seconds ?: 0L) * 1000L).let { if (it > 0L) it else nowMs }
            val day = fmt.format(ts)
            minsByDay[day] = (minsByDay[day] ?: 0L) + (s.focusedSeconds / 60L)
        }

        return keys.map { (key, label) ->
            val accList = examAccByDay[key]
            val avgAcc = if (accList != null && accList.isNotEmpty()) accList.average() else 0.0
            DailyStudyMetric(
                dayKey = key,
                shortLabel = label,
                xpEarned = xpByDay[key] ?: 0L,
                studyMinutes = (minsByDay[key] ?: 0L).toInt(),
                examAccuracy = avgAcc,
                sessionsCount = countByDay[key] ?: 0,
                lessonsCompleted = lessonsByDay[key] ?: 0,
                examsCompleted = examsByDay[key] ?: 0
            )
        }
    }

    companion object {
        private const val TAG = "AcademicProgressRepo"
    }
}
