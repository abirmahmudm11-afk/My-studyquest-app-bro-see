package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.core.AppLanguage
import com.example.core.StudyQuestStrings
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.model.AcademicGroup
import com.example.data.model.CurriculumVersion
import com.example.data.model.PublicProfile
import com.example.data.model.TextbookCatalogItem
import com.example.ui.components.AuthWelcomePreviewCarouselStage
import com.example.ui.components.AvatarBadgeView
import com.example.ui.components.GoogleLogoMark
import com.example.ui.components.LaunchStudyPortalHeroShowcase
import com.example.ui.theme.EditorialError
import com.example.ui.theme.EditorialSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalStudyQuestColors
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class AuthStage {
    WELCOME,
    SIGN_IN,
    SIGN_UP
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.hasActiveInternetConnection(): Boolean {
    val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return true
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

class GoogleSignInController(
    val launch: () -> Unit,
    val cancel: () -> Unit
)

@Composable
fun rememberGoogleSignInController(
    language: AppLanguage,
    onSigningInChanged: (Boolean) -> Unit,
    onIdTokenReceived: (String) -> Unit,
    onAuthError: (String) -> Unit
): GoogleSignInController {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val webClientId = remember(context) { context.getString(R.string.default_web_client_id) }

    val latestOnSigningInChanged by rememberUpdatedState(onSigningInChanged)
    val latestOnIdTokenReceived by rememberUpdatedState(onIdTokenReceived)
    val latestOnAuthError by rememberUpdatedState(onAuthError)
    var activeJob by remember { mutableStateOf<Job?>(null) }

    return remember(context, activity, language, webClientId) {
        GoogleSignInController(
            launch = {
                if (!context.hasActiveInternetConnection()) {
                    latestOnSigningInChanged(false)
                    latestOnAuthError(
                        StudyQuestStrings.t(
                            language,
                            "No internet connection detected. Connect to Wi-Fi or mobile data and try again.",
                            "ইন্টারনেট সংযোগ পাওয়া যায়নি। ওয়াই-ফাই বা মোবাইল ডেটা চালু করে আবার চেষ্টা করো।"
                        )
                    )
                    return@GoogleSignInController
                }
                activeJob?.cancel()
                activeJob = scope.launch {
                    latestOnSigningInChanged(true)
                    try {
                        Log.d("StudyQuestAuth", "Launching GetSignInWithGoogleOption via CredentialManager")
                        val googleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleOption)
                            .build()
                        val result = credentialManager.getCredential(activity ?: context, request)
                        val credential = result.credential
                        if (credential is CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                            if (googleCred.idToken.isBlank()) {
                                latestOnSigningInChanged(false)
                                latestOnAuthError(
                                    StudyQuestStrings.t(
                                        language,
                                        "Google did not return an ID token. Please try again.",
                                        "গুগল থেকে বৈধ টোকেন পাওয়া যায়নি। আবার চেষ্টা করো।"
                                    )
                                )
                            } else {
                                Log.d("StudyQuestAuth", "Received Google ID token; passing to Firebase Auth")
                                latestOnIdTokenReceived(googleCred.idToken)
                            }
                        } else {
                            Log.w("StudyQuestAuth", "Unexpected credential type: ${credential.type}")
                            latestOnSigningInChanged(false)
                            latestOnAuthError("Unexpected credential type received (${credential.type}).")
                        }
                    } catch (e: CancellationException) {
                        latestOnSigningInChanged(false)
                        throw e
                    } catch (e: GetCredentialCancellationException) {
                        Log.w("StudyQuestAuth", "Google Sign-In cancelled or rejected by Play Services: ${e.message}", e)
                        latestOnSigningInChanged(false)
                        latestOnAuthError(
                            StudyQuestStrings.t(
                                language,
                                "Google Sign-In did not complete (${e.errorMessage ?: "cancelled or signing configuration mismatch"}). Please try again.",
                                "গুগল সাইন-ইন সম্পন্ন হয়নি (${e.errorMessage ?: "বাতিল হয়েছে বা সাইনিং মিলছে না"})। আবার চেষ্টা করো।"
                            )
                        )
                    } catch (e: GetCredentialCustomException) {
                        val raw = e.errorMessage?.toString() ?: e.localizedMessage.orEmpty()
                        Log.e("StudyQuestAuth", "CredentialManager custom failure: $raw", e)
                        latestOnSigningInChanged(false)
                        val isDevConsole28444 = raw.contains("28444") || raw.contains("Developer console", ignoreCase = true)
                        latestOnAuthError(
                            if (isDevConsole28444) {
                                StudyQuestStrings.t(
                                    language,
                                    "Google Sign-In configuration error [28444]: The app signing SHA-1 fingerprint for com.aistudio.studyquest.bdxkpq does not match Firebase configuration.",
                                    "গুগল সাইন-ইন কনফিগারেশন ত্রুটি [28444]: com.aistudio.studyquest.bdxkpq প্যাকেজের SHA-1 ফিঙ্গারপ্রিন্ট Firebase কনফিগারেশনের সাথে মিলছে না।"
                                )
                            } else {
                                raw.ifBlank { "Google Sign-In failed" }
                            }
                        )
                    } catch (e: NoCredentialException) {
                        Log.w("StudyQuestAuth", "No Google account available on device", e)
                        latestOnSigningInChanged(false)
                        latestOnAuthError(
                            StudyQuestStrings.t(
                                language,
                                "No Google account found on this device. Please add a Google account in Android Settings and try again.",
                                "এই ডিভাইসে কোনো গুগল অ্যাকাউন্ট পাওয়া যায়নি। সেটিংস থেকে গুগল অ্যাকাউন্ট যোগ করে আবার চেষ্টা করো।"
                            )
                        )
                    } catch (e: GetCredentialException) {
                        Log.e("StudyQuestAuth", "CredentialManager failed [${e.type}]", e)
                        latestOnSigningInChanged(false)
                        latestOnAuthError(e.errorMessage?.toString() ?: e.localizedMessage ?: "Google Sign-In failed (${e.type})")
                    } catch (e: Exception) {
                        Log.e("StudyQuestAuth", "Sign-in exception", e)
                        latestOnSigningInChanged(false)
                        latestOnAuthError(e.localizedMessage ?: "Sign-in failed")
                    }
                }
            },
            cancel = {
                activeJob?.cancel()
                activeJob = null
                latestOnSigningInChanged(false)
            }
        )
    }
}

@Composable
fun rememberGoogleSignInLauncher(
    language: AppLanguage,
    onSigningInChanged: (Boolean) -> Unit,
    onIdTokenReceived: (String) -> Unit,
    onAuthError: (String) -> Unit
): () -> Unit {
    return rememberGoogleSignInController(
        language = language,
        onSigningInChanged = onSigningInChanged,
        onIdTokenReceived = onIdTokenReceived,
        onAuthError = onAuthError
    ).launch
}

/**
 * First Screen — Swiss Minimal Launch.
 * Architecture: Compact top [ বাংলা | EN ] pill toggle + single Slate/Lime focal stage + ONE Display Title + ONE Primary CTA.
 */
@Composable
fun LanguagePickerLaunchScreen(
    onSelectLanguage: (AppLanguage) -> Unit,
    reducedMotion: Boolean = false
) {
    var previewLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchStudyPortalHeroShowcase(
            language = previewLanguage,
            reducedMotion = reducedMotion,
            headerTrailingSlot = {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LaunchLangSegment(
                            label = "বাংলা",
                            selected = previewLanguage == AppLanguage.BANGLA,
                            testTag = "pick_lang_bn",
                            onClick = { previewLanguage = AppLanguage.BANGLA }
                        )
                        LaunchLangSegment(
                            label = "EN",
                            selected = previewLanguage == AppLanguage.ENGLISH,
                            testTag = "pick_lang_en",
                            onClick = { previewLanguage = AppLanguage.ENGLISH }
                        )
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = StudyQuestStrings.t(
                    previewLanguage,
                    "Your NCTB study space, simplified.",
                    "এনসিটিবি পড়াশোনার গোছানো স্থান।"
                ),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("launch_display_title")
            )

            Text(
                text = StudyQuestStrings.t(
                    previewLanguage,
                    "Offline textbooks, board practice, and bilingual help for Classes 6–10.",
                    "৬ষ্ঠ–১০ম শ্রেণির অফলাইন পাঠ্যবই, বোর্ড অনুশীলন ও সহজ AI সহায়তা।"
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onSelectLanguage(previewLanguage) },
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("launch_get_started_btn")
            ) {
                Text(
                    text = StudyQuestStrings.t(previewLanguage, "Get Started", "শুরু করো"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LaunchLangSegment(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color.White else Color.Transparent,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color(0xFF0F172A) else Color.White.copy(alpha = 0.88f),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

/**
 * Welcome & Redesigned Full-Canvas Auth Flow.
 * - WELCOME stage: Slate + Lime 3D carousel stage -> 1 Display Title + 1 Primary CTA ("Continue with Google") + quiet link strip below.
 * - SIGN_IN / SIGN_UP stage: Dedicated full-canvas monochrome Swiss typography Auth workspace (zero hero carousel competing on screen).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeAndGoogleSignInScreen(
    language: AppLanguage,
    reducedMotion: Boolean = false,
    isSigningIn: Boolean,
    authError: String?,
    onSetLanguage: (AppLanguage) -> Unit,
    onSigningInChanged: (Boolean) -> Unit,
    onIdTokenReceived: (String) -> Unit,
    onAuthError: (String) -> Unit,
    canCancelSignIn: Boolean = false,
    onCancelSignIn: () -> Unit = {},
    onBackToLanguagePicker: () -> Unit = {},
    authStatusMessage: String? = null,
    passwordResetStatus: Pair<Boolean, String>? = null,
    onEmailSignIn: (email: String, password: String) -> Unit = { _, _ -> },
    onEmailSignUp: (name: String, email: String, password: String, confirmPassword: String) -> Unit = { _, _, _, _ -> },
    onSendPasswordReset: (email: String) -> Unit = {},
    onClearAuthMessages: () -> Unit = {},
    onClearPasswordResetStatus: () -> Unit = {},
    onEnterGuestMode: (() -> Unit)? = null,
    ageBracket: String = "13_17",
    guardianConsentStatus: String = "verified",
    onAgeAndConsentSelected: (String, String) -> Unit = { _, _ -> },
    initialStage: AuthStage = AuthStage.WELCOME
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val focusManager = LocalFocusManager.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val webClientId = remember(context) { context.getString(R.string.default_web_client_id) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val semantic = LocalStudyQuestColors.current

    var currentStage by remember(initialStage) { mutableStateOf(initialStage) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedAgeBracket by remember(ageBracket) { mutableStateOf(ageBracket) }
    var guardianApproved by remember(guardianConsentStatus) { mutableStateOf(guardianConsentStatus == "verified") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var userInteractedWithAuth by remember { mutableStateOf(false) }
    var silentAutoSignInJob by remember { mutableStateOf<Job?>(null) }

    val googleController = rememberGoogleSignInController(
        language = language,
        onSigningInChanged = onSigningInChanged,
        onIdTokenReceived = onIdTokenReceived,
        onAuthError = onAuthError
    )

    BackHandler(enabled = showResetDialog) {
        showResetDialog = false
        onClearPasswordResetStatus()
    }

    BackHandler(enabled = !showResetDialog && currentStage != AuthStage.WELCOME) {
        userInteractedWithAuth = true
        currentStage = AuthStage.WELCOME
        onClearAuthMessages()
    }

    BackHandler(enabled = !showResetDialog && currentStage == AuthStage.WELCOME) {
        userInteractedWithAuth = true
        silentAutoSignInJob?.cancel()
        googleController.cancel()
        onCancelSignIn()
        onBackToLanguagePicker()
    }

    DisposableEffect(Unit) {
        onDispose { silentAutoSignInJob?.cancel() }
    }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser != null || isSigningIn || userInteractedWithAuth || !context.hasActiveInternetConnection()) {
            return@LaunchedEffect
        }
        silentAutoSignInJob = coroutineScope.launch {
            try {
                val autoOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()
                val req = GetCredentialRequest.Builder().addCredentialOption(autoOption).build()
                val result = withTimeoutOrNull(2_500L) {
                    credentialManager.getCredential(activity ?: context, req)
                } ?: return@launch
                val cred = result.credential
                if (!userInteractedWithAuth &&
                    !isSigningIn &&
                    cred is CustomCredential &&
                    cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCred = GoogleIdTokenCredential.createFrom(cred.data)
                    if (googleCred.idToken.isNotBlank()) {
                        onIdTokenReceived(googleCred.idToken)
                    }
                }
            } catch (_: NoCredentialException) {
            } catch (_: GetCredentialCancellationException) {
            } catch (_: CancellationException) {
            } catch (e: GetCredentialCustomException) {
                Log.w("StudyQuestAuth", "Silent auto sign-in custom error: ${e.errorMessage ?: e.message}")
            } catch (e: Exception) {
                Log.d("StudyQuestAuth", "Silent auto sign-in skipped: ${e.message}")
            }
        }
    }

    LaunchedEffect(authError, passwordResetStatus) {
        if (!authError.isNullOrBlank() || passwordResetStatus != null) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val emailTrimmed = email.trim()
    val emailLooksMalformed = emailTrimmed.isNotEmpty() && (!emailTrimmed.contains("@") || !emailTrimmed.contains("."))
    val passwordScore = remember(password) {
        var score = 0
        if (password.length >= 6) score += 1
        if (password.any { it.isDigit() }) score += 1
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score += 1
        score
    }
    val passwordStrengthLabel = when (passwordScore) {
        0, 1 -> StudyQuestStrings.t(language, "Weak • use 6+ chars", "দুর্বল • কমপক্ষে ৬ অক্ষর")
        2 -> StudyQuestStrings.t(language, "Fair • add uppercase or digits", "মাঝারি • বড় হাতের অক্ষর বা সংখ্যা দিন")
        else -> StudyQuestStrings.t(language, "Strong password", "শক্তিশালী পাসওয়ার্ড")
    }
    val passwordMismatch = confirmPassword.isNotEmpty() && confirmPassword != password

    val cleanFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )

    AnimatedContent(
        targetState = currentStage,
        transitionSpec = {
            if (reducedMotion) fadeIn(tween(10)) togetherWith fadeOut(tween(10))
            else fadeIn(tween(220)) togetherWith fadeOut(tween(150))
        },
        label = "auth_canvas_stage",
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { stage ->
        if (stage == AuthStage.WELCOME) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthWelcomePreviewCarouselStage(
                    language = language,
                    reducedMotion = reducedMotion,
                    showCarousel = true,
                    headerContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    userInteractedWithAuth = true
                                    silentAutoSignInJob?.cancel()
                                    googleController.cancel()
                                    onCancelSignIn()
                                    onBackToLanguagePicker()
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("welcome_back_to_lang_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    tint = Color.White,
                                    contentDescription = StudyQuestStrings.t(
                                        language,
                                        "Back to language picker",
                                        "ভাষা নির্বাচনে ফিরে যাও"
                                    )
                                )
                            }

                            Surface(
                                onClick = {
                                    onSetLanguage(
                                        if (language == AppLanguage.BANGLA) AppLanguage.ENGLISH
                                        else AppLanguage.BANGLA
                                    )
                                },
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("toggle_lang_welcome")
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "বাংলা  |  EN" else "বাংলা  |  EN",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "Learn calmly. Master every chapter.",
                            "গোছানো পড়াশোনায় প্রতিটি অধ্যায়ে দক্ষ হও।"
                        ),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "Sync textbooks, practice history, and notes across devices.",
                            "পাঠ্যবই, অনুশীলনের ইতিহাস ও নোট সব ডিভাইসে সিঙ্ক রাখো।"
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))

                    // ONE Primary High-Contrast Pill CTA
                    Button(
                        onClick = {
                            userInteractedWithAuth = true
                            silentAutoSignInJob?.cancel()
                            focusManager.clearFocus()
                            googleController.launch()
                        },
                        enabled = !isSigningIn,
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = semantic.heroLimeStart,
                            contentColor = semantic.onHeroLime,
                            disabledContainerColor = semantic.heroLimeStart.copy(alpha = 0.55f),
                            disabledContentColor = semantic.onHeroLime.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("google_sign_in_button")
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = semantic.onHeroLime,
                                strokeWidth = 2.4.dp
                            )
                        } else {
                            GoogleLogoMark(size = 20.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = StudyQuestStrings.t(language, "Continue with Google", "গুগল দিয়ে শুরু করো"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Single Quiet Link Strip Beneath Primary CTA
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {
                                userInteractedWithAuth = true
                                silentAutoSignInJob?.cancel()
                                onClearAuthMessages()
                                currentStage = AuthStage.SIGN_IN
                            },
                            enabled = !isSigningIn,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("welcome_sign_in_btn")
                        ) {
                            Text(
                                text = StudyQuestStrings.t(language, "Sign in with email", "ইমেইল সাইন-ইন"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(
                            onClick = {
                                userInteractedWithAuth = true
                                silentAutoSignInJob?.cancel()
                                onClearAuthMessages()
                                currentStage = AuthStage.SIGN_UP
                            },
                            enabled = !isSigningIn,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("welcome_create_account_btn")
                        ) {
                            Text(
                                text = StudyQuestStrings.t(language, "Create account", "অ্যাকাউন্ট তৈরি"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (onEnterGuestMode != null) {
                            TextButton(
                                onClick = {
                                    userInteractedWithAuth = true
                                    silentAutoSignInJob?.cancel()
                                    onEnterGuestMode()
                                },
                                enabled = !isSigningIn,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("welcome_guest_mode_btn")
                            ) {
                                Text(
                                    text = StudyQuestStrings.t(language, "Guest mode", "গেস্ট মোড"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    AuthFeedbackSection(
                        language = language,
                        isSigningIn = isSigningIn,
                        canCancelSignIn = canCancelSignIn,
                        authStatusMessage = authStatusMessage,
                        passwordResetStatus = passwordResetStatus,
                        authError = authError,
                        onCancel = {
                            googleController.cancel()
                            onCancelSignIn()
                        },
                        onRetryGoogle = { googleController.launch() }
                    )
                }
            }
        } else {
            // Standalone Full-Canvas Monochrome Auth Screen (SIGN_IN & SIGN_UP)
            val isSignUp = stage == AuthStage.SIGN_UP
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Chrome: Back Arrow + Segmented [Sign In | Create Account] Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                userInteractedWithAuth = true
                                currentStage = AuthStage.WELCOME
                                onClearAuthMessages()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("auth_back_to_welcome_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = StudyQuestStrings.t(language, "Back", "ফিরে যাও")
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                AuthModeSegmentPill(
                                    label = StudyQuestStrings.t(language, "Sign In", "সাইন-ইন"),
                                    selected = !isSignUp,
                                    enabled = !isSigningIn,
                                    testTag = "auth_mode_sign_in",
                                    onClick = {
                                        currentStage = AuthStage.SIGN_IN
                                        onClearAuthMessages()
                                    }
                                )
                                AuthModeSegmentPill(
                                    label = StudyQuestStrings.t(language, "Create Account", "নতুন অ্যাকাউন্ট"),
                                    selected = isSignUp,
                                    enabled = !isSigningIn,
                                    testTag = "auth_mode_sign_up",
                                    onClick = {
                                        currentStage = AuthStage.SIGN_UP
                                        onClearAuthMessages()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Oversized Display Headline + Quiet Subtitle
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isSignUp) {
                                StudyQuestStrings.t(language, "Create your account.", "নতুন অ্যাকাউন্ট তৈরি করো।")
                            } else {
                                StudyQuestStrings.t(language, "Welcome back.", "আবার স্বাগতম।")
                            },
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (isSignUp) {
                                StudyQuestStrings.t(
                                    language,
                                    "Save your textbooks, badges, and notes securely.",
                                    "আপনার পাঠ্যবই, ব্যাজ ও নোট ক্লাউডে নিরাপদে সংরক্ষণ করুন।"
                                )
                            } else {
                                StudyQuestStrings.t(
                                    language,
                                    "Sign in to continue your NCTB study plan.",
                                    "আপনার এনসিটিবি অধ্যয়ন চালিয়ে যেতে সাইন-ইন করুন।"
                                )
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Minimal Form Fields
                    if (isSignUp) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(StudyQuestStrings.t(language, "Full Name", "তোমার পুরো নাম")) },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            singleLine = true,
                            enabled = !isSigningIn,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(16.dp),
                            colors = cleanFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(StudyQuestStrings.t(language, "Email Address", "ইমেইল ঠিকানা")) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        isError = emailLooksMalformed,
                        supportingText = if (emailLooksMalformed) {
                            {
                                Text(
                                    text = StudyQuestStrings.t(language, "Enter a valid email address", "সঠিক ইমেইল ঠিকানা দিন"),
                                    color = EditorialError
                                )
                            }
                        } else null,
                        singleLine = true,
                        enabled = !isSigningIn,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = cleanFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(StudyQuestStrings.t(language, "Password", "পাসওয়ার্ড")) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("auth_password_visibility_btn")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = StudyQuestStrings.t(
                                        language,
                                        if (passwordVisible) "Hide password" else "Show password",
                                        if (passwordVisible) "পাসওয়ার্ড লুকাও" else "পাসওয়ার্ড দেখাও"
                                    )
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isSigningIn,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (!isSignUp) ImeAction.Done else ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!isSignUp && !isSigningIn) {
                                    focusManager.clearFocus()
                                    onEmailSignIn(email, password)
                                }
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = cleanFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    if (isSignUp && password.isNotEmpty()) {
                        val strengthColor = when (passwordScore) {
                            0, 1 -> EditorialError
                            2 -> MaterialTheme.colorScheme.secondary
                            else -> EditorialSuccess
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_strength")
                        ) {
                            LinearProgressIndicator(
                                progress = { (passwordScore.coerceAtLeast(1) / 3f).coerceIn(0.25f, 1f) },
                                color = strengthColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(999.dp))
                            )
                            Text(
                                text = passwordStrengthLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = strengthColor
                            )
                        }
                    }

                    if (isSignUp) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text(StudyQuestStrings.t(language, "Confirm Password", "পাসওয়ার্ড নিশ্চিত করো")) },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            isError = passwordMismatch,
                            supportingText = if (passwordMismatch) {
                                {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Passwords do not match", "পাসওয়ার্ড দুটি মিলছে না"),
                                        color = EditorialError
                                    )
                                }
                            } else null,
                            trailingIcon = {
                                IconButton(
                                    onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = StudyQuestStrings.t(
                                            language,
                                            if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password",
                                            if (confirmPasswordVisible) "পাসওয়ার্ড লুকাও" else "পাসওয়ার্ড দেখাও"
                                        )
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !isSigningIn,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (!isSigningIn) {
                                        focusManager.clearFocus()
                                        onEmailSignUp(fullName, email, password, confirmPassword)
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = cleanFieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = StudyQuestStrings.t(language, "Age Group", "শিক্ষার্থীর বয়সসীমা"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "under_13" to StudyQuestStrings.t(language, "Under 13", "১৩-এর নিচে"),
                                    "13_17" to StudyQuestStrings.t(language, "13–17", "১৩–১৭"),
                                    "18_plus" to StudyQuestStrings.t(language, "18+", "১৮+")
                                ).forEach { (bracketId, label) ->
                                    FilterChip(
                                        selected = selectedAgeBracket == bracketId,
                                        onClick = {
                                            selectedAgeBracket = bracketId
                                            val consent = if (bracketId != "under_13" || guardianApproved) "verified" else "pending"
                                            onAgeAndConsentSelected(bracketId, consent)
                                        },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag("auth_age_$bracketId")
                                    )
                                }
                            }
                            if (selectedAgeBracket == "under_13") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = guardianApproved,
                                        onCheckedChange = { checked ->
                                            guardianApproved = checked
                                            onAgeAndConsentSelected("under_13", if (checked) "verified" else "pending")
                                        },
                                        modifier = Modifier.testTag("auth_guardian_consent_checkbox")
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Parent or guardian approved this study account (limits public feed).",
                                            "অভিভাবকের সম্মতি রয়েছে (পাবলিক ফিড সীমিত থাকবে)।"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    resetEmail = email.trim()
                                    onClearPasswordResetStatus()
                                    showResetDialog = true
                                },
                                enabled = !isSigningIn,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("auth_forgot_password_btn")
                            ) {
                                Text(
                                    text = StudyQuestStrings.t(language, "Forgot password?", "পাসওয়ার্ড ভুলে গেছ?"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Primary Submit Pill CTA
                    Button(
                        onClick = {
                            userInteractedWithAuth = true
                            silentAutoSignInJob?.cancel()
                            focusManager.clearFocus()
                            if (!isSignUp) {
                                onEmailSignIn(email, password)
                            } else {
                                onAgeAndConsentSelected(
                                    selectedAgeBracket,
                                    if (selectedAgeBracket != "under_13" || guardianApproved) "verified" else "pending"
                                )
                                onEmailSignUp(fullName, email, password, confirmPassword)
                            }
                        },
                        enabled = !isSigningIn && (!isSignUp || selectedAgeBracket != "under_13" || guardianApproved),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("email_auth_submit_button")
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.4.dp
                            )
                        } else {
                            Text(
                                text = if (!isSignUp) {
                                    StudyQuestStrings.t(language, "Sign In", "সাইন-ইন")
                                } else {
                                    StudyQuestStrings.t(language, "Create Account", "অ্যাকাউন্ট তৈরি করো")
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = StudyQuestStrings.t(language, "or", "অথবা"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    OutlinedButton(
                        onClick = {
                            userInteractedWithAuth = true
                            silentAutoSignInJob?.cancel()
                            focusManager.clearFocus()
                            googleController.launch()
                        },
                        enabled = !isSigningIn,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        GoogleLogoMark(size = 18.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = StudyQuestStrings.t(language, "Continue with Google", "গুগল দিয়ে শুরু করো"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    AuthFeedbackSection(
                        language = language,
                        isSigningIn = isSigningIn,
                        canCancelSignIn = canCancelSignIn,
                        authStatusMessage = authStatusMessage,
                        passwordResetStatus = passwordResetStatus,
                        authError = authError,
                        onCancel = {
                            googleController.cancel()
                            onCancelSignIn()
                        },
                        onRetryGoogle = { googleController.launch() }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = StudyQuestStrings.t(
                                language,
                                "Secured by Firebase Authentication",
                                "Firebase Authentication দ্বারা সুরক্ষিত"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(StudyQuestStrings.t(language, "Reset Password", "পাসওয়ার্ড রিসেট")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "Enter your registered email address and we'll send a password reset link.",
                            "তোমার নিবন্ধিত ইমেইল লিখো, আমরা পাসওয়ার্ড রিসেট লিংক পাঠিয়ে দেব।"
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text(StudyQuestStrings.t(language, "Email Address", "ইমেইল ঠিকানা")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSendPasswordReset(resetEmail)
                        showResetDialog = false
                    },
                    modifier = Modifier.testTag("reset_send_btn")
                ) {
                    Text(StudyQuestStrings.t(language, "Send Link", "লিংক পাঠাও"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(StudyQuestStrings.t(language, "Cancel", "বাতিল"))
                }
            }
        )
    }
}

@Composable
private fun AuthModeSegmentPill(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AuthFeedbackSection(
    language: AppLanguage,
    isSigningIn: Boolean,
    canCancelSignIn: Boolean,
    authStatusMessage: String?,
    passwordResetStatus: Pair<Boolean, String>?,
    authError: String?,
    onCancel: () -> Unit,
    onRetryGoogle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSigningIn) {
            Text(
                text = authStatusMessage ?: StudyQuestStrings.t(
                    language,
                    "Signing in with Google...",
                    "গুগল অ্যাকাউন্ট যাচাই হচ্ছে..."
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_status_message")
            )
        }

        AnimatedVisibility(visible = isSigningIn && canCancelSignIn) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "Retrying automatically in background. You can cancel anytime.",
                        "ব্যাকগ্রাউন্ডে স্বয়ংক্রিয়ভাবে আবার চেষ্টা চলছে। চাইলে বাতিল করতে পারো।"
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("google_sign_in_cancel_button")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(StudyQuestStrings.t(language, "Cancel Sign-In", "সাইন-ইন বাতিল করো"))
                }
            }
        }

        if (passwordResetStatus != null) {
            val (resetOk, resetMsg) = passwordResetStatus
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (resetOk) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                border = BorderStroke(
                    1.dp,
                    (if (resetOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error).copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_reset_banner")
            ) {
                Text(
                    text = resetMsg,
                    color = if (resetOk) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        if (!authError.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_error_banner")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = authError,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedButton(
                        onClick = onRetryGoogle,
                        enabled = !isSigningIn,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(StudyQuestStrings.t(language, "Retry Google Sign-In", "আবার চেষ্টা করো"))
                    }
                }
            }
        }
    }
}

/**
 * 4-Step Progressive Onboarding Wizard.
 * Replaces dense scrollable forms with one clear question, one title, and one sticky primary CTA per step.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AcademicSetupWizardScreen(
    profile: PublicProfile,
    language: AppLanguage,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onSignOut: () -> Unit = {},
    onBack: () -> Unit = onSignOut,
    cloudTextbooks: List<TextbookCatalogItem> = emptyList(),
    onCompleteSetupWithBook: ((
        displayName: String,
        classId: String,
        curriculumVersion: String,
        selectedSubjects: List<String>,
        avatarId: String,
        academicGroup: String,
        dailyGoalMinutes: Long,
        studyGoalTitle: String,
        activeBook: TextbookCatalogItem
    ) -> Unit)? = null,
    onCompleteSetup: (
        displayName: String,
        classId: String,
        curriculumVersion: String,
        selectedSubjects: List<String>,
        avatarId: String,
        academicGroup: String,
        dailyGoalMinutes: Long,
        studyGoalTitle: String
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(0) } // 0=Identity, 1=Class&Stream, 2=Book&Subjects, 3=Goal&Start
    val totalSteps = 4

    BackHandler {
        if (!isSaving) {
            if (step > 0) step -= 1 else onBack()
        }
    }

    val context = LocalContext.current
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var selectedClassId by remember { mutableStateOf(profile.classId.ifBlank { "class_10" }) }
    var selectedVersion by remember { mutableStateOf(profile.curriculumVersion.ifBlank { "bangla" }) }
    var selectedAvatar by remember { mutableStateOf(profile.avatarId.takeIf { it.isNotBlank() && it != "owl_classic" } ?: "crest_indigo") }
    var selectedGroup by remember(profile.academicGroup) {
        mutableStateOf(
            profile.academicGroup.takeIf { it in listOf("science", "business", "humanities") } ?: "science"
        )
    }
    var selectedEditionYear by remember(profile.activeBookYear) {
        mutableStateOf(profile.activeBookYear.takeIf { it in listOf("2026", "2027") } ?: "2026")
    }
    var selectedBookId by remember(profile.activeBookId) { mutableStateOf(profile.activeBookId) }
    var dailyGoalMinutes by remember(profile.dailyGoalMinutes) {
        mutableLongStateOf(profile.dailyGoalMinutes.takeIf { it in listOf(15L, 30L, 45L, 60L) } ?: 30L)
    }
    val goalPresets = remember(language) {
        listOf(
            StudyQuestStrings.t(language, "SSC Board Readiness", "এসএসসি বোর্ড প্রস্তুতি"),
            StudyQuestStrings.t(language, "Weekly Chapter Mastery", "সাপ্তাহিক অধ্যায় দক্ষতা"),
            StudyQuestStrings.t(language, "Daily Focus Habit", "প্রতিদিনের পড়া অভ্যাস"),
            StudyQuestStrings.t(language, "Term Exam Prep", "সাময়িক পরীক্ষার প্রস্তুতি")
        )
    }
    var selectedGoalIndex by remember { mutableIntStateOf(0) }

    val selectedClassInfo = BangladeshCurriculumCatalog.classes.firstOrNull { it.id == selectedClassId }
    val supportsGroups = selectedClassInfo?.supportsGroups == true
    val effectiveGroup = if (supportsGroups) selectedGroup else "general"
    val candidateBooks = remember(selectedEditionYear, selectedClassId, selectedVersion, effectiveGroup, cloudTextbooks) {
        TextbookCatalog.booksFor(
            academicYear = selectedEditionYear,
            classId = selectedClassId,
            curriculumVersion = selectedVersion,
            academicGroup = effectiveGroup,
            extraCloudBooks = cloudTextbooks
        )
    }
    val activeLockedBookChoice = remember(candidateBooks, selectedBookId) {
        candidateBooks.firstOrNull { it.id == selectedBookId }
            ?: candidateBooks.firstOrNull()
            ?: TextbookCatalog.defaultBookFor(selectedClassId, selectedVersion, effectiveGroup, selectedEditionYear, cloudTextbooks)
    }

    val selectedSubjects = remember {
        mutableStateListOf<String>().apply {
            val initialValid = BangladeshCurriculumCatalog.subjectsForClassAndGroup(
                selectedClassId,
                effectiveGroup
            ).map { it.id }.toSet()
            val seeded = profile.selectedSubjects.filter { it in initialValid }
            addAll(seeded.ifEmpty { initialValid.take(4).toList() })
        }
    }
    var notifGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notifGranted = granted }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxSize()
        ) {
            // Minimal Step Chrome + Linear Progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (!isSaving) {
                                if (step > 0) step -= 1 else onBack()
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("setup_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = StudyQuestStrings.t(language, "Back", "ফিরে যাও")
                        )
                    }

                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "Step ${step + 1} of $totalSteps",
                            "ধাপ ${StudyQuestStrings.formatDigits(step + 1, language)}/${StudyQuestStrings.formatDigits(totalSteps, language)}"
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = onSignOut,
                        enabled = !isSaving,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("onboarding_switch_account")
                    ) {
                        Text(
                            text = StudyQuestStrings.t(language, "Switch", "বদলান"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (step + 1).toFloat() / totalSteps.toFloat() },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp))
                )
            }

            // Focused Step Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(140)) },
                    label = "wizard_step_content"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "What should we call you?",
                                            "আপনার নাম কী?"
                                        ),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "ID: ${profile.studyQuestId} • Pick an emblem and display name.",
                                            "আইডি: ${profile.studyQuestId} • একটি প্রতীক ও নাম নির্বাচন করো।"
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AvatarBadgeView(
                                        avatarId = selectedAvatar,
                                        frameId = profile.frameId,
                                        displayName = displayName.ifBlank { profile.displayName },
                                        size = 64.dp
                                    )
                                    OutlinedTextField(
                                        value = displayName,
                                        onValueChange = { displayName = it },
                                        label = { Text(StudyQuestStrings.t(language, "Student Name", "শিক্ষার্থীর নাম")) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("setup_name_input")
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Starter Emblem", "পছন্দের প্রতীক"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        BangladeshCurriculumCatalog.cosmeticsCatalog
                                            .filter { it.category == "avatar" && it.costCoins == 0 }
                                            .forEach { cos ->
                                                FilterChip(
                                                    selected = selectedAvatar == cos.id,
                                                    onClick = { selectedAvatar = cos.id },
                                                    label = { Text(StudyQuestStrings.t(language, cos.nameEn, cos.nameBn)) },
                                                    modifier = Modifier.testTag("setup_avatar_${cos.id}")
                                                )
                                            }
                                    }
                                }
                            }
                        }

                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Select your class & stream.",
                                            "আপনার শ্রেণি ও ভার্সন নির্বাচন করো।"
                                        ),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Classes 6–10 & SSC aligned with NCTB curriculum.",
                                            "এনসিটিবি পাঠ্যক্রম অনুযায়ী ৬ষ্ঠ–১০ম শ্রেণি ও এসএসসি।"
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Class", "শ্রেণি"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        BangladeshCurriculumCatalog.classes.filter { it.futureReadyTag == null }.forEach { cls ->
                                            FilterChip(
                                                selected = selectedClassId == cls.id,
                                                onClick = {
                                                    selectedClassId = cls.id
                                                    val nextGroup = if (cls.supportsGroups) selectedGroup else "general"
                                                    val validIds = BangladeshCurriculumCatalog.subjectsForClassAndGroup(cls.id, nextGroup).map { it.id }
                                                    selectedSubjects.retainAll(validIds.toSet())
                                                    if (selectedSubjects.isEmpty()) selectedSubjects.addAll(validIds.take(4))
                                                },
                                                label = { Text(StudyQuestStrings.t(language, cls.labelEn, cls.labelBn)) },
                                                modifier = Modifier.testTag("setup_class_${cls.id}")
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Version", "পাঠ্যক্রম ভার্সন"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        CurriculumVersion.entries.forEach { ver ->
                                            FilterChip(
                                                selected = selectedVersion == ver.id,
                                                onClick = { selectedVersion = ver.id },
                                                label = { Text(StudyQuestStrings.t(language, ver.labelEn, ver.labelBn)) },
                                                modifier = Modifier.testTag("setup_version_${ver.id}")
                                            )
                                        }
                                    }
                                }

                                if (supportsGroups) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = StudyQuestStrings.t(language, "Academic Group", "বিভাগ নির্বাচন"),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AcademicGroup.entries.filter { it != AcademicGroup.GENERAL }.forEach { grp ->
                                                FilterChip(
                                                    selected = selectedGroup == grp.id,
                                                    onClick = {
                                                        selectedGroup = grp.id
                                                        val validIds = BangladeshCurriculumCatalog.subjectsForClassAndGroup(selectedClassId, grp.id).map { it.id }
                                                        selectedSubjects.clear()
                                                        selectedSubjects.addAll(validIds.take(5))
                                                    },
                                                    label = { Text(StudyQuestStrings.t(language, grp.labelEn, grp.labelBn)) },
                                                    modifier = Modifier.testTag("setup_group_${grp.id}")
                                                )
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("class_lock_policy_note")
                                ) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Academic Year Lock: Your class stays locked for this cycle and auto-promotes each Jan 1 (Asia/Dhaka) when eligible.",
                                            "শিক্ষাবর্ষ লক নীতি: নির্বাচিত শ্রেণি বর্তমান শিক্ষাবর্ষের জন্য লক থাকবে এবং প্রতি ১ জানুয়ারি স্বয়ংক্রিয়ভাবে পরবর্তী শ্রেণিতে উন্নীত হবে।"
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }

                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Lock your active textbook.",
                                            "সক্রিয় পাঠ্যবই নির্বাচন করো।"
                                        ),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Your reader, practice drills, and AI explanations stay focused on your active book.",
                                            "পাঠ্যবই পড়া, অনুশীলন ও AI সমাধান আপনার নির্বাচিত বইয়ের ওপর ভিত্তি করে চলবে।"
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("2026" to "2026 NCTB", "2027" to "2027 NCTB").forEach { (yr, label) ->
                                        FilterChip(
                                            selected = selectedEditionYear == yr,
                                            onClick = { selectedEditionYear = yr },
                                            label = { Text(label) },
                                            modifier = Modifier.testTag("setup_year_$yr")
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    candidateBooks.take(6).forEach { b ->
                                        val isSelected = activeLockedBookChoice.id == b.id
                                        Surface(
                                            onClick = { selectedBookId = b.id },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("setup_book_${b.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = StudyQuestStrings.t(language, b.titleEn, b.titleBn),
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${b.bookCode} • ${b.academicYear}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Subjects", "বিষয়সমূহ"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val availableSubjects = BangladeshCurriculumCatalog.subjectsForClassAndGroup(selectedClassId, effectiveGroup)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        availableSubjects.forEach { subj ->
                                            val active = subj.id in selectedSubjects
                                            FilterChip(
                                                selected = active,
                                                onClick = {
                                                    if (active && selectedSubjects.size > 1) selectedSubjects.remove(subj.id)
                                                    else if (!active) selectedSubjects.add(subj.id)
                                                },
                                                label = { Text(StudyQuestStrings.t(language, subj.titleEn, subj.titleBn)) },
                                                modifier = Modifier.testTag("setup_subject_${subj.id}")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Set your daily goal.",
                                            "দৈনিক পড়ার লক্ষ্য ঠিক করো।"
                                        ),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = StudyQuestStrings.t(
                                            language,
                                            "Choose a sustainable daily rhythm to maintain your study streak.",
                                            "প্রতিদিনের স্ট্রিক ধরে রাখতে সহজ একটি সময় নির্ধারণ করো।"
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Daily Target", "দৈনিক সময়"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(15L, 30L, 45L, 60L).forEach { mins ->
                                            FilterChip(
                                                selected = dailyGoalMinutes == mins,
                                                onClick = { dailyGoalMinutes = mins },
                                                label = {
                                                    Text(StudyQuestStrings.t(language, "$mins min / day", "দৈনিক $mins মিনিট"))
                                                },
                                                modifier = Modifier.testTag("setup_daily_goal_$mins")
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = StudyQuestStrings.t(language, "Primary Focus", "মূল লক্ষ্য"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        goalPresets.forEachIndexed { idx, label ->
                                            FilterChip(
                                                selected = selectedGoalIndex == idx,
                                                onClick = { selectedGoalIndex = idx },
                                                label = { Text(label) },
                                                modifier = Modifier.testTag("setup_goal_$idx")
                                            )
                                        }
                                    }
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifGranted) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = StudyQuestStrings.t(language, "Study Reminders", "পড়ার রিমাইন্ডার"),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = StudyQuestStrings.t(language, "Enable reminders for spaced revision & challenges.", "রিভিশন ও চ্যালেঞ্জের রিমাইন্ডার পেতে চালু করো।"),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            OutlinedButton(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                                Text(StudyQuestStrings.t(language, "Allow", "অনুমতি"))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sticky Single Primary Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (step < totalSteps - 1) {
                    Button(
                        onClick = { step += 1 },
                        enabled = !isSaving && (step != 2 || selectedSubjects.isNotEmpty()),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("setup_next_step_btn")
                    ) {
                        Text(
                            text = StudyQuestStrings.t(language, "Continue", "এগিয়ে যাও"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Button(
                        onClick = {
                            val goal = goalPresets.getOrElse(selectedGoalIndex) { goalPresets.first() }
                            if (onCompleteSetupWithBook != null) {
                                onCompleteSetupWithBook(
                                    displayName.ifBlank { profile.displayName },
                                    selectedClassId,
                                    selectedVersion,
                                    selectedSubjects.toList(),
                                    selectedAvatar,
                                    effectiveGroup,
                                    dailyGoalMinutes,
                                    goal,
                                    activeLockedBookChoice
                                )
                            } else {
                                onCompleteSetup(
                                    displayName.ifBlank { profile.displayName },
                                    selectedClassId,
                                    selectedVersion,
                                    selectedSubjects.toList(),
                                    selectedAvatar,
                                    effectiveGroup,
                                    dailyGoalMinutes,
                                    goal
                                )
                            }
                        },
                        enabled = !isSaving && selectedSubjects.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("complete_setup_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = StudyQuestStrings.t(language, "Start Learning", "শেখা শুরু করো"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
