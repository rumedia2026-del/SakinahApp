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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.feature.quran.components.BookmarkHeroCard
import com.sakinah.app.feature.quran.components.KhatmahPlanCard
import com.sakinah.app.feature.quran.components.SurahListItem
import com.sakinah.app.feature.quran.components.TadabburAyahBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)
    val warmPaperBg = Color(0xFFFDFBF7)

    var showFontSlider by remember { mutableStateOf(false) }

    // نافذة التدبر المتقدم بألسنتها الأربعة
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

    when {
        // ── 1. وضع قراءة المصحف بالصفحات 604 (مصحف المدينة المنورة) ──
        uiState.isPageMode -> {
            val pageAyahs = uiState.pageAyahs
            val firstAyah = pageAyahs.firstOrNull()
            val surahName = firstAyah?.surahName ?: "المصحف الشريف"
            val juzNumber = firstAyah?.juz ?: 1

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "$surahName • الجزء $juzNumber",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "صفحة ${uiState.currentPage} من 604",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = goldAccent
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = viewModel::togglePageMode) {
                                Icon(imageVector = Icons.Default.List, contentDescription = "الفهرس")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showFontSlider = !showFontSlider }) {
                                Icon(imageVector = Icons.Default.FormatSize, contentDescription = "حجم الخط", tint = goldAccent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = warmPaperBg)
                    )
                },
                bottomBar = {
                    // شريط التنقل السفلي بين صفحات المصحف
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.prevPage()
                                    haptic.tick()
                                },
                                enabled = uiState.currentPage > 1,
                                colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("السابق")
                            }

                            Text(
                                text = "صفحة ${uiState.currentPage}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            )

                            Button(
                                onClick = {
                                    viewModel.nextPage()
                                    haptic.tick()
                                },
                                enabled = uiState.currentPage < 604,
                                colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary)
                            ) {
                                Text("التالي")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                },
                containerColor = warmPaperBg
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
                                    valueRange = 0.8f..1.6f,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = emeraldPrimary)
                        }
                    } else {
                        // إطار صفحة المصحف الشريف بالورق الدافئ
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .clip(MaterialTheme.shapes.large)
                                .border(1.dp, goldAccent.copy(alpha = 0.4f), MaterialTheme.shapes.large)
                                .background(warmPaperBg)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // نص آيات الصفحة متصلة بالرسم العثماني
                                pageAyahs.forEach { ayah ->
                                    // إذا كانت أول آية في السورة وغير التوبة، اعرض البسملة
                                    if (ayah.numberInSurah == 1 && ayah.surahNumber != 9 && ayah.surahNumber != 1) {
                                        Text(
                                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = emeraldPrimary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp)
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.small)
                                            .clickable {
                                                viewModel.showAyahTafsir(ayah)
                                                haptic.tick()
                                            }
                                            .padding(vertical = 4.dp),
                                        color = Color.Transparent
                                    ) {
                                        Text(
                                            text = "${ayah.text} ﴿${ayah.numberInSurah}﴾",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = (21 * uiState.fontSizeMultiplier).sp,
                                                lineHeight = (42 * uiState.fontSizeMultiplier).sp
                                            ),
                                            color = Color(0xFF1B2E24),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "💡 انقر على أي آية لفتح الوقفات التدبرية والعمل بالآية والاستماع للتلاوة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 2. وضع قراءة السورة الفردية ──
        uiState.selectedSurah != null -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = uiState.selectedSurah!!.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.selectedSurah!!.revelationType} • ${uiState.selectedSurah!!.numberOfAyahs} آيات",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = goldAccent
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = viewModel::closeSurahReader) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showFontSlider = !showFontSlider }) {
                                Icon(imageVector = Icons.Default.FormatSize, contentDescription = "حجم الخط", tint = goldAccent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = emeraldPrimary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (uiState.selectedSurah!!.number != 9 && uiState.selectedSurah!!.number != 1) {
                                item(key = "bismillah_header") {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = emeraldPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp)
                                    )
                                    HorizontalDivider(color = goldAccent.copy(alpha = 0.3f), thickness = 1.dp)
                                }
                            }

                            itemsIndexed(
                                items = uiState.currentAyahs,
                                key = { _, ayah -> ayah.number }
                            ) { _, ayah ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable {
                                            viewModel.showAyahTafsir(ayah)
                                            haptic.tick()
                                        },
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = goldAccent.copy(alpha = 0.15f),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${ayah.numberInSurah}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = goldAccent
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = ayah.text,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = (22 * uiState.fontSizeMultiplier).sp,
                                                lineHeight = (40 * uiState.fontSizeMultiplier).sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── 3. شاشة فهرس السور والختمة والبحث ──
        else -> {
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
                                Text("المصحف (604 ص)")
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
                        // بطاقة فتح المصحف بالصفحات 604
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
                                    Column {
                                        Text(
                                            text = "مصحف المدينة المنورة بالصفحات",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "تصفح الـ 604 صفحة كاملة مع وقفات تدبر وعمل",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = goldAccent,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        // 1. بطاقة آخر موضع قراءة
                        if (uiState.lastBookmark != null) {
                            item(key = "bookmark_card") {
                                BookmarkHeroCard(
                                    bookmark = uiState.lastBookmark!!,
                                    onClick = {
                                        val surah = uiState.surahs.find { it.number == uiState.lastBookmark!!.surahNumber }
                                        if (surah != null) viewModel.openSurah(surah)
                                    }
                                )
                            }
                        }

                        // 2. بطاقة خطة الختمة الذكية
                        item(key = "khatmah_card") {
                            KhatmahPlanCard(
                                plan = uiState.khatmahPlan,
                                onPlanClick = {
                                    haptic.tick()
                                }
                            )
                        }

                        // 3. شريط البحث في السور
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

                        // 4. عنوان الفهرس
                        item(key = "index_header") {
                            Text(
                                text = "فهرس السور (114 سورة كاملة)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // 5. قائمة السور الـ 114
                        items(
                            items = uiState.filteredSurahs,
                            key = { it.number }
                        ) { surah ->
                            SurahListItem(
                                surah = surah,
                                onClick = {
                                    viewModel.openSurah(surah)
                                    haptic.tick()
                                }
                            )
                        }

                        item(key = "quran_bottom_spacer") {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}
