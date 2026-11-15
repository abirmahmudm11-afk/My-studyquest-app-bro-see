package com.example.data.repository

import android.util.Log
import com.example.core.AppLanguage
import com.example.core.NotificationCategory
import com.example.core.NotificationDispatcher
import com.example.core.OperationType
import com.example.core.StudyQuestPreferences
import com.example.core.StudyQuestStrings
import com.example.core.handleFirestoreError
import com.example.data.curriculum.GamificationCatalog
import com.example.data.local.LocalBadgeProgressEntity
import com.example.data.local.LocalStudyGoalEntity
import com.example.data.local.LocalXpLedgerEntity
import com.example.data.local.StudyQuestDao
import com.example.data.model.AssignmentDefinitionDoc
import com.example.data.model.AssignmentSubmissionDoc
import com.example.data.model.BadgeCatalogItem
import com.example.data.model.PublicProfile
import com.example.data.model.StudyGoalDoc
import com.example.data.model.UserBadgeDoc
import com.example.data.model.XpRuleConfig
import com.example.data.model.XpTransactionDoc
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.UUID

sealed interface CelebrationEvent {
    data class LevelUp(val newLevel: Long, val levelTitle: String, val totalXp: Long) : CelebrationEvent
    data class BadgeUnlocked(val badge: BadgeCatalogItem) : CelebrationEvent
    data class StreakExtended(val newStreak: Long, val usedFreeze: Boolean) : CelebrationEvent
    data class GoalCompleted(val title: String, val xpReward: Long) : CelebrationEvent
    data class ClassPromoted(val fromClassId: String, val toClassId: String, val academicYear: Int) : CelebrationEvent
}

data class StreakTransitionResult(
    val newStreakCount: Long,
    val newLastDateKey: String,
    val newHistoryKeys: List<String>,
    val remainingFreezes: Long,
    val usedFreeze: Boolean,
    val incrementedToday: Boolean
)

data class ClassLifecycleDecision(
    val updatedClassId: String,
    val lockedAcademicYear: Int,
    val annualExamStatus: String,
    val promotedFromClassId: String?,
    val didPromote: Boolean,
    val wasHeldBackForExamFailure: Boolean
)

object GamificationEventBus {
    private val _events = MutableSharedFlow<CelebrationEvent>(extraBufferCapacity = 24)
    val events: SharedFlow<CelebrationEvent> = _events.asSharedFlow()

    fun emit(event: CelebrationEvent) {
        _events.tryEmit(event)
    }
}

