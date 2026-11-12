package com.sakinah.app.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH COLOR SYSTEM — 4 Carefully Crafted Themes
//  مبني على معيار WCAG AA (تباين 4.5:1 على الأقل)
// ═══════════════════════════════════════════════════════════════════

// ── 1. Warm Paper Theme (الورقي الدافئ) ──────────────────────────
object WarmPaperColors {
    val Background       = Color(0xFFFAF8F5)   // خلفية الورق الدافئ
    val Surface          = Color(0xFFF5F0EA)   // سطح البطاقات
    val SurfaceVariant   = Color(0xFFEDE7DC)
    val OnBackground     = Color(0xFF1E242B)   // نص داكن غير حاد
    val OnSurface        = Color(0xFF2C3540)
    val OnSurfaceVariant = Color(0xFF5A6472)

    val Primary          = Color(0xFF1B5E20)   // أخضر زمردي هادئ
    val PrimaryContainer = Color(0xFFD4EDDA)
    val OnPrimary        = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF0A3D10)

    val Secondary        = Color(0xFF4A7C59)
    val SecondaryContainer = Color(0xFFCCE8D4)
    val OnSecondary      = Color(0xFFFFFFFF)

    val Tertiary         = Color(0xFF8D6E37)   // ذهبي دافئ للتمييز
    val TertiaryContainer = Color(0xFFF5E6C8)
    val OnTertiary       = Color(0xFFFFFFFF)

    val Error            = Color(0xFFB71C1C)
    val ErrorContainer   = Color(0xFFFFDAD6)
    val OnError          = Color(0xFFFFFFFF)

    val Outline          = Color(0xFFBFB8AE)
    val OutlineVariant   = Color(0xFFD6CECC)

    val Scrim            = Color(0xFF000000)
}

// ── 2. Soft Slate Dark Theme (الليلي المريح) ──────────────────────
object SoftSlateDarkColors {
    val Background       = Color(0xFF121619)   // خلفية داكنة مزرقة
    val Surface          = Color(0xFF1A1E24)
    val SurfaceVariant   = Color(0xFF252B33)
    val OnBackground     = Color(0xFFE8EDF2)   // نص فاتح مريح
    val OnSurface        = Color(0xFFDDE3E9)
    val OnSurfaceVariant = Color(0xFF9BA5B0)

    val Primary          = Color(0xFF4CAF50)   // أخضر أفتح للوضع الداكن
    val PrimaryContainer = Color(0xFF1B5E20)
    val OnPrimary        = Color(0xFF000000)
    val OnPrimaryContainer = Color(0xFFB7F0BB)

    val Secondary        = Color(0xFF72B080)
    val SecondaryContainer = Color(0xFF2D5E38)
    val OnSecondary      = Color(0xFF000000)

    val Tertiary         = Color(0xFFD4A843)   // ذهبي للوضع الداكن
    val TertiaryContainer = Color(0xFF5C4315)
    val OnTertiary       = Color(0xFF000000)

    val Error            = Color(0xFFEF5350)
    val ErrorContainer   = Color(0xFF8C1A18)
    val OnError          = Color(0xFF000000)

    val Outline          = Color(0xFF3A4350)
    val OutlineVariant   = Color(0xFF2D3540)

    val Scrim            = Color(0xFF000000)
}

// ── 3. Pure OLED Black Theme (الأسود النقي) ──────────────────────
object OledBlackColors {
    val Background       = Color(0xFF000000)   // أسود نقي لشاشات OLED
    val Surface          = Color(0xFF0A0A0A)
    val SurfaceVariant   = Color(0xFF141414)
    val OnBackground     = Color(0xFFF5F5F5)   // أبيض ناصع
    val OnSurface        = Color(0xFFEEEEEE)
    val OnSurfaceVariant = Color(0xFF9E9E9E)

    val Primary          = Color(0xFF66BB6A)   // أخضر فاتح للأسود
    val PrimaryContainer = Color(0xFF1A3D1E)
    val OnPrimary        = Color(0xFF000000)
    val OnPrimaryContainer = Color(0xFFC8F5CC)

    val Secondary        = Color(0xFF81C784)
    val SecondaryContainer = Color(0xFF1E3D22)
    val OnSecondary      = Color(0xFF000000)

    val Tertiary         = Color(0xFFFFD54F)   // أصفر ذهبي للأسود
    val TertiaryContainer = Color(0xFF3D3000)
    val OnTertiary       = Color(0xFF000000)

    val Error            = Color(0xFFEF9A9A)
    val ErrorContainer   = Color(0xFF3D0F0E)
    val OnError          = Color(0xFF000000)

    val Outline          = Color(0xFF2A2A2A)
    val OutlineVariant   = Color(0xFF1A1A1A)

    val Scrim            = Color(0xFF000000)
}

// ── Color Schemes (Material3) ─────────────────────────────────────

