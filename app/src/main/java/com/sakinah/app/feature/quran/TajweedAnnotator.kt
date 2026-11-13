package com.sakinah.app.feature.quran

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

object TajweedAnnotator {

    val colorGhunnah = Color(0xFF2E7D32) // أخضر هادئ للغنة
    val colorQalqalah = Color(0xFF1565C0) // أزرق للقلقلة
    val colorMadd = Color(0xFFC62828)     // أحمر خفيف للمدود
    val colorAmberAyah = Color(0xFFB8860B) // كهرماني/ذهبي لفواصل الآيات

    /**
     * تلوين أحكام التجويد الأساسية أوفلاين بأسلوب مريح للعين
     */
    fun annotate(text: String, isTajweedEnabled: Boolean, baseColor: Color): AnnotatedString {
        if (!isTajweedEnabled) {
            return buildAnnotatedString {
                append(text)
                addStyle(SpanStyle(color = baseColor), 0, text.length)
            }
        }

        return buildAnnotatedString {
            append(text)
            addStyle(SpanStyle(color = baseColor), 0, text.length)

            val qalqalahChars = charArrayOf('ق', 'ط', 'ب', 'ج', 'د')

            for (i in text.indices) {
                val c = text[i]

                // 1. المدود (وجود علامة المد فوق الحرف \u0653 أو حرف آ)
                if (c == '\u0653' || c == 'آ') {
                    val start = (i - 1).coerceAtLeast(0)
                    val end = (i + 1).coerceAtMost(text.length)
                    addStyle(
                        SpanStyle(color = colorMadd, fontWeight = FontWeight.Bold),
                        start,
                        end
                    )
                }

                // 2. الغنة (نون أو ميم مشددة)
                if ((c == 'ن' || c == 'م') && i + 1 < text.length && text[i + 1] == '\u0651') {
                    val end = (i + 2).coerceAtMost(text.length)
                    addStyle(
                        SpanStyle(color = colorGhunnah, fontWeight = FontWeight.Bold),
                        i,
                        end
                    )
                }

                // 3. القلقلة (أحرف قطب جد ساكنة \u0652 أو \u06E1)
                if (c in qalqalahChars) {
                    val nextChar = if (i + 1 < text.length) text[i + 1] else ' '
                    if (nextChar == '\u0652' || nextChar == '\u06E1') {
                        val end = (i + 2).coerceAtMost(text.length)
                        addStyle(
                            SpanStyle(color = colorQalqalah, fontWeight = FontWeight.Bold),
                            i,
                            end
                        )
                    }
                }
            }
        }
    }
}
