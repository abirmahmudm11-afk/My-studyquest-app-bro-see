package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import com.example.data.curriculum.TextbookCatalog
import com.example.data.model.ExamTemplate
import com.example.data.repository.GuestDataRepository
import com.example.data.repository.LeaderboardCategory
import com.example.data.repository.LeaderboardPeriod
import com.example.ui.components.AudioFeedbackHelper
import com.example.ui.components.GamificationCelebrationHost
import com.example.ui.components.LottieCelebrationOverlay
import com.example.ui.components.QuestNavigationScaffold
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
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
    val downloadPacks by learnVm.downloadPacks.collectAsStateWithLifecycle()
    val unresolvedMistakes by learnVm.unresolvedMistakes.collectAsStateWithLifecycle()
    val examCountdowns by learnVm.examCountdowns.collectAsStateWithLifecycle()
    val formulaBookmarks by learnVm.formulaBookmarks.collectAsStateWithLifecycle()
    val learnBanner by learnVm.statusBanner.collectAsStateWithLifecycle()

    val activeLockedBook by guestVm.activeLockedBook.collectAsStateWithLifecycle()
    val cachedBooks by learnVm.cachedBooks.collectAsStateWithLifecycle()
    val bookChapters by remember(activeLockedBook.id) {
        learnVm.observeBookChapters(activeLockedBook.id)
    }.collectAsStateWithLifecycle(emptyList())
    val bookNotes by remember(activeLockedBook.id) {
        learnVm.observeBookNotes(activeLockedBook.id)
    }.collectAsStateWithLifecycle(emptyList())
    val availableBooksForLock = remember(profile.classId, profile.curriculumVersion, profile.academicGroup, profile.activeBookYear) {
        TextbookCatalog.booksFor(
            academicYear = profile.activeBookYear.ifBlank { "2026" },
            classId = profile.classId,
            curriculumVersion = profile.curriculumVersion,
            academicGroup = profile.academicGroup
        ).ifEmpty {
            TextbookCatalog.books.filter { it.classId == profile.classId }
        }.ifEmpty { TextbookCatalog.books }
    }

    val tabBackStack = remember { mutableStateListOf(0) }
    val selectedTab = tabBackStack.lastOrNull() ?: 0
    val saveableStateHolder = rememberSaveableStateHolder()
    var todayOverlay by remember { mutableStateOf(TodayOverlayDestination.None) }
    var leaderboardScope by remember { mutableStateOf(LeaderboardScopeFilter.CLASS_ONLY) }
    var leaderboardPeriod by remember { mutableStateOf(LeaderboardPeriod.WEEKLY) }
    var leaderboardCategory by remember { mutableStateOf(LeaderboardCategory.XP) }
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

    BackHandler(enabled = showGlobalAiSheet) {
        learnVm.resetAiDoubtState()
        showGlobalAiSheet = false
    }

    BackHandler(enabled = !showGlobalAiSheet && selectedTab == 0 && todayOverlay != TodayOverlayDestination.None) {
        todayOverlay = TodayOverlayDestination.None
    }

    BackHandler(enabled = !showGlobalAiSheet && (selectedTab != 0 || todayOverlay == TodayOverlayDestination.None) && tabBackStack.size > 1) {
        tabBackStack.removeAt(tabBackStack.lastIndex)
    }

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
        navigateToTab(1)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        QuestNavigationScaffold(
            selectedTab = selectedTab,
            language = lang,
            profileDisplayName = profile.displayName,
            profileAvatarId = profile.avatarId,
            profileFrameId = profile.frameId,
            totalXp = profile.totalXp,
            streakCount = profile.streakCount,
            soundEnabled = prefs.soundEnabled,
            reducedMotion = prefs.reducedMotion,
            onSelectTab = { idx -> navigateToTab(idx) },
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
                                onClearAuthError()
                                guestVm.clearBanner()
                                learnVm.clearBanner()
                            }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        ) {
            saveableStateHolder.SaveableStateProvider(key = selectedTab) {
                        when (selectedTab) {
                            0 -> when (todayOverlay) {
                                TodayOverlayDestination.StudyProgress -> StudyProgressDashboardScreen(
                                    profile = profile,
                                    language = lang,
                                    reducedMotion = prefs.reducedMotion,
                                    learningRecords = learningRecords,
                                    dailyStudySeries = dailyStudySeries,
                                    subjectAnalytics = subjectAnalytics,
                                    weakTopics = weakTopics,
                                    onOpenQuickPractice = {
                                        todayOverlay = TodayOverlayDestination.None
                                        val qs = mergedQuestions.filter { it.classId == profile.classId }.ifEmpty { mergedQuestions }.take(6)
                                        pendingLearnDest = LearnDestination.PracticeSession(
                                            StudyQuestStrings.t(lang, "Quick Practice", "কুইক প্র্যাকটিস"),
                                            qs
                                        )
                                        navigateToTab(1)
                                    },
                                    onBack = { todayOverlay = TodayOverlayDestination.None }
                                )

                                TodayOverlayDestination.Leaderboard -> LeaderboardScreen(
                                    myProfile = profile,
                                    language = lang,
                                    reducedMotion = prefs.reducedMotion,
                                    leaderboard = leaderboard,
                                    selectedScope = leaderboardScope,
                                    selectedPeriod = leaderboardPeriod,
                                    selectedCategory = leaderboardCategory,
                                    isGuestMode = true,
                                    onSelectScope = { leaderboardScope = it },
                                    onSelectPeriod = { leaderboardPeriod = it },
                                    onSelectCategory = { leaderboardCategory = it },
                                    onRequestGoogleSignIn = onRequestGoogleSignIn,
                                    onBack = { todayOverlay = TodayOverlayDestination.None }
                                )

                                TodayOverlayDestination.AiDoubtSolver -> AiDoubtSolverScreen(
                                    profile = profile,
                                    language = lang,
                                    reducedMotion = prefs.reducedMotion,
                                    onAskGeminiDoubt = { prompt, studentQ, subjName ->
                                        learnVm.streamDirectGeminiDoubt(
                                            questionPrompt = prompt,
                                            studentQuestion = studentQ,
                                            classId = profile.classId,
                                            subjectName = subjName,
                                            language = lang,
                                            activeBookTitle = StudyQuestStrings.t(lang, activeLockedBook.titleEn, activeLockedBook.titleBn)
                                        )
                                    },
                                    onBack = { todayOverlay = TodayOverlayDestination.None },
                                    isOnline = isOnline
                                )

                                TodayOverlayDestination.None -> TodayTabScreen(
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
                                        navigateToTab(1)
                                    },
                                    onOpenMockExam = { navigateToTab(1) },
                                    onOpenRevision = {
                                        pendingLearnDest = LearnDestination.RevisionAndPlanner
                                        navigateToTab(1)
                                    },
                                    onOpenAiDoubtSolver = { todayOverlay = TodayOverlayDestination.AiDoubtSolver },
                                    onOpenMessages = { navigateToTab(3) },
                                    subjectAnalytics = subjectAnalytics,
                                    dailyStudySeries = dailyStudySeries,
                                    dailyChallengeDoneToday = learnVm.isDailyChallengeDoneToday(profile),
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
                                    onRetakePromotionExam = { openPromotionExam() },
                                    onOpenAiLab = {
                                        pendingLearnDest = LearnDestination.AiLab
                                        navigateToTab(1)
                                    },
                                    onOpenStudyProgress = { todayOverlay = TodayOverlayDestination.StudyProgress },
                                    onOpenStudyGroups = {
                                        pendingFriendsSubTab = 0
                                        navigateToTab(3)
                                    },
                                    onOpenLeaderboard = { todayOverlay = TodayOverlayDestination.Leaderboard },
                                    isGuestMode = true,
                                    onRequestGoogleSignIn = onRequestGoogleSignIn,
                                    activeLockedBook = activeLockedBook,
                                    availableBooksForLock = availableBooksForLock,
                                    cachedBooks = cachedBooks,
                                    onOpenTextbookReader = {
                                        pendingLearnDest = LearnDestination.TextbookReader
                                        navigateToTab(1)
                                    },
                                    onSwitchActiveBook = { book -> guestVm.switchGuestActiveBook(book) },
                                    userBadges = userBadges,
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
                                onReportQuestion = { },
                                onAskAi = { prompt, studentQ, cls, subj ->
                                    learnVm.askAiDoubtSolver(
                                        prompt, studentQ, cls, subj, lang,
                                        activeBookTitle = StudyQuestStrings.t(lang, activeLockedBook.titleEn, activeLockedBook.titleBn)
                                    )
                                },
                                onResetAi = { learnVm.resetAiDoubtState() },
                                reducedMotion = prefs.reducedMotion,
                                profile = profile,
                                subjectAnalytics = subjectAnalytics,
                                weakTopics = weakTopics,
                                dailyChallengeQuestions = remember(mergedQuestions, activeLockedBook) {
                                    learnVm.dailyChallengeQuestions(
                                        TextbookCatalog.questionsForActiveBook(mergedQuestions, activeLockedBook),
                                        profile.classId
                                    )
                                },
                                dailyChallengeDoneToday = learnVm.isDailyChallengeDoneToday(profile),
                                dailyChallengeDateKey = learnVm.todayChallengeDateKey(),
                                onCompletePomodoro = { subjId, mins ->
                                    learnVm.completePomodoroSession(subjId, mins, isOnline, bookId = activeLockedBook.id) {
                                        AudioFeedbackHelper.triggerCelebration(view, prefs.soundEnabled, prefs.reducedMotion)
                                    }
                                },
                                onCompleteFlashcardDeck = { subjId, rev, mast ->
                                    learnVm.recordFlashcardSession(subjId, rev, mast, isOnline, bookId = activeLockedBook.id) {
                                        AudioFeedbackHelper.triggerSuccess(view, prefs.soundEnabled, prefs.reducedMotion)
                                    }
                                },
                                onSubmitDailyChallenge = { qs, sel, txt ->
                                    learnVm.completeDailyChallenge(qs, sel, txt, isOnline, bookId = activeLockedBook.id) {
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
                                onSetExamOrQuizActive = { learnVm.setExamOrQuizActive(it) },
                                onSendAiLabMessage = { msg ->
                                    learnVm.sendStudyQuestMessage(
                                        msg, profile.classId, activeLockedBook.subjectId, lang,
                                        activeBookTitle = StudyQuestStrings.t(lang, activeLockedBook.titleEn, activeLockedBook.titleBn)
                                    )
                                },
                                onCancelAiRequest = { learnVm.cancelAIRequest() },
                                onRetryAiRequest = { learnVm.retryFailedAIRequest() },
                                onOpenPlanStore = onRequestGoogleSignIn,
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
                                onSwitchActiveBook = { book -> guestVm.switchGuestActiveBook(book) },
                                onReportBookIssue = { tType, tId, details ->
                                    learnVm.reportQuestionOrBook(tType, tId, "Textbook content review", details)
                                },
                                downloadPacks = downloadPacks,
                                unresolvedMistakes = unresolvedMistakes,
                                examCountdowns = examCountdowns,
                                formulaBookmarks = formulaBookmarks,
                                learningRecords = learningRecords,
                                soloMatches = soloMatches,
                                onSeedDownloads = {
                                    learnVm.seedDownloadCatalog(
                                        profile.classId,
                                        profile.curriculumVersion,
                                        profile.academicGroup
                                    )
                                },
                                onDownloadPack = { pack -> learnVm.downloadPack(pack, isOnline, lang) },
                                onDownloadClassBundle = {
                                    learnVm.downloadClassBundle(
                                        profile.classId,
                                        profile.curriculumVersion,
                                        profile.academicGroup,
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
                                onRequestGoogleSignIn = onRequestGoogleSignIn,
                                isOnline = isOnline
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
                                onRequestGoogleSignIn = onRequestGoogleSignIn,
                                externalSubTab = pendingFriendsSubTab,
                                onConsumeExternalSubTab = { pendingFriendsSubTab = null },
                                isOnline = isOnline
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
                                onUpdateSurfaceStyle = { guestVm.updateSurfaceStyle(it) },
                                onUpdateNotificationCategory = { c, e -> guestVm.updateNotificationCategory(c, e) },
                                onRetakePromotionExam = { openPromotionExam() },
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
        AiDoubtSolverSheet(
            initialContext = "",
            classId = profile.classId,
            subjectName = "General Study",
            language = lang,
            aiState = aiState,
            onOpenAiLab = {
                showGlobalAiSheet = false
                pendingLearnDest = LearnDestination.AiLab
                navigateToTab(1)
            },
            onSubmitDoubt = { q ->
                learnVm.askAiDoubtSolver("", q, profile.classId, "General Study", lang)
            },
            onDismiss = {
                learnVm.resetAiDoubtState()
                showGlobalAiSheet = false
            },
            isOnline = isOnline
        )
    }
}
