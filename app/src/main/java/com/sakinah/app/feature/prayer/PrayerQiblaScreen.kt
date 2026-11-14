package com.sakinah.app.feature.prayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sakinah.app.core.utils.audio.SheikhAudioPlayer
import com.sakinah.app.core.utils.rememberHapticFeedback
import kotlin.math.abs
import kotlin.math.roundToInt

enum class QiblaDisplayMode {
    COMPASS_2D,
    CAMERA_AR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerQiblaScreen(
    onNavigateBack: () -> Unit,
    onOpenAdhanSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = rememberHapticFeedback()

    val emeraldPrimary = Color(0xFF0F3D2E)
    val goldAccent = Color(0xFFC8A951)
    val alignedGreen = Color(0xFF10B981)

    val sheikhPlayer = remember { SheikhAudioPlayer(context) }
    DisposableEffect(Unit) {
        onDispose { sheikhPlayer.release() }
    }

    var selectedCity by remember { mutableStateOf(PrayerTimesHelper.SUPPORTED_CITIES.first()) }
    var prayerState by remember { mutableStateOf(PrayerTimesHelper.calculateTodayPrayers(selectedCity)) }
    var monthlySchedule by remember { mutableStateOf(PrayerTimesHelper.calculateMonthlySchedule(selectedCity)) }
    var showCityDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: القبلة واليوم، 1: جدول الشهر
    var qiblaMode by remember { mutableStateOf(QiblaDisplayMode.COMPASS_2D) }

    // مستشعر التوجيه وبوصلة القبلة
    var rawHeading by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var isLowAccuracy by remember { mutableStateOf(false) }

    // إذن الكاميرا للواقع المعزز
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            qiblaMode = QiblaDisplayMode.COMPASS_2D
        }
    }

    // حساب زاوية القبلة الكروية من الموقع
    val qiblaBearing = remember(selectedCity) {
        QiblaCalculator.calculateBearing(selectedCity.latitude, selectedCity.longitude)
    }

    // تصحيح الانحراف المغناطيسي للحصول على True Azimuth
    val trueHeading = remember(rawHeading, selectedCity) {
        QiblaCalculator.calculateTrueAzimuth(rawHeading, selectedCity.latitude, selectedCity.longitude)
    }

    // تنعيم حركة المؤشر
    val smoothedHeading by animateFloatAsState(
        targetValue = trueHeading,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "SmoothedHeading"
    )

    // فرق الزاوية الأقصر وإرشادات التوجيه
    val shortestDiff = remember(smoothedHeading, qiblaBearing) {
        QiblaCalculator.calculateShortestDifference(smoothedHeading, qiblaBearing)
    }
    val isFacingQibla = abs(shortestDiff) <= 3.0f

    // اهتزاز لمسي لمرة واحدة عند محاذاة القبلة تماماً
    var wasFacingQibla by remember { mutableStateOf(false) }
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !wasFacingQibla) {
            haptic.heavy()
        }
        wasFacingQibla = isFacingQibla
    }

    // مستشعرات الحركة والبوصلة
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val rotationMatrix = FloatArray(9)
        val remappedMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        val lastAccelerometer = FloatArray(3)
        val lastMagnetometer = FloatArray(3)
        var lastAccelerometerSet = false
        var lastMagnetometerSet = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedMatrix
                    )
                    SensorManager.getOrientation(remappedMatrix, orientationAngles)
                    val deg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    rawHeading = (deg + 360f) % 360f
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                    lastAccelerometerSet = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                    lastMagnetometerSet = true
                }

                if (lastAccelerometerSet && lastMagnetometerSet) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                        SensorManager.remapCoordinateSystem(
                            rotationMatrix,
                            SensorManager.AXIS_X,
                            SensorManager.AXIS_Z,
                            remappedMatrix
                        )
                        SensorManager.getOrientation(remappedMatrix, orientationAngles)
                        val deg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        rawHeading = (deg + 360f) % 360f
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
                isLowAccuracy = (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE ||
                        accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW)
            }
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

    val glowColor by animateColorAsState(
        targetValue = if (isFacingQibla) alignedGreen else goldAccent,
        animationSpec = tween(250),
        label = "QiblaGlowColor"
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // بطاقة إعدادات الأذان والمؤذنين
                    item(key = "muezzin_selection_card") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .clickable { onOpenAdhanSettings() },
                            colors = CardDefaults.cardColors(containerColor = emeraldPrimary.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, goldAccent)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = goldAccent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "إعدادات الأذان والمؤذنين 🕌",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = emeraldPrimary
                                        )
                                        Text(
                                            text = "اختر المؤذن، تدرج الصوت، قلب الهاتف للكتم، وصوت الفجر",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "فتح الإعدادات",
                                    tint = emeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // تنبيه انخفاض دقة البوصلة
                    if (isLowAccuracy) {
                        item(key = "calibration_warning") {
                            val infiniteTransition = rememberInfiniteTransition(label = "InfinityAnim")
                            val infinityAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                                label = "InfinityAlpha"
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107)),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFF856404),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "دقة حساس البوصلة منخفضة",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF856404)
                                        )
                                        Text(
                                            text = "حرك الهاتف في الهواء على شكل رقم 8 (∞) لمعايرة البوصلة",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF856404)
                                        )
                                    }
                                    Text(
                                        text = "∞",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404).copy(alpha = infinityAlpha)
                                    )
                                }
                            }
                        }
                    }

                    // بطاقة القبلة (2D أو AR)
                    item(key = "qibla_main_card") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, glowColor.copy(alpha = 0.6f), MaterialTheme.shapes.large),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // أزرار التبديل بين 2D و AR
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    FilterChip(
                                        selected = qiblaMode == QiblaDisplayMode.COMPASS_2D,
                                        onClick = { qiblaMode = QiblaDisplayMode.COMPASS_2D },
                                        label = { Text("بوصلة كلاسيكية 🧭") },
                                        leadingIcon = { Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = goldAccent.copy(alpha = 0.2f),
                                            selectedLabelColor = goldAccent
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilterChip(
                                        selected = qiblaMode == QiblaDisplayMode.CAMERA_AR,
                                        onClick = {
                                            if (hasCameraPermission) {
                                                qiblaMode = QiblaDisplayMode.CAMERA_AR
                                            } else {
                                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        },
                                        label = { Text("واقع معزز (AR) 📷") },
                                        leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = goldAccent.copy(alpha = 0.2f),
                                            selectedLabelColor = goldAccent
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // إرشادات التوجيه
                                val (guidanceText, isAligned) = QiblaCalculator.getGuidanceText(smoothedHeading, qiblaBearing)
                                Text(
                                    text = if (isAligned) "🕋 أنت بمواجهة القبلة تماماً بفضل الله!" else guidanceText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAligned) alignedGreen else goldAccent,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "زاوية القبلة: ${qiblaBearing.roundToInt()}° • توجيه الجهاز: ${smoothedHeading.roundToInt()}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (qiblaMode == QiblaDisplayMode.CAMERA_AR && hasCameraPermission) {
                                    // وضع الواقع المعزز بالكاميرا
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(2.dp, glowColor, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AndroidView(
                                            modifier = Modifier.fillMaxSize(),
                                            factory = { ctx ->
                                                val previewView = PreviewView(ctx)
                                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                                cameraProviderFuture.addListener({
                                                    val cameraProvider = cameraProviderFuture.get()
                                                    val preview = Preview.Builder().build().also {
                                                        it.surfaceProvider = previewView.surfaceProvider
                                                    }
                                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                                    try {
                                                        cameraProvider.unbindAll()
                                                        cameraProvider.bindToLifecycle(
                                                            lifecycleOwner,
                                                            cameraSelector,
                                                            preview
                                                        )
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }, ContextCompat.getMainExecutor(ctx))
                                                previewView
                                            }
                                        )

                                        // تراكب AR للقبلة
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val cX = size.width / 2f
                                            val cY = size.height / 2f
                                            // رسم خط الأفق والتصويب
                                            drawLine(
                                                color = Color.White.copy(alpha = 0.5f),
                                                start = Offset(cX - 60.dp.toPx(), cY),
                                                end = Offset(cX + 60.dp.toPx(), cY),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                            drawLine(
                                                color = Color.White.copy(alpha = 0.5f),
                                                start = Offset(cX, cY - 60.dp.toPx()),
                                                end = Offset(cX, cY + 60.dp.toPx()),
                                                strokeWidth = 2.dp.toPx()
                                            )

                                            // دائرة تصويب مركزية
                                            drawCircle(
                                                color = if (isFacingQibla) alignedGreen else goldAccent,
                                                radius = 35.dp.toPx(),
                                                style = Stroke(width = 3.dp.toPx())
                                            )
                                        }

                                        // سهم أو رمز الكعبة المتموضع بحسب الزاوية
                                        val horizontalOffset = (shortestDiff * 4.5f).coerceIn(-140f, 140f)
                                        Box(
                                            modifier = Modifier
                                                .offset(x = horizontalOffset.dp)
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(if (isFacingQibla) alignedGreen else goldAccent)
                                                .border(2.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "🕋", fontSize = 26.sp)
                                        }
                                    }
                                } else {
                                    // وضع البوصلة الكلاسيكية (2D)
                                    Box(
                                        modifier = Modifier
                                            .size(240.dp)
                                            .clip(CircleShape)
                                            .background(emeraldPrimary.copy(alpha = 0.06f))
                                            .border(2.5.dp, glowColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val center = Offset(size.width / 2f, size.height / 2f)
                                            val radius = size.width / 2f - 16.dp.toPx()

                                            drawCircle(
                                                color = glowColor.copy(alpha = 0.2f),
                                                radius = radius,
                                                style = Stroke(width = 2.dp.toPx())
                                            )
                                        }

                                        val needleRotation = qiblaBearing - smoothedHeading
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .rotate(needleRotation),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 10.dp)
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isFacingQibla) alignedGreen else goldAccent)
                                                    .border(2.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "🕋", fontSize = 20.sp)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(glowColor)
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .padding(bottom = 12.dp)
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Gray.copy(alpha = 0.5f))
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (qiblaMode == QiblaDisplayMode.CAMERA_AR)
                                        "وجّه كاميرا الهاتف حولك حتى يظهر رمز الكعبة المشرفة في منتصف دائرة التصويب"
                                    else
                                        "ضع الهاتف مستوياً وحركه ببطء حتى يضيء المؤشر بالأخضر الزمردي",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // بطاقة صلوات اليوم
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
                // جدول مواقيت الشهر بالكامل
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "monthly_header") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = emeraldPrimary),
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
