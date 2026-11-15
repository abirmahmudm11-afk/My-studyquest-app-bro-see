package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.model.AcademicSubject
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.UserQuestDoc
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MangoYellow
import com.example.ui.theme.NaturalGreen
import com.example.ui.theme.WarmOrange

@Composable
fun StudyQuestCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        modifier.fillMaxWidth()
    }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SyncStatusPill(
    isOnline: Boolean,
    pendingSyncCount: Int,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isOnline) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val fgColor = if (isOnline) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = if (isOnline) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(14.dp)
            )
            val label = buildString {
                append(if (isOnline) StudyQuestStrings.onlineStatus(language) else StudyQuestStrings.offlineStatus(language))
                if (pendingSyncCount > 0) {
                    append(" • ")
                    append(StudyQuestStrings.pendingSyncLabel(pendingSyncCount, language))
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fgColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StatBadgePill(
    icon: ImageVector,
    label: String,
    tint: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SubjectCard(
    subject: AcademicSubject,
    language: AppLanguage,
    chapterCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    masteryProgress: Float = 0f
) {
    val accent = Color(subject.accentHex)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "subject_press"
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .minimumInteractiveComponentSize()
            .clickable(interactionSource = interactionSource, indication = androidx.compose.material3.ripple(), onClick = onClick)
            .testTag("subject_card_${subject.id}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = accent.copy(alpha = 0.08f),
                    radius = size.height * 0.65f,
                    center = Offset(size.width * 0.94f, size.height * 0.22f)
                )
            }
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.55f)))
                        )
                )
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accent.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = subject.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = StudyQuestStrings.t(
                                language,
                                "$chapterCount ch",
                                "${StudyQuestStrings.formatDigits(chapterCount, language)} অধ্যায়"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = StudyQuestStrings.t(language, subject.titleEn, subject.titleBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (masteryProgress > 0f) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { masteryProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = accent,
                            trackColor = accent.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestCard(
    quest: UserQuestDoc,
    titleEn: String,
    titleBn: String,
    descriptionEn: String,
    descriptionBn: String,
    language: AppLanguage,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressRatio by animateFloatAsState(
        targetValue = (quest.progress.toFloat() / quest.target.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "quest_progress"
    )
    StudyQuestCard(modifier = modifier.testTag("quest_card_${quest.questId}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = StudyQuestStrings.t(language, titleEn, titleBn),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = StudyQuestStrings.t(language, descriptionEn, descriptionBn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            StatBadgePill(
                icon = Icons.Filled.Star,
                label = "+${StudyQuestStrings.formatDigits(quest.xpReward, language)} XP",
                tint = WarmOrange,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progressRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = if (quest.completed) ForestGreen else NaturalGreen
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${StudyQuestStrings.formatDigits(quest.progress, language)} / ${StudyQuestStrings.formatDigits(quest.target, language)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when {
                quest.claimed -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = StudyQuestStrings.t(language, "Claimed", "সংগৃহীত"),
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                quest.completed -> {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("claim_quest_${quest.questId}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MangoYellow, contentColor = Color(0xFF2B2000))
                    ) {
                        Text(StudyQuestStrings.t(language, "Claim +${quest.coinReward} Coins", "সংগ্রহ করুন"))
                    }
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = MangoYellow, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "+${StudyQuestStrings.formatDigits(quest.coinReward, language)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionPracticeCard(
    question: QuestionItem,
    questionLanguage: AppLanguage,
    uiLanguage: AppLanguage,
    selectedIndices: Set<Int>,
    textAnswer: String,
    cqSelfScore: Double?,
    isBookmarked: Boolean,
    showFeedback: Boolean,
    onToggleOption: (Int) -> Unit,
    onTextAnswerChange: (String) -> Unit,
    onCqSelfScoreChange: (Double) -> Unit,
    onToggleBookmark: () -> Unit,
    onAddRevision: () -> Unit,
    onReportQuestion: () -> Unit,
    onAskAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var flashcardFlipped by remember(question.id) { mutableStateOf(false) }

    StudyQuestCard(modifier = modifier.testTag("question_card_${question.id}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, question.type.labelEn, question.type.labelBn),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${question.board} • ${StudyQuestStrings.formatDigits(question.marks, uiLanguage)} ${StudyQuestStrings.t(uiLanguage, "mark(s)", "নম্বর")}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Row {
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("bookmark_q_${question.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = StudyQuestStrings.t(uiLanguage, "Bookmark question", "প্রশ্ন বুকমার্ক"),
                        tint = if (isBookmarked) MangoYellow else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onReportQuestion,
                    modifier = Modifier.testTag("report_q_${question.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = StudyQuestStrings.t(uiLanguage, "Report issue", "ত্রুটি জানান"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (question.type == QuestionType.CREATIVE_CQ) {
            val stem = StudyQuestStrings.t(questionLanguage, question.cqStemEn, question.cqStemBn)
                .ifBlank { StudyQuestStrings.t(questionLanguage, question.promptEn, question.promptBn) }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stem,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            val subs = if (questionLanguage == AppLanguage.BANGLA && question.cqSubQuestionsBn.isNotEmpty()) {
                question.cqSubQuestionsBn
            } else {
                question.cqSubQuestionsEn
            }
            subs.forEach { sub ->
                Text(
                    text = "• $sub",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = textAnswer,
                onValueChange = onTextAnswerChange,
                label = { Text(StudyQuestStrings.t(uiLanguage, "Your structured answer notes", "তোমার সৃজনশীল উত্তর নোট")) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            val rubric = StudyQuestStrings.t(questionLanguage, question.cqRubricEn, question.cqRubricBn)
            if (rubric.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "Self-Assessment Rubric (not auto-graded):",
                                "স্ব-মূল্যায়ন নির্দেশিকা (স্বয়ংক্রিয় গ্রেড নয়):"
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = rubric, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            val currentSelf = cqSelfScore ?: 0.0
            Spacer(Modifier.height(6.dp))
            Text(
                text = StudyQuestStrings.t(
                    uiLanguage,
                    "Self-Assessment Score: ${currentSelf.toInt()} / ${question.marks}",
                    "স্ব-মূল্যায়ন নম্বর: ${StudyQuestStrings.formatDigits(currentSelf.toInt(), uiLanguage)} / ${StudyQuestStrings.formatDigits(question.marks, uiLanguage)}"
                ),
                style = MaterialTheme.typography.labelLarge
            )
            Slider(
                value = currentSelf.toFloat(),
                onValueChange = { onCqSelfScoreChange(it.toDouble()) },
                valueRange = 0f..question.marks.toFloat().coerceAtLeast(1f),
                steps = (question.marks - 1).coerceAtLeast(0)
            )
        } else if (question.type == QuestionType.FLASHCARD) {
            Text(
                text = StudyQuestStrings.t(questionLanguage, question.promptEn, question.promptBn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(
                onClick = {
                    flashcardFlipped = !flashcardFlipped
                    onToggleOption(0)
                },
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Filled.Flip, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    StudyQuestStrings.t(
                        uiLanguage,
                        if (flashcardFlipped) "Hide Answer" else "Flip Flashcard",
                        if (flashcardFlipped) "উত্তর লুকাও" else "কার্ড উল্টাও"
                    )
                )
            }
            AnimatedVisibility(visible = flashcardFlipped) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = StudyQuestStrings.t(questionLanguage, question.explanationEn, question.explanationBn),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else if (question.type in listOf(QuestionType.SINGLE_MCQ, QuestionType.MULTI_MCQ, QuestionType.TRUE_FALSE)) {
            Text(
                text = StudyQuestStrings.t(questionLanguage, question.promptEn, question.promptBn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            val opts = if (questionLanguage == AppLanguage.BANGLA && question.optionsBn.isNotEmpty()) {
                question.optionsBn
            } else {
                question.optionsEn
            }
            opts.forEachIndexed { idx, optText ->
                val isSelected = idx in selectedIndices
                val isCorrect = idx in question.correctOptionIndices
                val bg by animateColorAsState(
                    targetValue = when {
                        showFeedback && isCorrect -> MaterialTheme.colorScheme.secondaryContainer
                        showFeedback && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    },
                    label = "opt_bg"
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .clickable { onToggleOption(idx) }
                        .testTag("q_${question.id}_opt_$idx"),
                    shape = RoundedCornerShape(14.dp),
                    color = bg,
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val badge = listOf("A", "B", "C", "D").getOrElse(idx) { "${idx + 1}" }
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            Text(
                text = StudyQuestStrings.t(questionLanguage, question.promptEn, question.promptBn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = textAnswer,
                onValueChange = onTextAnswerChange,
                label = { Text(StudyQuestStrings.t(uiLanguage, "Type your answer", "তোমার উত্তর লেখো")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("q_text_input_${question.id}")
            )
        }

        AnimatedVisibility(visible = showFeedback && question.type != QuestionType.FLASHCARD) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Explanation", "ব্যাখ্যা"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = StudyQuestStrings.t(questionLanguage, question.explanationEn, question.explanationBn),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onAddRevision,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
            ) {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Save to Revision", "রিভিশনে যোগ"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = onAskAi,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("ask_ai_${question.id}")
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Ask AI Tutor", "AI সমাধান"),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    titleEn: String,
    titleBn: String,
    subtitleEn: String,
    subtitleBn: String,
    language: AppLanguage,
    actionLabelEn: String? = null,
    actionLabelBn: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    StudyQuestCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_state),
                contentDescription = StudyQuestStrings.t(language, titleEn, titleBn),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = StudyQuestStrings.t(language, titleEn, titleBn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = StudyQuestStrings.t(language, subtitleEn, subtitleBn),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabelEn != null && actionLabelBn != null && onAction != null) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAction, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Text(StudyQuestStrings.t(language, actionLabelEn, actionLabelBn))
                }
            }
        }
    }
}

@Composable
fun RankTierBadge(
    tier: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val tierColor = when (tier) {
        "Master" -> Color(0xFF9333EA)
        "Diamond" -> Color(0xFF0284C7)
        "Platinum" -> Color(0xFF0D9488)
        "Gold" -> MangoYellow
        "Silver" -> Color(0xFF64748B)
        else -> Color(0xFFB45309)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = tierColor.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = tierColor, modifier = Modifier.size(14.dp))
            Text(
                text = StudyQuestStrings.rankTierLabel(tier, language),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tierColor
            )
        }
    }
}

@Composable
fun AvatarBadgeView(
    avatarId: String,
    frameId: String?,
    displayName: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val frameColor = when (frameId) {
        "frame_mango" -> MangoYellow
        "frame_emerald" -> ForestGreen
        else -> NaturalGreen.copy(alpha = 0.45f)
    }
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        border = BorderStroke(2.dp, frameColor),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (avatarId == "owl_classic") {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_titu),
                contentDescription = displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.clip(CircleShape)
            )
        } else {
            val bg = when (avatarId) {
                "tiger_bengal" -> WarmOrange.copy(alpha = 0.22f)
                "kingfisher_bd" -> Color(0xFF0284C7).copy(alpha = 0.22f)
                else -> ForestGreen.copy(alpha = 0.22f)
            }
            Box(
                modifier = Modifier.background(bg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
