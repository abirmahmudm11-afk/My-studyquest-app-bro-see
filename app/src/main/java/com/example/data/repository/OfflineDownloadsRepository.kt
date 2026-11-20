package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.core.AppLanguage
import com.example.core.OfflineVaultManager
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.DownloadPackEntity
import com.example.data.local.StudyQuestDao
import com.example.data.model.AcademicGroup
import com.example.data.model.TextbookCatalogItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DownloadPackCategory {
    const val TEXTBOOK = "textbook"
    const val AUDIO_BRIEFING = "audio_briefing"
    const val FOCUS_SOUNDSCAPE = "focus_soundscape"
    const val STEM_LAB_PACK = "stem_lab_pack"
}

class OfflineDownloadsRepository(
    private val context: Context,
    private val dao: StudyQuestDao,
    private val textbookRepo: TextbookDownloadRepository
) {
    private val appContext = context.applicationContext

    private fun packsDir(): File =
        File(appContext.filesDir, "vault_packs").apply { if (!exists()) mkdirs() }

    fun observeDownloadPacks(): Flow<List<DownloadPackEntity>> = dao.observeDownloadPacks()

    suspend fun seedCatalogIfNeeded(
        classId: String = "class_10",
        curriculumVersion: String = "bangla",
        academicGroup: String = AcademicGroup.SCIENCE.id
    ) = withContext(Dispatchers.IO) {
        val packs = buildDefaultDownloadCatalog(classId, curriculumVersion, academicGroup)
        dao.insertDownloadPacksIfAbsent(packs)
        // Reconcile any textbooks already installed in CachedBookEntity
        val existingBooks = textbookRepo.pdfFileForBook("").parentFile?.listFiles()?.map { it.name }.orEmpty().toSet()
        packs.filter { it.category == DownloadPackCategory.TEXTBOOK }.forEach { pack ->
            val pdfFile = textbookRepo.pdfFileForBook(pack.assetFileName)
            if (pdfFile.exists() && pdfFile.length() > 1024L || "${pack.assetFileName}.pdf" in existingBooks) {
                val cur = dao.getDownloadPack(pack.packId)
                if (cur != null && cur.status != "installed") {
                    dao.updateDownloadPackState(
                        packId = pack.packId,
                        status = "installed",
                        progressPct = 100,
                        localPath = pdfFile.absolutePath,
                        installedAtMs = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    suspend fun downloadOrInstallPack(
        pack: DownloadPackEntity,
        isOnline: Boolean,
        language: AppLanguage = AppLanguage.ENGLISH
    ): Result<DownloadPackEntity> = withContext(Dispatchers.IO) {
        try {
            dao.updateDownloadPackState(pack.packId, "downloading", 15, pack.localPath, pack.installedAtMs)

            if (pack.category == DownloadPackCategory.TEXTBOOK) {
                val book = TextbookCatalog.bookById(pack.assetFileName)
                    ?: TextbookCatalog.defaultBookFor(pack.classId, "bangla", AcademicGroup.SCIENCE.id)
                if (!book.pdfUrl.isNullOrBlank() && !isOnline) {
                    dao.updateDownloadPackState(pack.packId, "failed", 0, pack.localPath, 0L)
                    return@withContext Result.failure(IllegalStateException("Internet required to fetch remote PDF edition."))
                }
                dao.updateDownloadPackState(pack.packId, "downloading", 55, pack.localPath, 0L)
                val res = textbookRepo.ensureBookInstalledOffline(book, forceRedownloadPdf = false, language = language)
                return@withContext res.fold(
                    onSuccess = { cached ->
                        val updated = pack.copy(
                            status = "installed",
                            progressPct = 100,
                            localPath = cached.localPdfPath,
                            installedAtMs = System.currentTimeMillis()
                        )
                        dao.upsertDownloadPack(updated)
                        Result.success(updated)
                    },
                    onFailure = { err ->
                        dao.updateDownloadPackState(pack.packId, "failed", 0, null, 0L)
                        Result.failure(err)
                    }
                )
            }

            // Copy real audio briefing / focus soundscape / STEM plate asset from bundled Study Vault into app-private packsDir
            val assetSubPath = when (pack.category) {
                DownloadPackCategory.AUDIO_BRIEFING,
                DownloadPackCategory.FOCUS_SOUNDSCAPE -> "study_vault/audio/${pack.assetFileName}"
                DownloadPackCategory.STEM_LAB_PACK -> "study_vault/plates/${pack.assetFileName}"
                else -> "study_vault/${pack.assetFileName}"
            }
            val destFile = File(packsDir(), "${pack.packId}_${pack.assetFileName}")
            delay(120L)
            dao.updateDownloadPackState(pack.packId, "downloading", 55, null, 0L)

            val copied = runCatching {
                appContext.assets.open(assetSubPath).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output, bufferSize = 64 * 1024)
                    }
                }
                destFile.exists() && destFile.length() > 512L
            }.getOrDefault(false)

            if (!copied) {
                dao.updateDownloadPackState(pack.packId, "failed", 0, null, 0L)
                return@withContext Result.failure(IllegalStateException("Asset $assetSubPath could not be unpacked."))
            }

            delay(100L)
            val updated = pack.copy(
                status = "installed",
                progressPct = 100,
                localPath = destFile.absolutePath,
                installedAtMs = System.currentTimeMillis()
            )
            dao.upsertDownloadPack(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Log.e(TAG, "Pack install failed for ${pack.packId}", e)
            dao.updateDownloadPackState(pack.packId, "failed", 0, null, 0L)
            Result.failure(e)
        }
    }

    suspend fun downloadClassBundle(
        classId: String,
        curriculumVersion: String,
        academicGroup: String,
        isOnline: Boolean,
        language: AppLanguage = AppLanguage.ENGLISH
    ): Int = withContext(Dispatchers.IO) {
        seedCatalogIfNeeded(classId, curriculumVersion, academicGroup)
        val all = dao.getAllDownloadPacks()
        val targets = all.filter {
            it.status != "installed" &&
                (it.classId == classId || it.classId == "all")
        }
        var installedCount = 0
        targets.forEach { pack ->
            if (downloadOrInstallPack(pack, isOnline, language).isSuccess) {
                installedCount++
            }
        }
        installedCount
    }

    suspend fun removeDownloadedPack(packId: String) = withContext(Dispatchers.IO) {
        val existing = dao.getDownloadPack(packId) ?: return@withContext
        existing.localPath?.let { path ->
            runCatching { File(path).delete() }
        }
        if (existing.category == DownloadPackCategory.TEXTBOOK) {
            textbookRepo.deleteOfflineBookCache(existing.assetFileName)
        }
        dao.updateDownloadPackState(
            packId = packId,
            status = "not_downloaded",
            progressPct = 0,
            localPath = null,
            installedAtMs = 0L
        )
    }

    suspend fun installedLocalBytes(): Long = withContext(Dispatchers.IO) {
        val packBytes = packsDir().walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val bookBytes = textbookRepo.pdfFileForBook("").parentFile
            ?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L
        packBytes + bookBytes
    }

    private fun buildDefaultDownloadCatalog(
        classId: String,
        curriculumVersion: String,
        academicGroup: String
    ): List<DownloadPackEntity> {
        val items = mutableListOf<DownloadPackEntity>()

        // 1. NCTB Textbooks across the student's class (plus key SSC books)
        val books: List<TextbookCatalogItem> = TextbookCatalog.booksFor(
            academicYear = "2026",
            classId = classId,
            curriculumVersion = curriculumVersion,
            academicGroup = academicGroup
        ).ifEmpty {
            TextbookCatalog.books.filter { it.academicYear == "2026" && it.classId == "class_10" && it.curriculumVersion == "bangla" }
        }
        books.take(10).forEach { book ->
            items.add(
                DownloadPackEntity(
                    packId = "book_${book.id}",
                    category = DownloadPackCategory.TEXTBOOK,
                    classId = book.classId,
                    subjectId = book.subjectId,
                    titleEn = book.titleEn,
                    titleBn = book.titleBn,
                    subtitleEn = "${book.bookCode} • ${book.estimatedPages} pages • Illustrated PDF",
                    subtitleBn = "${book.bookCode} • ${book.estimatedPages} পৃষ্ঠা • অফলাইন PDF",
                    sizeMb = book.estimatedSizeMb,
                    assetFileName = book.id,
                    remoteUrl = book.pdfUrl,
                    status = "not_downloaded"
                )
            )
        }

        // 2. Studio Subject Audio Briefings (44.1 kHz)
        val audioSubjects = listOf(
            "math" to ("Mathematics Audio Revision" to "গণিত অডিও ব্রিফিং"),
            "physics" to ("Physics Laws Audio Guide" to "পদার্থবিজ্ঞান অডিও গাইড"),
            "chemistry" to ("Chemistry Reactions Audio" to "রসায়ন অডিও ব্রিফিং"),
            "biology" to ("Biology Concepts Audio" to "জীববিজ্ঞান অডিও গাইড"),
            "ict" to ("ICT & Logic Audio Guide" to "আইসিটি অডিও গাইড"),
            "english" to ("English Grammar Audio" to "ইংরেজি গ্রামার অডিও"),
            "bangla" to ("Bangla Sahitya Audio" to "বাংলা সাহিত্য অডিও")
        )
        audioSubjects.forEach { (subjId, titles) ->
            val file = "briefing_${subjId}.wav"
            items.add(
                DownloadPackEntity(
                    packId = "audio_brief_$subjId",
                    category = DownloadPackCategory.AUDIO_BRIEFING,
                    classId = "all",
                    subjectId = subjId,
                    titleEn = titles.first,
                    titleBn = titles.second,
                    subtitleEn = "44.1 kHz studio chapter summary audio",
                    subtitleBn = "৪৪.১ kHz স্টুডিও অধ্যায় সারসংক্ষেপ অডিও",
                    sizeMb = 7.4,
                    assetFileName = file,
                    status = "installed",
                    progressPct = 100
                )
            )
        }

        // 3. Focus Studio Soundscapes
        val soundscapes = listOf(
            Triple("track_deep_alpha", "Deep Alpha Concentration", "ডিপ আলফা ফোকাস (৪৪.১ kHz)") to 18.2,
            Triple("track_monsoon_rain", "Dhaka Monsoon Rain Layer", "বর্ষার বৃষ্টি অ্যাম্বিয়েন্স") to 16.5,
            Triple("track_night_library", "Midnight Library Ambience", "নীরব লাইব্রেরি সাউন্ডস্কেপ") to 15.8,
            Triple("track_study_room", "Quiet Exam Study Room", "পরীক্ষা প্রস্তুতি কক্ষ") to 14.9,
            Triple("track_zen_breath", "Calm Breath & Recall", "মনোযোগ ও শ্বাস প্রশ্বাস") to 14.2
        )
        soundscapes.forEach { (meta, size) ->
            val (id, en, bn) = meta
            items.add(
                DownloadPackEntity(
                    packId = "sound_$id",
                    category = DownloadPackCategory.FOCUS_SOUNDSCAPE,
                    classId = "all",
                    subjectId = "general",
                    titleEn = en,
                    titleBn = bn,
                    subtitleEn = "Lossless 44.1 kHz offline study loop",
                    subtitleBn = "লসলেস ৪৪.১ kHz অফলাইন স্টাডি অডিও",
                    sizeMb = size,
                    assetFileName = "$id.wav",
                    status = "installed",
                    progressPct = 100
                )
            )
        }

        // 4. STEM Lab High-DPI Simulation & Diagram Packs
        val labPacks = listOf(
            Triple("lab_trig", "Trigonometry Unit Circle Lab Pack", "ত্রিকোণমিতি একক বৃত্ত প্যাক") to "math",
            Triple("lab_physics", "Projectile Kinematics Lab Pack", "প্রাস ও গতিবিদ্যা ল্যাব প্যাক") to "physics",
            Triple("lab_chem", "Bohr Atomic Shell Lab Pack", "বোর পরমাণু মডেল প্যাক") to "chemistry",
            Triple("lab_bio", "Eukaryotic Cell Atlas Pack", "কোষ অ্যাটলাস হাই-ডিপিআই প্যাক") to "biology"
        )
        labPacks.forEach { (meta, subj) ->
            val (id, en, bn) = meta
            items.add(
                DownloadPackEntity(
                    packId = "pack_$id",
                    category = DownloadPackCategory.STEM_LAB_PACK,
                    classId = "all",
                    subjectId = subj,
                    titleEn = en,
                    titleBn = bn,
                    subtitleEn = "High-DPI technical plate + offline Canvas simulation",
                    subtitleBn = "উচ্চ-রেজোলিউশন ডায়াগ্রাম প্লেট ও অফলাইন সিমুলেশন",
                    sizeMb = 8.4,
                    assetFileName = "$id.png",
                    status = "installed",
                    progressPct = 100
                )
            )
        }

        return items
    }

    companion object {
        private const val TAG = "OfflineDownloadsRepo"
    }
}
