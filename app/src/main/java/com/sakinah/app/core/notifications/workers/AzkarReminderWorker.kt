package com.sakinah.app.core.notifications.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sakinah.app.MainActivity
import com.sakinah.app.R
import com.sakinah.app.SakinahApplication
import com.sakinah.app.core.notifications.receivers.NotificationReceiverHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AzkarReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderType = inputData.getString(KEY_REMINDER_TYPE) ?: TYPE_MORNING
        showNotification(reminderType)
        return Result.success()
    }

    private fun showNotification(reminderType: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = when (reminderType) {
            TYPE_MORNING -> "أذكار الصباح 🌅"
            TYPE_EVENING -> "أذكار المساء 🌇"
            TYPE_NIGHT -> "أذكار النوم 🌙"
            TYPE_FRIDAY -> "سُنن ويوم الجمعة 🕌"
            else -> "ذكر الله وسكينته ✨"
        }

        val message = when (reminderType) {
            TYPE_MORNING -> "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ.. ابدأ يومك بنور الذكر والبركة."
            TYPE_EVENING -> "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ.. حصّن نفسك وعائلتك بورد المساء."
            TYPE_NIGHT -> "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي.. اختم يومك بسكينة واطمئنان."
            TYPE_FRIDAY -> "أكثروا من الصلاة على النبي ﷺ واقرأوا سورة الكهف."
            else -> "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ."
        }

        val notifId = when (reminderType) {
            TYPE_MORNING -> 101
            TYPE_EVENING -> 102
            TYPE_NIGHT -> 103
            TYPE_FRIDAY -> 104
            else -> 100
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, notifId, openAppIntent, flags)

        // خيار التسبيح التفاعلي مباشرة من داخل الإشعار
        val tasbihPendingIntent = NotificationReceiverHelper.getTasbihPendingIntent(
            context = context,
            notificationId = notifId,
            currentCount = 0,
            phrase = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ"
        )

        val notification = NotificationCompat.Builder(context, SakinahApplication.CHANNEL_ID_AZKAR)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_launcher_foreground, "تسبيح سريع 📿", tasbihPendingIntent)
            .build()

        manager.notify(notifId, notification)
    }

    companion object {
        const val KEY_REMINDER_TYPE = "key_reminder_type"
        const val TYPE_MORNING = "type_morning"
        const val TYPE_EVENING = "type_evening"
        const val TYPE_NIGHT = "type_night"
        const val TYPE_FRIDAY = "type_friday"
    }
}
