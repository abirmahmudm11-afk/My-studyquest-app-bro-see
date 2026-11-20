package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AmbientMusicPlayer
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.FormulaAtlasCatalog
import com.example.data.curriculum.FormulaEntry
import com.example.data.local.ExamCountdownEntity
import com.example.data.local.FormulaBookmarkEntity
import com.example.data.local.LocalSoloMatchEntity
import com.example.data.local.MistakeNoteEntity
import com.example.data.model.LearningRecordDoc
import com.example.data.model.QuestionItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FormulaAtlasScreen(
    uiLanguage: AppLanguage,
    bookmarks: List<FormulaBookmarkEntity>,
    onToggleBookmark: (FormulaEntry) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("all") }
    var savedOnly by remember { mutableStateOf(false) }

    val savedIds = remember(bookmarks) { bookmarks.map { it.formulaId }.toSet() }
    val filtered = remember(searchQuery, selectedSubject, savedOnly, savedIds) {
        val q = searchQuery.trim().lowercase(Locale.ROOT)
        FormulaAtlasCatalog.entries.filter { e ->
            val subjOk = selectedSubject == "all" || e.subjectId == selectedSubject
            val savedOk = !savedOnly || e.id in savedIds
            val qOk = q.isBlank() ||
                e.titleEn.lowercase(Locale.ROOT).contains(q) ||
                e.titleBn.contains(searchQuery.trim()) ||
                e.expression.lowercase(Locale.ROOT).contains(q)
            subjOk && savedOk && qOk
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Formula & Constants Atlas", "সূত্র ও ধ্রুবক অ্যাটলাস"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Offline NCTB Classes 6–10 Quick Reference", "অফলাইন পকেট সূত্র নির্দেশিকা"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("formula_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxSize()
                    .testTag("formula_atlas_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        placeholder = {
                            Text(StudyQuestStrings.t(uiLanguage, "Search formula, law, or constant...", "সূত্র বা ধ্রুবক খুঁজুন..."))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("formula_search_input")
                    )
                }

                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val chips = listOf(
                            "all" to StudyQuestStrings.t(uiLanguage, "All", "সব"),
                            "math" to StudyQuestStrings.t(uiLanguage, "Math", "গণিত"),
                            "physics" to StudyQuestStrings.t(uiLanguage, "Physics", "পদার্থ"),
                            "chemistry" to StudyQuestStrings.t(uiLanguage, "Chemistry", "রসায়ন"),
                            "biology" to StudyQuestStrings.t(uiLanguage, "Biology", "জীববিজ্ঞান")
                        )
                        chips.forEach { (id, label) ->
                            FilterChip(
                                selected = selectedSubject == id,
                                onClick = { selectedSubject = id },
                                label = { Text(label) },
                                modifier = Modifier.testTag("formula_subj_$id")
                            )
                        }
                        FilterChip(
                            selected = savedOnly,
                            onClick = { savedOnly = !savedOnly },
                            label = { Text(StudyQuestStrings.t(uiLanguage, "Saved (${savedIds.size})", "সংরক্ষিত (${savedIds.size})")) },
                            modifier = Modifier.testTag("formula_saved_filter")
                        )
                    }
                }

                items(filtered, key = { it.id }) { entry ->
                    val isSaved = entry.id in savedIds
                    StudyQuestCard(modifier = Modifier.testTag("formula_card_${entry.id}")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${entry.subjectId.uppercase()} • ${StudyQuestStrings.t(uiLanguage, entry.categoryEn, entry.categoryBn)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, entry.titleEn, entry.titleBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { onToggleBookmark(entry) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("formula_bookmark_${entry.id}")
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = StudyQuestStrings.t(uiLanguage, "Bookmark formula", "সূত্র সংরক্ষণ"),
                                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = entry.expression,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, entry.conditionEn, entry.conditionBn),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Tip: ${entry.tipEn}", "টিপস: ${entry.tipBn}"),
                            style = MaterialTheme.typography.labelSmall,
                            color = EditorialSuccess
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeNotebookScreen(
    uiLanguage: AppLanguage,
    mistakes: List<MistakeNoteEntity>,
    allQuestions: List<QuestionItem>,
    onStartRepairDrill: (List<QuestionItem>) -> Unit,
    onResolveMistake: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val repairQuestions = remember(mistakes, allQuestions) {
        val ids = mistakes.map { it.questionId }.toSet()
        allQuestions.filter { it.id in ids }.take(5)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Mistake Notebook", "ভুলের খাতা ও সংশোধন"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "${mistakes.size} unresolved items • Works offline",
                                "${StudyQuestStrings.formatDigits(mistakes.size, uiLanguage)}টি অসমাধিত প্রশ্ন • অফলাইন প্রস্তুত"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("mistake_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxSize()
                    .testTag("mistake_notebook_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StudyQuestCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Turn Past Mistakes Into Mastery",
                                    "ভুল প্রশ্ন থেকে পুনরায় দক্ষতা গড়ো"
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Missed quiz or mock exam questions are automatically collected here so you can drill and clear them offline.",
                                    "অনুশীলন ও পরীক্ষার ভুল উত্তরগুলো স্বয়ংক্রিয়ভাবে এখানে জমা হয়।"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { if (repairQuestions.isNotEmpty()) onStartRepairDrill(repairQuestions) },
                                enabled = repairQuestions.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("mistake_repair_btn")
                            ) {
                                Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    StudyQuestStrings.t(
                                        uiLanguage,
                                        "Start ${repairQuestions.size}-Question Mistake Repair",
                                        "${StudyQuestStrings.formatDigits(repairQuestions.size, uiLanguage)} প্রশ্নের রিপিট অনুশীলন শুরু"
                                    )
                                )
                            }
                        }
                    }
                }

                if (mistakes.isEmpty()) {
                    item {
                        EmptyStateView(
                            titleEn = "No pending mistakes",
                            titleBn = "কোনো বকেয়া ভুল নেই",
                            subtitleEn = "Great job! Any missed questions from practice or mock exams will appear here for targeted repair.",
                            subtitleBn = "চমৎকার! পরবর্তীতে কোনো ভুল হলে এখানে যুক্ত হবে।",
                            language = uiLanguage
                        )
                    }
                } else {
                    items(mistakes, key = { it.questionId }) { item ->
                        StudyQuestCard(modifier = Modifier.testTag("mistake_card_${item.questionId}")) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.subjectId.uppercase()} • Missed ${item.wrongCount}x",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                TextButton(
                                    onClick = { onResolveMistake(item.questionId) },
                                    modifier = Modifier.testTag("mistake_resolve_${item.questionId}")
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Mastered", "আয়ত্ত হয়েছে"))
                                }
                            }
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, item.promptEn, item.promptBn),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCountdownAndHeatmapScreen(
    uiLanguage: AppLanguage,
    countdowns: List<ExamCountdownEntity>,
    learningRecords: List<LearningRecordDoc>,
    soloMatches: List<LocalSoloMatchEntity>,
    onAddExam: (title: String, subjectId: String, daysFromNow: Int, dailyGoal: Int) -> Unit,
    onIncrementTopic: (String) -> Unit,
    onDeleteExam: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var daysInput by remember { mutableIntStateOf(30) }

    val activityByDay = remember(learningRecords, soloMatches) {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val counts = mutableMapOf<String, Int>()
        learningRecords.forEach { rec ->
            val ms = rec.createdAt?.toDate()?.time ?: System.currentTimeMillis()
            val key = fmt.format(Date(ms))
            counts[key] = (counts[key] ?: 0) + 1
        }
        soloMatches.forEach { m ->
            val key = fmt.format(Date(m.completedAtMs))
            counts[key] = (counts[key] ?: 0) + 1
        }
        counts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Exam Countdown & Streak Heatmap", "পরীক্ষা কাউন্টডাউন ও ধারাবাহিকতা"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("exam_countdown_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("exam_add_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = StudyQuestStrings.t(uiLanguage, "Add exam", "পরীক্ষা যোগ"))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxSize()
                    .testTag("exam_countdown_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 70-Day Study Heatmap
                item {
                    StudyQuestCard(modifier = Modifier.testTag("study_heatmap_card")) {
                        val fmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
                        val todayMs = System.currentTimeMillis()
                        val activeDaysCount = (0 until 70).count { offset ->
                            val key = fmt.format(Date(todayMs - offset * 86_400_000L))
                            (activityByDay[key] ?: 0) > 0
                        }
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "70-Day Study Consistency Heatmap", "৭০ দিনের পড়াশোনার হিটম্যাপ"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "$activeDaysCount active days in last 10 weeks (works offline)",
                                "গত ১০ সপ্তাহে ${StudyQuestStrings.formatDigits(activeDaysCount, uiLanguage)} দিন সক্রিয়"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (week in 9 downTo 0) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (day in 6 downTo 0) {
                                        val offset = week * 7 + day
                                        val key = fmt.format(Date(todayMs - offset * 86_400_000L))
                                        val count = activityByDay[key] ?: 0
                                        val cellColor = when {
                                            count >= 4 -> EditorialSuccess
                                            count >= 2 -> EditorialSuccess.copy(alpha = 0.65f)
                                            count == 1 -> EditorialSuccess.copy(alpha = 0.35f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(cellColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Upcoming Exam Targets", "আসন্ন পরীক্ষার লক্ষ্য"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(uiLanguage, "New Target", "নতুন লক্ষ্য"))
                        }
                    }
                }

                if (countdowns.isEmpty()) {
                    item {
                        EmptyStateView(
                            titleEn = "No exam countdown set",
                            titleBn = "কোনো পরীক্ষা সেট করা নেই",
                            subtitleEn = "Add your SSC Board Exam or school class test date to track daily topic pacing offline.",
                            subtitleBn = "এসএসসি বা শ্রেণি পরীক্ষার তারিখ যোগ করে দৈনিক প্রস্তুতি হিসাব রাখুন।",
                            language = uiLanguage
                        )
                    }
                } else {
                    items(countdowns, key = { it.id }) { exam ->
                        val daysLeft = TimeUnit.MILLISECONDS.toDays(
                            (exam.targetDateEpochMs - System.currentTimeMillis()).coerceAtLeast(0L)
                        ).toInt()
                        StudyQuestCard(modifier = Modifier.testTag("exam_card_${exam.id}")) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exam.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "$daysLeft days left • ${exam.completedTopics} topics done • Goal ${exam.dailyTopicGoal}/day",
                                            "${StudyQuestStrings.formatDigits(daysLeft, uiLanguage)} দিন বাকি • ${StudyQuestStrings.formatDigits(exam.completedTopics, uiLanguage)} অধ্যায় সম্পন্ন"
                                        ),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilledTonalButton(onClick = { onIncrementTopic(exam.id) }) {
                                        Text("+1")
                                    }
                                    IconButton(onClick = { onDeleteExam(exam.id) }) {
                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(StudyQuestStrings.t(uiLanguage, "Add Exam Countdown", "পরীক্ষার লক্ষ্য যোগ")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Exam title (e.g. SSC Model Test)", "পরীক্ষার নাম")) },
                        singleLine = true,
                        modifier = Modifier.testTag("exam_dialog_title_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(14, 30, 60, 90).forEach { d ->
                            FilterChip(
                                selected = daysInput == d,
                                onClick = { daysInput = d },
                                label = { Text("${d}d") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddExam(titleInput.ifBlank { "SSC Preparation Target" }, "math", daysInput, 2)
                        titleInput = ""
                        showAddDialog = false
                    },
                    modifier = Modifier.testTag("exam_dialog_save_btn")
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Save", "সংরক্ষণ"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketAudioRevisionScreen(
    uiLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val player = remember(context) { AmbientMusicPlayer(context) }
    val audioState by player.state.collectAsStateWithLifecycle()

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    val subjects = remember {
        listOf(
            "math" to ("Mathematics Audio Revision" to "গণিত অডিও ব্রিফিং"),
            "physics" to ("Physics Laws Audio Guide" to "পদার্থবিজ্ঞান অডিও গাইড"),
            "chemistry" to ("Chemistry Reactions Audio" to "রসায়ন অডিও ব্রিফিং"),
            "biology" to ("Biology Concepts Audio" to "জীববিজ্ঞান অডিও গাইড"),
            "ict" to ("ICT & Logic Audio Guide" to "আইসিটি অডিও গাইড"),
            "english" to ("English Grammar Audio" to "ইংরেজি গ্রামার অডিও"),
            "bangla" to ("Bangla Sahitya Audio" to "বাংলা সাহিত্য অডিও")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Pocket Audio Briefings", "পকেট অডিও ব্রিফিং"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "44.1 kHz Studio Offline Audio", "৪৪.১ kHz স্টুডিও অফলাইন অডিও"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pocket_audio_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxSize()
                    .testTag("pocket_audio_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    StudyQuestCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, "Monsoon Rain Ambient Layer", "বৃষ্টির অ্যাম্বিয়েন্স লেয়ার"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, "Mix gentle rainfall behind chapter briefings", "অডিওর সাথে বৃষ্টির শব্দ মিশ্রণ করুন"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = audioState.rainLayerEnabled,
                                onCheckedChange = { player.toggleAmbientRainLayer() },
                                modifier = Modifier.testTag("pocket_audio_rain_switch")
                            )
                        }
                    }
                }

                items(subjects, key = { it.first }) { (subjId, titles) ->
                    val playing = audioState.activeBriefingSubjectId == subjId
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (playing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Headphones, contentDescription = null)
                                Column {
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, titles.first, titles.second),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${subjId.uppercase()} • 44.1 kHz Studio WAV",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = {
                                    if (playing) player.stopSubjectBriefing()
                                    else player.playSubjectBriefing(subjId)
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("briefing_play_$subjId")
                            ) {
                                Icon(
                                    imageVector = if (playing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (playing) StudyQuestStrings.t(uiLanguage, "Stop", "থামাও")
                                    else StudyQuestStrings.t(uiLanguage, "Listen", "শুনুন")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
