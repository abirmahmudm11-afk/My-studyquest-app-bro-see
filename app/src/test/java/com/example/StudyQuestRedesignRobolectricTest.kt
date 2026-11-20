package com.example

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.core.AppLanguage
import com.example.core.LocalUserPreferences
import com.example.core.StudyQuestPreferences
import com.example.core.SurfaceStyle
import com.example.core.ThemeMode
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.StudyQuestDatabase
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.LearningRecordDoc
import com.example.data.model.PublicProfile
import com.example.data.repository.AcademicProgressRepository
import com.example.data.repository.DailyStudyMetric
import com.example.data.repository.LeaderboardCategory
import com.example.data.repository.LeaderboardPeriod
import com.example.ui.screens.AiDoubtSolverScreen
import com.example.ui.screens.LanguagePickerLaunchScreen
import com.example.ui.screens.LeaderboardScopeFilter
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.ProfileTabScreen
import com.example.ui.screens.StudyProgressDashboardScreen
import com.example.ui.screens.TodayTabScreen
import com.example.ui.screens.WelcomeAndGoogleSignInScreen
import com.example.ui.theme.StudyQuestTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyQuestRedesignRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun ensureFirebaseAppInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.studyquest.bdxkpq")
                    .setProjectId("demo-no-project")
                    .setApiKey("fake-api-key-for-robolectric")
                    .build()
            )
        }
    }

    @Test
    fun preferences_persistsSurfaceStyleAndThemeMode(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = StudyQuestPreferences(context)

        prefs.setSurfaceStyle(SurfaceStyle.GLASSMORPHISM)
        prefs.setThemeMode(ThemeMode.DARK)

        val current = withTimeout(3000L) {
            prefs.preferencesFlow.first {
                it.surfaceStyle == SurfaceStyle.GLASSMORPHISM && it.themeMode == ThemeMode.DARK
            }
        }
        assertEquals(SurfaceStyle.GLASSMORPHISM, current.surfaceStyle)
        assertEquals(ThemeMode.DARK, current.themeMode)
        assertTrue(current.darkMode)

        prefs.setSurfaceStyle(SurfaceStyle.NORMAL)
        prefs.setThemeMode(ThemeMode.LIGHT)

        val reset = withTimeout(3000L) {
            prefs.preferencesFlow.first {
                it.surfaceStyle == SurfaceStyle.NORMAL && it.themeMode == ThemeMode.LIGHT
            }
        }
        assertEquals(SurfaceStyle.NORMAL, reset.surfaceStyle)
        assertEquals(ThemeMode.LIGHT, reset.themeMode)
    }

    @Test
    fun computeDailyStudySeries_countsLessonsExamsAndOptimisticRecords() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)
        val db = StudyQuestDatabase.getInstance(context)
        val repo = AcademicProgressRepository(
            db = FirebaseFirestore.getInstance(),
            dao = db.studyQuestDao()
        )

        val records = listOf(
            LearningRecordDoc(
                id = "l1",
                subjectId = "math",
                recordType = "lesson",
                xpAwarded = 15L,
                accuracy = 1.0,
                createdAt = null
            ),
            LearningRecordDoc(
                id = "l2",
                subjectId = "physics",
                recordType = "lesson",
                xpAwarded = 15L,
                accuracy = 1.0,
                createdAt = null
            ),
            LearningRecordDoc(
                id = "e1",
                subjectId = "math",
                recordType = "exam",
                xpAwarded = 60L,
                accuracy = 0.85,
                createdAt = null
            )
        )

        val series = repo.computeDailyStudySeries(records = records, daysCount = 7)
        assertEquals(7, series.size)
        val today = series.last()
        assertEquals(2, today.lessonsCompleted)
        assertEquals(1, today.examsCompleted)
        assertEquals(90L, today.xpEarned)
        assertEquals(0.85, today.examAccuracy, 0.001)
    }

    @Test
    fun todayTabScreen_displaysAndTriggersAllFourHeroCards() {
        var progressOpened = false
        var groupsOpened = false
        var leaderboardOpened = false
        var aiSolverOpened = false

        composeRule.setContent {
            StudyQuestTheme(
                darkTheme = false,
                isBangla = false,
                surfaceStyle = SurfaceStyle.GLASSMORPHISM
            ) {
                TodayTabScreen(
                    profile = PublicProfile(
                        userId = "u1",
                        displayName = "Amina",
                        classId = "class_10",
                        totalXp = 320L,
                        streakCount = 5L
                    ),
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isOnline = true,
                    pendingSyncCount = 0,
                    quests = emptyList(),
                    notifications = emptyList(),
                    unreadNotificationCount = 0,
                    weakTopics = emptyList(),
                    revisions = emptyList(),
                    studyPlans = emptyList(),
                    recentRecords = emptyList(),
                    onClaimQuest = {},
                    onMarkNotificationRead = {},
                    onOpenQuickPractice = {},
                    onOpenMockExam = {},
                    onOpenRevision = {},
                    onOpenAiDoubtSolver = { aiSolverOpened = true },
                    onOpenMessages = {},
                    onOpenStudyProgress = { progressOpened = true },
                    onOpenStudyGroups = { groupsOpened = true },
                    onOpenLeaderboard = { leaderboardOpened = true }
                )
            }
        }

        composeRule.onNodeWithTag("today_home_list").performScrollToNode(hasTestTag("today_hero_study_progress"))
        composeRule.onNodeWithTag("today_hero_study_progress").assertIsDisplayed().performClick()

        composeRule.onNodeWithTag("today_home_list").performScrollToNode(hasTestTag("today_hero_study_groups"))
        composeRule.onNodeWithTag("today_hero_study_groups").assertIsDisplayed().performClick()

        composeRule.onNodeWithTag("today_home_list").performScrollToNode(hasTestTag("today_hero_leaderboard"))
        composeRule.onNodeWithTag("today_hero_leaderboard").assertIsDisplayed().performClick()

        composeRule.onNodeWithTag("today_home_list").performScrollToNode(hasTestTag("today_hero_ai_solver"))
        composeRule.onNodeWithTag("today_hero_ai_solver").assertIsDisplayed().performClick()

        assertTrue(progressOpened)
        assertTrue(groupsOpened)
        assertTrue(leaderboardOpened)
        assertTrue(aiSolverOpened)
    }

    @Test
    fun profileSettings_switchesThemeModeAndSurfaceStyle() {
        var selectedTheme: ThemeMode? = null
        var selectedSurface: SurfaceStyle? = null

        composeRule.setContent {
            StudyQuestTheme(
                darkTheme = false,
                isBangla = false,
                surfaceStyle = SurfaceStyle.NORMAL
            ) {
                ProfileTabScreen(
                    profile = PublicProfile(userId = "u1", displayName = "Tariq"),
                    preferences = LocalUserPreferences(
                        uiLanguage = AppLanguage.ENGLISH,
                        questionLanguage = AppLanguage.ENGLISH,
                        themeMode = ThemeMode.LIGHT,
                        surfaceStyle = SurfaceStyle.NORMAL,
                        reducedMotion = true
                    ),
                    adminRole = null,
                    unlockedCosmetics = emptySet(),
                    learningRecords = emptyList(),
                    subjectAnalytics = emptyList(),
                    weakTopics = emptyList(),
                    friendCount = 2,
                    exportedJson = null,
                    onClearExportedJson = {},
                    onEquipCosmetic = { _, _ -> },
                    onPurchaseCosmetic = { _, _ -> },
                    onUpdateUiLanguage = {},
                    onUpdateQuestionLanguage = {},
                    onUpdateDarkMode = {},
                    onUpdateReducedMotion = {},
                    onUpdateSound = {},
                    onUpdateQuietHours = {},
                    onUpdateNotifications = {},
                    onUpdateNotificationCategory = { _, _ -> },
                    onUpdateOptInNational = {},
                    onUpdateOnlineStatusVisible = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {},
                    adminViewModelProvider = { error("AdminStudio not opened in settings test") },
                    onUpdateThemeMode = { selectedTheme = it },
                    onUpdateSurfaceStyle = { selectedSurface = it }
                )
            }
        }

        composeRule.onNodeWithTag("profile_home_list").performScrollToNode(hasTestTag("profile_open_settings"))
        composeRule.onNodeWithTag("profile_open_settings").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings_safety_list").performScrollToNode(hasTestTag("settings_theme_dark"))
        composeRule.onNodeWithTag("settings_theme_dark").assertIsDisplayed().performClick()

        composeRule.onNodeWithTag("settings_safety_list").performScrollToNode(hasTestTag("settings_surface_glassmorphism"))
        composeRule.onNodeWithTag("settings_surface_glassmorphism").assertIsDisplayed().performClick()

        assertEquals(ThemeMode.DARK, selectedTheme)
        assertEquals(SurfaceStyle.GLASSMORPHISM, selectedSurface)
    }

    @Test
    fun studyProgressDashboard_rendersAndSupportsBackNavigation() {
        var progressBack = false
        composeRule.setContent {
            StudyQuestTheme(
                darkTheme = false,
                isBangla = false,
                surfaceStyle = SurfaceStyle.GLASSMORPHISM
            ) {
                StudyProgressDashboardScreen(
                    profile = PublicProfile(userId = "u1", displayName = "Rafi", totalXp = 540L, streakCount = 7L),
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    learningRecords = emptyList(),
                    dailyStudySeries = listOf(
                        DailyStudyMetric("2026-11-15", "Sat", 90L, 45, 0.82, 3, lessonsCompleted = 2, examsCompleted = 1),
                        DailyStudyMetric("2026-11-16", "Sun", 120L, 60, 0.90, 4, lessonsCompleted = 3, examsCompleted = 1)
                    ),
                    subjectAnalytics = emptyList(),
                    weakTopics = emptyList(),
                    onOpenQuickPractice = {},
                    onBack = { progressBack = true }
                )
            }
        }

        composeRule.onNodeWithTag("study_progress_back_btn").performClick()
        assertTrue(progressBack)
    }

    @Test
    fun leaderboardScreen_rendersEntriesAndSupportsBackNavigation() {
        var leaderboardBack = false
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                LeaderboardScreen(
                    myProfile = PublicProfile(userId = "u1", displayName = "Rafi", classId = "class_10", totalXp = 540L),
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    leaderboard = listOf(
                        LeaderboardEntryDoc(userId = "u2", displayName = "Nadia", classId = "class_10", weeklyXp = 780L),
                        LeaderboardEntryDoc(userId = "u1", displayName = "Rafi", classId = "class_10", weeklyXp = 540L)
                    ),
                    selectedScope = LeaderboardScopeFilter.CLASS_ONLY,
                    selectedPeriod = LeaderboardPeriod.WEEKLY,
                    selectedCategory = LeaderboardCategory.XP,
                    onSelectScope = {},
                    onSelectPeriod = {},
                    onSelectCategory = {},
                    onBack = { leaderboardBack = true }
                )
            }
        }

        composeRule.onNodeWithTag("leaderboard_screen").performScrollToNode(hasTestTag("lb_row_u2"))
        composeRule.onNodeWithTag("lb_row_u2").assertIsDisplayed()
        composeRule.onNode(
            hasTestTag("lb_row_u2") and hasAnyDescendant(hasText("Nadia"))
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("leaderboard_screen").performScrollToNode(hasTestTag("leaderboard_back_btn"))
        composeRule.onNodeWithTag("leaderboard_back_btn").performClick()
        assertTrue(leaderboardBack)
    }

    @Test
    fun aiDoubtSolverScreen_submitsPromptAndSupportsBackNavigation() {
        var aiBack = false
        var askedQuestion = ""
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                AiDoubtSolverScreen(
                    profile = PublicProfile(userId = "u1", displayName = "Rafi", classId = "class_10", selectedSubjects = listOf("math", "physics")),
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    initialQuestionPrompt = "Explain Newton's second law",
                    onAskGeminiDoubt = { _, studentQuestion, _ ->
                        askedQuestion = studentQuestion
                        flowOf("F = m * a explains how acceleration scales with force.")
                    },
                    onBack = { aiBack = true }
                )
            }
        }

        composeRule.onNodeWithTag("ai_doubt_solver_screen").performScrollToNode(hasTestTag("ai_doubt_ask_btn"))
        composeRule.onNodeWithTag("ai_doubt_ask_btn").performClick()
        composeRule.waitUntil(2000L) { askedQuestion.isNotBlank() }
        assertEquals("Explain Newton's second law", askedQuestion)
        composeRule.onNodeWithTag("ai_doubt_solver_screen").performScrollToNode(hasTestTag("ai_doubt_back_btn"))
        composeRule.onNodeWithTag("ai_doubt_back_btn").performClick()
        assertTrue(aiBack)
    }

    @Test
    fun todayTabScreen_showsLockedNctbBookCardAndOpensReader() {
        val mathBook = TextbookCatalog.bookById("nctb_2026_class_10_bangla_math")!!
        var readerOpened = false

        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                TodayTabScreen(
                    profile = PublicProfile(
                        userId = "u1",
                        displayName = "Amina",
                        classId = "class_10",
                        activeBookId = mathBook.id,
                        activeBookYear = "2026"
                    ),
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isOnline = true,
                    pendingSyncCount = 0,
                    quests = emptyList(),
                    notifications = emptyList(),
                    unreadNotificationCount = 0,
                    weakTopics = emptyList(),
                    revisions = emptyList(),
                    studyPlans = emptyList(),
                    recentRecords = emptyList(),
                    onClaimQuest = {},
                    onMarkNotificationRead = {},
                    onOpenQuickPractice = {},
                    onOpenMockExam = {},
                    onOpenRevision = {},
                    onOpenAiDoubtSolver = {},
                    onOpenMessages = {},
                    activeLockedBook = mathBook,
                    availableBooksForLock = listOf(mathBook),
                    onOpenTextbookReader = { readerOpened = true }
                )
            }
        }

        composeRule.onNodeWithTag("today_home_list").performScrollToNode(hasTestTag("today_active_book_card"))
        composeRule.onNodeWithTag("today_active_book_card").assertIsDisplayed()
        composeRule.onNodeWithTag("today_open_reader_btn").performClick()
        assertTrue(readerOpened)
    }

    @Test
    fun welcomeSignInScreen_supportsOfflineGuestModeTrigger() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)
        var guestEntered = false

        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                WelcomeAndGoogleSignInScreen(
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isSigningIn = false,
                    authError = null,
                    onSetLanguage = {},
                    onSigningInChanged = {},
                    onIdTokenReceived = {},
                    onAuthError = {},
                    onEnterGuestMode = { guestEntered = true }
                )
            }
        }

        composeRule.onNodeWithTag("welcome_guest_mode_btn").performScrollTo().assertIsDisplayed().performClick()
        assertTrue(guestEntered)
    }

    @Test
    fun languagePickerLaunchScreen_rendersLiveShowcaseAndSelectsLanguages() {
        var selectedLanguage: AppLanguage? = null
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                LanguagePickerLaunchScreen(
                    onSelectLanguage = { selectedLanguage = it },
                    reducedMotion = true
                )
            }
        }

        composeRule.onNodeWithTag("launch_live_pillar_pager").assertIsDisplayed()
        composeRule.onNodeWithTag("launch_pillar_card_nctb").assertIsDisplayed()
        composeRule.onNodeWithTag("launch_pillar_dot_ai").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pick_lang_bn").performClick()
        composeRule.onNodeWithTag("launch_get_started_btn").performScrollTo().performClick()
        assertEquals(AppLanguage.BANGLA, selectedLanguage)
        composeRule.onNodeWithTag("pick_lang_en").performClick()
        composeRule.onNodeWithTag("launch_get_started_btn").performScrollTo().performClick()
        assertEquals(AppLanguage.ENGLISH, selectedLanguage)
    }

    @Test
    fun welcomeAuthScreen_interactive3dCarouselAndGlassAuthCardWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)

        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                WelcomeAndGoogleSignInScreen(
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isSigningIn = false,
                    authError = null,
                    onSetLanguage = {},
                    onSigningInChanged = {},
                    onIdTokenReceived = {},
                    onAuthError = {}
                )
            }
        }

        composeRule.onNodeWithTag("auth_preview_carousel").assertIsDisplayed()
        composeRule.onNodeWithTag("auth_carousel_next_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_carousel_prev_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("welcome_sign_in_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_email_input").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("auth_back_to_welcome_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("welcome_create_account_btn").performScrollTo().assertIsDisplayed()
    }
}
