package com.sakinah.app.feature.quran

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.feature.quran.audio.QuranAudioDownloader
import com.sakinah.app.feature.quran.audio.QuranSheikhManager
import com.sakinah.app.feature.quran.model.AyahItem
import com.sakinah.app.feature.quran.model.Bookmark
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
    val fontSizeMultiplier: Float = 1.0f,
    val isLoading: Boolean = false
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState(isLoading = true))
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val list = quranRepository.getSurahsList()
            val allPages = quranRepository.getAllPages()
            _uiState.value = _uiState.value.copy(
                surahs = list,
                filteredSurahs = list,
                pagesMap = allPages,
                isLoading = false,
                lastBookmark = Bookmark(1, "سُورَةُ ٱلْفَاتِحَةِ", 1, 1)
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.surahs
        } else {
            _uiState.value.surahs.filter {
                it.name.contains(query.trim(), ignoreCase = true) ||
                        it.englishName.contains(query.trim(), ignoreCase = true) ||
                        it.number.toString() == query.trim()
            }
        }
        _uiState.value = _uiState.value.copy(searchQuery = query, filteredSurahs = filtered)
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
        _uiState.value = _uiState.value.copy(fontSizeMultiplier = multiplier.coerceIn(0.8f, 1.6f))
    }

    fun updateKhatmahPlan(days: Int) {
        val pagesPerDay = (604 / days.coerceAtLeast(1)).coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            khatmahPlan = KhatmahPlan(totalDays = days, pagesPerDay = pagesPerDay)
        )
    }
}
