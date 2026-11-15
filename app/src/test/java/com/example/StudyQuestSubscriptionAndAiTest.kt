package com.example

import com.example.core.BillingAndAdProvider
import com.example.data.model.AiProviderSettingsDoc
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.SystemSettingsDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.repository.EducationalRequestCategory
import com.example.data.repository.StudyQuestAiToolCatalog
import com.example.data.repository.StudyQuestModelRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyQuestSubscriptionAndAiTest {

    @Test
    fun planCatalog_hasExactSevenPlans_studyQuestAiAt150Bdt_andZeroStoragePlans() {
        val plans = QuestPlanCatalog.getPlanCatalog()
        assertEquals(7, plans.size)
        assertEquals(
            listOf("free", "plus", "video_pass", "studyquest_ai", "scholar", "creator", "complete_max"),
            plans.map { it.id }
        )

        val aiPlan = QuestPlanCatalog.planById("studyquest_ai")
        assertEquals(150, aiPlan.priceBdtMonthly)
        assertEquals(100, aiPlan.monthlyAiCredits)
        assertEquals(2, aiPlan.monthlyVideoCredits)
        assertTrue(aiPlan.adFree)

        assertEquals(20, QuestPlanCatalog.videoBoost.priceBdtOneTime)
        assertFalse(QuestPlanCatalog.videoBoost.recurring)
        assertEquals(60, QuestPlanCatalog.videoBoost.maxDurationSeconds)

        // Strictly zero storage plans or storage add-ons anywhere in highlights/titles
        val allText = plans.flatMap { it.highlightsEn + it.highlightsBn + listOf(it.titleEn, it.id) }
            .joinToString(" ")
            .lowercase()
        assertFalse("No storage wording allowed in plan catalog", allText.contains("storage"))
        assertFalse("No GB quota wording allowed in plan catalog", allText.contains("gb"))
    }

    @Test
    fun modelRouter_defaultsToLuna_routesHardReasoningToSol_andGatesAstraBehindAdmin() {
        val router = StudyQuestModelRouter()
        val defaultSettings = AiProviderSettingsDoc()

        // Normal everyday tutoring goes to gpt-6-luna
        val normalDecision = router.selectAIModel(
            toolId = "QuestTutor",
            prompt = "What is photosynthesis in simple words?",
            settings = defaultSettings
        )
        assertEquals("gpt-6-luna", normalDecision.selectedModelId)
        assertEquals(EducationalRequestCategory.EVERYDAY_TUTORING, normalDecision.category)

        // Reasoning tool or hard math goes to gpt-6.1-sol
        val mathDecision = router.selectAIModel(
            toolId = "QuestionExplainer",
            prompt = "Prove the trigonometric identity sin^2(x) + cos^2(x) = 1 step by step",
            settings = defaultSettings
        )
        assertEquals("gpt-6.1-sol", mathDecision.selectedModelId)
        assertEquals(EducationalRequestCategory.COMPLEX_REASONING, mathDecision.category)

        // Astra blocked for routine students even if prompt contains advanced words
        val blockedAstra = router.selectAIModel(
            toolId = "QuestTutor",
            prompt = "olympiad deep synthesis proof of Euler identity",
            settings = defaultSettings,
            explicitAdvancedRequest = false
        )
        assertFalse(blockedAstra.selectedModelId == "gpt-6-astra")

        // Astra permitted only when admin enabled AND explicit admin request
        val adminEnabledSettings = defaultSettings.copy(advancedModelEnabled = true)
        val allowedAstra = router.selectAIModel(
            toolId = "QuestTutor",
            prompt = "olympiad deep synthesis proof",
            settings = adminEnabledSettings,
            explicitAdvancedRequest = true
        )
        assertEquals("gpt-6-astra", allowedAstra.selectedModelId)

        // Model fallback picks configured default when requested model unavailable
        val fallback = router.handleModelFallback(
            requestedModelId = "gpt-6-astra",
            availableModels = setOf("gpt-6-luna", "gpt-6.1-sol"),
            settings = defaultSettings
        )
        assertEquals("gpt-6-luna", fallback)
    }

    @Test
    fun adPlacement_suppressesAdsDuringFocusExamVideoAndPaidPlans() {
        val sysAdsOn = SystemSettingsDoc(adMobConfigured = true)
        val freeSub = UserSubscriptionDoc(tier = "free", adEnabled = true)
        val aiSub = UserSubscriptionDoc(tier = "studyquest_ai", adEnabled = false)

        // Free browsing outside active focus/exam/video permits ad slot when configured
        val idleFree = BillingAndAdProvider.evaluateAdPlacement(
            subscription = freeSub,
            settings = sysAdsOn,
            isActiveFocusSession = false,
            isActiveExamOrQuiz = false,
            isActiveVideoSession = false
        )
        assertTrue(idleFree.shouldDisplayAdSlot)

        // Suppressed during active focus, quiz/exam, or video playback
        assertFalse(
            BillingAndAdProvider.evaluateAdPlacement(freeSub, sysAdsOn, true, false, false).shouldDisplayAdSlot
        )
        assertFalse(
            BillingAndAdProvider.evaluateAdPlacement(freeSub, sysAdsOn, false, true, false).shouldDisplayAdSlot
        )
        assertFalse(
            BillingAndAdProvider.evaluateAdPlacement(freeSub, sysAdsOn, false, false, true).shouldDisplayAdSlot
        )

        // Paid ad-free tier suppresses ads everywhere
        assertFalse(
            BillingAndAdProvider.evaluateAdPlacement(aiSub, sysAdsOn, false, false, false).shouldDisplayAdSlot
        )
    }

    @Test
    fun aiToolCatalog_containsTwelveDistinctTools() {
        assertEquals(12, StudyQuestAiToolCatalog.tools.size)
        assertEquals(12, StudyQuestAiToolCatalog.tools.map { it.id }.toSet().size)
    }
}
