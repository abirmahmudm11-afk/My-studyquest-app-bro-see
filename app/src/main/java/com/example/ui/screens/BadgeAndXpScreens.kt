package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.GamificationCatalog
import com.example.data.model.BadgeCategory
import com.example.data.model.BadgeRarity
import com.example.data.model.PublicProfile
import com.example.data.model.UserBadgeDoc
import com.example.data.model.XpTransactionDoc
import com.example.ui.components.CharcoalMilestoneCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.components.rarityAccentColor
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BadgesAndXpScreen(
    profile: PublicProfile,
    uiLanguage: AppLanguage,
    userBadges: List<UserBadgeDoc>,
    xpLedger: List<XpTransactionDoc>,
    isGuestMode: Boolean,
    onToggleFavorite: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Badges (30+), 1=Trophy Cabinet, 2=XP History & Levels
    var statusFilter by remember { mutableStateOf("all") } // "all", "unlocked", "locked"
    var categoryFilter by remember { mutableStateOf<BadgeCategory?>(null) }
    var rarityFilter by remember { mutableStateOf<BadgeRarity?>(null) }

    val semantic = LocalStudyQuestColors.current
    val badgeDocMap = remember(userBadges) { userBadges.associateBy { it.badgeId } }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("badges_xp_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = StudyQuestStrings.t(uiLanguage, "Badges, Trophies & Milestones", "ব্যাজ, ট্রফি ও মাইলফলক"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Badges (${GamificationCatalog.badgesCatalog.size})", "ব্যাজ")) },
                modifier = Modifier.testTag("tab_badges_gallery")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Trophies", "ট্রফি")) },
                modifier = Modifier.testTag("tab_trophies")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Milestones", "মাইলফলক")) },
                modifier = Modifier.testTag("tab_xp_ledger")
            )
        }

        when (selectedTab) {
            0 -> {
                val unlockedTotal = GamificationCatalog.badgesCatalog.count { badgeDocMap[it.id]?.unlocked == true }
                val filteredBadges = remember(statusFilter, categoryFilter, rarityFilter, badgeDocMap) {
                    GamificationCatalog.badgesCatalog.filter { def ->
                        val doc = badgeDocMap[def.id]
                        val unlocked = doc?.unlocked == true
                        val statusOk = when (statusFilter) {
                            "unlocked" -> unlocked
                            "locked" -> !unlocked
                            else -> true
                        }
                        val catOk = categoryFilter == null || def.category == categoryFilter
                        val rarOk = rarityFilter == null || def.rarity == rarityFilter
                        statusOk && catOk && rarOk
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        StudyQuestGradientHeroBanner(
                            eyebrow = StudyQuestStrings.t(uiLanguage, "BADGE COLLECTION", "ব্যাজ সংগ্রহশালা"),
                            title = StudyQuestStrings.t(
                                uiLanguage,
                                "$unlockedTotal / ${GamificationCatalog.badgesCatalog.size} Badges Unlocked",
                                "${StudyQuestStrings.formatDigits(unlockedTotal, uiLanguage)} / ${StudyQuestStrings.formatDigits(GamificationCatalog.badgesCatalog.size, uiLanguage)}টি ব্যাজ অর্জিত"
                            ),
                            subtitle = StudyQuestStrings.t(
                                uiLanguage,
                                "Every badge requires verified learning milestones across study, streaks, exams & community.",
                                "প্রতিটি ব্যাজ প্রকৃত অধ্যয়ন, স্ট্রিক, পরীক্ষা ও কমিউনিটি অবদানে অর্জিত হয়।"
                            ),
                            reducedMotion = false
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("all" to "All", "unlocked" to "Unlocked", "locked" to "Locked").forEach { (k, lbl) ->
                                    FilterChip(selected = statusFilter == k, onClick = { statusFilter = k }, label = { Text(lbl) })
                                }
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = categoryFilter == null,
                                    onClick = { categoryFilter = null },
                                    label = { Text(StudyQuestStrings.t(uiLanguage, "All Categories", "সব বিভাগ")) }
                                )
                                BadgeCategory.entries.forEach { cat ->
                                    FilterChip(
                                        selected = categoryFilter == cat,
                                        onClick = { categoryFilter = if (categoryFilter == cat) null else cat },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, cat.labelEn, cat.labelBn)) }
                                    )
                                }
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BadgeRarity.entries.forEach { r ->
                                    FilterChip(
                                        selected = rarityFilter == r,
                                        onClick = { rarityFilter = if (rarityFilter == r) null else r },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, r.labelEn, r.labelBn)) }
                                    )
                                }
                            }
                        }
                    }

                    items(filteredBadges, key = { it.id }) { def ->
                        val doc = badgeDocMap[def.id]
                        val unlocked = doc?.unlocked == true
                        val prog = (doc?.progress ?: 0L).coerceAtMost(def.target)
                        val rarityColor = rarityAccentColor(def.rarity, semantic.success)

                        StudyQuestCard(
                            containerColor = if (unlocked) semantic.successContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("badge_item_${def.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                                    Canvas(modifier = Modifier.size(48.dp)) {
                                        drawCircle(
                                            color = if (unlocked) rarityColor.copy(alpha = 0.2f) else CobaltPrimary.copy(alpha = 0.08f)
                                        )
                                        drawCircle(
                                            color = if (unlocked) rarityColor else CobaltPrimary.copy(alpha = 0.3f),
                                            radius = size.minDimension / 2.2f,
                                            style = Stroke(width = 2.5.dp.toPx())
                                        )
                                    }
                                    Icon(
                                        Icons.Filled.WorkspacePremium,
                                        contentDescription = null,
                                        tint = if (unlocked) rarityColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = StudyQuestStrings.t(uiLanguage, def.titleEn, def.titleBn),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = rarityColor.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = StudyQuestStrings.t(uiLanguage, def.rarity.labelEn, def.rarity.labelBn),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = rarityColor,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = StudyQuestStrings.t(uiLanguage, def.descriptionEn, def.descriptionBn),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { prog.toFloat() / def.target.coerceAtLeast(1L).toFloat() },
                                        color = if (unlocked) semantic.success else rarityColor,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (unlocked) {
                                            StudyQuestStrings.t(
                                                uiLanguage,
                                                "Unlocked • ${def.rarity.labelEn} Achievement",
                                                "অর্জিত • ${def.rarity.labelBn} অর্জন"
                                            )
                                        } else {
                                            "${StudyQuestStrings.formatDigits(prog, uiLanguage)} / ${StudyQuestStrings.formatDigits(def.target, uiLanguage)} • ${StudyQuestStrings.t(uiLanguage, def.rarity.labelEn, def.rarity.labelBn)}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (unlocked) semantic.success else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (unlocked && !isGuestMode) {
                                    Column {
                                        IconButton(
                                            onClick = { onToggleFavorite(def.id) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                if (doc?.favorite == true) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                contentDescription = "Favorite badge",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = { onTogglePin(def.id) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                if (doc?.pinned == true) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                                contentDescription = "Pin badge to profile",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (profile.unlockedTrophyTitles.isEmpty()) {
                        item {
                            EmptyStateView(
                                titleEn = "No competition trophies yet",
                                titleBn = "এখনো কোনো প্রতিযোগিতা ট্রফি অর্জিত হয়নি",
                                subtitleEn = "Finish Top 3 in Weekly, Two-Week, or Monthly leaderboards to earn permanent Trophy Titles.",
                                subtitleBn = "সাপ্তাহিক, পাক্ষিক বা মাসিক লিডারবোর্ডে শীর্ষ ৩-এ স্থান পেলে এখানে ট্রফি যুক্ত হবে।",
                                language = uiLanguage
                            )
                        }
                    } else {
                        items(profile.unlockedTrophyTitles) { trophy ->
                            StudyQuestCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = AmberGold, modifier = Modifier.size(32.dp))
                                    Text(trophy, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                val unlockedTotal = GamificationCatalog.badgesCatalog.count { badgeDocMap[it.id]?.unlocked == true }
                val totalCatalog = GamificationCatalog.badgesCatalog.size.coerceAtLeast(1)
                val nextTargetDef = remember(badgeDocMap) {
                    GamificationCatalog.badgesCatalog.firstOrNull { badgeDocMap[it.id]?.unlocked != true }
                        ?: GamificationCatalog.badgesCatalog.first()
                }
                val nextProg = (badgeDocMap[nextTargetDef.id]?.progress ?: 0L).coerceAtMost(nextTargetDef.target)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        CharcoalMilestoneCard(
                            eyebrow = StudyQuestStrings.t(uiLanguage, "ACHIEVEMENT PROGRESS", "অর্জনের অগ্রগতি"),
                            title = StudyQuestStrings.t(
                                uiLanguage,
                                "$unlockedTotal of $totalCatalog Badges Earned",
                                "${StudyQuestStrings.formatDigits(totalCatalog, uiLanguage)}টির মধ্যে ${StudyQuestStrings.formatDigits(unlockedTotal, uiLanguage)}টি ব্যাজ অর্জিত"
                            ),
                            progressLabel = StudyQuestStrings.t(
                                uiLanguage,
                                "Next: ${nextTargetDef.titleEn} ($nextProg / ${nextTargetDef.target})",
                                "পরবর্তী: ${nextTargetDef.titleBn} (${StudyQuestStrings.formatDigits(nextProg, uiLanguage)} / ${StudyQuestStrings.formatDigits(nextTargetDef.target, uiLanguage)})"
                            ),
                            progress = unlockedTotal.toFloat() / totalCatalog.toFloat(),
                            unlockedCount = unlockedTotal,
                            totalCount = totalCatalog,
                            onClick = { selectedTab = 0 }
                        )
                    }

                    item {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Verified Learning Milestones", "যাচাইকৃত অধ্যয়ন মাইলফলক"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (xpLedger.isEmpty()) {
                        item {
                            EmptyStateView(
                                titleEn = "No learning milestones logged yet",
                                titleBn = "এখনো কোনো মাইলফলক রেকর্ড হয়নি",
                                subtitleEn = "Complete a lesson, quiz, focus session, or assignment to record your first verified milestone.",
                                subtitleBn = "প্রথম মাইলফলক দেখতে একটি পাঠ, কুইজ বা ফোকাস সেশন সম্পন্ন করো।",
                                language = uiLanguage
                            )
                        }
                    } else {
                        items(xpLedger, key = { it.idempotencyKey.ifBlank { it.id } }) { tx ->
                            StudyQuestCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = semantic.success,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tx.description?.ifBlank { tx.sourceType } ?: tx.sourceType,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${tx.sourceType.uppercase()} • ${tx.sourceId}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = semantic.successContainer
                                    ) {
                                        Text(
                                            text = StudyQuestStrings.t(uiLanguage, "Verified", "যাচাইকৃত"),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = semantic.onSuccessContainer,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
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
