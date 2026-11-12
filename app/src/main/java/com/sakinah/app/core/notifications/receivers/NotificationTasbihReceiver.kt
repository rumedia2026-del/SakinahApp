package com.sakinah.app.core.notifications.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sakinah.app.R
import com.sakinah.app.SakinahApplication
import com.sakinah.app.data.local.db.SakinahDatabase
import com.sakinah.app.data.local.db.entities.DailyProgressEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationTasbihReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TASBIH_CLICK) {
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)
            val currentCount = intent.getIntExtra(EXTRA_COUNT, 0) + 1
            val phrase = intent.getStringExtra(EXTRA_PHRASE) ?: "سُبْحَانَ اللَّهِ"

            // تحديث الإشعار لحظياً بالعدد الجديد
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val updatedNotification = NotificationCompat.Builder(context, SakinahApplication.CHANNEL_ID_AZKAR)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("تسبيح سريع • $phrase")
                .setContentText("عدد التسبيحات: $currentCount مرة ✨")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    "تسبيح ($currentCount)",
                    NotificationReceiverHelper.getTasbihPendingIntent(context, notificationId, currentCount, phrase)
                )
                .build()

            manager.notify(notificationId, updatedNotification)

            // تسجيل التسبيحة في قاعدة البيانات بالخلفية
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = SakinahDatabase.getInstance(context, scope)
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    db.progressDao().insertOrUpdateProgress(
                        DailyProgressEntity(
                            date = today,
                            completedAzkarIds = "",
                            totalTasbihCount = 1,
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
        const val ACTION_TASBIH_CLICK = "com.sakinah.app.ACTION_TASBIH_CLICK"
        const val EXTRA_NOTIFICATION_ID = "extra_notif_id"
        const val EXTRA_COUNT = "extra_count"
        const val EXTRA_PHRASE = "extra_phrase"
    }
}
