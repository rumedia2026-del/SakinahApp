package com.sakinah.app.feature.quran.audio

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object QuranAudioDownloader {

    fun getSurahFile(context: Context, sheikhId: String, surahNumber: Int): File {
        val dir = File(context.filesDir, "quran_audio/$sheikhId")
        if (!dir.exists()) dir.mkdirs()
        val formatted = String.format("%03d", surahNumber)
        return File(dir, "$formatted.mp3")
    }

    fun isSurahDownloaded(context: Context, sheikhId: String, surahNumber: Int): Boolean {
        val file = getSurahFile(context, sheikhId, surahNumber)
        return file.exists() && file.length() > 1024 // Greater than 1KB
    }

    suspend fun downloadSurah(
        context: Context,
        sheikhId: String,
        surahNumber: Int,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val destinationFile = getSurahFile(context, sheikhId, surahNumber)
        val tempFile = File(destinationFile.parentFile, "${destinationFile.name}.tmp")
        val urlString = QuranSheikhManager.getSurahOnlineUrl(sheikhId, surahNumber)

        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                requestMethod = "GET"
            }
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("HTTP error ${connection.responseCode}"))
            }

            val fileLength = connection.contentLength.toFloat()
            inputStream = connection.inputStream
            outputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(8192)
            var totalBytesRead = 0L
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                if (fileLength > 0) {
                    val progress = (totalBytesRead / fileLength).coerceIn(0f, 1f)
                    onProgress(progress)
                }
            }

            outputStream.flush()
            if (tempFile.renameTo(destinationFile)) {
                onProgress(1.0f)
                Result.success(destinationFile)
            } else {
                tempFile.copyTo(destinationFile, overwrite = true)
                tempFile.delete()
                onProgress(1.0f)
                Result.success(destinationFile)
            }
        } catch (e: Exception) {
            tempFile.delete()
            Result.failure(e)
        } finally {
            outputStream?.close()
            inputStream?.close()
            connection?.disconnect()
        }
    }

    fun deleteSurah(context: Context, sheikhId: String, surahNumber: Int): Boolean {
        val file = getSurahFile(context, sheikhId, surahNumber)
        return if (file.exists()) file.delete() else false
    }

    fun clearAllDownloads(context: Context): Long {
        val baseDir = File(context.filesDir, "quran_audio")
        var freedBytes = 0L
        if (baseDir.exists()) {
            baseDir.walkTopDown().forEach { f ->
                if (f.isFile) {
                    freedBytes += f.length()
                    f.delete()
                }
            }
            baseDir.deleteRecursively()
        }
        return freedBytes
    }

    fun getTotalDownloadedSizeFormatted(context: Context): String {
        val baseDir = File(context.filesDir, "quran_audio")
        var totalBytes = 0L
        if (baseDir.exists()) {
            baseDir.walkTopDown().forEach { f ->
                if (f.isFile) totalBytes += f.length()
            }
        }
        val mb = totalBytes / (1024 * 1024)
        return if (mb > 0) "$mb ميغابايت" else "${totalBytes / 1024} كيلوبايت"
    }
}
