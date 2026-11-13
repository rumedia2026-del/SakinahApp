package com.sakinah.app.feature.quran

import android.content.Context
import com.sakinah.app.R
import com.sakinah.app.feature.quran.model.AyahItem
import com.sakinah.app.feature.quran.model.JuzItem
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
    private var jalalaynJsonRoot: JSONObject? = null

    fun getJuzList(): List<JuzItem> = listOf(
        JuzItem(1, "الجزء الأول", "سورة الفاتحة", 1),
        JuzItem(2, "الجزء الثاني", "سورة البقرة (١٤٢)", 22),
        JuzItem(3, "الجزء الثالث", "سورة البقرة (٢٥٣)", 42),
        JuzItem(4, "الجزء الرابع", "سورة آل عمران (٩٣)", 62),
        JuzItem(5, "الجزء الخامس", "سورة النساء (٢٤)", 82),
        JuzItem(6, "الجزء السادس", "سورة النساء (١٤٨)", 102),
        JuzItem(7, "الجزء السابع", "سورة المائدة (٨٢)", 121),
        JuzItem(8, "الجزء الثامن", "سورة الأنعام (١١١)", 142),
        JuzItem(9, "الجزء التاسع", "سورة الأعراف (٨٨)", 162),
        JuzItem(10, "الجزء العاشر", "سورة الأنفال (٤١)", 182),
        JuzItem(11, "الجزء الحادي عشر", "سورة التوبة (٩٣)", 201),
        JuzItem(12, "الجزء الثاني عشر", "سورة هود (٦)", 222),
        JuzItem(13, "الجزء الثالث عشر", "سورة يوسف (٥٣)", 242),
        JuzItem(14, "الجزء الرابع عشر", "سورة الحجر (١)", 262),
        JuzItem(15, "الجزء الخامس عشر", "سورة الإسراء (١)", 282),
        JuzItem(16, "الجزء السادس عشر", "سورة الكهف (٧٥)", 302),
        JuzItem(17, "الجزء السابع عشر", "سورة الأنبياء (١)", 322),
        JuzItem(18, "الجزء الثامن عشر", "سورة المؤمنون (١)", 342),
        JuzItem(19, "الجزء التاسع عشر", "سورة الفرقان (٢١)", 362),
        JuzItem(20, "الجزء العشرون", "سورة النمل (٥٦)", 382),
        JuzItem(21, "الجزء الحادي والعشرون", "سورة العنكبوت (٤٦)", 402),
        JuzItem(22, "الجزء الثاني والعشرون", "سورة الأحزاب (٣١)", 422),
        JuzItem(23, "الجزء الثالث والعشرون", "سورة يس (٢٨)", 442),
        JuzItem(24, "الجزء الرابع والعشرون", "سورة الزمر (٣٢)", 462),
        JuzItem(25, "الجزء الخامس والعشرون", "سورة فصلت (٤٧)", 482),
        JuzItem(26, "الجزء السادس والعشرون", "سورة الأحقاف (١)", 502),
        JuzItem(27, "الجزء السابع والعشرون", "سورة الذاريات (٣١)", 522),
        JuzItem(28, "الجزء الثامن والعشرون", "سورة المجادلة (١)", 542),
        JuzItem(29, "الجزء التاسع والعشرون", "سورة الملك (١)", 562),
        JuzItem(30, "الجزء الثلاثون", "سورة النبأ (١)", 582)
    )

    suspend fun getSurahsList(): List<SurahHeader> = withContext(Dispatchers.IO) {
        cachedSurahs?.let { return@withContext it }

        ensureUthmaniLoaded()
        val data = uthmaniJsonRoot?.getJSONObject("data")
        val surahsArray = data?.getJSONArray("surahs")

        val list = mutableListOf<SurahHeader>()
        if (surahsArray != null) {
            for (i in 0 until surahsArray.length()) {
                val s = surahsArray.getJSONObject(i)
                val ayahsArr = s.getJSONArray("ayahs")
                val startPage = if (ayahsArr.length() > 0) ayahsArr.getJSONObject(0).optInt("page", 1) else 1
                list.add(
                    SurahHeader(
                        number = s.getInt("number"),
                        name = s.getString("name"),
                        englishName = s.getString("englishName"),
                        revelationType = if (s.getString("revelationType").equals("Meccan", true)) "مكية" else "مدنية",
                        numberOfAyahs = ayahsArr.length(),
                        startPage = startPage
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
        ensureJalalaynLoaded()

        val uthmaniSurahs = uthmaniJsonRoot?.getJSONObject("data")?.getJSONArray("surahs")
        val tafsirSurahs = tafsirJsonRoot?.getJSONObject("data")?.getJSONArray("surahs")
        val jalalaynSurahs = jalalaynJsonRoot?.getJSONObject("data")?.getJSONArray("surahs")

        val result = mutableListOf<AyahItem>()
        if (uthmaniSurahs != null && surahNumber in 1..114) {
            val surahObj = uthmaniSurahs.getJSONObject(surahNumber - 1)
            val tafsirSurahObj = tafsirSurahs?.optJSONObject(surahNumber - 1)
            val jalalaynSurahObj = jalalaynSurahs?.optJSONObject(surahNumber - 1)

            val ayahs = surahObj.getJSONArray("ayahs")
            val tafsirAyahs = tafsirSurahObj?.optJSONArray("ayahs")
            val jalalaynAyahs = jalalaynSurahObj?.optJSONArray("ayahs")

            val surahName = surahObj.getString("name")

            for (i in 0 until ayahs.length()) {
                val a = ayahs.getJSONObject(i)
                val tafsirText = tafsirAyahs?.optJSONObject(i)?.optString("text", "التفسير الميسر غير متاح")
                val jalalaynText = jalalaynAyahs?.optJSONObject(i)?.optString("text", null)

                result.add(
                    AyahItem(
                        number = a.getInt("number"),
                        numberInSurah = a.getInt("numberInSurah"),
                        text = a.getString("text"),
                        surahNumber = surahNumber,
                        surahName = surahName,
                        juz = a.getInt("juz"),
                        page = a.getInt("page"),
                        tafsir = tafsirText,
                        jalalaynTafsir = jalalaynText
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

    private fun ensureJalalaynLoaded() {
        if (jalalaynJsonRoot == null) {
            try {
                val stream = context.resources.openRawResource(R.raw.quran_jalalayn)
                val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
                val content = reader.use { it.readText() }
                jalalaynJsonRoot = JSONObject(content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
