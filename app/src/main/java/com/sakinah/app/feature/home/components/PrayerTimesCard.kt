package com.sakinah.app.feature.home.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.feature.prayer.PrayerScheduleState
import com.sakinah.app.feature.prayer.PrayerTimesHelper
import kotlinx.coroutines.delay

import androidx.compose.foundation.clickable

@Composable
fun PrayerTimesCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    var state by remember { mutableStateOf(PrayerTimesHelper.calculateTodayPrayers()) }

    // تحديث العد التنازلي التفاعلي كل دقيقة
    LaunchedEffect(Unit) {
        while (true) {
            state = PrayerTimesHelper.calculateTodayPrayers()
            delay(60000)
        }
    }

    // تصميم إسلامي فاخر: أخضر زمردي عميق مع لمسات ذهبية دافئة
    val deepEmerald = Color(0xFF0F3D2E)
    val goldAccent = Color(0xFFC8A951)
    val lightGold = Color(0xFFE8D399)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .border(1.dp, goldAccent.copy(alpha = 0.4f), MaterialTheme.shapes.large)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = deepEmerald),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // الترويسة: المدينة والتاريخ والعد التنازلي الحي
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = goldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.cityName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // شارة الصلاة القادمة والعد التنازلي
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(goldAccent.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = lightGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.nextPrayerName}: ${state.countdownFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = lightGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = state.gregorianDateFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = goldAccent.copy(alpha = 0.25f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // شريط مواقيت الصلوات الخمس والشروق
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.prayerList.forEach { prayerItem ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = if (prayerItem.isNext) {
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(goldAccent.copy(alpha = 0.3f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        } else {
                            Modifier
                        }
                    ) {
                        Text(
                            text = prayerItem.nameAr,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (prayerItem.isNext) FontWeight.Bold else FontWeight.Normal,
                            color = if (prayerItem.isNext) lightGold else Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = prayerItem.formattedTime,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = if (prayerItem.isNext) FontWeight.Bold else FontWeight.Normal,
                            color = if (prayerItem.isNext) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
