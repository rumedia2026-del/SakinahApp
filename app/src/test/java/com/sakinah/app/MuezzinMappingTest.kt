package com.sakinah.app

import com.sakinah.app.feature.prayer.MuadhinCatalog
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MuezzinMappingTest {

    @Test
    fun testAllMuezzinsHaveValidExplicitRawResId() {
        val muadhins = MuadhinCatalog.muadhins
        assertTrue("MuadhinCatalog must contain at least 5 muezzins", muadhins.size >= 5)

        for (muezzin in muadhins) {
            assertNotNull("Muezzin id must not be null", muezzin.id)
            assertTrue("Muezzin id must not be blank", muezzin.id.isNotBlank())
            assertTrue("Muezzin name must not be blank", muezzin.name.isNotBlank())
            assertNotEquals("Muezzin rawResId must not be 0 for ${muezzin.name}", 0, muezzin.rawResId)
        }
    }

    @Test
    fun testRawAudioFilesExistOnDisk() {
        val rawPaths = listOf(
            File("app/src/main/res/raw"),
            File("src/main/res/raw"),
            File("../app/src/main/res/raw")
        )
        val rawDir = rawPaths.firstOrNull { it.exists() && it.isDirectory }
        assertNotNull("res/raw directory must exist", rawDir)

        val expectedFiles = listOf(
            "adhan_makkah.mp3",
            "adhan_madinah.mp3",
            "adhan_afasy.mp3",
            "adhan_abdulbasit.mp3",
            "adhan_qatami.mp3",
            "adhan_takbeer.mp3"
        )

        for (fileName in expectedFiles) {
            val audioFile = File(rawDir, fileName)
            assertTrue("Audio file $fileName must exist", audioFile.exists())
            assertTrue("Audio file $fileName must be larger than 50KB, found ${audioFile.length()} bytes", audioFile.length() > 50_000)
        }
    }
}
