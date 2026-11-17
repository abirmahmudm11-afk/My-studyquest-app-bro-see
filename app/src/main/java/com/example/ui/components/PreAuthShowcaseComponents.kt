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
import androidx.compose.material.icons.filled.Timer
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
import com.example.ui.theme.DeepSlateInk
import com.example.ui.theme.HeroGlassBorderLight
import com.example.ui.theme.HeroGlassStageLight
import com.example.ui.theme.HeroMintCardEnd
import com.example.ui.theme.HeroMintCardStart
import com.example.ui.theme.HeroSlateBlueBottom
import com.example.ui.theme.HeroSlateBlueTop
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
    val subtitleEn: String,
    val subtitleBn: String,
    val statChip: String,
    val progress: Float,
    val icon: ImageVector,
    val gradientLight: List<Color>,
    val gradientDark: List<Color>,
    val onCardLight: Color,
    val onCardDark: Color
)

private data class AuthCarouselSlide(
    val id: String,
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val statusBadge: String,
    val statusSub: String,
    val progress: Float,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val onCardColor: Color
)

/**
 * Live-animated Launch Hero Showcase for the very first Language Selection screen.
 * Combines custom architectural educational artwork, an atmospheric cool slate blue-gray backdrop,
 * and live-cycling 3D purpose cards that explain StudyQuest's educational value.
 */
