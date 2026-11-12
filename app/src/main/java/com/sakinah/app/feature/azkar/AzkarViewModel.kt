package com.sakinah.app.feature.azkar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.data.local.datastore.PreferencesDataStore
import com.sakinah.app.data.local.datastore.UserPreferences
import com.sakinah.app.data.local.db.entities.CategoryEntity
import com.sakinah.app.data.local.db.entities.ZikrEntity
import com.sakinah.app.data.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AzkarListUiState(
    val category: CategoryEntity? = null,
    val azkarList: List<ZikrEntity> = emptyList(),
    val progressMap: Map<Int, Int> = emptyMap(), // zikrId -> current completed count
    val userPreferences: UserPreferences? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class AzkarViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository,
    private val preferencesDataStore: PreferencesDataStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Int = checkNotNull(savedStateHandle["categoryId"])

    private val _progressMap = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val progressMap: StateFlow<Map<Int, Int>> = _progressMap.asStateFlow()

    private val _category = MutableStateFlow<CategoryEntity?>(null)

    init {
        viewModelScope.launch {
            _category.value = azkarRepository.getCategoryById(categoryId)
        }
    }

    val uiState: StateFlow<AzkarListUiState> = combine(
        azkarRepository.getAzkarByCategory(categoryId),
        _progressMap,
        preferencesDataStore.userPreferencesFlow,
        _category
    ) { azkar, progress, prefs, cat ->
        AzkarListUiState(
            category = cat,
            azkarList = azkar,
            progressMap = progress,
            userPreferences = prefs,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AzkarListUiState(isLoading = true)
    )

    fun onZikrTapped(zikrId: Int, maxCount: Int): Boolean {
        val current = _progressMap.value[zikrId] ?: 0
        if (current < maxCount) {
            val updated = current + 1
            _progressMap.value = _progressMap.value + (zikrId to updated)
            return updated == maxCount // أتم الذكر
        }
        return false
    }

    fun resetZikrProgress(zikrId: Int) {
        _progressMap.value = _progressMap.value + (zikrId to 0)
    }

    fun toggleFavorite(zikr: ZikrEntity) {
        viewModelScope.launch {
            azkarRepository.toggleFavorite(zikr.id, zikr.isFavorite)
        }
    }

    fun toggleTashkeel(show: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setShowTashkeel(show)
        }
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            preferencesDataStore.setFontSizeMultiplier(multiplier)
        }
    }
}
