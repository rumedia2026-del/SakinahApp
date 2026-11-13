package com.sakinah.app.feature.emotional

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun EmotionalCompanionScreen(
    navController: NavController,
    viewModel: EmotionalViewModel = hiltViewModel()
) {
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val azkarList by viewModel.suggestedAzkar.collectAsState()
    val haptic = rememberHapticFeedback()

    val progressMap = remember { mutableStateMapOf<Int, Int>() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val sheikhPlayer = remember { com.sakinah.app.core.utils.audio.SheikhAudioPlayer(context) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { sheikhPlayer.release() }
    }

    EmotionalCompanionContent(
        emotions = viewModel.emotions,
        selectedEmotion = selectedEmotion,
        azkarList = azkarList,
        progressMap = progressMap,
        haptic = haptic,
        onSpeak = { zikrText ->
            // إذا كان الذكر يحتوي على رابط صوتي مسجل
            val matching = azkarList.find { it.textWithTashkeel == zikrText || it.textPlain == zikrText }
            if (matching != null && !matching.audioUrl.isNullOrBlank()) {
                sheikhPlayer.playUrl(matching.audioUrl)
            }
        },
        onSelectEmotion = { emotion ->
            viewModel.selectEmotion(emotion)
            haptic.tick()
        },
        onClearSelection = viewModel::clearSelection,
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
    azkarList: List<ZikrEntity>,
    progressMap: Map<Int, Int>,
    haptic: HapticFeedback?,
    onSpeak: (String) -> Unit = {},
    onSelectEmotion: (EmotionItem) -> Unit,
    onClearSelection: () -> Unit,
    onZikrTap: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedEmotion?.title ?: "بماذا تشعر الآن؟",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

// ═══════════════════════════════════════════════════════════════════
//  COMPOSE PREVIEWS — الوضع النهاري والوضع الليلي
// ═══════════════════════════════════════════════════════════════════

private val sampleEmotions = listOf(
    EmotionItem("قلق", "قلق وتوتر", "ألا بذكر الله تطمئن القلوب", "🌧️"),
    EmotionItem("حزن", "حزن وضيق صدر", "إني مسني الضر وأنت أرحم الراحمين", "💔"),
    EmotionItem("خوف", "خوف وعدم أمان", "حسبي الله لا إله إلا هو", "🛡️"),
    EmotionItem("رزق", "طلب الرزق والبركة", "وارزقنا وأنت خير الرازقين", "🌱")
)

@Preview(name = "Emotional Companion - Light Warm Paper", showBackground = true)
@Composable
private fun EmotionalCompanionLightPreview() {
    SakinahTheme(themeMode = SakinahThemeMode.WARM_PAPER) {
        EmotionalCompanionContent(
            emotions = sampleEmotions,
            selectedEmotion = null,
            azkarList = emptyList(),
            progressMap = emptyMap(),
            haptic = null,
            onSelectEmotion = {},
            onClearSelection = {},
            onZikrTap = { _, _ -> }
        )
    }
}

@Preview(name = "Emotional Companion - Dark Slate", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EmotionalCompanionDarkPreview() {
    SakinahTheme(themeMode = SakinahThemeMode.SOFT_SLATE_DARK) {
        EmotionalCompanionContent(
            emotions = sampleEmotions,
            selectedEmotion = sampleEmotions.first(),
            azkarList = listOf(
                ZikrEntity(
                    id = 601,
                    categoryId = 6,
                    textWithTashkeel = "اللَّهُمَّ إِنِّي عَبْدُكَ ابْنُ عَبْدِكَ...",
                    textPlain = "اللهم إني عبدك ابن عبدك...",
                    repeatCount = 1,
                    virtueText = "أذهب الله همه وحزنه وأبدله مكانه فرجاً.",
                    sourceReference = "مسند أحمد",
                    authenticityGrade = "صحيح",
                    audioUrl = null,
                    emotionalTags = "قلق,حزن"
                )
            ),
            progressMap = mapOf(601 to 0),
            haptic = null,
            onSelectEmotion = {},
            onClearSelection = {},
            onZikrTap = { _, _ -> }
        )
    }
}
