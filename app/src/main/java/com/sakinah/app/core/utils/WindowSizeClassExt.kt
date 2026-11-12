package com.sakinah.app.core.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

// ═══════════════════════════════════════════════════════════════════
//  WINDOW SIZE CLASS EXTENSIONS
//  أدوات مساعدة لتحديد نوع الجهاز وضبط التخطيط المتجاوب
// ═══════════════════════════════════════════════════════════════════

/**
 * هل الجهاز هاتف مدمج (Compact)؟
 * يشمل الهواتف الصغيرة والمتوسطة (عرض < 600dp)
 */
val WindowSizeClass.isCompact: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Compact

/**
 * هل الجهاز جهاز لوحي أو قابل للطي مفتوح؟
 * يشمل الأجهزة ذات العرض المتوسط أو الكبير (≥ 600dp)
 */
val WindowSizeClass.isTabletOrFoldable: Boolean
    get() = widthSizeClass != WindowWidthSizeClass.Compact

/**
 * هل يجب استخدام تخطيط المجلدين (Two-Pane)?
 * نعم للأجهزة اللوحية والهواتف القابلة للطي المفتوحة
 */
val WindowSizeClass.useTwoPaneLayout: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Expanded

/** عدد أعمدة شبكة الأقسام حسب حجم الشاشة */
val WindowSizeClass.categoryGridColumns: Int
    get() = when (widthSizeClass) {
        WindowWidthSizeClass.Compact  -> 2
        WindowWidthSizeClass.Medium   -> 3
        WindowWidthSizeClass.Expanded -> 4
        else                          -> 2
    }
