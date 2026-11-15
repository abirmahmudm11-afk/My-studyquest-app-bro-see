package com.example.core

import com.example.data.model.SystemSettingsDoc
import com.example.data.model.UserSubscriptionDoc
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

sealed interface BillingActionResult {
    data class Unconfigured(val reasonEn: String, val reasonBn: String) : BillingActionResult
    data class ConfiguredReady(val messageEn: String, val messageBn: String) : BillingActionResult
}

data class AdBannerPlacementState(
    val shouldDisplayAdSlot: Boolean,
    val isSuppressedForStudyOrExam: Boolean,
    val adMobConfigured: Boolean,
    val tier: String
)

object StudyActivityMonitor {
    val isFocusActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isExamOrQuizActive = kotlinx.coroutines.flow.MutableStateFlow(false)
}

class BillingAndAdProvider(private val db: FirebaseFirestore) {

    fun observeSubscription(userId: String): Flow<UserSubscriptionDoc> =
        db.collection("users").document(userId)
            .collection("subscription").document("status")
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(
                        UserSubscriptionDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: UserSubscriptionDoc(id = "status", userId = userId)
                } else {
                    UserSubscriptionDoc(id = "status", userId = userId)
                }
            }

    suspend fun ensureFreeSubscriptionInitialized(userId: String) {
        if (userId.isBlank() || userId == "guest_local") return
        val ref = db.collection("users").document(userId)
            .collection("subscription").document("status")
        try {
            if (!ref.get().await().exists()) {
                ref.set(
                    mapOf(
                        "userId" to userId,
                        "tier" to "free",
                        "billingProvider" to "google_play_unconfigured",
                        "adEnabled" to true,
                        "expiresAtEpochMs" to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Non-fatal
        }
    }

    fun evaluateAdPlacement(
        subscription: UserSubscriptionDoc,
        settings: SystemSettingsDoc,
        isActiveFocusSession: Boolean,
        isActiveExamOrQuiz: Boolean
    ): AdBannerPlacementState = Companion.evaluateAdPlacement(
        subscription = subscription,
        settings = settings,
        isActiveFocusSession = isActiveFocusSession,
        isActiveExamOrQuiz = isActiveExamOrQuiz
    )

    fun requestUpgradeToPremium(settings: SystemSettingsDoc): BillingActionResult {
        if (!settings.billingConfigured) {
            return BillingActionResult.Unconfigured(
                reasonEn = "Google Play Billing product IDs & Play Console verification are not configured in this build. Free study, focus, quizzes, and community remain 100% unlocked.",
                reasonBn = "এই বিল্ডে Google Play Billing প্রোডাক্ট আইডি কনফিগার করা নেই। তবে সব পড়া, ফোকাস টাইমার, কুইজ ও কমিউনিটি ফিচার বিনামূল্যে সম্পূর্ণ চালু রয়েছে।"
            )
        }
        return BillingActionResult.ConfiguredReady(
            messageEn = "Google Play Billing is enabled. Launching Play Store sheet...",
            messageBn = "Google Play Billing সক্রিয় রয়েছে..."
        )
    }

    companion object {
        fun evaluateAdPlacement(
            subscription: UserSubscriptionDoc,
            settings: SystemSettingsDoc,
            isActiveFocusSession: Boolean,
            isActiveExamOrQuiz: Boolean
        ): AdBannerPlacementState {
            val suppressed = isActiveFocusSession || isActiveExamOrQuiz
            val isFree = subscription.tier != "premium" && subscription.adEnabled
            val showSlot = !suppressed && isFree && settings.adMobConfigured
            return AdBannerPlacementState(
                shouldDisplayAdSlot = showSlot,
                isSuppressedForStudyOrExam = suppressed,
                adMobConfigured = settings.adMobConfigured,
                tier = subscription.tier
            )
        }
    }
}
