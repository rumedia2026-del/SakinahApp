package com.sakinah.app.feature.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val pageAyahs: List<AyahItem> = emptyList(),
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
        loadSurahs()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val list = quranRepository.getSurahsList()
            _uiState.value = _uiState.value.copy(
                surahs = list,
                filteredSurahs = list,
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isPageMode = true, currentPage = safePage)
            val ayahs = quranRepository.getAyahsForPage(safePage)
            _uiState.value = _uiState.value.copy(pageAyahs = ayahs, isLoading = false)
        }
    }

    fun nextPage() {
        val next = (_uiState.value.currentPage + 1).coerceAtMost(604)
        openPage(next)
    }

    fun prevPage() {
        val prev = (_uiState.value.currentPage - 1).coerceAtLeast(1)
        openPage(prev)
    }

    fun togglePageMode() {
        if (_uiState.value.isPageMode) {
            _uiState.value = _uiState.value.copy(isPageMode = false)
        } else {
            val pageToOpen = _uiState.value.lastBookmark?.ayahGlobalNumber?.let { 1 } ?: 1
            openPage(pageToOpen)
        }
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
}
