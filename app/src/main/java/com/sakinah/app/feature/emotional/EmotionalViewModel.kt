package com.sakinah.app.feature.emotional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.data.local.db.entities.ZikrEntity
import com.sakinah.app.data.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class EmotionItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val emoji: String
)

@HiltViewModel
class EmotionalViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository
) : ViewModel() {

    val emotions = listOf(
        EmotionItem("قلق", "قلق وتوتر", "ألا بذكر الله تطمئن القلوب", "🌧️"),
        EmotionItem("حزن", "حزن وضيق صدر", "إني مسني الضر وأنت أرحم الراحمين", "💔"),
        EmotionItem("خوف", "خوف وعدم أمان", "حسبي الله لا إله إلا هو", "🛡️"),
        EmotionItem("هم", "هموم وأعباء الحياة", "اللهم إني أعوذ بك من الهم والحزن", "⚖️"),
        EmotionItem("رزق", "طلب الرزق والبركة", "وارزقنا وأنت خير الرازقين", "🌱"),
        EmotionItem("شكر", "شكر وامتنان", "لئن شكرتم لأزيدنكم", "🌸"),
        EmotionItem("استخارة", "حيرة وتردد", "استخر ربك واستفته يهدك", "🧭"),
        EmotionItem("مرض", "مرض وألم", "وإذا مرضت فهو يشفين", "🩺"),
        EmotionItem("غضب", "غضب وضيق", "والكاظمين الغيظ والعافين عن الناس", "🔥")
    )

    private val _selectedEmotion = MutableStateFlow<EmotionItem?>(null)
    val selectedEmotion: StateFlow<EmotionItem?> = _selectedEmotion.asStateFlow()

    val suggestedAzkar: StateFlow<List<ZikrEntity>> = _selectedEmotion.flatMapLatest { emotion ->
        if (emotion == null) {
            MutableStateFlow(emptyList())
        } else {
            azkarRepository.getAzkarByEmotionalTag(emotion.key)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectEmotion(emotion: EmotionItem) {
        _selectedEmotion.value = emotion
    }

    fun clearSelection() {
        _selectedEmotion.value = null
    }
}
