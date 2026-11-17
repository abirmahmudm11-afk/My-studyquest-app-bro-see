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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.PureEditorialSurface
import com.example.ui.theme.QuestGold
import com.example.ui.theme.SoftQuestGold
import com.example.ui.theme.SoftSageGreen
import com.example.ui.theme.WarmTerracotta
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Warm Editorial Paper / Dark Forest Canvas Backdrop.
 * Uses whisper-soft sage & sunlit-gold organic washes (zero harsh neon or purple orbs).
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
                val sageAlpha = if (visualConfig.isDark) 0.22f else 0.34f
                val goldAlpha = if (visualConfig.isDark) 0.14f else 0.24f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SoftSageGreen.copy(alpha = sageAlpha), Color.Transparent),
                        center = Offset(w * 0.16f, h * 0.10f),
                        radius = w * 0.72f
                    ),
                    radius = w * 0.72f,
                    center = Offset(w * 0.16f, h * 0.10f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(QuestGold.copy(alpha = goldAlpha), Color.Transparent),
                        center = Offset(w * 0.88f, h * 0.22f),
                        radius = w * 0.56f
                    ),
                    radius = w * 0.56f,
                    center = Offset(w * 0.88f, h * 0.22f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandForestGreen.copy(alpha = if (visualConfig.isDark) 0.16f else 0.10f), Color.Transparent),
                        center = Offset(w * 0.22f, h * 0.82f),
                        radius = w * 0.60f
                    ),
                    radius = w * 0.60f,
                    center = Offset(w * 0.22f, h * 0.82f)
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandForestGreen.copy(alpha = if (visualConfig.isDark) 0.10f else 0.045f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.90f, h * 0.05f),
                        radius = w * 0.62f
                    ),
                    radius = w * 0.62f,
                    center = Offset(w * 0.90f, h * 0.05f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            QuestGold.copy(alpha = if (visualConfig.isDark) 0.06f else 0.055f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.06f, h * 0.94f),
                        radius = w * 0.52f
                    ),
                    radius = w * 0.52f,
                    center = Offset(w * 0.06f, h * 0.94f)
                )
            }
        }
        content()
    }
}

/**
 * Refined Editorial Section Header with Serif heading + optional uppercase eyebrow + trailing action.
 */
