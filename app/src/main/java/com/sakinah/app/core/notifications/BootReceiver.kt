package com.sakinah.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

// ═══════════════════════════════════════════════════════════════════
//  BOOT & TIMEZONE RECEIVER
//  إعادة جدولة التنبيهات تلقائياً عند إعادة تشغيل الهاتف أو تحديث التطبيق
//  أو تغيير المنطقة الزمنية أو ضبط الوقت يدوياً
// ═══════════════════════════════════════════════════════════════════

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> {
                Log.i(TAG, "Triggered by $action — rescheduling Sakinah reminders and prayer alarms")
                NotificationScheduler.scheduleDailyReminders(context)
                com.sakinah.app.feature.prayer.AdhanAlarmScheduler.scheduleAllPrayers(context)
            }
        }
    }

    companion object {
        private const val TAG = "SakinahBootReceiver"
    }
}
