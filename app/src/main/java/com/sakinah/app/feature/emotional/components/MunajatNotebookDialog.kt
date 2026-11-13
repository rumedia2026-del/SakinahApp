package com.sakinah.app.feature.emotional.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.data.local.db.entities.MunajatEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MunajatNotebookDialog(
    activeList: List<MunajatEntity>,
    answeredList: List<MunajatEntity>,
    onAddMunajat: (title: String, content: String, emotionTag: String?) -> Unit,
    onMarkAnswered: (Long) -> Unit,
    onDelete: (MunajatEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: مناجاة جارية, 1: دعوات استجيبت ✨
    var showAddDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "دفتر خاص",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دفتر المناجاة الخاص",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة دعاء",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("مناجاة قائمة (${activeList.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("دعوات استُجيبت ✨ (${answeredList.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val currentList = if (selectedTab == 0) activeList else answeredList

                if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == 0)
                                "لا توجد مناجاة حالياً.\nاضغط (+) لكتابة ما يفيض به قلبك لربك بحرية وسرية تامة."
                            else
                                "هنا تُحفظ دعواتك التي استجابها الله لك، لتكون شاهداً على كرمه ورحمته ✨",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(currentList, key = { it.id }) { item ->
                            MunajatCardItem(
                                item = item,
                                isAnsweredTab = selectedTab == 1,
                                onMarkAnswered = { onMarkAnswered(item.id) },
                                onDelete = { onDelete(item) }
                            )
                        }
                    }
                }
            }
        }
    )

    if (showAddDialog) {
        AddMunajatDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content, tag ->
                onAddMunajat(title, content, tag)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MunajatCardItem(
    item: MunajatEntity,
    isAnsweredTab: Boolean,
    onMarkAnswered: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val formattedDate = remember(item.createdAt) { dateFormat.format(Date(item.createdAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAnsweredTab) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAnsweredTab) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isAnsweredTab) {
                    TextButton(onClick = onMarkAnswered) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "استُجيبت",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استُجيبت بفضل الله ✨", color = Color(0xFF2E7D32), fontSize = 12.sp)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMunajatDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, emotionTag: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مناجاة جديدة بينك وبين الله", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المناجاة (مثلاً: تفريج كرب، توفيق لوظيفة)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("ماذا تريد أن تبث لخالقك؟ (سر لا يطّلع عليه إلا هو)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title, content, null)
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("حفظ في دفتري")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
