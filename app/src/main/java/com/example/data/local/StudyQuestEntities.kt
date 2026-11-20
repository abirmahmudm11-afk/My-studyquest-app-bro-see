package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_books")
data class CachedBookEntity(
    @PrimaryKey val bookId: String,
    val academicYear: String,
    val classId: String,
    val curriculumVersion: String,
    val groupId: String,
    val subjectId: String,
    val titleEn: String,
    val titleBn: String,
    val editionLabelEn: String,
    val editionLabelBn: String,
    val officialSourceUrl: String,
    val pdfUrl: String?,
    val localPdfPath: String?,
    val downloadStatus: String, // "not_downloaded", "downloading", "installed", "failed"
    val downloadProgress: Int = 0,
    val fileSizeBytes: Long = 0L,
    val totalPages: Int = 0,
    val lastReadPage: Int = 0,
    val installedOfflineAtMs: Long = 0L,
    val isCustomImported: Boolean = false
)

@Entity(tableName = "cached_book_chapters")
data class BookChapterEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val orderIndex: Int,
    val titleEn: String,
    val titleBn: String,
    val summaryEn: String,
    val summaryBn: String,
    val startPage: Int,
    val endPage: Int,
    val keyTopicsCsv: String,
    val formulasCsv: String,
    val studyNotesEn: String,
    val studyNotesBn: String,
    val completed: Boolean = false
)

@Entity(tableName = "book_page_notes")
data class BookPageNoteEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val pageNumber: Int,
    val chapterId: String? = null,
    val title: String,
    val noteText: String,
    val isBookmark: Boolean = false,
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_lessons")
data class CachedLessonEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val subjectId: String,
    val chapterId: String,
    val bookId: String = "",
    val titleEn: String,
    val titleBn: String,
    val explanationEn: String,
    val explanationBn: String,
    val formulasCsv: String,
    val keyPointsEnCsv: String,
    val keyPointsBnCsv: String,
    val completed: Boolean = false,
    val downloadedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_questions")
data class CachedQuestionEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val subjectId: String,
    val chapterId: String,
    val topicId: String,
    val bookId: String = "",
    val questionType: String,
    val difficulty: String,
    val promptEn: String,
    val promptBn: String,
    val optionsEnPipe: String,
    val optionsBnPipe: String,
    val correctIndicesCsv: String,
    val explanationEn: String,
    val explanationBn: String,
    val marks: Int,
    val provenance: String = "studyquest_original",
    val sourceAttribution: String = "StudyQuest Original Practice Item",
    val downloadedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_bookmarks")
data class LocalBookmarkEntity(
    @PrimaryKey val questionId: String,
    val bookId: String = "",
    val subjectId: String,
    val chapterId: String,
    val topicId: String,
    val isMistake: Boolean,
    val note: String = "",
    val savedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_revisions")
data class LocalRevisionEntity(
    @PrimaryKey val itemId: String, // questionId or flashcardId
    val bookId: String = "",
    val subjectId: String,
    val chapterId: String,
    val topicId: String,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val wrongCount: Int = 0,
    val correctCount: Int = 0,
    val lastReviewedAtMs: Long = 0L,
    val nextDueAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_sync_actions")
data class PendingSyncActionEntity(
    @PrimaryKey val idempotencyKey: String,
    val userId: String,
    val actionType: String, // "lesson", "practice", "exam"
    val refId: String,
    val bookId: String = "",
    val subjectId: String,
    val chapterId: String,
    val topicId: String,
    val score: Double,
    val totalMarks: Double,
    val accuracy: Double,
    val xpAwarded: Int,
    val summary: String,
    val synced: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_study_plans")
data class LocalStudyPlanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookId: String = "",
    val title: String,
    val subjectId: String,
    val planType: String,
    val targetDateEpochMs: Long,
    val minutesPerDay: Long,
    val completed: Boolean = false,
    val syncedToCloud: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_quest_progress")
data class LocalQuestProgressEntity(
    @PrimaryKey val questId: String,
    val questType: String,
    val progress: Long,
    val target: Long,
    val completed: Boolean,
    val claimed: Boolean,
    val xpReward: Long,
    val coinReward: Long,
    val periodKey: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_solo_matches")
data class LocalSoloMatchEntity(
    @PrimaryKey val id: String,
    val mode: String, // "solo_sprint" or "solo_tournament"
    val subjectId: String,
    val classId: String,
    val title: String,
    val score: Long,
    val totalQuestions: Int,
    val xpEarned: Long,
    val won: Boolean,
    val completedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_study_circle_notes")
data class LocalStudyCircleNoteEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val authorLabel: String,
    val isAiBuddy: Boolean,
    val text: String,
    val reactionsCsv: String = "",
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_study_sessions")
data class LocalStudySessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookId: String = "",
    val subjectId: String,
    val mode: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long,
    val focusedSeconds: Long,
    val pausedSeconds: Long,
    val breakSeconds: Long,
    val xpAwarded: Long,
    val status: String,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_study_goals")
data class LocalStudyGoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val bookId: String = "",
    val title: String,
    val goalType: String,
    val periodType: String,
    val subjectId: String,
    val targetValue: Long,
    val currentValue: Long,
    val difficulty: String,
    val xpReward: Long,
    val completed: Boolean,
    val rewardClaimed: Boolean,
    val deadlineEpochMs: Long,
    val periodKey: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_badge_progress")
data class LocalBadgeProgressEntity(
    @PrimaryKey val badgeId: String,
    val userId: String,
    val progress: Long,
    val target: Long,
    val unlocked: Boolean,
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val animationShown: Boolean = false,
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_xp_ledger")
data class LocalXpLedgerEntity(
    @PrimaryKey val idempotencyKey: String,
    val userId: String,
    val sourceType: String,
    val sourceId: String,
    val xpDelta: Long,
    val coinDelta: Long,
    val description: String,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "download_packs")
data class DownloadPackEntity(
    @PrimaryKey val packId: String,
    val category: String, // "textbook", "audio_briefing", "focus_soundscape", "stem_lab_pack"
    val classId: String,
    val subjectId: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val sizeMb: Double,
    val assetFileName: String,
    val remoteUrl: String? = null,
    val status: String = "not_downloaded", // "not_downloaded", "downloading", "installed", "failed"
    val progressPct: Int = 0,
    val localPath: String? = null,
    val installedAtMs: Long = 0L
)

@Entity(tableName = "mistake_notes")
data class MistakeNoteEntity(
    @PrimaryKey val questionId: String,
    val subjectId: String,
    val classId: String,
    val promptEn: String,
    val promptBn: String,
    val wrongCount: Int = 1,
    val lastMissedAtMs: Long = System.currentTimeMillis(),
    val resolved: Boolean = false,
    val reflectionNote: String = ""
)

@Entity(tableName = "exam_countdowns")
data class ExamCountdownEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subjectId: String,
    val targetDateEpochMs: Long,
    val dailyTopicGoal: Int = 2,
    val completedTopics: Int = 0,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "formula_bookmarks")
data class FormulaBookmarkEntity(
    @PrimaryKey val formulaId: String,
    val subjectId: String,
    val titleEn: String,
    val savedAtMs: Long = System.currentTimeMillis()
)

