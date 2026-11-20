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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.CeruleanSlate
import com.example.ui.theme.CrispCoolSurface
import com.example.ui.theme.DarkHeroSlateBottom
import com.example.ui.theme.DarkHeroSlateTop
import com.example.ui.theme.DeepSlateInk
import com.example.ui.theme.DeepSlateTeal
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.HeroDarkDockSlate
import com.example.ui.theme.HeroGlassBorderLight
import com.example.ui.theme.HeroGlassStageLight
import com.example.ui.theme.HeroMintCardEnd
import com.example.ui.theme.HeroMintCardStart
import com.example.ui.theme.HeroSlateBlueBottom
import com.example.ui.theme.HeroSlateBlueTop
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.MineralTeal
import com.example.ui.theme.MutedMintContainer
import com.example.ui.theme.MutedMintFocal
import com.example.ui.theme.SoftPowderBlue
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class HeroCarouselSlide(
    val id: String,
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val metricValue: String,
    val metricCaption: String,
    val progress: Float,
    val actionLabel: String,
    val actionIcon: ImageVector,
    val gradientColors: List<Color>,
    val contentOnCard: Color,
    val secondaryActionLabel: String? = null,
    val secondaryActionTestTag: String? = null,
    val onSecondaryAction: (() -> Unit)? = null,
    val cardTestTag: String? = null,
    val primaryActionTestTag: String? = null,
    val onClick: () -> Unit
)

data class HeroDockAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String? = null,
    val onClick: () -> Unit
)

/**
 * Reference-Inspired Cool Slate Blue-Gray Atmospheric Hero Stage.
 * Features flanking 3D perspective carousel cards, a central frosted glass frame,
 * subtle circular left/right arrow controls, and an anchored dark translucent pill dock.
 */