class GamificationEngine(
    private val db: FirebaseFirestore,
    private val dao: StudyQuestDao? = null,
    private val preferences: StudyQuestPreferences? = null,
    private val notifications: NotificationDispatcher? = null
) {

    fun observeXpLedger(userId: String): Flow<List<XpTransactionDoc>> =
        if (userId == GuestDataRepository.GUEST_UID && dao != null) {
            dao.observeLocalXpLedger(userId).map { list ->
                list.map {
                    XpTransactionDoc(
                        id = it.idempotencyKey,
                        userId = it.userId,
                        sourceType = it.sourceType,
                        sourceId = it.sourceId,
                        xpDelta = it.xpDelta,
                        coinDelta = it.coinDelta,
                        description = it.description
                    )
                }
            }
        } else {
            db.collection("users").document(userId).collection("xp_ledger")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        XpTransactionDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
        }

    fun observeUserBadges(userId: String): Flow<List<UserBadgeDoc>> =
        if (userId == GuestDataRepository.GUEST_UID && dao != null) {
            dao.observeLocalBadges(userId).map { list ->
                list.mapNotNull { entity ->
                    val def = GamificationCatalog.badgesCatalog.find { it.id == entity.badgeId } ?: return@mapNotNull null
                    UserBadgeDoc(
                        id = entity.badgeId,
                        userId = entity.userId,
                        badgeId = entity.badgeId,
                        category = def.category.id,
                        rarity = def.rarity.id,
                        progress = entity.progress,
                        target = entity.target,
                        unlocked = entity.unlocked,
                        pinned = entity.pinned,
                        favorite = entity.favorite,
                        animationShown = entity.animationShown
                    )
                }
            }
        } else {
            db.collection("users").document(userId).collection("badges")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        UserBadgeDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
        }

    fun observeStudyGoals(userId: String): Flow<List<StudyGoalDoc>> =
        if (userId == GuestDataRepository.GUEST_UID && dao != null) {
            dao.observeLocalStudyGoals(userId).map { list ->
                list.map {
                    StudyGoalDoc(
                        id = it.id,
                        userId = it.userId,
                        title = it.title,
                        goalType = it.goalType,
                        periodType = it.periodType,
                        subjectId = it.subjectId,
                        targetValue = it.targetValue,
                        currentValue = it.currentValue,
                        difficulty = it.difficulty,
                        xpReward = it.xpReward,
                        completed = it.completed,
                        rewardClaimed = it.rewardClaimed,
                        deadlineEpochMs = it.deadlineEpochMs,
                        periodKey = it.periodKey
                    )
                }
            }
        } else {
            db.collection("users").document(userId).collection("study_goals")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        StudyGoalDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
        }

    fun observeAssignments(classId: String): Flow<List<AssignmentDefinitionDoc>> =
        db.collection("assignments")
            .whereEqualTo("active", true)
            .snapshots()
            .map { snap ->
                val remote = snap.toObjects(
                    AssignmentDefinitionDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).filter { it.classId == "all" || it.classId == classId }
                val defaults = GamificationCatalog.defaultAssignmentsForClass(classId)
                val byId = LinkedHashMap<String, AssignmentDefinitionDoc>()
                defaults.forEach { byId[it.id] = it }
                remote.forEach { byId[it.id] = it }
                byId.values.toList()
            }

    fun observeAssignmentSubmissions(userId: String): Flow<List<AssignmentSubmissionDoc>> =
        if (userId == GuestDataRepository.GUEST_UID) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            db.collection("users").document(userId).collection("assignment_submissions")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        AssignmentSubmissionDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
        }

    suspend fun loadXpRuleConfig(): XpRuleConfig {
        return try {
            val snap = db.collection("system_settings").document("global").get().await()
            val raw = snap.getString("xpRulesJson") ?: return XpRuleConfig()
            val json = JSONObject(raw)
            XpRuleConfig(
                lessonXp = json.optLong("lessonXp", 20L).coerceIn(5L, 200L),
                practiceXpPerCorrect = json.optLong("practiceXpPerCorrect", 4L).coerceIn(1L, 30L),
                examXpBase = json.optLong("examXpBase", 30L).coerceIn(10L, 300L),
                focusXpPerMinute = json.optLong("focusXpPerMinute", 2L).coerceIn(1L, 20L),
                dailyChallengeXp = json.optLong("dailyChallengeXp", 45L).coerceIn(10L, 300L),
                streakBonusPerDay = json.optLong("streakBonusPerDay", 5L).coerceIn(0L, 50L),
                communityHelpfulXp = json.optLong("communityHelpfulXp", 15L).coerceIn(0L, 100L),
                maxDailyFocusXp = json.optLong("maxDailyFocusXp", 240L).coerceIn(30L, 600L),
                maxDailyCommunityXp = json.optLong("maxDailyCommunityXp", 60L).coerceIn(0L, 200L)
            )
        } catch (_: Exception) {
            XpRuleConfig()
        }
    }

    /**
     * Single auditable XP + Coin + Leaderboard + Streak + Goal + Badge award pipeline.
     * Enforces idempotency via idempotencyKey and anti-farming caps.
     */
    suspend fun awardVerifiedActivity(
        userId: String,
        sourceType: String,
        sourceId: String,
        idempotencyKey: String,
        rawXpDelta: Long,
        coinDelta: Long,
        description: String,
        qualifiesForStreak: Boolean = false,
        studyMinutesDelta: Long = 0L,
        examScoreDelta: Long = 0L,
        assignmentScoreDelta: Long = 0L,
        challengeScoreDelta: Long = 0L,
        questionsSolvedDelta: Long = 0L,
        lessonsDelta: Long = 0L,
        sessionsDelta: Long = 0L,
        perfectExam: Boolean = false,
        highScoreExam: Boolean = false,
        subjectId: String = "all"
    ): Result<Long> {
        val safeKey = idempotencyKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(140)
        if (userId == GuestDataRepository.GUEST_UID) {
            if (dao?.getLocalXpEntry(safeKey) != null) return Result.success(0L)
            val xp = rawXpDelta.coerceIn(0L, 1000L)
            dao?.insertLocalXpEntry(
                LocalXpLedgerEntity(
                    idempotencyKey = safeKey,
                    userId = userId,
                    sourceType = sourceType,
                    sourceId = sourceId,
                    xpDelta = xp,
                    coinDelta = coinDelta,
                    description = description
                )
            )
            val beforeXp = preferences?.snapshot()?.guestXp ?: 0L
            val beforeLv = StudyQuestStrings.levelForXp(beforeXp)
            preferences?.addGuestReward(xpDelta = xp, coinDelta = coinDelta)
            val afterLv = StudyQuestStrings.levelForXp(beforeXp + xp)
            if (afterLv > beforeLv) {
                GamificationEventBus.emit(
                    CelebrationEvent.LevelUp(afterLv, StudyQuestStrings.levelTitle(afterLv.toInt(), AppLanguage.ENGLISH), beforeXp + xp)
                )
            }
            if (qualifiesForStreak) {
                preferences?.recordGuestStreakCompletion(
                    todayKey = GamificationCatalog.currentDayKey(),
                    yesterdayKey = GamificationCatalog.yesterdayDayKey()
                )
            }
            progressLocalGoals(userId, studyMinutesDelta, questionsSolvedDelta, lessonsDelta, sessionsDelta, subjectId)
            return Result.success(xp)
        }

        val ledgerRef = db.collection("users").document(userId).collection("xp_ledger").document(safeKey)
        val profileRef = db.collection("public_profiles").document(userId)
        val lbRef = db.collection("leaderboard_entries").document(userId)

        return try {
            if (ledgerRef.get().await().exists()) {
                return Result.success(0L)
            }

            val rules = loadXpRuleConfig()
            val todayKey = GamificationCatalog.currentDayKey()
            val yesterdayKey = GamificationCatalog.yesterdayDayKey()

            // Anti-farming daily cap for focus & community sources
            val clampedXp = applyDailyAntiFarmingCap(userId, sourceType, rawXpDelta, rules, todayKey)
            if (clampedXp <= 0L && coinDelta <= 0L && studyMinutesDelta <= 0L && !qualifiesForStreak) {
                return Result.success(0L)
            }

            val profileSnap = profileRef.get().await()
            val profile = profileSnap.toObject(
                PublicProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: return Result.failure(IllegalStateException("Profile missing"))

            val oldXp = profile.totalXp
            val oldLevel = StudyQuestStrings.levelForXp(oldXp)

            var streakBonusXp = 0L
            var streakTransition: StreakTransitionResult? = null
            if (qualifiesForStreak) {
                val st = computeStreakTransition(
                    currentStreak = profile.streakCount,
                    lastDateKey = profile.lastChallengeDateKey,
                    historyKeys = profile.streakHistoryKeys,
                    freezeCount = profile.streakFreezeCount,
                    todayKey = todayKey,
                    yesterdayKey = yesterdayKey
                )
                streakTransition = st
                if (st.incrementedToday) {
                    streakBonusXp = (st.newStreakCount.coerceAtMost(10L)) * rules.streakBonusPerDay
                }
            }

            val totalAwardedXp = (clampedXp + streakBonusXp).coerceIn(0L, 1000L)
            val newXp = oldXp + totalAwardedXp
            val newLevel = StudyQuestStrings.levelForXp(newXp)
            val newLevelTitle = StudyQuestStrings.levelTitle(newLevel.toInt(), AppLanguage.ENGLISH)

            val weekKey = GamificationCatalog.currentWeekKey()
            val biweekKey = GamificationCatalog.currentBiweekKey()
            val monthKey = GamificationCatalog.currentMonthKey()

            val batch = db.batch()
            batch.set(
                ledgerRef,
                mapOf(
                    "userId" to userId,
                    "sourceType" to sourceType.take(38),
                    "sourceId" to sourceId.take(120),
                    "xpDelta" to totalAwardedXp,
                    "coinDelta" to coinDelta.coerceIn(-5000L, 1000L),
                    "description" to description.take(195),
                    "idempotencyKey" to safeKey,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            val profileUpdates = mutableMapOf<String, Any>(
                "totalXp" to newXp,
                "overallLevel" to newLevel,
                "levelTitle" to newLevelTitle,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (coinDelta != 0L) {
                profileUpdates["coins"] = FieldValue.increment(coinDelta)
            }
            if (studyMinutesDelta > 0L) {
                profileUpdates["totalFocusMinutes"] = FieldValue.increment(studyMinutesDelta)
            }
            if (streakTransition != null) {
                profileUpdates["streakCount"] = streakTransition.newStreakCount
                profileUpdates["lastChallengeDateKey"] = streakTransition.newLastDateKey
                profileUpdates["streakHistoryKeys"] = streakTransition.newHistoryKeys
                profileUpdates["streakFreezeCount"] = streakTransition.remainingFreezes
            }
            batch.update(profileRef, profileUpdates)

            // Update 5 segregated leaderboard metrics across Weekly, Biweekly, Monthly
            val existingLb = runCatching { lbRef.get().await() }.getOrNull()
            val sameWeek = existingLb?.getString("weekKey") == weekKey
            val sameBiweek = existingLb?.getString("biweekKey") == biweekKey
            val sameMonth = existingLb?.getString("monthKey") == monthKey

            fun cur(field: String, samePeriod: Boolean): Long =
                if (samePeriod) (existingLb?.getLong(field) ?: 0L) else 0L

            val lbPayload = mutableMapOf<String, Any?>(
                "userId" to userId,
                "displayName" to profile.displayName,
                "studyQuestId" to profile.studyQuestId,
                "avatarId" to profile.avatarId,
                "frameId" to profile.frameId,
                "classId" to profile.classId,
                "subjectId" to subjectId,
                "seasonId" to "season_2026_11",
                "weekKey" to weekKey,
                "biweekKey" to biweekKey,
                "monthKey" to monthKey,
                "weeklyPoints" to (cur("weeklyPoints", sameWeek) + totalAwardedXp),
                "seasonalPoints" to ((existingLb?.getLong("seasonalPoints") ?: oldXp) + totalAwardedXp),
                "weeklyStudyMinutes" to (cur("weeklyStudyMinutes", sameWeek) + studyMinutesDelta),
                "weeklyXp" to (cur("weeklyXp", sameWeek) + totalAwardedXp),
                "weeklyExamScore" to (cur("weeklyExamScore", sameWeek) + examScoreDelta),
                "weeklyAssignmentScore" to (cur("weeklyAssignmentScore", sameWeek) + assignmentScoreDelta),
                "weeklyChallengeScore" to (cur("weeklyChallengeScore", sameWeek) + challengeScoreDelta),
                "biweeklyStudyMinutes" to (cur("biweeklyStudyMinutes", sameBiweek) + studyMinutesDelta),
                "biweeklyXp" to (cur("biweeklyXp", sameBiweek) + totalAwardedXp),
                "biweeklyExamScore" to (cur("biweeklyExamScore", sameBiweek) + examScoreDelta),
                "biweeklyAssignmentScore" to (cur("biweeklyAssignmentScore", sameBiweek) + assignmentScoreDelta),
                "biweeklyChallengeScore" to (cur("biweeklyChallengeScore", sameBiweek) + challengeScoreDelta),
                "monthlyStudyMinutes" to (cur("monthlyStudyMinutes", sameMonth) + studyMinutesDelta),
                "monthlyXp" to (cur("monthlyXp", sameMonth) + totalAwardedXp),
                "monthlyExamScore" to (cur("monthlyExamScore", sameMonth) + examScoreDelta),
                "monthlyAssignmentScore" to (cur("monthlyAssignmentScore", sameMonth) + assignmentScoreDelta),
                "monthlyChallengeScore" to (cur("monthlyChallengeScore", sameMonth) + challengeScoreDelta),
                "competitionRating" to (existingLb?.getLong("competitionRating") ?: profile.competitionRating),
                "rankTier" to profile.rankTier,
                "optInNational" to profile.optInNationalLeaderboard,
                "streakCount" to (streakTransition?.newStreakCount ?: profile.streakCount),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            batch.set(lbRef, lbPayload.filterValues { it != null })
            batch.commit().await()

            dao?.insertLocalXpEntry(
                LocalXpLedgerEntity(
                    idempotencyKey = safeKey,
                    userId = userId,
                    sourceType = sourceType,
                    sourceId = sourceId,
                    xpDelta = totalAwardedXp,
                    coinDelta = coinDelta,
                    description = description
                )
            )

            // Emit level-up celebration + notification
            if (newLevel > oldLevel) {
                GamificationEventBus.emit(CelebrationEvent.LevelUp(newLevel, newLevelTitle, newXp))
                notifications?.dispatch(
                    userId = userId,
                    category = NotificationCategory.XP,
                    title = "Level Up! Level $newLevel ($newLevelTitle)",
                    body = "You reached $newXp total XP through genuine study!",
                    actionRoute = "profile",
                    idempotencyKey = "lvl_${userId}_$newLevel"
                )
            }

            // Emit streak extension celebration + notification
            if (streakTransition != null && streakTransition.incrementedToday) {
                GamificationEventBus.emit(
                    CelebrationEvent.StreakExtended(streakTransition.newStreakCount, streakTransition.usedFreeze)
                )
                notifications?.dispatch(
                    userId = userId,
                    category = NotificationCategory.STUDY,
                    title = "${streakTransition.newStreakCount}-Day Study Streak!",
                    body = if (streakTransition.usedFreeze) {
                        "A Streak Freeze saved your streak! Keep studying daily."
                    } else {
                        "You kept your flame alive today (+$streakBonusXp bonus XP)."
                    },
                    actionRoute = "today",
                    idempotencyKey = "streak_${userId}_$todayKey"
                )
            }

            // Progress personal goals & active assignments
            progressCloudGoalsAndAssignments(
                userId = userId,
                classId = profile.classId,
                subjectId = subjectId,
                studyMinutesDelta = studyMinutesDelta,
                questionsSolvedDelta = questionsSolvedDelta,
                lessonsDelta = lessonsDelta,
                sessionsDelta = sessionsDelta,
                examDelta = if (examScoreDelta > 0L) 1L else 0L
            )

            // Evaluate & unlock badges
            evaluateAndSyncBadges(
                userId = userId,
                profile = profile.copy(
                    totalXp = newXp,
                    streakCount = streakTransition?.newStreakCount ?: profile.streakCount,
                    totalFocusMinutes = profile.totalFocusMinutes + studyMinutesDelta
                ),
                perfectExamFlag = perfectExam,
                highScoreExamFlag = highScoreExam
            )

            Result.success(totalAwardedXp)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ledgerRef.path)
            Result.failure(e)
        }
    }

    private suspend fun applyDailyAntiFarmingCap(
        userId: String,
        sourceType: String,
        rawXp: Long,
        rules: XpRuleConfig,
        todayKey: String
    ): Long {
        val base = rawXp.coerceIn(0L, 1000L)
        if (sourceType !in listOf("focus_session", "pomodoro", "community_post", "community_comment")) {
            return base
        }
        val cap = if (sourceType.startsWith("community")) rules.maxDailyCommunityXp else rules.maxDailyFocusXp
        val existingTodayXp = runCatching {
            db.collection("users").document(userId).collection("xp_ledger")
                .whereEqualTo("sourceType", sourceType)
                .get().await().documents
                .filter { (it.getString("idempotencyKey") ?: "").contains(todayKey) }
                .sumOf { it.getLong("xpDelta") ?: 0L }
        }.getOrDefault(0L)
        val remaining = (cap - existingTodayXp).coerceAtLeast(0L)
        return base.coerceAtMost(remaining)
    }

    suspend fun evaluateAcademicYearLifecycle(userId: String): Result<ClassLifecycleDecision?> {
        if (userId.isBlank() || userId == GuestDataRepository.GUEST_UID) return Result.success(null)
        val ref = db.collection("public_profiles").document(userId)
        val lbRef = db.collection("leaderboard_entries").document(userId)
        return try {
            val snap = ref.get().await()
            if (!snap.exists()) return Result.success(null)
            val profile = snap.toObject(
                PublicProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: return Result.success(null)

            if (!profile.onboardingCompleted) return Result.success(null)

            val currentYear = GamificationCatalog.currentAcademicYear()
            val decision = computeClassPromotionDecision(profile, currentYear)

            if (decision.didPromote || decision.wasHeldBackForExamFailure || profile.classLockedAcademicYear == null) {
                val updates = mutableMapOf<String, Any?>(
                    "classId" to decision.updatedClassId,
                    "classLockedAcademicYear" to decision.lockedAcademicYear,
                    "annualExamStatus" to decision.annualExamStatus,
                    "promotedFromClassId" to decision.promotedFromClassId,
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                ref.update(updates).await()

                runCatching {
                    if (lbRef.get().await().exists()) {
                        lbRef.update(
                            mapOf(
                                "classId" to decision.updatedClassId,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }

                if (decision.didPromote && decision.promotedFromClassId != null) {
                    GamificationEventBus.emit(
                        CelebrationEvent.ClassPromoted(
                            fromClassId = decision.promotedFromClassId,
                            toClassId = decision.updatedClassId,
                            academicYear = currentYear
                        )
                    )
                    notifications?.dispatch(
                        userId = userId,
                        category = NotificationCategory.STUDY,
                        title = "Promoted to ${StudyQuestStrings.classLabel(decision.updatedClassId, AppLanguage.ENGLISH)}!",
                        body = "Happy New Academic Year ($currentYear)! Your locked class has automatically promoted on Jan 1.",
                        actionRoute = "learn",
                        idempotencyKey = "promo_${userId}_$currentYear"
                    )
                } else if (decision.wasHeldBackForExamFailure) {
                    notifications?.dispatch(
                        userId = userId,
                        category = NotificationCategory.EXAM,
                        title = "Promotion Exam Required",
                        body = "Your annual final exam was marked failed, so you remain in ${StudyQuestStrings.classLabel(decision.updatedClassId, AppLanguage.ENGLISH)} until you pass the Promotion Exam retake.",
                        actionRoute = "learn",
                        idempotencyKey = "holdback_${userId}_$currentYear"
                    )
                }
            }
            Result.success(decision)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Academic lifecycle evaluation skipped: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun recordAnnualPromotionExamOutcome(
        userId: String,
        passed: Boolean,
        score: Double,
        totalMarks: Double
    ): Result<Boolean> {
        val accuracy = if (totalMarks > 0) (score / totalMarks).coerceIn(0.0, 1.0) else 0.0
        val profileRef = db.collection("public_profiles").document(userId)
        return try {
            val snap = profileRef.get().await()
            val profile = snap.toObject(
                PublicProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: return Result.failure(IllegalStateException("Profile not found"))

            val wasFailed = profile.annualExamStatus == "failed"
            val nextClass = StudyQuestStrings.nextClassId(profile.classId)
            val shouldPromoteNow = passed && wasFailed && nextClass != null

            val updates = mutableMapOf<String, Any>(
                "annualExamStatus" to if (passed) "passed" else "failed",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (shouldPromoteNow && nextClass != null) {
                updates["classId"] = nextClass
                updates["promotedFromClassId"] = profile.classId
                updates["classLockedAcademicYear"] = GamificationCatalog.currentAcademicYear()
            }
            profileRef.update(updates).await()

            val recId = "promo_exam_${System.currentTimeMillis()}"
            db.collection("users").document(userId).collection("learning_records").document(recId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "recordType" to "promotion_exam",
                        "refId" to "annual_promotion_${profile.classId}",
                        "subjectId" to "all",
                        "score" to score,
                        "totalMarks" to totalMarks,
                        "accuracy" to accuracy,
                        "xpAwarded" to if (passed) 80L else 20L,
                        "idempotencyKey" to recId,
                        "summary" to if (passed) "Passed Annual Promotion Exam (${(accuracy * 100).toInt()}%)" else "Annual Promotion Exam Failed — Retake Required (${(accuracy * 100).toInt()}%)",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

            awardVerifiedActivity(
                userId = userId,
                sourceType = "promotion_exam",
                sourceId = profile.classId,
                idempotencyKey = recId,
                rawXpDelta = if (passed) 80L else 20L,
                coinDelta = if (passed) 40L else 5L,
                description = if (passed) "Passed Annual Promotion Exam" else "Attempted Annual Promotion Exam",
                examScoreDelta = score.toLong(),
                perfectExam = accuracy >= 0.999,
                highScoreExam = accuracy >= 0.85
            )

            if (shouldPromoteNow && nextClass != null) {
                GamificationEventBus.emit(
                    CelebrationEvent.ClassPromoted(profile.classId, nextClass, GamificationCatalog.currentAcademicYear())
                )
            }
            Result.success(shouldPromoteNow)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun purchaseStreakFreeze(userId: String): Result<Unit> {
        if (userId == GuestDataRepository.GUEST_UID) {
            return Result.failure(IllegalStateException("Sign in with Google to use cloud Streak Freezes"))
        }
        val profileRef = db.collection("public_profiles").document(userId)
        val cost = 40L
        val txId = "freeze_${System.currentTimeMillis()}"
        return try {
            val snap = profileRef.get().await()
            val coins = snap.getLong("coins") ?: 0L
            val freezes = snap.getLong("streakFreezeCount") ?: 0L
            if (freezes >= 5L) return Result.failure(IllegalStateException("Max 5 Streak Freezes held"))
            if (coins < cost) return Result.failure(IllegalStateException("Need 40 coins for Streak Freeze"))

            val batch = db.batch()
            batch.update(
                profileRef,
                mapOf(
                    "coins" to FieldValue.increment(-cost),
                    "streakFreezeCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                db.collection("users").document(userId).collection("xp_ledger").document(txId),
                mapOf(
                    "userId" to userId,
                    "sourceType" to "streak_freeze",
                    "sourceId" to "streak_freeze_item",
                    "xpDelta" to 0L,
                    "coinDelta" to -cost,
                    "description" to "Purchased 1 Streak Freeze",
                    "idempotencyKey" to txId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun createStudyGoal(
        userId: String,
        title: String,
        goalType: String,
        periodType: String,
        subjectId: String,
        targetValue: Long,
        difficulty: String
    ): Result<Unit> {
        val cleanTitle = title.trim().take(118)
        if (cleanTitle.isBlank()) return Result.failure(IllegalArgumentException("Goal title is required"))
        if (goalType == "time_minutes" && targetValue < 5L) {
            return Result.failure(IllegalArgumentException("Minimum time goal is 5 minutes"))
        }
        if (targetValue < 1L) return Result.failure(IllegalArgumentException("Invalid target value"))

        val xpReward = when (difficulty) {
            "hard" -> 90L
            "medium" -> 55L
            else -> 35L
        }
        val periodKey = when (periodType) {
            "weekly" -> GamificationCatalog.currentWeekKey()
            else -> GamificationCatalog.currentDayKey()
        }
        val deadline = System.currentTimeMillis() + if (periodType == "weekly") 7L * 86_400_000L else 86_400_000L
        val goalId = "goal_${UUID.randomUUID().toString().take(10)}"

        if (userId == GuestDataRepository.GUEST_UID) {
            dao?.upsertLocalStudyGoal(
                LocalStudyGoalEntity(
                    id = goalId,
                    userId = userId,
                    title = cleanTitle,
                    goalType = goalType,
                    periodType = periodType,
                    subjectId = subjectId,
                    targetValue = targetValue,
                    currentValue = 0L,
                    difficulty = difficulty,
                    xpReward = xpReward,
                    completed = false,
                    rewardClaimed = false,
                    deadlineEpochMs = deadline,
                    periodKey = periodKey
                )
            )
            return Result.success(Unit)
        }

        val ref = db.collection("users").document(userId).collection("study_goals").document(goalId)
        return try {
            ref.set(
                mapOf(
                    "userId" to userId,
                    "title" to cleanTitle,
                    "goalType" to goalType,
                    "periodType" to periodType,
                    "subjectId" to subjectId,
                    "targetValue" to targetValue,
                    "currentValue" to 0L,
                    "difficulty" to difficulty,
                    "xpReward" to xpReward,
                    "completed" to false,
                    "rewardClaimed" to false,
                    "deadlineEpochMs" to deadline,
                    "periodKey" to periodKey,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun claimGoalReward(userId: String, goal: StudyGoalDoc): Result<Long> {
        if (!goal.completed || goal.rewardClaimed) {
            return Result.failure(IllegalStateException("Goal not ready to claim"))
        }
        if (userId == GuestDataRepository.GUEST_UID) {
            dao?.upsertLocalStudyGoal(
                LocalStudyGoalEntity(
                    id = goal.id,
                    userId = userId,
                    title = goal.title,
                    goalType = goal.goalType,
                    periodType = goal.periodType,
                    subjectId = goal.subjectId,
                    targetValue = goal.targetValue,
                    currentValue = goal.currentValue,
                    difficulty = goal.difficulty,
                    xpReward = goal.xpReward,
                    completed = true,
                    rewardClaimed = true,
                    deadlineEpochMs = goal.deadlineEpochMs,
                    periodKey = goal.periodKey
                )
            )
            return awardVerifiedActivity(
                userId = userId,
                sourceType = "goal_reward",
                sourceId = goal.id,
                idempotencyKey = "goal_${goal.id}",
                rawXpDelta = goal.xpReward,
                coinDelta = 15L,
                description = "Completed Personal Goal: ${goal.title}"
            )
        }

        val ref = db.collection("users").document(userId).collection("study_goals").document(goal.id)
        return try {
            ref.update(
                mapOf(
                    "rewardClaimed" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            awardVerifiedActivity(
                userId = userId,
                sourceType = "goal_reward",
                sourceId = goal.id,
                idempotencyKey = "goal_${goal.id}",
                rawXpDelta = goal.xpReward,
                coinDelta = 15L,
                description = "Completed Personal Goal: ${goal.title}"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun deleteStudyGoal(userId: String, goalId: String) {
        if (userId == GuestDataRepository.GUEST_UID) {
            dao?.deleteLocalStudyGoal(goalId)
        } else {
            runCatching {
                db.collection("users").document(userId).collection("study_goals").document(goalId).delete().await()
            }
        }
    }

    suspend fun submitAssignmentCompletion(
        userId: String,
        assignment: AssignmentDefinitionDoc,
        score: Double = 100.0
    ): Result<Long> {
        if (userId == GuestDataRepository.GUEST_UID) {
            return Result.failure(IllegalStateException("Sign in with Google to submit cloud assignments"))
        }
        val subRef = db.collection("users").document(userId)
            .collection("assignment_submissions").document(assignment.id)
        return try {
            val existing = subRef.get().await()
            if (existing.exists() && existing.getBoolean("completed") == true) {
                return Result.success(0L)
            }
            subRef.set(
                mapOf(
                    "userId" to userId,
                    "assignmentId" to assignment.id,
                    "periodKey" to assignment.periodKey,
                    "progress" to assignment.targetCount,
                    "target" to assignment.targetCount,
                    "completed" to true,
                    "score" to score,
                    "xpAwarded" to assignment.xpReward,
                    "submittedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            notifications?.dispatch(
                userId = userId,
                category = NotificationCategory.ASSIGNMENT,
                title = "Assignment Completed!",
                body = "${assignment.titleEn} (+${assignment.xpReward} XP)",
                actionRoute = "assignments",
                idempotencyKey = "assign_${assignment.id}"
            )

            awardVerifiedActivity(
                userId = userId,
                sourceType = "assignment",
                sourceId = assignment.id,
                idempotencyKey = "assign_${userId}_${assignment.id}",
                rawXpDelta = assignment.xpReward,
                coinDelta = assignment.coinReward,
                description = "Assignment: ${assignment.titleEn}",
                assignmentScoreDelta = assignment.xpReward,
                subjectId = assignment.subjectId
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, subRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleBadgeFavoriteOrPin(
        userId: String,
        badgeId: String,
        pinAsFeatured: Boolean = false,
        toggleFavorite: Boolean = false
    ) {
        if (userId == GuestDataRepository.GUEST_UID) return
        val badgeRef = db.collection("users").document(userId).collection("badges").document(badgeId)
        val profileRef = db.collection("public_profiles").document(userId)
        runCatching {
            val snap = badgeRef.get().await()
            if (!snap.exists()) return
            val currentFav = snap.getBoolean("favorite") ?: false
            val currentPin = snap.getBoolean("pinned") ?: false
            val updates = mutableMapOf<String, Any>("updatedAt" to FieldValue.serverTimestamp())
            if (toggleFavorite) updates["favorite"] = !currentFav
            if (pinAsFeatured) updates["pinned"] = !currentPin
            badgeRef.update(updates).await()

            if (pinAsFeatured) {
                val allPinned = db.collection("users").document(userId).collection("badges")
                    .whereEqualTo("pinned", true).get().await().documents.map { it.id }.take(6)
                profileRef.update(
                    mapOf(
                        "featuredBadgeId" to (allPinned.firstOrNull() ?: badgeId),
                        "pinnedBadgeIds" to allPinned,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }
    }

    suspend fun markBadgeAnimationShown(userId: String, badgeId: String) {
        if (userId == GuestDataRepository.GUEST_UID) return
        runCatching {
            db.collection("users").document(userId).collection("badges").document(badgeId)
                .update(mapOf("animationShown" to true, "updatedAt" to FieldValue.serverTimestamp())).await()
        }
    }

    private suspend fun progressLocalGoals(
        userId: String,
        studyMinutesDelta: Long,
        questionsSolvedDelta: Long,
        lessonsDelta: Long,
        sessionsDelta: Long,
        subjectId: String
    ) {
        val goals = dao?.getLocalStudyGoals(userId).orEmpty()
        for (goal in goals) {
            if (goal.completed) continue
            if (goal.subjectId != "all" && subjectId != "all" && goal.subjectId != subjectId) continue
            val delta = when (goal.goalType) {
                "time_minutes" -> studyMinutesDelta
                "questions_solved" -> questionsSolvedDelta
                "chapters_finished" -> lessonsDelta
                "sessions_count" -> sessionsDelta
                else -> 0L
            }
            if (delta <= 0L) continue
            val next = (goal.currentValue + delta).coerceAtMost(goal.targetValue)
            val nowDone = next >= goal.targetValue
            dao?.upsertLocalStudyGoal(goal.copy(currentValue = next, completed = nowDone))
            if (nowDone) {
                GamificationEventBus.emit(CelebrationEvent.GoalCompleted(goal.title, goal.xpReward))
            }
        }
    }

    private suspend fun progressCloudGoalsAndAssignments(
        userId: String,
        classId: String,
        subjectId: String,
        studyMinutesDelta: Long,
        questionsSolvedDelta: Long,
        lessonsDelta: Long,
        sessionsDelta: Long,
        examDelta: Long
    ) {
        runCatching {
            val goalsSnap = db.collection("users").document(userId).collection("study_goals").get().await()
            for (doc in goalsSnap.documents) {
                if (doc.getBoolean("completed") == true) continue
                val goalSubject = doc.getString("subjectId") ?: "all"
                if (goalSubject != "all" && subjectId != "all" && goalSubject != subjectId) continue
                val gType = doc.getString("goalType") ?: continue
                val target = doc.getLong("targetValue") ?: 1L
                val cur = doc.getLong("currentValue") ?: 0L
                val delta = when (gType) {
                    "time_minutes" -> studyMinutesDelta
                    "questions_solved" -> questionsSolvedDelta
                    "chapters_finished" -> lessonsDelta
                    "sessions_count" -> sessionsDelta
                    else -> 0L
                }
                if (delta <= 0L) continue
                val next = (cur + delta).coerceAtMost(target)
                val nowDone = next >= target
                doc.reference.update(
                    mapOf(
                        "currentValue" to next,
                        "completed" to nowDone,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                if (nowDone) {
                    val title = doc.getString("title") ?: "Study Goal"
                    val xp = doc.getLong("xpReward") ?: 40L
                    GamificationEventBus.emit(CelebrationEvent.GoalCompleted(title, xp))
                }
            }
        }

        // Also advance matching automated assignments cleanly
        runCatching {
            val activeAssignments = GamificationCatalog.defaultAssignmentsForClass(classId)
            for (assign in activeAssignments) {
                val delta = when (assign.assignmentType) {
                    "daily" -> if (questionsSolvedDelta > 0L) 1L else 0L
                    "weekly" -> lessonsDelta
                    "two_week" -> if (questionsSolvedDelta > 0L || examDelta > 0L) 1L else 0L
                    "monthly" -> sessionsDelta + examDelta
                    else -> 0L
                }
                if (delta <= 0L) continue
                val subRef = db.collection("users").document(userId)
                    .collection("assignment_submissions").document(assign.id)
                val snap = subRef.get().await()
                if (snap.exists() && snap.getBoolean("completed") == true) continue
                val cur = snap.getLong("progress") ?: 0L
                val next = (cur + delta).coerceAtMost(assign.targetCount)
                val completed = next >= assign.targetCount
                if (completed) {
                    submitAssignmentCompletion(userId, assign, score = 100.0)
                } else {
                    subRef.set(
                        mapOf(
                            "userId" to userId,
                            "assignmentId" to assign.id,
                            "periodKey" to assign.periodKey,
                            "progress" to next,
                            "target" to assign.targetCount,
                            "completed" to false,
                            "score" to 0.0,
                            "xpAwarded" to 0L,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
        }
    }

    suspend fun evaluateAndSyncBadges(
        userId: String,
        profile: PublicProfile,
        perfectExamFlag: Boolean = false,
        highScoreExamFlag: Boolean = false
    ) {
        if (userId == GuestDataRepository.GUEST_UID) return
        try {
            val sessions = db.collection("users").document(userId).collection("study_sessions").get().await()
            val records = db.collection("users").document(userId).collection("learning_records").get().await()
            val goals = db.collection("users").document(userId).collection("study_goals").get().await()
            val assignments = db.collection("users").document(userId).collection("assignment_submissions").get().await()
            val existingBadges = db.collection("users").document(userId).collection("badges").get().await()
                .documents.associateBy { it.id }

            val recDocs = records.documents
            val metrics = mapOf(
                "study_sessions" to sessions.size().toLong().coerceAtLeast(
                    recDocs.count { it.getString("recordType") in listOf("pomodoro", "focus_session") }.toLong()
                ),
                "focus_minutes" to profile.totalFocusMinutes,
                "lessons_completed" to recDocs.count { it.getString("recordType") == "lesson" }.toLong(),
                "streak_days" to profile.streakCount,
                "assignments_completed" to assignments.documents.count { it.getBoolean("completed") == true }.toLong(),
                "exams_completed" to recDocs.count { it.getString("recordType") in listOf("exam", "promotion_exam") }.toLong(),
                "high_score_exams" to (recDocs.count { (it.getString("recordType") in listOf("exam", "promotion_exam")) && (it.getDouble("accuracy") ?: 0.0) >= 0.85 }.toLong() + if (highScoreExamFlag) 1L else 0L).coerceAtMost(100L),
                "perfect_exams" to (recDocs.count { (it.getString("recordType") in listOf("exam", "promotion_exam")) && (it.getDouble("accuracy") ?: 0.0) >= 0.999 }.toLong() + if (perfectExamFlag) 1L else 0L).coerceAtMost(100L),
                "goals_completed" to goals.documents.count { it.getBoolean("completed") == true }.toLong(),
                "challenges_completed" to recDocs.count { it.getString("recordType") == "daily_challenge" }.toLong(),
                "podium_finishes" to profile.unlockedTrophyTitles.size.toLong(),
                "ai_videos_completed" to recDocs.count { it.getString("recordType") == "ai_video" }.toLong(),
                "community_contributions" to recDocs.count { it.getString("recordType") == "community" }.toLong(),
                "promotion_passes" to (if (profile.annualExamStatus == "passed" || profile.promotedFromClassId != null) 1L else 0L)
            )

            for (def in GamificationCatalog.badgesCatalog) {
                val curVal = metrics[def.metricKey] ?: 0L
                val clamped = curVal.coerceAtMost(def.target)
                val shouldUnlock = clamped >= def.target
                val existingDoc = existingBadges[def.id]
                val wasUnlocked = existingDoc?.getBoolean("unlocked") == true
                val prevProgress = existingDoc?.getLong("progress") ?: -1L

                if (existingDoc == null || prevProgress != clamped || shouldUnlock != wasUnlocked) {
                    val badgeRef = db.collection("users").document(userId).collection("badges").document(def.id)
                    val payload = mutableMapOf<String, Any?>(
                        "userId" to userId,
                        "badgeId" to def.id,
                        "category" to def.category.id,
                        "rarity" to def.rarity.id,
                        "progress" to clamped,
                        "target" to def.target,
                        "unlocked" to (wasUnlocked || shouldUnlock),
                        "pinned" to (existingDoc?.getBoolean("pinned") ?: false),
                        "favorite" to (existingDoc?.getBoolean("favorite") ?: false),
                        "animationShown" to (existingDoc?.getBoolean("animationShown") ?: false),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    if (!wasUnlocked && shouldUnlock) {
                        payload["unlockedAt"] = FieldValue.serverTimestamp()
                        payload["animationShown"] = false
                    }
                    badgeRef.set(payload.filterValues { it != null }).await()

                    if (!wasUnlocked && shouldUnlock) {
                        GamificationEventBus.emit(CelebrationEvent.BadgeUnlocked(def))
                        notifications?.dispatch(
                            userId = userId,
                            category = NotificationCategory.BADGE,
                            title = "Badge Unlocked: ${def.titleEn}!",
                            body = "${def.descriptionEn} (+${def.xpBonus} XP bonus)",
                            actionRoute = "badges",
                            idempotencyKey = "badge_unlock_${userId}_${def.id}"
                        )
                        // Grant one-time badge XP bonus via idempotent ledger
                        val bonusKey = "badge_bonus_${userId}_${def.id}"
                        val bonusRef = db.collection("users").document(userId).collection("xp_ledger").document(bonusKey)
                        if (!bonusRef.get().await().exists() && def.xpBonus > 0L) {
                            val pRef = db.collection("public_profiles").document(userId)
                            val bBatch = db.batch()
                            bBatch.set(
                                bonusRef,
                                mapOf(
                                    "userId" to userId,
                                    "sourceType" to "badge_unlock",
                                    "sourceId" to def.id,
                                    "xpDelta" to def.xpBonus,
                                    "coinDelta" to 15L,
                                    "description" to "Unlocked ${def.rarity.labelEn} Badge: ${def.titleEn}",
                                    "idempotencyKey" to bonusKey,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                            )
                            bBatch.update(
                                pRef,
                                mapOf(
                                    "totalXp" to FieldValue.increment(def.xpBonus),
                                    "coins" to FieldValue.increment(15L),
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                            )
                            bBatch.commit().await()
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Badge evaluation warning: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "GamificationEngine"

        fun computeStreakTransition(
            currentStreak: Long,
            lastDateKey: String?,
            historyKeys: List<String>,
            freezeCount: Long,
            todayKey: String,
            yesterdayKey: String
        ): StreakTransitionResult {
            if (lastDateKey == todayKey) {
                val updatedHistory = (historyKeys + todayKey).distinct().takeLast(30)
                return StreakTransitionResult(
                    newStreakCount = currentStreak.coerceAtLeast(1L),
                    newLastDateKey = todayKey,
                    newHistoryKeys = updatedHistory,
                    remainingFreezes = freezeCount,
                    usedFreeze = false,
                    incrementedToday = false
                )
            }

            var usedFreeze = false
            var remainingFreezes = freezeCount
            val nextStreak = when {
                lastDateKey == yesterdayKey -> currentStreak + 1L
                !lastDateKey.isNullOrBlank() && currentStreak > 0L && freezeCount > 0L -> {
                    // Check if missed exactly 1 day or recovered with Streak Freeze
                    usedFreeze = true
                    remainingFreezes = (freezeCount - 1L).coerceAtLeast(0L)
                    currentStreak + 1L
                }
                else -> 1L
            }
            val updatedHistory = (historyKeys + todayKey).distinct().takeLast(30)
            return StreakTransitionResult(
                newStreakCount = nextStreak,
                newLastDateKey = todayKey,
                newHistoryKeys = updatedHistory,
                remainingFreezes = remainingFreezes,
                usedFreeze = usedFreeze,
                incrementedToday = true
            )
        }

        fun computeClassPromotionDecision(
            profile: PublicProfile,
            currentAcademicYear: Int
        ): ClassLifecycleDecision {
            val lockedYear = (profile.classLockedAcademicYear ?: currentAcademicYear.toLong()).toInt()
            val examStatus = profile.annualExamStatus ?: "pending"

            if (profile.classLockedAcademicYear == null) {
                return ClassLifecycleDecision(
                    updatedClassId = profile.classId,
                    lockedAcademicYear = currentAcademicYear,
                    annualExamStatus = examStatus,
                    promotedFromClassId = profile.promotedFromClassId,
                    didPromote = false,
                    wasHeldBackForExamFailure = false
                )
            }

            // New academic year started (Jan 1 Asia/Dhaka) -> optimize for late joiners & promote unless annual exam failed
            if (currentAcademicYear > lockedYear) {
                if (examStatus == "failed") {
                    return ClassLifecycleDecision(
                        updatedClassId = profile.classId,
                        lockedAcademicYear = currentAcademicYear,
                        annualExamStatus = "failed",
                        promotedFromClassId = profile.promotedFromClassId,
                        didPromote = false,
                        wasHeldBackForExamFailure = true
                    )
                }
                val promotedClass = StudyQuestStrings.nextClassId(profile.classId)
                if (promotedClass != null) {
                    return ClassLifecycleDecision(
                        updatedClassId = promotedClass,
                        lockedAcademicYear = currentAcademicYear,
                        annualExamStatus = "pending",
                        promotedFromClassId = profile.classId,
                        didPromote = true,
                        wasHeldBackForExamFailure = false
                    )
                }
                return ClassLifecycleDecision(
                    updatedClassId = profile.classId,
                    lockedAcademicYear = currentAcademicYear,
                    annualExamStatus = examStatus,
                    promotedFromClassId = profile.promotedFromClassId,
                    didPromote = false,
                    wasHeldBackForExamFailure = false
                )
            }

            return ClassLifecycleDecision(
                updatedClassId = profile.classId,
                lockedAcademicYear = lockedYear,
                annualExamStatus = examStatus,
                promotedFromClassId = profile.promotedFromClassId,
                didPromote = false,
                wasHeldBackForExamFailure = false
            )
        }
    }
}
