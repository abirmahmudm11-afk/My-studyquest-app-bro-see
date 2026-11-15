package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.curriculum.GamificationCatalog
import com.example.data.model.AiProviderSettingsDoc
import com.example.data.model.AiUsageLogDoc
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import java.util.concurrent.TimeUnit

data class StudyQuestAiToolSpec(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val promptHintEn: String,
    val promptHintBn: String,
    val prefersReasoningModel: Boolean = false
)

object StudyQuestAiToolCatalog {
    val tools: List<StudyQuestAiToolSpec> = listOf(
        StudyQuestAiToolSpec(
            id = "QuestTutor",
            titleEn = "QuestTutor",
            titleBn = "কোয়েস্ট টিউটর",
            subtitleEn = "Everyday Socratic NCTB tutor",
            subtitleBn = "ধাপে ধাপে দৈনন্দিন পাঠ সহায়ক",
            promptHintEn = "Explain Ohm's Law with an everyday example",
            promptHintBn = "ওহমের সূত্র উদাহরণসহ সহজভাবে বুঝিয়ে দাও"
        ),
        StudyQuestAiToolSpec(
            id = "QuickExplain",
            titleEn = "QuickExplain",
            titleBn = "দ্রুত ব্যাখ্যা",
            subtitleEn = "Fast 60-second concept clarity",
            subtitleBn = "দ্রুত মূল ধারণা পরিষ্কার করা",
            promptHintEn = "Summarize mitosis vs meiosis in 4 bullets",
            promptHintBn = "মাইটোসিস ও মিয়োসিসের পার্থক্য ৪ পয়েন্টে বলো"
        ),
        StudyQuestAiToolSpec(
            id = "ChapterSummarizer",
            titleEn = "ChapterSummarizer",
            titleBn = "অধ্যায় সারসংক্ষেপ",
            subtitleEn = "Structured board-ready notes",
            subtitleBn = "বোর্ড পরীক্ষা উপযোগী অধ্যায় নোট",
            promptHintEn = "Summarize Class 10 Trigonometry identities",
            promptHintBn = "দশম শ্রেণির ত্রিকোণমিতিক সূত্রসমূহের সারসংক্ষেপ দাও"
        ),
        StudyQuestAiToolSpec(
            id = "NotesToFlashcards",
            titleEn = "NotesToFlashcards",
            titleBn = "ফ্ল্যাশকার্ড মেকার",
            subtitleEn = "Turn notes into active recall cards",
            subtitleBn = "নোট থেকে রিভিশন ফ্ল্যাশকার্ড তৈরি",
            promptHintEn = "Turn quadratic formula rules into 5 Q&A flashcards",
            promptHintBn = "দ্বিঘাত সমীকরণের নিয়মগুলো থেকে ৫টি প্রশ্নোত্তর কার্ড বানাও"
        ),
        StudyQuestAiToolSpec(
            id = "SmartQuizMaker",
            titleEn = "SmartQuizMaker",
            titleBn = "স্মার্ট কুইজ মেকার",
            subtitleEn = "Custom NCTB practice MCQs",
            subtitleBn = "অধ্যায়ভিত্তিক অনুশীলন MCQ তৈরি",
            promptHintEn = "Create 5 SSC Biology MCQs on Photosynthesis",
            promptHintBn = "সালোকসংশ্লেষণ বিষয়ে ৫টি SSC MCQ তৈরি করো"
        ),
        StudyQuestAiToolSpec(
            id = "QuestionExplainer",
            titleEn = "QuestionExplainer",
            titleBn = "প্রশ্ন বিশ্লেষক",
            subtitleEn = "Deep step-by-step math & CQ walkthrough",
            subtitleBn = "জটিল অঙ্ক ও সৃজনশীল প্রশ্নের সমাধান",
            promptHintEn = "Solve x^2 - 5x + 6 = 0 and check discriminant",
            promptHintBn = "x^2 - 5x + 6 = 0 সমাধান করে নিশ্চায়ক দেখাও",
            prefersReasoningModel = true
        ),
        StudyQuestAiToolSpec(
            id = "StudyPlanBuilder",
            titleEn = "StudyPlanBuilder",
            titleBn = "স্টাডি প্ল্যান",
            subtitleEn = "7-day or exam sprint schedule",
            subtitleBn = "সাপ্তাহিক বা পরীক্ষার রুটিন তৈরি",
            promptHintEn = "Build a 7-day SSC Math & Physics revision timetable",
            promptHintBn = "গণিত ও পদার্থবিজ্ঞানের ৭ দিনের রিভিশন রুটিন দাও",
            prefersReasoningModel = true
        ),
        StudyQuestAiToolSpec(
            id = "EnglishCoach",
            titleEn = "EnglishCoach",
            titleBn = "ইংরেজি কোচ",
            subtitleEn = "Grammar, paragraph & writing feedback",
            subtitleBn = "গ্রামার, প্যারাগ্রাফ ও রাইটিং অনুশীলন",
            promptHintEn = "Fix grammar and upgrade my paragraph clarity",
            promptHintBn = "আমার ইংরেজি প্যারাগ্রাফের গ্রামার ঠিক করে দাও"
        ),
        StudyQuestAiToolSpec(
            id = "ConceptExplorer",
            titleEn = "ConceptExplorer",
            titleBn = "কনসেপ্ট এক্সপ্লোরার",
            subtitleEn = "Why it works & real-life intuition",
            subtitleBn = "কেন এমন ঘটে তা বাস্তব উদাহরণে বোঝো",
            promptHintEn = "Why does refraction bend light toward the normal?",
            promptHintBn = "প্রতিসরণের সময় আলো অভিলম্বের দিকে বেঁকে যায় কেন?"
        ),
        StudyQuestAiToolSpec(
            id = "DocumentLearningAssistant",
            titleEn = "DocumentLearningAssistant",
            titleBn = "ডকুমেন্ট বিশ্লেষণ",
            subtitleEn = "Analyze pasted notes or PDF text",
            subtitleBn = "নোট বা ডকুমেন্ট থেকে জটিল বিশ্লেষণ",
            promptHintEn = "Extract key definitions and board questions from these notes",
            promptHintBn = "এই নোট থেকে মূল সংজ্ঞা ও সম্ভাব্য প্রশ্ন বের করো",
            prefersReasoningModel = true
        ),
        StudyQuestAiToolSpec(
            id = "RevisionAssistant",
            titleEn = "RevisionAssistant",
            titleBn = "রিভিশন সহায়ক",
            subtitleEn = "Targeted spaced-repetition drills",
            subtitleBn = "ভুলে যাওয়া টপিক দ্রুত ঝালাই",
            promptHintEn = "Give me rapid recall prompts for Chemistry Periodic Table",
            promptHintBn = "পর্যায় সারণীর দ্রুত রিভিশনের জন্য প্রশ্ন করো"
        ),
        StudyQuestAiToolSpec(
            id = "MistakeAnalyzer",
            titleEn = "MistakeAnalyzer",
            titleBn = "ভুল বিশ্লেষক",
            subtitleEn = "Diagnose wrong answers & fix gaps",
            subtitleBn = "ভুল উত্তরের কারণ খুঁজে সমাধান",
            promptHintEn = "Why did I confuse velocity and acceleration here?",
            promptHintBn = "বেগ ও ত্বরণের হিসাব এখানে কেন ভুল হলো বুঝিয়ে দাও",
            prefersReasoningModel = true
        )
    )

