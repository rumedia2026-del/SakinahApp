package com.sakinah.app.feature.quran

import androidx.compose.ui.graphics.Color

data class QuranTheme(
    val id: Int,
    val nameAr: String,
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val accentColor: Color
)

object QuranThemeManager {
    val themes = listOf(
        QuranTheme(
            id = 0,
            nameAr = "الورقي الدافئ",
            backgroundColor = Color(0xFFFDFBF7),
            textColor = Color(0xFF1B2E24),
            borderColor = Color(0xFFC8A951),
            accentColor = Color(0xFFB8860B)
        ),
        QuranTheme(
            id = 1,
            nameAr = "المصحف الأبيض",
            backgroundColor = Color(0xFFFFFFFF),
            textColor = Color(0xFF111111),
            borderColor = Color(0xFF0F3D2E),
            accentColor = Color(0xFF0F3D2E)
        ),
        QuranTheme(
            id = 2,
            nameAr = "الأخضر الزمردي",
            backgroundColor = Color(0xFFF2F7F4),
            textColor = Color(0xFF0A2B20),
            borderColor = Color(0xFF2E7D32),
            accentColor = Color(0xFF1B5E20)
        ),
        QuranTheme(
            id = 3,
            nameAr = "الليلي Sepia",
            backgroundColor = Color(0xFF2D241E),
            textColor = Color(0xFFE8DCB8),
            borderColor = Color(0xFFC8A951),
            accentColor = Color(0xFFD4AF37)
        ),
        QuranTheme(
            id = 4,
            nameAr = "الأزرق الليلي",
            backgroundColor = Color(0xFF0F1A24),
            textColor = Color(0xFFD8E4EE),
            borderColor = Color(0xFF3A6E99),
            accentColor = Color(0xFF64B5F6)
        ),
        QuranTheme(
            id = 5,
            nameAr = "الوضع المظلم الملكي",
            backgroundColor = Color(0xFF121417),
            textColor = Color(0xFFF5F0E6),
            borderColor = Color(0xFFFCD34D),
            accentColor = Color(0xFFFCD34D)
        )
    )

    fun getTheme(id: Int): QuranTheme {
        return themes.getOrNull(id) ?: themes[0]
    }
}
