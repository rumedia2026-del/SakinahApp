package com.sakinah.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH SHAPE SYSTEM
//  شكل هادئ ومتسق يوحي بالسكينة والاتزان
// ═══════════════════════════════════════════════════════════════════

val SakinahShapes = Shapes(
    // بطاقات صغيرة (تاغات، شرائح)
    extraSmall = RoundedCornerShape(4.dp),
    // أزرار، حقول إدخال
    small      = RoundedCornerShape(8.dp),
    // بطاقات الأذكار المتوسطة
    medium     = RoundedCornerShape(16.dp),
    // بطاقة الذكر الكبيرة، الحاويات الرئيسية
    large      = RoundedCornerShape(24.dp),
    // BottomSheet، الحاويات الكبيرة جداً
    extraLarge = RoundedCornerShape(28.dp),
)
