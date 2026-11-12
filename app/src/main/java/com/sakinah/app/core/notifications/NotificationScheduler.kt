package com.sakinah.app.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.sakinah.app.core.notifications.workers.AzkarReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleDailyReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // تذكير أذكار الصباح (حوالي الساعة 5:30 صباحاً)
        schedulePeriodicReminder(
            workManager = workManager,
            workName = "morning_azkar_reminder",
            type = AzkarReminderWorker.TYPE_MORNING,
            targetHour = 5,
            targetMinute = 30
        )

        // تذكير أذكار المساء (حوالي الساعة 17:00 عصراً)
        schedulePeriodicReminder(
            workManager = workManager,
            workName = "evening_azkar_reminder",
            type = AzkarReminderWorker.TYPE_EVENING,
            targetHour = 17,
            targetMinute = 0
        )

        // تذكير أذكار النوم (حوالي الساعة 22:30 مساءً)
        schedulePeriodicReminder(
            workManager = workManager,
            workName = "night_azkar_reminder",
            type = AzkarReminderWorker.TYPE_NIGHT,
            targetHour = 22,
            targetMinute = 30
        )
    }

    private fun schedulePeriodicReminder(
        workManager: WorkManager,
        workName: String,
        type: String,
        targetHour: Int,
        targetMinute: Int
    ) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<AzkarReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(AzkarReminderWorker.KEY_REMINDER_TYPE to type))
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