    fun byId(id: String): StudyQuestAiToolSpec =
        tools.find { it.id == id } ?: tools.first()
}

enum class EducationalRequestCategory {
    EVERYDAY_TUTORING,
    COMPLEX_REASONING,
    ADVANCED_ADMIN_ONLY
}

data class ModelRoutingDecision(
    val selectedModelId: String,
    val category: EducationalRequestCategory,
    val complexityScore: Int,
    val reason: String
)

data class OpenRouterModelCheckResult(
    val keyConfigured: Boolean,
    val reachable: Boolean,
    val totalModelsReturned: Int,
    val defaultModelAvailable: Boolean,
    val reasoningModelAvailable: Boolean,
    val advancedModelAvailable: Boolean,
    val verifiedModelIds: Set<String>,
    val message: String
)

data class OpenRouterCompletionResult(
    val text: String,
    val modelUsed: String,
    val inputTokens: Long,
    val outputTokens: Long
)

class StudyQuestModelRouter {

    fun classifyEducationalRequest(toolId: String, prompt: String): EducationalRequestCategory {
        val lower = prompt.lowercase()
        val toolSpec = StudyQuestAiToolCatalog.byId(toolId)
        val hasHardMathMarkers = listOf(
            "prove", "derivation", "derive", "integral", "calculus", "proof",
            "multi-step", "step by step proof", "trigonometric identity", "quadratic",
            "chemical equation", "stoichiometry", "mechanics", "kinematics",
            "প্রমাণ কর", "উপপাদ্য", "জটিল", "সমীকরণ সমাধান", "বিশ্লেষণ কর"
        ).any { lower.contains(it) }

        val hasAdvancedMarkers = listOf(
            "olympiad", "research-grade", "deep synthesis", "astra"
        ).any { lower.contains(it) }

        return when {
            hasAdvancedMarkers -> EducationalRequestCategory.ADVANCED_ADMIN_ONLY
            toolSpec.prefersReasoningModel || hasHardMathMarkers || prompt.length > 900 ->
                EducationalRequestCategory.COMPLEX_REASONING
            else -> EducationalRequestCategory.EVERYDAY_TUTORING
        }
    }

