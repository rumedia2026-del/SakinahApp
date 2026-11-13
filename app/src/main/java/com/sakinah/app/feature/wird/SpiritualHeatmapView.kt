package com.sakinah.app.feature.wird

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.data.local.db.entities.DailyProgressEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpiritualHeatmapView(
    progressList: List<DailyProgressEntity>,
    modifier: Modifier = Modifier
) {
    // 52 أسبوعاً (365 يوماً)
    val progressMap = remember(progressList) {
        progressList.associate { it.date to (it.totalTasbihCount + it.completedAzkarIds.split(",").filter { id -> id.isNotBlank() }.size) }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
    val calendar = remember { Calendar.getInstance() }

    // توليد مصفوفة لآخر 52 أسبوعاً (7 صفوف × 52 عمود)
    val weeksData = remember(progressMap) {
        val weeks = mutableListOf<List<Int>>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -364)

        for (w in 0 until 52) {
            val weekDays = mutableListOf<Int>()
            for (d in 0 until 7) {
                val dateStr = dateFormat.format(cal.time)
                val score = progressMap[dateStr] ?: 0
                weekDays.add(score)
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            weeks.add(weekDays)
        }
        weeks
    }

    val scrollState = rememberScrollState(Int.MAX_VALUE) // يبدأ بالتمرير لليمين (الأيام الحالية)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "خريطة الالتزام الروحاني السنوية (٣٦٥ يوماً)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("أقل", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf(
                    Color(0xFFE0E0E0),
                    Color(0xFFA5D6A7),
                    Color(0xFF66BB6A),
                    Color(0xFF2E7D32),
                    Color(0xFF1B5E20)
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                Text("أكثر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            weeksData.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { score ->
                        val color = when {
                            score == 0 -> Color(0xFFE0E0E0).copy(alpha = 0.6f)
                            score < 5 -> Color(0xFFA5D6A7)
                            score < 15 -> Color(0xFF66BB6A)
                            score < 30 -> Color(0xFF2E7D32)
                            else -> Color(0xFF1B5E20)
                        }
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}
