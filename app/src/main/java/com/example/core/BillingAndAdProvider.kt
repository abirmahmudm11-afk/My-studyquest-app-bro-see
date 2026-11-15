package com.example.core

import com.example.data.model.AiCreditLedgerDoc
import com.example.data.model.PurchaseRecordDoc
import com.example.data.model.QuestPlanCatalog
import com.example.data.model.QuestPlanDefinition
import com.example.data.model.SystemSettingsDoc
import com.example.data.model.UserSubscriptionDoc
import com.example.data.model.VideoCreditLedgerDoc
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

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

data class AiEntitlementSummary(
    val planId: String,
    val planTitleEn: String,
    val planTitleBn: String,
    val monthlyLimit: Int,
    val creditsUsed: Int,
    val remainingCredits: Int,
    val adFree: Boolean
)

data class VideoEntitlementSummary(
    val monthlyLimit: Int,
    val creditsUsed: Int,
    val bonusCredits: Int,
    val remainingCredits: Int
)

object StudyActivityMonitor {
    val isFocusActive = MutableStateFlow(false)
    val isExamOrQuizActive = MutableStateFlow(false)
    val isVideoActive = MutableStateFlow(false)
}

class BillingAndAdProvider(private val db: FirebaseFirestore) {

    fun getPlanCatalog(): List<QuestPlanDefinition> = QuestPlanCatalog.getPlanCatalog()

    fun getCanonicalPrice(planId: String): Int = QuestPlanCatalog.getCanonicalPrice(planId)

