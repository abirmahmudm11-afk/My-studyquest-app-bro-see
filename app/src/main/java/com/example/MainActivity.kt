package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import com.example.core.AppLanguage
import com.example.core.BillingAndAdProvider
import com.example.core.LocalUserPreferences
import com.example.core.StudyQuestStrings
import com.example.core.ThemeMode
import com.example.core.configureAppCheck
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.StudyQuestWorkScheduler
import com.example.data.model.ExamTemplate
import com.example.data.model.PublicProfile
import com.example.data.model.QuestPlanCatalog
import com.example.ui.components.AudioFeedbackHelper
import com.example.ui.components.GamificationCelebrationHost
import com.example.ui.components.LottieCelebrationOverlay
import com.example.ui.components.QuestNavigationScaffold
import com.example.ui.components.StudyQuestAmbientBackdrop
import com.example.ui.components.StudyQuestCard
import com.example.ui.screens.AcademicSetupWizardScreen
import com.example.ui.screens.AiDoubtSolverScreen
import com.example.ui.screens.AiDoubtSolverSheet
import com.example.ui.screens.CompeteTabScreen
import com.example.ui.screens.FriendsTabScreen
import com.example.ui.screens.GuestStudyQuestHost
import com.example.ui.screens.LanguagePickerLaunchScreen
import com.example.ui.screens.LeaderboardScopeFilter
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LearnDestination
import com.example.ui.screens.LearnTabScreen
import com.example.ui.screens.ProfileTabScreen
import com.example.ui.screens.QuickGuestSetupScreen
import com.example.ui.screens.StudyProgressDashboardScreen
import com.example.ui.screens.SubscriptionSelectionScreen
import com.example.ui.screens.TodayOverlayDestination
import com.example.ui.screens.TodayTabScreen
import com.example.ui.screens.WelcomeAndGoogleSignInScreen
import com.example.ui.screens.WelcomePlanSelectionScreen
import com.example.ui.theme.StudyQuestTheme
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
import com.example.ui.viewmodel.AdminStudioViewModel
import com.example.ui.viewmodel.AiVideoViewModel
import com.example.ui.viewmodel.BootstrapState
import com.example.ui.viewmodel.CompeteViewModel
import com.example.ui.viewmodel.FocusTimerViewModel
import com.example.ui.viewmodel.LearnAndExamViewModel
import com.example.ui.viewmodel.MainSessionViewModel
import com.example.ui.viewmodel.PreAuthViewModel
import com.example.ui.viewmodel.SocialViewModel
import com.example.ui.viewmodel.StudyQuestViewModelFactories
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Phase 1 AI Logic Contract: Initialize App Check BEFORE setContent / ViewModels / Firebase calls
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            StudyQuestRootApp()
        }
    }
}

