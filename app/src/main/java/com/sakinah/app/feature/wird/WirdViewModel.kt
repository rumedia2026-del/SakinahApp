package com.sakinah.app.feature.wird

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.data.local.db.entities.ZikrEntity
import com.sakinah.app.data.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WirdViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository
) : ViewModel() {

    val favoriteAzkar: StateFlow<List<ZikrEntity>> = azkarRepository.getFavoriteAzkar()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavorite(zikr: ZikrEntity) {
        viewModelScope.launch {
            azkarRepository.toggleFavorite(zikr.id, zikr.isFavorite)
        }
    }

    fun addCustomZikr(text: String, count: Int) {
        viewModelScope.launch {
            val customZikr = ZikrEntity(
                id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                categoryId = 6,
                textWithTashkeel = text,
                textPlain = text,
                repeatCount = count,
                virtueText = "دعاء خاص من وردك الشخصي",
                sourceReference = "وردك الشخصي",
                authenticityGrade = null,
                audioUrl = null,
                emotionalTags = "خاص",
                isUserCustom = true,
                isFavorite = true
            )
            azkarRepository.addCustomZikr(customZikr)
        }
    }

    fun deleteCustomZikr(id: Int) {
        viewModelScope.launch {
            azkarRepository.deleteCustomZikr(id)
        }
    }
}
