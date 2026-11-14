package com.sakinah.app

import androidx.compose.ui.graphics.Color
import com.sakinah.app.feature.quran.QuranThemeManager
import com.sakinah.app.feature.social.SocialMediaReminderManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialMediaAndQuranUnitTest {

    @Test
    fun testQuranRoyalDarkThemeColors() {
        val darkTheme = QuranThemeManager.getTheme(5)
        assertNotNull(darkTheme)
        assertEquals("الوضع المظلم الملكي", darkTheme.nameAr)
        assertEquals(Color(0xFF121417), darkTheme.backgroundColor)
        assertEquals(Color(0xFFF5F0E6), darkTheme.textColor)
        assertEquals(Color(0xFFFCD34D), darkTheme.accentColor)
    }

    @Test
    fun testSocialMediaTargetPackages() {
        val targets = SocialMediaReminderManager.TARGET_PACKAGES
        assertTrue("Must include Facebook", targets.contains("com.facebook.katana"))
        assertTrue("Must include Instagram", targets.contains("com.instagram.android"))
        assertTrue("Must include TikTok", targets.contains("com.zhiliaoapp.musically") || targets.contains("com.ss.android.ugc.trill"))
        assertTrue("Must include YouTube", targets.contains("com.google.android.youtube"))
        assertTrue("Must include Twitter/X", targets.contains("com.twitter.android"))
    }

    @Test
    fun testSocialMediaWittyMessages() {
        val messages = SocialMediaReminderManager.WITTY_MESSAGES
        assertTrue("Must contain at least 15 witty messages", messages.size >= 15)
        val hasParadiseReminder = messages.any { it.contains("الجنة") || it.contains("تيك توك") || it.contains("مصحف") }
        assertTrue("Must contain engaging friendly reminder", hasParadiseReminder)
    }
}
