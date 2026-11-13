package com.sakinah.app.feature.emotional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.data.local.db.dao.MunajatDao
import com.sakinah.app.data.local.db.entities.MunajatEntity
import com.sakinah.app.data.local.db.entities.ZikrEntity
import com.sakinah.app.data.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmotionItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val emoji: String
)

@HiltViewModel
class EmotionalViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository,
    private val munajatDao: MunajatDao
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

    private val _intensityLevel = MutableStateFlow(2) // 1: خفيف, 2: متوسط, 3: شديد
    val intensityLevel: StateFlow<Int> = _intensityLevel.asStateFlow()

    private val _showBreathingDialog = MutableStateFlow(false)
    val showBreathingDialog: StateFlow<Boolean> = _showBreathingDialog.asStateFlow()

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

    // دفتر المناجاة
    val activeMunajat = munajatDao.getActiveMunajat().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val answeredMunajat = munajatDao.getAnsweredMunajat().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectEmotion(emotion: EmotionItem) {
        _selectedEmotion.value = emotion
        // إذا كان الشعور قلق أو خوف أو حزن أو هم أو غضب، نقترح دائرة التنفس والاسترخاء
        if (emotion.key in listOf("قلق", "خوف", "حزن", "هم", "غضب")) {
            _showBreathingDialog.value = true
        }
    }

    fun setIntensityLevel(level: Int) {
        _intensityLevel.value = level
    }

    fun dismissBreathingDialog() {
        _showBreathingDialog.value = false
    }

    fun clearSelection() {
        _selectedEmotion.value = null
    }

    fun addMunajat(title: String, content: String, emotionTag: String?) {
        viewModelScope.launch {
            munajatDao.insert(
                MunajatEntity(
                    title = title,
                    content = content,
                    emotionTag = emotionTag
                )
            )
        }
    }

    fun markMunajatAnswered(id: Long, notes: String?) {
        viewModelScope.launch {
            munajatDao.markAsAnswered(id, System.currentTimeMillis(), notes)
        }
    }

    fun deleteMunajat(munajat: MunajatEntity) {
        viewModelScope.launch {
            munajatDao.delete(munajat)
        }
    }
}
