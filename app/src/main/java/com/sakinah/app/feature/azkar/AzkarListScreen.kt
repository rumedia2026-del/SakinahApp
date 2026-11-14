package com.sakinah.app.feature.azkar

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakinah.app.core.navigation.Screen
import com.sakinah.app.core.utils.audio.SheikhAudioPlayer
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.data.local.db.entities.ZikrEntity
import java.util.Locale
import com.sakinah.app.feature.azkar.components.ZikrCardItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun getZikrAudioUrls(zikr: ZikrEntity): List<String> {
    return AzkarAudioManager.getAudioUrlsForZikr(zikr)
}

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

    val sheikhPlayer = remember { SheikhAudioPlayer(context) }
    val isAudioPlaying by sheikhPlayer.isPlaying.collectAsState()
    var currentlyPlayingZikrId by remember { mutableStateOf<Int?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            sheikhPlayer.release()
        }
    }

    var showFontSlider by remember { mutableStateOf(false) }
    var isDeafModeActive by remember { mutableStateOf(false) }
    var flashState by remember { mutableStateOf(false) }
    var isPlayingFullWird by remember { mutableStateOf(false) }

    val flashAlpha by animateFloatAsState(
        targetValue = if (flashState) 0.35f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "DeafModeFlash"
    )

    val showTashkeel = uiState.userPreferences?.showTashkeel ?: true
    val fontSizeMultiplier = uiState.userPreferences?.fontSizeMultiplier ?: 1.0f

    val totalAzkar = uiState.azkarList.size
    val completedAzkarCount = uiState.azkarList.count { zikr ->
        (uiState.progressMap[zikr.id] ?: 0) >= zikr.repeatCount
    }
    val targetTotalProgress = if (totalAzkar > 0) completedAzkarCount.toFloat() / totalAzkar.toFloat() else 0f
    val animatedTotalProgress by animateFloatAsState(targetValue = targetTotalProgress, label = "TotalWirdProgress")

    val goldAccent = Color(0xFFC8A951)
    val deepEmerald = Color(0xFF0F3D2E)

    val snackbarHostState = remember { SnackbarHostState() }
    val audioErrorMessage by sheikhPlayer.errorMessage.collectAsState()

    androidx.compose.runtime.LaunchedEffect(audioErrorMessage) {
        audioErrorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            sheikhPlayer.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            // زر الاستماع للورد كاملاً بصوت الشيخ الحقيقي
                            IconButton(
                                onClick = {
                                    if (isPlayingFullWird) {
                                        sheikhPlayer.stop()
                                        isPlayingFullWird = false
                                        currentlyPlayingZikrId = null
                                    } else {
                                        isPlayingFullWird = true
                                        coroutineScope.launch {
                                            for ((idx, zikr) in uiState.azkarList.withIndex()) {
                                                if (!isPlayingFullWird) break
                                                currentlyPlayingZikrId = zikr.id
                                                listState.animateScrollToItem(idx)
                                                val urls = getZikrAudioUrls(zikr)
                                                if (urls.isNotEmpty()) {
                                                    sheikhPlayer.playUrls(urls)
                                                    delay(4500)
                                                }
                                            }
                                            isPlayingFullWird = false
                                            currentlyPlayingZikrId = null
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlayingFullWird) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                                    contentDescription = "استماع للورد كاملاً",
                                    tint = goldAccent
                                )
                            }

                            // زر الانتقال لوضع التركيز الغامر
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
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.TextFields,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )

                            FilterChip(
                                selected = isDeafModeActive,
                                onClick = {
                                    isDeafModeActive = !isDeafModeActive
                                    if (isDeafModeActive) haptic.milestone()
                                },
                                label = {
                                    Text(
                                        text = if (isDeafModeActive) "وضع فاقدي السمع ✓" else "وضع فاقدي السمع",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.HearingDisabled,
                                        contentDescription = null,
                                        tint = if (isDeafModeActive) goldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                label = { Text("الخط", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(16.dp))
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

                                if (isDeafModeActive) {
                                    coroutineScope.launch {
                                        flashState = true
                                        delay(140)
                                        flashState = false
                                    }
                                }

                                if (completed && index < uiState.azkarList.lastIndex) {
                                    coroutineScope.launch {
                                        delay(400)
                                        listState.animateScrollToItem(index + 1)
                                    }
                                }
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(zikr)
                            },
                            isPlayingAudio = (isAudioPlaying && currentlyPlayingZikrId == zikr.id),
                            onPlayAudio = if (getZikrAudioUrls(zikr).isNotEmpty()) {
                                {
                                    if (isAudioPlaying && currentlyPlayingZikrId == zikr.id) {
                                        sheikhPlayer.stop()
                                        currentlyPlayingZikrId = null
                                    } else {
                                        val urls = getZikrAudioUrls(zikr)
                                        if (urls.isNotEmpty()) {
                                            currentlyPlayingZikrId = zikr.id
                                            sheikhPlayer.playUrls(urls) {
                                                currentlyPlayingZikrId = null
                                            }
                                        }
                                    }
                                }
                            } else null
                        )
                    }

                    // بطاقة ختام الورد المبارك (End-of-Wird Completion Card)
                    if (totalAzkar > 0 && completedAzkarCount >= totalAzkar) {
                        item(key = "wird_completion_card") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.large)
                                    .border(1.dp, goldAccent, MaterialTheme.shapes.large),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = deepEmerald)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🎉 هنيئاً لك! أتممت الورد كاملاً بحمد الله",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = goldAccent
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "دعاء كفارة المجلس:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "«سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ»",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            val shareText = "الحمد لله، أتممت ورد ${uiState.category?.titleAr ?: "الأذكار"} كاملاً عبر تطبيق سكينة.\n«سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا أَنْتَ، أَسْتَغْفِرُكَ وَأَتُوبُ إِلَيْكَ»"
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "مشاركة إنجاز الورد"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = goldAccent)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("مشاركة إنجاز اليوم المبارك", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }

        // وميض بصري للشاشة لوضع فاقدي السمع
        if (flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(goldAccent.copy(alpha = flashAlpha))
            )
        }
    }
}
