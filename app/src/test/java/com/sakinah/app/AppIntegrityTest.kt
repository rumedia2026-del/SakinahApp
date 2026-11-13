package com.sakinah.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppIntegrityTest {

    private fun loadAzkarSeedJson(): JSONObject {
        // Look for azkar_seed.json in standard path relative to project root
        val possiblePaths = listOf(
            File("app/src/main/res/raw/azkar_seed.json"),
            File("src/main/res/raw/azkar_seed.json"),
            File("../app/src/main/res/raw/azkar_seed.json")
        )
        val file = possiblePaths.firstOrNull { it.exists() }
            ?: throw IllegalStateException("azkar_seed.json not found in expected paths")
        val content = file.readText(Charsets.UTF_8)
        return JSONObject(content)
    }

    @Test
    fun testSleepingAzkarIntegrity() {
        val root = loadAzkarSeedJson()
        val azkarArray = root.getJSONArray("azkar")
        val sleepingAzkar = mutableListOf<JSONObject>()

        for (i in 0 until azkarArray.length()) {
            val z = azkarArray.getJSONObject(i)
            if (z.getInt("categoryId") == 4) {
                sleepingAzkar.add(z)
            }
        }

        // 1. Must contain at least 12 authentic sleeping azkar (we populated 14)
        assertTrue("Sleeping Azkar should have at least 12 items, found: ${sleepingAzkar.size}", sleepingAzkar.size >= 12)

        // 2. Sleeping Azkar must NOT contain Istikhara
        for (z in sleepingAzkar) {
            val textPlain = z.getString("textPlain")
            assertFalse(
                "Sleeping Azkar should not contain Istikhara, found in zikr id: ${z.getInt("id")}",
                textPlain.contains("الاستخارة") || textPlain.contains("أستخيرك")
            )
        }
    }

    @Test
    fun testAllCategoriesNonEmpty() {
        val root = loadAzkarSeedJson()
        val categoriesArray = root.getJSONArray("categories")
        val azkarArray = root.getJSONArray("azkar")

        val categoryCounts = mutableMapOf<Int, Int>()
        for (i in 0 until azkarArray.length()) {
            val catId = azkarArray.getJSONObject(i).getInt("categoryId")
            categoryCounts[catId] = (categoryCounts[catId] ?: 0) + 1
        }

        assertTrue("Should have at least 10 categories", categoriesArray.length() >= 10)

        for (i in 0 until categoriesArray.length()) {
            val cat = categoriesArray.getJSONObject(i)
            val catId = cat.getInt("id")
            val catTitle = cat.getString("titleAr")
            val count = categoryCounts[catId] ?: 0
            assertTrue("Category '$catTitle' (id: $catId) must not be empty", count > 0)
        }
    }

    @Test
    fun testEmotionalCompanionTags() {
        val root = loadAzkarSeedJson()
        val azkarArray = root.getJSONArray("azkar")
        val tags = listOf("قلق", "حزن", "خوف", "هم", "رزق", "شكر", "استخارة", "مرض", "غضب")

        for (tag in tags) {
            var count = 0
            for (i in 0 until azkarArray.length()) {
                val z = azkarArray.getJSONObject(i)
                val emotionalTags = if (z.isNull("emotionalTags")) "" else z.getString("emotionalTags")
                if (emotionalTags.contains(tag)) {
                    count++
                }
            }
            assertTrue("Emotional tag '$tag' should have at least 10 azkar, found $count", count >= 10)
        }
    }

    @Test
    fun testAudioUrlsNoBasmalahFallback() {
        val root = loadAzkarSeedJson()
        val azkarArray = root.getJSONArray("azkar")

        for (i in 0 until azkarArray.length()) {
            val z = azkarArray.getJSONObject(i)
            if (!z.isNull("audioUrl")) {
                val url = z.getString("audioUrl")
                assertFalse(
                    "Azkar audio URL should not be 001001.mp3 fallback: ${z.getInt("id")}",
                    url.endsWith("001001.mp3")
                )
                assertTrue(
                    "Audio URL must be HTTPS or empty: $url",
                    url.startsWith("https://") || url.isBlank()
                )
            }
        }
    }

    @Test
    fun testRuqyahCategorySize() {
        val root = loadAzkarSeedJson()
        val azkarArray = root.getJSONArray("azkar")
        var ruqyahCount = 0

        for (i in 0 until azkarArray.length()) {
            val z = azkarArray.getJSONObject(i)
            if (z.getInt("categoryId") == 12) {
                ruqyahCount++
            }
        }

        assertTrue("Ruqyah category (id: 12) should have at least 30 azkar, found $ruqyahCount", ruqyahCount >= 30)
    }

    @Test
    fun testQuranUthmaniNoDuplicateBasmalah() {
        val possiblePaths = listOf(
            File("app/src/main/res/raw/quran_uthmani.json"),
            File("src/main/res/raw/quran_uthmani.json"),
            File("../app/src/main/res/raw/quran_uthmani.json")
        )
        val file = possiblePaths.firstOrNull { it.exists() }
            ?: throw IllegalStateException("quran_uthmani.json not found in expected paths")
        val content = file.readText(Charsets.UTF_8)
        val root = JSONObject(content)
        val surahs = root.getJSONObject("data").getJSONArray("surahs")

        for (i in 0 until surahs.length()) {
            val surah = surahs.getJSONObject(i)
            val surahNumber = surah.getInt("number")
            val ayahs = surah.getJSONArray("ayahs")
            val firstAyah = ayahs.getJSONObject(0)
            val text = firstAyah.getString("text")

            if (surahNumber != 1 && surahNumber != 9) {
                assertFalse(
                    "Surah $surahNumber Ayah 1 should not start with duplicate Basmalah",
                    text.startsWith("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ")
                )
            }
        }
    }

    @Test
    fun testQuranJalalaynExistsAndValid() {
        val possiblePaths = listOf(
            File("app/src/main/res/raw/quran_jalalayn.json"),
            File("src/main/res/raw/quran_jalalayn.json"),
            File("../app/src/main/res/raw/quran_jalalayn.json")
        )
        val file = possiblePaths.firstOrNull { it.exists() }
            ?: throw IllegalStateException("quran_jalalayn.json not found in expected paths")
        val content = file.readText(Charsets.UTF_8)
        val root = JSONObject(content)
        val surahs = root.getJSONObject("data").getJSONArray("surahs")
        assertEquals(114, surahs.length())
    }
}
