package com.sakinah.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.sakinah.app.R

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH TYPOGRAPHY SYSTEM
//  استخدام Google Downloadable Fonts لتنزيل خطي Cairo و Amiri تلقائياً
//  مع توفير خطوط النظام كـ Fallback فوري لضمان عدم حدوث أي خطأ في البناء
// ═══════════════════════════════════════════════════════════════════

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val CairoFontName = GoogleFont("Cairo")
private val AmiriFontName = GoogleFont("Amiri")

/**
 * خط القاهرة للعناوين وعناصر الواجهة (Downloadable Font + Fallback FontFamily.SansSerif)
 */
val CairoFontFamily = FontFamily(
    Font(googleFont = CairoFontName, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = CairoFontName, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = CairoFontName, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = CairoFontName, fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

/**
 * خط أميري للأذكار والنصوص القرآنية (Downloadable Font + Fallback FontFamily.Serif)
 */
val AmiriFontFamily = FontFamily(
    Font(googleFont = AmiriFontName, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = AmiriFontName, fontProvider = googleFontProvider, weight = FontWeight.Bold)
)

/**
 * SakinahTypography: مقياس طباعي متكامل مبني على واجهة عربية أصيلة (RTL).
 *
 * @param baseFontSizeSp معامل تكبير/تصغير يتحكم فيه المستخدم (0.8f - 1.4f)
 */
fun sakinahTypography(baseFontSizeSp: Float = 1f): Typography {
    fun sp(size: Float) = (size * baseFontSizeSp).sp

    return Typography(
        // ── Display ──────────────────────────────────────────────
        displayLarge = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Bold,
            fontSize     = sp(57f),
            lineHeight   = sp(64f),
            letterSpacing = (-0.25).sp,
            textDirection = TextDirection.Rtl,
        ),
        displayMedium = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Bold,
            fontSize     = sp(45f),
            lineHeight   = sp(52f),
            textDirection = TextDirection.Rtl,
        ),
        displaySmall = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = sp(36f),
            lineHeight   = sp(44f),
            textDirection = TextDirection.Rtl,
        ),

        // ── Headline ─────────────────────────────────────────────
        headlineLarge = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = sp(32f),
            lineHeight   = sp(40f),
            textDirection = TextDirection.Rtl,
        ),
        headlineMedium = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = sp(28f),
            lineHeight   = sp(36f),
            textDirection = TextDirection.Rtl,
        ),
        headlineSmall = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = sp(24f),
            lineHeight   = sp(32f),
            textDirection = TextDirection.Rtl,
        ),

        // ── Title ────────────────────────────────────────────────
        titleLarge = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.SemiBold,
            fontSize     = sp(22f),
            lineHeight   = sp(28f),
            textDirection = TextDirection.Rtl,
        ),
        titleMedium = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = sp(16f),
            lineHeight   = sp(24f),
            letterSpacing = 0.15.sp,
            textDirection = TextDirection.Rtl,
        ),
        titleSmall = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = sp(14f),
            lineHeight   = sp(20f),
            letterSpacing = 0.1.sp,
            textDirection = TextDirection.Rtl,
        ),

        // ── Body ─────────────────────────────────────────────────
        // نص الذكر الرئيسي: خط أميري مع تباعد سطور مريح لعلامات التشكيل
        bodyLarge = TextStyle(
            fontFamily   = AmiriFontFamily,
            fontWeight   = FontWeight.Normal,
            fontSize     = sp(21f),
            lineHeight   = sp(38f),
            letterSpacing = 0.sp,
            textDirection = TextDirection.Rtl,
        ),
        bodyMedium = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Normal,
            fontSize     = sp(14f),
            lineHeight   = sp(22f),
            letterSpacing = 0.25.sp,
            textDirection = TextDirection.Rtl,
        ),
        bodySmall = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Normal,
            fontSize     = sp(12f),
            lineHeight   = sp(18f),
            letterSpacing = 0.4.sp,
            textDirection = TextDirection.Rtl,
        ),

        // ── Label ────────────────────────────────────────────────
        labelLarge = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = sp(14f),
            lineHeight   = sp(20f),
            letterSpacing = 0.1.sp,
            textDirection = TextDirection.Rtl,
        ),
        labelMedium = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = sp(12f),
            lineHeight   = sp(16f),
            letterSpacing = 0.5.sp,
            textDirection = TextDirection.Rtl,
        ),
        labelSmall = TextStyle(
            fontFamily   = CairoFontFamily,
            fontWeight   = FontWeight.Medium,
            fontSize     = sp(11f),
            lineHeight   = sp(16f),
            letterSpacing = 0.5.sp,
            textDirection = TextDirection.Rtl,
        ),
    )
}

/** Typography افتراضية بالحجم الطبيعي */
val SakinahTypography = sakinahTypography(1f)
