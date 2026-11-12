package com.sakinah.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import com.sakinah.app.core.notifications.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH APPLICATION
//  نقطة الدخول الرئيسية — Hilt + WorkManager + قنوات الإشعارات والجدولة
// ═══════════════════════════════════════════════════════════════════

@HiltAndroidApp
class SakinahApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        // جدولة التنبيهات اليومية الذكية
        NotificationScheduler.scheduleDailyReminders(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val azkarChannel = NotificationChannel(
                CHANNEL_ID_AZKAR,
                getString(R.string.notif_channel_azkar),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = getString(R.string.notif_channel_azkar_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 100, 150)
                setShowBadge(true)
            }

            val floatingChannel = NotificationChannel(
                CHANNEL_ID_FLOATING_ZIKR,
                "الذكر العائم",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "تذكير خفيف وغير مزعج"
                enableVibration(false)
                setShowBadge(false)
            }

            manager.createNotificationChannels(listOf(azkarChannel, floatingChannel))
        }
    }

    companion object {
        const val CHANNEL_ID_AZKAR         = "sakinah_azkar_reminders"
        const val CHANNEL_ID_FLOATING_ZIKR = "sakinah_floating_zikr"
    }
}