@Composable
fun LaunchStudyPortalHeroShowcase(
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    compactMode: Boolean = false
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val scope = rememberCoroutineScope()
    var userPaused by remember { mutableStateOf(false) }

    val pillars = remember {
        listOf(
            LaunchValuePillar(
                id = "nctb",
                badge = "NCTB 2026 & 2027",
                titleEn = "All NCTB Textbooks Offline",
                titleBn = "সব এনসিটিবি পাঠ্যবই অফলাইনে",
                subtitleEn = "Read Classes 6–10 & SSC chapters cleanly anytime without internet",
                subtitleBn = "৬ষ্ঠ–১০ম শ্রেণি ও এসএসসি অধ্যায় পড়ুন যেকোনো সময়, ইন্টারনেট ছাড়াই",
                statChip = "Classes 6–10",
                progress = 0.88f,
                icon = Icons.Filled.MenuBook,
                gradientLight = listOf(HeroMintCardStart, HeroMintCardEnd),
                gradientDark = listOf(Color(0xFF1D4F4C), Color(0xFF153B3B)),
                onCardLight = DeepSlateInk,
                onCardDark = Color(0xFFEAF7F5)
            ),
            LaunchValuePillar(
                id = "prep",
                badge = "BOARD PRACTICE",
                titleEn = "MCQ, CQ & SSC Mock Exams",
                titleBn = "বোর্ড স্ট্যান্ডার্ড MCQ, CQ ও পরীক্ষা",
                subtitleEn = "Chapter-wise practice sets with instant rubric & accuracy insights",
                subtitleBn = "অধ্যায়ভিত্তিক অনুশীলন ও তাৎক্ষণিক ফলাফল বিশ্লেষণ",
                statChip = "SSC Ready",
                progress = 0.76f,
                icon = Icons.Filled.Quiz,
                gradientLight = listOf(Color(0xFFDCEAF8), Color(0xFFBED5ED)),
                gradientDark = listOf(Color(0xFF213850), Color(0xFF17283B)),
                onCardLight = DeepSlateInk,
                onCardDark = Color(0xFFEAF2FA)
            ),
            LaunchValuePillar(
                id = "ai",
                badge = "BILINGUAL AI TUTOR",
                titleEn = "Clear Step-by-Step Explanations",
                titleBn = "বাংলা ও ইংরেজিতে সহজ AI সমাধান",
                subtitleEn = "Doubt solver grounded in your locked active textbook syllabus",
                subtitleBn = "আপনার পাঠ্যবইয়ের সিলেবাস অনুযায়ী ধাপে ধাপে সহজ ব্যাখ্যা",
                statChip = "BN • EN",
                progress = 0.92f,
                icon = Icons.Filled.AutoAwesome,
                gradientLight = listOf(Color(0xFFD6EEF0), Color(0xFFB3DCE0)),
                gradientDark = listOf(Color(0xFF1A434B), Color(0xFF133138)),
                onCardLight = DeepSlateInk,
                onCardDark = Color(0xFFE7F6F8)
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pillars.size })

    LaunchedEffect(reducedMotion, userPaused, pagerState.isScrollInProgress) {
        if (!reducedMotion && !userPaused && !pagerState.isScrollInProgress) {
            while (true) {
                delay(3400L)
                val next = (pagerState.currentPage + 1) % pillars.size
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
            initialValue = -3.5f,
            targetValue = 3.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
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
        // Custom architectural artwork subtly blended into the cool slate atmosphere
        Image(
            painter = painterResource(id = R.drawable.img_launch_study_portal),
            contentDescription = "StudyQuest study workspace illustration",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (visualConfig.isDark) 0.24f else 0.34f }
        )

        // Soft atmospheric scrim so typography and floating glass stay sharp
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            bandColors.first().copy(alpha = 0.62f),
                            bandColors.last().copy(alpha = 0.78f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (compactMode) Modifier.padding(top = 12.dp, bottom = 26.dp) else Modifier.statusBarsPadding().padding(top = 12.dp, bottom = 32.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Editorial Brand Strip
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
                    StudyQuestBrandMark(size = 36.dp)
                    Column {
                        Text(
                            text = "StudyQuest",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Bangladesh Digital Study Workspace",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.80f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "NCTB 2026–27",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Live Floating Value-Pillar 3D Pager inside Frosted Frame
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val isWide = maxWidth >= 600.dp
                val cardWidth = if (isWide) 356.dp else (maxWidth - 74.dp).coerceIn(248.dp, 336.dp)
                val sidePadding = ((maxWidth - cardWidth) / 2f).coerceAtLeast(20.dp)

                // Back frosted glass frame anchored on the hero band
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = if (visualConfig.isDark) Color.White.copy(alpha = 0.08f) else HeroGlassStageLight,
                    border = BorderStroke(
                        1.dp,
                        if (visualConfig.isDark) Color.White.copy(alpha = 0.18f) else HeroGlassBorderLight
                    ),
                    modifier = Modifier
                        .widthIn(max = if (isWide) 490.dp else maxWidth - 28.dp)
                        .fillMaxWidth()
                        .height(198.dp)
                ) {}

                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fixed(cardWidth),
                    contentPadding = PaddingValues(horizontal = sidePadding),
                    pageSpacing = 14.dp,
                    beyondViewportPageCount = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("launch_live_pillar_pager")
                ) { page ->
                    val pillar = pillars[page]
                    val rawOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absOffset = rawOffset.absoluteValue.coerceIn(0f, 1.4f)
                    val scale = lerp(1f, 0.87f, absOffset.coerceAtMost(1f))
                    val alpha = lerp(1f, 0.72f, absOffset.coerceAtMost(1f))
                    val tiltY = if (reducedMotion) 0f else (rawOffset * 10f).coerceIn(-12f, 12f)
                    val bobPx = if (absOffset < 0.25f) floatOffsetDp else 0f
                    val gradient = if (visualConfig.isDark) pillar.gradientDark else pillar.gradientLight
                    val inkColor = if (visualConfig.isDark) pillar.onCardDark else pillar.onCardLight

                    Surface(
                        onClick = {
                            userPaused = true
                            scope.launch { pagerState.animateScrollToPage(page) }
                        },
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        shadowElevation = if (absOffset < 0.25f) 10.dp else 2.dp,
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = if (absOffset < 0.25f) 0.58f else 0.25f)
                        ),
                        modifier = Modifier
                            .width(cardWidth)
                            .height(178.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                rotationY = tiltY
                                translationY = bobPx * density
                                cameraDistance = 12f * density
                            }
                            .testTag("launch_pillar_card_${pillar.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(gradient))
                                .padding(16.dp)
                        ) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.16f),
                                    radius = size.height * 0.65f,
                                    center = Offset(size.width * 0.88f, size.height * 0.16f)
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = inkColor.copy(alpha = 0.10f),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = pillar.icon,
                                                    contentDescription = null,
                                                    tint = inkColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = pillar.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = inkColor.copy(alpha = 0.78f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.White.copy(alpha = if (visualConfig.isDark) 0.16f else 0.48f),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f))
                                    ) {
                                        Text(
                                            text = pillar.statChip,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = inkColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = pillar.titleBn,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = inkColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = pillar.titleEn,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = inkColor.copy(alpha = 0.84f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${pillar.subtitleBn} • ${pillar.subtitleEn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = inkColor.copy(alpha = 0.78f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { pillar.progress },
                                    color = inkColor,
                                    trackColor = inkColor.copy(alpha = 0.18f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                )
                            }
                        }
                    }
                }
            }

            // Interactive animated pill indicators for cycling reasons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pillars.forEachIndexed { idx, pillar ->
                    val active = pagerState.currentPage == idx
                    val pillWidth by animateDpAsState(
                        targetValue = if (active) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                        label = "pillar_dot_width"
                    )
                    val pillColor by animateColorAsState(
                        targetValue = if (active) Color.White else Color.White.copy(alpha = 0.42f),
                        label = "pillar_dot_color"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(pillWidth)
                            .clip(RoundedCornerShape(999.dp))
                            .background(pillColor)
                            .clickable {
                                userPaused = true
                                scope.launch { pagerState.animateScrollToPage(idx) }
                            }
                            .testTag("launch_pillar_dot_${pillar.id}")
                    )
                }
            }
        }
    }
}

