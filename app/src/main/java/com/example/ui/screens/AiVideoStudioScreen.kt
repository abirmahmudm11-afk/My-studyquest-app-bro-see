package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.LearningVideoDoc
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.UserSubscriptionDoc
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.viewmodel.AiVideoViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiVideoStudioScreen(
    uiLanguage: AppLanguage,
    classId: String,
    ownerName: String = "Student",
    currentUserId: String = "",
    subscription: UserSubscriptionDoc = UserSubscriptionDoc(),
    aiVideoVm: AiVideoViewModel,
    isOnline: Boolean,
    externalVideoRendererConfigured: Boolean,
    isGuestMode: Boolean,
    onOpenVideoUpgrade: () -> Unit = {},
    onRequestGoogleSignIn: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        if (aiVideoVm.activeVideo.value != null) {
            aiVideoVm.closeVideo()
        } else {
            onBack()
        }
    }

    val jobs by aiVideoVm.jobs.collectAsStateWithLifecycle()
    val myVideos by aiVideoVm.videos.collectAsStateWithLifecycle()
    val publicLibrary by aiVideoVm.publicLibraryVideos.collectAsStateWithLifecycle()
    val activeVideo by aiVideoVm.activeVideo.collectAsStateWithLifecycle()
    val scenes by aiVideoVm.scenes.collectAsStateWithLifecycle()
    val quizQuestions by aiVideoVm.quizQuestions.collectAsStateWithLifecycle()
    val sceneIdx by aiVideoVm.currentSceneIndex.collectAsStateWithLifecycle()
    val sceneElapsed by aiVideoVm.sceneElapsedSec.collectAsStateWithLifecycle()
    val isPlaying by aiVideoVm.isPlaying.collectAsStateWithLifecycle()
    val ttsEnabled by aiVideoVm.ttsEnabled.collectAsStateWithLifecycle()
    val isGenerating by aiVideoVm.isGenerating.collectAsStateWithLifecycle()
    val message by aiVideoVm.message.collectAsStateWithLifecycle()

    val semantic = LocalStudyQuestColors.current
    var studioTab by remember { mutableIntStateOf(0) } // 0 = Library (Free), 1 = Create Animated, 2 = Upload Video
    var editingVideo by remember { mutableStateOf<LearningVideoDoc?>(null) }

    val activePlan = remember(subscription.tier) { QuestPlanCatalog.planById(subscription.tier) }
    val monthlyVidLimit = subscription.monthlyVideoLimit.toInt().coerceAtLeast(activePlan.monthlyVideoCredits)
    val monthlyVidLeft = (monthlyVidLimit - subscription.videoCreditsUsed.toInt()).coerceAtLeast(0)
    val totalVidLeft = monthlyVidLeft + subscription.bonusVideoCredits.toInt().coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (activeVideo != null) aiVideoVm.closeVideo() else onBack() },
                modifier = Modifier.testTag("ai_video_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Quest Video Library & Studio", "ভিডিও লাইব্রেরি ও স্টুডিও"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isGuestMode) {
                        StudyQuestStrings.t(uiLanguage, "Free Public Library Open • Sign in to Create", "ফ্রি পাবলিক লাইব্রেরি উন্মুক্ত")
                    } else {
                        StudyQuestStrings.t(
                            uiLanguage,
                            "$totalVidLeft video credit(s) left • Free Library Open",
                            "ভিডিও ক্রেডিট বাকি: $totalVidLeft • লাইব্রেরি উন্মুক্ত"
                        )
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!isGuestMode && activeVideo == null) {
                TextButton(onClick = onOpenVideoUpgrade) {
                    Text(StudyQuestStrings.t(uiLanguage, "Boost ৳20", "বুস্ট ৳২০"))
                }
            }
        }

        if (!message.isNullOrBlank()) {
            StudyQuestCard(
                containerColor = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(message.orEmpty(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { aiVideoVm.clearMessage() }) { Text("OK") }
                }
            }
        }

        val currentVid = activeVideo
        if (currentVid != null) {
            // Player Stage (Uploaded Native Video OR Animated Canvas Storyboard)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StudyQuestCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currentVid.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${currentVid.classId.uppercase()} • ${currentVid.subjectId.uppercase()} • ${currentVid.visibility.uppercase()} • by ${currentVid.ownerName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (currentUserId.isNotBlank() && currentVid.ownerUid == currentUserId) {
                                IconButton(onClick = { editingVideo = currentVid }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit video details")
                                }
                            }
                        }
                        if (currentVid.description.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(currentVid.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (currentVid.videoSourceType == "uploaded_video" && !currentVid.localVideoUri.isNullOrBlank()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .testTag("uploaded_video_player")
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        val mediaController = MediaController(ctx)
                                        mediaController.setAnchorView(this)
                                        setMediaController(mediaController)
                                        setVideoURI(Uri.parse(currentVid.localVideoUri))
                                        requestFocus()
                                        start()
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else if (scenes.isNotEmpty()) {
                    val safeIdx = sceneIdx.coerceIn(0, scenes.lastIndex)
                    val currentScene = scenes[safeIdx]

                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("storyboard_player_stage")
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(235.dp)) {
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val accentColor = when (currentVid.animationStyle) {
                                        "diagram_focus" -> Color(0xFF1F5D6B)
                                        "step_cards" -> Color(0xFF283A4E)
                                        else -> Color(0xFF237B7A)
                                    }
                                    drawRect(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF0E1621), Color(0xFF1B2B3C), accentColor),
                                            start = Offset.Zero,
                                            end = Offset(size.width, size.height)
                                        )
                                    )
                                    drawCircle(
                                        color = Color(0xFF79C7BA).copy(alpha = 0.16f),
                                        radius = size.minDimension * 0.42f,
                                        center = Offset(size.width * 0.88f, size.height * 0.22f)
                                    )
                                }
                                AnimatedContent(
                                    targetState = currentScene,
                                    label = "scene_stage",
                                    modifier = Modifier.fillMaxSize().padding(18.dp)
                                ) { sc ->
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.18f)) {
                                                Text(
                                                    text = "Scene ${safeIdx + 1}/${scenes.size} • ${sc.visualType.uppercase()}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            Text(
                                                text = "${sceneElapsed}s / ${sc.durationSec}s",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = sc.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            if (sc.formulaOrDiagram.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color.Black.copy(alpha = 0.35f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = sc.formulaOrDiagram,
                                                        fontFamily = JetBrainsMonoFontFamily,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF9EDCCF),
                                                        modifier = Modifier.padding(10.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = sc.caption,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.92f)
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { (sceneElapsed.toFloat() / sc.durationSec.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                                            color = Color(0xFF79C7BA),
                                            trackColor = Color.White.copy(alpha = 0.25f),
                                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(999.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        StudyQuestCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { aiVideoVm.togglePlayPause() },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("storyboard_play_pause_btn")
                                ) {
                                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isPlaying) StudyQuestStrings.t(uiLanguage, "Pause", "বিরতি") else StudyQuestStrings.t(uiLanguage, "Play Lesson", "চালু করো"))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Voice", "ভয়েস"), style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.width(6.dp))
                                    Switch(checked = ttsEnabled, onCheckedChange = { aiVideoVm.toggleTts(it) })
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                scenes.forEachIndexed { idx, _ ->
                                    FilterChip(
                                        selected = idx == safeIdx,
                                        onClick = { aiVideoVm.seekToScene(idx) },
                                        label = { Text("S${idx + 1}") }
                                    )
                                }
                            }
                        }
                    }
                }

                if (quizQuestions.isNotEmpty()) {
                    item {
                        val pickedAnswers = remember(currentVid.id) { mutableStateMapOf<Int, Int>() }
                        var quizSubmitted by remember(currentVid.id) { mutableStateOf(false) }

                        StudyQuestCard(modifier = Modifier.testTag("storyboard_check_quiz_card")) {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Post-Lesson Comprehension Quiz", "ভিডিও পরবর্তী কুইজ"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            quizQuestions.forEachIndexed { qIdx, qq ->
                                Text("${qIdx + 1}. ${qq.prompt}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                qq.options.forEachIndexed { oIdx, opt ->
                                    val picked = pickedAnswers[qIdx] == oIdx
                                    val isRight = oIdx == qq.correctIndex
                                    FilterChip(
                                        selected = picked,
                                        onClick = { if (!quizSubmitted) pickedAnswers[qIdx] = oIdx },
                                        label = {
                                            Text(
                                                text = if (quizSubmitted && isRight) "$opt ✓" else opt,
                                                color = if (quizSubmitted && isRight) semantic.success else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (quizSubmitted && qq.explanation.isNotBlank()) {
                                    Text(qq.explanation, style = MaterialTheme.typography.labelSmall, color = semantic.success)
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            Button(
                                onClick = {
                                    quizSubmitted = true
                                    val correct = quizQuestions.indices.count { i -> pickedAnswers[i] == quizQuestions[i].correctIndex }
                                    aiVideoVm.submitVideoCheckQuiz(currentVid, correct, quizQuestions.size)
                                },
                                enabled = !quizSubmitted && pickedAnswers.size == quizQuestions.size && !isGuestMode,
                                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("submit_storyboard_quiz_btn")
                            ) {
                                Text(
                                    when {
                                        isGuestMode -> StudyQuestStrings.t(uiLanguage, "Sign in to Claim XP", "XP পেতে সাইন-ইন করো")
                                        quizSubmitted -> StudyQuestStrings.t(uiLanguage, "Quiz Verified!", "কুইজ যাচাই সম্পন্ন")
                                        else -> StudyQuestStrings.t(uiLanguage, "Submit Quiz & Claim XP", "কুইজ জমা ও XP সংগ্রহ")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // 3 Studio Tabs
            TabRow(selectedTabIndex = studioTab) {
                Tab(
                    selected = studioTab == 0,
                    onClick = { studioTab = 0 },
                    text = { Text(StudyQuestStrings.t(uiLanguage, "Library (Free)", "লাইব্রেরি (ফ্রি)")) },
                    modifier = Modifier.testTag("video_tab_library")
                )
                Tab(
                    selected = studioTab == 1,
                    onClick = { studioTab = 1 },
                    text = { Text(StudyQuestStrings.t(uiLanguage, "Create Animated", "অ্যানিমেটেড তৈরি")) },
                    modifier = Modifier.testTag("video_tab_create")
                )
                Tab(
                    selected = studioTab == 2,
                    onClick = { studioTab = 2 },
                    text = { Text(StudyQuestStrings.t(uiLanguage, "Upload Video", "আপলোড ভিডিও")) },
                    modifier = Modifier.testTag("video_tab_upload")
                )
            }

            when (studioTab) {
                0 -> {
                    var searchQuery by remember { mutableStateOf("") }
                    var filterClass by remember { mutableStateOf("all") }
                    var filterSubject by remember { mutableStateOf("all") }

                    val combinedLibrary = remember(myVideos, publicLibrary, searchQuery, filterClass, filterSubject) {
                        val merged = (myVideos + publicLibrary).distinctBy { it.id }
                        merged.filter { v ->
                            val matchesSearch = searchQuery.isBlank() ||
                                v.title.contains(searchQuery, ignoreCase = true) ||
                                v.description.contains(searchQuery, ignoreCase = true) ||
                                v.subjectId.contains(searchQuery, ignoreCase = true)
                            val matchesCls = filterClass == "all" || v.classId == filterClass
                            val matchesSubj = filterSubject == "all" || v.subjectId == filterSubject
                            matchesSearch && matchesCls && matchesSubj
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                label = { Text(StudyQuestStrings.t(uiLanguage, "Search videos by title or topic...", "শিরোনাম বা বিষয় লিখে খুঁজো...")) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("video_library_search")
                            )
                            Spacer(Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(selected = filterClass == "all", onClick = { filterClass = "all" }, label = { Text("All Classes") })
                                BangladeshCurriculumCatalog.classes.filter { it.futureReadyTag == null }.forEach { c ->
                                    FilterChip(
                                        selected = filterClass == c.id,
                                        onClick = { filterClass = c.id },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, c.labelEn, c.labelBn)) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(selected = filterSubject == "all", onClick = { filterSubject = "all" }, label = { Text("All Subjects") })
                                BangladeshCurriculumCatalog.subjectsForClass(classId).take(5).forEach { s ->
                                    FilterChip(
                                        selected = filterSubject == s.id,
                                        onClick = { filterSubject = s.id },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, s.titleEn, s.titleBn)) }
                                    )
                                }
                            }
                        }

                        if (combinedLibrary.isEmpty()) {
                            item {
                                EmptyStateView(
                                    titleEn = "No matching videos found",
                                    titleBn = "কোনো ভিডিও পাওয়া যায়নি",
                                    subtitleEn = "Clear filters or create your own animated lesson in Tab 2.",
                                    subtitleBn = "ফিল্টার পরিবর্তন করো অথবা নতুন অ্যানিমেটেড ভিডিও তৈরি করো।",
                                    language = uiLanguage
                                )
                            }
                        } else {
                            items(combinedLibrary, key = { it.id }) { vid ->
                                val isSaved = currentUserId.isNotBlank() && currentUserId in vid.savedByUids
                                val isOwned = currentUserId.isNotBlank() && vid.ownerUid == currentUserId
                                StudyQuestCard(
                                    onClick = { aiVideoVm.openVideo(vid, uiLanguage) },
                                    modifier = Modifier.testTag("open_video_${vid.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(
                                            imageVector = if (vid.videoSourceType == "uploaded_video") Icons.Filled.VideoFile else Icons.Filled.VideoLibrary,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(vid.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${vid.classId.uppercase()} • ${vid.subjectId.uppercase()} • ${vid.durationSeconds}s • ${vid.visibility.uppercase()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val descText = vid.description.ifBlank { vid.summary }
                                            if (descText.isNotBlank()) {
                                                Text(
                                                    text = descText.take(90),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (!isGuestMode) {
                                            IconButton(onClick = { aiVideoVm.toggleBookmarkVideo(vid) }) {
                                                Icon(
                                                    if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                    contentDescription = "Save video"
                                                )
                                            }
                                        }
                                        if (isOwned) {
                                            IconButton(onClick = { editingVideo = vid }) {
                                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                            }
                                        }
                                        FilledTonalButton(onClick = { aiVideoVm.openVideo(vid, uiLanguage) }) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Watch", "দেখো"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    if (isGuestMode) {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                            item {
                                StudyQuestCard {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "Sign in with Google to create animated lesson videos with voiceover & quizzes (Free plan includes 1/month!).",
                                            "ভয়েসওভার ও কুইজসহ অ্যানিমেটেড লেসন তৈরি করতে গুগল অ্যাকাউন্টে সাইন-ইন করো।"
                                        ),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Button(onClick = onRequestGoogleSignIn, modifier = Modifier.fillMaxWidth()) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Sign in with Google", "গুগল সাইন-ইন"))
                                    }
                                }
                            }
                        }
                    } else {
                        var inputMode by remember { mutableStateOf("chapter") } // "chapter" or "document"
                        val availableSubjects = remember(classId) { BangladeshCurriculumCatalog.subjectsForClass(classId) }
                        var selectedSubjectId by remember { mutableStateOf(availableSubjects.firstOrNull()?.id ?: "math") }
                        val chaptersForSubject = remember(selectedSubjectId) {
                            BangladeshCurriculumCatalog.chapters.filter { it.subjectId == selectedSubjectId }
                        }
                        var selectedChapterId by remember(selectedSubjectId) {
                            mutableStateOf(chaptersForSubject.firstOrNull()?.id ?: "")
                        }
                        var customTitle by remember { mutableStateOf("") }
                        var customDescription by remember { mutableStateOf("") }
                        var lessonLang by remember { mutableStateOf(uiLanguage) }
                        var animStyle by remember { mutableStateOf("editorial") }
                        var visibility by remember { mutableStateOf("private") }
                        var pickedUri by remember { mutableStateOf<Uri?>(null) }
                        var pastedNotes by remember { mutableStateOf("") }

                        val documentPicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument()
                        ) { uri -> if (uri != null) pickedUri = uri }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                StudyQuestCard {
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, "Create Animated Video Lesson", "নতুন অ্যানিমেটেড লেসন তৈরি"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "Credits remaining: $totalVidLeft (1 credit per animated lesson; automatically refunded if generation fails).",
                                            "বাকি ক্রেডিট: $totalVidLeft (ব্যর্থ হলে ক্রেডিট স্বয়ংক্রিয়ভাবে ফেরত পাবেন)।"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = customTitle,
                                        onValueChange = { customTitle = it },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, "Lesson Title (optional)", "লেসনের নাম (ঐচ্ছিক)")) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("create_video_title_input")
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = customDescription,
                                        onValueChange = { customDescription = it },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, "Short Description (optional)", "সংক্ষিপ্ত বিবরণ (ঐচ্ছিক)")) },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().testTag("create_video_desc_input")
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Voiceover & Subtitle Language", "ভয়েস ও সাবটাইটেলের ভাষা"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AppLanguage.entries.forEach { l ->
                                            FilterChip(
                                                selected = lessonLang == l,
                                                onClick = { lessonLang = l },
                                                label = { Text(l.labelEn) }
                                            )
                                        }
                                    }
                                    Text(StudyQuestStrings.t(uiLanguage, "Animation Style", "অ্যানিমেশন স্টাইল"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("editorial" to "Editorial Board", "diagram_focus" to "Diagram Focus", "step_cards" to "Step Cards").forEach { (id, lbl) ->
                                            FilterChip(selected = animStyle == id, onClick = { animStyle = id }, label = { Text(lbl) })
                                        }
                                    }
                                    Text(StudyQuestStrings.t(uiLanguage, "Visibility", "প্রকাশনা"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(selected = visibility == "private", onClick = { visibility = "private" }, label = { Text("Private") })
                                        FilterChip(selected = visibility == "public", onClick = { visibility = "public" }, label = { Text("Public Library") })
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = inputMode == "chapter",
                                            onClick = { inputMode = "chapter" },
                                            label = { Text(StudyQuestStrings.t(uiLanguage, "NCTB Chapter", "এনসিটিবি অধ্যায়")) },
                                            modifier = Modifier.testTag("aivid_mode_chapter")
                                        )
                                        FilterChip(
                                            selected = inputMode == "document",
                                            onClick = { inputMode = "document" },
                                            label = { Text(StudyQuestStrings.t(uiLanguage, "PDF / Notes", "PDF / নোট")) },
                                            modifier = Modifier.testTag("aivid_mode_doc")
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        availableSubjects.take(5).forEach { s ->
                                            FilterChip(
                                                selected = selectedSubjectId == s.id,
                                                onClick = { selectedSubjectId = s.id },
                                                label = { Text(StudyQuestStrings.t(uiLanguage, s.titleEn, s.titleBn)) }
                                            )
                                        }
                                    }

                                    if (inputMode == "chapter") {
                                        Spacer(Modifier.height(6.dp))
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            chaptersForSubject.forEach { ch ->
                                                FilterChip(
                                                    selected = selectedChapterId == ch.id,
                                                    onClick = { selectedChapterId = ch.id },
                                                    label = { Text(StudyQuestStrings.t(uiLanguage, ch.titleEn, ch.titleBn)) }
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                aiVideoVm.generateFromChapter(
                                                    ownerName = ownerName,
                                                    classId = classId,
                                                    subjectId = selectedSubjectId,
                                                    chapterId = selectedChapterId,
                                                    customTitle = customTitle,
                                                    customDescription = customDescription,
                                                    animationStyle = animStyle,
                                                    visibility = visibility,
                                                    language = lessonLang,
                                                    subscription = subscription
                                                )
                                            },
                                            enabled = isOnline && !isGenerating && selectedChapterId.isNotBlank() && totalVidLeft > 0,
                                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("generate_chapter_storyboard_btn")
                                        ) {
                                            if (isGenerating) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                                Spacer(Modifier.width(8.dp))
                                                Text(StudyQuestStrings.t(uiLanguage, "Generating Lesson...", "ভিডিও তৈরি হচ্ছে..."))
                                            } else {
                                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                                Spacer(Modifier.width(6.dp))
                                                Text(StudyQuestStrings.t(uiLanguage, "Generate Animated Lesson (1 Credit)", "অ্যানিমেটেড লেসন তৈরি (১ ক্রেডিট)"))
                                            }
                                        }
                                    } else {
                                        Spacer(Modifier.height(6.dp))
                                        OutlinedButton(
                                            onClick = { documentPicker.launch(arrayOf("application/pdf", "text/plain")) },
                                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("pick_pdf_or_note_btn")
                                        ) {
                                            Icon(Icons.Filled.UploadFile, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text(pickedUri?.lastPathSegment ?: StudyQuestStrings.t(uiLanguage, "Pick PDF or Text File (Max 20MB)", "PDF বা টেক্সট ফাইল নির্বাচন করো"))
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = pastedNotes,
                                            onValueChange = { pastedNotes = it },
                                            label = { Text(StudyQuestStrings.t(uiLanguage, "Or paste topic notes (optional)", "অথবা নোট পেস্ট করো")) },
                                            minLines = 3,
                                            modifier = Modifier.fillMaxWidth().testTag("aivid_notes_input")
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                aiVideoVm.generateFromDocumentOrNotes(
                                                    ownerName = ownerName,
                                                    classId = classId,
                                                    subjectId = selectedSubjectId,
                                                    documentUri = pickedUri,
                                                    pastedNotes = pastedNotes,
                                                    customTitle = customTitle,
                                                    customDescription = customDescription,
                                                    animationStyle = animStyle,
                                                    visibility = visibility,
                                                    language = lessonLang,
                                                    subscription = subscription
                                                )
                                            },
                                            enabled = isOnline && !isGenerating && (pickedUri != null || pastedNotes.isNotBlank()) && totalVidLeft > 0,
                                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("generate_doc_storyboard_btn")
                                        ) {
                                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text(StudyQuestStrings.t(uiLanguage, "Generate from Notes / PDF (1 Credit)", "নোট থেকে তৈরি (১ ক্রেডিট)"))
                                        }
                                    }
                                    if (totalVidLeft == 0) {
                                        Spacer(Modifier.height(6.dp))
                                        OutlinedButton(onClick = onOpenVideoUpgrade, modifier = Modifier.fillMaxWidth()) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Out of Credits — Get Video Boost (৳20) or Video Pass (৳99/mo)", "ক্রেডিট শেষ — ভিডিও বুস্ট (৳২০) নিন"))
                                        }
                                    }
                                }
                            }

                            if (jobs.isNotEmpty()) {
                                item {
                                    Text(StudyQuestStrings.t(uiLanguage, "Recent Studio Jobs", "সাম্প্রতিক জবস"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                items(jobs.take(3), key = { it.id }) { job ->
                                    StudyQuestCard {
                                        Text("${job.sourceTitle} • ${job.status.uppercase()} (${job.progressPercent}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        if (!job.errorMessage.isNullOrBlank()) {
                                            Text(job.errorMessage.orEmpty(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    if (isGuestMode) {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                            item {
                                StudyQuestCard {
                                    Text(StudyQuestStrings.t(uiLanguage, "Sign in with Google to upload and publish your own educational videos with titles & descriptions.", "নিজের শিক্ষামূলক ভিডিও আপলোড করতে গুগল সাইন-ইন করো।"))
                                    Spacer(Modifier.height(10.dp))
                                    Button(onClick = onRequestGoogleSignIn, modifier = Modifier.fillMaxWidth()) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Sign in with Google", "গুগল সাইন-ইন"))
                                    }
                                }
                            }
                        }
                    } else {
                        var uploadTitle by remember { mutableStateOf("") }
                        var uploadDesc by remember { mutableStateOf("") }
                        val availableSubjects = remember(classId) { BangladeshCurriculumCatalog.subjectsForClass(classId) }
                        var uploadSubjectId by remember { mutableStateOf(availableSubjects.firstOrNull()?.id ?: "math") }
                        var uploadClassId by remember { mutableStateOf(classId) }
                        var uploadLang by remember { mutableStateOf(uiLanguage) }
                        var uploadVisibility by remember { mutableStateOf("public") }
                        var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

                        val photoPickerVideo = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.PickVisualMedia()
                        ) { uri -> if (uri != null) selectedVideoUri = uri }

                        val safVideoFallback = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument()
                        ) { uri -> if (uri != null) selectedVideoUri = uri }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                StudyQuestCard {
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, "Upload & Publish Lesson Video", "নিজের ভিডিও আপলোড ও প্রকাশ করুন"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "Pick a video using Android's zero-permission Photo Picker, add a Title, Description, Class, Subject, Language & Visibility.",
                                            "ডিভাইস থেকে ভিডিও নির্বাচন করে শিরোনাম, বিবরণ, শ্রেণি ও বিষয় যোগ করুন।"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                photoPickerVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                            },
                                            modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("pick_video_gallery_btn")
                                        ) {
                                            Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(StudyQuestStrings.t(uiLanguage, "Pick Video", "ভিডিও বাছাই"))
                                        }
                                        OutlinedButton(
                                            onClick = { safVideoFallback.launch(arrayOf("video/*")) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Files", "ফাইল"))
                                        }
                                    }
                                    if (selectedVideoUri != null) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = "Selected: ${selectedVideoUri?.lastPathSegment ?: "video"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = semantic.success
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = uploadTitle,
                                        onValueChange = { uploadTitle = it },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, "Video Title (Required)", "ভিডিওর নাম (আবশ্যক)")) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_video_title_input")
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = uploadDesc,
                                        onValueChange = { uploadDesc = it },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, "Video Description", "ভিডিওর বিবরণ")) },
                                        minLines = 3,
                                        modifier = Modifier.fillMaxWidth().testTag("upload_video_desc_input")
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Class", "শ্রেণি"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BangladeshCurriculumCatalog.classes.filter { it.futureReadyTag == null }.forEach { c ->
                                            FilterChip(selected = uploadClassId == c.id, onClick = { uploadClassId = c.id }, label = { Text(StudyQuestStrings.t(uiLanguage, c.labelEn, c.labelBn)) })
                                        }
                                    }
                                    Text(StudyQuestStrings.t(uiLanguage, "Subject", "বিষয়"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        availableSubjects.take(5).forEach { s ->
                                            FilterChip(selected = uploadSubjectId == s.id, onClick = { uploadSubjectId = s.id }, label = { Text(StudyQuestStrings.t(uiLanguage, s.titleEn, s.titleBn)) })
                                        }
                                    }
                                    Text(StudyQuestStrings.t(uiLanguage, "Language & Visibility", "ভাষা ও প্রকাশনা"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(selected = uploadLang == AppLanguage.BANGLA, onClick = { uploadLang = AppLanguage.BANGLA }, label = { Text("Bangla") })
                                        FilterChip(selected = uploadLang == AppLanguage.ENGLISH, onClick = { uploadLang = AppLanguage.ENGLISH }, label = { Text("English") })
                                        FilterChip(selected = uploadVisibility == "public", onClick = { uploadVisibility = "public" }, label = { Text("Public") })
                                        FilterChip(selected = uploadVisibility == "private", onClick = { uploadVisibility = "private" }, label = { Text("Private") })
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val uri = selectedVideoUri ?: return@Button
                                            aiVideoVm.uploadCustomVideo(
                                                ownerName = ownerName,
                                                videoUri = uri,
                                                title = uploadTitle,
                                                description = uploadDesc,
                                                classId = uploadClassId,
                                                subjectId = uploadSubjectId,
                                                language = uploadLang,
                                                visibility = uploadVisibility
                                            )
                                        },
                                        enabled = !isGenerating && selectedVideoUri != null && uploadTitle.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("publish_uploaded_video_btn")
                                    ) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Publish Video to Library", "লাইব্রেরিতে প্রকাশ করুন"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingVideo?.let { target ->
        var editTitle by remember(target.id) { mutableStateOf(target.title) }
        var editDesc by remember(target.id) { mutableStateOf(target.description.ifBlank { target.summary }) }
        var editVis by remember(target.id) { mutableStateOf(target.visibility) }

        AlertDialog(
            onDismissRequest = { editingVideo = null },
            title = { Text(StudyQuestStrings.t(uiLanguage, "Edit Video Details", "ভিডিওর তথ্য পরিবর্তন")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Title") }, singleLine = true)
                    OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Description") }, minLines = 2)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = editVis == "public", onClick = { editVis = "public" }, label = { Text("Public") })
                        FilterChip(selected = editVis == "private", onClick = { editVis = "private" }, label = { Text("Private") })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        aiVideoVm.updateVideoDetails(target, editTitle, editDesc, editVis)
                        editingVideo = null
                    },
                    enabled = editTitle.isNotBlank()
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Save", "সংরক্ষণ"))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingVideo = null }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                }
            }
        )
    }
}
