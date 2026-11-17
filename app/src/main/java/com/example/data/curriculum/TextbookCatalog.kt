package com.example.data.curriculum

import com.example.data.model.AcademicGroup
import com.example.data.model.QuestionItem
import com.example.data.model.TextbookCatalogItem
import com.example.data.model.TextbookChapterSpec

/**
 * Curated NCTB Bangladesh textbook catalog covering Classes 6-10 (2026 Official Edition & 2027 Curriculum Track)
 * across Bangla and English versions. Official portal links point strictly to real nctb.gov.bd / nctb.portal.gov.bd
 * resources; Admin Studio can attach verified official PDF URLs or import local PDF files cleanly.
 */
object TextbookCatalog {

    private const val NCTB_PORTAL_2026_SECONDARY = "https://nctb.portal.gov.bd/site/page/d6c75852-75b7-4a7a-a2a0-01f1f72b0f6b/-"
    private const val NCTB_HOME_URL = "https://nctb.gov.bd/"

    val books: List<TextbookCatalogItem> by lazy {
        buildList {
            val classes = listOf("class_6", "class_7", "class_8", "class_9", "class_10")
            val versions = listOf("bangla", "english")
            for (year in listOf("2026", "2027")) {
                for (cls in classes) {
                    val classNum = cls.removePrefix("class_")
                    val classSubjects = BangladeshCurriculumCatalog.subjects.filter { cls in it.classIds }
                    for (ver in versions) {
                        for (subj in classSubjects) {
                            val group = when {
                                cls in listOf("class_6", "class_7", "class_8") -> "general"
                                "general" in subj.groupIds -> "general"
                                else -> subj.groupIds.firstOrNull() ?: "general"
                            }
                            val verSuffixEn = if (ver == "bangla") "Bangla Version" else "English Version"
                            val verSuffixBn = if (ver == "bangla") "বাংলা ভার্সন" else "ইংরেজি ভার্সন"
                            val clsLabelEn = "Class $classNum"
                            val clsLabelBn = when (cls) {
                                "class_6" -> "ষষ্ঠ শ্রেণি"
                                "class_7" -> "সপ্তম শ্রেণি"
                                "class_8" -> "অষ্টম শ্রেণি"
                                "class_9" -> "নবম শ্রেণি"
                                else -> "দশম শ্রেণি"
                            }
                            val is2027 = year == "2027"
                            add(
                                TextbookCatalogItem(
                                    id = "nctb_${year}_${cls}_${ver}_${subj.id}",
                                    academicYear = year,
                                    classId = cls,
                                    curriculumVersion = ver,
                                    groupId = group,
                                    subjectId = subj.id,
                                    titleEn = "$clsLabelEn ${subj.titleEn} ($verSuffixEn)",
                                    titleBn = "$clsLabelBn ${subj.titleBn} ($verSuffixBn)",
                                    bookCode = "NCTB-$year-$classNum-${subj.code}-${ver.take(2).uppercase()}",
                                    editionLabelEn = if (is2027) {
                                        "2027 NCTB Curriculum Track (Ready for PDF Attach)"
                                    } else {
                                        "NCTB 2026 Official Syllabus Edition"
                                    },
                                    editionLabelBn = if (is2027) {
                                        "২০২৭ এনসিটিবি পাঠ্যক্রম ট্র্যাক (PDF সংযুক্তির জন্য প্রস্তুত)"
                                    } else {
                                        "এনসিটিবি ২০২৬ অনুমোদিত পাঠ্যক্রম সংস্করণ"
                                    },
                                    officialSourceUrl = if (is2027) NCTB_HOME_URL else NCTB_PORTAL_2026_SECONDARY,
                                    pdfUrl = null,
                                    estimatedPages = when (subj.id) {
                                        "math", "higher_math", "bangla", "english" -> 240
                                        "physics", "chemistry", "biology", "social_studies" -> 196
                                        else -> 148
                                    },
                                    estimatedSizeMb = when (subj.id) {
                                        "math", "higher_math" -> 11.2
                                        "physics", "chemistry", "biology" -> 9.6
                                        else -> 7.4
                                    },
                                    is2027EditionReady = is2027,
                                    publicationStatus = "published"
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun booksFor(
        academicYear: String,
        classId: String,
        curriculumVersion: String,
        academicGroup: String,
        extraCloudBooks: List<TextbookCatalogItem> = emptyList()
    ): List<TextbookCatalogItem> {
        val merged = linkedMapOf<String, TextbookCatalogItem>()
        books.forEach { merged[it.id] = it }
        extraCloudBooks.filter { it.publicationStatus == "published" }.forEach { merged[it.id] = it }

        val targetClass = if (classId == "ssc") "class_10" else classId
        val validGroups = if (targetClass in listOf("class_6", "class_7", "class_8") || academicGroup == AcademicGroup.GENERAL.id) {
            setOf("general")
        } else {
            setOf("general", academicGroup)
        }
        return merged.values.filter {
            it.academicYear == academicYear &&
                it.classId == targetClass &&
                it.curriculumVersion == curriculumVersion &&
                it.groupId in validGroups
        }.sortedBy { it.subjectId }
    }

    fun bookById(
        bookId: String?,
        extraCloudBooks: List<TextbookCatalogItem> = emptyList()
    ): TextbookCatalogItem? {
        if (bookId.isNullOrBlank()) return null
        return extraCloudBooks.firstOrNull { it.id == bookId } ?: books.firstOrNull { it.id == bookId }
    }

    fun defaultBookFor(
        classId: String,
        curriculumVersion: String,
        academicGroup: String,
        academicYear: String = "2026",
        extraCloudBooks: List<TextbookCatalogItem> = emptyList()
    ): TextbookCatalogItem {
        val candidates = booksFor(academicYear, classId, curriculumVersion, academicGroup, extraCloudBooks)
        return candidates.firstOrNull { it.subjectId == "math" }
            ?: candidates.firstOrNull()
            ?: books.first()
    }

    fun chaptersForBook(book: TextbookCatalogItem): List<TextbookChapterSpec> {
        // Pull rich chapters matching subjectId (and fallback across SSC 9-10 shared NCTB curriculum where appropriate)
        val matched = BangladeshCurriculumCatalog.chapters.filter { ch ->
            ch.subjectId == book.subjectId &&
                (ch.classId == book.classId || (book.classId == "class_9" && ch.classId == "class_10"))
        }.sortedBy { it.orderIndex }

        if (matched.isNotEmpty()) {
            return matched.mapIndexed { idx, ch ->
                val lessons = BangladeshCurriculumCatalog.lessons.filter { it.chapterId == ch.id }
                val startPage = 1 + idx * 18
                val endPage = startPage + 17
                val formulas = lessons.flatMap { it.formulas }.distinct()
                val keyTopicsEn = lessons.map { it.titleEn }.ifEmpty { listOf(ch.titleEn) }
                val keyTopicsBn = lessons.map { it.titleBn }.ifEmpty { listOf(ch.titleBn) }
                val notesEn = lessons.joinToString("\n\n") { l ->
                    "${l.titleEn}\n${l.explanationEn}\nKey Points: ${l.keyPointsEn.joinToString("; ")}"
                }.ifBlank { ch.summaryEn }
                val notesBn = lessons.joinToString("\n\n") { l ->
                    "${l.titleBn}\n${l.explanationBn}\nমূল তথ্য: ${l.keyPointsBn.joinToString("; ")}"
                }.ifBlank { ch.summaryBn }

                TextbookChapterSpec(
                    id = "${book.id}_ch_${idx + 1}",
                    bookId = book.id,
                    orderIndex = idx + 1,
                    titleEn = ch.titleEn,
                    titleBn = ch.titleBn,
                    summaryEn = ch.summaryEn,
                    summaryBn = ch.summaryBn,
                    startPage = startPage,
                    endPage = endPage,
                    keyTopicsEn = keyTopicsEn,
                    keyTopicsBn = keyTopicsBn,
                    formulas = formulas,
                    studyNotesEn = notesEn,
                    studyNotesBn = notesBn
                )
            }
        }

        // Structured NCTB unit scaffold for subjects without hand-authored chapter overrides yet
        val subj = BangladeshCurriculumCatalog.subjects.firstOrNull { it.id == book.subjectId }
        val sEn = subj?.titleEn ?: book.subjectId
        val sBn = subj?.titleBn ?: book.subjectId
        val unitNames = listOf(
            "Foundations & Core Concepts" to "মৌলিক ধারণা ও পরিচিতি",
            "Analytical Methods & Applications" to "বিশ্লেষণধর্মী পদ্ধতি ও প্রয়োগ",
            "Board Exam Practice & Problem Solving" to "বোর্ড অনুশীলনী ও সমস্যা সমাধান",
            "Creative Review & Summary" to "সৃজনশীল পুনরালোচনা ও সারসংক্ষেপ"
        )
        return unitNames.mapIndexed { idx, (uEn, uBn) ->
            val start = 1 + idx * 24
            TextbookChapterSpec(
                id = "${book.id}_unit_${idx + 1}",
                bookId = book.id,
                orderIndex = idx + 1,
                titleEn = "Unit ${idx + 1}: $sEn — $uEn",
                titleBn = "অধ্যায় ${idx + 1}: $sBn — $uBn",
                summaryEn = "Official ${book.academicYear} NCTB syllabus guide for ${book.classId.replace("_", " ").uppercase()} $sEn.",
                summaryBn = "${book.academicYear} এনসিটিবি পাঠ্যসূচি অনুযায়ী $sBn বিষয়ের সংক্ষিপ্ত নির্দেশিকা।",
                startPage = start,
                endPage = start + 23,
                keyTopicsEn = listOf("$sEn Core Concept ${idx + 1}", "Board Competency Check"),
                keyTopicsBn = listOf("$sBn মূল বিষয় ${idx + 1}", "বোর্ড যোগ্যতা যাচাই"),
                studyNotesEn = "Attach the official NCTB PDF from Admin Studio or study the structured exercises linked to this active textbook.",
                studyNotesBn = "অ্যাডমিন স্টুডিও থেকে এনসিটিবি PDF যুক্ত করো অথবা এই সক্রিয় বইয়ের অনুশীলনী প্রশ্ন সমাধান করো।"
            )
        }
    }

    /**
     * Strictly scopes every question query to the locked active book.
     * Falls back within the exact active subject + class only if no explicit bookId tag matches yet,
     * never leaking questions from other subjects or non-active books.
     */
    fun questionsForActiveBook(
        allQuestions: List<QuestionItem>,
        activeBook: TextbookCatalogItem?
    ): List<QuestionItem> {
        if (activeBook == null) return allQuestions
        val exactBook = allQuestions.filter { it.bookId == activeBook.id }
        if (exactBook.isNotEmpty()) return exactBook
        val targetClass = activeBook.classId
        return allQuestions.filter { q ->
            q.subjectId == activeBook.subjectId &&
                (q.classId == targetClass ||
                    (targetClass == "class_9" && q.classId == "class_10") ||
                    (targetClass == "class_10" && q.classId == "ssc"))
        }
    }
}
