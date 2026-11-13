package com.sakinah.app.feature.social

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sakinah.app.data.local.db.SakinahDatabase
import com.sakinah.app.data.local.db.entities.DailyProgressEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SocialMediaActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SOCIAL_TASBIH_DONE) {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, SocialMediaReminderManager.NOTIFICATION_ID)

            // 1. إغلاق الإشعار فوراً
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)

            // 2. إضافة +10 إلى عداد التسبيح اليومي في قاعدة البيانات
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = SakinahDatabase.getInstance(context, scope)
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    db.progressDao().insertOrUpdateProgress(
                        DailyProgressEntity(
                            date = today,
                            completedAzkarIds = "",
                            totalTasbihCount = 10,
                            completedCategories = ""
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val ACTION_SOCIAL_TASBIH_DONE = "com.sakinah.app.ACTION_SOCIAL_TASBIH_DONE"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
