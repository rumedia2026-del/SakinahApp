package com.sakinah.app.feature.emotional.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun BreathingCalmDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // دورات التنفس: 0 = شهيق (4ث)، 1 = ثبات (2ث)، 2 = زفير (4ث)
    var phase by remember { mutableIntStateOf(0) }
    var secondsRemaining by remember { mutableIntStateOf(30) }

    val emeraldPrimary = Color(0xFF0F3D2E)
    val goldAccent = Color(0xFFC8A951)

    // تحريك المقياس بسلاسة
    val scale by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 1.45f // تمدد مع الشهيق
            1 -> 1.45f // ثبات
            else -> 1.0f // انكماش مع الزفير
        },
        animationSpec = tween(
            durationMillis = when (phase) {
                0 -> 4000
                1 -> 2000
                else -> 4000
            },
            easing = FastOutSlowInEasing
        ),
        label = "BreathingScale"
    )

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            // شهيق 4 ثوانٍ
            phase = 0
            delay(4000)
            secondsRemaining = (secondsRemaining - 4).coerceAtLeast(0)
            if (secondsRemaining <= 0) break

            // ثبات ثانيتان
            phase = 1
            delay(2000)
            secondsRemaining = (secondsRemaining - 2).coerceAtLeast(0)
            if (secondsRemaining <= 0) break

            // زفير 4 ثوانٍ
            phase = 2
            delay(4000)
            secondsRemaining = (secondsRemaining - 4).coerceAtLeast(0)
        }
        onDismiss()
    }

    val (phaseText, zikrInstruction) = when (phase) {
        0 -> "شـَـهـِـيـق (٤ ثوانٍ)" to "« سُبْحَانَ اللَّهِ »"
        1 -> "ثـَـبـَـات (ثانيتان)" to "استشعر معية الله وسكينته ✨"
        else -> "زَفـِـيـر (٤ ثوانٍ)" to "« الْحَمْدُ لِلَّهِ »"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "دائرة السكينة والتنفس بالذكر 🌿",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = emeraldPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "٣٠ ثانية لتهدئة النبض واستعادة الطمأنينة",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // الدائرة المتنفسة
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        goldAccent.copy(alpha = 0.35f),
                                        emeraldPrimary.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(2.dp, goldAccent.copy(alpha = 0.6f), CircleShape)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = phaseText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = emeraldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = zikrInstruction,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = goldAccent,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "متبقي: $secondsRemaining ثانية",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تخطي إلى الأدعية ➔", color = Color.White)
            }
        },
        modifier = modifier
    )
}