val WarmPaperLightColorScheme = lightColorScheme(
    primary                = WarmPaperColors.Primary,
    onPrimary              = WarmPaperColors.OnPrimary,
    primaryContainer       = WarmPaperColors.PrimaryContainer,
    onPrimaryContainer     = WarmPaperColors.OnPrimaryContainer,
    secondary              = WarmPaperColors.Secondary,
    onSecondary            = WarmPaperColors.OnSecondary,
    secondaryContainer     = WarmPaperColors.SecondaryContainer,
    onSecondaryContainer   = WarmPaperColors.OnSurface,
    tertiary               = WarmPaperColors.Tertiary,
    onTertiary             = WarmPaperColors.OnTertiary,
    tertiaryContainer      = WarmPaperColors.TertiaryContainer,
    onTertiaryContainer    = WarmPaperColors.OnBackground,
    error                  = WarmPaperColors.Error,
    errorContainer         = WarmPaperColors.ErrorContainer,
    onError                = WarmPaperColors.OnError,
    onErrorContainer       = WarmPaperColors.OnBackground,
    background             = WarmPaperColors.Background,
    onBackground           = WarmPaperColors.OnBackground,
    surface                = WarmPaperColors.Surface,
    onSurface              = WarmPaperColors.OnSurface,
    surfaceVariant         = WarmPaperColors.SurfaceVariant,
    onSurfaceVariant       = WarmPaperColors.OnSurfaceVariant,
    outline                = WarmPaperColors.Outline,
    outlineVariant         = WarmPaperColors.OutlineVariant,
    scrim                  = WarmPaperColors.Scrim,
)

val SoftSlateDarkColorScheme = darkColorScheme(
    primary                = SoftSlateDarkColors.Primary,
    onPrimary              = SoftSlateDarkColors.OnPrimary,
    primaryContainer       = SoftSlateDarkColors.PrimaryContainer,
    onPrimaryContainer     = SoftSlateDarkColors.OnPrimaryContainer,
    secondary              = SoftSlateDarkColors.Secondary,
    onSecondary            = SoftSlateDarkColors.OnSecondary,
    secondaryContainer     = SoftSlateDarkColors.SecondaryContainer,
    onSecondaryContainer   = SoftSlateDarkColors.OnBackground,
    tertiary               = SoftSlateDarkColors.Tertiary,
    onTertiary             = SoftSlateDarkColors.OnTertiary,
    tertiaryContainer      = SoftSlateDarkColors.TertiaryContainer,
    onTertiaryContainer    = SoftSlateDarkColors.OnBackground,
    error                  = SoftSlateDarkColors.Error,
    errorContainer         = SoftSlateDarkColors.ErrorContainer,
    onError                = SoftSlateDarkColors.OnError,
    onErrorContainer       = SoftSlateDarkColors.OnBackground,
    background             = SoftSlateDarkColors.Background,
    onBackground           = SoftSlateDarkColors.OnBackground,
    surface                = SoftSlateDarkColors.Surface,
    onSurface              = SoftSlateDarkColors.OnSurface,
    surfaceVariant         = SoftSlateDarkColors.SurfaceVariant,
    onSurfaceVariant       = SoftSlateDarkColors.OnSurfaceVariant,
    outline                = SoftSlateDarkColors.Outline,
    outlineVariant         = SoftSlateDarkColors.OutlineVariant,
    scrim                  = SoftSlateDarkColors.Scrim,
)

val OledBlackColorScheme = darkColorScheme(
    primary                = OledBlackColors.Primary,
    onPrimary              = OledBlackColors.OnPrimary,
    primaryContainer       = OledBlackColors.PrimaryContainer,
    onPrimaryContainer     = OledBlackColors.OnPrimaryContainer,
    secondary              = OledBlackColors.Secondary,
    onSecondary            = OledBlackColors.OnSecondary,
    secondaryContainer     = OledBlackColors.SecondaryContainer,
    onSecondaryContainer   = OledBlackColors.OnBackground,
    tertiary               = OledBlackColors.Tertiary,
    onTertiary             = OledBlackColors.OnTertiary,
    tertiaryContainer      = OledBlackColors.TertiaryContainer,
    onTertiaryContainer    = OledBlackColors.OnBackground,
    error                  = OledBlackColors.Error,
    errorContainer         = OledBlackColors.ErrorContainer,
    onError                = OledBlackColors.OnError,
    onErrorContainer       = OledBlackColors.OnBackground,
    background             = OledBlackColors.Background,
    onBackground           = OledBlackColors.OnBackground,
    surface                = OledBlackColors.Surface,
    onSurface              = OledBlackColors.OnSurface,
    surfaceVariant         = OledBlackColors.SurfaceVariant,
    onSurfaceVariant       = OledBlackColors.OnSurfaceVariant,
    outline                = OledBlackColors.Outline,
    outlineVariant         = OledBlackColors.OutlineVariant,
    scrim                  = OledBlackColors.Scrim,
)
