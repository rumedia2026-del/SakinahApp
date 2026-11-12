package com.sakinah.app.feature.azkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakinah.app.core.ui.theme.sakinahColors
import com.sakinah.app.core.utils.rememberHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ZenModeScreen(
    navController: NavController,
    viewModel: AzkarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val haptic = rememberHapticFeedback()

    // إبقاء الشاشة مضاءة أثناء القراءة الغامرة
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    var isAutoScrolling by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    // حلقة التمرير التلقائي البطيء
    LaunchedEffect(isAutoScrolling) {
        if (isAutoScrolling) {
            val speed = uiState.userPreferences?.autoScrollSpeed ?: 1.0f
            while (isActive && isAutoScrolling) {
                listState.scrollBy(1.5f * speed)
                delay(16) // ~60fps
            }
        }
    }

    val zenBg = MaterialTheme.sakinahColors.zenBackground
    val zenText = MaterialTheme.sakinahColors.zenTextColor
    val fontSizeMultiplier = uiState.userPreferences?.fontSizeMultiplier ?: 1.0f
    val showTashkeel = uiState.userPreferences?.showTashkeel ?: true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(zenBg)
            .clickable { showControls = !showControls }
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 70.dp),
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                itemsIndexed(uiState.azkarList) { index, zikr ->
                    val currentCount = uiState.progressMap[zikr.id] ?: 0
                    val displayText = if (showTashkeel) zikr.textWithTashkeel else zikr.textPlain

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                viewModel.onZikrTapped(zikr.id, zikr.repeatCount)
                                haptic.tasbihTap()
                            }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (24 * fontSizeMultiplier).sp,
                                lineHeight = (44 * fontSizeMultiplier).sp
                            ),
                            color = zenText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // عداد شفاف هادئ
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "التكرار: $currentCount / ${zikr.repeatCount}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // شريط التحكم العلوي القابل للإخفاء
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(zenBg.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = zenText
                    )
                }

                Text(
                    text = "وضع التركيز (Zen Mode)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = zenText
                )

                // زر تشغيل/إيقاف التمرير التلقائي
                IconButton(onClick = { isAutoScrolling = !isAutoScrolling }) {
                    Icon(
                        imageVector = if (isAutoScrolling) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "التمرير التلقائي",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
