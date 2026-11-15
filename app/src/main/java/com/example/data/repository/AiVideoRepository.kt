package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.R
import com.example.core.AppLanguage
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.AiVideoJobDoc
import com.example.data.model.LearningVideoDoc
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
    private val gamificationEngine: GamificationEngine
) {
    constructor(context: Context, gamificationEngine: GamificationEngine) : this(
        FirebaseFirestore.getInstance(context.applicationContext.getString(R.string.firestore_database_id)),
        gamificationEngine
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

    suspend fun generateChapterStoryboard(
        userId: String,
        classId: String,
        subjectId: String,
        chapterId: String,
        language: AppLanguage
    ): Result<LearningVideoDoc> {
        val chapter = BangladeshCurriculumCatalog.chapters.find { it.id == chapterId }
        val lessons = BangladeshCurriculumCatalog.lessons.filter { it.chapterId == chapterId }
        val sourceTitle = chapter?.let { if (language == AppLanguage.BANGLA) it.titleBn else it.titleEn }
            ?: "NCTB Chapter Study"
        val sourceText = buildString {
            appendLine("Chapter: ${chapter?.titleEn ?: chapterId} (${chapter?.titleBn.orEmpty()})")
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
            sourceType = "chapter",
            sourceTitle = sourceTitle,
            classId = classId,
            subjectId = subjectId,
            chapterId = chapterId,
            textMaterial = sourceText,
            pageBitmaps = emptyList(),
            language = language
        )
    }

    suspend fun generateFromUriOrText(
        context: Context,
        userId: String,
        classId: String,
        subjectId: String,
        documentUri: Uri?,
        pastedNotes: String,
        language: AppLanguage
    ): Result<LearningVideoDoc> {
        var sourceType = "text"
        var sourceTitle = "Custom Study Notes"
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
            sourceTitle = displayName.take(150)

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
            sourceType = sourceType,
            sourceTitle = sourceTitle,
            classId = classId,
            subjectId = subjectId,
            chapterId = null,
            textMaterial = extractedText,
            pageBitmaps = pageBitmaps,
            language = language
        )
    }

    private suspend fun runGenerationJob(
        userId: String,
        sourceType: String,
        sourceTitle: String,
        classId: String,
        subjectId: String,
        chapterId: String?,
        textMaterial: String,
        pageBitmaps: List<Bitmap>,
        language: AppLanguage
    ): Result<LearningVideoDoc> {
        val jobId = "job_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
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
            val videoTitle = root.optString("videoTitle", sourceTitle).take(155)
            val summary = root.optString("summary", "Interactive AI storyboard summary")
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
                    "title" to videoTitle,
                    "classId" to classId,
                    "subjectId" to subjectId,
                    "chapterId" to chapterId,
                    "durationSeconds" to totalDuration,
                    "summary" to summary.take(500),
                    "scenesJson" to scenesArr.toString().take(48_000),
                    "quizJson" to quizArr.toString().take(20_000),
                    "visibility" to "private",
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

            val created = videoRef.get().await().toObject(
                LearningVideoDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: LearningVideoDoc(
                id = videoId,
                ownerUid = userId,
                title = videoTitle,
                classId = classId,
                subjectId = subjectId,
                chapterId = chapterId,
                durationSeconds = totalDuration,
                summary = summary,
                scenesJson = scenesArr.toString(),
                quizJson = quizArr.toString(),
                savedByUids = listOf(userId),
                externalRenderStatus = extStatus
            )
            return Result.success(created)
        } catch (e: TimeoutCancellationException) {
            pageBitmaps.forEach { runCatching { it.recycle() } }
            runCatching {
                jobRef.update(
                    mapOf(
                        "status" to "failed",
                        "errorMessage" to "Generation timed out after 60 seconds.",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            return Result.failure(IllegalStateException("AI storyboard generation timed out. Please try again."))
        } catch (e: CancellationException) {
            pageBitmaps.forEach { runCatching { it.recycle() } }
            throw e
        } catch (e: Exception) {
            pageBitmaps.forEach { runCatching { it.recycle() } }
            Log.e(TAG, "AI storyboard failed", e)
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
                        "summary" to "AI Storyboard Lesson: ${video.title.take(100)}",
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
                description = "AI Storyboard Check Quiz: ${video.title.take(50)}",
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
                        durationSec = o.optInt("durationSec", 12).coerceIn(6, 30)
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
