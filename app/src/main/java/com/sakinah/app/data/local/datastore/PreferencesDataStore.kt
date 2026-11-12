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

/**
 * فئة نموذجية لحمل تفضيلات المستخدم الخاصة بالواجهة والأذكار
 */
data class UserPreferences(
    val themeMode: SakinahThemeMode,
    val fontSizeMultiplier: Float,
    val showTashkeel: Boolean,
    val hapticFeedbackEnabled: Boolean,
    val autoScrollSpeed: Float,
    val morningNotificationTime: String,
    val eveningNotificationTime: String
)

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FONT_SIZE_MULTIPLIER = floatPreferencesKey("font_size_multiplier")
        val SHOW_TASHKEEL = booleanPreferencesKey("show_tashkeel")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val AUTO_SCROLL_SPEED = floatPreferencesKey("auto_scroll_speed")
        val MORNING_TIME = stringPreferencesKey("morning_notification_time")
        val EVENING_TIME = stringPreferencesKey("evening_notification_time")
    }

    /**
     * تدفق يبعث تفضيلات المستخدم بشكل فوري ومستمر
     */
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

            val fontSize = preferences[PreferencesKeys.FONT_SIZE_MULTIPLIER] ?: 1.0f
            val showTashkeel = preferences[PreferencesKeys.SHOW_TASHKEEL] ?: true
            val hapticEnabled = preferences[PreferencesKeys.HAPTIC_ENABLED] ?: true
            val autoScrollSpeed = preferences[PreferencesKeys.AUTO_SCROLL_SPEED] ?: 1.0f
            val morningTime = preferences[PreferencesKeys.MORNING_TIME] ?: "05:30"
            val eveningTime = preferences[PreferencesKeys.EVENING_TIME] ?: "17:00"

            UserPreferences(
                themeMode = themeMode,
                fontSizeMultiplier = fontSize,
                showTashkeel = showTashkeel,
                hapticFeedbackEnabled = hapticEnabled,
                autoScrollSpeed = autoScrollSpeed,
                morningNotificationTime = morningTime,
                eveningNotificationTime = eveningTime
            )
        }

    suspend fun setThemeMode(mode: SakinahThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setFontSizeMultiplier(multiplier: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE_MULTIPLIER] = multiplier.coerceIn(0.8f, 1.4f)
        }
    }

    suspend fun setShowTashkeel(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TASHKEEL] = show
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setAutoScrollSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCROLL_SPEED] = speed.coerceIn(0.5f, 3.0f)
        }
    }

    suspend fun setMorningNotificationTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MORNING_TIME] = time
        }
    }

    suspend fun setEveningNotificationTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EVENING_TIME] = time
        }
    }
}
