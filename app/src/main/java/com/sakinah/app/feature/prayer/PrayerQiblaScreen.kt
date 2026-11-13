package com.sakinah.app.feature.prayer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.core.utils.audio.SheikhAudioPlayer
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.feature.prayer.components.MuezzinSelectionCard
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerQiblaScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val sheikhPlayer = remember { SheikhAudioPlayer(context) }
    var selectedMuezzinId by remember { mutableStateOf("alafasy") }

    DisposableEffect(Unit) {
        onDispose { sheikhPlayer.release() }
    }

    var selectedCity by remember { mutableStateOf(PrayerTimesHelper.SUPPORTED_CITIES.first()) }
    var prayerState by remember { mutableStateOf(PrayerTimesHelper.calculateTodayPrayers(selectedCity)) }
    var monthlySchedule by remember { mutableStateOf(PrayerTimesHelper.calculateMonthlySchedule(selectedCity)) }
    var showCityDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: القبلة واليوم، 1: جدول الشهر

    // بوصلة القبلة ومستشعر التوجيه
    var currentAzimuth by remember { mutableFloatStateOf(0f) }
    val smoothedAzimuth by animateFloatAsState(
        targetValue = currentAzimuth,
        animationSpec = tween(durationMillis = 200),
        label = "SmoothedAzimuth"
    )

    // قراءة مستشعر التوجيه في الأندرويد
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        val lastAccelerometer = FloatArray(3)
        val lastMagnetometer = FloatArray(3)
        var lastAccelerometerSet = false
        var lastMagnetometerSet = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuthInDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    currentAzimuth = (azimuthInDegrees + 360) % 360
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                    lastAccelerometerSet = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                    lastMagnetometerSet = true
                }

                if (lastAccelerometerSet && lastMagnetometerSet) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val azimuthInDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        currentAzimuth = (azimuthInDegrees + 360) % 360
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelSensor?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            magnetSensor?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // تحديث المواعيد عند تغيير المدينة
    LaunchedEffect(selectedCity) {
        prayerState = PrayerTimesHelper.calculateTodayPrayers(selectedCity)
        monthlySchedule = PrayerTimesHelper.calculateMonthlySchedule(selectedCity)
    }

    val goldAccent = Color(0xFFC8A951)
    val deepEmerald = Color(0xFF0F3D2E)

    // الفرق بين زاوية الهاتف وزاوية القبلة
    val qiblaBearing = prayerState.qiblaDirection.toFloat()
    val diff = abs(smoothedAzimuth - qiblaBearing)
    val isFacingQibla = diff < 4f || diff > 356f

    val compassGlowColor by animateColorAsState(
        targetValue = if (isFacingQibla) goldAccent else deepEmerald,
        animationSpec = tween(300),
        label = "CompassGlow"
    )

    // نافذة اختيار المدينة
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "اختر مدينتك لمواقيت دقيقة والقبلة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(PrayerTimesHelper.SUPPORTED_CITIES) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    selectedCity = city
                                    showCityDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = city.name == selectedCity.name,
                                onClick = {
                                    selectedCity = city
                                    showCityDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = goldAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = city.country,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("إغلاق", color = goldAccent)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "مواقيت الصلاة والقبلة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCityDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = goldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${selectedCity.name} (${selectedCity.country}) ▾",
                                style = MaterialTheme.typography.labelSmall,
                                color = goldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showCityDialog = true }) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "تغيير المدينة", tint = goldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // شريط التبويب: القبلة واليوم / جدول الشهر
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = goldAccent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("بوصلة القبلة واليوم", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CompassCalibration, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("جدول مواقيت الشهر", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedTab == 0) {
                // تبويب 1: بوصلة القبلة التفاعلية + مواقيت اليوم
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // بطاقة الأذان والمؤذنون الرئيسية في أعلى الشاشة
                    item(key = "muezzin_selection_card") {
                        MuezzinSelectionCard(
                            selectedMuezzinId = selectedMuezzinId,
                            onSelectMuezzin = { selectedMuezzinId = it },
                            sheikhPlayer = sheikhPlayer
                        )
                    }

                    // 1. بوصلة القبلة الحية
                    item(key = "qibla_compass") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, compassGlowColor.copy(alpha = 0.5f), MaterialTheme.shapes.large),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isFacingQibla) "🕋 أنت باتجاه القبلة الآن بفضل الله!" else "وجّه الهاتف نحو الكعبة المشرفة",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFacingQibla) goldAccent else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "زاوية القبلة: ${qiblaBearing.toInt()}° • زاوية الهاتف: ${smoothedAzimuth.toInt()}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // القرص الدائري للبوصلة
                                Box(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(CircleShape)
                                        .background(deepEmerald.copy(alpha = 0.06f))
                                        .border(2.dp, compassGlowColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // مؤشرات الجهات الأربع
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val center = Offset(size.width / 2f, size.height / 2f)
                                        val radius = size.width / 2f - 16.dp.toPx()

                                        drawCircle(
                                            color = compassGlowColor.copy(alpha = 0.2f),
                                            radius = radius,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }

                                    // إبرة البوصلة التي تدور مع الهاتف
                                    val needleRotation = qiblaBearing - smoothedAzimuth
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .rotate(needleRotation),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // مؤشر الكعبة في رأس السهم
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 10.dp)
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isFacingQibla) goldAccent else deepEmerald),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🕋", fontSize = 18.sp)
                                        }

                                        // مركز البوصلة
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(compassGlowColor)
                                        )

                                        // الذيل الجنوبي
                                        Box(
                                            modifier = Modifier
                                                .padding(bottom = 12.dp)
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color.Gray.copy(alpha = 0.5f))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "ضع الهاتف مستوياً وحركه ببطء حتى تضيء البوصلة باللون الذهبي",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 2. بطاقة صلوات اليوم
                    item(key = "today_prayers") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "صلوات اليوم",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = prayerState.gregorianDateFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                prayerState.prayerList.forEach { p ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .background(if (p.isNext) goldAccent.copy(alpha = 0.12f) else Color.Transparent)
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = p.nameAr,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (p.isNext) FontWeight.Bold else FontWeight.Normal,
                                                color = if (p.isNext) goldAccent else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (p.isNext) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(MaterialTheme.shapes.extraSmall)
                                                        .background(goldAccent.copy(alpha = 0.25f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "الصلاة القادمة",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = goldAccent,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = p.formattedTime,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (p.isNext) FontWeight.Bold else FontWeight.Normal,
                                            color = if (p.isNext) goldAccent else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // تبويب 2: جدول مواقيت الشهر بالكامل
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "monthly_header") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = deepEmerald),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("اليوم", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("الفجر", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                Text("الظهر", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                Text("العصر", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                Text("المغرب", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                Text("العشاء", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }

                    items(monthlySchedule, key = { it.dayNumber }) { day ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (day.isToday) goldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${day.dayNumber} ${day.dayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (day.isToday) goldAccent else MaterialTheme.colorScheme.onSurface
                                )
                                Text(text = day.fajr, style = MaterialTheme.typography.bodySmall)
                                Text(text = day.dhuhr, style = MaterialTheme.typography.bodySmall)
                                Text(text = day.asr, style = MaterialTheme.typography.bodySmall)
                                Text(text = day.maghrib, style = MaterialTheme.typography.bodySmall)
                                Text(text = day.isha, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
