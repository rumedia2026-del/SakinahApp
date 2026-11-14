package com.sakinah.app.feature.fiqh

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakinah.app.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class FiqhIssue(
    val id: Int,
    val chapterId: Int,
    val chapterTitle: String,
    val title: String,
    val ruling: String,
    val steps: List<String>,
    val evidence: String,
    val madhabSummary: String,
    val commonMistakes: List<String>,
    val sourceBook: String
)

data class FiqhChapter(
    val id: Int,
    val title: String,
    val iconName: String,
    val description: String,
    val issues: List<FiqhIssue>
)

object FiqhDataLoader {

    fun loadFiqhGuide(context: Context): List<FiqhChapter> {
        val list = mutableListOf<FiqhChapter>()
        try {
            val inputStream = context.resources.openRawResource(R.raw.fiqh_guide)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val jsonString = reader.use { it.readText() }
            val root = JSONObject(jsonString)
            val chaptersArray = root.getJSONArray("chapters")

            for (i in 0 until chaptersArray.length()) {
                val chObj = chaptersArray.getJSONObject(i)
                val chId = chObj.getInt("id")
                val chTitle = chObj.getString("title")
                val icon = chObj.optString("icon", "menu_book")
                val desc = chObj.optString("description", "")

                val issuesArray = chObj.getJSONArray("issues")
                val issuesList = mutableListOf<FiqhIssue>()

                for (j in 0 until issuesArray.length()) {
                    val issObj = issuesArray.getJSONObject(j)
                    val stepsJson = issObj.getJSONArray("steps")
                    val steps = (0 until stepsJson.length()).map { stepsJson.getString(it) }

                    val mistakesJson = issObj.getJSONArray("common_mistakes")
                    val mistakes = (0 until mistakesJson.length()).map { mistakesJson.getString(it) }

                    issuesList.add(
                        FiqhIssue(
                            id = issObj.getInt("id"),
                            chapterId = chId,
                            chapterTitle = chTitle,
                            title = issObj.getString("title"),
                            ruling = issObj.getString("ruling"),
                            steps = steps,
                            evidence = issObj.getString("evidence_quran_or_hadith"),
                            madhabSummary = issObj.getString("madhab_summary"),
                            commonMistakes = mistakes,
                            sourceBook = issObj.getString("source_book")
                        )
                    )
                }

                list.add(
                    FiqhChapter(
                        id = chId,
                        title = chTitle,
                        iconName = icon,
                        description = desc,
                        issues = issuesList
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("FiqhDataLoader", "Failed to parse fiqh_guide.json", e)
        }
        return list
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiqhGuideScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    val chapters = remember { FiqhDataLoader.loadFiqhGuide(context) }
    var selectedChapterId by remember { mutableIntStateOf(0) } // 0 = all
    var searchQuery by remember { mutableStateOf("") }
    val expandedIssueIds = remember { mutableStateListOf<Int>() }

    val allIssues = remember(chapters) { chapters.flatMap { it.issues } }

    val filteredIssues = remember(allIssues, selectedChapterId, searchQuery) {
        allIssues.filter { issue ->
            val matchChapter = (selectedChapterId == 0 || issue.chapterId == selectedChapterId)
            val matchSearch = if (searchQuery.isBlank()) true else {
                issue.title.contains(searchQuery, ignoreCase = true) ||
                        issue.ruling.contains(searchQuery, ignoreCase = true) ||
                        issue.evidence.contains(searchQuery, ignoreCase = true) ||
                        issue.chapterTitle.contains(searchQuery, ignoreCase = true)
            }
            matchChapter && matchSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "دليل الفقه الميسر 📖",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "أحكام الصلاة والطهارة الموثقة أوفلاين",
                            style = MaterialTheme.typography.labelSmall,
                            color = goldAccent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // شريط البحث
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("ابحث في المسائل الفقهية، السهو، السفر...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = goldAccent) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = goldAccent,
                    cursorColor = goldAccent
                )
            )

            // رقاقات التصنيفات الفقهية
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedChapterId == 0,
                        onClick = { selectedChapterId = 0 },
                        label = { Text("الكل (${allIssues.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = emeraldPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = emeraldPrimary
                        )
                    )
                }
                items(chapters) { ch ->
                    FilterChip(
                        selected = selectedChapterId == ch.id,
                        onClick = { selectedChapterId = ch.id },
                        label = { Text(ch.title) },
                        leadingIcon = {
                            Icon(
                                imageVector = getChapterIcon(ch.iconName),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = emeraldPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = emeraldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // قائمة المسائل
            if (filteredIssues.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لم يتم العثور على مسائل مطابقة للبحث",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredIssues, key = { it.id }) { issue ->
                        val isExpanded = expandedIssueIds.contains(issue.id)
                        FiqhIssueCard(
                            issue = issue,
                            isExpanded = isExpanded,
                            onToggle = {
                                if (isExpanded) expandedIssueIds.remove(issue.id)
                                else expandedIssueIds.add(issue.id)
                            },
                            onShare = {
                                shareFiqhIssue(context, issue)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiqhIssueCard(
    issue: FiqhIssue,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onShare: () -> Unit
) {
    val emeraldPrimary = Color(0xFF0F3D2E)
    val goldAccent = Color(0xFFC8A951)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) goldAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(emeraldPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚖️", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = issue.chapterTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = goldAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = goldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ملخص الحكم (ظاهر دائماً)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = issue.ruling,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            // التفاصيل الموسعة
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. الخطوات العملية
                    if (issue.steps.isNotEmpty()) {
                        Text(
                            text = "📋 الخطوات والضوابط العملية:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = emeraldPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        issue.steps.forEachIndexed { idx, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    color = goldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 2. الدليل الشرعي
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(goldAccent.copy(alpha = 0.08f))
                            .border(1.dp, goldAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "📜 الدليل من الكتاب والسنة:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = issue.evidence,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. خلاصة المذاهب
                    Text(
                        text = "🏛 خلاصة المذاهب الأربعة:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = emeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = issue.madhabSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 4. أخطاء شائعة
                    if (issue.commonMistakes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFF3CD))
                                .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFF856404),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "أخطاء شائعة يجب الحذر منها:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                issue.commonMistakes.forEach { mistake ->
                                    Text(
                                        text = "⚠️ $mistake",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF856404)
                                    )
                                }
                            }
                        }
                    }

                    // 5. المصدر المعتمد
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📚 المصدر: ${issue.sourceBook}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun getChapterIcon(iconName: String): ImageVector {
    return when (iconName) {
        "self_improvement" -> Icons.Default.SelfImprovement
        "flight_takeoff" -> Icons.Default.FlightTakeoff
        "water_drop" -> Icons.Default.WaterDrop
        "groups" -> Icons.Default.Groups
        else -> Icons.Default.MenuBook
    }
}

private fun shareFiqhIssue(context: Context, issue: FiqhIssue) {
    val shareText = buildString {
        appendLine("⚖️ من دليل الفقه الميسر - تطبيق سكينة:")
        appendLine("📌 المسألة: ${issue.title} (${issue.chapterTitle})")
        appendLine()
        appendLine("💡 الحكم: ${issue.ruling}")
        if (issue.steps.isNotEmpty()) {
            appendLine()
            appendLine("📋 الخطوات:")
            issue.steps.forEach { appendLine("• $it") }
        }
        appendLine()
        appendLine("📜 الدليل: ${issue.evidence}")
        appendLine()
        appendLine("📚 المصدر: ${issue.sourceBook}")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة الفائدة الفقهية"))
}
