package com.sakinah.app.feature.prayer

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.core.ui.theme.SakinahTheme

class AdhanAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ضبط الشاشة للظهور فوق القفل وتشغيل الشاشة
        setupLockscreenFlags()

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"
        val muezzinName = intent.getStringExtra(EXTRA_MUEZZIN_NAME) ?: "المؤذن"

        setContent {
            SakinahTheme {
                AdhanAlertScreen(
                    prayerName = prayerName,
                    muezzinName = muezzinName,
                    onStopClick = {
                        stopAdhanAndFinish()
                    }
                )
            }
        }
    }

    private fun setupLockscreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
    }

    private fun stopAdhanAndFinish() {
        val stopIntent = Intent(this, AdhanForegroundService::class.java).apply {
            action = AdhanForegroundService.ACTION_STOP_ADHAN
        }
        startService(stopIntent)
        finishAndRemoveTask()
    }

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_alert_prayer_name"
        const val EXTRA_MUEZZIN_NAME = "extra_alert_muezzin_name"
    }
}

@Composable
fun AdhanAlertScreen(
    prayerName: String,
    muezzinName: String,
    onStopClick: () -> Unit
) {
    val emeraldPrimary = Color(0xFF0F3D2E)
    val goldAccent = Color(0xFFC8A951)
    val deepNavy = Color(0xFF0A1F18)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(deepNavy, emeraldPrimary, deepNavy)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // أيقونة المسجد البارزة
            Surface(
                shape = CircleShape,
                color = goldAccent.copy(alpha = 0.15f),
                modifier = Modifier.size(110.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = goldAccent,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "الله أكبر • الله أكبر",
                style = MaterialTheme.typography.titleLarge,
                color = goldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "حان الآن موعد أذان $prayerName",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "صوت: $muezzinName",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "حيّ على الصلاة • حيّ على الفلاح\nأسبغ الوضوء وأقبل إلى ربك بقلبٍ حاضر",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // زر أحمر كبير: إيقاف الأذان
            Button(
                onClick = onStopClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "إيقاف الأذان ⏹",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
