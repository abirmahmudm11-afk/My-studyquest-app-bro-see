package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class AiDoubtSolverRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    "You are Titu, the friendly scholarly StudyQuest tutor for Bangladeshi students in Classes 6 to 10 and SSC. " +
                        "Give clear, accurate, step-by-step academic explanations aligned with the Bangladesh NCTB curriculum. " +
                        "If asked in Bangla, explain in natural academic Bangla; if asked in English, explain clearly in English. " +
                        "Encourage understanding rather than shortcuts, and show key formulas or board exam tips."
                )
            }
        )
    }

    suspend fun isAiSolverEnabled(): Boolean {
        val currentUser = runCatching { com.google.firebase.Firebase.let { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser } }.getOrNull()
        if (currentUser == null) return true
        return try {
            val snap = db.collection("system_settings").document("global").get().await()
            if (!snap.exists()) true else snap.getBoolean("aiSolverEnabled") ?: true
        } catch (_: Exception) {
            true
        }
    }

    fun streamDoubtExplanation(
        questionPrompt: String,
        studentQuestion: String,
        classId: String,
        subjectName: String,
        language: AppLanguage
    ): Flow<String> = flow {
        val langDirective = if (language == AppLanguage.BANGLA) {
            "উত্তরটি পরিষ্কার বাংলায় ধাপে ধাপে বুঝিয়ে দাও।"
        } else {
            "Explain step-by-step clearly in English."
        }
        val fullPrompt = buildString {
            appendLine("Class: $classId | Subject: $subjectName")
            if (questionPrompt.isNotBlank()) {
                appendLine("Topic / Question Context: $questionPrompt")
            }
            appendLine("Student Question: $studentQuestion")
            appendLine(langDirective)
        }

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                model.generateContentStream(fullPrompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("AI tutor returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Doubt Solver timed out", e)
            throw IllegalStateException("AI explanation timed out after 60 seconds. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Doubt Solver failed", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "AiDoubtSolverRepo"
    }
}
