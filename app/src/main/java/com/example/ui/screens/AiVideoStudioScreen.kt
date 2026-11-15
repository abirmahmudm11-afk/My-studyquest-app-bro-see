package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.SkyCyanAccent
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.VioletTertiary
import com.example.ui.viewmodel.AiVideoViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiVideoStudioScreen(
    uiLanguage: AppLanguage,
    classId: String,
    aiVideoVm: AiVideoViewModel,
    isOnline: Boolean,
    externalVideoRendererConfigured: Boolean,
    isGuestMode: Boolean,
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
    val videos by aiVideoVm.videos.collectAsStateWithLifecycle()
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

    var inputMode by remember { mutableStateOf("chapter") } // "chapter" or "document"
    val availableSubjects = remember(classId) { BangladeshCurriculumCatalog.subjectsForClass(classId) }
    var selectedSubjectId by remember { mutableStateOf(availableSubjects.firstOrNull()?.id ?: "math") }
    val chaptersForSubject = remember(selectedSubjectId) {
        BangladeshCurriculumCatalog.chapters.filter { it.subjectId == selectedSubjectId }
    }
    var selectedChapterId by remember(selectedSubjectId) {
        mutableStateOf(chaptersForSubject.firstOrNull()?.id ?: "")
    }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pastedNotes by remember { mutableStateOf("") }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pickedUri = uri
    }

    if (isGuestMode) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    Text(StudyQuestStrings.t(uiLanguage, "AI Storyboard Studio", "এআই ভিডিও স্টুডিও"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            item {
                StudyQuestCard {
                    Text(
                        text = StudyQuestStrings.t(
                            uiLanguage,
                            "Sign in with Google to generate interactive Gemini AI Storyboard Lessons with voiceover & check quizzes from NCTB chapters or PDF notes.",
                            "অধ্যায় বা PDF নোট থেকে ভয়েসওভার ও কুইজসহ এআই স্টোরিবোর্ড লেসন তৈরি করতে গুগল অ্যাকাউন্ট দিয়ে সাইন-ইন করো।"
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (activeVideo != null) aiVideoVm.closeVideo() else onBack() },
                    modifier = Modifier.testTag("ai_video_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "AI Learning Storyboard Studio", "এআই স্টোরিবোর্ড ভিডিও স্টুডিও"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Gemini 3.8 + TTS Narration + Post-Lesson Quiz", "জেমিনি এআই + ভয়েসওভার + কুইজ"),
                        style = MaterialTheme.typography.labelSmall,
                        color = CobaltPrimary
                    )
                }
            }
        }

        if (!message.isNullOrBlank()) {
            item {
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(message.orEmpty(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { aiVideoVm.clearMessage() }) { Text("OK") }
                    }
                }
            }
        }

        val currentVid = activeVideo
        if (currentVid != null && scenes.isNotEmpty()) {
            val safeIdx = sceneIdx.coerceIn(0, scenes.lastIndex)
            val currentScene = scenes[safeIdx]

            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().testTag("storyboard_player_stage")
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(CobaltDark, CobaltPrimary, VioletTertiary.copy(alpha = 0.85f)),
                                    start = Offset.Zero,
                                    end = Offset(size.width, size.height)
                                )
                            )
                            drawCircle(
                                color = SkyCyanAccent.copy(alpha = 0.14f),
                                radius = size.minDimension * 0.45f,
                                center = Offset(size.width * 0.88f, size.height * 0.2f)
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
                                    Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.2f)) {
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
                                            color = Color.Black.copy(alpha = 0.28f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = sc.formulaOrDiagram,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFBAE6FD),
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
                                    color = SkyCyanAccent,
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
                            Text(if (isPlaying) StudyQuestStrings.t(uiLanguage, "Pause", "বিরতি") else StudyQuestStrings.t(uiLanguage, "Play Storyboard", "চালু করো"))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(StudyQuestStrings.t(uiLanguage, "Voiceover", "ভয়েস"), style = MaterialTheme.typography.labelMedium)
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
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (externalVideoRendererConfigured) {
                                    StudyQuestStrings.t(uiLanguage, "Cloud MP4 render job queued.", "ক্লাউড MP4 রেন্ডার কিউতে রয়েছে।")
                                } else {
                                    StudyQuestStrings.t(
                                        uiLanguage,
                                        "Cloud MP4 Export: Unconfigured (no external video rendering backend). Interactive native Storyboard + TTS plays directly on device.",
                                        "ক্লাউড MP4 এক্সপোর্ট: এখনো কনফিগার করা হয়নি। ডিভাইসেই ভয়েসসহ ইন্টার‍্যাক্টিভ স্টোরিবোর্ড দেখানো হচ্ছে।"
                                    )
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                            text = StudyQuestStrings.t(uiLanguage, "Post-Lesson Comprehension Quiz", "ভিডিও পরবর্তী মূল্যায়ন কুইজ"),
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
                                            color = when {
                                                quizSubmitted && isRight -> semantic.success
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (quizSubmitted && qq.explanation.isNotBlank()) {
                                Text(qq.explanation, style = MaterialTheme.typography.labelSmall, color = TealSecondary)
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        Button(
                            onClick = {
                                quizSubmitted = true
                                val correct = quizQuestions.indices.count { i -> pickedAnswers[i] == quizQuestions[i].correctIndex }
                                aiVideoVm.submitVideoCheckQuiz(currentVid, correct, quizQuestions.size)
                            },
                            enabled = !quizSubmitted && pickedAnswers.size == quizQuestions.size,
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("submit_storyboard_quiz_btn")
                        ) {
                            Text(
                                if (quizSubmitted) StudyQuestStrings.t(uiLanguage, "Quiz Verified!", "কুইজ যাচাই সম্পন্ন")
                                else StudyQuestStrings.t(uiLanguage, "Submit Quiz & Claim XP", "কুইজ জমা ও XP সংগ্রহ")
                            )
                        }
                    }
                }
            }
        } else {
            // Generator & Library View
            item {
                StudyQuestCard {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Generate New Storyboard Lesson", "নতুন স্টোরিবোর্ড তৈরি করো"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
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
                    Spacer(Modifier.height(8.dp))
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
                        Spacer(Modifier.height(8.dp))
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
                                aiVideoVm.generateFromChapter(classId, selectedSubjectId, selectedChapterId, uiLanguage)
                            },
                            enabled = isOnline && !isGenerating && selectedChapterId.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("generate_chapter_storyboard_btn")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Generating Storyboard...", "স্টোরিবোর্ড তৈরি হচ্ছে..."))
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Generate AI Storyboard", "এআই স্টোরিবোর্ড তৈরি করো"))
                            }
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { documentPicker.launch(arrayOf("application/pdf", "text/plain")) },
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("pick_pdf_or_note_btn")
                        ) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                pickedUri?.lastPathSegment ?: StudyQuestStrings.t(uiLanguage, "Pick PDF or Text File (SAF, Max 20MB)", "PDF বা টেক্সট ফাইল নির্বাচন করো")
                            )
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
                                aiVideoVm.generateFromDocumentOrNotes(classId, selectedSubjectId, pickedUri, pastedNotes, uiLanguage)
                            },
                            enabled = isOnline && !isGenerating && (pickedUri != null || pastedNotes.isNotBlank()),
                            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("generate_doc_storyboard_btn")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Analyzing & Generating...", "বিশ্লেষণ হচ্ছে..."))
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Generate Storyboard from Upload", "ফাইল থেকে স্টোরিবোর্ড তৈরি"))
                            }
                        }
                    }
                }
            }

            if (jobs.isNotEmpty()) {
                item {
                    Text(StudyQuestStrings.t(uiLanguage, "Recent Generation Jobs", "সাম্প্রতিক জবস"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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

            item {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "My AI Storyboard Library (${videos.size})", "আমার এআই ভিডিও লাইব্রেরি"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (videos.isEmpty()) {
                item {
                    EmptyStateView(
                        titleEn = "No AI storyboards yet",
                        titleBn = "এখনো কোনো এআই স্টোরিবোর্ড নেই",
                        subtitleEn = "Generate one from any NCTB chapter or upload a PDF study sheet above.",
                        subtitleBn = "উপরে যেকোনো অধ্যায় বা PDF থেকে তোমার প্রথম এআই স্টোরিবোর্ড তৈরি করো।",
                        language = uiLanguage
                    )
                }
            } else {
                items(videos, key = { it.id }) { vid ->
                    StudyQuestCard(onClick = { aiVideoVm.openVideo(vid, uiLanguage) }, modifier = Modifier.testTag("open_video_${vid.id}")) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = CobaltPrimary, modifier = Modifier.size(28.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(vid.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${vid.subjectId.uppercase()} • ${vid.durationSeconds}s • ${vid.summary.take(70)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
}
