package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TealSecondary
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlashcardsAndVaultScreen(
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    classId: String,
    allQuestions: List<QuestionItem>,
    reducedMotion: Boolean,
    onBack: () -> Unit,
    onAddRevision: (QuestionItem) -> Unit,
    onCompleteFlashcardDeck: (subjectId: String, reviewedCount: Int, masteredCount: Int) -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Flashcards, 1=Formula Vault
    var selectedSubjectId by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    val availableSubjects = remember(classId) {
        BangladeshCurriculumCatalog.subjectsForClass(classId)
    }

    val flashcards = remember(allQuestions, selectedSubjectId) {
        val base = allQuestions.filter { it.type == QuestionType.FLASHCARD }
        if (selectedSubjectId == "all") base else base.filter { it.subjectId == selectedSubjectId }
    }

    var currentIndex by remember(selectedSubjectId, flashcards.size) { mutableIntStateOf(0) }
    var isFlipped by remember(currentIndex, selectedSubjectId) { mutableStateOf(false) }
    var masteredCount by remember(selectedSubjectId) { mutableIntStateOf(0) }
    var reviewedCount by remember(selectedSubjectId) { mutableIntStateOf(0) }
    var deckClaimed by remember(selectedSubjectId) { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = if (reducedMotion) 10 else 360),
        label = "flashcard_flip"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("flashcards_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = StudyQuestStrings.t(uiLanguage, "Flashcards & Formula Vault", "ফ্ল্যাশকার্ড ও সূত্র ভান্ডার"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Filled.Style, contentDescription = null) },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Flashcards", "ফ্ল্যাশকার্ড")) },
                modifier = Modifier.testTag("tab_flashcards")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Filled.Functions, contentDescription = null) },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Formula Vault", "সূত্র ভান্ডার")) },
                modifier = Modifier.testTag("tab_formula_vault")
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedSubjectId == "all",
                        onClick = { selectedSubjectId = "all" },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "All Subjects", "সব বিষয়")) },
                        modifier = Modifier.testTag("vault_subj_all")
                    )
                    availableSubjects.forEach { subj ->
                        FilterChip(
                            selected = selectedSubjectId == subj.id,
                            onClick = { selectedSubjectId = subj.id },
                            label = { Text(StudyQuestStrings.t(uiLanguage, subj.titleEn, subj.titleBn)) },
                            modifier = Modifier.testTag("vault_subj_${subj.id}")
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                if (flashcards.isEmpty()) {
                    item {
                        EmptyStateView(
                            titleEn = "No flashcards in this subject",
                            titleBn = "এই বিষয়ে কোনো ফ্ল্যাশকার্ড নেই",
                            subtitleEn = "Choose All Subjects to review the full NCTB deck.",
                            subtitleBn = "সব কার্ড দেখতে 'সব বিষয়' নির্বাচন করো।",
                            language = uiLanguage
                        )
                    }
                } else if (currentIndex >= flashcards.size) {
                    item {
                        StudyQuestCard(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = TealSecondary, modifier = Modifier.size(48.dp))
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, "Deck Complete!", "ফ্ল্যাশকার্ড ডেক সম্পন্ন!"),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        uiLanguage,
                                        "Mastered $masteredCount of $reviewedCount reviewed cards",
                                        "${StudyQuestStrings.formatDigits(reviewedCount, uiLanguage)}টি কার্ডের মধ্যে ${StudyQuestStrings.formatDigits(masteredCount, uiLanguage)}টি আয়ত্ত হয়েছে"
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = {
                                        if (!deckClaimed) {
                                            deckClaimed = true
                                            val subj = if (selectedSubjectId == "all") flashcards.first().subjectId else selectedSubjectId
                                            onCompleteFlashcardDeck(subj, reviewedCount.coerceAtLeast(1), masteredCount)
                                        }
                                    },
                                    enabled = !deckClaimed,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("claim_flashcard_xp_btn")
                                ) {
                                    Text(
                                        if (deckClaimed) StudyQuestStrings.t(uiLanguage, "XP Claimed", "XP সংগৃহীত")
                                        else StudyQuestStrings.t(uiLanguage, "Save Mastery & Claim XP", "XP সংগ্রহ করুন")
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        currentIndex = 0
                                        masteredCount = 0
                                        reviewedCount = 0
                                        deckClaimed = false
                                    },
                                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Filled.Replay, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Practice Deck Again", "আবার অনুশীলন করো"))
                                }
                            }
                        }
                    }
                } else {
                    val card = flashcards[currentIndex]
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Card ${currentIndex + 1} of ${flashcards.size}",
                                    "কার্ড ${StudyQuestStrings.formatDigits(currentIndex + 1, uiLanguage)} / ${StudyQuestStrings.formatDigits(flashcards.size, uiLanguage)}"
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            StatBadgePill(
                                icon = Icons.Filled.CheckCircle,
                                label = StudyQuestStrings.t(uiLanguage, "$masteredCount Mastered", "${StudyQuestStrings.formatDigits(masteredCount, uiLanguage)} আয়ত্ত"),
                                tint = TealSecondary,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (currentIndex + 1).toFloat() / flashcards.size.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                    }

                    item {
                        val showingBack = rotation > 90f
                        StudyQuestCard(
                            onClick = { isFlipped = !isFlipped },
                            containerColor = if (showingBack) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12f * density
                                }
                                .testTag("flashcard_surface")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 22.dp, horizontal = 8.dp)
                                    .graphicsLayer {
                                        if (showingBack) rotationY = 180f
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (showingBack) {
                                            StudyQuestStrings.t(uiLanguage, "ANSWER & FORMULA", "উত্তর ও সূত্র")
                                        } else {
                                            "${card.subjectId.uppercase(Locale.ROOT)} • ${StudyQuestStrings.t(uiLanguage, "TAP TO FLIP", "উত্তর দেখতে ট্যাপ করো")}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                                Text(
                                    text = if (showingBack) {
                                        StudyQuestStrings.t(questionLanguage, card.explanationEn, card.explanationBn)
                                    } else {
                                        StudyQuestStrings.t(questionLanguage, card.promptEn, card.promptBn)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { isFlipped = !isFlipped },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("flip_flashcard_btn")
                            ) {
                                Icon(Icons.Filled.Flip, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Flip", "উল্টাও"))
                            }
                            FilledTonalButton(
                                onClick = {
                                    onAddRevision(card)
                                    reviewedCount += 1
                                    currentIndex += 1
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("flashcard_still_learning_btn")
                            ) {
                                Text(StudyQuestStrings.t(uiLanguage, "Still Learning", "আবার পড়ব"))
                            }
                            Button(
                                onClick = {
                                    masteredCount += 1
                                    reviewedCount += 1
                                    currentIndex += 1
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("flashcard_got_it_btn")
                            ) {
                                Text(StudyQuestStrings.t(uiLanguage, "Got It", "পেরেছি"))
                            }
                        }
                    }
                }
            } else {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Search formulas or definitions", "সূত্র বা সংজ্ঞা খুঁজুন")) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("formula_vault_search")
                    )
                }

                val filteredLessons = BangladeshCurriculumCatalog.lessons.filter { lesson ->
                    (selectedSubjectId == "all" || lesson.subjectId == selectedSubjectId) &&
                        (lesson.formulas.isNotEmpty() || lesson.definitionsEn.isNotEmpty()) &&
                        (searchQuery.isBlank() ||
                            lesson.titleEn.contains(searchQuery, ignoreCase = true) ||
                            lesson.titleBn.contains(searchQuery, ignoreCase = true) ||
                            lesson.formulas.any { it.contains(searchQuery, ignoreCase = true) })
                }

                if (filteredLessons.isEmpty()) {
                    item {
                        EmptyStateView(
                            titleEn = "No matching formulas",
                            titleBn = "কোনো সূত্র পাওয়া যায়নি",
                            subtitleEn = "Clear your search or choose another subject.",
                            subtitleBn = "সার্চ মুছে আবার চেষ্টা করো।",
                            language = uiLanguage
                        )
                    }
                } else {
                    items(filteredLessons, key = { it.id }) { lesson ->
                        StudyQuestCard {
                            Text(
                                text = "${lesson.subjectId.uppercase(Locale.ROOT)} • ${StudyQuestStrings.t(uiLanguage, lesson.titleEn, lesson.titleBn)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CobaltPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            lesson.formulas.forEach { formula ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = formula,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PomodoroFocusScreen(
    uiLanguage: AppLanguage,
    classId: String,
    onBack: () -> Unit,
    onCompleteFocusSession: (subjectId: String, focusedMinutes: Int) -> Unit
) {
    BackHandler { onBack() }

    val availableSubjects = remember(classId) {
        BangladeshCurriculumCatalog.subjectsForClass(classId)
    }
    var selectedSubjectId by remember { mutableStateOf(availableSubjects.firstOrNull()?.id ?: "math") }
    var durationMinutes by remember { mutableIntStateOf(25) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var sessionClaimed by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isRunning = false
            }
        }
    }

    val totalSeconds = durationMinutes * 60
    val elapsedSeconds = (totalSeconds - remainingSeconds).coerceAtLeast(0)
    val elapsedMinutes = elapsedSeconds / 60
    val canClaim = elapsedSeconds >= 60 && !sessionClaimed
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    val mm = remainingSeconds / 60
    val ss = remainingSeconds % 60
    val timeText = String.format(Locale.US, "%02d:%02d", mm, ss)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("pomodoro_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Pomodoro Focus Timer", "পোমোডোরো ফোকাস টাইমার"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Stay focused & earn +2 XP per minute", "মনোযোগ দিয়ে পড়ো ও প্রতি মিনিটে +২ XP পাও"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            StudyQuestCard {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Choose Focus Subject", "কোন বিষয় পড়বে?"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    availableSubjects.take(6).forEach { subj ->
                        FilterChip(
                            selected = selectedSubjectId == subj.id,
                            onClick = { if (!isRunning) selectedSubjectId = subj.id },
                            label = { Text(StudyQuestStrings.t(uiLanguage, subj.titleEn, subj.titleBn)) },
                            modifier = Modifier.testTag("pomo_subj_${subj.id}")
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Session Length", "সময়কাল"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 25, 45).forEach { mins ->
                        FilterChip(
                            selected = durationMinutes == mins,
                            onClick = {
                                if (!isRunning) {
                                    durationMinutes = mins
                                    remainingSeconds = mins * 60
                                    sessionClaimed = false
                                }
                            },
                            label = { Text(StudyQuestStrings.t(uiLanguage, "$mins min", "${StudyQuestStrings.formatDigits(mins, uiLanguage)} মি.")) },
                            modifier = Modifier.testTag("pomo_dur_$mins")
                        )
                    }
                }
            }
        }

        item {
            StudyQuestCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    val activeColor = if (remainingSeconds == 0) TealSecondary else CobaltPrimary
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(210.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 14.dp.toPx()
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = activeColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = StudyQuestStrings.formatDigits(timeText, uiLanguage),
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isRunning) {
                                    StudyQuestStrings.t(uiLanguage, "Deep Focus Active", "মনোযোগ চলমান")
                                } else if (remainingSeconds == 0) {
                                    StudyQuestStrings.t(uiLanguage, "Session Complete!", "সেশন সম্পন্ন!")
                                } else {
                                    StudyQuestStrings.t(uiLanguage, "Ready to Focus", "শুরু করতে প্রস্তুত")
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = CobaltPrimary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { isRunning = !isRunning },
                            enabled = remainingSeconds > 0,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("pomodoro_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isRunning) StudyQuestStrings.t(uiLanguage, "Pause", "বিরতি")
                                else StudyQuestStrings.t(uiLanguage, "Start Focus", "শুরু করো")
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                isRunning = false
                                remainingSeconds = durationMinutes * 60
                                sessionClaimed = false
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("pomodoro_reset_btn")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(uiLanguage, "Reset", "রিসেট"))
                        }
                    }

                    Button(
                        onClick = {
                            isRunning = false
                            sessionClaimed = true
                            onCompleteFocusSession(selectedSubjectId, elapsedMinutes.coerceAtLeast(1))
                        },
                        enabled = canClaim,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("pomodoro_claim_btn")
                    ) {
                        Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            when {
                                sessionClaimed -> StudyQuestStrings.t(uiLanguage, "Session XP Saved!", "XP সংরক্ষিত হয়েছে")
                                canClaim -> StudyQuestStrings.t(
                                    uiLanguage,
                                    "Complete & Claim +${(elapsedMinutes.coerceAtLeast(1) * 2).coerceIn(10, 80)} XP",
                                    "সম্পন্ন ও XP সংগ্রহ করুন"
                                )
                                else -> StudyQuestStrings.t(
                                    uiLanguage,
                                    "Focus at least 1 minute to claim XP",
                                    "XP পেতে অন্তত ১ মিনিট ফোকাস করো"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
