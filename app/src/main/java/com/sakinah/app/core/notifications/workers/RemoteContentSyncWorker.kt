package com.sakinah.app.core.notifications.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sakinah.app.data.local.db.SakinahDatabase
import com.sakinah.app.data.local.db.entities.ZikrEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

@HiltWorker
class RemoteContentSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val database: SakinahDatabase
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "remote_content_sync_work"
        const val PREFS_NAME = "sakinah_remote_sync_prefs"
        const val KEY_LOCAL_CONTENT_VERSION = "local_content_version"
        const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"

        const val VERSION_URL =
            "https://raw.githubusercontent.com/rumedia2026-del/SakinahApp/main/remote_content/version.json"
        const val AZKAR_URL =
            "https://raw.githubusercontent.com/rumedia2026-del/SakinahApp/main/remote_content/azkar_seed.json"

        fun getLocalContentVersion(context: Context): Int {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_LOCAL_CONTENT_VERSION, 1)
        }

        fun getLastSyncTime(context: Context): Long {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
        }

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<RemoteContentSyncWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun syncNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<RemoteContentSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val localVersion = getLocalContentVersion(context)
            Log.d("RemoteContentSync", "Checking remote version... local is $localVersion")

            val versionJsonString = fetchUrl(VERSION_URL, timeoutMs = 12000) ?: return@withContext Result.retry()
            val remoteObj = JSONObject(versionJsonString)
            val remoteVersion = remoteObj.optInt("contentVersion", localVersion)

            Log.d("RemoteContentSync", "Remote version is $remoteVersion (local: $localVersion)")

            if (remoteVersion > localVersion) {
                val azkarJsonString = fetchUrl(AZKAR_URL, timeoutMs = 12000)
                if (azkarJsonString != null) {
                    val root = JSONObject(azkarJsonString)
                    val azkarArray = root.getJSONArray("azkar")
                    val existingFavorites = try {
                        database.zikrDao().getFavoriteIds().toSet()
                    } catch (_: Exception) {
                        emptySet()
                    }

                    val updatedAzkar = mutableListOf<ZikrEntity>()
                    for (i in 0 until azkarArray.length()) {
                        val obj = azkarArray.getJSONObject(i)
                        val zId = obj.getInt("id")
                        val isFav = existingFavorites.contains(zId) || obj.optBoolean("isFavorite", false)
                        updatedAzkar.add(
                            ZikrEntity(
                                id = zId,
                                categoryId = obj.getInt("categoryId"),
                                textWithTashkeel = obj.getString("textWithTashkeel"),
                                textPlain = obj.getString("textPlain"),
                                repeatCount = obj.getInt("repeatCount"),
                                virtueText = if (obj.isNull("virtueText")) null else obj.getString("virtueText"),
                                sourceReference = if (obj.isNull("sourceReference")) null else obj.getString("sourceReference"),
                                authenticityGrade = if (obj.isNull("authenticityGrade")) null else obj.getString("authenticityGrade"),
                                audioUrl = if (obj.isNull("audioUrl")) null else obj.getString("audioUrl"),
                                emotionalTags = if (obj.isNull("emotionalTags")) null else obj.getString("emotionalTags"),
                                isUserCustom = obj.optBoolean("isUserCustom", false),
                                isFavorite = isFav
                            )
                        )
                    }

                    database.zikrDao().insertAzkar(updatedAzkar)

                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .putInt(KEY_LOCAL_CONTENT_VERSION, remoteVersion)
                        .putLong(KEY_LAST_SYNC_TIMESTAMP, System.currentTimeMillis())
                        .apply()

                    Log.i("RemoteContentSync", "Successfully synced ${updatedAzkar.size} azkar to version $remoteVersion")
                }
            } else {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(KEY_LAST_SYNC_TIMESTAMP, System.currentTimeMillis())
                    .apply()
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("RemoteContentSync", "Error during remote sync", e)
            Result.retry()
        }
    }

    private fun fetchUrl(urlString: String, timeoutMs: Int): String? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "SakinahApp-Android")
            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                reader.use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("RemoteContentSync", "Failed to fetch $urlString: ${e.message}")
            null
        }
    }
}
