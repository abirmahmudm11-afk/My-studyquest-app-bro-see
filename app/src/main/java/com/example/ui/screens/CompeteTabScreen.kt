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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import androidx.compose.material.icons.filled.Schedule
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.GamificationCatalog
import com.example.data.local.LocalSoloMatchEntity
import com.example.data.model.CompetitionArchiveDoc
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.MascotMood
import com.example.data.model.MatchDoc
import com.example.data.model.MatchRequestDoc
import com.example.data.model.MatchSubmissionDoc
import com.example.data.model.PublicProfile
import com.example.data.model.QuestionItem
import com.example.data.model.TournamentDoc
import com.example.data.repository.CompetitionRepository
import com.example.data.repository.LeaderboardCategory
import com.example.data.repository.LeaderboardPeriod
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MascotTituCard
import com.example.ui.components.RankTierBadge
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.example.ui.components.StudyQuestGradientHeroBanner
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.viewmodel.ActiveSoloSprintState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompeteTabScreen(
    profile: PublicProfile,
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    reducedMotion: Boolean,
    allQuestions: List<QuestionItem>,
    myMatches: List<MatchDoc>,
    activeMatch: MatchDoc?,
    activeMatchRequest: MatchRequestDoc?,
    activeSubmissions: List<MatchSubmissionDoc>,
    leaderboard: List<LeaderboardEntryDoc>,
    tournaments: List<TournamentDoc>,
    friendProfiles: Map<String, PublicProfile>,
    onEnterQuickBattle: (classId: String, subjectId: String) -> Unit,
    onCancelMatchRequest: () -> Unit,
    onCreatePrivateRoom: (classId: String, subjectId: String) -> Unit,
    onJoinRoomByCode: (String) -> Unit,
    onChallengeFriends: (friendUids: List<String>, isGroup: Boolean, classId: String, subjectId: String) -> Unit,
    onOpenMatch: (String) -> Unit,
    onJoinInvitedMatch: (MatchDoc) -> Unit,
    onStartActiveMatch: () -> Unit,
    onSubmitMatchAnswer: (MatchDoc, String, Int, Boolean, Long) -> Unit,
    onFinalizeMatch: (MatchDoc) -> Unit,
    onCloseActiveMatch: () -> Unit,
    onJoinTournament: (TournamentDoc) -> Unit,
    isGuestMode: Boolean = false,
    soloMatches: List<LocalSoloMatchEntity> = emptyList(),
    activeSoloSprint: ActiveSoloSprintState? = null,
    onStartSoloSprint: (classId: String, subjectId: String, mode: String, customTitle: String?) -> Unit = { _, _, _, _ -> },
    onAnswerSoloSprint: (isCorrect: Boolean, responseTimeMs: Long) -> Unit = { _, _ -> },
    onCloseSoloSprint: () -> Unit = {},
    onRequestGoogleSignIn: () -> Unit = {},
    selectedCategory: LeaderboardCategory = LeaderboardCategory.XP,
    selectedPeriod: LeaderboardPeriod = LeaderboardPeriod.WEEKLY,
    competitionArchives: List<CompetitionArchiveDoc> = emptyList(),
    onSelectCategory: (LeaderboardCategory) -> Unit = {},
    onSelectPeriod: (LeaderboardPeriod) -> Unit = {}
) {
    if (activeSoloSprint != null) {
        SoloSpeedSprintScreen(
            sprint = activeSoloSprint,
            uiLanguage = uiLanguage,
            questionLanguage = questionLanguage,
            reducedMotion = reducedMotion,
            onAnswerQuestion = onAnswerSoloSprint,
            onPlayAgain = {
                onStartSoloSprint(activeSoloSprint.classId, activeSoloSprint.subjectId, activeSoloSprint.mode, activeSoloSprint.title)
            },
            onBack = onCloseSoloSprint
        )
        return
    }

    if (activeMatch != null) {
        LiveBattleArenaScreen(
            match = activeMatch,
            profile = profile,
            uiLanguage = uiLanguage,
            questionLanguage = questionLanguage,
            allQuestions = allQuestions,
            submissions = activeSubmissions,
            onStartMatch = onStartActiveMatch,
            onSubmitAnswer = { qId, idx, correct, timeMs ->
                onSubmitMatchAnswer(activeMatch, qId, idx, correct, timeMs)
            },
            onFinalize = { onFinalizeMatch(activeMatch) },
            onBack = onCloseActiveMatch
        )
        return
    }

    var selectedTab by remember { mutableStateOf(0) } // 0=Arena, 1=Leaderboards, 2=Tournaments
    var selectedSubjectId by remember { mutableStateOf("math") }
    var joinCodeInput by remember { mutableStateOf("") }
    var showChallengeDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Battle Arena", "যুদ্ধক্ষেত্র")) },
                modifier = Modifier.testTag("compete_tab_arena")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Leaderboards", "লিডারবোর্ড")) },
                modifier = Modifier.testTag("compete_tab_leaderboard")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(StudyQuestStrings.t(uiLanguage, "Tournaments", "টুর্নামেন্ট")) },
                modifier = Modifier.testTag("compete_tab_tournaments")
            )
        }

        when (selectedTab) {
            0 -> {
                if (isGuestMode) {
                    GuestArenaTabContent(
                        profile = profile,
                        uiLanguage = uiLanguage,
                        reducedMotion = reducedMotion,
                        selectedSubjectId = selectedSubjectId,
                        soloMatches = soloMatches,
                        onSelectSubject = { selectedSubjectId = it },
                        onStartSoloSprint = { cls, subj ->
                            onStartSoloSprint(cls, subj, "solo_sprint", null)
                        },
                        onRequestGoogleSignIn = onRequestGoogleSignIn
                    )
                } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                            StudyQuestGradientHeroBanner(
                                eyebrow = StudyQuestStrings.t(uiLanguage, "LIVE BATTLE ARENA", "লাইভ কুইজ অ্যারেনা"),
                                title = StudyQuestStrings.t(uiLanguage, "Compete & Climb Tiers", "প্রতিযোগিতা ও র‍্যাংক উন্নয়ন"),
                                subtitle = StudyQuestStrings.t(
                                    uiLanguage,
                                    "Rating: ${profile.competitionRating} (${profile.rankTier}) • Accuracy first, speed breaks ties!",
                                    "রেটিং: ${StudyQuestStrings.formatDigits(profile.competitionRating, uiLanguage)} (${StudyQuestStrings.rankTierLabel(profile.rankTier, uiLanguage)})"
                                ),
                                reducedMotion = reducedMotion,
                                backgroundResId = R.drawable.img_arena_banner,
                                trailingSlot = {
                                    RankTierBadge(tier = profile.rankTier, language = uiLanguage)
                                }
                            )
                        }
                    }

                    // Active Quick Battle Waitlist (Genuine matchmaking state — never fake bots)
                    if (activeMatchRequest != null && activeMatchRequest.status == "waiting") {
                        item {
                            StudyQuestCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = StudyQuestStrings.t(uiLanguage, "Searching for a real student...", "বাস্তব প্রতিদ্বন্দ্বী খোঁজা হচ্ছে..."),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = StudyQuestStrings.t(
                                                uiLanguage,
                                                "Subject: ${activeMatchRequest.subjectId} • Real matchmaking only (no bots).",
                                                "বিষয়: ${activeMatchRequest.subjectId} • কোনো বট প্রতিপক্ষ নয়।"
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    OutlinedButton(onClick = onCancelMatchRequest) {
                                        Text(StudyQuestStrings.t(uiLanguage, "Cancel", "বাতিল"))
                                    }
                                }
                            }
                        }
                    }

                    // Mode Selector Card
                    item {
                        StudyQuestCard {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Choose Battle Subject", "প্রতিযোগিতার বিষয়"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BangladeshCurriculumCatalog.subjectsForClass(profile.classId).take(5).forEach { subj ->
                                    FilterChip(
                                        selected = selectedSubjectId == subj.id,
                                        onClick = { selectedSubjectId = subj.id },
                                        label = { Text(StudyQuestStrings.t(uiLanguage, subj.titleEn, subj.titleBn)) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onEnterQuickBattle(profile.classId, selectedSubjectId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("quick_battle_btn")
                                ) {
                                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Quick 1v1", "দ্রুত ১v১"))
                                }
                                FilledTonalButton(
                                    onClick = { onCreatePrivateRoom(profile.classId, selectedSubjectId) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("create_private_room_btn")
                                ) {
                                    Icon(Icons.Filled.MeetingRoom, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(StudyQuestStrings.t(uiLanguage, "Private Room", "প্রাইভেট রুম"))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            FilledTonalButton(
                                onClick = { showChallengeDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("challenge_friend_btn")
                            ) {
                                Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Challenge Friend or Squad", "বন্ধুকে চ্যালেঞ্জ করো"))
                            }
                        }
                    }

                    // Join Private Room by Code
                    item {
                        StudyQuestCard {
                            Text(
                                text = StudyQuestStrings.t(uiLanguage, "Join Room by Code", "কোড দিয়ে রুমে যুক্ত হও"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = joinCodeInput,
                                    onValueChange = { joinCodeInput = it.uppercase().take(8) },
                                    label = { Text(StudyQuestStrings.t(uiLanguage, "6-char Room Code", "৬ অক্ষরের কোড")) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("room_code_input")
                                )
                                Button(
                                    onClick = { if (joinCodeInput.isNotBlank()) onJoinRoomByCode(joinCodeInput) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("join_room_code_btn")
                                ) {
                                    Text(StudyQuestStrings.t(uiLanguage, "Join", "যুক্ত হও"))
                                }
                            }
                        }
                    }

                    // Matches & Invites
                    item {
                        Text(
                            text = StudyQuestStrings.t(uiLanguage, "Your Battles & Invitations", "তোমার ম্যাচ ও আমন্ত্রণ"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (myMatches.isEmpty()) {
                        item {
                            EmptyStateView(
                                titleEn = "No matches yet",
                                titleBn = "এখনো কোনো ম্যাচ নেই",
                                subtitleEn = "Create a Private Room and share the code with a classmate, or challenge a friend.",
                                subtitleBn = "একটি প্রাইভেট রুম খুলে সহপাঠীর সাথে কোড শেয়ার করো।",
                                language = uiLanguage
                            )
                        }
                    } else {
                        items(myMatches, key = { it.id }) { m ->
                            val isInvited = profile.userId in m.invitedUids && profile.userId !in m.playerUids
                            StudyQuestCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${m.mode.replace('_', ' ').uppercase()} • ${m.subjectId.uppercase()}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = buildString {
                                                append("Status: ${m.status}")
                                                if (!m.roomCode.isNullOrBlank()) append(" • Code: ${m.roomCode}")
                                                append(" • Players: ${m.playerUids.size}")
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isInvited) {
                                        Button(onClick = { onJoinInvitedMatch(m) }) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Accept", "গ্রহণ"))
                                        }
                                    } else {
                                        FilledTonalButton(onClick = { onOpenMatch(m.id) }) {
                                            Text(
                                                if (m.status == "completed") StudyQuestStrings.t(uiLanguage, "Results", "ফলাফল")
                                                else StudyQuestStrings.t(uiLanguage, "Enter", "প্রবেশ")
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

            1 -> {
                LeaderboardSubTab(
                    profile = profile,
                    uiLanguage = uiLanguage,
                    entries = leaderboard,
                    friendUids = friendProfiles.keys,
                    selectedCategory = selectedCategory,
                    selectedPeriod = selectedPeriod,
                    competitionArchives = competitionArchives,
                    onSelectCategory = onSelectCategory,
                    onSelectPeriod = onSelectPeriod,
                    isGuestMode = isGuestMode
                )
            }

            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tournaments.isEmpty()) {
                        item {
                            EmptyStateView(
                                titleEn = "No active tournaments",
                                titleBn = "চলমান টুর্নামেন্ট নেই",
                                subtitleEn = "Admins can launch official SSC & class tournaments in Admin Studio.",
                                subtitleBn = "নতুন টুর্নামেন্ট শীঘ্রই ঘোষণা করা হবে।",
                                language = uiLanguage
                            )
                        }
                    } else {
                        items(tournaments, key = { it.id }) { tour ->
                            val joined = profile.userId in tour.participantUids
                            StudyQuestCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = StudyQuestStrings.t(uiLanguage, tour.titleEn, tour.titleBn),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${tour.classId} • ${tour.subjectId} • ${tour.participantUids.size}/${tour.maxParticipants} • +${tour.rewardCoins} Coins",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isGuestMode) {
                                        Button(
                                            onClick = {
                                                onStartSoloSprint(
                                                    tour.classId,
                                                    tour.subjectId,
                                                    "solo_tournament",
                                                    StudyQuestStrings.t(uiLanguage, tour.titleEn, tour.titleBn)
                                                )
                                            },
                                            modifier = Modifier.testTag("guest_tourn_play_${tour.id}")
                                        ) {
                                            Text(StudyQuestStrings.t(uiLanguage, "Play Bracket", "খেলুন"))
                                        }
                                    } else {
                                        Button(
                                            onClick = { onJoinTournament(tour) },
                                            enabled = !joined && tour.status == "registration"
                                        ) {
                                            Text(
                                                if (joined) StudyQuestStrings.t(uiLanguage, "Joined", "যুক্ত")
                                                else StudyQuestStrings.t(uiLanguage, "Register", "নিবন্ধন")
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

    if (showChallengeDialog) {
        val selectedFriends = remember { mutableStateOf<Set<String>>(emptySet()) }
        AlertDialog(
            onDismissRequest = { showChallengeDialog = false },
            title = { Text(StudyQuestStrings.t(uiLanguage, "Challenge Friends", "বন্ধুদের চ্যালেঞ্জ করো")) },
            text = {
                if (friendProfiles.isEmpty()) {
                    Text(StudyQuestStrings.t(uiLanguage, "Add friends in the Friends tab first!", "প্রথমে বন্ধু ট্যাব থেকে বন্ধু যুক্ত করো!"))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        friendProfiles.values.forEach { fp ->
                            val picked = fp.userId in selectedFriends.value
                            FilterChip(
                                selected = picked,
                                onClick = {
                                    selectedFriends.value = if (picked) selectedFriends.value - fp.userId else selectedFriends.value + fp.userId
                                },
                                label = { Text("${fp.displayName} (${fp.studyQuestId})") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val list = selectedFriends.value.toList()
                        if (list.isNotEmpty()) {
                            onChallengeFriends(list, list.size > 1, profile.classId, selectedSubjectId)
                            showChallengeDialog = false
                        }
                    },
                    enabled = selectedFriends.value.isNotEmpty()
                ) {
                    Text(StudyQuestStrings.t(uiLanguage, "Send Challenge", "চ্যালেঞ্জ পাঠাও"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showChallengeDialog = false }) {
                    Text(StudyQuestStrings.t(uiLanguage, "Close", "বন্ধ"))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeaderboardSubTab(
    profile: PublicProfile,
    uiLanguage: AppLanguage,
    entries: List<LeaderboardEntryDoc>,
    friendUids: Set<String>,
    selectedCategory: LeaderboardCategory,
    selectedPeriod: LeaderboardPeriod,
    competitionArchives: List<CompetitionArchiveDoc>,
    onSelectCategory: (LeaderboardCategory) -> Unit,
    onSelectPeriod: (LeaderboardPeriod) -> Unit,
    isGuestMode: Boolean = false
) {
    var scopeFilter by remember(isGuestMode) { mutableStateOf("class") }
    val semantic = LocalStudyQuestColors.current

    val filtered = remember(entries, scopeFilter, selectedCategory, selectedPeriod, profile, friendUids, isGuestMode) {
        val base = when (scopeFilter) {
            "friends" -> entries.filter { it.userId == profile.userId || it.userId in friendUids }
            "class" -> entries.filter { it.classId == profile.classId }
            "national" -> if (isGuestMode) entries else entries.filter { it.optInNational }
            else -> entries
        }
        base.sortedByDescending { CompetitionRepository.scoreFor(it, selectedCategory, selectedPeriod) }
    }

    val remainingMs = remember(selectedPeriod) {
        GamificationCatalog.millisUntilPeriodEnd(selectedPeriod.id)
    }
    val remDays = (remainingMs / 86_400_000L).toInt()
    val remHours = ((remainingMs % 86_400_000L) / 3_600_000L).toInt()

    val categoryUnit = when (selectedCategory) {
        LeaderboardCategory.XP -> "XP"
        LeaderboardCategory.STUDY_TIME -> StudyQuestStrings.t(uiLanguage, "min", "মি.")
        LeaderboardCategory.EXAM -> StudyQuestStrings.t(uiLanguage, "pts", "নম্বর")
        LeaderboardCategory.ASSIGNMENT -> StudyQuestStrings.t(uiLanguage, "pts", "নম্বর")
        LeaderboardCategory.CHALLENGE -> StudyQuestStrings.t(uiLanguage, "pts", "পয়েন্ট")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            StudyQuestCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = CobaltPrimary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "${selectedPeriod.labelEn} ${selectedCategory.labelEn} Reset Countdown",
                                "${selectedPeriod.labelBn} ${selectedCategory.labelBn} রিসেট"
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "${remDays}d ${remHours}h remaining (Asia/Dhaka) • Top 3 earn Podium Badge!",
                                "${StudyQuestStrings.formatDigits(remDays, uiLanguage)} দিন ${StudyQuestStrings.formatDigits(remHours, uiLanguage)} ঘণ্টা বাকি"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Period Filter (Weekly / Two-Week / Monthly)
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LeaderboardPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onSelectPeriod(period) },
                        label = { Text(StudyQuestStrings.t(uiLanguage, period.labelEn, period.labelBn)) },
                        modifier = Modifier.testTag("lb_period_${period.id}")
                    )
                }
            }
        }

        // 5 Category Filters (XP, Study Time, Exam, Assignment, Challenge)
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LeaderboardCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(StudyQuestStrings.t(uiLanguage, cat.labelEn, cat.labelBn)) },
                        modifier = Modifier.testTag("lb_cat_${cat.id}")
                    )
                }
            }
        }

        // Scope Filter (My Class / Friends / National Opt-in)
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "class" to StudyQuestStrings.t(uiLanguage, "My Class", "আমার শ্রেণি"),
                    "friends" to StudyQuestStrings.t(uiLanguage, "Friends", "বন্ধু"),
                    "national" to StudyQuestStrings.t(uiLanguage, "National (Opt-in)", "জাতীয়")
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = scopeFilter == key,
                        onClick = { scopeFilter = key },
                        label = { Text(label) },
                        modifier = Modifier.testTag("lb_scope_$key")
                    )
                }
            }
            if (scopeFilter == "national" && !profile.optInNationalLeaderboard && !isGuestMode) {
                Spacer(Modifier.height(6.dp))
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Text(
                            text = StudyQuestStrings.t(
                                uiLanguage,
                                "National ranking is opt-in. Enable 'Opt-in National Leaderboard' in Profile Settings to appear here.",
                                "জাতীয় র‍্যাংকিং অপ্ট-ইন ভিত্তিক। প্রোফাইল সেটিংস থেকে এটি চালু করা যাবে।"
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else if (isGuestMode) {
                Spacer(Modifier.height(6.dp))
                StudyQuestCard(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)) {
                    Text(
                        text = StudyQuestStrings.t(
                            uiLanguage,
                            "Guest Leaderboard compares your progress against NCTB Tier Milestone Targets. Sign in with Google anytime to compete on live rankings!",
                            "গেস্ট লিডারবোর্ডে তোমার অগ্রগতি মাইলফলক লক্ষ্যমাত্রার সাথে তুলনা করা হচ্ছে।"
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateView(
                    titleEn = "No leaderboard entries yet",
                    titleBn = "এখনো র‍্যাংকিং তৈরি হয়নি",
                    subtitleEn = "Complete a focus session, exam, assignment, or challenge to rank here!",
                    subtitleBn = "ফোকাস, পরীক্ষা, অ্যাসাইনমেন্ট বা চ্যালেঞ্জ সম্পন্ন করে র‍্যাংকিংয়ে আসো!",
                    language = uiLanguage
                )
            }
        } else {
            itemsIndexed(filtered, key = { _, e -> e.userId }) { idx, entry ->
                val isMe = entry.userId == profile.userId
                val score = CompetitionRepository.scoreFor(entry, selectedCategory, selectedPeriod)
                val medal = when (idx) {
                    0 -> "🥇 "
                    1 -> "🥈 "
                    2 -> "🥉 "
                    else -> ""
                }
                val rankColor = when (idx) {
                    0 -> AmberGold
                    1 -> CobaltPrimary
                    2 -> semantic.success
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                StudyQuestCard(
                    containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "$medal#${StudyQuestStrings.formatDigits(idx + 1, uiLanguage)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = rankColor
                            )
                            Column {
                                Text(
                                    text = if (isMe) "${entry.displayName} (You)" else entry.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${entry.studyQuestId} • ${entry.streakCount}d streak • ${entry.matchesWon}/${entry.matchesPlayed} W",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            RankTierBadge(tier = entry.rankTier, language = uiLanguage)
                            Text(
                                text = "${StudyQuestStrings.formatDigits(score, uiLanguage)} $categoryUnit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (idx < 3) rankColor else CobaltPrimary
                            )
                        }
                    }
                }
            }
        }

        // Previous Period Winners Archive
        if (competitionArchives.isNotEmpty()) {
            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Previous Period Winners", "পূর্ববর্তী সময়ের বিজয়ীরা"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(competitionArchives.take(4), key = { it.id }) { arch ->
                StudyQuestCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = AmberGold)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${arch.periodKey} • ${arch.category.uppercase()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val names = arch.winners.mapIndexed { i, w ->
                                val name = w["displayName"]?.toString() ?: "Student"
                                val sc = w["score"]?.toString() ?: "0"
                                "#${i + 1} $name ($sc)"
                            }.joinToString(" • ")
                            Text(
                                text = names.ifBlank { StudyQuestStrings.t(uiLanguage, "Archive recorded", "আর্কাইভ সংরক্ষিত") },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveBattleArenaScreen(
    match: MatchDoc,
    profile: PublicProfile,
    uiLanguage: AppLanguage,
    questionLanguage: AppLanguage,
    allQuestions: List<QuestionItem>,
    submissions: List<MatchSubmissionDoc>,
    onStartMatch: () -> Unit,
    onSubmitAnswer: (questionId: String, selectedIndex: Int, isCorrect: Boolean, responseTimeMs: Long) -> Unit,
    onFinalize: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val matchQuestions = remember(match.questionIds, allQuestions) {
        match.questionIds.mapNotNull { id -> allQuestions.find { it.id == id } }
    }
    val submittedMap = remember(submissions) { submissions.associateBy { it.questionId } }
    var currentIndex by remember { mutableStateOf(0) }
    var questionShownAtMs by remember(currentIndex) { mutableStateOf(System.currentTimeMillis()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = StudyQuestStrings.t(uiLanguage, "Live Study Battle", "লাইভ কুইজ যুদ্ধ"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!match.roomCode.isNullOrBlank()) {
                        Text(
                            text = "Room Code: ${match.roomCode}",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.titleSmall,
                            color = CobaltPrimary
                        )
                    }
                }
            }
        }

        // Live Scoreboard
        item {
            StudyQuestCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                Text(
                    text = StudyQuestStrings.t(uiLanguage, "Live Scoreboard (${match.status})", "লাইভ স্কোরবোর্ড (${match.status})"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                match.playerUids.forEach { pid ->
                    val score = match.scores[pid] ?: 0L
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (pid == profile.userId) "${profile.displayName} (You)" else "Player ${pid.take(6)}",
                            fontWeight = if (pid == profile.userId) FontWeight.Bold else FontWeight.Normal
                        )
                        Text("${StudyQuestStrings.formatDigits(score, uiLanguage)} pts", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (match.status == "waiting") {
            item {
                StudyQuestCard {
                    Text(
                        text = StudyQuestStrings.t(
                            uiLanguage,
                            "Share Room Code '${match.roomCode ?: match.id.take(6)}' or start now once your opponent joins.",
                            "তোমার সহপাঠী যুক্ত হলেই ম্যাচ শুরু করো।"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onStartMatch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(StudyQuestStrings.t(uiLanguage, "Start Match Now", "এখনই শুরু করো"))
                    }
                }
            }
        } else if (match.status == "completed") {
            item {
                val won = match.winnerUid == profile.userId
                MascotTituCard(
                    mood = if (won) MascotMood.ACHIEVEMENT else MascotMood.ENCOURAGEMENT,
                    messageEn = if (won) "Victory! Your rating & leaderboard points have been updated." else "Match finished! Keep practicing to climb the leaderboard tier.",
                    messageBn = if (won) "দারুণ জয়! তোমার রেটিং ও লিডারবোর্ড পয়েন্ট আপডেট হয়েছে।" else "ম্যাচ সম্পন্ন হয়েছে! আরও অনুশীলন করে র‍্যাংক বাড়াও।",
                    language = uiLanguage,
                    reducedMotion = false,
                    showCelebrationLottie = won
                )
            }
        } else if (matchQuestions.isNotEmpty()) {
            val safeIdx = currentIndex.coerceIn(0, matchQuestions.lastIndex)
            val q = matchQuestions[safeIdx]
            val existingSub = submittedMap[q.id]

            item {
                LinearProgressIndicator(
                    progress = { (safeIdx + 1).toFloat() / matchQuestions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.height(8.dp))
                StudyQuestCard {
                    Text(
                        text = "Q${safeIdx + 1}/${matchQuestions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CobaltPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = StudyQuestStrings.t(questionLanguage, q.promptEn, q.promptBn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    val opts = if (questionLanguage == AppLanguage.BANGLA && q.optionsBn.isNotEmpty()) q.optionsBn else q.optionsEn
                    opts.forEachIndexed { idx, opt ->
                        val pickedThis = existingSub?.selectedIndex?.toInt() == idx
                        val isCorrectOpt = idx in q.correctOptionIndices
                        val bg = when {
                            existingSub != null && isCorrectOpt -> MaterialTheme.colorScheme.secondaryContainer
                            existingSub != null && pickedThis && !isCorrectOpt -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = bg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    if (existingSub == null) {
                                        val elapsed = (System.currentTimeMillis() - questionShownAtMs).coerceAtLeast(100L)
                                        onSubmitAnswer(q.id, idx, idx in q.correctOptionIndices, elapsed)
                                    }
                                },
                                enabled = existingSub == null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(opt, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (safeIdx < matchQuestions.lastIndex) {
                            Button(
                                onClick = { currentIndex = safeIdx + 1 },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(StudyQuestStrings.t(uiLanguage, "Next Question", "পরবর্তী প্রশ্ন"))
                            }
                        } else {
                            Button(
                                onClick = onFinalize,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(StudyQuestStrings.t(uiLanguage, "Finish & Claim Rank", "শেষ করো ও র‍্যাংক আপডেট"))
                            }
                        }
                    }
                }
            }
        }
    }
}
