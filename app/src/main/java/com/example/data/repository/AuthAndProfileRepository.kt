package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.AppLanguage
import com.example.core.LocalUserPreferences
import com.example.core.OperationType
import com.example.core.StudyQuestStrings
import com.example.core.handleFirestoreError
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.GamificationCatalog
import com.example.data.local.StudyQuestDao
import com.example.data.model.PrivateAccountInfo
import com.example.data.model.PublicProfile
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

class AuthAndProfileRepository(
    private val db: FirebaseFirestore,
    private val dao: StudyQuestDao? = null
) {
    constructor(context: Context, dao: StudyQuestDao? = null) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        dao
    )

    fun observePublicProfile(userId: String): Flow<PublicProfile?> {
        val ref = db.collection("public_profiles").document(userId)
        return ref.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(
                    PublicProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }
        }
    }

    fun observeUserQuests(userId: String): Flow<List<UserQuestDoc>> =
        db.collection("users").document(userId).collection("quests")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    UserQuestDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    fun observeNotifications(userId: String): Flow<List<UserNotificationDoc>> =
        db.collection("users").document(userId).collection("notifications")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    UserNotificationDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeUnlockedCosmetics(userId: String): Flow<Set<String>> =
        db.collection("users").document(userId).collection("xp_ledger")
            .whereEqualTo("sourceType", "cosmetic_purchase")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.getString("sourceId") }.toSet() + setOf("owl_classic")
            }

    suspend fun ensureUserBootstrapped(
        user: FirebaseUser,
        defaultLanguage: AppLanguage
    ): Result<PublicProfile> {
        val uid = user.uid
        val profileRef = db.collection("public_profiles").document(uid)
        val privateRef = db.collection("users").document(uid).collection("private").document("account")
        return try {
            // Ensure Firebase Auth ID token is ready before issuing Firestore requests
            runCatching {
                withTimeout(8_000L) { user.getIdToken(false).await() }
            }

            val existing = withTimeout(20_000L) { profileRef.get().await() }
            if (existing.exists()) {
                val profile = existing.toObject(
                    PublicProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: return Result.failure(IllegalStateException("Profile unreadable"))
                if (profile.showOnlineStatus && !profile.isOnline) {
                    runCatching {
                        withTimeout(8_000L) {
                            profileRef.update(
                                mapOf(
                                    "isOnline" to true,
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                            ).await()
                        }
                    }
                }
                ensureActivePeriodQuests(uid)
                return Result.success(profile.copy(isOnline = if (profile.showOnlineStatus) true else profile.isOnline))
            }

            val rawName = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")
                ?: "Scholar"
            val displayName = rawName.trim().ifBlank { "Scholar" }.take(58)
            val sqCode = "SQ-" + Random.nextInt(100000, 999999)
            val baseHandle = rawName.lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9_]"), "")
                .ifBlank { "scholar" }
                .take(18)
            val username = "${baseHandle}_${sqCode.takeLast(4)}".take(30)
            val curriculum = if (defaultLanguage == AppLanguage.BANGLA) "bangla" else "english"
            val initialSubjects = listOf("math", "english", "bangla", "physics", "ict")

            val batch = db.batch()
            batch.set(
                profileRef,
                mapOf(
                    "userId" to uid,
                    "studyQuestId" to sqCode,
                    "username" to username,
                    "displayName" to displayName,
                    "avatarId" to "owl_classic",
                    "frameId" to null,
                    "titleBadge" to null,
                    "classId" to "class_10",
                    "curriculumVersion" to curriculum,
                    "overallLevel" to 1L,
                    "levelTitle" to "Beginner",
                    "totalXp" to 0L,
                    "coins" to 50L,
                    "competitionRating" to 1000L,
                    "rankTier" to "Bronze",
                    "optInNationalLeaderboard" to false,
                    "showOnlineStatus" to true,
                    "isOnline" to true,
                    "onboardingCompleted" to false,
                    "selectedSubjects" to initialSubjects,
                    "academicGroup" to "science",
                    "dailyGoalMinutes" to 30L,
                    "streakCount" to 0L,
                    "streakFreezeCount" to 1L,
                    "streakHistoryKeys" to emptyList<String>(),
                    "annualExamStatus" to "pending",
                    "pinnedBadgeIds" to emptyList<String>(),
                    "unlockedTrophyTitles" to emptyList<String>(),
                    "totalFocusMinutes" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            )
            batch.set(
                privateRef,
                mapOf(
                    "userId" to uid,
                    "email" to (user.email ?: "").take(120),
                    "ageBracket" to "13_17",
                    "guardianConsentStatus" to "acknowledged",
                    "accountStatus" to "active",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val welcomeNotifRef = db.collection("users").document(uid)
                .collection("notifications").document("welcome_${uid.take(8)}")
            batch.set(
                welcomeNotifRef,
                mapOf(
                    "userId" to uid,
                    "category" to "system",
                    "title" to if (defaultLanguage == AppLanguage.BANGLA) "StudyQuest-এ স্বাগতম!" else "Welcome to StudyQuest!",
                    "body" to if (defaultLanguage == AppLanguage.BANGLA)
                        "তোমার ইউনিক আইডি $sqCode। প্রতিদিন পাঠ ও প্র্যাকটিস করে XP অর্জন করো!"
                    else
                        "Your StudyQuest ID is $sqCode. Complete lessons and practice daily to earn XP!",
                    "read" to false,
                    "actionRoute" to "learn",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            withTimeout(20_000L) { batch.commit().await() }
            ensureActivePeriodQuests(uid)

            val created = runCatching {
                withTimeout(5_000L) {
                    profileRef.get().await().toObject(
                        PublicProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
            }.getOrNull() ?: PublicProfile(
                id = uid,
                userId = uid,
                studyQuestId = sqCode,
                username = username,
                displayName = displayName,
                avatarId = "owl_classic",
                classId = "class_10",
                curriculumVersion = curriculum,
                overallLevel = 1L,
                levelTitle = "Beginner",
                totalXp = 0L,
                coins = 50L,
                competitionRating = 1000L,
                rankTier = "Bronze",
                optInNationalLeaderboard = false,
                showOnlineStatus = true,
                isOnline = true,
                onboardingCompleted = false,
                selectedSubjects = initialSubjects,
                academicGroup = "science",
                dailyGoalMinutes = 30L,
                streakCount = 0L
            )
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun ensureActivePeriodQuests(userId: String) {
        val dailyKey = currentDailyPeriodKey()
        val weeklyKey = currentWeeklyPeriodKey()
        val questsRef = db.collection("users").document(userId).collection("quests")
        try {
            withTimeout(10_000L) {
                val existing = questsRef.get().await().documents.associateBy { it.id }
                val batch = db.batch()
                var hasWrites = false
                for (item in BangladeshCurriculumCatalog.questsCatalog) {
                    val periodKey = if (item.questType == "daily") dailyKey else weeklyKey
                    val current = existing[item.id]
                    val existingPeriod = current?.getString("periodKey")
                    if (current == null || existingPeriod != periodKey) {
                        batch.set(
                            questsRef.document(item.id),
                            mapOf(
                                "userId" to userId,
                                "questId" to item.id,
                                "questType" to item.questType,
                                "progress" to 0L,
                                "target" to item.target.toLong(),
                                "completed" to false,
                                "claimed" to false,
                                "xpReward" to item.xpReward.toLong(),
                                "coinReward" to item.coinReward.toLong(),
                                "periodKey" to periodKey,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        hasWrites = true
                    }
                }
                if (hasWrites) batch.commit().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Quest refresh warning", e)
        }
    }

    suspend fun completeAcademicSetup(
        userId: String,
        displayName: String,
        classId: String,
        curriculumVersion: String,
        selectedSubjects: List<String>,
        avatarId: String,
        academicGroup: String = "general",
        dailyGoalMinutes: Long = 30L,
        studyGoalTitle: String = ""
    ): Result<Unit> {
        val ref = db.collection("public_profiles").document(userId)
        val lbRef = db.collection("leaderboard_entries").document(userId)
        return try {
            val cleanName = displayName.trim().ifBlank { "Student" }.take(60)
            val safeGroup = if (academicGroup in listOf("general", "science", "business", "humanities")) academicGroup else "general"
            val safeDailyMins = dailyGoalMinutes.coerceIn(5L, 360L)
            val currentAcademicYear = GamificationCatalog.currentAcademicYear()
            withTimeout(15_000L) {
                ref.update(
                    mapOf(
                        "displayName" to cleanName,
                        "classId" to classId,
                        "curriculumVersion" to curriculumVersion,
                        "selectedSubjects" to selectedSubjects.take(20),
                        "avatarId" to avatarId,
                        "academicGroup" to safeGroup,
                        "dailyGoalMinutes" to safeDailyMins,
                        "classLockedAcademicYear" to currentAcademicYear,
                        "annualExamStatus" to "pending",
                        "onboardingCompleted" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            val cleanGoal = studyGoalTitle.trim().take(120)
            if (cleanGoal.isNotEmpty()) {
                runCatching {
                    withTimeout(10_000L) {
                        val starterPlanRef = db.collection("users").document(userId)
                            .collection("study_plans").document("onboarding_starter_goal")
                        starterPlanRef.set(
                            mapOf(
                                "userId" to userId,
                                "title" to cleanGoal,
                                "subjectId" to (selectedSubjects.firstOrNull() ?: "math"),
                                "planType" to "exam_countdown",
                                "targetDateEpochMs" to (System.currentTimeMillis() + 30L * 86_400_000L),
                                "minutesPerDay" to safeDailyMins,
                                "completed" to false,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            }
            runCatching {
                withTimeout(10_000L) {
                    val profSnap = ref.get().await()
                    val existingLb = lbRef.get().await()
                    val weekKey = GamificationCatalog.currentWeekKey()
                    val biweekKey = GamificationCatalog.currentBiweekKey()
                    val monthKey = GamificationCatalog.currentMonthKey()
                    val oldWeekly = if (existingLb.getString("weekKey") == weekKey) existingLb.getLong("weeklyPoints") ?: 0L else 0L
                    lbRef.set(
                        mapOf(
                            "userId" to userId,
                            "displayName" to cleanName,
                            "studyQuestId" to (profSnap.getString("studyQuestId") ?: "SQ-100000"),
                            "avatarId" to avatarId,
                            "frameId" to profSnap.getString("frameId"),
                            "classId" to classId,
                            "subjectId" to selectedSubjects.firstOrNull(),
                            "seasonId" to "season_2026_11",
                            "weekKey" to weekKey,
                            "biweekKey" to biweekKey,
                            "monthKey" to monthKey,
                            "weeklyPoints" to oldWeekly,
                            "seasonalPoints" to (existingLb.getLong("seasonalPoints") ?: (profSnap.getLong("totalXp") ?: 0L)),
                            "competitionRating" to (existingLb.getLong("competitionRating") ?: (profSnap.getLong("competitionRating") ?: 1000L)),
                            "rankTier" to (profSnap.getString("rankTier") ?: "Bronze"),
                            "optInNational" to (profSnap.getBoolean("optInNationalLeaderboard") ?: false),
                            "matchesPlayed" to (existingLb.getLong("matchesPlayed") ?: 0L),
                            "matchesWon" to (existingLb.getLong("matchesWon") ?: 0L),
                            "streakCount" to (profSnap.getLong("streakCount") ?: 0L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ).filterValues { it != null }
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileCustomization(
        userId: String,
        displayName: String? = null,
        avatarId: String? = null,
        frameId: String? = null,
        titleBadge: String? = null,
        classId: String? = null,
        curriculumVersion: String? = null,
        optInNational: Boolean? = null,
        showOnlineStatus: Boolean? = null
    ): Result<Unit> {
        val ref = db.collection("public_profiles").document(userId)
        return try {
            if (classId != null) {
                val snap = ref.get().await()
                val alreadyOnboarded = snap.getBoolean("onboardingCompleted") == true
                val lockedYear = snap.getLong("classLockedAcademicYear")?.toInt()
                val currentClass = snap.getString("classId")
                val currentYear = GamificationCatalog.currentAcademicYear()
                if (alreadyOnboarded && lockedYear != null && lockedYear >= currentYear && classId != currentClass) {
                    return Result.failure(
                        IllegalStateException("Class is locked for $currentYear and promotes automatically on Jan 1 (unless annual final exam is failed).")
                    )
                }
            }
            val updates = mutableMapOf<String, Any?>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            displayName?.let { updates["displayName"] = it.trim().take(60) }
            avatarId?.let { updates["avatarId"] = it }
            if (frameId != null) updates["frameId"] = frameId.ifBlank { null }
            if (titleBadge != null) updates["titleBadge"] = titleBadge.ifBlank { null }
            classId?.let { updates["classId"] = it }
            curriculumVersion?.let { updates["curriculumVersion"] = it }
            optInNational?.let { updates["optInNationalLeaderboard"] = it }
            showOnlineStatus?.let {
                updates["showOnlineStatus"] = it
                updates["isOnline"] = it
            }
            ref.update(updates).await()
            if (displayName != null || avatarId != null || frameId != null || classId != null || optInNational != null) {
                val lbRef = db.collection("leaderboard_entries").document(userId)
                val lbSnap = lbRef.get().await()
                if (lbSnap.exists()) {
                    val lbUpdates = mutableMapOf<String, Any?>(
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    displayName?.let { lbUpdates["displayName"] = it.trim().take(60) }
                    avatarId?.let { lbUpdates["avatarId"] = it }
                    if (frameId != null) lbUpdates["frameId"] = frameId.ifBlank { null }
                    classId?.let { lbUpdates["classId"] = it }
                    optInNational?.let { lbUpdates["optInNational"] = it }
                    lbRef.update(lbUpdates).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun syncPreferencesToCloud(userId: String, prefs: LocalUserPreferences): Result<Unit> {
        val ref = db.collection("users").document(userId).collection("preferences").document("app")
        return try {
            ref.set(
                mapOf(
                    "userId" to userId,
                    "uiLanguage" to prefs.uiLanguage.code,
                    "questionLanguage" to prefs.questionLanguage.code,
                    "darkMode" to prefs.darkMode,
                    "themeMode" to prefs.themeMode.code,
                    "reducedMotion" to prefs.reducedMotion,
                    "soundEnabled" to prefs.soundEnabled,
                    "quietHoursEnabled" to prefs.quietHoursEnabled,
                    "notifStudyEnabled" to prefs.notifStudyEnabled,
                    "notifXpEnabled" to prefs.notifXpEnabled,
                    "notifBadgeEnabled" to prefs.notifBadgeEnabled,
                    "notifAssignmentEnabled" to prefs.notifAssignmentEnabled,
                    "notifCompetitionEnabled" to prefs.notifCompetitionEnabled,
                    "notifExamEnabled" to prefs.notifExamEnabled,
                    "notifCommunityEnabled" to prefs.notifCommunityEnabled,
                    "focusMusicVolume" to prefs.focusMusicVolume.toDouble(),
                    "musicDuringBreak" to prefs.musicDuringBreak,
                    "dailyGoalXp" to prefs.dailyGoalXp,
                    "weeklyGoalMinutes" to prefs.weeklyGoalMinutes,
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

    suspend fun markNotificationRead(userId: String, notificationId: String): Result<Unit> {
        val ref = db.collection("users").document(userId).collection("notifications").document(notificationId)
        return try {
            ref.update("read", true).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun claimQuestReward(userId: String, quest: UserQuestDoc): Result<Pair<Long, Long>> {
        if (!quest.completed || quest.claimed) {
            return Result.failure(IllegalStateException("Quest not claimable"))
        }
        val questRef = db.collection("users").document(userId).collection("quests").document(quest.id)
        val ledgerId = "quest_${quest.questId}_${quest.periodKey}"
        val ledgerRef = db.collection("users").document(userId).collection("xp_ledger").document(ledgerId)
        val profileRef = db.collection("public_profiles").document(userId)
        return try {
            val existingTx = ledgerRef.get().await()
            if (existingTx.exists()) {
                questRef.update(mapOf("claimed" to true, "updatedAt" to FieldValue.serverTimestamp())).await()
                return Result.success(0L to 0L)
            }
            val profileSnap = profileRef.get().await()
            val currentXp = profileSnap.getLong("totalXp") ?: 0L
            val newXp = currentXp + quest.xpReward
            val newLevel = StudyQuestStrings.levelForXp(newXp)
            val newTitle = StudyQuestStrings.levelTitle(newLevel.toInt(), AppLanguage.ENGLISH)

            val batch = db.batch()
            batch.update(
                questRef,
                mapOf("claimed" to true, "updatedAt" to FieldValue.serverTimestamp())
            )
            batch.set(
                ledgerRef,
                mapOf(
                    "userId" to userId,
                    "sourceType" to "quest_reward",
                    "sourceId" to quest.questId,
                    "xpDelta" to quest.xpReward,
                    "coinDelta" to quest.coinReward,
                    "idempotencyKey" to ledgerId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                profileRef,
                mapOf(
                    "totalXp" to newXp,
                    "overallLevel" to newLevel,
                    "levelTitle" to newTitle,
                    "coins" to FieldValue.increment(quest.coinReward),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(quest.xpReward to quest.coinReward)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, questRef.path)
            Result.failure(e)
        }
    }

    suspend fun searchStudents(query: String, excludeUid: String): Result<List<PublicProfile>> {
        val trimmed = query.trim()
        return try {
            val profilesRef = db.collection("public_profiles")
            val results = linkedMapOf<String, PublicProfile>()

            if (trimmed.startsWith("SQ-", ignoreCase = true)) {
                val upper = trimmed.uppercase(Locale.ROOT)
                profilesRef.whereEqualTo("studyQuestId", upper).limit(10).get().await()
                    .toObjects(PublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .forEach { results[it.userId] = it }
            }

            if (trimmed.isNotBlank()) {
                val lower = trimmed.lowercase(Locale.ROOT)
                profilesRef.whereEqualTo("username", lower).limit(10).get().await()
                    .toObjects(PublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .forEach { results[it.userId] = it }
            }

            val recentDocs = profilesRef.orderBy("totalXp", Query.Direction.DESCENDING).limit(25).get().await()
                .toObjects(PublicProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            for (p in recentDocs) {
                if (trimmed.isBlank() ||
                    p.displayName.contains(trimmed, ignoreCase = true) ||
                    p.username.contains(trimmed, ignoreCase = true) ||
                    p.studyQuestId.contains(trimmed, ignoreCase = true)
                ) {
                    results.putIfAbsent(p.userId, p)
                }
            }
            Result.success(results.values.filter { it.userId != excludeUid })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "public_profiles")
            Result.failure(e)
        }
    }

    suspend fun getPrivateAccountInfo(userId: String): Result<PrivateAccountInfo?> {
        val ref = db.collection("users").document(userId).collection("private").document("account")
        return try {
            val snap = ref.get().await()
            val info = snap.toObject(
                PrivateAccountInfo::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(info)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, ref.path)
            Result.failure(e)
        }
    }

    suspend fun exportUserDataJson(userId: String): Result<String> {
        return try {
            val profile = db.collection("public_profiles").document(userId).get().await().data ?: emptyMap()
            val privateAcc = db.collection("users").document(userId).collection("private").document("account").get().await().data ?: emptyMap()
            val records = db.collection("users").document(userId).collection("learning_records").get().await().documents.map { it.data ?: emptyMap() }
            val plans = db.collection("users").document(userId).collection("study_plans").get().await().documents.map { it.data ?: emptyMap() }
            val json = JSONObject().apply {
                put("exportedAtEpochMs", System.currentTimeMillis())
                put("studyQuestPublicProfile", JSONObject(profile))
                put("privateAccount", JSONObject(privateAcc))
                put("learningRecordsCount", records.size)
                put("learningRecords", JSONArray(records.map { JSONObject(it) }))
                put("studyPlans", JSONArray(plans.map { JSONObject(it) }))
            }
            Result.success(json.toString(2))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "users/$userId")
            Result.failure(e)
        }
    }

    suspend fun deleteAccountAndUserData(userId: String, auth: FirebaseAuth = Firebase.auth): Result<Unit> {
        return try {
            // 1. Delete subcollection records owned by the user
            val subCollections = listOf("learning_records", "quests", "study_plans", "notifications", "preferences", "private")
            for (sub in subCollections) {
                val docs = db.collection("users").document(userId).collection(sub).get().await().documents
                for (chunk in docs.chunked(200)) {
                    val batch = db.batch()
                    chunk.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }
            }
            // 2. Delete public profile and leaderboard entry
            db.collection("public_profiles").document(userId).delete().await()
            runCatching { db.collection("leaderboard_entries").document(userId).delete().await() }

            // 3. Clear local Room user artifacts
            dao?.clearBookmarks()
            dao?.clearRevisions()
            dao?.clearPendingSyncActions()

            // 4. Delete Firebase Auth account (or sign out if re-auth required)
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.uid == userId) {
                try {
                    currentUser.delete().await()
                } catch (_: Exception) {
                    auth.signOut()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "public_profiles/$userId")
            Result.failure(e)
        }
    }

    suspend fun setOnlinePresence(userId: String, isOnline: Boolean) {
        try {
            val ref = db.collection("public_profiles").document(userId)
            val snap = ref.get().await()
            if (snap.exists() && snap.getBoolean("showOnlineStatus") == true) {
                ref.update(
                    mapOf(
                        "isOnline" to isOnline,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (_: Exception) {
            // Non-fatal lifecycle presence update
        }
    }

    /**
     * Automatically and idempotently merges local Guest Mode XP, coins, profile settings,
     * unlocked cosmetics, practice/exam records, and study plans into the signed-in Google account.
     */
    suspend fun mergeGuestProgressToAccount(
        userId: String,
        preferences: com.example.core.StudyQuestPreferences
    ): Result<Int> {
        val prefs = preferences.snapshot()
        val guestRecords = dao?.getActionsForUser(GuestDataRepository.GUEST_UID).orEmpty()
        val guestPlans = dao?.getUnsyncedStudyPlans(GuestDataRepository.GUEST_UID).orEmpty()
        val unlockedCosmetics = prefs.guestUnlockedCosmeticsCsv
            .split(",")
            .filter { it.isNotBlank() && it != "owl_classic" }

        val hasAnyGuestState = prefs.guestHasPendingMerge ||
            prefs.guestXp > 0L ||
            guestRecords.isNotEmpty() ||
            guestPlans.isNotEmpty() ||
            unlockedCosmetics.isNotEmpty()

        if (!hasAnyGuestState) {
            if (prefs.isGuestMode) preferences.exitGuestMode()
            return Result.success(0)
        }

        return try {
            var mergedItems = 0
            val profileRef = db.collection("public_profiles").document(userId)
            val profileSnap = profileRef.get().await()
            val mergeLedgerId = "guest_merge_${userId.take(24)}"
            val mergeLedgerRef = db.collection("users").document(userId)
                .collection("xp_ledger").document(mergeLedgerId)

            if (!mergeLedgerRef.get().await().exists()) {
                val xpDelta = prefs.guestXp.coerceAtLeast(0L).coerceAtMost(500L)
                val extraCoins = (prefs.guestCoins - 50L).coerceAtLeast(0L).coerceAtMost(500L)
                val existingXp = profileSnap.getLong("totalXp") ?: 0L
                val newXp = existingXp + xpDelta
                val newLevel = StudyQuestStrings.levelForXp(newXp)
                val alreadyOnboarded = profileSnap.getBoolean("onboardingCompleted") == true

                val batch = db.batch()
                batch.set(
                    mergeLedgerRef,
                    mapOf(
                        "userId" to userId,
                        "sourceType" to "guest_merge",
                        "sourceId" to "guest_session",
                        "xpDelta" to xpDelta,
                        "coinDelta" to extraCoins,
                        "idempotencyKey" to mergeLedgerId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                val profileUpdates = mutableMapOf<String, Any>(
                    "totalXp" to newXp,
                    "overallLevel" to newLevel,
                    "levelTitle" to StudyQuestStrings.levelTitle(newLevel.toInt(), AppLanguage.ENGLISH),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (extraCoins > 0L) {
                    profileUpdates["coins"] = FieldValue.increment(extraCoins)
                }
                if (!alreadyOnboarded && prefs.guestOnboardingCompleted) {
                    profileUpdates["classId"] = prefs.guestClassId
                    profileUpdates["curriculumVersion"] = prefs.guestCurriculumVersion
                    profileUpdates["selectedSubjects"] = prefs.guestSelectedSubjectsCsv
                        .split(",").filter { it.isNotBlank() }.take(20)
                    profileUpdates["avatarId"] = prefs.guestAvatarId.ifBlank { "owl_classic" }
                    profileUpdates["onboardingCompleted"] = true
                    if (prefs.guestDisplayName.isNotBlank() && prefs.guestDisplayName != "Guest Scholar") {
                        profileUpdates["displayName"] = prefs.guestDisplayName.take(58)
                    }
                }
                batch.update(profileRef, profileUpdates)
                batch.commit().await()
                mergedItems++
            }

            // Merge unlocked cosmetics idempotently
            for (cosmeticId in unlockedCosmetics) {
                val ledgerId = "cosmetic_$cosmeticId"
                val ref = db.collection("users").document(userId).collection("xp_ledger").document(ledgerId)
                if (!ref.get().await().exists()) {
                    ref.set(
                        mapOf(
                            "userId" to userId,
                            "sourceType" to "cosmetic_purchase",
                            "sourceId" to cosmeticId,
                            "xpDelta" to 0L,
                            "coinDelta" to 0L,
                            "idempotencyKey" to ledgerId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    mergedItems++
                }
            }

            // Merge local learning records into Firestore
            for (action in guestRecords) {
                val safeDocId = action.idempotencyKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
                val recordRef = db.collection("users").document(userId)
                    .collection("learning_records").document(safeDocId)
                if (!recordRef.get().await().exists()) {
                    recordRef.set(
                        mapOf(
                            "userId" to userId,
                            "recordType" to action.actionType,
                            "refId" to action.refId,
                            "subjectId" to action.subjectId,
                            "chapterId" to action.chapterId.ifBlank { null },
                            "topicId" to action.topicId.ifBlank { null },
                            "score" to action.score,
                            "totalMarks" to action.totalMarks,
                            "accuracy" to action.accuracy,
                            "xpAwarded" to action.xpAwarded.toLong(),
                            "idempotencyKey" to action.idempotencyKey,
                            "summary" to action.summary.take(480),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ).filterValues { it != null }
                    ).await()
                }
                dao?.markSyncActionCompleted(action.idempotencyKey)
                mergedItems++
            }
            dao?.reassignPendingActionsUser(GuestDataRepository.GUEST_UID, userId)

            // Merge local study plans
            for (plan in guestPlans) {
                val planRef = db.collection("users").document(userId).collection("study_plans").document(plan.id)
                if (!planRef.get().await().exists()) {
                    planRef.set(
                        mapOf(
                            "userId" to userId,
                            "title" to plan.title.take(118),
                            "subjectId" to plan.subjectId,
                            "planType" to plan.planType,
                            "targetDateEpochMs" to plan.targetDateEpochMs,
                            "minutesPerDay" to plan.minutesPerDay.coerceIn(5L, 720L),
                            "completed" to plan.completed,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
                dao?.markStudyPlanSynced(plan.id, userId)
                mergedItems++
            }

            dao?.clearLocalQuests()
            dao?.clearSoloMatches()
            preferences.clearGuestDataAfterMerge()
            Result.success(mergedItems)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$userId/guest_merge")
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AuthProfileRepo"

        fun currentDailyPeriodKey(): String = "daily_${currentDailyChallengeDateKey()}"

        fun currentDailyChallengeDateKey(): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
            return fmt.format(Calendar.getInstance(fmt.timeZone).time)
        }

        fun yesterdayDailyChallengeDateKey(): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
            val cal = Calendar.getInstance(fmt.timeZone)
            cal.add(Calendar.DAY_OF_YEAR, -1)
            return fmt.format(cal.time)
        }

        fun currentWeeklyPeriodKey(): String {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Dhaka"))
            cal.firstDayOfWeek = Calendar.MONDAY
            val year = cal.get(Calendar.YEAR)
            val week = cal.get(Calendar.WEEK_OF_YEAR)
            return String.format(Locale.US, "%04d-W%02d", year, week)
        }
    }
}
