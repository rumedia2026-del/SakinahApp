package com.sakinah.app.core.navigation

// ═══════════════════════════════════════════════════════════════════
//  SAKINAH NAVIGATION ROUTES
//  تعريف جميع مسارات التنقل في التطبيق
// ═══════════════════════════════════════════════════════════════════

/**
 * مسارات التنقل الرئيسية.
 * كل مسار هو sealed class لأمان النوع (Type Safety).
 */
sealed class Screen(val route: String) {

    // ── الشاشات الرئيسية (Bottom Navigation) ──────────────────────
    /** الشاشة الرئيسية الذكية */
    data object Home : Screen("home")

    /** شاشة قائمة الأذكار بالتصنيف */
    data object AzkarList : Screen("azkar_list/{categoryId}") {
        fun createRoute(categoryId: Int) = "azkar_list/$categoryId"
    }

    /** شاشة تفاصيل ذكر واحد */
    data object ZikrDetail : Screen("zikr_detail/{zikrId}") {
        fun createRoute(zikrId: Int) = "zikr_detail/$zikrId"
    }

    /** وضع التركيز (Zen Mode) لقراءة الأذكار */
    data object ZenMode : Screen("zen_mode/{categoryId}") {
        fun createRoute(categoryId: Int) = "zen_mode/$categoryId"
    }

    /** المسبحة التفاعلية بالإيماءات */
    data object Tasbih : Screen("tasbih")

    /** دليل بماذا تشعر الآن */
    data object EmotionalCompanion : Screen("emotional_companion")

    /** نتائج الحالة الشعورية */
    data object EmotionalResults : Screen("emotional_results/{emotionKey}") {
        fun createRoute(emotionKey: String) = "emotional_results/$emotionKey"
    }

    /** وردي اليومي (Custom Wird Builder) */
    data object WirdBuilder : Screen("wird_builder")

    /** الإعدادات */
    data object Settings : Screen("settings")

    /** خريطة المواظبة (Heatmap) */
    data object Consistency : Screen("consistency")

    /** مواقيت الصلاة والقبلة */
    data object PrayerTimes : Screen("prayer_times")
}

/** عناصر شريط التنقل السفلي */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Tasbih,
    Screen.EmotionalCompanion,
    Screen.Settings,
)
