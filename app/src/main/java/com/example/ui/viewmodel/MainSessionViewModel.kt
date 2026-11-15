package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
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
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

sealed interface AuthSessionState {
    data object Loading : AuthSessionState
    data object SignedOut : AuthSessionState
    data class Error(val message: String) : AuthSessionState
    data class Authenticated(val user: FirebaseUser) : AuthSessionState
}

sealed interface BootstrapState {
    data object Loading : BootstrapState
    data class Ready(val profile: PublicProfile) : BootstrapState
    data class Error(val message: String) : BootstrapState
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

    private var signInJob: Job? = null

    fun setSigningIn(loading: Boolean) {
        _isSigningIn.value = loading
    }

    fun setAuthError(msg: String?) {
        _authMessage.value = msg
        _isSigningIn.value = false
    }

    fun signInWithGoogleIdToken(
        idToken: String,
        onSuccess: () -> Unit = {}
    ) {
        if (idToken.isBlank()) {
            _isSigningIn.value = false
            _authMessage.value = "Google Sign-In did not return a valid ID token. Please try again."
            return
        }
        if (signInJob?.isActive == true) {
            Log.d(TAG, "Ignoring duplicate signInWithGoogleIdToken call while exchange is in progress")
            return
        }
        signInJob = viewModelScope.launch {
            _isSigningIn.value = true
            _authMessage.value = null
            try {
                Log.d(TAG, "Exchanging Google ID token with Firebase Auth")
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                withTimeout(25_000L) {
                    Firebase.auth.signInWithCredential(credential).await()
                }
                Log.d(TAG, "Firebase signInWithCredential succeeded for uid=${Firebase.auth.currentUser?.uid}")
                _isSigningIn.value = false
                onSuccess()
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Firebase Google signIn timed out", e)
                _isSigningIn.value = false
                _authMessage.value = "Google Sign-In timed out while contacting Firebase. Please check your internet connection and try again."
            } catch (e: CancellationException) {
                _isSigningIn.value = false
                throw e
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "FirebaseAuthException during Google signIn [${e.errorCode}]", e)
                _isSigningIn.value = false
                _authMessage.value = "Sign-in failed [${e.errorCode}]: ${e.localizedMessage ?: "Firebase authentication rejected the credential."}"
            } catch (e: Exception) {
                Log.e(TAG, "Firebase Google signIn failed", e)
                _isSigningIn.value = false
                _authMessage.value = e.localizedMessage ?: "Google Sign-In failed. Please try again."
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
    connectivityObserver: ConnectivityObserver,
    private val appContext: Context? = null
) : ViewModel() {

    val localPreferences: StateFlow<LocalUserPreferences> = preferences.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), LocalUserPreferences())

