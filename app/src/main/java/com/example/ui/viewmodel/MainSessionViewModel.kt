package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AdBannerPlacementState
import com.example.core.AppLanguage
import com.example.core.BillingActionResult
import com.example.core.BillingAndAdProvider
import com.example.core.ConnectivityObserver
import com.example.core.LocalUserPreferences
import com.example.core.StudyActivityMonitor
import com.example.core.StudyQuestPreferences
import com.example.core.SurfaceStyle
import com.example.core.ThemeMode
import com.example.core.unwrapFirestoreMessage
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.CachedBookEntity
import com.example.data.model.AiCreditLedgerDoc
import com.example.data.model.AssignmentDefinitionDoc
import com.example.data.model.AssignmentSubmissionDoc
import com.example.data.model.PublicProfile
import com.example.data.model.PurchaseRecordDoc
import com.example.data.model.StudyGoalDoc
import com.example.data.model.SystemSettingsDoc
import com.example.data.model.TextbookCatalogItem
import com.example.data.model.UserBadgeDoc
import com.example.data.model.UserNotificationDoc
import com.example.data.model.UserQuestDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.model.VideoCreditLedgerDoc
import com.example.data.model.XpTransactionDoc
import com.example.data.repository.AdminStudioRepository
import com.example.data.repository.AuthAndProfileRepository
import com.example.data.repository.GamificationEngine
import com.example.data.repository.TextbookDownloadRepository
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
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
            started = SharingStarted.Eagerly,
            initialValue = LocalUserPreferences()
        )

    private val _languagePickerOverride = MutableStateFlow<Boolean?>(null)
    val shouldShowLanguagePicker: StateFlow<Boolean> = combine(
        localPreferences,
        _languagePickerOverride
    ) { prefs, override ->
        override ?: !prefs.hasChosenInitialLanguage
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = true
    )

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _authStatusMessage = MutableStateFlow<String?>(null)
    val authStatusMessage: StateFlow<String?> = _authStatusMessage.asStateFlow()

    private val _passwordResetStatus = MutableStateFlow<Pair<Boolean, String>?>(null)
    val passwordResetStatus: StateFlow<Pair<Boolean, String>?> = _passwordResetStatus.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _canCancelSignIn = MutableStateFlow(false)
    val canCancelSignIn: StateFlow<Boolean> = _canCancelSignIn.asStateFlow()

    private var cancelTimerJob: Job? = null
    private var signInJob: Job? = null

    fun selectInitialLanguage(lang: AppLanguage) {
        _languagePickerOverride.value = false
        viewModelScope.launch {
            preferences.setInitialLanguage(lang)
        }
    }

    fun returnToLanguagePicker() {
        cancelSignIn(showMessage = false)
        _passwordResetStatus.value = null
        _languagePickerOverride.value = true
        viewModelScope.launch {
            preferences.resetInitialLanguageSelection()
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

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setSurfaceStyle(style: SurfaceStyle) {
        viewModelScope.launch {
            preferences.setSurfaceStyle(style)
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
        avatarId: String,
        academicGroup: String = "general",
        activeBookYear: String = "2026",
        activeBookId: String = ""
    ) {
        viewModelScope.launch {
            preferences.completeGuestQuickSetup(
                displayName = displayName,
                classId = classId,
                curriculumVersion = curriculumVersion,
                selectedSubjects = selectedSubjects,
                avatarId = avatarId,
                academicGroup = academicGroup,
                activeBookYear = activeBookYear,
                activeBookId = activeBookId
            )
        }
    }

    fun clearAuthMessages() {
        _authMessage.value = null
        _passwordResetStatus.value = null
    }

    fun clearPasswordResetStatus() {
        _passwordResetStatus.value = null
    }

    fun setSigningIn(loading: Boolean, statusMessage: String? = null) {
        _isSigningIn.value = loading
        cancelTimerJob?.cancel()
        if (loading) {
            _authMessage.value = null
            _authStatusMessage.value = statusMessage
            _canCancelSignIn.value = false
            cancelTimerJob = viewModelScope.launch {
                delay(CANCEL_REVEAL_DELAY_MS)
                if (_isSigningIn.value) {
                    _canCancelSignIn.value = true
                }
            }
        } else {
            _authStatusMessage.value = null
            _canCancelSignIn.value = false
        }
    }

    fun setAuthError(msg: String?) {
        cancelTimerJob?.cancel()
        _canCancelSignIn.value = false
        _authStatusMessage.value = null
        _authMessage.value = msg
        _isSigningIn.value = false
    }

    fun cancelSignIn(showMessage: Boolean = true) {
        signInJob?.cancel()
        signInJob = null
        cancelTimerJob?.cancel()
        cancelTimerJob = null
        _isSigningIn.value = false
        _canCancelSignIn.value = false
        _authStatusMessage.value = null
        _authMessage.value = if (showMessage) {
            "Sign-in cancelled. Check your connection or try again when ready."
        } else {
            null
        }
    }

    fun signInWithGoogleIdToken(
        idToken: String,
        language: AppLanguage = AppLanguage.ENGLISH,
        onSuccess: () -> Unit = {}
    ) {
        if (idToken.isBlank()) {
            setAuthError("Google Sign-In did not return a valid ID token. Please try again.")
            return
        }
        val existingUser = runCatching { Firebase.auth.currentUser }.getOrNull()
        if (existingUser != null) {
            Log.d(TAG, "Firebase user already signed in (uid=${existingUser.uid}); completing immediately")
            setSigningIn(false)
            onSuccess()
            return
        }
        if (signInJob?.isActive == true) {
            Log.d(TAG, "Ignoring duplicate signInWithGoogleIdToken call while exchange is in progress")
            return
        }
        signInJob = viewModelScope.launch {
            setSigningIn(
                loading = true,
                statusMessage = if (language == AppLanguage.BANGLA) {
                    "গুগল দিয়ে সাইন-ইন হচ্ছে..."
                } else {
                    "Signing in with Google..."
                }
            )
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                executeWithBackgroundRetry(
                    language = language,
                    operationLabelEn = "contacting Firebase",
                    operationLabelBn = "Firebase-এর সাথে সংযোগ"
                ) {
                    if (Firebase.auth.currentUser == null) {
                        Firebase.auth.signInWithCredential(credential).await()
                    }
                }
                Log.d(TAG, "Firebase signInWithCredential succeeded for uid=${Firebase.auth.currentUser?.uid}")
                setSigningIn(false)
                onSuccess()
            } catch (e: CancellationException) {
                cancelTimerJob?.cancel()
                _canCancelSignIn.value = false
                _authStatusMessage.value = null
                _isSigningIn.value = false
                throw e
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "FirebaseAuthException during Google signIn [${e.errorCode}]", e)
                setAuthError(mapFirebaseAuthError(e, language))
            } catch (e: Exception) {
                Log.e(TAG, "Firebase Google signIn failed", e)
                setAuthError(e.localizedMessage ?: "Google Sign-In failed. Please check your internet and try again.")
            } finally {
                signInJob = null
            }
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
        language: AppLanguage,
        onSuccess: () -> Unit = {}
    ) {
        val cleanEmail = email.trim()
        if (!isValidEmailFormat(cleanEmail)) {
            setAuthError(
                if (language == AppLanguage.BANGLA) "অনুগ্রহ করে একটি সঠিক ইমেইল ঠিকানা লিখো।"
                else "Please enter a valid email address."
            )
            return
        }
        if (password.length < 6) {
            setAuthError(
                if (language == AppLanguage.BANGLA) "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
                else "Password must be at least 6 characters."
            )
            return
        }
        if (signInJob?.isActive == true) return
        signInJob = viewModelScope.launch {
            setSigningIn(
                loading = true,
                statusMessage = if (language == AppLanguage.BANGLA) "ইমেইল দিয়ে সাইন-ইন হচ্ছে..." else "Signing in with email..."
            )
            try {
                executeWithBackgroundRetry(
                    language = language,
                    operationLabelEn = "signing in",
                    operationLabelBn = "সাইন-ইন করা"
                ) {
                    Firebase.auth.signInWithEmailAndPassword(cleanEmail, password).await()
                }
                setSigningIn(false)
                onSuccess()
            } catch (e: CancellationException) {
                cancelTimerJob?.cancel()
                _canCancelSignIn.value = false
                _authStatusMessage.value = null
                _isSigningIn.value = false
                throw e
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Email signIn failed [${e.errorCode}]", e)
                setAuthError(mapFirebaseAuthError(e, language))
            } catch (e: Exception) {
                Log.e(TAG, "Email signIn error", e)
                setAuthError(e.localizedMessage ?: "Email sign-in failed. Please try again.")
            }
        }
    }

    fun signUpWithEmail(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String,
        language: AppLanguage,
        onSuccess: () -> Unit = {}
    ) {
        val cleanName = displayName.trim()
        val cleanEmail = email.trim()
        if (cleanName.length < 2) {
            setAuthError(
                if (language == AppLanguage.BANGLA) "অনুগ্রহ করে তোমার নাম লিখো (কমপক্ষে ২ অক্ষর)।"
                else "Please enter your full name (at least 2 characters)."
            )
            return
        }
        if (!isValidEmailFormat(cleanEmail)) {
            setAuthError(
                if (language == AppLanguage.BANGLA) "অনুগ্রহ করে একটি সঠিক ইমেইল ঠিকানা লিখো।"
                else "Please enter a valid email address."
            )
            return
        }
        if (password.length < 6) {
            setAuthError(
                if (language == AppLanguage.BANGLA) "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
                else "Password must be at least 6 characters."
            )
            return
        }
        if (password != confirmPassword) {
            setAuthError(
                if (language == AppLanguage.BANGLA) "দুটি পাসওয়ার্ড মিলছে না।"
                else "Passwords do not match."
            )
            return
        }
        if (signInJob?.isActive == true) return
        signInJob = viewModelScope.launch {
            setSigningIn(
                loading = true,
                statusMessage = if (language == AppLanguage.BANGLA) "অ্যাকাউন্ট তৈরি হচ্ছে..." else "Creating your StudyQuest account..."
            )
            try {
                executeWithBackgroundRetry(
                    language = language,
                    operationLabelEn = "creating account",
                    operationLabelBn = "অ্যাকাউন্ট তৈরি"
                ) {
                    val result = Firebase.auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                    val createdUser = result.user ?: Firebase.auth.currentUser
                    if (createdUser != null) {
                        runCatching {
                            createdUser.updateProfile(
                                userProfileChangeRequest {
                                    this.displayName = cleanName.take(58)
                                }
                            ).await()
                        }
                    }
                }
                setSigningIn(false)
                onSuccess()
            } catch (e: CancellationException) {
                cancelTimerJob?.cancel()
                _canCancelSignIn.value = false
                _authStatusMessage.value = null
                _isSigningIn.value = false
                throw e
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Email signUp failed [${e.errorCode}]", e)
                setAuthError(mapFirebaseAuthError(e, language))
            } catch (e: Exception) {
                Log.e(TAG, "Email signUp error", e)
                setAuthError(e.localizedMessage ?: "Account creation failed. Please try again.")
            }
        }
    }

    fun sendPasswordReset(email: String, language: AppLanguage) {
        val cleanEmail = email.trim()
        if (!isValidEmailFormat(cleanEmail)) {
            _passwordResetStatus.value = false to if (language == AppLanguage.BANGLA) {
                "পাসওয়ার্ড রিসেটের জন্য একটি সঠিক ইমেইল ঠিকানা লিখো।"
            } else {
                "Enter a valid email address to send a reset link."
            }
            return
        }
        viewModelScope.launch {
            try {
                withTimeout(15_000L) {
                    Firebase.auth.sendPasswordResetEmail(cleanEmail).await()
                }
                _passwordResetStatus.value = true to if (language == AppLanguage.BANGLA) {
                    "$cleanEmail ঠিকানায় পাসওয়ার্ড রিসেট ইমেইল পাঠানো হয়েছে।"
                } else {
                    "Password reset email sent to $cleanEmail. Check your inbox."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: FirebaseAuthException) {
                _passwordResetStatus.value = false to mapFirebaseAuthError(e, language)
            } catch (e: Exception) {
                _passwordResetStatus.value = false to (
                    e.localizedMessage ?: if (language == AppLanguage.BANGLA) {
                        "পাসওয়ার্ড রিসেট ইমেইল পাঠানো যায়নি।"
                    } else {
                        "Could not send password reset email. Check your connection."
                    }
                )
            }
        }
    }

    private suspend fun executeWithBackgroundRetry(
        language: AppLanguage,
        operationLabelEn: String,
        operationLabelBn: String,
        block: suspend () -> Unit
    ) {
        var attempt = 1
        while (attempt <= MAX_RETRY_ATTEMPTS && currentCoroutineContext().isActive && _isSigningIn.value) {
            val attemptTimeoutMs = when (attempt) {
                1 -> 10_000L
                2 -> 12_000L
                else -> 14_000L
            }
            try {
                withTimeout(attemptTimeoutMs) {
                    block()
                }
                return
            } catch (e: TimeoutCancellationException) {
                if (!currentCoroutineContext().isActive || !_isSigningIn.value) throw CancellationException("Sign-in cancelled")
                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    throw IllegalStateException(
                        if (language == AppLanguage.BANGLA) {
                            "Firebase-এর সাথে সংযোগে সময় শেষ হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করো।"
                        } else {
                            "Sign-in timed out while contacting Firebase. Please check your internet connection and try again."
                        },
                        e
                    )
                }
                attempt += 1
                Log.w(TAG, "Auth attempt timed out; background retry #$attempt", e)
                _authStatusMessage.value = if (language == AppLanguage.BANGLA) {
                    "$operationLabelBn বিলম্বিত হচ্ছে (রিট্রাই #$attempt/$MAX_RETRY_ATTEMPTS)..."
                } else {
                    "Still $operationLabelEn (retrying in background, attempt #$attempt/$MAX_RETRY_ATTEMPTS)..."
                }
                delay(1_200L)
            } catch (e: FirebaseNetworkException) {
                if (!currentCoroutineContext().isActive || !_isSigningIn.value) throw CancellationException("Sign-in cancelled")
                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    throw IllegalStateException(
                        if (language == AppLanguage.BANGLA) {
                            "নেটওয়ার্ক ত্রুটির কারণে সাইন-ইন সম্পন্ন হয়নি। ওয়াই-ফাই বা মোবাইল ডেটা পরীক্ষা করো।"
                        } else {
                            "Network error prevented sign-in. Check Wi-Fi or mobile data and try again."
                        },
                        e
                    )
                }
                attempt += 1
                Log.w(TAG, "Transient FirebaseNetworkException; background retry #$attempt", e)
                _authStatusMessage.value = if (language == AppLanguage.BANGLA) {
                    "নেটওয়ার্ক ধীরগতির, আবার চেষ্টা চলছে (#$attempt/$MAX_RETRY_ATTEMPTS)..."
                } else {
                    "Slow network detected — retrying in background (#$attempt/$MAX_RETRY_ATTEMPTS)..."
                }
                delay(1_200L)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val msg = e.message.orEmpty()
                val isTransient = e !is FirebaseAuthException && (
                    msg.contains("network", ignoreCase = true) ||
                        msg.contains("timeout", ignoreCase = true) ||
                        msg.contains("unreachable", ignoreCase = true) ||
                        msg.contains("unable to resolve", ignoreCase = true)
                    )
                if (isTransient && attempt < MAX_RETRY_ATTEMPTS && currentCoroutineContext().isActive && _isSigningIn.value) {
                    attempt += 1
                    Log.w(TAG, "Transient auth failure; background retry #$attempt: $msg")
                    _authStatusMessage.value = if (language == AppLanguage.BANGLA) {
                        "Firebase সংযোগে আবার চেষ্টা চলছে (#$attempt/$MAX_RETRY_ATTEMPTS)..."
                    } else {
                        "Reconnecting to Firebase (#$attempt/$MAX_RETRY_ATTEMPTS)..."
                    }
                    delay(1_200L)
                } else {
                    throw e
                }
            }
        }
    }

    private fun isValidEmailFormat(email: String): Boolean {
        if (email.isBlank() || !email.contains("@")) return false
        val parts = email.split("@")
        if (parts.size != 2 || parts[0].isBlank()) return false
        val domain = parts[1]
        return domain.contains(".") && !domain.startsWith(".") && !domain.endsWith(".")
    }

    private fun mapFirebaseAuthError(e: FirebaseAuthException, language: AppLanguage): String {
        return when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> if (language == AppLanguage.BANGLA) {
                "ইমেইল ঠিকানাটি সঠিক নয়।"
            } else {
                "The email address is badly formatted."
            }
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> if (language == AppLanguage.BANGLA) {
                "ইমেইল বা পাসওয়ার্ড ভুল হয়েছে। অনুগ্রহ করে আবার যাচাই করো।"
            } else {
                "Incorrect email or password. Please check your credentials and try again."
            }
            "ERROR_USER_NOT_FOUND" -> if (language == AppLanguage.BANGLA) {
                "এই ইমেইলে কোনো অ্যাকাউন্ট পাওয়া যায়নি। নতুন অ্যাকাউন্ট তৈরি করো।"
            } else {
                "No account found with this email. Switch to Create Account to sign up."
            }
            "ERROR_EMAIL_ALREADY_IN_USE" -> if (language == AppLanguage.BANGLA) {
                "এই ইমেইল দিয়ে আগেই একটি অ্যাকাউন্ট তৈরি করা আছে। সাইন-ইন করো।"
            } else {
                "An account already exists with this email. Switch to Sign In instead."
            }
            "ERROR_WEAK_PASSWORD" -> if (language == AppLanguage.BANGLA) {
                "পাসওয়ার্ডটি আরও শক্তিশালী করো (কমপক্ষে ৬ অক্ষর)।"
            } else {
                "Please choose a stronger password (at least 6 characters)."
            }
            "ERROR_OPERATION_NOT_ALLOWED" -> if (language == AppLanguage.BANGLA) {
                "Firebase Console → Authentication → Sign-in method থেকে Email/Password চালু করা প্রয়োজন (অথবা নিচে গুগল সাইন-ইন ব্যবহার করো)।"
            } else {
                "Email/Password sign-in is not enabled in Firebase Console (Authentication → Sign-in method) yet. Enable it there or use Google Sign-In below."
            }
            "ERROR_TOO_MANY_REQUESTS" -> if (language == AppLanguage.BANGLA) {
                "অনেকবার চেষ্টা করা হয়েছে। কিছুক্ষণ পর আবার চেষ্টা করো।"
            } else {
                "Too many attempts. Please wait a moment and try again."
            }
            "ERROR_USER_DISABLED" -> if (language == AppLanguage.BANGLA) {
                "এই অ্যাকাউন্টটি নিষ্ক্রিয় করা হয়েছে।"
            } else {
                "This account has been disabled."
            }
            else -> "Sign-in failed [${e.errorCode}]: ${e.localizedMessage ?: "Firebase authentication error."}"
        }
    }

    companion object {
        private const val TAG = "PreAuthVM"
        private const val CANCEL_REVEAL_DELAY_MS = 2_500L
        private const val MAX_RETRY_ATTEMPTS = 3
    }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainSessionViewModel(
    private val currentUser: FirebaseUser,
    private val authRepo: AuthAndProfileRepository,
    private val adminRepo: AdminStudioRepository,
    private val preferences: StudyQuestPreferences,
    connectivityObserver: ConnectivityObserver,
    private val appContext: Context? = null,
    private val gamificationEngine: GamificationEngine? = null,
    private val billingProvider: BillingAndAdProvider? = null,
    private val textbookRepo: TextbookDownloadRepository? = null
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
        .catch { emit(setOf("crest_indigo")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), setOf("crest_indigo"))

    val cloudTextbooks: StateFlow<List<TextbookCatalogItem>> = (textbookRepo?.observeCloudTextbookCatalog()
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val cachedBooks: StateFlow<List<CachedBookEntity>> = (textbookRepo?.observeCachedBooks()
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val availableBooksForProfile: StateFlow<List<TextbookCatalogItem>> = combine(
        profile,
        cloudTextbooks
    ) { prof, cloud ->
        val p = prof ?: return@combine TextbookCatalog.books
        val merged = (cloud + TextbookCatalog.books).distinctBy { it.id }
        val filtered = TextbookCatalog.booksFor(
            academicYear = p.activeBookYear.ifBlank { "2026" },
            classId = p.classId,
            curriculumVersion = p.curriculumVersion,
            academicGroup = p.academicGroup,
            extraCloudBooks = cloud
        )
        filtered.ifEmpty { merged.filter { it.classId == p.classId } }.ifEmpty { merged }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), TextbookCatalog.books)

    val activeLockedBook: StateFlow<TextbookCatalogItem> = combine(
        profile,
        cloudTextbooks
    ) { prof, cloud ->
        val p = prof
        if (p == null) {
            TextbookCatalog.books.first()
        } else {
            val found = TextbookCatalog.bookById(p.activeBookId, cloud)
            found ?: TextbookCatalog.defaultBookFor(
                classId = p.classId,
                curriculumVersion = p.curriculumVersion,
                academicGroup = p.academicGroup,
                academicYear = p.activeBookYear.ifBlank { "2026" },
                extraCloudBooks = cloud
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), TextbookCatalog.books.first())

    val badges: StateFlow<List<UserBadgeDoc>> = (gamificationEngine?.observeUserBadges(currentUser.uid)
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val xpLedger: StateFlow<List<XpTransactionDoc>> = (gamificationEngine?.observeXpLedger(currentUser.uid)
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val studyGoals: StateFlow<List<StudyGoalDoc>> = (gamificationEngine?.observeStudyGoals(currentUser.uid)
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val assignments: StateFlow<List<AssignmentDefinitionDoc>> = profile
        .flatMapLatest { prof ->
            gamificationEngine?.observeAssignments(prof?.classId ?: "class_10")
                ?: kotlinx.coroutines.flow.flowOf(emptyList())
        }.catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val assignmentSubmissions: StateFlow<List<AssignmentSubmissionDoc>> =
        (gamificationEngine?.observeAssignmentSubmissions(currentUser.uid)
            ?: kotlinx.coroutines.flow.flowOf(emptyList()))
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val systemSettings: StateFlow<SystemSettingsDoc> = adminRepo.observeSystemSettings()
        .catch { emit(SystemSettingsDoc()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SystemSettingsDoc())

    val subscription: StateFlow<UserSubscriptionDoc> = (billingProvider?.observeSubscription(currentUser.uid)
        ?: kotlinx.coroutines.flow.flowOf(UserSubscriptionDoc(userId = currentUser.uid)))
        .catch { emit(UserSubscriptionDoc(userId = currentUser.uid)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserSubscriptionDoc(userId = currentUser.uid))

    val aiCreditLedger: StateFlow<List<AiCreditLedgerDoc>> = (billingProvider?.observeAiCreditLedger(currentUser.uid)
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val videoCreditLedger: StateFlow<List<VideoCreditLedgerDoc>> = (billingProvider?.observeVideoCreditLedger(currentUser.uid)
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val purchaseHistory: StateFlow<List<PurchaseRecordDoc>> = (billingProvider?.observePurchaseHistory(currentUser.uid)
        ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val adPlacementState: StateFlow<AdBannerPlacementState> = combine(
        subscription,
        systemSettings,
        StudyActivityMonitor.isFocusActive,
        StudyActivityMonitor.isExamOrQuizActive,
        StudyActivityMonitor.isVideoActive
    ) { sub, settings, focusActive, examActive, videoActive ->
        billingProvider?.evaluateAdPlacement(sub, settings, focusActive, examActive, videoActive)
            ?: AdBannerPlacementState(
                shouldDisplayAdSlot = false,
                isSuppressedForStudyOrExam = focusActive || examActive || videoActive,
                adMobConfigured = settings.adMobConfigured,
                tier = sub.tier
            )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        AdBannerPlacementState(false, false, false, "free")
    )

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
                // Immediately unblock UI as soon as profile is ready — run secondary tasks asynchronously
                _bootstrappedProfile.value = loadedProfile
                _bootstrapError.value = null
                _bootstrapping.value = false

                launch {
                    runCatching {
                        withTimeout(8_000L) {
                            if (loadedProfile.showOnlineStatus) {
                                authRepo.setOnlinePresence(currentUser.uid, true)
                            }
                            authRepo.ensureActivePeriodQuests(currentUser.uid)
                        }
                    }
                }
                launch {
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
                }
                launch {
                    _adminRole.value = runCatching {
                        withTimeout(8_000L) { adminRepo.resolveAdminRole(currentUser) }
                    }.getOrNull()
                }
                launch {
                    runCatching {
                        withTimeout(8_000L) { gamificationEngine?.evaluateAcademicYearLifecycle(currentUser.uid) }
                    }
                }
                launch {
                    runCatching {
                        withTimeout(8_000L) { billingProvider?.ensureFreeSubscriptionInitialized(currentUser.uid) }
                    }
                }
                launch {
                    val book = TextbookCatalog.bookById(loadedProfile.activeBookId)
                        ?: TextbookCatalog.defaultBookFor(
                            loadedProfile.classId,
                            loadedProfile.curriculumVersion,
                            loadedProfile.academicGroup,
                            loadedProfile.activeBookYear.ifBlank { "2026" }
                        )
                    runCatching {
                        textbookRepo?.ensureBookInstalledOffline(book, forceRedownloadPdf = false, language = currentLang)
                    }
                }
            }.onFailure { err ->
                val msg = unwrapFirestoreMessage(err, err.localizedMessage ?: "Profile bootstrap failed")
                _bootstrapError.value = msg
                _bannerMessage.value = msg
                _bootstrapping.value = false
            }
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
        studyGoalTitle: String = "",
        activeBook: TextbookCatalogItem? = null
    ) {
        viewModelScope.launch {
            _isCompletingSetup.value = true
            val lang = localPreferences.value.uiLanguage
            val resolvedBook = activeBook ?: TextbookCatalog.defaultBookFor(
                classId = classId,
                curriculumVersion = curriculumVersion,
                academicGroup = academicGroup
            )
            val res = authRepo.completeAcademicSetup(
                userId = currentUser.uid,
                displayName = displayName,
                classId = classId,
                curriculumVersion = curriculumVersion,
                selectedSubjects = selectedSubjects,
                avatarId = avatarId,
                academicGroup = academicGroup,
                dailyGoalMinutes = dailyGoalMinutes,
                studyGoalTitle = studyGoalTitle,
                activeBook = resolvedBook
            )
            res.onSuccess {
                _bootstrappedProfile.value = (_bootstrappedProfile.value ?: profile.value)?.copy(
                    displayName = displayName.trim().ifBlank { "Student" }.take(60),
                    classId = classId,
                    curriculumVersion = curriculumVersion,
                    activeBookId = resolvedBook.id,
                    activeBookTitle = if (lang == AppLanguage.BANGLA) resolvedBook.titleBn else resolvedBook.titleEn,
                    activeBookSubjectId = resolvedBook.subjectId,
                    activeBookYear = resolvedBook.academicYear,
                    selectedSubjects = (listOf(resolvedBook.subjectId) + selectedSubjects).distinct().take(20),
                    avatarId = avatarId,
                    academicGroup = academicGroup,
                    dailyGoalMinutes = dailyGoalMinutes.coerceIn(5L, 360L),
                    onboardingCompleted = true
                )
                launch {
                    textbookRepo?.ensureBookInstalledOffline(resolvedBook, forceRedownloadPdf = false, language = lang)
                }
            }.onFailure {
                _bannerMessage.value = it.localizedMessage ?: "Setup failed"
            }
            _isCompletingSetup.value = false
        }
    }

    fun switchActiveBook(book: TextbookCatalogItem) {
        viewModelScope.launch {
            val lang = localPreferences.value.uiLanguage
            val res = authRepo.switchActiveBook(currentUser.uid, book, lang)
            res.onSuccess {
                _bootstrappedProfile.value = (_bootstrappedProfile.value ?: profile.value)?.copy(
                    activeBookId = book.id,
                    activeBookTitle = if (lang == AppLanguage.BANGLA) book.titleBn else book.titleEn,
                    activeBookSubjectId = book.subjectId,
                    activeBookYear = book.academicYear,
                    curriculumVersion = book.curriculumVersion,
                    selectedSubjects = listOf(book.subjectId)
                )
                _bannerMessage.value = if (lang == AppLanguage.BANGLA) {
                    "বই লক ও অফলাইনে ইনস্টল করা হচ্ছে: ${book.titleBn}"
                } else {
                    "Locked & installing offline: ${book.titleEn}"
                }
                launch {
                    textbookRepo?.ensureBookInstalledOffline(book, forceRedownloadPdf = false, language = lang)
                }
            }.onFailure {
                _bannerMessage.value = unwrapFirestoreMessage(it, "Could not switch textbook")
            }
        }
    }

    fun saveAgeAndConsent(ageBracket: String, guardianConsentStatus: String) {
        viewModelScope.launch {
            authRepo.saveAgeAndConsent(currentUser.uid, ageBracket, guardianConsentStatus)
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
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(darkMode = enabled, themeMode = if (enabled) ThemeMode.DARK else ThemeMode.LIGHT))
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(themeMode = mode, darkMode = mode == ThemeMode.DARK))
        }
    }

    fun updateSurfaceStyle(style: SurfaceStyle) {
        viewModelScope.launch {
            preferences.setSurfaceStyle(style)
            authRepo.syncPreferencesToCloud(currentUser.uid, localPreferences.value.copy(surfaceStyle = style))
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

    fun updateNotificationCategory(category: String, enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationCategoryEnabled(category, enabled)
            val snap = preferences.snapshot()
            authRepo.syncPreferencesToCloud(currentUser.uid, snap)
        }
    }

    fun purchaseStreakFreeze() {
        viewModelScope.launch {
            val res = gamificationEngine?.purchaseStreakFreeze(currentUser.uid)
                ?: Result.failure(IllegalStateException("Streak Freeze unavailable"))
            res.onSuccess {
                _bannerMessage.value = "Streak Freeze added to your inventory!"
            }.onFailure {
                _bannerMessage.value = unwrapFirestoreMessage(it, it.localizedMessage ?: "Purchase failed")
            }
        }
    }

    fun toggleBadgeFavorite(badgeId: String) {
        viewModelScope.launch {
            gamificationEngine?.toggleBadgeFavoriteOrPin(currentUser.uid, badgeId, toggleFavorite = true)
        }
    }

    fun toggleBadgePin(badgeId: String) {
        viewModelScope.launch {
            gamificationEngine?.toggleBadgeFavoriteOrPin(currentUser.uid, badgeId, pinAsFeatured = true)
        }
    }

    fun createStudyGoal(
        title: String,
        goalType: String,
        periodType: String,
        subjectId: String,
        targetValue: Long,
        difficulty: String
    ) {
        viewModelScope.launch {
            val res = gamificationEngine?.createStudyGoal(
                userId = currentUser.uid,
                title = title,
                goalType = goalType,
                periodType = periodType,
                subjectId = subjectId,
                targetValue = targetValue,
                difficulty = difficulty
            ) ?: Result.failure(IllegalStateException("Goals service unavailable"))
            res.onSuccess { _bannerMessage.value = "Personal study goal created!" }
                .onFailure { _bannerMessage.value = unwrapFirestoreMessage(it, it.localizedMessage ?: "Could not create goal") }
        }
    }

    fun claimStudyGoal(goal: StudyGoalDoc) {
        viewModelScope.launch {
            val res = gamificationEngine?.claimGoalReward(currentUser.uid, goal)
                ?: Result.failure(IllegalStateException("Goals service unavailable"))
            res.onSuccess { xp ->
                if (xp > 0) _bannerMessage.value = "Goal completed & verified!"
            }.onFailure {
                _bannerMessage.value = unwrapFirestoreMessage(it, it.localizedMessage ?: "Could not claim goal")
            }
        }
    }

    fun deleteStudyGoal(goalId: String) {
        viewModelScope.launch {
            gamificationEngine?.deleteStudyGoal(currentUser.uid, goalId)
        }
    }

    fun submitAssignment(assignment: AssignmentDefinitionDoc) {
        viewModelScope.launch {
            val res = gamificationEngine?.submitAssignmentCompletion(currentUser.uid, assignment)
                ?: Result.failure(IllegalStateException("Assignments service unavailable"))
            res.onSuccess { xp ->
                if (xp > 0) _bannerMessage.value = "Assignment verified & recorded!"
            }.onFailure {
                _bannerMessage.value = unwrapFirestoreMessage(it, it.localizedMessage ?: "Submission failed")
            }
        }
    }

    fun triggerBillingUpgradeFlow() {
        val outcome = billingProvider?.requestUpgradeToPremium(systemSettings.value) ?: return
        val lang = localPreferences.value.uiLanguage
        _bannerMessage.value = when (outcome) {
            is BillingActionResult.Unconfigured -> if (lang == AppLanguage.BANGLA) outcome.reasonBn else outcome.reasonEn
            is BillingActionResult.ConfiguredReady -> if (lang == AppLanguage.BANGLA) outcome.messageBn else outcome.messageEn
        }
    }

    fun requestPlanUpgrade(planId: String) {
        val provider = billingProvider ?: return
        viewModelScope.launch {
            val outcome = provider.requestPlanUpgrade(currentUser.uid, planId, systemSettings.value)
            val lang = localPreferences.value.uiLanguage
            _bannerMessage.value = when (outcome) {
                is BillingActionResult.Unconfigured -> if (lang == AppLanguage.BANGLA) outcome.reasonBn else outcome.reasonEn
                is BillingActionResult.ConfiguredReady -> if (lang == AppLanguage.BANGLA) outcome.messageBn else outcome.messageEn
            }
        }
    }

    fun purchaseVideoBoost() {
        val provider = billingProvider ?: return
        viewModelScope.launch {
            val outcome = provider.purchaseVideoBoost(currentUser.uid, systemSettings.value)
            val lang = localPreferences.value.uiLanguage
            _bannerMessage.value = when (outcome) {
                is BillingActionResult.Unconfigured -> if (lang == AppLanguage.BANGLA) outcome.reasonBn else outcome.reasonEn
                is BillingActionResult.ConfiguredReady -> if (lang == AppLanguage.BANGLA) outcome.messageBn else outcome.messageEn
            }
        }
    }

    fun switchToFreePlan() {
        val provider = billingProvider ?: return
        viewModelScope.launch {
            provider.applyFreePlanFallback(currentUser.uid)
            val lang = localPreferences.value.uiLanguage
            _bannerMessage.value = if (lang == AppLanguage.BANGLA) {
                "ফ্রি StudyQuest (৳০/মাস) চালু হয়েছে — প্রতি মাসে ২০টি AI ও ১টি ভিডিও ক্রেডিট।"
            } else {
                "Free StudyQuest (৳0/mo) active — 20 AI & 1 animated video credit every month."
            }
        }
    }

    fun markWelcomePlanCompleted() {
        viewModelScope.launch {
            preferences.setWelcomePlanCompleted(true)
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
