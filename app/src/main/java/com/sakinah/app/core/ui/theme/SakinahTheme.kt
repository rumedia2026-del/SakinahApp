package com.sakinah.app.core.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH THEME SYSTEM
//  نظام الثيمات الكامل للتطبيق — 4 ثيمات مدروسة
// ═══════════════════════════════════════════════════════════════════

/**
 * أنواع الثيمات المتاحة في التطبيق.
 */
enum class SakinahThemeMode {
    /** الورقي الدافئ — افتراضي، مريح للعين نهاراً */
    WARM_PAPER,
    /** الليلي الهادئ — رمادي داكن مزرق للقراءة الليلية */
    SOFT_SLATE_DARK,
    /** الأسود النقي — OLED لتوفير البطارية */
    PURE_OLED_BLACK,
    /** ألوان ديناميكية Material You — Android 12+ فقط */
    MATERIAL_YOU;

    /** هل هذا ثيم داكن؟ */
    val isDark: Boolean get() = this == SOFT_SLATE_DARK || this == PURE_OLED_BLACK
}

/**
 * بيانات الثيم الممتدة خارج نظام Material3 القياسي.
 * تحتوي على ألوان سكينة المخصصة.
 */
@Immutable
data class SakinahExtendedColors(
    /** لون الذهبي المستخدم للتمييز وعناصر الجمعة */
    val goldAccent: androidx.compose.ui.graphics.Color,
    /** خلفية بطاقة الذكر الذكية (Smart Hero Card) */
    val heroCardBackground: androidx.compose.ui.graphics.Color,
    /** لون شريط التقدم للمسبحة */
    val tasbihProgress: androidx.compose.ui.graphics.Color,
    /** لون خلفية وضع التركيز (Zen Mode) */
    val zenBackground: androidx.compose.ui.graphics.Color,
    /** لون النص في وضع التركيز */
    val zenTextColor: androidx.compose.ui.graphics.Color,
)

// ── Composition Locals ────────────────────────────────────────────

val LocalSakinahThemeMode = compositionLocalOf { SakinahThemeMode.WARM_PAPER }

val LocalSakinahExtendedColors = staticCompositionLocalOf {
    warmPaperExtendedColors
}

val LocalFontSizeMultiplier = compositionLocalOf { 1f }

// ── Extended Color Sets ───────────────────────────────────────────

val warmPaperExtendedColors = SakinahExtendedColors(
    goldAccent          = WarmPaperColors.Tertiary,
    heroCardBackground  = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
    tasbihProgress      = WarmPaperColors.Primary,
    zenBackground       = androidx.compose.ui.graphics.Color(0xFFFAF8F5),
    zenTextColor        = WarmPaperColors.OnBackground,
)

val softSlateDarkExtendedColors = SakinahExtendedColors(
    goldAccent          = SoftSlateDarkColors.Tertiary,
    heroCardBackground  = androidx.compose.ui.graphics.Color(0xFF1A2820),
    tasbihProgress      = SoftSlateDarkColors.Primary,
    zenBackground       = SoftSlateDarkColors.Background,
    zenTextColor        = SoftSlateDarkColors.OnBackground,
)

val oledBlackExtendedColors = SakinahExtendedColors(
    goldAccent          = OledBlackColors.Tertiary,
    heroCardBackground  = androidx.compose.ui.graphics.Color(0xFF0A0A0A),
    tasbihProgress      = OledBlackColors.Primary,
    zenBackground       = OledBlackColors.Background,
    zenTextColor        = OledBlackColors.OnBackground,
)

// ── Main Theme Composable ─────────────────────────────────────────

/**
 * الثيم الرئيسي لتطبيق سكينة.
 *
 * @param themeMode       الثيم المختار من [SakinahThemeMode]
 * @param fontSizeMultiplier  معامل حجم الخط (0.8f - 1.4f)
 * @param content         محتوى Compose
 */
@Composable
fun SakinahTheme(
    themeMode: SakinahThemeMode = SakinahThemeMode.WARM_PAPER,
    fontSizeMultiplier: Float = 1f,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // اختيار مجموعة الألوان بناءً على الثيم
    val colorScheme = when (themeMode) {
        SakinahThemeMode.WARM_PAPER      -> WarmPaperLightColorScheme
        SakinahThemeMode.SOFT_SLATE_DARK -> SoftSlateDarkColorScheme
        SakinahThemeMode.PURE_OLED_BLACK -> OledBlackColorScheme
        SakinahThemeMode.MATERIAL_YOU    -> {
            // Material You: متاح فقط على Android 12 (S) وما فوق
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (themeMode.isDark) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            } else {
                // Fallback للأجهزة القديمة: Warm Paper
                WarmPaperLightColorScheme
            }
        }
    }

    // الألوان الممتدة حسب الثيم
    val extendedColors = when (themeMode) {
        SakinahThemeMode.WARM_PAPER      -> warmPaperExtendedColors
        SakinahThemeMode.SOFT_SLATE_DARK -> softSlateDarkExtendedColors
        SakinahThemeMode.PURE_OLED_BLACK -> oledBlackExtendedColors
        SakinahThemeMode.MATERIAL_YOU    -> warmPaperExtendedColors   // Material You يتبع الورقي
    }

    // الطباعة بمعامل الحجم المختار
    val typography = sakinahTypography(fontSizeMultiplier)

    CompositionLocalProvider(
        LocalSakinahThemeMode provides themeMode,
        LocalSakinahExtendedColors provides extendedColors,
        LocalFontSizeMultiplier provides fontSizeMultiplier,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = typography,
            shapes      = SakinahShapes,
            content     = content,
        )
    }
}

// ── Convenience Accessors ─────────────────────────────────────────

/** وصول سريع لألوان سكينة الممتدة من أي Composable */
val MaterialTheme.sakinahColors: SakinahExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSakinahExtendedColors.current

/** وصول سريع لمعامل حجم الخط الحالي */
val MaterialTheme.fontSizeMultiplier: Float
    @Composable
    @ReadOnlyComposable
    get() = LocalFontSizeMultiplier.current

/** وصول سريع لنوع الثيم الحالي */
val MaterialTheme.sakinahThemeMode: SakinahThemeMode
    @Composable
    @ReadOnlyComposable
    get() = LocalSakinahThemeMode.current
