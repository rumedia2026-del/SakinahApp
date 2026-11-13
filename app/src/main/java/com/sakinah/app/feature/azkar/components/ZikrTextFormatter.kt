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

    val colorQuran = Color(0xFF0F3D2E)      // أخضر زمردي داكن للآيات القرآنية
    val colorHadith = Color(0xFF2C2620)     // بني دافئ للأحاديث والأدعية
    val colorGoldAccent = Color(0xFFB8860B) // كهرماني/ذهبي للأرقام والفواصل

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
     * تمييز الألوان داخل نصوص الأذكار:
     * - الآيات القرآنية بالأخضر الزمردي (#0F3D2E)
     * - الأحاديث والأدعية بالبني الدافئ (#2C2620)
     * - الأرقام والفواصل بالذهبي (#B8860B)
     */
    fun formatZikrText(text: String, isCompleted: Boolean): AnnotatedString {
        val baseAlpha = if (isCompleted) 0.75f else 1f

        return buildAnnotatedString {
            var inQuran = false
            var inBrackets = false

            var i = 0
            while (i < text.length) {
                val c = text[i]

                if (c == '﴿') {
                    inQuran = true
                    append(c)
                    addStyle(SpanStyle(color = colorGoldAccent.copy(alpha = baseAlpha), fontWeight = FontWeight.Bold), length - 1, length)
                    i++
                    continue
                } else if (c == '﴾') {
                    inQuran = false
                    append(c)
                    addStyle(SpanStyle(color = colorGoldAccent.copy(alpha = baseAlpha), fontWeight = FontWeight.Bold), length - 1, length)
                    i++
                    continue
                }

                if (c == '(' || c == '（') {
                    inBrackets = true
                    append(c)
                    addStyle(SpanStyle(color = colorGoldAccent.copy(alpha = baseAlpha)), length - 1, length)
                    i++
                    continue
                } else if (c == ')' || c == '）') {
                    inBrackets = false
                    append(c)
                    addStyle(SpanStyle(color = colorGoldAccent.copy(alpha = baseAlpha)), length - 1, length)
                    i++
                    continue
                }

                append(c)
                val style = when {
                    inBrackets -> SpanStyle(color = colorGoldAccent.copy(alpha = baseAlpha), fontWeight = FontWeight.SemiBold)
                    inQuran -> SpanStyle(color = colorQuran.copy(alpha = baseAlpha), fontWeight = FontWeight.Bold)
                    else -> SpanStyle(color = colorHadith.copy(alpha = baseAlpha))
                }
                addStyle(style, length - 1, length)
                i++
            }
        }
    }
}
