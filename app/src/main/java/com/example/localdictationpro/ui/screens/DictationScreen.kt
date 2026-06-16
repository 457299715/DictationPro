package com.example.localdictationpro.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localdictationpro.ui.components.WordDisplayCard
import com.example.localdictationpro.viewmodel.DictationViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictationScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBookSelection: () -> Unit,
    onNavigateToWordSelection: () -> Unit,
    onNavigateToResourceManager: () -> Unit,
    viewModel: DictationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val ttsError by viewModel.ttsError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 普通错误自动消失
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            var userDismissed = false

            val timeoutJob = launch {
                delay(1500)
                if (!userDismissed) {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }

            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "关闭",
                duration = SnackbarDuration.Indefinite,
                withDismissAction = true
            )

            userDismissed = true
            timeoutJob.cancel()

            when (result) {
                SnackbarResult.ActionPerformed -> { /* 用户点击关闭 */ }
                SnackbarResult.Dismissed -> { /* 超时或滑动关闭 */ }
            }

            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📚 单词听写") },
                actions = {
                    IconButton(onClick = onNavigateToBookSelection) {
                        Text("📖")
                    }
                    IconButton(onClick = onNavigateToWordSelection) {
                        Text("🔍")
                    }
                    IconButton(onClick = onNavigateToResourceManager) {
                        Text("📁")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Text("⚙️")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 书籍信息卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.bookName ?: "未选择书籍",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = uiState.rangeInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 进度条
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Text(
                text = "${uiState.currentIndex + 1} / ${uiState.totalWords}",
                style = MaterialTheme.typography.bodyMedium
            )

            // 单词显示区域
            WordDisplayCard(
                word = uiState.currentWord,
                meaning = uiState.currentMeaning,
                displayMeaning = settings?.displayMeaning ?: false,
                showWordNumber = !(settings?.displayMeaning ?: false),
                wordNumber = uiState.currentIndex + 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 控制面板
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 模式选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.mode == "continuous",
                            onClick = { viewModel.setMode("continuous") },
                            label = { Text("连续模式") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.mode == "single",
                            onClick = { viewModel.setMode("single") },
                            label = { Text("单个模式") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 选项行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = settings?.randomOrder ?: false,
                                onCheckedChange = { viewModel.setRandomOrder(it) }
                            )
                            Text("乱序", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = settings?.randomLanguage ?: false,
                                onCheckedChange = { viewModel.setRandomLanguage(it) }
                            )
                            Text("中英随机", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = settings?.displayMeaning ?: false,
                                onCheckedChange = { viewModel.setDisplayMeaning(it) }
                            )
                            Text("显示释义", fontSize = 12.sp)
                        }
                    }

                    // 间隔设置
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("播放间隔: ", fontSize = 14.sp)
                        Slider(
                            value = (settings?.intervalSeconds ?: 3).toFloat(),
                            onValueChange = { viewModel.setInterval(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${settings?.intervalSeconds ?: 3}秒", fontSize = 14.sp)
                    }

                    // 控制按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (uiState.isPlaying) {
                                    viewModel.pause()
                                } else {
                                    viewModel.start()
                                }
                            },
                            modifier = Modifier.weight(2f)
                        ) {
                            Text(if (uiState.isPlaying) "⏸ 暂停" else "▶ 开始")
                        }
                    }

                    // 停止、上一页、下一页、导出按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.stop() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⏹ 停止")
                        }
                        if (uiState.mode == "single") {
                            OutlinedButton(
                                onClick = { viewModel.previousWord() },
                                modifier = Modifier.weight(1f),
                                enabled = uiState.currentIndex > 0 && !uiState.isPlaying
                            ) {
                                Text("◀ 上一页")
                            }
                            OutlinedButton(
                                onClick = { viewModel.nextWord() },
                                modifier = Modifier.weight(1f),
                                enabled = uiState.currentIndex < uiState.totalWords - 1 && !uiState.isPlaying
                            ) {
                                Text("下一页 ▶")
                            }
                        }
                        OutlinedButton(
                            onClick = { viewModel.exportAnswers() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.playedWords.isNotEmpty()
                        ) {
                            Text("💾 导出")
                        }
                    }
                }
            }
        }
    }

    // 完成对话框
    if (uiState.isCompleted) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCompletion() },
            title = { Text("🎉 完成") },
            text = { Text("所有单词已完成听写！") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissCompletion() }) {
                    Text("确定")
                }
            }
        )
    }

    // TTS 专门错误提示（自定义样式，带操作按钮）
    ttsError?.let { error ->
        Snackbar(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Column {
                Text(
                    text = "⚠️ TTS 未就绪: $error",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "请尝试：重试 / 安装语音数据 / 打开系统 TTS 设置启用引擎",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.retryTTSInit() }) {
                        Text("重试")
                    }
                    viewModel.getTTSInstallIntent()?.let { intent ->
                        TextButton(onClick = { context.startActivity(intent) }) {
                            Text("安装")
                        }
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent().apply {
                                action = "com.android.settings.TTS_SETTINGS"
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                        }
                    ) {
                        Text("设置")
                    }
                }
            }
        }
    }
}