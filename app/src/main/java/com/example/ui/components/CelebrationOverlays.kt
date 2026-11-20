package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.model.BadgeRarity
import com.example.data.repository.CelebrationEvent
import com.example.data.repository.GamificationEventBus
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.VioletTertiary
import kotlinx.coroutines.delay

@Composable
fun GamificationCelebrationHost(
    language: AppLanguage,
    reducedMotion: Boolean
) {
    val queue = remember { mutableStateListOf<CelebrationEvent>() }

    LaunchedEffect(Unit) {
        GamificationEventBus.events.collect { event ->
            queue.add(event)
        }
    }

    val currentEvent = queue.firstOrNull() ?: return

    // Auto-dismiss lightweight banners after a short delay, keep Level-Up / Badge / Promotion interactive
    val isLightweightBanner = currentEvent is CelebrationEvent.StreakExtended ||
        currentEvent is CelebrationEvent.GoalCompleted

    if (isLightweightBanner) {
        LaunchedEffect(currentEvent) {
            delay(3600L)
            if (queue.firstOrNull() == currentEvent) {
                queue.removeAt(0)
            }
        }
        val semantic = LocalStudyQuestColors.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.94f),
                exit = fadeOut(tween(180))
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = semantic.successContainer,
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("celebration_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (currentEvent is CelebrationEvent.StreakExtended) {
                                Icons.Filled.LocalFireDepartment
                            } else {
                                Icons.Filled.CheckCircle
                            },
                            contentDescription = null,
                            tint = semantic.success,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            when (currentEvent) {
                                is CelebrationEvent.StreakExtended -> {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "${currentEvent.newStreak}-Day Study Streak!",
                                            "${StudyQuestStrings.formatDigits(currentEvent.newStreak, language)} দিনের স্ট্রিক!"
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = semantic.onSuccessContainer
                                    )
                                    Text(
                                        text = if (currentEvent.usedFreeze) {
                                            StudyQuestStrings.t(language, "Streak Freeze protected your streak!", "স্ট্রিক ফ্রিজ তোমার স্ট্রিক রক্ষা করেছে!")
                                        } else {
                                            StudyQuestStrings.t(language, "Daily learning goal achieved!", "আজকের শেখার লক্ষ্য পূর্ণ হয়েছে!")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = semantic.onSuccessContainer
                                    )
                                }
                                is CelebrationEvent.GoalCompleted -> {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Goal Completed!", "লক্ষ্য পূরণ হয়েছে!"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = semantic.onSuccessContainer
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "${currentEvent.title} • Milestone logged",
                                            "${currentEvent.title} • মাইলফলক অর্জিত"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = semantic.onSuccessContainer
                                    )
                                }
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    } else {
        val pulse = remember(currentEvent) { Animatable(if (reducedMotion) 1f else 0.72f) }
        LaunchedEffect(currentEvent) {
            if (!reducedMotion) {
                pulse.animateTo(1f, tween(340))
            }
        }

        val semantic = LocalStudyQuestColors.current

        AlertDialog(
            onDismissRequest = { if (queue.isNotEmpty()) queue.removeAt(0) },
            confirmButton = {
                Button(
                    onClick = { if (queue.isNotEmpty()) queue.removeAt(0) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("celebration_dismiss_btn")
                ) {
                    Text(StudyQuestStrings.t(language, "Awesome!", "দারুণ!"))
                }
            },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!reducedMotion) {
                        val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_celebration))
                        LottieAnimation(
                            composition = comp,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(140.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(pulse.value),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    when (currentEvent) {
                        is CelebrationEvent.LevelUp -> {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp)) {
                                Canvas(modifier = Modifier.size(84.dp)) {
                                    drawCircle(
                                        color = CobaltPrimary.copy(alpha = 0.16f),
                                        radius = size.minDimension / 2f
                                    )
                                    drawCircle(
                                        color = CobaltPrimary,
                                        radius = size.minDimension / 2.25f,
                                        style = Stroke(width = 6.dp.toPx())
                                    )
                                }
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = CobaltPrimary,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Milestone ${currentEvent.newLevel} Reached!",
                                    "মাইলফলক ${StudyQuestStrings.formatDigits(currentEvent.newLevel, language)} অর্জিত!"
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        is CelebrationEvent.BadgeUnlocked -> {
                            val rarityColor = rarityAccentColor(currentEvent.badge.rarity, semantic.success)
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(86.dp)) {
                                Canvas(modifier = Modifier.size(86.dp)) {
                                    drawCircle(color = rarityColor.copy(alpha = 0.18f))
                                    drawCircle(
                                        color = rarityColor,
                                        radius = size.minDimension / 2.3f,
                                        style = Stroke(width = 5.dp.toPx())
                                    )
                                    drawCircle(
                                        color = rarityColor.copy(alpha = 0.45f),
                                        radius = 4.dp.toPx(),
                                        center = Offset(size.width * 0.18f, size.height * 0.22f)
                                    )
                                    drawCircle(
                                        color = rarityColor.copy(alpha = 0.45f),
                                        radius = 4.dp.toPx(),
                                        center = Offset(size.width * 0.84f, size.height * 0.78f)
                                    )
                                }
                                Icon(
                                    Icons.Filled.WorkspacePremium,
                                    contentDescription = null,
                                    tint = rarityColor,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = StudyQuestStrings.t(language, "Badge Unlocked!", "নতুন ব্যাজ অর্জিত!"),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        is CelebrationEvent.ClassPromoted -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(semantic.successContainer, CircleShape)
                            ) {
                                Icon(
                                    Icons.Filled.School,
                                    contentDescription = null,
                                    tint = semantic.success,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Promoted to ${StudyQuestStrings.classLabel(currentEvent.toClassId, language)}!",
                                    "${StudyQuestStrings.classLabel(currentEvent.toClassId, language)}-তে উত্তীর্ণ!"
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        else -> Unit
                    }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (currentEvent) {
                        is CelebrationEvent.LevelUp -> {
                            Text(
                                text = StudyQuestStrings.levelTitle(currentEvent.newLevel.toInt(), language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CobaltPrimary
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Keep completing lessons and challenges to unlock rare trophies!",
                                    "নতুন ব্যাজ ও ট্রফি আনলক করতে প্রতিদিন অনুশীলন চালিয়ে যাও!"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        is CelebrationEvent.BadgeUnlocked -> {
                            val b = currentEvent.badge
                            Text(
                                text = StudyQuestStrings.t(language, b.titleEn, b.titleBn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = StudyQuestStrings.t(language, b.descriptionEn, b.descriptionBn),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = StudyQuestStrings.t(language, "${b.rarity.labelEn} Achievement", "${b.rarity.labelBn} অর্জন"),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = semantic.success
                            )
                        }
                        is CelebrationEvent.ClassPromoted -> {
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Academic Year ${currentEvent.academicYear} Promotion from ${StudyQuestStrings.classLabel(currentEvent.fromClassId, language)} is active.",
                                    "${StudyQuestStrings.formatDigits(currentEvent.academicYear, language)} শিক্ষাবর্ষে তোমার শ্রেণি স্বয়ংক্রিয়ভাবে উন্নীত হয়েছে।"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> Unit
                    }
                }
            }
        )
    }
}

fun rarityAccentColor(rarity: BadgeRarity, greenColor: Color): Color = when (rarity) {
    BadgeRarity.COMMON -> CobaltPrimary
    BadgeRarity.UNCOMMON -> greenColor
    BadgeRarity.RARE -> TealSecondary
    BadgeRarity.EPIC -> VioletTertiary
    BadgeRarity.LEGENDARY -> AmberGold
}
