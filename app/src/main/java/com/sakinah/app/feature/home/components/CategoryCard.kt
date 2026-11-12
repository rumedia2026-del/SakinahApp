package com.sakinah.app.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.data.local.db.entities.CategoryEntity

@Composable
fun CategoryCard(
    category: CategoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emoji = when (category.id) {
        1 -> "🌅"
        2 -> "🌇"
        3 -> "🕌"
        4 -> "🌙"
        5 -> "⏰"
        6 -> "💧"
        7 -> "🕋"
        8 -> "🏡"
        9 -> "🍽️"
        10 -> "✈️"
        11 -> "💚"
        12 -> "🛡️"
        13 -> "📖"
        else -> "✨"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.titleAr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (category.recommendedTimeStart != "00:00" || category.recommendedTimeEnd != "23:59") {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "الوقت: ${category.recommendedTimeStart} - ${category.recommendedTimeEnd}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
