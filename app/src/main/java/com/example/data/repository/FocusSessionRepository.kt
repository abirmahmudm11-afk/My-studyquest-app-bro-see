package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.NotificationCategory
import com.example.core.NotificationDispatcher
import com.example.core.OperationType
import com.example.core.PersistedFocusTimerState
import com.example.core.StudyQuestPreferences
import com.example.core.handleFirestoreError
import com.example.data.curriculum.GamificationCatalog
import com.example.data.local.LocalStudySessionEntity
import com.example.data.local.StudyQuestDao
import com.example.data.model.StudySessionDoc
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class FocusAnalyticsSummary(
    val todayMinutes: Int = 0,
    val weeklyMinutes: Int = 0,
    val previousWeekMinutes: Int = 0,
    val monthlyMinutes: Int = 0,
    val totalSessions: Int = 0,
    val dailySeriesMinutes: List<Pair<String, Int>> = emptyList()
)

class FocusSessionRepository(
    private val db: FirebaseFirestore,
    private val dao: StudyQuestDao,
    private val preferences: StudyQuestPreferences,
    private val gamificationEngine: GamificationEngine,
    private val notifications: NotificationDispatcher? = null
) {
    constructor(
        context: Context,
        dao: StudyQuestDao,
        preferences: StudyQuestPreferences,
        gamificationEngine: GamificationEngine,
        notifications: NotificationDispatcher? = null
    ) : this(
        FirebaseFirestore.getInstance(context.applicationContext.getString(R.string.firestore_database_id)),
        dao,
        preferences,
        gamificationEngine,
        notifications
    )

    fun observeStudySessions(userId: String): Flow<List<StudySessionDoc>> =
        if (userId == GuestDataRepository.GUEST_UID) {
            dao.observeLocalStudySessions(userId).map { list ->
                list.map {
                    StudySessionDoc(
                        id = it.id,
                        userId = it.userId,
                        subjectId = it.subjectId,
                        mode = it.mode,
                        startedAtEpochMs = it.startedAtEpochMs,
                        endedAtEpochMs = it.endedAtEpochMs,
                        focusedSeconds = it.focusedSeconds,
                        pausedSeconds = it.pausedSeconds,
                        breakSeconds = it.breakSeconds,
                        xpAwarded = it.xpAwarded,
                        status = it.status,
                        idempotencyKey = it.id,
                        createdAt = Timestamp(it.createdAtMs / 1000L, 0)
                    )
                }
            }
        } else {
            db.collection("users").document(userId).collection("study_sessions")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        StudySessionDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.endedAtEpochMs }
                }
        }

    fun observeTimerState(): Flow<PersistedFocusTimerState> =
        preferences.preferencesFlow.map { it.focusTimer }

    suspend fun startSession(
        subjectId: String,
        mode: String,
        targetFocusMinutes: Int,
        targetBreakMinutes: Int,
        autoBreak: Boolean
    ): PersistedFocusTimerState {
        val now = System.currentTimeMillis()
        val state = PersistedFocusTimerState(
            active = true,
            sessionId = "focus_${now}_${UUID.randomUUID().toString().take(6)}",
            subjectId = subjectId,
            mode = mode,
            phase = "focus",
            isRunning = true,
            autoBreak = autoBreak,
            targetFocusSeconds = (targetFocusMinutes.coerceIn(1, 240)) * 60,
            targetBreakSeconds = (targetBreakMinutes.coerceIn(0, 60)) * 60,
            accumulatedFocusMs = 0L,
            accumulatedPausedMs = 0L,
            accumulatedBreakMs = 0L,
            sessionStartEpochMs = now,
            lastResumeEpochMs = now,
            lastPauseEpochMs = 0L,
            phaseStartEpochMs = now
        )
        preferences.saveFocusTimerState(state)
        return state
    }

    suspend fun pauseSession(): PersistedFocusTimerState? {
        val current = preferences.snapshot().focusTimer
        if (!current.active || !current.isRunning) return current
        val now = System.currentTimeMillis()
        val delta = (now - current.lastResumeEpochMs).coerceAtLeast(0L)
        val updated = if (current.phase == "focus") {
            current.copy(
                isRunning = false,
                accumulatedFocusMs = current.accumulatedFocusMs + delta,
                lastPauseEpochMs = now
            )
        } else {
            current.copy(
                isRunning = false,
                accumulatedBreakMs = current.accumulatedBreakMs + delta,
                lastPauseEpochMs = now
            )
        }
        preferences.saveFocusTimerState(updated)
        return updated
    }

    suspend fun resumeSession(): PersistedFocusTimerState? {
        val current = preferences.snapshot().focusTimer
        if (!current.active || current.isRunning) return current
        val now = System.currentTimeMillis()
        val pauseDelta = if (current.lastPauseEpochMs > 0L) (now - current.lastPauseEpochMs).coerceAtLeast(0L) else 0L
        val updated = current.copy(
            isRunning = true,
            accumulatedPausedMs = current.accumulatedPausedMs + pauseDelta,
            lastResumeEpochMs = now,
            lastPauseEpochMs = 0L
        )
        preferences.saveFocusTimerState(updated)
        return updated
    }

    suspend fun transitionToBreakPhase(): PersistedFocusTimerState? {
        val current = preferences.snapshot().focusTimer
        if (!current.active || current.phase == "break") return current
        val now = System.currentTimeMillis()
        val focusDelta = if (current.isRunning) (now - current.lastResumeEpochMs).coerceAtLeast(0L) else 0L
        val updated = current.copy(
            phase = "break",
            isRunning = true,
            accumulatedFocusMs = current.accumulatedFocusMs + focusDelta,
            lastResumeEpochMs = now,
            lastPauseEpochMs = 0L,
            phaseStartEpochMs = now
        )
        preferences.saveFocusTimerState(updated)
        return updated
    }

    suspend fun resetTimer() {
        preferences.clearFocusTimerState()
    }

    suspend fun stopAndVerifySession(userId: String, completedTarget: Boolean): Result<Long> {
        val current = preferences.snapshot().focusTimer
        if (!current.active) return Result.failure(IllegalStateException("No active focus timer"))

        val now = System.currentTimeMillis()
        val finalFocusMs = current.accumulatedFocusMs + if (current.isRunning && current.phase == "focus") {
            (now - current.lastResumeEpochMs).coerceAtLeast(0L)
        } else 0L
        val finalBreakMs = current.accumulatedBreakMs + if (current.isRunning && current.phase == "break") {
            (now - current.lastResumeEpochMs).coerceAtLeast(0L)
        } else 0L
        val finalPausedMs = current.accumulatedPausedMs + if (!current.isRunning && current.lastPauseEpochMs > 0L) {
            (now - current.lastPauseEpochMs).coerceAtLeast(0L)
        } else 0L

        val focusedSeconds = (finalFocusMs / 1000L).coerceIn(0L, 28_800L)
        val pausedSeconds = (finalPausedMs / 1000L).coerceAtLeast(0L)
        val breakSeconds = (finalBreakMs / 1000L).coerceAtLeast(0L)
        val startMs = current.sessionStartEpochMs.takeIf { it > 0L } ?: (now - finalFocusMs)
        val endMs = now.coerceAtLeast(startMs + 1000L)

        preferences.clearFocusTimerState()

        if (focusedSeconds < 60L) {
            return Result.failure(IllegalStateException("Focus for at least 1 full minute to record verified XP."))
        }

        // Reject overlapping intervals against local & cloud sessions
        val hasOverlap = checkSessionOverlap(userId, startMs, endMs)
        if (hasOverlap) {
            return Result.failure(IllegalStateException("Overlapping focus session detected; duplicate XP blocked."))
        }

        val focusedMinutes = (focusedSeconds / 60L).coerceAtLeast(1L)
        val rules = gamificationEngine.loadXpRuleConfig()
        val rawXp = (focusedMinutes * rules.focusXpPerMinute).coerceIn(2L, 300L)
        val coinBonus = (focusedMinutes / 5L).coerceIn(1L, 30L)
        val status = if (completedTarget) "completed" else "stopped"
        val sessionId = current.sessionId.ifBlank { "focus_${startMs}" }

        if (userId != GuestDataRepository.GUEST_UID) {
            val sessionRef = db.collection("users").document(userId)
                .collection("study_sessions").document(sessionId)
            try {
                sessionRef.set(
                    mapOf(
                        "userId" to userId,
                        "subjectId" to current.subjectId,
                        "mode" to current.mode,
                        "startedAtEpochMs" to startMs,
                        "endedAtEpochMs" to endMs,
                        "focusedSeconds" to focusedSeconds,
                        "pausedSeconds" to pausedSeconds,
                        "breakSeconds" to breakSeconds,
                        "xpAwarded" to rawXp,
                        "status" to status,
                        "idempotencyKey" to sessionId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, sessionRef.path)
            }
        }

        dao.insertLocalStudySession(
            LocalStudySessionEntity(
                id = sessionId,
                userId = userId,
                subjectId = current.subjectId,
                mode = current.mode,
                startedAtEpochMs = startMs,
                endedAtEpochMs = endMs,
                focusedSeconds = focusedSeconds,
                pausedSeconds = pausedSeconds,
                breakSeconds = breakSeconds,
                xpAwarded = rawXp,
                status = status
            )
        )

        val awardedRes = gamificationEngine.awardVerifiedActivity(
            userId = userId,
            sourceType = "focus_session",
            sourceId = sessionId,
            idempotencyKey = "focus_${GamificationCatalog.currentDayKey()}_$sessionId",
            rawXpDelta = rawXp,
            coinDelta = coinBonus,
            description = "Verified Focus Study (${focusedMinutes}m • ${current.subjectId})",
            qualifiesForStreak = false, // Daily streak strictly requires a full daily quiz/lesson
            studyMinutesDelta = focusedMinutes,
            sessionsDelta = 1L,
            subjectId = current.subjectId
        )

        val awardedXp = awardedRes.getOrDefault(rawXp)
        notifications?.dispatch(
            userId = userId,
            category = NotificationCategory.STUDY,
            title = "Focus Session Verified (${focusedMinutes}m)",
            body = "+$awardedXp XP logged cleanly for ${current.subjectId}.",
            actionRoute = "focus",
            idempotencyKey = "notif_focus_$sessionId"
        )
        return Result.success(awardedXp)
    }

    private suspend fun checkSessionOverlap(userId: String, startMs: Long, endMs: Long): Boolean {
        val localOverlap = dao.getLocalStudySessions(userId).any { existing ->
            startMs < existing.endedAtEpochMs && endMs > existing.startedAtEpochMs && existing.focusedSeconds >= 60L
        }
        if (localOverlap) return true
        if (userId == GuestDataRepository.GUEST_UID) return false
        return runCatching {
            val recent = db.collection("users").document(userId).collection("study_sessions")
                .get().await().toObjects(StudySessionDoc::class.java)
            recent.any { existing ->
                startMs < existing.endedAtEpochMs && endMs > existing.startedAtEpochMs && existing.focusedSeconds >= 60L
            }
        }.getOrDefault(false)
    }

    companion object {
        fun computeLiveFocusElapsedMs(state: PersistedFocusTimerState, nowMs: Long = System.currentTimeMillis()): Long {
            if (!state.active) return 0L
            val runningDelta = if (state.isRunning && state.phase == "focus") {
                (nowMs - state.lastResumeEpochMs).coerceAtLeast(0L)
            } else 0L
            return state.accumulatedFocusMs + runningDelta
        }

        fun computeLiveBreakElapsedMs(state: PersistedFocusTimerState, nowMs: Long = System.currentTimeMillis()): Long {
            if (!state.active) return 0L
            val runningDelta = if (state.isRunning && state.phase == "break") {
                (nowMs - state.lastResumeEpochMs).coerceAtLeast(0L)
            } else 0L
            return state.accumulatedBreakMs + runningDelta
        }

        fun computeFocusAnalytics(sessions: List<StudySessionDoc>, nowMs: Long = System.currentTimeMillis()): FocusAnalyticsSummary {
            val todayKey = GamificationCatalog.currentDayKey(nowMs)
            val thisWeekKey = GamificationCatalog.currentWeekKey(nowMs)
            val prevWeekKey = GamificationCatalog.previousWeekKey(nowMs)
            val thisMonthKey = GamificationCatalog.currentMonthKey(nowMs)
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = GamificationCatalog.dhakaTimeZone }

            var todaySec = 0L
            var weekSec = 0L
            var prevWeekSec = 0L
            var monthSec = 0L
            val byDay = mutableMapOf<String, Long>()

            for (s in sessions) {
                val ts = s.endedAtEpochMs.takeIf { it > 0L } ?: ((s.createdAt?.seconds ?: 0L) * 1000L)
                if (ts <= 0L) continue
                val dKey = fmt.format(ts)
                val wKey = GamificationCatalog.currentWeekKey(ts)
                val mKey = GamificationCatalog.currentMonthKey(ts)

                byDay[dKey] = (byDay[dKey] ?: 0L) + s.focusedSeconds
                if (dKey == todayKey) todaySec += s.focusedSeconds
                if (wKey == thisWeekKey) weekSec += s.focusedSeconds
                if (wKey == prevWeekKey) prevWeekSec += s.focusedSeconds
                if (mKey == thisMonthKey) monthSec += s.focusedSeconds
            }

            val dayLabels = SimpleDateFormat("EEE", Locale.US).apply { timeZone = GamificationCatalog.dhakaTimeZone }
            val sevenDays = GamificationCatalog.recentSevenDayKeys(nowMs).map { key ->
                val cal = Calendar.getInstance(GamificationCatalog.dhakaTimeZone)
                val date = fmt.parse(key)
                if (date != null) cal.time = date
                val shortLabel = dayLabels.format(cal.timeInMillis)
                shortLabel to ((byDay[key] ?: 0L) / 60L).toInt()
            }

            return FocusAnalyticsSummary(
                todayMinutes = (todaySec / 60L).toInt(),
                weeklyMinutes = (weekSec / 60L).toInt(),
                previousWeekMinutes = (prevWeekSec / 60L).toInt(),
                monthlyMinutes = (monthSec / 60L).toInt(),
                totalSessions = sessions.size,
                dailySeriesMinutes = sevenDays
            )
        }
    }
}
