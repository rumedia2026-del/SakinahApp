package com.sakinah.app.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakinah.app.R
import com.sakinah.app.core.ui.theme.SakinahThemeMode
import com.sakinah.app.core.utils.BackupRestoreHelper
import com.sakinah.app.core.utils.BatteryOptimizationHelper
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.data.repository.AzkarRepository
import com.sakinah.app.feature.prayer.PrayerTimesHelper
import com.sakinah.app.feature.quran.audio.QuranAudioDownloader
import com.sakinah.app.feature.quran.audio.QuranSheikhManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    azkarRepository: AzkarRepository = hiltViewModel<SettingsRepositoryHolder>().azkarRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences by viewModel.userPreferences.collectAsState()
    val haptic = rememberHapticFeedback()

    val guide = BatteryOptimizationHelper.getDeviceSpecificGuide(context)
    val isBatteryOptimized = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)

    var showResetDialog by remember { mutableStateOf(false) }
    var testFlashActive by remember { mutableStateOf(false) }

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    // استعادة النسخة الاحتياطية
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val count = BackupRestoreHelper.restoreFromJson(context, uri, azkarRepository)
                    Toast.makeText(context, "تمت استعادة $count أذكار بنجاح!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "حدث خطأ أثناء الاستعادة: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // تصدير النسخة الاحتياطية
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val count = BackupRestoreHelper.exportToJson(context, uri, azkarRepository)
                    Toast.makeText(context, "تم تصدير $count أذكار بنجاح!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "حدث خطأ أثناء التصدير: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("إعادة ضبط الإعدادات", fontWeight = FontWeight.Bold) },
            text = { Text("هل تود استعادة الإعدادات الافتراضية للتطبيق بالكامل؟ لن تتأثر أذكارك أو تقدمك.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                        Toast.makeText(context, "تمت استعادة الإعدادات الافتراضية بنجاح", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("نعم، استعادة", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مركز التحكم والإعدادات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ═══════════════════════════════════════════════════════════════════
                // 1. بطاقة تعريفية رسمية مع شعار سكينة
                // ═══════════════════════════════════════════════════════════════════
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = emeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sakinah_logo),
                            contentDescription = "شعار سكينة",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "تطبيق سـَـكـِـيـنـَـة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            )
                            Text(
                                text = "مصحف تدبر وعمل • حصن المسلم الشامل • بدون إعلانات",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "الإصدار 1.0.0 (Flagship Edition)",
                                style = MaterialTheme.typography.labelSmall,
                                color = goldAccent.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════════════════
                // 2. المظهر ونظام الألوان والخطوط
                // ═══════════════════════════════════════════════════════════════════
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = goldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "المظهر ونظام الألوان",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val themes = listOf(
                            SakinahThemeMode.WARM_PAPER to "الورقي الدافئ (Warm Paper)",
                            SakinahThemeMode.SOFT_SLATE_DARK to "الليلي المريح (Soft Slate)",
                            SakinahThemeMode.PURE_OLED_BLACK to "الأسود النقي (Pure OLED Black)",
                            SakinahThemeMode.MATERIAL_YOU to "الألوان الديناميكية (Material You)"
                        )

                        themes.forEach { (mode, title) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = preferences.themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    colors = RadioButtonDefaults.colors(selectedColor = goldAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Text(
                            text = "حجم خط الأذكار والمصحف (${(preferences.fontSizeMultiplier * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = preferences.fontSizeMultiplier,
                            onValueChange = viewModel::setFontSizeMultiplier,
                            valueRange = 0.8f..1.5f
                        )

                        // معاينة حية لآية كريمة
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(emeraldPrimary.copy(alpha = 0.06f))
                                .border(1.dp, goldAccent.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (preferences.showTashkeel) "﴿ اللَّهُ نُورُ السَّمَاوَاتِ وَالْأَرْضِ ﴾" else "﴿ الله نور السماوات والارض ﴾",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = (20 * preferences.fontSizeMultiplier).sp,
                                    lineHeight = (36 * preferences.fontSizeMultiplier).sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = emeraldPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إظهار التشكيل افتراضياً", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = preferences.showTashkeel,
                                onCheckedChange = viewModel::toggleTashkeel
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إبقاء الشاشة مضاءة أثناء القراءة", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = preferences.keepScreenOn,
                                onCheckedChange = viewModel::toggleKeepScreenOn
                            )
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════════════════
                // 3. التحكم في الأذكار والمسبحة ووضع فاقدي السمع
                // ═══════════════════════════════════════════════════════════════════
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = goldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "التحكم في الأذكار والمسبحة",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الانتقال التلقائي للذكر التالي", style = MaterialTheme.typography.bodyMedium)
                                Text("تمرير الشاشة للذكر التالي فور اكتمال التكرارات", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = preferences.autoAdvanceZikr,
                                onCheckedChange = viewModel::toggleAutoAdvance
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الاهتزاز اللمسي (Haptic Feedback)", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = preferences.hapticFeedbackEnabled,
                                onCheckedChange = viewModel::toggleHaptic
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // وضع فاقدي السمع
                        Card(
                            colors = CardDefaults.cardColors(containerColor = goldAccent.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.HearingDisabled, contentDescription = null, tint = goldAccent)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("وضع فاقدي السمع", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("اهتزاز موجي ووميض بصري ذهبي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Switch(
                                        checked = preferences.deafHapticVisualMode,
                                        onCheckedChange = viewModel::toggleDeafMode
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        haptic.milestone(deafMode = true)
                                        scope.launch {
                                            testFlashActive = true
                                            delay(200)
                                            testFlashActive = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("تجربة الاهتزاز والوميض البصري الآن")
                                }
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════════════════
                // 4. الصوت والشيوخ والتحميل أوفلاين
                // ═══════════════════════════════════════════════════════════════════
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = goldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "الصوت وقراء القرآن الكريم",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("القارئ الافتراضي لتلاوة القرآن:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))

                        QuranSheikhManager.RECITERS.forEach { sheikh ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { viewModel.setDefaultQuranReciterId(sheikh.id) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = preferences.defaultQuranReciterId == sheikh.id,
                                    onClick = { viewModel.setDefaultQuranReciterId(sheikh.id) },
                                    colors = RadioButtonDefaults.colors(selectedColor = goldAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "${sheikh.nameAr} (${sheikh.style})", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        // إدارة الصوتيات المحملة أوفلاين
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("الصوتيات المحمّلة أوفلاين", style = MaterialTheme.typography.bodyMedium)
                                Text("المساحة المستخدمة: ${QuranAudioDownloader.getTotalDownloadedSizeFormatted(context)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            OutlinedButton(
                                onClick = {
                                    val freed = QuranAudioDownloader.clearAllDownloads(context)
                                    Toast.makeText(context, "تم تفريغ ذاكرة الصوتيات المؤقتة بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تفريغ الكاش")
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════════════════
                // 5. مواقيت الصلاة والمدينة المختارة والأذان
                // ═══════════════════════════════════════════════════════════════════
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = goldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مواقيت الصلاة والمدينة",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("المدينة الحالية: ${preferences.selectedCityName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = goldAccent)
                        Spacer(modifier = Modifier.height(8.dp))

                        // اختيار المدينة
                        Text("اختر مدينتك لحساب دقيق للمواقيت والقبلة:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        PrayerTimesHelper.SUPPORTED_CITIES.forEach { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { viewModel.setSelectedCity(city.name) }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = preferences.selectedCityName == city.name,
                                    onClick = { viewModel.setSelectedCity(city.name) },
                                    colors = RadioButtonDefaults.colors(selectedColor = goldAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "${city.name} (${city.country})", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تفعيل تنبيهات الصلوات والأذان", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = preferences.prayerAlarmEnabled,
                                onCheckedChange = viewModel::togglePrayerAlarm
                            )
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════════════════
                // 6. استثناء توفير البطارية (OEM Battery Optimization)
                // ═══════════════════════════════════════════════════════════════════
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBatteryOptimized) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = emeraldPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = guide.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        guide.steps.forEach { step ->
                            Text(text = step, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (guide.actionIntent != null) {
                                    try {
                                        context.startActivity(guide.actionIntent)
                                    } catch (e: Exception) {
                                        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                                    }
                                } else {
                                    BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isBatteryOptimized) "استثناء التطبيق لضمان دقة الأذان والتنبيهات" else "التطبيق مستثنى بالفعل ✓")
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════════════════
                // 7. النسخ الاحتياطي والاستعادة وإعادة ضبط المصنع
                // ═══════════════════════════════════════════════════════════════════
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("النسخ الاحتياطي والبيانات", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "احفظ وردك المفضل وأدعيتك في ملف JSON أو استعدها بأمان:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { backupLauncher.launch("sakinah_backup_${System.currentTimeMillis()}.json") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تصدير JSON")
                            }

                            OutlinedButton(
                                onClick = { restoreLauncher.launch(arrayOf("application/json")) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استعادة")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة الإعدادات الافتراضية للتطبيق", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // ═══════════════════════════════════════════════════════════════════
                // 7. أدوات إسلامية إضافية (التقويم الهجري، حاسبة الزكاة، مصمم البطاقات)
                // ═══════════════════════════════════════════════════════════════════
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = goldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "التقويم الهجري والمناسبات الإسلامية",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(goldAccent.copy(alpha = 0.10f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "اليوم: ٢٤ جمادى الأولى ١٤٤٨ هـ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = goldAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "أقرب المناسبات: صيام الأيام البيض القادمة (١٣-١٤-١٥) • شهر رجب بعد ٦٥ يوماً",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // حاسبة الزكاة الشرعية
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = goldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "حاسبة الزكاة الشرعية (٢.٥%)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "احسب زكاة مالك فوراً بنسبة ربع العشر (٢.٥%) إذا بلغ النصاب وحال عليه الحول.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        var moneyInput by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = moneyInput,
                            onValueChange = { moneyInput = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("إجمالي المال المدخر (بالجنيه/الريال)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        val amount = moneyInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val zakatDue = amount * 0.025
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .background(emeraldPrimary.copy(alpha = 0.08f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "مبلغ الزكاة الواجب إخراجه: ${"%,.2f".format(zakatDue)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldPrimary
                                )
                            }
                        }
                    }
                }

                // مُصمم ومشارك بطاقات الآيات والأذكار
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brush, contentDescription = null, tint = goldAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مُصمم بطاقات الآيات والأذكار",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "يمكنك مشاركة أي ذكر أو آية كريمة كبطاقة إيمانية مزخرفة بتوقيع تطبيق سكينة عبر زر المشاركة الموجود في بطاقات الأذكار وتدبر المصحف.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // وميض تجربة فاقدي السمع
        if (testFlashActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(goldAccent.copy(alpha = 0.35f))
            )
        }
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class SettingsRepositoryHolder @javax.inject.Inject constructor(
    val azkarRepository: AzkarRepository
) : androidx.lifecycle.ViewModel()
