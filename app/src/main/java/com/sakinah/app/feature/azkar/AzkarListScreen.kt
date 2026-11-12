package com.sakinah.app.feature.azkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakinah.app.core.navigation.Screen
import com.sakinah.app.core.utils.audio.SakinahAudioSpeaker
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.feature.azkar.components.ZikrCardItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarListScreen(
    navController: NavController,
    viewModel: AzkarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // تهيئة القارئ الصوتي
    val speaker = remember { SakinahAudioSpeaker(context) }
    DisposableEffect(Unit) {
        onDispose { speaker.shutdown() }
    }

    var showFontSlider by remember { mutableStateOf(false) }

    val showTashkeel = uiState.userPreferences?.showTashkeel ?: true
    val fontSizeMultiplier = uiState.userPreferences?.fontSizeMultiplier ?: 1.0f

    // حساب نسبة إنجاز الورد كاملاً
    val totalAzkar = uiState.azkarList.size
    val completedAzkarCount = uiState.azkarList.count { zikr ->
        (uiState.progressMap[zikr.id] ?: 0) >= zikr.repeatCount
    }
    val targetTotalProgress = if (totalAzkar > 0) completedAzkarCount.toFloat() / totalAzkar.toFloat() else 0f
    val animatedTotalProgress by animateFloatAsState(targetValue = targetTotalProgress, label = "TotalWirdProgress")

    val goldAccent = Color(0xFFC8A951)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.category?.titleAr ?: "الأذكار",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (totalAzkar > 0) {
                                Text(
                                    text = "إنجاز الورد: $completedAzkarCount من $totalAzkar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = goldAccent
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    },
                    actions = {
                        // زر الانتقال المباشر لوضع التركيز الغامر (Zen Mode)
                        IconButton(
                            onClick = {
                                uiState.category?.let {
                                    navController.navigate(Screen.ZenMode.createRoute(it.id))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = "وضع التركيز",
                                tint = goldAccent
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // شريط التقدم العلوي للورد الكامل
                if (totalAzkar > 0) {
                    LinearProgressIndicator(
                        progress = { animatedTotalProgress },
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = goldAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (showFontSlider) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حجم الخط",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Slider(
                                value = fontSizeMultiplier,
                                onValueChange = viewModel::setFontSizeMultiplier,
                                valueRange = 0.8f..1.4f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = goldAccent,
                                    activeTrackColor = goldAccent
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = showTashkeel,
                            onClick = { viewModel.toggleTashkeel(!showTashkeel) },
                            label = {
                                Text(
                                    text = if (showTashkeel) "التشكيل: مفعّل" else "التشكيل: مخفي",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.TextFields,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = goldAccent.copy(alpha = 0.2f),
                                selectedLabelColor = goldAccent
                            )
                        )

                        FilterChip(
                            selected = showFontSlider,
                            onClick = { showFontSlider = !showFontSlider },
                            label = {
                                Text(
                                    text = "تكبير الخط",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = uiState.azkarList,
                    key = { _, zikr -> zikr.id }
                ) { index, zikr ->
                    val currentCount = uiState.progressMap[zikr.id] ?: 0
                    ZikrCardItem(
                        zikr = zikr,
                        currentCount = currentCount,
                        showTashkeel = showTashkeel,
                        fontSizeMultiplier = fontSizeMultiplier,
                        haptic = haptic,
                        onTap = {
                            val completed = viewModel.onZikrTapped(zikr.id, zikr.repeatCount)
                            // الانتقال التلقائي السلس للذكر التالي فور اكتمال التكرارات
                            if (completed && index < uiState.azkarList.lastIndex) {
                                coroutineScope.launch {
                                    delay(400) // وقفة لطيفة للاستشعار بالإتمام
                                    listState.animateScrollToItem(index + 1)
                                }
                            }
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(zikr)
                        },
                        onSpeak = {
                            val speechText = if (showTashkeel) zikr.textWithTashkeel else zikr.textPlain
                            speaker.speak(speechText)
                        }
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
