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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.AssignmentDefinitionDoc
import com.example.data.model.AssignmentSubmissionDoc
import com.example.data.model.StudyGoalDoc
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatBadgePill
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.VioletTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssignmentsAndGoalsScreen(
    uiLanguage: AppLanguage,
    classId: String,
    studyGoals: List<StudyGoalDoc>,
    assignments: List<AssignmentDefinitionDoc>,
    submissions: List<AssignmentSubmissionDoc>,
    isGuestMode: Boolean,
    onCreateGoal: (title: String, goalType: String, periodType: String, subjectId: String, targetValue: Long, difficulty: String) -> Unit,
    onClaimGoal: (StudyGoalDoc) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onSubmitAssignment: (AssignmentDefinitionDoc) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Goals, 1=Assignments
    var periodFilter by remember { mutableStateOf("all") }
    var showCreateGoalDialog by remember { mutableStateOf(false) }

    val semantic = LocalStudyQuestColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("goals_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Study Goals & Assignments", "পড়ার লক্ষ্য ও অ্যাসাইনমেন্ট"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (selectedTab == 0) {
                IconButton(
                    onClick = { showCreateGoalDialog = true },
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("open_create_goal_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = StudyQuestStrings.t(uiLanguage, "New Goal", "নতুন লক্ষ্য"))
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Personal Goals (${studyGoals.size})", "ব্যক্তিগত লক্ষ্য")) },
                modifier = Modifier.testTag("tab_personal_goals")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Assignments (${assignments.size})", "অ্যাসাইনমেন্ট")) },
                modifier = Modifier.testTag("tab_assignments_board")
            )
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Button(
                            onClick = { showCreateGoalDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("create_goal_cta")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(StudyQuestStrings.t(uiLanguage, "Create Personal Study Goal", "নতুন ব্যক্তিগত লক্ষ্য যোগ করো"))
                        }
                    }

                    if (studyGoals.isEmpty()) {
                        item {
                            EmptyStateView(
                                titleEn = "No personal goals yet",
                                titleBn = "এখনো কোনো লক্ষ্য নির্ধারণ করোনি",
                                subtitleEn = "Set a daily or weekly study time, lesson, or quiz goal to earn Goal Badges!",
                                subtitleBn = "দৈনিক বা সাপ্তাহিক পড়া, পাঠ বা প্রশ্ন সমাধানের লক্ষ্য ঠিক করে ব্যাজ অর্জন করো।",
                                language = uiLanguage
                            )
                        }
                    } else {
                        items(studyGoals, key = { it.id }) { goal ->
                            val frac = (goal.currentValue.toFloat() / goal.targetValue.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                            StudyQuestCard(
                                containerColor = if (goal.completed) semantic.successContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("goal_card_${goal.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(goal.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${goal.periodType.uppercase()} • ${goal.difficulty.uppercase()} • ${goal.currentValue}/${goal.targetValue}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeleteGoal(goal.id) }) {
                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete goal")
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { frac },
                                    color = if (goal.completed) semantic.success else CobaltPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            uiLanguage,
                                            "${goal.currentValue} / ${goal.targetValue} progress",
                                            "${StudyQuestStrings.formatDigits(goal.currentValue, uiLanguage)} / ${StudyQuestStrings.formatDigits(goal.targetValue, uiLanguage)} অগ্রগতি"
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (goal.completed) semantic.success else CobaltPrimary
                                    )
                                    when {
                                        goal.rewardClaimed -> StatBadgePill(
                                            icon = Icons.Filled.CheckCircle,
                                            label = StudyQuestStrings.t(uiLanguage, "Claimed", "সংগৃহীত"),
                                            tint = semantic.success,
                                            containerColor = semantic.successContainer
                                        )
                                        goal.completed -> Button(
                                            onClick = { onClaimGoal(goal) },
                                            colors = ButtonDefaults.buttonColors(containerColor = semantic.success),
                                            modifier = Modifier.minimumInteractiveComponentSize().testTag("claim_goal_${goal.id}")
                                        ) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Claim Reward", "পুরস্কার নাও"))
                                        }
                                        else -> Text(
                                            text = StudyQuestStrings.t(uiLanguage, "In Progress", "চলমান"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                val filteredAssignments = remember(assignments, periodFilter) {
                    if (periodFilter == "all") assignments else assignments.filter { it.assignmentType == periodFilter }
                }
                val submissionMap = remember(submissions) { submissions.associateBy { it.assignmentId } }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "all" to StudyQuestStrings.t(uiLanguage, "All", "সব"),
                                "daily" to StudyQuestStrings.t(uiLanguage, "Daily", "দৈনিক"),
                                "weekly" to StudyQuestStrings.t(uiLanguage, "Weekly", "সাপ্তাহিক"),
                                "two_week" to StudyQuestStrings.t(uiLanguage, "Two-Week", "পাক্ষিক"),
                                "monthly" to StudyQuestStrings.t(uiLanguage, "Monthly", "মাসিক")
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = periodFilter == key,
                                    onClick = { periodFilter = key },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("assign_filter_$key")
                                )
                            }
                        }
                    }

                    if (filteredAssignments.isEmpty()) {
                        item {
                            EmptyStateView(
                                titleEn = "No assignments in this period",
                                titleBn = "এই সময়কালে কোনো অ্যাসাইনমেন্ট নেই",
                                subtitleEn = "Switch to All to see active Daily, Weekly, Two-Week, and Monthly assignments.",
                                subtitleBn = "সকল অ্যাসাইনমেন্ট দেখতে 'সব' নির্বাচন করো।",
                                language = uiLanguage
                            )
                        }
                    } else {
                        items(filteredAssignments, key = { it.id }) { item ->
                            val sub = submissionMap[item.id]
                            val done = sub?.completed == true
                            StudyQuestCard(
                                containerColor = if (done) semantic.successContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.testTag("assignment_card_${item.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.assignmentType.replace('_', '-').uppercase()} • ${item.difficulty.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CobaltPrimary
                                    )
                                    StatBadgePill(
                                        icon = Icons.Filled.Assignment,
                                        label = StudyQuestStrings.t(
                                            uiLanguage,
                                            "${item.targetCount} target • ${item.durationLimitMinutes}m",
                                            "${StudyQuestStrings.formatDigits(item.targetCount, uiLanguage)} লক্ষ্য • ${StudyQuestStrings.formatDigits(item.durationLimitMinutes, uiLanguage)} মি."
                                        ),
                                        tint = VioletTertiary,
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, item.titleEn, item.titleBn),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = StudyQuestStrings.t(uiLanguage, item.descriptionEn, item.descriptionBn),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                if (done) {
                                    StatBadgePill(
                                        icon = Icons.Filled.CheckCircle,
                                        label = StudyQuestStrings.t(uiLanguage, "Completed & Verified", "সম্পন্ন ও যাচাইকৃত"),
                                        tint = semantic.success,
                                        containerColor = semantic.successContainer
                                    )
                                } else {
                                    Button(
                                        onClick = { onSubmitAssignment(item) },
                                        enabled = !isGuestMode,
                                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("submit_assign_${item.id}")
                                    ) {
                                        Text(
                                            if (isGuestMode) StudyQuestStrings.t(uiLanguage, "Sign in with Google to submit", "জমা দিতে গুগল সাইন-ইন করো")
                                            else StudyQuestStrings.t(uiLanguage, "Verify & Complete Assignment", "যাচাই ও সম্পন্ন করো")
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

    if (showCreateGoalDialog) {
        var title by remember { mutableStateOf("") }
        var goalType by remember { mutableStateOf("time_minutes") }
        var periodType by remember { mutableStateOf("daily") }
        var difficulty by remember { mutableStateOf("medium") }
        var targetText by remember { mutableStateOf("30") }
        val classSubjects = remember(classId) { BangladeshCurriculumCatalog.subjectsForClass(classId) }
        var subjectId by remember { mutableStateOf("all") }

        AlertDialog(
            onDismissRequest = { showCreateGoalDialog = false },
            title = { Text(StudyQuestStrings.t(uiLanguage, "New Personal Study Goal", "নতুন ব্যক্তিগত লক্ষ্য")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(StudyQuestStrings.t(uiLanguage, "Goal Title (e.g., Math Trig Revision)", "লক্ষ্যের শিরোনাম")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
                    )
                    Text(StudyQuestStrings.t(uiLanguage, "Goal Metric", "ধরন"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "time_minutes" to "Focus Minutes",
                            "questions_solved" to "Questions",
                            "chapters_finished" to "Lessons"
                        ).forEach { (id, lbl) ->
                            FilterChip(selected = goalType == id, onClick = { goalType = id }, label = { Text(lbl) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("daily" to "Daily", "weekly" to "Weekly").forEach { (id, lbl) ->
                            FilterChip(selected = periodType == id, onClick = { periodType = id }, label = { Text(lbl) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("easy" to "Easy", "medium" to "Medium", "hard" to "Hard").forEach { (id, lbl) ->
                            FilterChip(selected = difficulty == id, onClick = { difficulty = id }, label = { Text(lbl) })
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = subjectId == "all", onClick = { subjectId = "all" }, label = { Text("All") })
                        classSubjects.take(4).forEach { s ->
                            FilterChip(selected = subjectId == s.id, onClick = { subjectId = s.id }, label = { Text(s.code) })
                        }
                    }
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it.filter { ch -> ch.isDigit() } },
                        label = {
                            Text(
                                if (goalType == "time_minutes") StudyQuestStrings.t(uiLanguage, "Target Minutes (min 5)", "লক্ষ্য মিনিট (সর্বনিম্ন ৫)")
                                else StudyQuestStrings.t(uiLanguage, "Target Count", "লক্ষ্য সংখ্যা")
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("goal_target_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valLong = targetText.toLongOrNull() ?: 0L
                        onCreateGoal(title, goalType, periodType, subjectId, valLong, difficulty)
                        showCreateGoalDialog = false
                    },
                    enabled = title.trim().isNotEmpty(),
                    modifier = Modifier.testTag("goal_confirm_create_btn")
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Save Goal", "সংরক্ষণ"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGoalDialog = false }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                }
            }
        )
    }
}
