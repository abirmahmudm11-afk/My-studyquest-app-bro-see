package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
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
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.model.QuestCoachMood
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.MineralTeal
import com.example.ui.theme.SoftPowderBlue
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor

/**
 * Refined Minimal Educational Note Card — clean cool slate/powder-blue surface without cartoon/game chrome.
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

    val moodBadge = when (mood) {
        QuestCoachMood.WELCOME -> StudyQuestStrings.t(language, "STUDY BRIEF", "পাঠ সারসংক্ষেপ")
        QuestCoachMood.LEARNING_ASSISTANCE -> StudyQuestStrings.t(language, "STUDY NOTE", "পাঠ নির্দেশিকা")
        QuestCoachMood.ENCOURAGEMENT -> StudyQuestStrings.t(language, "MOMENTUM", "ধারাবাহিকতা")
        QuestCoachMood.SUCCESS -> StudyQuestStrings.t(language, "COMPLETED", "সম্পন্ন")
        QuestCoachMood.QUEST_COMPLETION -> StudyQuestStrings.t(language, "READY", "প্রস্তুত")
        QuestCoachMood.ACHIEVEMENT -> StudyQuestStrings.t(language, "MILESTONE", "মাইলফলক")
        QuestCoachMood.STUDY_REMINDER -> StudyQuestStrings.t(language, "FOCUS TIME", "ফোকাস সময়")
        QuestCoachMood.EMPTY_STATE -> StudyQuestStrings.t(language, "GETTING STARTED", "শুরুর ধাপ")
    }

    val accent = when (mood) {
        QuestCoachMood.SUCCESS, QuestCoachMood.ACHIEVEMENT -> EditorialSuccess
        QuestCoachMood.LEARNING_ASSISTANCE -> MineralTeal
        else -> MaterialTheme.colorScheme.primary
    }

    val icon = when (mood) {
        QuestCoachMood.SUCCESS, QuestCoachMood.ACHIEVEMENT, QuestCoachMood.QUEST_COMPLETION -> Icons.Outlined.CheckCircle
        QuestCoachMood.STUDY_REMINDER -> Icons.Outlined.Schedule
        QuestCoachMood.LEARNING_ASSISTANCE -> Icons.Outlined.AutoAwesome
        else -> Icons.AutoMirrored.Outlined.MenuBook
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        tonalElevation = 0.dp,
        shadowElevation = if (visualConfig.isGlass) 0.dp else 1.dp,
        border = studyQuestGlassAwareBorder(accent.copy(alpha = 0.30f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftPowderBlue,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = moodBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Spacer(Modifier.size(3.dp))
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(if (reducedMotion) 10 else 180))
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

@Composable
fun StudyQuestBrandMark(
    size: Dp = 48.dp,
    ringColor: Color = BrandTealPrimary,
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
