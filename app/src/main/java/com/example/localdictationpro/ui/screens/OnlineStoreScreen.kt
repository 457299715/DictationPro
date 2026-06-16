// ui/screens/OnlineStoreScreen.kt
package com.example.localdictationpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localdictationpro.viewmodel.OnlineStoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineStoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: OnlineStoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            viewModel.clearImportSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("在线词库") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // 分类横向滚动
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("全部") }
                    )
                }
                items(uiState.categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { viewModel.selectCategory(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            HorizontalDivider()

            // 词库列表
            Box(Modifier.fillMaxSize()) {
                if (uiState.isLoading && uiState.wordBanks.isEmpty()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else if (uiState.error != null && uiState.wordBanks.isEmpty()) {
                    Text(
                        text = uiState.error ?: "未知错误",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn {
                        items(uiState.wordBanks) { item ->
                            WordBankCard(
                                item = item,
                                onDownload = { viewModel.downloadAndImport(item.id, item.name) },
                                isDownloading = uiState.isDownloading
                            )
                        }
                        // 加载更多
                        if (uiState.wordBanks.size < uiState.totalItems && !uiState.isLoading) {
                            item {
                                Button(
                                    onClick = { viewModel.loadNextPage() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("加载更多")
                                }
                            }
                        }
                    }
                }

                // 下载进度
                if (uiState.isDownloading) {
                    LinearProgressIndicator(
                        progress = { uiState.downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun WordBankCard(
    item: com.example.localdictationpro.network.models.WordBankItem,
    onDownload: () -> Unit,
    isDownloading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${item.wordCount} 词 · ${item.author} · v${item.version}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = item.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
            IconButton(onClick = onDownload, enabled = !isDownloading) {
                Icon(Icons.Default.CloudDownload, contentDescription = "下载")
            }
        }
    }
}