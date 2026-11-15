package com.example.core

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
import com.example.R
import com.example.data.curriculum.GamificationCatalog
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import java.util.Calendar

object NotificationCategory {
    const val STUDY = "study"
    const val XP = "xp"
    const val BADGE = "badge"
    const val ASSIGNMENT = "assignment"
    const val COMPETITION = "competition"
    const val EXAM = "exam"
    const val COMMUNITY = "community"
    const val SYSTEM = "system"
}

class NotificationDispatcher(
    private val db: FirebaseFirestore,
    private val preferences: StudyQuestPreferences,
    private val appContext: Context? = null
) {
    fun isCategoryEnabled(prefs: LocalUserPreferences, category: String): Boolean {
        if (!prefs.notificationsEnabled) return false
        return when (category) {
            NotificationCategory.STUDY -> prefs.notifStudyEnabled
            NotificationCategory.XP -> prefs.notifXpEnabled
            NotificationCategory.BADGE -> prefs.notifBadgeEnabled
            NotificationCategory.ASSIGNMENT -> prefs.notifAssignmentEnabled
            NotificationCategory.COMPETITION -> prefs.notifCompetitionEnabled
            NotificationCategory.EXAM -> prefs.notifExamEnabled
            NotificationCategory.COMMUNITY -> prefs.notifCommunityEnabled
            else -> true
        }
    }

    fun isQuietHoursNow(prefs: LocalUserPreferences, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (!prefs.quietHoursEnabled) return false
        val cal = Calendar.getInstance(GamificationCatalog.dhakaTimeZone).apply { timeInMillis = nowMs }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 6
    }

    suspend fun dispatch(
        userId: String,
        category: String,
        title: String,
        body: String,
        actionRoute: String? = null,
        idempotencyKey: String? = null,
        pushSystemBanner: Boolean = false
    ) {
        if (userId.isBlank() || userId == "guest_local") return
        val prefs = runCatching { preferences.snapshot() }.getOrNull() ?: LocalUserPreferences()
        if (!isCategoryEnabled(prefs, category)) return

        val docId = (idempotencyKey ?: "notif_${category}_${System.currentTimeMillis()}")
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(120)

        try {
            db.collection("users").document(userId)
                .collection("notifications").document(docId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "category" to category,
                        "title" to title.take(118),
                        "body" to body.take(395),
                        "read" to false,
                        "actionRoute" to actionRoute?.take(118),
                        "createdAt" to FieldValue.serverTimestamp()
                    ).filterValues { it != null }
                ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "In-app notification dispatch skipped: ${e.message}")
        }

        if (pushSystemBanner && !isQuietHoursNow(prefs)) {
            postLocalAndroidNotification(category, title, body, docId.hashCode())
        }
    }

    private fun postLocalAndroidNotification(
        category: String,
        title: String,
        body: String,
        notificationId: Int
    ) {
        val ctx = appContext ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        runCatching {
            val channelId = "studyquest_$category"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (mgr.getNotificationChannel(channelId) == null) {
                    val channel = NotificationChannel(
                        channelId,
                        "StudyQuest ${category.replaceFirstChar { it.uppercase() }}",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    mgr.createNotificationChannel(channel)
                }
            }
            val builder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            NotificationManagerCompat.from(ctx).notify(notificationId, builder.build())
        }
    }

    companion object {
        private const val TAG = "NotifDispatcher"
    }
}
