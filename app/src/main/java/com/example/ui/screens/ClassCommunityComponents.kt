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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.CommunityCommentDoc
import com.example.data.model.CommunityPostDoc
import com.example.data.model.PublicProfile
import com.example.ui.components.AvatarBadgeView
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OfflineLockBadgeChip
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.LocalStudyQuestColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClassCommunityBoard(
    myProfile: PublicProfile,
    uiLanguage: AppLanguage,
    posts: List<CommunityPostDoc>,
    selectedPost: CommunityPostDoc?,
    comments: List<CommunityCommentDoc>,
    isGuestMode: Boolean,
    onOpenPost: (CommunityPostDoc) -> Unit,
    onClosePost: () -> Unit,
    onCreatePost: (subjectId: String, postType: String, title: String, body: String) -> Unit,
    onAddComment: (CommunityPostDoc, String) -> Unit,
    onToggleUpvote: (CommunityPostDoc) -> Unit,
    onMarkHelpful: (CommunityPostDoc, CommunityCommentDoc) -> Unit,
    onReportPost: (CommunityPostDoc, String) -> Unit,
    onDeleteOwnPost: (String) -> Unit,
    onRequestGoogleSignIn: () -> Unit,
    isOnline: Boolean = true
) {
    val semantic = LocalStudyQuestColors.current

    if (isGuestMode) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StudyQuestCard {
                    Text(
                        text = StudyQuestStrings.t(
                            uiLanguage,
                            "Class Community is exclusive to verified class students. Sign in with Google to ask doubts, share study tips, and help classmates in ${StudyQuestStrings.classLabel(myProfile.classId, uiLanguage)}!",
                            "শ্রেণি কমিউনিটিতে সহপাঠীদের সাথে প্রশ্ন ও উত্তর শেয়ার করতে গুগল অ্যাকাউন্ট দিয়ে সাইন-ইন করো!"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onRequestGoogleSignIn,
                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                    ) {
                        Text(StudyQuestStrings.t(uiLanguage, "Sign in with Google", "গুগল সাইন-ইন"))
                    }
                }
            }
        }
        return
    }

    if (selectedPost != null) {
        BackHandler { onClosePost() }
        var replyText by remember { mutableStateOf("") }
        val livePost = posts.find { it.id == selectedPost.id } ?: selectedPost

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClosePost, modifier = Modifier.testTag("community_post_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Discussion Thread", "আলোচনা"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    StudyQuestCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AvatarBadgeView(livePost.authorAvatarId, null, livePost.authorName, size = 36.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(livePost.authorName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "${livePost.subjectId.uppercase()} • ${livePost.postType.replace('_', ' ').uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CobaltPrimary
                                )
                            }
                            if (livePost.authorUid == myProfile.userId) {
                                IconButton(onClick = { onDeleteOwnPost(livePost.id) }) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete post")
                                }
                            } else {
                                IconButton(onClick = { onReportPost(livePost, "Community guideline review") }) {
                                    Icon(Icons.Filled.Flag, contentDescription = "Flag post")
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(livePost.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(livePost.body, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        val upvoted = myProfile.userId in livePost.upvoterUids
                        TextButton(
                            onClick = { onToggleUpvote(livePost) },
                            enabled = isOnline
                        ) {
                            Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = if (upvoted) CobaltPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${livePost.upvoterUids.size} Upvotes")
                        }
                    }
                }

                item {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Replies (${comments.size})", "উত্তরসমূহ (${comments.size})"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(comments, key = { it.id }) { c ->
                    StudyQuestCard(
                        containerColor = if (c.isHelpful) semantic.successContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(c.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            if (c.isHelpful) {
                                Surface(shape = RoundedCornerShape(999.dp), color = semantic.successContainer) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = semantic.success, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(StudyQuestStrings.t(uiLanguage, "Helpful", "সহায়ক"), style = MaterialTheme.typography.labelSmall, color = semantic.onSuccessContainer)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(c.body, style = MaterialTheme.typography.bodySmall)
                        if (livePost.authorUid == myProfile.userId) {
                            TextButton(onClick = { onMarkHelpful(livePost, c) }) {
                                Text(
                                    if (c.isHelpful) StudyQuestStrings.t(uiLanguage, "Unmark Helpful", "সহায়ক সরান")
                                    else StudyQuestStrings.t(uiLanguage, "Mark Helpful Answer", "সহায়ক উত্তর চিহ্নিত করো")
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = {
                        Text(
                            if (isOnline) StudyQuestStrings.t(uiLanguage, "Write a helpful reply...", "সহায়ক উত্তর লিখো...")
                            else StudyQuestStrings.t(uiLanguage, "Offline — replies unlock online", "অফলাইন — নেটওয়ার্কে উত্তর দিন")
                        )
                    },
                    enabled = isOnline,
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("community_reply_input")
                )
                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onAddComment(livePost, replyText)
                            replyText = ""
                        }
                    },
                    enabled = isOnline && replyText.isNotBlank(),
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("community_reply_send_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send reply", tint = CobaltPrimary)
                }
            }
        }
        return
    }

    var filterSubject by remember { mutableStateOf("all") }
    var filterType by remember { mutableStateOf("all") }
    var showComposer by remember { mutableStateOf(false) }

    val classSubjects = remember(myProfile.classId) { BangladeshCurriculumCatalog.subjectsForClass(myProfile.classId) }
    val filteredPosts = remember(posts, filterSubject, filterType) {
        posts.filter { p ->
            (filterSubject == "all" || p.subjectId == filterSubject) &&
                (filterType == "all" || p.postType == filterType)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            StudyQuestCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "${StudyQuestStrings.classLabel(myProfile.classId, uiLanguage)} Study Community",
                                "${StudyQuestStrings.classLabel(myProfile.classId, uiLanguage)} স্টাডি কমিউনিটি"
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "Safe peer Q&A, tips & notes strictly scoped to your class.",
                                "শুধুমাত্র তোমার শ্রেণির শিক্ষার্থীদের নিরাপদ প্রশ্ন-উত্তর ও নোট শেয়ারিং।"
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isOnline) {
                            OfflineLockBadgeChip(language = uiLanguage)
                        }
                        Button(
                            onClick = { showComposer = true },
                            enabled = isOnline,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("community_new_post_btn")
                        ) {
                            Icon(Icons.Filled.AddComment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(uiLanguage, "Ask", "লিখো"))
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = filterType == "all", onClick = { filterType = "all" }, label = { Text("All") })
                    listOf("question" to "Question", "study_help" to "Help", "tip" to "Tip", "discussion" to "Discussion").forEach { (k, lbl) ->
                        FilterChip(selected = filterType == k, onClick = { filterType = k }, label = { Text(lbl) })
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = filterSubject == "all", onClick = { filterSubject = "all" }, label = { Text("All Subjects") })
                    classSubjects.take(5).forEach { s ->
                        FilterChip(selected = filterSubject == s.id, onClick = { filterSubject = s.id }, label = { Text(s.code) })
                    }
                }
            }
        }

        if (filteredPosts.isEmpty()) {
            item {
                EmptyStateView(
                    titleEn = "No class posts yet",
                    titleBn = "তোমার শ্রেণিতে এখনো কোনো পোস্ট নেই",
                    subtitleEn = "Tap Ask to start a study question or share a helpful tip with classmates!",
                    subtitleBn = "সহপাঠীদের সাথে প্রশ্ন বা টিপস শেয়ার করতে 'লিখো' বোতামে ট্যাপ করো।",
                    language = uiLanguage
                )
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                val upvoted = myProfile.userId in post.upvoterUids
                StudyQuestCard(onClick = { onOpenPost(post) }, modifier = Modifier.testTag("community_post_${post.id}")) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AvatarBadgeView(post.authorAvatarId, null, post.authorName, size = 32.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(post.authorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "${post.subjectId.uppercase()} • ${post.postType.replace('_', ' ').uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CobaltPrimary
                            )
                        }
                        IconButton(
                            onClick = { onToggleUpvote(post) },
                            enabled = isOnline
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ThumbUp, contentDescription = "Upvote", tint = if (upvoted) CobaltPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("${post.upvoterUids.size}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(post.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(post.body.take(150), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Forum, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${post.commentCount} replies", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showComposer) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var subjectId by remember { mutableStateOf(classSubjects.firstOrNull()?.id ?: "math") }
        var postType by remember { mutableStateOf("question") }

        AlertDialog(
            onDismissRequest = { showComposer = false },
            title = { Text(StudyQuestStrings.t(uiLanguage, "New Class Post", "নতুন শ্রেণি পোস্ট")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("question" to "Question", "study_help" to "Help", "tip" to "Tip", "resource" to "Notes").forEach { (k, lbl) ->
                            FilterChip(selected = postType == k, onClick = { postType = k }, label = { Text(lbl) })
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        classSubjects.take(5).forEach { s ->
                            FilterChip(selected = subjectId == s.id, onClick = { subjectId = s.id }, label = { Text(s.code) })
                        }
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Title", "শিরোনাম")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("community_post_title_input")
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Details (no external links)", "বিস্তারিত লিখো")) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("community_post_body_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreatePost(subjectId, postType, title, body)
                        showComposer = false
                    },
                    enabled = title.trim().length >= 3 && body.trim().length >= 3,
                    modifier = Modifier.testTag("community_publish_btn")
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Post", "পোস্ট করো"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showComposer = false }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                }
            }
        )
    }
}
