package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.core.configureAppCheck
import com.example.data.local.StudyQuestWorkScheduler
import com.example.ui.components.AudioFeedbackHelper
import com.example.ui.components.LottieCelebrationOverlay
import com.example.ui.screens.AcademicSetupWizardScreen
import com.example.ui.screens.AiDoubtSolverSheet
import com.example.ui.screens.CompeteTabScreen
import com.example.ui.screens.FriendsTabScreen
import com.example.ui.screens.GuestStudyQuestHost
import com.example.ui.screens.LanguagePickerLaunchScreen
import com.example.ui.screens.LearnDestination
import com.example.ui.screens.LearnTabScreen
import com.example.ui.screens.ProfileTabScreen
import com.example.ui.screens.QuickGuestSetupScreen
import com.example.ui.screens.TodayTabScreen
import com.example.ui.screens.WelcomeAndGoogleSignInScreen
import com.example.ui.screens.rememberGoogleSignInLauncher
import com.example.ui.theme.StudyQuestTheme
import com.example.ui.viewmodel.AdminStudioViewModel
import com.example.ui.viewmodel.CompeteViewModel
import com.example.ui.viewmodel.LearnAndExamViewModel
import com.example.ui.viewmodel.MainSessionViewModel
import com.example.ui.viewmodel.PreAuthViewModel
import com.example.ui.viewmodel.SocialViewModel
import com.example.ui.viewmodel.StudyQuestViewModelFactories
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Phase 1 AI Logic Contract: Initialize App Check BEFORE setContent / ViewModels / Firebase calls
        configureAppCheck(this, intent)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyQuestRootApp()
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
private fun StudyQuestRootApp() {
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    val preAuthVm: PreAuthViewModel = viewModel(factory = StudyQuestViewModelFactories.preAuthFactory())
    val preAuthPrefs by preAuthVm.localPreferences.collectAsStateWithLifecycle()
    val isSigningIn by preAuthVm.isSigningIn.collectAsStateWithLifecycle()
    val authMessage by preAuthVm.authMessage.collectAsStateWithLifecycle()

    val googleSignInLauncher = rememberGoogleSignInLauncher(
        language = preAuthPrefs.uiLanguage,
        onSigningInChanged = { preAuthVm.setSigningIn(it) },
        onIdTokenReceived = { preAuthVm.signInWithGoogleIdToken(it) },
        onAuthError = { preAuthVm.setAuthError(it) }
    )

    StudyQuestTheme(
        darkTheme = preAuthPrefs.darkMode,
        isBangla = preAuthPrefs.uiLanguage == AppLanguage.BANGLA
    ) {
        val user = currentUser
        if (user != null) {
            // Feature ViewModels & Firestore listeners are gated cleanly behind authenticated session!
            AuthenticatedStudyQuestHost(user = user)
        } else if (preAuthPrefs.isGuestMode && !preAuthPrefs.guestOnboardingCompleted) {
            QuickGuestSetupScreen(
                language = preAuthPrefs.uiLanguage,
                reducedMotion = preAuthPrefs.reducedMotion,
                onBackToWelcome = { preAuthVm.exitGuestMode() },
                onCompleteGuestSetup = { name, cls, ver, subjs, avatar ->
                    preAuthVm.completeGuestQuickSetup(name, cls, ver, subjs, avatar)
                }
            )
        } else if (preAuthPrefs.isGuestMode) {
            GuestStudyQuestHost(
                authError = authMessage,
                onClearAuthError = { preAuthVm.setAuthError(null) },
                onRequestGoogleSignIn = googleSignInLauncher
            )
        } else if (!preAuthPrefs.hasChosenInitialLanguage) {
            LanguagePickerLaunchScreen(
                onSelectLanguage = { lang -> preAuthVm.selectInitialLanguage(lang) }
            )
        } else {
            WelcomeAndGoogleSignInScreen(
                language = preAuthPrefs.uiLanguage,
                reducedMotion = preAuthPrefs.reducedMotion,
                isSigningIn = isSigningIn,
                authError = authMessage,
                onSetLanguage = { preAuthVm.setUiLanguage(it) },
                onSigningInChanged = { preAuthVm.setSigningIn(it) },
                onIdTokenReceived = { preAuthVm.signInWithGoogleIdToken(it) },
                onAuthError = { preAuthVm.setAuthError(it) },
                onContinueAsGuest = { preAuthVm.enterGuestMode() }
            )
        }
    }
}

