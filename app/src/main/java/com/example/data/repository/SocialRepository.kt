package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.model.BlockedUserDoc
import com.example.data.model.ChatMessageDoc
import com.example.data.model.ConversationDoc
import com.example.data.model.FriendRequestDoc
import com.example.data.model.FriendshipDoc
import com.example.data.model.PublicProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SocialRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeIncomingRequests(userId: String): Flow<List<FriendRequestDoc>> =
        db.collection("friend_requests")
            .whereEqualTo("toUid", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    FriendRequestDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).filter { it.status == "pending" }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeOutgoingRequests(userId: String): Flow<List<FriendRequestDoc>> =
        db.collection("friend_requests")
            .whereEqualTo("fromUid", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    FriendRequestDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).filter { it.status == "pending" }
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeFriendships(userId: String): Flow<List<FriendshipDoc>> =
        db.collection("friendships")
            .whereArrayContains("memberUids", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    FriendshipDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    fun observeBlockedUsers(userId: String): Flow<List<BlockedUserDoc>> =
        db.collection("blocked_users")
            .whereEqualTo("blockerUid", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    BlockedUserDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    fun observeConversations(userId: String): Flow<List<ConversationDoc>> {
        val memberFlow = db.collection("conversations")
            .whereArrayContains("memberUids", userId)
            .snapshots()
            .map {
                it.toObjects(
                    ConversationDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
        val invitedFlow = db.collection("conversations")
            .whereArrayContains("invitedUids", userId)
            .snapshots()
            .map {
                it.toObjects(
                    ConversationDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
        return combine(memberFlow, invitedFlow) { joined, invited ->
            (joined + invited)
                .distinctBy { it.id }
                .sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
        }
    }

    fun observeMessages(conversationId: String, currentUid: String): Flow<List<ChatMessageDoc>> =
        db.collection("conversations").document(conversationId).collection("messages")
            .whereArrayContains("memberUids", currentUid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ChatMessageDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }

    suspend fun fetchProfilesByIds(uids: List<String>): Map<String, PublicProfile> {
        if (uids.isEmpty()) return emptyMap()
        val map = mutableMapOf<String, PublicProfile>()
        for (uid in uids.distinct().take(40)) {
            runCatching {
                val snap = db.collection("public_profiles").document(uid).get().await()
                if (snap.exists()) {
                    snap.toObject(
                        PublicProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )?.let { map[uid] = it }
                }
            }
        }
        return map
    }

    suspend fun sendFriendRequest(
        fromProfile: PublicProfile,
        toProfile: PublicProfile
    ): Result<String> {
        if (fromProfile.userId == toProfile.userId) {
            return Result.failure(IllegalArgumentException("Cannot add yourself as a friend"))
        }
        val pair = canonicalPairKey(fromProfile.userId, toProfile.userId)
        val reqId = "frq_$pair"
        val reqRef = db.collection("friend_requests").document(reqId)
        return try {
            // Query via membership/ownership filters rather than probing non-existent deterministic IDs
            val existingFriendship = db.collection("friendships")
                .whereArrayContains("memberUids", fromProfile.userId)
                .get()
                .await()
                .documents
                .any { it.id == "fr_$pair" }
            if (existingFriendship) {
                return Result.failure(IllegalStateException("Already connected as friends"))
            }
            val blocked = db.collection("blocked_users")
                .whereEqualTo("blockerUid", fromProfile.userId)
                .get()
                .await()
                .documents
                .any { it.getString("blockedUid") == toProfile.userId }
            if (blocked) {
                return Result.failure(IllegalStateException("Unblock this student before sending a request"))
            }
            val outgoingReqs = db.collection("friend_requests")
                .whereEqualTo("fromUid", fromProfile.userId)
                .get()
                .await()
                .documents
            val incomingReqs = db.collection("friend_requests")
                .whereEqualTo("toUid", fromProfile.userId)
                .get()
                .await()
                .documents
            val matchingReq = (outgoingReqs + incomingReqs).firstOrNull { it.id == reqId }
            if (matchingReq != null && matchingReq.getString("status") == "pending") {
                return Result.failure(IllegalStateException("Friend request already pending"))
            }
            if (matchingReq != null) {
                // Clean up prior declined/cancelled request so re-request triggers allow create
                runCatching { reqRef.delete().await() }
            }

            reqRef.set(
                mapOf(
                    "fromUid" to fromProfile.userId,
                    "toUid" to toProfile.userId,
                    "fromDisplayName" to fromProfile.displayName.take(60),
                    "fromStudyQuestId" to fromProfile.studyQuestId,
                    "toDisplayName" to toProfile.displayName.take(60),
                    "toStudyQuestId" to toProfile.studyQuestId,
                    "status" to "pending",
                    "pairKey" to pair,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(reqId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reqRef.path)
            Result.failure(e)
        }
    }

    suspend fun respondToFriendRequest(
        request: FriendRequestDoc,
        accept: Boolean,
        currentUid: String
    ): Result<Unit> {
        val reqRef = db.collection("friend_requests").document(request.id)
        return try {
            val newStatus = if (currentUid == request.fromUid) {
                "cancelled"
            } else if (accept) {
                "accepted"
            } else {
                "declined"
            }
            reqRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (accept && currentUid == request.toUid) {
                val sorted = listOf(request.fromUid, request.toUid).sorted()
                val frRef = db.collection("friendships").document("fr_${request.pairKey}")
                frRef.set(
                    mapOf(
                        "memberUids" to sorted,
                        "pairKey" to request.pairKey,
                        "mutedByUids" to emptyList<String>(),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                val questRef = db.collection("users").document(currentUid).collection("quests").document("q_weekly_social")
                runCatching {
                    questRef.update(
                        mapOf(
                            "progress" to 1L,
                            "completed" to true,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, reqRef.path)
            Result.failure(e)
        }
    }

    suspend fun unfriend(friendshipId: String): Result<Unit> {
        val ref = db.collection("friendships").document(friendshipId)
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

    suspend fun blockStudent(blockerUid: String, targetUid: String): Result<Unit> {
        if (blockerUid == targetUid) return Result.failure(IllegalArgumentException("Cannot block yourself"))
        val pair = canonicalPairKey(blockerUid, targetUid)
        val blockRef = db.collection("blocked_users").document("blk_${blockerUid}_$targetUid")
        return try {
            blockRef.set(
                mapOf(
                    "blockerUid" to blockerUid,
                    "blockedUid" to targetUid,
                    "pairKey" to pair,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            runCatching { db.collection("friendships").document("fr_$pair").delete().await() }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, blockRef.path)
            Result.failure(e)
        }
    }

    suspend fun unblockStudent(blockDocId: String): Result<Unit> {
        val ref = db.collection("blocked_users").document(blockDocId)
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

    suspend fun openOrCreateDirectConversation(
        currentUid: String,
        friendProfile: PublicProfile
    ): Result<ConversationDoc> {
        val pair = canonicalPairKey(currentUid, friendProfile.userId)
        val convId = "dm_$pair"
        val convRef = db.collection("conversations").document(convId)
        return try {
            val existingSnap = db.collection("conversations")
                .whereArrayContains("memberUids", currentUid)
                .get()
                .await()
                .documents
                .firstOrNull { it.id == convId }
            if (existingSnap != null) {
                val existing = existingSnap.toObject(
                    ConversationDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: return Result.failure(IllegalStateException("Conversation unreadable"))
                return Result.success(existing)
            }
            val members = listOf(currentUid, friendProfile.userId).sorted()
            convRef.set(
                mapOf(
                    "type" to "direct",
                    "name" to friendProfile.displayName.take(78),
                    "ownerId" to currentUid,
                    "memberUids" to members,
                    "invitedUids" to emptyList<String>(),
                    "mutedByUids" to emptyList<String>(),
                    "lastMessagePreview" to null,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            ).await()
            val created = convRef.get().await().toObject(
                ConversationDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: ConversationDoc(id = convId, type = "direct", ownerId = currentUid, memberUids = members)
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, convRef.path)
            Result.failure(e)
        }
    }

    suspend fun createStudyGroup(
        ownerUid: String,
        groupName: String,
        invitedFriendUids: List<String>
    ): Result<ConversationDoc> {
        val cleanName = groupName.trim().take(78)
        if (cleanName.isBlank()) return Result.failure(IllegalArgumentException("Group name required"))
        val groupId = "grp_${UUID.randomUUID().toString().take(12)}"
        val ref = db.collection("conversations").document(groupId)
        return try {
            val invited = invitedFriendUids.filter { it != ownerUid }.distinct()
            ref.set(
                mapOf(
                    "type" to "group",
                    "name" to cleanName,
                    "avatarId" to "owl_classic",
                    "ownerId" to ownerUid,
                    "memberUids" to listOf(ownerUid),
                    "invitedUids" to invited,
                    "mutedByUids" to emptyList<String>(),
                    "lastMessagePreview" to "Study Group created",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val created = ref.get().await().toObject(
                ConversationDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: ConversationDoc(
                id = groupId,
                type = "group",
                name = cleanName,
                ownerId = ownerUid,
                memberUids = listOf(ownerUid),
                invitedUids = invited
            )
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun acceptGroupInvitation(conversation: ConversationDoc, userId: String): Result<Unit> {
        val ref = db.collection("conversations").document(conversation.id)
        return try {
            val newMembers = (conversation.memberUids + userId).distinct()
            val newInvited = conversation.invitedUids.filter { it != userId }
            ref.update(
                mapOf(
                    "memberUids" to newMembers,
                    "invitedUids" to newInvited,
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

    suspend fun leaveStudyGroup(conversation: ConversationDoc, userId: String): Result<Unit> {
        val ref = db.collection("conversations").document(conversation.id)
        return try {
            val remaining = conversation.memberUids.filter { it != userId }
            if (remaining.isEmpty()) {
                if (conversation.ownerId == userId) {
                    ref.delete().await()
                }
                return Result.success(Unit)
            }
            val newOwner = if (conversation.ownerId == userId) remaining.first() else conversation.ownerId
            ref.update(
                mapOf(
                    "memberUids" to remaining,
                    "ownerId" to newOwner,
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

    suspend fun toggleConversationMuted(conversation: ConversationDoc, userId: String): Result<Unit> {
        val ref = db.collection("conversations").document(conversation.id)
        return try {
            val muted = if (userId in conversation.mutedByUids) {
                conversation.mutedByUids - userId
            } else {
                conversation.mutedByUids + userId
            }
            ref.update(
                mapOf(
                    "mutedByUids" to muted.distinct(),
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

    suspend fun sendMessage(
        conversation: ConversationDoc,
        senderUid: String,
        senderName: String,
        rawText: String,
        replyTo: ChatMessageDoc? = null,
        quizInviteId: String? = null
    ): Result<String> {
        val cleanText = rawText.trim().take(980)
        if (cleanText.isBlank()) return Result.failure(IllegalArgumentException("Message cannot be empty"))
        if (containsSuspiciousLink(cleanText)) {
            return Result.failure(IllegalArgumentException("External links are blocked in student chat for safety."))
        }

        val msgId = "msg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val convRef = db.collection("conversations").document(conversation.id)
        val msgRef = convRef.collection("messages").document(msgId)
        return try {
            val batch = db.batch()
            batch.set(
                msgRef,
                mapOf(
                    "senderUid" to senderUid,
                    "senderName" to senderName.take(58),
                    "memberUids" to conversation.memberUids,
                    "text" to cleanText,
                    "replyToId" to replyTo?.id,
                    "replyPreview" to replyTo?.text?.take(120),
                    "reactions" to emptyMap<String, String>(),
                    "quizInviteId" to quizInviteId,
                    "deliveryStatus" to "sent",
                    "readByUids" to listOf(senderUid),
                    "createdAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            )
            batch.update(
                convRef,
                mapOf(
                    "lastMessagePreview" to cleanText.take(180),
                    "lastMessageAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(msgId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleMessageReaction(
        conversationId: String,
        message: ChatMessageDoc,
        userId: String,
        emoji: String
    ): Result<Unit> {
        val ref = db.collection("conversations").document(conversationId)
            .collection("messages").document(message.id)
        return try {
            val updated = message.reactions.toMutableMap()
            if (updated[userId] == emoji) updated.remove(userId) else updated[userId] = emoji
            ref.update("reactions", updated).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun markMessagesRead(
        conversationId: String,
        messages: List<ChatMessageDoc>,
        userId: String
    ) {
        val unread = messages.filter { it.senderUid != userId && userId !in it.readByUids }
        if (unread.isEmpty()) return
        for (msg in unread.take(20)) {
            runCatching {
                db.collection("conversations").document(conversationId)
                    .collection("messages").document(msg.id)
                    .update(
                        mapOf(
                            "readByUids" to (msg.readByUids + userId).distinct(),
                            "deliveryStatus" to "read"
                        )
                    ).await()
            }
        }
    }

    suspend fun reportIssue(
        reporterUid: String,
        targetType: String, // user, message, group, question, match
        targetId: String,
        reason: String,
        details: String
    ): Result<String> {
        val caseId = "case_${UUID.randomUUID().toString().take(12)}"
        val ref = db.collection("moderation_cases").document(caseId)
        return try {
            ref.set(
                mapOf(
                    "reporterUid" to reporterUid,
                    "targetType" to targetType,
                    "targetId" to targetId.take(120),
                    "reason" to reason.trim().take(118),
                    "details" to details.trim().take(980),
                    "status" to "open",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(caseId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    companion object {
        fun canonicalPairKey(uid1: String, uid2: String): String =
            listOf(uid1, uid2).sorted().joinToString("_")

        fun containsSuspiciousLink(text: String): Boolean {
            val lower = text.lowercase()
            return lower.contains("http://") ||
                lower.contains("https://") ||
                lower.contains("bit.ly/") ||
                lower.contains("tinyurl.com/") ||
                lower.contains("t.me/")
        }
    }
}
