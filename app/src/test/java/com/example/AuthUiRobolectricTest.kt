package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.core.AppLanguage
import com.example.core.StudyQuestPreferences
import com.example.ui.screens.WelcomeAndGoogleSignInScreen
import com.example.ui.theme.StudyQuestTheme
import com.example.ui.viewmodel.PreAuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
}
