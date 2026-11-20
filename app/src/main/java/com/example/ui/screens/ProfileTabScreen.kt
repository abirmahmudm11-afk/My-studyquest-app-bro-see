package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLanguage
import com.example.core.LocalUserPreferences
import com.example.core.OfflineVaultManager
import com.example.core.OfflineVaultStats
import com.example.core.StudyQuestStrings
import com.example.core.SurfaceStyle
import com.example.core.ThemeMode
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.GamificationCatalog
import com.example.data.local.CachedBookEntity
import com.example.data.model.AiCreditLedgerDoc
import com.example.data.model.LearningRecordDoc
import com.example.data.model.PublicProfile
import com.example.data.model.PurchaseRecordDoc
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.TextbookCatalogItem
import com.example.data.model.TextbookDownloadStatus
import com.example.data.model.UserBadgeDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.model.VideoCreditLedgerDoc
import com.example.data.model.XpTransactionDoc
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.SubjectAnalyticsSummary
import com.example.data.repository.WeakTopicStat
import com.example.core.BillingAndAdProvider
import com.example.ui.components.AnimatedCircularMasteryRing
import com.example.ui.components.AvatarBadgeView
import com.example.ui.components.GuestUpgradeBannerCard
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.components.StudyTimeAndXpBarChart
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
import com.example.ui.viewmodel.AdminStudioViewModel
import com.google.firebase.FirebaseApp

