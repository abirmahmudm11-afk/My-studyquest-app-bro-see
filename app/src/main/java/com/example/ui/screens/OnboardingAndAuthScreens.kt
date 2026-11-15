package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.data.model.AcademicGroup
import com.example.data.model.CurriculumVersion
import com.example.data.model.MascotMood
import com.example.data.model.PublicProfile
import com.example.ui.components.AvatarBadgeView
import com.example.ui.components.MascotAvatar
import com.example.ui.components.MascotTituCard
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.StudyQuestCard
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun rememberGoogleSignInLauncher(
    language: AppLanguage,
    onSigningInChanged: (Boolean) -> Unit,
    onIdTokenReceived: (String) -> Unit,
    onAuthError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val webClientId = remember(context) { context.getString(R.string.default_web_client_id) }

    val latestOnSigningInChanged by rememberUpdatedState(onSigningInChanged)
    val latestOnIdTokenReceived by rememberUpdatedState(onIdTokenReceived)
    val latestOnAuthError by rememberUpdatedState(onAuthError)

    return remember(context, activity, language, webClientId) {
        {
            scope.launch {
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
        }
    }
}

@Composable
fun LanguagePickerLaunchScreen(
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
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
            Image(
                painter = painterResource(id = R.drawable.ic_studyquest_logo),
                contentDescription = "StudyQuest",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
            Text(
                text = "StudyQuest",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
                    .clickable { onSelectLanguage(AppLanguage.BANGLA) }
                    .testTag("pick_lang_bn"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "বাংলা (Bangla)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "বাংলাদেশ বোর্ড পাঠ্যক্রম, প্রশ্ন ও ইন্টারফেস",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                    Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clickable { onSelectLanguage(AppLanguage.ENGLISH) }
                    .testTag("pick_lang_en"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "English (ইংরেজি)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "English interface & NCTB English/Bangla Version",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
    onAuthError: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val webClientId = remember(context) { context.getString(R.string.default_web_client_id) }
    val scrollState = rememberScrollState()

    // Separate GetGoogleIdOption silent auto sign-in on welcome screen launch
    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser != null || isSigningIn) return@LaunchedEffect
        try {
            val autoOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            val req = GetCredentialRequest.Builder().addCredentialOption(autoOption).build()
            val result = credentialManager.getCredential(activity ?: context, req)
            val cred = result.credential
            if (!isSigningIn &&
                cred is CustomCredential &&
                cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(cred.data)
                if (googleCred.idToken.isNotBlank()) {
                    onIdTokenReceived(googleCred.idToken)
                }
            }
        } catch (_: NoCredentialException) {
            // Expected on first install when no authorized account is cached
        } catch (_: GetCredentialCancellationException) {
            // Silent flow cancelled
        } catch (e: GetCredentialCustomException) {
            Log.w("StudyQuestAuth", "Silent auto sign-in custom error: ${e.errorMessage ?: e.message}")
        } catch (e: Exception) {
            Log.d("StudyQuestAuth", "Silent auto sign-in skipped: ${e.message}")
        }
    }

    LaunchedEffect(authError) {
        if (!authError.isNullOrBlank()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val launchInteractiveGoogleSignIn = rememberGoogleSignInLauncher(
        language = language,
        onSigningInChanged = onSigningInChanged,
        onIdTokenReceived = onIdTokenReceived,
        onAuthError = onAuthError
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .safeDrawingPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MascotAvatar(size = 38.dp)
                    Text("StudyQuest", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        onSetLanguage(if (language == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA)
                    },
                    modifier = Modifier.testTag("toggle_lang_welcome")
                ) {
                    Text(if (language == AppLanguage.BANGLA) "English" else "বাংলা")
                }
            }

            StaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_study),
                    contentDescription = "Bangladeshi students learning together",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            StaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                Text(
                    text = StudyQuestStrings.t(
                        language,
                        "Master Classes 6–10 & SSC with Joy",
                        "আনন্দ ও আত্মবিশ্বাসের সাথে শ্রেণি ৬–১০ এবং এসএসসি প্রস্তুতি"
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            StaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                StudyQuestCard {
                    FeatureBulletRow(Icons.Filled.School, StudyQuestStrings.t(language, "NCTB-Aligned Lessons, MCQ & CQ Bank + Mock Exams", "এনসিটিবি পাঠ, MCQ-CQ প্রশ্নব্যাংক ও মডেল টেস্ট"))
                    Spacer(Modifier.height(10.dp))
                    FeatureBulletRow(Icons.Filled.EmojiEvents, StudyQuestStrings.t(language, "Live 1v1 Battles, Solo Sprints & Class Leaderboards", "১v১ কুইজ যুদ্ধ, সোলো স্প্রিন্ট ও লিডারবোর্ড"))
                    Spacer(Modifier.height(10.dp))
                    FeatureBulletRow(Icons.Filled.AutoAwesome, StudyQuestStrings.t(language, "Step-by-step AI Doubt Solver & Offline Chapter Downloads", "ধাপে ধাপে AI সমাধান ও অফলাইন অধ্যায় ডাউনলোড"))
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
                    Text(
                        text = authError,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            StaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { launchInteractiveGoogleSignIn() },
                        enabled = !isSigningIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = StudyQuestStrings.t(language, "Continue with Google", "গুগল দিয়ে শুরু করুন"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isSigningIn) {
                        Text(
                            text = StudyQuestStrings.t(
                                language,
                                "Signing in with Google...",
                                "গুগল অ্যাকাউন্ট যাচাই হচ্ছে..."
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = StudyQuestStrings.t(
                    language,
                    "Sign in with your Google account to sync your Class 6–10 progress, streak, flashcards, and leaderboard rank across devices.",
                    "তোমার শ্রেণি ৬–১০ অগ্রগতি, স্ট্রিক, ফ্ল্যাশকার্ড এবং লিডারবোর্ড র‍্যাংক সিঙ্ক রাখতে গুগল অ্যাকাউন্ট দিয়ে সাইন-ইন করো।"
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureBulletRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
