package com.sakinah.app.feature.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sakinah.app.R
import com.sakinah.app.core.navigation.Screen
import com.sakinah.app.core.ui.theme.SakinahTheme
import com.sakinah.app.core.ui.theme.SakinahThemeMode
import com.sakinah.app.data.local.db.entities.CategoryEntity
import com.sakinah.app.feature.home.components.CategoryCard
import com.sakinah.app.feature.home.components.PrayerTimesCard
import com.sakinah.app.feature.home.components.SmartHeroCard

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onHeroCardClick = {
            navController.navigate(
                Screen.AzkarList.createRoute(uiState.smartContext.targetCategoryId)
            )
        },
        onCategoryClick = { categoryId ->
            navController.navigate(Screen.AzkarList.createRoute(categoryId))
        },
        onSettingsClick = {
            navController.navigate(Screen.Settings.route)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onSearchQueryChanged: (String) -> Unit,
    onHeroCardClick: () -> Unit,
    onCategoryClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goldAccent = Color(0xFFC8A951)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "سـَـكـِـيـنـَـة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = goldAccent
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. بطاقة مواقيت الصلاة والعد التنازلي التفاعلي
                item(key = "prayer_times_card") {
                    PrayerTimesCard()
                }

                // 2. البطاقة الذكية المعتمدة على السياق والوقت
                item(key = "smart_hero_card") {
                    SmartHeroCard(
                        contextType = uiState.smartContext,
                        onClick = onHeroCardClick
                    )
                }

                // 3. شريط البحث السريع عن الأذكار والأدعية
                item(key = "search_bar") {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "ابحث عن ذكر أو فضل أو دعاء...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "مسح",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = goldAccent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // 4. عنوان تصنيفات الأذكار
                item(key = "categories_header") {
                    Text(
                        text = "مكتبة حصن المسلم والأدعية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // 5. قائمة الأقسام
                items(
                    items = uiState.filteredCategories,
                    key = { it.id }
                ) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category.id) }
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private val sampleCategories = listOf(
    CategoryEntity(1, "أذكار الصباح", "wb_sunny", "04:30", "11:00", 1),
    CategoryEntity(2, "أذكار المساء", "nights_stay", "15:30", "20:00", 2),
    CategoryEntity(3, "أذكار بعد الصلاة", "mosque", "00:00", "23:59", 3),
    CategoryEntity(4, "أذكار النوم", "bedtime", "21:00", "04:00", 4)
)

@Preview(name = "Home Screen - Light Warm Paper", showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    SakinahTheme(themeMode = SakinahThemeMode.WARM_PAPER) {
        HomeContent(
            uiState = HomeUiState(
                smartContext = SmartContextType.MORNING,
                categories = sampleCategories,
                filteredCategories = sampleCategories,
                searchQuery = "",
                isLoading = false
            ),
            onSearchQueryChanged = {},
            onHeroCardClick = {},
            onCategoryClick = {},
            onSettingsClick = {}
        )
    }
}