sealed interface ProfileDestination {
    data object Home : ProfileDestination
    data object Analytics : ProfileDestination
    data object BadgesAndXp : ProfileDestination
    data object CosmeticsShop : ProfileDestination
    data object SettingsAndSafety : ProfileDestination
    data object AdminStudio : ProfileDestination
    data object PlanStore : ProfileDestination
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
    onExitGuestMode: () -> Unit = {},
    userBadges: List<UserBadgeDoc> = emptyList(),
    xpLedger: List<XpTransactionDoc> = emptyList(),
    dailyStudySeries: List<DailyStudyMetric> = emptyList(),
    subscription: UserSubscriptionDoc = UserSubscriptionDoc(),
    billingConfigured: Boolean = false,
    onUpdateThemeMode: (ThemeMode) -> Unit = {},
    onUpdateSurfaceStyle: (SurfaceStyle) -> Unit = {},
    onUpdateNotificationCategory: (String, Boolean) -> Unit = { _, _ -> },
    onToggleBadgeFavorite: (String) -> Unit = {},
    onToggleBadgePin: (String) -> Unit = {},
    onTriggerBillingUpgrade: () -> Unit = {},
    onRetakePromotionExam: () -> Unit = {},
    aiCreditLedger: List<AiCreditLedgerDoc> = emptyList(),
    videoCreditLedger: List<VideoCreditLedgerDoc> = emptyList(),
    purchaseHistory: List<PurchaseRecordDoc> = emptyList(),
    onSelectPlan: (String) -> Unit = {},
    onPurchaseVideoBoost: () -> Unit = {},
    onSwitchToFreePlan: () -> Unit = {},
    onOpenAiLab: () -> Unit = {},
    activeLockedBook: TextbookCatalogItem? = null,
    cachedBooks: List<CachedBookEntity> = emptyList(),
    onOpenTextbookReader: () -> Unit = {},
    onReinstallBookOffline: (TextbookCatalogItem) -> Unit = {},
    onDeleteCachedBook: (String) -> Unit = {},
    onOpenDownloadManager: () -> Unit = {}
) {
    val destinationStack = remember { mutableStateListOf<ProfileDestination>(ProfileDestination.Home) }
    val destination = destinationStack.lastOrNull() ?: ProfileDestination.Home
    val lang = preferences.uiLanguage
    val reducedMotion = preferences.reducedMotion
    val semantic = LocalStudyQuestColors.current

    fun pushDest(next: ProfileDestination) {
        if (next == ProfileDestination.Home) {
            while (destinationStack.size > 1) {
                destinationStack.removeAt(destinationStack.lastIndex)
            }
        } else if (destinationStack.lastOrNull() != next) {
            destinationStack.add(next)
        }
    }

    fun popDest() {
        if (destinationStack.size > 1) {
            destinationStack.removeAt(destinationStack.lastIndex)
        }
    }

    when (destination) {
        ProfileDestination.Home -> {
            val unlockedBadgesCount = remember(userBadges) {
                userBadges.count { it.unlocked }
            }
            val pinnedBadges = remember(userBadges) {
                userBadges.filter { it.unlocked && it.pinned }.take(3)
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 980.dp)
                    .fillMaxSize()
                    .testTag("profile_home_list"),
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
                        val streakPct = (profile.streakCount.toFloat() / 7f).coerceIn(0.08f, 1f)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                            border = studyQuestGlassAwareBorder(),
                            shadowElevation = 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .align(Alignment.TopCenter)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    Box(modifier = Modifier.padding(top = 14.dp)) {
                                        AvatarBadgeView(profile.avatarId, profile.frameId, profile.displayName, size = 76.dp)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = studyQuestGlassAwareBorder()
                                ) {
                                    Text(
                                        text = "${StudyQuestStrings.rankTierLabel(profile.rankTier, lang)} • ${profile.studyQuestId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = profile.displayName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            lang,
                                            "@${profile.username} • ${cls?.labelEn ?: profile.classId} • ${profile.curriculumVersion.uppercase()}",
                                            "@${profile.username} • ${cls?.labelBn ?: profile.classId} • ${profile.curriculumVersion.uppercase()}"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProfileMetricChip(
                                        value = "${StudyQuestStrings.formatDigits(profile.streakCount, lang)}d",
                                        label = StudyQuestStrings.t(lang, "Total Streak", "মোট স্ট্রিক"),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ProfileMetricChip(
                                        value = StudyQuestStrings.formatDigits(unlockedBadgesCount, lang),
                                        label = StudyQuestStrings.t(lang, "Badges", "অর্জিত ব্যাজ"),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ProfileMetricChip(
                                        value = StudyQuestStrings.formatDigits(learningRecords.size, lang),
                                        label = StudyQuestStrings.t(lang, "Sessions", "সেশন"),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.LocalFireDepartment,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = StudyQuestStrings.t(
                                                        lang,
                                                        "You're on a ${profile.streakCount}-day streak!",
                                                        "${StudyQuestStrings.formatDigits(profile.streakCount, lang)} দিনের স্ট্রিক চলছে!"
                                                    ),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "$unlockedBadgesCount/${GamificationCatalog.badgesCatalog.size}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { streakPct },
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
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
                }

                // Locked Academic Year & Jan 1 Promotion Card
                item {
                    val daysToJan1 = (GamificationCatalog.millisUntilPeriodEnd("jan1") / 86_400_000L).toInt()
                    StudyQuestCard(
                        containerColor = if (profile.annualExamStatus == "failed") {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        modifier = Modifier.testTag("profile_academic_lock_card")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StudyQuestStrings.t(
                                        lang,
                                        "${StudyQuestStrings.classLabel(profile.classId, lang)} • Locked (${profile.classLockedAcademicYear ?: GamificationCatalog.currentAcademicYear()})",
                                        "${StudyQuestStrings.classLabel(profile.classId, lang)} • লকড (${profile.classLockedAcademicYear ?: GamificationCatalog.currentAcademicYear()})"
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (profile.annualExamStatus == "failed") {
                                        StudyQuestStrings.t(lang, "Promotion paused: pass Promotion Exam (60%+) to advance.", "প্রোমোশন স্থগিত: প্রোমোশন পরীক্ষায় ৬০%+ পেলে পরবর্তী শ্রেণিতে যাবেন।")
                                    } else {
                                        StudyQuestStrings.t(lang, "Next Jan 1 promotion in $daysToJan1 days (Asia/Dhaka)", "১ জানুয়ারি শ্রেণি উন্নয়ন $daysToJan1 দিন পর")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (profile.annualExamStatus == "failed") {
                                Button(onClick = onRetakePromotionExam) {
                                    Text(StudyQuestStrings.t(lang, "Retake", "পুনঃপরীক্ষা"))
                                }
                            }
                        }
                    }
                }

                if (activeLockedBook != null) {
                    item {
                        val activeCache = cachedBooks.firstOrNull { it.bookId == activeLockedBook.id }
                        val installedCount = cachedBooks.count { it.downloadStatus == TextbookDownloadStatus.INSTALLED.id }
                        StudyQuestCard(
                            modifier = Modifier.testTag("profile_offline_books_card")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = StudyQuestStrings.t(
                                        lang,
                                        "Active NCTB Book & Offline Packs ($installedCount installed)",
                                        "সক্রিয় পাঠ্যবই ও অফলাইন প্যাক (${installedCount}টি সংরক্ষিত)"
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(lang, activeLockedBook.titleEn, activeLockedBook.titleBn),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (activeCache?.downloadStatus == TextbookDownloadStatus.INSTALLED.id) {
                                        StudyQuestStrings.t(
                                            lang,
                                            "Offline ready • Last read page ${(activeCache.lastReadPage + 1).coerceAtLeast(1)}",
                                            "অফলাইনে সংরক্ষিত • সর্বশেষ পঠিত পৃষ্ঠা ${(activeCache.lastReadPage + 1).coerceAtLeast(1)}"
                                        )
                                    } else {
                                        StudyQuestStrings.t(lang, "Tap Install to cache chapters offline.", "অফলাইনে সংরক্ষণ করতে ইনস্টল চাপুন।")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = onOpenTextbookReader) {
                                        Text(StudyQuestStrings.t(lang, "Open Reader", "বই খোলো"))
                                    }
                                    FilledTonalButton(onClick = { onReinstallBookOffline(activeLockedBook) }) {
                                        Text(StudyQuestStrings.t(lang, "Install / Refresh", "ইনস্টল / রিফ্রেশ"))
                                    }
                                    if (activeCache != null) {
                                        OutlinedButton(onClick = { onDeleteCachedBook(activeCache.bookId) }) {
                                            Text(StudyQuestStrings.t(lang, "Clear", "মুছুন"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    val context = LocalContext.current
                    var vaultStats by remember { mutableStateOf(OfflineVaultStats()) }
                    LaunchedEffect(cachedBooks.size) {
                        vaultStats = OfflineVaultManager.computeVaultStats(context)
                    }
                    StudyQuestCard(modifier = Modifier.testTag("profile_offline_vault_card")) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(98.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_vault_offline_vault),
                                    contentDescription = StudyQuestStrings.t(
                                        lang,
                                        "Offline Study Vault Library",
                                        "অফলাইন স্টাডি ভল্ট লাইব্রেরি"
                                    ),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            lang,
                                            "Offline Study Vault",
                                            "অফলাইন স্টাডি ভল্ট"
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            lang,
                                            "${vaultStats.bundledBooksCount} Books • ${vaultStats.bundledAudioTracksCount} Audio • ${vaultStats.bundledPlatesCount} Plates • ${vaultStats.workedEntriesCount} Guides",
                                            "${StudyQuestStrings.formatDigits(vaultStats.bundledBooksCount, lang)} বই • ${StudyQuestStrings.formatDigits(vaultStats.bundledAudioTracksCount, lang)} অডিও • ${StudyQuestStrings.formatDigits(vaultStats.workedEntriesCount, lang)} গাইড"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    val mbText = String.format(java.util.Locale.US, "%.0f MB", vaultStats.bundledVaultSizeMb)
                                    Text(
                                        text = mbText,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = onOpenDownloadManager,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("profile_open_downloads_btn")
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(lang, "Open Offline Downloads & Packs", "অফলাইন ডাউনলোড ম্যানেজার খোলো"))
                            }
                        }
                    }
                }

                // Badges & Trophy Cabinet Card
                item {
                    StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                        StudyQuestCard(
                            onClick = { pushDest(ProfileDestination.BadgesAndXp) },
                            modifier = Modifier.testTag("profile_open_badges_xp")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = StudyQuestStrings.t(lang, "Badges, Trophies & Milestones", "ব্যাজ, ট্রফি ও মাইলফলক"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            lang,
                                            "$unlockedBadgesCount / ${GamificationCatalog.badgesCatalog.size} Badges • ${pinnedBadges.size} Pinned • ${xpLedger.size} Milestones",
                                            "${StudyQuestStrings.formatDigits(unlockedBadgesCount, lang)} / ${StudyQuestStrings.formatDigits(GamificationCatalog.badgesCatalog.size, lang)} ব্যাজ • ${StudyQuestStrings.formatDigits(xpLedger.size, lang)} মাইলফলক"
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatBadgePill(
                                    icon = Icons.Filled.WorkspacePremium,
                                    label = "${StudyQuestStrings.formatDigits(unlockedBadgesCount, lang)} / ${StudyQuestStrings.formatDigits(GamificationCatalog.badgesCatalog.size, lang)}",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }

                // My Plan & AI/Video Credits Card (No Storage Plans)
                if (!isGuestMode) {
                    item {
                        val normalizedSub = remember(subscription) { BillingAndAdProvider.normalizeSubscription(subscription) }
                        val activePlan = remember(normalizedSub.tier) { QuestPlanCatalog.planById(normalizedSub.tier) }
                        val aiLeft = (normalizedSub.monthlyAiLimit.toInt().coerceAtLeast(activePlan.monthlyAiCredits) - normalizedSub.aiCreditsUsed.toInt()).coerceAtLeast(0)
                        val videoLeft = ((normalizedSub.monthlyVideoLimit.toInt().coerceAtLeast(activePlan.monthlyVideoCredits) + normalizedSub.bonusVideoCredits.toInt()) - normalizedSub.videoCreditsUsed.toInt()).coerceAtLeast(0)
                        StudyQuestCard(modifier = Modifier.testTag("profile_subscription_card")) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = StudyQuestStrings.t(
                                                lang,
                                                "${activePlan.titleEn} (${QuestPlanCatalog.formatPriceBdt(activePlan.priceBdtMonthly, lang)})",
                                                "${activePlan.titleBn} (${QuestPlanCatalog.formatPriceBdt(activePlan.priceBdtMonthly, lang)})"
                                            ),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = StudyQuestStrings.t(
                                                lang,
                                                "$aiLeft AI credits • $videoLeft Video credits remaining",
                                                "বাকি: ${StudyQuestStrings.formatDigits(aiLeft, lang)} AI ও ${StudyQuestStrings.formatDigits(videoLeft, lang)} ভিডিও ক্রেডিট"
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = onOpenAiLab,
                                            modifier = Modifier.testTag("profile_open_ai_lab_btn")
                                        ) {
                                            Text(StudyQuestStrings.t(lang, "AI Lab", "AI ল্যাব"))
                                        }
                                        Button(
                                            onClick = { pushDest(ProfileDestination.PlanStore) },
                                            modifier = Modifier.testTag("upgrade_premium_btn")
                                        ) {
                                            Text(StudyQuestStrings.t(lang, "Plans", "প্ল্যান"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                        ProfileNavTile(
                            icon = Icons.Filled.Analytics,
                            title = StudyQuestStrings.t(lang, "Academic Analytics & Charts", "অ্যাকাডেমিক বিশ্লেষণ ও চার্ট"),
                            subtitle = StudyQuestStrings.t(lang, "${learningRecords.size} records • Subject mastery & trends", "বিষয়ভিত্তিক দক্ষতা ও গ্রাফ"),
                            onClick = { pushDest(ProfileDestination.Analytics) },
                            testTag = "profile_open_analytics"
                        )
                    }
                }

                item {
                    StaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                        ProfileNavTile(
                            icon = Icons.Filled.ShoppingBag,
                            title = StudyQuestStrings.t(lang, "Scholar Emblems & Frames", "অ্যাভাটার, ফ্রেম ও টাইটেল"),
                            subtitle = StudyQuestStrings.t(lang, "Unlock & equip scholarly frames, avatars & titles", "অ্যাভাটার, ফ্রেম ও মর্যাদার টাইটেল আনলক করো"),
                            onClick = { pushDest(ProfileDestination.CosmeticsShop) },
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
                            onClick = { pushDest(ProfileDestination.SettingsAndSafety) },
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
                            onClick = { pushDest(ProfileDestination.AdminStudio) },
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
        }

        ProfileDestination.Analytics -> {
            BackHandler { popDest() }
            AnalyticsAndAchievementsScreen(
                lang = lang,
                learningRecords = learningRecords,
                subjectAnalytics = subjectAnalytics,
                weakTopics = weakTopics,
                dailyStudySeries = dailyStudySeries,
                friendCount = friendCount,
                matchCount = profile.competitionRating.let { if (it > 1000L) 1 else 0 },
                onBack = { popDest() }
            )
        }

        ProfileDestination.BadgesAndXp -> {
            BadgesAndXpScreen(
                profile = profile,
                uiLanguage = lang,
                userBadges = userBadges,
                xpLedger = xpLedger,
                isGuestMode = isGuestMode,
                onToggleFavorite = onToggleBadgeFavorite,
                onTogglePin = onToggleBadgePin,
                onBack = { popDest() }
            )
        }

        ProfileDestination.CosmeticsShop -> {
            BackHandler { popDest() }
            CosmeticsShopScreen(
                lang = lang,
                profile = profile,
                unlockedCosmetics = unlockedCosmetics,
                onBack = { popDest() },
                onPurchase = onPurchaseCosmetic,
                onEquip = onEquipCosmetic
            )
        }

        ProfileDestination.SettingsAndSafety -> {
            BackHandler { popDest() }
            PreferencesAndSafetyScreen(
                profile = profile,
                preferences = preferences,
                isGuestMode = isGuestMode,
                exportedJson = exportedJson,
                onClearExportedJson = onClearExportedJson,
                onBack = { popDest() },
                onUpdateUiLanguage = onUpdateUiLanguage,
                onUpdateQuestionLanguage = onUpdateQuestionLanguage,
                onUpdateThemeMode = onUpdateThemeMode,
                onUpdateSurfaceStyle = onUpdateSurfaceStyle,
                onUpdateReducedMotion = onUpdateReducedMotion,
                onUpdateSound = onUpdateSound,
                onUpdateQuietHours = onUpdateQuietHours,
                onUpdateNotifications = onUpdateNotifications,
                onUpdateNotificationCategory = onUpdateNotificationCategory,
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
                onBack = { popDest() }
            )
        }

        ProfileDestination.PlanStore -> {
            SubscriptionSelectionScreen(
                language = lang,
                subscription = subscription,
                billingConfigured = billingConfigured,
                aiCreditLedger = aiCreditLedger,
                videoCreditLedger = videoCreditLedger,
                purchaseHistory = purchaseHistory,
                onSelectPlan = onSelectPlan,
                onPurchaseVideoBoost = onPurchaseVideoBoost,
                onSwitchToFree = onSwitchToFreePlan,
                onOpenAiLab = {
                    pushDest(ProfileDestination.Home)
                    onOpenAiLab()
                },
                onBack = { popDest() }
            )
        }
    }
}

@Composable
private fun ProfileMetricChip(
    value: String,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.78f),
                maxLines = 1
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
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
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
    dailyStudySeries: List<DailyStudyMetric>,
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
            StudyTimeAndXpBarChart(dailySeries = dailyStudySeries, language = lang)
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
                        Text(
                            text = "${(item.averageAccuracy * 100).toInt()}% • ${item.lessonsCompleted + item.practiceSessions + item.examSessions} ${StudyQuestStrings.t(lang, "sessions", "সেশন")}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
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
            val semanticColors = LocalStudyQuestColors.current
            StudyQuestCard(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = if (unlocked) Icons.Filled.CheckCircle else Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = if (unlocked) semanticColors.success else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(StudyQuestStrings.t(lang, ach.titleEn, ach.titleBn), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(StudyQuestStrings.t(lang, ach.descriptionEn, ach.descriptionBn), style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = if (unlocked) StudyQuestStrings.t(lang, "Unlocked", "অর্জিত") else StudyQuestStrings.t(lang, "Milestone", "লক্ষ্য"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (unlocked) semanticColors.success else MaterialTheme.colorScheme.primary
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
    val semantic = LocalStudyQuestColors.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    Text(StudyQuestStrings.t(lang, "Scholar Emblems & Frames", "অ্যাভাটার ও ফ্রেম"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                StatBadgePill(
                    icon = Icons.Filled.WorkspacePremium,
                    label = StudyQuestStrings.t(lang, "${unlockedCosmetics.size} Unlocked", "${StudyQuestStrings.formatDigits(unlockedCosmetics.size, lang)}টি অর্জিত"),
                    tint = semantic.onPastelLavender,
                    containerColor = semantic.pastelLavender
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
                            onClick = { onPurchase(item.id, item.costCoins) }
                        ) {
                            Text(StudyQuestStrings.t(lang, "Unlock", "আনলক"))
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
    onUpdateThemeMode: (ThemeMode) -> Unit,
    onUpdateSurfaceStyle: (SurfaceStyle) -> Unit,
    onUpdateReducedMotion: (Boolean) -> Unit,
    onUpdateSound: (Boolean) -> Unit,
    onUpdateQuietHours: (Boolean) -> Unit,
    onUpdateNotifications: (Boolean) -> Unit,
    onUpdateNotificationCategory: (String, Boolean) -> Unit,
    onUpdateOptInNational: (Boolean) -> Unit,
    onUpdateOnlineStatusVisible: (Boolean) -> Unit,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onRequestGoogleSignIn: () -> Unit
) {
    val lang = preferences.uiLanguage
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_safety_list"),
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
                Spacer(Modifier.height(8.dp))
                Text(StudyQuestStrings.t(lang, "Appearance Theme", "থিম নির্বাচন"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = preferences.themeMode == mode,
                            onClick = { onUpdateThemeMode(mode) },
                            label = { Text(StudyQuestStrings.t(lang, mode.labelEn, mode.labelBn)) },
                            modifier = Modifier.testTag("settings_theme_${mode.code}")
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = StudyQuestStrings.t(lang, "Card & Surface Style", "কার্ড ও সারফেস স্টাইল"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = StudyQuestStrings.t(
                        lang,
                        "Switch between crisp Vivid Classic cards or translucent Glassmorphism.",
                        "উজ্জ্বল ভিভিড ক্লাসিক কার্ড অথবা স্বচ্ছ গ্লাসমরফিজম স্টাইল বেছে নাও।"
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SurfaceStyle.entries.forEach { style ->
                        FilterChip(
                            selected = preferences.surfaceStyle == style,
                            onClick = { onUpdateSurfaceStyle(style) },
                            label = { Text(StudyQuestStrings.t(lang, style.labelEn, style.labelBn)) },
                            modifier = Modifier.testTag("settings_surface_${style.code}")
                        )
                    }
                }
            }
        }

        item {
            StudyQuestCard {
                SettingToggleRow(StudyQuestStrings.t(lang, "Reduced Motion (Accessibility)", "কম অ্যানিমেশন"), preferences.reducedMotion, onUpdateReducedMotion)
                SettingToggleRow(StudyQuestStrings.t(lang, "Sound Effects & Haptics", "সাউন্ড ও হ্যাপটিক্স"), preferences.soundEnabled, onUpdateSound)
                SettingToggleRow(StudyQuestStrings.t(lang, "All Study Notifications", "সকল নোটিফিকেশন"), preferences.notificationsEnabled, onUpdateNotifications)
                SettingToggleRow(StudyQuestStrings.t(lang, "Quiet Hours (10 PM – 6 AM Dhaka)", "নীরব সময় (রাত ১০টা – সকাল ৬টা)"), preferences.quietHoursEnabled, onUpdateQuietHours)
                if (!isGuestMode) {
                    SettingToggleRow(StudyQuestStrings.t(lang, "Opt-in National Leaderboard", "জাতীয় লিডারবোর্ডে অংশগ্রহণ"), profile.optInNationalLeaderboard, onUpdateOptInNational)
                    SettingToggleRow(StudyQuestStrings.t(lang, "Show Online Status to Friends", "অনলাইন অবস্থা দেখান"), profile.showOnlineStatus, onUpdateOnlineStatusVisible)
                }
            }
        }

        if (preferences.notificationsEnabled) {
            item {
                StudyQuestCard(modifier = Modifier.testTag("settings_notification_categories")) {
                    Text(
                        text = StudyQuestStrings.t(lang, "Notification Categories", "নোটিফিকেশনের ধরন"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    SettingToggleRow(StudyQuestStrings.t(lang, "Study & Streak Reminders", "পড়া ও স্ট্রিক রিমাইন্ডার"), preferences.notifStudyEnabled) {
                        onUpdateNotificationCategory("study", it)
                    }
                    SettingToggleRow(StudyQuestStrings.t(lang, "Streak & Milestone Alerts", "স্ট্রিক ও মাইলফলক এলার্ট"), preferences.notifXpEnabled) {
                        onUpdateNotificationCategory("xp", it)
                    }
                    SettingToggleRow(StudyQuestStrings.t(lang, "Badge Unlocks", "ব্যাজ আনলক"), preferences.notifBadgeEnabled) {
                        onUpdateNotificationCategory("badge", it)
                    }
                    SettingToggleRow(StudyQuestStrings.t(lang, "Assignments & Deadlines", "অ্যাসাইনমেন্ট"), preferences.notifAssignmentEnabled) {
                        onUpdateNotificationCategory("assignment", it)
                    }
                    SettingToggleRow(StudyQuestStrings.t(lang, "Competitions & Ranks", "প্রতিযোগিতা ও র‍্যাংক"), preferences.notifCompetitionEnabled) {
                        onUpdateNotificationCategory("competition", it)
                    }
                    SettingToggleRow(StudyQuestStrings.t(lang, "Mock & Promotion Exams", "মডেল ও বার্ষিক পরীক্ষা"), preferences.notifExamEnabled) {
                        onUpdateNotificationCategory("exam", it)
                    }
                    SettingToggleRow(StudyQuestStrings.t(lang, "Class Community Replies", "কমিউনিটি উত্তর"), preferences.notifCommunityEnabled) {
                        onUpdateNotificationCategory("community", it)
                    }
                }
            }
        }

        item {
            StudyQuestCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
            val ctx = LocalContext.current
            val firebaseOpts = remember {
                runCatching { FirebaseApp.getInstance().options }.getOrNull()
            }
            val dbId = stringResource(R.string.firestore_database_id)
            StudyQuestCard(modifier = Modifier.testTag("firebase_project_info_card")) {
                Text(
                    text = StudyQuestStrings.t(lang, "Firebase Project & App Info", "ফায়ারবেস প্রোজেক্ট ও অ্যাপ তথ্য"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "App Name: ${stringResource(R.string.app_name)}\n" +
                        "Android Package: ${ctx.packageName}\n" +
                        "Firebase Project ID: ${firebaseOpts?.projectId ?: "unconfigured"}\n" +
                        "Firestore Database ID: $dbId\n" +
                        "Storage Bucket: ${firebaseOpts?.storageBucket ?: "unconfigured"}",
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
