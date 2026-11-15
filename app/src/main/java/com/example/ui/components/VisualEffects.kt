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
import com.example.ui.theme.CobaltDark
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.OnCobaltContainer
import com.example.ui.theme.SkyCyanAccent
import com.example.ui.theme.TealSecondary
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
        shape = RoundedCornerShape(24.dp),
        color = CobaltDark,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, CobaltPrimary.copy(alpha = 0.35f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (backgroundResId != null) {
                Image(
                    painter = painterResource(id = backgroundResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.24f)
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CobaltDark.copy(alpha = 0.95f),
                                CobaltPrimary.copy(alpha = 0.90f),
                                TealSecondary.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    color = SkyCyanAccent.copy(alpha = 0.16f),
                    radius = h * (0.42f + 0.06f * shimmer),
                    center = Offset(w * 0.90f, h * 0.18f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = h * 0.55f,
                    center = Offset(w * (0.15f + 0.05f * shimmer), h * 1.05f)
                )
                drawCircle(
                    color = SkyCyanAccent.copy(alpha = 0.35f),
                    radius = 5.dp.toPx(),
                    center = Offset(w * 0.72f, h * (0.25f + 0.1f * shimmer))
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f),
                    radius = 3.5.dp.toPx(),
                    center = Offset(w * 0.82f, h * (0.72f - 0.08f * shimmer))
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
                                color = SkyCyanAccent.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = eyebrow,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE0F2FE),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                    if (trailingSlot != null) {
                        Spacer(Modifier.width(12.dp))
                        trailingSlot()
                    }
                }
                if (bottomContent != null) {
                    Spacer(Modifier.height(12.dp))
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
    ringColor: Color = SkyCyanAccent,
    trackColor: Color = Color.White.copy(alpha = 0.22f),
    valueColor: Color = Color.White,
    captionColor: Color = Color.White.copy(alpha = 0.84f),
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
    color: Color = TealSecondary,
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, CobaltPrimary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = CobaltPrimary.copy(alpha = 0.14f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = OnCobaltContainer
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
                    containerColor = CobaltPrimary,
                    contentColor = Color.White
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
