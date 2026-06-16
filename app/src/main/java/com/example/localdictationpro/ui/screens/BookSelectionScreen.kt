package com.example.localdictationpro.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localdictationpro.data.entities.Book
import com.example.localdictationpro.viewmodel.BookViewModel
import com.example.localdictationpro.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSelectionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOnlineStore: () -> Unit,   // 新增
    bookViewModel: BookViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val books by bookViewModel.allBooks.collectAsStateWithLifecycle()
    val uiState by bookViewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val currentBookId = settings?.currentBookId

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "unknown.txt"
            val dictId = settings?.currentDictionaryId
            bookViewModel.importBook(it, fileName, dictId)
        }
    }

    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            bookViewModel.clearImportState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择书籍") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                },
                actions = {
                    IconButton(onClick = { launcher.launch("text/plain") }) {
                        Icon(Icons.Default.Add, contentDescription = "导入书籍")
                    }
                    IconButton(onClick = onNavigateToOnlineStore) {
                        Icon(Icons.Default.Cloud, contentDescription = "在线词库")
                    }
                    IconButton(onClick = { launcher.launch("text/plain") }) {
                        Icon(Icons.Default.Add, contentDescription = "导入书籍")
                    }

                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books) { book ->
                    BookItem(
                        book = book,
                        isSelected = book.id == currentBookId,
                        onSelect = {
                            bookViewModel.setCurrentBook(book.id)
                        },
                        onDelete = {
                            bookViewModel.deleteBook(book)
                        }
                    )
                }

                if (books.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无书籍，请点击右上角导入")
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { bookViewModel.clearImportState() }) {
                            Text("关闭")
                        }
                    }
                ) {
                    Text(uiState.error ?: "")
                }
            }
        }
    }
}

@Composable
fun BookItem(
    book: Book,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    // 修改选中背景色为更浅的色调
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "单词数: ${book.wordCount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                if (!isSelected) {
                    TextButton(onClick = onSelect) {
                        Text("选择")
                    }
                } else {
                    Text(
                        "当前使用",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}