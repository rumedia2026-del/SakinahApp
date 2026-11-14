package com.sakinah.app.core.utils

import java.util.regex.Pattern

object ArabicNormalizer {

    private val TASHKEEL_REGEX = Pattern.compile("[\\u064B-\\u065F\\u0670]")
    private val ALEF_REGEX = Pattern.compile("[أإآٱ]")
    private val NON_ARABIC_REGEX = Pattern.compile("[^\\p{InArabic}\\s]")
    private val WHITESPACE_REGEX = Pattern.compile("\\s+")

    fun normalize(text: String?): String {
        if (text.isNullOrBlank()) return ""
        var s = TASHKEEL_REGEX.matcher(text).replaceAll("")
        s = s.replace("\u0640", "") // Remove Tatweel
        s = ALEF_REGEX.matcher(s).replaceAll("ا")
        s = s.replace('ة', 'ه')
        s = s.replace('ى', 'ي')
        s = NON_ARABIC_REGEX.matcher(s).replaceAll(" ")
        s = WHITESPACE_REGEX.matcher(s).replaceAll(" ")
        return s.trim()
    }
}
