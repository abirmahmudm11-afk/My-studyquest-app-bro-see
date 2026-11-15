package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.ExamTemplate
import com.example.data.repository.GuestDataRepository
import com.example.ui.components.AudioFeedbackHelper
import com.example.ui.components.GamificationCelebrationHost
import com.example.ui.components.LottieCelebrationOverlay
import com.example.ui.viewmodel.AdminStudioViewModel
import com.example.ui.viewmodel.AiVideoViewModel
import com.example.ui.viewmodel.FocusTimerViewModel
import com.example.ui.viewmodel.GuestSessionViewModel
import com.example.ui.viewmodel.LearnAndExamViewModel
import com.example.ui.viewmodel.StudyQuestViewModelFactories

@Composable
fun GuestStudyQuestHost(
    authError: String?,
    onClearAuthError: () -> Unit,
    onRequestGoogleSignIn: () -> Unit
) {
    val view = LocalView.current

    val guestVm: GuestSessionViewModel = viewModel(
        key = "guest_session",
        factory = StudyQuestViewModelFactories.guestSessionFactory()
    )
    val learnVm: LearnAndExamViewModel = viewModel(
        key = "learn_${GuestDataRepository.GUEST_UID}",
        factory = StudyQuestViewModelFactories.learnAndExamFactory(GuestDataRepository.GUEST_UID)
    )
    val focusVm: FocusTimerViewModel = viewModel(
        key = "focus_${GuestDataRepository.GUEST_UID}",
        factory = StudyQuestViewModelFactories.focusTimerFactory(GuestDataRepository.GUEST_UID)
    )
    val aiVideoVm: AiVideoViewModel = viewModel(
        key = "aivideo_${GuestDataRepository.GUEST_UID}",
        factory = StudyQuestViewModelFactories.aiVideoFactory(GuestDataRepository.GUEST_UID)
    )

    val prefs by guestVm.localPreferences.collectAsStateWithLifecycle()
    val profile by guestVm.profile.collectAsStateWithLifecycle()
    val isOnline by guestVm.isOnline.collectAsStateWithLifecycle()
    val quests by guestVm.quests.collectAsStateWithLifecycle()
    val notifications by guestVm.notifications.collectAsStateWithLifecycle()
    val unlockedCosmetics by guestVm.unlockedCosmetics.collectAsStateWithLifecycle()
    val soloMatches by guestVm.soloMatches.collectAsStateWithLifecycle()
    val leaderboard by guestVm.leaderboard.collectAsStateWithLifecycle()
    val tournaments by guestVm.tournaments.collectAsStateWithLifecycle()
    val selectedCircleChannel by guestVm.selectedCircleChannel.collectAsStateWithLifecycle()
    val studyCircleNotes by guestVm.studyCircleNotes.collectAsStateWithLifecycle()
    val isAiReplyingInCircle by guestVm.isAiReplyingInCircle.collectAsStateWithLifecycle()
    val activeSoloSprint by guestVm.activeSoloSprint.collectAsStateWithLifecycle()
    val celebrationPulse by guestVm.celebrationPulse.collectAsStateWithLifecycle()
    val exportedJson by guestVm.exportedJson.collectAsStateWithLifecycle()
    val guestBanner by guestVm.bannerMessage.collectAsStateWithLifecycle()
    val studyGoals by guestVm.studyGoals.collectAsStateWithLifecycle()
    val xpLedger by guestVm.xpLedger.collectAsStateWithLifecycle()
    val userBadges by guestVm.badges.collectAsStateWithLifecycle()

    val learningRecords by learnVm.learningRecords.collectAsStateWithLifecycle()
    val mergedQuestions by learnVm.mergedQuestions.collectAsStateWithLifecycle()
    val cachedLessons by learnVm.cachedLessons.collectAsStateWithLifecycle()
    val bookmarks by learnVm.bookmarks.collectAsStateWithLifecycle()
    val revisions by learnVm.revisions.collectAsStateWithLifecycle()
    val studyPlans by learnVm.studyPlans.collectAsStateWithLifecycle()
    val weakTopics by learnVm.weakTopics.collectAsStateWithLifecycle()
    val subjectAnalytics by learnVm.subjectAnalytics.collectAsStateWithLifecycle()
    val dailyStudySeries by learnVm.dailyStudySeries.collectAsStateWithLifecycle()
    val examRemaining by learnVm.examRemainingSeconds.collectAsStateWithLifecycle()
    val assessmentResult by learnVm.lastAssessmentResult.collectAsStateWithLifecycle()
    val aiState by learnVm.aiState.collectAsStateWithLifecycle()
    val learnBanner by learnVm.statusBanner.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingLearnDest by remember { mutableStateOf<LearnDestination?>(null) }
    var showGlobalAiSheet by remember { mutableStateOf(false) }

    val activeBanner = authError ?: guestBanner ?: learnBanner
    val lang = prefs.uiLanguage

    fun openPromotionExam() {
        val qs = mergedQuestions.filter { it.classId == profile.classId }.ifEmpty { mergedQuestions }.take(8)
        val tpl = ExamTemplate(
            id = "promotion_${profile.classId}",
            titleEn = "Annual Class Promotion Exam (${StudyQuestStrings.classLabel(profile.classId, lang)})",
            titleBn = "বার্ষিক শ্রেণি প্রোমোশন পরীক্ষা (${StudyQuestStrings.classLabel(profile.classId, lang)})",
            classId = profile.classId,
            subjectIds = BangladeshCurriculumCatalog.subjectsForClass(profile.classId).map { it.id },
            durationSeconds = 900,
            totalMarks = 8,
            negativeMarkingPerWrong = 0.0,
            descriptionEn = "Pass 60%+ to advance your locked class.",
            descriptionBn = "উত্তীর্ণ হতে ৬০%+ নম্বর অর্জন করুন।"
        )
        pendingLearnDest = LearnDestination.MockExamSession(tpl, qs)
        selectedTab = 1
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val tabs = listOf(
                    Triple(0, StudyQuestStrings.tabToday(lang), Icons.Filled.Home to Icons.Outlined.Home),
                    Triple(1, StudyQuestStrings.tabLearn(lang), Icons.AutoMirrored.Filled.MenuBook to Icons.AutoMirrored.Outlined.MenuBook),
                    Triple(2, StudyQuestStrings.tabCompete(lang), Icons.Filled.EmojiEvents to Icons.Outlined.EmojiEvents),
                    Triple(3, StudyQuestStrings.tabFriends(lang), Icons.Filled.Groups to Icons.Outlined.Groups),
                    Triple(4, StudyQuestStrings.tabProfile(lang), Icons.Filled.Person to Icons.Outlined.Person)
                )
                tabs.forEach { (idx, label, icons) ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick = {
                            AudioFeedbackHelper.triggerClick(view, prefs.soundEnabled, prefs.reducedMotion)
                            selectedTab = idx
                        },
                        icon = { Icon(if (selectedTab == idx) icons.first else icons.second, contentDescription = label) },
                        label = { Text(label, maxLines = 1) },
                        modifier = Modifier.testTag("nav_tab_$idx")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = !activeBanner.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(activeBanner.orEmpty(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                onClearAuthError()
                                guestVm.clearBanner()
                                learnVm.clearBanner()
                            }) {
                                Text("OK")
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (prefs.reducedMotion) {
                                fadeIn(tween(10)) togetherWith fadeOut(tween(10))
                            } else {
                                fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                            }
                        },
                        label = "guest_tab_transition"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> TodayTabScreen(
                                profile = profile,
                                language = lang,
                                reducedMotion = prefs.reducedMotion,
                                isOnline = isOnline,
                                pendingSyncCount = 0,
                                quests = quests,
                                notifications = notifications,
                                unreadNotificationCount = notifications.count { !it.read },
                                weakTopics = weakTopics,
                                revisions = revisions,
                                studyPlans = studyPlans,
                                recentRecords = learningRecords,
                                onClaimQuest = { q ->
                                    guestVm.claimQuest(q) { _, _ ->
                                        AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                                    }
                                },
                                onMarkNotificationRead = {},
                                onOpenQuickPractice = {
                                    val qs = mergedQuestions.filter { it.classId == profile.classId }.ifEmpty { mergedQuestions }.take(6)
                                    pendingLearnDest = LearnDestination.PracticeSession(
                                        StudyQuestStrings.t(lang, "Quick Practice", "কুইক প্র্যাকটিস"),
                                        qs
                                    )
                                    selectedTab = 1
                                },
                                onOpenMockExam = { selectedTab = 1 },
                                onOpenRevision = {
                                    pendingLearnDest = LearnDestination.RevisionAndPlanner
                                    selectedTab = 1
                                },
                                onOpenAiDoubtSolver = { showGlobalAiSheet = true },
                                onOpenMessages = { selectedTab = 3 },
                                subjectAnalytics = subjectAnalytics,
                                dailyStudySeries = dailyStudySeries,
                                dailyChallengeDoneToday = learnVm.isDailyChallengeDoneToday(profile),
                                onOpenSubjectCategory = { subj ->
                                    pendingLearnDest = LearnDestination.SubjectDetail(subj)
                                    selectedTab = 1
                                },
                                onOpenFlashcardsVault = {
                                    pendingLearnDest = LearnDestination.FlashcardsAndVault
                                    selectedTab = 1
                                },
                                onOpenPomodoro = {
                                    pendingLearnDest = LearnDestination.PomodoroFocus
                                    selectedTab = 1
                                },
                                onOpenDailyChallenge = {
                                    pendingLearnDest = LearnDestination.DailyChallenge
                                    selectedTab = 1
                                },
                                onOpenMasteryAnalytics = {
                                    pendingLearnDest = LearnDestination.MasteryAnalytics
                                    selectedTab = 1
                                },
                                onOpenAssignmentsAndGoals = {
                                    pendingLearnDest = LearnDestination.AssignmentsAndGoals
                                    selectedTab = 1
                                },
                                onOpenAiVideoStudio = {
                                    pendingLearnDest = LearnDestination.AiVideoStudio
                                    selectedTab = 1
                                },
                                onOpenBadgesAndXp = {
                                    pendingLearnDest = LearnDestination.BadgesAndXp
                                    selectedTab = 1
                                },
                                onRetakePromotionExam = { openPromotionExam() },
                                isGuestMode = true,
                                onRequestGoogleSignIn = onRequestGoogleSignIn
                            )

                            1 -> LearnTabScreen(
                                uiLanguage = lang,
                                questionLanguage = prefs.questionLanguage,
                                initialClassId = profile.classId,
                                isOnline = isOnline,
                                allQuestions = mergedQuestions,
                                cachedLessons = cachedLessons,
                                bookmarks = bookmarks,
                                revisions = revisions,
                                studyPlans = studyPlans,
                                examRemainingSeconds = examRemaining,
                                lastAssessmentResult = assessmentResult,
                                aiState = aiState,
                                externalDestination = pendingLearnDest,
                                onConsumeExternalDestination = { pendingLearnDest = null },
                                onDownloadChapter = { learnVm.downloadChapterOffline(it) },
                                onCompleteLesson = { lesson ->
                                    learnVm.completeLesson(lesson, isOnline) {
                                        AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                                    }
                                },
                                onToggleBookmark = { learnVm.toggleBookmark(it) },
                                onAddRevision = { learnVm.addToRevision(it) },
                                onReviewRevision = { id, q -> learnVm.reviewRevisionItem(id, q) },
                                onCreateStudyPlan = { t, s, p, d, m -> learnVm.createStudyPlan(t, s, p, d, m) },
                                onToggleStudyPlan = { learnVm.toggleStudyPlan(it) },
                                onDeleteStudyPlan = { learnVm.deleteStudyPlan(it) },
                                onStartExamTimer = { sec, cb -> learnVm.startExamTimer(sec, cb) },
                                onSubmitAssessment = { title, qs, sel, txt, cq, tpl ->
                                    learnVm.submitPracticeOrExam(title, qs, sel, txt, cq, tpl, isOnline) {
                                        AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                                    }
                                },
                                onClearAssessmentResult = { learnVm.clearAssessmentResult() },
                                onReportQuestion = { },
                                onAskAi = { prompt, studentQ, cls, subj ->
                                    learnVm.askAiDoubtSolver(prompt, studentQ, cls, subj, lang)
                                },
                                onResetAi = { learnVm.resetAiDoubtState() },
                                reducedMotion = prefs.reducedMotion,
                                profile = profile,
                                subjectAnalytics = subjectAnalytics,
                                weakTopics = weakTopics,
                                dailyChallengeQuestions = remember(mergedQuestions, profile.classId) {
                                    learnVm.dailyChallengeQuestions(mergedQuestions, profile.classId)
                                },
                                dailyChallengeDoneToday = learnVm.isDailyChallengeDoneToday(profile),
                                dailyChallengeDateKey = learnVm.todayChallengeDateKey(),
                                onCompletePomodoro = { subjId, mins ->
                                    learnVm.completePomodoroSession(subjId, mins, isOnline) {
                                        AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                                    }
                                },
                                onCompleteFlashcardDeck = { subjId, rev, mast ->
                                    learnVm.recordFlashcardSession(subjId, rev, mast, isOnline) {
                                        AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                                    }
                                },
                                onSubmitDailyChallenge = { qs, sel, txt ->
                                    learnVm.completeDailyChallenge(qs, sel, txt, isOnline) {
                                        AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                                    }
                                },
                                focusVm = focusVm,
                                aiVideoVm = aiVideoVm,
                                dailyStudySeries = dailyStudySeries,
                                studyGoals = studyGoals,
                                userBadges = userBadges,
                                xpLedger = xpLedger,
                                isGuestMode = true,
                                onRequestGoogleSignIn = onRequestGoogleSignIn,
                                onCreateStudyGoal = { t, gt, pt, s, tv, d -> guestVm.createStudyGoal(t, gt, pt, s, tv, d) },
                                onClaimStudyGoal = { guestVm.claimStudyGoal(it) },
                                onDeleteStudyGoal = { guestVm.deleteStudyGoal(it) },
                                onSetExamOrQuizActive = { learnVm.setExamOrQuizActive(it) }
                            )

                            2 -> CompeteTabScreen(
                                profile = profile,
                                uiLanguage = lang,
                                questionLanguage = prefs.questionLanguage,
                                reducedMotion = prefs.reducedMotion,
                                allQuestions = mergedQuestions,
                                myMatches = emptyList(),
                                activeMatch = null,
                                activeMatchRequest = null,
                                activeSubmissions = emptyList(),
                                leaderboard = leaderboard,
                                tournaments = tournaments,
                                friendProfiles = emptyMap(),
                                onEnterQuickBattle = { _, _ -> },
                                onCancelMatchRequest = {},
                                onCreatePrivateRoom = { _, _ -> },
                                onJoinRoomByCode = {},
                                onChallengeFriends = { _, _, _, _ -> },
                                onOpenMatch = {},
                                onJoinInvitedMatch = {},
                                onStartActiveMatch = {},
                                onSubmitMatchAnswer = { _, _, _, _, _ -> },
                                onFinalizeMatch = {},
                                onCloseActiveMatch = {},
                                onJoinTournament = {},
                                isGuestMode = true,
                                soloMatches = soloMatches,
                                activeSoloSprint = activeSoloSprint,
                                onStartSoloSprint = { cls, subj, mode, title ->
                                    guestVm.startSoloSprint(cls, subj, mode, title)
                                },
                                onAnswerSoloSprint = { corr, ms ->
                                    if (corr) AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                                    guestVm.answerSoloSprintQuestion(corr, ms)
                                },
                                onCloseSoloSprint = { guestVm.closeSoloSprint() },
                                onRequestGoogleSignIn = onRequestGoogleSignIn
                            )

                            3 -> FriendsTabScreen(
                                myProfile = profile,
                                uiLanguage = lang,
                                friendships = emptyList(),
                                friendProfiles = emptyMap(),
                                incomingRequests = emptyList(),
                                outgoingRequests = emptyList(),
                                conversations = emptyList(),
                                blockedUsers = emptyList(),
                                searchResults = emptyList(),
                                activeConversation = null,
                                activeMessages = emptyList(),
                                onSearchStudents = {},
                                onSendFriendRequest = {},
                                onRespondFriendRequest = { _, _ -> },
                                onUnfriend = {},
                                onOpenDirectChat = {},
                                onOpenConversation = {},
                                onCloseConversation = {},
                                onCreateStudyGroup = { _, _ -> },
                                onAcceptGroupInvite = {},
                                onLeaveGroup = {},
                                onToggleMute = {},
                                onBlockUser = {},
                                onUnblockUser = {},
                                onSendMessage = { _, _, _ -> },
                                onToggleReaction = { _, _, _ -> },
                                onMarkRead = { _, _ -> },
                                onReport = { _, _, _, _ -> },
                                isGuestMode = true,
                                reducedMotion = prefs.reducedMotion,
                                selectedCircleChannel = selectedCircleChannel,
                                studyCircleNotes = studyCircleNotes,
                                isAiReplyingInCircle = isAiReplyingInCircle,
                                onSelectCircleChannel = { guestVm.selectCircleChannel(it) },
                                onPostStudyCircleNote = { txt, askAi -> guestVm.postStudyCircleNote(txt, askAi) },
                                onToggleCircleReaction = { note, emoji -> guestVm.toggleCircleNoteReaction(note, emoji) },
                                onDeleteCircleNote = { guestVm.deleteCircleNote(it) },
                                onRequestGoogleSignIn = onRequestGoogleSignIn
                            )

                            4 -> ProfileTabScreen(
                                profile = profile,
                                preferences = prefs,
                                adminRole = null,
                                unlockedCosmetics = unlockedCosmetics,
                                learningRecords = learningRecords,
                                subjectAnalytics = subjectAnalytics,
                                weakTopics = weakTopics,
                                friendCount = 0,
                                exportedJson = exportedJson,
                                onClearExportedJson = { guestVm.clearExportedJson() },
                                onEquipCosmetic = { cat, id ->
                                    when (cat) {
                                        "avatar" -> guestVm.updateProfileCustomization(avatarId = id)
                                        "frame" -> guestVm.updateProfileCustomization(frameId = id)
                                        "title" -> guestVm.updateProfileCustomization(titleBadge = id)
                                    }
                                },
                                onPurchaseCosmetic = { id, cost -> guestVm.purchaseCosmetic(id, cost) },
                                onUpdateUiLanguage = { guestVm.updateUiLanguage(it) },
                                onUpdateQuestionLanguage = { guestVm.updateQuestionLanguage(it) },
                                onUpdateDarkMode = { guestVm.updateDarkMode(it) },
                                onUpdateReducedMotion = { guestVm.updateReducedMotion(it) },
                                onUpdateSound = { guestVm.updateSoundEnabled(it) },
                                onUpdateQuietHours = { guestVm.updateQuietHours(it) },
                                onUpdateNotifications = { guestVm.updateNotificationsEnabled(it) },
                                onUpdateOptInNational = {},
                                onUpdateOnlineStatusVisible = {},
                                onExportData = { guestVm.exportGuestData() },
                                onDeleteAccount = {},
                                onSignOut = { guestVm.exitGuestModeToSignIn() },
                                adminViewModelProvider = {
                                    viewModel<AdminStudioViewModel>(
                                        key = "admin_unused",
                                        factory = StudyQuestViewModelFactories.adminStudioFactory(GuestDataRepository.GUEST_UID)
                                    )
                                },
                                isGuestMode = true,
                                onRequestGoogleSignIn = onRequestGoogleSignIn,
                                onExitGuestMode = { guestVm.exitGuestModeToSignIn() },
                                userBadges = userBadges,
                                xpLedger = xpLedger,
                                dailyStudySeries = dailyStudySeries,
                                onUpdateThemeMode = { guestVm.updateThemeMode(it) },
                                onUpdateNotificationCategory = { c, e -> guestVm.updateNotificationCategory(c, e) },
                                onRetakePromotionExam = { openPromotionExam() }
                            )
                        }
                    }
                }
            }

            LottieCelebrationOverlay(
                triggerKey = celebrationPulse,
                reducedMotion = prefs.reducedMotion
            )

            GamificationCelebrationHost(
                language = lang,
                reducedMotion = prefs.reducedMotion
            )
        }
    }

    if (showGlobalAiSheet) {
        AiDoubtSolverSheet(
            initialContext = "",
            classId = profile.classId,
            subjectName = "General Study",
            language = lang,
            aiState = aiState,
            onSubmitDoubt = { q ->
                learnVm.askAiDoubtSolver("", q, profile.classId, "General Study", lang)
            },
            onDismiss = {
                learnVm.resetAiDoubtState()
                showGlobalAiSheet = false
            }
        )
    }
}
