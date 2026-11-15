package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.local.LocalStudyCircleNoteEntity
import com.example.data.model.BlockedUserDoc
import com.example.data.model.ChatMessageDoc
import com.example.data.model.CommunityCommentDoc
import com.example.data.model.CommunityPostDoc
import com.example.data.model.ConversationDoc
import com.example.data.model.FriendRequestDoc
import com.example.data.model.FriendshipDoc
import com.example.data.model.PublicProfile
import com.example.ui.components.AvatarBadgeView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TealSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FriendsTabScreen(
    myProfile: PublicProfile,
    uiLanguage: AppLanguage,
    friendships: List<FriendshipDoc>,
    friendProfiles: Map<String, PublicProfile>,
    incomingRequests: List<FriendRequestDoc>,
    outgoingRequests: List<FriendRequestDoc>,
    conversations: List<ConversationDoc>,
    blockedUsers: List<BlockedUserDoc>,
    searchResults: List<PublicProfile>,
    activeConversation: ConversationDoc?,
    activeMessages: List<ChatMessageDoc>,
    onSearchStudents: (String) -> Unit,
    onSendFriendRequest: (PublicProfile) -> Unit,
    onRespondFriendRequest: (FriendRequestDoc, Boolean) -> Unit,
    onUnfriend: (String) -> Unit,
    onOpenDirectChat: (PublicProfile) -> Unit,
    onOpenConversation: (ConversationDoc) -> Unit,
    onCloseConversation: () -> Unit,
    onCreateStudyGroup: (name: String, invitedUids: List<String>) -> Unit,
    onAcceptGroupInvite: (ConversationDoc) -> Unit,
    onLeaveGroup: (ConversationDoc) -> Unit,
    onToggleMute: (ConversationDoc) -> Unit,
    onBlockUser: (String) -> Unit,
    onUnblockUser: (String) -> Unit,
    onSendMessage: (ConversationDoc, String, ChatMessageDoc?) -> Unit,
    onToggleReaction: (String, ChatMessageDoc, String) -> Unit,
    onMarkRead: (String, List<ChatMessageDoc>) -> Unit,
    onReport: (targetType: String, targetId: String, reason: String, details: String) -> Unit,
    isGuestMode: Boolean = false,
    reducedMotion: Boolean = false,
    selectedCircleChannel: String = "ssc_math",
    studyCircleNotes: List<LocalStudyCircleNoteEntity> = emptyList(),
    isAiReplyingInCircle: Boolean = false,
    onSelectCircleChannel: (String) -> Unit = {},
    onPostStudyCircleNote: (String, Boolean) -> Unit = { _, _ -> },
    onToggleCircleReaction: (LocalStudyCircleNoteEntity, String) -> Unit = { _, _ -> },
    onDeleteCircleNote: (String) -> Unit = {},
    onRequestGoogleSignIn: () -> Unit = {},
    communityPosts: List<CommunityPostDoc> = emptyList(),
    selectedCommunityPost: CommunityPostDoc? = null,
    selectedPostComments: List<CommunityCommentDoc> = emptyList(),
    onOpenCommunityPost: (CommunityPostDoc) -> Unit = {},
    onCloseCommunityPost: () -> Unit = {},
    onCreateCommunityPost: (subjectId: String, postType: String, title: String, body: String) -> Unit = { _, _, _, _ -> },
    onAddCommunityComment: (CommunityPostDoc, String) -> Unit = { _, _ -> },
    onToggleCommunityUpvote: (CommunityPostDoc) -> Unit = {},
    onMarkCommentHelpful: (CommunityPostDoc, CommunityCommentDoc) -> Unit = { _, _ -> },
    onReportCommunityPost: (CommunityPostDoc, String) -> Unit = { _, _ -> },
    onDeleteOwnCommunityPost: (String) -> Unit = {}
) {
    if (activeConversation != null) {
        ChatConversationScreen(
            conversation = activeConversation,
            myProfile = myProfile,
            uiLanguage = uiLanguage,
            messages = activeMessages,
            onBack = onCloseConversation,
            onSendMessage = { txt, reply -> onSendMessage(activeConversation, txt, reply) },
            onToggleReaction = { msg, emoji -> onToggleReaction(activeConversation.id, msg, emoji) },
            onMarkRead = { msgs -> onMarkRead(activeConversation.id, msgs) },
            onToggleMute = { onToggleMute(activeConversation) },
            onLeaveGroup = { onLeaveGroup(activeConversation) },
            onBlockOther = {
                val other = activeConversation.memberUids.firstOrNull { it != myProfile.userId }
                if (other != null) onBlockUser(other)
            },
            onReportConversation = { reason ->
                onReport(
                    if (activeConversation.type == "group") "group" else "user",
                    activeConversation.id,
                    reason,
                    "Reported from StudyQuest chat"
                )
            }
        )
        return
    }

    var selectedTab by remember { mutableStateOf(0) } // 0=Friends & Chats (or Study Circle), 1=Discover, 2=Requests, 3=Study Groups
    var searchText by remember { mutableStateOf("") }
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        if (isGuestMode) StudyQuestStrings.t(uiLanguage, "Study Circle", "স্টাডি সার্কেল")
                        else StudyQuestStrings.t(uiLanguage, "Chats", "চ্যাট")
                    )
                },
                modifier = Modifier.testTag("friends_tab_chats")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Discover", "খুঁজুন")) },
                modifier = Modifier.testTag("friends_tab_discover")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    val count = incomingRequests.size
                    Text(
                        if (count > 0) StudyQuestStrings.t(uiLanguage, "Requests ($count)", "অনুরোধ ($count)")
                        else StudyQuestStrings.t(uiLanguage, "Requests", "অনুরোধ")
                    )
                },
                modifier = Modifier.testTag("friends_tab_requests")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Groups", "গ্রুপ")) },
                modifier = Modifier.testTag("friends_tab_groups")
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Community", "কমিউনিটি")) },
                modifier = Modifier.testTag("friends_tab_community")
            )
        }

        when (selectedTab) {
            0 -> {
                if (isGuestMode) {
                    GuestStudyCircleContent(
                        myProfile = myProfile,
                        uiLanguage = uiLanguage,
                        reducedMotion = reducedMotion,
                        selectedChannelId = selectedCircleChannel,
                        notes = studyCircleNotes,
                        isAiReplying = isAiReplyingInCircle,
                        onSelectChannel = onSelectCircleChannel,
                        onPostNote = onPostStudyCircleNote,
                        onToggleReaction = onToggleCircleReaction,
                        onDeleteNote = onDeleteCircleNote,
                        onRequestGoogleSignIn = onRequestGoogleSignIn
                    )
                } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (conversations.isNotEmpty()) {
                        item {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Recent Conversations", "সাম্প্রতিক কথোপকথন"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(conversations.filter { myProfile.userId in it.memberUids }, key = { it.id }) { conv ->
                            StudyQuestCard(onClick = { onOpenConversation(conv) }) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = conv.name ?: StudyQuestStrings.t(uiLanguage, "Direct Chat", "ব্যক্তিগত চ্যাট"),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = conv.lastMessagePreview ?: StudyQuestStrings.t(uiLanguage, "Tap to start chatting", "মেসেজ লিখতে ট্যাপ করো"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = CobaltPrimary)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "My Friends (${friendships.size})",
                                "আমার বন্ধু (${StudyQuestStrings.formatDigits(friendships.size, uiLanguage)})"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (friendships.isEmpty()) {
                        item {
                            EmptyStateView(
                                titleEn = "No friends added yet",
                                titleBn = "এখনো কোনো বন্ধু নেই",
                                subtitleEn = "Discover classmates using StudyQuest ID (${myProfile.studyQuestId}) or username.",
                                subtitleBn = "StudyQuest আইডি দিয়ে সহপাঠীদের খুঁজে বন্ধু বানাও।",
                                language = uiLanguage,
                                actionLabelEn = "Find Friends",
                                actionLabelBn = "বন্ধু খুঁজুন",
                                onAction = { selectedTab = 1 }
                            )
                        }
                    } else {
                        items(friendships, key = { it.id }) { fs ->
                            val friendUid = fs.memberUids.firstOrNull { it != myProfile.userId } ?: return@items
                            val fp = friendProfiles[friendUid] ?: PublicProfile(userId = friendUid, displayName = "Friend ${friendUid.take(5)}")
                            StudyQuestCard {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                        AvatarBadgeView(fp.avatarId, fp.frameId, fp.displayName, size = 44.dp)
                                        Column {
                                            Text(fp.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            val presence = if (fp.showOnlineStatus && fp.isOnline) {
                                                StudyQuestStrings.t(uiLanguage, "Online", "অনলাইন")
                                            } else {
                                                StudyQuestStrings.t(uiLanguage, "Offline", "অফলাইন")
                                            }
                                            Text(
                                                text = "${fp.studyQuestId} • $presence",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (fp.showOnlineStatus && fp.isOnline) TealSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilledTonalButton(onClick = { onOpenDirectChat(fp) }) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Chat", "চ্যাট"))
                                        }
                                        IconButton(onClick = { onUnfriend(fs.id) }) {
                                            Icon(Icons.Filled.Block, contentDescription = "Unfriend")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }

            1 -> {
                if (isGuestMode) {
                    GuestSocialFeatureGateCard(
                        titleEn = "Discover Classmates",
                        titleBn = "সহপাঠী খুঁজুন",
                        subtitleEn = "Find friends across Bangladesh by unique StudyQuest ID or username.",
                        subtitleBn = "StudyQuest আইডি বা ইউজারনেম দিয়ে সহপাঠী খুঁজুন।",
                        uiLanguage = uiLanguage,
                        reducedMotion = reducedMotion,
                        onSignInClick = onRequestGoogleSignIn
                    )
                } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        StudyQuestCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Your Invite ID", "তোমার আমন্ত্রণ আইডি"),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "${myProfile.studyQuestId} • @${myProfile.username}",
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CobaltPrimary
                            )
                        }
                    }

                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                label = { Text(StudyQuestStrings.t(uiLanguage, "Search SQ-ID, username or name", "SQ-ID বা নাম লিখুন")) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("discover_student_input")
                            )
                            Button(
                                onClick = { onSearchStudents(searchText) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text(StudyQuestStrings.t(uiLanguage, "Search", "খুঁজুন"))
                            }
                        }
                    }

                    val blockedIds = blockedUsers.map { it.blockedUid }.toSet()
                    val visibleResults = searchResults.filter { it.userId !in blockedIds }

                    items(visibleResults, key = { it.userId }) { candidate ->
                        val isFriend = friendships.any { candidate.userId in it.memberUids }
                        val pendingOut = outgoingRequests.any { it.toUid == candidate.userId }
                        StudyQuestCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                    AvatarBadgeView(candidate.avatarId, candidate.frameId, candidate.displayName, size = 42.dp)
                                    Column {
                                        Text(candidate.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${candidate.studyQuestId} • Lv.${candidate.overallLevel}", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                when {
                                    isFriend -> FilledTonalButton(onClick = { onOpenDirectChat(candidate) }) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Chat", "চ্যাট"))
                                    }
                                    pendingOut -> OutlinedButton(onClick = {}, enabled = false) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Pending", "অপেক্ষমান"))
                                    }
                                    else -> Button(
                                        onClick = { onSendFriendRequest(candidate) },
                                        modifier = Modifier.testTag("send_friend_req_${candidate.userId}")
                                    ) {
                                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(StudyQuestStrings.t(uiLanguage, "Add", "যুক্ত"))
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }

            2 -> {
                if (isGuestMode) {
                    GuestSocialFeatureGateCard(
                        titleEn = "Friend Requests & Safety",
                        titleBn = "বন্ধুত্বের অনুরোধ ও নিরাপত্তা",
                        subtitleEn = "Accept classmates you trust and manage block/report safety controls.",
                        subtitleBn = "বিশ্বস্ত সহপাঠীদের অনুরোধ গ্রহণ করুন এবং ব্লক/নিরাপত্তা নিয়ন্ত্রণ করুন।",
                        uiLanguage = uiLanguage,
                        reducedMotion = reducedMotion,
                        onSignInClick = onRequestGoogleSignIn
                    )
                } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(StudyQuestStrings.t(uiLanguage, "Incoming Requests", "আগত বন্ধুত্বের অনুরোধ"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    if (incomingRequests.isEmpty()) {
                        item {
                            Text(StudyQuestStrings.t(uiLanguage, "No pending incoming requests.", "কোনো নতুন অনুরোধ নেই।"), style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        items(incomingRequests, key = { it.id }) { req ->
                            StudyQuestCard {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(req.fromDisplayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(req.fromStudyQuestId ?: "", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(onClick = { onRespondFriendRequest(req, true) }) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Accept", "গ্রহণ"))
                                        }
                                        OutlinedButton(onClick = { onRespondFriendRequest(req, false) }) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Decline", "বাতিল"))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (blockedUsers.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(StudyQuestStrings.t(uiLanguage, "Blocked Accounts", "ব্লক তালিকা"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(blockedUsers, key = { it.id }) { blk ->
                            StudyQuestCard {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ID: ${blk.blockedUid.take(10)}", style = MaterialTheme.typography.bodySmall)
                                    OutlinedButton(onClick = { onUnblockUser(blk.id) }) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Unblock", "আনব্লক"))
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }

            3 -> {
                if (isGuestMode) {
                    GuestSocialFeatureGateCard(
                        titleEn = "Private Study Groups",
                        titleBn = "প্রাইভেট স্টাডি গ্রুপ",
                        subtitleEn = "Create invitation-only SSC & class study squads for live discussions and group quiz challenges.",
                        subtitleBn = "আমন্ত্রণ-ভিত্তিক স্টাডি গ্রুপে পড়াশোনা ও গ্রুপ কুইজ খেলুন।",
                        uiLanguage = uiLanguage,
                        reducedMotion = reducedMotion,
                        onSignInClick = onRequestGoogleSignIn
                    )
                } else {
                val groups = conversations.filter { it.type == "group" }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = { showCreateGroupDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("create_study_group_btn")
                        ) {
                            Icon(Icons.Filled.GroupAdd, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(uiLanguage, "Create Private Study Group", "প্রাইভেট স্টাডি গ্রুপ তৈরি"))
                        }
                    }

                    if (groups.isEmpty()) {
                        item {
                            EmptyStateView(
                                titleEn = "No Study Groups yet",
                                titleBn = "কোনো স্টাডি গ্রুপ নেই",
                                subtitleEn = "Create an invitation-only study group to discuss chapters and challenge teammates.",
                                subtitleBn = "সহপাঠীদের সাথে পড়া ও কুইজ আয়োজনের জন্য স্টাডি গ্রুপ তৈরি করো।",
                                language = uiLanguage
                            )
                        }
                    } else {
                        items(groups, key = { it.id }) { grp ->
                            val isInvited = myProfile.userId in grp.invitedUids && myProfile.userId !in grp.memberUids
                            StudyQuestCard {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(grp.name ?: "Study Group", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${grp.memberUids.size} members",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isInvited) {
                                        Button(onClick = { onAcceptGroupInvite(grp) }) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Join Group", "যোগ দাও"))
                                        }
                                    } else {
                                        FilledTonalButton(onClick = { onOpenConversation(grp) }) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Open Chat", "চ্যাট খুলুন"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }

            4 -> {
                ClassCommunityBoard(
                    myProfile = myProfile,
                    uiLanguage = uiLanguage,
                    posts = communityPosts,
                    selectedPost = selectedCommunityPost,
                    comments = selectedPostComments,
                    isGuestMode = isGuestMode,
                    onOpenPost = onOpenCommunityPost,
                    onClosePost = onCloseCommunityPost,
                    onCreatePost = onCreateCommunityPost,
                    onAddComment = onAddCommunityComment,
                    onToggleUpvote = onToggleCommunityUpvote,
                    onMarkHelpful = onMarkCommentHelpful,
                    onReportPost = onReportCommunityPost,
                    onDeleteOwnPost = onDeleteOwnCommunityPost,
                    onRequestGoogleSignIn = onRequestGoogleSignIn
                )
            }
        }
    }

    if (showCreateGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        var invitedSet by remember { mutableStateOf<Set<String>>(emptySet()) }
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text(StudyQuestStrings.t(uiLanguage, "New Study Group", "নতুন স্টাডি গ্রুপ")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Group Name (e.g. SSC Science Squad)", "গ্রুপের নাম")) },
                        singleLine = true
                    )
                    if (friendProfiles.isNotEmpty()) {
                        Text(StudyQuestStrings.t(uiLanguage, "Invite Friends:", "বন্ধুদের আমন্ত্রণ জানান:"), style = MaterialTheme.typography.labelMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            friendProfiles.values.forEach { fp ->
                                val picked = fp.userId in invitedSet
                                FilterChip(
                                    selected = picked,
                                    onClick = { invitedSet = if (picked) invitedSet - fp.userId else invitedSet + fp.userId },
                                    label = { Text(fp.displayName) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onCreateStudyGroup(groupName, invitedSet.toList())
                            showCreateGroupDialog = false
                        }
                    }
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Create", "তৈরি"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                }
            }
        )
    }
}

@Composable
fun ChatConversationScreen(
    conversation: ConversationDoc,
    myProfile: PublicProfile,
    uiLanguage: AppLanguage,
    messages: List<ChatMessageDoc>,
    onBack: () -> Unit,
    onSendMessage: (String, ChatMessageDoc?) -> Unit,
    onToggleReaction: (ChatMessageDoc, String) -> Unit,
    onMarkRead: (List<ChatMessageDoc>) -> Unit,
    onToggleMute: () -> Unit,
    onLeaveGroup: () -> Unit,
    onBlockOther: () -> Unit,
    onReportConversation: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    var inputText by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<ChatMessageDoc?>(null) }

    LaunchedEffect(messages.size) {
        onMarkRead(messages)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = conversation.name ?: StudyQuestStrings.t(uiLanguage, "Direct Chat", "ব্যক্তিগত চ্যাট"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${conversation.memberUids.size} members",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = onToggleMute) {
                        Icon(Icons.Filled.NotificationsOff, contentDescription = "Mute")
                    }
                    if (conversation.type == "direct") {
                        IconButton(onClick = onBlockOther) {
                            Icon(Icons.Filled.Block, contentDescription = "Block user")
                        }
                    } else {
                        TextButton(onClick = onLeaveGroup) {
                            Text(StudyQuestStrings.t(uiLanguage, "Leave", "ত্যাগ"))
                        }
                    }
                    IconButton(onClick = { onReportConversation("Inappropriate chat message") }) {
                        Icon(Icons.Filled.Report, contentDescription = "Report")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderUid == myProfile.userId
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(msg.senderName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            if (!msg.replyPreview.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "↩ ${msg.replyPreview}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                            Text(msg.text, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                listOf("👍", "🔥").forEach { emoji ->
                                    val count = msg.reactions.values.count { it == emoji }
                                    TextButton(
                                        onClick = { onToggleReaction(msg, emoji) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text(if (count > 0) "$emoji $count" else emoji, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                TextButton(
                                    onClick = { replyTarget = msg },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text(StudyQuestStrings.t(uiLanguage, "Reply", "উত্তর"), style = MaterialTheme.typography.labelSmall)
                                }
                                if (isMine) {
                                    Text(
                                        text = if (msg.readByUids.size > 1) "✓✓" else "✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CobaltPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (replyTarget != null) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Replying to: ${replyTarget?.text?.take(40)}", style = MaterialTheme.typography.labelSmall)
                    TextButton(onClick = { replyTarget = null }) { Text("✕") }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(StudyQuestStrings.t(uiLanguage, "Write a safe study message...", "মেসেজ লেখো...")) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input")
            )
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText, replyTarget)
                        inputText = ""
                        replyTarget = null
                    }
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("chat_send_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}
