package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PublicProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studyQuestId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarId: String = "owl_classic",
    val frameId: String? = null,
    val titleBadge: String? = null,
    val classId: String = "class_10",
    val curriculumVersion: String = "bangla",
    val overallLevel: Long = 1L,
    val levelTitle: String = "Beginner",
    val totalXp: Long = 0L,
    val coins: Long = 50L,
    val competitionRating: Long = 1000L,
    val rankTier: String = "Bronze",
    val optInNationalLeaderboard: Boolean = false,
    val showOnlineStatus: Boolean = true,
    val isOnline: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val selectedSubjects: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PrivateAccountInfo(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val ageBracket: String = "13_17",
    val guardianConsentStatus: String = "acknowledged",
    val accountStatus: String = "active",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LearningRecordDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val recordType: String = "practice", // lesson, practice, exam, bookmark, revision
    val refId: String = "",
    val subjectId: String = "",
    val chapterId: String? = null,
    val topicId: String? = null,
    val score: Double = 0.0,
    val totalMarks: Double = 0.0,
    val accuracy: Double = 0.0,
    val xpAwarded: Long = 0L,
    val idempotencyKey: String = "",
    val summary: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class XpTransactionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sourceType: String = "",
    val sourceId: String = "",
    val xpDelta: Long = 0L,
    val coinDelta: Long = 0L,
    val idempotencyKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserQuestDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val questId: String = "",
    val questType: String = "daily",
    val progress: Long = 0L,
    val target: Long = 1L,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val xpReward: Long = 30L,
    val coinReward: Long = 15L,
    val periodKey: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudyPlanDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subjectId: String = "",
    val planType: String = "daily", // daily, weekly, exam_countdown, chapter_goal
    val targetDateEpochMs: Long = 0L,
    val minutesPerDay: Long = 30L,
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserNotificationDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "system",
    val title: String = "",
    val body: String = "",
    val read: Boolean = false,
    val actionRoute: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AcademicContentDoc(
    @DocumentId val id: String = "",
    val contentType: String = "question", // lesson, question, exam_template
    val classId: String = "class_10",
    val curriculumVersion: String = "bangla",
    val subjectId: String = "math",
    val chapterId: String? = null,
    val topicId: String? = null,
    val titleEn: String = "",
    val titleBn: String = "",
    val status: String = "draft", // draft, source_verification, academic_review, language_review, approved, published
    val authorUid: String = "",
    val sourceAttribution: String? = null,
    val version: Long = 1L,
    val payloadJson: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FriendRequestDoc(
    @DocumentId val id: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val fromDisplayName: String = "",
    val fromStudyQuestId: String? = null,
    val toDisplayName: String = "",
    val toStudyQuestId: String? = null,
    val status: String = "pending",
    val pairKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FriendshipDoc(
    @DocumentId val id: String = "",
    val memberUids: List<String> = emptyList(),
    val pairKey: String = "",
    val mutedByUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BlockedUserDoc(
    @DocumentId val id: String = "",
    val blockerUid: String = "",
    val blockedUid: String = "",
    val pairKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ConversationDoc(
    @DocumentId val id: String = "",
    val type: String = "direct", // direct, group
    val name: String? = null,
    val avatarId: String? = null,
    val ownerId: String = "",
    val memberUids: List<String> = emptyList(),
    val invitedUids: List<String> = emptyList(),
    val mutedByUids: List<String> = emptyList(),
    val lastMessagePreview: String? = null,
    @ServerTimestamp val lastMessageAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessageDoc(
    @DocumentId val id: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val memberUids: List<String> = emptyList(),
    val text: String = "",
    val replyToId: String? = null,
    val replyPreview: String? = null,
    val reactions: Map<String, String> = emptyMap(),
    val quizInviteId: String? = null,
    val deliveryStatus: String = "sent",
    val readByUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MatchRequestDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val classId: String = "class_10",
    val subjectId: String = "math",
    val difficulty: String = "medium",
    val status: String = "waiting",
    val matchId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MatchDoc(
    @DocumentId val id: String = "",
    val mode: String = "quick_battle",
    val roomCode: String? = null,
    val hostUid: String = "",
    val classId: String = "class_10",
    val subjectId: String = "math",
    val difficulty: String = "medium",
    val status: String = "waiting",
    val playerUids: List<String> = emptyList(),
    val invitedUids: List<String> = emptyList(),
    val readyUids: List<String> = emptyList(),
    val questionIds: List<String> = emptyList(),
    val questionCount: Long = 5L,
    val durationSeconds: Long = 120L,
    val scores: Map<String, Long> = emptyMap(),
    val winnerUid: String? = null,
    val startTime: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MatchSubmissionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val matchId: String = "",
    val questionId: String = "",
    val selectedIndex: Long = -1L,
    val isCorrect: Boolean = false,
    val responseTimeMs: Long = 0L,
    val pointsAwarded: Long = 0L,
    @ServerTimestamp val submittedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LeaderboardEntryDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val studyQuestId: String = "",
    val avatarId: String? = null,
    val frameId: String? = null,
    val classId: String = "class_10",
    val subjectId: String? = null,
    val seasonId: String = "season_2026_11",
    val weekKey: String = "2026-W46",
    val weeklyPoints: Long = 0L,
    val seasonalPoints: Long = 0L,
    val competitionRating: Long = 1000L,
    val rankTier: String = "Bronze",
    val optInNational: Boolean = false,
    val matchesPlayed: Long = 0L,
    val matchesWon: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TournamentDoc(
    @DocumentId val id: String = "",
    val titleEn: String = "",
    val titleBn: String = "",
    val classId: String = "class_10",
    val subjectId: String = "math",
    val status: String = "registration",
    val participantUids: List<String> = emptyList(),
    val maxParticipants: Long = 32L,
    val rewardCoins: Long = 150L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ModerationCaseDoc(
    @DocumentId val id: String = "",
    val reporterUid: String = "",
    val targetType: String = "user",
    val targetId: String = "",
    val reason: String = "",
    val details: String? = null,
    val status: String = "open",
    val assignedModeratorUid: String? = null,
    val decisionNote: String? = null,
    val appealText: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AdminUserDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "moderator",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLogDoc(
    @DocumentId val id: String = "",
    val actorUid: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val details: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SystemSettingsDoc(
    @DocumentId val id: String = "",
    val aiSolverEnabled: Boolean = true,
    val currentSeasonId: String = "season_2026_11",
    val seasonNameEn: String = "Season 1: Autumn Scholars",
    val seasonNameBn: String = "মৌসুম ১: শরৎ বিদ্বান",
    val weeklyResetTz: String = "Asia/Dhaka",
    val updatedByUid: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