@Composable
private fun StudyQuestRootApp() {
    val auth = remember { Firebase.auth }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val preAuthVm: PreAuthViewModel = viewModel(factory = StudyQuestViewModelFactories.preAuthFactory())
    val preAuthPrefs by preAuthVm.localPreferences.collectAsStateWithLifecycle()
    val shouldShowLanguagePicker by preAuthVm.shouldShowLanguagePicker.collectAsStateWithLifecycle()
    val isSigningIn by preAuthVm.isSigningIn.collectAsStateWithLifecycle()
    val canCancelSignIn by preAuthVm.canCancelSignIn.collectAsStateWithLifecycle()
    val authMessage by preAuthVm.authMessage.collectAsStateWithLifecycle()
    val authStatusMessage by preAuthVm.authStatusMessage.collectAsStateWithLifecycle()
    val passwordResetStatus by preAuthVm.passwordResetStatus.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (preAuthPrefs.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    DisposableEffect(useDarkTheme) {
        val activity = context as? ComponentActivity
        val style = if (useDarkTheme) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        activity?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        onDispose {}
    }

    var pendingAgeBracket by remember { mutableStateOf("13_17") }
    var pendingGuardianConsent by remember { mutableStateOf("verified") }
    var ageConsentDirty by remember { mutableStateOf(false) }

    StudyQuestTheme(
        darkTheme = useDarkTheme,
        isBangla = preAuthPrefs.uiLanguage == AppLanguage.BANGLA,
        surfaceStyle = preAuthPrefs.surfaceStyle
    ) {
        StudyQuestAmbientBackdrop {
            val user = currentUser
            if (user != null) {
                // Feature ViewModels & Firestore listeners are gated behind authenticated session and keyed by UID
                key(user.uid) {
                    AuthenticatedStudyQuestHost(
                        user = user,
                        pendingAgeBracket = pendingAgeBracket,
                        pendingGuardianConsent = pendingGuardianConsent,
                        ageConsentDirty = ageConsentDirty,
                        onClearAgeConsentDirty = { ageConsentDirty = false }
                    )
                }
            } else if (shouldShowLanguagePicker) {
                LanguagePickerLaunchScreen(
                    onSelectLanguage = { lang -> preAuthVm.selectInitialLanguage(lang) },
                    reducedMotion = preAuthPrefs.reducedMotion
                )
            } else if (preAuthPrefs.isGuestMode) {
                if (!preAuthPrefs.guestOnboardingCompleted) {
                    QuickGuestSetupScreen(
                        language = preAuthPrefs.uiLanguage,
                        reducedMotion = preAuthPrefs.reducedMotion,
                        onBackToWelcome = { preAuthVm.exitGuestMode() },
                        onCompleteGuestSetupWithBook = { name, cls, ver, subjs, avatar, grp, book ->
                            preAuthVm.completeGuestQuickSetup(
                                displayName = name,
                                classId = cls,
                                curriculumVersion = ver,
                                selectedSubjects = subjs,
                                avatarId = avatar,
                                academicGroup = grp,
                                activeBookYear = book.academicYear,
                                activeBookId = book.id
                            )
                        },
                        onCompleteGuestSetup = { name, cls, ver, subjs, avatar ->
                            preAuthVm.completeGuestQuickSetup(name, cls, ver, subjs, avatar)
                        }
                    )
                } else {
                    GuestStudyQuestHost(
                        authError = authMessage,
                        onClearAuthError = { preAuthVm.clearAuthMessages() },
                        onRequestGoogleSignIn = { preAuthVm.exitGuestMode() }
                    )
                }
            } else {
                WelcomeAndGoogleSignInScreen(
                    language = preAuthPrefs.uiLanguage,
                    reducedMotion = preAuthPrefs.reducedMotion,
                    isSigningIn = isSigningIn,
                    authError = authMessage,
                    onSetLanguage = { preAuthVm.setUiLanguage(it) },
                    onSigningInChanged = { preAuthVm.setSigningIn(it) },
                    onIdTokenReceived = { token ->
                        preAuthVm.signInWithGoogleIdToken(token, preAuthPrefs.uiLanguage) {
                            currentUser = auth.currentUser
                        }
                    },
                    onAuthError = { preAuthVm.setAuthError(it) },
                    canCancelSignIn = canCancelSignIn,
                    onCancelSignIn = { preAuthVm.cancelSignIn() },
                    onBackToLanguagePicker = { preAuthVm.returnToLanguagePicker() },
                    authStatusMessage = authStatusMessage,
                    passwordResetStatus = passwordResetStatus,
                    onEmailSignIn = { email, password ->
                        preAuthVm.signInWithEmail(email, password, preAuthPrefs.uiLanguage) {
                            currentUser = auth.currentUser
                        }
                    },
                    onEmailSignUp = { name, email, password, confirmPassword ->
                        preAuthVm.signUpWithEmail(name, email, password, confirmPassword, preAuthPrefs.uiLanguage) {
                            currentUser = auth.currentUser
                        }
                    },
                    onSendPasswordReset = { email ->
                        preAuthVm.sendPasswordReset(email, preAuthPrefs.uiLanguage)
                    },
                    onClearAuthMessages = { preAuthVm.clearAuthMessages() },
                    onClearPasswordResetStatus = { preAuthVm.clearPasswordResetStatus() },
                    onEnterGuestMode = { preAuthVm.enterGuestMode() },
                    ageBracket = pendingAgeBracket,
                    guardianConsentStatus = pendingGuardianConsent,
                    onAgeAndConsentSelected = { age, consent ->
                        pendingAgeBracket = age
                        pendingGuardianConsent = consent
                        ageConsentDirty = true
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedStudyQuestHost(
    user: FirebaseUser,
    pendingAgeBracket: String = "13_17",
    pendingGuardianConsent: String = "verified",
    ageConsentDirty: Boolean = false,
    onClearAgeConsentDirty: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = LocalView.current

    val sessionVm: MainSessionViewModel = viewModel(
        key = "session_${user.uid}",
        factory = StudyQuestViewModelFactories.mainSessionFactory(user)
    )

    LaunchedEffect(user.uid) {
        StudyQuestWorkScheduler.schedulePeriodicReminders(context)
        StudyQuestWorkScheduler.enqueueImmediateSync(context)
    }

    val prefs by sessionVm.localPreferences.collectAsStateWithLifecycle()
    val bootstrapState by sessionVm.bootstrapState.collectAsStateWithLifecycle()
    val isCompletingSetup by sessionVm.isCompletingSetup.collectAsStateWithLifecycle()
    val sessionBanner by sessionVm.bannerMessage.collectAsStateWithLifecycle()
    val subscription by sessionVm.subscription.collectAsStateWithLifecycle()
    val systemSettings by sessionVm.systemSettings.collectAsStateWithLifecycle()
    val cloudTextbooks by sessionVm.cloudTextbooks.collectAsStateWithLifecycle()

    LaunchedEffect(subscription.tier) {
        if (subscription.tier != "free" && !prefs.welcomePlanCompleted) {
            sessionVm.markWelcomePlanCompleted()
        }
    }

    when (val state = bootstrapState) {
        BootstrapState.Loading -> {
            BackHandler { sessionVm.signOut() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                StudyQuestCard(
                    modifier = Modifier.widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = StudyQuestStrings.t(prefs.uiLanguage, "Loading StudyQuest...", "লোড হচ্ছে..."),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                prefs.uiLanguage,
                                "Syncing your student profile and curriculum progress...",
                                "তোমার প্রোফাইল ও পড়াশোনার অগ্রগতি সিঙ্ক হচ্ছে..."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        TextButton(
                            onClick = { sessionVm.signOut() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("bootstrap_loading_sign_out")
                        ) {
                            Text(StudyQuestStrings.t(prefs.uiLanguage, "Switch Account", "অ্যাকাউন্ট বদলান"))
                        }
                    }
                }
            }
        }

        is BootstrapState.Error -> {
            BackHandler { sessionVm.signOut() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                StudyQuestCard(
                    modifier = Modifier.widthIn(max = 440.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = StudyQuestStrings.t(
                                prefs.uiLanguage,
                                "Couldn't Load Your Profile",
                                "প্রোফাইল লোড করা যায়নি"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { sessionVm.retryBootstrap() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("bootstrap_retry_button")
                        ) {
                            Text(StudyQuestStrings.t(prefs.uiLanguage, "Retry", "আবার চেষ্টা করুন"))
                        }
                        OutlinedButton(
                            onClick = { sessionVm.signOut() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("bootstrap_sign_out_button")
                        ) {
                            Text(StudyQuestStrings.t(prefs.uiLanguage, "Switch Account", "অ্যাকাউন্ট বদলান"))
                        }
                    }
                }
            }
        }

        is BootstrapState.Ready -> {
            val activeProfile = state.profile
            var editingAcademicSetup by remember(activeProfile.userId) { mutableStateOf(false) }
            if (!activeProfile.onboardingCompleted || editingAcademicSetup) {
                AcademicSetupWizardScreen(
                    profile = activeProfile,
                    language = prefs.uiLanguage,
                    isSaving = isCompletingSetup,
                    errorMessage = sessionBanner,
                    onSignOut = {
                        editingAcademicSetup = false
                        sessionVm.signOut()
                    },
                    onBack = {
                        editingAcademicSetup = false
                        sessionVm.signOut()
                    },
                    cloudTextbooks = cloudTextbooks,
                    onCompleteSetupWithBook = { name, cls, ver, subjs, avatar, grp, dailyMins, goalTitle, book ->
                        AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                        editingAcademicSetup = false
                        sessionVm.completeAcademicSetup(
                            displayName = name,
                            classId = cls,
                            curriculumVersion = ver,
                            selectedSubjects = subjs,
                            avatarId = avatar,
                            academicGroup = grp,
                            dailyGoalMinutes = dailyMins,
                            studyGoalTitle = goalTitle,
                            activeBook = book
                        )
                        if (ageConsentDirty) {
                            sessionVm.saveAgeAndConsent(pendingAgeBracket, pendingGuardianConsent)
                            onClearAgeConsentDirty()
                        }
                    },
                    onCompleteSetup = { name, cls, ver, subjs, avatar, grp, dailyMins, goalTitle ->
                        AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                        editingAcademicSetup = false
                        sessionVm.completeAcademicSetup(
                            displayName = name,
                            classId = cls,
                            curriculumVersion = ver,
                            selectedSubjects = subjs,
                            avatarId = avatar,
                            academicGroup = grp,
                            dailyGoalMinutes = dailyMins,
                            studyGoalTitle = goalTitle
                        )
                        if (ageConsentDirty) {
                            sessionVm.saveAgeAndConsent(pendingAgeBracket, pendingGuardianConsent)
                            onClearAgeConsentDirty()
                        }
                    }
                )
            } else if (!prefs.welcomePlanCompleted && subscription.tier == "free") {
                WelcomePlanSelectionScreen(
                    language = prefs.uiLanguage,
                    billingConfigured = systemSettings.billingConfigured,
                    onContinueWithFree = { sessionVm.markWelcomePlanCompleted() },
                    onSelectPaidPlan = { planId ->
                        sessionVm.requestPlanUpgrade(planId)
                        sessionVm.markWelcomePlanCompleted()
                    },
                    onBack = { editingAcademicSetup = true }
                )
            } else {
                AuthenticatedReadyHost(
                    user = user,
                    activeProfile = activeProfile,
                    prefs = prefs,
                    sessionBanner = sessionBanner,
                    sessionVm = sessionVm
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedReadyHost(
    user: FirebaseUser,
    activeProfile: PublicProfile,
    prefs: LocalUserPreferences,
    sessionBanner: String?,
    sessionVm: MainSessionViewModel
) {
    val view = LocalView.current

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
    val focusVm: FocusTimerViewModel = viewModel(
        key = "focus_${user.uid}",
        factory = StudyQuestViewModelFactories.focusTimerFactory(user.uid)
    )
    val aiVideoVm: AiVideoViewModel = viewModel(
        key = "aivideo_${user.uid}",
        factory = StudyQuestViewModelFactories.aiVideoFactory(user.uid)
    )

    val isOnline by sessionVm.isOnline.collectAsStateWithLifecycle()
    val quests by sessionVm.quests.collectAsStateWithLifecycle()
    val notifications by sessionVm.notifications.collectAsStateWithLifecycle()
    val unreadNotifs by sessionVm.unreadNotificationCount.collectAsStateWithLifecycle()
    val unlockedCosmetics by sessionVm.unlockedCosmetics.collectAsStateWithLifecycle()
    val adminRole by sessionVm.adminRole.collectAsStateWithLifecycle()
    val exportedJson by sessionVm.exportedJson.collectAsStateWithLifecycle()
    val guestMergedCount by sessionVm.guestMergedCount.collectAsStateWithLifecycle()
    val badges by sessionVm.badges.collectAsStateWithLifecycle()
    val xpLedger by sessionVm.xpLedger.collectAsStateWithLifecycle()
    val studyGoals by sessionVm.studyGoals.collectAsStateWithLifecycle()
    val assignments by sessionVm.assignments.collectAsStateWithLifecycle()
    val assignmentSubmissions by sessionVm.assignmentSubmissions.collectAsStateWithLifecycle()
    val systemSettings by sessionVm.systemSettings.collectAsStateWithLifecycle()
    val subscription by sessionVm.subscription.collectAsStateWithLifecycle()
    val aiCreditLedger by sessionVm.aiCreditLedger.collectAsStateWithLifecycle()
    val videoCreditLedger by sessionVm.videoCreditLedger.collectAsStateWithLifecycle()
    val purchaseHistory by sessionVm.purchaseHistory.collectAsStateWithLifecycle()
    val adPlacementState by sessionVm.adPlacementState.collectAsStateWithLifecycle()
    val availableBooksForLock by sessionVm.availableBooksForProfile.collectAsStateWithLifecycle()
    val activeLockedBook by sessionVm.activeLockedBook.collectAsStateWithLifecycle()
    val cachedBooks by sessionVm.cachedBooks.collectAsStateWithLifecycle()

    val bookChapters by remember(activeLockedBook.id) {
        learnVm.observeBookChapters(activeLockedBook.id)
    }.collectAsStateWithLifecycle(emptyList())
    val bookNotes by remember(activeLockedBook.id) {
        learnVm.observeBookNotes(activeLockedBook.id)
    }.collectAsStateWithLifecycle(emptyList())

    val learningRecords by learnVm.learningRecords.collectAsStateWithLifecycle()
    val mergedQuestions by learnVm.mergedQuestions.collectAsStateWithLifecycle()
    val cachedLessons by learnVm.cachedLessons.collectAsStateWithLifecycle()
    val bookmarks by learnVm.bookmarks.collectAsStateWithLifecycle()
    val revisions by learnVm.revisions.collectAsStateWithLifecycle()
    val studyPlans by learnVm.studyPlans.collectAsStateWithLifecycle()
    val pendingSyncCount by learnVm.pendingSyncCount.collectAsStateWithLifecycle()
    val weakTopics by learnVm.weakTopics.collectAsStateWithLifecycle()
    val subjectAnalytics by learnVm.subjectAnalytics.collectAsStateWithLifecycle()
    val dailyStudySeries by learnVm.dailyStudySeries.collectAsStateWithLifecycle()
    val examRemaining by learnVm.examRemainingSeconds.collectAsStateWithLifecycle()
    val assessmentResult by learnVm.lastAssessmentResult.collectAsStateWithLifecycle()
    val aiState by learnVm.aiState.collectAsStateWithLifecycle()
    val aiConversations by learnVm.aiConversations.collectAsStateWithLifecycle()
    val activeAiConversationId by learnVm.activeConversationId.collectAsStateWithLifecycle()
    val activeAiMessages by learnVm.activeAiMessages.collectAsStateWithLifecycle()
    val activeAiToolId by learnVm.activeToolId.collectAsStateWithLifecycle()
    val downloadPacks by learnVm.downloadPacks.collectAsStateWithLifecycle()
    val unresolvedMistakes by learnVm.unresolvedMistakes.collectAsStateWithLifecycle()
    val examCountdowns by learnVm.examCountdowns.collectAsStateWithLifecycle()
    val formulaBookmarks by learnVm.formulaBookmarks.collectAsStateWithLifecycle()
    val learnBanner by learnVm.statusBanner.collectAsStateWithLifecycle()

    val myMatches by competeVm.myMatches.collectAsStateWithLifecycle()
    val activeMatch by competeVm.activeMatch.collectAsStateWithLifecycle()
    val activeRequest by competeVm.activeMatchRequest.collectAsStateWithLifecycle()
    val activeSubmissions by competeVm.activeSubmissions.collectAsStateWithLifecycle()
    val leaderboard by competeVm.leaderboard.collectAsStateWithLifecycle()
    val selectedCategory by competeVm.selectedCategory.collectAsStateWithLifecycle()
    val selectedPeriod by competeVm.selectedPeriod.collectAsStateWithLifecycle()
    val competitionArchives by competeVm.competitionArchives.collectAsStateWithLifecycle()
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
    val activeGroupParticipants by socialVm.activeGroupParticipants.collectAsStateWithLifecycle()
    val communityPosts by socialVm.communityPosts.collectAsStateWithLifecycle()
    val selectedCommunityPost by socialVm.selectedPost.collectAsStateWithLifecycle()
    val selectedPostComments by socialVm.selectedPostComments.collectAsStateWithLifecycle()
    val socialBanner by socialVm.message.collectAsStateWithLifecycle()

    val tabBackStack = remember { mutableStateListOf(0) } // 0=Today, 1=Learn, 2=Compete, 3=Friends, 4=Profile
    val selectedTab = tabBackStack.lastOrNull() ?: 0
    val saveableStateHolder = rememberSaveableStateHolder()
    var todayOverlay by remember { mutableStateOf(TodayOverlayDestination.None) }
    var aiDoubtPrefillPrompt by remember { mutableStateOf("") }
    var leaderboardScope by remember { mutableStateOf(LeaderboardScopeFilter.CLASS_ONLY) }
    var leaderboardSubjectFilter by remember { mutableStateOf("all") }
    var pendingFriendsSubTab by remember { mutableStateOf<Int?>(null) }

    fun navigateToTab(idx: Int) {
        todayOverlay = TodayOverlayDestination.None
        if (idx == 0) {
            tabBackStack.clear()
            tabBackStack.add(0)
        } else if (tabBackStack.lastOrNull() != idx) {
            tabBackStack.remove(idx)
            tabBackStack.add(idx)
        }
    }
    var pendingLearnDest by remember { mutableStateOf<LearnDestination?>(null) }
    var showGlobalAiSheet by remember { mutableStateOf(false) }
    var showPlanStoreOverlay by remember { mutableStateOf(false) }
    var celebrationPulse by remember { mutableIntStateOf(0) }

    BackHandler(enabled = showGlobalAiSheet) {
        learnVm.resetAiDoubtState()
        showGlobalAiSheet = false
    }

    BackHandler(enabled = !showPlanStoreOverlay && !showGlobalAiSheet && selectedTab == 0 && todayOverlay != TodayOverlayDestination.None) {
        todayOverlay = TodayOverlayDestination.None
        aiDoubtPrefillPrompt = ""
    }

    BackHandler(enabled = !showPlanStoreOverlay && !showGlobalAiSheet && (selectedTab != 0 || todayOverlay == TodayOverlayDestination.None) && tabBackStack.size > 1) {
        tabBackStack.removeAt(tabBackStack.lastIndex)
    }

    LaunchedEffect(activeProfile.classId) {
        socialVm.setCommunityClassId(activeProfile.classId)
    }

    LaunchedEffect(guestMergedCount) {
        if (guestMergedCount > 0) {
            celebrationPulse += 1
        }
    }

    val activeBanner = sessionBanner ?: learnBanner ?: competeBanner ?: socialBanner
    val lang = prefs.uiLanguage

    fun openPromotionExam() {
        val qs = mergedQuestions.filter { it.classId == activeProfile.classId }.ifEmpty { mergedQuestions }.take(8)
        val tpl = ExamTemplate(
            id = "promotion_${activeProfile.classId}",
            titleEn = "Annual Class Promotion Exam (${StudyQuestStrings.classLabel(activeProfile.classId, lang)})",
            titleBn = "বার্ষিক শ্রেণি প্রোমোশন পরীক্ষা (${StudyQuestStrings.classLabel(activeProfile.classId, lang)})",
            classId = activeProfile.classId,
            subjectIds = BangladeshCurriculumCatalog.subjectsForClass(activeProfile.classId).map { it.id },
            durationSeconds = 900,
            totalMarks = 8,
            negativeMarkingPerWrong = 0.0,
            descriptionEn = "Pass 60%+ to advance your locked class.",
            descriptionBn = "উত্তীর্ণ হতে ৬০%+ নম্বর অর্জন করুন।"
        )
        pendingLearnDest = LearnDestination.MockExamSession(tpl, qs)
        navigateToTab(1)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        QuestNavigationScaffold(
            selectedTab = selectedTab,
            language = lang,
            profileDisplayName = activeProfile.displayName,
            profileAvatarId = activeProfile.avatarId,
            profileFrameId = activeProfile.frameId,
            totalXp = activeProfile.totalXp,
            streakCount = activeProfile.streakCount,
            soundEnabled = prefs.soundEnabled,
            reducedMotion = prefs.reducedMotion,
            onSelectTab = { idx -> navigateToTab(idx) },
            adBannerSlot = if (adPlacementState.shouldDisplayAdSlot) {
                {
                    Surface(
                        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admob_banner_slot")
                    ) {
                        Text(
                            text = StudyQuestStrings.t(
                                lang,
                                "Ad Slot Ready (Paused automatically during Focus, Quizzes & Exams)",
                                "বিজ্ঞাপন স্লট প্রস্তুত (ফোকাস, কুইজ ও পরীক্ষায় স্বয়ংক্রিয়ভাবে বন্ধ)"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            } else null,
            topBannerSlot = {
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
            }
        ) {
            if (showPlanStoreOverlay) {
                    SubscriptionSelectionScreen(
                        language = lang,
                        subscription = subscription,
                        billingConfigured = systemSettings.billingConfigured,
                        aiCreditLedger = aiCreditLedger,
                        videoCreditLedger = videoCreditLedger,
                        purchaseHistory = purchaseHistory,
                        onSelectPlan = { sessionVm.requestPlanUpgrade(it) },
                        onPurchaseVideoBoost = { sessionVm.purchaseVideoBoost() },
                        onSwitchToFree = { sessionVm.switchToFreePlan() },
                        onOpenAiLab = {
                            showPlanStoreOverlay = false
                            pendingLearnDest = LearnDestination.AiLab
                            navigateToTab(1)
                        },
                        onBack = { showPlanStoreOverlay = false }
                    )
                } else {
                    saveableStateHolder.SaveableStateProvider(key = selectedTab) {
                        when (selectedTab) {
                    0 -> when (todayOverlay) {
                        TodayOverlayDestination.StudyProgress -> StudyProgressDashboardScreen(
                            profile = activeProfile,
                            language = lang,
                            reducedMotion = prefs.reducedMotion,
                            learningRecords = learningRecords,
                            dailyStudySeries = dailyStudySeries,
                            subjectAnalytics = subjectAnalytics,
                            weakTopics = weakTopics,
                            onOpenQuickPractice = {
                                todayOverlay = TodayOverlayDestination.None
                                val qs = mergedQuestions.filter { it.classId == activeProfile.classId }.ifEmpty { mergedQuestions }.take(6)
                                pendingLearnDest = LearnDestination.PracticeSession(
                                    StudyQuestStrings.t(lang, "Quick Practice", "কুইক প্র্যাকটিস"),
                                    qs
                                )
                                navigateToTab(1)
                            },
                            onBack = { todayOverlay = TodayOverlayDestination.None }
                        )

                        TodayOverlayDestination.Leaderboard -> LeaderboardScreen(
                            myProfile = activeProfile,
                            language = lang,
                            reducedMotion = prefs.reducedMotion,
                            leaderboard = leaderboard,
                            selectedScope = leaderboardScope,
                            selectedPeriod = selectedPeriod,
                            selectedCategory = selectedCategory,
                            selectedSubjectId = leaderboardSubjectFilter,
                            friendUids = friendProfiles.keys,
                            isGuestMode = false,
                            onSelectScope = { leaderboardScope = it },
                            onSelectPeriod = { competeVm.selectPeriod(it) },
                            onSelectCategory = { competeVm.selectCategory(it) },
                            onSelectSubject = { leaderboardSubjectFilter = it },
                            onRefresh = {},
                            onBack = { todayOverlay = TodayOverlayDestination.None }
                        )

                        TodayOverlayDestination.AiDoubtSolver -> AiDoubtSolverScreen(
                            profile = activeProfile,
                            language = lang,
                            reducedMotion = prefs.reducedMotion,
                            initialQuestionPrompt = aiDoubtPrefillPrompt,
                            onAskGeminiDoubt = { prompt, studentQ, subjName ->
                                learnVm.streamDirectGeminiDoubt(
                                    questionPrompt = prompt,
                                    studentQuestion = studentQ,
                                    classId = activeProfile.classId,
                                    subjectName = subjName,
                                    language = lang,
                                    activeBookTitle = StudyQuestStrings.t(lang, activeLockedBook.titleEn, activeLockedBook.titleBn)
                                )
                            },
                            onBack = {
                                todayOverlay = TodayOverlayDestination.None
                                aiDoubtPrefillPrompt = ""
                            },
                            isOnline = isOnline
                        )

                        TodayOverlayDestination.None -> TodayTabScreen(
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
                                navigateToTab(1)
                            },
                            onOpenMockExam = {
                                navigateToTab(1)
                            },
                            onOpenRevision = {
                                pendingLearnDest = LearnDestination.RevisionAndPlanner
                                navigateToTab(1)
                            },
                            onOpenAiDoubtSolver = {
                                aiDoubtPrefillPrompt = ""
                                todayOverlay = TodayOverlayDestination.AiDoubtSolver
                            },
                            onOpenMessages = { navigateToTab(3) },
                            subjectAnalytics = subjectAnalytics,
                            dailyStudySeries = dailyStudySeries,
                            dailyChallengeDoneToday = learnVm.isDailyChallengeDoneToday(activeProfile),
                            onOpenSubjectCategory = { subj ->
                                pendingLearnDest = LearnDestination.SubjectDetail(subj)
                                navigateToTab(1)
                            },
                            onOpenFlashcardsVault = {
                                pendingLearnDest = LearnDestination.FlashcardsAndVault
                                navigateToTab(1)
                            },
                            onOpenPomodoro = {
                                pendingLearnDest = LearnDestination.PomodoroFocus
                                navigateToTab(1)
                            },
                            onOpenDailyChallenge = {
                                pendingLearnDest = LearnDestination.DailyChallenge
                                navigateToTab(1)
                            },
                            onOpenMasteryAnalytics = {
                                pendingLearnDest = LearnDestination.MasteryAnalytics
                                navigateToTab(1)
                            },
                            onOpenAssignmentsAndGoals = {
                                pendingLearnDest = LearnDestination.AssignmentsAndGoals
                                navigateToTab(1)
                            },
                            onOpenAiVideoStudio = {
                                pendingLearnDest = LearnDestination.AiVideoStudio
                                navigateToTab(1)
                            },
                            onOpenBadgesAndXp = {
                                pendingLearnDest = LearnDestination.BadgesAndXp
                                navigateToTab(1)
                            },
                            onBuyStreakFreeze = { sessionVm.purchaseStreakFreeze() },
                            onRetakePromotionExam = { openPromotionExam() },
                            subscription = subscription,
                            onOpenAiLab = {
                                pendingLearnDest = LearnDestination.AiLab
                                navigateToTab(1)
                            },
                            onOpenPlanStore = { showPlanStoreOverlay = true },
                            onOpenStudyProgress = { todayOverlay = TodayOverlayDestination.StudyProgress },
                            onOpenStudyGroups = {
                                pendingFriendsSubTab = 3
                                navigateToTab(3)
                            },
                            onOpenLeaderboard = { todayOverlay = TodayOverlayDestination.Leaderboard },
                            activeLockedBook = activeLockedBook,
                            availableBooksForLock = availableBooksForLock,
                            cachedBooks = cachedBooks,
                            onOpenTextbookReader = {
                                pendingLearnDest = LearnDestination.TextbookReader
                                navigateToTab(1)
                            },
                            onSwitchActiveBook = { book -> sessionVm.switchActiveBook(book) },
                            userBadges = badges,
                            onOpenStemLab = {
                                val labId = when (activeLockedBook.subjectId) {
                                    "physics" -> "physics"
                                    "chemistry" -> "chem"
                                    "biology", "gen_science" -> "bio"
                                    else -> "trig"
                                }
                                pendingLearnDest = LearnDestination.InteractiveStemLab(labId)
                                navigateToTab(1)
                            },
                            onOpenDownloadManager = {
                                pendingLearnDest = LearnDestination.OfflineDownloads
                                navigateToTab(1)
                            },
                            onOpenFormulaAtlas = {
                                pendingLearnDest = LearnDestination.FormulaAtlas
                                navigateToTab(1)
                            },
                            onOpenMistakeNotebook = {
                                pendingLearnDest = LearnDestination.MistakeNotebook
                                navigateToTab(1)
                            },
                            onOpenExamCountdown = {
                                pendingLearnDest = LearnDestination.ExamCountdown
                                navigateToTab(1)
                            },
                            onOpenPocketAudio = {
                                pendingLearnDest = LearnDestination.PocketAudioRevision
                                navigateToTab(1)
                            },
                            unresolvedMistakesCount = unresolvedMistakes.size
                        )
                    }

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
                        onDownloadChapter = { learnVm.downloadChapterOffline(it, activeLockedBook.id) },
                        onCompleteLesson = { lesson ->
                            learnVm.completeLesson(lesson, isOnline, bookId = activeLockedBook.id) {
                                AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                            }
                        },
                        onToggleBookmark = { learnVm.toggleBookmark(it, bookId = activeLockedBook.id) },
                        onAddRevision = { learnVm.addToRevision(it) },
                        onReviewRevision = { id, q -> learnVm.reviewRevisionItem(id, q) },
                        onCreateStudyPlan = { t, s, p, d, m -> learnVm.createStudyPlan(t, s, p, d, m, bookId = activeLockedBook.id) },
                        onToggleStudyPlan = { learnVm.toggleStudyPlan(it) },
                        onDeleteStudyPlan = { learnVm.deleteStudyPlan(it) },
                        onStartExamTimer = { sec, cb -> learnVm.startExamTimer(sec, cb) },
                        onSubmitAssessment = { title, qs, sel, txt, cq, tpl ->
                            learnVm.submitPracticeOrExam(title, qs, sel, txt, cq, tpl, isOnline, bookId = activeLockedBook.id) {
                                AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                            }
                        },
                        onClearAssessmentResult = { learnVm.clearAssessmentResult() },
                        onReportQuestion = { q ->
                            socialVm.reportIssue("question", q.id, "Content accuracy review", q.promptEn)
                        },
                        onAskAi = { prompt, studentQ, cls, subj ->
                            learnVm.askAiDoubtSolver(
                                prompt, studentQ, cls, subj, lang, subscription,
                                activeBookTitle = StudyQuestStrings.t(lang, activeLockedBook.titleEn, activeLockedBook.titleBn)
                            )
                        },
                        onResetAi = { learnVm.resetAiDoubtState() },
                        reducedMotion = prefs.reducedMotion,
                        profile = activeProfile,
                        subjectAnalytics = subjectAnalytics,
                        weakTopics = weakTopics,
                        dailyChallengeQuestions = remember(mergedQuestions, activeLockedBook) {
                            learnVm.dailyChallengeQuestions(
                                TextbookCatalog.questionsForActiveBook(mergedQuestions, activeLockedBook),
                                activeProfile.classId
                            )
                        },
                        dailyChallengeDoneToday = learnVm.isDailyChallengeDoneToday(activeProfile),
                        dailyChallengeDateKey = learnVm.todayChallengeDateKey(),
                        onCompletePomodoro = { subjId, mins ->
                            learnVm.completePomodoroSession(subjId, mins, isOnline, bookId = activeLockedBook.id) {
                                celebrationPulse += 1
                                AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                            }
                        },
                        onCompleteFlashcardDeck = { subjId, rev, mast ->
                            learnVm.recordFlashcardSession(subjId, rev, mast, isOnline, bookId = activeLockedBook.id) {
                                celebrationPulse += 1
                                AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                            }
                        },
                        onSubmitDailyChallenge = { qs, sel, txt ->
                            learnVm.completeDailyChallenge(qs, sel, txt, isOnline, bookId = activeLockedBook.id) {
                                celebrationPulse += 1
                                AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                            }
                        },
                        focusVm = focusVm,
                        aiVideoVm = aiVideoVm,
                        dailyStudySeries = dailyStudySeries,
                        studyGoals = studyGoals,
                        assignments = assignments,
                        assignmentSubmissions = assignmentSubmissions,
                        userBadges = badges,
                        xpLedger = xpLedger,
                        externalVideoRendererConfigured = systemSettings.externalVideoRendererConfigured,
                        isGuestMode = false,
                        onCreateStudyGoal = { t, gt, pt, s, tv, d -> sessionVm.createStudyGoal(t, gt, pt, s, tv, d) },
                        onClaimStudyGoal = { sessionVm.claimStudyGoal(it) },
                        onDeleteStudyGoal = { sessionVm.deleteStudyGoal(it) },
                        onSubmitAssignment = { sessionVm.submitAssignment(it) },
                        onToggleBadgeFavorite = { sessionVm.toggleBadgeFavorite(it) },
                        onToggleBadgePin = { sessionVm.toggleBadgePin(it) },
                        onSetExamOrQuizActive = { learnVm.setExamOrQuizActive(it) },
                        subscription = subscription,
                        aiConversations = aiConversations,
                        activeAiConversationId = activeAiConversationId,
                        activeAiMessages = activeAiMessages,
                        activeAiToolId = activeAiToolId,
                        onSelectAiTool = { learnVm.selectAiTool(it) },
                        onSelectAiConversation = { learnVm.openAIConversation(it) },
                        onCreateAiConversation = { title, toolId ->
                            learnVm.createAIConversation(title, toolId, activeProfile.classId, activeLockedBook.subjectId)
                        },
                        onDeleteAiConversation = { learnVm.deleteAIConversation(it) },
                        onSendAiLabMessage = { msg ->
                            learnVm.sendStudyQuestMessage(
                                userText = msg,
                                classId = activeProfile.classId,
                                subjectName = activeLockedBook.subjectId,
                                language = lang,
                                subscription = subscription,
                                activeBookTitle = StudyQuestStrings.t(lang, activeLockedBook.titleEn, activeLockedBook.titleBn)
                            )
                        },
                        onCancelAiRequest = { learnVm.cancelAIRequest() },
                        onRetryAiRequest = { learnVm.retryFailedAIRequest() },
                        onOpenPlanStore = { showPlanStoreOverlay = true },
                        activeLockedBook = activeLockedBook,
                        availableBooksForLock = availableBooksForLock,
                        cachedBooks = cachedBooks,
                        bookChapters = bookChapters,
                        bookNotes = bookNotes,
                        onInstallBookOffline = { book, force -> learnVm.installBookOffline(book, lang, force) },
                        onImportPdfForBook = { book, uri -> learnVm.importPdfForBook(book, uri, lang) },
                        onSaveLastReadPage = { bookId, page -> learnVm.saveLastReadPage(bookId, page) },
                        onMarkBookChapterCompleted = { chId -> learnVm.markBookChapterCompleted(chId) },
                        onSaveBookPageNote = { bookId, page, chId, title, text, isBm ->
                            learnVm.saveBookPageNote(bookId, page, chId, title, text, isBm)
                        },
                        onDeleteBookPageNote = { noteId -> learnVm.deleteBookPageNote(noteId) },
                        onSwitchActiveBook = { book -> sessionVm.switchActiveBook(book) },
                        onReportBookIssue = { tType, tId, details ->
                            socialVm.reportIssue(tType, tId, "Textbook content review", details)
                        },
                        downloadPacks = downloadPacks,
                        unresolvedMistakes = unresolvedMistakes,
                        examCountdowns = examCountdowns,
                        formulaBookmarks = formulaBookmarks,
                        learningRecords = learningRecords,
                        onSeedDownloads = {
                            learnVm.seedDownloadCatalog(
                                activeProfile.classId,
                                activeProfile.curriculumVersion,
                                activeProfile.academicGroup
                            )
                        },
                        onDownloadPack = { pack -> learnVm.downloadPack(pack, isOnline, lang) },
                        onDownloadClassBundle = {
                            learnVm.downloadClassBundle(
                                activeProfile.classId,
                                activeProfile.curriculumVersion,
                                activeProfile.academicGroup,
                                isOnline,
                                lang
                            )
                        },
                        onRemoveDownloadPack = { packId -> learnVm.removeDownloadedPack(packId) },
                        onToggleFormulaBookmark = { entry -> learnVm.toggleFormulaBookmark(entry) },
                        onResolveMistake = { qId -> learnVm.resolveMistake(qId) },
                        onAddExamCountdown = { t, s, days, goal -> learnVm.addExamCountdown(t, s, days, goal) },
                        onIncrementExamTopic = { id -> learnVm.incrementExamTopic(id) },
                        onDeleteExamCountdown = { id -> learnVm.deleteExamCountdown(id) }
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
                        onJoinTournament = { competeVm.joinTournament(it) },
                        selectedCategory = selectedCategory,
                        selectedPeriod = selectedPeriod,
                        competitionArchives = competitionArchives,
                        onSelectCategory = { competeVm.selectCategory(it) },
                        onSelectPeriod = { competeVm.selectPeriod(it) },
                        isOnline = isOnline
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
                        reducedMotion = prefs.reducedMotion,
                        communityPosts = communityPosts,
                        selectedCommunityPost = selectedCommunityPost,
                        selectedPostComments = selectedPostComments,
                        onOpenCommunityPost = { socialVm.openCommunityPost(it) },
                        onCloseCommunityPost = { socialVm.closeCommunityPost() },
                        onCreateCommunityPost = { subj, type, title, body ->
                            socialVm.createCommunityPost(activeProfile, subj, type, title, body)
                        },
                        onAddCommunityComment = { post, txt ->
                            socialVm.addCommunityComment(activeProfile, post, txt)
                        },
                        onToggleCommunityUpvote = { socialVm.toggleCommunityUpvote(it) },
                        onMarkCommentHelpful = { post, c -> socialVm.markCommentHelpful(post, c) },
                        onReportCommunityPost = { post, reason -> socialVm.reportCommunityPost(post, reason) },
                        onDeleteOwnCommunityPost = { socialVm.deleteOwnCommunityPost(it) },
                        externalSubTab = pendingFriendsSubTab,
                        onConsumeExternalSubTab = { pendingFriendsSubTab = null },
                        activeGroupParticipants = activeGroupParticipants,
                        onUpdateGroupStatus = { conv, status -> socialVm.updateGroupParticipantStatus(conv, activeProfile, status) },
                        onUpdateGroupSharedProblem = { conv, title, prompt ->
                            socialVm.updateGroupSharedProblem(conv, activeProfile.displayName, title, prompt)
                        },
                        onAskAiAboutProblem = { problem ->
                            aiDoubtPrefillPrompt = problem
                            navigateToTab(0)
                            todayOverlay = TodayOverlayDestination.AiDoubtSolver
                        },
                        onCreateStudyGroupWithSubject = { name, uids, subj -> socialVm.createStudyGroup(name, uids, subj) },
                        isOnline = isOnline
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
                        },
                        userBadges = badges,
                        xpLedger = xpLedger,
                        dailyStudySeries = dailyStudySeries,
                        subscription = subscription,
                        billingConfigured = systemSettings.billingConfigured,
                        onUpdateThemeMode = { sessionVm.updateThemeMode(it) },
                        onUpdateSurfaceStyle = { sessionVm.updateSurfaceStyle(it) },
                        onUpdateNotificationCategory = { c, e -> sessionVm.updateNotificationCategory(c, e) },
                        onToggleBadgeFavorite = { sessionVm.toggleBadgeFavorite(it) },
                        onToggleBadgePin = { sessionVm.toggleBadgePin(it) },
                        onTriggerBillingUpgrade = { showPlanStoreOverlay = true },
                        onRetakePromotionExam = { openPromotionExam() },
                        aiCreditLedger = aiCreditLedger,
                        videoCreditLedger = videoCreditLedger,
                        purchaseHistory = purchaseHistory,
                        onSelectPlan = { sessionVm.requestPlanUpgrade(it) },
                        onPurchaseVideoBoost = { sessionVm.purchaseVideoBoost() },
                        onSwitchToFreePlan = { sessionVm.switchToFreePlan() },
                        onOpenAiLab = {
                            pendingLearnDest = LearnDestination.AiLab
                            navigateToTab(1)
                        },
                        activeLockedBook = activeLockedBook,
                        cachedBooks = cachedBooks,
                        onOpenTextbookReader = {
                            pendingLearnDest = LearnDestination.TextbookReader
                            navigateToTab(1)
                        },
                        onReinstallBookOffline = { book -> learnVm.installBookOffline(book, lang, forceRedownload = true) },
                        onDeleteCachedBook = { bookId -> learnVm.deleteOfflineBookCache(bookId) },
                        onOpenDownloadManager = {
                            pendingLearnDest = LearnDestination.OfflineDownloads
                            navigateToTab(1)
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

        GamificationCelebrationHost(
            language = lang,
            reducedMotion = prefs.reducedMotion
        )
    }

    if (showGlobalAiSheet) {
        val normSub = BillingAndAdProvider.normalizeSubscription(subscription)
        val plan = QuestPlanCatalog.planById(normSub.tier)
        val aiRemaining = (normSub.monthlyAiLimit.toInt().coerceAtLeast(plan.monthlyAiCredits) - normSub.aiCreditsUsed.toInt()).coerceAtLeast(0)
        AiDoubtSolverSheet(
            initialContext = "",
            classId = activeProfile.classId,
            subjectName = "General Study",
            language = lang,
            aiState = aiState,
            remainingAiCredits = aiRemaining,
            onOpenAiLab = {
                showGlobalAiSheet = false
                pendingLearnDest = LearnDestination.AiLab
                navigateToTab(1)
            },
            onSubmitDoubt = { q ->
                learnVm.askAiDoubtSolver(
                    "", q, activeProfile.classId, activeLockedBook.subjectId, lang, subscription,
                    activeBookTitle = StudyQuestStrings.t(lang, activeLockedBook.titleEn, activeLockedBook.titleBn)
                )
            },
            onDismiss = {
                learnVm.resetAiDoubtState()
                showGlobalAiSheet = false
            },
            isOnline = isOnline
        )
    }
}
