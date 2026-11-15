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
    val academicGroup: String = "general",
    val dailyGoalMinutes: Long = 30L,
    val streakCount: Long = 0L,
    val lastChallengeDateKey: String? = null,
    val classLockedAcademicYear: Long? = null,
    val annualExamStatus: String? = null, // "pending", "passed", "failed"
    val promotedFromClassId: String? = null,
    val streakHistoryKeys: List<String> = emptyList(),
    val streakFreezeCount: Long = 1L,
    val featuredBadgeId: String? = null,
    val pinnedBadgeIds: List<String> = emptyList(),
    val unlockedTrophyTitles: List<String> = emptyList(),
    val totalFocusMinutes: Long = 0L,
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
    val recordType: String = "practice",
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
    val description: String? = null,
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
    val planType: String = "daily",
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
data class UserBadgeDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val badgeId: String = "",
    val category: String = "study",
    val rarity: String = "common",
    val progress: Long = 0L,
    val target: Long = 1L,
    val unlocked: Boolean = false,
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val animationShown: Boolean = false,
    @ServerTimestamp val unlockedAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BadgeDefinitionDoc(
    @DocumentId val id: String = "",
    val badgeId: String = "",
    val category: String = "study",
    val rarity: String = "common",
    val titleEn: String = "",
    val titleBn: String = "",
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    val metricKey: String = "",
    val target: Long = 1L,
    val xpBonus: Long = 25L,
    val enabled: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudySessionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val chapterId: String? = null,
    val mode: String = "pomodoro",
    val startedAtEpochMs: Long = 0L,
    val endedAtEpochMs: Long = 0L,
    val focusedSeconds: Long = 0L,
    val pausedSeconds: Long = 0L,
    val breakSeconds: Long = 0L,
    val xpAwarded: Long = 0L,
    val status: String = "completed",
    val idempotencyKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudyGoalDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val goalType: String = "time_minutes",
    val periodType: String = "daily",
    val subjectId: String = "all",
    val targetValue: Long = 30L,
    val currentValue: Long = 0L,
    val difficulty: String = "medium",
    val xpReward: Long = 45L,
    val completed: Boolean = false,
    val rewardClaimed: Boolean = false,
    val deadlineEpochMs: Long = 0L,
    val periodKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AssignmentDefinitionDoc(
    @DocumentId val id: String = "",
    val titleEn: String = "",
    val titleBn: String = "",
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    val assignmentType: String = "daily",
    val classId: String = "all",
    val subjectId: String = "math",
    val chapterId: String? = null,
    val difficulty: String = "medium",
    val targetCount: Long = 1L,
    val durationLimitMinutes: Long = 0L,
    val xpReward: Long = 50L,
    val coinReward: Long = 20L,
    val active: Boolean = true,
    val periodKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AssignmentSubmissionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val assignmentId: String = "",
    val periodKey: String = "",
    val progress: Long = 0L,
    val target: Long = 1L,
    val completed: Boolean = false,
    val score: Double = 0.0,
    val xpAwarded: Long = 0L,
    @ServerTimestamp val submittedAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AcademicContentDoc(
    @DocumentId val id: String = "",
    val contentType: String = "question",
    val classId: String = "class_10",
    val curriculumVersion: String = "bangla",
    val subjectId: String = "math",
    val chapterId: String? = null,
    val topicId: String? = null,
    val titleEn: String = "",
    val titleBn: String = "",
    val status: String = "draft",
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
    val type: String = "direct",
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
    val biweekKey: String = "2026-BW23",
    val monthKey: String = "2026-11",
    val weeklyPoints: Long = 0L,
    val seasonalPoints: Long = 0L,
    val weeklyStudyMinutes: Long = 0L,
    val weeklyXp: Long = 0L,
    val weeklyExamScore: Long = 0L,
    val weeklyAssignmentScore: Long = 0L,
    val weeklyChallengeScore: Long = 0L,
    val biweeklyStudyMinutes: Long = 0L,
    val biweeklyXp: Long = 0L,
    val biweeklyExamScore: Long = 0L,
    val biweeklyAssignmentScore: Long = 0L,
    val biweeklyChallengeScore: Long = 0L,
    val monthlyStudyMinutes: Long = 0L,
    val monthlyXp: Long = 0L,
    val monthlyExamScore: Long = 0L,
    val monthlyAssignmentScore: Long = 0L,
    val monthlyChallengeScore: Long = 0L,
    val competitionRating: Long = 1000L,
    val rankTier: String = "Bronze",
    val optInNational: Boolean = false,
    val matchesPlayed: Long = 0L,
    val matchesWon: Long = 0L,
    val streakCount: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CompetitionArchiveDoc(
    @DocumentId val id: String = "",
    val periodKey: String = "",
    val periodType: String = "weekly",
    val category: String = "xp",
    val classId: String = "all",
    val winners: List<Map<String, Any>> = emptyList(),
    @ServerTimestamp val archivedAt: Timestamp? = null
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
data class CommunityPostDoc(
    @DocumentId val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorAvatarId: String = "owl_classic",
    val authorClassId: String = "class_10",
    val classId: String = "class_10",
    val subjectId: String = "math",
    val postType: String = "question",
    val title: String = "",
    val body: String = "",
    val upvoterUids: List<String> = emptyList(),
    val commentCount: Long = 0L,
    val flagged: Boolean = false,
    val hidden: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CommunityCommentDoc(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val classId: String = "class_10",
    val body: String = "",
    val upvoterUids: List<String> = emptyList(),
    val isHelpful: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AiVideoJobDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sourceType: String = "chapter",
    val sourceTitle: String = "",
    val classId: String = "class_10",
    val subjectId: String = "math",
    val chapterId: String? = null,
    val status: String = "queued",
    val progressPercent: Long = 0L,
    val errorMessage: String? = null,
    val videoId: String? = null,
    val externalRenderStatus: String = "unconfigured",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LearningVideoDoc(
    @DocumentId val id: String = "",
    val ownerUid: String = "",
    val title: String = "",
    val classId: String = "class_10",
    val subjectId: String = "math",
    val chapterId: String? = null,
    val durationSeconds: Long = 60L,
    val summary: String = "",
    val scenesJson: String = "[]",
    val quizJson: String = "[]",
    val visibility: String = "private",
    val savedByUids: List<String> = emptyList(),
    val lastWatchedScene: Long = 0L,
    val externalRenderStatus: String = "unconfigured",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserSubscriptionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tier: String = "free",
    val billingProvider: String = "google_play_unconfigured",
    val adEnabled: Boolean = true,
    val expiresAtEpochMs: Long = 0L,
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
    val xpRulesJson: String? = null,
    val billingConfigured: Boolean = false,
    val adMobConfigured: Boolean = false,
    val externalVideoRendererConfigured: Boolean = false,
    val updatedByUid: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
