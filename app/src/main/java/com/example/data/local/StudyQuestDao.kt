package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyQuestDao {

    // --- Offline Textbooks, Chapters & Page Notes ---
    @Query("SELECT * FROM cached_books ORDER BY installedOfflineAtMs DESC")
    fun observeCachedBooks(): Flow<List<CachedBookEntity>>

    @Query("SELECT * FROM cached_books WHERE bookId = :bookId LIMIT 1")
    suspend fun getCachedBook(bookId: String): CachedBookEntity?

    @Query("SELECT * FROM cached_books WHERE bookId = :bookId LIMIT 1")
    fun observeCachedBook(bookId: String): Flow<CachedBookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedBook(book: CachedBookEntity)

    @Query("UPDATE cached_books SET lastReadPage = :page WHERE bookId = :bookId")
    suspend fun updateBookLastReadPage(bookId: String, page: Int)

    @Query("UPDATE cached_books SET downloadStatus = :status, downloadProgress = :progress WHERE bookId = :bookId")
    suspend fun updateBookDownloadProgress(bookId: String, status: String, progress: Int)

    @Query("DELETE FROM cached_books WHERE bookId = :bookId")
    suspend fun deleteCachedBook(bookId: String)

    @Query("SELECT * FROM cached_book_chapters WHERE bookId = :bookId ORDER BY orderIndex ASC")
    fun observeBookChapters(bookId: String): Flow<List<BookChapterEntity>>

    @Query("SELECT * FROM cached_book_chapters WHERE bookId = :bookId ORDER BY orderIndex ASC")
    suspend fun getBookChapters(bookId: String): List<BookChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookChapters(chapters: List<BookChapterEntity>)

    @Query("UPDATE cached_book_chapters SET completed = 1 WHERE id = :chapterEntityId")
    suspend fun markBookChapterCompleted(chapterEntityId: String)

    @Query("SELECT * FROM book_page_notes WHERE bookId = :bookId ORDER BY pageNumber ASC, updatedAtMs DESC")
    fun observeBookNotes(bookId: String): Flow<List<BookPageNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookPageNote(note: BookPageNoteEntity)

    @Query("DELETE FROM book_page_notes WHERE id = :noteId")
    suspend fun deleteBookPageNote(noteId: String)

    // --- Cached Lessons & Questions ---
    @Query("SELECT * FROM cached_lessons ORDER BY downloadedAtMs DESC")
    fun observeCachedLessons(): Flow<List<CachedLessonEntity>>

    @Query("SELECT * FROM cached_lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getCachedLesson(lessonId: String): CachedLessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: CachedLessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessons(lessons: List<CachedLessonEntity>)

    @Query("UPDATE cached_lessons SET completed = 1 WHERE id = :lessonId")
    suspend fun markLessonCompleted(lessonId: String)

    @Query("SELECT * FROM cached_questions WHERE subjectId = :subjectId")
    fun observeCachedQuestionsBySubject(subjectId: String): Flow<List<CachedQuestionEntity>>

    @Query("SELECT * FROM cached_questions")
    fun observeAllCachedQuestions(): Flow<List<CachedQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestions(questions: List<CachedQuestionEntity>)

    // --- Bookmarks & Revisions ---
    @Query("SELECT * FROM local_bookmarks ORDER BY savedAtMs DESC")
    fun observeBookmarks(): Flow<List<LocalBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: LocalBookmarkEntity)

    @Query("DELETE FROM local_bookmarks WHERE questionId = :questionId")
    suspend fun deleteBookmark(questionId: String)

    @Query("SELECT * FROM local_revisions ORDER BY nextDueAtMs ASC")
    fun observeRevisions(): Flow<List<LocalRevisionEntity>>

    @Query("SELECT * FROM local_revisions WHERE itemId = :itemId LIMIT 1")
    suspend fun getRevision(itemId: String): LocalRevisionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRevision(revision: LocalRevisionEntity)

    // --- Pending Sync Actions ---
    @Query("SELECT * FROM pending_sync_actions WHERE synced = 0 ORDER BY createdAtMs ASC")
    fun observePendingSyncActions(): Flow<List<PendingSyncActionEntity>>

    @Query("SELECT * FROM pending_sync_actions WHERE userId = :userId ORDER BY createdAtMs DESC")
    fun observeAllActionsForUser(userId: String): Flow<List<PendingSyncActionEntity>>

    @Query("SELECT * FROM pending_sync_actions WHERE userId = :userId")
    suspend fun getActionsForUser(userId: String): List<PendingSyncActionEntity>

    @Query("UPDATE pending_sync_actions SET userId = :targetUserId WHERE userId = :sourceUserId")
    suspend fun reassignPendingActionsUser(sourceUserId: String, targetUserId: String)

    @Query("SELECT * FROM pending_sync_actions WHERE synced = 0 ORDER BY createdAtMs ASC")
    suspend fun getUnsyncedActions(): List<PendingSyncActionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueueSyncAction(action: PendingSyncActionEntity)

    @Query("UPDATE pending_sync_actions SET synced = 1 WHERE idempotencyKey = :key")
    suspend fun markSyncActionCompleted(key: String)

    @Query("DELETE FROM pending_sync_actions")
    suspend fun clearPendingSyncActions()

    @Query("DELETE FROM local_bookmarks")
    suspend fun clearBookmarks()

    @Query("DELETE FROM local_revisions")
    suspend fun clearRevisions()

    // --- Local Study Plans (Guest + Offline) ---
    @Query("SELECT * FROM local_study_plans WHERE userId = :userId ORDER BY createdAtMs DESC")
    fun observeStudyPlansForUser(userId: String): Flow<List<LocalStudyPlanEntity>>

    @Query("SELECT * FROM local_study_plans WHERE userId = :userId AND syncedToCloud = 0")
    suspend fun getUnsyncedStudyPlans(userId: String): List<LocalStudyPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyPlan(plan: LocalStudyPlanEntity)

    @Query("UPDATE local_study_plans SET completed = :completed WHERE id = :planId")
    suspend fun updateStudyPlanCompleted(planId: String, completed: Boolean)

    @Query("UPDATE local_study_plans SET syncedToCloud = 1, userId = :targetUserId WHERE id = :planId")
    suspend fun markStudyPlanSynced(planId: String, targetUserId: String)

    @Query("DELETE FROM local_study_plans WHERE id = :planId")
    suspend fun deleteStudyPlan(planId: String)

    // --- Local Guest Quests ---
    @Query("SELECT * FROM local_quest_progress")
    fun observeLocalQuests(): Flow<List<LocalQuestProgressEntity>>

    @Query("SELECT * FROM local_quest_progress WHERE questId = :questId LIMIT 1")
    suspend fun getLocalQuest(questId: String): LocalQuestProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalQuest(quest: LocalQuestProgressEntity)

    @Query("DELETE FROM local_quest_progress")
    suspend fun clearLocalQuests()

    // --- Local Solo Matches (Compete Offline Arena) ---
    @Query("SELECT * FROM local_solo_matches ORDER BY completedAtMs DESC")
    fun observeSoloMatches(): Flow<List<LocalSoloMatchEntity>>

    @Query("SELECT * FROM local_solo_matches ORDER BY completedAtMs DESC")
    suspend fun getAllSoloMatches(): List<LocalSoloMatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoloMatch(match: LocalSoloMatchEntity)

    @Query("DELETE FROM local_solo_matches")
    suspend fun clearSoloMatches()

    // --- Local Study Circle Channels ---
    @Query("SELECT * FROM local_study_circle_notes WHERE channelId = :channelId ORDER BY createdAtMs ASC")
    fun observeStudyCircleNotes(channelId: String): Flow<List<LocalStudyCircleNoteEntity>>

    @Query("SELECT COUNT(*) FROM local_study_circle_notes")
    suspend fun countStudyCircleNotes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyCircleNote(note: LocalStudyCircleNoteEntity)

    @Query("DELETE FROM local_study_circle_notes WHERE id = :noteId")
    suspend fun deleteStudyCircleNote(noteId: String)

    // --- Local Focus Study Sessions ---
    @Query("SELECT * FROM local_study_sessions WHERE userId = :userId ORDER BY endedAtEpochMs DESC")
    fun observeLocalStudySessions(userId: String): Flow<List<LocalStudySessionEntity>>

    @Query("SELECT * FROM local_study_sessions WHERE userId = :userId ORDER BY endedAtEpochMs DESC")
    suspend fun getLocalStudySessions(userId: String): List<LocalStudySessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalStudySession(session: LocalStudySessionEntity)

    // --- Local Personal Study Goals ---
    @Query("SELECT * FROM local_study_goals WHERE userId = :userId ORDER BY updatedAtMs DESC")
    fun observeLocalStudyGoals(userId: String): Flow<List<LocalStudyGoalEntity>>

    @Query("SELECT * FROM local_study_goals WHERE userId = :userId")
    suspend fun getLocalStudyGoals(userId: String): List<LocalStudyGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalStudyGoal(goal: LocalStudyGoalEntity)

    @Query("DELETE FROM local_study_goals WHERE id = :goalId")
    suspend fun deleteLocalStudyGoal(goalId: String)

    // --- Local Badges ---
    @Query("SELECT * FROM local_badge_progress WHERE userId = :userId")
    fun observeLocalBadges(userId: String): Flow<List<LocalBadgeProgressEntity>>

    @Query("SELECT * FROM local_badge_progress WHERE userId = :userId")
    suspend fun getLocalBadges(userId: String): List<LocalBadgeProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalBadge(badge: LocalBadgeProgressEntity)

    // --- Local XP Ledger ---
    @Query("SELECT * FROM local_xp_ledger WHERE userId = :userId ORDER BY createdAtMs DESC LIMIT 100")
    fun observeLocalXpLedger(userId: String): Flow<List<LocalXpLedgerEntity>>

    @Query("SELECT * FROM local_xp_ledger WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getLocalXpEntry(key: String): LocalXpLedgerEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocalXpEntry(entry: LocalXpLedgerEntity)
}
