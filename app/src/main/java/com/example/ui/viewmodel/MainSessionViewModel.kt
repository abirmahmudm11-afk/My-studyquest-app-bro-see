package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.core.ConnectivityObserver
import com.example.core.LocalUserPreferences
import com.example.core.StudyQuestPreferences
import com.example.core.unwrapFirestoreMessage
import com.example.data.model.PublicProfile
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import com.example.data.repository.AdminStudioRepository
import com.example.data.repository.AuthAndProfileRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthSessionState {
    data object Loading : AuthSessionState
    data object SignedOut : AuthSessionState
    data class Error(val message: String) : AuthSessionState
    data class Authenticated(val user: FirebaseUser) : AuthSessionState
}

class PreAuthViewModel(
    private val preferences: StudyQuestPreferences
) : ViewModel() {

    val localPreferences: StateFlow<LocalUserPreferences> = preferences.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = LocalUserPreferences()
        )

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    fun selectInitialLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            preferences.setInitialLanguage(lang)
        }
    }

    fun setUiLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            preferences.setUiLanguage(lang)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDarkMode(enabled)
        }
    }

    fun enterGuestMode() {
        viewModelScope.launch {
            preferences.enterGuestMode()
        }
    }

    fun exitGuestMode() {
        viewModelScope.launch {
            preferences.exitGuestMode()
        }
    }

    fun completeGuestQuickSetup(
        displayName: String,
        classId: String,
        curriculumVersion: String,
        selectedSubjects: List<String>,
        avatarId: String
    ) {
        viewModelScope.launch {
            preferences.completeGuestQuickSetup(
                displayName = displayName,
                classId = classId,
                curriculumVersion = curriculumVersion,
                selectedSubjects = selectedSubjects,
                avatarId = avatarId
            )
        }
    }

    fun setSigningIn(loading: Boolean) {
        _isSigningIn.value = loading
    }

    fun setAuthError(msg: String?) {
        _authMessage.value = msg
        _isSigningIn.value = false
    }

    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _authMessage.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                Firebase.auth.signInWithCredential(credential).await()
                _isSigningIn.value = false
            } catch (e: CancellationException) {
                _isSigningIn.value = false
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase Google signIn failed", e)
                _isSigningIn.value = false
                _authMessage.value = e.localizedMessage ?: "Google Sign-In failed"
            }
        }
    }

    companion object {
        private const val TAG = "PreAuthVM"
    }
}

