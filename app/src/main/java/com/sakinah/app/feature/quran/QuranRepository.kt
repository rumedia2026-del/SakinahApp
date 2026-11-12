package com.sakinah.app.feature.quran

import android.content.Context
import com.sakinah.app.R
import com.sakinah.app.feature.quran.model.AyahItem
import com.sakinah.app.feature.quran.model.SurahHeader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var cachedSurahs: List<SurahHeader>? = null
    private var cachedAllAyahs: List<AyahItem>? = null
    private var pageMap: Map<Int, List<AyahItem>>? = null
    private var uthmaniJsonRoot: JSONObject? = null
    private var tafsirJsonRoot: JSONObject? = null

    suspend fun getSurahsList(): List<SurahHeader> = withContext(Dispatchers.IO) {
        cachedSurahs?.let { return@withContext it }

        ensureUthmaniLoaded()
        val data = uthmaniJsonRoot?.getJSONObject("data")
        val surahsArray = data?.getJSONArray("surahs")

        val list = mutableListOf<SurahHeader>()
        if (surahsArray != null) {
            for (i in 0 until surahsArray.length()) {
                val s = surahsArray.getJSONObject(i)
                list.add(
                    SurahHeader(
                        number = s.getInt("number"),
                        name = s.getString("name"),
                        englishName = s.getString("englishName"),
                        revelationType = if (s.getString("revelationType").equals("Meccan", true)) "مكية" else "مدنية",
                        numberOfAyahs = s.getJSONArray("ayahs").length()
                    )
                )
            }
        }
        cachedSurahs = list
        list
    }

    suspend fun getAyahsForSurah(surahNumber: Int): List<AyahItem> = withContext(Dispatchers.IO) {
        ensureUthmaniLoaded()
        ensureTafsirLoaded()

        val uthmaniSurahs = uthmaniJsonRoot?.getJSONObject("data")?.getJSONArray("surahs")
        val tafsirSurahs = tafsirJsonRoot?.getJSONObject("data")?.getJSONArray("surahs")

        val result = mutableListOf<AyahItem>()
        if (uthmaniSurahs != null && surahNumber in 1..114) {
            val surahObj = uthmaniSurahs.getJSONObject(surahNumber - 1)
            val tafsirSurahObj = tafsirSurahs?.optJSONObject(surahNumber - 1)

            val ayahs = surahObj.getJSONArray("ayahs")
            val tafsirAyahs = tafsirSurahObj?.optJSONArray("ayahs")

            val surahName = surahObj.getString("name")

            for (i in 0 until ayahs.length()) {
                val a = ayahs.getJSONObject(i)
                val tafsirText = tafsirAyahs?.optJSONObject(i)?.optString("text", "التفسير الميسر غير متاح")

                result.add(
                    AyahItem(
                        number = a.getInt("number"),
                        numberInSurah = a.getInt("numberInSurah"),
                        text = a.getString("text"),
                        surahNumber = surahNumber,
                        surahName = surahName,
                        juz = a.getInt("juz"),
                        page = a.getInt("page"),
                        tafsir = tafsirText
                    )
                )
            }
        }
        result
    }

    suspend fun getAllPages(): Map<Int, List<AyahItem>> = withContext(Dispatchers.IO) {
        if (pageMap == null) {
            val all = mutableListOf<AyahItem>()
            for (surahNum in 1..114) {
                all.addAll(getAyahsForSurah(surahNum))
            }
            cachedAllAyahs = all
            pageMap = all.groupBy { it.page }
        }
        pageMap ?: emptyMap()
    }

    suspend fun getAyahsForPage(pageNumber: Int): List<AyahItem> = withContext(Dispatchers.IO) {
        val safePage = pageNumber.coerceIn(1, 604)
        val all = getAllPages()
        all[safePage] ?: emptyList()
    }

    private fun ensureUthmaniLoaded() {
        if (uthmaniJsonRoot == null) {
            val stream = context.resources.openRawResource(R.raw.quran_uthmani)
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            val content = reader.use { it.readText() }
            uthmaniJsonRoot = JSONObject(content)
        }
    }

    private fun ensureTafsirLoaded() {
        if (tafsirJsonRoot == null) {
            val stream = context.resources.openRawResource(R.raw.quran_tafsir)
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            val content = reader.use { it.readText() }
            tafsirJsonRoot = JSONObject(content)
        }
    }
}