    fun estimateRequestComplexity(toolId: String, prompt: String): Int {
        var score = 1
        if (prompt.length > 400) score += 2
        if (prompt.length > 1000) score += 2
        if (StudyQuestAiToolCatalog.byId(toolId).prefersReasoningModel) score += 3
        if (prompt.contains("?") && prompt.count { it == '?' } > 2) score += 1
        if (Regex("[=^√∫∑]").containsMatchIn(prompt)) score += 2
        return score.coerceIn(1, 10)
    }

    fun checkAdvancedModelEligibility(
        settings: AiProviderSettingsDoc,
        explicitAdminRequest: Boolean
    ): Boolean = settings.advancedModelEnabled && explicitAdminRequest

    fun routeToDefaultModel(settings: AiProviderSettingsDoc): String =
        settings.defaultModelId.ifBlank { DEFAULT_MODEL }

    fun routeToReasoningModel(settings: AiProviderSettingsDoc): String =
        settings.reasoningModelId.ifBlank { REASONING_MODEL }

    fun routeToAdvancedModel(settings: AiProviderSettingsDoc): String =
        settings.advancedModelId.ifBlank { ADVANCED_MODEL }

    fun selectAIModel(
        toolId: String,
        prompt: String,
        settings: AiProviderSettingsDoc,
        explicitAdvancedRequest: Boolean = false
    ): ModelRoutingDecision {
        val category = classifyEducationalRequest(toolId, prompt)
        val complexity = estimateRequestComplexity(toolId, prompt)

        if (category == EducationalRequestCategory.ADVANCED_ADMIN_ONLY &&
            checkAdvancedModelEligibility(settings, explicitAdvancedRequest)
        ) {
            val astra = routeToAdvancedModel(settings)
            return ModelRoutingDecision(
                selectedModelId = astra,
                category = category,
                complexityScore = complexity,
                reason = "Admin-enabled advanced reasoning ($astra)"
            )
        }

        if (category == EducationalRequestCategory.COMPLEX_REASONING || complexity >= 6) {
            val sol = routeToReasoningModel(settings)
            return ModelRoutingDecision(
                selectedModelId = sol,
                category = EducationalRequestCategory.COMPLEX_REASONING,
                complexityScore = complexity,
                reason = "Multi-step educational reasoning ($sol)"
            )
        }

        val luna = routeToDefaultModel(settings)
        return ModelRoutingDecision(
            selectedModelId = luna,
            category = EducationalRequestCategory.EVERYDAY_TUTORING,
            complexityScore = complexity,
            reason = "Default cost-controlled StudyQuest Helper ($luna)"
        )
    }

    fun handleModelFallback(
        requestedModelId: String,
        availableModels: Set<String>,
        settings: AiProviderSettingsDoc
    ): String {
        if (availableModels.isEmpty() || requestedModelId in availableModels) {
            return requestedModelId
        }
        val defaultCandidate = routeToDefaultModel(settings)
        if (defaultCandidate in availableModels) return defaultCandidate
        val reasoningCandidate = routeToReasoningModel(settings)
        if (reasoningCandidate in availableModels) return reasoningCandidate
        return availableModels.first()
    }

    companion object {
        const val DEFAULT_MODEL = "gpt-6-luna"
        const val REASONING_MODEL = "gpt-6.1-sol"
        const val ADVANCED_MODEL = "gpt-6-astra"
    }
}

