package com.sakinah.app.feature.quran

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.sakinah.app.feature.quran.model.AyahItem

object MushafPageLayoutHelper {

    private fun toArabicDigits(number: Int): String {
        val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { if (it in '0'..'9') arabicChars[it - '0'] else it }.joinToString("")
    }

    /**
     * تجميع آيات الصفحة الواحدة في كتلة نصية متصلة واحدة (Continuous Flow Block)
     * تمنع التفكك الرأسي وتطابق إخراج مصحف "تدبر وعمل" ومصحف المدينة المنورة بدقة.
     */
    fun buildPageAnnotatedString(
        ayahs: List<AyahItem>,
        isTajweedEnabled: Boolean,
        textColor: Color,
        goldSeparatorColor: Color,
        isHifzMode: Boolean,
        unmaskedWordKeys: Set<String>
    ): AnnotatedString {
        return buildAnnotatedString {
            ayahs.forEachIndexed { index, ayah ->
                if (index > 0) {
                    append(" ")
                }

                if (isHifzMode) {
                    val words = ayah.text.split(" ")
                    val masked = words.mapIndexed { idx, w ->
                        val key = "${ayah.number}_$idx"
                        if (unmaskedWordKeys.contains(key)) w else "████"
                    }.joinToString(" ")
                    append(masked)
                } else {
                    val annotated = TajweedAnnotator.annotate(ayah.text, isTajweedEnabled, textColor)
                    append(annotated)
                }

                // فاصل الآية المزخرف ﴿٦﴾ بلون عنبري مذهب
                append(" ")
                val startSep = length
                append("﴿${toArabicDigits(ayah.numberInSurah)}﴾")
                val endSep = length

                addStyle(
                    SpanStyle(
                        color = goldSeparatorColor,
                        fontWeight = FontWeight.Bold
                    ),
                    startSep,
                    endSep
                )
            }
        }
    }
}
