package com.sakinah.app.feature.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Search
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
import com.sakinah.app.feature.quran.components.AyahTafsirBottomSheet
import com.sakinah.app.feature.quran.components.BookmarkHeroCard
import com.sakinah.app.feature.quran.components.KhatmahPlanCard
import com.sakinah.app.feature.quran.components.SurahListItem

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

    var showFontSlider by remember { mutableStateOf(false) }

    // نافذة التفسير الميسر والاستماع
    if (uiState.selectedAyahForTafsir != null) {
        AyahTafsirBottomSheet(
            ayah = uiState.selectedAyahForTafsir!!,
            sheetState = sheetState,
            onDismiss = viewModel::closeTafsir,
            onBookmark = {
                viewModel.saveBookmark(uiState.selectedAyahForTafsir!!)
                haptic.completion()
            }
        )
    }

    if (uiState.selectedSurah != null) {
        // شاشة قراءة السورة والآيات (المصحف الشريف)
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // البسملة (لغير سورة التوبة وسورة الفاتحة إذا كانت مدرجة كآية أولى)
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

                        // عرض الآيات الكريمة
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
                                    // رقم الآية
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

                                    // نص الآية
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

                        item(key = "bottom_reading_space") {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    } else {
        // شاشة فهرس السور والختمة والبحث
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
                                // تبديل خطة الختمة بين 30 و 15 يوماً
                                val newDays = if (uiState.khatmahPlan.totalDays == 30) 15 else 30
                                viewModel.updateKhatmahPlan(newDays)
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
