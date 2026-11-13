package com.sakinah.app.feature.quran

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakinah.app.core.utils.audio.SheikhAudioPlayer
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.feature.quran.audio.QuranAudioDownloader
import com.sakinah.app.feature.quran.audio.QuranSheikhManager
import com.sakinah.app.feature.quran.components.BookmarkHeroCard
import com.sakinah.app.feature.quran.components.KhatmahPlanCard
import com.sakinah.app.feature.quran.components.TadabburAyahBottomSheet
import com.sakinah.app.feature.quran.model.AyahItem
import kotlinx.coroutines.launch

private fun toArabicDigits(number: Int): String {
    val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { if (it in '0'..'9') arabicChars[it - '0'] else it }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = hiltViewModel(),
    onFullScreenChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isFullScreen by remember { mutableStateOf(false) }

    val window = (context as? Activity)?.window
    DisposableEffect(isFullScreen, uiState.isPageMode) {
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (isFullScreen && uiState.isPageMode) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            if (window != null) {
                val ic = WindowCompat.getInsetsController(window, window.decorView)
                ic.show(WindowInsetsCompat.Type.systemBars())
            }
            onFullScreenChanged(false)
        }
    }

    LaunchedEffect(isFullScreen, uiState.isPageMode) {
        onFullScreenChanged(isFullScreen && uiState.isPageMode)
    }

    val sheikhPlayer = remember { SheikhAudioPlayer(context) }
    val isAudioPlaying by sheikhPlayer.isPlaying.collectAsState()
    val isBuffering by sheikhPlayer.isBuffering.collectAsState()
    val currentlyPlayingSurah by sheikhPlayer.currentlyPlayingSurah.collectAsState()
    val audioTitle by sheikhPlayer.currentPlayingTitle.collectAsState()

    DisposableEffect(Unit) {
        viewModel.checkDownloadedSurahs(context)
        onDispose { sheikhPlayer.release() }
    }

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    var showFontSlider by remember { mutableStateOf(false) }
    var isTajweedEnabled by remember { mutableStateOf(false) }
    var selectedThemeIndex by remember { mutableIntStateOf(0) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var isHifzModeActive by remember { mutableStateOf(false) }
    var unmaskedWordKeys by remember { mutableStateOf(setOf<String>()) }
    var highlightedAyahNumber by remember { mutableStateOf<Int?>(null) }

    val currentTheme = QuranThemeManager.getTheme(selectedThemeIndex)

    // نافذة اختيار ثيمات المصحف الـ 6
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("اختر ثيم المصحف الشريف", fontWeight = FontWeight.Bold, color = emeraldPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuranThemeManager.themes.forEach { theme ->
                        val isSelected = selectedThemeIndex == theme.id
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = theme.backgroundColor,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) goldAccent else theme.borderColor.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedThemeIndex = theme.id
                                    showThemeDialog = false
                                    haptic.tick()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = theme.nameAr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = theme.textColor
                                )
                                if (isSelected) {
                                    Text("✓ المختار", color = goldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("إغلاق", color = emeraldPrimary)
                }
            }
        )
    }

    // شيت التدبر والعمل لآية مختارة
    if (uiState.selectedAyahForTafsir != null) {
        TadabburAyahBottomSheet(
            ayah = uiState.selectedAyahForTafsir!!,
            sheetState = sheetState,
            onDismiss = {
                highlightedAyahNumber = null
                viewModel::closeTafsir.invoke()
            },
            onBookmark = {
                viewModel.saveBookmark(uiState.selectedAyahForTafsir!!)
                haptic.completion()
            }
        )
    }

    if (uiState.isPageMode) {
        // ════════════════════════════════════════════════════════════════════════
        // 1. مصحف تدبر وعمل (604 صفحة منفصلة 100% أوفلاين مع تقليب صفحات RTL)
        // ════════════════════════════════════════════════════════════════════════
        val pagerState = rememberPagerState(
            initialPage = (uiState.currentPage - 1).coerceIn(0, 603),
            pageCount = { 604 }
        )

        val pageBg = if (uiState.isNightMode) Color(0xFF121619) else currentTheme.backgroundColor
        val pageTextColor = if (uiState.isNightMode) Color(0xFFE8EDF2) else currentTheme.textColor
        val pageBorderColor = if (uiState.isNightMode) goldAccent.copy(alpha = 0.35f) else currentTheme.borderColor.copy(alpha = 0.5f)

        Scaffold(
            topBar = {
                AnimatedVisibility(visible = !isFullScreen) {
                    TopAppBar(
                        title = {
                            val currentPageNum = pagerState.currentPage + 1
                            val firstAyah = uiState.pagesMap[currentPageNum]?.firstOrNull()
                            val surahName = firstAyah?.surahName ?: "المصحف الشريف"
                            val juzNum = firstAyah?.juz ?: 1

                            Column {
                                Text(
                                    text = "$surahName • الجزء ${toArabicDigits(juzNum)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isNightMode) Color.White else emeraldPrimary
                                )
                                Text(
                                    text = "صفحة ${toArabicDigits(currentPageNum)} من ٦٠٤",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = goldAccent
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = viewModel::togglePageMode) {
                                Icon(imageVector = Icons.Default.List, contentDescription = "فهرس السور", tint = goldAccent)
                            }
                        },
                        actions = {
                            // تلوين أحكام التجويد
                            IconButton(onClick = {
                                isTajweedEnabled = !isTajweedEnabled
                                haptic.tick()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "التجويد الملون",
                                    tint = if (isTajweedEnabled) goldAccent else goldAccent.copy(alpha = 0.5f)
                                )
                            }

                            // مبدل الثيمات الـ 6
                            IconButton(onClick = { showThemeDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = "ثيمات المصحف",
                                    tint = goldAccent
                                )
                            }

                            // وضع المحفظ الذكي (Word Masking)
                            IconButton(onClick = {
                                isHifzModeActive = !isHifzModeActive
                                haptic.tick()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "المحفظ الذكي",
                                    tint = if (isHifzModeActive) goldAccent else goldAccent.copy(alpha = 0.5f)
                                )
                            }

                            // وضع ملء الشاشة الغامر
                            IconButton(onClick = {
                                isFullScreen = !isFullScreen
                                haptic.tick()
                            }) {
                                Icon(
                                    imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullScreen) "الخروج من ملء الشاشة" else "ملء الشاشة",
                                    tint = goldAccent
                                )
                            }

                            // الوضع الليلي لقراءة المصحف
                            IconButton(onClick = viewModel::toggleNightMode) {
                                Icon(
                                    imageVector = if (uiState.isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "الوضع الليلي",
                                    tint = goldAccent
                                )
                            }

                            // حجم الخط
                            IconButton(onClick = { showFontSlider = !showFontSlider }) {
                                Icon(imageVector = Icons.Default.FormatSize, contentDescription = "حجم الخط", tint = goldAccent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = pageBg)
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(visible = !isFullScreen) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        val currentPageNum = pagerState.currentPage + 1
                        val pageAyahs = uiState.pagesMap[currentPageNum] ?: emptyList()
                        val firstAyah = pageAyahs.firstOrNull()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (firstAyah != null) {
                                        viewModel.showAyahTafsir(firstAyah)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary)
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, tint = goldAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تدبر وعمل (صفحة ${toArabicDigits(currentPageNum)})")
                            }

                            Text(
                                text = "صفحة ${toArabicDigits(currentPageNum)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            )
                        }
                    }
                }
            },
            containerColor = pageBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isFullScreen) PaddingValues(0.dp) else innerPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (showFontSlider && !isFullScreen) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("حجم الخط", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.width(10.dp))
                                Slider(
                                    value = uiState.fontSizeMultiplier,
                                    onValueChange = viewModel::setFontSizeMultiplier,
                                    valueRange = 0.8f..1.8f,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // التصفح الأفقي من اليمين لليسار (RTL) لـ 604 صفحات
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { pageIdx ->
                            val pageNum = pageIdx + 1
                            val ayahsOfPage = uiState.pagesMap[pageNum] ?: emptyList()
                            val pageMargin = if (isFullScreen) 2.dp else 10.dp
                            val pageInnerPadding = if (isFullScreen) 8.dp else 14.dp

                            // تصميم صفحة مصحف المدينة المنورة بالورق والإطار الزخرفي
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(pageMargin)
                                    .clip(MaterialTheme.shapes.large)
                                    .border(2.dp, pageBorderColor, MaterialTheme.shapes.large)
                                    .background(pageBg)
                                    .clickable {
                                        isFullScreen = !isFullScreen
                                        haptic.tick()
                                    }
                                    .padding(pageInnerPadding)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // ترويسة الصفحة
                                    val topAyah = ayahsOfPage.firstOrNull()
                                    if (topAyah != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = topAyah.surahName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                            Text(
                                                text = "الجزء ${toArabicDigits(topAyah.juz)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        HorizontalDivider(color = pageBorderColor.copy(alpha = 0.4f), thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // الآيات الكريمة للصفحة
                                    ayahsOfPage.forEach { ayah ->
                                        // إذا كانت الآية بداية سورة (numberInSurah == 1)
                                        if (ayah.numberInSurah == 1) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp)
                                                    .clip(MaterialTheme.shapes.small)
                                                    .background(goldAccent.copy(alpha = 0.12f))
                                                    .border(1.dp, goldAccent.copy(alpha = 0.4f), MaterialTheme.shapes.small)
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "❖ ${ayah.surahName} ❖",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = goldAccent
                                                )
                                            }

                                            if (ayah.surahNumber != 9 && ayah.surahNumber != 1) {
                                                Text(
                                                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = goldAccent,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(bottom = 6.dp)
                                                )
                                            }
                                        }

                                        // نص الآية الكريمة بتنسيق مضبوط وتلوين التجويد والضغط المطول للتدبر
                                        val isHighlighted = (highlightedAyahNumber == ayah.number)
                                        val amberAyahNum = Color(0xFFB8860B)

                                        val formattedAyahText = remember(ayah.text, isTajweedEnabled, pageTextColor, isHifzModeActive, unmaskedWordKeys) {
                                            if (isHifzModeActive) {
                                                // وضع المحفظ الذكي: تمويه الكلمات لاختبار الحفظ
                                                val words = ayah.text.split(" ")
                                                val masked = words.mapIndexed { idx, w ->
                                                    val key = "${ayah.number}_$idx"
                                                    if (unmaskedWordKeys.contains(key)) w else "████"
                                                }.joinToString(" ")
                                                androidx.compose.ui.text.buildAnnotatedString {
                                                    append(masked)
                                                    append(" ")
                                                    val startNum = length
                                                    append("﴿${toArabicDigits(ayah.numberInSurah)}﴾")
                                                    addStyle(
                                                        androidx.compose.ui.text.SpanStyle(color = amberAyahNum, fontWeight = FontWeight.Bold),
                                                        startNum,
                                                        length
                                                    )
                                                }
                                            } else {
                                                val annotated = TajweedAnnotator.annotate(ayah.text, isTajweedEnabled, pageTextColor)
                                                androidx.compose.ui.text.buildAnnotatedString {
                                                    append(annotated)
                                                    append(" ")
                                                    val startNum = length
                                                    append("﴿${toArabicDigits(ayah.numberInSurah)}﴾")
                                                    addStyle(
                                                        androidx.compose.ui.text.SpanStyle(color = amberAyahNum, fontWeight = FontWeight.Bold),
                                                        startNum,
                                                        length
                                                    )
                                                }
                                            }
                                        }

                                        @OptIn(ExperimentalFoundationApi::class)
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(MaterialTheme.shapes.small)
                                                .combinedClickable(
                                                    onClick = {
                                                        if (isHifzModeActive) {
                                                            // كشف كلمات الآية عند النقر
                                                            val words = ayah.text.split(" ")
                                                            val newKeys = unmaskedWordKeys.toMutableSet()
                                                            words.indices.forEach { idx -> newKeys.add("${ayah.number}_$idx") }
                                                            unmaskedWordKeys = newKeys
                                                            haptic.tick()
                                                        } else {
                                                            isFullScreen = !isFullScreen
                                                            haptic.tick()
                                                        }
                                                    },
                                                    onLongClick = {
                                                        highlightedAyahNumber = ayah.number
                                                        viewModel.showAyahTafsir(ayah)
                                                        haptic.completion()
                                                    }
                                                )
                                                .padding(vertical = 3.dp),
                                            color = if (isHighlighted) goldAccent.copy(alpha = 0.2f) else Color.Transparent
                                        ) {
                                            Text(
                                                text = formattedAyahText,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontSize = (22 * uiState.fontSizeMultiplier).sp,
                                                    lineHeight = (44 * uiState.fontSizeMultiplier).sp
                                                ),
                                                textAlign = TextAlign.Justify,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // رقم الصفحة في إطار زخرفي أسفل الصفحة
                                    Box(
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.extraSmall)
                                            .border(1.dp, goldAccent.copy(alpha = 0.5f), MaterialTheme.shapes.extraSmall)
                                            .padding(horizontal = 12.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "— ${toArabicDigits(pageNum)} —",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = goldAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // زر عائم شبه شفاف في الأسفل للخروج من ملء الشاشة بدون أي تداخل مع ترويسة الجزء
                if (isFullScreen) {
                    IconButton(
                        onClick = {
                            isFullScreen = false
                            haptic.tick()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 24.dp, end = 20.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "الخروج من ملء الشاشة",
                            tint = goldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    } else {
        // ════════════════════════════════════════════════════════════════════════
        // 2. فهرس السور والقراء والتحميل أوفلاين
        // ════════════════════════════════════════════════════════════════════════
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "المصحف الشريف",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = emeraldPrimary
                        )
                    },
                    actions = {
                        Button(
                            onClick = viewModel::togglePageMode,
                            colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = goldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("المصحف (٦٠٤ ص)")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = emeraldPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // بطاقة الدخول لمصحف تدبر وعمل
                    item(key = "page_mushaf_banner") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .clickable { viewModel.openPage(1) },
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = emeraldPrimary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "مصحف تدبر وعمل (٦٠٤ صفحة)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "تصفح صفحات مصحف المدينة المنورة أوفلاين مع وقفات التدبر والعمل",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = goldAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    // بطاقة اختيار القارئ وتنزيل التلاوات
                    item(key = "sheikh_reciter_selector") {
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "اختر القارئ لتلاوة القرآن (أونلاين وتحميل أوفلاين):",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(QuranSheikhManager.RECITERS) { sheikh ->
                                        val isSelected = (sheikh.id == uiState.selectedSheikhId)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.setSelectedSheikh(sheikh.id)
                                                viewModel.checkDownloadedSurahs(context)
                                            },
                                            label = { Text(sheikh.nameAr, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = goldAccent.copy(alpha = 0.2f),
                                                selectedLabelColor = goldAccent
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // بطاقة آخر موضع قراءة
                    if (uiState.lastBookmark != null) {
                        item(key = "bookmark_card") {
                            BookmarkHeroCard(
                                bookmark = uiState.lastBookmark!!,
                                onClick = {
                                    viewModel.openPage(1)
                                }
                            )
                        }
                    }

                    // بطاقة خطة الختمة
                    item(key = "khatmah_card") {
                        KhatmahPlanCard(
                            plan = uiState.khatmahPlan,
                            onPlanClick = {
                                val newDays = if (uiState.khatmahPlan.totalDays == 30) 15 else 30
                                viewModel.updateKhatmahPlan(newDays)
                                haptic.tick()
                            }
                        )
                    }

                    // شريط البحث في السور
                    item(key = "search_surah_bar") {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = goldAccent,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    // شريط التصفية والتبويب الموحد للمصحف
                    item(key = "filter_tabs_row") {
                        val filterTabs = listOf(
                            "ALL" to "الكل",
                            "MECCAN" to "مكية",
                            "MEDINAN" to "مدنية",
                            "DOWNLOADED" to "المحمّلة أوفلاين",
                            "JUZ" to "الأجزاء الـ ٣٠"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filterTabs) { (key, label) ->
                                val isSelected = (uiState.selectedFilterTab == key)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setFilterTab(key) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = emeraldPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    if (uiState.selectedFilterTab == "JUZ") {
                        // عرض فهرس الأجزاء الـ 30
                        item(key = "juz_header") {
                            Text(
                                text = "فهرس الأجزاء (٣٠ جزءاً)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        items(
                            items = uiState.juzList,
                            key = { "juz_${it.number}" }
                        ) { juz ->
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        viewModel.openPage(juz.startPage)
                                        haptic.tick()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(goldAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = toArabicDigits(juz.number),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = juz.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "يبدأ من ${juz.startSurahName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.extraSmall)
                                            .background(emeraldPrimary.copy(alpha = 0.08f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "صفحة ${toArabicDigits(juz.startPage)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = emeraldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // عنوان قائمة السور
                        item(key = "index_header") {
                            Text(
                                text = "فهرس السور (${toArabicDigits(uiState.filteredSurahs.size)} سورة)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // قائمة السور الـ 114 مع أزرار الاستماع المنفصلة والتحميل أوفلاين
                        items(
                            items = uiState.filteredSurahs,
                            key = { it.number }
                        ) { surah ->
                            val isDownloaded = uiState.downloadedSurahs.contains(surah.number)
                            val isDownloading = (uiState.downloadingSurahNumber == surah.number)
                            val isThisSurahPlaying = (isAudioPlaying && currentlyPlayingSurah == surah.number)
                            val isThisSurahBuffering = (isBuffering && currentlyPlayingSurah == surah.number)

                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.openPage(surah.startPage)
                                                haptic.tick()
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(goldAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = toArabicDigits(surah.number),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = surah.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${surah.revelationType} • ${toArabicDigits(surah.numberOfAyahs)} آية",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "• ص ${toArabicDigits(surah.startPage)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = goldAccent,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // زر تشغيل التلاوة المخصص لهذه السورة فقط
                                        IconButton(
                                            onClick = {
                                                if (isThisSurahPlaying) {
                                                    sheikhPlayer.stop()
                                                } else {
                                                    sheikhPlayer.playSurah(uiState.selectedSheikhId, surah.number, surah.name)
                                                }
                                            }
                                        ) {
                                            if (isThisSurahBuffering) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = emeraldPrimary
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (isThisSurahPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = "استماع",
                                                    tint = if (isThisSurahPlaying) goldAccent else emeraldPrimary
                                                )
                                            }
                                        }

                                        // زر التحميل أوفلاين
                                        if (isDownloaded) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteDownloadedSurah(context, surah.number)
                                                    haptic.tick()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "محمّل أوفلاين - انقر للحذف",
                                                    tint = emeraldPrimary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        } else if (isDownloading) {
                                            Box(contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(
                                                    progress = { uiState.downloadProgress },
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp,
                                                    color = goldAccent
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    viewModel.downloadSurah(context, surah.number)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "تحميل للاستماع أوفلاين",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item(key = "quran_bottom_spacer") {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
