package com.example.localdictationpro.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localdictationpro.tts.TTSManager
import com.example.localdictationpro.viewmodel.SettingsViewModel
import android.provider.DocumentsContract
import android.content.ContentResolver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("常规") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("听写") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("TTS引擎") }
                )
            }

            when (selectedTabIndex) {
                0 -> GeneralSettingsTab(settings, viewModel)
                1 -> DictationSettingsTab(settings, viewModel)
                2 -> TTSSettingsCard()
            }
        }
    }
}

@Composable
fun GeneralSettingsTab(
    settings: com.example.localdictationpro.data.entities.Settings?,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current

    var displayPath by remember { mutableStateOf(settings?.exportPath ?: "默认 (内部存储/Android/data/.../files/exports)") }

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val path = getPathFromUri(context, it)
            viewModel.updateExportPath(path)
            displayPath = path ?: it.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 语速设置
        Text("语速", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = settings?.speechRate ?: 1.0f,
            onValueChange = { viewModel.updateSpeechRate(it) },
            valueRange = 0.5f..2.0f,
            steps = 5
        )
        Text("当前语速: ${"%.1f".format(settings?.speechRate ?: 1.0f)}")

        Divider()

        // 字体大小
        Text("字体大小", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = (settings?.fontSize ?: 16).toFloat(),
            onValueChange = { viewModel.updateFontSize(it.toInt()) },
            valueRange = 10f..24f,
            steps = 13
        )
        Text("当前字体: ${settings?.fontSize ?: 16} px")

        Divider()

        // 主题选择
        Text("主题", style = MaterialTheme.typography.titleMedium)
        Row {
            listOf("Light", "Dark").forEach { theme ->
                FilterChip(
                    selected = settings?.theme == theme,
                    onClick = { viewModel.updateTheme(theme) },
                    label = { Text(theme) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Divider()

        // 导出路径设置
        Text("导出路径", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayPath,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            IconButton(
                onClick = { folderLauncher.launch(null) }
            ) {
                Icon(Icons.Default.Folder, contentDescription = "选择文件夹")
            }
        }
        Text(
            text = "点击文件夹图标选择导出目录",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DictationSettingsTab(
    settings: com.example.localdictationpro.data.entities.Settings?,
    viewModel: SettingsViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 播放间隔
        Text("播放间隔 (秒)", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = (settings?.intervalSeconds ?: 3).toFloat(),
            onValueChange = { viewModel.updateInterval(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8
        )
        Text("间隔: ${settings?.intervalSeconds ?: 3} 秒")

        Divider()

        // 权重设置
        Text("中英文权重 (当主界面开启「中英随机」时生效)", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("英文权重")
                Slider(
                    value = (settings?.englishWeight ?: 8).toFloat(),
                    onValueChange = {
                        viewModel.updateWeights(it.toInt(), settings?.chineseWeight ?: 2)
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Text("${settings?.englishWeight ?: 8}")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("中文权重")
                Slider(
                    value = (settings?.chineseWeight ?: 2).toFloat(),
                    onValueChange = {
                        viewModel.updateWeights(settings?.englishWeight ?: 8, it.toInt())
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Text("${settings?.chineseWeight ?: 2}")
            }
        }
    }
}

@Composable
fun TTSSettingsCard() {
    val context = LocalContext.current
    val ttsManager = remember { TTSManager(context) }
    val scope = rememberCoroutineScope()

    var isGoogleTtsInstalled by remember { mutableStateOf(false) }
    var isGoogleTtsDefault by remember { mutableStateOf(false) }
    var installedEngines by remember { mutableStateOf<List<String>>(emptyList()) }

    // 刷新状态的方法
    fun refreshTTSStatus() {
        isGoogleTtsInstalled = ttsManager.isGoogleTTsInstalled()
        isGoogleTtsDefault = ttsManager.isGoogleTTsDefault()
        installedEngines = ttsManager.getInstalledEngines()?.map { it.label } ?: emptyList()
    }

    // 进入页面时自动初始化并刷新状态
    LaunchedEffect(Unit) {
        // 先确保 TTS 完成初始化
        ttsManager.initialize { _, _ -> }
        // 稍等一下让引擎信息准备好
        delay(500)
        refreshTTSStatus()
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "文字转语音 (TTS) 引擎配置",
                style = MaterialTheme.typography.titleLarge
            )

            // 当前状态卡片
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 当前状态", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("已安装的引擎: ${if (installedEngines.isEmpty()) "无" else installedEngines.joinToString()}")
                    Text("Google TTS 已安装: ${if (isGoogleTtsInstalled) "✅" else "❌"}")
                    Text("Google TTS 为默认引擎: ${if (isGoogleTtsDefault) "✅" else "❌"}")

                    // 手动刷新按钮（以备用户操作后需要立即更新）
                    Button(
                        onClick = {
                            scope.launch {
                                ttsManager.initialize { _, _ -> }
                                delay(300)
                                refreshTTSStatus()
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("刷新状态")
                    }
                }
            }

            Divider()

            // 引导步骤
            Text(
                text = "🚀 快速配置指南 (推荐使用 Google TTS)",
                style = MaterialTheme.typography.titleMedium
            )

            // 步骤1：安装 Google TTS
            TTSGuideStep(
                number = 1,
                title = "安装 Google 文字转语音",
                description = "前往 Google Play 商店下载并安装 Google TTS 应用。",
                actionLabel = "去商店下载",
                onAction = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=com.google.android.tts")
                        setPackage("com.android.vending")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts"))
                        context.startActivity(webIntent)
                    }
                },
                isCompleted = isGoogleTtsInstalled,
                enabled = true
            )

            // 步骤2：设置为默认引擎
            TTSGuideStep(
                number = 2,
                title = "设置为默认 TTS 引擎",
                description = "打开系统 TTS 设置，将 Google TTS 设置为默认引擎。",
                actionLabel = "打开 TTS 设置",
                onAction = {
                    context.startActivity(ttsManager.openTTSSettings())
                },
                isCompleted = isGoogleTtsDefault,
                enabled = isGoogleTtsInstalled
            )

            // 步骤3：下载英文语音数据
            TTSGuideStep(
                number = 3,
                title = "下载英文语音数据",
                description = "确保已下载英文语音包，否则无法朗读英文单词。",
                actionLabel = "检查语音数据",
                onAction = {
                    ttsManager.installTTSData()?.let { intent ->
                        context.startActivity(intent)
                    } ?: run {
                        context.startActivity(ttsManager.openAppInfo("com.google.android.tts"))
                    }
                },
                isCompleted = false,
                enabled = isGoogleTtsDefault
            )

            Divider()

            // 备用引擎提示
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 提示",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "如果无法安装 Google TTS，您也可以使用手机自带的 TTS 引擎。只需在系统设置中启用任意一个 TTS 引擎，应用会自动检测并使用。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun TTSGuideStep(
    number: Int,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
    isCompleted: Boolean,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
//        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 步骤编号圆圈
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = number.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            // 内容区域
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 操作按钮
            if (!isCompleted && enabled) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(actionLabel)
                }
            } else if (isCompleted) {
                Text(
                    text = "已完成",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else if (!enabled) {
                Text(
                    text = "需先完成上一步",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// 辅助函数：将 Uri 转换为实际路径字符串
fun getPathFromUri(context: android.content.Context, uri: Uri): String? {
    if (DocumentsContract.isDocumentUri(context, uri)) {
        return uri.toString()
    }
    return uri.path
}