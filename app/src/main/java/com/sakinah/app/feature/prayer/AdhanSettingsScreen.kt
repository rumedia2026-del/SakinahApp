package com.sakinah.app.feature.prayer

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.core.utils.audio.SheikhAudioPlayer
import com.sakinah.app.core.utils.rememberHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhanSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val prefs = remember { context.getSharedPreferences("sakinah_prayer_prefs", Context.MODE_PRIVATE) }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    val sheikhPlayer = remember { SheikhAudioPlayer(context) }
    val isAudioPlaying by sheikhPlayer.isPlaying.collectAsState()
    var previewingMuezzinId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            sheikhPlayer.release()
        }
    }

    var selectedMuezzinId by remember {
        mutableStateOf(prefs.getString(AdhanAlarmScheduler.KEY_SELECTED_MUEZZIN, "makkah") ?: "makkah")
    }
    var fajrMuezzinId by remember {
        mutableStateOf(prefs.getString(AdhanAlarmScheduler.KEY_FAJR_MUEZZIN, "makkah") ?: "makkah")
    }
    var customFajrMuezzinEnabled by remember {
        mutableStateOf(prefs.getString(AdhanAlarmScheduler.KEY_FAJR_MUEZZIN, null) != null)
    }

    var isAdhanEnabled by remember {
        mutableStateOf(prefs.getBoolean(AdhanAlarmScheduler.KEY_ADHAN_ENABLED, true))
    }
    var isPreAdhanEnabled by remember {
        mutableStateOf(prefs.getBoolean(AdhanAlarmScheduler.KEY_PRE_ADHAN_ENABLED, true))
    }
    var isIqamaEnabled by remember {
        mutableStateOf(prefs.getBoolean(AdhanAlarmScheduler.KEY_IQAMA_ENABLED, true))
    }
    var isCrescendoEnabled by remember {
        mutableStateOf(prefs.getBoolean(AdhanAlarmScheduler.KEY_CRESCENDO_VOLUME, true))
    }
    var isFlipToSilenceEnabled by remember {
        mutableStateOf(prefs.getBoolean(AdhanAlarmScheduler.KEY_FLIP_TO_SILENCE, true))
    }
    var isDeafModeEnabled by remember {
        mutableStateOf(prefs.getBoolean(AdhanAlarmScheduler.KEY_DEAF_MODE, false))
    }
    var isMosqueSilentEnabled by remember {
        mutableStateOf(prefs.getBoolean(AdhanAlarmScheduler.KEY_MOSQUE_SILENT, false))
    }

    val maxAlarmVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM).toFloat() }
    var currentAlarmVolume by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_ALARM).toFloat())
    }

    val emeraldPrimary = Color(0xFF0F3D2E)
    val goldAccent = Color(0xFFC8A951)

    fun savePrefsAndReschedule() {
        prefs.edit().apply {
            putString(AdhanAlarmScheduler.KEY_SELECTED_MUEZZIN, selectedMuezzinId)
            if (customFajrMuezzinEnabled) {
                putString(AdhanAlarmScheduler.KEY_FAJR_MUEZZIN, fajrMuezzinId)
            } else {
                remove(AdhanAlarmScheduler.KEY_FAJR_MUEZZIN)
            }
            putBoolean(AdhanAlarmScheduler.KEY_ADHAN_ENABLED, isAdhanEnabled)
            putBoolean(AdhanAlarmScheduler.KEY_PRE_ADHAN_ENABLED, isPreAdhanEnabled)
            putBoolean(AdhanAlarmScheduler.KEY_IQAMA_ENABLED, isIqamaEnabled)
            putBoolean(AdhanAlarmScheduler.KEY_CRESCENDO_VOLUME, isCrescendoEnabled)
            putBoolean(AdhanAlarmScheduler.KEY_FLIP_TO_SILENCE, isFlipToSilenceEnabled)
            putBoolean(AdhanAlarmScheduler.KEY_DEAF_MODE, isDeafModeEnabled)
            putBoolean(AdhanAlarmScheduler.KEY_MOSQUE_SILENT, isMosqueSilentEnabled)
            apply()
        }
        AdhanAlarmScheduler.scheduleAllPrayers(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الأذان والمؤذنون",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = emeraldPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. المفتاح الرئيسي للتنبيهات
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = emeraldPrimary.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, goldAccent.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيهات الأذان والصلوات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = emeraldPrimary
                            )
                            Text(
                                text = "أذان كامل بصوت المؤذن في الوقت الدقيق دون تأخير",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAdhanEnabled,
                            onCheckedChange = {
                                isAdhanEnabled = it
                                savePrefsAndReschedule()
                                haptic.tick()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = goldAccent, checkedTrackColor = emeraldPrimary)
                        )
                    }
                }
            }

            // 2. قائمة المؤذنين واختيار المؤذن العام
            item {
                Text(
                    text = "اختر المؤذن العام لجميع الصلوات:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = emeraldPrimary
                )
            }

            items(MuadhinCatalog.muadhins) { muadhin ->
                val isSelected = selectedMuezzinId == muadhin.id
                val isCurrentPlaying = isAudioPlaying && previewingMuezzinId == muadhin.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable {
                            selectedMuezzinId = muadhin.id
                            savePrefsAndReschedule()
                            haptic.completion()
                        },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) emeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) goldAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) goldAccent else emeraldPrimary.copy(alpha = 0.1f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else emeraldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = muadhin.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) emeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = muadhin.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // زر الاستماع / المعاينة
                        IconButton(
                            onClick = {
                                if (isCurrentPlaying) {
                                    sheikhPlayer.stop()
                                    previewingMuezzinId = null
                                } else {
                                    previewingMuezzinId = muadhin.id
                                    sheikhPlayer.playRawResource(muadhin.rawResId) {
                                        previewingMuezzinId = null
                                    }
                                }
                                haptic.tick()
                            }
                        ) {
                            Icon(
                                imageVector = if (isCurrentPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "معاينة صوت الأذان",
                                tint = if (isCurrentPlaying) goldAccent else emeraldPrimary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "محدد",
                                tint = goldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // 3. تخصيص مؤذن مستقل لصلاة الفجر
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تخصيص مؤذن لصلاة الفجر 🌅",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldPrimary
                                )
                                Text(
                                    text = "بصيغة (الصلاة خير من النوم) العذبة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = customFajrMuezzinEnabled,
                                onCheckedChange = {
                                    customFajrMuezzinEnabled = it
                                    savePrefsAndReschedule()
                                    haptic.tick()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = goldAccent, checkedTrackColor = emeraldPrimary)
                            )
                        }

                        AnimatedVisibility(visible = customFajrMuezzinEnabled) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                                MuadhinCatalog.muadhins.forEach { muadhin ->
                                    val isFajrSelected = fajrMuezzinId == muadhin.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable {
                                                fajrMuezzinId = muadhin.id
                                                savePrefsAndReschedule()
                                                haptic.tick()
                                            }
                                            .background(if (isFajrSelected) emeraldPrimary.copy(alpha = 0.08f) else Color.Transparent)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = muadhin.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isFajrSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isFajrSelected) emeraldPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isFajrSelected) {
                                            Text("✓ مؤذن الفجر", color = goldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. مستوى صوت الأذان المستقل (STREAM_ALARM)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = emeraldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مستوى صوت الأذان (تجاوز الوضع الصامت)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${((currentAlarmVolume / maxAlarmVolume) * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            )
                        }
                        Slider(
                            value = currentAlarmVolume,
                            onValueChange = { newVol ->
                                currentAlarmVolume = newVol
                                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVol.toInt(), 0)
                            },
                            valueRange = 0f..maxAlarmVolume,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "يعمل عبر قناة المنبّه (STREAM_ALARM) ليصدح بالحق حتى إن كان الهاتف في الوضع الصامت.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 5. الميزات الذكية المتقدمة (Crescendo, Flip-to-Silence, Pre-Adhan, Iqama, Deaf Mode)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "الميزات الذكية للأذان:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = emeraldPrimary
                        )

                        // Crescendo Volume
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("التدرج الصوتي الذكي (Crescendo)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("يبدأ الأذان بهدوء ويرتفع تدريجياً خلال ١٠ ثوانٍ لراحة القلب والسكينة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isCrescendoEnabled,
                                onCheckedChange = {
                                    isCrescendoEnabled = it
                                    savePrefsAndReschedule()
                                    haptic.tick()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = goldAccent, checkedTrackColor = emeraldPrimary)
                            )
                        }

                        HorizontalDivider()

                        // Flip-to-Silence
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("قلب الهاتف لكتم الأذان (Flip-to-Silence) 📱", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("اقلب شاشة الهاتف على وجهها لكتم صوت الأذان فوراً عبر مستشعر التسارع", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isFlipToSilenceEnabled,
                                onCheckedChange = {
                                    isFlipToSilenceEnabled = it
                                    savePrefsAndReschedule()
                                    haptic.tick()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = goldAccent, checkedTrackColor = emeraldPrimary)
                            )
                        }

                        HorizontalDivider()

                        // Pre-Adhan Reminder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تنبيه ما قبل الصلاة (١٥ دقيقة)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("تذكير هادئ لإسباغ الوضوء والاستعداد قبل رفع الأذان", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isPreAdhanEnabled,
                                onCheckedChange = {
                                    isPreAdhanEnabled = it
                                    savePrefsAndReschedule()
                                    haptic.tick()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = goldAccent, checkedTrackColor = emeraldPrimary)
                            )
                        }

                        HorizontalDivider()

                        // Iqama Reminder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تنبيه وقت الإقامة (٢٠ دقيقة)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("إشعار يحثك على التراص واستقبال القبلة مع تكبيرة الإحرام", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isIqamaEnabled,
                                onCheckedChange = {
                                    isIqamaEnabled = it
                                    savePrefsAndReschedule()
                                    haptic.tick()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = goldAccent, checkedTrackColor = emeraldPrimary)
                            )
                        }

                        HorizontalDivider()

                        // Deaf & Hard of Hearing Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("وضع فاقدي السمع (الاهتزاز الملموس) 📳", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("نبضات اهتزازية فريدة لكل صلاة تُشعر ذوي الهمم بدخول وقت الفريضة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isDeafModeEnabled,
                                onCheckedChange = {
                                    isDeafModeEnabled = it
                                    savePrefsAndReschedule()
                                    haptic.tick()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = goldAccent, checkedTrackColor = emeraldPrimary)
                            )
                        }

                        HorizontalDivider()

                        // Mosque Silent Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("وضع احترام المسجد (الصامت التلقائي) 🕌", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("تحويل الهاتف للصامت لمدة ٣٠ دقيقة بعد الأذان احتراماً لبيوت الله", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isMosqueSilentEnabled,
                                onCheckedChange = {
                                    isMosqueSilentEnabled = it
                                    savePrefsAndReschedule()
                                    haptic.tick()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = goldAccent, checkedTrackColor = emeraldPrimary)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
