package com.sakinah.app.feature.social

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SocialMediaZikrReminderCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(SocialMediaReminderManager.isEnabled(context)) }
    var selectedThreshold by remember { mutableIntStateOf(SocialMediaReminderManager.getThresholdMinutes(context)) }
    var hasPermission by remember { mutableStateOf(SocialMediaReminderManager.hasUsageAccessPermission(context)) }

    val emeraldPrimary = Color(0xFF0F3D2E)
    val goldAccent = Color(0xFFC8A951)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isEnabled) emeraldPrimary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Title + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) goldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (isEnabled) goldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "منبّه السوشيال ميديا الفكاهي 🌿",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = emeraldPrimary
                        )
                        Text(
                            text = "تذكير لطيف عند قضاء وقت طويل بالريلز والمواقع",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        isEnabled = checked
                        SocialMediaReminderManager.setEnabled(context, checked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = emeraldPrimary
                    )
                )
            }

            AnimatedVisibility(visible = isEnabled) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // Check Permission Banner
                    if (!hasPermission) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF3CD),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEEBA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFF856404),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "يتطلب إذن الوصول لبيانات الاستخدام",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "لقياس وقتك على تطبيقات التواصل وإرسال التنبيه الفكاهي بدقة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF856404),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF856404)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("تفعيل الصلاحية الآن", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Threshold Selector Chips
                    Text(
                        text = "التنبيه بعد قضاء:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val thresholds = listOf(15, 20, 30, 45)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        thresholds.forEach { min ->
                            val isSelected = (selectedThreshold == min)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedThreshold = min
                                        SocialMediaReminderManager.setThresholdMinutes(context, min)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) emeraldPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) emeraldPrimary else Color.Transparent
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$min دقيقة",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test notification button
                    OutlinedButton(
                        onClick = {
                            SocialMediaReminderManager.showWittyNotification(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = emeraldPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("جرّب نموذج التنبيه الفكاهي الآن ✨", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
