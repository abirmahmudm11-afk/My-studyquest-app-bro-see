package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_lessons")
data class CachedLessonEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val subjectId: String,
    val chapterId: String,
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
    val downloadedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_bookmarks")
data class LocalBookmarkEntity(
    @PrimaryKey val questionId: String,
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
