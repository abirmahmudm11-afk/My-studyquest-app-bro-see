package com.example.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.studyQuestDataStore: DataStore<Preferences> by preferencesDataStore(name = "studyquest_prefs")

enum class AppLanguage(val code: String, val labelEn: String, val labelBn: String) {
    ENGLISH("en", "English", "ইংরেজি"),
    BANGLA("bn", "Bangla", "বাংলা");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            if (code == "bn") BANGLA else ENGLISH
    }
}

enum class ThemeMode(val code: String, val labelEn: String, val labelBn: String) {
    LIGHT("light", "Crisp White (Default)", "পরিষ্কার সাদা (ডিফল্ট)"),
    DARK("dark", "Midnight Dark", "ডার্ক থিম"),
    SYSTEM("system", "System Default", "সিস্টেম থিম");

    companion object {
        fun fromCode(code: String?): ThemeMode = when (code) {
            "dark" -> DARK
            "system" -> SYSTEM
            else -> LIGHT
        }
    }
}

enum class SurfaceStyle(val code: String, val labelEn: String, val labelBn: String) {
    NORMAL("normal", "Vivid Classic", "ভিভিড ক্লাসিক"),
    GLASSMORPHISM("glassmorphism", "Glassmorphism", "গ্লাসমরফিজম");

    companion object {
        fun fromCode(code: String?): SurfaceStyle =
            if (code == "glassmorphism") GLASSMORPHISM else NORMAL
    }
}

data class PersistedFocusTimerState(
    val active: Boolean = false,
    val sessionId: String = "",
    val subjectId: String = "math",
    val mode: String = "pomodoro",
    val phase: String = "focus", // "focus" or "break"
    val isRunning: Boolean = false,
    val autoBreak: Boolean = true,
    val targetFocusSeconds: Int = 25 * 60,
    val targetBreakSeconds: Int = 5 * 60,
    val accumulatedFocusMs: Long = 0L,
    val accumulatedPausedMs: Long = 0L,
    val accumulatedBreakMs: Long = 0L,
    val sessionStartEpochMs: Long = 0L,
    val lastResumeEpochMs: Long = 0L,
    val lastPauseEpochMs: Long = 0L,
    val phaseStartEpochMs: Long = 0L
)

data class LocalUserPreferences(
    val hasChosenInitialLanguage: Boolean = false,
    val welcomePlanCompleted: Boolean = false,
    val uiLanguage: AppLanguage = AppLanguage.BANGLA,
    val questionLanguage: AppLanguage = AppLanguage.BANGLA,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val darkMode: Boolean = false,
    val surfaceStyle: SurfaceStyle = SurfaceStyle.NORMAL,
    val reducedMotion: Boolean = false,
    val soundEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notifStudyEnabled: Boolean = true,
    val notifXpEnabled: Boolean = true,
    val notifBadgeEnabled: Boolean = true,
    val notifAssignmentEnabled: Boolean = true,
    val notifCompetitionEnabled: Boolean = true,
    val notifExamEnabled: Boolean = true,
    val notifCommunityEnabled: Boolean = true,
    val focusMusicVolume: Float = 0.65f,
    val musicDuringBreak: Boolean = true,
    val dailyGoalXp: Int = 60,
    val weeklyGoalMinutes: Int = 180,
    // Guest Mode State
    val isGuestMode: Boolean = false,
    val guestOnboardingCompleted: Boolean = false,
    val guestDisplayName: String = "Guest Scholar",
    val guestClassId: String = "class_10",
    val guestCurriculumVersion: String = "bangla",
    val guestAvatarId: String = "owl_classic",
    val guestFrameId: String = "",
    val guestTitleBadge: String = "",
    val guestSelectedSubjectsCsv: String = "math,english,bangla,physics,ict",
    val guestXp: Long = 0L,
    val guestCoins: Long = 50L,
    val guestRating: Long = 1000L,
    val guestStreakCount: Long = 0L,
    val guestLastChallengeDateKey: String = "",
    val guestUnlockedCosmeticsCsv: String = "owl_classic",
    val guestHasPendingMerge: Boolean = false,
    // Persisted Focus Timer State
    val focusTimer: PersistedFocusTimerState = PersistedFocusTimerState()
)

