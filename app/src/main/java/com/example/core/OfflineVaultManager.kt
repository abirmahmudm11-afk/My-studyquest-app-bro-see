package com.example.core

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.model.LearningVideoDoc
import com.example.data.repository.StoryboardQuizQuestion
import com.example.data.repository.StoryboardScene
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class OfflineVaultStats(
    val bundledBooksCount: Int = 0,
    val bundledAudioTracksCount: Int = 0,
    val bundledPlatesCount: Int = 0,
    val workedEntriesCount: Int = 0,
    val bundledVaultSizeMb: Double = 0.0,
    val cachedLocalBooksSizeMb: Double = 0.0
)

data class OfflineAtlasEntry(
    val id: String,
    val subjectId: String,
    val classRange: String,
    val titleEn: String,
    val titleBn: String,
    val keywords: List<String>,
    val formulas: List<String>,
    val stepsEn: List<String>,
    val stepsBn: List<String>,
    val boardTipEn: String,
    val boardTipBn: String
)

/**
 * Central resolver for StudyQuest's bundled Offline Study Vault (assets/study_vault):
 * - Multi-page illustrated NCTB reference PDFs (study_vault/books)
 * - 44.1 kHz studio focus & subject briefing WAVs (study_vault/audio)
 * - High-DPI architectural subject & lab plates (study_vault/plates)
 * - Offline bilingual NCTB worked-solution atlas (study_vault/knowledge_atlas.json)
 */
object OfflineVaultManager {

    private const val TAG = "OfflineVaultManager"
    private const val VAULT_ROOT = "study_vault"

    private val bitmapCache = object : LruCache<String, Bitmap>(16) {}

    @Volatile
    private var cachedAtlasEntries: List<OfflineAtlasEntry>? = null

    @Volatile
    private var cachedStats: OfflineVaultStats? = null

    fun openAudioAssetFd(context: Context, assetFileName: String): AssetFileDescriptor? {
        return runCatching {
            context.applicationContext.assets.openFd("$VAULT_ROOT/audio/$assetFileName")
        }.getOrNull()
    }

    fun hasBundledSubjectPdf(context: Context, subjectId: String): Boolean {
        val list = runCatching {
            context.applicationContext.assets.list("$VAULT_ROOT/books")?.toSet().orEmpty()
        }.getOrDefault(emptySet())
        return "${subjectId}.pdf" in list
    }

