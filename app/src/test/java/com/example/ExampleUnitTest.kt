package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun studyVault_assetsAreCompleteAndPreviewSized() {
        val root = File(System.getProperty("user.dir") ?: ".").let {
            if (it.name == "app") it.parentFile else it
        }
        val vaultDir = File(root, "app/src/main/assets/study_vault")
        assertTrue("Expected study_vault directory to exist", vaultDir.isDirectory)

        val books = File(vaultDir, "books").listFiles()?.filter { it.extension == "pdf" }.orEmpty()
        assertEquals("Expected 10 bundled subject PDFs", 10, books.size)
        books.forEach { pdf ->
            assertTrue("${pdf.name} should be preview-optimized (< 500 KB), got ${pdf.length()}", pdf.length() in 4_000L..500_000L)
            val header = pdf.inputStream().use { input ->
                val buf = ByteArray(5)
                val read = input.read(buf)
                if (read > 0) String(buf, 0, read, Charsets.US_ASCII) else ""
            }
            assertEquals("${pdf.name} must have valid %PDF- header", "%PDF-", header)
        }

        val audioFiles = File(vaultDir, "audio").listFiles()?.filter { it.extension == "wav" }.orEmpty()
        assertTrue("Expected at least 16 bundled WAV audio files, got ${audioFiles.size}", audioFiles.size >= 16)

        val plates = File(vaultDir, "plates").listFiles()?.filter { it.extension == "png" }.orEmpty()
        assertEquals("Expected 14 bundled visual plates", 14, plates.size)

        val atlas = File(vaultDir, "knowledge_atlas.json")
        assertTrue("Expected knowledge_atlas.json to exist", atlas.isFile && atlas.length() > 1_000_000L)

        val totalBytes = vaultDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        val totalMb = totalBytes / (1024.0 * 1024.0)
        assertTrue("Expected total study_vault size > 12 MB (got $totalMb MB)", totalMb > 12.0)
        assertTrue("Expected total study_vault size < 25 MB for fast preview install (got $totalMb MB)", totalMb < 25.0)
    }
}
