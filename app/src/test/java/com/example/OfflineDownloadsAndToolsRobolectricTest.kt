package com.example

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.AppLanguage
import com.example.data.local.DownloadPackEntity
import com.example.data.local.ExamCountdownEntity
import com.example.data.local.FormulaBookmarkEntity
import com.example.data.local.MistakeNoteEntity
import com.example.data.local.StudyQuestDatabase
import com.example.data.model.PublicProfile
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.repository.DownloadPackCategory
import com.example.data.repository.OfflineDownloadsRepository
import com.example.data.repository.TextbookDownloadRepository
import com.example.ui.screens.CompeteTabScreen
import com.example.ui.screens.ExamCountdownAndHeatmapScreen
import com.example.ui.screens.FormulaAtlasScreen
import com.example.ui.screens.FriendsTabScreen
import com.example.ui.screens.MistakeNotebookScreen
import com.example.ui.screens.OfflineDownloadsScreen
import com.example.ui.screens.PocketAudioRevisionScreen
import com.example.ui.screens.TodayTabScreen
import com.example.ui.theme.StudyQuestTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineDownloadsAndToolsRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var context: Context
    private lateinit var inMemoryDb: StudyQuestDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
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
        inMemoryDb = Room.inMemoryDatabaseBuilder(context, StudyQuestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
    }

    @Test
    fun offlineDownloadsRepository_seedsAllCategoriesAndInstallsAndRemovesPack(): Unit = runBlocking {
        val dao = inMemoryDb.studyQuestDao()
        val textbookRepo = TextbookDownloadRepository(context, FirebaseFirestore.getInstance(), dao)
        val downloadsRepo = OfflineDownloadsRepository(context, dao, textbookRepo)

        downloadsRepo.seedCatalogIfNeeded(classId = "class_10")
        val seeded = downloadsRepo.observeDownloadPacks().first()
        assertTrue(seeded.any { it.category == DownloadPackCategory.TEXTBOOK })
        assertTrue(seeded.any { it.category == DownloadPackCategory.AUDIO_BRIEFING })
        assertTrue(seeded.any { it.category == DownloadPackCategory.FOCUS_SOUNDSCAPE })
        assertTrue(seeded.any { it.category == DownloadPackCategory.STEM_LAB_PACK })

        val audioPack = seeded.first { it.category == DownloadPackCategory.AUDIO_BRIEFING }
        val installResult = downloadsRepo.downloadOrInstallPack(audioPack, isOnline = false)
        assertTrue("Expected offline audio pack install to succeed", installResult.isSuccess)
        val installed = installResult.getOrNull()
        assertNotNull(installed)
        assertEquals("installed", installed?.status)
        assertEquals(100, installed?.progressPct)
        assertTrue(File(installed?.localPath.orEmpty()).exists())

        downloadsRepo.removeDownloadedPack(audioPack.packId)
        val afterRemove = dao.getDownloadPack(audioPack.packId)
        assertNotNull(afterRemove)
        assertEquals("not_downloaded", afterRemove?.status)
        assertEquals(0, afterRemove?.progressPct)
    }

    @Test
    fun competeTabScreen_whenOffline_locksOnlineActionsAndEnablesSoloSprint() {
        var soloLaunched = false
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                CompeteTabScreen(
                    profile = PublicProfile(userId = "u1", displayName = "Nadia", classId = "class_10"),
                    uiLanguage = AppLanguage.ENGLISH,
                    questionLanguage = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    allQuestions = emptyList(),
                    myMatches = emptyList(),
                    activeMatch = null,
                    activeMatchRequest = null,
                    activeSubmissions = emptyList(),
                    leaderboard = emptyList(),
                    tournaments = emptyList(),
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
                    onStartSoloSprint = { _, _, _, _ -> soloLaunched = true },
                    isOnline = false
                )
            }
        }

        composeRule.onNodeWithTag("compete_offline_notice").assertIsDisplayed()
        composeRule.onNodeWithTag("compete_offline_solo_card").assertIsDisplayed()
        composeRule.onNodeWithTag("compete_offline_solo_play_btn").assertIsEnabled().performClick()
        assertTrue(soloLaunched)

        composeRule.onNodeWithTag("compete_arena_list").performScrollToNode(hasTestTag("quick_battle_btn"))
        composeRule.onNodeWithTag("quick_battle_btn").assertIsNotEnabled()
        composeRule.onNodeWithTag("create_private_room_btn").assertIsNotEnabled()
        composeRule.onNodeWithTag("compete_arena_list").performScrollToNode(hasTestTag("challenge_friend_btn"))
        composeRule.onNodeWithTag("challenge_friend_btn").assertIsNotEnabled()
        composeRule.onNodeWithTag("compete_arena_list").performScrollToNode(hasTestTag("join_room_code_btn"))
        composeRule.onNodeWithTag("join_room_code_btn").assertIsNotEnabled()
    }

    @Test
    fun friendsTabScreen_whenOffline_locksDiscoverAndGroupCreation() {
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                FriendsTabScreen(
                    myProfile = PublicProfile(userId = "u1", displayName = "Rafiq", classId = "class_10"),
                    uiLanguage = AppLanguage.ENGLISH,
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
                    isOnline = false
                )
            }
        }

        composeRule.onNodeWithTag("friends_offline_notice").assertIsDisplayed()

        composeRule.onNodeWithTag("friends_tab_discover").performClick()
        composeRule.onNodeWithTag("discover_student_input").assertIsNotEnabled()

        composeRule.onNodeWithTag("friends_tab_groups").performClick()
        composeRule.onNodeWithTag("create_study_group_btn").assertIsNotEnabled()
    }

    @Test
    fun todayTabScreen_clickingModePillOpensModeStatusSheet() {
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                TodayTabScreen(
                    profile = PublicProfile(userId = "u1", displayName = "Amina", classId = "class_10"),
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isOnline = false,
                    pendingSyncCount = 2,
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
                    onOpenMessages = {}
                )
            }
        }

        composeRule.onNodeWithTag("mode_status_pill").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("mode_status_sheet").assertIsDisplayed()
    }

    @Test
    fun offlineDownloadsScreen_filtersCategoriesAndTriggersInstall() {
        var downloadedPackId: String? = null
        val samplePacks = listOf(
            DownloadPackEntity(
                packId = "book_math_10",
                category = DownloadPackCategory.TEXTBOOK,
                classId = "class_10",
                subjectId = "math",
                titleEn = "Class 10 Mathematics",
                titleBn = "দশম শ্রেণি গণিত",
                subtitleEn = "NCTB Book",
                subtitleBn = "পাঠ্যবই",
                sizeMb = 18.0,
                assetFileName = "math_10",
                status = "not_downloaded"
            ),
            DownloadPackEntity(
                packId = "audio_brief_math",
                category = DownloadPackCategory.AUDIO_BRIEFING,
                classId = "all",
                subjectId = "math",
                titleEn = "Math Audio Brief",
                titleBn = "গণিত অডিও",
                subtitleEn = "Studio WAV",
                subtitleBn = "স্টুডিও",
                sizeMb = 7.4,
                assetFileName = "briefing_math.wav",
                status = "installed",
                progressPct = 100
            )
        )

        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                OfflineDownloadsScreen(
                    uiLanguage = AppLanguage.ENGLISH,
                    isOnline = false,
                    classId = "class_10",
                    packs = samplePacks,
                    onSeedCatalog = {},
                    onDownloadPack = { downloadedPackId = it.packId },
                    onDownloadClassBundle = {},
                    onRemovePack = {},
                    onOpenPack = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("downloads_cat_textbook").performClick()
        composeRule.onNodeWithTag("downloads_screen_list").performScrollToNode(hasTestTag("download_install_book_math_10"))
        composeRule.onNodeWithTag("download_install_book_math_10").performClick()
        assertEquals("book_math_10", downloadedPackId)
    }

    @Test
    fun formulaAtlasScreen_searchesAndBookmarksFormula() {
        var toggledId: String? = null
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                FormulaAtlasScreen(
                    uiLanguage = AppLanguage.ENGLISH,
                    bookmarks = listOf(FormulaBookmarkEntity("math_quad_roots", "math", "Quadratic Formula & Discriminant")),
                    onToggleBookmark = { toggledId = it.id },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("formula_subj_physics").performClick()
        composeRule.onNodeWithTag("formula_atlas_list").performScrollToNode(hasTestTag("formula_bookmark_phys_kinematics"))
        composeRule.onNodeWithTag("formula_bookmark_phys_kinematics").performClick()
        assertEquals("phys_kinematics", toggledId)
    }

    @Test
    fun mistakeNotebookScreen_startsRepairDrillAndResolvesMistake() {
        var startedDrillSize = 0
        var resolvedId: String? = null
        val q = QuestionItem(
            id = "q_trig_1",
            subjectId = "math",
            classId = "class_10",
            chapterId = "ch_trig",
            topicId = "topic_trig_ratios",
            type = QuestionType.SINGLE_MCQ,
            difficulty = QuestionDifficulty.EASY,
            promptEn = "What is sin(30°)?",
            promptBn = "sin(30°) এর মান কত?",
            optionsEn = listOf("0", "1/2", "1", "sqrt(3)/2"),
            optionsBn = listOf("0", "1/2", "1", "sqrt(3)/2"),
            correctOptionIndices = listOf(1),
            explanationEn = "sin(30°) = 1/2 from standard trigonometric values.",
            explanationBn = "আদর্শ ত্রিকোণমিতিক মান অনুযায়ী sin(30°) = 1/2।"
        )
        val mistake = MistakeNoteEntity(
            questionId = "q_trig_1",
            classId = "class_10",
            subjectId = "math",
            promptEn = q.promptEn,
            promptBn = q.promptBn,
            wrongCount = 2
        )

        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                MistakeNotebookScreen(
                    uiLanguage = AppLanguage.ENGLISH,
                    mistakes = listOf(mistake),
                    allQuestions = listOf(q),
                    onStartRepairDrill = { startedDrillSize = it.size },
                    onResolveMistake = { resolvedId = it },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("mistake_repair_btn").assertIsEnabled().performClick()
        assertEquals(1, startedDrillSize)

        composeRule.onNodeWithTag("mistake_notebook_list").performScrollToNode(hasTestTag("mistake_resolve_q_trig_1"))
        composeRule.onNodeWithTag("mistake_resolve_q_trig_1").performClick()
        assertEquals("q_trig_1", resolvedId)
    }

    @Test
    fun examCountdownAndHeatmapScreen_addsExamTargetAndShowsHeatmap() {
        var savedTitle: String? = null
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                ExamCountdownAndHeatmapScreen(
                    uiLanguage = AppLanguage.ENGLISH,
                    countdowns = listOf(
                        ExamCountdownEntity(
                            id = "e1",
                            title = "SSC Physics Final",
                            subjectId = "physics",
                            targetDateEpochMs = System.currentTimeMillis() + 86_400_000L * 20,
                            dailyTopicGoal = 2,
                            completedTopics = 5
                        )
                    ),
                    learningRecords = emptyList(),
                    soloMatches = emptyList(),
                    onAddExam = { title, _, _, _ -> savedTitle = title },
                    onIncrementTopic = {},
                    onDeleteExam = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("study_heatmap_card").assertIsDisplayed()
        composeRule.onNodeWithTag("exam_add_btn").performClick()
        composeRule.onNodeWithTag("exam_dialog_title_input").performTextInput("HSC Readiness Test")
        composeRule.onNodeWithTag("exam_dialog_save_btn").performClick()
        assertEquals("HSC Readiness Test", savedTitle)
    }

    @Test
    fun pocketAudioRevisionScreen_rendersStudioAudioControlsAndBack() {
        var backed = false
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                PocketAudioRevisionScreen(
                    uiLanguage = AppLanguage.ENGLISH,
                    onBack = { backed = true }
                )
            }
        }

        composeRule.onNodeWithTag("pocket_audio_rain_switch").assertIsDisplayed()
        composeRule.onNodeWithTag("pocket_audio_list").performScrollToNode(hasTestTag("briefing_play_math"))
        composeRule.onNodeWithTag("briefing_play_math").assertIsDisplayed()
        composeRule.onNodeWithTag("pocket_audio_back_btn").performClick()
        assertTrue(backed)
    }
}
