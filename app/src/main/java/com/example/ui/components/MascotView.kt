package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.data.model.MascotMood
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.VioletTertiary

@Composable
fun MascotTituCard(
    mood: MascotMood,
    messageEn: String,
    messageBn: String,
    language: AppLanguage,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    showCelebrationLottie: Boolean = false,
    trailingAction: @Composable (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "titu_bob")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob_y"
    )

    val moodBadge = when (mood) {
        MascotMood.WELCOME -> StudyQuestStrings.t(language, "Welcome!", "স্বাগতম!")
        MascotMood.LEARNING_ASSISTANCE -> StudyQuestStrings.t(language, "Titu's Tip", "টিটুর পরামর্শ")
        MascotMood.ENCOURAGEMENT -> StudyQuestStrings.t(language, "Keep Going", "এগিয়ে চলো")
        MascotMood.SUCCESS -> StudyQuestStrings.t(language, "Brilliant!", "দারুণ!")
        MascotMood.QUEST_COMPLETION -> StudyQuestStrings.t(language, "Quest Done!", "কোয়েস্ট সম্পন্ন!")
        MascotMood.ACHIEVEMENT -> StudyQuestStrings.t(language, "Achievement!", "অর্জন!")
        MascotMood.STUDY_REMINDER -> StudyQuestStrings.t(language, "Study Time", "পড়ার সময়")
        MascotMood.EMPTY_STATE -> StudyQuestStrings.t(language, "Ready to Start", "শুরু করতে প্রস্তুত")
    }

    val ringColor = when (mood) {
        MascotMood.SUCCESS, MascotMood.QUEST_COMPLETION, MascotMood.ACHIEVEMENT -> VioletTertiary
        MascotMood.ENCOURAGEMENT, MascotMood.STUDY_REMINDER -> CobaltPrimary
        else -> TealSecondary
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            if (showCelebrationLottie && !reducedMotion) {
                val celebrationComp by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.lottie_celebration)
                )
                LottieAnimation(
                    composition = celebrationComp,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .size(110.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MascotAvatar(
                    size = 64.dp,
                    ringColor = ringColor,
                    modifier = Modifier.offset(y = bobOffset.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ringColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = moodBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ringColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.size(6.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = if (reducedMotion) fadeIn(tween(10)) else fadeIn() + scaleIn(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
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
fun MascotAvatar(
    size: Dp = 56.dp,
    ringColor: Color = TealSecondary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        border = BorderStroke(2.5.dp, ringColor),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_mascot_titu),
            contentDescription = "StudyQuest Mascot Titu",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    }
}
