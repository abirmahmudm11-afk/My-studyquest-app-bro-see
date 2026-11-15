package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalUserPreferences
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.LearningRecordDoc
import com.example.data.model.PublicProfile
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.AvatarBadgeView
import com.example.ui.components.GuestUpgradeBannerCard
import com.example.ui.components.RankTierBadge
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MangoYellow
import com.example.ui.viewmodel.AdminStudioViewModel

sealed interface ProfileDestination {
    data object Home : ProfileDestination
    data object Analytics : ProfileDestination
    data object CosmeticsShop : ProfileDestination
    data object SettingsAndSafety : ProfileDestination
    data object AdminStudio : ProfileDestination
}

@Composable
fun ProfileTabScreen(
    profile: PublicProfile,
    preferences: LocalUserPreferences,
    adminRole: String?,
    unlockedCosmetics: Set<String>,
    learningRecords: List<LearningRecordDoc>,
    subjectAnalytics: List<SubjectAnalyticsSummary>,
    weakTopics: List<WeakTopicStat>,
    friendCount: Int,
    exportedJson: String?,
    onClearExportedJson: () -> Unit,
    onEquipCosmetic: (category: String, id: String) -> Unit,
    onPurchaseCosmetic: (id: String, costCoins: Int) -> Unit,
    onUpdateUiLanguage: (AppLanguage) -> Unit,
    onUpdateQuestionLanguage: (AppLanguage) -> Unit,
    onUpdateDarkMode: (Boolean) -> Unit,
    onUpdateReducedMotion: (Boolean) -> Unit,
    onUpdateSound: (Boolean) -> Unit,
    onUpdateQuietHours: (Boolean) -> Unit,
    onUpdateNotifications: (Boolean) -> Unit,
    onUpdateOptInNational: (Boolean) -> Unit,
    onUpdateOnlineStatusVisible: (Boolean) -> Unit,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onSignOut: () -> Unit,
    adminViewModelProvider: @Composable () -> AdminStudioViewModel,
    isGuestMode: Boolean = false,
    onRequestGoogleSignIn: () -> Unit = {},
    onExitGuestMode: () -> Unit = {}
) {
    var destination by remember { mutableStateOf<ProfileDestination>(ProfileDestination.Home) }
    val lang = preferences.uiLanguage
    val reducedMotion = preferences.reducedMotion

    if (destination != ProfileDestination.Home) {
        BackHandler { destination = ProfileDestination.Home }
    }

    when (destination) {
        ProfileDestination.Home -> {
            val unlockedAchievementCount = remember(learningRecords, friendCount, profile.competitionRating) {
                BangladeshCurriculumCatalog.achievementsCatalog.count { ach ->
                    when (ach.id) {
                        "ach_first_lesson" -> learningRecords.any { it.recordType == "lesson" }
                        "ach_subject_explorer" -> learningRecords.map { it.subjectId }.distinct().size >= 3
                        "ach_first_mock" -> learningRecords.any { it.recordType == "exam" }
                        "ach_first_friend" -> friendCount >= 1
                        "ach_first_battle" -> profile.competitionRating > 1000L
                        else -> false
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isGuestMode) {
                    item {
                        StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                            GuestUpgradeBannerCard(
                                language = lang,
                                totalXp = profile.totalXp,
                                coins = profile.coins,
                                onSignInClick = onRequestGoogleSignIn
                            )
                        }
                    }
                }

                item {
                    StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                        val cls = BangladeshCurriculumCatalog.classes.find { it.id == profile.classId }
                        val xpWithinLevel = (profile.totalXp % 100L).toInt()
                        StudyQuestGradientHeroBanner(
                            eyebrow = "${profile.studyQuestId} • @${profile.username}",
                            title = profile.displayName,
                            subtitle = StudyQuestStrings.t(
                                lang,
                                "${cls?.labelEn ?: profile.classId} • ${profile.curriculumVersion.uppercase()} Version • Lv.${profile.overallLevel}",
                                "${cls?.labelBn ?: profile.classId} • লেভেল ${StudyQuestStrings.formatDigits(profile.overallLevel, lang)}"
                            ),
                            reducedMotion = reducedMotion,
                            trailingSlot = {
                                AvatarBadgeView(profile.avatarId, profile.frameId, profile.displayName, size = 64.dp)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RankTierBadge(tier = profile.rankTier, language = lang)
                                    StatBadgePill(
                                        icon = Icons.Filled.MonetizationOn,
                                        label = "${StudyQuestStrings.formatDigits(profile.coins, lang)} Coins",
                                        tint = MangoYellow,
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                }
                                AnimatedCircularMasteryRing(
                                    progress = xpWithinLevel / 100f,
                                    centerValueText = "${xpWithinLevel}%",
                                    centerCaptionText = "Lv.${profile.overallLevel + 1}",
                                    reducedMotion = reducedMotion,
                                    size = 58.dp
                                )
                            }
                        }
                    }
                }

                // Quick Achievements & Stats Showcase
                item {
                    StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                        StudyQuestCard(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            onClick = { destination = ProfileDestination.Analytics }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(lang, "Badges & Mastery Overview", "ব্যাজ ও দক্ষতার সারসংক্ষেপ"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            lang,
                                            "$unlockedAchievementCount / ${BangladeshCurriculumCatalog.achievementsCatalog.size} Badges • ${subjectAnalytics.size} Active Subjects",
                                            "${StudyQuestStrings.formatDigits(unlockedAchievementCount, lang)}টি ব্যাজ • ${StudyQuestStrings.formatDigits(subjectAnalytics.size, lang)}টি বিষয়"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatBadgePill(
                                    icon = Icons.Filled.EmojiEvents,
                                    label = "${profile.totalXp} XP",
                                    tint = MangoYellow,
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                    }
                }

                item {
                    StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                        ProfileNavTile(
                            icon = Icons.Filled.Analytics,
                            title = StudyQuestStrings.t(lang, "Academic Analytics & Achievements", "অ্যাকাডেমিক বিশ্লেষণ ও অর্জন"),
                            subtitle = StudyQuestStrings.t(lang, "${learningRecords.size} records • Subject mastery & badges", "বিষয়ভিত্তিক দক্ষতা ও ব্যাজ গ্যালারি"),
                            onClick = { destination = ProfileDestination.Analytics },
                            testTag = "profile_open_analytics"
                        )
                    }
                }

                item {
                    StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                        ProfileNavTile(
                            icon = Icons.Filled.ShoppingBag,
                            title = StudyQuestStrings.t(lang, "Avatar & Coin Shop", "অ্যাভাটার ও কয়েন শপ"),
                            subtitle = StudyQuestStrings.t(lang, "Unlock scholarly frames, avatars & titles with earned Coins", "অর্জিত কয়েন দিয়ে অ্যাভাটার ও ফ্রেম আনলক করো"),
                            onClick = { destination = ProfileDestination.CosmeticsShop },
                            testTag = "profile_open_shop"
                        )
                    }
                }

                item {
                    StaggeredEntrance(index = 5, reducedMotion = reducedMotion) {
                        ProfileNavTile(
                            icon = Icons.Filled.Tune,
                            title = StudyQuestStrings.t(lang, "Languages, Accessibility & Privacy", "ভাষা, অ্যাক্সেসিবিলিটি ও নিরাপত্তা"),
                            subtitle = StudyQuestStrings.t(lang, "Bangla/English, Dark mode, Reduced motion, Export & Account deletion", "ভাষা, ডার্ক মোড, ডেটা এক্সপোর্ট ও অ্যাকাউন্ট সেটিংস"),
                            onClick = { destination = ProfileDestination.SettingsAndSafety },
                            testTag = "profile_open_settings"
                        )
                    }
                }

                if (adminRole != null && !isGuestMode) {
                    item {
                        ProfileNavTile(
                            icon = Icons.Filled.AdminPanelSettings,
                            title = StudyQuestStrings.t(lang, "Admin Studio ($adminRole)", "অ্যাডমিন স্টুডিও ($adminRole)"),
                            subtitle = StudyQuestStrings.t(lang, "Editorial content pipeline, moderation queue, tournaments & AI toggle", "কনটেন্ট অনুমোদন, মডারেশন ও সিস্টেম সেটিংস"),
                            onClick = { destination = ProfileDestination.AdminStudio },
                            testTag = "profile_open_admin_studio"
                        )
                    }
                }

                item {
                    if (isGuestMode) {
                        OutlinedButton(
                            onClick = onExitGuestMode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("exit_guest_mode_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(StudyQuestStrings.t(lang, "Back to Sign-In Screen", "সাইন-ইন স্ক্রিনে ফিরে যান"))
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(StudyQuestStrings.t(lang, "Sign Out", "সাইন আউট"))
                        }
                    }
                }
            }
        }

        ProfileDestination.Analytics -> {
            AnalyticsAndAchievementsScreen(
                lang = lang,
                learningRecords = learningRecords,
                subjectAnalytics = subjectAnalytics,
                weakTopics = weakTopics,
                friendCount = friendCount,
                matchCount = profile.competitionRating.let { if (it > 1000L) 1 else 0 },
                onBack = { destination = ProfileDestination.Home }
            )
        }

        ProfileDestination.CosmeticsShop -> {
            CosmeticsShopScreen(
                lang = lang,
                profile = profile,
                unlockedCosmetics = unlockedCosmetics,
                onBack = { destination = ProfileDestination.Home },
                onPurchase = onPurchaseCosmetic,
                onEquip = onEquipCosmetic
            )
        }

        ProfileDestination.SettingsAndSafety -> {
            PreferencesAndSafetyScreen(
                profile = profile,
                preferences = preferences,
                isGuestMode = isGuestMode,
                exportedJson = exportedJson,
                onClearExportedJson = onClearExportedJson,
                onBack = { destination = ProfileDestination.Home },
                onUpdateUiLanguage = onUpdateUiLanguage,
                onUpdateQuestionLanguage = onUpdateQuestionLanguage,
                onUpdateDarkMode = onUpdateDarkMode,
                onUpdateReducedMotion = onUpdateReducedMotion,
                onUpdateSound = onUpdateSound,
                onUpdateQuietHours = onUpdateQuietHours,
                onUpdateNotifications = onUpdateNotifications,
                onUpdateOptInNational = onUpdateOptInNational,
                onUpdateOnlineStatusVisible = onUpdateOnlineStatusVisible,
                onExportData = onExportData,
                onDeleteAccount = onDeleteAccount,
                onRequestGoogleSignIn = onRequestGoogleSignIn
            )
        }

        ProfileDestination.AdminStudio -> {
            val adminVm = adminViewModelProvider()
            AdminStudioScreen(
                adminRole = adminRole ?: "moderator",
                language = lang,
                viewModel = adminVm,
                onBack = { destination = ProfileDestination.Home }
            )
        }
    }
}