    fun observeSubscription(userId: String): Flow<UserSubscriptionDoc> =
        db.collection("users").document(userId)
            .collection("subscription").document("status")
            .snapshots()
            .map { snap ->
                val raw = if (snap.exists()) {
                    snap.toObject(
                        UserSubscriptionDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: UserSubscriptionDoc(id = "status", userId = userId)
                } else {
                    UserSubscriptionDoc(id = "status", userId = userId)
                }
                normalizeSubscription(raw)
            }

    fun observeAiCreditLedger(userId: String): Flow<List<AiCreditLedgerDoc>> =
        db.collection("users").document(userId).collection("ai_credit_ledger")
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    AiCreditLedgerDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeVideoCreditLedger(userId: String): Flow<List<VideoCreditLedgerDoc>> =
        db.collection("users").document(userId).collection("video_credit_ledger")
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    VideoCreditLedgerDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observePurchaseHistory(userId: String): Flow<List<PurchaseRecordDoc>> =
        db.collection("users").document(userId).collection("purchase_history")
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    PurchaseRecordDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    suspend fun ensureFreeSubscriptionInitialized(userId: String) {
        if (userId.isBlank() || userId == "guest_local") return
        val ref = db.collection("users").document(userId)
            .collection("subscription").document("status")
        val now = System.currentTimeMillis()
        try {
            val snap = ref.get().await()
            if (!snap.exists()) {
                val freePlan = QuestPlanCatalog.planById("free")
                ref.set(
                    mapOf(
                        "userId" to userId,
                        "tier" to freePlan.id,
                        "activePlanName" to freePlan.titleEn,
                        "billingProvider" to "google_play_unconfigured",
                        "adEnabled" to true,
                        "monthlyAiLimit" to freePlan.monthlyAiCredits.toLong(),
                        "aiCreditsUsed" to 0L,
                        "monthlyVideoLimit" to freePlan.monthlyVideoCredits.toLong(),
                        "videoCreditsUsed" to 0L,
                        "bonusVideoCredits" to 0L,
                        "cycleStartEpochMs" to now,
                        "autoRenewing" to false,
                        "expiresAtEpochMs" to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val doc = snap.toObject(UserSubscriptionDoc::class.java)
                if (doc != null) {
                    // Automatically renew monthly allowance if 30+ days passed on active cycle, or fallback expired paid plan
                    if (doc.tier != "free" && doc.expiresAtEpochMs > 0L && now > doc.expiresAtEpochMs && !doc.autoRenewing) {
                        applyFreePlanFallback(userId)
                    } else if (doc.cycleStartEpochMs > 0L && (now - doc.cycleStartEpochMs) >= CYCLE_DURATION_MS) {
                        renewMonthlyAIAllowance(userId, doc)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Non-fatal
        }
    }

    fun getUserAIEntitlement(subscription: UserSubscriptionDoc): AiEntitlementSummary {
        val norm = normalizeSubscription(subscription)
        val plan = QuestPlanCatalog.planById(norm.tier)
        val limit = norm.monthlyAiLimit.toInt().coerceAtLeast(plan.monthlyAiCredits)
        val used = norm.aiCreditsUsed.toInt().coerceAtLeast(0)
        return AiEntitlementSummary(
            planId = plan.id,
            planTitleEn = plan.titleEn,
            planTitleBn = plan.titleBn,
            monthlyLimit = limit,
            creditsUsed = used,
            remainingCredits = (limit - used).coerceAtLeast(0),
            adFree = plan.adFree || !norm.adEnabled
        )
    }

    fun getRemainingAIAllowance(subscription: UserSubscriptionDoc): Int =
        getUserAIEntitlement(subscription).remainingCredits

    fun getMonthlyAIUsage(subscription: UserSubscriptionDoc): Pair<Int, Int> {
        val ent = getUserAIEntitlement(subscription)
        return ent.creditsUsed to ent.monthlyLimit
    }

    fun estimateAIRequestCredits(
        toolId: String,
        modelId: String,
        promptLength: Int
    ): Int {
        val base = when (modelId) {
            "gpt-6-astra" -> 4
            "gpt-6.1-sol" -> 2
            else -> 1
        }
        val extra = if (promptLength > 2200 || toolId in setOf("DocumentLearningAssistant", "StudyPlanBuilder")) 1 else 0
        return (base + extra).coerceIn(1, 5)
    }

    suspend fun reserveAIRequestCredits(
        userId: String,
        subscription: UserSubscriptionDoc,
        estimatedCredits: Int,
        requestId: String,
        toolId: String,
        modelUsed: String
    ): Result<Int> {
        if (userId.isBlank() || userId == "guest_local") {
            return Result.failure(IllegalStateException("Sign in with Google to use StudyQuest AI credits."))
        }
        val subRef = db.collection("users").document(userId).collection("subscription").document("status")
        val ledgerRef = db.collection("users").document(userId).collection("ai_credit_ledger").document("res_$requestId")
        val needed = estimatedCredits.coerceAtLeast(1).toLong()
        return try {
            db.runTransaction { tx ->
                val snap = tx.get(subRef)
                val current = if (snap.exists()) {
                    normalizeSubscription(snap.toObject(UserSubscriptionDoc::class.java) ?: UserSubscriptionDoc(userId = userId))
                } else {
                    normalizeSubscription(subscription.copy(userId = userId))
                }
                val remaining = (current.monthlyAiLimit - current.aiCreditsUsed).coerceAtLeast(0L)
                if (remaining < needed) {
                    throw IllegalStateException("Insufficient AI credits ($remaining left, $needed needed). Renew or use Puter.js Pay-As-You-Go.")
                }
                val newUsed = current.aiCreditsUsed + needed
                tx.update(
                    subRef,
                    mapOf(
                        "aiCreditsUsed" to newUsed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "userId" to userId,
                        "delta" to -needed,
                        "entryType" to "reserve",
                        "toolId" to toolId.take(64),
                        "modelUsed" to modelUsed.take(64),
                        "requestId" to requestId.take(100),
                        "note" to "Reserved $needed credit(s) for $toolId",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(needed.toInt())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun settleAIRequestCredits(
        userId: String,
        requestId: String,
        reservedCredits: Int,
        actualCredits: Int,
        toolId: String,
        modelUsed: String
    ) {
        if (userId.isBlank() || userId == "guest_local") return
        val refundDiff = (reservedCredits - actualCredits).coerceAtLeast(0).toLong()
        if (refundDiff <= 0L) return
        val subRef = db.collection("users").document(userId).collection("subscription").document("status")
        val ledgerRef = db.collection("users").document(userId).collection("ai_credit_ledger").document("settle_$requestId")
        runCatching {
            db.runTransaction { tx ->
                val snap = tx.get(subRef)
                val used = snap.getLong("aiCreditsUsed") ?: 0L
                tx.update(
                    subRef,
                    mapOf(
                        "aiCreditsUsed" to (used - refundDiff).coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "userId" to userId,
                        "delta" to refundDiff,
                        "entryType" to "settle",
                        "toolId" to toolId.take(64),
                        "modelUsed" to modelUsed.take(64),
                        "requestId" to requestId.take(100),
                        "note" to "Settled unused $refundDiff credit(s)",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
        }
    }

    suspend fun restoreFailedAIRequestCredits(
        userId: String,
        requestId: String,
        reservedCredits: Int,
        toolId: String,
        modelUsed: String,
        reason: String
    ) {
        if (userId.isBlank() || userId == "guest_local" || reservedCredits <= 0) return
        val subRef = db.collection("users").document(userId).collection("subscription").document("status")
        val ledgerRef = db.collection("users").document(userId).collection("ai_credit_ledger").document("refund_$requestId")
        runCatching {
            db.runTransaction { tx ->
                val snap = tx.get(subRef)
                val used = snap.getLong("aiCreditsUsed") ?: 0L
                val restored = (used - reservedCredits.toLong()).coerceAtLeast(0L)
                tx.update(
                    subRef,
                    mapOf(
                        "aiCreditsUsed" to restored,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    ledgerRef,
                    mapOf(
                        "userId" to userId,
                        "delta" to reservedCredits.toLong(),
                        "entryType" to "refund",
                        "toolId" to toolId.take(64),
                        "modelUsed" to modelUsed.take(64),
                        "requestId" to requestId.take(100),
                        "note" to "Restored after failure: ${reason.take(90)}",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
        }
    }

    suspend fun renewMonthlyAIAllowance(userId: String, currentSub: UserSubscriptionDoc) {
        if (userId.isBlank() || userId == "guest_local") return
        val plan = QuestPlanCatalog.planById(currentSub.tier)
        val subRef = db.collection("users").document(userId).collection("subscription").document("status")
        val ledgerRef = db.collection("users").document(userId)
            .collection("ai_credit_ledger").document("renew_${System.currentTimeMillis()}")
        runCatching {
            val now = System.currentTimeMillis()
            val batch = db.batch()
            batch.update(
                subRef,
                mapOf(
                    "monthlyAiLimit" to plan.monthlyAiCredits.toLong(),
                    "aiCreditsUsed" to 0L,
                    "monthlyVideoLimit" to plan.monthlyVideoCredits.toLong(),
                    "videoCreditsUsed" to 0L,
                    "cycleStartEpochMs" to now,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                ledgerRef,
                mapOf(
                    "userId" to userId,
                    "delta" to plan.monthlyAiCredits.toLong(),
                    "entryType" to "renewal",
                    "toolId" to "CycleRenewal",
                    "modelUsed" to "system",
                    "requestId" to "renew_${plan.id}",
                    "note" to "Monthly allowance renewed for ${plan.titleEn}",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
        }
    }

    fun getVideoCredits(subscription: UserSubscriptionDoc): VideoEntitlementSummary {
        val norm = normalizeSubscription(subscription)
        val plan = QuestPlanCatalog.planById(norm.tier)
        val limit = norm.monthlyVideoLimit.toInt().coerceAtLeast(plan.monthlyVideoCredits)
        val used = norm.videoCreditsUsed.toInt().coerceAtLeast(0)
        val bonus = norm.bonusVideoCredits.toInt().coerceAtLeast(0)
        val remaining = ((limit - used).coerceAtLeast(0) + bonus)
        return VideoEntitlementSummary(
            monthlyLimit = limit,
            creditsUsed = used,
            bonusCredits = bonus,
            remainingCredits = remaining
        )
    }

    suspend fun reserveVideoCredit(
        userId: String,
        subscription: UserSubscriptionDoc,
        jobId: String
    ): Result<String> {
        if (userId.isBlank() || userId == "guest_local") {
            return Result.failure(IllegalStateException("Sign in with Google to create animated video lessons."))
        }
        val subRef = db.collection("users").document(userId).collection("subscription").document("status")
        val ledgerRef = db.collection("users").document(userId).collection("video_credit_ledger").document("vres_$jobId")
        return try {
            val sourceUsed = db.runTransaction { tx ->
                val snap = tx.get(subRef)
                val current = if (snap.exists()) {
                    normalizeSubscription(snap.toObject(UserSubscriptionDoc::class.java) ?: UserSubscriptionDoc(userId = userId))
                } else {
                    normalizeSubscription(subscription.copy(userId = userId))
                }
                val monthlyRemaining = (current.monthlyVideoLimit - current.videoCreditsUsed).coerceAtLeast(0L)
                when {
                    monthlyRemaining > 0L -> {
                        tx.update(
                            subRef,
                            mapOf(
                                "videoCreditsUsed" to (current.videoCreditsUsed + 1L),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        tx.set(
                            ledgerRef,
                            mapOf(
                                "userId" to userId,
                                "delta" to -1L,
                                "entryType" to "reserve",
                                "jobId" to jobId,
                                "note" to "Reserved 1 monthly video credit",
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        )
                        "monthly"
                    }
                    current.bonusVideoCredits > 0L -> {
                        tx.update(
                            subRef,
                            mapOf(
                                "bonusVideoCredits" to (current.bonusVideoCredits - 1L),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        tx.set(
                            ledgerRef,
                            mapOf(
                                "userId" to userId,
                                "delta" to -1L,
                                "entryType" to "reserve_boost",
                                "jobId" to jobId,
                                "note" to "Reserved 1 Video Boost credit",
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        )
                        "boost"
                    }
                    else -> throw IllegalStateException("No video creation credits left this month. Upgrade plan or buy Video Boost (৳20).")
                }
            }.await()
            Result.success(sourceUsed)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeVideoCreditUsage(userId: String, jobId: String) {
        if (userId.isBlank() || userId == "guest_local") return
        val ref = db.collection("users").document(userId).collection("video_credit_ledger").document("vcmp_$jobId")
        runCatching {
            ref.set(
                mapOf(
                    "userId" to userId,
                    "delta" to 0L,
                    "entryType" to "complete",
                    "jobId" to jobId,
                    "note" to "Video lesson created successfully",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun restoreFailedVideoCredit(
        userId: String,
        jobId: String,
        creditSource: String,
        reason: String
    ) {
        if (userId.isBlank() || userId == "guest_local") return
        val subRef = db.collection("users").document(userId).collection("subscription").document("status")
        val ledgerRef = db.collection("users").document(userId).collection("video_credit_ledger").document("vref_$jobId")
        runCatching {
            db.runTransaction { tx ->
                val snap = tx.get(subRef)
                if (creditSource == "boost") {
                    val bonus = snap.getLong("bonusVideoCredits") ?: 0L
                    tx.update(
                        subRef,
                        mapOf(
                            "bonusVideoCredits" to (bonus + 1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    val used = snap.getLong("videoCreditsUsed") ?: 0L
                    tx.update(
                        subRef,
                        mapOf(
                            "videoCreditsUsed" to (used - 1L).coerceAtLeast(0L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                tx.set(
                    ledgerRef,
                    mapOf(
                        "userId" to userId,
                        "delta" to 1L,
                        "entryType" to "refund",
                        "jobId" to jobId,
                        "note" to "Video credit restored: ${reason.take(90)}",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
        }
    }

    suspend fun purchaseVideoBoost(
        userId: String,
        settings: SystemSettingsDoc
    ): BillingActionResult {
        if (!settings.billingConfigured) {
            recordUnconfiguredPurchaseAttempt(
                userId = userId,
                planId = QuestPlanCatalog.videoBoost.id,
                productId = QuestPlanCatalog.videoBoost.playProductId,
                title = QuestPlanCatalog.videoBoost.titleEn,
                amountBdt = QuestPlanCatalog.videoBoost.priceBdtOneTime.toLong(),
                purchaseType = "one_time_boost"
            )
            return BillingActionResult.Unconfigured(
                reasonEn = "Play Billing checkout for Video Boost (৳20 one-time for +1 60s short video) is unconfigured in this build. Free library viewing and all academic study tools remain open.",
                reasonBn = "এই বিল্ডে ভিডিও বুস্ট (৳২০ এককালীন = +১টি ৬০ সেকেন্ড শর্ট ভিডিও) পেমেন্ট কনফিগার করা নেই। তবে ভিডিও লাইব্রেরি ও সকল পড়াশোনা সম্পূর্ণ উন্মুক্ত।"
            )
        }
        return BillingActionResult.ConfiguredReady(
            messageEn = "Google Play Billing ready for Video Boost (৳20 one-time).",
            messageBn = "ভিডিও বুস্ট (৳২০ এককালীন) কেনার জন্য Google Play Billing প্রস্তুত।"
        )
    }

    suspend fun applyFreePlanFallback(userId: String) {
        if (userId.isBlank() || userId == "guest_local") return
        val freePlan = QuestPlanCatalog.planById("free")
        val ref = db.collection("users").document(userId).collection("subscription").document("status")
        runCatching {
            ref.set(
                mapOf(
                    "userId" to userId,
                    "tier" to freePlan.id,
                    "activePlanName" to freePlan.titleEn,
                    "billingProvider" to "google_play_unconfigured",
                    "adEnabled" to true,
                    "monthlyAiLimit" to freePlan.monthlyAiCredits.toLong(),
                    "aiCreditsUsed" to 0L,
                    "monthlyVideoLimit" to freePlan.monthlyVideoCredits.toLong(),
                    "videoCreditsUsed" to 0L,
                    "autoRenewing" to false,
                    "expiresAtEpochMs" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun requestPlanUpgrade(
        userId: String,
        planId: String,
        settings: SystemSettingsDoc
    ): BillingActionResult {
        val plan = QuestPlanCatalog.planById(planId)
        if (plan.id == "free") {
            applyFreePlanFallback(userId)
            return BillingActionResult.ConfiguredReady(
                messageEn = "Free StudyQuest (৳0/mo) active — 20 AI credits & 1 animated video credit every month.",
                messageBn = "ফ্রি StudyQuest (৳০) সক্রিয় — প্রতি মাসে ২০টি AI ক্রেডিট ও ১টি ভিডিও ক্রেডিট।"
            )
        }
        if (!settings.billingConfigured) {
            recordUnconfiguredPurchaseAttempt(
                userId = userId,
                planId = plan.id,
                productId = plan.playProductId ?: "sq_plan_${plan.id}",
                title = plan.titleEn,
                amountBdt = plan.priceBdtMonthly.toLong(),
                purchaseType = "subscription"
            )
            return BillingActionResult.Unconfigured(
                reasonEn = "Google Play Billing is not linked to Play Console yet for ${plan.titleEn} (৳${plan.priceBdtMonthly}/mo). Your Free StudyQuest plan (20 AI credits/mo + 1 video/mo) & Puter.js Pay-As-You-Go remain fully active.",
                reasonBn = "${plan.titleBn} (৳${StudyQuestStrings.formatDigits(plan.priceBdtMonthly, AppLanguage.BANGLA)}/মাস) এর জন্য Play Billing এখনো লিংক করা হয়নি। ফ্রি StudyQuest এবং Puter.js সম্পূর্ণ চালু রয়েছে।"
            )
        }
        return BillingActionResult.ConfiguredReady(
            messageEn = "Google Play Billing ready for ${plan.titleEn} (৳${plan.priceBdtMonthly}/mo). Play verification required on release build.",
            messageBn = "${plan.titleBn} (৳${StudyQuestStrings.formatDigits(plan.priceBdtMonthly, AppLanguage.BANGLA)}/মাস) এর জন্য Play Billing প্রস্তুত।"
        )
    }

    private suspend fun recordUnconfiguredPurchaseAttempt(
        userId: String,
        planId: String,
        productId: String,
        title: String,
        amountBdt: Long,
        purchaseType: String
    ) {
        if (userId.isBlank() || userId == "guest_local") return
        val id = "pur_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}"
        runCatching {
            db.collection("users").document(userId).collection("purchase_history").document(id).set(
                mapOf(
                    "userId" to userId,
                    "productId" to productId,
                    "planId" to planId,
                    "title" to title,
                    "amountBdt" to amountBdt,
                    "purchaseType" to purchaseType,
                    "status" to "unconfigured",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    fun requestUpgradeToPremium(settings: SystemSettingsDoc): BillingActionResult {
        if (!settings.billingConfigured) {
            return BillingActionResult.Unconfigured(
                reasonEn = "Google Play Billing product IDs & Play Console verification are not configured in this build. Free study, focus, quizzes, and Puter.js AI remain 100% unlocked.",
                reasonBn = "এই বিল্ডে Google Play Billing প্রোডাক্ট আইডি কনফিগার করা নেই। তবে সব পড়া, ফোকাস টাইমার, কুইজ ও Puter.js AI বিনামূল্যে চালু রয়েছে।"
            )
        }
        return BillingActionResult.ConfiguredReady(
            messageEn = "Google Play Billing is enabled. Launching Play Store sheet...",
            messageBn = "Google Play Billing সক্রিয় রয়েছে..."
        )
    }

    fun evaluateAdPlacement(
        subscription: UserSubscriptionDoc,
        settings: SystemSettingsDoc,
        isActiveFocusSession: Boolean,
        isActiveExamOrQuiz: Boolean,
        isActiveVideoSession: Boolean = StudyActivityMonitor.isVideoActive.value
    ): AdBannerPlacementState = Companion.evaluateAdPlacement(
        subscription = subscription,
        settings = settings,
        isActiveFocusSession = isActiveFocusSession,
        isActiveExamOrQuiz = isActiveExamOrQuiz,
        isActiveVideoSession = isActiveVideoSession
    )

    companion object {
        private const val CYCLE_DURATION_MS = 30L * 24L * 60L * 60L * 1000L

        fun normalizeSubscription(sub: UserSubscriptionDoc): UserSubscriptionDoc {
            val plan = QuestPlanCatalog.planById(sub.tier)
            return sub.copy(
                tier = plan.id,
                activePlanName = sub.activePlanName.ifBlank { plan.titleEn },
                monthlyAiLimit = sub.monthlyAiLimit.coerceAtLeast(plan.monthlyAiCredits.toLong()),
                monthlyVideoLimit = sub.monthlyVideoLimit.coerceAtLeast(plan.monthlyVideoCredits.toLong()),
                adEnabled = !plan.adFree && sub.adEnabled
            )
        }

        fun disableAdsForPaidEntitlement(subscription: UserSubscriptionDoc): Boolean =
            QuestPlanCatalog.planById(subscription.tier).adFree || !subscription.adEnabled

        fun disableAdsDuringFocus(isActiveFocusSession: Boolean): Boolean = isActiveFocusSession

        fun disableAdsDuringExam(isActiveExamOrQuiz: Boolean): Boolean = isActiveExamOrQuiz

        fun disableAdsDuringVideo(isActiveVideoSession: Boolean): Boolean = isActiveVideoSession

        fun evaluateAdPlacement(
            subscription: UserSubscriptionDoc,
            settings: SystemSettingsDoc,
            isActiveFocusSession: Boolean,
            isActiveExamOrQuiz: Boolean,
            isActiveVideoSession: Boolean = false
        ): AdBannerPlacementState {
            val suppressed = disableAdsDuringFocus(isActiveFocusSession) ||
                disableAdsDuringExam(isActiveExamOrQuiz) ||
                disableAdsDuringVideo(isActiveVideoSession)
            val paidAdFree = disableAdsForPaidEntitlement(subscription)
            val showSlot = !suppressed && !paidAdFree && settings.adMobConfigured
            return AdBannerPlacementState(
                shouldDisplayAdSlot = showSlot,
                isSuppressedForStudyOrExam = suppressed,
                adMobConfigured = settings.adMobConfigured,
                tier = QuestPlanCatalog.planById(subscription.tier).id
            )
        }
    }
}
