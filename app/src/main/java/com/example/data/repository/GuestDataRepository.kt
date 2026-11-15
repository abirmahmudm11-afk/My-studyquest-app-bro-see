package com.example.data.repository

import android.content.Context
import com.example.core.AppLanguage
import com.example.core.LocalUserPreferences
import com.example.core.StudyQuestPreferences
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.local.LocalQuestProgressEntity
import com.example.data.local.LocalSoloMatchEntity
import com.example.data.local.LocalStudyCircleNoteEntity
import com.example.data.local.StudyQuestDao
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.PublicProfile
import com.example.data.model.TournamentDoc
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class GuestDataRepository(
    private val dao: StudyQuestDao,
    private val preferences: StudyQuestPreferences
) {
    fun buildGuestProfile(prefs: LocalUserPreferences): PublicProfile {
        val level = StudyQuestStrings.levelForXp(prefs.guestXp)
        val tier = when {
            prefs.guestRating >= 1800L -> "Master"
            prefs.guestRating >= 1550L -> "Diamond"
            prefs.guestRating >= 1350L -> "Platinum"
            prefs.guestRating >= 1200L -> "Gold"
            prefs.guestRating >= 1080L -> "Silver"
            else -> "Bronze"
        }
        return PublicProfile(
            id = GUEST_UID,
            userId = GUEST_UID,
            studyQuestId = "SQ-GUEST",
            username = "guest_learner",
            displayName = prefs.guestDisplayName.ifBlank { "Guest Scholar" },
            avatarId = prefs.guestAvatarId.ifBlank { "owl_classic" },
            frameId = prefs.guestFrameId.ifBlank { null },
            titleBadge = prefs.guestTitleBadge.ifBlank { null },
            classId = prefs.guestClassId.ifBlank { "class_10" },
            curriculumVersion = prefs.guestCurriculumVersion.ifBlank { "bangla" },
            overallLevel = level,
            levelTitle = StudyQuestStrings.levelTitle(level.toInt(), prefs.uiLanguage),
            totalXp = prefs.guestXp,
            coins = prefs.guestCoins,
            competitionRating = prefs.guestRating,
            rankTier = tier,
            optInNationalLeaderboard = false,
            showOnlineStatus = false,
            isOnline = true,
            onboardingCompleted = prefs.guestOnboardingCompleted,
            selectedSubjects = prefs.guestSelectedSubjectsCsv.split(",").filter { it.isNotBlank() },
            streakCount = prefs.guestStreakCount,
            lastChallengeDateKey = prefs.guestLastChallengeDateKey.ifBlank { null }
        )
    }

    fun observeGuestQuests(): Flow<List<UserQuestDoc>> =
        dao.observeLocalQuests().map { entities ->
            entities.map {
                UserQuestDoc(
                    id = it.questId,
                    userId = GUEST_UID,
                    questId = it.questId,
                    questType = it.questType,
                    progress = it.progress,
                    target = it.target,
                    completed = it.completed,
                    claimed = it.claimed,
                    xpReward = it.xpReward,
                    coinReward = it.coinReward,
                    periodKey = it.periodKey
                )
            }
        }

    suspend fun ensureGuestQuestsSeeded() {
        val dailyKey = AuthAndProfileRepository.currentDailyPeriodKey()
        val weeklyKey = AuthAndProfileRepository.currentWeeklyPeriodKey()
        for (item in BangladeshCurriculumCatalog.questsCatalog) {
            val periodKey = if (item.questType == "daily") dailyKey else weeklyKey
            val existing = dao.getLocalQuest(item.id)
            if (existing == null || existing.periodKey != periodKey) {
                dao.upsertLocalQuest(
                    LocalQuestProgressEntity(
                        questId = item.id,
                        questType = item.questType,
                        progress = 0L,
                        target = item.target.toLong(),
                        completed = false,
                        claimed = false,
                        xpReward = item.xpReward.toLong(),
                        coinReward = item.coinReward.toLong(),
                        periodKey = periodKey
                    )
                )
            }
        }
    }

    suspend fun incrementGuestQuest(recordType: String) {
        val questId = when (recordType) {
            "lesson" -> "q_daily_lesson"
            "practice" -> "q_daily_practice"
            "exam" -> "q_weekly_exam"
            "solo_arena" -> "q_daily_practice"
            else -> return
        }
        ensureGuestQuestsSeeded()
        val q = dao.getLocalQuest(questId) ?: return
        val next = (q.progress + 1L).coerceAtMost(q.target)
        dao.upsertLocalQuest(
            q.copy(
                progress = next,
                completed = next >= q.target,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun claimGuestQuest(questId: String): Result<Pair<Long, Long>> {
        val q = dao.getLocalQuest(questId)
            ?: return Result.failure(IllegalStateException("Quest not found"))
        if (!q.completed || q.claimed) {
            return Result.failure(IllegalStateException("Quest not claimable yet"))
        }
        dao.upsertLocalQuest(q.copy(claimed = true, updatedAtMs = System.currentTimeMillis()))
        preferences.addGuestReward(xpDelta = q.xpReward, coinDelta = q.coinReward)
        return Result.success(q.xpReward to q.coinReward)
    }

    fun observeSoloMatches(): Flow<List<LocalSoloMatchEntity>> = dao.observeSoloMatches()

    suspend fun recordSoloArenaResult(
        mode: String,
        classId: String,
        subjectId: String,
        title: String,
        correctCount: Int,
        totalQuestions: Int,
        scorePoints: Long
    ): Triple<Long, Long, Long> {
        val won = correctCount * 2 >= totalQuestions
        val xpEarned = (18L + correctCount * 5L).coerceIn(15L, 90L)
        val coinsEarned = if (won) 20L else 8L
        val ratingDelta = if (won) 18L else 6L
        dao.insertSoloMatch(
            LocalSoloMatchEntity(
                id = "solo_${UUID.randomUUID().toString().take(10)}",
                mode = mode,
                subjectId = subjectId,
                classId = classId,
                title = title,
                score = scorePoints,
                totalQuestions = totalQuestions,
                xpEarned = xpEarned,
                won = won
            )
        )
        preferences.addGuestReward(xpDelta = xpEarned, coinDelta = coinsEarned, ratingDelta = ratingDelta)
        incrementGuestQuest("practice")
        return Triple(xpEarned, coinsEarned, ratingDelta)
    }

    fun buildGuestLeaderboard(
        profile: PublicProfile,
        soloMatches: List<LocalSoloMatchEntity>
    ): List<LeaderboardEntryDoc> {
        val wins = soloMatches.count { it.won }.toLong()
        val guestEntry = LeaderboardEntryDoc(
            id = GUEST_UID,
            userId = GUEST_UID,
            displayName = "${profile.displayName} (You)",
            studyQuestId = profile.studyQuestId,
            avatarId = profile.avatarId,
            frameId = profile.frameId,
            classId = profile.classId,
            subjectId = profile.selectedSubjects.firstOrNull() ?: "math",
            seasonId = "season_local",
            weekKey = AuthAndProfileRepository.currentWeeklyPeriodKey(),
            weeklyPoints = profile.totalXp,
            seasonalPoints = profile.totalXp,
            weeklyStudyMinutes = profile.totalFocusMinutes,
            weeklyXp = profile.totalXp,
            weeklyChallengeScore = soloMatches.sumOf { it.score },
            biweeklyStudyMinutes = profile.totalFocusMinutes,
            biweeklyXp = profile.totalXp,
            biweeklyChallengeScore = soloMatches.sumOf { it.score },
            monthlyStudyMinutes = profile.totalFocusMinutes,
            monthlyXp = profile.totalXp,
            monthlyChallengeScore = soloMatches.sumOf { it.score },
            competitionRating = profile.competitionRating,
            rankTier = profile.rankTier,
            optInNational = true,
            matchesPlayed = soloMatches.size.toLong(),
            matchesWon = wins,
            streakCount = profile.streakCount
        )
        return listOf(guestEntry)
    }

    fun buildLocalTournaments(classId: String): List<TournamentDoc> = listOf(
        TournamentDoc(
            id = "guest_tourn_ssc_math",
            titleEn = "NCTB Speed Math Sprint (Solo Bracket)",
            titleBn = "এনসিটিবি দ্রুত গণিত স্প্রিন্ট (একক ব্র্যাকেট)",
            classId = classId,
            subjectId = "math",
            status = "registration",
            maxParticipants = 1,
            rewardCoins = 45L
        ),
        TournamentDoc(
            id = "guest_tourn_science",
            titleEn = "Science & ICT Concept Cup (Solo Bracket)",
            titleBn = "বিজ্ঞান ও আইসিটি কনসেপ্ট কাপ (একক ব্র্যাকেট)",
            classId = classId,
            subjectId = "science",
            status = "registration",
            maxParticipants = 1,
            rewardCoins = 40L
        )
    )

    fun observeStudyCircleNotes(channelId: String): Flow<List<LocalStudyCircleNoteEntity>> =
        dao.observeStudyCircleNotes(channelId)

    suspend fun ensureStudyCircleSeeded(language: AppLanguage) {
        if (dao.countStudyCircleNotes() > 0) return
        val now = System.currentTimeMillis()
        val initialNotes = listOf(
            LocalStudyCircleNoteEntity(
                id = "seed_math_1",
                channelId = "ssc_math",
                authorLabel = "Titu Study Buddy",
                isAiBuddy = true,
                text = if (language == AppLanguage.BANGLA)
                    "স্বাগতম! এখানে তোমার গণিত ও ত্রিকোণমিতির সূত্র নোট জমা রাখো অথবা নিচে লিখে AI টিটুর সাহায্য নাও: যেমন sin²θ + cos²θ = 1।"
                else
                    "Welcome to SSC Math Circle! Save formula notes here or ask Titu Study Buddy any concept question below (e.g., sin²θ + cos²θ = 1).",
                reactionsCsv = "📌,💡",
                createdAtMs = now - 3000L
            ),
            LocalStudyCircleNoteEntity(
                id = "seed_sci_1",
                channelId = "science_lab",
                authorLabel = "Titu Study Buddy",
                isAiBuddy = true,
                text = if (language == AppLanguage.BANGLA)
                    "পদার্থবিজ্ঞান টিপস: F = ma এবং v² = u² + 2as সমীকরণগুলো আলাদা করে লিখে অনুশীলন করলে CQ-তে পূর্ণ নম্বর পাওয়া সহজ হয়।"
                else
                    "Science Tip: Group F = ma and v² = u² + 2as with SI units before tackling Creative Questions (CQ) for full marks.",
                reactionsCsv = "🔥",
                createdAtMs = now - 2000L
            ),
            LocalStudyCircleNoteEntity(
                id = "seed_eng_1",
                channelId = "english_prep",
                authorLabel = "Titu Study Buddy",
                isAiBuddy = true,
                text = if (language == AppLanguage.BANGLA)
                    "ইংরেজি ও আইসিটি চ্যানেল: গুরুত্বপূর্ণ ভোকাবুলারি এবং বাইনারি-হেক্সাডেসিমেল কনভার্সন নোট এখানে লিখে রাখো!"
                else
                    "English & ICT Prep: Pin tough transformation rules and binary-hex conversion steps right here for quick revision.",
                reactionsCsv = "✅",
                createdAtMs = now - 1000L
            )
        )
        initialNotes.forEach { dao.upsertStudyCircleNote(it) }
    }

    suspend fun postStudyCircleNote(
        channelId: String,
        authorLabel: String,
        text: String,
        isAiBuddy: Boolean = false
    ) {
        val clean = text.trim()
        if (clean.isBlank()) return
        dao.upsertStudyCircleNote(
            LocalStudyCircleNoteEntity(
                id = "note_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
                channelId = channelId,
                authorLabel = authorLabel,
                isAiBuddy = isAiBuddy,
                text = clean,
                createdAtMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleNoteReaction(note: LocalStudyCircleNoteEntity, emoji: String) {
        val set = note.reactionsCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (!set.add(emoji)) set.remove(emoji)
        dao.upsertStudyCircleNote(note.copy(reactionsCsv = set.joinToString(",")))
    }

    suspend fun deleteNote(noteId: String) {
        dao.deleteStudyCircleNote(noteId)
    }

    fun guestWelcomeNotifications(language: AppLanguage, prefs: LocalUserPreferences): List<UserNotificationDoc> = listOf(
        UserNotificationDoc(
            id = "guest_notif_1",
            userId = GUEST_UID,
            category = "system",
            title = StudyQuestStrings.t(language, "Guest Mode Active", "গেস্ট মোড চালু আছে"),
            body = StudyQuestStrings.t(
                language,
                "Your ${prefs.guestXp} XP & ${prefs.guestCoins} Coins are saved locally. Sign in with Google anytime to auto-merge!",
                "তোমার ${StudyQuestStrings.formatDigits(prefs.guestXp, language)} XP এবং কয়েন ডিভাইসে সংরক্ষিত আছে। গুগল দিয়ে সাইন-ইন করলে সব স্বয়ংক্রিয়ভাবে সিঙ্ক হবে!"
            ),
            read = false
        )
    )

    suspend fun exportGuestDataJson(prefs: LocalUserPreferences): String {
        val records = dao.getActionsForUser(GUEST_UID)
        val plans = dao.getUnsyncedStudyPlans(GUEST_UID)
        val solo = dao.getAllSoloMatches()
        return JSONObject().apply {
            put("mode", "guest_local")
            put("exportedAtEpochMs", System.currentTimeMillis())
            put("displayName", prefs.guestDisplayName)
            put("classId", prefs.guestClassId)
            put("curriculumVersion", prefs.guestCurriculumVersion)
            put("xp", prefs.guestXp)
            put("coins", prefs.guestCoins)
            put("rating", prefs.guestRating)
            put("learningRecords", JSONArray(records.map {
                JSONObject().apply {
                    put("type", it.actionType)
                    put("refId", it.refId)
                    put("subjectId", it.subjectId)
                    put("xp", it.xpAwarded)
                    put("summary", it.summary)
                }
            }))
            put("studyPlansCount", plans.size)
            put("soloMatchesCount", solo.size)
        }.toString(2)
    }

    companion object {
        const val GUEST_UID = "guest_local"
    }
}
