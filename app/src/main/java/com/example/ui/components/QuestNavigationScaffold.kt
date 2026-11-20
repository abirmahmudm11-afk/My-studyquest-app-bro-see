package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.SoftPowderBlue
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor

data class QuestNavDestinationItem(
    val index: Int,
    val label: String,
    val subtitle: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

@Composable
fun rememberQuestDestinations(language: AppLanguage): List<QuestNavDestinationItem> {
    return remember(language) {
        listOf(
            QuestNavDestinationItem(
                index = 0,
                label = StudyQuestStrings.tabToday(language),
                subtitle = StudyQuestStrings.t(language, "Daily overview & recent study", "দৈনিক সারসংক্ষেপ ও সাম্প্রতিক পড়া"),
                activeIcon = Icons.Filled.Home,
                inactiveIcon = Icons.Outlined.Home
            ),
            QuestNavDestinationItem(
                index = 1,
                label = StudyQuestStrings.tabLearn(language),
                subtitle = StudyQuestStrings.t(language, "Courses, NCTB books & practice", "পাঠ্যবই, বিষয় ও অনুশীলন"),
                activeIcon = Icons.AutoMirrored.Filled.MenuBook,
                inactiveIcon = Icons.AutoMirrored.Outlined.MenuBook
            ),
            QuestNavDestinationItem(
                index = 2,
                label = StudyQuestStrings.tabCompete(language),
                subtitle = StudyQuestStrings.t(language, "Timed sprints & class rankings", "সময়ভিত্তিক পরীক্ষা ও র‌্যাংকিং"),
                activeIcon = Icons.Filled.EmojiEvents,
                inactiveIcon = Icons.Outlined.EmojiEvents
            ),
            QuestNavDestinationItem(
                index = 3,
                label = StudyQuestStrings.tabFriends(language),
                subtitle = StudyQuestStrings.t(language, "Study circles, peers & chat", "স্টাডি সার্কেল, বন্ধু ও চ্যাট"),
                activeIcon = Icons.Filled.Groups,
                inactiveIcon = Icons.Outlined.Groups
            ),
            QuestNavDestinationItem(
                index = 4,
                label = StudyQuestStrings.tabProfile(language),
                subtitle = StudyQuestStrings.t(language, "Analytics, offline packs & settings", "বিশ্লেষণ, অফলাইন প্যাক ও সেটিংস"),
                activeIcon = Icons.Filled.Person,
                inactiveIcon = Icons.Outlined.Person
            )
        )
    }
}

@Composable
fun QuestNavigationScaffold(
    selectedTab: Int,
    language: AppLanguage,
    profileDisplayName: String,
    profileAvatarId: String,
    profileFrameId: String?,
    totalXp: Long,
    streakCount: Long,
    soundEnabled: Boolean,
    reducedMotion: Boolean,
    onSelectTab: (Int) -> Unit,
    adBannerSlot: (@Composable () -> Unit)? = null,
    topBannerSlot: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val destinations = rememberQuestDestinations(language)
    val activeDest = destinations.getOrNull(selectedTab) ?: destinations.first()
    var showQuickNavigator by remember { mutableStateOf(false) }

    BackHandler(enabled = showQuickNavigator) {
        showQuickNavigator = false
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                if (isWideScreen) {
                    CompactDesktopTopNavBar(
                        destinations = destinations,
                        selectedTab = selectedTab,
                        language = language,
                        profileDisplayName = profileDisplayName,
                        profileAvatarId = profileAvatarId,
                        profileFrameId = profileFrameId,
                        onSelectTab = { idx ->
                            AudioFeedbackHelper.triggerClick(view, soundEnabled, reducedMotion)
                            onSelectTab(idx)
                        }
                    )
                } else {
                    QuestAppHeader(
                        activeDest = activeDest,
                        language = language,
                        profileDisplayName = profileDisplayName,
                        profileAvatarId = profileAvatarId,
                        profileFrameId = profileFrameId,
                        onProfileClick = {
                            AudioFeedbackHelper.triggerClick(view, soundEnabled, reducedMotion)
                            onSelectTab(4)
                        },
                        onOpenQuestMap = {
                            AudioFeedbackHelper.triggerClick(view, soundEnabled, reducedMotion)
                            showQuickNavigator = true
                        }
                    )
                }
            },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (adBannerSlot != null) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            adBannerSlot()
                        }
                    }
                    if (!isWideScreen) {
                        Surface(
                            color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                            tonalElevation = 0.dp,
                            shadowElevation = if (visualConfig.isGlass) 0.dp else 4.dp
                        ) {
                            Column {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    tonalElevation = 0.dp,
                                    windowInsets = WindowInsets.navigationBars
                                ) {
                                    destinations.forEach { dest ->
                                        val selected = selectedTab == dest.index
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = {
                                                AudioFeedbackHelper.triggerClick(view, soundEnabled, reducedMotion)
                                                onSelectTab(dest.index)
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) dest.activeIcon else dest.inactiveIcon,
                                                    contentDescription = dest.label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = dest.label,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.testTag("nav_tab_${dest.index}")
                                        )
                                    }
                                }
                            }
                        }
                    } else if (adBannerSlot == null) {
                        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (topBannerSlot != null) {
                    topBannerSlot()
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }

        if (!isWideScreen) {
            QuestMapNavigationOverlay(
                visible = showQuickNavigator,
                destinations = destinations,
                selectedTab = selectedTab,
                language = language,
                reducedMotion = reducedMotion,
                onDismiss = { showQuickNavigator = false },
                onSelectDestination = { idx ->
                    AudioFeedbackHelper.triggerClick(view, soundEnabled, reducedMotion)
                    showQuickNavigator = false
                    onSelectTab(idx)
                }
            )
        }
    }
}

