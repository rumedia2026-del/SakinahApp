package com.sakinah.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.core.ui.theme.SakinahThemeMode
import com.sakinah.app.data.local.datastore.PreferencesDataStore
import com.sakinah.app.data.local.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesDataStore.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences(
                themeMode = SakinahThemeMode.WARM_PAPER,
                fontSizeMultiplier = 1.0f,
                showTashkeel = true,
                hapticFeedbackEnabled = true,
                deafHapticVisualMode = false,
                autoScrollSpeed = 1.0f,
                selectedCityName = "رشيد - البحيرة",
                calculationMethod = "EGYPTIAN",
                prayerAlarmEnabled = true,
                morningNotificationTime = "05:30",
                eveningNotificationTime = "17:00"
            )
        )

    fun setThemeMode(mode: SakinahThemeMode) {
        viewModelScope.launch {
            preferencesDataStore.setThemeMode(mode)
        }
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            preferencesDataStore.setFontSizeMultiplier(multiplier)
        }
    }

    fun toggleTashkeel(show: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setShowTashkeel(show)
        }
    }

    fun toggleHaptic(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setHapticEnabled(enabled)
        }
    }

    fun toggleDeafMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setDeafHapticVisualMode(enabled)
        }
    }

    fun setSelectedCity(cityName: String) {
        viewModelScope.launch {
            preferencesDataStore.setSelectedCity(cityName)
        }
    }

    fun togglePrayerAlarm(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setPrayerAlarmEnabled(enabled)
        }
    }

    fun setAutoScrollSpeed(speed: Float) {
        viewModelScope.launch {
            preferencesDataStore.setAutoScrollSpeed(speed)
        }
    }
}
