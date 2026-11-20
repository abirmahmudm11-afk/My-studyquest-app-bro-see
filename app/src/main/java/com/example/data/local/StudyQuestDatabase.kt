package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedBookEntity::class,
        BookChapterEntity::class,
        BookPageNoteEntity::class,
        CachedLessonEntity::class,
        CachedQuestionEntity::class,
        LocalBookmarkEntity::class,
        LocalRevisionEntity::class,
        PendingSyncActionEntity::class,
        LocalStudyPlanEntity::class,
        LocalQuestProgressEntity::class,
        LocalSoloMatchEntity::class,
        LocalStudyCircleNoteEntity::class,
        LocalStudySessionEntity::class,
        LocalStudyGoalEntity::class,
        LocalBadgeProgressEntity::class,
        LocalXpLedgerEntity::class,
        DownloadPackEntity::class,
        MistakeNoteEntity::class,
        ExamCountdownEntity::class,
        FormulaBookmarkEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class StudyQuestDatabase : RoomDatabase() {
    abstract fun studyQuestDao(): StudyQuestDao

    companion object {
        @Volatile
        private var INSTANCE: StudyQuestDatabase? = null

        fun getInstance(context: Context): StudyQuestDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyQuestDatabase::class.java,
                    "studyquest_offline.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
