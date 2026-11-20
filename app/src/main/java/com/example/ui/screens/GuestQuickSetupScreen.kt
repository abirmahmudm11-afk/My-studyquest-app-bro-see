package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.model.AcademicGroup
import com.example.data.model.CurriculumVersion
import com.example.data.model.TextbookCatalogItem
import com.example.ui.theme.JetBrainsMonoFontFamily

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
    var step by remember { mutableIntStateOf(0) } // 0 = Class & Version, 1 = Active Book & Emblem

    BackHandler {
        if (step > 0) step -= 1 else onBackToWelcome()
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
        ) {
            // Minimal Step Header + Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (step > 0) step -= 1 else onBackToWelcome() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("guest_setup_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = StudyQuestStrings.t(language, "Back", "ফিরে যাও")
                        )
                    }
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "Guest • Step ${step + 1} of 2",
                            "গেস্ট • ধাপ ${StudyQuestStrings.formatDigits(step + 1, language)}/২"
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { (step + 1) / 2f },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
            }

            // Guided Step Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (reducedMotion) fadeIn(tween(10)) togetherWith fadeOut(tween(10))
                        else fadeIn(tween(200)) togetherWith fadeOut(tween(140))
                    },
                    label = "guest_setup_step"
                ) { currentStep ->
                    if (currentStep == 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "Pick your class & stream.",
                                        "তোমার শ্রেণি ও ভার্সন বেছে নাও।"
                                    ),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "Sign in later anytime to sync your notes and progress.",
                                        "পরে যেকোনো সময় সাইন-ইন করে পড়া সিঙ্ক করতে পারবে।"
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = StudyQuestStrings.t(language, "Class (6–10 & SSC)", "শ্রেণি"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
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
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = StudyQuestStrings.t(language, "Curriculum Version", "পাঠ্যক্রম ভার্সন"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
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
                            }

                            if (supportsGroups) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "SSC Group", "এসএসসি বিভাগ"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
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
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "Choose your starter textbook.",
                                        "প্রাথমিক পাঠ্যবই বেছে নাও।"
                                    ),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = StudyQuestStrings.t(
                                        language,
                                        "All lessons, quizzes, and offline pages open around this selection.",
                                        "আপনার পাঠ, কুইজ ও অফলাইন পড়ার ব্যবস্থা এই বই অনুযায়ী সাজানো হবে।"
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("2026" to "2026 NCTB", "2027" to "2027 NCTB").forEach { (yr, label) ->
                                    FilterChip(
                                        selected = selectedEditionYear == yr,
                                        onClick = { selectedEditionYear = yr },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                candidateBooks.take(6).forEach { b ->
                                    val selected = activeLockedBook.id == b.id
                                    Surface(
                                        onClick = { selectedBookId = b.id },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = if (selected) 1.5.dp else 1.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = StudyQuestStrings.t(language, b.titleEn, b.titleBn),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${b.bookCode} • ${b.academicYear}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (selected) {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text(StudyQuestStrings.t(language, "Nickname (Optional)", "ডাকনাম (ঐচ্ছিক)")) },
                                placeholder = { Text(StudyQuestStrings.t(language, "Guest Scholar", "গেস্ট শিক্ষার্থী")) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guest_nickname_input")
                            )

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
                }
            }

            // Single Sticky Primary CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                if (step == 0) {
                    Button(
                        onClick = { step = 1 },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("guest_next_step_btn")
                    ) {
                        Text(
                            text = StudyQuestStrings.t(language, "Continue", "এগিয়ে যান"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                } else {
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
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("guest_start_learning_button")
                    ) {
                        Text(
                            text = StudyQuestStrings.t(language, "Start Learning", "শেখা শুরু করো"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
