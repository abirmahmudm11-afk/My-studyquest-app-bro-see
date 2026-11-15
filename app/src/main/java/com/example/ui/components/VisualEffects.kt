package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.ui.theme.CoolNeutralFill
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalGreen
import com.example.ui.theme.OnSageContainer
import com.example.ui.theme.PureCardWhite
import com.example.ui.theme.SageContainer
import kotlinx.coroutines.delay

@Composable
fun StaggeredEntrance(
    index: Int,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (reducedMotion) {
        Box(modifier = modifier) { content() }
        return
    }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(index) {
        delay((index.coerceIn(0, 8) * 45L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(280)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialOffsetY = { it / 5 }
        )
    ) {
        content()
    }
}

@Composable
fun StudyQuestGradientHeroBanner(
    title: String,
    subtitle: String,
    eyebrow: String? = null,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundResId: Int? = null,
    trailingSlot: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val shimmer = if (reducedMotion) 0f else {
        val inf = rememberInfiniteTransition(label = "hero_wave")
        val anim by inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave_phase"
        )
        anim
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (backgroundResId != null) {
                Image(
                    painter = painterResource(id = backgroundResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.06f)
                )
            }
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    color = ForestGreenPrimary.copy(alpha = 0.045f + 0.015f * shimmer),
                    radius = h * 0.52f,
                    center = Offset(w * 0.94f, h * 0.14f)
                )
                drawCircle(
                    color = CoolNeutralFill.copy(alpha = 0.65f),
                    radius = h * 0.42f,
                    center = Offset(w * 0.12f, h * 1.05f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (!eyebrow.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = eyebrow,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (trailingSlot != null) {
                        Spacer(Modifier.width(12.dp))
                        trailingSlot()
                    }
                }
                if (bottomContent != null) {
                    Spacer(Modifier.height(14.dp))
                    bottomContent()
                }
            }
        }
    }
}

@Composable
fun AnimatedCircularMasteryRing(
    progress: Float,
    centerValueText: String,
    centerCaptionText: String,
    reducedMotion: Boolean,
    size: Dp = 76.dp,
    ringColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    captionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped,
        animationSpec = if (reducedMotion) tween(10) else spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ring_progress"
    )
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 7.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerValueText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = centerCaptionText,
                style = MaterialTheme.typography.labelSmall,
                color = captionColor
            )
        }
    }
}

@Composable
fun AnimatedCounterText(
    targetValue: Int,
    language: AppLanguage,
    suffix: String = "",
    reducedMotion: Boolean = false,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val animated by animateIntAsState(
        targetValue = targetValue,
        animationSpec = if (reducedMotion) tween(10) else tween(550),
        label = "animated_counter"
    )
    Text(
        text = StudyQuestStrings.formatDigits(animated, language) + suffix,
        style = style,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
fun PulseLiveDot(
    color: Color = NaturalGreen,
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale = if (reducedMotion) 1f else {
        val inf = rememberInfiniteTransition(label = "pulse_dot")
        val s by inf.animateFloat(
            initialValue = 0.78f,
            targetValue = 1.22f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_scale"
        )
        s
    }
    Box(
        modifier = modifier
            .size((9 * scale).dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun LottieCelebrationOverlay(
    triggerKey: Int,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(triggerKey) {
        if (triggerKey > 0 && !reducedMotion) {
            visible = true
            delay(2100L)
            visible = false
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(300)),
        modifier = modifier
    ) {
        val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_celebration))
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LottieAnimation(
                composition = comp,
                iterations = 1,
                modifier = Modifier.size(220.dp)
            )
        }
    }
}

@Composable
fun GuestUpgradeBannerCard(
    language: AppLanguage,
    totalXp: Long,
    coins: Long,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("guest_upgrade_banner"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SageContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = OnSageContainer
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = StudyQuestStrings.t(language, "Guest Mode • Local Progress", "গেস্ট মোড • ডিভাইসে সংরক্ষিত"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "$totalXp XP & $coins Coins ready to auto-merge on Google Sign-In.",
                        "${StudyQuestStrings.formatDigits(totalXp, language)} XP ও ${StudyQuestStrings.formatDigits(coins, language)} কয়েন গুগল সাইন-ইনেই সিঙ্ক হবে।"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onSignInClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreenPrimary,
                    contentColor = PureCardWhite
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("guest_banner_signin_btn")
            ) {
                Text(StudyQuestStrings.t(language, "Sync", "সিঙ্ক"))
            }
        }
    }
}
