package com.example

import com.example.core.BillingAndAdProvider
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.GamificationCatalog
import com.example.data.model.LeaderboardEntryDoc
import com.example.data.model.PublicProfile
import com.example.data.model.SystemSettingsDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.repository.CompetitionRepository
import com.example.data.repository.GamificationEngine
import com.example.data.repository.LeaderboardCategory
import com.example.data.repository.LeaderboardPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationLogicTest {

    @Test
    fun `progressive XP curve computes exact level thresholds and intra-level progress`() {
        assertEquals(0L, StudyQuestStrings.xpThresholdForLevel(1L))
        assertEquals(100L, StudyQuestStrings.xpThresholdForLevel(2L))
        assertEquals(250L, StudyQuestStrings.xpThresholdForLevel(3L))
        assertEquals(450L, StudyQuestStrings.xpThresholdForLevel(4L))

        assertEquals(1L, StudyQuestStrings.levelForXp(0L))
        assertEquals(1L, StudyQuestStrings.levelForXp(99L))
        assertEquals(2L, StudyQuestStrings.levelForXp(100L))
        assertEquals(2L, StudyQuestStrings.levelForXp(249L))
        assertEquals(3L, StudyQuestStrings.levelForXp(250L))
        assertEquals(4L, StudyQuestStrings.levelForXp(450L))

        val (intoLv3, spanLv3) = StudyQuestStrings.xpIntoCurrentLevel(310L)
        assertEquals(60L, intoLv3)
        assertEquals(200L, spanLv3)
    }

    @Test
    fun `streak transition increments strictly once per day and consumes freeze when missed`() {
        // Same-day completion should NOT double-increment streak
        val sameDay = GamificationEngine.computeStreakTransition(
            currentStreak = 4L,
            lastDateKey = "2026-11-15",
            historyKeys = listOf("2026-11-12", "2026-11-13", "2026-11-14", "2026-11-15"),
            freezeCount = 2L,
            todayKey = "2026-11-15",
            yesterdayKey = "2026-11-14"
        )
        assertFalse(sameDay.incrementedToday)
        assertEquals(4L, sameDay.newStreakCount)
        assertEquals(2L, sameDay.remainingFreezes)

        // Consecutive day increments normally without spending freeze
        val consecutive = GamificationEngine.computeStreakTransition(
            currentStreak = 4L,
            lastDateKey = "2026-11-14",
            historyKeys = listOf("2026-11-13", "2026-11-14"),
            freezeCount = 2L,
            todayKey = "2026-11-15",
            yesterdayKey = "2026-11-14"
        )
        assertTrue(consecutive.incrementedToday)
        assertFalse(consecutive.usedFreeze)
        assertEquals(5L, consecutive.newStreakCount)
        assertEquals(2L, consecutive.remainingFreezes)

        // Missed day with available Streak Freeze preserves & increments streak, spending 1 freeze
        val recovered = GamificationEngine.computeStreakTransition(
            currentStreak = 6L,
            lastDateKey = "2026-11-12",
            historyKeys = listOf("2026-11-11", "2026-11-12"),
            freezeCount = 1L,
            todayKey = "2026-11-15",
            yesterdayKey = "2026-11-14"
        )
        assertTrue(recovered.usedFreeze)
        assertEquals(7L, recovered.newStreakCount)
        assertEquals(0L, recovered.remainingFreezes)

        // Missed day with 0 freezes resets streak to 1
        val reset = GamificationEngine.computeStreakTransition(
            currentStreak = 6L,
            lastDateKey = "2026-11-12",
            historyKeys = listOf("2026-11-12"),
            freezeCount = 0L,
            todayKey = "2026-11-15",
            yesterdayKey = "2026-11-14"
        )
        assertFalse(reset.usedFreeze)
        assertEquals(1L, reset.newStreakCount)
    }

    @Test
    fun `Jan 1 academic lifecycle promotes late joiners automatically unless final exam failed`() {
        // Student joins Class 7 on Dec 12, 2026 -> when 2027 begins (Jan 1), immediately promotes to Class 8
        val lateJoinerDec12 = PublicProfile(
            userId = "u1",
            classId = "class_7",
            classLockedAcademicYear = 2026L,
            annualExamStatus = "pending"
        )
        val promoDecision = GamificationEngine.computeClassPromotionDecision(
            profile = lateJoinerDec12,
            currentAcademicYear = 2027
        )
        assertTrue(promoDecision.didPromote)
        assertFalse(promoDecision.wasHeldBackForExamFailure)
        assertEquals("class_8", promoDecision.updatedClassId)
        assertEquals(2027, promoDecision.lockedAcademicYear)
        assertEquals("class_7", promoDecision.promotedFromClassId)
        assertEquals("pending", promoDecision.annualExamStatus)

        // Student failed Class 8 annual exam -> remains in Class 8 in 2027 until passing promotion exam
        val failedStudent = PublicProfile(
            userId = "u2",
            classId = "class_8",
            classLockedAcademicYear = 2026L,
            annualExamStatus = "failed"
        )
        val holdDecision = GamificationEngine.computeClassPromotionDecision(
            profile = failedStudent,
            currentAcademicYear = 2027
        )
        assertFalse(holdDecision.didPromote)
        assertTrue(holdDecision.wasHeldBackForExamFailure)
        assertEquals("class_8", holdDecision.updatedClassId)
        assertEquals("failed", holdDecision.annualExamStatus)
        assertEquals(2027, holdDecision.lockedAcademicYear)
    }

    @Test
    fun `leaderboard scoreFor segregates metrics cleanly across categories and periods`() {
        val entry = LeaderboardEntryDoc(
            userId = "u1",
            weeklyPoints = 999L,
            seasonalPoints = 888L,
            competitionRating = 1420L,
            weeklyStudyMinutes = 45L,
            weeklyXp = 180L,
            weeklyExamScore = 88L,
            weeklyAssignmentScore = 60L,
            weeklyChallengeScore = 140L,
            biweeklyStudyMinutes = 95L,
            biweeklyXp = 370L,
            biweeklyExamScore = 172L,
            biweeklyAssignmentScore = 120L,
            biweeklyChallengeScore = 290L,
            monthlyStudyMinutes = 240L,
            monthlyXp = 820L,
            monthlyExamScore = 410L,
            monthlyAssignmentScore = 300L,
            monthlyChallengeScore = 610L
        )

        assertEquals(45L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.STUDY_TIME, LeaderboardPeriod.WEEKLY))
        assertEquals(180L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.XP, LeaderboardPeriod.WEEKLY))
        assertEquals(88L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.EXAM, LeaderboardPeriod.WEEKLY))
        assertEquals(60L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.ASSIGNMENT, LeaderboardPeriod.WEEKLY))
        assertEquals(140L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.CHALLENGE, LeaderboardPeriod.WEEKLY))

        assertEquals(95L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.STUDY_TIME, LeaderboardPeriod.BIWEEKLY))
        assertEquals(370L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.XP, LeaderboardPeriod.BIWEEKLY))
        assertEquals(172L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.EXAM, LeaderboardPeriod.BIWEEKLY))
        assertEquals(120L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.ASSIGNMENT, LeaderboardPeriod.BIWEEKLY))
        assertEquals(290L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.CHALLENGE, LeaderboardPeriod.BIWEEKLY))

        assertEquals(240L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.STUDY_TIME, LeaderboardPeriod.MONTHLY))
        assertEquals(820L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.XP, LeaderboardPeriod.MONTHLY))
        assertEquals(410L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.EXAM, LeaderboardPeriod.MONTHLY))
        assertEquals(300L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.ASSIGNMENT, LeaderboardPeriod.MONTHLY))
        assertEquals(610L, CompetitionRepository.scoreFor(entry, LeaderboardCategory.CHALLENGE, LeaderboardPeriod.MONTHLY))
    }

    @Test
    fun `ads are strictly suppressed during focus or exams, for premium tier, and when unconfigured`() {
        val freeSub = UserSubscriptionDoc(userId = "u1", tier = "free", adEnabled = true)
        val premiumSub = UserSubscriptionDoc(userId = "u1", tier = "premium", adEnabled = false)
        val adEnabledSettings = SystemSettingsDoc(adMobConfigured = true)
        val unconfiguredSettings = SystemSettingsDoc(adMobConfigured = false)

        // Unconfigured -> never show
        assertFalse(
            BillingAndAdProvider.evaluateAdPlacement(freeSub, unconfiguredSettings, isActiveFocusSession = false, isActiveExamOrQuiz = false).shouldDisplayAdSlot
        )
        // Active focus -> strictly suppressed
        val duringFocus = BillingAndAdProvider.evaluateAdPlacement(freeSub, adEnabledSettings, isActiveFocusSession = true, isActiveExamOrQuiz = false)
        assertFalse(duringFocus.shouldDisplayAdSlot)
        assertTrue(duringFocus.isSuppressedForStudyOrExam)

        // Active exam -> strictly suppressed
        val duringExam = BillingAndAdProvider.evaluateAdPlacement(freeSub, adEnabledSettings, isActiveFocusSession = false, isActiveExamOrQuiz = true)
        assertFalse(duringExam.shouldDisplayAdSlot)
        assertTrue(duringExam.isSuppressedForStudyOrExam)

        // Premium -> strictly suppressed
        assertFalse(
            BillingAndAdProvider.evaluateAdPlacement(premiumSub, adEnabledSettings, isActiveFocusSession = false, isActiveExamOrQuiz = false).shouldDisplayAdSlot
        )

        // Free user, idle, AdMob configured -> ready
        assertTrue(
            BillingAndAdProvider.evaluateAdPlacement(freeSub, adEnabledSettings, isActiveFocusSession = false, isActiveExamOrQuiz = false).shouldDisplayAdSlot
        )
    }

    @Test
    fun `badge catalog covers all 7 categories with non-empty definitions`() {
        assertTrue(GamificationCatalog.badgesCatalog.size >= 30)
        val categories = GamificationCatalog.badgesCatalog.map { it.category }.toSet()
        assertEquals(7, categories.size)
        assertTrue(GamificationCatalog.focusMusicTracks.size >= 5)
        assertEquals(3, GamificationCatalog.focusMusicTracks.map { it.category }.toSet().size)
    }
}
