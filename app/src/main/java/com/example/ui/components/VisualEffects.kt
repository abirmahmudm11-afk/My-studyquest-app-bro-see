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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.ui.theme.BerryViolet
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.PureCardWhite
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SunsetCoral
import com.example.ui.theme.VividEmerald
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
import kotlinx.coroutines.delay

/**
 * Full-screen playful ambient canvas supporting both Vivid Classic and Glassmorphism modes.
 * When Glassmorphism is enabled, multi-color blurred-like gradient orbs sit behind translucent
 * frosted surfaces so specular glass cards pop naturally.
 */
@Composable
fun StudyQuestAmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val baseBg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (visualConfig.isGlass) {
                val indigoAlpha = if (visualConfig.isDark) 0.34f else 0.22f
                val cyanAlpha = if (visualConfig.isDark) 0.24f else 0.18f
                val violetAlpha = if (visualConfig.isDark) 0.26f else 0.16f
                val emeraldAlpha = if (visualConfig.isDark) 0.18f else 0.14f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricIndigo.copy(alpha = indigoAlpha), Color.Transparent),
                        center = Offset(w * 0.14f, h * 0.08f),
                        radius = w * 0.72f
                    ),
                    radius = w * 0.72f,
                    center = Offset(w * 0.14f, h * 0.08f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SkyCyan.copy(alpha = cyanAlpha), Color.Transparent),
                        center = Offset(w * 0.92f, h * 0.24f),
                        radius = w * 0.62f
                    ),
                    radius = w * 0.62f,
                    center = Offset(w * 0.92f, h * 0.24f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BerryViolet.copy(alpha = violetAlpha), Color.Transparent),
                        center = Offset(w * 0.22f, h * 0.68f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.22f, h * 0.68f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VividEmerald.copy(alpha = emeraldAlpha), Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.88f),
                        radius = w * 0.58f
                    ),
                    radius = w * 0.58f,
                    center = Offset(w * 0.85f, h * 0.88f)
                )
            } else {
                // Subtle top playful sky accent in Normal Vivid mode
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricIndigo.copy(alpha = if (visualConfig.isDark) 0.16f else 0.07f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.88f, h * 0.05f),
                        radius = w * 0.6f
                    ),
                    radius = w * 0.6f,
                    center = Offset(w * 0.88f, h * 0.05f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SkyCyan.copy(alpha = if (visualConfig.isDark) 0.10f else 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.08f, h * 0.92f),
                        radius = w * 0.5f
                    ),
                    radius = w * 0.5f,
                    center = Offset(w * 0.08f, h * 0.92f)
                )
            }
        }
        content()
    }
}

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
        delay((index.coerceIn(0, 8) * 40L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(260)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialOffsetY = { it / 6 }
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
    heroGradientColors: List<Color> = listOf(ElectricIndigo, BerryViolet),
    trailingSlot: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
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

    val containerBrush = if (visualConfig.isGlass) {
        Brush.linearGradient(
            listOf(
                heroGradientColors.first().copy(alpha = if (visualConfig.isDark) 0.52f else 0.82f),
                heroGradientColors.last().copy(alpha = if (visualConfig.isDark) 0.45f else 0.78f)
            )
        )
    } else {
        Brush.linearGradient(heroGradientColors)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = if (visualConfig.isGlass) 0.dp else 4.dp,
        border = if (visualConfig.isGlass) studyQuestGlassAwareBorder(heroGradientColors.first()) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerBrush)
        ) {
            if (backgroundResId != null) {
                Image(
                    painter = painterResource(id = backgroundResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.14f)
                )
            }
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    color = Color.White.copy(alpha = 0.14f + 0.05f * shimmer),
                    radius = h * 0.56f,
                    center = Offset(w * 0.94f, h * 0.12f)
                )
                drawCircle(
                    color = SkyCyan.copy(alpha = 0.20f),
                    radius = h * 0.40f,
                    center = Offset(w * 0.08f, h * 1.04f)
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
                                color = Color.White.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = eyebrow,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.90f)
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
    ringColor: Color = Color.White,
    trackColor: Color = Color.White.copy(alpha = 0.26f),
    valueColor: Color = Color.White,
    captionColor: Color = Color.White.copy(alpha = 0.85f),
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
    color: Color = VividEmerald,
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
            delay(2500L)
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
                modifier = Modifier.size(260.dp)
            )
        }
    }
}

/**
 * Prominent Lottie 'Success' celebration card shown on finishing a Mock Exam, Practice Quiz, or Lesson.
 */
@Composable
fun LottieSuccessCelebrationCard(
    title: String,
    subtitle: String,
    xpAwardedText: String? = null,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val semantic = LocalStudyQuestColors.current
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_celebration))
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lottie_success_card"),
        shape = RoundedCornerShape(22.dp),
        color = studyQuestGlassAwareSurfaceColor(semantic.successContainer.copy(alpha = 0.55f)),
        border = studyQuestGlassAwareBorder(semantic.success)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(136.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!reducedMotion) {
                    LottieAnimation(
                        composition = comp,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = semantic.success,
                    modifier = Modifier.size(56.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (!xpAwardedText.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = ElectricIndigo
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(
                            text = xpAwardedText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonSuccessLottieDialog(
    lessonTitle: String,
    xpEarned: Int,
    language: AppLanguage,
    reducedMotion: Boolean,
    onDismiss: () -> Unit
) {
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_celebration))
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("lesson_success_dismiss_btn")
            ) {
                Text(StudyQuestStrings.t(language, "Continue Learning", "শেখা চালিয়ে যাও"))
            }
        },
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lesson_success_lottie_dialog"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!reducedMotion) {
                        LottieAnimation(
                            composition = comp,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = VividEmerald,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Text(
                    text = StudyQuestStrings.t(language, "Lesson Mastered!", "পাঠ সম্পন্ন হয়েছে!"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = lessonTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "+${StudyQuestStrings.formatDigits(xpEarned, language)} XP",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    )
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
        shape = RoundedCornerShape(20.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(SunsetCoral)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SunsetCoral.copy(alpha = 0.16f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = SunsetCoral
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
                    containerColor = ElectricIndigo,
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
