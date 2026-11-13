package com.sakinah.app.feature.prayer

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

object MosqueSilentManager {

    /**
     * التحقق مما إذا كان التطبيق يملك إذن سياسة عدم الإزعاج (Do Not Disturb Access)
     */
    fun hasDndAccess(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.isNotificationPolicyAccessGranted == true
        } else {
            true
        }
    }

    /**
     * فتح صفحة إعدادات النظام لمنح إذن الوصول إلى سياسة عدم الإزعاج
     */
    fun openDndSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * تفعيل وضع الصامت (احترام المسجد)
     */
    fun enableMosqueSilence(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasDndAccess(context)) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        }
    }

    /**
     * استعادة الوضع الطبيعي بعد انتهاء وقت الصلاة في المسجد
     */
    fun disableMosqueSilence(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasDndAccess(context)) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}
