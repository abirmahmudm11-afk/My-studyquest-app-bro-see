package com.example.data.curriculum

import com.example.data.model.AssignmentDefinitionDoc
import com.example.data.model.BadgeCatalogItem
import com.example.data.model.BadgeCategory
import com.example.data.model.BadgeRarity
import com.example.data.model.FocusMusicTrack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object GamificationCatalog {

    val dhakaTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun currentDayKey(nowMs: Long = System.currentTimeMillis()): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = dhakaTimeZone }
        return fmt.format(nowMs)
    }

    fun yesterdayDayKey(nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(dhakaTimeZone).apply {
            timeInMillis = nowMs
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = dhakaTimeZone }
        return fmt.format(cal.timeInMillis)
    }

    fun recentSevenDayKeys(nowMs: Long = System.currentTimeMillis()): List<String> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = dhakaTimeZone }
        val cal = Calendar.getInstance(dhakaTimeZone).apply { timeInMillis = nowMs }
        val keys = mutableListOf<String>()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        repeat(7) {
            keys.add(fmt.format(cal.timeInMillis))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return keys
    }

    fun currentAcademicYear(nowMs: Long = System.currentTimeMillis()): Int {
        val cal = Calendar.getInstance(dhakaTimeZone).apply { timeInMillis = nowMs }
        return cal.get(Calendar.YEAR)
    }

    fun currentWeekKey(nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(dhakaTimeZone).apply {
            timeInMillis = nowMs
            firstDayOfWeek = Calendar.MONDAY
        }
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%04d-W%02d", year, week)
    }

    fun previousWeekKey(nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(dhakaTimeZone).apply {
            timeInMillis = nowMs
            firstDayOfWeek = Calendar.MONDAY
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        return String.format(Locale.US, "%04d-W%02d", cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR))
    }

    fun currentBiweekKey(nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(dhakaTimeZone).apply {
            timeInMillis = nowMs
            firstDayOfWeek = Calendar.MONDAY
        }
        val year = cal.get(Calendar.YEAR)
        val biweek = ((cal.get(Calendar.WEEK_OF_YEAR) - 1) / 2) + 1
        return String.format(Locale.US, "%04d-BW%02d", year, biweek)
    }

    fun previousBiweekKey(nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(dhakaTimeZone).apply {
            timeInMillis = nowMs
            firstDayOfWeek = Calendar.MONDAY
            add(Calendar.WEEK_OF_YEAR, -2)
        }
        val biweek = ((cal.get(Calendar.WEEK_OF_YEAR) - 1) / 2) + 1
        return String.format(Locale.US, "%04d-BW%02d", cal.get(Calendar.YEAR), biweek)
    }

    fun currentMonthKey(nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(dhakaTimeZone).apply { timeInMillis = nowMs }
        return String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun previousMonthKey(nowMs: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(dhakaTimeZone).apply {
            timeInMillis = nowMs
            add(Calendar.MONTH, -1)
        }
        return String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun millisUntilPeriodEnd(periodType: String, nowMs: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(dhakaTimeZone).apply { timeInMillis = nowMs }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        when (periodType) {
            "weekly" -> {
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                val daysToNextMonday = if (dow == Calendar.MONDAY) 7 else ((Calendar.MONDAY - dow + 7) % 7).let { if (it == 0) 7 else it }
                cal.add(Calendar.DAY_OF_YEAR, daysToNextMonday)
            }
            "biweekly" -> {
                val week = cal.get(Calendar.WEEK_OF_YEAR)
                val addWeeks = if (week % 2 == 1) 2 else 1
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                val daysToMonday = ((Calendar.MONDAY - dow + 7) % 7)
                cal.add(Calendar.DAY_OF_YEAR, daysToMonday)
                cal.add(Calendar.WEEK_OF_YEAR, addWeeks)
            }
            "monthly" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.add(Calendar.MONTH, 1)
            }
            "jan1" -> {
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.add(Calendar.YEAR, 1)
            }
            else -> cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return (cal.timeInMillis - nowMs).coerceAtLeast(0L)
    }

    val badgesCatalog: List<BadgeCatalogItem> = listOf(
        // Study Badges
        BadgeCatalogItem("badge_first_study", BadgeCategory.STUDY, BadgeRarity.COMMON, "First Focus Session", "প্রথম মনোযোগ সেশন", "Complete your first verified study focus session.", "তোমার প্রথম যাচাইকৃত ফোকাস সেশন সম্পন্ন করো।", "study_sessions", 1L, 25L),
        BadgeCatalogItem("badge_1h_learner", BadgeCategory.STUDY, BadgeRarity.COMMON, "1-Hour Scholar", "১ ঘণ্টার শিক্ষার্থী", "Accumulate 60 minutes of verified focus time.", "মোট ৬০ মিনিট ফোকাস স্টাডি সম্পূর্ণ করো।", "focus_minutes", 60L, 35L),
        BadgeCatalogItem("badge_10h_learner", BadgeCategory.STUDY, BadgeRarity.UNCOMMON, "10-Hour Scholar", "১০ ঘণ্টার শিক্ষার্থী", "Accumulate 600 minutes of deep focus.", "মোট ৬০০ মিনিট গভীর অধ্যয়ন সম্পন্ন করো।", "focus_minutes", 600L, 75L),
        BadgeCatalogItem("badge_50h_learner", BadgeCategory.STUDY, BadgeRarity.RARE, "50-Hour Scholar", "৫০ ঘণ্টার শিক্ষার্থী", "Accumulate 3,000 minutes of focused learning.", "মোট ৩,০০০ মিনিট ফোকাস পড়াশোনা সম্পন্ন করো।", "focus_minutes", 3000L, 150L),
        BadgeCatalogItem("badge_100h_learner", BadgeCategory.STUDY, BadgeRarity.EPIC, "100-Hour Master", "১০০ ঘণ্টার মাস্টার", "Accumulate 6,000 minutes of verified study.", "মোট ৬,০০০ মিনিট যাচাইকৃত পড়াশোনা সম্পন্ন করো।", "focus_minutes", 6000L, 250L),
        BadgeCatalogItem("badge_study_master", BadgeCategory.STUDY, BadgeRarity.LEGENDARY, "NCTB Lesson Master", "পাঠ মাস্টার", "Complete 25 NCTB curriculum lessons.", "২৫টি এনসিটিবি পাঠ সম্পূর্ণ করো।", "lessons_completed", 25L, 300L),

        // Streak Badges
        BadgeCatalogItem("badge_streak_3", BadgeCategory.STREAK, BadgeRarity.COMMON, "3-Day Flame", "৩ দিনের শিখা", "Keep a 3-day study streak via daily quizzes or lessons.", "দৈনিক কুইজ বা পাঠে ৩ দিনের স্ট্রিক ধরে রাখো।", "streak_days", 3L, 30L),
        BadgeCatalogItem("badge_streak_7", BadgeCategory.STREAK, BadgeRarity.UNCOMMON, "7-Day Discipline", "৭ দিনের নিয়মানুবর্তিতা", "Maintain a full 7-day learning streak.", "৭ দিনের ধারাবাহিক স্ট্রিক অর্জন করো।", "streak_days", 7L, 60L),
        BadgeCatalogItem("badge_streak_14", BadgeCategory.STREAK, BadgeRarity.RARE, "Fortnight Scholar", "১৪ দিনের সাধনা", "Maintain a 14-day uninterrupted streak.", "টানা ১৪ দিনের স্ট্রিক বজায় রাখো।", "streak_days", 14L, 100L),
        BadgeCatalogItem("badge_streak_30", BadgeCategory.STREAK, BadgeRarity.EPIC, "30-Day Iron Will", "৩০ দিনের অবিচলতা", "Maintain a 30-day Duolingo-grade streak.", "টানা ৩০ দিনের স্ট্রিক সম্পূর্ণ করো।", "streak_days", 30L, 200L),
        BadgeCatalogItem("badge_streak_100", BadgeCategory.STREAK, BadgeRarity.LEGENDARY, "Centurion Flame", "১০০ দিনের কিংবদন্তি", "Reach a legendary 100-day streak.", "১০০ দিনের ঐতিহাসিক স্ট্রিক অর্জন করো।", "streak_days", 100L, 450L),

        // Assignment Badges
        BadgeCatalogItem("badge_assign_1", BadgeCategory.ASSIGNMENT, BadgeRarity.COMMON, "First Assignment", "প্রথম অ্যাসাইনমেন্ট", "Submit your first verified assignment.", "তোমার প্রথম অ্যাসাইনমেন্ট জমা দাও।", "assignments_completed", 1L, 25L),
        BadgeCatalogItem("badge_assign_5", BadgeCategory.ASSIGNMENT, BadgeRarity.UNCOMMON, "Assignment Explorer", "অ্যাসাইনমেন্ট অভিযাত্রী", "Complete 5 curriculum assignments.", "৫টি অ্যাসাইনমেন্ট সম্পূর্ণ করো।", "assignments_completed", 5L, 60L),
        BadgeCatalogItem("badge_assign_15", BadgeCategory.ASSIGNMENT, BadgeRarity.RARE, "Assignment Master", "অ্যাসাইনমেন্ট মাস্টার", "Complete 15 assignments across periods.", "১৫টি অ্যাসাইনমেন্ট সম্পূর্ণ করো।", "assignments_completed", 15L, 140L),
        BadgeCatalogItem("badge_assign_30", BadgeCategory.ASSIGNMENT, BadgeRarity.EPIC, "Assignment Champion", "অ্যাসাইনমেন্ট চ্যাম্পিয়ন", "Complete 30 assignments cleanly.", "৩০টি অ্যাসাইনমেন্ট সম্পূর্ণ করো।", "assignments_completed", 30L, 240L),

        // Exam Badges
        BadgeCatalogItem("badge_exam_1", BadgeCategory.EXAM, BadgeRarity.COMMON, "First Mock Exam", "প্রথম মডেল টেস্ট", "Complete your first timed mock exam.", "তোমার প্রথম সময়বদ্ধ মডেল টেস্ট দাও।", "exams_completed", 1L, 30L),
        BadgeCatalogItem("badge_exam_high", BadgeCategory.EXAM, BadgeRarity.RARE, "High Scorer (85%+)", "উচ্চ নম্বর অর্জনকারী", "Score 85% or higher on a mock or promotion exam.", "মডেল টেস্টে ৮৫%+ নম্বর অর্জন করো।", "high_score_exams", 1L, 90L),
        BadgeCatalogItem("badge_exam_perfect", BadgeCategory.EXAM, BadgeRarity.LEGENDARY, "Perfect 100% Score", "শতভাগ সঠিক", "Achieve 100% accuracy in an official mock exam.", "মডেল টেস্টে ১০০% সঠিক উত্তর দাও।", "perfect_exams", 1L, 250L),
        BadgeCatalogItem("badge_exam_champ", BadgeCategory.EXAM, BadgeRarity.EPIC, "Exam Veteran", "পরীক্ষা চ্যাম্পিয়ন", "Complete 10 mock or promotion exams.", "১০টি মডেল বা প্রোমোশন পরীক্ষা সম্পন্ন করো।", "exams_completed", 10L, 180L),

        // Goal Badges
        BadgeCatalogItem("badge_goal_1", BadgeCategory.GOAL, BadgeRarity.COMMON, "Goal Setter", "লক্ষ্য নির্ধারক", "Complete your first personal study goal.", "তোমার প্রথম ব্যক্তিগত লক্ষ্য পূরণ করো।", "goals_completed", 1L, 25L),
        BadgeCatalogItem("badge_goal_5", BadgeCategory.GOAL, BadgeRarity.UNCOMMON, "Goal Finisher", "লক্ষ্য পূরণকারী", "Finish 5 personal study goals.", "৫টি ব্যক্তিগত অধ্যয়ন লক্ষ্য পূরণ করো।", "goals_completed", 5L, 70L),
        BadgeCatalogItem("badge_goal_15", BadgeCategory.GOAL, BadgeRarity.RARE, "Relentless Planner", "অদম্য পরিকল্পনাকারী", "Finish 15 personal study goals.", "১৫টি ব্যক্তিগত লক্ষ্য পূরণ করো।", "goals_completed", 15L, 150L),

        // Challenge & Competition Badges
        BadgeCatalogItem("badge_chal_1", BadgeCategory.CHALLENGE, BadgeRarity.COMMON, "Daily Challenger", "দৈনিক চ্যালেঞ্জার", "Complete a full daily question challenge.", "একটি পূর্ণ দৈনিক প্রশ্ন চ্যালেঞ্জ শেষ করো।", "challenges_completed", 1L, 25L),
        BadgeCatalogItem("badge_chal_10", BadgeCategory.CHALLENGE, BadgeRarity.RARE, "Arena Competitor", "অ্যারেনা যোদ্ধা", "Win or complete 10 challenges and battles.", "১০টি চ্যালেঞ্জ বা কুইজ যুদ্ধ সম্পন্ন করো।", "challenges_completed", 10L, 120L),
        BadgeCatalogItem("badge_rank_top3", BadgeCategory.CHALLENGE, BadgeRarity.EPIC, "Podium Finisher", "পোডিয়াম বিজয়ী", "Place Top 3 in a weekly/monthly competition period.", "সাপ্তাহিক বা মাসিক লিডারবোর্ডে শীর্ষ ৩-এ স্থান পাও।", "podium_finishes", 1L, 220L),
        BadgeCatalogItem("badge_ai_video_1", BadgeCategory.STUDY, BadgeRarity.UNCOMMON, "AI Storyboard Pioneer", "এআই ভিডিও শিক্ষার্থী", "Generate and complete an AI Learning Storyboard quiz.", "একটি এআই স্টোরিবোর্ড ভিডিও ও কুইজ সম্পন্ন করো।", "ai_videos_completed", 1L, 45L),

        // Community Badges
        BadgeCatalogItem("badge_comm_1", BadgeCategory.COMMUNITY, BadgeRarity.COMMON, "Class Contributor", "শ্রেণি সহপাঠী", "Share your first helpful question or tip in Class Community.", "তোমার শ্রেণি কমিউনিটিতে প্রথম পোস্ট বা সাহায্য শেয়ার করো।", "community_contributions", 1L, 20L),
        BadgeCatalogItem("badge_comm_5", BadgeCategory.COMMUNITY, BadgeRarity.UNCOMMON, "Peer Helper", "সহপাঠী বন্ধু", "Contribute 5 posts or helpful replies in your class.", "শ্রেণি কমিউনিটিতে ৫টি পোস্ট বা সহায়ক উত্তর দাও।", "community_contributions", 5L, 65L),
        BadgeCatalogItem("badge_comm_15", BadgeCategory.COMMUNITY, BadgeRarity.RARE, "Class Mentor", "শ্রেণি মেন্টর", "Contribute 15 helpful answers & study tips.", "১৫টি সহায়ক উত্তর ও টিপস শেয়ার করো।", "community_contributions", 15L, 140L),
        BadgeCatalogItem("badge_promo_pass", BadgeCategory.EXAM, BadgeRarity.RARE, "Annual Scholar", "বার্ষিক উত্তীর্ণ", "Pass your Annual Class Promotion Exam.", "বার্ষিক শ্রেণি প্রোমোশন পরীক্ষায় উত্তীর্ণ হও।", "promotion_passes", 1L, 160L)
    )

    fun defaultAssignmentsForClass(classId: String): List<AssignmentDefinitionDoc> {
        val dayKey = currentDayKey()
        val weekKey = currentWeekKey()
        val biweekKey = currentBiweekKey()
        val monthKey = currentMonthKey()
        return listOf(
            AssignmentDefinitionDoc(
                id = "assign_daily_mcq_$dayKey",
                titleEn = "Daily Sprint: Solve 10 NCTB MCQs",
                titleBn = "দৈনিক স্প্রিন্ট: ১০টি এনসিটিবি MCQ সমাধান",
                descriptionEn = "Answer 10 MCQs accurately across your subjects today.",
                descriptionBn = "আজ তোমার বিষয়গুলো থেকে ১০টি MCQ সমাধান করো।",
                assignmentType = "daily",
                classId = classId,
                subjectId = "math",
                difficulty = "easy",
                targetCount = 1L,
                durationLimitMinutes = 15L,
                xpReward = 40L,
                coinReward = 15L,
                active = true,
                periodKey = dayKey
            ),
            AssignmentDefinitionDoc(
                id = "assign_weekly_chapter_$weekKey",
                titleEn = "Weekly Chapter Drill: Complete 3 Lessons",
                titleBn = "সাপ্তাহিক অধ্যায় অনুশীলন: ৩টি পাঠ সম্পন্ন",
                descriptionEn = "Read and finish 3 full curriculum lessons this week.",
                descriptionBn = "এই সপ্তাহে ৩টি পূর্ণ পাঠ সম্পন্ন করো।",
                assignmentType = "weekly",
                classId = classId,
                subjectId = "physics",
                difficulty = "medium",
                targetCount = 3L,
                durationLimitMinutes = 45L,
                xpReward = 90L,
                coinReward = 35L,
                active = true,
                periodKey = weekKey
            ),
            AssignmentDefinitionDoc(
                id = "assign_biweek_challenge_$biweekKey",
                titleEn = "Two-Week Mastery Challenge: 4 Timed Quizzes",
                titleBn = "পাক্ষিক দক্ষতা চ্যালেঞ্জ: ৪টি সময়বদ্ধ কুইজ",
                descriptionEn = "Finish 4 practice quizzes or mock exams over 14 days.",
                descriptionBn = "১৪ দিনে ৪টি প্র্যাকটিস কুইজ বা মডেল টেস্ট শেষ করো।",
                assignmentType = "two_week",
                classId = classId,
                subjectId = "english",
                difficulty = "medium",
                targetCount = 4L,
                durationLimitMinutes = 60L,
                xpReward = 140L,
                coinReward = 50L,
                active = true,
                periodKey = biweekKey
            ),
            AssignmentDefinitionDoc(
                id = "assign_monthly_marathon_$monthKey",
                titleEn = "Monthly Scholar Marathon: 8 Focus/Exam Sessions",
                titleBn = "মাসিক বিদ্বান ম্যারাথন: ৮টি ফোকাস/পরীক্ষা সেশন",
                descriptionEn = "Log 8 verified focus sessions or mock exams this month.",
                descriptionBn = "এই মাসে ৮টি ফোকাস সেশন বা মডেল টেস্ট সম্পূর্ণ করো।",
                assignmentType = "monthly",
                classId = classId,
                subjectId = "all",
                difficulty = "hard",
                targetCount = 8L,
                durationLimitMinutes = 180L,
                xpReward = 240L,
                coinReward = 90L,
                active = true,
                periodKey = monthKey
            )
        )
    }

    val focusMusicTracks: List<FocusMusicTrack> = listOf(
        FocusMusicTrack(
            id = "track_deep_alpha",
            category = "focus",
            titleEn = "Deep Study Alpha Pad",
            titleBn = "গভীর মনোযোগ আলফা প্যাড",
            subtitleEn = "Warm low-harmonic focus drone (432Hz + 10Hz)",
            subtitleBn = "স্থির মনোযোগের জন্য মৃদু সুর (৪৩২Hz)",
            baseFreqHz = 216.0,
            harmonicFreqHz = 324.0,
            tempoBpm = 60,
            mode = "binaural_pad"
        ),
        FocusMusicTrack(
            id = "track_night_library",
            category = "focus",
            titleEn = "Dhaka Library Lo-Fi Drone",
            titleBn = "লাইব্রেরি শান্ত লয়",
            subtitleEn = "Gentle chord hum for math & problem solving",
            subtitleBn = "গণিত ও সৃজনশীল চর্চার উপযোগী মৃদু সুর",
            baseFreqHz = 196.0,
            harmonicFreqHz = 294.0,
            tempoBpm = 72,
            mode = "lofi_drone"
        ),
        FocusMusicTrack(
            id = "track_monsoon_rain",
            category = "relaxing",
            titleEn = "Monsoon Rain & Chimes",
            titleBn = "বর্ষার বৃষ্টি ও ঘণ্টা",
            subtitleEn = "Soft filtered rain wash with gentle harmonic bells",
            subtitleBn = "মৃদু বৃষ্টির ধ্বনি ও শান্ত ঘণ্টার সুর",
            baseFreqHz = 261.63,
            harmonicFreqHz = 392.0,
            tempoBpm = 54,
            mode = "rain_chime"
        ),
        FocusMusicTrack(
            id = "track_zen_breath",
            category = "relaxing",
            titleEn = "Break Breath Harmonics",
            titleBn = "বিরতির প্রশান্ত সুর",
            subtitleEn = "Calming fifth intervals for eye & mind rest",
            subtitleBn = "চোখ ও মনের বিশ্রামের জন্য প্রশান্ত সুর",
            baseFreqHz = 174.61,
            harmonicFreqHz = 261.63,
            tempoBpm = 50,
            mode = "soft_bell"
        ),
        FocusMusicTrack(
            id = "track_study_room",
            category = "background",
            titleEn = "Quiet Study Ambience",
            titleBn = "নীরব পড়ার ঘর",
            subtitleEn = "Subtle warm ambient air tone for zero distraction",
            subtitleBn = "বিরক্তিহীন মৃদু পটভূমি সুর",
            baseFreqHz = 146.83,
            harmonicFreqHz = 220.0,
            tempoBpm = 58,
            mode = "binaural_pad"
        )
    )
}
