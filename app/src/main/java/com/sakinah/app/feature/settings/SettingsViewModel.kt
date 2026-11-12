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
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: SakinahThemeMode) {
        viewModelScope.launch { preferencesDataStore.setThemeMode(mode) }
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch { preferencesDataStore.setFontSizeMultiplier(multiplier) }
    }

    fun toggleTashkeel(show: Boolean) {
        viewModelScope.launch { preferencesDataStore.setShowTashkeel(show) }
    }

    fun toggleKeepScreenOn(keep: Boolean) {
        viewModelScope.launch { preferencesDataStore.setKeepScreenOn(keep) }
    }

    fun toggleHaptic(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setHapticEnabled(enabled) }
    }

    fun setVibrationIntensity(intensity: String) {
        viewModelScope.launch { preferencesDataStore.setVibrationIntensity(intensity) }
    }

    fun toggleDeafMode(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setDeafHapticVisualMode(enabled) }
    }

    fun toggleAutoAdvance(auto: Boolean) {
        viewModelScope.launch { preferencesDataStore.setAutoAdvanceZikr(auto) }
    }

    fun toggleAutoCollapse(auto: Boolean) {
        viewModelScope.launch { preferencesDataStore.setAutoCollapseCompletedZikr(auto) }
    }

    fun setSelectedCity(cityName: String) {
        viewModelScope.launch { preferencesDataStore.setSelectedCity(cityName) }
    }

    fun setCalculationMethod(method: String) {
        viewModelScope.launch { preferencesDataStore.setCalculationMethod(method) }
    }

    fun togglePrayerAlarm(enabled: Boolean) {
        viewModelScope.launch { preferencesDataStore.setPrayerAlarmEnabled(enabled) }
    }

    fun setMuezzinVoice(voice: String) {
        viewModelScope.launch { preferencesDataStore.setMuezzinVoice(voice) }
    }

    fun setDefaultQuranReciterId(id: String) {
        viewModelScope.launch { preferencesDataStore.setDefaultQuranReciterId(id) }
    }

    fun toggleNightModeMushaf(night: Boolean) {
        viewModelScope.launch { preferencesDataStore.setNightModeMushaf(night) }
    }

    fun setKhatmahDays(days: Int) {
        viewModelScope.launch { preferencesDataStore.setKhatmahDays(days) }
    }

    fun resetToDefaults() {
        viewModelScope.launch { preferencesDataStore.resetToDefaults() }
    }
}
