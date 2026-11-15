package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.curriculum.GamificationCatalog
import com.example.data.model.AcademicContentDoc
import com.example.data.model.AiProviderSettingsDoc
import com.example.data.model.AiUsageLogDoc
import com.example.data.model.AuditLogDoc
import com.example.data.model.ModerationCaseDoc
import com.example.data.model.SystemSettingsDoc
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class BusinessInsightsSummary(
    val activeUsersTotal: Int = 0,
    val paidSubscribersTotal: Int = 0,
    val grossMonthlyRevenueBdt: Double = 0.0,
    val playStoreFeesBdt: Double = 0.0,
    val estimatedTaxesBdt: Double = 0.0,
    val dailyAiCostUsd: Double = 0.0,
    val monthlyAiCostUsd: Double = 0.0,
    val monthlyAiCostBdt: Double = 0.0,
    val totalRequestsThisMonth: Int = 0,
    val lunaRequests: Int = 0,
    val solRequests: Int = 0,
    val astraRequests: Int = 0,
    val fallbackRequests: Int = 0,
    val estimatedNetProfitBdt: Double = 0.0
)

class AdminStudioRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    suspend fun resolveAdminRole(user: FirebaseUser?): String? {
        if (user == null) return null
        if (user.email.equals(OWNER_EMAIL, ignoreCase = true) && user.isEmailVerified) {
            return "owner"
        }
        return try {
            val snap = db.collection("admin_users").document(user.uid).get().await()
            if (snap.exists() && snap.getBoolean("active") == true) {
                snap.getString("role") ?: "moderator"
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun observeSystemSettings(): Flow<SystemSettingsDoc> =
        db.collection("system_settings").document("global")
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(
                        SystemSettingsDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: SystemSettingsDoc()
                } else SystemSettingsDoc()
            }

    fun observeAllAcademicContent(): Flow<List<AcademicContentDoc>> =
        db.collection("academic_content")
            .snapshots()
            .map {
                it.toObjects(
                    AcademicContentDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { c -> c.updatedAt?.seconds ?: c.createdAt?.seconds ?: 0L }
            }

    fun observeModerationCases(): Flow<List<ModerationCaseDoc>> =
        db.collection("moderation_cases")
            .snapshots()
            .map {
                it.toObjects(
                    ModerationCaseDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { c -> c.createdAt?.seconds ?: 0L }
            }

    fun observeAuditLogs(): Flow<List<AuditLogDoc>> =
        db.collection("audit_logs")
            .snapshots()
            .map {
                it.toObjects(
                    AuditLogDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { a -> a.createdAt?.seconds ?: 0L }
            }

    suspend fun createDraftQuestion(
        actorUid: String,
        classId: String,
        subjectId: String,
        titleEn: String,
        titleBn: String,
        optionsEn: List<String>,
        optionsBn: List<String>,
        correctIndex: Int,
        explanationEn: String,
        explanationBn: String,
        sourceAttribution: String
    ): Result<String> {
        val contentId = "q_cloud_${UUID.randomUUID().toString().take(10)}"
        val ref = db.collection("academic_content").document(contentId)
        val payloadJson = JSONObject().apply {
            put("type", "single_mcq")
            put("difficulty", "medium")
            put("optionsEn", JSONArray(optionsEn))
            put("optionsBn", JSONArray(optionsBn))
            put("correctIndex", correctIndex)
            put("explanationEn", explanationEn)
            put("explanationBn", explanationBn)
        }.toString()

        return try {
            ref.set(
                mapOf(
                    "contentType" to "question",
                    "classId" to classId,
                    "curriculumVersion" to "bangla",
                    "subjectId" to subjectId,
                    "chapterId" to "ch_custom",
                    "topicId" to "topic_custom",
                    "titleEn" to titleEn.trim().take(195),
                    "titleBn" to titleBn.trim().ifBlank { titleEn.trim() }.take(195),
                    "status" to "draft",
                    "authorUid" to actorUid,
                    "sourceAttribution" to sourceAttribution.trim().ifBlank { "StudyQuest Editorial Draft" }.take(190),
                    "version" to 1L,
                    "payloadJson" to payloadJson,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(actorUid, "create_draft_question", "academic_content", contentId, titleEn)
            Result.success(contentId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun advanceEditorialStatus(
        actorUid: String,
        content: AcademicContentDoc,
        nextStatus: String
    ): Result<Unit> {
        val ref = db.collection("academic_content").document(content.id)
        return try {
            ref.update(
                mapOf(
                    "status" to nextStatus,
                    "version" to (content.version + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(actorUid, "editorial_transition_$nextStatus", "academic_content", content.id, content.titleEn)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun resolveModerationCase(
        actorUid: String,
        caseDoc: ModerationCaseDoc,
        newStatus: String, // triaged, resolved, dismissed
        decisionNote: String
    ): Result<Unit> {
        val ref = db.collection("moderation_cases").document(caseDoc.id)
        return try {
            ref.update(
                mapOf(
                    "status" to newStatus,
                    "assignedModeratorUid" to actorUid,
                    "decisionNote" to decisionNote.trim().take(900),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(actorUid, "moderation_$newStatus", "moderation_cases", caseDoc.id, decisionNote)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun appealModerationCase(
        caseDoc: ModerationCaseDoc,
        appealText: String
    ): Result<Unit> {
        val ref = db.collection("moderation_cases").document(caseDoc.id)
        return try {
            ref.update(
                mapOf(
                    "status" to "appealed",
                    "appealText" to appealText.trim().take(900),
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

    suspend fun updateSystemSettings(
        actorUid: String,
        aiSolverEnabled: Boolean,
        seasonNameEn: String,
        seasonNameBn: String,
        billingConfigured: Boolean = false,
        adMobConfigured: Boolean = false,
        externalVideoRendererConfigured: Boolean = false,
        xpRulesJson: String? = null
    ): Result<Unit> {
        val ref = db.collection("system_settings").document("global")
        return try {
            val payload = mutableMapOf<String, Any?>(
                "aiSolverEnabled" to aiSolverEnabled,
                "currentSeasonId" to "season_2026_11",
                "seasonNameEn" to seasonNameEn.trim().take(100),
                "seasonNameBn" to seasonNameBn.trim().take(100),
                "weeklyResetTz" to "Asia/Dhaka",
                "billingConfigured" to billingConfigured,
                "adMobConfigured" to adMobConfigured,
                "externalVideoRendererConfigured" to externalVideoRendererConfigured,
                "xpRulesJson" to xpRulesJson,
                "updatedByUid" to actorUid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload.filterValues { it != null }).await()
            writeAuditLog(actorUid, "update_system_settings", "system_settings", "global", "ai=$aiSolverEnabled")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun saveXpRuleConfig(
        actorUid: String,
        currentSettings: SystemSettingsDoc,
        rules: com.example.data.model.XpRuleConfig
    ): Result<Unit> {
        val json = JSONObject().apply {
            put("lessonXp", rules.lessonXp)
            put("practiceXpPerCorrect", rules.practiceXpPerCorrect)
            put("examXpBase", rules.examXpBase)
            put("focusXpPerMinute", rules.focusXpPerMinute)
            put("dailyChallengeXp", rules.dailyChallengeXp)
            put("streakBonusPerDay", rules.streakBonusPerDay)
            put("communityHelpfulXp", rules.communityHelpfulXp)
            put("maxDailyFocusXp", rules.maxDailyFocusXp)
            put("maxDailyCommunityXp", rules.maxDailyCommunityXp)
        }.toString()
        val res = updateSystemSettings(
            actorUid = actorUid,
            aiSolverEnabled = currentSettings.aiSolverEnabled,
            seasonNameEn = currentSettings.seasonNameEn,
            seasonNameBn = currentSettings.seasonNameBn,
            billingConfigured = currentSettings.billingConfigured,
            adMobConfigured = currentSettings.adMobConfigured,
            externalVideoRendererConfigured = currentSettings.externalVideoRendererConfigured,
            xpRulesJson = json
        )
        if (res.isSuccess) {
            writeAuditLog(actorUid, "update_xp_rules", "system_settings", "global", json.take(300))
        }
        return res
    }

    fun observeBadgeDefinitions(): Flow<List<com.example.data.model.BadgeDefinitionDoc>> =
        db.collection("badge_definitions")
            .snapshots()
            .map {
                it.toObjects(
                    com.example.data.model.BadgeDefinitionDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    suspend fun saveBadgeDefinition(
        actorUid: String,
        badgeId: String,
        category: String,
        rarity: String,
        titleEn: String,
        titleBn: String,
        descriptionEn: String,
        target: Long,
        xpBonus: Long,
        enabled: Boolean
    ): Result<Unit> {
        val cleanId = badgeId.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(60)
        if (cleanId.isBlank()) return Result.failure(IllegalArgumentException("Badge ID required"))
        val ref = db.collection("badge_definitions").document(cleanId)
        return try {
            ref.set(
                mapOf(
                    "badgeId" to cleanId,
                    "category" to category,
                    "rarity" to rarity,
                    "titleEn" to titleEn.trim().take(98),
                    "titleBn" to titleBn.trim().ifBlank { titleEn.trim() }.take(98),
                    "descriptionEn" to descriptionEn.trim().take(190),
                    "descriptionBn" to descriptionEn.trim().take(190),
                    "metricKey" to "study_sessions",
                    "target" to target.coerceIn(1L, 100000L),
                    "xpBonus" to xpBonus.coerceIn(0L, 1000L),
                    "enabled" to enabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(actorUid, "save_badge_definition", "badge_definitions", cleanId, titleEn)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            Result.failure(e)
        }
    }

    fun observeAllAssignments(): Flow<List<com.example.data.model.AssignmentDefinitionDoc>> =
        db.collection("assignments")
            .snapshots()
            .map {
                it.toObjects(
                    com.example.data.model.AssignmentDefinitionDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { a -> a.createdAt?.seconds ?: 0L }
            }

    suspend fun createAssignmentDefinition(
        actorUid: String,
        titleEn: String,
        titleBn: String,
        descriptionEn: String,
        assignmentType: String,
        classId: String,
        subjectId: String,
        difficulty: String,
        targetCount: Long,
        xpReward: Long
    ): Result<String> {
        val id = "assign_${UUID.randomUUID().toString().take(10)}"
        val periodKey = when (assignmentType) {
            "weekly" -> com.example.data.curriculum.GamificationCatalog.currentWeekKey()
            "two_week" -> com.example.data.curriculum.GamificationCatalog.currentBiweekKey()
            "monthly" -> com.example.data.curriculum.GamificationCatalog.currentMonthKey()
            else -> com.example.data.curriculum.GamificationCatalog.currentDayKey()
        }
        val ref = db.collection("assignments").document(id)
        return try {
            ref.set(
                mapOf(
                    "titleEn" to titleEn.trim().take(138),
                    "titleBn" to titleBn.trim().ifBlank { titleEn.trim() }.take(138),
                    "descriptionEn" to descriptionEn.trim().take(240),
                    "descriptionBn" to descriptionEn.trim().take(240),
                    "assignmentType" to assignmentType,
                    "classId" to classId,
                    "subjectId" to subjectId,
                    "difficulty" to difficulty,
                    "targetCount" to targetCount.coerceIn(1L, 500L),
                    "durationLimitMinutes" to 30L,
                    "xpReward" to xpReward.coerceIn(10L, 1000L),
                    "coinReward" to (xpReward / 3L).coerceIn(5L, 250L),
                    "active" to true,
                    "periodKey" to periodKey,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(actorUid, "create_assignment", "assignments", id, titleEn)
            Result.success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    fun observeFlaggedCommunityPosts(): Flow<List<com.example.data.model.CommunityPostDoc>> =
        db.collection("community_posts")
            .whereEqualTo("flagged", true)
            .snapshots()
            .map {
                it.toObjects(
                    com.example.data.model.CommunityPostDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    suspend fun moderateCommunityPost(actorUid: String, postId: String, hidePost: Boolean): Result<Unit> {
        val ref = db.collection("community_posts").document(postId)
        return try {
            ref.update(
                mapOf(
                    "hidden" to hidePost,
                    "flagged" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(actorUid, if (hidePost) "hide_community_post" else "approve_community_post", "community_posts", postId, "")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun createOfficialTournament(
        actorUid: String,
        titleEn: String,
        titleBn: String,
        classId: String,
        subjectId: String
    ): Result<String> {
        val id = "tour_${UUID.randomUUID().toString().take(10)}"
        val ref = db.collection("tournaments").document(id)
        return try {
            ref.set(
                mapOf(
                    "titleEn" to titleEn.trim().take(118),
                    "titleBn" to titleBn.trim().ifBlank { titleEn.trim() }.take(118),
                    "classId" to classId,
                    "subjectId" to subjectId,
                    "status" to "registration",
                    "participantUids" to emptyList<String>(),
                    "maxParticipants" to 32L,
                    "rewardCoins" to 150L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(actorUid, "create_tournament", "tournaments", id, titleEn)
            Result.success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    private val openRouterClient = OpenRouterClient()
    private val aiCostGuard = StudyQuestAiCostGuard(db)

    fun observeAiProviderSettings(): Flow<AiProviderSettingsDoc> =
        aiCostGuard.observeProviderSettings()

    fun observeAiUsageLogs(): Flow<List<AiUsageLogDoc>> =
        aiCostGuard.observeUsageLogs()

    fun isOpenRouterKeyConfigured(): Boolean = openRouterClient.isApiKeyConfigured()

    suspend fun verifyOpenRouterModels(settings: AiProviderSettingsDoc): OpenRouterModelCheckResult {
        val check = openRouterClient.verifyModelAvailability(
            defaultModelId = settings.defaultModelId.ifBlank { StudyQuestModelRouter.DEFAULT_MODEL },
            reasoningModelId = settings.reasoningModelId.ifBlank { StudyQuestModelRouter.REASONING_MODEL },
            advancedModelId = settings.advancedModelId.ifBlank { StudyQuestModelRouter.ADVANCED_MODEL }
        )
        runCatching {
            db.collection("system_settings").document("ai_provider_config")
                .set(
                    mapOf(
                        "lastVerificationStatus" to if (check.reachable) "verified" else if (!check.keyConfigured) "missing_key" else "error",
                        "lastVerifiedModelsSummary" to check.message.take(280),
                        "lastErrorSummary" to if (!check.reachable) check.message.take(200) else null,
                        "lastErrorAtEpochMs" to if (!check.reachable) System.currentTimeMillis() else 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
        }
        return check
    }

    suspend fun saveAiProviderSettings(
        actorUid: String,
        openRouterEnabled: Boolean,
        defaultModelId: String,
        reasoningModelId: String,
        advancedModelId: String,
        advancedModelEnabled: Boolean,
        fallbackToFirebaseAi: Boolean,
        globalMonthlyBudgetUsd: Double,
        perUserMonthlyBudgetUsd: Double,
        astraMonthlyBudgetUsd: Double
    ): Result<Unit> {
        val ref = db.collection("system_settings").document("ai_provider_config")
        return try {
            ref.set(
                mapOf(
                    "openRouterEnabled" to openRouterEnabled,
                    "defaultModelId" to defaultModelId.trim().ifBlank { StudyQuestModelRouter.DEFAULT_MODEL }.take(80),
                    "reasoningModelId" to reasoningModelId.trim().ifBlank { StudyQuestModelRouter.REASONING_MODEL }.take(80),
                    "advancedModelId" to advancedModelId.trim().ifBlank { StudyQuestModelRouter.ADVANCED_MODEL }.take(80),
                    "advancedModelEnabled" to advancedModelEnabled,
                    "fallbackToFirebaseAi" to fallbackToFirebaseAi,
                    "globalMonthlyBudgetUsd" to globalMonthlyBudgetUsd.coerceIn(1.0, 50000.0),
                    "perUserMonthlyBudgetUsd" to perUserMonthlyBudgetUsd.coerceIn(0.1, 500.0),
                    "astraMonthlyBudgetUsd" to astraMonthlyBudgetUsd.coerceIn(0.5, 10000.0),
                    "updatedByUid" to actorUid,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            writeAuditLog(actorUid, "update_ai_provider_config", "system_settings", "ai_provider_config", "astra=$advancedModelEnabled, globalBudget=$globalMonthlyBudgetUsd")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun computeBusinessInsights(logs: List<AiUsageLogDoc>): BusinessInsightsSummary {
        val monthKey = GamificationCatalog.currentMonthKey()
        val dayKey = GamificationCatalog.currentDayKey()
        val monthLogs = logs.filter { it.monthKey == monthKey || it.monthKey.isBlank() }
        val dailyUsd = monthLogs.filter { it.dayKey == dayKey }.sumOf { it.estimatedCostUsd }
        val monthlyUsd = monthLogs.sumOf { it.estimatedCostUsd }
        val usdToBdt = 120.0
        val monthlyAiCostBdt = monthlyUsd * usdToBdt

        val activeUsers = runCatching {
            db.collection("public_profiles").limit(200).get().await().size()
        }.getOrDefault(0)

        // Note: Revenue stays 0.0 when Play Billing is unconfigured; real recorded transactions come from collectionGroup('purchase_history')
        val paidPurchases = runCatching {
            db.collectionGroup("purchase_history")
                .whereEqualTo("status", "completed")
                .limit(200)
                .get()
                .await()
                .documents
        }.getOrDefault(emptyList())

        val grossRevenueBdt = paidPurchases.sumOf { (it.getLong("amountBdt") ?: 0L).toDouble() }
        val playFeesBdt = grossRevenueBdt * 0.15
        val taxesBdt = grossRevenueBdt * 0.05
        val netBdt = grossRevenueBdt - playFeesBdt - taxesBdt - monthlyAiCostBdt

        return BusinessInsightsSummary(
            activeUsersTotal = activeUsers,
            paidSubscribersTotal = paidPurchases.mapNotNull { it.getString("userId") }.distinct().size,
            grossMonthlyRevenueBdt = grossRevenueBdt,
            playStoreFeesBdt = playFeesBdt,
            estimatedTaxesBdt = taxesBdt,
            dailyAiCostUsd = dailyUsd,
            monthlyAiCostUsd = monthlyUsd,
            monthlyAiCostBdt = monthlyAiCostBdt,
            totalRequestsThisMonth = monthLogs.size,
            lunaRequests = monthLogs.count { it.modelId.contains("luna", ignoreCase = true) },
            solRequests = monthLogs.count { it.modelId.contains("sol", ignoreCase = true) },
            astraRequests = monthLogs.count { it.modelId.contains("astra", ignoreCase = true) },
            fallbackRequests = monthLogs.count { it.provider == "firebase_ai" },
            estimatedNetProfitBdt = netBdt
        )
    }

    private suspend fun writeAuditLog(
        actorUid: String,
        action: String,
        targetType: String,
        targetId: String,
        details: String
    ) {
        val logId = "log_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        runCatching {
            db.collection("audit_logs").document(logId).set(
                mapOf(
                    "actorUid" to actorUid,
                    "action" to action,
                    "targetType" to targetType,
                    "targetId" to targetId,
                    "details" to details.take(400),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    companion object {
        const val OWNER_EMAIL = "abirmahmudm116@gmail.com"
    }
}
