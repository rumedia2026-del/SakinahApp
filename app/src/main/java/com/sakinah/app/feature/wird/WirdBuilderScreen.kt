package com.sakinah.app.feature.wird

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakinah.app.core.navigation.Screen
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.feature.azkar.components.ZikrCardItem

private fun toArabicDigits(number: Int): String {
    val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { if (it in '0'..'9') arabicChars[it - '0'] else it }.joinToString("")
}

data class WorshipHabit(
    val id: String,
    val title: String,
    val category: String, // فرض, سنة, ذكر, عمل صالح
    var isDone: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirdBuilderScreen(
    navController: NavController? = null,
    viewModel: WirdViewModel = hiltViewModel()
) {
    val favorites by viewModel.favoriteAzkar.collectAsState()
    val haptic = rememberHapticFeedback()

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)
    val skyBlue = Color(0xFF1E88E5)

    // خطة الختمة الذكية
    var khatmahDurationDays by remember { mutableIntStateOf(30) } // 30, 15, 10
    var currentDayOfKhatmah by remember { mutableIntStateOf(5) }
    val pagesPerDay = (604 / khatmahDurationDays).coerceAtLeast(1)
    val startPage = ((currentDayOfKhatmah - 1) * pagesPerDay + 1).coerceIn(1, 604)
    val endPage = (currentDayOfKhatmah * pagesPerDay).coerceIn(1, 604)

    // جدول العبادات اليومي
    val habits = remember {
        mutableStateMapOf(
            "fajr" to WorshipHabit("fajr", "صلاة الفجر في وقتها", "فرض", true),
            "fajr_sunnah" to WorshipHabit("fajr_sunnah", "سنة الفجر (خير من الدنيا وما فيها)", "سنة", true),
            "dhuhr" to WorshipHabit("dhuhr", "صلاة الظهر", "فرض", true),
            "rawatib_dhuhr" to WorshipHabit("rawatib_dhuhr", "راتبة الظهر (٤ قبلها و٢ بعدها)", "سنة", true),
            "asr" to WorshipHabit("asr", "صلاة العصر (الصلاة الوسطى)", "فرض", false),
            "maghrib" to WorshipHabit("maghrib", "صلاة المغرب", "فرض", false),
            "maghrib_sunnah" to WorshipHabit("maghrib_sunnah", "سنة المغرب البعدية (ركعتان)", "سنة", false),
            "isha" to WorshipHabit("isha", "صلاة العشاء", "فرض", false),
            "isha_sunnah" to WorshipHabit("isha_sunnah", "سنة العشاء والوتر", "سنة", false),
            "duha" to WorshipHabit("duha", "صلاة الضحى (صلاة الأوابين)", "سنة", true),
            "morning_azkar" to WorshipHabit("morning_azkar", "أذكار الصباح كاملة", "ذكر", true),
            "evening_azkar" to WorshipHabit("evening_azkar", "أذكار المساء", "ذكر", false),
            "sleep_azkar" to WorshipHabit("sleep_azkar", "أذكار النوم وسورة الملك", "ذكر", false),
            "quran_wird" to WorshipHabit("quran_wird", "ورد القرآن اليومي (${toArabicDigits(pagesPerDay)} ص)", "قرآن", false),
            "istighfar" to WorshipHabit("istighfar", "الاستغفار ١٠٠ مرة", "ذكر", true),
            "sadaqah" to WorshipHabit("sadaqah", "صدقة اليوم أو كلمة طيبة", "عمل صالح", true)
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }
    var customCountString by remember { mutableStateOf("3") }

    val progressMap = remember { mutableStateMapOf<Int, Int>() }

    // حساب نسب التقدم
    val doneHabitsCount = habits.values.count { it.isDone }
    val totalHabitsCount = habits.size
    val habitsProgress = doneHabitsCount.toFloat() / totalHabitsCount.toFloat()

    val azkarProgress = 0.75f
    val quranProgress = 0.50f
    val tasbihProgress = 0.85f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "وردي ومتابعتي اليومية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = emeraldPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(goldAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔥 ٧ أيام متتالية",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = emeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة ورد خاص")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ════════════════════════════════════════════════════════════════
            // 1. حلقات الإنجاز اليومية الثلاثية + عداد المواظبة (Streak Days 🔥)
            // ════════════════════════════════════════════════════════════════
            item(key = "achievement_rings_card") {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "حلقات الإنجاز اليومية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "إنجاز العبادات لليوم: ${(habitsProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = goldAccent
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(0xFFFF9800).copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "مواظبة ٧ أيام",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // حلقة الأذكار
                            RingItem(
                                title = "الأذكار",
                                progress = azkarProgress,
                                color = emeraldPrimary,
                                icon = "📿"
                            )

                            // حلقة القرآن
                            RingItem(
                                title = "القرآن",
                                progress = quranProgress,
                                color = goldAccent,
                                icon = "📖"
                            )

                            // حلقة التسبيح
                            RingItem(
                                title = "المسبحة",
                                progress = tasbihProgress,
                                color = skyBlue,
                                icon = "✨"
                            )
                        }
                    }
                }
            }

            // ════════════════════════════════════════════════════════════════
            // 2. مُخطط الختمة الذكية (Smart Khatmah Planner)
            // ════════════════════════════════════════════════════════════════
            item(key = "khatmah_planner_card") {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = emeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = goldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مُخطط الختمة الذكية",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "ختمة ${toArabicDigits(khatmahDurationDays)} يوماً",
                                style = MaterialTheme.typography.labelSmall,
                                color = goldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // اختيار مدة الختمة
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 15, 10).forEach { days ->
                                FilterChip(
                                    selected = khatmahDurationDays == days,
                                    onClick = { khatmahDurationDays = days },
                                    label = { Text("${toArabicDigits(days)} يوماً (${toArabicDigits(604 / days)} ص/يوم)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = goldAccent,
                                        selectedLabelColor = emeraldPrimary,
                                        containerColor = Color.White.copy(alpha = 0.15f),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // تفاصيل ورد اليوم
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "ورد اليوم ${toArabicDigits(currentDayOfKhatmah)} من ${toArabicDigits(khatmahDurationDays)}:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = goldAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الصفحات من ص ${toArabicDigits(startPage)} إلى ص ${toArabicDigits(endPage)} (${toArabicDigits(pagesPerDay)} صفحة)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    navController?.navigate(Screen.Quran.route)
                                    haptic.tick()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اقرأ ورد اليوم الآن", color = emeraldPrimary, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (currentDayOfKhatmah > 1) currentDayOfKhatmah--
                                    haptic.tick()
                                },
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إعادة جدولة", color = Color.White)
                            }
                        }
                    }
                }
            }

            // ════════════════════════════════════════════════════════════════
            // 3. جدول العبادات اليومي التفاعلي (Daily Worship Tracker)
            // ════════════════════════════════════════════════════════════════
            item(key = "worship_tracker_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "جدول العبادات اليومي (${toArabicDigits(doneHabitsCount)} / ${toArabicDigits(totalHabitsCount)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+ إضافة ورد خاص",
                        style = MaterialTheme.typography.labelSmall,
                        color = emeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showAddDialog = true }
                    )
                }
            }

            items(habits.values.toList(), key = { it.id }) { habit ->
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = if (habit.isDone) emeraldPrimary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            1.dp,
                            if (habit.isDone) goldAccent.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            MaterialTheme.shapes.medium
                        )
                        .clickable {
                            val current = habits[habit.id]
                            if (current != null) {
                                habits[habit.id] = current.copy(isDone = !current.isDone)
                                if (!current.isDone) haptic.tasbihTap()
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = habit.isDone,
                                onCheckedChange = { isChecked ->
                                    habits[habit.id] = habit.copy(isDone = isChecked)
                                    if (isChecked) haptic.tasbihTap()
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = emeraldPrimary,
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = habit.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (habit.isDone) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (habit.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(goldAccent.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = habit.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = goldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ════════════════════════════════════════════════════════════════
            // 4. رسم بياني لإنجاز الأسبوع (Weekly Compliance Chart)
            // ════════════════════════════════════════════════════════════════
            item(key = "weekly_chart_card") {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نسبة الالتزام في آخر ٧ أيام",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "معدل الأسبوع: ٨٢%",
                                style = MaterialTheme.typography.labelSmall,
                                color = goldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // أعمدة الأيام السبعة
                        val daysData = listOf(
                            "سبت" to 0.70f,
                            "أحد" to 0.85f,
                            "اثنين" to 0.90f,
                            "ثلاثاء" to 0.65f,
                            "أربعاء" to 0.95f,
                            "خميس" to 0.80f,
                            "جمعة" to 1.0f
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            daysData.forEach { (dayName, ratio) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${(ratio * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (ratio >= 0.8f) emeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height((ratio * 80).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(if (ratio >= 0.8f) goldAccent else emeraldPrimary.copy(alpha = 0.5f))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // أذكار الورد الخاص المضافة
            if (favorites.isNotEmpty()) {
                item(key = "custom_azkar_header") {
                    Text(
                        text = "أدعيتك وأذكارك الخاصة (${favorites.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = emeraldPrimary
                    )
                }

                items(favorites, key = { it.id }) { zikr ->
                    val currentCount = progressMap[zikr.id] ?: 0
                    ZikrCardItem(
                        zikr = zikr,
                        currentCount = currentCount,
                        showTashkeel = true,
                        fontSizeMultiplier = 1.0f,
                        haptic = haptic,
                        onTap = {
                            if (currentCount < zikr.repeatCount) {
                                progressMap[zikr.id] = currentCount + 1
                            }
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(zikr)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // نافذة إضافة دعاء أو ذكر شخصي
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("إضافة ذكر أو ورد خاص", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            label = { Text("نص الدعاء أو الآية") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customCountString,
                            onValueChange = { customCountString = it.filter { char -> char.isDigit() } },
                            label = { Text("عدد التكرار") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (customText.isNotBlank()) {
                                val count = customCountString.toIntOrNull() ?: 1
                                viewModel.addCustomZikr(customText.trim(), count)
                                customText = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("إضافة إلى الورد")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

@Composable
private fun RingItem(
    title: String,
    progress: Float,
    color: Color,
    icon: String
) {
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "RingAnim")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                // Track
                drawArc(
                    color = color.copy(alpha = 0.15f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
                // Progress
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(text = icon, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(text = "${(animatedProgress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}
