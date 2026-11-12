package com.sakinah.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sakinah.app.core.ui.theme.SakinahThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sakinah_preferences")

data class UserPreferences(
    val themeMode: SakinahThemeMode = SakinahThemeMode.WARM_PAPER,
    val fontSizeMultiplier: Float = 1.0f,
    val showTashkeel: Boolean = true,
    val keepScreenOn: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val vibrationIntensity: String = "MEDIUM", // OFF, LIGHT, MEDIUM, STRONG
    val deafHapticVisualMode: Boolean = false,
    val autoAdvanceZikr: Boolean = true,
    val autoCollapseCompletedZikr: Boolean = false,
    val autoScrollSpeed: Float = 1.0f,
    val selectedCityName: String = "رشيد - البحيرة",
    val calculationMethod: String = "EGYPTIAN",
    val prayerAlarmEnabled: Boolean = true,
    val muezzinVoice: String = "مكة المكرمة",
    val defaultQuranReciterId: String = "afs",
    val nightModeMushaf: Boolean = false,
    val khatmahDays: Int = 30,
    val morningNotificationTime: String = "05:30",
    val eveningNotificationTime: String = "17:00"
)

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE_MULTIPLIER = floatPreferencesKey("font_size_multiplier")
        val SHOW_TASHKEEL = booleanPreferencesKey("show_tashkeel")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val VIBRATION_INTENSITY = stringPreferencesKey("vibration_intensity")
        val DEAF_MODE = booleanPreferencesKey("deaf_haptic_visual_mode")
        val AUTO_ADVANCE = booleanPreferencesKey("auto_advance_zikr")
        val AUTO_COLLAPSE = booleanPreferencesKey("auto_collapse_completed_zikr")
        val AUTO_SCROLL_SPEED = floatPreferencesKey("auto_scroll_speed")
        val SELECTED_CITY = stringPreferencesKey("selected_city_name")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val PRAYER_ALARM = booleanPreferencesKey("prayer_alarm_enabled")
        val MUEZZIN_VOICE = stringPreferencesKey("muezzin_voice")
        val DEFAULT_RECITER_ID = stringPreferencesKey("default_reciter_id")
        val NIGHT_MODE_MUSHAF = booleanPreferencesKey("night_mode_mushaf")
        val KHATMAH_DAYS = intPreferencesKey("khatmah_days")
        val MORNING_TIME = stringPreferencesKey("morning_notification_time")
        val EVENING_TIME = stringPreferencesKey("evening_notification_time")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeString = preferences[PreferencesKeys.THEME_MODE] ?: SakinahThemeMode.WARM_PAPER.name
            val themeMode = try {
                SakinahThemeMode.valueOf(themeString)
            } catch (e: Exception) {
                SakinahThemeMode.WARM_PAPER
            }

            UserPreferences(
                themeMode = themeMode,
                fontSizeMultiplier = preferences[PreferencesKeys.FONT_SIZE_MULTIPLIER] ?: 1.0f,
                showTashkeel = preferences[PreferencesKeys.SHOW_TASHKEEL] ?: true,
                keepScreenOn = preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: false,
                hapticFeedbackEnabled = preferences[PreferencesKeys.HAPTIC_ENABLED] ?: true,
                vibrationIntensity = preferences[PreferencesKeys.VIBRATION_INTENSITY] ?: "MEDIUM",
                deafHapticVisualMode = preferences[PreferencesKeys.DEAF_MODE] ?: false,
                autoAdvanceZikr = preferences[PreferencesKeys.AUTO_ADVANCE] ?: true,
                autoCollapseCompletedZikr = preferences[PreferencesKeys.AUTO_COLLAPSE] ?: false,
                autoScrollSpeed = preferences[PreferencesKeys.AUTO_SCROLL_SPEED] ?: 1.0f,
                selectedCityName = preferences[PreferencesKeys.SELECTED_CITY] ?: "رشيد - البحيرة",
                calculationMethod = preferences[PreferencesKeys.CALC_METHOD] ?: "EGYPTIAN",
                prayerAlarmEnabled = preferences[PreferencesKeys.PRAYER_ALARM] ?: true,
                muezzinVoice = preferences[PreferencesKeys.MUEZZIN_VOICE] ?: "مكة المكرمة",
                defaultQuranReciterId = preferences[PreferencesKeys.DEFAULT_RECITER_ID] ?: "afs",
                nightModeMushaf = preferences[PreferencesKeys.NIGHT_MODE_MUSHAF] ?: false,
                khatmahDays = preferences[PreferencesKeys.KHATMAH_DAYS] ?: 30,
                morningNotificationTime = preferences[PreferencesKeys.MORNING_TIME] ?: "05:30",
                eveningNotificationTime = preferences[PreferencesKeys.EVENING_TIME] ?: "17:00"
            )
        }

    suspend fun setThemeMode(mode: SakinahThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setFontSizeMultiplier(multiplier: Float) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SIZE_MULTIPLIER] = multiplier.coerceIn(0.8f, 1.6f) }
    }

    suspend fun setShowTashkeel(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_TASHKEEL] = show }
    }

    suspend fun setKeepScreenOn(keep: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.KEEP_SCREEN_ON] = keep }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setVibrationIntensity(intensity: String) {
        context.dataStore.edit { it[PreferencesKeys.VIBRATION_INTENSITY] = intensity }
    }

    suspend fun setDeafHapticVisualMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DEAF_MODE] = enabled }
    }

    suspend fun setAutoAdvanceZikr(auto: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_ADVANCE] = auto }
    }

    suspend fun setAutoCollapseCompletedZikr(auto: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_COLLAPSE] = auto }
    }

    suspend fun setAutoScrollSpeed(speed: Float) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_SCROLL_SPEED] = speed.coerceIn(0.5f, 3.0f) }
    }

    suspend fun setSelectedCity(cityName: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_CITY] = cityName }
    }

    suspend fun setCalculationMethod(method: String) {
        context.dataStore.edit { it[PreferencesKeys.CALC_METHOD] = method }
    }

    suspend fun setPrayerAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.PRAYER_ALARM] = enabled }
    }

    suspend fun setMuezzinVoice(voice: String) {
        context.dataStore.edit { it[PreferencesKeys.MUEZZIN_VOICE] = voice }
    }

    suspend fun setDefaultQuranReciterId(id: String) {
        context.dataStore.edit { it[PreferencesKeys.DEFAULT_RECITER_ID] = id }
    }

    suspend fun setNightModeMushaf(night: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NIGHT_MODE_MUSHAF] = night }
    }

    suspend fun setKhatmahDays(days: Int) {
        context.dataStore.edit { it[PreferencesKeys.KHATMAH_DAYS] = days }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { it.clear() }
    }
}
