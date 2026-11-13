package com.sakinah.app.feature.azkar.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

data class ShortSurahItem(
    val title: String,
    val text: String
)

object ZikrTextFormatter {

    /**
     * فحص هل يحتوي الذكر على المعوذات والسور الثلاث القصيرة
     */
    fun extractShortSurahs(text: String): List<ShortSurahItem>? {
        val hasIkhlas = text.contains("قل هو الله أحد") || text.contains("قُلْ هُوَ اللَّهُ أَحَدٌ")
        val hasFalaq = text.contains("قل أعوذ برب الفلق") || text.contains("قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ")
        val hasNas = text.contains("قل أعوذ برب الناس") || text.contains("قُلْ أَعُوذُ بِرَبِّ النَّاسِ")

        if (hasIkhlas && (hasFalaq || hasNas)) {
            val list = mutableListOf<ShortSurahItem>()
            list.add(
                ShortSurahItem(
                    title = "﴿ سُورَةُ الإِخْلَاصِ ﴾",
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ\nقُلْ هُوَ اللَّهُ أَحَدٌ ﴿١﴾ اللَّهُ الصَّمَدُ ﴿٢﴾ لَمْ يَلِدْ وَلَمْ يُولَدْ ﴿٣﴾ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ ﴿٤﴾"
                )
            )
            list.add(
                ShortSurahItem(
                    title = "﴿ سُورَةُ الفَلَقِ ﴾",
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ﴿١﴾ مِن شَرِّ مَا خَلَقَ ﴿٢﴾ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ﴿٣﴾ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ﴿٤﴾ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴿٥﴾"
                )
            )
            list.add(
                ShortSurahItem(
                    title = "﴿ سُورَةُ النَّاسِ ﴾",
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ النَّاسِ ﴿١﴾ مَلِكِ النَّاسِ ﴿٢﴾ إِلَٰهِ النَّاسِ ﴿٣﴾ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ﴿٤﴾ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ﴿٥﴾ مِنَ الْجِنَّةِ وَالنَّاسِ ﴿٦﴾"
                )
            )
            return list
        }
        return null
    }

    /**
     * تنسيق نصوص الأذكار بدون تقطيع الحروف العربية إطلاقاً (Whole Word/Phrase Shaping).
     * يعتمد على معايير WCAG والتباين اللوني من ألوان السمة Theme:
     * - textPrimary: للنصوص العامة والأدعية (onSurface)
     * - quranColor: للآيات القرآنية داخل ﴿ ... ﴾ (primary)
     * - numberAccent: للأقواس والفواصل والأرقام (tertiary / gold)
     */
    fun formatZikrText(
        text: String,
        isCompleted: Boolean,
        textPrimary: Color,
        quranColor: Color,
        numberAccent: Color
    ): AnnotatedString {
        val baseAlpha = if (isCompleted) 0.70f else 1f

        // تقسيم النص إلى مقاطع كاملة بدلاً من حرف بحرف للحفاظ على HarfBuzz Arabic Shaping
        val regex = Regex("""(﴿[^﴾]*﴾|\([^\)]*\}|（[^）]*）|\([^)]*\))""")
        val parts = mutableListOf<Pair<String, Int>>() // Text to type: 0: normal, 1: quran, 2: bracket

        var lastIdx = 0
        val matcher = regex.toPattern().matcher(text)
        while (matcher.find()) {
            if (matcher.start() > lastIdx) {
                parts.add(Pair(text.substring(lastIdx, matcher.start()), 0))
            }
            val matchStr = matcher.group()
            if (matchStr.startsWith("﴿") && matchStr.endsWith("﴾")) {
                parts.add(Pair(matchStr, 1))
            } else {
                parts.add(Pair(matchStr, 2))
            }
            lastIdx = matcher.end()
        }
        if (lastIdx < text.length) {
            parts.add(Pair(text.substring(lastIdx), 0))
        }

        return buildAnnotatedString {
            for ((segment, type) in parts) {
                val start = length
                append(segment)
                val end = length

                val style = when (type) {
                    1 -> SpanStyle(
                        color = quranColor.copy(alpha = baseAlpha),
                        fontWeight = FontWeight.Bold
                    )
                    2 -> SpanStyle(
                        color = numberAccent.copy(alpha = baseAlpha),
                        fontWeight = FontWeight.SemiBold
                    )
                    else -> SpanStyle(
                        color = textPrimary.copy(alpha = baseAlpha)
                    )
                }
                addStyle(style, start, end)
            }
        }
    }
}