class StudyQuestPreferences(private val context: Context) {

    private object Keys {
        val HAS_CHOSEN_LANG = booleanPreferencesKey("has_chosen_initial_language")
        val WELCOME_PLAN_COMPLETED = booleanPreferencesKey("welcome_plan_completed")
        val UI_LANGUAGE = stringPreferencesKey("ui_language")
        val QUESTION_LANGUAGE = stringPreferencesKey("question_language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SURFACE_STYLE = stringPreferencesKey("surface_style")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val QUIET_HOURS = booleanPreferencesKey("quiet_hours_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIF_STUDY = booleanPreferencesKey("notif_study_enabled")
        val NOTIF_XP = booleanPreferencesKey("notif_xp_enabled")
        val NOTIF_BADGE = booleanPreferencesKey("notif_badge_enabled")
        val NOTIF_ASSIGNMENT = booleanPreferencesKey("notif_assignment_enabled")
        val NOTIF_COMPETITION = booleanPreferencesKey("notif_competition_enabled")
        val NOTIF_EXAM = booleanPreferencesKey("notif_exam_enabled")
        val NOTIF_COMMUNITY = booleanPreferencesKey("notif_community_enabled")
        val FOCUS_MUSIC_VOLUME = floatPreferencesKey("focus_music_volume")
        val MUSIC_DURING_BREAK = booleanPreferencesKey("music_during_break")
        val DAILY_GOAL_XP = intPreferencesKey("daily_goal_xp")
        val WEEKLY_GOAL_MINUTES = intPreferencesKey("weekly_goal_minutes")

        // Guest Mode Keys
        val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val GUEST_ONBOARDING_DONE = booleanPreferencesKey("guest_onboarding_completed")
        val GUEST_DISPLAY_NAME = stringPreferencesKey("guest_display_name")
        val GUEST_CLASS_ID = stringPreferencesKey("guest_class_id")
        val GUEST_CURRICULUM_VERSION = stringPreferencesKey("guest_curriculum_version")
        val GUEST_AVATAR_ID = stringPreferencesKey("guest_avatar_id")
        val GUEST_FRAME_ID = stringPreferencesKey("guest_frame_id")
        val GUEST_TITLE_BADGE = stringPreferencesKey("guest_title_badge")
        val GUEST_SUBJECTS_CSV = stringPreferencesKey("guest_subjects_csv")
        val GUEST_XP = longPreferencesKey("guest_xp")
        val GUEST_COINS = longPreferencesKey("guest_coins")
        val GUEST_RATING = longPreferencesKey("guest_rating")
        val GUEST_STREAK_COUNT = longPreferencesKey("guest_streak_count")
        val GUEST_LAST_STREAK_DAY = stringPreferencesKey("guest_last_streak_day")
        val GUEST_UNLOCKED_COSMETICS_CSV = stringPreferencesKey("guest_unlocked_cosmetics_csv")
        val GUEST_HAS_PENDING_MERGE = booleanPreferencesKey("guest_has_pending_merge")

        // Persisted Focus Timer Keys
        val TIMER_ACTIVE = booleanPreferencesKey("timer_active")
        val TIMER_SESSION_ID = stringPreferencesKey("timer_session_id")
        val TIMER_SUBJECT_ID = stringPreferencesKey("timer_subject_id")
        val TIMER_MODE = stringPreferencesKey("timer_mode")
        val TIMER_PHASE = stringPreferencesKey("timer_phase")
        val TIMER_RUNNING = booleanPreferencesKey("timer_running")
        val TIMER_AUTO_BREAK = booleanPreferencesKey("timer_auto_break")
        val TIMER_TARGET_FOCUS_SEC = intPreferencesKey("timer_target_focus_sec")
        val TIMER_TARGET_BREAK_SEC = intPreferencesKey("timer_target_break_sec")
        val TIMER_ACCUM_FOCUS_MS = longPreferencesKey("timer_accum_focus_ms")
        val TIMER_ACCUM_PAUSED_MS = longPreferencesKey("timer_accum_paused_ms")
        val TIMER_ACCUM_BREAK_MS = longPreferencesKey("timer_accum_break_ms")
        val TIMER_SESSION_START_MS = longPreferencesKey("timer_session_start_ms")
        val TIMER_LAST_RESUME_MS = longPreferencesKey("timer_last_resume_ms")
        val TIMER_LAST_PAUSE_MS = longPreferencesKey("timer_last_pause_ms")
        val TIMER_PHASE_START_MS = longPreferencesKey("timer_phase_start_ms")
    }

