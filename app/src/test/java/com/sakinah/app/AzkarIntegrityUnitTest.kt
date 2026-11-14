package com.sakinah.app

import com.sakinah.app.core.utils.ArabicNormalizer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AzkarIntegrityUnitTest {

    private fun loadAzkarSeed(): JSONObject {
        val paths = listOf(
            File("app/src/main/res/raw/azkar_seed.json"),
            File("src/main/res/raw/azkar_seed.json"),
            File("../app/src/main/res/raw/azkar_seed.json")
        )
        val file = paths.firstOrNull { it.exists() }
            ?: throw IllegalStateException("azkar_seed.json not found")
        return JSONObject(file.readText(Charsets.UTF_8))
    }

    @Test
    fun testArabicNormalizer() {
        val original = "أَعُوذُ بِاللَّهِ مِنَ الشَّيطَانِ الرَّجِيمِ"
        val normalized = ArabicNormalizer.normalize(original)
        assertFalse(normalized.contains("أ"))
        assertFalse(normalized.contains("َ"))
        assertEquals("اعوذ بالله من الشيطان الرجيم", normalized)

        val withTaa = "صَلاَةٌ وَزَكَاةٌ"
        val normalizedTaa = ArabicNormalizer.normalize(withTaa)
        assertEquals("صلاه وزكاه", normalizedTaa)

        val withYaa = "عَلَى الْهُدَى"
        val normalizedYaa = ArabicNormalizer.normalize(withYaa)
        assertEquals("علي الهدي", normalizedYaa)
    }

    @Test
    fun testNoExactDuplicatesPerCategory() {
        val root = loadAzkarSeed()
        val azkar = root.getJSONArray("azkar")
        val map = mutableMapOf<Int, MutableSet<String>>()

        for (i in 0 until azkar.length()) {
            val z = azkar.getJSONObject(i)
            val catId = z.getInt("categoryId")
            val text = z.optString("textPlain").ifBlank { z.getString("textWithTashkeel") }
            val norm = ArabicNormalizer.normalize(text)
            val set = map.getOrPut(catId) { mutableSetOf() }
            assertFalse(
                "Duplicate zikr found in category $catId: ID ${z.getInt("id")}, text: $norm",
                set.contains(norm)
            )
            set.add(norm)
        }
    }

    @Test
    fun testAudioUrlsValidity() {
        val root = loadAzkarSeed()
        val azkar = root.getJSONArray("azkar")
        for (i in 0 until azkar.length()) {
            val z = azkar.getJSONObject(i)
            if (!z.isNull("audioUrl")) {
                val url = z.getString("audioUrl")
                if (url.isNotBlank()) {
                    assertTrue("Audio URL must start with https://: $url", url.startsWith("https://"))
                    assertFalse(
                        "Audio URL must not be 001001.mp3 fallback: ID ${z.getInt("id")}",
                        url.endsWith("001001.mp3")
                    )
                }
            }
        }
    }
}
