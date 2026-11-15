package com.example.data.model

import com.example.core.AppLanguage

/**
 * Authoritative Quest Plan Store catalog for StudyQuest.
 * Strictly 7 monthly plans + 1 non-recurring Video Boost add-on.
 * No storage plans or storage add-ons exist anywhere in StudyQuest.
 */
data class QuestPlanDefinition(
    val id: String,
    val playProductId: String?,
    val titleEn: String,
    val titleBn: String,
    val badgeEn: String,
    val badgeBn: String,
    val priceBdtMonthly: Int,
    val monthlyAiCredits: Int,
    val monthlyVideoCredits: Int,
    val adFree: Boolean,
    val highlightsEn: List<String>,
    val highlightsBn: List<String>
)

data class VideoBoostDefinition(
    val id: String = "video_boost",
    val playProductId: String = "sq_addon_video_boost",
    val titleEn: String = "Video Boost (+1 Short Video Credit)",
    val titleBn: String = "ভিডিও বুস্ট (+১টি শর্ট ভিডিও ক্রেডিট)",
    val priceBdtOneTime: Int = 20,
    val maxDurationSeconds: Int = 60,
    val creditsGranted: Int = 1,
    val recurring: Boolean = false
)

object QuestPlanCatalog {
    const val STUDYQUEST_AI_PLAN_ID = "studyquest_ai"
    const val STUDYQUEST_AI_PRICE_BDT = 150

    val videoBoost = VideoBoostDefinition()

