package com.sakinah.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH ARCHITECTURAL SHAPES SYSTEM
//  أشكال متباينة هندسياً لكل نمط تصميمي (Classic, Modern, Oasis, etc.)
// ═══════════════════════════════════════════════════════════════════

/**
 * 1. المصحف الملكي الكلاسيكي (Royal Madinah): زوايا 16dp
 */
val RoyalMadinahShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * 2. الزمرد العصري (Modern Emerald): زوايا فائقة النعومة 22dp
 */
val ModernEmeraldShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(22.dp),
    large      = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * 3. الواحة الهادئة (Calm Oasis): كبسولية ناعمة 26dp
 */
val CalmOasisShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(14.dp),
    medium     = RoundedCornerShape(26.dp),
    large      = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/**
 * 4. المخطوطة الأندلسية (Andalusian Manuscript): زوايا هندسية 10dp
 */
val AndalusianShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small      = RoundedCornerShape(6.dp),
    medium     = RoundedCornerShape(10.dp),
    large      = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(18.dp),
)

/**
 * 5. سكون الليل الأزرق (Midnight Sapphire): زوايا متوازنة 18dp
 */
val MidnightSapphireShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(10.dp),
    medium     = RoundedCornerShape(18.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * 6. وضع الخشوع المطلق (OLED Pure Black): زوايا 14dp مع إطارات حادة
 */
val OledBlackShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(14.dp),
    large      = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

val SakinahShapes = RoyalMadinahShapes