/**
 * Reference-inspired compact centered horizontal top navigation bar for desktop & tablet widths.
 */
@Composable
private fun CompactDesktopTopNavBar(
    destinations: List<QuestNavDestinationItem>,
    selectedTab: Int,
    language: AppLanguage,
    profileDisplayName: String,
    profileAvatarId: String,
    profileFrameId: String?,
    onSelectTab: (Int) -> Unit
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    Surface(
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quest_navigation_rail")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StudyQuestBrandMark(size = 30.dp, ringColor = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "StudyQuest",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Centered crisp horizontal tab links matching the reference image
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    destinations.forEach { dest ->
                        val isSelected = selectedTab == dest.index
                        Surface(
                            onClick = { onSelectTab(dest.index) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSelected) {
                                if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftPowderBlue
                            } else {
                                Color.Transparent
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("nav_tab_${dest.index}")
                        ) {
                            Text(
                                text = dest.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .clickable { onSelectTab(4) }
                            .testTag("header_profile_shortcut"),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarBadgeView(
                            avatarId = profileAvatarId,
                            frameId = profileFrameId,
                            displayName = profileDisplayName.ifBlank { "S" },
                            size = 34.dp
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun QuestAppHeader(
    activeDest: QuestNavDestinationItem,
    language: AppLanguage,
    profileDisplayName: String,
    profileAvatarId: String,
    profileFrameId: String?,
    onProfileClick: () -> Unit,
    onOpenQuestMap: () -> Unit
) {
    Surface(
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.background),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quest_app_header")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activeDest.label,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onOpenQuestMap,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("open_quest_map_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Explore,
                            contentDescription = StudyQuestStrings.t(language, "Browse sections", "বিভাগসমূহ"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .clickable(onClick = onProfileClick)
                            .testTag("header_profile_shortcut"),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarBadgeView(
                            avatarId = profileAvatarId,
                            frameId = profileFrameId,
                            displayName = profileDisplayName.ifBlank { "S" },
                            size = 32.dp
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun QuestMapNavigationOverlay(
    visible: Boolean,
    destinations: List<QuestNavDestinationItem>,
    selectedTab: Int,
    language: AppLanguage,
    reducedMotion: Boolean,
    onDismiss: () -> Unit,
    onSelectDestination: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = if (reducedMotion) fadeIn(tween(10)) else fadeIn(tween(200)) + scaleIn(
            initialScale = 0.96f,
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        ),
        exit = if (reducedMotion) fadeOut(tween(10)) else fadeOut(tween(160)) + scaleOut(
            targetScale = 0.96f,
            animationSpec = tween(160)
        )
    ) {
        val visualConfig = LocalStudyQuestSurfaceStyle.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("quest_map_overlay"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StudyQuestStrings.t(language, "WORKSPACE DIRECTORY", "অ্যাপ বিভাগসমূহ"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = StudyQuestStrings.t(language, "Study Sections", "শেখার বিভাগসমূহ"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_quest_map_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = StudyQuestStrings.t(language, "Close", "বন্ধ করুন")
                        )
                    }
                }

                val connectorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                Column(modifier = Modifier.fillMaxWidth()) {
                    destinations.forEachIndexed { idx, dest ->
                        val isActive = selectedTab == dest.index
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(38.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isActive) BrandTealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = if (isActive) BrandTealPrimary else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = StudyQuestStrings.formatDigits(idx + 1, language),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                if (idx < destinations.lastIndex) {
                                    Canvas(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(36.dp)
                                    ) {
                                        drawLine(
                                            color = connectorColor,
                                            start = Offset(size.width / 2f, 4.dp.toPx()),
                                            end = Offset(size.width / 2f, size.height - 4.dp.toPx()),
                                            strokeWidth = 2.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Surface(
                                onClick = { onSelectDestination(dest.index) },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isActive) {
                                    if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftPowderBlue
                                } else {
                                    studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface)
                                },
                                border = if (isActive) {
                                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    studyQuestGlassAwareBorder()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = if (idx < destinations.lastIndex) 10.dp else 0.dp)
                                    .minimumInteractiveComponentSize()
                                    .testTag("map_nav_tab_${dest.index}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isActive) dest.activeIcon else dest.inactiveIcon,
                                                contentDescription = dest.label,
                                                tint = if (isActive) Color.White else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dest.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = dest.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
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
