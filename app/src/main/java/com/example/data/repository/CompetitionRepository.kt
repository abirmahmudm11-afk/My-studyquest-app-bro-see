package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.MatchDoc
import com.example.data.model.MatchRequestDoc
import com.example.data.model.MatchSubmissionDoc
import com.example.data.model.PublicProfile
import com.example.data.model.QuestionType
import com.example.data.model.TournamentDoc
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

import com.example.data.curriculum.GamificationCatalog
import com.example.data.model.CompetitionArchiveDoc

enum class LeaderboardCategory(val id: String, val labelEn: String, val labelBn: String) {
    XP("xp", "XP", "অর্জিত XP"),
    STUDY_TIME("study_time", "Study Time", "অধ্যয়ন সময়"),
    EXAM("exam", "Exam", "পরীক্ষা স্কোর"),
    ASSIGNMENT("assignment", "Assignment", "অ্যাসাইনমেন্ট"),
    CHALLENGE("challenge", "Challenge", "চ্যালেঞ্জ")
}

enum class LeaderboardPeriod(val id: String, val labelEn: String, val labelBn: String) {
    WEEKLY("weekly", "Weekly", "সাপ্তাহিক"),
    BIWEEKLY("biweekly", "Two-Week", "পাক্ষিক"),
    MONTHLY("monthly", "Monthly", "মাসিক")
}

