package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.model.QuestCoachMood
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.QuestGold
import com.example.ui.theme.SoftSageGreen
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor

/**
 * Editorial Quest Guide Card — restrained soft-sage / gold note strip with no cartoonish mascot chrome.
 */
@Composable
fun QuestCoachCard(
    mood: QuestCoachMood,
    messageEn: String,
    messageBn: String,
    language: AppLanguage,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    showCelebrationLottie: Boolean = false,
    trailingAction: @Composable (() -> Unit)? = null
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val semantic = LocalStudyQuestColors.current

    val moodBadge = when (mood) {
        QuestCoachMood.WELCOME -> StudyQuestStrings.t(language, "QUEST GUIDE", "কোয়েস্ট গাইড")
        QuestCoachMood.LEARNING_ASSISTANCE -> StudyQuestStrings.t(language, "STUDY NOTE", "পাঠ নির্দেশিকা")
        QuestCoachMood.ENCOURAGEMENT -> StudyQuestStrings.t(language, "MOMENTUM", "গতি ধরে রাখো")
        QuestCoachMood.SUCCESS -> StudyQuestStrings.t(language, "MILESTONE", "অর্জন")
        QuestCoachMood.QUEST_COMPLETION -> StudyQuestStrings.t(language, "QUEST READY", "কোয়েস্ট প্রস্তুত")
        QuestCoachMood.ACHIEVEMENT -> StudyQuestStrings.t(language, "UNLOCKED", "উন্মোচিত")
        QuestCoachMood.STUDY_REMINDER -> StudyQuestStrings.t(language, "FOCUS TIME", "ফোকাস সময়")
        QuestCoachMood.EMPTY_STATE -> StudyQuestStrings.t(language, "GETTING STARTED", "শুরুর ধাপ")
    }

    val isGoldAccent = mood == QuestCoachMood.QUEST_COMPLETION || mood == QuestCoachMood.ACHIEVEMENT
    val accent = when {
        isGoldAccent -> QuestGold
        mood == QuestCoachMood.SUCCESS -> EditorialSuccess
        else -> MaterialTheme.colorScheme.primary
    }
    val surfaceColor = if (isGoldAccent) {
        studyQuestGlassAwareSurfaceColor(semantic.warningContainer)
    } else {
        studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = if (visualConfig.isGlass) 0.dp else 1.dp,
        border = studyQuestGlassAwareBorder(if (isGoldAccent) QuestGold else null)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            if (showCelebrationLottie && !reducedMotion) {
                val celebrationComp by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.lottie_celebration)
                )
                LottieAnimation(
                    composition = celebrationComp,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .size(88.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StudyQuestBrandMark(
                    size = 48.dp,
                    ringColor = accent
                )

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (isGoldAccent) QuestGold.copy(alpha = 0.25f) else SoftSageGreen.copy(alpha = if (visualConfig.isDark) 0.16f else 1f)
                    ) {
                        Text(
                            text = moodBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoldAccent) semantic.onWarningContainer else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.size(6.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(if (reducedMotion) 10 else 200))
                    ) {
                        Text(
                            text = StudyQuestStrings.t(language, messageEn, messageBn),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (trailingAction != null) {
                        Spacer(Modifier.size(8.dp))
                        trailingAction()
                    }
                }
            }
        }
    }
}

@Composable
fun StudyQuestBrandMark(
    size: Dp = 48.dp,
    ringColor: Color = BrandForestGreen,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        border = BorderStroke(1.5.dp, ringColor.copy(alpha = 0.45f)),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_studyquest_logo),
            contentDescription = "StudyQuest Emblem",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    }
}
