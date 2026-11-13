package com.sakinah.app.feature.emotional

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakinah.app.core.ui.theme.SakinahTheme
import com.sakinah.app.core.ui.theme.SakinahThemeMode
import com.sakinah.app.core.utils.HapticFeedback
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.data.local.db.entities.ZikrEntity
import com.sakinah.app.feature.azkar.components.ZikrCardItem
import com.sakinah.app.feature.emotional.components.BreathingCalmDialog
import com.sakinah.app.feature.emotional.components.MunajatNotebookDialog

@Composable
fun EmotionalCompanionScreen(
    navController: NavController,
    viewModel: EmotionalViewModel = hiltViewModel()
) {
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val intensityLevel by viewModel.intensityLevel.collectAsState()
    val showBreathingDialog by viewModel.showBreathingDialog.collectAsState()
    val azkarList by viewModel.suggestedAzkar.collectAsState()
    val activeMunajat by viewModel.activeMunajat.collectAsState()
    val answeredMunajat by viewModel.answeredMunajat.collectAsState()
    val haptic = rememberHapticFeedback()

    val progressMap = remember { mutableStateMapOf<Int, Int>() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val sheikhPlayer = remember { com.sakinah.app.core.utils.audio.SheikhAudioPlayer(context) }
    var isBackgroundAudioPlaying by remember { mutableStateOf(false) }
    var showMunajatDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            sheikhPlayer.release()
        }
    }

    if (showBreathingDialog) {
        BreathingCalmDialog(
            onDismiss = { viewModel.dismissBreathingDialog() }
        )
    }

    if (showMunajatDialog) {
        MunajatNotebookDialog(
            activeList = activeMunajat,
            answeredList = answeredMunajat,
            onAddMunajat = { title, content, tag ->
                viewModel.addMunajat(title, content, tag)
            },
            onMarkAnswered = { id ->
                viewModel.markMunajatAnswered(id)
            },
            onDelete = { entity ->
                viewModel.deleteMunajat(entity)
            },
            onDismiss = { showMunajatDialog = false }
        )
    }

    EmotionalCompanionContent(
        emotions = viewModel.emotions,
        selectedEmotion = selectedEmotion,
        intensityLevel = intensityLevel,
        azkarList = azkarList,
        progressMap = progressMap,
        haptic = haptic,
        isBackgroundAudioPlaying = isBackgroundAudioPlaying,
        onToggleBackgroundAudio = { audioUrl ->
            if (isBackgroundAudioPlaying) {
                sheikhPlayer.stop()
                isBackgroundAudioPlaying = false
            } else {
                sheikhPlayer.playUrl(audioUrl)
                isBackgroundAudioPlaying = true
            }
        },
        onSpeak = { zikrText ->
            val matching = azkarList.find { it.textWithTashkeel == zikrText || it.textPlain == zikrText }
            if (matching != null && !matching.audioUrl.isNullOrBlank()) {
                sheikhPlayer.playUrl(matching.audioUrl)
            }
        },
        onSelectEmotion = { emotion ->
            viewModel.selectEmotion(emotion)
            haptic.tick()
        },
        onSetIntensity = viewModel::setIntensityLevel,
        onClearSelection = viewModel::clearSelection,
        onOpenMunajat = { showMunajatDialog = true },
        onZikrTap = { zikrId, maxCount ->
            val current = progressMap[zikrId] ?: 0
            if (current < maxCount) {
                progressMap[zikrId] = current + 1
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionalCompanionContent(
    emotions: List<EmotionItem>,
    selectedEmotion: EmotionItem?,
    intensityLevel: Int,
    azkarList: List<ZikrEntity>,
    progressMap: Map<Int, Int>,
    haptic: HapticFeedback?,
    isBackgroundAudioPlaying: Boolean = false,
    onToggleBackgroundAudio: (String) -> Unit = {},
    onSpeak: (String) -> Unit = {},
    onSelectEmotion: (EmotionItem) -> Unit,
    onSetIntensity: (Int) -> Unit = {},
    onClearSelection: () -> Unit,
    onOpenMunajat: () -> Unit = {},
    onZikrTap: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedEmotion?.title ?: "مشاعري وبلسم القلوب",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (selectedEmotion != null) {
                        IconButton(onClick = onClearSelection) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    }
                },
                actions = {
                    // زر دفتر المناجاة الخاص
                    IconButton(onClick = onOpenMunajat) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "دفتر المناجاة الخاص",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (selectedEmotion == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenMunajat),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "دفتر المناجاة الخاص و«دعوات استُجيبت ✨»",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "مساحتك الآمنة والسرية لبث همومك لله وتوثيق كرمه معك",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(emotions, key = { it.key }) { emotion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onSelectEmotion(emotion) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = emotion.emoji,
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = emotion.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = emotion.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        } else {
            val prescription = remember(selectedEmotion.key, intensityLevel) {
                EmotionalPrescriptionHelper.getPrescription(selectedEmotion.key, intensityLevel)
            }
            val propheticStory = remember(selectedEmotion.key) {
                PropheticStoriesHelper.getStoryForEmotion(selectedEmotion.key)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. منتقي شدة الشعور (كيف يبدو قلبك الآن؟)
                item(key = "intensity_selector") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "كيف يبدو ثقل هذا الشعور في قلبك الآن؟",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1 to "عابر / خفيف", 2 to "متوسط الأثر", 3 to "شديد ومضيق").forEach { (lvl, title) ->
                                    val isSelected = intensityLevel == lvl
                                    Button(
                                        onClick = { onSetIntensity(lvl) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        Text(title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. روشتة السكينة الفورية (Direct Ayah + Du'a + Action + Recitation)
                item(key = "sakinah_prescription") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌿", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "روشتة السكينة الفورية لقلبك",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // الآية الشافية
                            Text(
                                text = "﴿ ${prescription.ayahText} ﴾",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F3D2E),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            )
                            Text(
                                text = prescription.ayahSurah,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB8860B),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                            )

                            // الدعاء المأثور
                            Text(
                                text = "« ${prescription.duaText} »",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2C2620),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = prescription.duaSource,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            // العمل السريع (Instant Action)
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "خطوة عملية فورية الآن:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                    Text(
                                        text = prescription.instantAction,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // التلاوة المهدئة
                            FilledTonalButton(
                                onClick = { onToggleBackgroundAudio(prescription.recitationUrl) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBackgroundAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBackgroundAudioPlaying) "إيقاف التلاوة المهدئة" else prescription.recitationTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3. كرت "مواقف الأنبياء مع نفس الشعور"
                if (propheticStory != null) {
                    item(key = "prophetic_story") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF9C4).copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✨", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "موقف الأنبياء مع نفس الشعور",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF795548)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = propheticStory.prophetName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E342E)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = propheticStory.situation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "﴿ ${propheticStory.ayah} ﴾",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F3D2E)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "💡 العبرة لقلبك: ${propheticStory.lesson}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                item(key = "emotional_header") {
                    Text(
                        text = "أدعية مختارة تلامس قلبك وتفرّج ما أهمك:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(azkarList, key = { it.id }) { zikr ->
                    val currentCount = progressMap[zikr.id] ?: 0
                    if (haptic != null) {
                        ZikrCardItem(
                            zikr = zikr,
                            currentCount = currentCount,
                            showTashkeel = true,
                            fontSizeMultiplier = 1.0f,
                            haptic = haptic,
                            onTap = { onZikrTap(zikr.id, zikr.repeatCount) },
                            onToggleFavorite = {},
                            onSpeak = { onSpeak(zikr.textWithTashkeel) }
                        )
                    }
                }

                item(key = "emotional_bottom_spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