@Composable
fun EditorialSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (!eyebrow.isNullOrBlank()) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            TextButton(
                onClick = onAction,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Bespoke Compose Canvas Illustration: Open NCTB-inspired Textbook + Dotted Quest Path + Gold Stars.
 * Refined architectural line art styled seamlessly for Light (#F7F8F4) and Dark (#17221E) modes.
 */
@Composable
fun OpenBookQuestIllustration(
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false
) {
    val isDark = LocalStudyQuestSurfaceStyle.current.isDark
    val pageColor = if (isDark) Color(0xFF23322C) else PureEditorialSurface
    val pageBackColor = if (isDark) Color(0xFF1B2823) else SoftSageGreen
    val lineInk = if (isDark) Color(0xFF83B89F) else BrandForestGreen
    val subtleTextRule = if (isDark) Color(0xFF42594E) else Color(0xFFD0D8D1)
    val goldAccent = QuestGold
    val starGlow = if (isDark) QuestGold.copy(alpha = 0.24f) else SoftQuestGold

    val shimmer = if (reducedMotion) 0f else {
        val inf = rememberInfiniteTransition(label = "book_quest_float")
        val v by inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "star_pulse"
        )
        v
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Warm soft backdrop halo
        drawCircle(
            color = if (isDark) BrandForestGreen.copy(alpha = 0.14f) else SoftSageGreen.copy(alpha = 0.72f),
            radius = minOf(w, h) * 0.42f,
            center = Offset(w * 0.50f, h * 0.56f)
        )
        drawCircle(
            color = starGlow,
            radius = minOf(w, h) * (0.20f + 0.02f * shimmer),
            center = Offset(w * 0.73f, h * 0.24f)
        )

        // Quest dotted curve arcing up from open pages toward milestone star
        val questArc = Path().apply {
            moveTo(w * 0.34f, h * 0.58f)
            cubicTo(
                w * 0.42f, h * 0.30f,
                w * 0.58f, h * 0.42f,
                w * 0.73f, h * 0.24f
            )
        }
        drawPath(
            path = questArc,
            color = goldAccent,
            style = Stroke(
                width = 2.6.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
        )

        // Book cover shadow base
        drawRoundRect(
            color = DeepForestGreen.copy(alpha = if (isDark) 0.55f else 0.16f),
            topLeft = Offset(w * 0.14f, h * 0.60f),
            size = Size(w * 0.72f, h * 0.26f),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )

        // Outer hardcover frame
        drawRoundRect(
            color = DeepForestGreen,
            topLeft = Offset(w * 0.16f, h * 0.56f),
            size = Size(w * 0.68f, h * 0.26f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )

        // Left & right open pages
        val leftPage = Path().apply {
            moveTo(w * 0.50f, h * 0.57f)
            cubicTo(w * 0.40f, h * 0.52f, w * 0.26f, h * 0.53f, w * 0.18f, h * 0.57f)
            lineTo(w * 0.18f, h * 0.78f)
            cubicTo(w * 0.26f, h * 0.74f, w * 0.40f, h * 0.73f, w * 0.50f, h * 0.78f)
            close()
        }
        val rightPage = Path().apply {
            moveTo(w * 0.50f, h * 0.57f)
            cubicTo(w * 0.60f, h * 0.52f, w * 0.74f, h * 0.53f, w * 0.82f, h * 0.57f)
            lineTo(w * 0.82f, h * 0.78f)
            cubicTo(w * 0.74f, h * 0.74f, w * 0.60f, h * 0.73f, w * 0.50f, h * 0.78f)
            close()
        }
        drawPath(leftPage, color = pageBackColor)
        drawPath(rightPage, color = pageColor)
        drawPath(
            leftPage,
            color = lineInk.copy(alpha = 0.45f),
            style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round)
        )
        drawPath(
            rightPage,
            color = lineInk.copy(alpha = 0.65f),
            style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round)
        )

        // Subtle text lines on pages
        for (row in 0..2) {
            val yFrac = 0.63f + row * 0.045f
            drawLine(
                color = subtleTextRule,
                start = Offset(w * 0.23f, h * yFrac),
                end = Offset(w * (0.43f - row * 0.03f), h * yFrac),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = subtleTextRule,
                start = Offset(w * 0.55f, h * yFrac),
                end = Offset(w * (0.76f - row * 0.02f), h * yFrac),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Gold & forest bookmark ribbon at spine
        val ribbon = Path().apply {
            moveTo(w * 0.485f, h * 0.57f)
            lineTo(w * 0.52f, h * 0.57f)
            lineTo(w * 0.52f, h * 0.85f)
            lineTo(w * 0.502f, h * 0.82f)
            lineTo(w * 0.485f, h * 0.85f)
            close()
        }
        drawPath(ribbon, color = goldAccent)

        // Milestone node dots along the dotted arc
        val nodes = listOf(
            Offset(w * 0.44f, h * 0.39f) to 5.5.dp.toPx(),
            Offset(w * 0.58f, h * 0.35f) to 6.dp.toPx()
        )
        nodes.forEach { (pt, r) ->
            drawCircle(color = PureEditorialSurface, radius = r + 2.dp.toPx(), center = pt)
            drawCircle(color = lineInk, radius = r, center = pt)
        }

        // 4-point editorial north star at summit of quest path
        fun drawEditorialStar(center: Offset, outerR: Float, fill: Color) {
            val innerR = outerR * 0.38f
            val starPath = Path()
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0) - 90.0)
                val rad = if (i % 2 == 0) outerR else innerR
                val px = center.x + (rad * cos(angle)).toFloat()
                val py = center.y + (rad * sin(angle)).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            drawPath(starPath, color = fill)
        }
        drawEditorialStar(Offset(w * 0.73f, h * 0.24f), minOf(w, h) * 0.085f, goldAccent)
        drawEditorialStar(Offset(w * 0.27f, h * 0.30f), minOf(w, h) * 0.042f, lineInk.copy(alpha = 0.65f))
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
        delay((index.coerceIn(0, 8) * 35L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
            animationSpec = tween(240, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 8 }
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
    heroGradientColors: List<Color> = listOf(DeepForestGreen, BrandForestGreen),
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
                animation = tween(4200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave_phase"
        )
        anim
    }

    val containerBrush = if (visualConfig.isGlass) {
        Brush.linearGradient(
            listOf(
                heroGradientColors.first().copy(alpha = if (visualConfig.isDark) 0.78f else 0.94f),
                heroGradientColors.last().copy(alpha = if (visualConfig.isDark) 0.72f else 0.90f)
            )
        )
    } else {
        Brush.linearGradient(heroGradientColors)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = if (visualConfig.isGlass) 0.dp else 2.dp,
        border = BorderStroke(1.dp, QuestGold.copy(alpha = 0.35f))
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
                        .alpha(0.08f)
                )
            }
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f + 0.03f * shimmer),
                    radius = h * 0.56f,
                    center = Offset(w * 0.94f, h * 0.14f)
                )
                drawCircle(
                    color = QuestGold.copy(alpha = 0.14f),
                    radius = h * 0.36f,
                    center = Offset(w * 0.06f, h * 1.04f)
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
                                color = Color.White.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, QuestGold.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = eyebrow,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftQuestGold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
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
    ringColor: Color = QuestGold,
    trackColor: Color = Color.White.copy(alpha = 0.24f),
    valueColor: Color = Color.White,
    captionColor: Color = Color.White.copy(alpha = 0.86f),
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
            val strokePx = 6.5.dp.toPx()
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
        animationSpec = if (reducedMotion) tween(10) else tween(450),
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
    color: Color = EditorialSuccess,
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale = if (reducedMotion) 1f else {
        val inf = rememberInfiniteTransition(label = "pulse_dot")
        val s by inf.animateFloat(
            initialValue = 0.80f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_scale"
        )
        s
    }
    Box(
        modifier = modifier
            .size((8 * scale).dp)
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
                modifier = Modifier.size(240.dp)
            )
        }
    }
}

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
        shape = RoundedCornerShape(18.dp),
        color = studyQuestGlassAwareSurfaceColor(semantic.successContainer.copy(alpha = 0.65f)),
        border = studyQuestGlassAwareBorder(semantic.success)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(120.dp),
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
                    modifier = Modifier.size(52.dp),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
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
                    color = BrandForestGreen
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = QuestGold, modifier = Modifier.size(16.dp))
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
                    modifier = Modifier.size(120.dp),
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
                        color = EditorialSuccess,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                Text(
                    text = StudyQuestStrings.t(language, "Lesson Mastered!", "পাঠ সম্পন্ন হয়েছে!"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
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
    val semantic = LocalStudyQuestColors.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("guest_upgrade_banner"),
        shape = RoundedCornerShape(16.dp),
        color = studyQuestGlassAwareSurfaceColor(semantic.warningContainer),
        border = studyQuestGlassAwareBorder(QuestGold)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = QuestGold.copy(alpha = 0.25f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = WarmTerracotta
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = StudyQuestStrings.t(language, "Offline Guest Study", "অফলাইন গেস্ট সেশন"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "$totalXp XP & $coins Coins ready to sync on Sign-In.",
                        "${StudyQuestStrings.formatDigits(totalXp, language)} XP ও ${StudyQuestStrings.formatDigits(coins, language)} কয়েন সাইন-ইনেই সিঙ্ক হবে।"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onSignInClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandForestGreen,
                    contentColor = PureEditorialSurface
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
