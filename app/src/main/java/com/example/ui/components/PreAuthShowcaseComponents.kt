package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.DarkHeroSlateBottom
import com.example.ui.theme.DarkHeroSlateTop
import com.example.ui.theme.HeroSlateBlueBottom
import com.example.ui.theme.HeroSlateBlueTop
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.studyQuestGlassAwareBorder

/**
 * Architectural hero stage for the Launch screen: full-bleed atmospheric study visual
 * fading cleanly into the surface canvas, leaving focus solely on the title and single CTA.
 */
@Composable
fun LaunchStudyPortalHeroShowcase(
    language: AppLanguage = AppLanguage.ENGLISH,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    headerTrailingSlot: (@Composable () -> Unit)? = null
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val bgColor = MaterialTheme.colorScheme.background
    val bandColors = if (visualConfig.isDark) {
        listOf(DarkHeroSlateTop, DarkHeroSlateBottom)
    } else {
        listOf(HeroSlateBlueTop, HeroSlateBlueBottom)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .background(Brush.verticalGradient(bandColors))
            .testTag("launch_hero_stage")
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_launch_study_portal),
            contentDescription = StudyQuestStrings.t(
                language,
                "StudyQuest editorial study portal illustration",
                "স্টাডিকোয়েস্ট পাঠ্যকক্ষ চিত্র"
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (visualConfig.isDark) 0.56f else 0.72f }
        )

        // Top readability scrim + seamless bottom dissolve into the page background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to bandColors.first().copy(alpha = 0.62f),
                            0.52f to bandColors.last().copy(alpha = 0.24f),
                            0.84f to bgColor.copy(alpha = 0.72f),
                            1.0f to bgColor
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .widthIn(max = 920.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudyQuestBrandMark(size = 34.dp)
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
 * Clean atmospheric library backdrop stage for the Welcome screen.
 * Keeps header navigation crisp and lets the single headline + single primary CTA command attention.
 */
@Composable
fun AuthWelcomePreviewCarouselStage(
    language: AppLanguage,
    reducedMotion: Boolean,
    showCarousel: Boolean = false,
    headerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val bgColor = MaterialTheme.colorScheme.background
    val bandColors = if (visualConfig.isDark) {
        listOf(DarkHeroSlateTop, DarkHeroSlateBottom)
    } else {
        listOf(HeroSlateBlueTop, HeroSlateBlueBottom)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(296.dp)
            .background(Brush.verticalGradient(bandColors))
            .testTag("auth_welcome_hero_stage")
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_auth_library_stage),
            contentDescription = StudyQuestStrings.t(
                language,
                "StudyQuest library desk illustration",
                "স্টাডিকোয়েস্ট পাঠাগার চিত্র"
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (visualConfig.isDark) 0.52f else 0.68f }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to bandColors.first().copy(alpha = 0.62f),
                            0.50f to bandColors.last().copy(alpha = 0.28f),
                            0.84f to bgColor.copy(alpha = 0.76f),
                            1.0f to bgColor
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .widthIn(max = 920.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            headerContent()
        }
    }
}
