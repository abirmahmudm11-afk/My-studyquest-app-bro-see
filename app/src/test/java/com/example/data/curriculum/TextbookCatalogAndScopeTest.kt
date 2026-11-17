package com.example.data.curriculum

import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionProvenance
import com.example.data.model.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextbookCatalogAndScopeTest {

    @Test
    fun `curated catalog covers 2026 and 2027 across classes 6 to 10 and both versions with nctb links`() {
        val books = TextbookCatalog.books
        assertTrue(books.isNotEmpty())
        val years = books.map { it.academicYear }.toSet()
        assertEquals(setOf("2026", "2027"), years)

        val classes = books.map { it.classId }.toSet()
        assertEquals(setOf("class_6", "class_7", "class_8", "class_9", "class_10"), classes)

        val versions = books.map { it.curriculumVersion }.toSet()
        assertEquals(setOf("bangla", "english"), versions)

        assertTrue(
            books.all {
                it.officialSourceUrl.startsWith("https://nctb.") && it.publicationStatus == "published"
            }
        )
    }

    @Test
    fun `booksFor filters strictly by academicYear classId curriculumVersion and academicGroup`() {
        val class10ScienceEn2027 = TextbookCatalog.booksFor(
            academicYear = "2027",
            classId = "class_10",
            curriculumVersion = "english",
            academicGroup = "science"
        )
        assertTrue(class10ScienceEn2027.isNotEmpty())
        assertTrue(class10ScienceEn2027.all {
            it.academicYear == "2027" &&
                it.classId == "class_10" &&
                it.curriculumVersion == "english" &&
                (it.groupId == "general" || it.groupId == "science")
        })
        assertTrue(class10ScienceEn2027.any { it.subjectId == "physics" })
        assertTrue(class10ScienceEn2027.none { it.subjectId == "accounting" })
    }

    @Test
    fun `questionsForActiveBook enforces strict active book lock across subject and class`() {
        val activeMathBook = TextbookCatalog.bookById("nctb_2026_class_10_bangla_math")!!
        val mixedPool = listOf(
            QuestionItem(
                id = "q_math_10",
                classId = "class_10",
                subjectId = "math",
                chapterId = "ch_math_1",
                topicId = "top_1",
                type = QuestionType.SINGLE_MCQ,
                difficulty = QuestionDifficulty.EASY,
                promptEn = "Math 10",
                promptBn = "গণিত ১০",
                optionsEn = listOf("1", "2"),
                optionsBn = listOf("১", "২"),
                correctOptionIndices = listOf(0),
                explanationEn = "Ok",
                explanationBn = "ঠিক",
                provenance = QuestionProvenance.STUDYQUEST_ORIGINAL
            ),
            QuestionItem(
                id = "q_phy_10",
                classId = "class_10",
                subjectId = "physics",
                chapterId = "ch_phy_1",
                topicId = "top_2",
                type = QuestionType.SINGLE_MCQ,
                difficulty = QuestionDifficulty.EASY,
                promptEn = "Physics 10",
                promptBn = "পদার্থ ১০",
                optionsEn = listOf("A", "B"),
                optionsBn = listOf("ক", "খ"),
                correctOptionIndices = listOf(0),
                explanationEn = "Ok",
                explanationBn = "ঠিক",
                provenance = QuestionProvenance.STUDYQUEST_ORIGINAL
            ),
            QuestionItem(
                id = "q_math_6",
                classId = "class_6",
                subjectId = "math",
                chapterId = "ch_math_6",
                topicId = "top_3",
                type = QuestionType.SINGLE_MCQ,
                difficulty = QuestionDifficulty.EASY,
                promptEn = "Math 6",
                promptBn = "গণিত ৬",
                optionsEn = listOf("3", "4"),
                optionsBn = listOf("৩", "৪"),
                correctOptionIndices = listOf(0),
                explanationEn = "Ok",
                explanationBn = "ঠিক",
                provenance = QuestionProvenance.STUDYQUEST_ORIGINAL
            )
        )

        val scoped = TextbookCatalog.questionsForActiveBook(mixedPool, activeMathBook)
        assertEquals(listOf("q_math_10"), scoped.map { it.id })
    }
}
