package com.example.localdictationpro.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.localdictationpro.data.entities.Dictionary
import com.example.localdictationpro.viewmodel.BookViewModel
import com.example.localdictationpro.viewmodel.DictionaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceManagerScreen(
    onNavigateBack: () -> Unit,
    bookViewModel: BookViewModel = hiltViewModel(),
    dictionaryViewModel: DictionaryViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val books by bookViewModel.allBooks.collectAsStateWithLifecycle()
    val dictionaries by dictionaryViewModel.allDictionaries.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val bookLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "book.txt"
            bookViewModel.importBook(it, fileName)
        }
    }

    val dictLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "dict.txt"
            dictionaryViewModel.importDictionary(it, fileName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资源管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            when (selectedTab) {
                                0 -> bookLauncher.launch("text/plain")
                                1 -> dictLauncher.launch("text/plain")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "导入")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("书籍") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("词典") }
                )
            }

            when (selectedTab) {
                0 -> ResourceList<Book>(
                    items = books,
                    itemName = { it.name },
                    itemSubtitle = { "单词数: ${it.wordCount}" },
                    onDelete = { bookViewModel.deleteBook(it) }
                )
                1 -> ResourceList<Dictionary>(
                    items = dictionaries,
                    itemName = { it.name },
                    itemSubtitle = { "词条数: ${it.entryCount}" },
                    onDelete = { dictionaryViewModel.deleteDictionary(it) }
                )
            }
        }
    }
}

@Composable
fun <T> ResourceList(
    items: List<T>,
    itemName: (T) -> String,
    itemSubtitle: (T) -> String,
    onDelete: (T) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无资源，请点击右上角导入")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.hashCode() }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(itemName(item), style = MaterialTheme.typography.titleMedium)
                            Text(itemSubtitle(item), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onDelete(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            }
        }
    }
}