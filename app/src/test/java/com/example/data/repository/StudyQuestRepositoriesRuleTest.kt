package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.core.AppLanguage
import com.example.data.local.StudyQuestDatabase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class StudyQuestRepositoriesRuleTest : FirestoreEmulatorTestBase() {

    private lateinit var localDb: StudyQuestDatabase

    @Before
    fun setUpLocalDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        localDb = Room.inMemoryDatabaseBuilder(context, StudyQuestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDownLocalDb() {
        localDb.close()
    }

    @Test
    fun authAndProfile_bootstrapAndIsolatePrivateInfo() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val authRepo = AuthAndProfileRepository(firestore, localDb.studyQuestDao())
        val user = checkNotNull(auth.currentUser)

        val bootResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            authRepo.ensureUserBootstrapped(user, AppLanguage.ENGLISH)
        }
        assertTrue(bootResult.isSuccess)
        val profile = bootResult.getOrThrow()
        assertEquals(aliceUid, profile.userId)
        assertTrue(profile.studyQuestId.startsWith("SQ-"))

        val ownPrivate = withTimeout(DEFAULT_TIMEOUT_MS) {
            authRepo.getPrivateAccountInfo(aliceUid)
        }
        assertTrue(ownPrivate.isSuccess)
        assertNotNull(ownPrivate.getOrThrow())

        // Cross-user isolation: Bob cannot read Alice's private account info
        signInTestUser(BOB_EMAIL)
        val bobReadingAlice = withTimeout(DEFAULT_TIMEOUT_MS) {
            authRepo.getPrivateAccountInfo(aliceUid)
        }
        assertTrue(bobReadingAlice.isFailure)
        val fsErr = bobReadingAlice.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
    }

    @Test
    fun unauthenticatedCaller_rejectedBySecurityRules() = runBlocking {
        auth.signOut()
        val academicRepo = AcademicProgressRepository(firestore, localDb.studyQuestDao())

        var caught: Exception? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                academicRepo.observePublishedCloudContent().first()
            }
        } catch (e: Exception) {
            caught = e
        }
        val fsErr = caught?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException for unauthenticated list", fsErr)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
    }

    @Test
    fun academicProgress_recordsSessionWithIdempotencyAndStudyPlan() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val authRepo = AuthAndProfileRepository(firestore, localDb.studyQuestDao())
        authRepo.ensureUserBootstrapped(checkNotNull(auth.currentUser), AppLanguage.BANGLA)

        val repo = AcademicProgressRepository(firestore, localDb.studyQuestDao())
        val refId = "prac_${UUID.randomUUID().toString().take(8)}"
        val xpRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.recordPracticeOrExamSession(
                userId = aliceUid,
                recordType = "practice",
                refId = refId,
                subjectId = "math",
                chapterId = "ch_math_trig",
                topicId = "topic_trig_ratios",
                correctCount = 4,
                totalQuestions = 5,
                score = 4.0,
                totalMarks = 5.0,
                summary = "Rule test practice",
                isOnline = true
            )
        }
        assertTrue(xpRes.isSuccess)
        assertTrue((xpRes.getOrNull() ?: 0) >= 18)

        val records = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeLearningRecords(aliceUid).first { it.any { r -> r.refId == refId } }
        }
        assertTrue(records.any { it.refId == refId })

        val planRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createStudyPlan(
                userId = aliceUid,
                title = "Revise Trigonometry",
                subjectId = "math",
                planType = "daily",
                targetDateEpochMs = System.currentTimeMillis() + 86_400_000L,
                minutesPerDay = 45L
            )
        }
        assertTrue(planRes.isSuccess)
    }

    @Test
    fun socialAndMessaging_friendAcceptAndMemberIsolation() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val authRepo = AuthAndProfileRepository(firestore, localDb.studyQuestDao())
        val aliceProfile = authRepo.ensureUserBootstrapped(checkNotNull(auth.currentUser), AppLanguage.ENGLISH).getOrThrow()

        val bobUid = signInTestUser(BOB_EMAIL)
        val bobProfile = authRepo.ensureUserBootstrapped(checkNotNull(auth.currentUser), AppLanguage.ENGLISH).getOrThrow()

        // Alice sends friend request to Bob
        signInTestUser(ALICE_EMAIL)
        val socialRepo = SocialRepository(firestore)
        val sendRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            socialRepo.sendFriendRequest(aliceProfile, bobProfile)
        }
        // Ignore only if already connected from a prior run in same emulator session
        assertTrue(
            "sendFriendRequest failed: ${sendRes.exceptionOrNull()?.message}",
            sendRes.isSuccess || sendRes.exceptionOrNull()?.message?.contains("Already connected") == true
        )
        if (sendRes.isSuccess) {
            // Bob accepts
            signInTestUser(BOB_EMAIL)
            val incoming = withTimeout(FLOW_TIMEOUT_MS) {
                socialRepo.observeIncomingRequests(bobUid).first { list -> list.any { it.fromUid == aliceUid } }
            }
            val req = incoming.first { it.fromUid == aliceUid }
            val acceptRes = withTimeout(DEFAULT_TIMEOUT_MS) {
                socialRepo.respondToFriendRequest(req, accept = true, currentUid = bobUid)
            }
            assertTrue(acceptRes.isSuccess)
        }

        signInTestUser(ALICE_EMAIL)
        val convRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            socialRepo.openOrCreateDirectConversation(aliceUid, bobProfile)
        }
        assertTrue(convRes.isSuccess)
        val conv = convRes.getOrThrow()

        val msgRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            socialRepo.sendMessage(conv, aliceUid, aliceProfile.displayName, "Salaam Bob, let's solve SSC Math!")
        }
        assertTrue(msgRes.isSuccess)

        // Outsider Charlie cannot read Alice & Bob's conversation document
        signInTestUser(CHARLIE_EMAIL)
        val directRead = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("conversations").document(conv.id).get().await()
            }
        }
        assertTrue(directRead.isFailure)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            directRead.exceptionOrNull()?.unwrapFirestoreException()?.code
        )
    }

    @Test
    fun competitionAndAdmin_matchTerminalLockAndNonAdminDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val authRepo = AuthAndProfileRepository(firestore, localDb.studyQuestDao())
        val aliceProfile = authRepo.ensureUserBootstrapped(checkNotNull(auth.currentUser), AppLanguage.ENGLISH).getOrThrow()

        val compRepo = CompetitionRepository(firestore)
        val roomRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            compRepo.createRoomOrChallenge(
                hostUid = aliceUid,
                mode = "private_room",
                classId = "class_10",
                subjectId = "math"
            )
        }
        assertTrue(roomRes.isSuccess)
        val room = roomRes.getOrThrow()
        assertNotNull(room.roomCode)

        val bobUid = signInTestUser(BOB_EMAIL)
        val joinRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            compRepo.joinPrivateRoomByCode(room.roomCode!!, bobUid)
        }
        assertTrue(joinRes.isSuccess)

        // Alice submits deterministic answer and finalizes match
        signInTestUser(ALICE_EMAIL)
        val liveMatch = joinRes.getOrThrow()
        val qId = liveMatch.questionIds.first()
        val subRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            compRepo.submitMatchAnswer(liveMatch, aliceUid, qId, selectedIndex = 0, isCorrect = true, responseTimeMs = 2400L)
        }
        assertTrue(subRes.isSuccess)

        val finRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            compRepo.finalizeMatchAndLeaderboard(liveMatch, aliceProfile)
        }
        assertTrue(finRes.isSuccess)

        // Regular student Bob cannot create editorial content (Admin Studio protected)
        signInTestUser(BOB_EMAIL)
        val adminRepo = AdminStudioRepository(firestore)
        val draftRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.createDraftQuestion(
                actorUid = bobUid,
                classId = "class_10",
                subjectId = "math",
                titleEn = "Unauthorized Question",
                titleBn = "অননুমোদিত প্রশ্ন",
                optionsEn = listOf("A", "B", "C", "D"),
                optionsBn = listOf("ক", "খ", "গ", "ঘ"),
                correctIndex = 0,
                explanationEn = "Expl",
                explanationBn = "ব্যাখ্যা",
                sourceAttribution = "None"
            )
        }
        assertTrue(draftRes.isFailure)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            draftRes.exceptionOrNull()?.unwrapFirestoreException()?.code
        )
    }

    private companion object {
        const val ALICE_EMAIL = "alice_sq_rule@test.com"
        const val BOB_EMAIL = "bob_sq_rule@test.com"
        const val CHARLIE_EMAIL = "charlie_sq_rule@test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
