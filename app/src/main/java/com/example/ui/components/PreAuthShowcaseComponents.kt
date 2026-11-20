package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.DarkHeroSlateBottom
import com.example.ui.theme.DarkHeroSlateTop
import com.example.ui.theme.HeroGlassBorderLight
import com.example.ui.theme.HeroGlassStageLight
import com.example.ui.theme.HeroSlateBlueBottom
import com.example.ui.theme.HeroSlateBlueTop
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.studyQuestGlassAwareBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private data class LaunchValuePillar(
    val id: String,
    val badge: String,
    val titleEn: String,
    val titleBn: String,
    val statChip: String,
    val progress: Float,
    val icon: ImageVector,
    val isLimeFocal: Boolean
)

private data class AuthCarouselSlide(
    val id: String,
    val eyebrow: String,
    val title: String,
    val statusBadge: String,
    val progress: Float,
    val icon: ImageVector,
    val isLimeFocal: Boolean
)

/**
 * Compact, serene Slate Blue-Gray & Electric Lime 3D Stage reserved for the Launch screen.
 * Clean typographic header + frosted glass 3D card showcase without crowded badges or rainbows.
 */
@Composable
fun LaunchStudyPortalHeroShowcase(
    language: AppLanguage = AppLanguage.ENGLISH,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    headerTrailingSlot: (@Composable () -> Unit)? = null
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val semantic = LocalStudyQuestColors.current
    val scope = rememberCoroutineScope()
    var userPaused by remember { mutableStateOf(false) }

    val pillars = remember {
        listOf(
            LaunchValuePillar(
                id = "nctb",
                badge = "NCTB 2026–27",
                titleEn = "Offline Textbooks",
                titleBn = "অফলাইন পাঠ্যবই",
                statChip = "Classes 6–10",
                progress = 0.88f,
                icon = Icons.Filled.MenuBook,
                isLimeFocal = true
            ),
            LaunchValuePillar(
                id = "prep",
                badge = "BOARD EXAMS",
                titleEn = "MCQ, CQ & Mock Tests",
                titleBn = "বোর্ড MCQ ও মডেল টেস্ট",
                statChip = "SSC Ready",
                progress = 0.76f,
                icon = Icons.Filled.Quiz,
                isLimeFocal = false
            ),
            LaunchValuePillar(
                id = "ai",
                badge = "BILINGUAL TUTOR",
                titleEn = "Step-by-Step AI Help",
                titleBn = "ধাপে ধাপে AI সমাধান",
                statChip = "BN • EN",
                progress = 0.92f,
                icon = Icons.Filled.AutoAwesome,
                isLimeFocal = false
            )
        )
    }

    val canonicalBase = pillars.size * 100
    val virtualCount = pillars.size * 200
    val pagerState = rememberPagerState(initialPage = canonicalBase, pageCount = { virtualCount })

    LaunchedEffect(reducedMotion, userPaused, pagerState.isScrollInProgress) {
        if (!reducedMotion && !userPaused && !pagerState.isScrollInProgress) {
            while (true) {
                delay(3600L)
                val next = (pagerState.currentPage + 1).coerceAtMost(virtualCount - 1)
                pagerState.animateScrollToPage(
                    page = next,
                    animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val floatOffsetDp by if (!reducedMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "launch_hero_float")
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "launch_float_y"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val bandColors = if (visualConfig.isDark) {
        listOf(DarkHeroSlateTop, DarkHeroSlateBottom)
    } else {
        listOf(HeroSlateBlueTop, HeroSlateBlueBottom)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(bandColors))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_launch_study_portal),
            contentDescription = "StudyQuest study workspace visual",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (visualConfig.isDark) 0.22f else 0.30f }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            bandColors.first().copy(alpha = 0.64f),
                            bandColors.last().copy(alpha = 0.82f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 14.dp, bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Minimal Swiss Top Bar: Brand Mark + Compact Language Toggle
            Row(
                modifier = Modifier
                    .widthIn(max = 920.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StudyQuestBrandMark(size = 32.dp)
                    Text(
                        text = "StudyQuest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                if (headerTrailingSlot != null) {
                    headerTrailingSlot()
                }
            }

            // Frosted 3D Glass Pager Showcase
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val isWide = maxWidth >= 600.dp
                val cardWidth = if (isWide) 336.dp else (maxWidth - 82.dp).coerceIn(238.dp, 316.dp)
                val sidePadding = ((maxWidth - cardWidth) / 2f).coerceAtLeast(20.dp)

                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = if (visualConfig.isDark) Color.White.copy(alpha = 0.08f) else HeroGlassStageLight,
                    border = BorderStroke(
                        1.dp,
                        if (visualConfig.isDark) Color.White.copy(alpha = 0.16f) else HeroGlassBorderLight
                    ),
                    modifier = Modifier
                        .widthIn(max = if (isWide) 470.dp else maxWidth - 32.dp)
                        .fillMaxWidth()
                        .height(174.dp)
                ) {}

                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fixed(cardWidth),
                    contentPadding = PaddingValues(horizontal = sidePadding),
                    pageSpacing = 12.dp,
                    beyondViewportPageCount = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("launch_live_pillar_pager")
                ) { page ->
                    val pillarIdx = page % pillars.size
                    val pillar = pillars[pillarIdx]
                    val isCanonical = page in canonicalBase until (canonicalBase + pillars.size)
                    val rawOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absOffset = rawOffset.absoluteValue.coerceIn(0f, 1.4f)
                    val scale = lerp(1f, 0.85f, absOffset.coerceAtMost(1f))
                    val alpha = lerp(1f, 0.72f, absOffset.coerceAtMost(1f))
                    val tiltY = if (reducedMotion) 0f else (rawOffset * 14f).coerceIn(-16f, 16f)
                    val bobPx = if (absOffset < 0.25f) floatOffsetDp else 0f

                    val cardGradient = if (pillar.isLimeFocal) {
                        listOf(semantic.heroLimeStart, semantic.heroLimeEnd)
                    } else if (visualConfig.isDark) {
                        listOf(Color(0xFF1E2634), Color(0xFF161C27))
                    } else {
                        listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                    }
                    val inkColor = when {
                        pillar.isLimeFocal -> semantic.onHeroLime
                        visualConfig.isDark -> Color(0xFFF8FAFC)
                        else -> Color(0xFF0F172A)
                    }

                    Surface(
                        onClick = {
                            userPaused = true
                            scope.launch { pagerState.animateScrollToPage(page) }
                        },
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        shadowElevation = if (absOffset < 0.25f) 8.dp else 1.dp,
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = if (absOffset < 0.25f) 0.55f else 0.20f)
                        ),
                        modifier = Modifier
                            .width(cardWidth)
                            .height(154.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                rotationY = tiltY
                                translationY = bobPx * density
                                cameraDistance = 11f * density
                            }
                            .let { m -> if (isCanonical) m.testTag("launch_pillar_card_${pillar.id}") else m }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(cardGradient))
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = inkColor.copy(alpha = 0.09f),
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = pillar.icon,
                                                    contentDescription = null,
                                                    tint = inkColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = pillar.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = inkColor.copy(alpha = 0.72f)
                                        )
                                    }

                                    Text(
                                        text = pillar.statChip,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = inkColor.copy(alpha = 0.80f)
                                    )
                                }

                                Text(
                                    text = StudyQuestStrings.t(language, pillar.titleEn, pillar.titleBn),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = inkColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                LinearProgressIndicator(
                                    progress = { pillar.progress },
                                    color = inkColor,
                                    trackColor = inkColor.copy(alpha = 0.16f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                )
                            }
                        }
                    }
                }
            }

            // Quiet Pager Indicator Dots
            val currentLogicalPillar = pagerState.currentPage % pillars.size
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pillars.forEachIndexed { idx, pillar ->
                    val active = currentLogicalPillar == idx
                    val pillWidth by animateDpAsState(
                        targetValue = if (active) 22.dp else 7.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                        label = "pillar_dot_width"
                    )
                    val pillColor by animateColorAsState(
                        targetValue = if (active) Color.White else Color.White.copy(alpha = 0.38f),
                        label = "pillar_dot_color"
                    )
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(pillWidth)
                            .clip(RoundedCornerShape(999.dp))
                            .background(pillColor)
                            .clickable {
                                userPaused = true
                                val currentCycleStart = pagerState.currentPage - currentLogicalPillar
                                scope.launch { pagerState.animateScrollToPage(currentCycleStart + idx) }
                            }
                            .testTag("launch_pillar_dot_${pillar.id}")
                    )
                }
            }
        }
    }
}

