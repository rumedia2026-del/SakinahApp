package com.sakinah.app.feature.azkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakinah.app.core.navigation.Screen
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.feature.azkar.components.ZikrCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarListScreen(
    navController: NavController,
    viewModel: AzkarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()

    var showFontSlider by remember { mutableStateOf(false) }

    val showTashkeel = uiState.userPreferences?.showTashkeel ?: true
    val fontSizeMultiplier = uiState.userPreferences?.fontSizeMultiplier ?: 1.0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.category?.titleAr ?: "الأذكار",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // شريط تحكم سفلي سهل الوصول بإبهام اليد (Thumb-friendly Toolbar)
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
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // مفتاح تبديل التشكيل
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
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )

                        // زر تبديل إظهار شريط تعديل حجم الخط
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = uiState.azkarList,
                    key = { it.id }
                ) { zikr ->
                    val currentCount = uiState.progressMap[zikr.id] ?: 0
                    ZikrCardItem(
                        zikr = zikr,
                        currentCount = currentCount,
                        showTashkeel = showTashkeel,
                        fontSizeMultiplier = fontSizeMultiplier,
                        haptic = haptic,
                        onTap = {
                            viewModel.onZikrTapped(zikr.id, zikr.repeatCount)
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(zikr)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
