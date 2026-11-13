package com.sakinah.app.feature.quran

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.sakinah.app.feature.quran.components.ReciterSelectorBar
import com.sakinah.app.feature.quran.components.SurahItemCard
import com.sakinah.app.feature.quran.components.TadabburAyahBottomSheet
import kotlinx.coroutines.launch

private fun toArabicDigits(number: Int): String {
    val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { if (it in '0'..'9') arabicChars[it - '0'] else it }.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    val sheikhPlayer = remember { SheikhAudioPlayer(context) }
    val isPlayingAudio by sheikhPlayer.isPlaying.collectAsState()
    val currentlyPlayingSurah by sheikhPlayer.currentlyPlayingSurah.collectAsState()
    val currentAudioTitle by sheikhPlayer.currentPlayingTitle.collectAsState()

    DisposableEffect(Unit) {
        onDispose { sheikhPlayer.release() }
    }

    var showFontSlider by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var selectedThemeIndex by remember { mutableIntStateOf(1) } // Default Warm Paper

    var isTajweedEnabled by remember { mutableStateOf(true) }
    var isHifzModeActive by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var showFullScreenExitBar by remember { mutableStateOf(false) }
    var unmaskedWordKeys by remember { mutableStateOf(setOf<String>()) }
    var highlightedAyahNumber by remember { mutableStateOf<Int?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val goldAccent = if (uiState.isNightMode) Color(0xFFFCD34D) else Color(0xFFB8860B)
    val emeraldPrimary = Color(0xFF0F3D2E)

    // إدارة ملء الشاشة الغامر
    val activity = context as? Activity
    LaunchedEffect(isFullScreen) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullScreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

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
                viewModel.closeTafsir()
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

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "$surahName • الجزء ${toArabicDigits(juzNum)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isNightMode) Color.White else emeraldPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
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
                            // 1. وضع ملء الشاشة الغامر
                            IconButton(onClick = {
                                isFullScreen = true
                                haptic.tick()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "ملء الشاشة",
                                    tint = goldAccent
                                )
                            }

                            // 2. التجويد الملون
                            IconButton(onClick = {
                                isTajweedEnabled = !isTajweedEnabled
                                haptic.tick()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "التجويد الملون",
                                    tint = if (isTajweedEnabled) goldAccent else goldAccent.copy(alpha = 0.4f)
                                )
                            }

                            // 3. الوضع الليلي لقراءة المصحف
                            IconButton(onClick = viewModel::toggleNightMode) {
                                Icon(
                                    imageVector = if (uiState.isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "الوضع الليلي",
                                    tint = goldAccent
                                )
                            }

                            // 4. قائمة الخيارات الإضافية (المحفظ، الثيمات، حجم الخط)
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "المزيد من الخيارات",
                                        tint = goldAccent
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("المحفّظ الذكي (إخفاء الكلمات)") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = if (isHifzModeActive) goldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        onClick = {
                                            isHifzModeActive = !isHifzModeActive
                                            showMoreMenu = false
                                            haptic.tick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("سمات وتصميم المصحف (٦ سمات)") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ColorLens,
                                                contentDescription = null,
                                                tint = goldAccent
                                            )
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showThemeDialog = true
                                            haptic.tick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("تعديل حجم الخط") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.FormatSize,
                                                contentDescription = null,
                                                tint = goldAccent
                                            )
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showFontSlider = !showFontSlider
                                            haptic.tick()
                                        }
                                    )
                                }
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
                            val pageMargin = if (isFullScreen) 2.dp else 8.dp
                            val pageInnerPadding = if (isFullScreen) 8.dp else 12.dp

                            // تصميم صفحة مصحف تدبر وعمل الموحدة
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(pageMargin)
                                    .clip(MaterialTheme.shapes.large)
                                    .border(2.dp, pageBorderColor, MaterialTheme.shapes.large)
                                    .background(pageBg)
                                    .clickable {
                                        if (isFullScreen) {
                                            showFullScreenExitBar = !showFullScreenExitBar
                                        } else {
                                            isFullScreen = true
                                        }
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

                                    // فحص إذا كانت بداية سورة جديدة في الصفحة
                                    val newSurahAyah = ayahsOfPage.firstOrNull { it.numberInSurah == 1 }
                                    if (newSurahAyah != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(goldAccent.copy(alpha = 0.12f))
                                                .border(1.dp, goldAccent.copy(alpha = 0.4f), MaterialTheme.shapes.small)
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "❖ ${newSurahAyah.surahName} ❖",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = goldAccent
                                            )
                                        }

                                        if (newSurahAyah.surahNumber != 9 && newSurahAyah.surahNumber != 1) {
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

                                    // تجميع آيات الصفحة في كتلة نصية متراصة واحدة (Continuous Flow Block)
                                    val pageContinuousText = remember(
                                        ayahsOfPage,
                                        isTajweedEnabled,
                                        pageTextColor,
                                        goldAccent,
                                        isHifzModeActive,
                                        unmaskedWordKeys
                                    ) {
                                        MushafPageLayoutHelper.buildPageAnnotatedString(
                                            ayahs = ayahsOfPage,
                                            isTajweedEnabled = isTajweedEnabled,
                                            textColor = pageTextColor,
                                            goldSeparatorColor = goldAccent,
                                            isHifzMode = isHifzModeActive,
                                            unmaskedWordKeys = unmaskedWordKeys
                                        )
                                    }

                                    @OptIn(ExperimentalFoundationApi::class)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    if (isHifzModeActive) {
                                                        val newKeys = unmaskedWordKeys.toMutableSet()
                                                        ayahsOfPage.forEach { a ->
                                                            val words = a.text.split(" ")
                                                            words.indices.forEach { idx -> newKeys.add("${a.number}_$idx") }
                                                        }
                                                        unmaskedWordKeys = newKeys
                                                        haptic.tick()
                                                    } else if (isFullScreen) {
                                                        showFullScreenExitBar = !showFullScreenExitBar
                                                        haptic.tick()
                                                    }
                                                },
                                                onLongClick = {
                                                    val targetAyah = ayahsOfPage.firstOrNull()
                                                    if (targetAyah != null) {
                                                        highlightedAyahNumber = targetAyah.number
                                                        viewModel.showAyahTafsir(targetAyah)
                                                        haptic.completion()
                                                    }
                                                }
                                            ),
                                        color = Color.Transparent
                                    ) {
                                        Text(
                                            text = pageContinuousText,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = (22 * uiState.fontSizeMultiplier).sp,
                                                lineHeight = (36 * uiState.fontSizeMultiplier).sp
                                            ),
                                            textAlign = TextAlign.Justify,
                                            modifier = Modifier.fillMaxWidth()
                                        )
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

                // شريط علوي شفاف للخروج من ملء الشاشة يظهر بالنقر ولا يحجب آيات الزوايا
                AnimatedVisibility(
                    visible = isFullScreen && showFullScreenExitBar,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المصحف الشريف • ملء الشاشة",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    isFullScreen = false
                                    showFullScreenExitBar = false
                                    haptic.tick()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.FullscreenExit, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إنهاء ملء الشاشة", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
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
                            text = "المصحف الشريف والتلاوات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = emeraldPrimary
                        )
                    },
                    actions = {
                        Button(
                            onClick = viewModel::togglePageMode,
                            colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, tint = emeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح المصحف (٦٠٤ ص)", color = emeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // بطاقة آخر موضع قراءة (Bookmark Hero Card)
                item(key = "quran_bookmark_card") {
                    BookmarkHeroCard(
                        bookmark = uiState.bookmark,
                        onContinueReading = {
                            if (uiState.bookmark != null) {
                                viewModel.jumpToPage(uiState.bookmark!!.pageNumber)
                            } else {
                                viewModel.jumpToPage(1)
                            }
                        }
                    )
                }

                // شريط اختيار الشيخ القارئ للتلاوة الصوتية
                item(key = "quran_reciter_selector") {
                    ReciterSelectorBar(
                        selectedReciterId = uiState.selectedSheikhId,
                        onSelectReciter = viewModel::selectSheikh
                    )
                }

                // مشغل التلاوة الصوتي الحي عند تشغيل سورة
                if (isPlayingAudio && currentlyPlayingSurah != null) {
                    item(key = "quran_active_audio_player") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = emeraldPrimary),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = goldAccent,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentAudioTitle ?: "تلاوة خاشعة",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "جاري الاستماع الآن...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = goldAccent
                                        )
                                    }
                                }

                                IconButton(onClick = { sheikhPlayer.stop() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "إيقاف",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // شريط البحث في السور
                item(key = "quran_search_field") {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = goldAccent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // عنوان قائمة السور
                item(key = "surahs_header") {
                    Text(
                        text = "سور القرآن الكريم (${uiState.surahs.size} سورة):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // قائمة السور
                items(uiState.surahs, key = { it.number }) { surah ->
                    val isSurahDownloaded = QuranAudioDownloader.isSurahDownloaded(
                        context = context,
                        sheikhId = uiState.selectedSheikhId,
                        surahNumber = surah.number
                    )

                    SurahItemCard(
                        surah = surah,
                        isSelected = (currentlyPlayingSurah == surah.number),
                        isDownloaded = isSurahDownloaded,
                        onSurahClick = {
                            viewModel.jumpToPage(surah.pageStart)
                            haptic.tick()
                        },
                        onPlayAudio = {
                            if (isPlayingAudio && currentlyPlayingSurah == surah.number) {
                                sheikhPlayer.pause()
                            } else {
                                sheikhPlayer.playSurah(
                                    sheikhId = uiState.selectedSheikhId,
                                    surahNumber = surah.number,
                                    surahName = surah.nameArabic
                                )
                            }
                            haptic.tick()
                        },
                        onDownloadClick = {
                            scope.launch {
                                QuranAudioDownloader.downloadSurah(
                                    context = context,
                                    sheikhId = uiState.selectedSheikhId,
                                    surahNumber = surah.number
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