/**
 * Retained helper for compatibility; styled cleanly in Swiss monochrome.
 */
@Composable
fun AnimatedLanguageChoiceCard(
    title: String,
    subtitle: String,
    badgeCode: String,
    isFeaturedTeal: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "lang_card_scale"
    )

    val containerBg = when {
        isFeaturedTeal -> BrandTealPrimary
        visualConfig.isDark -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.White
    }
    val titleColor = if (isFeaturedTeal) Color.White else MaterialTheme.colorScheme.onSurface
    val subColor = if (isFeaturedTeal) Color.White.copy(alpha = 0.80f) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = studyQuestGlassAwareBorder(),
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isFeaturedTeal) Color.White.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badgeCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = titleColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Interactive 3D Preview Carousel Stage reserved for the Welcome screen.
 * Electric Lime focal highlight card + monochrome frosted flanking slides.
 */
@Composable
fun AuthWelcomePreviewCarouselStage(
    language: AppLanguage,
    reducedMotion: Boolean,
    showCarousel: Boolean,
    headerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val semantic = LocalStudyQuestColors.current
    val scope = rememberCoroutineScope()
    var userInteracted by remember { mutableStateOf(false) }

    val slides = remember(language, visualConfig.isDark) {
        listOf(
            AuthCarouselSlide(
                id = "books",
                eyebrow = StudyQuestStrings.t(language, "NCTB 2026 & 2027", "এনসিটিবি ২০২৬ ও ২০২৭"),
                title = StudyQuestStrings.t(language, "Classes 6–10 Textbooks", "৬ষ্ঠ–১০ম শ্রেণি পাঠ্যবই"),
                statusBadge = StudyQuestStrings.t(language, "Offline", "অফলাইন"),
                progress = 0.86f,
                icon = Icons.Filled.MenuBook,
                isLimeFocal = true
            ),
            AuthCarouselSlide(
                id = "practice",
                eyebrow = StudyQuestStrings.t(language, "BOARD PRACTICE", "বোর্ড প্রস্তুতি"),
                title = StudyQuestStrings.t(language, "MCQ, CQ & Mock Exams", "MCQ, CQ ও মডেল টেস্ট"),
                statusBadge = "MCQ • CQ",
                progress = 0.78f,
                icon = Icons.Filled.Quiz,
                isLimeFocal = false
            ),
            AuthCarouselSlide(
                id = "ai_tutor",
                eyebrow = StudyQuestStrings.t(language, "STUDY COMPANION", "স্মার্ট সহায়ক"),
                title = StudyQuestStrings.t(language, "Step-by-Step AI Tutor", "ধাপে ধাপে AI সমাধান"),
                statusBadge = "BN • EN",
                progress = 0.90f,
                icon = Icons.Filled.AutoAwesome,
                isLimeFocal = false
            ),
            AuthCarouselSlide(
                id = "focus_sync",
                eyebrow = StudyQuestStrings.t(language, "CLOUD SYNC", "ক্লাউড সিঙ্ক"),
                title = StudyQuestStrings.t(language, "Badges, Streaks & Sync", "ব্যাজ, স্ট্রিক ও সিঙ্ক"),
                statusBadge = StudyQuestStrings.t(language, "Synced", "সুরক্ষিত"),
                progress = 0.82f,
                icon = Icons.Filled.CloudDone,
                isLimeFocal = false
            )
        )
    }

    val authCanonicalBase = slides.size * 100
    val authVirtualCount = slides.size * 200
    val pagerState = rememberPagerState(initialPage = authCanonicalBase, pageCount = { authVirtualCount })

    LaunchedEffect(showCarousel, reducedMotion, userInteracted, pagerState.isScrollInProgress) {
        if (showCarousel && !reducedMotion && !userInteracted && !pagerState.isScrollInProgress) {
            while (true) {
                delay(3800L)
                val next = (pagerState.currentPage + 1).coerceAtMost(authVirtualCount - 1)
                pagerState.animateScrollToPage(
                    page = next,
                    animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val bandColors = if (visualConfig.isDark) {
        listOf(DarkHeroSlateTop, DarkHeroSlateBottom)
    } else {
        listOf(HeroSlateBlueTop, HeroSlateBlueBottom)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(bandColors))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_auth_library_stage),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (visualConfig.isDark) 0.22f else 0.30f }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            bandColors.first().copy(alpha = 0.58f),
                            bandColors.last().copy(alpha = 0.82f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 980.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                headerContent()
            }

            if (showCarousel) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val isWide = maxWidth >= 600.dp
                    val cardWidth = if (isWide) 320.dp else (maxWidth - 88.dp).coerceIn(228.dp, 304.dp)
                    val sidePadding = ((maxWidth - cardWidth) / 2f).coerceAtLeast(24.dp)

                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = if (visualConfig.isDark) Color.White.copy(alpha = 0.08f) else HeroGlassStageLight,
                        border = BorderStroke(
                            1.dp,
                            if (visualConfig.isDark) Color.White.copy(alpha = 0.16f) else HeroGlassBorderLight
                        ),
                        modifier = Modifier
                            .widthIn(max = if (isWide) 476.dp else maxWidth - 32.dp)
                            .fillMaxWidth()
                            .height(174.dp)
                    ) {}

                    HorizontalPager(
                        state = pagerState,
                        pageSize = PageSize.Fixed(cardWidth),
                        contentPadding = PaddingValues(horizontal = sidePadding),
                        pageSpacing = 12.dp,
                        beyondViewportPageCount = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_preview_carousel")
                    ) { page ->
                        val slide = slides[page % slides.size]
                        val isCanonical = page in authCanonicalBase until (authCanonicalBase + slides.size)
                        val rawOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val absOffset = rawOffset.absoluteValue.coerceIn(0f, 1.4f)
                        val scale = lerp(1f, 0.85f, absOffset.coerceAtMost(1f))
                        val alpha = lerp(1f, 0.72f, absOffset.coerceAtMost(1f))
                        val tiltY = if (reducedMotion) 0f else (rawOffset * 14f).coerceIn(-16f, 16f)

                        val slideGradient = if (slide.isLimeFocal) {
                            listOf(semantic.heroLimeStart, semantic.heroLimeEnd)
                        } else if (visualConfig.isDark) {
                            listOf(Color(0xFF1E2634), Color(0xFF161C27))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                        }
                        val inkColor = when {
                            slide.isLimeFocal -> semantic.onHeroLime
                            visualConfig.isDark -> Color(0xFFF8FAFC)
                            else -> Color(0xFF0F172A)
                        }

                        Surface(
                            onClick = {
                                userInteracted = true
                                scope.launch { pagerState.animateScrollToPage(page) }
                            },
                            shape = RoundedCornerShape(22.dp),
                            color = Color.Transparent,
                            shadowElevation = if (absOffset < 0.25f) 8.dp else 1.dp,
                            border = BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = if (absOffset < 0.25f) 0.55f else 0.20f)
                            ),
                            modifier = Modifier
                                .width(cardWidth)
                                .height(154.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    rotationY = tiltY
                                    cameraDistance = 11f * density
                                }
                                .let { m -> if (isCanonical) m.testTag("auth_carousel_card_${slide.id}") else m }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(slideGradient))
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = inkColor.copy(alpha = 0.09f),
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = slide.icon,
                                                        contentDescription = null,
                                                        tint = inkColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = slide.eyebrow,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = inkColor.copy(alpha = 0.72f)
                                            )
                                        }
                                        Text(
                                            text = slide.statusBadge,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = inkColor.copy(alpha = 0.80f)
                                        )
                                    }

                                    Text(
                                        text = slide.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = inkColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    LinearProgressIndicator(
                                        progress = { slide.progress },
                                        color = inkColor,
                                        trackColor = inkColor.copy(alpha = 0.16f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .widthIn(max = if (isWide) 510.dp else maxWidth - 16.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val canPrev = pagerState.currentPage > 0
                        val canNext = pagerState.currentPage < authVirtualCount - 1
                        CarouselGlassArrowButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = StudyQuestStrings.t(language, "Previous preview card", "আগের কার্ড"),
                            enabled = canPrev,
                            testTag = "auth_carousel_prev_btn",
                            onClick = {
                                userInteracted = true
                                scope.launch {
                                    pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                                }
                            }
                        )
                        CarouselGlassArrowButton(
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = StudyQuestStrings.t(language, "Next preview card", "পরের কার্ড"),
                            enabled = canNext,
                            testTag = "auth_carousel_next_btn",
                            onClick = {
                                userInteracted = true
                                scope.launch {
                                    pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(authVirtualCount - 1))
                                }
                            }
                        )
                    }
                }

                val activeLogicalSlide = pagerState.currentPage % slides.size
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.forEachIndexed { idx, _ ->
                        val active = activeLogicalSlide == idx
                        val w by animateDpAsState(
                            targetValue = if (active) 20.dp else 7.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                            label = "auth_dot_w"
                        )
                        Box(
                            modifier = Modifier
                                .height(7.dp)
                                .width(w)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.38f))
                                .clickable {
                                    userInteracted = true
                                    val cycleStart = pagerState.currentPage - activeLogicalSlide
                                    scope.launch { pagerState.animateScrollToPage(cycleStart + idx) }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarouselGlassArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (enabled) 0.48f else 0.18f)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(38.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = if (enabled) 1f else 0.40f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
