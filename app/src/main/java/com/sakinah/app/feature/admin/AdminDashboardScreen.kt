package com.sakinah.app.feature.admin

import android.content.Context
import android.content.Intent
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.app.core.notifications.workers.RemoteContentSyncWorker
import com.sakinah.app.core.utils.normalizeArabicForSearch
import com.sakinah.app.data.local.db.entities.ZikrEntity
import com.sakinah.app.data.repository.AzkarRepository
import com.sakinah.app.feature.fiqh.FiqhChapter
import com.sakinah.app.feature.fiqh.FiqhIssue
import com.sakinah.app.feature.social.SocialMediaReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository
) : ViewModel() {

    val allAzkar: StateFlow<List<ZikrEntity>> = azkarRepository.getAllAzkarFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    fun saveZikr(zikr: ZikrEntity) {
        viewModelScope.launch {
            azkarRepository.saveZikr(zikr)
        }
    }

    fun deleteZikr(id: Int) {
        viewModelScope.launch {
            azkarRepository.deleteZikr(id)
        }
    }

    suspend fun getAllAzkarSnapshot(): List<ZikrEntity> = azkarRepository.getAllAzkar()

    fun publishToGitHub(
        token: String,
        azkarList: List<ZikrEntity>,
        onSuccess: (newVersion: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isPublishing.value = true
            try {
                val newVersion = withContext(Dispatchers.IO) {
                    executeGitHubPublish(token, azkarList)
                }
                onSuccess(newVersion)
            } catch (e: Exception) {
                onError(e.message ?: "خطأ غير معروف في الاتصال بـ GitHub")
            } finally {
                _isPublishing.value = false
            }
        }
    }

    private fun executeGitHubPublish(token: String, azkarList: List<ZikrEntity>): Int {
        val repo = "rumedia2026-del/SakinahApp"
        val branch = "main"

        // 1. Fetch current version.json
        val versionUrl = "https://api.github.com/repos/$repo/contents/remote_content/version.json"
        var currentVersion = 1
        var versionSha: String? = null

        val connVer = (URL(versionUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/vnd.github.v3+json")
        }

        if (connVer.responseCode in 200..299) {
            val resText = connVer.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(resText)
            versionSha = json.optString("sha")
            val contentBase64 = json.optString("content").replace("\n", "").trim()
            if (contentBase64.isNotEmpty()) {
                val decoded = String(Base64.decode(contentBase64, Base64.DEFAULT), Charsets.UTF_8)
                val verObj = JSONObject(decoded)
                currentVersion = verObj.optInt("contentVersion", 1)
            }
        }

        val bumpedVersion = currentVersion + 1

        // 2. Commit azkar_seed.json
        val azkarArray = JSONArray()
        for (z in azkarList) {
            val obj = JSONObject().apply {
                put("id", z.id)
                put("categoryId", z.categoryId)
                put("textWithTashkeel", z.textWithTashkeel)
                put("textPlain", z.textPlain)
                put("repeatCount", z.repeatCount)
                put("virtueText", z.virtueText)
                put("sourceReference", z.sourceReference)
                put("authenticityGrade", z.authenticityGrade)
                put("audioUrl", z.audioUrl)
                put("emotionalTags", z.emotionalTags)
                put("isUserCustom", false)
                put("isFavorite", false)
            }
            azkarArray.put(obj)
        }
        val fullAzkarRoot = JSONObject().apply {
            put("azkar", azkarArray)
        }

        commitGitHubFile(
            repo = repo,
            filePath = "remote_content/azkar_seed.json",
            content = fullAzkarRoot.toString(2),
            commitMessage = "OTA Update: Azkar list updated [v$bumpedVersion]",
            token = token,
            branch = branch
        )

        // 3. Commit version.json
        val newVerObj = JSONObject().apply {
            put("contentVersion", bumpedVersion)
            put("lastUpdated", java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()))
            put("description", "OTA Push from Sakinah Admin Dashboard")
        }

        commitGitHubFile(
            repo = repo,
            filePath = "remote_content/version.json",
            content = newVerObj.toString(2),
            commitMessage = "OTA Bump contentVersion to $bumpedVersion",
            token = token,
            branch = branch,
            existingSha = versionSha
        )

        return bumpedVersion
    }

    private fun commitGitHubFile(
        repo: String,
        filePath: String,
        content: String,
        commitMessage: String,
        token: String,
        branch: String,
        existingSha: String? = null
    ) {
        val url = "https://api.github.com/repos/$repo/contents/$filePath"

        var sha = existingSha
        if (sha == null) {
            try {
                val checkConn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 12000
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                }
                if (checkConn.responseCode in 200..299) {
                    val resp = checkConn.inputStream.bufferedReader().use { it.readText() }
                    sha = JSONObject(resp).optString("sha")
                }
            } catch (_: Exception) {}
        }

        val base64Content = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val body = JSONObject().apply {
            put("message", commitMessage)
            put("content", base64Content)
            put("branch", branch)
            if (!sha.isNullOrBlank()) {
                put("sha", sha)
            }
        }

        val putConn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "PUT"
            doOutput = true
            connectTimeout = 12000
            readTimeout = 12000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/vnd.github.v3+json")
        }

        OutputStreamWriter(putConn.outputStream, "UTF-8").use {
            it.write(body.toString())
            it.flush()
        }

        if (putConn.responseCode !in 200..299) {
            val err = putConn.errorStream?.bufferedReader()?.use { it.readText() } ?: putConn.responseMessage
            throw RuntimeException("فشل نشر $filePath ($putConn.responseCode): $err")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allAzkar by viewModel.allAzkar.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("الأذكار والأدعية", "منبّه السوشيال", "مسائل الفقه")

    val goldAccent = Color(0xFFC8A951)
    val emeraldPrimary = Color(0xFF0F3D2E)

    // Editing Zikr Dialog State
    var editingZikr by remember { mutableStateOf<ZikrEntity?>(null) }
    var isNewZikrDialog by remember { mutableStateOf(false) }

    // GitHub PAT Dialog State
    var showGhTokenDialog by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("sakinah_admin_prefs", Context.MODE_PRIVATE) }
    var savedGhToken by remember { mutableStateOf(prefs.getString("github_pat", "") ?: "") }

    // Social Media Phrases State
    val phrases = remember { mutableStateListOf<String>().apply { addAll(SocialMediaReminderManager.getAllPhrases(context)) } }
    var editingPhraseIndex by remember { mutableStateOf(-1) }
    var phraseEditText by remember { mutableStateOf("") }
    var showPhraseDialog by remember { mutableStateOf(false) }

    // Search Query
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "لوحة تحكم المشرف (Admin)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = goldAccent
                        )
                        Text(
                            text = "إدارة المحتوى المباشرة وتحديث السحابة OTA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showGhTokenDialog = true }) {
                        Icon(Icons.Default.Key, contentDescription = "GitHub Token", tint = if (savedGhToken.isNotBlank()) emeraldPrimary else goldAccent)
                    }
                    IconButton(onClick = {
                        RemoteContentSyncWorker.syncNow(context)
                        Toast.makeText(context, "بدأت المزامنة الصامتة في الخلفية...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Sync, contentDescription = "مزامنة الآن")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        editingZikr = ZikrEntity(
                            id = (allAzkar.maxOfOrNull { it.id } ?: 1000) + 1,
                            categoryId = 1,
                            textWithTashkeel = "",
                            textPlain = "",
                            repeatCount = 1,
                            virtueText = null,
                            sourceReference = "حصن المسلم",
                            authenticityGrade = "صحيح",
                            audioUrl = null,
                            emotionalTags = "طمأنينة, بركة",
                            isUserCustom = false,
                            isFavorite = false
                        )
                        isNewZikrDialog = true
                    },
                    containerColor = emeraldPrimary,
                    contentColor = goldAccent
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة ذكر")
                }
            } else if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        editingPhraseIndex = -1
                        phraseEditText = ""
                        showPhraseDialog = true
                    },
                    containerColor = emeraldPrimary,
                    contentColor = goldAccent
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة عبارة")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Action Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(emeraldPrimary.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val azkar = viewModel.getAllAzkarSnapshot()
                            val root = JSONObject()
                            val arr = JSONArray()
                            for (z in azkar) {
                                val obj = JSONObject().apply {
                                    put("id", z.id)
                                    put("categoryId", z.categoryId)
                                    put("textPlain", z.textPlain)
                                    put("textWithTashkeel", z.textWithTashkeel)
                                    put("repeatCount", z.repeatCount)
                                    put("audioUrl", z.audioUrl)
                                }
                                arr.put(obj)
                            }
                            root.put("azkar", arr)

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, root.toString(2))
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "تصدير بيانات الأذكار JSON"))
                        }
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تصدير JSON", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (savedGhToken.isBlank()) {
                            showGhTokenDialog = true
                        } else {
                            viewModel.publishToGitHub(
                                token = savedGhToken,
                                azkarList = allAzkar,
                                onSuccess = { newVer ->
                                    Toast.makeText(context, "تم نشر التحديث على GitHub بنجاح! الإصدار الجديد: v$newVer", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, "فشل النشر: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary),
                    enabled = !isPublishing
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = goldAccent, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("جاري النشر...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = goldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نشر على GitHub", fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = emeraldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = goldAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) emeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> AzkarAdminTab(
                    azkar = allAzkar,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onEdit = { zikr ->
                        editingZikr = zikr
                        isNewZikrDialog = false
                    },
                    onDelete = { id -> viewModel.deleteZikr(id) }
                )
                1 -> SocialPhrasesAdminTab(
                    phrases = phrases,
                    onAddOrUpdate = { idx, text ->
                        if (idx >= 0 && idx < phrases.size) {
                            phrases[idx] = text
                            SocialMediaReminderManager.updatePhrase(context, idx, text)
                        } else {
                            phrases.add(0, text)
                            SocialMediaReminderManager.addPhrase(context, text)
                        }
                    },
                    onDelete = { idx ->
                        if (idx in phrases.indices) {
                            phrases.removeAt(idx)
                            SocialMediaReminderManager.deletePhrase(context, idx)
                        }
                    },
                    onOpenEditor = { idx, text ->
                        editingPhraseIndex = idx
                        phraseEditText = text
                        showPhraseDialog = true
                    }
                )
                2 -> FiqhAdminTab()
            }
        }
    }

    // Edit/Add Zikr Dialog
    if (editingZikr != null) {
        var textTashkeel by remember { mutableStateOf(editingZikr?.textWithTashkeel ?: "") }
        var textPlain by remember { mutableStateOf(editingZikr?.textPlain ?: "") }
        var repeat by remember { mutableStateOf((editingZikr?.repeatCount ?: 1).toString()) }
        var audioUrl by remember { mutableStateOf(editingZikr?.audioUrl ?: "") }
        var categoryId by remember { mutableStateOf((editingZikr?.categoryId ?: 1).toString()) }

        AlertDialog(
            onDismissRequest = { editingZikr = null },
            title = {
                Text(if (isNewZikrDialog) "إضافة ذكر جديد" else "تعديل الذكر (#${editingZikr?.id})", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = textTashkeel,
                        onValueChange = { textTashkeel = it },
                        label = { Text("النص بالتشكيل التام") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = textPlain,
                        onValueChange = { textPlain = it },
                        label = { Text("النص المبسط (بدون تشكيل)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = repeat,
                            onValueChange = { repeat = it.filter { c -> c.isDigit() } },
                            label = { Text("التكرار") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = categoryId,
                            onValueChange = { categoryId = it.filter { c -> c.isDigit() } },
                            label = { Text("رقم القسم (1..12)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = audioUrl,
                        onValueChange = { audioUrl = it },
                        label = { Text("رابط الصوت (Audio URL)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = editingZikr ?: return@Button
                        val updated = current.copy(
                            textWithTashkeel = textTashkeel.ifBlank { textPlain },
                            textPlain = textPlain.ifBlank { textTashkeel },
                            repeatCount = repeat.toIntOrNull() ?: 1,
                            categoryId = categoryId.toIntOrNull() ?: 1,
                            audioUrl = audioUrl.ifBlank { null }
                        )
                        viewModel.saveZikr(updated)
                        editingZikr = null
                        Toast.makeText(context, "تم حفظ التعديل بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary)
                ) {
                    Text("حفظ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingZikr = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // GitHub Token Dialog
    if (showGhTokenDialog) {
        var tokenInput by remember { mutableStateOf(savedGhToken) }
        AlertDialog(
            onDismissRequest = { showGhTokenDialog = false },
            title = { Text("إعداد GitHub Personal Access Token", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "أدخل التوكن الخاص بحسابك على GitHub مع صلاحية (repo) لتمكين لوحة التحكم من النشر المباشر على المستودع.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("GitHub PAT (ghp_...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        savedGhToken = tokenInput.trim()
                        prefs.edit().putString("github_pat", savedGhToken).apply()
                        showGhTokenDialog = false
                        Toast.makeText(context, "تم حفظ التوكن", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGhTokenDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Social Phrase Edit Dialog
    if (showPhraseDialog) {
        AlertDialog(
            onDismissRequest = { showPhraseDialog = false },
            title = { Text(if (editingPhraseIndex >= 0) "تعديل عبارة السوشيال ميديا" else "إضافة عبارة جديدة", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = phraseEditText,
                    onValueChange = { phraseEditText = it },
                    label = { Text("نص العبارة التذكيرية") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val text = phraseEditText.trim()
                        if (text.isNotBlank()) {
                            if (editingPhraseIndex >= 0 && editingPhraseIndex < phrases.size) {
                                phrases[editingPhraseIndex] = text
                                SocialMediaReminderManager.updatePhrase(context, editingPhraseIndex, text)
                            } else {
                                phrases.add(0, text)
                                SocialMediaReminderManager.addPhrase(context, text)
                            }
                        }
                        showPhraseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhraseDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun AzkarAdminTab(
    azkar: List<ZikrEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEdit: (ZikrEntity) -> Unit,
    onDelete: (Int) -> Unit
) {
    val filteredAzkar = remember(azkar, searchQuery) {
        if (searchQuery.isBlank()) azkar
        else {
            val q = searchQuery.normalizeArabicForSearch()
            azkar.filter {
                it.textPlain.normalizeArabicForSearch().contains(q) ||
                it.textWithTashkeel.normalizeArabicForSearch().contains(q) ||
                it.id.toString() == searchQuery.trim()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("ابحث برقم الذكر أو كلماته...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFC8A951),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredAzkar, key = { it.id }) { zikr ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ذكر #${zikr.id} • قسم ${zikr.categoryId}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC8A951)
                            )
                            Text(
                                text = "تكرار: ${zikr.repeatCount}×",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = zikr.textWithTashkeel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (!zikr.audioUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "صوت: ${zikr.audioUrl}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF3B82F6),
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onEdit(zikr) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF0F3D2E), modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDelete(zikr.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialPhrasesAdminTab(
    phrases: List<String>,
    onAddOrUpdate: (Int, String) -> Unit,
    onDelete: (Int) -> Unit,
    onOpenEditor: (Int, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(phrases.indices.toList(), key = { it }) { idx ->
            val phrase = phrases[idx]
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${idx + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC8A951),
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = phrase,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onOpenEditor(idx, phrase) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF0F3D2E), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onDelete(idx) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FiqhAdminTab() {
    val context = LocalContext.current
    var chapters by remember { mutableStateOf<List<FiqhChapter>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val input = context.resources.openRawResource(com.sakinah.app.R.raw.fiqh_guide)
                val jsonStr = input.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val chaptersArray = root.getJSONArray("chapters")
                val list = mutableListOf<FiqhChapter>()
                for (i in 0 until chaptersArray.length()) {
                    val cObj = chaptersArray.getJSONObject(i)
                    val issuesArray = cObj.getJSONArray("issues")
                    val issues = mutableListOf<FiqhIssue>()
                    for (j in 0 until issuesArray.length()) {
                        val iObj = issuesArray.getJSONObject(j)
                        val stepsArr = iObj.optJSONArray("steps")
                        val stepsList = mutableListOf<String>()
                        if (stepsArr != null) {
                            for (s in 0 until stepsArr.length()) stepsList.add(stepsArr.getString(s))
                        }
                        val mistakesArr = iObj.optJSONArray("commonMistakes")
                        val mistakesList = mutableListOf<String>()
                        if (mistakesArr != null) {
                            for (m in 0 until mistakesArr.length()) mistakesList.add(mistakesArr.getString(m))
                        }
                        issues.add(
                            FiqhIssue(
                                id = iObj.getInt("id"),
                                chapterId = cObj.getInt("id"),
                                chapterTitle = cObj.getString("title"),
                                title = iObj.getString("title"),
                                ruling = iObj.getString("ruling"),
                                steps = stepsList,
                                evidence = iObj.getString("evidence"),
                                madhabSummary = iObj.optString("madhabSummary", ""),
                                commonMistakes = mistakesList,
                                sourceBook = iObj.optString("sourceBook", "")
                            )
                        )
                    }
                    list.add(
                        FiqhChapter(
                            id = cObj.getInt("id"),
                            title = cObj.getString("title"),
                            iconName = cObj.optString("iconName", ""),
                            description = cObj.optString("description", ""),
                            issues = issues
                        )
                    )
                }
                chapters = list
            } catch (_: Exception) {}
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "دليل الفقه اليومي الشامل (${chapters.sumOf { it.issues.size }} مسألة فقهية في 8 أبواب)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC8A951)
            )
        }
        chapters.forEach { chapter ->
            item {
                Text(
                    text = "• ${chapter.title} (${chapter.issues.size} مسألة)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F3D2E),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(chapter.issues, key = { "fiqh_${it.id}" }) { issue ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = issue.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الحكم: ${issue.ruling}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0F3D2E)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "الدليل: ${issue.evidence}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
