package com.sakinah.app.core.utils

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    val context = LocalContext.current
    return HapticFeedback(view, context)
}

class HapticFeedback(
    private val view: View,
    private val context: Context
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun tick() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /**
     * نقرة عادية للتسبيح
     * في وضع فاقدي السمع (deafMode = true): نبضة ملموسة واضحة وممتعة
     */
    fun tasbihTap(deafMode: Boolean = false) {
        if (deafMode) {
            vibrateCustom(timings = longArrayOf(0, 45), amplitudes = intArrayOf(0, 180))
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
    }

    /**
     * الوصول لنقطة مرحلية (33 أو 66)
     * نبضتان متتاليتان مميزتان
     */
    fun milestone(deafMode: Boolean = false) {
        if (deafMode) {
            vibrateCustom(
                timings = longArrayOf(0, 70, 70, 70),
                amplitudes = intArrayOf(0, 220, 0, 220)
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    /**
     * إتمام الورد أو الوصول لـ 100
     * نمط ثلاثي قوي ومبهج يشعر به فاقد السمع بوضوح تام
     */
    fun completion(deafMode: Boolean = false) {
        if (deafMode) {
            vibrateCustom(
                timings = longArrayOf(0, 90, 60, 90, 60, 140),
                amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    fun swipe() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    /**
     * نمط اهتزاز مخصص لدخول وقت الصلاة لفاقدي السمع
     * @param prayerIndex: 0=الفجر(نبضتان طويلتان), 1=الشروق, 2=الظهر(4 نبضات), 3=العصر(3 نبضات), 4=المغرب, 5=العشاء
     */
    fun prayerAlarmVibration(prayerIndex: Int) {
        val (timings, amplitudes) = when (prayerIndex) {
            0 -> longArrayOf(0, 400, 200, 400) to intArrayOf(0, 255, 0, 255) // الفجر
            2 -> longArrayOf(0, 150, 100, 150, 100, 150, 100, 150) to intArrayOf(0, 220, 0, 220, 0, 220, 0, 220) // الظهر 4
            3 -> longArrayOf(0, 200, 120, 200, 120, 200) to intArrayOf(0, 240, 0, 240, 0, 240) // العصر 3
            4 -> longArrayOf(0, 300, 120, 300, 100, 150) to intArrayOf(0, 255, 0, 255, 0, 200) // المغرب
            else -> longArrayOf(0, 200, 100, 200, 100, 200, 100, 300) to intArrayOf(0, 230, 0, 230, 0, 230, 0, 255) // العشاء
        }
        vibrateCustom(timings, amplitudes)
    }

    private fun vibrateCustom(timings: LongArray, amplitudes: IntArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (e: Exception) {
            // fallback
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}