@Composable
fun ReferenceHeroCarouselStage(
    slides: List<HeroCarouselSlide>,
    dockActions: List<HeroDockAction>,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    headerSlot: (@Composable () -> Unit)? = null
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val scope = rememberCoroutineScope()
    val safeSlides = slides.ifEmpty { return }
    val hasMultiple = safeSlides.size > 1
    val canonicalBase = if (hasMultiple) safeSlides.size * 100 else 0
    val virtualCount = if (hasMultiple) safeSlides.size * 200 else 1
    val pagerState = rememberPagerState(initialPage = canonicalBase, pageCount = { virtualCount })
    val activeSlideIndex = pagerState.currentPage % safeSlides.size

    val bandColors = if (visualConfig.isDark) {
        listOf(DarkHeroSlateTop, DarkHeroSlateBottom)
    } else {
        listOf(HeroSlateBlueTop, HeroSlateBlueBottom)
    }

    val stageBg = if (visualConfig.isDark) {
        Color(0x36FFFFFF).copy(alpha = 0.08f)
    } else {
        HeroGlassStageLight
    }
    val stageBorder = if (visualConfig.isDark) {
        Color.White.copy(alpha = 0.18f)
    } else {
        HeroGlassBorderLight
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(bandColors))
    ) {
        // Subtle radial light sheen inside the slate band
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = if (visualConfig.isDark) 0.07f else 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.5f, size.height * 0.42f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (headerSlot != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 980.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    headerSlot()
                }
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val isWide = maxWidth >= 600.dp
                val cardWidth = if (isWide) 320.dp else (maxWidth - 88.dp).coerceIn(228.dp, 310.dp)
                val sidePadding = ((maxWidth - cardWidth) / 2f).coerceAtLeast(24.dp)

                // Back frosted glass frame centered behind active slide (Photo 1 signature stage)
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = stageBg,
                    border = BorderStroke(1.2.dp, stageBorder),
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .widthIn(max = if (isWide) 540.dp else maxWidth - 26.dp)
                        .fillMaxWidth()
                        .height(if (dockActions.isNotEmpty()) 296.dp else 234.dp)
                        .padding(horizontal = if (isWide) 0.dp else 10.dp)
                ) {}

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            pageSize = PageSize.Fixed(cardWidth),
                            contentPadding = PaddingValues(horizontal = sidePadding),
                            pageSpacing = 12.dp,
                            beyondViewportPageCount = 2,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            val slideIdx = page % safeSlides.size
                            val slide = safeSlides[slideIdx]
                            val isCanonical = page in canonicalBase until (canonicalBase + safeSlides.size)
                            val rawOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                            val absOffset = rawOffset.absoluteValue.coerceIn(0f, 1.5f)
                            val scale = lerp(1f, 0.84f, absOffset.coerceAtMost(1f))
                            val alpha = lerp(1f, 0.78f, absOffset.coerceAtMost(1f))
                            val tiltY = if (reducedMotion) 0f else (rawOffset * 15f).coerceIn(-18f, 18f)

                            val baseCardMod = Modifier
                                .width(cardWidth)
                                .height(206.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    rotationY = tiltY
                                    cameraDistance = 11f * density
                                }
                                .let { m ->
                                    if (isCanonical && !slide.cardTestTag.isNullOrBlank()) m.testTag(slide.cardTestTag) else m
                                }

                            Surface(
                                onClick = slide.onClick,
                                shape = RoundedCornerShape(22.dp),
                                color = Color.Transparent,
                                shadowElevation = if (absOffset < 0.25f) 12.dp else 3.dp,
                                border = BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = if (absOffset < 0.25f) 0.58f else 0.24f)
                                ),
                                modifier = baseCardMod
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(slide.gradientColors))
                                        .padding(16.dp)
                                ) {
                                    // Subtle soft highlight arc inside focal card
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.14f),
                                            radius = size.height * 0.70f,
                                            center = Offset(size.width * 0.88f, size.height * 0.15f)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = slide.eyebrow,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = slide.contentOnCard.copy(alpha = 0.78f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(Modifier.height(3.dp))
                                                Text(
                                                    text = slide.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = slide.contentOnCard,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Spacer(Modifier.width(8.dp))

                                            // Floating translucent status pill (top-right, inspired by reference)
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color.White.copy(alpha = 0.36f),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f))
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text(
                                                        text = slide.metricValue,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = slide.contentOnCard
                                                    )
                                                    Text(
                                                        text = slide.metricCaption,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = slide.contentOnCard.copy(alpha = 0.78f),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = slide.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = slide.contentOnCard.copy(alpha = 0.86f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            LinearProgressIndicator(
                                                progress = { slide.progress.coerceIn(0.05f, 1f) },
                                                color = slide.contentOnCard,
                                                trackColor = slide.contentOnCard.copy(alpha = 0.20f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(999.dp))
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val primaryMod = Modifier
                                                    .weight(1f)
                                                    .minimumInteractiveComponentSize()
                                                    .let { m ->
                                                        if (isCanonical && !slide.primaryActionTestTag.isNullOrBlank()) {
                                                            m.testTag(slide.primaryActionTestTag)
                                                        } else m
                                                    }
                                                Surface(
                                                    onClick = slide.onClick,
                                                    shape = RoundedCornerShape(999.dp),
                                                    color = slide.contentOnCard.copy(alpha = 0.14f),
                                                    border = BorderStroke(1.dp, slide.contentOnCard.copy(alpha = 0.25f)),
                                                    modifier = primaryMod
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = slide.actionIcon,
                                                            contentDescription = null,
                                                            tint = slide.contentOnCard,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = slide.actionLabel,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = slide.contentOnCard,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                if (!slide.secondaryActionLabel.isNullOrBlank() && slide.onSecondaryAction != null) {
                                                    val secMod = Modifier
                                                        .minimumInteractiveComponentSize()
                                                        .let { m ->
                                                            if (isCanonical && !slide.secondaryActionTestTag.isNullOrBlank()) {
                                                                m.testTag(slide.secondaryActionTestTag)
                                                            } else m
                                                        }
                                                    Surface(
                                                        onClick = slide.onSecondaryAction,
                                                        shape = RoundedCornerShape(999.dp),
                                                        color = Color.White.copy(alpha = 0.30f),
                                                        modifier = secMod
                                                    ) {
                                                        Text(
                                                            text = slide.secondaryActionLabel,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = slide.contentOnCard,
                                                            maxLines = 1,
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Subtle translucent circular Left / Right navigation arrows
                        if (hasMultiple) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 530.dp)
                                    .padding(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = {
                                        val prev = (pagerState.currentPage - 1).coerceAtLeast(0)
                                        scope.launch { pagerState.animateScrollToPage(prev) }
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = if (visualConfig.isDark) 0.18f else 0.34f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.60f)),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .minimumInteractiveComponentSize()
                                        .testTag("hero_carousel_prev_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Previous slide",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Surface(
                                    onClick = {
                                        val next = (pagerState.currentPage + 1).coerceAtMost(virtualCount - 1)
                                        scope.launch { pagerState.animateScrollToPage(next) }
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = if (visualConfig.isDark) 0.18f else 0.34f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.60f)),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .minimumInteractiveComponentSize()
                                        .testTag("hero_carousel_next_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Next slide",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Anchored dark translucent slate quick-action pill dock (3 actions)
                    if (dockActions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (visualConfig.isDark) Color(0xD90E1824) else HeroDarkDockSlate,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .widthIn(max = 360.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                dockActions.forEachIndexed { idx, action ->
                                    if (idx > 0) {
                                        VerticalDivider(
                                            modifier = Modifier.height(28.dp),
                                            color = Color.White.copy(alpha = 0.18f)
                                        )
                                    }
                                    val actMod = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .minimumInteractiveComponentSize()
                                        .clickable(onClick = action.onClick)
                                        .padding(vertical = 6.dp, horizontal = 4.dp)
                                        .let { m ->
                                            if (!action.testTag.isNullOrBlank()) m.testTag(action.testTag) else m
                                        }
                                    Column(
                                        modifier = actMod,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = action.icon,
                                            contentDescription = action.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = action.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.94f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Summary Panel styled after the reference's paired overview cards beneath the hero.
 */
@Composable
fun ReferenceSummaryPanel(
    label: String,
    primaryValue: String,
    supportingText: String,
    progress: Float,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder(),
        shadowElevation = 0.dp,
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0.04f, 1f) },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(999.dp))
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Clean Activity Row styled after the reference's crisp history feed rows.
 */
@Composable
fun ReferenceActivityRow(
    title: String,
    subtitle: String,
    trailingPrimary: String,
    trailingSecondary: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgePill: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!badgePill.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = badgePill,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = trailingPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = trailingSecondary,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Cool Blue-Gray / Deep Slate-Navy Atmospheric Canvas Backdrop.
 * Clean, subtle depth with zero warm/neon/distracting orbs.
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
                val powderAlpha = if (visualConfig.isDark) 0.18f else 0.36f
                val mintAlpha = if (visualConfig.isDark) 0.12f else 0.22f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SoftPowderBlue.copy(alpha = powderAlpha), Color.Transparent),
                        center = Offset(w * 0.18f, h * 0.12f),
                        radius = w * 0.70f
                    ),
                    radius = w * 0.70f,
                    center = Offset(w * 0.18f, h * 0.12f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MutedMintFocal.copy(alpha = mintAlpha), Color.Transparent),
                        center = Offset(w * 0.88f, h * 0.24f),
                        radius = w * 0.56f
                    ),
                    radius = w * 0.56f,
                    center = Offset(w * 0.88f, h * 0.24f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandTealPrimary.copy(alpha = if (visualConfig.isDark) 0.14f else 0.08f), Color.Transparent),
                        center = Offset(w * 0.24f, h * 0.84f),
                        radius = w * 0.60f
                    ),
                    radius = w * 0.60f,
                    center = Offset(w * 0.24f, h * 0.84f)
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CeruleanSlate.copy(alpha = if (visualConfig.isDark) 0.12f else 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.90f, h * 0.06f),
                        radius = w * 0.60f
                    ),
                    radius = w * 0.60f,
                    center = Offset(w * 0.90f, h * 0.06f)
                )
            }
        }
        content()
    }
}

/**
 * Refined Section Header with modern sans heading + optional uppercase eyebrow + trailing action.
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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
 * Clean Architectural Open-Book Vector Illustration in Cool Slate Blue & Muted Mint.
 */
@Composable
fun OpenBookQuestIllustration(
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false
) {
    val isDark = LocalStudyQuestSurfaceStyle.current.isDark
    val pageColor = if (isDark) Color(0xFF1F2E40) else CrispCoolSurface
    val pageBackColor = if (isDark) Color(0xFF172434) else SoftPowderBlue
    val lineInk = if (isDark) Color(0xFF7CA7D6) else CeruleanSlate
    val subtleTextRule = if (isDark) Color(0xFF39506A) else Color(0xFFCBD7E6)
    val mintAccent = MutedMintFocal
    val haloGlow = if (isDark) MutedMintFocal.copy(alpha = 0.22f) else MutedMintContainer

    val shimmer = if (reducedMotion) 0f else {
        val inf = rememberInfiniteTransition(label = "book_cool_float")
        val v by inf.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "node_pulse"
        )
        v
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = if (isDark) CeruleanSlate.copy(alpha = 0.16f) else SoftPowderBlue.copy(alpha = 0.80f),
            radius = minOf(w, h) * 0.42f,
            center = Offset(w * 0.50f, h * 0.56f)
        )
        drawCircle(
            color = haloGlow,
            radius = minOf(w, h) * (0.18f + 0.02f * shimmer),
            center = Offset(w * 0.73f, h * 0.24f)
        )

        val arcPath = Path().apply {
            moveTo(w * 0.34f, h * 0.58f)
            cubicTo(
                w * 0.42f, h * 0.30f,
                w * 0.58f, h * 0.42f,
                w * 0.73f, h * 0.24f
            )
        }
        drawPath(
            path = arcPath,
            color = mintAccent,
            style = Stroke(
                width = 2.6.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
        )

        drawRoundRect(
            color = DeepSlateTeal.copy(alpha = if (isDark) 0.50f else 0.15f),
            topLeft = Offset(w * 0.14f, h * 0.60f),
            size = Size(w * 0.72f, h * 0.26f),
            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )
        drawRoundRect(
            color = DeepSlateTeal,
            topLeft = Offset(w * 0.16f, h * 0.56f),
            size = Size(w * 0.68f, h * 0.26f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )

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
        drawPath(leftPage, color = lineInk.copy(alpha = 0.45f), style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round))
        drawPath(rightPage, color = lineInk.copy(alpha = 0.65f), style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round))

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

        val ribbon = Path().apply {
            moveTo(w * 0.485f, h * 0.57f)
            lineTo(w * 0.52f, h * 0.57f)
            lineTo(w * 0.52f, h * 0.85f)
            lineTo(w * 0.502f, h * 0.82f)
            lineTo(w * 0.485f, h * 0.85f)
            close()
        }
        drawPath(ribbon, color = mintAccent)

        listOf(
            Offset(w * 0.44f, h * 0.39f) to 5.dp.toPx(),
            Offset(w * 0.58f, h * 0.35f) to 5.5.dp.toPx()
        ).forEach { (pt, r) ->
            drawCircle(color = CrispCoolSurface, radius = r + 2.dp.toPx(), center = pt)
            drawCircle(color = lineInk, radius = r, center = pt)
        }

        drawCircle(
            color = mintAccent,
            radius = minOf(w, h) * 0.065f,
            center = Offset(w * 0.73f, h * 0.24f)
        )
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
        delay((index.coerceIn(0, 8) * 30L))
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
            animationSpec = tween(240, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 10 }
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
    heroGradientColors: List<Color> = listOf(HeroSlateBlueTop, HeroSlateBlueBottom),
    trailingSlot: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val resolvedSurface = if (visualConfig.isDark) Color(0xFF161D2A) else Color(0xFF0F172A)
    val resolvedBorder = if (visualConfig.isDark) Color.White.copy(alpha = 0.12f) else Color(0xFF1E293B)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = resolvedSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, resolvedBorder)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
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
                                color = Color.White.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                            ) {
                                Text(
                                    text = eyebrow,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.90f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f)
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
    ringColor: Color = MutedMintFocal,
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
            delay(2200L)
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
        color = studyQuestGlassAwareSurfaceColor(semantic.successContainer.copy(alpha = 0.72f)),
        border = studyQuestGlassAwareBorder(semantic.success)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!reducedMotion) {
                    LottieAnimation(
                        composition = comp,
                        iterations = 1,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = semantic.success,
                    modifier = Modifier.size(48.dp),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
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
                    color = BrandTealPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!reducedMotion) {
                        LottieAnimation(
                            composition = comp,
                            iterations = 1,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = EditorialSuccess,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                Text(
                    text = StudyQuestStrings.t(language, "Lesson Completed", "পাঠ সম্পন্ন হয়েছে"),
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
                        text = StudyQuestStrings.t(
                            language,
                            "Chapter milestone & badge progress saved",
                            "অধ্যায়ের মাইলফলক ও ব্যাজ অগ্রগতি সংরক্ষিত"
                        ),
                        style = MaterialTheme.typography.labelMedium,
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
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        border = studyQuestGlassAwareBorder()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
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
                        "Your notes and lesson progress will sync cleanly on Sign-In.",
                        "সাইন-ইন করলেই আপনার নোট ও পাঠের অগ্রগতি ক্লাউডে সিঙ্ক হবে।"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onSignInClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