class OpenRouterClient(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()
) {
    fun configuredApiKey(): String = runCatching { BuildConfig.OPENROUTER_API_KEY }.getOrDefault("")

    fun isApiKeyConfigured(): Boolean {
        val key = configuredApiKey().trim()
        return key.isNotBlank() &&
            key != "OPENROUTER_API_KEY_DEFAULT_VALUE" &&
            !key.startsWith("PLACEHOLDER", ignoreCase = true)
    }

    suspend fun verifyModelAvailability(
        defaultModelId: String,
        reasoningModelId: String,
        advancedModelId: String
    ): OpenRouterModelCheckResult = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext OpenRouterModelCheckResult(
                keyConfigured = false,
                reachable = false,
                totalModelsReturned = 0,
                defaultModelAvailable = false,
                reasoningModelAvailable = false,
                advancedModelAvailable = false,
                verifiedModelIds = emptySet(),
                message = "OPENROUTER_API_KEY is not set in AI Studio Secrets. Live OpenRouter requests are waiting for your secret key."
            )
        }
        try {
            val req = Request.Builder()
                .url("https://openrouter.ai/api/v1/models")
                .addHeader("Authorization", "Bearer ${configuredApiKey()}")
                .addHeader("HTTP-Referer", "https://studyquest.app")
                .addHeader("X-Title", "StudyQuest Helper")
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    return@withContext OpenRouterModelCheckResult(
                        keyConfigured = true,
                        reachable = false,
                        totalModelsReturned = 0,
                        defaultModelAvailable = false,
                        reasoningModelAvailable = false,
                        advancedModelAvailable = false,
                        verifiedModelIds = emptySet(),
                        message = "OpenRouter /models returned HTTP ${resp.code}: ${bodyStr.take(180)}"
                    )
                }
                val root = JSONObject(bodyStr)
                val dataArr = root.optJSONArray("data") ?: JSONArray()
                val ids = HashSet<String>(dataArr.length())
                for (i in 0 until dataArr.length()) {
                    val item = dataArr.optJSONObject(i) ?: continue
                    val id = item.optString("id", "")
                    if (id.isNotBlank()) ids.add(id)
                }
                fun matches(target: String): Boolean =
                    ids.any { it == target || it.endsWith("/$target") }
                val defOk = matches(defaultModelId)
                val solOk = matches(reasoningModelId)
                val astOk = matches(advancedModelId)
                OpenRouterModelCheckResult(
                    keyConfigured = true,
                    reachable = true,
                    totalModelsReturned = ids.size,
                    defaultModelAvailable = defOk,
                    reasoningModelAvailable = solOk,
                    advancedModelAvailable = astOk,
                    verifiedModelIds = ids,
                    message = "Verified ${ids.size} OpenRouter models. $defaultModelId=$defOk, $reasoningModelId=$solOk, $advancedModelId=$astOk"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "OpenRouter model verification failed", e)
            OpenRouterModelCheckResult(
                keyConfigured = true,
                reachable = false,
                totalModelsReturned = 0,
                defaultModelAvailable = false,
                reasoningModelAvailable = false,
                advancedModelAvailable = false,
                verifiedModelIds = emptySet(),
                message = "Verification error: ${e.localizedMessage ?: "Network failure"}"
            )
        }
    }

    suspend fun streamChatCompletion(
        modelId: String,
        systemPrompt: String,
        messages: List<Pair<String, String>>,
        onChunk: suspend (String) -> Unit
    ): OpenRouterCompletionResult = withContext(Dispatchers.IO) {
        val key = configuredApiKey().trim()
        if (!isApiKeyConfigured()) {
            throw IllegalStateException("OPENROUTER_API_KEY missing in Secrets panel.")
        }

        val jsonMsgs = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", systemPrompt))
            messages.forEach { (role, text) ->
                put(JSONObject().put("role", role).put("content", text))
            }
        }
        val payload = JSONObject().apply {
            put("model", modelId)
            put("messages", jsonMsgs)
            put("stream", true)
            put("max_tokens", 1200)
        }
        val req = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://studyquest.app")
            .addHeader("X-Title", "StudyQuest Helper")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val accumulated = StringBuilder()
        var resolvedModel = modelId
        var inTok = (messages.sumOf { it.second.length } / 4L).coerceAtLeast(10L)
        var outTok = 0L

        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val errBody = resp.body?.string().orEmpty()
                throw IllegalStateException("OpenRouter HTTP ${resp.code}: ${errBody.take(220)}")
            }
            val stream = resp.body?.byteStream() ?: throw IllegalStateException("Empty OpenRouter stream")
            BufferedReader(InputStreamReader(stream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val raw = line?.trim().orEmpty()
                    if (raw.isEmpty() || !raw.startsWith("data:")) continue
                    val data = raw.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    runCatching {
                        val obj = JSONObject(data)
                        val m = obj.optString("model", "")
                        if (m.isNotBlank()) resolvedModel = m
                        val usage = obj.optJSONObject("usage")
                        if (usage != null) {
                            inTok = usage.optLong("prompt_tokens", inTok)
                            outTok = usage.optLong("completion_tokens", outTok)
                        }
                        val choices = obj.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val delta = choices.getJSONObject(0).optJSONObject("delta")
                            val piece = delta?.optString("content", "").orEmpty()
                            if (piece.isNotEmpty()) {
                                accumulated.append(piece)
                                onChunk(accumulated.toString())
                            }
                        }
                    }
                }
            }
        }
        if (outTok <= 0L) {
            outTok = (accumulated.length / 4L).coerceAtLeast(1L)
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("OpenRouter returned empty completion.")
        }
        OpenRouterCompletionResult(
            text = accumulated.toString(),
            modelUsed = resolvedModel,
            inputTokens = inTok,
            outputTokens = outTok
        )
    }

    companion object {
        private const val TAG = "OpenRouterClient"
    }
}

