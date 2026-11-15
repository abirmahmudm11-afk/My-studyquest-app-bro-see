package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.NotificationCategory
import com.example.core.NotificationDispatcher
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.curriculum.GamificationCatalog
import com.example.data.model.CommunityCommentDoc
import com.example.data.model.CommunityPostDoc
import com.example.data.model.PublicProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommunityRepository(
    private val db: FirebaseFirestore,
    private val gamificationEngine: GamificationEngine,
    private val notifications: NotificationDispatcher? = null
) {
    constructor(
        context: Context,
        gamificationEngine: GamificationEngine,
        notifications: NotificationDispatcher? = null
    ) : this(
        FirebaseFirestore.getInstance(context.applicationContext.getString(R.string.firestore_database_id)),
        gamificationEngine,
        notifications
    )

    private var lastPostAtMs = 0L

    fun observeClassCommunityPosts(classId: String): Flow<List<CommunityPostDoc>> =
        db.collection("community_posts")
            .whereEqualTo("classId", classId)
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    CommunityPostDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).filter { !it.hidden }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeComments(postId: String): Flow<List<CommunityCommentDoc>> =
        db.collection("community_posts").document(postId).collection("comments")
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    CommunityCommentDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.createdAt?.seconds ?: 0L }
            }

    suspend fun createPost(
        profile: PublicProfile,
        subjectId: String,
        postType: String,
        title: String,
        body: String
    ): Result<String> {
        val cleanTitle = title.trim().take(138)
        val cleanBody = body.trim().take(1950)
        if (cleanTitle.length < 3 || cleanBody.length < 3) {
            return Result.failure(IllegalArgumentException("Please enter at least 3 characters for title and details."))
        }
        if (SocialRepository.containsSuspiciousLink(cleanTitle) || SocialRepository.containsSuspiciousLink(cleanBody)) {
            return Result.failure(IllegalArgumentException("External links are blocked in student community posts."))
        }
        val now = System.currentTimeMillis()
        if (now - lastPostAtMs < 10_000L) {
            return Result.failure(IllegalStateException("Please wait a few seconds before posting again."))
        }

        val postId = "post_${now}_${UUID.randomUUID().toString().take(6)}"
        val ref = db.collection("community_posts").document(postId)
        return try {
            ref.set(
                mapOf(
                    "authorUid" to profile.userId,
                    "authorName" to profile.displayName.take(58),
                    "authorAvatarId" to profile.avatarId,
                    "authorClassId" to profile.classId,
                    "classId" to profile.classId,
                    "subjectId" to subjectId,
                    "postType" to postType,
                    "title" to cleanTitle,
                    "body" to cleanBody,
                    "upvoterUids" to emptyList<String>(),
                    "commentCount" to 0L,
                    "flagged" to false,
                    "hidden" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            lastPostAtMs = now

            // Log contribution for Community badges & capped XP
            val dayKey = GamificationCatalog.currentDayKey()
            db.collection("users").document(profile.userId)
                .collection("learning_records").document("comm_$postId")
                .set(
                    mapOf(
                        "userId" to profile.userId,
                        "recordType" to "community",
                        "refId" to postId,
                        "subjectId" to subjectId,
                        "score" to 1.0,
                        "totalMarks" to 1.0,
                        "accuracy" to 1.0,
                        "xpAwarded" to 10L,
                        "idempotencyKey" to "comm_post_$postId",
                        "summary" to cleanTitle.take(120),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

            gamificationEngine.awardVerifiedActivity(
                userId = profile.userId,
                sourceType = "community_post",
                sourceId = postId,
                idempotencyKey = "comm_post_${dayKey}_$postId",
                rawXpDelta = 10L,
                coinDelta = 3L,
                description = "Class Community Post: ${cleanTitle.take(50)}",
                subjectId = subjectId
            )

            Result.success(postId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun addComment(
        profile: PublicProfile,
        post: CommunityPostDoc,
        rawText: String
    ): Result<String> {
        val clean = rawText.trim().take(980)
        if (clean.isBlank()) return Result.failure(IllegalArgumentException("Comment cannot be empty"))
        if (SocialRepository.containsSuspiciousLink(clean)) {
            return Result.failure(IllegalArgumentException("External links are not allowed."))
        }
        val commentId = "cmt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val postRef = db.collection("community_posts").document(post.id)
        val commentRef = postRef.collection("comments").document(commentId)
        return try {
            val batch = db.batch()
            batch.set(
                commentRef,
                mapOf(
                    "postId" to post.id,
                    "authorUid" to profile.userId,
                    "authorName" to profile.displayName.take(58),
                    "classId" to post.classId,
                    "body" to clean,
                    "upvoterUids" to emptyList<String>(),
                    "isHelpful" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                postRef,
                mapOf(
                    "commentCount" to FieldValue.increment(1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            val dayKey = GamificationCatalog.currentDayKey()
            db.collection("users").document(profile.userId)
                .collection("learning_records").document("comm_$commentId")
                .set(
                    mapOf(
                        "userId" to profile.userId,
                        "recordType" to "community",
                        "refId" to commentId,
                        "subjectId" to post.subjectId,
                        "score" to 1.0,
                        "totalMarks" to 1.0,
                        "accuracy" to 1.0,
                        "xpAwarded" to 8L,
                        "idempotencyKey" to "comm_cmt_$commentId",
                        "summary" to "Replied on ${post.title.take(60)}",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

            gamificationEngine.awardVerifiedActivity(
                userId = profile.userId,
                sourceType = "community_comment",
                sourceId = commentId,
                idempotencyKey = "comm_cmt_${dayKey}_$commentId",
                rawXpDelta = 8L,
                coinDelta = 2L,
                description = "Helped classmate on ${post.title.take(40)}",
                subjectId = post.subjectId
            )

            if (post.authorUid != profile.userId) {
                notifications?.dispatch(
                    userId = post.authorUid,
                    category = NotificationCategory.COMMUNITY,
                    title = "${profile.displayName} replied to your question",
                    body = clean.take(120),
                    actionRoute = "community",
                    idempotencyKey = "cmt_notif_$commentId"
                )
            }

            Result.success(commentId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleUpvotePost(post: CommunityPostDoc, userId: String): Result<Unit> {
        val ref = db.collection("community_posts").document(post.id)
        return try {
            val updated = if (userId in post.upvoterUids) post.upvoterUids - userId else (post.upvoterUids + userId).distinct()
            ref.update(
                mapOf(
                    "upvoterUids" to updated,
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

    suspend fun markCommentHelpful(post: CommunityPostDoc, comment: CommunityCommentDoc): Result<Unit> {
        val ref = db.collection("community_posts").document(post.id)
            .collection("comments").document(comment.id)
        return try {
            ref.update("isHelpful", !comment.isHelpful).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun reportPost(reporterUid: String, post: CommunityPostDoc, reason: String): Result<Unit> {
        val postRef = db.collection("community_posts").document(post.id)
        val caseId = "case_${UUID.randomUUID().toString().take(12)}"
        val caseRef = db.collection("moderation_cases").document(caseId)
        return try {
            val batch = db.batch()
            batch.update(
                postRef,
                mapOf(
                    "flagged" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                caseRef,
                mapOf(
                    "reporterUid" to reporterUid,
                    "targetType" to "community_post",
                    "targetId" to post.id,
                    "reason" to reason.trim().ifBlank { "Inappropriate content" }.take(118),
                    "details" to "Class=${post.classId} Title=${post.title}".take(900),
                    "status" to "open",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, caseRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteOwnPost(postId: String): Result<Unit> {
        val ref = db.collection("community_posts").document(postId)
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
}
