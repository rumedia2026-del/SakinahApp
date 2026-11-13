package com.sakinah.app.feature.social

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.sakinah.app.data.local.db.SakinahDatabase
import com.sakinah.app.data.local.db.entities.DailyProgressEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SocialMediaOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val customMessage = intent?.getStringExtra(EXTRA_MESSAGE)
        showCenterPopup(customMessage)
        return START_NOT_STICKY
    }

    private fun showCenterPopup(customMsg: String?) {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val messageText = customMsg ?: SocialMediaReminderManager.WITTY_MESSAGES[
            Random.nextInt(SocialMediaReminderManager.WITTY_MESSAGES.size)
        ]

        // 1. حاوية الشاشة الكاملة لتظليل الخلفية بخفة
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#99000000")) // 60% Black scrim
            setPadding(40, 40, 40, 40)
            setOnClickListener {
                // النقر خارج البطاقة يغلقها
                closeOverlay()
            }
        }

        // 2. البطاقة المركزية الأنيقة (تصميم سكينة الزجاجي الفاخر)
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 44, 50, 44)
            val cardBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 48f
                setColor(Color.parseColor("#FDFBF7")) // الورق الدافئ العاجي
                setStroke(4, Color.parseColor("#C8A951")) // ذهبي إسلامي
            }
            background = cardBg
            elevation = 20f
            // منع إغلاق البطاقة عند النقر عليها بالداخل
            isClickable = true
            isFocusable = true
        }

        // الأيقونة والوسم العلوي
        val headerTv = TextView(this).apply {
            text = "🌿 استراحة سكينة وإيمان 🌿"
            setTextColor(Color.parseColor("#C8A951"))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }

        // نص التذكير الفكاهي البارز
        val msgTv = TextView(this).apply {
            text = messageText
            setTextColor(Color.parseColor("#0F3D2E")) // الزمردي العميق
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setLineSpacing(12f, 1.15f)
            setPadding(0, 8, 0, 32)
        }

        // زر التسبيح الأخضر الكبير "سبّحت +10 📿"
        val tasbihBtn = Button(this).apply {
            text = "سبّحت +١٠ 📿"
            setTextColor(Color.WHITE)
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 32f
                setColor(Color.parseColor("#0F3D2E"))
            }
            background = btnBg
            setPadding(40, 24, 40, 24)
            setOnClickListener {
                vibrateClick()
                addTasbihProgress(10)
                closeOverlay()
            }
        }

        // زر تذكير لاحقاً
        val laterBtn = TextView(this).apply {
            text = "تذكير لاحقاً"
            setTextColor(Color.parseColor("#666666"))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 4)
            setOnClickListener {
                closeOverlay()
            }
        }

        cardLayout.addView(headerTv)
        cardLayout.addView(msgTv)
        cardLayout.addView(tasbihBtn)
        cardLayout.addView(laterBtn)

        rootLayout.addView(cardLayout)
        overlayView = rootLayout

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun addTasbihProgress(count: Int) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val db = SakinahDatabase.getInstance(applicationContext, scope)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                db.progressDao().insertOrUpdateProgress(
                    DailyProgressEntity(
                        date = today,
                        completedAzkarIds = "",
                        totalTasbihCount = count,
                        completedCategories = ""
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun vibrateClick() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(45)
        }
    }

    private fun closeOverlay() {
        try {
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeOverlay()
    }

    companion object {
        const val EXTRA_MESSAGE = "extra_overlay_message"
    }
}
