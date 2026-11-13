package com.sakinah.app.feature.social

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SocialMediaCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            SocialMediaReminderManager.checkAndNotifyIfExceeded(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "social_media_check_work"
    }
}
