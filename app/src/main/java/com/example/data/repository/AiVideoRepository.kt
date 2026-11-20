package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.R
import com.example.core.AppLanguage
import com.example.core.BillingAndAdProvider
import com.example.core.OfflineVaultManager
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.AiVideoJobDoc
import com.example.data.model.LearningVideoDoc
import com.example.data.model.UserSubscriptionDoc
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class StoryboardScene(
    val title: String,
    val caption: String,
    val visualType: String, // "formula", "diagram", "step_list", "highlight"
    val formulaOrDiagram: String,
    val narrationText: String,
    val durationSec: Int
)

data class StoryboardQuizQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

class AiVideoRepository(
    private val db: FirebaseFirestore,
    private val gamificationEngine: GamificationEngine,
    private val billingProvider: BillingAndAdProvider = BillingAndAdProvider(db),
    private val appContext: Context? = null
) {
    constructor(context: Context, gamificationEngine: GamificationEngine) : this(
        db = FirebaseFirestore.getInstance(context.applicationContext.getString(R.string.firestore_database_id)),
        gamificationEngine = gamificationEngine,
        appContext = context.applicationContext
    )

    private val storyboardSchema = Schema.obj(
        properties = mapOf(
            "videoTitle" to Schema.string(description = "Concise lesson video title"),
            "summary" to Schema.string(description = "Brief 2-sentence overview of the lesson"),
            "scenes" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Scene header"),
                        "caption" to Schema.string(description = "Short on-screen subtitle caption"),
                        "visualType" to Schema.enumeration(
                            values = listOf("formula", "diagram", "step_list", "highlight"),
                            description = "Visual layout style for the scene"
                        ),
                        "formulaOrDiagram" to Schema.string(description = "Key formula, equation, or structured bullet points shown on the board"),
                        "narrationText" to Schema.string(description = "Spoken voiceover script for TTS (1-2 clear sentences)"),
                        "durationSec" to Schema.integer(description = "Scene duration in seconds between 8 and 20")
                    )
                ),
                description = "5 to 7 sequential storyboard scenes explaining the topic step by step"
            ),
            "quiz" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "prompt" to Schema.string(description = "Check-for-understanding question"),
                        "options" to Schema.array(items = Schema.string(), description = "Exactly 4 short answer options"),
                        "correctIndex" to Schema.integer(description = "0-based index of the right option"),
                        "explanation" to Schema.string(description = "Short explanation why the answer is right")
                    )
                ),
                description = "3 post-video multiple choice check questions"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = storyboardSchema
            }
        )
    }

    fun observeAiVideoJobs(userId: String): Flow<List<AiVideoJobDoc>> =
        db.collection("users").document(userId).collection("ai_video_jobs")
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    AiVideoJobDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeLearningVideos(userId: String): Flow<List<LearningVideoDoc>> =
        db.collection("learning_videos")
            .whereEqualTo("ownerUid", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    LearningVideoDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observePublicLibraryVideos(): Flow<List<LearningVideoDoc>> =
        db.collection("learning_videos")
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snap ->
                val cloud = snap.toObjects(
                    LearningVideoDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                if (cloud.isEmpty()) curatedStarterLibrary() else cloud + curatedStarterLibrary()
            }

    suspend fun generateChapterStoryboard(
        userId: String,
        ownerName: String,
        classId: String,
        subjectId: String,
        chapterId: String,
        customTitle: String,
        customDescription: String,
        animationStyle: String,
        visibility: String,
        language: AppLanguage,
        subscription: UserSubscriptionDoc
    ): Result<LearningVideoDoc> {
        val chapter = BangladeshCurriculumCatalog.chapters.find { it.id == chapterId }
        val lessons = BangladeshCurriculumCatalog.lessons.filter { it.chapterId == chapterId }
        val sourceTitle = customTitle.trim().ifBlank {
            chapter?.let { if (language == AppLanguage.BANGLA) it.titleBn else it.titleEn }
                ?: "NCTB Chapter Study"
        }
        val sourceText = buildString {
            appendLine("Chapter: ${chapter?.titleEn ?: chapterId} (${chapter?.titleBn.orEmpty()})")
            if (customDescription.isNotBlank()) appendLine("Creator Focus Note: $customDescription")
            appendLine("Summary: ${chapter?.summaryEn.orEmpty()}")
            lessons.take(3).forEach { l ->
                appendLine("Lesson: ${l.titleEn}")
                appendLine(l.explanationEn)
                if (l.formulas.isNotEmpty()) appendLine("Formulas: ${l.formulas.joinToString("; ")}")
                if (l.keyPointsEn.isNotEmpty()) appendLine("Key Points: ${l.keyPointsEn.joinToString("; ")}")
            }
        }
        return runGenerationJob(
            userId = userId,
            ownerName = ownerName,
            sourceType = "chapter",
            sourceTitle = sourceTitle,
            customDescription = customDescription,
            animationStyle = animationStyle,
            visibility = visibility,
            classId = classId,
            subjectId = subjectId,
            chapterId = chapterId,
            textMaterial = sourceText,
            pageBitmaps = emptyList(),
            language = language,
            subscription = subscription
        )
    }

    suspend fun generateFromUriOrText(
        context: Context,
        userId: String,
        ownerName: String,
        classId: String,
        subjectId: String,
        documentUri: Uri?,
        pastedNotes: String,
        customTitle: String,
        customDescription: String,
        animationStyle: String,
        visibility: String,
        language: AppLanguage,
        subscription: UserSubscriptionDoc
    ): Result<LearningVideoDoc> {
        var sourceType = "text"
        var sourceTitle = customTitle.trim().ifBlank { "Custom Study Notes" }
        var extractedText = pastedNotes.trim().take(12_000)
        val pageBitmaps = mutableListOf<Bitmap>()

        if (documentUri != null) {
            val resolver = context.contentResolver
            var displayName = "Uploaded Document"
            var fileSize = 0L
            resolver.query(documentUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                    if (sizeIdx >= 0) fileSize = cursor.getLong(sizeIdx)
                }
            }
            if (fileSize > 20L * 1024L * 1024L) {
                return Result.failure(IllegalArgumentException("File exceeds 20 MB limit. Please pick a smaller PDF or note file."))
            }
            val mime = resolver.getType(documentUri).orEmpty()
            if (customTitle.isBlank()) {
                sourceTitle = displayName.take(150)
            }

            if (mime.contains("pdf", ignoreCase = true) || displayName.endsWith(".pdf", ignoreCase = true)) {
                sourceType = "pdf"
                runCatching {
                    resolver.openFileDescriptor(documentUri, "r")?.use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            val pagesToRender = renderer.pageCount.coerceAtMost(3)
                            for (idx in 0 until pagesToRender) {
                                renderer.openPage(idx).use { page ->
                                    val targetW = 960
                                    val targetH = ((page.height.toFloat() / page.width.coerceAtLeast(1)) * targetW).toInt().coerceIn(320, 1400)
                                    val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                                    bmp.eraseColor(android.graphics.Color.WHITE)
                                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    pageBitmaps.add(bmp)
                                }
                            }
                        }
                    }
                }.onFailure { err ->
                    return Result.failure(IllegalStateException("Could not read PDF document: ${err.localizedMessage ?: "Invalid PDF"}"))
                }
            } else {
                val readText = runCatching {
                    resolver.openInputStream(documentUri)?.bufferedReader()?.use { it.readText().take(12_000) }
                }.getOrNull().orEmpty()
                if (readText.isNotBlank()) {
                    extractedText = if (extractedText.isBlank()) readText else "$readText\n\n$extractedText"
                }
            }
        }

        if (extractedText.isBlank() && pageBitmaps.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please pick a readable PDF/text file or enter study notes."))
        }

        return runGenerationJob(
            userId = userId,
            ownerName = ownerName,
            sourceType = sourceType,
            sourceTitle = sourceTitle,
            customDescription = customDescription,
            animationStyle = animationStyle,
            visibility = visibility,
            classId = classId,
            subjectId = subjectId,
            chapterId = null,
            textMaterial = extractedText,
            pageBitmaps = pageBitmaps,
            language = language,
            subscription = subscription
        )
    }

    suspend fun publishUploadedVideo(
        context: Context,
        userId: String,
        ownerName: String,
        videoUri: Uri,
        title: String,
        description: String,
        classId: String,
        subjectId: String,
        language: AppLanguage,
        visibility: String
    ): Result<LearningVideoDoc> {
        val cleanTitle = title.trim().ifBlank { "My Study Video Lesson" }.take(150)
        val cleanDesc = description.trim().ifBlank { "Student-uploaded educational lesson" }.take(500)
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                videoUri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val videoId = "vid_up_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val defaultScene = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("title", cleanTitle)
                    put("caption", cleanDesc)
                    put("visualType", "highlight")
                    put("formulaOrDiagram", cleanTitle)
                    put("narrationText", cleanDesc)
                    put("durationSec", 60)
                }
            )
        }.toString()
        val videoRef = db.collection("learning_videos").document(videoId)
        return try {
            videoRef.set(
                mapOf(
                    "ownerUid" to userId,
                    "ownerName" to ownerName.take(60),
                    "title" to cleanTitle,
                    "description" to cleanDesc,
                    "language" to language.code,
                    "animationStyle" to "uploaded_native",
                    "videoSourceType" to "uploaded_video",
                    "localVideoUri" to videoUri.toString(),
                    "classId" to classId,
                    "subjectId" to subjectId,
                    "durationSeconds" to 60L,
                    "summary" to cleanDesc,
                    "scenesJson" to defaultScene,
                    "quizJson" to "[]",
                    "visibility" to if (visibility == "public") "public" else "private",
                    "savedByUids" to listOf(userId),
                    "lastWatchedScene" to 0L,
                    "externalRenderStatus" to "unconfigured",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val created = videoRef.get().await().toObject(
                LearningVideoDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: LearningVideoDoc(
                id = videoId,
                ownerUid = userId,
                ownerName = ownerName,
                title = cleanTitle,
                description = cleanDesc,
                language = language.code,
                videoSourceType = "uploaded_video",
                localVideoUri = videoUri.toString(),
                classId = classId,
                subjectId = subjectId,
                summary = cleanDesc,
                scenesJson = defaultScene,
                visibility = if (visibility == "public") "public" else "private"
            )
            Result.success(created)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, videoRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateVideoMetadata(
        videoId: String,
        title: String,
        description: String,
        visibility: String
    ): Result<Unit> {
        if (videoId.startsWith("starter_vid_")) return Result.success(Unit)
        return try {
            db.collection("learning_videos").document(videoId).update(
                mapOf(
                    "title" to title.trim().take(150),
                    "description" to description.trim().take(500),
                    "summary" to description.trim().take(500),
                    "visibility" to if (visibility == "public") "public" else "private",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun runGenerationJob(
        userId: String,
        ownerName: String,
        sourceType: String,
        sourceTitle: String,
        customDescription: String,
        animationStyle: String,
        visibility: String,
        classId: String,
        subjectId: String,
        chapterId: String?,
        textMaterial: String,
        pageBitmaps: List<Bitmap>,
        language: AppLanguage,
        subscription: UserSubscriptionDoc
    ): Result<LearningVideoDoc> {
        val jobId = "job_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val creditSource = billingProvider.reserveVideoCredit(userId, subscription, jobId)
            .getOrElse { err ->
                pageBitmaps.forEach { runCatching { it.recycle() } }
                val ctx = appContext
                if (ctx != null && (userId.isBlank() || userId == "guest_local" || err is com.google.firebase.firestore.FirebaseFirestoreException || err is java.io.IOException)) {
                    return Result.success(
                        OfflineVaultManager.defaultOfflineVideoDoc(
                            context = ctx,
                            subjectId = subjectId,
                            title = sourceTitle,
                            language = language
                        )
                    )
                }
                return Result.failure(err)
            }

        val jobRef = db.collection("users").document(userId).collection("ai_video_jobs").document(jobId)
        val externalRenderEnabled = runCatching {
            db.collection("system_settings").document("global").get().await()
                .getBoolean("externalVideoRendererConfigured") ?: false
        }.getOrDefault(false)
        val extStatus = if (externalRenderEnabled) "queued" else "unconfigured"

        try {
            jobRef.set(
                mapOf(
                    "userId" to userId,
                    "sourceType" to sourceType,
                    "sourceTitle" to sourceTitle.take(155),
                    "description" to customDescription.take(450),
                    "language" to language.code,
                    "animationStyle" to animationStyle,
                    "classId" to classId,
                    "subjectId" to subjectId,
                    "chapterId" to chapterId,
                    "status" to "processing",
                    "progressPercent" to 25L,
                    "externalRenderStatus" to extStatus,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            ).await()

            val langInstruction = if (language == AppLanguage.BANGLA) {
                "Generate all titles, captions, voiceover narration, and quiz items in clear natural academic Bangla (বাংলা)."
            } else {
                "Generate all titles, captions, voiceover narration, and quiz items in clear academic English."
            }

            val promptText = """
                Create an interactive educational storyboard lesson for a Bangladeshi NCTB student ($classId, subject: $subjectId).
                Source Title: $sourceTitle
                Description: ${customDescription.ifBlank { "Step-by-step animated visual lesson" }}
                Animation Style: $animationStyle
                $langInstruction
                Produce 5 to 6 concise visual storyboard scenes with synchronized narration scripts and 3 MCQ check questions.
                Study Material:
                ${textMaterial.take(10_000)}
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(
                    content {
                        text(promptText)
                        pageBitmaps.forEach { bmp -> image(bmp) }
                    }
                )
            }

            pageBitmaps.forEach { runCatching { it.recycle() } }

            val rawJson = response.text ?: throw IllegalStateException("AI storyboard generator returned empty output.")
            val root = JSONObject(rawJson)
            val videoTitle = sourceTitle.take(155)
            val summary = customDescription.trim().ifBlank {
                root.optString("summary", "Interactive animated storyboard lesson")
            }.take(500)
            val scenesArr = root.optJSONArray("scenes") ?: JSONArray()
            val quizArr = root.optJSONArray("quiz") ?: JSONArray()

            var totalDuration = 0L
            for (i in 0 until scenesArr.length()) {
                totalDuration += scenesArr.getJSONObject(i).optInt("durationSec", 12).coerceIn(6, 30)
            }
            totalDuration = totalDuration.coerceIn(15L, 3600L)

            val videoId = "vid_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val videoRef = db.collection("learning_videos").document(videoId)

            val batch = db.batch()
            batch.set(
                videoRef,
                mapOf(
                    "ownerUid" to userId,
                    "ownerName" to ownerName.take(60),
                    "title" to videoTitle,
                    "description" to summary,
                    "language" to language.code,
                    "animationStyle" to animationStyle,
                    "videoSourceType" to "animated_lesson",
                    "classId" to classId,
                    "subjectId" to subjectId,
                    "chapterId" to chapterId,
                    "durationSeconds" to totalDuration,
                    "summary" to summary,
                    "scenesJson" to scenesArr.toString().take(48_000),
                    "quizJson" to quizArr.toString().take(20_000),
                    "visibility" to if (visibility == "public") "public" else "private",
                    "savedByUids" to listOf(userId),
                    "lastWatchedScene" to 0L,
                    "externalRenderStatus" to extStatus,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
            )
            batch.update(
                jobRef,
                mapOf(
                    "status" to "completed",
                    "progressPercent" to 100L,
                    "videoId" to videoId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            billingProvider.completeVideoCreditUsage(userId, jobId)

            val created = videoRef.get().await().toObject(
                LearningVideoDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: LearningVideoDoc(
                id = videoId,
                ownerUid = userId,
                ownerName = ownerName,
                title = videoTitle,
                description = summary,
                language = language.code,
                animationStyle = animationStyle,
                classId = classId,
                subjectId = subjectId,
                chapterId = chapterId,
                durationSeconds = totalDuration,
                summary = summary,
                scenesJson = scenesArr.toString(),
                quizJson = quizArr.toString(),
                visibility = if (visibility == "public") "public" else "private",
                savedByUids = listOf(userId),
                externalRenderStatus = extStatus
            )
            return Result.success(created)
        } catch (e: TimeoutCancellationException) {
            pageBitmaps.forEach { runCatching { it.recycle() } }
            billingProvider.restoreFailedVideoCredit(userId, jobId, creditSource, "Generation timed out")
            val ctx = appContext
            if (ctx != null) {
                return Result.success(
                    OfflineVaultManager.defaultOfflineVideoDoc(
                        context = ctx,
                        subjectId = subjectId,
                        title = sourceTitle,
                        language = language
                    )
                )
            }
            runCatching {
                jobRef.update(
                    mapOf(
                        "status" to "failed",
                        "errorMessage" to "Generation timed out. Video credit restored.",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            return Result.failure(IllegalStateException("Animated lesson generation timed out. Your video credit has been restored."))
        } catch (e: CancellationException) {
            pageBitmaps.forEach { runCatching { it.recycle() } }
            billingProvider.restoreFailedVideoCredit(userId, jobId, creditSource, "Cancelled")
            throw e
        } catch (e: Exception) {
            pageBitmaps.forEach { runCatching { it.recycle() } }
            billingProvider.restoreFailedVideoCredit(userId, jobId, creditSource, e.localizedMessage ?: "Failed")
            Log.e(TAG, "AI storyboard failed", e)
            val ctx = appContext
            if (ctx != null) {
                return Result.success(
                    OfflineVaultManager.defaultOfflineVideoDoc(
                        context = ctx,
                        subjectId = subjectId,
                        title = sourceTitle,
                        language = language
                    )
                )
            }
            runCatching {
                jobRef.update(
                    mapOf(
                        "status" to "failed",
                        "errorMessage" to (e.localizedMessage ?: "Generation failed").take(180),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            handleFirestoreError(e, OperationType.WRITE, jobRef.path)
            return Result.failure(e)
        }
    }

    suspend fun saveVideoWatchProgress(videoId: String, sceneIndex: Int) {
        if (videoId.startsWith("starter_vid_")) return
        runCatching {
            db.collection("learning_videos").document(videoId)
                .update(
                    mapOf(
                        "lastWatchedScene" to sceneIndex.toLong().coerceIn(0L, 100L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        }
    }

    suspend fun toggleSavePublicVideo(userId: String, video: LearningVideoDoc) {
        if (video.id.startsWith("starter_vid_") || userId.isBlank()) return
        val isSaved = userId in video.savedByUids
        runCatching {
            db.collection("learning_videos").document(video.id).update(
                mapOf(
                    "savedByUids" to if (isSaved) FieldValue.arrayRemove(userId) else FieldValue.arrayUnion(userId),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun recordAiVideoQuizCompleted(
        userId: String,
        video: LearningVideoDoc,
        correctCount: Int,
        totalQuestions: Int
    ): Result<Long> {
        val accuracy = if (totalQuestions > 0) correctCount.toDouble() / totalQuestions.toDouble() else 0.0
        val xp = (15L + correctCount * 10L).coerceIn(15L, 60L)
        val recId = "aivid_${video.id}"

        return try {
            val recRef = db.collection("users").document(userId).collection("learning_records").document(recId)
            if (!recRef.get().await().exists()) {
                recRef.set(
                    mapOf(
                        "userId" to userId,
                        "recordType" to "ai_video",
                        "refId" to video.id,
                        "subjectId" to video.subjectId,
                        "chapterId" to video.chapterId,
                        "score" to correctCount.toDouble(),
                        "totalMarks" to totalQuestions.toDouble(),
                        "accuracy" to accuracy,
                        "xpAwarded" to xp,
                        "idempotencyKey" to recId,
                        "summary" to "Animated Video Lesson: ${video.title.take(100)}",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ).filterValues { it != null }
                ).await()
            }

            gamificationEngine.awardVerifiedActivity(
                userId = userId,
                sourceType = "ai_video",
                sourceId = video.id,
                idempotencyKey = "xp_$recId",
                rawXpDelta = xp,
                coinDelta = 10L,
                description = "Video Lesson Check Quiz: ${video.title.take(50)}",
                questionsSolvedDelta = totalQuestions.toLong(),
                subjectId = video.subjectId
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/$userId/learning_records/$recId")
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AiVideoRepository"

        fun curatedStarterLibrary(): List<LearningVideoDoc> = listOf(
            LearningVideoDoc(
                id = "starter_vid_trig_ratios",
                ownerUid = "studyquest_editorial",
                ownerName = "StudyQuest Editorial",
                title = "Trigonometry Ratios & Right Triangle Intuition",
                description = "Master sin, cos, tan and SSC Chapter 9 identities visually in 60 seconds.",
                language = "en",
                animationStyle = "diagram_focus",
                videoSourceType = "animated_lesson",
                classId = "class_10",
                subjectId = "math",
                durationSeconds = 48L,
                summary = "Visual intuition for sin θ = Opposite/Hypotenuse, cos θ = Adjacent/Hypotenuse, and Pythagorean identities.",
                visibility = "public",
                scenesJson = JSONArray().apply {
                    put(sceneObj("1. Meet the Right Triangle", "Every SSC trigonometry identity starts from one angle θ inside a right triangle.", "diagram", "Hypotenuse (c) | Opposite (a) | Adjacent (b)", "Every SSC trigonometry identity starts from one angle theta inside a right triangle.", 12))
                    put(sceneObj("2. The Three Core Ratios", "Remember SOH-CAH-TOA to connect side lengths cleanly.", "formula", "sin θ = a/c  •  cos θ = b/c  •  tan θ = a/b", "Remember SOH CAH TOA to connect side lengths cleanly.", 12))
                    put(sceneObj("3. Fundamental Identity", "Apply Pythagoras a² + b² = c² and divide by c².", "formula", "sin²θ + cos²θ = 1\n1 + tan²θ = sec²θ", "Apply Pythagoras and divide by c squared to derive sine squared plus cosine squared equals one.", 12))
                    put(sceneObj("4. Board Exam Tip", "Always sketch and label the 90° corner before plugging values.", "highlight", "tan 45° = 1  |  sin 30° = 1/2  |  cos 60° = 1/2", "Always sketch and label the ninety degree corner before plugging values.", 12))
                }.toString(),
                quizJson = JSONArray().apply {
                    put(quizObj("What is the value of sin²30° + cos²30°?", listOf("0", "0.5", "1", "2"), 2, "For any angle θ, sin²θ + cos²θ = 1."))
                    put(quizObj("In a right triangle, sin θ equals:", listOf("Adjacent / Hypotenuse", "Opposite / Hypotenuse", "Opposite / Adjacent", "Hypotenuse / Opposite"), 1, "sin θ is the ratio of Opposite side to Hypotenuse."))
                }.toString()
            ),
            LearningVideoDoc(
                id = "starter_vid_motion_bn",
                ownerUid = "studyquest_editorial",
                ownerName = "StudyQuest Editorial",
                title = "গতির সমীকরণ ও ত্বরণ (Equations of Motion)",
                description = "শ্রেণি ৯–১০ পদার্থবিজ্ঞান: বেগ, ত্বরণ এবং v = u + at সূত্রের অ্যানিমেটেড ব্যাখ্যা।",
                language = "bn",
                animationStyle = "step_cards",
                videoSourceType = "animated_lesson",
                classId = "class_9",
                subjectId = "physics",
                durationSeconds = 45L,
                summary = "সমত্বরণে গতিশীল বস্তুর ৪টি মূল সমীকরণ সহজ বাংলায় ধাপে ধাপে।",
                visibility = "public",
                scenesJson = JSONArray().apply {
                    put(sceneObj("১. বেগ ও ত্বরণ কী?", "সময়ের সাথে বেগের পরিবর্তনের হারকে ত্বরণ (a) বলে।", "highlight", "a = (v - u) / t\nএকক: m s⁻²", "সময়ের সাথে বেগের পরিবর্তনের হারকে ত্বরণ বলে। এর একক মিটার পার সেকেন্ড স্কয়ার।", 15))
                    put(sceneObj("২. প্রথম ও দ্বিতীয় সমীকরণ", "আদিবেগ u থেকে t সময়ে শেষবেগ v হলে:", "formula", "v = u + at\ns = ut + ½at²", "আদিবেগ ইউ থেকে টি সময়ে শেষবেগ ভি হলে v সমান u যোগ at।", 15))
                    put(sceneObj("৩. অঙ্ক করার সহজ নিয়ম", "বস্তু স্থির অবস্থান থেকে যাত্রা করলে সবসময় u = 0 ধরবে।", "step_list", "স্থির অবস্থান ⇒ u = 0\nথামার সময় ⇒ v = 0", "বস্তু স্থির অবস্থান থেকে যাত্রা করলে আদিবেগ শূন্য ধরবে।", 15))
                }.toString(),
                quizJson = JSONArray().apply {
                    put(quizObj("স্থির অবস্থান থেকে যাত্রা শুরু করলে আদিবেগ (u) কত হবে?", listOf("১০ m/s", "০ m/s", "৯.৮ m/s", "১ m/s"), 1, "স্থির অবস্থান থেকে শুরু করলে u = 0।"))
                    put(quizObj("ত্বরণের এসআই (SI) একক কোনটি?", listOf("m/s", "m/s²", "N", "J"), 1, "ত্বরণের একক m/s²।"))
                }.toString()
            )
        )

        private fun sceneObj(
            title: String,
            caption: String,
            visualType: String,
            formulaOrDiagram: String,
            narration: String,
            durationSec: Int
        ): JSONObject = JSONObject().apply {
            put("title", title)
            put("caption", caption)
            put("visualType", visualType)
            put("formulaOrDiagram", formulaOrDiagram)
            put("narrationText", narration)
            put("durationSec", durationSec)
        }

        private fun quizObj(
            prompt: String,
            options: List<String>,
            correctIdx: Int,
            explanation: String
        ): JSONObject = JSONObject().apply {
            put("prompt", prompt)
            put("options", JSONArray(options))
            put("correctIndex", correctIdx)
            put("explanation", explanation)
        }

        fun parseScenes(scenesJson: String): List<StoryboardScene> {
            return runCatching {
                val arr = JSONArray(scenesJson)
                (0 until arr.length()).map { idx ->
                    val o = arr.getJSONObject(idx)
                    StoryboardScene(
                        title = o.optString("title", "Scene ${idx + 1}"),
                        caption = o.optString("caption", ""),
                        visualType = o.optString("visualType", "highlight"),
                        formulaOrDiagram = o.optString("formulaOrDiagram", ""),
                        narrationText = o.optString("narrationText", o.optString("caption", "")),
                        durationSec = o.optInt("durationSec", 12).coerceIn(6, 60)
                    )
                }
            }.getOrDefault(emptyList())
        }

        fun parseQuiz(quizJson: String): List<StoryboardQuizQuestion> {
            return runCatching {
                val arr = JSONArray(quizJson)
                (0 until arr.length()).map { idx ->
                    val o = arr.getJSONObject(idx)
                    val optsArr = o.optJSONArray("options") ?: JSONArray()
                    val opts = (0 until optsArr.length()).map { optsArr.getString(it) }
                    StoryboardQuizQuestion(
                        prompt = o.optString("prompt", ""),
                        options = opts,
                        correctIndex = o.optInt("correctIndex", 0).coerceIn(0, (opts.size - 1).coerceAtLeast(0)),
                        explanation = o.optString("explanation", "")
                    )
                }
            }.getOrDefault(emptyList())
        }
    }
}
