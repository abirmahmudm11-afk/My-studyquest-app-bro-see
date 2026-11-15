package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.BillingAndAdProvider
import com.example.core.StudyQuestStrings
import com.example.data.model.AiCreditLedgerDoc
import com.example.data.model.PurchaseRecordDoc
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.QuestPlanDefinition
import com.example.data.model.UserSubscriptionDoc
import com.example.data.model.VideoCreditLedgerDoc
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.LocalStudyQuestColors

@Composable
fun WelcomePlanSelectionScreen(
    language: AppLanguage,
    billingConfigured: Boolean,
    onContinueWithFree: () -> Unit,
    onSelectPaidPlan: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    BackHandler { onBack() }

    var selectedPlanId by remember { mutableStateOf("free") }
    val plans = remember { QuestPlanCatalog.getPlanCatalog() }
    val semantic = LocalStudyQuestColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("welcome_plan_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = StudyQuestStrings.t(language, "Back", "ফিরে যাও")
                            )
                        }
                        Text(
                            text = StudyQuestStrings.t(language, "Step 5 of 5 • Choose Your Plan", "ধাপ ৫/৫ • তোমার প্ল্যান নির্বাচন করো"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = StudyQuestStrings.t(language, "Start Free or Explore StudyQuest Plans", "ফ্রিতে শুরু করো অথবা StudyQuest প্ল্যান দেখো"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "Every student gets full NCTB Classes 6–10 lessons, MCQ/CQ bank, Mock Exams, Focus Timer, public Video Library, 20 AI credits/mo, and 1 animated video/mo free.",
                            "প্রত্যেক শিক্ষার্থী সম্পূর্ণ এনসিটিবি পাঠ, MCQ/CQ ব্যাংক, মডেল টেস্ট, ফোকাস, পাবলিক ভিডিও লাইব্রেরি, মাসে ২০টি AI ক্রেডিট ও ১টি ভিডিও সম্পূর্ণ ফ্রিতে পাবে।"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(plans, key = { it.id }) { plan ->
                val isSelected = selectedPlanId == plan.id
                QuestPlanItemCard(
                    plan = plan,
                    language = language,
                    isSelected = isSelected,
                    isActive = plan.id == "free",
                    onSelect = { selectedPlanId = plan.id }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chosen = QuestPlanCatalog.planById(selectedPlanId)
                Button(
                    onClick = {
                        if (chosen.id == "free") {
                            onContinueWithFree()
                        } else {
                            onSelectPaidPlan(chosen.id)
                            onContinueWithFree()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("welcome_plan_continue_btn")
                ) {
                    Text(
                        text = if (chosen.id == "free") {
                            StudyQuestStrings.t(language, "Continue with Free Plan (৳0)", "ফ্রি প্ল্যান দিয়ে শুরু করো (৳০)")
                        } else {
                            val label = if (billingConfigured) "Continue with" else "Try Preview & Start Free —"
                            StudyQuestStrings.t(
                                language,
                                "$label ${chosen.titleEn} (${QuestPlanCatalog.formatPriceBdt(chosen.priceBdtMonthly, language)})",
                                "${chosen.titleBn} (${QuestPlanCatalog.formatPriceBdt(chosen.priceBdtMonthly, language)})"
                            )
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                if (selectedPlanId != "free") {
                    OutlinedButton(
                        onClick = onContinueWithFree,
                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                    ) {
                        Text(StudyQuestStrings.t(language, "Continue with Free StudyQuest Instead (৳0)", "পরিবর্তে ফ্রি প্ল্যান ব্যবহার করো"))
                    }
                } else {
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "No card required. Puter.js Pay-As-You-Go AI is also available anytime.",
                            "কোনো কার্ড লাগবে না। যেকোনো সময় Puter.js পে-অ্যাজ-ইউ-গো ব্যবহার করতে পারবে।"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = semantic.success
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionSelectionScreen(
    language: AppLanguage,
    subscription: UserSubscriptionDoc,
    billingConfigured: Boolean,
    aiCreditLedger: List<AiCreditLedgerDoc> = emptyList(),
    videoCreditLedger: List<VideoCreditLedgerDoc> = emptyList(),
    purchaseHistory: List<PurchaseRecordDoc> = emptyList(),
    onSelectPlan: (String) -> Unit,
    onPurchaseVideoBoost: () -> Unit,
    onSwitchToFree: () -> Unit,
    onOpenAiLab: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) } // 0=My Plan, 1=Plan Store, 2=Video Add-on, 3=History
    val normalized = remember(subscription) { BillingAndAdProvider.normalizeSubscription(subscription) }
    val activePlan = remember(normalized.tier) { QuestPlanCatalog.planById(normalized.tier) }
    val aiRemaining = (normalized.monthlyAiLimit.toInt().coerceAtLeast(activePlan.monthlyAiCredits) - normalized.aiCreditsUsed.toInt()).coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("plans_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = StudyQuestStrings.t(language, "Quest Plan Store", "কোয়েস্ট প্ল্যান স্টোর"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "Active: ${activePlan.titleEn} • $aiRemaining AI cr left",
                        "সক্রিয়: ${activePlan.titleBn} • ${StudyQuestStrings.formatDigits(aiRemaining, language)} AI ক্রেডিট বাকি"
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(StudyQuestStrings.t(language, "My Plan", "আমার প্ল্যান")) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(StudyQuestStrings.t(language, "Plans", "প্ল্যানসমূহ")) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(StudyQuestStrings.t(language, "Video", "ভিডিও")) })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text(StudyQuestStrings.t(language, "History", "ইতিহাস")) })
        }

        when (selectedTab) {
            0 -> MyPlansScreen(
                language = language,
                subscription = normalized,
                billingConfigured = billingConfigured,
                onBrowsePlans = { selectedTab = 1 },
                onOpenVideoUpgrade = { selectedTab = 2 },
                onOpenAiLab = onOpenAiLab,
                onSwitchToFree = onSwitchToFree
            )

            1 -> {
                val plans = remember { QuestPlanCatalog.getPlanCatalog() }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!billingConfigured) {
                        item {
                            StudyQuestCard(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)) {
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "Play Console Billing is not linked in this debug build yet. All Free StudyQuest tools (20 AI credits/mo + 1 animated video/mo) and Puter.js Pay-As-You-Go work today.",
                                        "এই বিল্ডে Play Console Billing এখনো লিংক করা হয়নি। তবে ফ্রি প্ল্যান ও Puter.js Pay-As-You-Go সম্পূর্ণ সক্রিয় আছে।"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(plans, key = { it.id }) { plan ->
                        val isActive = normalized.tier == plan.id
                        QuestPlanItemCard(
                            plan = plan,
                            language = language,
                            isSelected = isActive,
                            isActive = isActive,
                            onSelect = {
                                if (plan.id == "free") onSwitchToFree()
                                else onSelectPlan(plan.id)
                            },
                            actionLabel = if (isActive) {
                                StudyQuestStrings.t(language, "Current Plan", "বর্তমান প্ল্যান")
                            } else if (plan.id == "free") {
                                StudyQuestStrings.t(language, "Use Free Plan (৳0)", "ফ্রি প্ল্যান বেছে নিন")
                            } else {
                                StudyQuestStrings.t(
                                    language,
                                    "Choose ${plan.titleEn} • ৳${plan.priceBdtMonthly}/mo",
                                    "বেছে নিন • ${QuestPlanCatalog.formatPriceBdt(plan.priceBdtMonthly, language)}"
                                )
                            }
                        )
                    }
                }
            }

            2 -> VideoUpgradeScreen(
                language = language,
                subscription = normalized,
                onSelectVideoPass = { onSelectPlan("video_pass") },
                onSelectCreatorBundle = { onSelectPlan("creator") },
                onPurchaseVideoBoost = onPurchaseVideoBoost
            )

            3 -> PurchaseHistoryScreen(
                language = language,
                aiCreditLedger = aiCreditLedger,
                videoCreditLedger = videoCreditLedger,
                purchaseHistory = purchaseHistory
            )
        }
    }
}

@Composable
fun MyPlansScreen(
    language: AppLanguage,
    subscription: UserSubscriptionDoc,
    billingConfigured: Boolean,
    onBrowsePlans: () -> Unit,
    onOpenVideoUpgrade: () -> Unit,
    onOpenAiLab: () -> Unit,
    onSwitchToFree: () -> Unit
) {
    val plan = QuestPlanCatalog.planById(subscription.tier)
    val aiLimit = subscription.monthlyAiLimit.toInt().coerceAtLeast(plan.monthlyAiCredits)
    val aiUsed = subscription.aiCreditsUsed.toInt().coerceAtLeast(0)
    val aiRemaining = (aiLimit - aiUsed).coerceAtLeast(0)

    val vidLimit = subscription.monthlyVideoLimit.toInt().coerceAtLeast(plan.monthlyVideoCredits)
    val vidUsed = subscription.videoCreditsUsed.toInt().coerceAtLeast(0)
    val vidRemaining = ((vidLimit - vidUsed).coerceAtLeast(0) + subscription.bonusVideoCredits.toInt().coerceAtLeast(0))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StudyQuestCard(
                modifier = Modifier.testTag("my_plan_summary_card")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StudyQuestStrings.t(language, plan.titleEn, plan.titleBn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = QuestPlanCatalog.formatPriceBdt(plan.priceBdtMonthly, language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (plan.adFree) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (plan.adFree) StudyQuestStrings.t(language, "Ad-Free", "বিজ্ঞাপনমুক্ত")
                            else StudyQuestStrings.t(language, "Free Tier", "ফ্রি"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "StudyQuest AI Credits: $aiRemaining left of $aiLimit this month",
                        "StudyQuest AI ক্রেডিট: $aiLimit এর মধ্যে ${aiRemaining}টি বাকি"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (aiRemaining.toFloat() / aiLimit.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "Animated Video Credits: $vidRemaining total left ($vidUsed/$vidLimit monthly + ${subscription.bonusVideoCredits} Video Boost)",
                        "অ্যানিমেটেড ভিডিও ক্রেডিট: মোট ${vidRemaining}টি বাকি"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { ((vidLimit - vidUsed).coerceAtLeast(0).toFloat() / vidLimit.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onBrowsePlans,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                    ) {
                        Text(StudyQuestStrings.t(language, "Compare Plans", "সব প্ল্যান দেখো"))
                    }
                    OutlinedButton(
                        onClick = onOpenAiLab,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                    ) {
                        Text(StudyQuestStrings.t(language, "Open AI Lab", "AI ল্যাব"))
                    }
                }
            }
        }

        item {
            StudyQuestCard {
                Text(
                    text = StudyQuestStrings.t(language, "StudyQuest AI Standalone (৳150/mo) vs Puter.js", "StudyQuest AI (৳১৫০/মাস) বনাম Puter.js"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "• StudyQuest AI Subscription (৳150/mo): Owner-funded OpenRouter tutor with 100 monthly credits and zero ads.\n• Puter.js Pay-As-You-Go: Sign into your own Puter account inside StudyQuest and pay Puter directly whenever you need extra tutoring.",
                        "• StudyQuest AI (৳১৫০/মাস): মাসিক ১০০টি ক্রেডিট ও বিজ্ঞাপনমুক্ত পড়াশোনা।\n• Puter.js: নিজস্ব Puter অ্যাকাউন্ট দিয়ে সরাসরি পে-অ্যাজ-ইউ-গো ব্যবহার।"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onOpenVideoUpgrade, modifier = Modifier.weight(1f)) {
                        Text(StudyQuestStrings.t(language, "Video Boost (৳20)", "ভিডিও বুস্ট (৳২০)"))
                    }
                    if (plan.id != "free") {
                        OutlinedButton(onClick = onSwitchToFree, modifier = Modifier.weight(1f)) {
                            Text(StudyQuestStrings.t(language, "Use Free Plan", "ফ্রি প্ল্যান"))
                        }
                    }
                }
                if (!billingConfigured) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Billing status: Google Play Billing unconfigured in local/debug build.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VideoUpgradeScreen(
    language: AppLanguage,
    subscription: UserSubscriptionDoc,
    onSelectVideoPass: () -> Unit,
    onSelectCreatorBundle: () -> Unit,
    onPurchaseVideoBoost: () -> Unit
) {
    val videoPass = QuestPlanCatalog.planById("video_pass")
    val creator = QuestPlanCatalog.planById("creator")
    val boost = QuestPlanCatalog.videoBoost

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StudyQuestCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StudyQuestStrings.t(language, boost.titleEn, boost.titleBn),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = QuestPlanCatalog.formatPriceBdt(boost.priceBdtOneTime, language, recurring = false),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "One-time non-recurring top-up: adds 1 animated short video (up to 60s) without changing your monthly plan. Current bonus credits: ${subscription.bonusVideoCredits}.",
                        "এককালীন রিচার্জ: মাসিক প্ল্যান না বদলেই ১টি অতিরিক্ত ৬০ সেকেন্ডের অ্যানিমেটেড ভিডিও তৈরি করুন।"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onPurchaseVideoBoost,
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("buy_video_boost_btn")
                ) {
                    Text(StudyQuestStrings.t(language, "Get Video Boost — ৳20 (One-Time)", "ভিডিও বুস্ট নিন — ৳২০"))
                }
            }
        }

        item {
            QuestPlanItemCard(
                plan = videoPass,
                language = language,
                isSelected = subscription.tier == videoPass.id,
                isActive = subscription.tier == videoPass.id,
                onSelect = onSelectVideoPass,
                actionLabel = StudyQuestStrings.t(language, "Choose Video Pass (৳99/mo)", "ভিডিও পাস নিন (৳৯৯/মাস)")
            )
        }

        item {
            QuestPlanItemCard(
                plan = creator,
                language = language,
                isSelected = subscription.tier == creator.id,
                isActive = subscription.tier == creator.id,
                onSelect = onSelectCreatorBundle,
                actionLabel = StudyQuestStrings.t(language, "Choose Creator Bundle (৳329/mo)", "ক্রিয়েটর বান্ডেল (৳৩২৯/মাস)")
            )
        }
    }
}

@Composable
fun PurchaseHistoryScreen(
    language: AppLanguage,
    aiCreditLedger: List<AiCreditLedgerDoc>,
    videoCreditLedger: List<VideoCreditLedgerDoc>,
    purchaseHistory: List<PurchaseRecordDoc>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = StudyQuestStrings.t(language, "Purchases & Play Billing Log (${purchaseHistory.size})", "ক্রয় ও বিলিং ইতিহাস (${purchaseHistory.size})"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (purchaseHistory.isEmpty()) {
            item {
                EmptyStateView(
                    titleEn = "No purchase records yet",
                    titleBn = "এখনো কোনো ক্রয়ের রেকর্ড নেই",
                    subtitleEn = "Any subscription checkout attempts or Play Console purchases appear here.",
                    subtitleBn = "যেকোনো প্ল্যান বা ভিডিও বুস্ট লেনদেন এখানে দেখা যাবে।",
                    language = language
                )
            }
        } else {
            items(purchaseHistory.take(15), key = { it.id }) { p ->
                StudyQuestCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${p.title} • ৳${p.amountBdt}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Type: ${p.purchaseType} • Status: ${p.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                text = StudyQuestStrings.t(language, "AI Credit Ledger (${aiCreditLedger.size})", "AI ক্রেডিট হিসাব (${aiCreditLedger.size})"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(aiCreditLedger.take(20), key = { it.id }) { item ->
            StudyQuestCard {
                Text("${item.entryType.uppercase()} (${if (item.delta > 0) "+${item.delta}" else "${item.delta}"} cr) • ${item.toolId}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${item.modelUsed} • ${item.note}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (videoCreditLedger.isNotEmpty()) {
            item {
                Text(
                    text = StudyQuestStrings.t(language, "Video Credit Ledger (${videoCreditLedger.size})", "ভিডিও ক্রেডিট হিসাব"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(videoCreditLedger.take(15), key = { it.id }) { item ->
                StudyQuestCard {
                    Text("${item.entryType.uppercase()} (${if (item.delta > 0) "+${item.delta}" else "${item.delta}"})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(item.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun QuestPlanItemCard(
    plan: QuestPlanDefinition,
    language: AppLanguage,
    isSelected: Boolean,
    isActive: Boolean,
    onSelect: () -> Unit,
    actionLabel: String? = null
) {
    val semantic = LocalStudyQuestColors.current
    val highlightStudyQuestAi = plan.id == QuestPlanCatalog.STUDYQUEST_AI_PLAN_ID
    StudyQuestCard(
        onClick = onSelect,
        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.testTag("plan_card_${plan.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = StudyQuestStrings.t(language, plan.titleEn, plan.titleBn),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (highlightStudyQuestAi || isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isActive) StudyQuestStrings.t(language, "Active", "সক্রিয়")
                            else StudyQuestStrings.t(language, plan.badgeEn, plan.badgeBn),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (highlightStudyQuestAi || isActive) semantic.success else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "${plan.monthlyAiCredits} AI credits/mo • ${plan.monthlyVideoCredits} animated videos/mo",
                        "মাসে ${StudyQuestStrings.formatDigits(plan.monthlyAiCredits, language)} AI ক্রেডিট • ${StudyQuestStrings.formatDigits(plan.monthlyVideoCredits, language)}টি ভিডিও"
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = QuestPlanCatalog.formatPriceBdt(plan.priceBdtMonthly, language),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(6.dp))
        val points = if (language == AppLanguage.BANGLA) plan.highlightsBn else plan.highlightsEn
        points.forEach { bullet ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 1.dp)) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = semantic.success, modifier = Modifier.size(14.dp))
                Text(bullet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (actionLabel != null) {
            Spacer(Modifier.height(10.dp))
            if (isSelected) {
                FilledTonalButton(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                ) {
                    Text(actionLabel, fontWeight = FontWeight.SemiBold)
                }
            } else {
                OutlinedButton(
                    onClick = onSelect,
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