    val preferencesFlow: Flow<LocalUserPreferences> = context.studyQuestDataStore.data.map { prefs ->
        val rawThemeMode = prefs[Keys.THEME_MODE]
        val legacyDark = prefs[Keys.DARK_MODE]
        val resolvedThemeMode = when {
            rawThemeMode != null -> ThemeMode.fromCode(rawThemeMode)
            legacyDark == true -> ThemeMode.DARK
            else -> ThemeMode.LIGHT
        }
        LocalUserPreferences(
            hasChosenInitialLanguage = prefs[Keys.HAS_CHOSEN_LANG] ?: false,
            welcomePlanCompleted = prefs[Keys.WELCOME_PLAN_COMPLETED] ?: false,
            uiLanguage = AppLanguage.fromCode(prefs[Keys.UI_LANGUAGE] ?: "bn"),
            questionLanguage = AppLanguage.fromCode(prefs[Keys.QUESTION_LANGUAGE] ?: "bn"),
            themeMode = resolvedThemeMode,
            darkMode = resolvedThemeMode == ThemeMode.DARK,
            surfaceStyle = SurfaceStyle.fromCode(prefs[Keys.SURFACE_STYLE]),
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            quietHoursEnabled = prefs[Keys.QUIET_HOURS] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            notifStudyEnabled = prefs[Keys.NOTIF_STUDY] ?: true,
            notifXpEnabled = prefs[Keys.NOTIF_XP] ?: true,
            notifBadgeEnabled = prefs[Keys.NOTIF_BADGE] ?: true,
            notifAssignmentEnabled = prefs[Keys.NOTIF_ASSIGNMENT] ?: true,
            notifCompetitionEnabled = prefs[Keys.NOTIF_COMPETITION] ?: true,
            notifExamEnabled = prefs[Keys.NOTIF_EXAM] ?: true,
            notifCommunityEnabled = prefs[Keys.NOTIF_COMMUNITY] ?: true,
            focusMusicVolume = (prefs[Keys.FOCUS_MUSIC_VOLUME] ?: 0.65f).coerceIn(0f, 1f),
            musicDuringBreak = prefs[Keys.MUSIC_DURING_BREAK] ?: true,
            dailyGoalXp = prefs[Keys.DAILY_GOAL_XP] ?: 60,
            weeklyGoalMinutes = prefs[Keys.WEEKLY_GOAL_MINUTES] ?: 180,
            isGuestMode = prefs[Keys.IS_GUEST_MODE] ?: false,
            guestOnboardingCompleted = prefs[Keys.GUEST_ONBOARDING_DONE] ?: false,
            guestDisplayName = prefs[Keys.GUEST_DISPLAY_NAME] ?: "Guest Scholar",
            guestClassId = prefs[Keys.GUEST_CLASS_ID] ?: "class_10",
            guestCurriculumVersion = prefs[Keys.GUEST_CURRICULUM_VERSION] ?: "bangla",
            guestAvatarId = prefs[Keys.GUEST_AVATAR_ID] ?: "owl_classic",
            guestFrameId = prefs[Keys.GUEST_FRAME_ID] ?: "",
            guestTitleBadge = prefs[Keys.GUEST_TITLE_BADGE] ?: "",
            guestSelectedSubjectsCsv = prefs[Keys.GUEST_SUBJECTS_CSV] ?: "math,english,bangla,physics,ict",
            guestXp = prefs[Keys.GUEST_XP] ?: 0L,
            guestCoins = prefs[Keys.GUEST_COINS] ?: 50L,
            guestRating = prefs[Keys.GUEST_RATING] ?: 1000L,
            guestStreakCount = prefs[Keys.GUEST_STREAK_COUNT] ?: 0L,
            guestLastChallengeDateKey = prefs[Keys.GUEST_LAST_STREAK_DAY] ?: "",
            guestUnlockedCosmeticsCsv = prefs[Keys.GUEST_UNLOCKED_COSMETICS_CSV] ?: "owl_classic",
            guestHasPendingMerge = prefs[Keys.GUEST_HAS_PENDING_MERGE] ?: false,
            focusTimer = PersistedFocusTimerState(
                active = prefs[Keys.TIMER_ACTIVE] ?: false,
                sessionId = prefs[Keys.TIMER_SESSION_ID] ?: "",
                subjectId = prefs[Keys.TIMER_SUBJECT_ID] ?: "math",
                mode = prefs[Keys.TIMER_MODE] ?: "pomodoro",
                phase = prefs[Keys.TIMER_PHASE] ?: "focus",
                isRunning = prefs[Keys.TIMER_RUNNING] ?: false,
                autoBreak = prefs[Keys.TIMER_AUTO_BREAK] ?: true,
                targetFocusSeconds = prefs[Keys.TIMER_TARGET_FOCUS_SEC] ?: (25 * 60),
                targetBreakSeconds = prefs[Keys.TIMER_TARGET_BREAK_SEC] ?: (5 * 60),
                accumulatedFocusMs = prefs[Keys.TIMER_ACCUM_FOCUS_MS] ?: 0L,
                accumulatedPausedMs = prefs[Keys.TIMER_ACCUM_PAUSED_MS] ?: 0L,
                accumulatedBreakMs = prefs[Keys.TIMER_ACCUM_BREAK_MS] ?: 0L,
                sessionStartEpochMs = prefs[Keys.TIMER_SESSION_START_MS] ?: 0L,
                lastResumeEpochMs = prefs[Keys.TIMER_LAST_RESUME_MS] ?: 0L,
                lastPauseEpochMs = prefs[Keys.TIMER_LAST_PAUSE_MS] ?: 0L,
                phaseStartEpochMs = prefs[Keys.TIMER_PHASE_START_MS] ?: 0L
            )
        )
    }

