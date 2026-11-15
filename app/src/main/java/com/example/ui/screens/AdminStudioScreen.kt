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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.model.XpRuleConfig
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.viewmodel.AdminStudioViewModel
import org.json.JSONObject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminStudioScreen(
    adminRole: String,
    language: AppLanguage,
    viewModel: AdminStudioViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val settings by viewModel.systemSettings.collectAsStateWithLifecycle()
    val content by viewModel.academicContent.collectAsStateWithLifecycle()
    val cases by viewModel.moderationCases.collectAsStateWithLifecycle()
    val logs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val badgeDefs by viewModel.badgeDefinitions.collectAsStateWithLifecycle()
    val allAssignments by viewModel.allAssignments.collectAsStateWithLifecycle()
    val flaggedPosts by viewModel.flaggedPosts.collectAsStateWithLifecycle()
    val aiProviderSettings by viewModel.aiProviderSettings.collectAsStateWithLifecycle()
    val aiUsageLogs by viewModel.aiUsageLogs.collectAsStateWithLifecycle()
    val modelCheckResult by viewModel.modelCheckResult.collectAsStateWithLifecycle()
    val isVerifyingModels by viewModel.isVerifyingModels.collectAsStateWithLifecycle()
    val businessInsights by viewModel.businessInsights.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Editorial, 1=Moderation, 2=XP & Tasks, 3=System, 4=Audit, 5=AI Provider, 6=Business Insights

    LaunchedEffect(selectedTab, aiUsageLogs) {
        if (selectedTab == 6) {
            viewModel.refreshBusinessInsights()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Column(modifier = Modifier.weight(1f)) {
                Text("Admin Studio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Role: $adminRole", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }

        if (!message.isNullOrBlank()) {
            StudyQuestCard(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(message.orEmpty(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { viewModel.clearMessage() }) { Text("OK") }
                }
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Editorial") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Moderation (${cases.count { it.status == "open" } + flaggedPosts.size})") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("XP & Tasks") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("System") })
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Audit") })
            Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }, text = { Text("AI Provider") })
            Tab(selected = selectedTab == 6, onClick = { selectedTab = 6 }, text = { Text("Insights") })
        }

        when (selectedTab) {
            0 -> {
                var qTitleEn by remember { mutableStateOf("") }
                var qTitleBn by remember { mutableStateOf("") }
                var qOpt0 by remember { mutableStateOf("") }
                var qOpt1 by remember { mutableStateOf("") }
                var qOpt2 by remember { mutableStateOf("") }
                var qOpt3 by remember { mutableStateOf("") }
                var qExpl by remember { mutableStateOf("") }
                var qSource by remember { mutableStateOf("StudyQuest Official Editorial") }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        StudyQuestCard {
                            Text("Create Draft Question (6-Stage Pipeline)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = qTitleEn, onValueChange = { qTitleEn = it }, label = { Text("Prompt (English)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = qTitleBn, onValueChange = { qTitleBn = it }, label = { Text("Prompt (Bangla)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = qOpt0, onValueChange = { qOpt0 = it }, label = { Text("Option A (Correct)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = qOpt1, onValueChange = { qOpt1 = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = qOpt2, onValueChange = { qOpt2 = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = qOpt3, onValueChange = { qOpt3 = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = qExpl, onValueChange = { qExpl = it }, label = { Text("Explanation") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = qSource, onValueChange = { qSource = it }, label = { Text("Source Attribution") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (qTitleEn.isNotBlank() && qOpt0.isNotBlank()) {
                                        viewModel.createDraftQuestion(
                                            classId = "class_10",
                                            subjectId = "math",
                                            titleEn = qTitleEn,
                                            titleBn = qTitleBn.ifBlank { qTitleEn },
                                            optionsEn = listOf(qOpt0, qOpt1.ifBlank { "Option B" }, qOpt2.ifBlank { "Option C" }, qOpt3.ifBlank { "Option D" }),
                                            optionsBn = listOf(qOpt0, qOpt1.ifBlank { "খ" }, qOpt2.ifBlank { "গ" }, qOpt3.ifBlank { "ঘ" }),
                                            correctIndex = 0,
                                            explanationEn = qExpl.ifBlank { qTitleEn },
                                            explanationBn = qExpl.ifBlank { qTitleBn },
                                            sourceAttribution = qSource
                                        )
                                        qTitleEn = ""; qTitleBn = ""; qOpt0 = ""; qOpt1 = ""; qOpt2 = ""; qOpt3 = ""; qExpl = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_create_draft_btn")
                            ) {
                                Text("Save Draft")
                            }
                        }
                    }

                    items(content, key = { it.id }) { doc ->
                        val nextStage = when (doc.status) {
                            "draft" -> "source_verification"
                            "source_verification" -> "academic_review"
                            "academic_review" -> "language_review"
                            "language_review" -> "approved"
                            "approved" -> "published"
                            else -> null
                        }
                        StudyQuestCard {
                            Text(StudyQuestStrings.t(language, doc.titleEn, doc.titleBn), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Status: ${doc.status} • v${doc.version} • ${doc.sourceAttribution ?: ""}", style = MaterialTheme.typography.labelSmall)
                            if (nextStage != null) {
                                Spacer(Modifier.height(6.dp))
                                FilledTonalButton(onClick = { viewModel.advanceEditorialStatus(doc, nextStage) }) {
                                    Text("Advance -> $nextStage")
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (flaggedPosts.isNotEmpty()) {
                        item {
                            Text("Flagged Class Community Posts (${flaggedPosts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        items(flaggedPosts, key = { "flag_${it.id}" }) { post ->
                            StudyQuestCard {
                                Text("${post.classId} • ${post.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(post.body, style = MaterialTheme.typography.bodySmall)
                                Text("Author: ${post.authorName} • Hidden: ${post.hidden}", style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.height(6.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { viewModel.moderateCommunityPost(post.id, hidePost = true) }) {
                                        Text("Hide Post")
                                    }
                                    OutlinedButton(onClick = { viewModel.moderateCommunityPost(post.id, hidePost = false) }) {
                                        Text("Approve & Unflag")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Safety Reports Queue (${cases.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(cases, key = { it.id }) { c ->
                        StudyQuestCard {
                            Text("${c.targetType.uppercase()} • ${c.reason}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Status: ${c.status} • Details: ${c.details ?: ""}", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.resolveModerationCase(c, "resolved", "Reviewed & action taken") }) {
                                    Text("Resolve")
                                }
                                OutlinedButton(onClick = { viewModel.resolveModerationCase(c, "dismissed", "No violation") }) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                val parsedRules = remember(settings.xpRulesJson) {
                    runCatching {
                        settings.xpRulesJson?.takeIf { it.isNotBlank() }?.let { raw ->
                            val obj = JSONObject(raw)
                            XpRuleConfig(
                                lessonXp = obj.optLong("lessonXp", 20L),
                                practiceXpPerCorrect = obj.optLong("practiceXpPerCorrect", 4L),
                                examXpBase = obj.optLong("examXpBase", 30L),
                                focusXpPerMinute = obj.optLong("focusXpPerMinute", 2L),
                                dailyChallengeXp = obj.optLong("dailyChallengeXp", 45L),
                                streakBonusPerDay = obj.optLong("streakBonusPerDay", 5L),
                                communityHelpfulXp = obj.optLong("communityHelpfulXp", 15L),
                                maxDailyFocusXp = obj.optLong("maxDailyFocusXp", 240L),
                                maxDailyCommunityXp = obj.optLong("maxDailyCommunityXp", 60L)
                            )
                        }
                    }.getOrNull() ?: XpRuleConfig()
                }
                var lessonXpText by remember(parsedRules) { mutableStateOf(parsedRules.lessonXp.toString()) }
                var examPassXpText by remember(parsedRules) { mutableStateOf(parsedRules.examXpBase.toString()) }
                var dailyChallengeXpText by remember(parsedRules) { mutableStateOf(parsedRules.dailyChallengeXp.toString()) }
                var focusXpText by remember(parsedRules) { mutableStateOf(parsedRules.focusXpPerMinute.toString()) }
                var dailyCapText by remember(parsedRules) { mutableStateOf(parsedRules.maxDailyFocusXp.toString()) }

                var assignTitleEn by remember { mutableStateOf("") }
                var assignTitleBn by remember { mutableStateOf("") }
                var assignType by remember { mutableStateOf("weekly") }
                var assignClass by remember { mutableStateOf("class_10") }
                var assignSubject by remember { mutableStateOf("math") }
                var assignDifficulty by remember { mutableStateOf("medium") }
                var assignTargetText by remember { mutableStateOf("3") }
                var assignXpText by remember { mutableStateOf("80") }

                var badgeIdInput by remember { mutableStateOf("badge_custom_scholar") }
                var badgeTitleEn by remember { mutableStateOf("Term Star") }
                var badgeTitleBn by remember { mutableStateOf("টার্ম তারকা") }
                var badgeTargetText by remember { mutableStateOf("5") }
                var badgeXpText by remember { mutableStateOf("75") }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StudyQuestCard {
                            Text("XP Rules & Anti-Farming Caps", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = lessonXpText, onValueChange = { lessonXpText = it.filter(Char::isDigit) }, label = { Text("Lesson XP") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = examPassXpText, onValueChange = { examPassXpText = it.filter(Char::isDigit) }, label = { Text("Exam Base XP") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = dailyChallengeXpText, onValueChange = { dailyChallengeXpText = it.filter(Char::isDigit) }, label = { Text("Daily Challenge XP") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = focusXpText, onValueChange = { focusXpText = it.filter(Char::isDigit) }, label = { Text("Focus XP per Minute") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = dailyCapText, onValueChange = { dailyCapText = it.filter(Char::isDigit) }, label = { Text("Daily Anti-Farming Focus XP Cap") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.saveXpRules(
                                        parsedRules.copy(
                                            lessonXp = lessonXpText.toLongOrNull() ?: parsedRules.lessonXp,
                                            examXpBase = examPassXpText.toLongOrNull() ?: parsedRules.examXpBase,
                                            dailyChallengeXp = dailyChallengeXpText.toLongOrNull() ?: parsedRules.dailyChallengeXp,
                                            focusXpPerMinute = focusXpText.toLongOrNull() ?: parsedRules.focusXpPerMinute,
                                            maxDailyFocusXp = dailyCapText.toLongOrNull() ?: parsedRules.maxDailyFocusXp
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("admin_save_xp_rules_btn")
                            ) {
                                Text("Save XP Rules")
                            }
                        }
                    }

                    item {
                        StudyQuestCard {
                            Text("Publish Period Assignment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = assignTitleEn, onValueChange = { assignTitleEn = it }, label = { Text("Title (EN)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = assignTitleBn, onValueChange = { assignTitleBn = it }, label = { Text("Title (BN)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("daily", "weekly", "two_week", "monthly").forEach { tp ->
                                    FilterChip(selected = assignType == tp, onClick = { assignType = tp }, label = { Text(tp) })
                                }
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("easy", "medium", "hard").forEach { d ->
                                    FilterChip(selected = assignDifficulty == d, onClick = { assignDifficulty = d }, label = { Text(d) })
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = assignClass, onValueChange = { assignClass = it }, label = { Text("Class ID") }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = assignSubject, onValueChange = { assignSubject = it }, label = { Text("Subject ID") }, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = assignTargetText, onValueChange = { assignTargetText = it.filter(Char::isDigit) }, label = { Text("Target Count") }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = assignXpText, onValueChange = { assignXpText = it.filter(Char::isDigit) }, label = { Text("XP Reward") }, modifier = Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (assignTitleEn.isNotBlank()) {
                                        viewModel.createAssignment(
                                            titleEn = assignTitleEn,
                                            titleBn = assignTitleBn.ifBlank { assignTitleEn },
                                            descriptionEn = "Published by StudyQuest Admin",
                                            assignmentType = assignType,
                                            classId = assignClass,
                                            subjectId = assignSubject,
                                            difficulty = assignDifficulty,
                                            targetCount = assignTargetText.toLongOrNull() ?: 3L,
                                            xpReward = assignXpText.toLongOrNull() ?: 80L
                                        )
                                        assignTitleEn = ""
                                        assignTitleBn = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("admin_publish_assignment_btn")
                            ) {
                                Text("Publish Assignment (${allAssignments.size} total)")
                            }
                        }
                    }

                    item {
                        StudyQuestCard {
                            Text("Badge Definitions Override (${badgeDefs.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = badgeIdInput, onValueChange = { badgeIdInput = it }, label = { Text("Badge ID") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = badgeTitleEn, onValueChange = { badgeTitleEn = it }, label = { Text("Title (EN)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = badgeTitleBn, onValueChange = { badgeTitleBn = it }, label = { Text("Title (BN)") }, modifier = Modifier.fillMaxWidth())
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = badgeTargetText, onValueChange = { badgeTargetText = it.filter(Char::isDigit) }, label = { Text("Target") }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = badgeXpText, onValueChange = { badgeXpText = it.filter(Char::isDigit) }, label = { Text("XP Bonus") }, modifier = Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (badgeIdInput.isNotBlank()) {
                                        viewModel.saveBadgeDefinition(
                                            badgeId = badgeIdInput,
                                            category = "study",
                                            rarity = "rare",
                                            titleEn = badgeTitleEn,
                                            titleBn = badgeTitleBn,
                                            descriptionEn = "Custom admin badge milestone",
                                            target = badgeTargetText.toLongOrNull() ?: 5L,
                                            xpBonus = badgeXpText.toLongOrNull() ?: 75L,
                                            enabled = true
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Badge Definition")
                            }
                        }
                    }
                }
            }

            3 -> {
                var aiEnabled by remember(settings.aiSolverEnabled) { mutableStateOf(settings.aiSolverEnabled) }
                var billingEnabled by remember(settings.billingConfigured) { mutableStateOf(settings.billingConfigured) }
                var adMobEnabled by remember(settings.adMobConfigured) { mutableStateOf(settings.adMobConfigured) }
                var rendererEnabled by remember(settings.externalVideoRendererConfigured) { mutableStateOf(settings.externalVideoRendererConfigured) }
                var seasonEn by remember(settings.seasonNameEn) { mutableStateOf(settings.seasonNameEn) }
                var tourTitleEn by remember { mutableStateOf("SSC National Math Cup") }
                var tourTitleBn by remember { mutableStateOf("এসএসসি জাতীয় গণিত কাপ") }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StudyQuestCard {
                            Text("System, Integrations & Season Controls", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Enable AI Doubt Solver (Gemini)")
                                Switch(checked = aiEnabled, onCheckedChange = { aiEnabled = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Play Billing Configured")
                                Switch(checked = billingEnabled, onCheckedChange = { billingEnabled = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("AdMob Banner Configured")
                                Switch(checked = adMobEnabled, onCheckedChange = { adMobEnabled = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("External MP4 Video Renderer Configured")
                                Switch(checked = rendererEnabled, onCheckedChange = { rendererEnabled = it })
                            }
                            OutlinedTextField(value = seasonEn, onValueChange = { seasonEn = it }, label = { Text("Active Season Title") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateSystemSettings(
                                        aiEnabled = aiEnabled,
                                        seasonEn = seasonEn,
                                        seasonBn = settings.seasonNameBn,
                                        billingConfigured = billingEnabled,
                                        adMobConfigured = adMobEnabled,
                                        externalVideoRendererConfigured = rendererEnabled
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save System Settings")
                            }
                        }
                    }
                    item {
                        StudyQuestCard {
                            Text("Launch Official Tournament", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = tourTitleEn, onValueChange = { tourTitleEn = it }, label = { Text("Tournament Title (EN)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = tourTitleBn, onValueChange = { tourTitleBn = it }, label = { Text("Tournament Title (BN)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.createTournament(tourTitleEn, tourTitleBn, "class_10", "math") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Create Tournament")
                            }
                        }
                    }
                }
            }

            4 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        StudyQuestCard {
                            Text("${log.action} -> ${log.targetType}/${log.targetId}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Actor: ${log.actorUid.take(10)} • ${log.details ?: ""}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            5 -> {
                var openRouterEnabled by remember(aiProviderSettings.openRouterEnabled) { mutableStateOf(aiProviderSettings.openRouterEnabled) }
                var defaultModelId by remember(aiProviderSettings.defaultModelId) { mutableStateOf(aiProviderSettings.defaultModelId) }
                var reasoningModelId by remember(aiProviderSettings.reasoningModelId) { mutableStateOf(aiProviderSettings.reasoningModelId) }
                var advancedModelId by remember(aiProviderSettings.advancedModelId) { mutableStateOf(aiProviderSettings.advancedModelId) }
                var advancedEnabled by remember(aiProviderSettings.advancedModelEnabled) { mutableStateOf(aiProviderSettings.advancedModelEnabled) }
                var firebaseFallback by remember(aiProviderSettings.fallbackToFirebaseAi) { mutableStateOf(aiProviderSettings.fallbackToFirebaseAi) }
                var globalBudgetStr by remember(aiProviderSettings.globalMonthlyBudgetUsd) { mutableStateOf(aiProviderSettings.globalMonthlyBudgetUsd.toString()) }
                var perUserBudgetStr by remember(aiProviderSettings.perUserMonthlyBudgetUsd) { mutableStateOf(aiProviderSettings.perUserMonthlyBudgetUsd.toString()) }
                var astraBudgetStr by remember(aiProviderSettings.astraMonthlyBudgetUsd) { mutableStateOf(aiProviderSettings.astraMonthlyBudgetUsd.toString()) }
                val semantic = LocalStudyQuestColors.current

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StudyQuestCard {
                            Text("OpenRouter Secret & Model Routing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (viewModel.isOpenRouterKeyConfigured) {
                                    "OPENROUTER_API_KEY injected via BuildConfig (masked for security)."
                                } else {
                                    "OPENROUTER_API_KEY not configured in AI Studio Secrets. Requests fall back to Firebase AI if enabled."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (viewModel.isOpenRouterKeyConfigured) semantic.success else MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Enable OpenRouter Provider")
                                Switch(checked = openRouterEnabled, onCheckedChange = { openRouterEnabled = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Enable Advanced Model (Astra)")
                                Switch(checked = advancedEnabled, onCheckedChange = { advancedEnabled = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Fallback to Firebase AI (Gemini)")
                                Switch(checked = firebaseFallback, onCheckedChange = { firebaseFallback = it })
                            }
                            OutlinedTextField(value = defaultModelId, onValueChange = { defaultModelId = it }, label = { Text("Default Tutoring Model") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = reasoningModelId, onValueChange = { reasoningModelId = it }, label = { Text("Complex Reasoning Model") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = advancedModelId, onValueChange = { advancedModelId = it }, label = { Text("Admin-Gated Advanced Model") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.verifyOpenRouterModels() },
                                    enabled = !isVerifyingModels,
                                    modifier = Modifier.weight(1f).testTag("admin_verify_models_btn")
                                ) {
                                    Text(if (isVerifyingModels) "Checking..." else "Verify Models Live")
                                }
                                Button(
                                    onClick = {
                                        viewModel.saveAiProviderSettings(
                                            openRouterEnabled = openRouterEnabled,
                                            defaultModelId = defaultModelId,
                                            reasoningModelId = reasoningModelId,
                                            advancedModelId = advancedModelId,
                                            advancedModelEnabled = advancedEnabled,
                                            fallbackToFirebaseAi = firebaseFallback,
                                            globalMonthlyBudgetUsd = globalBudgetStr.toDoubleOrNull() ?: aiProviderSettings.globalMonthlyBudgetUsd,
                                            perUserMonthlyBudgetUsd = perUserBudgetStr.toDoubleOrNull() ?: aiProviderSettings.perUserMonthlyBudgetUsd,
                                            astraMonthlyBudgetUsd = astraBudgetStr.toDoubleOrNull() ?: aiProviderSettings.astraMonthlyBudgetUsd
                                        )
                                    },
                                    modifier = Modifier.weight(1f).testTag("admin_save_ai_provider_btn")
                                ) {
                                    Text("Save AI Config")
                                }
                            }
                            val check = modelCheckResult
                            if (check != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(check.message, style = MaterialTheme.typography.labelSmall, color = if (check.reachable) semantic.success else MaterialTheme.colorScheme.error)
                            } else if (aiProviderSettings.lastVerifiedModelsSummary.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text("Last check (${aiProviderSettings.lastVerificationStatus}): ${aiProviderSettings.lastVerifiedModelsSummary}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    item {
                        StudyQuestCard {
                            Text("Cost Guard & Monthly Budget Caps (USD)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = globalBudgetStr, onValueChange = { globalBudgetStr = it }, label = { Text("Global Monthly Budget (USD)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = perUserBudgetStr, onValueChange = { perUserBudgetStr = it }, label = { Text("Per-User Monthly Cap (USD)") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = astraBudgetStr, onValueChange = { astraBudgetStr = it }, label = { Text("Astra Monthly Budget (USD)") }, modifier = Modifier.fillMaxWidth())
                            if (!aiProviderSettings.lastErrorSummary.isNullOrBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text("Recent AI Alert: ${aiProviderSettings.lastErrorSummary}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    item {
                        Text("Recent AI Requests (${aiUsageLogs.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(aiUsageLogs.take(15), key = { it.id }) { log ->
                        StudyQuestCard {
                            Text("${log.toolId} • ${log.modelId} (${log.provider})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Status: ${log.status} • In/Out: ${log.inputTokens}/${log.outputTokens} tok • Est: $${"%.4f".format(log.estimatedCostUsd)} • ${log.creditsDeducted} cr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            6 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StudyQuestCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Quest Business Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                OutlinedButton(onClick = { viewModel.refreshBusinessInsights() }) {
                                    Text("Refresh")
                                }
                            }
                            Text(
                                text = if (settings.billingConfigured) "Live Play Billing telemetry active." else "Honest mode: Play Billing is unconfigured, so paid revenue displays ৳0.00 until linked.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        StudyQuestCard {
                            Text("Subscribers & Revenue (BDT)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("Active Profiles: ${businessInsights.activeUsersTotal}", style = MaterialTheme.typography.bodyMedium)
                            Text("Completed Paid Subscriptions: ${businessInsights.paidSubscribersTotal}", style = MaterialTheme.typography.bodyMedium)
                            Text("Gross Monthly Revenue: ৳${"%.2f".format(businessInsights.grossMonthlyRevenueBdt)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Est. Play Fees (15%) & Tax (5%): -৳${"%.2f".format(businessInsights.playStoreFeesBdt + businessInsights.estimatedTaxesBdt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    item {
                        StudyQuestCard {
                            Text("AI Provider Cost & Usage Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("Today's AI Spend: $${"%.4f".format(businessInsights.dailyAiCostUsd)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Monthly AI Spend: $${"%.4f".format(businessInsights.monthlyAiCostUsd)} (≈ ৳${"%.2f".format(businessInsights.monthlyAiCostBdt)})", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "This Month (${businessInsights.totalRequestsThisMonth} req): Luna=${businessInsights.lunaRequests}, Sol=${businessInsights.solRequests}, Astra=${businessInsights.astraRequests}, Firebase Fallback=${businessInsights.fallbackRequests}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Estimated Net Margin: ৳${"%.2f".format(businessInsights.estimatedNetProfitBdt)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (businessInsights.estimatedNetProfitBdt >= 0) LocalStudyQuestColors.current.success else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
