package com.example.data.repository

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.core.AppLanguage
import com.example.core.OfflineVaultManager
import com.example.data.curriculum.BangladeshCurriculumCatalog
import com.example.data.curriculum.TextbookCatalog
import com.example.data.local.BookChapterEntity
import com.example.data.local.BookPageNoteEntity
import com.example.data.local.CachedBookEntity
import com.example.data.local.CachedLessonEntity
import com.example.data.local.CachedQuestionEntity
import com.example.data.local.StudyQuestDao
import com.example.data.model.TextbookCatalogDoc
import com.example.data.model.TextbookCatalogItem
import com.example.data.model.TextbookChapterDoc
import com.example.data.model.TextbookChapterSpec
import com.example.data.model.TextbookDownloadStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class TextbookDownloadRepository(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val dao: StudyQuestDao
) {
    private val appContext = context.applicationContext
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun textbooksDir(): File =
        File(appContext.filesDir, "textbooks").apply { if (!exists()) mkdirs() }

    fun pdfFileForBook(bookId: String): File {
        val safeId = bookId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return File(textbooksDir(), "$safeId.pdf")
    }

    fun observeCachedBooks(): Flow<List<CachedBookEntity>> = dao.observeCachedBooks()

    fun observeCachedBook(bookId: String): Flow<CachedBookEntity?> = dao.observeCachedBook(bookId)

    fun observeBookChapters(bookId: String): Flow<List<BookChapterEntity>> = dao.observeBookChapters(bookId)

    fun observeBookNotes(bookId: String): Flow<List<BookPageNoteEntity>> = dao.observeBookNotes(bookId)

    fun observeCloudTextbookCatalog(): Flow<List<TextbookCatalogItem>> =
        db.collection("textbook_catalog")
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    TextbookCatalogDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).map { doc ->
                    TextbookCatalogItem(
                        id = doc.bookId.ifBlank { doc.id },
                        academicYear = doc.academicYear,
                        classId = doc.classId,
                        curriculumVersion = doc.curriculumVersion,
                        groupId = doc.groupId,
                        subjectId = doc.subjectId,
                        titleEn = doc.titleEn,
                        titleBn = doc.titleBn,
                        bookCode = doc.bookCode,
                        editionLabelEn = doc.editionLabelEn,
                        editionLabelBn = doc.editionLabelBn,
                        officialSourceUrl = doc.officialSourceUrl,
                        pdfUrl = doc.pdfUrl,
                        estimatedPages = doc.estimatedPages.toInt().coerceAtLeast(1),
                        estimatedSizeMb = doc.estimatedSizeMb,
                        is2027EditionReady = doc.academicYear == "2027",
                        publicationStatus = doc.status
                    )
                }
            }
            .catch { emit(emptyList()) }

    fun observeCloudChaptersForBook(bookId: String): Flow<List<TextbookChapterSpec>> =
        db.collection("textbook_chapters")
            .whereEqualTo("bookId", bookId)
            .whereEqualTo("status", "published")
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    TextbookChapterDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).map { doc ->
                    TextbookChapterSpec(
                        id = doc.id,
                        bookId = doc.bookId,
                        orderIndex = doc.orderIndex.toInt(),
                        titleEn = doc.titleEn,
                        titleBn = doc.titleBn,
                        summaryEn = doc.summaryEn,
                        summaryBn = doc.summaryBn,
                        startPage = doc.startPage.toInt(),
                        endPage = doc.endPage.toInt(),
                        keyTopicsEn = doc.keyTopicsEn,
                        keyTopicsBn = doc.keyTopicsBn,
                        formulas = doc.formulas,
                        studyNotesEn = doc.studyNotesEn,
                        studyNotesBn = doc.studyNotesBn
                    )
                }.sortedBy { it.orderIndex }
            }
            .catch { emit(emptyList()) }

    /**
     * Automatically installs the active locked book's chapters, lessons, exercises, and PDF study pack
     * inside app-private storage so the student can read, search, bookmark, and practice offline.
     */
    suspend fun ensureBookInstalledOffline(
        book: TextbookCatalogItem,
        forceRedownloadPdf: Boolean = false,
        language: AppLanguage = AppLanguage.ENGLISH
    ): Result<CachedBookEntity> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getCachedBook(book.id)
            val chapters = TextbookCatalog.chaptersForBook(book)
            val chapterEntities = chapters.map { ch ->
                val prev = dao.getBookChapters(book.id).firstOrNull { it.id == ch.id }
                BookChapterEntity(
                    id = ch.id,
                    bookId = book.id,
                    orderIndex = ch.orderIndex,
                    titleEn = ch.titleEn,
                    titleBn = ch.titleBn,
                    summaryEn = ch.summaryEn,
                    summaryBn = ch.summaryBn,
                    startPage = ch.startPage,
                    endPage = ch.endPage,
                    keyTopicsCsv = (ch.keyTopicsEn + ch.keyTopicsBn).joinToString("||"),
                    formulasCsv = ch.formulas.joinToString("||"),
                    studyNotesEn = ch.studyNotesEn,
                    studyNotesBn = ch.studyNotesBn,
                    completed = prev?.completed == true
                )
            }
            dao.upsertBookChapters(chapterEntities)

            // Cache curriculum lessons & questions tagged to this active book
            val lessons = BangladeshCurriculumCatalog.lessons.filter {
                it.subjectId == book.subjectId &&
                    (it.classId == book.classId || (book.classId == "class_9" && it.classId == "class_10"))
            }
            if (lessons.isNotEmpty()) {
                dao.upsertLessons(
                    lessons.map { l ->
                        val prevLesson = dao.getCachedLesson(l.id)
                        CachedLessonEntity(
                            id = l.id,
                            classId = l.classId,
                            subjectId = l.subjectId,
                            chapterId = l.chapterId,
                            bookId = book.id,
                            titleEn = l.titleEn,
                            titleBn = l.titleBn,
                            explanationEn = l.explanationEn,
                            explanationBn = l.explanationBn,
                            formulasCsv = l.formulas.joinToString("||"),
                            keyPointsEnCsv = l.keyPointsEn.joinToString("||"),
                            keyPointsBnCsv = l.keyPointsBn.joinToString("||"),
                            completed = prevLesson?.completed == true
                        )
                    }
                )
            }

            val bookQuestions = TextbookCatalog.questionsForActiveBook(
                BangladeshCurriculumCatalog.questions,
                book
            )
            if (bookQuestions.isNotEmpty()) {
                dao.upsertQuestions(
                    bookQuestions.map { q ->
                        CachedQuestionEntity(
                            id = q.id,
                            classId = q.classId,
                            subjectId = q.subjectId,
                            chapterId = q.chapterId,
                            topicId = q.topicId,
                            bookId = book.id,
                            questionType = q.type.id,
                            difficulty = q.difficulty.id,
                            promptEn = q.promptEn,
                            promptBn = q.promptBn,
                            optionsEnPipe = q.optionsEn.joinToString("||"),
                            optionsBnPipe = q.optionsBn.joinToString("||"),
                            correctIndicesCsv = q.correctOptionIndices.joinToString(","),
                            explanationEn = q.explanationEn,
                            explanationBn = q.explanationBn,
                            marks = q.marks,
                            provenance = q.provenance.id,
                            sourceAttribution = q.sourceAttribution
                        )
                    }
                )
            }

            val destFile = pdfFileForBook(book.id)
            dao.upsertCachedBook(
                CachedBookEntity(
                    bookId = book.id,
                    academicYear = book.academicYear,
                    classId = book.classId,
                    curriculumVersion = book.curriculumVersion,
                    groupId = book.groupId,
                    subjectId = book.subjectId,
                    titleEn = book.titleEn,
                    titleBn = book.titleBn,
                    editionLabelEn = book.editionLabelEn,
                    editionLabelBn = book.editionLabelBn,
                    officialSourceUrl = book.officialSourceUrl,
                    pdfUrl = book.pdfUrl,
                    localPdfPath = existing?.localPdfPath,
                    downloadStatus = TextbookDownloadStatus.DOWNLOADING.id,
                    downloadProgress = 15,
                    fileSizeBytes = existing?.fileSizeBytes ?: 0L,
                    totalPages = existing?.totalPages ?: 0,
                    lastReadPage = existing?.lastReadPage ?: 0,
                    installedOfflineAtMs = existing?.installedOfflineAtMs ?: 0L,
                    isCustomImported = existing?.isCustomImported == true
                )
            )

            // Download remote official PDF when url exists; otherwise install bundled Study Vault illustrated PDF or generate fallback
            val pdfReadyFile: File = when {
                !book.pdfUrl.isNullOrBlank() && (forceRedownloadPdf || !destFile.exists() || existing?.isCustomImported == false) -> {
                    val downloaded = downloadRemotePdf(book.id, book.pdfUrl, destFile)
                    if (downloaded) destFile else {
                        if (!destFile.exists()) {
                            val copied = OfflineVaultManager.copyBundledSubjectPdf(appContext, book.subjectId, destFile)
                            if (!copied) generateCompanionStudyPdf(book, chapters, destFile, language)
                        }
                        destFile
                    }
                }
                destFile.exists() && destFile.length() > 256L && !forceRedownloadPdf -> destFile
                else -> {
                    val copied = OfflineVaultManager.copyBundledSubjectPdf(appContext, book.subjectId, destFile)
                    if (!copied) {
                        generateCompanionStudyPdf(book, chapters, destFile, language)
                    }
                    destFile
                }
            }

            val pageCount = countPdfPages(pdfReadyFile)
            val entity = CachedBookEntity(
                bookId = book.id,
                academicYear = book.academicYear,
                classId = book.classId,
                curriculumVersion = book.curriculumVersion,
                groupId = book.groupId,
                subjectId = book.subjectId,
                titleEn = book.titleEn,
                titleBn = book.titleBn,
                editionLabelEn = book.editionLabelEn,
                editionLabelBn = book.editionLabelBn,
                officialSourceUrl = book.officialSourceUrl,
                pdfUrl = book.pdfUrl,
                localPdfPath = pdfReadyFile.absolutePath,
                downloadStatus = TextbookDownloadStatus.INSTALLED.id,
                downloadProgress = 100,
                fileSizeBytes = pdfReadyFile.length(),
                totalPages = pageCount.coerceAtLeast(1),
                lastReadPage = (existing?.lastReadPage ?: 0).coerceIn(0, (pageCount - 1).coerceAtLeast(0)),
                installedOfflineAtMs = System.currentTimeMillis(),
                isCustomImported = existing?.isCustomImported == true
            )
            dao.upsertCachedBook(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Offline book install failed for ${book.id}", e)
            dao.updateBookDownloadProgress(book.id, TextbookDownloadStatus.FAILED.id, 0)
            Result.failure(e)
        }
    }

    /**
     * Imports a user/admin-picked PDF via Android SAF (zero broad storage permissions) directly into
     * app-private textbook storage for the given bookId.
     */
    suspend fun importLocalPdfForBook(
        book: TextbookCatalogItem,
        sourceUri: Uri
    ): Result<CachedBookEntity> = withContext(Dispatchers.IO) {
        try {
            val destFile = pdfFileForBook(book.id)
            appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Unable to open selected PDF."))

            val pageCount = countPdfPages(destFile)
            if (pageCount <= 0) {
                destFile.delete()
                return@withContext Result.failure(IllegalArgumentException("Selected file is not a valid readable PDF."))
            }

            val existing = dao.getCachedBook(book.id)
            val entity = CachedBookEntity(
                bookId = book.id,
                academicYear = book.academicYear,
                classId = book.classId,
                curriculumVersion = book.curriculumVersion,
                groupId = book.groupId,
                subjectId = book.subjectId,
                titleEn = book.titleEn,
                titleBn = book.titleBn,
                editionLabelEn = "${book.editionLabelEn} • Imported PDF",
                editionLabelBn = "${book.editionLabelBn} • সংযুক্ত PDF",
                officialSourceUrl = book.officialSourceUrl,
                pdfUrl = book.pdfUrl,
                localPdfPath = destFile.absolutePath,
                downloadStatus = TextbookDownloadStatus.INSTALLED.id,
                downloadProgress = 100,
                fileSizeBytes = destFile.length(),
                totalPages = pageCount,
                lastReadPage = 0,
                installedOfflineAtMs = System.currentTimeMillis(),
                isCustomImported = true
            )
            dao.upsertCachedBook(entity)
            if (existing == null) {
                ensureBookInstalledOffline(book, forceRedownloadPdf = false)
            }
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveLastReadPage(bookId: String, pageIndex: Int) {
        dao.updateBookLastReadPage(bookId, pageIndex.coerceAtLeast(0))
    }

    suspend fun markChapterCompleted(chapterEntityId: String) {
        dao.markBookChapterCompleted(chapterEntityId)
    }

    suspend fun savePageNote(
        bookId: String,
        pageNumber: Int,
        chapterId: String?,
        title: String,
        noteText: String,
        isBookmark: Boolean
    ) {
        val cleanId = if (isBookmark && noteText.isBlank()) {
            "bm_${bookId}_p$pageNumber"
        } else {
            "note_${bookId}_p${pageNumber}_${UUID.randomUUID().toString().take(6)}"
        }
        dao.upsertBookPageNote(
            BookPageNoteEntity(
                id = cleanId,
                bookId = bookId,
                pageNumber = pageNumber,
                chapterId = chapterId,
                title = title.trim().ifBlank { "Page ${pageNumber + 1}" },
                noteText = noteText.trim(),
                isBookmark = isBookmark
            )
        )
    }

    suspend fun deletePageNote(noteId: String) {
        dao.deleteBookPageNote(noteId)
    }

    suspend fun deleteOfflineBookCache(bookId: String) = withContext(Dispatchers.IO) {
        runCatching { pdfFileForBook(bookId).delete() }
        dao.deleteCachedBook(bookId)
    }

    private suspend fun downloadRemotePdf(bookId: String, url: String, destFile: File): Boolean {
        return try {
            val req = Request.Builder().url(url).build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return false
                val body = resp.body ?: return false
                val total = body.contentLength()
                var copied = 0L
                val tmp = File(destFile.parentFile, "${destFile.name}.part")
                body.byteStream().use { input ->
                    FileOutputStream(tmp).use { output ->
                        val buf = ByteArray(16 * 1024)
                        var read: Int
                        var lastPct = 15
                        while (input.read(buf).also { read = it } != -1) {
                            output.write(buf, 0, read)
                            copied += read
                            if (total > 0L) {
                                val pct = ((copied * 80L) / total + 15).toInt().coerceIn(15, 95)
                                if (pct - lastPct >= 10) {
                                    lastPct = pct
                                    dao.updateBookDownloadProgress(bookId, TextbookDownloadStatus.DOWNLOADING.id, pct)
                                }
                            }
                        }
                    }
                }
                if (countPdfPages(tmp) > 0) {
                    if (destFile.exists()) destFile.delete()
                    tmp.renameTo(destFile)
                    true
                } else {
                    tmp.delete()
                    false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Remote PDF download failed ($url): ${e.message}")
            false
        }
    }

    private fun countPdfPages(file: File): Int {
        if (!file.exists() || file.length() < 64L) return 0
        return runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer -> renderer.pageCount }
            }
        }.getOrDefault(0)
    }

    /**
     * Compiles a clean, high-resolution, zero-copyright-infringement StudyQuest Offline Study Guide PDF
     * from curated NCTB-aligned chapters, formulas, and key points when no official NCTB PDF has been attached yet.
     */
    private fun generateCompanionStudyPdf(
        book: TextbookCatalogItem,
        chapters: List<TextbookChapterSpec>,
        destFile: File,
        language: AppLanguage
    ) {
        val doc = PdfDocument()
        val pageWidth = 595 // A4 points
        val pageHeight = 842
        val margin = 44f
        val maxLineWidth = pageWidth - margin * 2

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E1B4B")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4F46E5")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#312E81")
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2937")
            textSize = 11.5f
        }
        val formulaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E")
            textSize = 11.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6B7280")
            textSize = 9.5f
        }
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E0E7FF")
            strokeWidth = 1.5f
        }

        // Cover Page
        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
        var c = page.canvas
        c.drawColor(Color.parseColor("#F8FAFF"))
        c.drawText("STUDYQUEST • OFFLINE TEXTBOOK PACK", margin, 72f, badgePaint)
        drawWrappedText(
            c,
            if (language == AppLanguage.BANGLA) book.titleBn else book.titleEn,
            margin,
            110f,
            maxLineWidth,
            26f,
            titlePaint
        )
        c.drawText(
            "Book Code: ${book.bookCode}  |  Year: ${book.academicYear}",
            margin,
            172f,
            bodyPaint
        )
        c.drawText(
            if (language == AppLanguage.BANGLA) book.editionLabelBn else book.editionLabelEn,
            margin,
            192f,
            bodyPaint
        )
        c.drawLine(margin, 212f, pageWidth - margin, 212f, dividerPaint)
        c.drawText("Table of Contents (${chapters.size} Units)", margin, 240f, headerPaint)
        var y = 266f
        chapters.take(18).forEach { ch ->
            val label = "${ch.orderIndex}. ${if (language == AppLanguage.BANGLA) ch.titleBn else ch.titleEn}"
            y = drawWrappedText(c, label, margin, y, maxLineWidth, 16f, bodyPaint) + 4f
        }
        c.drawText("Official NCTB Portal: ${book.officialSourceUrl}", margin, pageHeight - 54f, footerPaint)
        c.drawText("Attach official NCTB PDF in Reader or Admin Studio • Page $pageNum", margin, pageHeight - 36f, footerPaint)
        doc.finishPage(page)

        // Chapter Pages
        chapters.forEach { ch ->
            pageNum += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
            c = page.canvas
            c.drawColor(Color.WHITE)
            c.drawText("UNIT ${ch.orderIndex} • ${book.bookCode}", margin, 56f, badgePaint)
            val chTitle = if (language == AppLanguage.BANGLA) ch.titleBn else ch.titleEn
            y = drawWrappedText(c, chTitle, margin, 82f, maxLineWidth, 21f, headerPaint) + 8f
            c.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
            y += 22f

            val summary = if (language == AppLanguage.BANGLA) ch.summaryBn else ch.summaryEn
            y = drawWrappedText(c, summary, margin, y, maxLineWidth, 16f, bodyPaint) + 12f

            if (ch.formulas.isNotEmpty()) {
                c.drawText("Key Formulas:", margin, y, headerPaint)
                y += 18f
                ch.formulas.forEach { f ->
                    if (y < pageHeight - 100f) {
                        y = drawWrappedText(c, "• $f", margin + 8f, y, maxLineWidth - 8f, 16f, formulaPaint) + 2f
                    }
                }
                y += 8f
            }

            val notes = (if (language == AppLanguage.BANGLA) ch.studyNotesBn else ch.studyNotesEn)
            notes.split("\n\n").forEach { para ->
                if (y < pageHeight - 70f && para.isNotBlank()) {
                    y = drawWrappedText(c, para, margin, y, maxLineWidth, 16f, bodyPaint) + 8f
                }
            }
            c.drawText("${book.titleEn} • Page $pageNum", margin, pageHeight - 32f, footerPaint)
            doc.finishPage(page)
        }

        FileOutputStream(destFile).use { out -> doc.writeTo(out) }
        doc.close()
    }

    private fun drawWrappedText(
        canvas: android.graphics.Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ): Float {
        var y = startY
        val words = text.replace("\n", " ").split(Regex("\\s+")).filter { it.isNotBlank() }
        var currentLine = StringBuilder()
        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = StringBuilder(candidate)
            } else {
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine.toString(), x, y, paint)
                    y += lineHeight
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, y, paint)
            y += lineHeight
        }
        return y
    }

    companion object {
        private const val TAG = "TextbookDownloadRepo"
    }
}
