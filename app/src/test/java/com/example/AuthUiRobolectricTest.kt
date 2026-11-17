package com.example

import android.content.Context
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.core.AppLanguage
import com.example.core.StudyQuestPreferences
import com.example.ui.screens.WelcomeAndGoogleSignInScreen
import com.example.ui.theme.StudyQuestTheme
import com.example.ui.viewmodel.PreAuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthUiRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun ensureFirebaseAppInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.studyquest.bdxkpq")
                    .setProjectId("demo-no-project")
                    .setApiKey("fake-api-key-for-robolectric")
                    .build()
            )
        }
    }

    @Test
    fun welcomeSignInScreen_displaysErrorBannerAndSigningInState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)

        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                WelcomeAndGoogleSignInScreen(
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isSigningIn = true,
                    authError = "Google Sign-In timed out while contacting Firebase.",
                    authStatusMessage = "Still contacting Firebase (retrying automatically in background, attempt #2)...",
                    onSetLanguage = {},
                    onSigningInChanged = {},
                    onIdTokenReceived = {},
                    onAuthError = {}
                )
            }
        }

        composeRule.onNodeWithTag("auth_error_banner").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Google Sign-In timed out while contacting Firebase.").assertIsDisplayed()
        composeRule.onNodeWithTag("google_sign_in_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("email_auth_submit_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("auth_status_message").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Still contacting Firebase (retrying automatically in background, attempt #2)...").assertIsDisplayed()
    }

    @Test
    fun preAuthViewModel_blankTokenSurfacesErrorWithoutHanging() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)
        val vm = PreAuthViewModel(StudyQuestPreferences(context))

        vm.setSigningIn(true)
        vm.signInWithGoogleIdToken("   ")

        assertFalse(vm.isSigningIn.value)
        assertNotNull(vm.authMessage.value)
        assertEquals(
            "Google Sign-In did not return a valid ID token. Please try again.",
            vm.authMessage.value
        )
    }

    @Test
    fun welcomeSignInScreen_cancelButtonShownWhenCanCancelSignInIsTrue() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)

        var cancelClicked = false
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                WelcomeAndGoogleSignInScreen(
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isSigningIn = true,
                    authError = null,
                    onSetLanguage = {},
                    onSigningInChanged = {},
                    onIdTokenReceived = {},
                    onAuthError = {},
                    canCancelSignIn = true,
                    onCancelSignIn = { cancelClicked = true },
                    onBackToLanguagePicker = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("google_sign_in_cancel_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("google_sign_in_cancel_button").performClick()
        composeRule.waitForIdle()
        assertTrue(cancelClicked)
    }

    @Test
    fun welcomeSignInScreen_hidesCancelButtonBeforeDelayElapsed() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)

        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                WelcomeAndGoogleSignInScreen(
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isSigningIn = true,
                    authError = null,
                    onSetLanguage = {},
                    onSigningInChanged = {},
                    onIdTokenReceived = {},
                    onAuthError = {},
                    canCancelSignIn = false
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("google_sign_in_cancel_button").assertDoesNotExist()
    }

    @Test
    fun welcomeSignInScreen_emailSignInSignUpAndResetPasswordWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)

        var signedInEmail = ""
        var signedInPassword = ""
        var signedUpName = ""
        var signedUpEmail = ""
        var resetRequestedEmail = ""

        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                WelcomeAndGoogleSignInScreen(
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isSigningIn = false,
                    authError = null,
                    passwordResetStatus = true to "Password reset email sent.",
                    onSetLanguage = {},
                    onSigningInChanged = {},
                    onIdTokenReceived = {},
                    onAuthError = {},
                    onEmailSignIn = { email, pass ->
                        signedInEmail = email
                        signedInPassword = pass
                    },
                    onEmailSignUp = { name, email, _, _ ->
                        signedUpName = name
                        signedUpEmail = email
                    },
                    onSendPasswordReset = { email ->
                        resetRequestedEmail = email
                    }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_email_input").performScrollTo().performTextInput("student@studyquest.bd")
        composeRule.onNodeWithTag("auth_password_input").performScrollTo().performTextInput("secret123")
        composeRule.onNodeWithTag("email_auth_submit_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("student@studyquest.bd", signedInEmail)
        assertEquals("secret123", signedInPassword)

        // Switch to Create Account
        composeRule.onNodeWithTag("auth_mode_sign_up").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_name_input").performScrollTo().performTextInput("Amina Rahman")
        composeRule.onNodeWithTag("auth_confirm_password_input").performScrollTo().performTextInput("secret123")
        composeRule.onNodeWithTag("email_auth_submit_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("Amina Rahman", signedUpName)
        assertEquals("student@studyquest.bd", signedUpEmail)

        // Switch back to Sign In and test Forgot Password dialog
        composeRule.onNodeWithTag("auth_mode_sign_in").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_forgot_password_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reset_send_btn").performClick()
        composeRule.waitForIdle()
        assertEquals("student@studyquest.bd", resetRequestedEmail)
        composeRule.onNodeWithTag("password_reset_banner").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun welcomeSignInScreen_topBarBackAndSystemBackReturnToLanguagePicker() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)

        var backCount = 0
        composeRule.setContent {
            StudyQuestTheme(darkTheme = false, isBangla = false) {
                WelcomeAndGoogleSignInScreen(
                    language = AppLanguage.ENGLISH,
                    reducedMotion = true,
                    isSigningIn = false,
                    authError = null,
                    onSetLanguage = {},
                    onSigningInChanged = {},
                    onIdTokenReceived = {},
                    onAuthError = {},
                    onBackToLanguagePicker = { backCount += 1 }
                )
            }
        }

        composeRule.waitForIdle()
        // System back press invokes BackHandler -> returns to Language Picker
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        assertEquals(1, backCount)

        // Top-bar Back IconButton also returns to Language Picker
        composeRule.onNodeWithTag("welcome_back_to_lang_btn").performClick()
        composeRule.waitForIdle()
        assertEquals(2, backCount)
    }

    @Test
    fun preAuthViewModel_validationAndInstantLanguagePickerToggleWork() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)
        val prefs = StudyQuestPreferences(context)
        prefs.setInitialLanguage(AppLanguage.ENGLISH)
        val vm = PreAuthViewModel(prefs)

        vm.selectInitialLanguage(AppLanguage.BANGLA)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertFalse(vm.shouldShowLanguagePicker.value)

        vm.signInWithEmail("invalid-email", "123456", AppLanguage.ENGLISH)
        assertEquals("Please enter a valid email address.", vm.authMessage.value)

        vm.signUpWithEmail("Amina", "amina@example.com", "123456", "different", AppLanguage.ENGLISH)
        assertEquals("Passwords do not match.", vm.authMessage.value)

        vm.sendPasswordReset("bad", AppLanguage.ENGLISH)
        assertEquals(false, vm.passwordResetStatus.value?.first)

        vm.setSigningIn(true)
        assertTrue(vm.isSigningIn.value)

        vm.cancelSignIn()
        assertFalse(vm.isSigningIn.value)
        assertFalse(vm.canCancelSignIn.value)
        assertNotNull(vm.authMessage.value)

        vm.returnToLanguagePicker()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        assertTrue(vm.shouldShowLanguagePicker.value)

        val deadline = System.currentTimeMillis() + 2000L
        var chosen = prefs.preferencesFlow.first().hasChosenInitialLanguage
        while (chosen && System.currentTimeMillis() < deadline) {
            Thread.sleep(25L)
            chosen = prefs.preferencesFlow.first().hasChosenInitialLanguage
        }
        assertFalse(chosen)
    }
}