    val plans: List<QuestPlanDefinition> = listOf(
        QuestPlanDefinition(
            id = "free",
            playProductId = null,
            titleEn = "Free StudyQuest",
            titleBn = "ফ্রি StudyQuest",
            badgeEn = "Always Free",
            badgeBn = "সর্বদা ফ্রি",
            priceBdtMonthly = 0,
            monthlyAiCredits = 20,
            monthlyVideoCredits = 1,
            adFree = false,
            highlightsEn = listOf(
                "All NCTB Classes 6–10 lessons, MCQ/CQ Bank, Mock Exams & Focus",
                "Full public Video Library viewing & search",
                "20 StudyQuest AI Helper credits / month",
                "1 animated short-video creation credit / month",
                "Limited permitted ads (never in focus, quizzes, exams, or video)"
            ),
            highlightsBn = listOf(
                "শ্রেণি ৬–১০ এর সকল এনসিটিবি পাঠ, MCQ/CQ ও মডেল টেস্ট",
                "পাবলিক ভিডিও লাইব্রেরি সম্পূর্ণ দেখা ও সার্চ",
                "প্রতি মাসে ২০টি StudyQuest AI হেল্পার ক্রেডিট",
                "প্রতি মাসে ১টি অ্যানিমেটেড শর্ট ভিডিও ক্রেডিট",
                "সীমিত বিজ্ঞাপন (ফোকাস, পরীক্ষা ও ভিডিওতে কোনো বিজ্ঞাপন নয়)"
            )
        ),
        QuestPlanDefinition(
            id = "plus",
            playProductId = "sq_plan_plus",
            titleEn = "StudyQuest Plus",
            titleBn = "StudyQuest প্লাস",
            badgeEn = "Ad-Free",
            badgeBn = "বিজ্ঞাপনমুক্ত",
            priceBdtMonthly = 79,
            monthlyAiCredits = 50,
            monthlyVideoCredits = 2,
            adFree = true,
            highlightsEn = listOf(
                "100% ad-free study across all screens",
                "50 StudyQuest AI Helper credits / month",
                "2 animated video creation credits / month",
                "Monthly renewable AI + video credits"
            ),
            highlightsBn = listOf(
                "সব স্ক্রিনে ১০০% বিজ্ঞাপনমুক্ত পড়াশোনা",
                "প্রতি মাসে ৫০টি StudyQuest AI ক্রেডিট",
                "প্রতি মাসে ২টি ভিডিও তৈরির ক্রেডিট",
                "প্রতি মাসে নবায়নযোগ্য ক্রেডিট"
            )
        ),
        QuestPlanDefinition(
            id = "video_pass",
            playProductId = "sq_plan_video_pass",
            titleEn = "Video Pass",
            titleBn = "ভিডিও পাস",
            badgeEn = "Creator Starter",
            badgeBn = "ভিডিও ক্রিয়েটর",
            priceBdtMonthly = 99,
            monthlyAiCredits = 30,
            monthlyVideoCredits = 5,
            adFree = true,
            highlightsEn = listOf(
                "Ad-free learning & video creation",
                "5 animated video creation credits / month",
                "30 StudyQuest AI Helper credits / month",
                "Bangla & English voiceover storyboard studio"
            ),
            highlightsBn = listOf(
                "বিজ্ঞাপনমুক্ত পড়া ও ভিডিও স্টুডিও",
                "প্রতি মাসে ৫টি অ্যানিমেটেড ভিডিও ক্রেডিট",
                "প্রতি মাসে ৩০টি StudyQuest AI ক্রেডিট",
                "বাংলা ও ইংরেজি ভয়েসওভার স্টোরিবোর্ড"
            )
        ),
        QuestPlanDefinition(
            id = STUDYQUEST_AI_PLAN_ID,
            playProductId = "sq_plan_studyquest_ai",
            titleEn = "StudyQuest AI",
            titleBn = "StudyQuest AI",
            badgeEn = "Best AI Value",
            badgeBn = "সেরা AI প্ল্যান",
            priceBdtMonthly = STUDYQUEST_AI_PRICE_BDT,
            monthlyAiCredits = 100,
            monthlyVideoCredits = 2,
            adFree = true,
            highlightsEn = listOf(
                "Dedicated owner-funded StudyQuest AI Helper plan",
                "100 StudyQuest AI credits / month (all 12 AI Lab tools)",
                "2 animated video creation credits / month",
                "100% ad-free studying & priority tutor routing"
            ),
            highlightsBn = listOf(
                "ডেডিকেটেড StudyQuest AI হেল্পার প্ল্যান",
                "প্রতি মাসে ১০০টি AI ক্রেডিট (সকল ১২টি AI ল্যাব টুল)",
                "প্রতি মাসে ২টি ভিডিও তৈরির ক্রেডিট",
                "১০০% বিজ্ঞাপনমুক্ত অভিজ্ঞতা"
            )
        ),
        QuestPlanDefinition(
            id = "scholar",
            playProductId = "sq_plan_scholar",
            titleEn = "Scholar Bundle",
            titleBn = "স্কলার বান্ডেল",
            badgeEn = "Popular",
            badgeBn = "জনপ্রিয়",
            priceBdtMonthly = 229,
            monthlyAiCredits = 140,
            monthlyVideoCredits = 6,
            adFree = true,
            highlightsEn = listOf(
                "Balanced AI tutoring + animated video bundle",
                "140 StudyQuest AI credits / month",
                "6 animated video creation credits / month",
                "100% ad-free experience"
            ),
            highlightsBn = listOf(
                "AI টিউটর ও অ্যানিমেটেড ভিডিওর সমন্বিত বান্ডেল",
                "প্রতি মাসে ১৪০টি AI ক্রেডিট",
                "প্রতি মাসে ৬টি ভিডিও তৈরির ক্রেডিট",
                "১০০% বিজ্ঞাপনমুক্ত পড়াশোনা"
            )
        ),
        QuestPlanDefinition(
            id = "creator",
            playProductId = "sq_plan_creator",
            titleEn = "Creator Bundle",
            titleBn = "ক্রিয়েটর বান্ডেল",
            badgeEn = "Heavy Study",
            badgeBn = "হ্যাভি ক্রিয়েটর",
            priceBdtMonthly = 329,
            monthlyAiCredits = 250,
            monthlyVideoCredits = 10,
            adFree = true,
            highlightsEn = listOf(
                "Built for active educators & SSC power learners",
                "250 StudyQuest AI credits / month",
                "10 animated video creation credits / month",
                "100% ad-free experience"
            ),
            highlightsBn = listOf(
                "সক্রিয় শিক্ষার্থী ও ক্রিয়েটরদের জন্য আদর্শ",
                "প্রতি মাসে ২৫০টি AI ক্রেডিট",
                "প্রতি মাসে ১০টি ভিডিও তৈরির ক্রেডিট",
                "১০০% বিজ্ঞাপনমুক্ত অভিজ্ঞতা"
            )
        ),
        QuestPlanDefinition(
            id = "complete_max",
            playProductId = "sq_plan_complete_max",
            titleEn = "Complete Max",
            titleBn = "কমপ্লিট ম্যাক্স",
            badgeEn = "Max Bundle",
            badgeBn = "সর্বোচ্চ বান্ডেল",
            priceBdtMonthly = 449,
            monthlyAiCredits = 400,
            monthlyVideoCredits = 15,
            adFree = true,
            highlightsEn = listOf(
                "Highest monthly StudyQuest allowance",
                "400 StudyQuest AI credits / month",
                "15 animated video creation credits / month",
                "100% ad-free experience across all tools"
            ),
            highlightsBn = listOf(
                "সর্বোচ্চ মাসিক StudyQuest প্যাকেজ",
                "প্রতি মাসে ৪০০টি AI ক্রেডিট",
                "প্রতি মাসে ১৫টি ভিডিও তৈরির ক্রেডিট",
                "সব টুলে ১০০% বিজ্ঞাপনমুক্ত অভিজ্ঞতা"
            )
        )
    )

    fun getPlanCatalog(): List<QuestPlanDefinition> = plans

    fun planById(planId: String?): QuestPlanDefinition {
        val normalized = when (planId?.lowercase()) {
            "premium" -> "plus"
            null, "" -> "free"
            else -> planId.lowercase()
        }
        return plans.find { it.id == normalized } ?: plans.first()
    }

    fun getCanonicalPrice(planId: String): Int =
        if (planId == videoBoost.id) videoBoost.priceBdtOneTime
        else planById(planId).priceBdtMonthly

    fun formatPriceBdt(priceBdt: Int, language: AppLanguage, recurring: Boolean = true): String {
        return if (priceBdt == 0) {
            if (language == AppLanguage.BANGLA) "৳০ / ফ্রি" else "৳0 / Free"
        } else {
            val digits = com.example.core.StudyQuestStrings.formatDigits(priceBdt, language)
            if (recurring) {
                if (language == AppLanguage.BANGLA) "৳$digits / মাস" else "৳$digits/mo"
            } else {
                if (language == AppLanguage.BANGLA) "৳$digits (এককালীন)" else "৳$digits (one-time)"
            }
        }
    }
}
