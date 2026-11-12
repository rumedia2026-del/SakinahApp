package com.sakinah.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.data.local.db.entities.CategoryEntity
import com.sakinah.app.data.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

enum class SmartContextType(
    val title: String,
    val subtitle: String,
    val targetCategoryId: Int,
    val iconEmoji: String
) {
    MORNING("أذكار الصباح", "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ", 1, "🌅"),
    EVENING("أذكار المساء", "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ", 2, "🌇"),
    NIGHT("أذكار النوم", "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي", 4, "🌙"),
    FRIDAY("سُنن ويوم الجمعة", "أكثروا من الصلاة على النبي ﷺ وقراءة الكهف", 1, "🕌"),
    GENERAL("ذكر الله وسكينته", "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", 3, "✨")
}

data class HomeUiState(
    val smartContext: SmartContextType = SmartContextType.MORNING,
    val categories: List<CategoryEntity> = emptyList(),
    val filteredCategories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val smartContext = determineSmartContext()

    val uiState: StateFlow<HomeUiState> = combine(
        azkarRepository.getAllCategories(),
        _searchQuery
    ) { categories, query ->
        val filtered = if (query.isBlank()) {
            categories
        } else {
            categories.filter { it.titleAr.contains(query.trim(), ignoreCase = true) }
        }
        HomeUiState(
            smartContext = smartContext,
            categories = categories,
            filteredCategories = filtered,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(smartContext = smartContext, isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun determineSmartContext(): SmartContextType {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // التحقق من يوم الجمعة (من مغرب الخميس وحتى مغرب الجمعة - تقريباً خلال ساعات الجمعة)
        if (dayOfWeek == Calendar.FRIDAY && hour in 6..17) {
            return SmartContextType.FRIDAY
        }

        return when (hour) {
            in 4..11 -> SmartContextType.MORNING
            in 15..19 -> SmartContextType.EVENING
            in 20..23, in 0..3 -> SmartContextType.NIGHT
            else -> SmartContextType.GENERAL
        }
    }
}