@Composable
private fun AuthenticatedStudyQuestHost(user: FirebaseUser) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = LocalView.current

    val sessionVm: MainSessionViewModel = viewModel(
        key = "session_${user.uid}",
        factory = StudyQuestViewModelFactories.mainSessionFactory(user)
    )
    val learnVm: LearnAndExamViewModel = viewModel(
        key = "learn_${user.uid}",
        factory = StudyQuestViewModelFactories.learnAndExamFactory(user.uid)
    )
    val competeVm: CompeteViewModel = viewModel(
        key = "compete_${user.uid}",
        factory = StudyQuestViewModelFactories.competeFactory(user.uid)
    )
    val socialVm: SocialViewModel = viewModel(
        key = "social_${user.uid}",
        factory = StudyQuestViewModelFactories.socialFactory(user.uid)
    )

    LaunchedEffect(user.uid) {
        StudyQuestWorkScheduler.schedulePeriodicReminders(context)
        StudyQuestWorkScheduler.enqueueImmediateSync(context)
    }

    val prefs by sessionVm.localPreferences.collectAsStateWithLifecycle()
    val profile by sessionVm.profile.collectAsStateWithLifecycle()
    val bootstrapping by sessionVm.bootstrapping.collectAsStateWithLifecycle()
    val isOnline by sessionVm.isOnline.collectAsStateWithLifecycle()
    val quests by sessionVm.quests.collectAsStateWithLifecycle()
    val notifications by sessionVm.notifications.collectAsStateWithLifecycle()
    val unreadNotifs by sessionVm.unreadNotificationCount.collectAsStateWithLifecycle()
    val unlockedCosmetics by sessionVm.unlockedCosmetics.collectAsStateWithLifecycle()
    val adminRole by sessionVm.adminRole.collectAsStateWithLifecycle()
    val exportedJson by sessionVm.exportedJson.collectAsStateWithLifecycle()
    val sessionBanner by sessionVm.bannerMessage.collectAsStateWithLifecycle()
    val guestMergedCount by sessionVm.guestMergedCount.collectAsStateWithLifecycle()

    val learningRecords by learnVm.learningRecords.collectAsStateWithLifecycle()
    val mergedQuestions by learnVm.mergedQuestions.collectAsStateWithLifecycle()
    val cachedLessons by learnVm.cachedLessons.collectAsStateWithLifecycle()
    val bookmarks by learnVm.bookmarks.collectAsStateWithLifecycle()
    val revisions by learnVm.revisions.collectAsStateWithLifecycle()
    val studyPlans by learnVm.studyPlans.collectAsStateWithLifecycle()
    val pendingSyncCount by learnVm.pendingSyncCount.collectAsStateWithLifecycle()
    val weakTopics by learnVm.weakTopics.collectAsStateWithLifecycle()
    val subjectAnalytics by learnVm.subjectAnalytics.collectAsStateWithLifecycle()
    val examRemaining by learnVm.examRemainingSeconds.collectAsStateWithLifecycle()
    val assessmentResult by learnVm.lastAssessmentResult.collectAsStateWithLifecycle()
    val aiState by learnVm.aiState.collectAsStateWithLifecycle()
    val learnBanner by learnVm.statusBanner.collectAsStateWithLifecycle()

    val myMatches by competeVm.myMatches.collectAsStateWithLifecycle()
    val activeMatch by competeVm.activeMatch.collectAsStateWithLifecycle()
    val activeRequest by competeVm.activeMatchRequest.collectAsStateWithLifecycle()
    val activeSubmissions by competeVm.activeSubmissions.collectAsStateWithLifecycle()
    val leaderboard by competeVm.leaderboard.collectAsStateWithLifecycle()
    val tournaments by competeVm.tournaments.collectAsStateWithLifecycle()
    val competeBanner by competeVm.message.collectAsStateWithLifecycle()

    val incomingReq by socialVm.incomingRequests.collectAsStateWithLifecycle()
    val outgoingReq by socialVm.outgoingRequests.collectAsStateWithLifecycle()
    val friendships by socialVm.friendships.collectAsStateWithLifecycle()
    val friendProfiles by socialVm.friendProfiles.collectAsStateWithLifecycle()
    val blockedUsers by socialVm.blockedUsers.collectAsStateWithLifecycle()
    val conversations by socialVm.conversations.collectAsStateWithLifecycle()
    val searchResults by socialVm.searchResults.collectAsStateWithLifecycle()
    val activeConversation by socialVm.activeConversation.collectAsStateWithLifecycle()
    val activeMessages by socialVm.activeMessages.collectAsStateWithLifecycle()
    val socialBanner by socialVm.message.collectAsStateWithLifecycle()

    val activeProfile = profile
    if (bootstrapping || activeProfile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator()
                Text(StudyQuestStrings.t(prefs.uiLanguage, "Loading StudyQuest...", "লোড হচ্ছে..."))
            }
        }
        return
    }

    if (!activeProfile.onboardingCompleted) {
        AcademicSetupWizardScreen(
            profile = activeProfile,
            language = prefs.uiLanguage,
            onCompleteSetup = { name, cls, ver, subjs, avatar ->
                AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                sessionVm.completeAcademicSetup(name, cls, ver, subjs, avatar)
            }
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Today, 1=Learn, 2=Compete, 3=Friends, 4=Profile
    var pendingLearnDest by remember { mutableStateOf<LearnDestination?>(null) }
    var showGlobalAiSheet by remember { mutableStateOf(false) }
    var celebrationPulse by remember { mutableIntStateOf(0) }

    LaunchedEffect(guestMergedCount) {
        if (guestMergedCount > 0) {
            celebrationPulse += 1
        }
    }

    val activeBanner = sessionBanner ?: learnBanner ?: competeBanner ?: socialBanner
    val lang = prefs.uiLanguage

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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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
                            sessionVm.clearBanner()
                            learnVm.clearBanner()
                            competeVm.clearMessage()
                            socialVm.clearMessage()
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
                    label = "auth_tab_transition"
                ) { tabIndex ->
                when (tabIndex) {
                    0 -> TodayTabScreen(
                        profile = activeProfile,
                        language = lang,
                        reducedMotion = prefs.reducedMotion,
                        isOnline = isOnline,
                        pendingSyncCount = pendingSyncCount,
                        quests = quests,
                        notifications = notifications,
                        unreadNotificationCount = unreadNotifs,
                        weakTopics = weakTopics,
                        revisions = revisions,
                        studyPlans = studyPlans,
                        recentRecords = learningRecords,
                        onClaimQuest = { q ->
                            sessionVm.claimQuest(q) { _, _ ->
                                celebrationPulse += 1
                                AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                            }
                        },
                        onMarkNotificationRead = { sessionVm.markNotificationRead(it) },
                        onOpenQuickPractice = {
                            val qs = mergedQuestions.filter { it.classId == activeProfile.classId }.ifEmpty { mergedQuestions }.take(6)
                            pendingLearnDest = LearnDestination.PracticeSession(
                                StudyQuestStrings.t(lang, "Quick Practice", "কুইক প্র্যাকটিস"),
                                qs
                            )
                            selectedTab = 1
                        },
                        onOpenMockExam = {
                            selectedTab = 1
                        },
                        onOpenRevision = {
                            pendingLearnDest = LearnDestination.RevisionAndPlanner
                            selectedTab = 1
                        },
                        onOpenAiDoubtSolver = { showGlobalAiSheet = true },
                        onOpenMessages = { selectedTab = 3 }
                    )

                    1 -> LearnTabScreen(
                        uiLanguage = lang,
                        questionLanguage = prefs.questionLanguage,
                        initialClassId = activeProfile.classId,
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
                        onReportQuestion = { q ->
                            socialVm.reportIssue("question", q.id, "Content accuracy review", q.promptEn)
                        },
                        onAskAi = { prompt, studentQ, cls, subj ->
                            learnVm.askAiDoubtSolver(prompt, studentQ, cls, subj, lang)
                        },
                        onResetAi = { learnVm.resetAiDoubtState() },
                        reducedMotion = prefs.reducedMotion
                    )

                    2 -> CompeteTabScreen(
                        profile = activeProfile,
                        uiLanguage = lang,
                        questionLanguage = prefs.questionLanguage,
                        reducedMotion = prefs.reducedMotion,
                        allQuestions = mergedQuestions,
                        myMatches = myMatches,
                        activeMatch = activeMatch,
                        activeMatchRequest = activeRequest,
                        activeSubmissions = activeSubmissions,
                        leaderboard = leaderboard,
                        tournaments = tournaments,
                        friendProfiles = friendProfiles,
                        onEnterQuickBattle = { cls, subj -> competeVm.enterQuickBattle(activeProfile, cls, subj) },
                        onCancelMatchRequest = { competeVm.cancelWaitingMatchRequest() },
                        onCreatePrivateRoom = { cls, subj -> competeVm.createPrivateRoom(cls, subj) },
                        onJoinRoomByCode = { competeVm.joinRoomByCode(it) },
                        onChallengeFriends = { uids, isGrp, cls, subj -> competeVm.challengeFriendOrGroup(uids, isGrp, cls, subj) },
                        onOpenMatch = { competeVm.openMatch(it) },
                        onJoinInvitedMatch = { competeVm.joinInvitedMatch(it) },
                        onStartActiveMatch = { competeVm.startActiveMatch() },
                        onSubmitMatchAnswer = { m, qId, idx, corr, ms ->
                            if (corr) AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                            competeVm.submitAnswer(m, qId, idx, corr, ms)
                        },
                        onFinalizeMatch = { m ->
                            AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                            competeVm.finalizeMatch(m, activeProfile)
                        },
                        onCloseActiveMatch = { competeVm.closeActiveMatch() },
                        onJoinTournament = { competeVm.joinTournament(it) }
                    )

                    3 -> FriendsTabScreen(
                        myProfile = activeProfile,
                        uiLanguage = lang,
                        friendships = friendships,
                        friendProfiles = friendProfiles,
                        incomingRequests = incomingReq,
                        outgoingRequests = outgoingReq,
                        conversations = conversations,
                        blockedUsers = blockedUsers,
                        searchResults = searchResults,
                        activeConversation = activeConversation,
                        activeMessages = activeMessages,
                        onSearchStudents = { socialVm.searchStudents(it) },
                        onSendFriendRequest = { socialVm.sendFriendRequest(activeProfile, it) },
                        onRespondFriendRequest = { r, acc -> socialVm.respondToRequest(r, acc) },
                        onUnfriend = { socialVm.unfriend(it) },
                        onOpenDirectChat = { socialVm.openDirectChat(it) },
                        onOpenConversation = { socialVm.openConversation(it) },
                        onCloseConversation = { socialVm.closeConversation() },
                        onCreateStudyGroup = { name, uids -> socialVm.createStudyGroup(name, uids) },
                        onAcceptGroupInvite = { socialVm.acceptGroupInvitation(it) },
                        onLeaveGroup = { socialVm.leaveStudyGroup(it) },
                        onToggleMute = { socialVm.toggleMute(it) },
                        onBlockUser = { socialVm.blockUser(it) },
                        onUnblockUser = { socialVm.unblockUser(it) },
                        onSendMessage = { conv, txt, reply -> socialVm.sendMessage(conv, activeProfile.displayName, txt, reply) },
                        onToggleReaction = { convId, msg, emoji -> socialVm.toggleReaction(convId, msg, emoji) },
                        onMarkRead = { convId, msgs -> socialVm.markVisibleMessagesRead(convId, msgs) },
                        onReport = { t, id, r, d -> socialVm.reportIssue(t, id, r, d) },
                        reducedMotion = prefs.reducedMotion
                    )

                    4 -> ProfileTabScreen(
                        profile = activeProfile,
                        preferences = prefs,
                        adminRole = adminRole,
                        unlockedCosmetics = unlockedCosmetics,
                        learningRecords = learningRecords,
                        subjectAnalytics = subjectAnalytics,
                        weakTopics = weakTopics,
                        friendCount = friendships.size,
                        exportedJson = exportedJson,
                        onClearExportedJson = { sessionVm.clearExportedJson() },
                        onEquipCosmetic = { cat, id ->
                            when (cat) {
                                "avatar" -> sessionVm.updateProfile(avatarId = id)
                                "frame" -> sessionVm.updateProfile(frameId = id)
                                "title" -> sessionVm.updateProfile(titleBadge = id)
                            }
                        },
                        onPurchaseCosmetic = { id, cost -> competeVm.purchaseCosmetic(activeProfile, id, cost) },
                        onUpdateUiLanguage = { sessionVm.updateUiLanguage(it) },
                        onUpdateQuestionLanguage = { sessionVm.updateQuestionLanguage(it) },
                        onUpdateDarkMode = { sessionVm.updateDarkMode(it) },
                        onUpdateReducedMotion = { sessionVm.updateReducedMotion(it) },
                        onUpdateSound = { sessionVm.updateSoundEnabled(it) },
                        onUpdateQuietHours = { sessionVm.updateQuietHours(it) },
                        onUpdateNotifications = { sessionVm.updateNotificationsEnabled(it) },
                        onUpdateOptInNational = { sessionVm.updateProfile(optInNational = it) },
                        onUpdateOnlineStatusVisible = { sessionVm.updateProfile(showOnlineStatus = it) },
                        onExportData = { sessionVm.exportMyData() },
                        onDeleteAccount = { sessionVm.deleteAccount { } },
                        onSignOut = { sessionVm.signOut() },
                        adminViewModelProvider = {
                            viewModel<AdminStudioViewModel>(
                                key = "admin_${user.uid}",
                                factory = StudyQuestViewModelFactories.adminStudioFactory(user.uid)
                            )
                        }
                    )
                }
                }
            }
        }

            LottieCelebrationOverlay(
                triggerKey = celebrationPulse,
                reducedMotion = prefs.reducedMotion
            )
        }
    }

    if (showGlobalAiSheet) {
        AiDoubtSolverSheet(
            initialContext = "",
            classId = activeProfile.classId,
            subjectName = "General Study",
            language = lang,
            aiState = aiState,
            onSubmitDoubt = { q ->
                learnVm.askAiDoubtSolver("", q, activeProfile.classId, "General Study", lang)
            },
            onDismiss = {
                learnVm.resetAiDoubtState()
                showGlobalAiSheet = false
            }
        )
    }
}
