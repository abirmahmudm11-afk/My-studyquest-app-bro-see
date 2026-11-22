package com.example

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.core.AppLanguage
import com.example.core.OfflineVaultManager
import com.example.data.repository.AiVideoRepository
import com.example.ui.screens.InteractiveStemLabScreen
import com.example.ui.theme.StudyQuestTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineStudyVaultRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun offlineVaultStats_meetsBundledCapacityAndAssetCounts(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val stats = OfflineVaultManager.computeVaultStats(context)

        assertEquals(10, stats.bundledBooksCount)
        assertTrue(
            "Expected >= 16 bundled studio audio files, got ${stats.bundledAudioTracksCount}",
            stats.bundledAudioTracksCount >= 16
        )
        assertEquals(14, stats.bundledPlatesCount)
        assertEquals(6000, stats.workedEntriesCount)
        assertTrue(
            "Expected bundled vault size > 12 MB, got ${stats.bundledVaultSizeMb}",
            stats.bundledVaultSizeMb > 12.0
        )
    }

    @Test
    fun copyBundledSubjectPdf_writesValidMultiPagePdf(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val destFile = File.createTempFile("study_vault_math_", ".pdf", context.cacheDir)
        try {
            val copied = OfflineVaultManager.copyBundledSubjectPdf(context, "math", destFile)
            assertTrue("Expected bundled math.pdf to copy cleanly", copied)
            assertTrue(destFile.exists())
            assertTrue(
                "Expected bundled math.pdf > 4 KB, got ${destFile.length()} bytes",
                destFile.length() > 4_000L
            )
            val header = destFile.inputStream().use { input ->
                val buf = ByteArray(5)
                val read = input.read(buf)
                if (read > 0) String(buf, 0, read, Charsets.US_ASCII) else ""
            }
            assertEquals("%PDF-", header)
        } finally {
            destFile.delete()
        }
    }

    @Test
    fun resolveOfflineDoubtExplanation_returnsBilingualWorkedGuidance() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val enAnswer = OfflineVaultManager.resolveOfflineDoubtExplanation(
            context = context,
            questionPrompt = "Solve quadratic equation x^2 - 5x + 6 = 0",
            studentQuestion = "How do I factor and find roots using discriminant?",
            classId = "class_10",
            subjectName = "Mathematics",
            language = AppLanguage.ENGLISH
        )
        assertTrue(enAnswer.isNotBlank())
        assertTrue(enAnswer.contains("Offline Study Vault"))
        assertTrue(enAnswer.contains("Step-by-Step Guide"))

        val bnAnswer = OfflineVaultManager.resolveOfflineDoubtExplanation(
            context = context,
            questionPrompt = "নিউটনের দ্বিতীয় সূত্র ব্যাখ্যা কর",
            studentQuestion = "F = ma কীভাবে কাজ করে?",
            classId = "class_9",
            subjectName = "পদার্থবিজ্ঞান",
            language = AppLanguage.BANGLA
        )
        assertTrue(bnAnswer.isNotBlank())
        assertTrue(bnAnswer.contains("অফলাইন স্টাডি ভল্ট"))
        assertTrue(bnAnswer.contains("ধাপে ধাপে সমাধান"))
    }

    @Test
    fun defaultOfflineVideoDoc_parsesNonEmptyScenesAndQuiz() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val doc = OfflineVaultManager.defaultOfflineVideoDoc(
            context = context,
            subjectId = "physics",
            title = "Motion & Kinematics",
            language = AppLanguage.ENGLISH
        )

        val scenes = AiVideoRepository.parseScenes(doc.scenesJson)
        val quiz = AiVideoRepository.parseQuiz(doc.quizJson)

        assertTrue("Expected non-empty storyboard scenes", scenes.isNotEmpty())
        assertTrue("Expected non-empty storyboard check quiz", quiz.isNotEmpty())
        assertEquals("offline_vault_ready", doc.externalRenderStatus)
    }

    @Test
    fun interactiveStemLabScreen_switchesAllFourLabsAndSupportsBack() {
        var backed = false
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                InteractiveStemLabScreen(
                    uiLanguage = AppLanguage.ENGLISH,
                    initialLabId = "trig",
                    onBack = { backed = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("stem_lab_list").performScrollToNode(hasTestTag("lab_card_trig"))
        composeRule.onNodeWithTag("lab_card_trig").assertIsDisplayed()

        composeRule.onNodeWithTag("stem_lab_list").performScrollToNode(hasTestTag("lab_chip_physics"))
        composeRule.onNodeWithTag("lab_chip_physics").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("stem_lab_list").performScrollToNode(hasTestTag("lab_card_physics"))
        composeRule.onNodeWithTag("lab_card_physics").assertIsDisplayed()

        composeRule.onNodeWithTag("stem_lab_list").performScrollToNode(hasTestTag("lab_chip_chem"))
        composeRule.onNodeWithTag("lab_chip_chem").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("stem_lab_list").performScrollToNode(hasTestTag("lab_card_chem"))
        composeRule.onNodeWithTag("lab_card_chem").assertIsDisplayed()

        composeRule.onNodeWithTag("stem_lab_list").performScrollToNode(hasTestTag("lab_chip_bio"))
        composeRule.onNodeWithTag("lab_chip_bio").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("stem_lab_list").performScrollToNode(hasTestTag("lab_card_bio"))
        composeRule.onNodeWithTag("lab_card_bio").assertIsDisplayed()

        composeRule.onNodeWithTag("stem_lab_back_btn").performClick()
        assertTrue(backed)
    }
}
