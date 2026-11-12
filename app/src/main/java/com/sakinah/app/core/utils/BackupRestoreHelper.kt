package com.sakinah.app.core.utils

import android.content.Context
import android.net.Uri
import com.sakinah.app.data.local.db.entities.ZikrEntity
import com.sakinah.app.data.repository.AzkarRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object BackupRestoreHelper {

    suspend fun createBackupJson(azkarRepository: AzkarRepository): String {
        val favorites = azkarRepository.getFavoriteAzkar().first()

        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val array = JSONArray()
        favorites.forEach { zikr ->
            val item = JSONObject().apply {
                put("id", zikr.id)
                put("categoryId", zikr.categoryId)
                put("textWithTashkeel", zikr.textWithTashkeel)
                put("textPlain", zikr.textPlain)
                put("repeatCount", zikr.repeatCount)
                put("virtueText", zikr.virtueText ?: "")
                put("sourceReference", zikr.sourceReference ?: "")
                put("isUserCustom", zikr.isUserCustom)
                put("isFavorite", zikr.isFavorite)
            }
            array.put(item)
        }

        root.put("azkar", array)
        return root.toString(2)
    }

    suspend fun exportToJson(
        context: Context,
        uri: Uri,
        azkarRepository: AzkarRepository
    ): Int {
        val jsonString = createBackupJson(azkarRepository)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
        }
        val root = JSONObject(jsonString)
        return root.optJSONArray("azkar")?.length() ?: 0
    }

    suspend fun restoreFromJson(
        context: Context,
        uri: Uri,
        azkarRepository: AzkarRepository
    ): Int {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return 0
        val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

        val root = JSONObject(jsonString)
        val array = root.getJSONArray("azkar")
        var count = 0

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val zikr = ZikrEntity(
                id = item.getInt("id"),
                categoryId = item.getInt("categoryId"),
                textWithTashkeel = item.getString("textWithTashkeel"),
                textPlain = item.getString("textPlain"),
                repeatCount = item.getInt("repeatCount"),
                virtueText = item.optString("virtueText", null),
                sourceReference = item.optString("sourceReference", null),
                authenticityGrade = null,
                audioUrl = null,
                emotionalTags = null,
                isUserCustom = item.optBoolean("isUserCustom", false),
                isFavorite = item.optBoolean("isFavorite", true)
            )
            azkarRepository.addCustomZikr(zikr)
            count++
        }

        return count
    }
}
