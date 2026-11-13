package com.sakinah.app.feature.quran.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sakinah.app.core.utils.audio.SheikhAudioPlayer
import com.sakinah.app.feature.quran.TadabburHelper
import com.sakinah.app.feature.quran.audio.RecitationRecorder
import com.sakinah.app.feature.quran.model.AyahItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TadabburAyahBottomSheet(
    ayah: AyahItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // مشغل صوت الشيخ عبر Media3 ExoPlayer
    val sheikhPlayer = remember { SheikhAudioPlayer(context) }
    val isSheikhPlaying by sheikhPlayer.isPlaying.collectAsState()

    // مسجل التلاوة الذاتي
    val recorder = remember { RecitationRecorder(context) }
    val isRecording by recorder.isRecording.collectAsState()
    val isPlayingRecording by recorder.isPlayingRecording.collectAsState()
    val hasRecording by recorder.hasRecording.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sheikhPlayer.stop()
            recorder.startRecording(ayah.surahNumber, ayah.numberInSurah)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sheikhPlayer.release()
            recorder.release()
        }
    }

    val tadabbur = remember(ayah) { TadabburHelper.getTadabburForAyah(ayah) }

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    ModalBottomSheet(
        onDismissRequest = {
            sheikhPlayer.stop()
            recorder.release()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // شريط العنوان
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${ayah.surahName} • الآية (${ayah.numberInSurah}) • صفحة ${ayah.page}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = emeraldPrimary
                    )
                    Text(
                        text = "الجزء ${ayah.juz} • الحزب ${(ayah.juz * 2)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // نص الآية الكريمة بالرسم العثماني
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)),
                border = androidx.compose.foundation.BorderStroke(1.dp, goldAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${ayah.text} ﴿${ayah.numberInSurah}﴾",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 36.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B2E24),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // التبويبات الأربعة
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = goldAccent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("وقفات", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("العمل بالآية", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Handyman, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("معاني الكلمات", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("التفسير والتلاوة", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // 1. الوقفات التدبرية
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        tadabbur.reflections.forEachIndexed { idx, point ->
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(goldAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = goldAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = point,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // 2. العمل بالآيات
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        tadabbur.actionPoints.forEachIndexed { idx, action ->
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = emeraldPrimary.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(emeraldPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = action,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // 3. معاني الكلمات
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tadabbur.wordMeanings.forEach { (word, meaning) ->
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = word,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = goldAccent,
                                        modifier = Modifier.width(90.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = meaning,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // 4. التفسير والتلاوة + مسجل التلاوة الذاتي (سجّل صوتك وقارن)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // تلاوة الشيخ مشاري العفاسي
                        Button(
                            onClick = {
                                if (isSheikhPlaying) {
                                    sheikhPlayer.stop()
                                } else {
                                    recorder.stopPlaying()
                                    recorder.stopRecording()
                                    sheikhPlayer.playAyah(ayah.surahNumber, ayah.numberInSurah)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isSheikhPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = goldAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSheikhPlaying) "إيقاف تلاوة الشيخ" else "استمع لتلاوة الشيخ مشاري العفاسي 🎧",
                                color = Color.White
                            )
                        }

                        // بطاقة "سجّل صوتك وقارن مع تلاوة الشيخ"
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = goldAccent.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, goldAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "🎙️ سجّل صوتك وقارن مع الشيخ لتصحيح النطق:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (isRecording) {
                                                recorder.stopRecording()
                                            } else {
                                                val hasPermission = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                ) == PackageManager.PERMISSION_GRANTED

                                                if (hasPermission) {
                                                    sheikhPlayer.stop()
                                                    recorder.startRecording(ayah.surahNumber, ayah.numberInSurah)
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isRecording) Color(0xFFC62828) else emeraldPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = if (isRecording) "إيقاف التسجيل" else "سجّل الآن")
                                    }

                                    if (hasRecording) {
                                        OutlinedButton(
                                            onClick = {
                                                if (isPlayingRecording) {
                                                    recorder.stopPlaying()
                                                } else {
                                                    sheikhPlayer.stop()
                                                    recorder.playRecording()
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = emeraldPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = if (isPlayingRecording) "إيقاف" else "استمع لتسجيلك")
                                        }
                                    }
                                }
                            }
                        }

                        // التفسير الميسر
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "التفسير الميسر:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tadabbur.tafsirMuyassar,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // تفسير الجلالين
                        if (tadabbur.tafsirJalalayn.isNotEmpty()) {
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "تفسير الجلالين:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = goldAccent
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = tadabbur.tafsirJalalayn,
                                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))

            // أزرار الحفظ في العلامات والمشاركة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onBookmark()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = goldAccent)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ العلامة")
                }

                OutlinedButton(
                    onClick = {
                        val shareText = "﴿${ayah.text}﴾\n[${ayah.surahName} - آية ${ayah.numberInSurah}]\n\nتدبر: ${tadabbur.reflections.firstOrNull() ?: ""}\n\nتطبيق سكينة"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "مشاركة الآية الكريمة"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
