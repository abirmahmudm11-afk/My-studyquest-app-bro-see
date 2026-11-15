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

data class LocalUserPreferences(
    val hasChosenInitialLanguage: Boolean = false,
    val uiLanguage: AppLanguage = AppLanguage.BANGLA,
    val questionLanguage: AppLanguage = AppLanguage.BANGLA,
    val darkMode: Boolean = false,
    val reducedMotion: Boolean = false,
    val soundEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
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
    val guestUnlockedCosmeticsCsv: String = "owl_classic",
    val guestHasPendingMerge: Boolean = false
)

class StudyQuestPreferences(private val context: Context) {

    private object Keys {
        val HAS_CHOSEN_LANG = booleanPreferencesKey("has_chosen_initial_language")
        val UI_LANGUAGE = stringPreferencesKey("ui_language")
        val QUESTION_LANGUAGE = stringPreferencesKey("question_language")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val QUIET_HOURS = booleanPreferencesKey("quiet_hours_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
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
        val GUEST_UNLOCKED_COSMETICS_CSV = stringPreferencesKey("guest_unlocked_cosmetics_csv")
        val GUEST_HAS_PENDING_MERGE = booleanPreferencesKey("guest_has_pending_merge")
    }

    val preferencesFlow: Flow<LocalUserPreferences> = context.studyQuestDataStore.data.map { prefs ->
        LocalUserPreferences(
            hasChosenInitialLanguage = prefs[Keys.HAS_CHOSEN_LANG] ?: false,
            uiLanguage = AppLanguage.fromCode(prefs[Keys.UI_LANGUAGE] ?: "bn"),
            questionLanguage = AppLanguage.fromCode(prefs[Keys.QUESTION_LANGUAGE] ?: "bn"),
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            quietHoursEnabled = prefs[Keys.QUIET_HOURS] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
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
            guestUnlockedCosmeticsCsv = prefs[Keys.GUEST_UNLOCKED_COSMETICS_CSV] ?: "owl_classic",
            guestHasPendingMerge = prefs[Keys.GUEST_HAS_PENDING_MERGE] ?: false
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

    suspend fun setDarkMode(enabled: Boolean) {
        context.studyQuestDataStore.edit { prefs -> prefs[Keys.DARK_MODE] = enabled }
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
