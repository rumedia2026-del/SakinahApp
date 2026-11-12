package com.sakinah.app.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sakinah.app.feature.azkar.AzkarListScreen
import com.sakinah.app.feature.azkar.ZenModeScreen
import com.sakinah.app.feature.emotional.EmotionalCompanionScreen
import com.sakinah.app.feature.home.HomeScreen
import com.sakinah.app.feature.quran.QuranScreen
import com.sakinah.app.feature.settings.SettingsScreen
import com.sakinah.app.feature.tasbih.TasbihScreen
import com.sakinah.app.feature.wird.WirdBuilderScreen

private data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

private val navItems = listOf(
    BottomNavItem(Screen.Home, "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Quran, "المصحف", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    BottomNavItem(Screen.Tasbih, "المسبحة", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
    BottomNavItem(Screen.EmotionalCompanion, "مشاعري", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    BottomNavItem(Screen.WirdBuilder, "وردي", Icons.Filled.Book, Icons.Outlined.Book)
)

private const val NAV_ANIM_DURATION = 250

@Composable
fun SakinahNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // إظهار الشريط السفلي للتبويبات الأربعة الرئيسية فقط
    val isTopLevel = navItems.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(tween(NAV_ANIM_DURATION)) +
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION))
            },
            exitTransition = {
                fadeOut(tween(NAV_ANIM_DURATION)) +
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION))
            },
            popEnterTransition = {
                fadeIn(tween(NAV_ANIM_DURATION)) +
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION))
            },
            popExitTransition = {
                fadeOut(tween(NAV_ANIM_DURATION)) +
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION))
            }
        ) {
            // ── التبويبات الأربعة الرئيسية ──
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Quran.route) {
                QuranScreen()
            }
            composable(Screen.Tasbih.route) {
                TasbihScreen()
            }
            composable(Screen.EmotionalCompanion.route) {
                EmotionalCompanionScreen(navController = navController)
            }
            composable(Screen.WirdBuilder.route) {
                WirdBuilderScreen(navController = navController)
            }

            // ── شاشة عرض الأذكار بالتصنيف ──
            composable(
                route = Screen.AzkarList.route,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.IntType }
                )
            ) {
                AzkarListScreen(navController = navController)
            }

            // ── وضع القراءة الغامر (Zen Mode) ──
            composable(
                route = Screen.ZenMode.route,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.IntType }
                )
            ) {
                ZenModeScreen(navController = navController)
            }

            // ── شاشة مواقيت الصلاة والقبلة ──
            composable(Screen.PrayerTimes.route) {
                com.sakinah.app.feature.prayer.PrayerQiblaScreen(onNavigateBack = { navController.popBackStack() })
            }

            // ── شاشة الإعدادات ──
            composable(Screen.Settings.route) {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
