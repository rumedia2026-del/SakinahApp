package com.sakinah.app.core.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH THEME SYSTEM — 6 Comprehensive Academic Themes
// ═══════════════════════════════════════════════════════════════════

enum class SakinahThemeMode {
    /** 1. المصحف الملكي الكلاسيكي (Royal Madinah) */
    WARM_PAPER,
    /** 2. الزمرد العصري (Modern Emerald) */
    MODERN_EMERALD,
    /** 3. الواحة الهادئة (Calm Oasis) */
    CALM_OASIS,
    /** 4. المخطوطة الأندلسية (Andalusian Manuscript) */
    ANDALUSIAN,
    /** 5. سكون الليل الأزرق (Midnight Sapphire) */
    SOFT_SLATE_DARK,
    /** 6. وضع الخشوع المطلق (OLED Pure Black) */
    PURE_OLED_BLACK,
    /** ألوان ديناميكية Material You */
    MATERIAL_YOU;

    val isDark: Boolean get() = this == SOFT_SLATE_DARK || this == PURE_OLED_BLACK
}

@Immutable
data class SakinahExtendedColors(
    val goldAccent: Color,
    val heroCardBackground: Color,
    val tasbihProgress: Color,
    val zenBackground: Color,
    val zenTextColor: Color,
)

val LocalSakinahThemeMode = compositionLocalOf { SakinahThemeMode.WARM_PAPER }

val LocalSakinahExtendedColors = staticCompositionLocalOf {
    royalMadinahExtendedColors
}

val LocalFontSizeMultiplier = compositionLocalOf { 1f }

val royalMadinahExtendedColors = SakinahExtendedColors(
    goldAccent          = RoyalMadinahColors.Tertiary,
    heroCardBackground  = Color(0xFFE8F5E9),
    tasbihProgress      = RoyalMadinahColors.Primary,
    zenBackground       = RoyalMadinahColors.Background,
    zenTextColor        = RoyalMadinahColors.OnBackground,
)

val modernEmeraldExtendedColors = SakinahExtendedColors(
    goldAccent          = ModernEmeraldColors.Tertiary,
    heroCardBackground  = Color(0xFFE0F7EA),
    tasbihProgress      = ModernEmeraldColors.Primary,
    zenBackground       = ModernEmeraldColors.Background,
    zenTextColor        = ModernEmeraldColors.OnBackground,
)

val calmOasisExtendedColors = SakinahExtendedColors(
    goldAccent          = CalmOasisColors.Tertiary,
    heroCardBackground  = Color(0xFFDCEEE7),
    tasbihProgress      = CalmOasisColors.Primary,
    zenBackground       = CalmOasisColors.Background,
    zenTextColor        = CalmOasisColors.OnBackground,
)

val andalusianExtendedColors = SakinahExtendedColors(
    goldAccent          = AndalusianColors.Tertiary,
    heroCardBackground  = Color(0xFFF7EBD7),
    tasbihProgress      = AndalusianColors.Primary,
    zenBackground       = AndalusianColors.Background,
    zenTextColor        = AndalusianColors.OnBackground,
)

val midnightSapphireExtendedColors = SakinahExtendedColors(
    goldAccent          = MidnightSapphireColors.Tertiary,
    heroCardBackground  = Color(0xFF1E293B),
    tasbihProgress      = MidnightSapphireColors.Primary,
    zenBackground       = MidnightSapphireColors.Background,
    zenTextColor        = MidnightSapphireColors.OnBackground,
)

val oledBlackExtendedColors = SakinahExtendedColors(
    goldAccent          = OledBlackColors.Tertiary,
    heroCardBackground  = Color(0xFF141414),
    tasbihProgress      = OledBlackColors.Primary,
    zenBackground       = OledBlackColors.Background,
    zenTextColor        = OledBlackColors.OnBackground,
)

@Composable
fun SakinahTheme(
    themeMode: SakinahThemeMode = SakinahThemeMode.WARM_PAPER,
    fontSizeMultiplier: Float = 1f,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colorScheme = when (themeMode) {
        SakinahThemeMode.WARM_PAPER       -> RoyalMadinahColorScheme
        SakinahThemeMode.MODERN_EMERALD   -> ModernEmeraldColorScheme
        SakinahThemeMode.CALM_OASIS       -> CalmOasisColorScheme
        SakinahThemeMode.ANDALUSIAN       -> AndalusianColorScheme
        SakinahThemeMode.SOFT_SLATE_DARK  -> MidnightSapphireColorScheme
        SakinahThemeMode.PURE_OLED_BLACK  -> OledBlackColorScheme
        SakinahThemeMode.MATERIAL_YOU     -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (themeMode.isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                RoyalMadinahColorScheme
            }
        }
    }

    val shapes: Shapes = when (themeMode) {
        SakinahThemeMode.WARM_PAPER       -> RoyalMadinahShapes
        SakinahThemeMode.MODERN_EMERALD   -> ModernEmeraldShapes
        SakinahThemeMode.CALM_OASIS       -> CalmOasisShapes
        SakinahThemeMode.ANDALUSIAN       -> AndalusianShapes
        SakinahThemeMode.SOFT_SLATE_DARK  -> MidnightSapphireShapes
        SakinahThemeMode.PURE_OLED_BLACK  -> OledBlackShapes
        SakinahThemeMode.MATERIAL_YOU     -> RoyalMadinahShapes
    }

    val extendedColors = when (themeMode) {
        SakinahThemeMode.WARM_PAPER       -> royalMadinahExtendedColors
        SakinahThemeMode.MODERN_EMERALD   -> modernEmeraldExtendedColors
        SakinahThemeMode.CALM_OASIS       -> calmOasisExtendedColors
        SakinahThemeMode.ANDALUSIAN       -> andalusianExtendedColors
        SakinahThemeMode.SOFT_SLATE_DARK  -> midnightSapphireExtendedColors
        SakinahThemeMode.PURE_OLED_BLACK  -> oledBlackExtendedColors
        SakinahThemeMode.MATERIAL_YOU     -> royalMadinahExtendedColors
    }

    val typography = sakinahTypography(fontSizeMultiplier)

    CompositionLocalProvider(
        LocalSakinahThemeMode provides themeMode,
        LocalSakinahExtendedColors provides extendedColors,
        LocalFontSizeMultiplier provides fontSizeMultiplier,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = typography,
            shapes      = shapes,
            content     = content,
        )
    }
}

val MaterialTheme.sakinahColors: SakinahExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSakinahExtendedColors.current

val MaterialTheme.fontSizeMultiplier: Float
    @Composable
    @ReadOnlyComposable
    get() = LocalFontSizeMultiplier.current

val MaterialTheme.sakinahThemeMode: SakinahThemeMode
    @Composable
    @ReadOnlyComposable
    get() = LocalSakinahThemeMode.current
