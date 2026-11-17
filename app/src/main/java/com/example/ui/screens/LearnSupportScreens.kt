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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.local.LocalBookmarkEntity
import com.example.data.local.LocalRevisionEntity
import com.example.data.model.ExamTemplate
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.QuestCoachMood
import com.example.data.model.StudyPlanDoc
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LottieSuccessCelebrationCard
import com.example.ui.components.QuestCoachCard
import com.example.ui.components.QuestionPracticeCard
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.RoseAlert
import com.example.ui.viewmodel.AiDoubtUiState
import com.example.ui.viewmodel.AssessmentResultSummary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionBankScreen(
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    allQuestions: List<QuestionItem>,
    bookmarks: List<LocalBookmarkEntity>,
    onBack: () -> Unit,
    onToggleBookmark: (QuestionItem) -> Unit,
    onAddRevision: (QuestionItem) -> Unit,
    onReportQuestion: (QuestionItem) -> Unit,
    onAskAi: (QuestionItem) -> Unit,
    onStartFilteredPractice: (String, List<QuestionItem>) -> Unit
) {
    BackHandler(onBack = onBack)

    var searchQuery by remember { mutableStateOf("") }
    var filterSubjectId by remember { mutableStateOf<String?>(null) }
    var filterDifficulty by remember { mutableStateOf<QuestionDifficulty?>(null) }
    var filterType by remember { mutableStateOf<QuestionType?>(null) }
    var bookmarkedOnly by remember { mutableStateOf(false) }

    val selectedOptions = remember { mutableStateMapOf<String, Set<Int>>() }
    val textAnswers = remember { mutableStateMapOf<String, String>() }
    val cqScores = remember { mutableStateMapOf<String, Double>() }
    val checkedQuestions = remember { mutableStateMapOf<String, Boolean>() }

    val filtered = remember(allQuestions, searchQuery, filterSubjectId, filterDifficulty, filterType, bookmarkedOnly, bookmarks) {
        allQuestions.filter { q ->
            val matchQuery = searchQuery.isBlank() ||
                q.promptEn.contains(searchQuery, true) ||
                q.promptBn.contains(searchQuery, true) ||
                q.cqStemEn.contains(searchQuery, true) ||
                q.cqStemBn.contains(searchQuery, true) ||
                q.board.contains(searchQuery, true)
            val matchSubj = filterSubjectId == null || q.subjectId == filterSubjectId
            val matchDiff = filterDifficulty == null || q.difficulty == filterDifficulty
            val matchType = filterType == null || q.type == filterType
            val matchBookmark = !bookmarkedOnly || bookmarks.any { it.questionId == q.id }
            matchQuery && matchSubj && matchDiff && matchType && matchBookmark
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Question Bank (${filtered.size})", "প্রশ্নব্যাংক (${StudyQuestStrings.formatDigits(filtered.size, uiLanguage)})"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                label = { Text(StudyQuestStrings.t(uiLanguage, "Search topic, keyword, board...", "বিষয় বা প্রশ্ন খুঁজুন...")) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qbank_search")
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = bookmarkedOnly,
                        onClick = { bookmarkedOnly = !bookmarkedOnly },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Bookmarks", "বুকমার্ক")) }
                    )
                }
                items(BangladeshCurriculumCatalog.subjects, key = { it.id }) { subj ->
                    FilterChip(
                        selected = filterSubjectId == subj.id,
                        onClick = { filterSubjectId = if (filterSubjectId == subj.id) null else subj.id },
                        label = { Text(StudyQuestStrings.t(uiLanguage, subj.titleEn, subj.titleBn)) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(QuestionDifficulty.entries, key = { it.id }) { diff ->
                    FilterChip(
                        selected = filterDifficulty == diff,
                        onClick = { filterDifficulty = if (filterDifficulty == diff) null else diff },
                        label = { Text(StudyQuestStrings.t(uiLanguage, diff.labelEn, diff.labelBn)) }
                    )
                }
                items(QuestionType.entries, key = { it.id }) { qType ->
                    FilterChip(
                        selected = filterType == qType,
                        onClick = { filterType = if (filterType == qType) null else qType },
                        label = { Text(StudyQuestStrings.t(uiLanguage, qType.labelEn, qType.labelBn)) }
                    )
                }
            }
            if (filtered.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        onStartFilteredPractice(
                            StudyQuestStrings.t(uiLanguage, "Filtered Practice", "বাছাইকৃত অনুশীলন"),
                            filtered.take(10)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(StudyQuestStrings.t(uiLanguage, "Practice These ${filtered.take(10).size} Questions", "এই প্রশ্নগুলো অনুশীলন করো"))
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateView(
                    titleEn = "No matching questions",
                    titleBn = "কোনো প্রশ্ন পাওয়া যায়নি",
                    subtitleEn = "Try clearing one of the filters or search keywords.",
                    subtitleBn = "ফিল্টার পরিবর্তন করে আবার চেষ্টা করো।",
                    language = uiLanguage
                )
            }
        } else {
            items(filtered, key = { it.id }) { q ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuestionPracticeCard(
                        question = q,
                        questionLanguage = questionLanguage,
                        uiLanguage = uiLanguage,
                        selectedIndices = selectedOptions[q.id] ?: emptySet(),
                        textAnswer = textAnswers[q.id] ?: "",
                        cqSelfScore = cqScores[q.id],
                        isBookmarked = bookmarks.any { it.questionId == q.id },
                        showFeedback = checkedQuestions[q.id] == true,
                        onToggleOption = { idx ->
                            val current = selectedOptions[q.id] ?: emptySet()
                            selectedOptions[q.id] = if (q.type == QuestionType.MULTI_MCQ) {
                                if (idx in current) current - idx else current + idx
                            } else {
                                setOf(idx)
                            }
                            checkedQuestions[q.id] = true
                        },
                        onTextAnswerChange = { textAnswers[q.id] = it },
                        onCqSelfScoreChange = { cqScores[q.id] = it },
                        onToggleBookmark = { onToggleBookmark(q) },
                        onAddRevision = { onAddRevision(q) },
                        onReportQuestion = { onReportQuestion(q) },
                        onAskAi = { onAskAi(q) }
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeAndExamRunnerScreen(
    title: String,
    questions: List<QuestionItem>,
    template: ExamTemplate?,
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    bookmarks: List<LocalBookmarkEntity>,
    examRemainingSeconds: Int,
    assessmentResult: AssessmentResultSummary?,
    reducedMotion: Boolean = false,
    onBack: () -> Unit,
    onStartExamTimer: (Int, () -> Unit) -> Unit,
    onToggleBookmark: (QuestionItem) -> Unit,
    onAddRevision: (QuestionItem) -> Unit,
    onReportQuestion: (QuestionItem) -> Unit,
    onAskAi: (QuestionItem) -> Unit,
    onSubmit: (
        selected: Map<String, Set<Int>>,
        textAnswers: Map<String, String>,
        cqSelfScores: Map<String, Double>
    ) -> Unit
) {
    BackHandler(onBack = onBack)

    var currentIdx by remember { mutableStateOf(0) }
    val selectedMap = remember { mutableStateMapOf<String, Set<Int>>() }
    val textMap = remember { mutableStateMapOf<String, String>() }
    val cqMap = remember { mutableStateMapOf<String, Double>() }
    var showAnswerImmediately by remember { mutableStateOf(template == null) }

    LaunchedEffect(template?.id) {
        if (template != null && assessmentResult == null) {
            onStartExamTimer(template.durationSeconds) {
                onSubmit(selectedMap.toMap(), textMap.toMap(), cqMap.toMap())
            }
        }
    }

    if (assessmentResult != null) {
        val accuracyPct = if (questions.isEmpty()) 0 else ((assessmentResult.correctCount * 100f) / questions.size).toInt().coerceIn(0, 100)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LottieSuccessCelebrationCard(
                    title = if (template != null) {
                        StudyQuestStrings.t(uiLanguage, "Mock Exam Completed!", "মডেল টেস্ট সম্পন্ন!")
                    } else {
                        StudyQuestStrings.t(uiLanguage, "Practice Session Complete!", "অনুশীলন সম্পন্ন!")
                    },
                    subtitle = StudyQuestStrings.t(
                        uiLanguage,
                        "${assessmentResult.title} • $accuracyPct% accuracy",
                        "${assessmentResult.title} • ${StudyQuestStrings.formatDigits(accuracyPct, uiLanguage)}% সঠিকতা"
                    ),
                    xpAwardedText = if (assessmentResult.xpEarned > 0) "+${StudyQuestStrings.formatDigits(assessmentResult.xpEarned, uiLanguage)} XP" else null,
                    reducedMotion = reducedMotion
                )
            }
            item {
                QuestCoachCard(
                    mood = QuestCoachMood.SUCCESS,
                    messageEn = "Awesome effort! You earned +${assessmentResult.xpEarned} XP on ${assessmentResult.title}.",
                    messageBn = "দারুণ প্রচেষ্টা! ${assessmentResult.title}-এ তুমি +${StudyQuestStrings.formatDigits(assessmentResult.xpEarned, uiLanguage)} XP অর্জন করেছ।",
                    language = uiLanguage,
                    reducedMotion = reducedMotion,
                    showCelebrationLottie = false
                )
            }
            item {
                StudyQuestCard {
                    Text(assessmentResult.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = StudyQuestStrings.t(
                            uiLanguage,
                            "Score: ${String.format("%.2f", assessmentResult.score)} / ${String.format("%.1f", assessmentResult.totalMarks)}",
                            "প্রাপ্ত নম্বর: ${String.format("%.2f", assessmentResult.score)} / ${String.format("%.1f", assessmentResult.totalMarks)}"
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = CobaltPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("✓ ${StudyQuestStrings.t(uiLanguage, "Correct", "সঠিক")}: ${assessmentResult.correctCount}")
                    Text("✗ ${StudyQuestStrings.t(uiLanguage, "Wrong", "ভুল")}: ${assessmentResult.wrongCount}")
                    Text("• ${StudyQuestStrings.t(uiLanguage, "Skipped", "বাদ দেওয়া")}: ${assessmentResult.skippedCount}")
                    if (assessmentResult.negativeDeduction > 0.0) {
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "Negative marking deducted: -${String.format("%.2f", assessmentResult.negativeDeduction)}",
                                "নেগেটিভ মার্কিং কর্তন: -${String.format("%.2f", assessmentResult.negativeDeduction)}"
                            ),
                            color = RoseAlert,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (assessmentResult.includesCqSelfAssessment) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "Note: Creative Questions (CQ) in this session used Student Self-Assessment, not automated board grading.",
                                "নোট: সৃজনশীল প্রশ্নগুলো শিক্ষার্থীর স্ব-মূল্যায়ন নির্দেশিকা অনুযায়ী হিসাব করা হয়েছে।"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("finish_assessment_summary")
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Back to Learn Hub", "লার্নিং হাবে ফিরে যাও"))
                }
            }
        }
        return
    }

    if (questions.isEmpty()) {
        EmptyStateView(
            titleEn = "No questions available",
            titleBn = "কোনো প্রশ্ন নেই",
            subtitleEn = "Choose another subject or chapter.",
            subtitleBn = "অন্য বিষয় বা অধ্যায় নির্বাচন করো।",
            language = uiLanguage,
            actionLabelEn = "Back",
            actionLabelBn = "ফিরে যাও",
            onAction = onBack
        )
        return
    }

    val safeIdx = currentIdx.coerceIn(0, questions.lastIndex)
    val q = questions[safeIdx]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${StudyQuestStrings.formatDigits(safeIdx + 1, uiLanguage)} / ${StudyQuestStrings.formatDigits(questions.size, uiLanguage)}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (template != null) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (examRemainingSeconds < 60) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        val mins = examRemainingSeconds / 60
                        val secs = examRemainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        LinearProgressIndicator(
            progress = { (safeIdx + 1).toFloat() / questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                val hasAnswered = !selectedMap[q.id].isNullOrEmpty() || !textMap[q.id].isNullOrBlank()
                QuestionPracticeCard(
                    question = q,
                    questionLanguage = questionLanguage,
                    uiLanguage = uiLanguage,
                    selectedIndices = selectedMap[q.id] ?: emptySet(),
                    textAnswer = textMap[q.id] ?: "",
                    cqSelfScore = cqMap[q.id],
                    isBookmarked = bookmarks.any { it.questionId == q.id },
                    showFeedback = showAnswerImmediately && hasAnswered,
                    onToggleOption = { idx ->
                        val curr = selectedMap[q.id] ?: emptySet()
                        selectedMap[q.id] = if (q.type == QuestionType.MULTI_MCQ) {
                            if (idx in curr) curr - idx else curr + idx
                        } else {
                            setOf(idx)
                        }
                    },
                    onTextAnswerChange = { textMap[q.id] = it },
                    onCqSelfScoreChange = { cqMap[q.id] = it },
                    onToggleBookmark = { onToggleBookmark(q) },
                    onAddRevision = { onAddRevision(q) },
                    onReportQuestion = { onReportQuestion(q) },
                    onAskAi = { onAskAi(q) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { if (safeIdx > 0) currentIdx = safeIdx - 1 },
                enabled = safeIdx > 0,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
            ) {
                Text(StudyQuestStrings.t(uiLanguage, "Previous", "পূর্ববর্তী"))
            }
            if (safeIdx < questions.lastIndex) {
                Button(
                    onClick = { currentIdx = safeIdx + 1 },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("assessment_next_btn")
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Next", "পরবর্তী"))
                }
            } else {
                Button(
                    onClick = { onSubmit(selectedMap.toMap(), textMap.toMap(), cqMap.toMap()) },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("assessment_submit_btn")
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(StudyQuestStrings.t(uiLanguage, "Submit", "জমা দিন"))
                }
            }
        }
    }
}

@Composable
fun RevisionAndPlannerScreen(
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    revisions: List<LocalRevisionEntity>,
    allQuestions: List<QuestionItem>,
    studyPlans: List<StudyPlanDoc>,
    onBack: () -> Unit,
    onReviewItem: (String, Int) -> Unit,
    onCreatePlan: (String, String, String, Int, Long) -> Unit,
    onTogglePlan: (StudyPlanDoc) -> Unit,
    onDeletePlan: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    var showAddPlanDialog by remember { mutableStateOf(false) }
    val now = System.currentTimeMillis()
    val dueRevisions = remember(revisions) { revisions.filter { it.nextDueAtMs <= now } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Revision & Study Planner", "রিভিশন ও স্টাডি প্ল্যানার"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                FilledTonalButton(
                    onClick = { showAddPlanDialog = true },
                    modifier = Modifier.testTag("add_study_plan_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(StudyQuestStrings.t(uiLanguage, "Plan", "পরিকল্পনা"))
                }
            }
        }

        item {
            Text(
                text = StudyQuestStrings.t(
                    uiLanguage,
                    "Spaced Repetition Due (${dueRevisions.size})",
                    "আজকের রিভিশন (${StudyQuestStrings.formatDigits(dueRevisions.size, uiLanguage)})"
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (dueRevisions.isEmpty()) {
            item {
                StudyQuestCard {
                    Text(
                        text = StudyQuestStrings.t(
                            uiLanguage,
                            "No spaced-repetition cards due right now! Bookmark or practice questions to populate your smart review queue.",
                            "এই মুহূর্তে কোনো রিভিশন বাকি নেই! প্রশ্ন অনুশীলন বা বুকমার্ক করলে এখানে স্বয়ংক্রিয়ভাবে জমা হবে।"
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            items(dueRevisions, key = { it.itemId }) { rev ->
                val q = allQuestions.find { it.id == rev.itemId }
                StudyQuestCard {
                    Text(
                        text = if (q != null) StudyQuestStrings.t(questionLanguage, q.promptEn, q.promptBn) else rev.itemId,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (q != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = StudyQuestStrings.t(questionLanguage, q.explanationEn, q.explanationBn),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onReviewItem(rev.itemId, 1) }, modifier = Modifier.weight(1f)) {
                            Text(StudyQuestStrings.t(uiLanguage, "Again", "আবার"))
                        }
                        FilledTonalButton(onClick = { onReviewItem(rev.itemId, 3) }, modifier = Modifier.weight(1f)) {
                            Text(StudyQuestStrings.t(uiLanguage, "Good", "ভালো"))
                        }
                        Button(onClick = { onReviewItem(rev.itemId, 5) }, modifier = Modifier.weight(1f)) {
                            Text(StudyQuestStrings.t(uiLanguage, "Easy", "সহজ"))
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = StudyQuestStrings.t(uiLanguage, "Study Plans & Exam Countdowns", "পড়ার পরিকল্পনা ও পরীক্ষার কাউন্টডাউন"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (studyPlans.isEmpty()) {
            item {
                EmptyStateView(
                    titleEn = "No study plans yet",
                    titleBn = "কোনো পরিকল্পনা নেই",
                    subtitleEn = "Create a daily goal or SSC countdown target.",
                    subtitleBn = "তোমার পরীক্ষার লক্ষ্য বা দৈনিক পরিকল্পনা তৈরি করো।",
                    language = uiLanguage,
                    actionLabelEn = "Create Study Plan",
                    actionLabelBn = "পরিকল্পনা তৈরি করো",
                    onAction = { showAddPlanDialog = true }
                )
            }
        } else {
            items(studyPlans, key = { it.id }) { plan ->
                StudyQuestCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = plan.completed, onCheckedChange = { onTogglePlan(plan) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(plan.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${plan.subjectId.uppercase()} • ${plan.minutesPerDay} min/day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDeletePlan(plan.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete plan")
                        }
                    }
                }
            }
        }
    }

    if (showAddPlanDialog) {
        var title by remember { mutableStateOf("") }
        var subjectId by remember { mutableStateOf("math") }
        var daysAhead by remember { mutableStateOf("14") }
        var minutesPerDay by remember { mutableStateOf("45") }

        AlertDialog(
            onDismissRequest = { showAddPlanDialog = false },
            title = { Text(StudyQuestStrings.t(uiLanguage, "New Study Plan", "নতুন পড়ার পরিকল্পনা")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Goal title (e.g. SSC Math Prep)", "লক্ষ্যের নাম")) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = daysAhead,
                        onValueChange = { daysAhead = it.filter(Char::isDigit) },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Days until exam", "কত দিন বাকি")) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minutesPerDay,
                        onValueChange = { minutesPerDay = it.filter(Char::isDigit) },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Minutes per day", "প্রতিদিন কত মিনিট")) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreatePlan(
                                title,
                                subjectId,
                                "exam_countdown",
                                daysAhead.toIntOrNull() ?: 14,
                                minutesPerDay.toLongOrNull() ?: 45L
                            )
                            showAddPlanDialog = false
                        }
                    }
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Save", "সংরক্ষণ"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlanDialog = false }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDoubtSolverSheet(
    initialContext: String,
    classId: String,
    subjectName: String,
    language: AppLanguage,
    aiState: AiDoubtUiState,
    remainingAiCredits: Int? = null,
    onOpenAiLab: (() -> Unit)? = null,
    onSubmitDoubt: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var questionText by remember(initialContext) {
        mutableStateOf(
            if (initialContext.isNotBlank()) {
                StudyQuestStrings.t(
                    language,
                    "Please explain step-by-step: $initialContext",
                    "ধাপে ধাপে বুঝিয়ে দাও: $initialContext"
                )
            } else ""
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        BackHandler { onDismiss() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            text = "STUDYQUEST HELPER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(language, "Your Personal Learning Companion", "আপনার ব্যক্তিগত পড়াশোনার সঙ্গী"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (remainingAiCredits != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$remainingAiCredits cr left",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Text(
                text = StudyQuestStrings.t(
                    language,
                    "Ask any academic doubt from $classId ($subjectName) in Bangla or English.",
                    "বাংলা বা ইংরেজিতে যেকোনো পাঠ্যবইয়ের প্রশ্ন জিজ্ঞাসা করো।"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text(StudyQuestStrings.t(language, "Your question or topic", "তোমার প্রশ্ন লেখো")) },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_doubt_input")
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { if (questionText.isNotBlank()) onSubmitDoubt(questionText) },
                    enabled = questionText.isNotBlank() && aiState !is AiDoubtUiState.Loading,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("ai_doubt_submit")
                ) {
                    Text(StudyQuestStrings.t(language, "Explain Step-by-Step", "ধাপে ধাপে সমাধান"))
                }
                if (onOpenAiLab != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenAiLab()
                        },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(StudyQuestStrings.t(language, "AI Lab", "AI ল্যাব"))
                    }
                }
            }

            when (aiState) {
                AiDoubtUiState.Idle -> Unit
                AiDoubtUiState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(StudyQuestStrings.t(language, "StudyQuest Helper is thinking...", "উত্তর তৈরি হচ্ছে..."))
                    }
                }
                is AiDoubtUiState.Streaming -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(aiState.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
                    }
                }
                is AiDoubtUiState.Success -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(aiState.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
                    }
                }
                is AiDoubtUiState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = aiState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedButton(onClick = { if (questionText.isNotBlank()) onSubmitDoubt(questionText) }) {
                                Text(StudyQuestStrings.t(language, "Retry", "আবার চেষ্টা"))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