    suspend fun snapshot(): LocalUserPreferences = preferencesFlow.first()

    suspend fun setInitialLanguage(language: AppLanguage) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.HAS_CHOSEN_LANG] = true
            prefs[Keys.UI_LANGUAGE] = language.code
            prefs[Keys.QUESTION_LANGUAGE] = language.code
        }
    }

    suspend fun resetInitialLanguageSelection() {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.HAS_CHOSEN_LANG] = false
        }
    }

    suspend fun setWelcomePlanCompleted(completed: Boolean) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.WELCOME_PLAN_COMPLETED] = completed
        }
    }

    suspend fun setUiLanguage(language: AppLanguage) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.UI_LANGUAGE] = language.code
        }
    }

    suspend fun setQuestionLanguage(language: AppLanguage) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.QUESTION_LANGUAGE] = language.code
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.code
            prefs[Keys.DARK_MODE] = (mode == ThemeMode.DARK)
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    suspend fun setSurfaceStyle(style: SurfaceStyle) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.SURFACE_STYLE] = style.code
        }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.studyQuestDataStore.edit { prefs -> prefs[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.studyQuestDataStore.edit { prefs -> prefs[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setQuietHoursEnabled(enabled: Boolean) {
        context.studyQuestDataStore.edit { prefs -> prefs[Keys.QUIET_HOURS] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.studyQuestDataStore.edit { prefs -> prefs[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationCategoryEnabled(category: String, enabled: Boolean) {
        context.studyQuestDataStore.edit { prefs ->
            when (category) {
                "study" -> prefs[Keys.NOTIF_STUDY] = enabled
                "xp" -> prefs[Keys.NOTIF_XP] = enabled
                "badge" -> prefs[Keys.NOTIF_BADGE] = enabled
                "assignment" -> prefs[Keys.NOTIF_ASSIGNMENT] = enabled
                "competition" -> prefs[Keys.NOTIF_COMPETITION] = enabled
                "exam" -> prefs[Keys.NOTIF_EXAM] = enabled
                "community" -> prefs[Keys.NOTIF_COMMUNITY] = enabled
            }
        }
    }

    suspend fun setMusicPreferences(volume: Float, musicDuringBreak: Boolean) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.FOCUS_MUSIC_VOLUME] = volume.coerceIn(0f, 1f)
            prefs[Keys.MUSIC_DURING_BREAK] = musicDuringBreak
        }
    }

    suspend fun saveFocusTimerState(state: PersistedFocusTimerState) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.TIMER_ACTIVE] = state.active
            prefs[Keys.TIMER_SESSION_ID] = state.sessionId
            prefs[Keys.TIMER_SUBJECT_ID] = state.subjectId
            prefs[Keys.TIMER_MODE] = state.mode
            prefs[Keys.TIMER_PHASE] = state.phase
            prefs[Keys.TIMER_RUNNING] = state.isRunning
            prefs[Keys.TIMER_AUTO_BREAK] = state.autoBreak
            prefs[Keys.TIMER_TARGET_FOCUS_SEC] = state.targetFocusSeconds
            prefs[Keys.TIMER_TARGET_BREAK_SEC] = state.targetBreakSeconds
            prefs[Keys.TIMER_ACCUM_FOCUS_MS] = state.accumulatedFocusMs
            prefs[Keys.TIMER_ACCUM_PAUSED_MS] = state.accumulatedPausedMs
            prefs[Keys.TIMER_ACCUM_BREAK_MS] = state.accumulatedBreakMs
            prefs[Keys.TIMER_SESSION_START_MS] = state.sessionStartEpochMs
            prefs[Keys.TIMER_LAST_RESUME_MS] = state.lastResumeEpochMs
            prefs[Keys.TIMER_LAST_PAUSE_MS] = state.lastPauseEpochMs
            prefs[Keys.TIMER_PHASE_START_MS] = state.phaseStartEpochMs
        }
    }

    suspend fun clearFocusTimerState() {
        saveFocusTimerState(PersistedFocusTimerState())
    }

    suspend fun updateGoals(dailyXp: Int, weeklyMinutes: Int) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.DAILY_GOAL_XP] = dailyXp
            prefs[Keys.WEEKLY_GOAL_MINUTES] = weeklyMinutes
        }
    }

    suspend fun enterGuestMode() {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.IS_GUEST_MODE] = true
        }
    }

    suspend fun exitGuestMode() {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.IS_GUEST_MODE] = false
        }
    }

    suspend fun completeGuestQuickSetup(
        displayName: String,
        classId: String,
        curriculumVersion: String,
        selectedSubjects: List<String>,
        avatarId: String
    ) {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.IS_GUEST_MODE] = true
            prefs[Keys.GUEST_ONBOARDING_DONE] = true
            prefs[Keys.GUEST_DISPLAY_NAME] = displayName.trim().ifBlank { "Guest Scholar" }.take(50)
            prefs[Keys.GUEST_CLASS_ID] = classId
            prefs[Keys.GUEST_CURRICULUM_VERSION] = curriculumVersion
            prefs[Keys.GUEST_SUBJECTS_CSV] = selectedSubjects.ifEmpty { listOf("math", "english", "bangla") }.joinToString(",")
            prefs[Keys.GUEST_AVATAR_ID] = avatarId.ifBlank { "owl_classic" }
            prefs[Keys.GUEST_HAS_PENDING_MERGE] = true
            if (prefs[Keys.GUEST_COINS] == null) {
                prefs[Keys.GUEST_COINS] = 50L
            }
        }
    }

    suspend fun addGuestReward(xpDelta: Long, coinDelta: Long, ratingDelta: Long = 0L) {
        context.studyQuestDataStore.edit { prefs ->
            val curXp = prefs[Keys.GUEST_XP] ?: 0L
            val curCoins = prefs[Keys.GUEST_COINS] ?: 50L
            val curRating = prefs[Keys.GUEST_RATING] ?: 1000L
            prefs[Keys.GUEST_XP] = (curXp + xpDelta).coerceAtLeast(0L)
            prefs[Keys.GUEST_COINS] = (curCoins + coinDelta).coerceAtLeast(0L)
            prefs[Keys.GUEST_RATING] = (curRating + ratingDelta).coerceIn(0L, 10000L)
            prefs[Keys.GUEST_HAS_PENDING_MERGE] = true
        }
    }

    suspend fun recordGuestStreakCompletion(todayKey: String, yesterdayKey: String) {
        context.studyQuestDataStore.edit { prefs ->
            val last = prefs[Keys.GUEST_LAST_STREAK_DAY] ?: ""
            if (last != todayKey) {
                val cur = prefs[Keys.GUEST_STREAK_COUNT] ?: 0L
                val next = if (last == yesterdayKey) cur + 1L else 1L
                prefs[Keys.GUEST_STREAK_COUNT] = next
                prefs[Keys.GUEST_LAST_STREAK_DAY] = todayKey
                prefs[Keys.GUEST_HAS_PENDING_MERGE] = true
            }
        }
    }

    suspend fun purchaseGuestCosmetic(cosmeticId: String, costCoins: Int): Boolean {
        var success = false
        context.studyQuestDataStore.edit { prefs ->
            val unlocked = (prefs[Keys.GUEST_UNLOCKED_COSMETICS_CSV] ?: "owl_classic")
                .split(",").filter { it.isNotBlank() }.toMutableSet()
            if (cosmeticId in unlocked) {
                success = true
                return@edit
            }
            val curCoins = prefs[Keys.GUEST_COINS] ?: 50L
            if (curCoins >= costCoins) {
                prefs[Keys.GUEST_COINS] = curCoins - costCoins
                unlocked.add(cosmeticId)
                prefs[Keys.GUEST_UNLOCKED_COSMETICS_CSV] = unlocked.joinToString(",")
                prefs[Keys.GUEST_HAS_PENDING_MERGE] = true
                success = true
            }
        }
        return success
    }

    suspend fun updateGuestCustomization(
        displayName: String? = null,
        avatarId: String? = null,
        frameId: String? = null,
        titleBadge: String? = null,
        classId: String? = null,
        curriculumVersion: String? = null
    ) {
        context.studyQuestDataStore.edit { prefs ->
            displayName?.let { prefs[Keys.GUEST_DISPLAY_NAME] = it.trim().ifBlank { "Guest Scholar" }.take(50) }
            avatarId?.let { prefs[Keys.GUEST_AVATAR_ID] = it }
            frameId?.let { prefs[Keys.GUEST_FRAME_ID] = it }
            titleBadge?.let { prefs[Keys.GUEST_TITLE_BADGE] = it }
            classId?.let { prefs[Keys.GUEST_CLASS_ID] = it }
            curriculumVersion?.let { prefs[Keys.GUEST_CURRICULUM_VERSION] = it }
            prefs[Keys.GUEST_HAS_PENDING_MERGE] = true
        }
    }

    suspend fun clearGuestDataAfterMerge() {
        context.studyQuestDataStore.edit { prefs ->
            prefs[Keys.IS_GUEST_MODE] = false
            prefs[Keys.GUEST_ONBOARDING_DONE] = false
            prefs[Keys.GUEST_HAS_PENDING_MERGE] = false
            prefs[Keys.GUEST_XP] = 0L
            prefs[Keys.GUEST_COINS] = 50L
            prefs[Keys.GUEST_RATING] = 1000L
            prefs[Keys.GUEST_STREAK_COUNT] = 0L
            prefs[Keys.GUEST_LAST_STREAK_DAY] = ""
            prefs[Keys.GUEST_UNLOCKED_COSMETICS_CSV] = "owl_classic"
        }
    }
}

class ConnectivityObserver(context: Context) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observeOnline(): Flow<Boolean> = callbackFlow {
        fun checkCurrent(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        trySend(checkCurrent())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(checkCurrent())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
