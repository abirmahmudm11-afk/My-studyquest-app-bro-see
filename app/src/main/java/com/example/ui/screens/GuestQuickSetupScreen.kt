package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.model.AcademicGroup
import com.example.data.model.CurriculumVersion
import com.example.data.model.QuestCoachMood
import com.example.data.model.TextbookCatalogItem
import com.example.ui.components.QuestCoachCard
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestAmbientBackdrop
import com.example.ui.components.StudyQuestBrandMark
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.theme.PureEditorialSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickGuestSetupScreen(
    language: AppLanguage,
    reducedMotion: Boolean,
    onBackToWelcome: () -> Unit,
    onCompleteGuestSetupWithBook: ((
        displayName: String,
        classId: String,
        curriculumVersion: String,
        selectedSubjects: List<String>,
        avatarId: String,
        academicGroup: String,
        activeBook: TextbookCatalogItem
    ) -> Unit)? = null,
    onCompleteGuestSetup: (
        displayName: String,
        classId: String,
        curriculumVersion: String,
        selectedSubjects: List<String>,
        avatarId: String
    ) -> Unit
) {
    BackHandler(onBack = onBackToWelcome)

    var displayName by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf("class_10") }
    var selectedVersion by remember {
        mutableStateOf(if (language == AppLanguage.BANGLA) "bangla" else "english")
    }
    var selectedGroup by remember { mutableStateOf("science") }
    var selectedEditionYear by remember { mutableStateOf("2026") }
    var selectedBookId by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("crest_indigo") }
    val selectedSubjects = remember {
        mutableStateListOf("math", "english", "bangla", "ict")
    }
    val supportsGroups = BangladeshCurriculumCatalog.classes.firstOrNull { it.id == selectedClassId }?.supportsGroups == true
    val effectiveGroup = if (supportsGroups) selectedGroup else "general"
    val candidateBooks = remember(selectedEditionYear, selectedClassId, selectedVersion, effectiveGroup) {
        TextbookCatalog.booksFor(
            academicYear = selectedEditionYear,
            classId = selectedClassId,
            curriculumVersion = selectedVersion,
            academicGroup = effectiveGroup
        )
    }
    val activeLockedBook = remember(candidateBooks, selectedBookId) {
        candidateBooks.firstOrNull { it.id == selectedBookId }
            ?: candidateBooks.firstOrNull()
            ?: TextbookCatalog.defaultBookFor(selectedClassId, selectedVersion, effectiveGroup, selectedEditionYear)
    }

    StudyQuestAmbientBackdrop(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 580.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToWelcome,
                        modifier = Modifier.testTag("guest_setup_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = StudyQuestStrings.t(language, "Quick Guest Setup", "দ্রুত গেস্ট সেটআপ"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                    StudyQuestGradientHeroBanner(
                        eyebrow = StudyQuestStrings.t(language, "GUEST MODE • AUTO-MERGE READY", "গেস্ট মোড • স্বয়ংক্রিয় সিঙ্ক"),
                        title = StudyQuestStrings.t(language, "Pick Your Class & Version", "শ্রেণি ও ভার্সন নির্বাচন করো"),
                        subtitle = StudyQuestStrings.t(
                            language,
                            "Jump straight into NCTB lessons, Mock Exams, Solo Sprints & AI Tutor. Sign in anytime to merge XP!",
                            "সরাসরি এনসিটিবি পাঠ, মডেল টেস্ট ও AI টিউটরে ঢুকে পড়ো। গুগল সাইন-ইন করলেই সব XP যোগ হবে!"
                        ),
                        reducedMotion = reducedMotion,
                        trailingSlot = { StudyQuestBrandMark(size = 50.dp) }
                    )
                }

                StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                    StudyQuestCard {
                        Text(
                            text = StudyQuestStrings.t(language, "1. Your Class (Classes 6–10 & SSC)", "১. তোমার শ্রেণি"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BangladeshCurriculumCatalog.classes.filter { it.futureReadyTag == null }.forEach { cls ->
                                FilterChip(
                                    selected = selectedClassId == cls.id,
                                    onClick = {
                                        selectedClassId = cls.id
                                        val valid = BangladeshCurriculumCatalog.subjectsForClass(cls.id).map { it.id }
                                        selectedSubjects.retainAll(valid.toSet())
                                        if (selectedSubjects.isEmpty()) selectedSubjects.addAll(valid.take(4))
                                    },
                                    label = { Text(StudyQuestStrings.t(language, cls.labelEn, cls.labelBn)) },
                                    modifier = Modifier.testTag("guest_class_${cls.id}")
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = StudyQuestStrings.t(language, "2. NCTB Curriculum Version", "২. পাঠ্যক্রম ভার্সন"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CurriculumVersion.entries.forEach { ver ->
                                FilterChip(
                                    selected = selectedVersion == ver.id,
                                    onClick = { selectedVersion = ver.id },
                                    label = { Text(StudyQuestStrings.t(language, ver.labelEn, ver.labelBn)) },
                                    modifier = Modifier.testTag("guest_ver_${ver.id}")
                                )
                            }
                        }

                        if (supportsGroups) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = StudyQuestStrings.t(language, "SSC Academic Group", "এসএসসি বিভাগ"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AcademicGroup.entries.filter { it != AcademicGroup.GENERAL }.forEach { grp ->
                                    FilterChip(
                                        selected = selectedGroup == grp.id,
                                        onClick = { selectedGroup = grp.id },
                                        label = { Text(StudyQuestStrings.t(language, grp.labelEn, grp.labelBn)) }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = StudyQuestStrings.t(language, "3. Lock Active NCTB Textbook (2026/2027)", "৩. সক্রিয় পাঠ্যবই লক (২০২৬/২০২৭)"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("2026" to "2026 NCTB", "2027" to "2027 NCTB").forEach { (yr, label) ->
                                FilterChip(
                                    selected = selectedEditionYear == yr,
                                    onClick = { selectedEditionYear = yr },
                                    label = { Text(label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            candidateBooks.forEach { b ->
                                FilterChip(
                                    selected = activeLockedBook.id == b.id,
                                    onClick = { selectedBookId = b.id },
                                    label = { Text(StudyQuestStrings.t(language, b.titleEn, b.titleBn)) }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text(StudyQuestStrings.t(language, "Nickname (Optional)", "নাম / ডাকনাম (ঐচ্ছিক)")) },
                            placeholder = { Text(StudyQuestStrings.t(language, "Guest Scholar", "গেস্ট শিক্ষার্থী")) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("guest_nickname_input")
                        )

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = StudyQuestStrings.t(language, "Starter Emblem", "পছন্দের প্রতীক"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BangladeshCurriculumCatalog.cosmeticsCatalog
                                .filter { it.category == "avatar" && it.costCoins == 0 }
                                .forEach { cos ->
                                    FilterChip(
                                        selected = selectedAvatar == cos.id,
                                        onClick = { selectedAvatar = cos.id },
                                        label = { Text(StudyQuestStrings.t(language, cos.nameEn, cos.nameBn)) }
                                    )
                                }
                        }
                    }
                }

                StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                    QuestCoachCard(
                        mood = QuestCoachMood.LEARNING_ASSISTANCE,
                        messageEn = "Offline access for '${activeLockedBook.titleEn}', practice sets, and AI explanations will be ready right after setup.",
                        messageBn = "সেটআপের পরপরই '${activeLockedBook.titleBn}' অফলাইনে পড়া, অনুশীলন ও AI ব্যাখ্যা চালু হয়ে যাবে।",
                        language = language,
                        reducedMotion = reducedMotion
                    )
                }

                Button(
                    onClick = {
                        val fallbackName = StudyQuestStrings.t(language, "Guest Scholar", "গেস্ট শিক্ষার্থী")
                        val resolvedName = displayName.trim().ifBlank { fallbackName }
                        if (onCompleteGuestSetupWithBook != null) {
                            onCompleteGuestSetupWithBook(
                                resolvedName,
                                selectedClassId,
                                selectedVersion,
                                selectedSubjects.toList(),
                                selectedAvatar,
                                effectiveGroup,
                                activeLockedBook
                            )
                        } else {
                            onCompleteGuestSetup(
                                resolvedName,
                                selectedClassId,
                                selectedVersion,
                                selectedSubjects.toList(),
                                selectedAvatar
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTealPrimary,
                        contentColor = PureEditorialSurface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("guest_start_learning_button")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = StudyQuestStrings.t(language, "Enter StudyQuest as Guest", "গেস্ট হিসেবে প্রবেশ করুন"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
