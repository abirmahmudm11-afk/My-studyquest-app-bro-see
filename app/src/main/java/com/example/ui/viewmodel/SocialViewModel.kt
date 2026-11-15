package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.unwrapFirestoreMessage
import com.example.data.model.BlockedUserDoc
import com.example.data.model.ChatMessageDoc
import com.example.data.model.CommunityCommentDoc
import com.example.data.model.CommunityPostDoc
import com.example.data.model.ConversationDoc
import com.example.data.model.FriendRequestDoc
import com.example.data.model.FriendshipDoc
import com.example.data.model.PublicProfile
import com.example.data.repository.AuthAndProfileRepository
import com.example.data.repository.CommunityRepository
import com.example.data.repository.SocialRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SocialViewModel(
    private val currentUserId: String,
    private val socialRepo: SocialRepository,
    private val authRepo: AuthAndProfileRepository,
    private val communityRepo: CommunityRepository? = null
) : ViewModel() {

    val incomingRequests: StateFlow<List<FriendRequestDoc>> = socialRepo.observeIncomingRequests(currentUserId)
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            Log.w(TAG, "Incoming requests error", fsErr ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val outgoingRequests: StateFlow<List<FriendRequestDoc>> = socialRepo.observeOutgoingRequests(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val friendships: StateFlow<List<FriendshipDoc>> = socialRepo.observeFriendships(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val blockedUsers: StateFlow<List<BlockedUserDoc>> = socialRepo.observeBlockedUsers(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val conversations: StateFlow<List<ConversationDoc>> = socialRepo.observeConversations(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _friendProfiles = MutableStateFlow<Map<String, PublicProfile>>(emptyMap())
    val friendProfiles: StateFlow<Map<String, PublicProfile>> = _friendProfiles.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PublicProfile>>(emptyList())
    val searchResults: StateFlow<List<PublicProfile>> = _searchResults.asStateFlow()

    private val _activeConversation = MutableStateFlow<ConversationDoc?>(null)
    val activeConversation: StateFlow<ConversationDoc?> = _activeConversation.asStateFlow()

    val activeMessages: StateFlow<List<ChatMessageDoc>> = _activeConversation
        .flatMapLatest { conv ->
            if (conv == null) flowOf(emptyList())
            else socialRepo.observeMessages(conv.id, currentUserId).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _communityClassId = MutableStateFlow("class_10")
    val communityPosts: StateFlow<List<CommunityPostDoc>> = _communityClassId
        .flatMapLatest { cid ->
            communityRepo?.observeClassCommunityPosts(cid)?.catch { emit(emptyList()) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _selectedPost = MutableStateFlow<CommunityPostDoc?>(null)
    val selectedPost: StateFlow<CommunityPostDoc?> = _selectedPost.asStateFlow()

    val selectedPostComments: StateFlow<List<CommunityCommentDoc>> = _selectedPost
        .flatMapLatest { post ->
            if (post == null || communityRepo == null) flowOf(emptyList())
            else communityRepo.observeComments(post.id).catch { emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            friendships.collect { list ->
                val uids = list.flatMap { it.memberUids }.filter { it != currentUserId }
                if (uids.isNotEmpty()) {
                    _friendProfiles.value = socialRepo.fetchProfilesByIds(uids)
                }
            }
        }
        searchStudents("")
    }

    fun clearMessage() {
        _message.value = null
    }

    fun searchStudents(query: String) {
        viewModelScope.launch {
            val res = authRepo.searchStudents(query, currentUserId)
            res.onSuccess { _searchResults.value = it }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Search failed") }
        }
    }

    fun sendFriendRequest(myProfile: PublicProfile, target: PublicProfile) {
        viewModelScope.launch {
            val res = socialRepo.sendFriendRequest(myProfile, target)
            res.onSuccess { _message.value = "Friend request sent to ${target.displayName}" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Request failed") }
        }
    }

    fun respondToRequest(request: FriendRequestDoc, accept: Boolean) {
        viewModelScope.launch {
            val res = socialRepo.respondToFriendRequest(request, accept, currentUserId)
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Could not update request") }
        }
    }

    fun unfriend(friendshipId: String) {
        viewModelScope.launch {
            socialRepo.unfriend(friendshipId)
        }
    }

    fun blockUser(targetUid: String) {
        viewModelScope.launch {
            val res = socialRepo.blockStudent(currentUserId, targetUid)
            res.onSuccess {
                _activeConversation.value = null
                _message.value = "Student blocked"
            }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Block failed") }
        }
    }

    fun unblockUser(blockDocId: String) {
        viewModelScope.launch {
            socialRepo.unblockStudent(blockDocId)
        }
    }

    fun openDirectChat(friendProfile: PublicProfile) {
        viewModelScope.launch {
            val res = socialRepo.openOrCreateDirectConversation(currentUserId, friendProfile)
            res.onSuccess { _activeConversation.value = it }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Unable to open chat") }
        }
    }

    fun openConversation(conv: ConversationDoc) {
        _activeConversation.value = conv
    }

    fun closeConversation() {
        _activeConversation.value = null
    }

    fun createStudyGroup(name: String, invitedUids: List<String>) {
        viewModelScope.launch {
            val res = socialRepo.createStudyGroup(currentUserId, name, invitedUids)
            res.onSuccess {
                _activeConversation.value = it
                _message.value = "Study group '${it.name}' created"
            }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Could not create group") }
        }
    }

    fun acceptGroupInvitation(conv: ConversationDoc) {
        viewModelScope.launch {
            val res = socialRepo.acceptGroupInvitation(conv, currentUserId)
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Join failed") }
        }
    }

    fun leaveStudyGroup(conv: ConversationDoc) {
        viewModelScope.launch {
            socialRepo.leaveStudyGroup(conv, currentUserId)
            if (_activeConversation.value?.id == conv.id) _activeConversation.value = null
        }
    }

    fun toggleMute(conv: ConversationDoc) {
        viewModelScope.launch {
            socialRepo.toggleConversationMuted(conv, currentUserId)
        }
    }

    fun sendMessage(
        conv: ConversationDoc,
        senderName: String,
        text: String,
        replyTo: ChatMessageDoc? = null,
        quizInviteId: String? = null
    ) {
        viewModelScope.launch {
            val res = socialRepo.sendMessage(conv, currentUserId, senderName, text, replyTo, quizInviteId)
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Message not sent") }
        }
    }

    fun toggleReaction(convId: String, msg: ChatMessageDoc, emoji: String) {
        viewModelScope.launch {
            socialRepo.toggleMessageReaction(convId, msg, currentUserId, emoji)
        }
    }

    fun markVisibleMessagesRead(convId: String, messages: List<ChatMessageDoc>) {
        viewModelScope.launch {
            socialRepo.markMessagesRead(convId, messages, currentUserId)
        }
    }

    fun reportIssue(targetType: String, targetId: String, reason: String, details: String) {
        viewModelScope.launch {
            val res = socialRepo.reportIssue(currentUserId, targetType, targetId, reason, details)
            res.onSuccess { _message.value = "Report submitted for safety review" }
            res.onFailure { _message.value = unwrapFirestoreMessage(it, "Report failed") }
        }
    }

    fun setCommunityClassId(classId: String) {
        if (classId.isNotBlank()) _communityClassId.value = classId
    }

    fun openCommunityPost(post: CommunityPostDoc) {
        _selectedPost.value = post
    }

    fun closeCommunityPost() {
        _selectedPost.value = null
    }

    fun createCommunityPost(
        profile: PublicProfile,
        subjectId: String,
        postType: String,
        title: String,
        body: String
    ) {
        val repo = communityRepo ?: return
        viewModelScope.launch {
            val res = repo.createPost(profile, subjectId, postType, title, body)
            res.onSuccess { _message.value = "Posted to Class Community!" }
                .onFailure { _message.value = unwrapFirestoreMessage(it, it.localizedMessage ?: "Post failed") }
        }
    }

    fun addCommunityComment(profile: PublicProfile, post: CommunityPostDoc, text: String) {
        val repo = communityRepo ?: return
        viewModelScope.launch {
            val res = repo.addComment(profile, post, text)
            res.onFailure { _message.value = unwrapFirestoreMessage(it, it.localizedMessage ?: "Comment failed") }
        }
    }

    fun toggleCommunityUpvote(post: CommunityPostDoc) {
        val repo = communityRepo ?: return
        viewModelScope.launch {
            repo.toggleUpvotePost(post, currentUserId)
        }
    }

    fun markCommentHelpful(post: CommunityPostDoc, comment: CommunityCommentDoc) {
        val repo = communityRepo ?: return
        viewModelScope.launch {
            repo.markCommentHelpful(post, comment)
        }
    }

    fun reportCommunityPost(post: CommunityPostDoc, reason: String) {
        val repo = communityRepo ?: return
        viewModelScope.launch {
            val res = repo.reportPost(currentUserId, post, reason)
            res.onSuccess { _message.value = "Post flagged for moderation review" }
                .onFailure { _message.value = unwrapFirestoreMessage(it, "Report failed") }
        }
    }

    fun deleteOwnCommunityPost(postId: String) {
        val repo = communityRepo ?: return
        viewModelScope.launch {
            repo.deleteOwnPost(postId)
            if (_selectedPost.value?.id == postId) _selectedPost.value = null
        }
    }

    companion object {
        private const val TAG = "SocialVM"
    }
}