    suspend fun copyBundledSubjectPdf(
        context: Context,
        subjectId: String,
        destFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val assets = appContext.assets
        val candidateNames = listOf("${subjectId}.pdf", "math.pdf")
        for (name in candidateNames) {
            val copied = runCatching {
                assets.open("$VAULT_ROOT/books/$name").use { input ->
                    destFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output, bufferSize = 64 * 1024)
                    }
                }
                destFile.exists() && destFile.length() > 1024L
            }.getOrDefault(false)
            if (copied) return@withContext true
        }
        false
    }

    suspend fun loadSubjectPlate(
        context: Context,
        subjectId: String,
        sampleSize: Int = 2
    ): Bitmap? = loadVaultBitmap(context, "plate_${subjectId}.png", sampleSize)

    suspend fun loadLabPlate(
        context: Context,
        labId: String,
        sampleSize: Int = 2
    ): Bitmap? = loadVaultBitmap(context, "lab_${labId}.png", sampleSize)

    private suspend fun loadVaultBitmap(
        context: Context,
        fileName: String,
        sampleSize: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${fileName}_$sampleSize"
        bitmapCache.get(cacheKey)?.let { if (!it.isRecycled) return@withContext it }
        runCatching {
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize.coerceAtLeast(1)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            context.applicationContext.assets.open("$VAULT_ROOT/plates/$fileName").use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)?.also { bmp ->
                    bitmapCache.put(cacheKey, bmp)
                }
            }
        }.onFailure {
            Log.d(TAG, "Plate $fileName not found: ${it.message}")
        }.getOrNull()
    }

    fun loadAtlasEntries(context: Context): List<OfflineAtlasEntry> {
        cachedAtlasEntries?.let { return it }
        val parsed = runCatching {
            val jsonStr = context.applicationContext.assets
                .open("$VAULT_ROOT/knowledge_atlas.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JSONObject(jsonStr)
            val arr = root.optJSONArray("entries") ?: JSONArray()
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        OfflineAtlasEntry(
                            id = obj.optString("id"),
                            subjectId = obj.optString("subjectId"),
                            classRange = obj.optString("classRange", "6-10"),
                            titleEn = obj.optString("titleEn"),
                            titleBn = obj.optString("titleBn"),
                            keywords = obj.optJSONArray("keywords")?.toStringList().orEmpty(),
                            formulas = obj.optJSONArray("formulas")?.toStringList().orEmpty(),
                            stepsEn = obj.optJSONArray("stepsEn")?.toStringList().orEmpty(),
                            stepsBn = obj.optJSONArray("stepsBn")?.toStringList().orEmpty(),
                            boardTipEn = obj.optString("boardTipEn"),
                            boardTipBn = obj.optString("boardTipBn")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
        if (parsed.isNotEmpty()) {
            cachedAtlasEntries = parsed
        }
        return parsed
    }

    /**
     * Searches the bundled Offline Knowledge Atlas + NCTB Curriculum Catalog so students receive
     * structured step-by-step explanations and formulas even in 100% Airplane Mode.
     */
    fun resolveOfflineDoubtExplanation(
        context: Context,
        questionPrompt: String,
        studentQuestion: String,
        classId: String,
        subjectName: String,
        language: AppLanguage
    ): String {
        val entries = loadAtlasEntries(context)
        val combinedQuery = "$studentQuestion $questionPrompt $subjectName".lowercase(Locale.ROOT)
        val tokens = combinedQuery
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 2 }
            .toSet()

        val matchedSubject = BangladeshCurriculumCatalog.subjects.firstOrNull { subj ->
            combinedQuery.contains(subj.id.lowercase(Locale.ROOT)) ||
                combinedQuery.contains(subj.titleEn.lowercase(Locale.ROOT)) ||
                combinedQuery.contains(subj.titleBn)
        }?.id

        val bestAtlas = entries.maxByOrNull { entry ->
            var score = 0
            if (matchedSubject != null && entry.subjectId == matchedSubject) score += 6
            entry.keywords.forEach { kw ->
                val cleanKw = kw.lowercase(Locale.ROOT)
                if (combinedQuery.contains(cleanKw)) score += 5
                if (cleanKw in tokens) score += 3
            }
            val titleWords = "${entry.titleEn} ${entry.titleBn}".lowercase(Locale.ROOT)
                .split(Regex("\\s+"))
            titleWords.forEach { w -> if (w.length >= 3 && w in tokens) score += 2 }
            score
        }

        val matchingLessons = BangladeshCurriculumCatalog.lessons.filter { lesson ->
            (matchedSubject == null || lesson.subjectId == matchedSubject) &&
                tokens.any { tok ->
                    lesson.titleEn.lowercase(Locale.ROOT).contains(tok) ||
                        lesson.explanationEn.lowercase(Locale.ROOT).contains(tok) ||
                        lesson.titleBn.contains(tok)
                }
        }.ifEmpty {
            BangladeshCurriculumCatalog.lessons.filter {
                matchedSubject == null || it.subjectId == matchedSubject
            }.take(1)
        }

        val matchingQuestions = BangladeshCurriculumCatalog.questions.filter { q ->
            (matchedSubject == null || q.subjectId == matchedSubject) &&
                tokens.any { tok -> q.promptEn.lowercase(Locale.ROOT).contains(tok) || q.promptBn.contains(tok) }
        }.take(2)

        return buildString {
            if (language == AppLanguage.BANGLA) {
                appendLine("[অফলাইন স্টাডি ভল্ট সমাধান — ইন্টারনেট ছাড়াই প্রস্তুত]")
                appendLine("শ্রেণি: ${StudyQuestStrings.classLabel(classId, language)} • বিষয়: $subjectName")
                appendLine()
                if (bestAtlas != null) {
                    appendLine("মূল বিষয়: ${bestAtlas.titleBn}")
                    appendLine()
                    appendLine("ধাপে ধাপে সমাধান:")
                    bestAtlas.stepsBn.forEachIndexed { idx, step ->
                        appendLine("${idx + 1}. $step")
                    }
                    if (bestAtlas.formulas.isNotEmpty()) {
                        appendLine()
                        appendLine("গুরুত্বপূর্ণ সূত্র / নিয়ম:")
                        bestAtlas.formulas.forEach { f -> appendLine("• $f") }
                    }
                    if (bestAtlas.boardTipBn.isNotBlank()) {
                        appendLine()
                        appendLine("বোর্ড পরীক্ষার টিপস: ${bestAtlas.boardTipBn}")
                    }
                }
                matchingLessons.firstOrNull()?.let { l ->
                    appendLine()
                    appendLine("এনসিটিবি পাঠ রেফারেন্স — ${l.titleBn}:")
                    appendLine(l.explanationBn)
                }
                if (matchingQuestions.isNotEmpty()) {
                    appendLine()
                    appendLine("সম্পর্কিত প্রশ্ন ও ব্যাখ্যা:")
                    matchingQuestions.forEach { q ->
                        appendLine("• ${q.promptBn}")
                        appendLine("  ব্যাখ্যা: ${q.explanationBn}")
                    }
                }
            } else {
                appendLine("[Offline Study Vault Solution — Works Without Internet]")
                appendLine("Class: ${StudyQuestStrings.classLabel(classId, language)} • Subject: $subjectName")
                appendLine()
                if (bestAtlas != null) {
                    appendLine("Core Concept: ${bestAtlas.titleEn}")
                    appendLine()
                    appendLine("Step-by-Step Guide:")
                    bestAtlas.stepsEn.forEachIndexed { idx, step ->
                        appendLine("${idx + 1}. $step")
                    }
                    if (bestAtlas.formulas.isNotEmpty()) {
                        appendLine()
                        appendLine("Key Formulas & Rules:")
                        bestAtlas.formulas.forEach { f -> appendLine("• $f") }
                    }
                    if (bestAtlas.boardTipEn.isNotBlank()) {
                        appendLine()
                        appendLine("SSC Board Exam Tip: ${bestAtlas.boardTipEn}")
                    }
                }
                matchingLessons.firstOrNull()?.let { l ->
                    appendLine()
                    appendLine("NCTB Lesson Reference — ${l.titleEn}:")
                    appendLine(l.explanationEn)
                }
                if (matchingQuestions.isNotEmpty()) {
                    appendLine()
                    appendLine("Related NCTB Board Question:")
                    matchingQuestions.forEach { q ->
                        appendLine("• ${q.promptEn}")
                        appendLine("  Explanation: ${q.explanationEn}")
                    }
                }
            }
        }.trim()
    }

    /**
     * Generates an illustrated offline multi-scene storyboard & check quiz from the bundled Knowledge Atlas
     * when the student creates a lesson storyboard offline.
     */
    fun buildOfflineStoryboard(
        context: Context,
        subjectId: String,
        topicTitle: String,
        language: AppLanguage
    ): Pair<List<StoryboardScene>, List<StoryboardQuizQuestion>> {
        val entries = loadAtlasEntries(context).filter { it.subjectId == subjectId }
            .ifEmpty { loadAtlasEntries(context) }
        val primary = entries.firstOrNull()
        val lessons = BangladeshCurriculumCatalog.lessons.filter { it.subjectId == subjectId }
            .ifEmpty { BangladeshCurriculumCatalog.lessons.take(2) }
        val questions = BangladeshCurriculumCatalog.questions.filter { it.subjectId == subjectId }
            .ifEmpty { BangladeshCurriculumCatalog.questions.take(3) }

        val scenes = buildList {
            add(
                StoryboardScene(
                    title = topicTitle,
                    caption = StudyQuestStrings.t(
                        language,
                        "Offline Study Vault • Concept Overview",
                        "অফলাইন স্টাডি ভল্ট • মূল ধারণা"
                    ),
                    visualType = "highlight",
                    formulaOrDiagram = if (language == AppLanguage.BANGLA) {
                        primary?.titleBn ?: lessons.firstOrNull()?.titleBn ?: topicTitle
                    } else {
                        primary?.titleEn ?: lessons.firstOrNull()?.titleEn ?: topicTitle
                    },
                    narrationText = if (language == AppLanguage.BANGLA) {
                        lessons.firstOrNull()?.explanationBn ?: primary?.stepsBn?.firstOrNull().orEmpty()
                    } else {
                        lessons.firstOrNull()?.explanationEn ?: primary?.stepsEn?.firstOrNull().orEmpty()
                    },
                    durationSec = 12
                )
            )
            if (primary != null && primary.formulas.isNotEmpty()) {
                add(
                    StoryboardScene(
                        title = StudyQuestStrings.t(language, "Core Formulas & Structure", "মূল সূত্র ও গঠন"),
                        caption = StudyQuestStrings.t(language, "Key equations to memorize", "মনে রাখার প্রধান সূত্র"),
                        visualType = "formula",
                        formulaOrDiagram = primary.formulas.take(4).joinToString("\n"),
                        narrationText = if (language == AppLanguage.BANGLA) {
                            primary.stepsBn.firstOrNull().orEmpty()
                        } else {
                            primary.stepsEn.firstOrNull().orEmpty()
                        },
                        durationSec = 14
                    )
                )
            }
            lessons.firstOrNull()?.diagramAscii?.takeIf { it.isNotBlank() }?.let { diag ->
                add(
                    StoryboardScene(
                        title = StudyQuestStrings.t(language, "Visual Diagram Blueprint", "ভিজ্যুয়াল ডায়াগ্রাম"),
                        caption = lessons.first().titleEn,
                        visualType = "diagram",
                        formulaOrDiagram = diag,
                        narrationText = if (language == AppLanguage.BANGLA) {
                            lessons.first().keyPointsBn.joinToString(". ")
                        } else {
                            lessons.first().keyPointsEn.joinToString(". ")
                        },
                        durationSec = 14
                    )
                )
            }
            add(
                StoryboardScene(
                    title = StudyQuestStrings.t(language, "Worked Problem Steps", "ধাপে ধাপে সমাধান"),
                    caption = StudyQuestStrings.t(language, "Board exam solving method", "বোর্ড পরীক্ষার সমাধান পদ্ধতি"),
                    visualType = "step_list",
                    formulaOrDiagram = if (language == AppLanguage.BANGLA) {
                        (primary?.stepsBn ?: emptyList()).take(4).mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")
                    } else {
                        (primary?.stepsEn ?: emptyList()).take(4).mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")
                    },
                    narrationText = if (language == AppLanguage.BANGLA) {
                        primary?.boardTipBn.orEmpty()
                    } else {
                        primary?.boardTipEn.orEmpty()
                    },
                    durationSec = 14
                )
            )
        }

        val quiz = questions.take(3).map { q ->
            val opts = if (language == AppLanguage.BANGLA) q.optionsBn else q.optionsEn
            val safeOpts = if (opts.size >= 2) opts.take(4) else listOf("A", "B", "C", "D")
            StoryboardQuizQuestion(
                prompt = if (language == AppLanguage.BANGLA) q.promptBn else q.promptEn,
                options = safeOpts,
                correctIndex = q.correctOptionIndices.firstOrNull()?.coerceIn(0, safeOpts.lastIndex) ?: 0,
                explanation = if (language == AppLanguage.BANGLA) q.explanationBn else q.explanationEn
            )
        }

        return scenes to quiz
    }

    fun defaultOfflineVideoDoc(
        context: Context,
        subjectId: String,
        title: String,
        language: AppLanguage
    ): LearningVideoDoc {
        val (scenes, quiz) = buildOfflineStoryboard(context, subjectId, title, language)
        val scenesJsonStr = JSONArray().apply {
            scenes.forEach { s ->
                put(JSONObject().apply {
                    put("title", s.title)
                    put("caption", s.caption)
                    put("visualType", s.visualType)
                    put("formulaOrDiagram", s.formulaOrDiagram)
                    put("narrationText", s.narrationText)
                    put("durationSec", s.durationSec)
                })
            }
        }.toString()
        val quizJsonStr = JSONArray().apply {
            quiz.forEach { q ->
                put(JSONObject().apply {
                    put("prompt", q.prompt)
                    put("options", JSONArray(q.options))
                    put("correctIndex", q.correctIndex)
                    put("explanation", q.explanation)
                })
            }
        }.toString()

        val desc = StudyQuestStrings.t(
            language,
            "100% offline illustrated NCTB chapter storyboard & check quiz.",
            "সম্পূর্ণ অফলাইন এনসিটিবি অধ্যায় স্টোরিবোর্ড ও কুইজ।"
        )

        return LearningVideoDoc(
            id = "offline_storyboard_${subjectId}_${System.currentTimeMillis()}",
            ownerUid = "offline_vault",
            ownerName = "StudyQuest Offline Vault",
            title = title,
            description = desc,
            language = language.code,
            animationStyle = "diagram_focus",
            videoSourceType = "animated_lesson",
            classId = "class_10",
            subjectId = subjectId,
            durationSeconds = scenes.sumOf { it.durationSec }.toLong(),
            summary = desc,
            scenesJson = scenesJsonStr,
            quizJson = quizJsonStr,
            visibility = "private",
            externalRenderStatus = "offline_vault_ready"
        )
    }

    suspend fun computeVaultStats(context: Context): OfflineVaultStats = withContext(Dispatchers.IO) {
        cachedStats?.let { return@withContext it }
        val appContext = context.applicationContext
        val assets = appContext.assets
        var totalBytes = 0L

        fun measureDir(subDir: String): Int {
            val files = runCatching { assets.list("$VAULT_ROOT/$subDir").orEmpty() }.getOrDefault(emptyArray())
            files.forEach { name ->
                val len = runCatching {
                    assets.openFd("$VAULT_ROOT/$subDir/$name").use { it.length.coerceAtLeast(0L) }
                }.getOrElse {
                    runCatching {
                        assets.open("$VAULT_ROOT/$subDir/$name").use { stream ->
                            var count = 0L
                            val buf = ByteArray(16384)
                            while (true) {
                                val r = stream.read(buf)
                                if (r <= 0) break
                                count += r
                            }
                            count
                        }
                    }.getOrDefault(0L)
                }
                totalBytes += len
            }
            return files.size
        }

        val books = measureDir("books")
        val audio = measureDir("audio")
        val plates = measureDir("plates")
        val atlasBytes = runCatching {
            assets.openFd("$VAULT_ROOT/knowledge_atlas.json").use { it.length.coerceAtLeast(0L) }
        }.getOrDefault(0L)
        totalBytes += atlasBytes

        val entriesCount = loadAtlasEntries(appContext).size
        val localCacheBytes = File(appContext.filesDir, "textbooks")
            .takeIf { it.exists() }
            ?.walkTopDown()
            ?.filter { it.isFile }
            ?.sumOf { it.length() }
            ?: 0L

        OfflineVaultStats(
            bundledBooksCount = books,
            bundledAudioTracksCount = audio,
            bundledPlatesCount = plates,
            workedEntriesCount = entriesCount,
            bundledVaultSizeMb = totalBytes / (1024.0 * 1024.0),
            cachedLocalBooksSizeMb = localCacheBytes / (1024.0 * 1024.0)
        ).also {
            if (totalBytes > 0L) cachedStats = it
        }
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (i in 0 until length()) {
            optString(i).takeIf { it.isNotBlank() }?.let(::add)
        }
    }
}
