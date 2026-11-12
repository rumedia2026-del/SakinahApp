package com.sakinah.app.core.notifications.receivers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object NotificationReceiverHelper {

    fun getTasbihPendingIntent(
        context: Context,
        notificationId: Int,
        currentCount: Int,
        phrase: String
    ): PendingIntent {
        val intent = Intent(context, NotificationTasbihReceiver::class.java).apply {
            action = NotificationTasbihReceiver.ACTION_TASBIH_CLICK
            putExtra(NotificationTasbihReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationTasbihReceiver.EXTRA_COUNT, currentCount)
            putExtra(NotificationTasbihReceiver.EXTRA_PHRASE, phrase)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(context, notificationId, intent, flags)
    }
}
