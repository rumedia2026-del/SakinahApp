package com.sakinah.app.feature.quran

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.core.utils.ArabicNormalizer
import com.sakinah.app.core.utils.normalizeArabicForSearch
import com.sakinah.app.feature.quran.audio.QuranAudioDownloader
import com.sakinah.app.feature.quran.audio.QuranSheikhManager
import com.sakinah.app.feature.quran.model.AyahItem
import com.sakinah.app.feature.quran.model.Bookmark
import com.sakinah.app.feature.quran.model.JuzItem
import com.sakinah.app.feature.quran.model.KhatmahPlan
import com.sakinah.app.feature.quran.model.SurahHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuranUiState(
    val surahs: List<SurahHeader> = emptyList(),
    val filteredSurahs: List<SurahHeader> = emptyList(),
    val matchedAyahs: List<AyahItem> = emptyList(),
    val matchedPageNumber: Int? = null,
    val matchedJuz: JuzItem? = null,
    val currentAyahs: List<AyahItem> = emptyList(),
    val selectedSurah: SurahHeader? = null,
    val selectedAyahForTafsir: AyahItem? = null,
    val isPageMode: Boolean = false,
    val currentPage: Int = 1,
    val pagesMap: Map<Int, List<AyahItem>> = emptyMap(),
    val isNightMode: Boolean = false,
    val selectedSheikhId: String = "afs",
    val downloadingSurahNumber: Int? = null,
    val downloadProgress: Float = 0f,
    val downloadedSurahs: Set<Int> = emptySet(),
    val lastBookmark: Bookmark? = null,
    val khatmahPlan: KhatmahPlan = KhatmahPlan(),
    val searchQuery: String = "",
    val selectedFilterTab: String = "ALL", // ALL, MECCAN, MEDINAN, DOWNLOADED, JUZ, BOOKMARK
    val juzList: List<JuzItem> = emptyList(),
    val fontSizeMultiplier: Float = 1.0f,
    val isLoading: Boolean = false
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState(isLoading = true))
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    private var allAyahsCache: List<AyahItem> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val list = quranRepository.getSurahsList()
            val allPages = quranRepository.getAllPages()
            val juzList = quranRepository.getJuzList()
            val allAyahs = quranRepository.getAllAyahs()
            allAyahsCache = allAyahs
            _uiState.value = _uiState.value.copy(
                surahs = list,
                filteredSurahs = list,
                pagesMap = allPages,
                juzList = juzList,
                isLoading = false,
                lastBookmark = Bookmark(1, "سُورَةُ ٱلْفَاتِحَةِ", 1, 1)
            )
        }
    }

    fun setFilterTab(tab: String) {
        _uiState.value = _uiState.value.copy(selectedFilterTab = tab)
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    private fun applyFilters() {
        val rawQuery = _uiState.value.searchQuery.trim()
        val tab = _uiState.value.selectedFilterTab
        val downloaded = _uiState.value.downloadedSurahs

        var base = _uiState.value.surahs

        base = when (tab) {
            "MECCAN" -> base.filter { it.revelationType == "مكية" }
            "MEDINAN" -> base.filter { it.revelationType == "مدنية" }
            "DOWNLOADED" -> base.filter { downloaded.contains(it.number) }
            else -> base
        }

        if (rawQuery.isBlank()) {
            _uiState.value = _uiState.value.copy(
                filteredSurahs = base,
                matchedAyahs = emptyList(),
                matchedPageNumber = null,
                matchedJuz = null
            )
            return
        }

        // 1. الانتقال المباشر برقم الصفحة (1..604)
        val pageNum = rawQuery.toIntOrNull()?.takeIf { it in 1..604 }

        // 2. الانتقال المباشر برقم الجزء (1..30 أو "جزء 15")
        val normQuery = rawQuery.normalizeArabicForSearch()
        val juzMatch = _uiState.value.juzList.firstOrNull { juz ->
            val normJuzName = juz.name.normalizeArabicForSearch()
            normJuzName.contains(normQuery) ||
                    rawQuery == juz.number.toString() && (rawQuery.toIntOrNull() in 1..30) ||
                    normQuery == "جزء ${juz.number}" ||
                    normQuery == "الجزء ${juz.number}"
        }

        // 3. مطابقة السور الـ 114 بمرونة وبدون تشكيل
        val filtered = base.filter { surah ->
            ArabicNormalizer.matchesSmart(surah.name, rawQuery) ||
                    surah.englishName.contains(rawQuery, ignoreCase = true) ||
                    surah.number.toString() == rawQuery
        }

        // 4. البحث داخل نصوص الآيات كلها (6236 آية) بدون تشكيل
        val matchedAyahs = if (normQuery.length >= 2) {
            allAyahsCache.filter { ayah ->
                ayah.text.normalizeArabicForSearch().contains(normQuery)
            }.take(50)
        } else {
            emptyList()
        }

        _uiState.value = _uiState.value.copy(
            filteredSurahs = filtered,
            matchedAyahs = matchedAyahs,
            matchedPageNumber = pageNum,
            matchedJuz = juzMatch
        )
    }

    fun openSurah(surah: SurahHeader) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedSurah = surah, isPageMode = false)
            val ayahs = quranRepository.getAyahsForSurah(surah.number)
            _uiState.value = _uiState.value.copy(currentAyahs = ayahs, isLoading = false)
        }
    }

    fun openPage(page: Int) {
        val safePage = page.coerceIn(1, 604)
        _uiState.value = _uiState.value.copy(isPageMode = true, currentPage = safePage, selectedSurah = null)
    }

    fun togglePageMode() {
        _uiState.value = _uiState.value.copy(isPageMode = !_uiState.value.isPageMode, selectedSurah = null)
    }

    fun toggleNightMode() {
        _uiState.value = _uiState.value.copy(isNightMode = !_uiState.value.isNightMode)
    }

    fun setSelectedSheikh(sheikhId: String) {
        _uiState.value = _uiState.value.copy(selectedSheikhId = sheikhId)
    }

    fun checkDownloadedSurahs(context: Context) {
        val downloaded = mutableSetOf<Int>()
        for (i in 1..114) {
            if (QuranAudioDownloader.isSurahDownloaded(context, _uiState.value.selectedSheikhId, i)) {
                downloaded.add(i)
            }
        }
        _uiState.value = _uiState.value.copy(downloadedSurahs = downloaded)
    }

    fun downloadSurah(context: Context, surahNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                downloadingSurahNumber = surahNumber,
                downloadProgress = 0f
            )
            val result = QuranAudioDownloader.downloadSurah(
                context = context,
                sheikhId = _uiState.value.selectedSheikhId,
                surahNumber = surahNumber,
                onProgress = { progress ->
                    _uiState.value = _uiState.value.copy(downloadProgress = progress)
                }
            )

            if (result.isSuccess) {
                val updated = _uiState.value.downloadedSurahs + surahNumber
                _uiState.value = _uiState.value.copy(
                    downloadingSurahNumber = null,
                    downloadProgress = 0f,
                    downloadedSurahs = updated
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    downloadingSurahNumber = null,
                    downloadProgress = 0f
                )
            }
        }
    }

    fun deleteDownloadedSurah(context: Context, surahNumber: Int) {
        QuranAudioDownloader.deleteSurah(context, _uiState.value.selectedSheikhId, surahNumber)
        val updated = _uiState.value.downloadedSurahs - surahNumber
        _uiState.value = _uiState.value.copy(downloadedSurahs = updated)
    }

    fun closeSurahReader() {
        _uiState.value = _uiState.value.copy(selectedSurah = null, isPageMode = false, currentAyahs = emptyList())
    }

    fun showAyahTafsir(ayah: AyahItem) {
        _uiState.value = _uiState.value.copy(selectedAyahForTafsir = ayah)
    }

    fun closeTafsir() {
        _uiState.value = _uiState.value.copy(selectedAyahForTafsir = null)
    }

    fun saveBookmark(ayah: AyahItem) {
        val bookmark = Bookmark(
            surahNumber = ayah.surahNumber,
            surahName = ayah.surahName,
            ayahNumberInSurah = ayah.numberInSurah,
            ayahGlobalNumber = ayah.number
        )
        _uiState.value = _uiState.value.copy(lastBookmark = bookmark)
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        _uiState.value = _uiState.value.copy(fontSizeMultiplier = multiplier.coerceIn(0.8f, 1.8f))
    }

    fun updateKhatmahPlan(days: Int) {
        val pagesPerDay = (604 / days.coerceAtLeast(1)).coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            khatmahPlan = KhatmahPlan(totalDays = days, pagesPerDay = pagesPerDay)
        )
    }
}
