package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.local.LocalSoloMatchEntity
import com.example.data.model.MascotMood
import com.example.data.model.PublicProfile
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.MascotTituCard
import com.example.ui.components.RankTierBadge
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.VioletTertiary
import com.example.ui.viewmodel.ActiveSoloSprintState
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoloSpeedSprintScreen(
    sprint: ActiveSoloSprintState,
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    reducedMotion: Boolean,
    onAnswerQuestion: (isCorrect: Boolean, responseTimeMs: Long) -> Unit,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedOptionIdx by remember(sprint.currentIndex) { mutableStateOf<Int?>(null) }
    var shownAtMs by remember(sprint.currentIndex) { mutableLongStateOf(System.currentTimeMillis()) }
    var elapsedSec by remember(sprint.currentIndex) { mutableIntStateOf(0) }

    LaunchedEffect(sprint.currentIndex, sprint.finished) {
        if (!sprint.finished) {
            shownAtMs = System.currentTimeMillis()
            elapsedSec = 0
            while (selectedOptionIdx == null) {
                delay(1000L)
                elapsedSec += 1
            }
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sprint.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Solo Speed Sprint • Local Arena", "সোলো স্পিড স্প্রিন্ট • লোকাল অ্যারেনা"),
                        style = MaterialTheme.typography.labelSmall,
                        color = CobaltPrimary
                    )
                }
                if (!sprint.finished) {
                    StatBadgePill(
                        icon = Icons.Filled.Timer,
                        label = "${StudyQuestStrings.formatDigits(elapsedSec, uiLanguage)}s",
                        tint = VioletTertiary,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }
        }

        if (sprint.finished) {
            item {
                val total = sprint.questions.size.coerceAtLeast(1)
                val won = sprint.correctCount * 2 >= total
                MascotTituCard(
                    mood = if (won) MascotMood.ACHIEVEMENT else MascotMood.ENCOURAGEMENT,
                    messageEn = if (won) "Awesome sprint! You scored ${sprint.scorePoints} points and earned +${sprint.earnedXp} XP & +${sprint.earnedCoins} Coins."
                    else "Good effort! You answered ${sprint.correctCount}/$total correctly and earned +${sprint.earnedXp} XP.",
                    messageBn = if (won) "দারুণ খেলেছো! ${StudyQuestStrings.formatDigits(sprint.scorePoints, uiLanguage)} পয়েন্ট সহ +${StudyQuestStrings.formatDigits(sprint.earnedXp, uiLanguage)} XP অর্জন করেছো।"
                    else "ভালো চেষ্টা! ${StudyQuestStrings.formatDigits(sprint.correctCount, uiLanguage)}/${StudyQuestStrings.formatDigits(total, uiLanguage)} সঠিক হয়েছে।",
                    language = uiLanguage,
                    reducedMotion = reducedMotion,
                    showCelebrationLottie = won
                )
            }
            item {
                StudyQuestCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Sprint Scorecard", "স্প্রিন্ট ফলাফল"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatBadgePill(
                                    icon = Icons.Filled.Star,
                                    label = "+${StudyQuestStrings.formatDigits(sprint.earnedXp, uiLanguage)} XP",
                                    tint = CobaltPrimary,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                                StatBadgePill(
                                    icon = Icons.Filled.MonetizationOn,
                                    label = "+${StudyQuestStrings.formatDigits(sprint.earnedCoins, uiLanguage)} Coins",
                                    tint = VioletTertiary,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            }
                        }
                        AnimatedCircularMasteryRing(
                            progress = sprint.correctCount.toFloat() / sprint.questions.size.coerceAtLeast(1).toFloat(),
                            centerValueText = "${sprint.correctCount}/${sprint.questions.size}",
                            centerCaptionText = StudyQuestStrings.t(uiLanguage, "Correct", "সঠিক"),
                            reducedMotion = reducedMotion,
                            ringColor = TealSecondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            captionColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onPlayAgain,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                        ) {
                            Text(StudyQuestStrings.t(uiLanguage, "Play Again", "আবার খেলো"))
                        }
                        FilledTonalButton(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                        ) {
                            Text(StudyQuestStrings.t(uiLanguage, "Done", "সম্পন্ন"))
                        }
                    }
                }
            }
        } else if (sprint.questions.isNotEmpty()) {
            val safeIdx = sprint.currentIndex.coerceIn(0, sprint.questions.lastIndex)
            val q = sprint.questions[safeIdx]
            item {
                LinearProgressIndicator(
                    progress = { (safeIdx + 1).toFloat() / sprint.questions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = CobaltPrimary
                )
            }
            item {
                StudyQuestCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Q${safeIdx + 1}/${sprint.questions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = CobaltPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${StudyQuestStrings.formatDigits(sprint.scorePoints, uiLanguage)} pts",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = StudyQuestStrings.t(questionLanguage, q.promptEn, q.promptBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    val opts = if (questionLanguage == AppLanguage.BANGLA && q.optionsBn.isNotEmpty()) q.optionsBn else q.optionsEn
                    opts.forEachIndexed { idx, optText ->
                        val picked = selectedOptionIdx == idx
                        val isCorrectOpt = idx in q.correctOptionIndices
                        val bg by animateColorAsState(
                            targetValue = when {
                                selectedOptionIdx != null && isCorrectOpt -> MaterialTheme.colorScheme.secondaryContainer
                                selectedOptionIdx != null && picked && !isCorrectOpt -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            label = "solo_opt_bg"
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = bg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    if (selectedOptionIdx == null) {
                                        selectedOptionIdx = idx
                                        val elapsed = (System.currentTimeMillis() - shownAtMs).coerceAtLeast(100L)
                                        onAnswerQuestion(isCorrectOpt, elapsed)
                                    }
                                },
                                enabled = selectedOptionIdx == null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("solo_sprint_opt_$idx")
                            ) {
                                Text(
                                    text = optText,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
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
fun GuestArenaTabContent(
    profile: PublicProfile,
    uiLanguage: AppLanguage,
    reducedMotion: Boolean,
    selectedSubjectId: String,
    soloMatches: List<LocalSoloMatchEntity>,
    onSelectSubject: (String) -> Unit,
    onStartSoloSprint: (classId: String, subjectId: String) -> Unit,
    onRequestGoogleSignIn: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                StudyQuestGradientHeroBanner(
                    eyebrow = StudyQuestStrings.t(uiLanguage, "SOLO ARENA • OFFLINE READY", "সোলো অ্যারেনা • অফলাইন প্রস্তুত"),
                    title = StudyQuestStrings.t(uiLanguage, "Quiz Battle Arena", "কুইজ ব্যাটল অ্যারেনা"),
                    subtitle = StudyQuestStrings.t(
                        uiLanguage,
                        "Play 5-question timed Solo Speed Sprints anytime to earn Coins, XP & Rating!",
                        "৫ প্রশ্নের টাইমড সোলো স্প্রিন্ট খেলে কয়েন, XP ও রেটিং বাড়াও!"
                    ),
                    reducedMotion = reducedMotion,
                    backgroundResId = R.drawable.img_arena_banner,
                    trailingSlot = {
                        RankTierBadge(tier = profile.rankTier, language = uiLanguage)
                    }
                )
            }
        }

        item {
            StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                StudyQuestCard {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Pick Sprint Subject", "স্প্রিন্টের বিষয়"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BangladeshCurriculumCatalog.subjectsForClass(profile.classId).take(6).forEach { subj ->
                            FilterChip(
                                selected = selectedSubjectId == subj.id,
                                onClick = { onSelectSubject(subj.id) },
                                label = { Text(StudyQuestStrings.t(uiLanguage, subj.titleEn, subj.titleBn)) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onStartSoloSprint(profile.classId, selectedSubjectId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("guest_solo_sprint_btn")
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            StudyQuestStrings.t(uiLanguage, "Start 5-Question Speed Sprint", "৫ প্রশ্নের স্পিড স্প্রিন্ট শুরু করো"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = CobaltPrimary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Unlock Live 1v1 & Private Rooms", "লাইভ ১v১ ও প্রাইভেট রুম আনলক করুন"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Sign in with Google to challenge real classmates live. All Solo XP merges automatically!",
                                    "সহপাঠীদের সাথে লাইভ কুইজ খেলতে গুগল সাইন-ইন করো।"
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(
                            onClick = onRequestGoogleSignIn,
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary, contentColor = Color.White),
                            modifier = Modifier.testTag("guest_compete_signin_btn")
                        ) {
                            Text(StudyQuestStrings.t(uiLanguage, "Sign In", "সাইন-ইন"))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = StudyQuestStrings.t(uiLanguage, "Recent Solo Sprints (${soloMatches.size})", "সাম্প্রতিক সোলো স্প্রিন্ট"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (soloMatches.isEmpty()) {
            item {
                StudyQuestCard {
                    Text(
                        text = StudyQuestStrings.t(
                            uiLanguage,
                            "No sprints completed yet — tap Start above to earn your first battle XP!",
                            "এখনো কোনো স্প্রিন্ট খেলা হয়নি — উপরে ট্যাপ করে প্রথম স্প্রিন্ট শুরু করো!"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(soloMatches, key = { it.id }) { m ->
                StudyQuestCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${m.score} pts • +${m.xpEarned} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatBadgePill(
                            icon = Icons.Filled.EmojiEvents,
                            label = if (m.won) StudyQuestStrings.t(uiLanguage, "Cleared", "সফল")
                            else StudyQuestStrings.t(uiLanguage, "Practiced", "অনুশীলিত"),
                            tint = if (m.won) VioletTertiary else CobaltPrimary,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }
            }
        }
    }
}
