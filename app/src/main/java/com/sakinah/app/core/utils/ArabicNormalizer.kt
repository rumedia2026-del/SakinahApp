package com.sakinah.app.core.utils

import java.util.regex.Pattern

object ArabicNormalizer {

    private val TASHKEEL_AND_QURAN_REGEX = Pattern.compile("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val ALEF_REGEX = Pattern.compile("[أإآٱ]")
    private val NON_ARABIC_REGEX = Pattern.compile("[^\\p{InArabic}0-9\\s]")
    private val WHITESPACE_REGEX = Pattern.compile("\\s+")

    /**
     * التطبيع النصي العربي الدقيق للبحث القرآني والفقهي:
     * 1) حذف جميع حركات التشكيل وعلامات الوقف والضبط القرآنية والتطويل
     * 2) توحيد الألفات وألف الوصل (أ، إ، آ، ٱ -> ا)
     * 3) توحيد التاء المربوطة (ة -> ه)
     * 4) توحيد الألف المقصورة (ى -> ي)
     */
    fun normalize(text: String?): String {
        if (text.isNullOrBlank()) return ""
        var s = TASHKEEL_AND_QURAN_REGEX.matcher(text).replaceAll("")
        s = s.replace("\u0640", "") // Remove Tatweel
        s = ALEF_REGEX.matcher(s).replaceAll("ا")
        s = s.replace('ة', 'ه')
        s = s.replace('ى', 'ي')
        s = NON_ARABIC_REGEX.matcher(s).replaceAll(" ")
        s = WHITESPACE_REGEX.matcher(s).replaceAll(" ")
        return s.trim()
    }

    /**
     * إزالة كلمة "سورة" أو "ال" الزائدة للمطابقة الذكية
     */
    fun stripSurahOrAl(text: String): String {
        var s = text.trim()
        if (s.startsWith("سورة ")) {
            s = s.removePrefix("سورة ").trim()
        }
        if (s.startsWith("ال") && s.length > 2) {
            s = s.removePrefix("ال").trim()
        }
        return s
    }

    /**
     * التحقق مما إذا كان النص المطبع يطابق كلمة البحث بمرونة مع/بدون "ال" التعريف
     */
    fun matchesSmart(target: String, query: String): Boolean {
        val normTarget = normalize(target)
        val normQuery = normalize(query)
        if (normTarget.contains(normQuery, ignoreCase = true)) return true

        val strippedTarget = stripSurahOrAl(normTarget)
        val strippedQuery = stripSurahOrAl(normQuery)
        return strippedTarget.contains(strippedQuery, ignoreCase = true) ||
                normTarget.contains(strippedQuery, ignoreCase = true) ||
                strippedTarget.contains(normQuery, ignoreCase = true)
    }
}

/**
 * Extension function للتطبيع الفوري للسلاسل النصية
 */
fun String.normalizeArabicForSearch(): String = ArabicNormalizer.normalize(this)
