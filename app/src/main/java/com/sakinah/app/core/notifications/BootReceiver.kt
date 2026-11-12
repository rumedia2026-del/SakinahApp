package com.sakinah.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

// ═══════════════════════════════════════════════════════════════════
//  BOOT RECEIVER
//  إعادة جدولة التنبيهات تلقائياً عند إعادة تشغيل الهاتف أو تحديث التطبيق
// ═══════════════════════════════════════════════════════════════════

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Log.i(TAG, "Device rebooted or package replaced — rescheduling Sakinah azkar reminders")
            NotificationScheduler.scheduleDailyReminders(context)
        }
    }

    companion object {
        private const val TAG = "SakinahBootReceiver"
    }
}
