package com.sakinah.app

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FiqhGuideUnitTest {

    private fun loadFiqhGuide(): JSONObject {
        val paths = listOf(
            File("app/src/main/res/raw/fiqh_guide.json"),
            File("src/main/res/raw/fiqh_guide.json"),
            File("../app/src/main/res/raw/fiqh_guide.json")
        )
        val file = paths.firstOrNull { it.exists() }
            ?: throw IllegalStateException("fiqh_guide.json not found")
        return JSONObject(file.readText(Charsets.UTF_8))
    }

    @Test
    fun testAllMandatoryChaptersExist() {
        val root = loadFiqhGuide()
        val chapters = root.getJSONArray("chapters")

        assertEquals("Must contain exactly 5 mandatory chapters", 5, chapters.length())

        val expectedTitles = listOf(
            "سجود السهو",
            "صلاة المسافر",
            "أحكام الطهارة والوضوء والمسح",
            "أحكام المسبوق",
            "أركان الصلاة وواجباتها وسننها"
        )

        for (i in 0 until chapters.length()) {
            val ch = chapters.getJSONObject(i)
            val title = ch.getString("title")
            val matched = expectedTitles.any { title.contains(it) }
            assertTrue("Chapter title '$title' must match expected mandatory categories", matched)
            assertTrue("Chapter must have non-empty issues", ch.getJSONArray("issues").length() > 0)
        }
    }

    @Test
    fun testAllIssuesHaveEvidenceAndSources() {
        val root = loadFiqhGuide()
        val chapters = root.getJSONArray("chapters")
        var totalIssues = 0

        for (i in 0 until chapters.length()) {
            val ch = chapters.getJSONObject(i)
            val issues = ch.getJSONArray("issues")
            totalIssues += issues.length()

            for (j in 0 until issues.length()) {
                val issue = issues.getJSONObject(j)
                val id = issue.getInt("id")
                val title = issue.getString("title")

                // Check title
                assertFalse("Issue $id title should not be blank", title.isBlank())

                // Check ruling
                val ruling = issue.getString("ruling")
                assertFalse("Issue $id ruling should not be blank", ruling.isBlank())

                // Check steps
                val steps = issue.getJSONArray("steps")
                assertTrue("Issue $id must have practical steps", steps.length() > 0)

                // Check evidence
                val evidence = issue.getString("evidence_quran_or_hadith")
                assertFalse("Issue $id must have Quran or Hadith evidence", evidence.isBlank())
                assertTrue(
                    "Issue $id evidence must cite Quran or Hadith: $evidence",
                    evidence.contains("ﷺ") || evidence.contains("صحيح") || evidence.contains("تعالى") || evidence.contains("أبي داود")
                )

                // Check madhab summary
                val madhab = issue.getString("madhab_summary")
                assertFalse("Issue $id madhab summary should not be blank", madhab.isBlank())

                // Check common mistakes
                val mistakes = issue.getJSONArray("common_mistakes")
                assertTrue("Issue $id must list common mistakes to avoid", mistakes.length() > 0)

                // Check source book
                val source = issue.getString("source_book")
                assertFalse("Issue $id must have authoritative source book", source.isBlank())
            }
        }

        assertTrue("Fiqh guide must have at least 12 detailed issues, found $totalIssues", totalIssues >= 12)
    }
}
