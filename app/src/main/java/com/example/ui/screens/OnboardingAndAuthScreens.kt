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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import com.example.core.SurfaceStyle
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.AcademicGroup
import com.example.data.model.CurriculumVersion
import com.example.data.model.MascotMood
import com.example.data.model.PublicProfile
import com.example.ui.components.AvatarBadgeView
import com.example.ui.components.GoogleLogoMark
import com.example.ui.components.MascotAvatar
import com.example.ui.components.MascotTituCard
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestAmbientBackdrop
import com.example.ui.components.StudyQuestCard
import com.example.ui.theme.BerryViolet
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LocalStudyQuestSurfaceStyle
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SunsetCoral
import com.example.ui.theme.VividEmerald
import com.example.ui.theme.studyQuestGlassAwareBorder
import com.example.ui.theme.studyQuestGlassAwareSurfaceColor
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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

@Composable
fun LanguagePickerLaunchScreen(
    onSelectLanguage: (AppLanguage) -> Unit
) {
    val visualConfig = LocalStudyQuestSurfaceStyle.current
    StudyQuestAmbientBackdrop(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 10.dp,
                    border = BorderStroke(2.dp, ElectricIndigo.copy(alpha = 0.28f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_studyquest_logo),
                        contentDescription = "StudyQuest",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(26.dp))
                    )
                }
                Text(
                    text = "StudyQuest",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricIndigo
                )
                Text(
                    text = "Learn. Connect. Compete. Achieve.\nশিখুন • যুক্ত হোন • প্রতিযোগিতা করুন • অর্জন করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MascotTituCard(
                    mood = MascotMood.WELCOME,
                    messageEn = "Assalamu Alaikum! I am Titu. Choose your preferred language to start your academic journey.",
                    messageBn = "আসসালামু আলাইকুম! আমি টিটু। তোমার পড়াশোনার যাত্রা শুরু করতে ভাষা নির্বাচন করো।",
                    language = AppLanguage.BANGLA,
                    reducedMotion = false
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onSelectLanguage(AppLanguage.BANGLA) }
                        .testTag("pick_lang_bn"),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.Transparent,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ElectricIndigo, BerryViolet)
                                )
                            )
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "বাংলা (Bangla)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "বাংলাদেশ বোর্ড পাঠ্যক্রম, প্রশ্ন ও ইন্টারফেস",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }
                        Icon(Icons.Filled.Language, contentDescription = null, tint = Color.White)
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onSelectLanguage(AppLanguage.ENGLISH) }
                        .testTag("pick_lang_en"),
                    shape = RoundedCornerShape(22.dp),
                    color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        if (visualConfig.isGlass) 1.3.dp else 1.6.dp,
                        ElectricIndigo.copy(alpha = 0.65f)
                    ),
                    tonalElevation = if (visualConfig.isGlass) 0.dp else 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "English (ইংরেজি)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ElectricIndigo
                            )
                            Text(
                                text = "English interface & NCTB English/Bangla Version",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.Language, contentDescription = null, tint = ElectricIndigo)
                    }
                }
            }
        }
    }
}

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
    onClearPasswordResetStatus: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val focusManager = LocalFocusManager.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val webClientId = remember(context) { context.getString(R.string.default_web_client_id) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val surfaceStyle = LocalStudyQuestSurfaceStyle.current

    // 0 = Sign In, 1 = Create Account
    var authMode by remember { mutableIntStateOf(0) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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

    BackHandler(enabled = !showResetDialog) {
        userInteractedWithAuth = true
        silentAutoSignInJob?.cancel()
        googleController.cancel()
        onCancelSignIn()
        onBackToLanguagePicker()
    }

    DisposableEffect(Unit) {
        onDispose {
            silentAutoSignInJob?.cancel()
        }
    }

    // Separate bounded silent auto sign-in; cancelled immediately on any manual user interaction
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
                // Expected when no authorized account is cached
            } catch (_: GetCredentialCancellationException) {
                // Silent flow cancelled
            } catch (_: CancellationException) {
                // Cancelled cleanly because user interacted manually
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

    val heroGradient = Brush.linearGradient(
        colors = listOf(
            ElectricIndigo,
            BerryViolet,
            SkyCyan.copy(alpha = 0.90f)
        )
    )

    StudyQuestAmbientBackdrop(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vivid Gradient Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroGradient)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = size.minDimension * 0.48f,
                        center = Offset(size.width * 0.88f, size.height * 0.18f)
                    )
                    drawCircle(
                        color = SunsetCoral.copy(alpha = 0.16f),
                        radius = size.minDimension * 0.34f,
                        center = Offset(size.width * 0.12f, size.height * 0.78f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.18f)
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
                                        contentDescription = StudyQuestStrings.t(
                                            language,
                                            "Back to language picker",
                                            "ভাষা নির্বাচনে ফিরে যাও"
                                        ),
                                        tint = Color.White
                                    )
                                }
                            }
                            MascotAvatar(size = 34.dp)
                            Text(
                                text = "StudyQuest",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(999.dp))
                                .clickable {
                                    onSetLanguage(
                                        if (language == AppLanguage.BANGLA) AppLanguage.ENGLISH
                                        else AppLanguage.BANGLA
                                    )
                                }
                                .testTag("toggle_lang_welcome")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Language,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "English" else "বাংলা",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Welcome Back, Scholar",
                                    "স্বাগতম, প্রিয় শিক্ষার্থী"
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = StudyQuestStrings.t(
                                    language,
                                    "Sync Classes 6–10 & SSC lessons, mock exams, streaks, and AI tutor across devices.",
                                    "শ্রেণি ৬–১০ ও এসএসসি পাঠ, মক টেস্ট, স্ট্রিক এবং এআই টিউটর সব ডিভাইসে সিঙ্ক রাখো।"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.90f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 420.dp)
                            )
                        }
                    }
                }
            }

            // Floating Auth Card overlapping hero bottom
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = studyQuestGlassAwareSurfaceColor(MaterialTheme.colorScheme.surface),
                        tonalElevation = if (surfaceStyle.isGlass) 0.dp else 4.dp,
                        shadowElevation = if (surfaceStyle.isGlass) 0.dp else 10.dp,
                        border = studyQuestGlassAwareBorder(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Segmented Sign In / Create Account selector
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val modes = listOf(
                                        0 to StudyQuestStrings.t(language, "Sign In", "সাইন-ইন"),
                                        1 to StudyQuestStrings.t(language, "Create Account", "নতুন অ্যাকাউন্ট")
                                    )
                                    modes.forEach { (idx, label) ->
                                        val selected = authMode == idx
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                            tonalElevation = if (selected) 2.dp else 0.dp,
                                            shadowElevation = if (selected) 1.dp else 0.dp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .minimumInteractiveComponentSize()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable(enabled = !isSigningIn) {
                                                    authMode = idx
                                                    onClearAuthMessages()
                                                }
                                                .testTag(if (idx == 0) "auth_mode_sign_in" else "auth_mode_sign_up")
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            AnimatedContent(
                                targetState = authMode,
                                transitionSpec = {
                                    if (reducedMotion) {
                                        fadeIn(tween(10)) togetherWith fadeOut(tween(10))
                                    } else {
                                        fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                                    }
                                },
                                label = "auth_mode_fields"
                            ) { mode ->
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (mode == 1) {
                                        OutlinedTextField(
                                            value = fullName,
                                            onValueChange = { fullName = it },
                                            label = { Text(StudyQuestStrings.t(language, "Full Name", "তোমার পুরো নাম")) },
                                            leadingIcon = {
                                                Icon(Icons.Filled.Person, contentDescription = null)
                                            },
                                            singleLine = true,
                                            enabled = !isSigningIn,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("auth_name_input")
                                        )
                                    }

                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text(StudyQuestStrings.t(language, "Email Address", "ইমেইল ঠিকানা")) },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Email, contentDescription = null)
                                        },
                                        singleLine = true,
                                        enabled = !isSigningIn,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Next
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("auth_email_input")
                                    )

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text(StudyQuestStrings.t(language, "Password", "পাসওয়ার্ড")) },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Lock, contentDescription = null)
                                        },
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
                                            imeAction = if (mode == 0) ImeAction.Done else ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (mode == 0 && !isSigningIn) {
                                                    focusManager.clearFocus()
                                                    onEmailSignIn(email, password)
                                                }
                                            }
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("auth_password_input")
                                    )

                                    if (mode == 1) {
                                        OutlinedTextField(
                                            value = confirmPassword,
                                            onValueChange = { confirmPassword = it },
                                            label = { Text(StudyQuestStrings.t(language, "Confirm Password", "পাসওয়ার্ড নিশ্চিত করো")) },
                                            leadingIcon = {
                                                Icon(Icons.Filled.Lock, contentDescription = null)
                                            },
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
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("auth_confirm_password_input")
                                        )
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
                                                    text = StudyQuestStrings.t(language, "Forgot Password?", "পাসওয়ার্ড ভুলে গেছ?"),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            userInteractedWithAuth = true
                                            silentAutoSignInJob?.cancel()
                                            focusManager.clearFocus()
                                            if (mode == 0) {
                                                onEmailSignIn(email, password)
                                            } else {
                                                onEmailSignUp(fullName, email, password, confirmPassword)
                                            }
                                        },
                                        enabled = !isSigningIn,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("email_auth_submit_button")
                                    ) {
                                        if (isSigningIn) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.5.dp
                                            )
                                        } else {
                                            Text(
                                                text = if (mode == 0) {
                                                    StudyQuestStrings.t(language, "Sign In with Email", "ইমেইল দিয়ে সাইন-ইন")
                                                } else {
                                                    StudyQuestStrings.t(language, "Create Free Account", "নতুন অ্যাকাউন্ট তৈরি করো")
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Text(
                                    text = StudyQuestStrings.t(language, "or continue with", "অথবা চালিয়ে যান"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    userInteractedWithAuth = true
                                    silentAutoSignInJob?.cancel()
                                    focusManager.clearFocus()
                                    googleController.launch()
                                },
                                enabled = !isSigningIn,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("google_sign_in_button")
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    GoogleLogoMark(size = 20.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = StudyQuestStrings.t(language, "Continue with Google", "গুগল দিয়ে শুরু করুন"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

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
                                        onClick = {
                                            googleController.cancel()
                                            onCancelSignIn()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .minimumInteractiveComponentSize()
                                            .testTag("google_sign_in_cancel_button")
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            StudyQuestStrings.t(
                                                language,
                                                "Cancel Sign-In",
                                                "সাইন-ইন বাতিল করো"
                                            )
                                        )
                                    }
                                }
                            }
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
                            (if (resetOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error).copy(alpha = 0.4f)
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
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
                                onClick = { googleController.launch() },
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

                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "Secured by Firebase Authentication. Your NCTB lessons, flashcards, and progress sync safely.",
                        "Firebase Authentication দ্বারা সুরক্ষিত। তোমার এনসিটিবি পাঠ, ফ্ল্যাশকার্ড এবং অগ্রগতি নিরাপদে সিঙ্ক থাকে।"
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(StudyQuestStrings.t(language, "Reset Password", "পাসওয়ার্ড রিসেট"))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = StudyQuestStrings.t(
                            language,
                            "Enter your registered email address and we'll send a Firebase password reset link.",
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
private fun FeatureBulletRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AcademicSetupWizardScreen(
    profile: PublicProfile,
    language: AppLanguage,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onSignOut: () -> Unit = {},
    onBack: () -> Unit = onSignOut,
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
    BackHandler {
        onBack()
    }
    val context = LocalContext.current
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var selectedClassId by remember { mutableStateOf(profile.classId.ifBlank { "class_10" }) }
    var selectedVersion by remember { mutableStateOf(profile.curriculumVersion.ifBlank { "bangla" }) }
    var selectedAvatar by remember { mutableStateOf(profile.avatarId.ifBlank { "owl_classic" }) }
    var selectedGroup by remember(profile.academicGroup) {
        mutableStateOf(
            profile.academicGroup.takeIf { it in listOf("science", "business", "humanities") } ?: "science"
        )
    }
    var dailyGoalMinutes by remember(profile.dailyGoalMinutes) {
        mutableLongStateOf(profile.dailyGoalMinutes.takeIf { it in listOf(15L, 30L, 45L, 60L) } ?: 30L)
    }
    val goalPresets = remember(language) {
        listOf(
            StudyQuestStrings.t(language, "SSC Board Exam Readiness", "এসএসসি বোর্ড পরীক্ষার পূর্ণ প্রস্তুতি"),
            StudyQuestStrings.t(language, "Weekly Chapter Mastery", "সাপ্তাহিক অধ্যায়ভিত্তিক দক্ষতা"),
            StudyQuestStrings.t(language, "Daily Streak & Focus Habit", "প্রতিদিনের পড়া ও স্ট্রিক অভ্যাস"),
            StudyQuestStrings.t(language, "School Term Exam Prep", "স্কুলের সাময়িক পরীক্ষার প্রস্তুতি")
        )
    }
    var selectedGoalIndex by remember { mutableIntStateOf(0) }

    val selectedClassInfo = BangladeshCurriculumCatalog.classes.firstOrNull { it.id == selectedClassId }
    val supportsGroups = selectedClassInfo?.supportsGroups == true
    val effectiveGroup = if (supportsGroups) selectedGroup else "general"

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

    StudyQuestAmbientBackdrop(
        modifier = Modifier.fillMaxSize()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { if (!isSaving) onBack() },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("setup_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = StudyQuestStrings.t(language, "Back", "ফিরে যাও")
                    )
                }
                MascotAvatar(size = 32.dp)
                Column {
                    Text(
                        text = StudyQuestStrings.t(language, "Student Onboarding", "শিক্ষার্থী প্রোফাইল সেটআপ"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile.studyQuestId,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            TextButton(
                onClick = onSignOut,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("onboarding_switch_account")
            ) {
                Text(StudyQuestStrings.t(language, "Switch Account", "অ্যাকাউন্ট বদলান"))
            }
        }

        MascotTituCard(
            mood = MascotMood.LEARNING_ASSISTANCE,
            messageEn = "Welcome! Let's personalize your Class, SSC Group, NCTB Version, Subjects & Daily Target.",
            messageBn = "স্বাগতম! চলো তোমার শ্রেণি, বিভাগ, ভার্সন, বিষয় ও দৈনিক লক্ষ্য সেট করি।",
            language = language,
            reducedMotion = false
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvatarBadgeView(
                avatarId = selectedAvatar,
                frameId = profile.frameId,
                displayName = displayName.ifBlank { profile.displayName },
                size = 56.dp
            )
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(StudyQuestStrings.t(language, "Your Student Name", "তোমার নাম")) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("setup_name_input")
            )
        }

        Text(
            text = StudyQuestStrings.t(language, "Select Class (6–10 & SSC)", "শ্রেণি নির্বাচন করো"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("class_lock_policy_note")
        ) {
            Text(
                text = StudyQuestStrings.t(
                    language,
                    "Academic Year Lock: Your selected class stays locked for this Bangladesh (Asia/Dhaka) academic year and automatically promotes every Jan 1 (even if you join late in December), unless the annual exam is not passed.",
                    "শিক্ষাবর্ষ লক নীতি: নির্বাচিত শ্রেণি বর্তমান শিক্ষাবর্ষের জন্য লক থাকবে এবং প্রতি ১ জানুয়ারি স্বয়ংক্রিয়ভাবে পরবর্তী শ্রেণিতে উন্নীত হবে (বার্ষিক পরীক্ষায় অনুত্তীর্ণ না হলে)।"
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }

        if (supportsGroups) {
            Text(
                text = StudyQuestStrings.t(language, "SSC Academic Group", "এসএসসি বিভাগ নির্বাচন"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

        Text(
            text = StudyQuestStrings.t(language, "Curriculum Version", "পাঠ্যক্রম ভার্সন"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
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

        Text(
            text = StudyQuestStrings.t(language, "Your Subjects", "তোমার বিষয়সমূহ"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        val availableSubjects = BangladeshCurriculumCatalog.subjectsForClassAndGroup(selectedClassId, effectiveGroup)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

        Text(
            text = StudyQuestStrings.t(language, "Daily Study Target", "দৈনিক পড়ার লক্ষ্য"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

        Text(
            text = StudyQuestStrings.t(language, "Primary Study Goal", "প্রাথমিক অধ্যয়ন লক্ষ্য"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            goalPresets.forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedGoalIndex == idx,
                    onClick = { selectedGoalIndex = idx },
                    label = { Text(label) },
                    modifier = Modifier.testTag("setup_goal_$idx")
                )
            }
        }

        Text(
            text = StudyQuestStrings.t(language, "Choose Starter Companion", "পছন্দের অ্যাভাটার"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BangladeshCurriculumCatalog.cosmeticsCatalog.filter { it.category == "avatar" && it.costCoins == 0 }.forEach { cos ->
                FilterChip(
                    selected = selectedAvatar == cos.id,
                    onClick = { selectedAvatar = cos.id },
                    label = { Text(StudyQuestStrings.t(language, cos.nameEn, cos.nameBn)) },
                    modifier = Modifier.testTag("setup_avatar_${cos.id}")
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifGranted) {
            StudyQuestCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = StudyQuestStrings.t(language, "Study & Battle Reminders", "পড়া ও কুইজ রিমাইন্ডার"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = StudyQuestStrings.t(language, "Allow notifications for spaced repetition reminders and friend challenges.", "রিভিশন ও বন্ধুর চ্যালেঞ্জের জন্য নোটিফিকেশন চালু রাখো।"),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                        Text(StudyQuestStrings.t(language, "Allow", "অনুমতি"))
                    }
                }
            }
        }

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

        Button(
            onClick = {
                onCompleteSetup(
                    displayName.ifBlank { profile.displayName },
                    selectedClassId,
                    selectedVersion,
                    selectedSubjects.toList(),
                    selectedAvatar,
                    effectiveGroup,
                    dailyGoalMinutes,
                    goalPresets.getOrElse(selectedGoalIndex) { goalPresets.first() }
                )
            },
            enabled = !isSaving && selectedSubjects.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("complete_setup_button")
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = StudyQuestStrings.t(language, "Start Learning with StudyQuest", "শেখা শুরু করুন"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    }
}
