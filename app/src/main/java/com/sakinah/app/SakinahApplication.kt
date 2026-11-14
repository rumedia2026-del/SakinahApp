package com.sakinah.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Configuration
import com.sakinah.app.core.notifications.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
        // جدولة الأذان والصلوات بدقة AlarmManager بأمان بعد التهيئة بشكل غير متزامن
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                com.sakinah.app.feature.prayer.AdhanAlarmScheduler.scheduleAllPrayers(this@SakinahApplication)
            } catch (e: Exception) {
                Log.e("SakinahApplication", "Failed to schedule prayers safely on startup", e)
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // حذف القناة القديمة لضمان تطبيق الإعدادات العاجلة الجديدة
            manager.deleteNotificationChannel("sakinah_adhan_channel")

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

            // القناة العاجلة الجديدة للأذان بدون صوت النظام لأن الصوت يشغل عبر الخدمة
            val adhanChannel = NotificationChannel(
                CHANNEL_ID_ADHAN_URGENT,
                "أذان الصلاة العاجل",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيه الأذان ودخول وقت الفريضة بدقة فوق شاشة القفل"
                setSound(null, null)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                // تجاوز وضع عدم الإزعاج
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (notificationManager?.isNotificationPolicyAccessGranted == true) {
                    setBypassDnd(true)
                }
            }

            val preAdhanChannel = NotificationChannel(
                CHANNEL_ID_PRE_ADHAN,
                "تنبيه اقتراب الصلاة والإقامة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تذكير بالاستعداد والوضوء قبل الأذان ووقت الإقامة"
                enableVibration(true)
                setShowBadge(true)
            }

            val socialChannel = NotificationChannel(
                CHANNEL_ID_SOCIAL,
                "منبّه السوشيال ميديا الفكاهي",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تذكير لطيف بذكر الله عند قضاء وقت طويل في التصفح"
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannels(
                listOf(azkarChannel, floatingChannel, adhanChannel, preAdhanChannel, socialChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_ID_AZKAR         = "sakinah_azkar_reminders"
        const val CHANNEL_ID_FLOATING_ZIKR = "sakinah_floating_zikr"
        const val CHANNEL_ID_ADHAN_URGENT  = "sakinah_adhan_urgent_v4"
        const val CHANNEL_ID_PRE_ADHAN     = "sakinah_pre_adhan_channel"
        const val CHANNEL_ID_SOCIAL        = "sakinah_social_reminder_channel"
    }
}
