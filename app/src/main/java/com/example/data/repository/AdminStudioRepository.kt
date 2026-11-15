package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.model.AcademicContentDoc
import com.example.data.model.AuditLogDoc
import com.example.data.model.ModerationCaseDoc
import com.example.data.model.SystemSettingsDoc
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

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
        seasonNameBn: String
    ): Result<Unit> {
        val ref = db.collection("system_settings").document("global")
        return try {
            ref.set(
                mapOf(
                    "aiSolverEnabled" to aiSolverEnabled,
                    "currentSeasonId" to "season_2026_11",
                    "seasonNameEn" to seasonNameEn.trim().take(100),
                    "seasonNameBn" to seasonNameBn.trim().take(100),
                    "weeklyResetTz" to "Asia/Dhaka",
                    "updatedByUid" to actorUid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAuditLog(actorUid, "update_system_settings", "system_settings", "global", "ai=$aiSolverEnabled")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
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
