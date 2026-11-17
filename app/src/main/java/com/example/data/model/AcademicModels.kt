package com.example.data.model

enum class CurriculumVersion(val id: String, val labelEn: String, val labelBn: String) {
    BANGLA("bangla", "Bangla Version (NCTB)", "বাংলা ভার্সন (এনসিটিবি)"),
    ENGLISH("english", "English Version (NCTB)", "ইংরেজি ভার্সন (এনসিটিবি)")
}

enum class AcademicGroup(val id: String, val labelEn: String, val labelBn: String) {
    GENERAL("general", "General", "সাধারণ"),
    SCIENCE("science", "Science", "বিজ্ঞান"),
    BUSINESS("business", "Business Studies", "ব্যবসায় শিক্ষা"),
    HUMANITIES("humanities", "Humanities", "মানবিক")
}

enum class QuestionType(val id: String, val labelEn: String, val labelBn: String) {
    SINGLE_MCQ("single_mcq", "Single MCQ", "একক বহুনির্বাচনি"),
    MULTI_MCQ("multi_mcq", "Multiple-Answer MCQ", "একাধিক উত্তর MCQ"),
    TRUE_FALSE("true_false", "True / False", "সত্য / মিথ্যা"),
    FILL_BLANK("fill_blank", "Fill in the Blanks", "শূন্যস্থান পূরণ"),
    SHORT_ANSWER("short_answer", "Short Answer", "সংক্ষিপ্ত প্রশ্ন"),
    CREATIVE_CQ("creative_cq", "Creative Question (CQ)", "সৃজনশীল প্রশ্ন (CQ)"),
    MATCHING("matching", "Matching", "মিলকরণ"),
    NUMERICAL("numerical", "Numerical", "গাণিতিক উত্তর"),
    FLASHCARD("flashcard", "Flashcard", "ফ্ল্যাশকার্ড")
}

enum class QuestionDifficulty(val id: String, val labelEn: String, val labelBn: String) {
    EASY("easy", "Easy", "সহজ"),
    MEDIUM("medium", "Medium", "মাঝারি"),
    HARD("hard", "Hard", "কঠিন")
}

enum class QuestionProvenance(val id: String, val labelEn: String, val labelBn: String) {
    OFFICIAL_TEXTBOOK_EXERCISE("official_textbook_exercise", "NCTB Textbook Exercise", "এনসিটিবি পাঠ্যবই অনুশীলনী"),
    OFFICIAL_BOARD_EXAM("official_board_exam", "Board Exam Archive", "বোর্ড পরীক্ষার প্রশ্ন"),
    STUDYQUEST_ORIGINAL("studyquest_original", "StudyQuest Original", "স্টাডিকোয়েস্ট মূল প্রশ্ন"),
    AI_ASSISTED_REVIEWED("ai_assisted_reviewed", "AI-Assisted (Reviewed)", "এআই-সহায়তাপ্রাপ্ত (যাচাইকৃত)")
}

enum class TextbookDownloadStatus(val id: String, val labelEn: String, val labelBn: String) {
    NOT_DOWNLOADED("not_downloaded", "Chapter Pack Ready", "অধ্যায় প্যাক প্রস্তুত"),
    DOWNLOADING("downloading", "Installing Offline...", "অফলাইনে ইনস্টল হচ্ছে..."),
    INSTALLED("installed", "Installed Offline", "অফলাইনে সংরক্ষিত"),
    FAILED("failed", "Retry Install", "আবার চেষ্টা করো")
}

enum class QuestCoachMood {
    WELCOME,
    LEARNING_ASSISTANCE,
    ENCOURAGEMENT,
    SUCCESS,
    QUEST_COMPLETION,
    ACHIEVEMENT,
    STUDY_REMINDER,
    EMPTY_STATE
}

data class ClassLevel(
    val id: String,
    val labelEn: String,
    val labelBn: String,
    val academicYear: String = "2026",
    val supportsGroups: Boolean = false,
    val futureReadyTag: String? = null
)

data class AcademicSubject(
    val id: String,
    val classIds: List<String>,
    val groupIds: List<String> = listOf("general"),
    val titleEn: String,
    val titleBn: String,
    val code: String,
    val accentHex: Long
)

data class TextbookCatalogItem(
    val id: String,
    val academicYear: String, // "2026" or "2027"
    val classId: String,
    val curriculumVersion: String, // "bangla" or "english"
    val groupId: String = "general",
    val subjectId: String,
    val titleEn: String,
    val titleBn: String,
    val bookCode: String,
    val editionLabelEn: String,
    val editionLabelBn: String,
    val officialSourceUrl: String,
    val pdfUrl: String? = null,
    val estimatedPages: Int = 180,
    val estimatedSizeMb: Double = 8.4,
    val is2027EditionReady: Boolean = false,
    val publicationStatus: String = "published"
)

data class TextbookChapterSpec(
    val id: String,
    val bookId: String,
    val orderIndex: Int,
    val titleEn: String,
    val titleBn: String,
    val summaryEn: String,
    val summaryBn: String,
    val startPage: Int,
    val endPage: Int,
    val keyTopicsEn: List<String>,
    val keyTopicsBn: List<String>,
    val formulas: List<String> = emptyList(),
    val studyNotesEn: String = "",
    val studyNotesBn: String = ""
)

