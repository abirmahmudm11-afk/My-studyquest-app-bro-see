package com.example.data.local

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.R
import com.example.core.AppLanguage
import com.example.core.OperationType
import com.example.core.StudyQuestPreferences
import com.example.core.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SyncOfflineRecordsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val currentUid = Firebase.auth.currentUser?.uid ?: return Result.success()
        val dao = StudyQuestDatabase.getInstance(applicationContext).studyQuestDao()
        val pending = dao.getUnsyncedActions().filter { it.userId == currentUid }
        if (pending.isEmpty()) return Result.success()

        val dbId = applicationContext.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(dbId)

        var anyFailed = false
        for (action in pending) {
            val docId = action.idempotencyKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
            val recordRef = db.collection("users").document(currentUid)
                .collection("learning_records").document(docId)
            val ledgerRef = db.collection("users").document(currentUid)
                .collection("xp_ledger").document("xp_$docId")
            val profileRef = db.collection("public_profiles").document(currentUid)

            try {
                val existingRecord = recordRef.get().await()
                if (!existingRecord.exists()) {
                    val batch = db.batch()
                    batch.set(
                        recordRef,
                        mapOf(
                            "userId" to currentUid,
                            "recordType" to action.actionType,
                            "refId" to action.refId,
                            "subjectId" to action.subjectId,
                            "chapterId" to action.chapterId,
                            "topicId" to action.topicId,
                            "score" to action.score,
                            "totalMarks" to action.totalMarks,
                            "accuracy" to action.accuracy,
                            "xpAwarded" to action.xpAwarded,
                            "idempotencyKey" to action.idempotencyKey,
                            "summary" to action.summary,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    if (action.xpAwarded > 0) {
                        batch.set(
                            ledgerRef,
                            mapOf(
                                "userId" to currentUid,
                                "sourceType" to action.actionType,
                                "sourceId" to action.refId,
                                "xpDelta" to action.xpAwarded,
                                "coinDelta" to (action.xpAwarded / 3).coerceAtLeast(1),
                                "idempotencyKey" to action.idempotencyKey,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        )
                        batch.update(
                            profileRef,
                            mapOf(
                                "totalXp" to FieldValue.increment(action.xpAwarded.toLong()),
                                "coins" to FieldValue.increment(((action.xpAwarded / 3).coerceAtLeast(1)).toLong()),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    batch.commit().await()
                }
                dao.markSyncActionCompleted(action.idempotencyKey)
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, recordRef.path)
                Log.w("SyncWorker", "Sync failed for ${action.idempotencyKey}", e)
                anyFailed = true
            }
        }
        return if (anyFailed) Result.retry() else Result.success()
    }
}

class StudyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = StudyQuestPreferences(applicationContext).preferencesFlow.first()
        if (!prefs.notificationsEnabled) return Result.success()

        // Respect quiet hours (10 PM - 7 AM Asia/Dhaka or local clock)
        if (prefs.quietHoursEnabled) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hour >= 22 || hour < 7) return Result.success()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return Result.success()
        }

        val channelId = "studyquest_reminders"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "StudyQuest Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val dao = StudyQuestDatabase.getInstance(applicationContext).studyQuestDao()
        val revisions = dao.observeRevisions().first()
        val dueCount = revisions.count { it.nextDueAtMs <= System.currentTimeMillis() }
        if (dueCount == 0) return Result.success()

        val title = if (prefs.uiLanguage == AppLanguage.BANGLA) "StudyQuest রিভিশন রিমাইন্ডার" else "StudyQuest Revision Reminder"
        val body = if (prefs.uiLanguage == AppLanguage.BANGLA)
            "আপনার ${dueCount}টি প্রশ্ন রিভিশনের জন্য প্রস্তুত। টিটু অপেক্ষা করছে!"
        else
            "You have $dueCount revision items due today. Titu is ready to help!"

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(1042, notification)
        return Result.success()
    }
}

object StudyQuestWorkScheduler {
    fun enqueueImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SyncOfflineRecordsWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            "studyquest_sync_now",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun schedulePeriodicReminders(context: Context) {
        val request = PeriodicWorkRequestBuilder<StudyReminderWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            "studyquest_study_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