class MainSessionViewModel(
    private val currentUser: FirebaseUser,
    private val authRepo: AuthAndProfileRepository,
    private val adminRepo: AdminStudioRepository,
    private val preferences: StudyQuestPreferences,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {

    val localPreferences: StateFlow<LocalUserPreferences> = preferences.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LocalUserPreferences())

    val isOnline: StateFlow<Boolean> = connectivityObserver.observeOnline()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    val profile: StateFlow<PublicProfile?> = authRepo.observePublicProfile(currentUser.uid)
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            Log.w(TAG, "Profile observe error", fsErr ?: error)
            _bannerMessage.value = unwrapFirestoreMessage(error, "Unable to sync profile")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val quests: StateFlow<List<UserQuestDoc>> = authRepo.observeUserQuests(currentUser.uid)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val notifications: StateFlow<List<UserNotificationDoc>> = authRepo.observeNotifications(currentUser.uid)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val unlockedCosmetics: StateFlow<Set<String>> = authRepo.observeUnlockedCosmetics(currentUser.uid)
        .catch { emit(setOf("owl_classic")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), setOf("owl_classic"))

    private val _adminRole = MutableStateFlow<String?>(null)
    val adminRole: StateFlow<String?> = _adminRole.asStateFlow()

    private val _bootstrapping = MutableStateFlow(true)
    val bootstrapping: StateFlow<Boolean> = _bootstrapping.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    private val _exportedJson = MutableStateFlow<String?>(null)
    val exportedJson: StateFlow<String?> = _exportedJson.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = combine(notifications) { (list) ->
        list.count { !it.read }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    private val _guestMergedCount = MutableStateFlow(0)
    val guestMergedCount: StateFlow<Int> = _guestMergedCount.asStateFlow()

    init {
        viewModelScope.launch {
            _bootstrapping.value = true
            val currentLang = localPreferences.value.uiLanguage
            val boot = authRepo.ensureUserBootstrapped(currentUser, currentLang)
            if (boot.isFailure) {
                _bannerMessage.value = boot.exceptionOrNull()?.localizedMessage
            } else {
                val mergeRes = authRepo.mergeGuestProgressToAccount(currentUser.uid, preferences)
                mergeRes.onSuccess { count ->
                    if (count > 0) {
                        _guestMergedCount.value = count
                        _bannerMessage.value = if (currentLang == AppLanguage.BANGLA) {
                            "গেস্ট মোডের অগ্রগতি আপনার গুগল অ্যাকাউন্টে স্বয়ংক্রিয়ভাবে সিঙ্ক হয়েছে!"
                        } else {
                            "Guest progress synced automatically to your Google account!"
                        }
                    }
                }
            }
            _adminRole.value = adminRepo.resolveAdminRole(currentUser)
            _bootstrapping.value = false
        }
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun clearExportedJson() {
        _exportedJson.value = null
    }

    fun completeAcademicSetup(
        displayName: String,
        classId: String,
        curriculumVersion: String,
        selectedSubjects: List<String>,
        avatarId: String
    ) {
        viewModelScope.launch {
            val res = authRepo.completeAcademicSetup(
                userId = currentUser.uid,
                displayName = displayName,
                classId = classId,
                curriculumVersion = curriculumVersion,
                selectedSubjects = selectedSubjects,
                avatarId = avatarId
            )
            if (res.isFailure) {
                _bannerMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Setup failed"
            }
        }
    }

    fun updateProfile(
        displayName: String? = null,
        avatarId: String? = null,
        frameId: String? = null,
        titleBadge: String? = null,
        classId: String? = null,
        curriculumVersion: String? = null,
        optInNational: Boolean? = null,
        showOnlineStatus: Boolean? = null
    ) {
        viewModelScope.launch {
            val res = authRepo.updateProfileCustomization(
                userId = currentUser.uid,
                displayName = displayName,
                avatarId = avatarId,
                frameId = frameId,
                titleBadge = titleBadge,
                classId = classId,
                curriculumVersion = curriculumVersion,
                optInNational = optInNational,
                showOnlineStatus = showOnlineStatus
            )
            if (res.isFailure) {
                _bannerMessage.value = res.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun updateUiLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            preferences.setUiLanguage(lang)
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(uiLanguage = lang))
        }
    }

    fun updateQuestionLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            preferences.setQuestionLanguage(lang)
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(questionLanguage = lang))
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDarkMode(enabled)
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(darkMode = enabled))
        }
    }

    fun updateReducedMotion(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setReducedMotion(enabled)
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(reducedMotion = enabled))
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setSoundEnabled(enabled)
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(soundEnabled = enabled))
        }
    }

    fun updateQuietHours(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setQuietHoursEnabled(enabled)
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(quietHoursEnabled = enabled))
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            authRepo.markNotificationRead(currentUser.uid, id)
        }
    }

    fun claimQuest(quest: UserQuestDoc, onRewarded: (Long, Long) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = authRepo.claimQuestReward(currentUser.uid, quest)
            res.onSuccess { (xp, coins) -> onRewarded(xp, coins) }
            res.onFailure { _bannerMessage.value = it.localizedMessage }
        }
    }

    fun exportMyData() {
        viewModelScope.launch {
            val res = authRepo.exportUserDataJson(currentUser.uid)
            res.onSuccess { _exportedJson.value = it }
            res.onFailure { _bannerMessage.value = it.localizedMessage }
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            val res = authRepo.deleteAccountAndUserData(currentUser.uid)
            res.onSuccess { onComplete() }
            res.onFailure { _bannerMessage.value = it.localizedMessage }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepo.setOnlinePresence(currentUser.uid, false)
            Firebase.auth.signOut()
        }
    }

    companion object {
        private const val TAG = "MainSessionVM"
    }
}
