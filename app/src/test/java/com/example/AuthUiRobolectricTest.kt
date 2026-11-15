package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthUiRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

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
                    onSetLanguage = {},
                    onSigningInChanged = {},
                    onIdTokenReceived = {},
                    onAuthError = {}
                )
            }
        }

        composeRule.onNodeWithTag("auth_error_banner").assertIsDisplayed()
        composeRule.onNodeWithText("Google Sign-In timed out while contacting Firebase.").assertIsDisplayed()
        composeRule.onNodeWithTag("google_sign_in_button").assertIsNotEnabled()
        composeRule.onNodeWithText("Signing in with Google...").assertIsDisplayed()
    }

    @Test
    fun preAuthViewModel_blankTokenSurfacesErrorWithoutHanging() {
        val context = ApplicationProvider.getApplicationContext<Context>()
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
    fun welcomeSignInScreen_emailGuideTabAndBackButtonWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ensureFirebaseAppInitialized(context)

        var backPressed = false
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
                    onBackToLanguagePicker = { backPressed = true }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("auth_tab_email_guide").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("email_console_guide_panel").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("switch_back_to_google_auth_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("google_sign_in_button").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("welcome_back_to_lang_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(backPressed)
    }

    @Test
    fun preAuthViewModel_cancelSignInAndReturnToLanguagePickerWork() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = StudyQuestPreferences(context)
        prefs.setInitialLanguage(AppLanguage.ENGLISH)
        val vm = PreAuthViewModel(prefs)

        vm.setSigningIn(true)
        assertTrue(vm.isSigningIn.value)

        vm.cancelSignIn()
        assertFalse(vm.isSigningIn.value)
        assertFalse(vm.canCancelSignIn.value)
        assertNotNull(vm.authMessage.value)

        vm.returnToLanguagePicker()
        val deadline = System.currentTimeMillis() + 2000L
        var chosen = prefs.preferencesFlow.first().hasChosenInitialLanguage
        while (chosen && System.currentTimeMillis() < deadline) {
            Thread.sleep(25L)
            chosen = prefs.preferencesFlow.first().hasChosenInitialLanguage
        }
        assertFalse(chosen)
    }
}
