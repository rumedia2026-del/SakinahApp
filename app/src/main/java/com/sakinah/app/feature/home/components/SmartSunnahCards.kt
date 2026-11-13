package com.sakinah.app.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * نافذة ما بين الأذان والإقامة الذكية
 */
@Composable
fun AdhanIqamaWindowCard(
    nextPrayerName: String,
    countdownText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F8E9) // خلفية خضراء هادئة
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ما بين الأذان والإقامة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2E7D32))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = countdownText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "«الدُّعَاءُ لَا يُرَدُّ بَيْنَ الأَذَانِ وَالإِقَامَةِ» — سنن أبي داود والترمذي (صحيح)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "اغتنم هذه الدقائق الفاضلة بالدعاء لنفسك وأهلك وسائر المسلمين؛ فإن أبواب السماء مفتوحة.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF33691E)
            )
        }
    }
}

/**
 * كرت "سُنة مهجورة كل يوم"
 */
@Composable
fun DailySunnahCard(
    modifier: Modifier = Modifier
) {
    val sunnahs = listOf(
        "صلاة ركعتين بعد الوضوء" to "قال ﷺ لبلال: «حَدِّثْنِي بِأَرْجَى عَمَلٍ عَمِلْتَهُ فِي الإِسْلاَمِ... فإني سَمِعْتُ دَفَّ نَعْلَيْكَ بَيْنَ يَدَيَّ فِي الجَنَّةِ» قال: ما توضأتُ إلا صليت ركعتين.",
        "التسمية والأكل باليمين ومما يليك" to "قال رسول الله ﷺ: «يا غلام سم الله، وكل بيمينك، وكل مما يليك».",
        "حمد الله بعد الفراغ من الطعام والشراب" to "قال ﷺ: «إن الله ليرضى عن العبد أن يأكل الأكلة فيحمده عليها، أو يشرب الشربة فيحمده عليها».",
        "المضمضة بعد شرب اللبن" to "عن ابن عباس أن النبي ﷺ شرب لبناً فمضمض وقال: «إن له دسماً».",
        "التبسم في وجوه الناس" to "قال ﷺ: «تبسمك في وجه أخيك لك صدقة».",
        "إفشاء السلام على من عرفت ومن لم تعرف" to "سئل النبي ﷺ: أي الإسلام خير؟ قال: «تُطْعِمُ الطَّعَامَ، وَتَقْرَأُ السَّلاَمَ عَلَى مَنْ عَرَفْتَ وَمَنْ لَمْ تَعْرِفْ»."
    )

    val currentDayIndex = (System.currentTimeMillis() / (1000 * 60 * 60 * 24) % sunnahs.size).toInt()
    val todaySunnah = sunnahs[currentDayIndex]

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFDE7) // خلفية عنبرية دافئة
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFF57F17),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سُنة نبوية مهجورة اليوم",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = todaySunnah.first,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = todaySunnah.second,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D4037)
            )
        }
    }
}
