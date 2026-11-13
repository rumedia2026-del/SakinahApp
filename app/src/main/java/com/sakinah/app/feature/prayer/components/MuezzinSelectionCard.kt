package com.sakinah.app.feature.prayer.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import com.sakinah.app.feature.prayer.MuadhinCatalog
import com.sakinah.app.feature.prayer.MuadhinItem

@Composable
fun MuezzinSelectionCard(
    selectedMuezzinId: String,
    onSelectMuezzin: (String) -> Unit,
    sheikhPlayer: SheikhAudioPlayer? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlayingId by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun stopPreview() {
        try {
            previewPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            previewPlayer = null
            isPlayingId = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopPreview()
        }
    }

    val currentMuezzin = remember(selectedMuezzinId) {
        MuadhinCatalog.getMuadhinById(selectedMuezzinId)
    }

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = emeraldPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(goldAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = goldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الأذان والمؤذنون",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "المؤذن المختار: ${currentMuezzin.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = goldAccent
                        )
                    }
                }

                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تغيير المؤذن", color = emeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = currentMuezzin.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                stopPreview()
                showDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = emeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اختر صوت الأذان الشريف", fontWeight = FontWeight.Bold, color = emeraldPrimary)
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MuadhinCatalog.muadhins) { muadhin ->
                        val isSelected = (muadhin.id == selectedMuezzinId)
                        val isPlaying = (isPlayingId == muadhin.id)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectMuezzin(muadhin.id)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) emeraldPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) goldAccent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = muadhin.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) emeraldPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "محدد",
                                                tint = goldAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = muadhin.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            stopPreview()
                                        } else {
                                            stopPreview()
                                            try {
                                                val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                                                am?.let { audio ->
                                                    val cur = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                                                    if (cur == 0) {
                                                        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (max * 0.7f).toInt().coerceAtLeast(1), 0)
                                                    }
                                                }
                                                val mp = MediaPlayer.create(context, muadhin.rawResId)
                                                if (mp != null) {
                                                    val audioAttributes = AudioAttributes.Builder()
                                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                        .build()
                                                    mp.setAudioAttributes(audioAttributes)
                                                    mp.setVolume(1.0f, 1.0f)
                                                    mp.setOnCompletionListener {
                                                        isPlayingId = null
                                                        it.release()
                                                        previewPlayer = null
                                                    }
                                                    mp.start()
                                                    previewPlayer = mp
                                                    isPlayingId = muadhin.id
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                stopPreview()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "معاينة الصوت",
                                        tint = goldAccent
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        stopPreview()
                        showDialog = false
                    }
                ) {
                    Text("حفظ واختيار", color = emeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