data class AcademicChapter(
    val id: String,
    val subjectId: String,
    val classId: String,
    val orderIndex: Int,
    val titleEn: String,
    val titleBn: String,
    val summaryEn: String,
    val summaryBn: String
)

data class AcademicTopic(
    val id: String,
    val chapterId: String,
    val titleEn: String,
    val titleBn: String
)

data class StudyLesson(
    val id: String,
    val classId: String,
    val subjectId: String,
    val chapterId: String,
    val topicId: String,
    val titleEn: String,
    val titleBn: String,
    val objectivesEn: List<String>,
    val objectivesBn: List<String>,
    val explanationEn: String,
    val explanationBn: String,
    val definitionsEn: List<Pair<String, String>>,
    val definitionsBn: List<Pair<String, String>>,
    val formulas: List<String>,
    val diagramAscii: String,
    val examplesEn: List<String>,
    val examplesBn: List<String>,
    val keyPointsEn: List<String>,
    val keyPointsBn: List<String>,
    val relatedTopicIds: List<String>,
    val estimatedMinutes: Int = 10,
    val sourceAttribution: String = "StudyQuest NCTB-Aligned Original Guide (2026)"
)

data class QuestionItem(
    val id: String,
    val academicYear: String = "2026",
    val curriculumVersion: String = "bangla",
    val classId: String,
    val subjectId: String,
    val chapterId: String,
    val topicId: String,
    val bookId: String = "",
    val board: String = "Practice Bank",
    val examYear: Int = 2025,
    val type: QuestionType,
    val difficulty: QuestionDifficulty,
    val language: String = "both",
    val promptEn: String,
    val promptBn: String,
    val optionsEn: List<String> = emptyList(),
    val optionsBn: List<String> = emptyList(),
    val correctOptionIndices: List<Int> = emptyList(),
    val acceptableTextAnswers: List<String> = emptyList(),
    val cqStemEn: String = "",
    val cqStemBn: String = "",
    val cqSubQuestionsEn: List<String> = emptyList(),
    val cqSubQuestionsBn: List<String> = emptyList(),
    val cqRubricEn: String = "",
    val cqRubricBn: String = "",
    val explanationEn: String,
    val explanationBn: String,
    val marks: Int = 1,
    val provenance: QuestionProvenance = QuestionProvenance.STUDYQUEST_ORIGINAL,
    val sourceAttribution: String = "StudyQuest Original Practice Item",
    val reviewStatus: String = "approved",
    val publicationStatus: String = "published",
    val version: Int = 1
)

data class ExamTemplate(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val classId: String,
    val subjectIds: List<String>,
    val chapterId: String? = null,
    val durationSeconds: Int,
    val totalMarks: Int,
    val negativeMarkingPerWrong: Double = 0.0,
    val isOfficialTemplate: Boolean = true,
    val descriptionEn: String,
    val descriptionBn: String
)

data class AvatarCosmetic(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val category: String, // "avatar", "frame", "title", "theme"
    val costCoins: Int,
    val unlockNoteEn: String,
    val unlockNoteBn: String
)

data class AchievementCatalogItem(
    val id: String,
    val category: String, // "Learning", "Consistency", "Academic", "Social", "Competition"
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val xpBonus: Int,
    val coinBonus: Int
)

data class QuestCatalogItem(
    val id: String,
    val questType: String, // "daily", "weekly", "subject"
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val target: Int,
    val xpReward: Int,
    val coinReward: Int
)

enum class BadgeRarity(val id: String, val labelEn: String, val labelBn: String) {
    COMMON("common", "Common", "সাধারণ"),
    UNCOMMON("uncommon", "Uncommon", "বিরলপ্রায়"),
    RARE("rare", "Rare", "বিরল"),
    EPIC("epic", "Epic", "মহাকাব্যিক"),
    LEGENDARY("legendary", "Legendary", "কিংবদন্তি")
}

enum class BadgeCategory(val id: String, val labelEn: String, val labelBn: String) {
    STUDY("study", "Study", "অধ্যয়ন"),
    STREAK("streak", "Streak", "স্ট্রিক"),
    ASSIGNMENT("assignment", "Assignment", "অ্যাসাইনমেন্ট"),
    EXAM("exam", "Exam", "পরীক্ষা"),
    GOAL("goal", "Goal", "লক্ষ্য"),
    CHALLENGE("challenge", "Challenge", "চ্যালেঞ্জ"),
    COMMUNITY("community", "Community", "কমিউনিটি")
}

data class BadgeCatalogItem(
    val id: String,
    val category: BadgeCategory,
    val rarity: BadgeRarity,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val metricKey: String,
    val target: Long,
    val xpBonus: Long
)

data class XpRuleConfig(
    val lessonXp: Long = 20L,
    val practiceXpPerCorrect: Long = 4L,
    val examXpBase: Long = 30L,
    val focusXpPerMinute: Long = 2L,
    val dailyChallengeXp: Long = 45L,
    val streakBonusPerDay: Long = 5L,
    val communityHelpfulXp: Long = 15L,
    val maxDailyFocusXp: Long = 240L,
    val maxDailyCommunityXp: Long = 60L
)

data class FocusMusicTrack(
    val id: String,
    val category: String, // "focus", "relaxing", "background"
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val baseFreqHz: Double,
    val harmonicFreqHz: Double,
    val tempoBpm: Int,
    val mode: String // "binaural_pad", "rain_chime", "lofi_drone", "soft_bell"
)