@Composable
private fun ProfileNavTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    StudyQuestCard(onClick = onClick, modifier = Modifier.testTag(testTag)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AnalyticsAndAchievementsScreen(
    lang: AppLanguage,
    learningRecords: List<LearningRecordDoc>,
    subjectAnalytics: List<SubjectAnalyticsSummary>,
    weakTopics: List<WeakTopicStat>,
    friendCount: Int,
    matchCount: Int,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text(StudyQuestStrings.t(lang, "Analytics & Achievements", "বিশ্লেষণ ও অর্জন"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(StudyQuestStrings.t(lang, "Subject Mastery", "বিষয়ভিত্তিক দক্ষতা"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (subjectAnalytics.isEmpty()) {
            item {
                StudyQuestCard {
                    Text(StudyQuestStrings.t(lang, "Complete lessons or practice quizzes to build your analytics profile.", "পাঠ বা প্র্যাকটিস সম্পন্ন করলে এখানে তোমার বিশ্লেষণ দেখা যাবে।"), style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            items(subjectAnalytics, key = { it.subjectId }) { item ->
                val subj = BangladeshCurriculumCatalog.subjects.find { it.id == item.subjectId }
                StudyQuestCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(StudyQuestStrings.t(lang, subj?.titleEn ?: item.subjectId, subj?.titleBn ?: item.subjectId), fontWeight = FontWeight.Bold)
                        Text("Lv.${item.subjectLevel} • ${item.totalXp} XP", color = ForestGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { item.averageAccuracy.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(999.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Lessons: ${item.lessonsCompleted} • Practice: ${item.practiceSessions} • Exams: ${item.examSessions} • Avg Accuracy: ${(item.averageAccuracy * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (weakTopics.isNotEmpty()) {
            item {
                Text(StudyQuestStrings.t(lang, "Topics Needing Practice", "আরও অনুশীলন প্রয়োজন"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(weakTopics, key = { "${it.subjectId}_${it.topicId}" }) { wt ->
                StudyQuestCard {
                    Text(StudyQuestStrings.t(lang, wt.topicLabelEn, wt.topicLabelBn), fontWeight = FontWeight.Bold)
                    Text("${(wt.averageAccuracy * 100).toInt()}% accuracy over ${wt.attempts} attempts", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Text(StudyQuestStrings.t(lang, "Achievements Gallery", "অর্জন গ্যালারি"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(BangladeshCurriculumCatalog.achievementsCatalog, key = { it.id }) { ach ->
            val unlocked = when (ach.id) {
                "ach_first_lesson" -> learningRecords.any { it.recordType == "lesson" }
                "ach_subject_explorer" -> learningRecords.map { it.subjectId }.distinct().size >= 3
                "ach_first_mock" -> learningRecords.any { it.recordType == "exam" }
                "ach_first_friend" -> friendCount >= 1
                "ach_first_battle" -> matchCount >= 1
                else -> false
            }
            StudyQuestCard(
                containerColor = if (unlocked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                else MaterialTheme.colorScheme.surface
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = if (unlocked) Icons.Filled.CheckCircle else Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = if (unlocked) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(StudyQuestStrings.t(lang, ach.titleEn, ach.titleBn), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(StudyQuestStrings.t(lang, ach.descriptionEn, ach.descriptionBn), style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = if (unlocked) StudyQuestStrings.t(lang, "Unlocked", "অর্জিত") else "+${ach.xpBonus} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun CosmeticsShopScreen(
    lang: AppLanguage,
    profile: PublicProfile,
    unlockedCosmetics: Set<String>,
    onBack: () -> Unit,
    onPurchase: (String, Int) -> Unit,
    onEquip: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    Text(StudyQuestStrings.t(lang, "Avatar & Coin Shop", "কয়েন শপ"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                StatBadgePill(
                    icon = Icons.Filled.MonetizationOn,
                    label = "${profile.coins}",
                    tint = MangoYellow,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }

        items(BangladeshCurriculumCatalog.cosmeticsCatalog, key = { it.id }) { item ->
            val owned = item.costCoins == 0 || item.id in unlockedCosmetics
            val equipped = when (item.category) {
                "avatar" -> profile.avatarId == item.id
                "frame" -> profile.frameId == item.id
                "title" -> profile.titleBadge == item.id
                else -> false
            }
            StudyQuestCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(StudyQuestStrings.t(lang, item.nameEn, item.nameBn), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(StudyQuestStrings.t(lang, item.unlockNoteEn, item.unlockNoteBn), style = MaterialTheme.typography.bodySmall)
                    }
                    when {
                        equipped -> OutlinedButton(onClick = {}, enabled = false) {
                            Text(StudyQuestStrings.t(lang, "Equipped", "ব্যবহৃত"))
                        }
                        owned -> FilledTonalButton(onClick = { onEquip(item.category, item.id) }) {
                            Text(StudyQuestStrings.t(lang, "Equip", "ব্যবহার করো"))
                        }
                        else -> Button(
                            onClick = { onPurchase(item.id, item.costCoins) },
                            enabled = profile.coins >= item.costCoins
                        ) {
                            Text("${item.costCoins} Coins")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferencesAndSafetyScreen(
    profile: PublicProfile,
    preferences: LocalUserPreferences,
    isGuestMode: Boolean,
    exportedJson: String?,
    onClearExportedJson: () -> Unit,
    onBack: () -> Unit,
    onUpdateUiLanguage: (AppLanguage) -> Unit,
    onUpdateQuestionLanguage: (AppLanguage) -> Unit,
    onUpdateDarkMode: (Boolean) -> Unit,
    onUpdateReducedMotion: (Boolean) -> Unit,
    onUpdateSound: (Boolean) -> Unit,
    onUpdateQuietHours: (Boolean) -> Unit,
    onUpdateNotifications: (Boolean) -> Unit,
    onUpdateOptInNational: (Boolean) -> Unit,
    onUpdateOnlineStatusVisible: (Boolean) -> Unit,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onRequestGoogleSignIn: () -> Unit
) {
    val lang = preferences.uiLanguage
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Text(StudyQuestStrings.t(lang, "Settings & Safety Center", "সেটিংস ও নিরাপত্তা"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        item {
            StudyQuestCard {
                Text(StudyQuestStrings.t(lang, "Interface Language", "অ্যাপের ভাষা"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { l ->
                        FilterChip(
                            selected = preferences.uiLanguage == l,
                            onClick = { onUpdateUiLanguage(l) },
                            label = { Text(StudyQuestStrings.t(lang, l.labelEn, l.labelBn)) },
                            modifier = Modifier.testTag("settings_ui_lang_${l.code}")
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(StudyQuestStrings.t(lang, "Question Language", "প্রশ্নের ভাষা"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { l ->
                        FilterChip(
                            selected = preferences.questionLanguage == l,
                            onClick = { onUpdateQuestionLanguage(l) },
                            label = { Text(StudyQuestStrings.t(lang, l.labelEn, l.labelBn)) },
                            modifier = Modifier.testTag("settings_q_lang_${l.code}")
                        )
                    }
                }
            }
        }

        item {
            StudyQuestCard {
                SettingToggleRow(StudyQuestStrings.t(lang, "Dark Theme", "ডার্ক থিম"), preferences.darkMode, onUpdateDarkMode)
                SettingToggleRow(StudyQuestStrings.t(lang, "Reduced Motion (Accessibility)", "কম অ্যানিমেশন"), preferences.reducedMotion, onUpdateReducedMotion)
                SettingToggleRow(StudyQuestStrings.t(lang, "Sound Effects & Haptics", "সাউন্ড ও হ্যাপটিক্স"), preferences.soundEnabled, onUpdateSound)
                SettingToggleRow(StudyQuestStrings.t(lang, "Study Notifications", "নোটিফিকেশন"), preferences.notificationsEnabled, onUpdateNotifications)
                SettingToggleRow(StudyQuestStrings.t(lang, "Quiet Hours (10 PM – 7 AM)", "নীরব সময় (রাত ১০টা – সকাল ৭টা)"), preferences.quietHoursEnabled, onUpdateQuietHours)
                if (!isGuestMode) {
                    SettingToggleRow(StudyQuestStrings.t(lang, "Opt-in National Leaderboard", "জাতীয় লিডারবোর্ডে অংশগ্রহণ"), profile.optInNationalLeaderboard, onUpdateOptInNational)
                    SettingToggleRow(StudyQuestStrings.t(lang, "Show Online Status to Friends", "অনলাইন অবস্থা দেখান"), profile.showOnlineStatus, onUpdateOnlineStatusVisible)
                }
            }
        }

        item {
            StudyQuestCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = ForestGreen)
                    Text(StudyQuestStrings.t(lang, "Student Safety & Data Controls", "শিক্ষার্থী নিরাপত্তা ও ডেটা নিয়ন্ত্রণ"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = StudyQuestStrings.t(
                        lang,
                        if (isGuestMode) "Guest data stays on this device until you sign in with Google to auto-merge."
                        else "Your email is never shared publicly. Only accepted friends can chat with you, and suspicious links are blocked automatically.",
                        if (isGuestMode) "গেস্ট ডেটা ডিভাইসেই থাকে এবং গুগল সাইন-ইন করলে স্বয়ংক্রিয়ভাবে সিঙ্ক হয়।"
                        else "তোমার ইমেইল কখনোই প্রকাশ করা হয় না। কেবল স্বীকৃত বন্ধুদের সাথেই চ্যাট করা যায়।"
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onExportData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("export_data_btn")
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(StudyQuestStrings.t(lang, "Export My Data (JSON)", "আমার ডেটা এক্সপোর্ট করুন"))
                }
                Spacer(Modifier.height(8.dp))
                if (isGuestMode) {
                    Button(
                        onClick = onRequestGoogleSignIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("settings_guest_signin_btn")
                    ) {
                        Text(StudyQuestStrings.t(lang, "Link Google Account & Auto-Merge", "গুগল অ্যাকাউন্ট লিংক করুন"))
                    }
                } else {
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("delete_account_btn")
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(StudyQuestStrings.t(lang, "Delete Account & Data", "অ্যাকাউন্ট মুছে ফেলুন"))
                    }
                }
            }
        }

        item {
            StudyQuestCard(modifier = Modifier.testTag("firebase_project_info_card")) {
                Text(
                    text = StudyQuestStrings.t(lang, "Firebase Project & App Info", "ফায়ারবেস প্রোজেক্ট ও অ্যাপ তথ্য"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "App Name: StudyQuest\n" +
                        "Android Package: com.aistudio.studyquest.bdxkpq\n" +
                        "Firebase Project ID: com-aistudio-studyquest-5811a\n" +
                        "Firebase Project Number: 716745939273\n" +
                        "Firestore Database ID: ai-studio-android-mystudyq-f4667920-563c-4888-a2b3-dd7d887506df\n" +
                        "Storage Bucket: com-aistudio-studyquest-5811a.firebasestorage.app",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (exportedJson != null) {
        AlertDialog(
            onDismissRequest = onClearExportedJson,
            title = { Text(StudyQuestStrings.t(lang, "Exported StudyQuest Data", "এক্সপোর্টকৃত ডেটা")) },
            text = {
                Text(
                    text = exportedJson,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                Button(onClick = onClearExportedJson) { Text(StudyQuestStrings.t(lang, "Close", "বন্ধ")) }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(StudyQuestStrings.t(lang, "Delete Account Permanently?", "অ্যাকাউন্ট স্থায়ীভাবে মুছবেন?")) },
            text = {
                Text(
                    StudyQuestStrings.t(
                        lang,
                        "This removes your public profile, learning records, study plans, and local offline bookmarks.",
                        "এটি আপনার প্রোফাইল, অনুশীলন রেকর্ড এবং অফলাইন বুকমার্ক মুছে ফেলবে।"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(StudyQuestStrings.t(lang, "Delete", "মুছে ফেলুন"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(StudyQuestStrings.t(lang, "Cancel", "বাতিল"))
                }
            }
        )
    }
}

@Composable
private fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
