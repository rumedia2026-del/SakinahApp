package com.sakinah.app.feature.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    viewModel: QuranViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sheikhPlayer = remember { SheikhAudioPlayer(context) }
    val isAudioPlaying by sheikhPlayer.isPlaying.collectAsState()
    val audioTitle by sheikhPlayer.currentPlayingTitle.collectAsState()

    DisposableEffect(Unit) {
        viewModel.checkDownloadedSurahs(context)
        onDispose { sheikhPlayer.release() }
    }

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    var showFontSlider by remember { mutableStateOf(false) }

    // شيت التدبر والعمل لآية مختارة
    if (uiState.selectedAyahForTafsir != null) {
        TadabburAyahBottomSheet(
            ayah = uiState.selectedAyahForTafsir!!,
            sheetState = sheetState,
            onDismiss = viewModel::closeTafsir,
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

        val pageBg = if (uiState.isNightMode) Color(0xFF121619) else Color(0xFFFDFBF7)
        val pageTextColor = if (uiState.isNightMode) Color(0xFFE8EDF2) else Color(0xFF1B2E24)
        val pageBorderColor = if (uiState.isNightMode) goldAccent.copy(alpha = 0.35f) else goldAccent.copy(alpha = 0.5f)

        Scaffold(
            topBar = {
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
            },
            bottomBar = {
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
            },
            containerColor = pageBg
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showFontSlider) {
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
                                valueRange = 0.8f..1.5f,
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

                        // تصميم صفحة مصحف المدينة المنورة بالورق والإطار الزخرفي
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
                                .clip(MaterialTheme.shapes.large)
                                .border(2.dp, pageBorderColor, MaterialTheme.shapes.large)
                                .background(pageBg)
                                .padding(14.dp)
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

                                    // نص الآية
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .clickable {
                                                viewModel.showAyahTafsir(ayah)
                                                haptic.tick()
                                            }
                                            .padding(vertical = 3.dp),
                                        color = Color.Transparent
                                    ) {
                                        Text(
                                            text = "${ayah.text} ﴿${toArabicDigits(ayah.numberInSurah)}﴾",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = (21 * uiState.fontSizeMultiplier).sp,
                                                lineHeight = (42 * uiState.fontSizeMultiplier).sp
                                            ),
                                            color = pageTextColor,
                                            textAlign = TextAlign.Center,
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

                    // عنوان قائمة السور
                    item(key = "index_header") {
                        Text(
                            text = "فهرس السور (١١٤ سورة)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // قائمة السور الـ 114 مع أزرار الاستماع والتحميل أوفلاين
                    items(
                        items = uiState.filteredSurahs,
                        key = { it.number }
                    ) { surah ->
                        val isDownloaded = uiState.downloadedSurahs.contains(surah.number)
                        val isDownloading = (uiState.downloadingSurahNumber == surah.number)

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
                                            viewModel.openPage(1)
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
                                        Text(
                                            text = "${surah.revelationType} • ${toArabicDigits(surah.numberOfAyahs)} آية",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // زر تشغيل التلاوة
                                    IconButton(
                                        onClick = {
                                            if (isAudioPlaying) {
                                                sheikhPlayer.stop()
                                            } else {
                                                sheikhPlayer.playSurah(uiState.selectedSheikhId, surah.number, surah.name)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "استماع",
                                            tint = emeraldPrimary
                                        )
                                    }

                                    // زر التحميل أوفلاين
                                    if (isDownloaded) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "محمّل أوفلاين",
                                            tint = emeraldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
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

                    item(key = "quran_bottom_spacer") {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