    val isOnline: StateFlow<Boolean> = connectivityObserver.observeOnline()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), true)

    private val _bootstrappedProfile = MutableStateFlow<PublicProfile?>(null)
    private val _bootstrapping = MutableStateFlow(true)
    val bootstrapping: StateFlow<Boolean> = _bootstrapping.asStateFlow()

    private val _bootstrapError = MutableStateFlow<String?>(null)
    private val _isCompletingSetup = MutableStateFlow(false)
    val isCompletingSetup: StateFlow<Boolean> = _isCompletingSetup.asStateFlow()

    private val liveProfileFlow = authRepo.observePublicProfile(currentUser.uid)
        .retryWhen { cause, attempt ->
            val fsErr = generateSequence(cause) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fsErr == null && cause is CancellationException) false
            else if (attempt < 3) {
                delay(1_000L * (attempt + 1))
                true
            } else {
                false
            }
        }
        .catch { error ->
            val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            if (fsErr == null && error is CancellationException) throw error
            Log.w(TAG, "Profile observe error", fsErr ?: error)
            val msg = unwrapFirestoreMessage(error, "Unable to sync profile")
            if (_bootstrappedProfile.value == null) {
                _bootstrapError.value = msg
            } else {
                _bannerMessage.value = msg
            }
            emit(_bootstrappedProfile.value)
        }

    val profile: StateFlow<PublicProfile?> = combine(
        liveProfileFlow,
        _bootstrappedProfile
    ) { live, fallback ->
        live ?: fallback
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val bootstrapState: StateFlow<BootstrapState> = combine(
        _bootstrapping,
        profile,
        _bootstrapError
    ) { loading, activeProfile, error ->
        when {
            activeProfile != null -> BootstrapState.Ready(activeProfile)
            loading -> BootstrapState.Loading
            else -> BootstrapState.Error(
                error ?: "Unable to load your StudyQuest profile. Please check your internet connection and try again."
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), BootstrapState.Loading)

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
        bootstrapSession()
    }

    fun retryBootstrap() {
        bootstrapSession()
    }

    private fun bootstrapSession() {
        viewModelScope.launch {
            _bootstrapping.value = true
            _bootstrapError.value = null
            val currentLang = localPreferences.value.uiLanguage
            val boot = authRepo.ensureUserBootstrapped(currentUser, currentLang)
            boot.onSuccess { loadedProfile ->
                _bootstrappedProfile.value = loadedProfile
                _bootstrapError.value = null
                runCatching {
                    withTimeout(15_000L) {
                        authRepo.mergeGuestProgressToAccount(currentUser.uid, preferences)
                    }
                }.getOrNull()?.onSuccess { count ->
                    if (count > 0) {
                        _guestMergedCount.value = count
                        _bannerMessage.value = if (currentLang == AppLanguage.BANGLA) {
                            "গেস্ট মোডের অগ্রগতি আপনার গুগল অ্যাকাউন্টে স্বয়ংক্রিয়ভাবে সিঙ্ক হয়েছে!"
                        } else {
                            "Guest progress synced automatically to your Google account!"
                        }
                    }
                }
            }.onFailure { err ->
                val msg = unwrapFirestoreMessage(err, err.localizedMessage ?: "Profile bootstrap failed")
                _bootstrapError.value = msg
                _bannerMessage.value = msg
            }
            _adminRole.value = runCatching {
                withTimeout(8_000L) { adminRepo.resolveAdminRole(currentUser) }
            }.getOrNull()
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
        avatarId: String,
        academicGroup: String = "general",
        dailyGoalMinutes: Long = 30L,
        studyGoalTitle: String = ""
    ) {
        viewModelScope.launch {
            _isCompletingSetup.value = true
            val res = authRepo.completeAcademicSetup(
                userId = currentUser.uid,
                displayName = displayName,
                classId = classId,
                curriculumVersion = curriculumVersion,
                selectedSubjects = selectedSubjects,
                avatarId = avatarId,
                academicGroup = academicGroup,
                dailyGoalMinutes = dailyGoalMinutes,
                studyGoalTitle = studyGoalTitle
            )
            res.onSuccess {
                _bootstrappedProfile.value = (_bootstrappedProfile.value ?: profile.value)?.copy(
                    displayName = displayName.trim().ifBlank { "Student" }.take(60),
                    classId = classId,
                    curriculumVersion = curriculumVersion,
                    selectedSubjects = selectedSubjects.take(20),
                    avatarId = avatarId,
                    academicGroup = academicGroup,
                    dailyGoalMinutes = dailyGoalMinutes.coerceIn(5L, 360L),
                    onboardingCompleted = true
                )
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Setup failed"
            }
            _isCompletingSetup.value = false
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
            withContext(NonCancellable) {
                try {
                    authRepo.setOnlinePresence(currentUser.uid, false)
                    appContext?.let { ctx ->
                        CredentialManager.create(ctx).clearCredentialState(ClearCredentialStateRequest())
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear credential state on sign-out: ${e.message}")
                } finally {
                    Firebase.auth.signOut()
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainSessionVM"
    }
}
