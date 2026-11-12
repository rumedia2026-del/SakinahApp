package com.sakinah.app.core.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

// ═══════════════════════════════════════════════════════════════════
//  HAPTIC FEEDBACK EXTENSIONS
//  استجابة لمسية مدروسة ومتوافقة مع جميع إصدارات الأندرويد
// ═══════════════════════════════════════════════════════════════════

/** الحصول على View الحالي لتنفيذ الاستجابة اللمسية */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return HapticFeedback(view)
}

/**
 * مغلف للاستجابة اللمسية مع أنماط متعددة.
 */
class HapticFeedback(private val view: View) {

    /** اهتزاز خفيف عند نقرة عادية */
    fun tick() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /**
     * اهتزاز عند كل تسبيحة (لمسة على المسبحة)
     * نمط خفيف وسريع
     */
    fun tasbihTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    /**
     * اهتزاز عند الوصول لـ 33 أو 66
     * نمط متوسط ومميز
     */
    fun milestone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * اهتزاز عند إتمام الذكر (99 أو الهدف)
     * نمط قوي ومُبهج
     */
    fun completion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /** اهتزاز عند الانتقال للذكر التالي/السابق */
    fun swipe() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}
