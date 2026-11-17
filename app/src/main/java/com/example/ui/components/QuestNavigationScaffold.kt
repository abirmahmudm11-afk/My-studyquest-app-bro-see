package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.ui.theme.BrandForestGreen
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.QuestGold
import com.example.ui.theme.SoftSageGreen
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
                subtitle = StudyQuestStrings.t(language, "Personal Quest HQ & daily focus", "ব্যক্তিগত কোয়েস্ট ও দৈনিক পরিকল্পনা"),
                activeIcon = Icons.Filled.Home,
                inactiveIcon = Icons.Outlined.Home
            ),
            QuestNavDestinationItem(
                index = 1,
                label = StudyQuestStrings.tabLearn(language),
                subtitle = StudyQuestStrings.t(language, "World of Knowledge & NCTB Reader", "শেখার বিশ্ব ও এনসিটিবি বই"),
                activeIcon = Icons.AutoMirrored.Filled.MenuBook,
                inactiveIcon = Icons.AutoMirrored.Outlined.MenuBook
            ),
            QuestNavDestinationItem(
                index = 2,
                label = StudyQuestStrings.tabCompete(language),
                subtitle = StudyQuestStrings.t(language, "Arenas, tournaments & speed sprints", "প্রতিযোগিতা, টুর্নামেন্ট ও স্প্রিন্ট"),
                activeIcon = Icons.Filled.EmojiEvents,
                inactiveIcon = Icons.Outlined.EmojiEvents
            ),
            QuestNavDestinationItem(
                index = 3,
                label = StudyQuestStrings.tabFriends(language),
                subtitle = StudyQuestStrings.t(language, "Study Circle, friends & class chat", "স্টাডি সার্কেল, বন্ধু ও চ্যাট"),
                activeIcon = Icons.Filled.Groups,
                inactiveIcon = Icons.Outlined.Groups
            ),
            QuestNavDestinationItem(
                index = 4,
                label = StudyQuestStrings.tabProfile(language),
                subtitle = StudyQuestStrings.t(language, "Mastery, badges, offline packs & tools", "অগ্রগতি, ব্যাজ, অফলাইন ও সেটিংস"),
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
    val destinations = rememberQuestDestinations(language)
    val activeDest = destinations.getOrNull(selectedTab) ?: destinations.first()
    var showQuestMap by remember { mutableStateOf(false) }
    var railExpanded by remember { mutableStateOf(true) }

    BackHandler(enabled = showQuestMap) {
        showQuestMap = false
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                QuestNavigationRail(
                    destinations = destinations,
                    selectedTab = selectedTab,
                    expanded = railExpanded,
                    language = language,
                    profileDisplayName = profileDisplayName,
                    profileAvatarId = profileAvatarId,
                    profileFrameId = profileFrameId,
                    reducedMotion = reducedMotion,
                    onToggleExpanded = { railExpanded = !railExpanded },
                    onSelectTab = { idx ->
                        AudioFeedbackHelper.triggerClick(view, soundEnabled, reducedMotion)
                        onSelectTab(idx)
                    }
                )
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = Color.Transparent,
                topBar = {
                    if (!isWideScreen) {
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
                                showQuestMap = true
                            }
                        )
                    }
                },
                bottomBar = {
                    if (adBannerSlot != null) {
                        Box(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                            adBannerSlot()
                        }
                    } else {
                        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
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
        }

        if (!isWideScreen) {
            QuestMapNavigationOverlay(
                visible = showQuestMap,
                destinations = destinations,
                selectedTab = selectedTab,
                language = language,
                totalXp = totalXp,
                streakCount = streakCount,
                reducedMotion = reducedMotion,
                onDismiss = { showQuestMap = false },
                onSelectDestination = { idx ->
                    AudioFeedbackHelper.triggerClick(view, soundEnabled, reducedMotion)
                    showQuestMap = false
                    onSelectTab(idx)
                }
            )
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
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    Surface(
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.background.copy(alpha = 0.94f)),
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    StudyQuestBrandMark(
                        size = 34.dp,
                        ringColor = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STUDY QUEST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Text(
                            text = activeDest.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onOpenQuestMap,
                        shape = RoundedCornerShape(999.dp),
                        color = if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftSageGreen,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("open_quest_map_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = StudyQuestStrings.t(language, "Quest Map", "কোয়েস্ট ম্যাপ"),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = StudyQuestStrings.t(language, "Quest Map", "ম্যাপ"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
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
                            size = 36.dp
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
    totalXp: Long,
    streakCount: Long,
    reducedMotion: Boolean,
    onDismiss: () -> Unit,
    onSelectDestination: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = if (reducedMotion) fadeIn(tween(10)) else fadeIn(tween(200)) + scaleIn(
            initialScale = 0.96f,
            animationSpec = tween(240, easing = FastOutSlowInEasing)
        ),
        exit = if (reducedMotion) fadeOut(tween(10)) else fadeOut(tween(180)) + scaleOut(
            targetScale = 0.96f,
            animationSpec = tween(180)
        )
    ) {
        val visualConfig = LocalStudyQuestSurfaceStyle.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("quest_map_overlay"),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = StudyQuestStrings.t(language, "STUDY QUEST WORLD", "স্টাডি কোয়েস্ট বিশ্ব"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = StudyQuestStrings.t(language, "Quest Map", "কোয়েস্ট ম্যাপ"),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
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
                                contentDescription = StudyQuestStrings.t(language, "Close Quest Map", "ম্যাপ বন্ধ করুন")
                            )
                        }
                    }

                    // Concise Telemetry Ribbon inside Quest Map
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (visualConfig.isDark) MaterialTheme.colorScheme.tertiaryContainer else Color(0xFFFAF1DD),
                        border = BorderStroke(1.dp, QuestGold.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Choose your next stop along the path",
                                    "আপনার পরবর্তী গন্তব্য নির্বাচন করুন"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "${StudyQuestStrings.formatDigits(totalXp, language)} XP • ${StudyQuestStrings.formatDigits(streakCount, language)}d",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Connected Milestone Route Nodes
                    val connectorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    val goldColor = QuestGold
                    Column(modifier = Modifier.fillMaxWidth()) {
                        destinations.forEachIndexed { idx, dest ->
                            val isActive = selectedTab == dest.index
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Left Milestone Track + Dotted Path
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(44.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            isActive -> MaterialTheme.colorScheme.primary
                                            idx < selectedTab -> goldColor.copy(alpha = 0.25f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        border = BorderStroke(
                                            width = if (isActive) 2.dp else 1.dp,
                                            color = if (isActive) goldColor else MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = StudyQuestStrings.formatDigits(idx + 1, language),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    if (idx < destinations.lastIndex) {
                                        Canvas(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(42.dp)
                                        ) {
                                            drawLine(
                                                color = if (idx < selectedTab) goldColor else connectorColor,
                                                start = Offset(size.width / 2f, 4.dp.toPx()),
                                                end = Offset(size.width / 2f, size.height - 4.dp.toPx()),
                                                strokeWidth = 2.5.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                pathEffect = if (idx < selectedTab) null else PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                // Milestone Stop Tile
                                Surface(
                                    onClick = { onSelectDestination(dest.index) },
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isActive) {
                                        if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftSageGreen
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
                                        .padding(bottom = if (idx < destinations.lastIndex) 12.dp else 0.dp)
                                        .minimumInteractiveComponentSize()
                                        .testTag("nav_tab_${dest.index}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isActive) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            modifier = Modifier.size(40.dp)
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
                                        if (isActive) {
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = StudyQuestStrings.t(language, "Here", "এখানে"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestNavigationRail(
    destinations: List<QuestNavDestinationItem>,
    selectedTab: Int,
    expanded: Boolean,
    language: AppLanguage,
    profileDisplayName: String,
    profileAvatarId: String,
    profileFrameId: String?,
    reducedMotion: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectTab: (Int) -> Unit
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    val railWidth by animateDpAsState(
        targetValue = if (expanded) 224.dp else 80.dp,
        animationSpec = tween(if (reducedMotion) 10 else 220, easing = FastOutSlowInEasing),
        label = "quest_rail_width"
    )

    Surface(
        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
        tonalElevation = 0.dp,
        modifier = Modifier
            .width(railWidth)
            .fillMaxHeight()
            .testTag("quest_navigation_rail")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (expanded) Arrangement.SpaceBetween else Arrangement.Center
            ) {
                if (expanded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudyQuestBrandMark(size = 32.dp)
                        Text(
                            text = "StudyQuest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
                IconButton(
                    onClick = onToggleExpanded,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("quest_rail_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Filled.Menu,
                        contentDescription = StudyQuestStrings.t(
                            language,
                            if (expanded) "Collapse navigation" else "Expand navigation",
                            if (expanded) "মেনু সংকুচিত করো" else "মেনু প্রসারিত করো"
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            destinations.forEach { dest ->
                val isSelected = selectedTab == dest.index
                val pillColor = if (isSelected) {
                    if (visualConfig.isDark) MaterialTheme.colorScheme.primaryContainer else SoftSageGreen
                } else {
                    Color.Transparent
                }
                val itemContent: @Composable () -> Unit = {
                    Surface(
                        onClick = { onSelectTab(dest.index) },
                        shape = RoundedCornerShape(14.dp),
                        color = pillColor,
                        border = if (isSelected) BorderStroke(1.dp, BrandForestGreen.copy(alpha = 0.30f)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("nav_tab_${dest.index}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = if (expanded) 12.dp else 0.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) dest.activeIcon else dest.inactiveIcon,
                                contentDescription = dest.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            if (expanded) {
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = dest.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (expanded) {
                    itemContent()
                } else {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(dest.label, textAlign = TextAlign.Center) } },
                        state = rememberTooltipState()
                    ) {
                        itemContent()
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Surface(
                onClick = { onSelectTab(4) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("rail_profile_card")
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center
                ) {
                    AvatarBadgeView(
                        avatarId = profileAvatarId,
                        frameId = profileFrameId,
                        displayName = profileDisplayName.ifBlank { "S" },
                        size = 34.dp
                    )
                    if (expanded) {
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profileDisplayName.ifBlank { StudyQuestStrings.t(language, "Scholar", "শিক্ষার্থী") },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = StudyQuestStrings.t(language, "Profile & Packs", "প্রোফাইল ও প্যাক"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
