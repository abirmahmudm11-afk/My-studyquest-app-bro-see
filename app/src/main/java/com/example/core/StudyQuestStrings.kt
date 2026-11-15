package com.example.core

object StudyQuestStrings {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun formatDigits(value: Int, lang: AppLanguage): String =
        formatDigits(value.toString(), lang)

    fun formatDigits(value: Long, lang: AppLanguage): String =
        formatDigits(value.toString(), lang)

    fun formatDigits(text: String, lang: AppLanguage): String {
        if (lang == AppLanguage.ENGLISH) return text
        val sb = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun t(lang: AppLanguage, en: String, bn: String): String =
        if (lang == AppLanguage.BANGLA) bn else en

    // 5-Tab Navigation Labels
    fun tabToday(lang: AppLanguage) = t(lang, "Today", "আজ")
    fun tabLearn(lang: AppLanguage) = t(lang, "Learn", "শিখুন")
    fun tabCompete(lang: AppLanguage) = t(lang, "Compete", "প্রতিযোগিতা")
    fun tabFriends(lang: AppLanguage) = t(lang, "Friends", "বন্ধু")
    fun tabProfile(lang: AppLanguage) = t(lang, "Profile", "প্রোফাইল")

    // Level Titles
    fun levelTitle(level: Int, lang: AppLanguage): String = when {
        level >= 25 -> t(lang, "Master Learner", "মাস্টার শিক্ষার্থী")
        level >= 18 -> t(lang, "Expert", "দক্ষ")
        level >= 12 -> t(lang, "Achiever", "অর্জনকারী")
        level >= 7 -> t(lang, "Scholar", "বিদ্বান")
        level >= 3 -> t(lang, "Explorer", "অভিযাত্রী")
        else -> t(lang, "Beginner", "নবীন")
    }

    // Rank Tiers
    fun rankTierLabel(tier: String, lang: AppLanguage): String = when (tier) {
        "Master" -> t(lang, "Master", "মাস্টার")
        "Diamond" -> t(lang, "Diamond", "ডায়মন্ড")
        "Platinum" -> t(lang, "Platinum", "প্ল্যাটিনাম")
        "Gold" -> t(lang, "Gold", "স্বর্ণ")
        "Silver" -> t(lang, "Silver", "রৌপ্য")
        else -> t(lang, "Bronze", "ব্রোঞ্জ")
    }

    // Connectivity & Sync
    fun onlineStatus(lang: AppLanguage) = t(lang, "Online", "অনলাইন")
    fun offlineStatus(lang: AppLanguage) = t(lang, "Offline Mode", "অফলাইন মোড")
    fun pendingSyncLabel(count: Int, lang: AppLanguage): String =
        if (lang == AppLanguage.BANGLA) "${formatDigits(count, lang)}টি সিঙ্ক বাকি"
        else "$count pending sync"
}
