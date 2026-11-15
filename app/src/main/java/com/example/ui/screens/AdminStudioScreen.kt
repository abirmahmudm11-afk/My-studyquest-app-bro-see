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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.CobaltPrimary
import com.example.ui.viewmodel.AdminStudioViewModel

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
    val message by viewModel.message.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0=Editorial, 1=Moderation, 2=System & Tournaments, 3=Audit

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
                Text("Role: $adminRole", style = MaterialTheme.typography.labelSmall, color = CobaltPrimary)
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

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Editorial") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Reports (${cases.count { it.status == "open" }})") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("System") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Audit") })
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
                var aiEnabled by remember(settings.aiSolverEnabled) { mutableStateOf(settings.aiSolverEnabled) }
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
                            Text("System & Season Controls", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Enable AI Doubt Solver (Gemini)")
                                Switch(checked = aiEnabled, onCheckedChange = { aiEnabled = it })
                            }
                            OutlinedTextField(value = seasonEn, onValueChange = { seasonEn = it }, label = { Text("Active Season Title") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.updateSystemSettings(aiEnabled, seasonEn, settings.seasonNameBn) },
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

            3 -> {
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
        }
    }
}
