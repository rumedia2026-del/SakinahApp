package com.sakinah.app.feature.prayer

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.sakinah.app.MainActivity
import com.sakinah.app.R
import com.sakinah.app.SakinahApplication

class AdhanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val muezzinId = intent.getStringExtra(EXTRA_MUEZZIN_ID) ?: "makkah"

        when (action) {
            ACTION_ADHAN -> {
                // 1. تشغيل الأذان في خدمة Foreground فورية
                try {
                    val serviceIntent = Intent(context, AdhanForegroundService::class.java).apply {
                        putExtra(AdhanForegroundService.EXTRA_PRAYER_NAME, prayerName)
                        putExtra(AdhanForegroundService.EXTRA_MUEZZIN_ID, muezzinId)
                    }
                    ContextCompat.startForegroundService(context, serviceIntent)
                } catch (e: Exception) {
                    Log.e("AdhanAlarmReceiver", "Failed to start AdhanForegroundService", e)
                }

                // 2. جدولة الصلوات القادمة لضمان استمرار التنبيهات
                try {
                    AdhanAlarmScheduler.scheduleAllPrayers(context)
                } catch (e: Exception) {
                    Log.e("AdhanAlarmReceiver", "Failed to reschedule prayers", e)
                }
            }

            ACTION_PRE_ADHAN -> {
                // تنبيه اقتراب الصلاة (15 دقيقة قبل الأذان)
                showPreAdhanNotification(context, prayerName)
            }

            ACTION_IQAMA -> {
                // تنبيه وقت إقامة الصلاة
                showIqamaNotification(context, prayerName)
            }
        }
    }

    private fun showPreAdhanNotification(context: Context, prayerName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SakinahApplication.CHANNEL_ID_PRE_ADHAN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("اقتربت صلاة $prayerName ⏳")
            .setContentText("متبقي ١٥ دقيقة على الأذان، استعد وأسبغ الوضوء وأجب داعي الله 🌿")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(3001, notification)
    }

    private fun showIqamaNotification(context: Context, prayerName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SakinahApplication.CHANNEL_ID_PRE_ADHAN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("حان وقت إقامة صلاة $prayerName 🤲")
            .setContentText("أقيموا صفوفكم وتراصّوا، استقبل القبلة بخشوع وسكينة")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(3002, notification)
    }

    companion object {
        const val ACTION_ADHAN = "com.sakinah.app.ACTION_ADHAN"
        const val ACTION_PRE_ADHAN = "com.sakinah.app.ACTION_PRE_ADHAN"
        const val ACTION_IQAMA = "com.sakinah.app.ACTION_IQAMA"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"
    }
}