data class BudgetCheckResult(
    val allowed: Boolean,
    val reason: String? = null
)

class StudyQuestAiCostGuard(private val db: FirebaseFirestore) {

    fun observeProviderSettings(): Flow<AiProviderSettingsDoc> =
        db.collection("system_settings").document("ai_provider_config")
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(
                        AiProviderSettingsDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: AiProviderSettingsDoc()
                } else {
                    AiProviderSettingsDoc()
                }
            }

    suspend fun getProviderSettings(): AiProviderSettingsDoc =
        runCatching {
            val snap = db.collection("system_settings").document("ai_provider_config").get().await()
            if (snap.exists()) snap.toObject(AiProviderSettingsDoc::class.java) ?: AiProviderSettingsDoc()
            else AiProviderSettingsDoc()
        }.getOrDefault(AiProviderSettingsDoc())

    fun observeUsageLogs(): Flow<List<AiUsageLogDoc>> =
        db.collection("ai_usage_logs")
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    AiUsageLogDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun calculateProviderCost(modelId: String, inputTokens: Long, outputTokens: Long): Double {
        val (inPer1k, outPer1k) = when {
            modelId.contains("astra", ignoreCase = true) -> 0.015 to 0.060
            modelId.contains("sol", ignoreCase = true) -> 0.003 to 0.012
            modelId.contains("gemini", ignoreCase = true) -> 0.0002 to 0.0006
            else -> 0.0005 to 0.0015 // gpt-6-luna low-cost default
        }
        return ((inputTokens / 1000.0) * inPer1k) + ((outputTokens / 1000.0) * outPer1k)
    }

    suspend fun recordAIUsage(
        userId: String,
        conversationId: String,
        toolId: String,
        modelId: String,
        provider: String,
        inputTokens: Long,
        outputTokens: Long,
        creditsDeducted: Long,
        status: String
    ) {
        val logId = "aiu_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val estCost = calculateProviderCost(modelId, inputTokens, outputTokens)
        val dayKey = GamificationCatalog.currentDayKey()
        val monthKey = GamificationCatalog.currentMonthKey()
        runCatching {
            db.collection("ai_usage_logs").document(logId).set(
                mapOf(
                    "userId" to userId,
                    "conversationId" to conversationId,
                    "toolId" to toolId,
                    "modelId" to modelId,
                    "provider" to provider,
                    "inputTokens" to inputTokens,
                    "outputTokens" to outputTokens,
                    "estimatedCostUsd" to estCost,
                    "creditsDeducted" to creditsDeducted,
                    "status" to status,
                    "dayKey" to dayKey,
                    "monthKey" to monthKey,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun getDailyAICost(dayKey: String = GamificationCatalog.currentDayKey()): Double =
        runCatching {
            db.collection("ai_usage_logs")
                .whereEqualTo("dayKey", dayKey)
                .get().await()
                .documents.sumOf { it.getDouble("estimatedCostUsd") ?: 0.0 }
        }.getOrDefault(0.0)

    suspend fun getMonthlyAICost(monthKey: String = GamificationCatalog.currentMonthKey()): Double =
        runCatching {
            db.collection("ai_usage_logs")
                .whereEqualTo("monthKey", monthKey)
                .get().await()
                .documents.sumOf { it.getDouble("estimatedCostUsd") ?: 0.0 }
        }.getOrDefault(0.0)

    suspend fun getModelUsage(modelId: String, monthKey: String = GamificationCatalog.currentMonthKey()): Double =
        runCatching {
            db.collection("ai_usage_logs")
                .whereEqualTo("monthKey", monthKey)
                .whereEqualTo("modelId", modelId)
                .get().await()
                .documents.sumOf { it.getDouble("estimatedCostUsd") ?: 0.0 }
        }.getOrDefault(0.0)

    suspend fun getUserAIUsage(userId: String, monthKey: String = GamificationCatalog.currentMonthKey()): Double =
        runCatching {
            db.collection("ai_usage_logs")
                .whereEqualTo("monthKey", monthKey)
                .whereEqualTo("userId", userId)
                .get().await()
                .documents.sumOf { it.getDouble("estimatedCostUsd") ?: 0.0 }
        }.getOrDefault(0.0)

    suspend fun setGlobalAIBudget(actorUid: String, budgetUsd: Double): Result<Unit> =
        updateBudgetField(actorUid, "globalMonthlyBudgetUsd", budgetUsd.coerceIn(1.0, 50000.0))

    suspend fun setPerUserAIBudget(actorUid: String, budgetUsd: Double): Result<Unit> =
        updateBudgetField(actorUid, "perUserMonthlyBudgetUsd", budgetUsd.coerceIn(0.1, 500.0))

    suspend fun setModelBudget(actorUid: String, astraBudgetUsd: Double): Result<Unit> =
        updateBudgetField(actorUid, "astraMonthlyBudgetUsd", astraBudgetUsd.coerceIn(0.5, 10000.0))

    private suspend fun updateBudgetField(actorUid: String, field: String, value: Double): Result<Unit> =
        try {
            db.collection("system_settings").document("ai_provider_config")
                .set(
                    mapOf(
                        field to value,
                        "updatedByUid" to actorUid,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun checkAIBudgetBeforeRequest(
        userId: String,
        targetModelId: String,
        settings: AiProviderSettingsDoc
    ): BudgetCheckResult {
        val monthKey = GamificationCatalog.currentMonthKey()
        val monthlyGlobal = getMonthlyAICost(monthKey)
        if (monthlyGlobal >= settings.globalMonthlyBudgetUsd) {
            pauseRequestsAtBudgetLimit("Global monthly AI budget reached ($${"%.2f".format(monthlyGlobal)})")
            sendAdminBudgetAlert(userId, "Global AI budget cap reached")
            return BudgetCheckResult(
                allowed = false,
                reason = "StudyQuest AI global budget limit reached for this month. Use Puter.js Pay-As-You-Go in the meantime."
            )
        }
        val userCost = getUserAIUsage(userId, monthKey)
        if (userCost >= settings.perUserMonthlyBudgetUsd) {
            return BudgetCheckResult(
                allowed = false,
                reason = "Your monthly StudyQuest AI cost cap has been reached. Switch to Puter.js Pay-As-You-Go or renew next cycle."
            )
        }
        if (targetModelId.contains("astra", ignoreCase = true)) {
            val astraCost = getModelUsage(targetModelId, monthKey)
            if (astraCost >= settings.astraMonthlyBudgetUsd) {
                return BudgetCheckResult(
                    allowed = false,
                    reason = "Advanced Astra monthly budget reached. Routing will pause Astra."
                )
            }
        }
        return BudgetCheckResult(allowed = true)
    }

    suspend fun pauseRequestsAtBudgetLimit(reason: String) {
        runCatching {
            db.collection("system_settings").document("ai_provider_config")
                .set(
                    mapOf(
                        "lastErrorSummary" to reason.take(200),
                        "lastErrorAtEpochMs" to System.currentTimeMillis(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
        }
    }

    suspend fun sendAdminBudgetAlert(actorUid: String, message: String) {
        val id = "budget_${System.currentTimeMillis()}"
        runCatching {
            db.collection("audit_logs").document(id).set(
                mapOf(
                    "actorUid" to actorUid,
                    "action" to "ai_budget_alert",
                    "targetType" to "ai_provider_config",
                    "targetId" to "ai_provider_config",
                    "details" to message.take(300),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }
}
