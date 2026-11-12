package com.sakinah.app.feature.quran.components

import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.feature.quran.model.AyahItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahTafsirBottomSheet(
    ayah: AyahItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // شريط العنوان: اسم السورة ورقم الآية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${ayah.surahName} • الآية (${ayah.numberInSurah})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = emeraldPrimary
                    )
                    Text(
                        text = "الجزء ${ayah.juz} • الصفحة ${ayah.page}",
                        style = MaterialTheme.typography.labelSmall,
                        color = goldAccent
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // نص الآية الكريمة بالرسم العثماني
            Text(
                text = ayah.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp, lineHeight = 38.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // أزرار الإجراءات: تلاوة صوتية، وضع علامة، ومشاركة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // زر الاستماع بصوت مشاري العفاسي
                Button(
                    onClick = {
                        if (isPlaying) {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPlaying = false
                        } else {
                            try {
                                val audioUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${ayah.number}.mp3"
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(audioUrl)
                                    prepareAsync()
                                    setOnPreparedListener {
                                        start()
                                        isPlaying = true
                                    }
                                    setOnCompletionListener {
                                        isPlaying = false
                                    }
                                }
                            } catch (e: Exception) {
                                isPlaying = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPlaying) "إيقاف" else "استماع")
                }

                // زر حفظ موضع القراءة (Bookmark)
                OutlinedButton(
                    onClick = {
                        onBookmark()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = goldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ الموضع")
                }

                // زر مشاركة الآية كنص
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${ayah.text}\n[${ayah.surahName}: ${ayah.numberInSurah}]\n\nالتفسير الميسر: ${ayah.tafsir}\n\nتطبيق سكينة 🕌"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الآية"))
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = goldAccent.copy(alpha = 0.3f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // التفسير الميسر
            Text(
                text = "التفسير الميسر:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = goldAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ayah.tafsir ?: "التفسير الميسر غير متوفر لهذه الآية.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 26.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
