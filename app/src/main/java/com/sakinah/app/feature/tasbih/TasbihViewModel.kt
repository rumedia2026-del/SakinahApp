package com.sakinah.app.feature.tasbih

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasbihPhrase(
    val id: Int,
    val arabicText: String,
    val meaning: String,
    val target: Int = 33
)

@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val progressRepository: ProgressRepository
) : ViewModel() {

    val phrases = listOf(
        TasbihPhrase(1, "سُبْحَانَ اللَّهِ", "تنزيه الله تعالى عن كل نقص وعيب", 33),
        TasbihPhrase(2, "الْحَمْدُ لِلَّهِ", "الثناء والشكر الكامل للمنعم", 33),
        TasbihPhrase(3, "اللَّهُ أَكْبَرُ", "إقرار بعظمة الخالق فوق كل شيء", 33),
        TasbihPhrase(4, "لَا إِلَهَ إِلَّا اللَّهُ", "كلمة التوحيد الخالصة", 100),
        TasbihPhrase(5, "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "طلب المغفرة والتوبة الصادقة", 100),
        TasbihPhrase(6, "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", "الصلاة والسلام على رسول الله", 100)
    )

    private val _currentPhraseIndex = MutableStateFlow(0)
    val currentPhraseIndex: StateFlow<Int> = _currentPhraseIndex.asStateFlow()

    private val _currentCount = MutableStateFlow(0)
    val currentCount: StateFlow<Int> = _currentCount.asStateFlow()

    private val _totalSessionCount = MutableStateFlow(0)
    val totalSessionCount: StateFlow<Int> = _totalSessionCount.asStateFlow()

    fun onScreenTapped(): Int {
        val newCount = _currentCount.value + 1
        _currentCount.value = newCount
        _totalSessionCount.value = _totalSessionCount.value + 1

        viewModelScope.launch {
            progressRepository.recordTasbih(_totalSessionCount.value)
        }

        return newCount
    }

    fun nextPhrase() {
        _currentPhraseIndex.value = (_currentPhraseIndex.value + 1) % phrases.size
        _currentCount.value = 0
    }

    fun prevPhrase() {
        val size = phrases.size
        _currentPhraseIndex.value = (_currentPhraseIndex.value - 1 + size) % size
        _currentCount.value = 0
    }

    fun resetCount() {
        _currentCount.value = 0
    }
}
