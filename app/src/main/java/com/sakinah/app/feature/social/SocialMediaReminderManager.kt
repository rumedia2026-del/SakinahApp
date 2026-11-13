package com.sakinah.app.feature.social

import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.core.app.NotificationCompat
import com.sakinah.app.MainActivity
import com.sakinah.app.R
import com.sakinah.app.SakinahApplication
import java.util.Calendar
import kotlin.random.Random

object SocialMediaReminderManager {

    private const val PREFS_NAME = "sakinah_social_prefs"
    const val KEY_ENABLED = "social_reminder_enabled"
    const val KEY_THRESHOLD_MINUTES = "social_threshold_minutes"
    private const val KEY_LAST_NOTIFIED_TIME = "social_last_notified_time"

    val TARGET_PACKAGES = setOf(
        "com.facebook.katana",
        "com.facebook.lite",
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.google.android.youtube",
        "com.twitter.android",
        "com.twitter.android.lite"
    )

    val WITTY_MESSAGES = listOf(
        "لسه بتقلب في الريلز؟ طب صلي على النبي الأول واربح حسنات! ﷺ",
        "التيك توك مش هيشفعلك.. سبّح 10 تسبيحات وتعال كمل! 🌿",
        "يا غالي، الخوارزميات سرقت وقتك.. خذ لك دقيقة استغفار تمسح ذنوبك ✨",
        "إنستغرام واخدك من وردك؟ ركعتين في جوف الليل أحسن من مليون ستوري! 🕌",
        "تويتة توديك وتويتة تجيبك.. اذكر الله وخلّي يومك مبارك 🤍",
        "الفيسبوك مش هيطير.. بس وقتك اللي راح محسوب عليك، استغفر ربك الآن 🌸",
        "بقالك كتير باصص في الشاشة.. ارفع راسك وقول: لا إله إلا الله وحده لا شريك له 🤲",
        "مش مكفيك التمرير ده كله؟ رطّب لسانك بذكر الله تنال رضاه وسكينة قلبك 🍃",
        "الشير واللايك حلوين.. بس التسبيحة في ميزانك أثقل من جبل أحد! سبحان الله وبحمده ⚖️",
        "صاحب التطبيق بيكسب مليارات.. وأنت تقدر تكسب قصور في الجنة بـ 'سبحان الله والحمد لله' 💎",
        "استراحة محارب من السوشيال ميديا: صلّ على الحبيب المصطفى ليرتاح قلبك ﷺ",
        "كل مقطع شفته هيفنى.. وذكر الله هو الباقي، قل: لا حول ولا قوة إلا بالله العلي العظيم 🕊️",
        "الريلز سحبك لعالم تاني؟ ارجع لربك وقول: أستغفر الله العظيم وأتوب إليه 💫",
        "يا طيب، العمر ثواني.. ما تضيعش ساعتك على منشورات تافهة، اذكر الله تجده تجاهك 🌿",
        "النت بيفصل بس اتصالك بربنا شغال 24 ساعة بدون انقطاع، سبّح الآن 📶",
        "بدل ما تقلب على الفاضي.. اجعل أصابعك تشهد لك بالخير يوم القيامة وسبّح 10 مرات ✨",
        "التريند بيخلص في يومين.. وحسناتك في الجنة باقية خالدة أبد الآبدين 🏰",
        "وقفة صفا: غمض عينك دقيقة واشكر ربنا على نعمة البصر والعافية، الحمد لله دائماً وأبداً 👁️",
        "فاكر الذكر اللي نسيته؟ ده وقته! سبحان الله، والحمد لله، ولا إله إلا الله، والله أكبر 📿",
        "بطارية الموبايل بتخلص.. وبطارية روحك محتاجة شحن بالصلاة والذكر والقرآن 🔋",
        "سيب الكومنتات والخناقات.. واكسب لك كنز في الجنة: لا حول ولا قوة إلا بالله 💎",
        "الدقيقة اللي بتقضيها هنا، لو قلت فيها سبحان الله وبحمده 100 مرة غُفرت ذنوبك! 🌊",
        "مين أولى بوقتك؟ فلان اللي ما تعرفوش ولا ربك اللي خلقك ورزقك؟ اذكر الله الآن 🕌",
        "تغذية بصرية؟ غذي روحك الأول بآية أو استغفار ينوّر دربك ويملأ صدرك انشراحاً 🌟",
        "نهاية الشوط في السوشيال ميديا: اضغط سبّحت وخلي يومك يشهدلك بخير، سبحان الله العظيم 🌿"
    )

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getThresholdMinutes(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THRESHOLD_MINUTES, 20)
    }

    fun setThresholdMinutes(context: Context, minutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THRESHOLD_MINUTES, minutes).apply()
    }

    fun hasUsageAccessPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun checkAndNotifyIfExceeded(context: Context) {
        if (!isEnabled(context)) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastNotified = prefs.getLong(KEY_LAST_NOTIFIED_TIME, 0L)
        val now = System.currentTimeMillis()

        // منع تكرار الإشعار خلال 15 دقيقة
        if (now - lastNotified < 15 * 60 * 1000L) return

        val thresholdMin = getThresholdMinutes(context)
        val totalSocialUsageMillis = getTodaySocialUsageMillis(context)
        val thresholdMillis = thresholdMin * 60 * 1000L

        if (totalSocialUsageMillis >= thresholdMillis) {
            prefs.edit().putLong(KEY_LAST_NOTIFIED_TIME, now).apply()
            showWittyNotification(context)
        }
    }

    fun getTodaySocialUsageMillis(context: Context): Long {
        if (!hasUsageAccessPermission(context)) return 0L
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return 0L

        var totalTime = 0L
        for (usage in stats) {
            if (TARGET_PACKAGES.contains(usage.packageName)) {
                totalTime += usage.totalTimeInForeground
            }
        }
        return totalTime
    }

    fun showWittyNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val randomMessage = WITTY_MESSAGES[Random.nextInt(WITTY_MESSAGES.size)]

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            401,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // زر التفاعل: "سبّحت ✓" (+10 تسبيحات)
        val actionIntent = Intent(context, SocialMediaActionReceiver::class.java).apply {
            action = SocialMediaActionReceiver.ACTION_SOCIAL_TASBIH_DONE
            putExtra(SocialMediaActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        }
        val actionPendingIntent = PendingIntent.getBroadcast(
            context,
            402,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SakinahApplication.CHANNEL_ID_SOCIAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("استراحة سكينة 🌿 • السوشيال ميديا خدتك!")
            .setContentText(randomMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "سبّحت ✓ (+10)", actionPendingIntent)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    const val NOTIFICATION_ID = 4001
}
