package com.sakinah.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sakinah.app.core.navigation.SakinahNavGraph
import com.sakinah.app.core.ui.theme.SakinahTheme
import com.sakinah.app.core.ui.theme.SakinahThemeMode
import com.sakinah.app.feature.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

// ═══════════════════════════════════════════════════════════════════
//  MAIN ACTIVITY — Single Activity Architecture
//  ربط الثيم المتغير لحظياً مع تفضيلات المستخدم المحفوظة في DataStore
// ═══════════════════════════════════════════════════════════════════

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val userPrefs by settingsViewModel.userPreferences.collectAsState()

            SakinahTheme(
                themeMode = userPrefs.themeMode,
                fontSizeMultiplier = userPrefs.fontSizeMultiplier,
            ) {
                SakinahNavGraph()
            }
        }
    }
}