class CompetitionRepository(
    private val db: FirebaseFirestore,
    private val gamificationEngine: GamificationEngine? = null
) {
    constructor(context: Context, gamificationEngine: GamificationEngine? = null) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        gamificationEngine
    )

    fun observeMyMatches(userId: String): Flow<List<MatchDoc>> {
        val playerFlow = db.collection("matches")
            .whereArrayContains("playerUids", userId)
            .snapshots()
            .map {
                it.toObjects(
                    MatchDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
        val invitedFlow = db.collection("matches")
            .whereArrayContains("invitedUids", userId)
            .snapshots()
            .map {
                it.toObjects(
                    MatchDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
        return combine(playerFlow, invitedFlow) { played, invited ->
            (played + invited)
                .distinctBy { it.id }
                .sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
        }
    }

    fun observeMatch(matchId: String): Flow<MatchDoc?> =
        db.collection("matches").document(matchId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(
                        MatchDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else null
            }

    fun observeMatchRequest(requestId: String): Flow<MatchRequestDoc?> =
        db.collection("match_requests").document(requestId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(
                        MatchRequestDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else null
            }

    fun observeSubmissions(matchId: String, userId: String): Flow<List<MatchSubmissionDoc>> =
        db.collection("matches").document(matchId).collection("submissions")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map {
                it.toObjects(
                    MatchSubmissionDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    fun observeLeaderboard(): Flow<List<LeaderboardEntryDoc>> =
        db.collection("leaderboard_entries")
            .orderBy("competitionRating", Query.Direction.DESCENDING)
            .limit(150)
            .snapshots()
            .map {
                it.toObjects(
                    LeaderboardEntryDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    fun observeCompetitionArchives(): Flow<List<CompetitionArchiveDoc>> =
        db.collection("competition_archives")
            .snapshots()
            .map {
                it.toObjects(
                    CompetitionArchiveDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { doc -> doc.archivedAt?.seconds ?: 0L }
            }

    fun observeTournaments(): Flow<List<TournamentDoc>> =
        db.collection("tournaments")
            .snapshots()
            .map {
                it.toObjects(
                    TournamentDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { t -> t.createdAt?.seconds ?: 0L }
            }

    suspend fun archivePreviousPeriodIfNeeded(entries: List<LeaderboardEntryDoc>, callerUid: String) {
        if (entries.isEmpty()) return
        val prevWeek = GamificationCatalog.previousWeekKey()
        for (cat in listOf(LeaderboardCategory.XP, LeaderboardCategory.STUDY_TIME, LeaderboardCategory.EXAM)) {
            val archiveId = "${prevWeek}_${cat.id}"
            val ref = db.collection("competition_archives").document(archiveId)
            runCatching {
                if (!ref.get().await().exists()) {
                    val top3 = entries
                        .filter { it.weekKey == prevWeek || scoreFor(it, cat, LeaderboardPeriod.WEEKLY) > 0L }
                        .sortedByDescending { scoreFor(it, cat, LeaderboardPeriod.WEEKLY) }
                        .take(3)
                    if (top3.isNotEmpty()) {
                        val winnerMaps = top3.mapIndexed { idx, entry ->
                            mapOf(
                                "rank" to (idx + 1).toLong(),
                                "userId" to entry.userId,
                                "displayName" to entry.displayName,
                                "studyQuestId" to entry.studyQuestId,
                                "classId" to entry.classId,
                                "score" to scoreFor(entry, cat, LeaderboardPeriod.WEEKLY)
                            )
                        }
                        ref.set(
                            mapOf(
                                "periodKey" to archiveId,
                                "periodType" to "weekly",
                                "category" to cat.id,
                                "classId" to "all",
                                "winners" to winnerMaps,
                                "archivedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()

                        // Grant Trophy Title if the caller placed Top 3
                        val myRank = top3.indexOfFirst { it.userId == callerUid }
                        if (myRank >= 0) {
                            val trophy = "Top ${myRank + 1} • ${cat.labelEn} ($prevWeek)"
                            val pRef = db.collection("public_profiles").document(callerUid)
                            val existingTitles = pRef.get().await().get("unlockedTrophyTitles") as? List<*> ?: emptyList<String>()
                            if (trophy !in existingTitles) {
                                val updated = (existingTitles.mapNotNull { it?.toString() } + trophy).takeLast(30)
                                pRef.update(
                                    mapOf(
                                        "unlockedTrophyTitles" to updated,
                                        "updatedAt" to FieldValue.serverTimestamp()
                                    )
                                ).await()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Real Quick Battle matchmaking:
     * Checks waiting requests in Firestore. If another real student is waiting, pairs immediately into a match;
     * otherwise registers a waiting MatchRequestDoc (never fabricates fake bots).
     */
    suspend fun enterQuickBattleQueue(
        profile: PublicProfile,
        classId: String,
        subjectId: String,
        difficulty: String = "medium"
    ): Result<Pair<MatchRequestDoc?, MatchDoc?>> {
        val myUid = profile.userId
        return try {
            val waitingSnap = db.collection("match_requests")
                .whereEqualTo("status", "waiting")
                .limit(20)
                .get()
                .await()

            val opponentReq = waitingSnap.toObjects(
                MatchRequestDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ).firstOrNull {
                it.userId != myUid && (it.subjectId == subjectId || it.classId == classId)
            }

            if (opponentReq != null) {
                val questionIds = pickMatchQuestionIds(classId, subjectId, 5)
                val matchId = "match_${UUID.randomUUID().toString().take(12)}"
                val matchRef = db.collection("matches").document(matchId)
                val reqRef = db.collection("match_requests").document(opponentReq.id)
                val players = listOf(opponentReq.userId, myUid)

                val batch = db.batch()
                batch.set(
                    matchRef,
                    mapOf(
                        "mode" to "quick_battle",
                        "hostUid" to myUid,
                        "classId" to classId,
                        "subjectId" to subjectId,
                        "difficulty" to difficulty,
                        "status" to "live",
                        "playerUids" to players,
                        "invitedUids" to emptyList<String>(),
                        "readyUids" to players,
                        "questionIds" to questionIds,
                        "questionCount" to questionIds.size.toLong(),
                        "durationSeconds" to 90L,
                        "scores" to mapOf(opponentReq.userId to 0L, myUid to 0L),
                        "startTime" to FieldValue.serverTimestamp(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    reqRef,
                    mapOf(
                        "status" to "matched",
                        "matchId" to matchId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()

                val createdMatch = matchRef.get().await().toObject(
                    MatchDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                return Result.success(null to createdMatch)
            }

            val reqId = "req_${myUid}_${System.currentTimeMillis()}"
            val reqRef = db.collection("match_requests").document(reqId)
            reqRef.set(
                mapOf(
                    "userId" to myUid,
                    "displayName" to profile.displayName.take(58),
                    "classId" to classId,
                    "subjectId" to subjectId,
                    "difficulty" to difficulty,
                    "status" to "waiting",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val reqDoc = reqRef.get().await().toObject(
                MatchRequestDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(reqDoc to null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "match_requests")
            Result.failure(e)
        }
    }

    suspend fun cancelMatchRequest(requestId: String): Result<Unit> {
        val ref = db.collection("match_requests").document(requestId)
        return try {
            ref.update(
                mapOf(
                    "status" to "cancelled",
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

    suspend fun createRoomOrChallenge(
        hostUid: String,
        mode: String, // "friend_challenge", "private_room", "group_battle"
        classId: String,
        subjectId: String,
        invitedUids: List<String> = emptyList(),
        questionCount: Int = 5
    ): Result<MatchDoc> {
        val matchId = "match_${UUID.randomUUID().toString().take(12)}"
        val roomCode = if (mode == "private_room") {
            (1..6).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"[Random.nextInt(32)] }.joinToString("")
        } else null
        val questionIds = pickMatchQuestionIds(classId, subjectId, questionCount)
        val matchRef = db.collection("matches").document(matchId)
        return try {
            matchRef.set(
                mapOf(
                    "mode" to mode,
                    "roomCode" to roomCode,
                    "hostUid" to hostUid,
                    "classId" to classId,
                    "subjectId" to subjectId,
                    "difficulty" to "medium",
                    "status" to "waiting",
                    "playerUids" to listOf(hostUid),
                    "invitedUids" to invitedUids.filter { it != hostUid }.distinct(),
                    "readyUids" to listOf(hostUid),
                    "questionIds" to questionIds,
                    "questionCount" to questionIds.size.toLong(),
                    "durationSeconds" to (questionIds.size * 20L).coerceAtLeast(60L),
                    "scores" to mapOf(hostUid to 0L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            ).await()
            val created = matchRef.get().await().toObject(
                MatchDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: MatchDoc(id = matchId, mode = mode, roomCode = roomCode, hostUid = hostUid)
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, matchRef.path)
            Result.failure(e)
        }
    }

    suspend fun joinPrivateRoomByCode(code: String, userId: String): Result<MatchDoc> {
        val cleanCode = code.trim().uppercase()
        return try {
            val snap = db.collection("matches")
                .whereEqualTo("mode", "private_room")
                .whereEqualTo("roomCode", cleanCode)
                .limit(5)
                .get()
                .await()
            val room = snap.toObjects(
                MatchDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ).firstOrNull { it.status in listOf("waiting", "lobby", "ready", "live") }
                ?: return Result.failure(IllegalStateException("Room not found or already closed"))

            if (userId !in room.playerUids) {
                val updatedPlayers = (room.playerUids + userId).distinct()
                val updatedReady = (room.readyUids + userId).distinct()
                db.collection("matches").document(room.id).update(
                    mapOf(
                        "playerUids" to updatedPlayers,
                        "readyUids" to updatedReady,
                        "status" to if (updatedPlayers.size >= 2) "live" else room.status,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            val refreshed = db.collection("matches").document(room.id).get().await()
                .toObject(MatchDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: room
            Result.success(refreshed)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "matches")
            Result.failure(e)
        }
    }

    suspend fun joinInvitedMatch(match: MatchDoc, userId: String): Result<MatchDoc> {
        val ref = db.collection("matches").document(match.id)
        return try {
            val players = (match.playerUids + userId).distinct()
            val ready = (match.readyUids + userId).distinct()
            ref.update(
                mapOf(
                    "playerUids" to players,
                    "readyUids" to ready,
                    "status" to if (players.size >= 2) "live" else match.status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val updated = ref.get().await().toObject(
                MatchDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: match
            Result.success(updated)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun startMatchNow(matchId: String): Result<Unit> {
        val ref = db.collection("matches").document(matchId)
        return try {
            ref.update(
                mapOf(
                    "status" to "live",
                    "startTime" to FieldValue.serverTimestamp(),
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

    suspend fun submitMatchAnswer(
        match: MatchDoc,
        userId: String,
        questionId: String,
        selectedIndex: Int,
        isCorrect: Boolean,
        responseTimeMs: Long
    ): Result<Long> {
        val submissionId = "${userId}_$questionId"
        val matchRef = db.collection("matches").document(match.id)
        val subRef = matchRef.collection("submissions").document(submissionId)
        return try {
            val existing = matchRef.collection("submissions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .firstOrNull { it.id == submissionId }
            if (existing != null) {
                return Result.success(existing.getLong("pointsAwarded") ?: 0L)
            }
            // Accuracy dominates (20 pts per correct answer) + modest speed tie-breaker (up to 5 pts)
            val points = if (isCorrect) {
                val speedBonus = ((15_000L - responseTimeMs.coerceIn(0L, 15_000L)) / 3000L).coerceIn(0L, 5L)
                20L + speedBonus
            } else {
                0L
            }
            val newScores = match.scores.toMutableMap()
            newScores[userId] = (newScores[userId] ?: 0L) + points

            val batch = db.batch()
            batch.set(
                subRef,
                mapOf(
                    "userId" to userId,
                    "matchId" to match.id,
                    "questionId" to questionId,
                    "selectedIndex" to selectedIndex.toLong(),
                    "isCorrect" to isCorrect,
                    "responseTimeMs" to responseTimeMs.coerceIn(0L, 300_000L),
                    "pointsAwarded" to points,
                    "submittedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                matchRef,
                mapOf(
                    "scores" to newScores,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(points)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, subRef.path)
            Result.failure(e)
        }
    }

    suspend fun finalizeMatchAndLeaderboard(
        match: MatchDoc,
        profile: PublicProfile
    ): Result<String?> {
        val matchRef = db.collection("matches").document(match.id)
        return try {
            val freshSnap = matchRef.get().await()
            val fresh = freshSnap.toObject(
                MatchDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: match

            val winnerUid = fresh.scores.entries
                .sortedByDescending { it.value }
                .firstOrNull()?.key

            if (fresh.status !in listOf("completed", "cancelled", "abandoned")) {
                matchRef.update(
                    mapOf(
                        "status" to "completed",
                        "winnerUid" to winnerUid,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Update caller's profile rating + leaderboard entry idempotently per match
            val ledgerId = "match_${match.id}_${profile.userId}"
            val ledgerRef = db.collection("users").document(profile.userId)
                .collection("xp_ledger").document(ledgerId)
            val alreadyProcessed = ledgerRef.get().await().exists()
            if (!alreadyProcessed) {
                val myScore = fresh.scores[profile.userId] ?: 0L
                val won = winnerUid == profile.userId && fresh.playerUids.size >= 2
                val ratingDelta = if (won) 24L else 8L
                val newRating = (profile.competitionRating + ratingDelta).coerceIn(0L, 10000L)
                val tier = computeRankTier(newRating)
                val xpEarned = (20L + (myScore / 2L)).coerceIn(10L, 120L)
                val coinsEarned = if (won) 25L else 10L
                val lbRef = db.collection("leaderboard_entries").document(profile.userId)
                val existingLb = lbRef.get().await()
                val weekKey = GamificationCatalog.currentWeekKey()
                val biweekKey = GamificationCatalog.currentBiweekKey()
                val monthKey = GamificationCatalog.currentMonthKey()
                val oldWeekly = if (existingLb.getString("weekKey") == weekKey) existingLb.getLong("weeklyPoints") ?: 0L else 0L
                val oldSeasonal = existingLb.getLong("seasonalPoints") ?: 0L
                val oldPlayed = existingLb.getLong("matchesPlayed") ?: 0L
                val oldWon = existingLb.getLong("matchesWon") ?: 0L

                val batch = db.batch()
                batch.set(
                    ledgerRef,
                    mapOf(
                        "userId" to profile.userId,
                        "sourceType" to "multiplayer_match",
                        "sourceId" to match.id,
                        "xpDelta" to xpEarned,
                        "coinDelta" to coinsEarned,
                        "description" to "Quiz Arena Match (${match.subjectId})",
                        "idempotencyKey" to ledgerId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    db.collection("public_profiles").document(profile.userId),
                    mapOf(
                        "totalXp" to FieldValue.increment(xpEarned),
                        "coins" to FieldValue.increment(coinsEarned),
                        "competitionRating" to newRating,
                        "rankTier" to tier,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.set(
                    lbRef,
                    mapOf(
                        "userId" to profile.userId,
                        "displayName" to profile.displayName,
                        "studyQuestId" to profile.studyQuestId,
                        "avatarId" to profile.avatarId,
                        "frameId" to profile.frameId,
                        "classId" to profile.classId,
                        "subjectId" to match.subjectId,
                        "seasonId" to "season_2026_11",
                        "weekKey" to weekKey,
                        "biweekKey" to biweekKey,
                        "monthKey" to monthKey,
                        "weeklyPoints" to (oldWeekly + xpEarned),
                        "seasonalPoints" to (oldSeasonal + xpEarned),
                        "weeklyXp" to ((if (existingLb.getString("weekKey") == weekKey) existingLb.getLong("weeklyXp") ?: 0L else 0L) + xpEarned),
                        "weeklyChallengeScore" to ((if (existingLb.getString("weekKey") == weekKey) existingLb.getLong("weeklyChallengeScore") ?: 0L else 0L) + myScore),
                        "biweeklyXp" to ((if (existingLb.getString("biweekKey") == biweekKey) existingLb.getLong("biweeklyXp") ?: 0L else 0L) + xpEarned),
                        "biweeklyChallengeScore" to ((if (existingLb.getString("biweekKey") == biweekKey) existingLb.getLong("biweeklyChallengeScore") ?: 0L else 0L) + myScore),
                        "monthlyXp" to ((if (existingLb.getString("monthKey") == monthKey) existingLb.getLong("monthlyXp") ?: 0L else 0L) + xpEarned),
                        "monthlyChallengeScore" to ((if (existingLb.getString("monthKey") == monthKey) existingLb.getLong("monthlyChallengeScore") ?: 0L else 0L) + myScore),
                        "competitionRating" to newRating,
                        "rankTier" to tier,
                        "optInNational" to profile.optInNationalLeaderboard,
                        "matchesPlayed" to (oldPlayed + 1L),
                        "matchesWon" to (oldWon + if (won) 1L else 0L),
                        "streakCount" to profile.streakCount,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ).filterValues { it != null }
                )
                batch.commit().await()
            }

            Result.success(winnerUid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, matchRef.path)
            Result.failure(e)
        }
    }

    suspend fun joinTournament(tournament: TournamentDoc, userId: String): Result<Unit> {
        val ref = db.collection("tournaments").document(tournament.id)
        return try {
            if (userId in tournament.participantUids) return Result.success(Unit)
            if (tournament.participantUids.size >= tournament.maxParticipants) {
                return Result.failure(IllegalStateException("Tournament is full"))
            }
            val updated = (tournament.participantUids + userId).distinct()
            ref.update(
                mapOf(
                    "participantUids" to updated,
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

    suspend fun purchaseCosmetic(
        profile: PublicProfile,
        cosmeticId: String,
        costCoins: Int
    ): Result<Unit> {
        if (profile.coins < costCoins) {
            return Result.failure(IllegalStateException("Insufficient coins"))
        }
        val ledgerId = "cosmetic_$cosmeticId"
        val ledgerRef = db.collection("users").document(profile.userId)
            .collection("xp_ledger").document(ledgerId)
        val profileRef = db.collection("public_profiles").document(profile.userId)
        return try {
            if (ledgerRef.get().await().exists()) return Result.success(Unit)
            val batch = db.batch()
            batch.set(
                ledgerRef,
                mapOf(
                    "userId" to profile.userId,
                    "sourceType" to "cosmetic_purchase",
                    "sourceId" to cosmeticId,
                    "xpDelta" to 0L,
                    "coinDelta" to (-costCoins).toLong(),
                    "idempotencyKey" to ledgerId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                profileRef,
                mapOf(
                    "coins" to (profile.coins - costCoins).coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ledgerRef.path)
            Result.failure(e)
        }
    }

    private fun pickMatchQuestionIds(classId: String, subjectId: String, count: Int): List<String> {
        val playable = BangladeshCurriculumCatalog.questions.filter {
            it.type in listOf(QuestionType.SINGLE_MCQ, QuestionType.TRUE_FALSE)
        }
        val subjectMatches = playable.filter { it.subjectId == subjectId && it.classId == classId }
            .ifEmpty { playable.filter { it.subjectId == subjectId } }
            .ifEmpty { playable }
        return (subjectMatches + playable)
            .distinctBy { it.id }
            .take(count)
            .map { it.id }
    }

    private fun computeRankTier(rating: Long): String = when {
        rating >= 1800L -> "Master"
        rating >= 1550L -> "Diamond"
        rating >= 1350L -> "Platinum"
        rating >= 1200L -> "Gold"
        rating >= 1080L -> "Silver"
        else -> "Bronze"
    }

    companion object {
        fun scoreFor(
            entry: LeaderboardEntryDoc,
            category: LeaderboardCategory,
            period: LeaderboardPeriod
        ): Long = when (period) {
            LeaderboardPeriod.WEEKLY -> when (category) {
                LeaderboardCategory.STUDY_TIME -> entry.weeklyStudyMinutes
                LeaderboardCategory.XP -> entry.weeklyXp.takeIf { it > 0L } ?: entry.weeklyPoints
                LeaderboardCategory.EXAM -> entry.weeklyExamScore
                LeaderboardCategory.ASSIGNMENT -> entry.weeklyAssignmentScore
                LeaderboardCategory.CHALLENGE -> entry.weeklyChallengeScore.takeIf { it > 0L } ?: entry.competitionRating
            }
            LeaderboardPeriod.BIWEEKLY -> when (category) {
                LeaderboardCategory.STUDY_TIME -> entry.biweeklyStudyMinutes
                LeaderboardCategory.XP -> entry.biweeklyXp.takeIf { it > 0L } ?: entry.weeklyPoints
                LeaderboardCategory.EXAM -> entry.biweeklyExamScore
                LeaderboardCategory.ASSIGNMENT -> entry.biweeklyAssignmentScore
                LeaderboardCategory.CHALLENGE -> entry.biweeklyChallengeScore.takeIf { it > 0L } ?: entry.competitionRating
            }
            LeaderboardPeriod.MONTHLY -> when (category) {
                LeaderboardCategory.STUDY_TIME -> entry.monthlyStudyMinutes
                LeaderboardCategory.XP -> entry.monthlyXp.takeIf { it > 0L } ?: entry.seasonalPoints
                LeaderboardCategory.EXAM -> entry.monthlyExamScore
                LeaderboardCategory.ASSIGNMENT -> entry.monthlyAssignmentScore
                LeaderboardCategory.CHALLENGE -> entry.monthlyChallengeScore.takeIf { it > 0L } ?: entry.competitionRating
            }
        }
    }
}