/**
 * Tactile Spring-Animated Language Selection Card for Bangla & English.
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
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "lang_card_scale"
    )

    val gradientColors = if (isFeaturedTeal) {
        listOf(BrandTealPrimary, Color(0xFF1D666A))
    } else if (visualConfig.isDark) {
        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
    } else {
        listOf(Color.White.copy(alpha = 0.94f), Color(0xFFEAF2FA))
    }

    val titleColor = if (isFeaturedTeal) Color.White else MaterialTheme.colorScheme.onSurface
    val subColor = if (isFeaturedTeal) Color.White.copy(alpha = 0.86f) else MaterialTheme.colorScheme.onSurfaceVariant
    val borderStroke = if (isFeaturedTeal) {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.34f))
    } else {
        studyQuestGlassAwareBorder(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    }

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = borderStroke,
        shadowElevation = if (isFeaturedTeal) 6.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(gradientColors))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isFeaturedTeal) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badgeCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isFeaturedTeal) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isFeaturedTeal) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (isFeaturedTeal) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Interactive 3D Preview Carousel Stage for Welcome & Authentication.
 * Mirrors StudyQuest's signature slate blue-gray frosted glass stage with custom artwork,
 * flanking 3D cards, circular arrow controls, and zero XP/badge clutter.
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
    val scope = rememberCoroutineScope()
    var userInteracted by remember { mutableStateOf(false) }

    val slides = remember(language, visualConfig.isDark) {
        listOf(
            AuthCarouselSlide(
                id = "books",
                eyebrow = StudyQuestStrings.t(language, "NCTB 2026 & 2027", "এনসিটিবি ২০২৬ ও ২০২৭"),
                title = StudyQuestStrings.t(language, "Classes 6–10 & SSC Textbooks", "৬ষ্ঠ–১০ম শ্রেণি ও এসএসসি পাঠ্যবই"),
                subtitle = StudyQuestStrings.t(
                    language,
                    "Offline chapter reader, highlighted notes & board archive",
                    "অফলাইন অধ্যায় রিডার, নোট ও বোর্ড সংগ্রহশালা"
                ),
                statusBadge = StudyQuestStrings.t(language, "Offline", "অফলাইন"),
                statusSub = StudyQuestStrings.t(language, "All Classes", "সব শ্রেণি"),
                progress = 0.86f,
                icon = Icons.Filled.MenuBook,
                gradientColors = if (visualConfig.isDark) {
                    listOf(Color(0xFF1F4E4C), Color(0xFF16383A))
                } else {
                    listOf(HeroMintCardStart, HeroMintCardEnd)
                },
                onCardColor = if (visualConfig.isDark) Color(0xFFEAF7F5) else DeepSlateInk
            ),
            AuthCarouselSlide(
                id = "practice",
                eyebrow = StudyQuestStrings.t(language, "BOARD PREPARATION", "বোর্ড পরীক্ষা প্রস্তুতি"),
                title = StudyQuestStrings.t(language, "MCQ, CQ & Mock Exams", "MCQ, CQ ও মডেল টেস্ট"),
                subtitle = StudyQuestStrings.t(
                    language,
                    "Instant scoring, structured CQ rubrics & chapter review",
                    "তাৎক্ষণিক মূল্যায়ন, সৃজনশীল গাইড ও অধ্যায় রিভিউ"
                ),
                statusBadge = "MCQ • CQ",
                statusSub = StudyQuestStrings.t(language, "Auto Review", "সঠিক যাচাই"),
                progress = 0.78f,
                icon = Icons.Filled.Quiz,
                gradientColors = if (visualConfig.isDark) {
                    listOf(Color(0xFF21374F), Color(0xFF162739))
                } else {
                    listOf(Color(0xFFDAE8F6), Color(0xFFBCD3EC))
                },
                onCardColor = if (visualConfig.isDark) Color(0xFFEAF2FA) else DeepSlateInk
            ),
            AuthCarouselSlide(
                id = "ai_tutor",
                eyebrow = StudyQuestStrings.t(language, "STUDY COMPANION", "স্মার্ট পড়া সহায়ক"),
                title = StudyQuestStrings.t(language, "Step-by-Step AI Tutor", "ধাপে ধাপে AI সমাধান"),
                subtitle = StudyQuestStrings.t(
                    language,
                    "Guided Bangla & English help matched to your textbook",
                    "লককৃত পাঠ্যবই অনুযায়ী বাংলা ও ইংরেজিতে সহজ সমাধান"
                ),
                statusBadge = "BN • EN",
                statusSub = StudyQuestStrings.t(language, "24/7 Help", "যেকোনো সময়"),
                progress = 0.90f,
                icon = Icons.Filled.AutoAwesome,
                gradientColors = if (visualConfig.isDark) {
                    listOf(Color(0xFF1B454F), Color(0xFF13323A))
                } else {
                    listOf(Color(0xFFD5EFF1), Color(0xFFB1DDE2))
                },
                onCardColor = if (visualConfig.isDark) Color(0xFFEAF7F9) else DeepSlateInk
            ),
            AuthCarouselSlide(
                id = "focus_sync",
                eyebrow = StudyQuestStrings.t(language, "FOCUS & SYNC", "ফোকাস ও সিঙ্ক"),
                title = StudyQuestStrings.t(language, "Calm Timer & Cloud Sync", "ফোকাস টাইমার ও ক্লাউড সিঙ্ক"),
                subtitle = StudyQuestStrings.t(
                    language,
                    "Track study sessions & pick up seamlessly on any device",
                    "প্রতিদিনের পড়ার হিসাব ও সব ডিভাইসে নিরাপদ ব্যাকআপ"
                ),
                statusBadge = StudyQuestStrings.t(language, "Synced", "সুরক্ষিত"),
                statusSub = StudyQuestStrings.t(language, "Firebase", "ক্লাউড সিঙ্ক"),
                progress = 0.82f,
                icon = Icons.Filled.CloudDone,
                gradientColors = if (visualConfig.isDark) {
                    listOf(Color(0xFF28344B), Color(0xFF1B2538))
                } else {
                    listOf(Color(0xFFE2E9F4), Color(0xFFC8D5E8))
                },
                onCardColor = if (visualConfig.isDark) Color(0xFFEDF2FA) else DeepSlateInk
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { slides.size })

    LaunchedEffect(showCarousel, reducedMotion, userInteracted, pagerState.isScrollInProgress) {
        if (showCarousel && !reducedMotion && !userInteracted && !pagerState.isScrollInProgress) {
            while (true) {
                delay(3800L)
                val next = (pagerState.currentPage + 1) % slides.size
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
        // Custom architectural glass backdrop artwork
        Image(
            painter = painterResource(id = R.drawable.img_auth_library_stage),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (visualConfig.isDark) 0.25f else 0.36f }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            bandColors.first().copy(alpha = 0.56f),
                            bandColors.last().copy(alpha = 0.80f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = if (showCarousel) 34.dp else 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    val cardWidth = if (isWide) 316.dp else (maxWidth - 92.dp).coerceIn(228.dp, 304.dp)
                    val sidePadding = ((maxWidth - cardWidth) / 2f).coerceAtLeast(24.dp)

                    // Central frosted glass backdrop frame behind the active slide
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = if (visualConfig.isDark) Color.White.copy(alpha = 0.08f) else HeroGlassStageLight,
                        border = BorderStroke(
                            1.dp,
                            if (visualConfig.isDark) Color.White.copy(alpha = 0.18f) else HeroGlassBorderLight
                        ),
                        modifier = Modifier
                            .widthIn(max = if (isWide) 476.dp else maxWidth - 28.dp)
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {}

                    HorizontalPager(
                        state = pagerState,
                        pageSize = PageSize.Fixed(cardWidth),
                        contentPadding = PaddingValues(horizontal = sidePadding),
                        pageSpacing = 14.dp,
                        beyondViewportPageCount = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_preview_carousel")
                    ) { page ->
                        val slide = slides[page]
                        val rawOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val absOffset = rawOffset.absoluteValue.coerceIn(0f, 1.4f)
                        val scale = lerp(1f, 0.85f, absOffset.coerceAtMost(1f))
                        val alpha = lerp(1f, 0.70f, absOffset.coerceAtMost(1f))
                        val tiltY = if (reducedMotion) 0f else (rawOffset * 11f).coerceIn(-12f, 12f)

                        Surface(
                            onClick = {
                                userInteracted = true
                                scope.launch { pagerState.animateScrollToPage(page) }
                            },
                            shape = RoundedCornerShape(22.dp),
                            color = Color.Transparent,
                            shadowElevation = if (absOffset < 0.25f) 10.dp else 2.dp,
                            border = BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = if (absOffset < 0.25f) 0.56f else 0.24f)
                            ),
                            modifier = Modifier
                                .width(cardWidth)
                                .height(178.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    rotationY = tiltY
                                    cameraDistance = 12f * density
                                }
                                .testTag("auth_carousel_card_${slide.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(slide.gradientColors))
                                    .padding(16.dp)
                            ) {
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.15f),
                                        radius = size.height * 0.65f,
                                        center = Offset(size.width * 0.88f, size.height * 0.14f)
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = slide.onCardColor.copy(alpha = 0.12f),
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = slide.icon,
                                                        contentDescription = null,
                                                        tint = slide.onCardColor,
                                                        modifier = Modifier.size(17.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = slide.eyebrow,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = slide.onCardColor.copy(alpha = 0.78f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White.copy(alpha = if (visualConfig.isDark) 0.16f else 0.44f),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(
                                                    text = slide.statusBadge,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = slide.onCardColor
                                                )
                                                Text(
                                                    text = slide.statusSub,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = slide.onCardColor.copy(alpha = 0.75f),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = slide.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = slide.onCardColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = slide.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = slide.onCardColor.copy(alpha = 0.82f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    LinearProgressIndicator(
                                        progress = { slide.progress },
                                        color = slide.onCardColor,
                                        trackColor = slide.onCardColor.copy(alpha = 0.18f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                    )
                                }
                            }
                        }
                    }

                    // Subtle translucent Left/Right carousel navigation arrows
                    Row(
                        modifier = Modifier
                            .widthIn(max = if (isWide) 510.dp else maxWidth - 16.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val canPrev = pagerState.currentPage > 0
                        val canNext = pagerState.currentPage < slides.lastIndex
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
                                    pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(slides.lastIndex))
                                }
                            }
                        )
                    }
                }

                // Compact pager dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.forEachIndexed { idx, _ ->
                        val active = pagerState.currentPage == idx
                        val w by animateDpAsState(
                            targetValue = if (active) 22.dp else 7.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                            label = "auth_dot_w"
                        )
                        Box(
                            modifier = Modifier
                                .height(7.dp)
                                .width(w)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.42f))
                                .clickable {
                                    userInteracted = true
                                    scope.launch { pagerState.animateScrollToPage(idx) }
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
        color = if (enabled) Color.White.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (enabled) 0.50f else 0.18f)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(38.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = if (enabled) 1f else 0.42f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
