package com.sakinah.app.feature.wird

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakinah.app.core.utils.rememberHapticFeedback
import com.sakinah.app.feature.azkar.components.ZikrCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WirdBuilderScreen(
    viewModel: WirdViewModel = hiltViewModel()
) {
    val favorites by viewModel.favoriteAzkar.collectAsState()
    val haptic = rememberHapticFeedback()

    var showAddDialog by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }
    var customCountString by remember { mutableStateOf("3") }

    val progressMap = remember { mutableStateMapOf<Int, Int>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "وردي اليومي المخصص",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة ذكر خاص")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📖", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لم تُضف أي أذكار لوردك اليومي بعد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يمكنك إضافة أي ذكر بنقر علامة القلب (المفضلة) أو كتابة أدعيتك الخاصة عبر زر الإضافة (+)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "أذكارك وأدعيتك المنتقاة لليوم (${favorites.size}):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(favorites, key = { it.id }) { zikr ->
                    val currentCount = progressMap[zikr.id] ?: 0
                    ZikrCardItem(
                        zikr = zikr,
                        currentCount = currentCount,
                        showTashkeel = true,
                        fontSizeMultiplier = 1.0f,
                        haptic = haptic,
                        onTap = {
                            if (currentCount < zikr.repeatCount) {
                                progressMap[zikr.id] = currentCount + 1
                            }
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(zikr)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }

        // نافذة إضافة دعاء أو ذكر شخصي
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("إضافة دعاء إلى وردي") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            label = { Text("نص الدعاء أو الآية") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customCountString,
                            onValueChange = { customCountString = it.filter { char -> char.isDigit() } },
                            label = { Text("عدد التكرار") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (customText.isNotBlank()) {
                                val count = customCountString.toIntOrNull() ?: 1
                                viewModel.addCustomZikr(customText.trim(), count)
                                customText = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("إضافة")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}
