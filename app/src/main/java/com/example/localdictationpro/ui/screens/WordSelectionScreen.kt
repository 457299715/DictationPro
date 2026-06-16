package com.example.localdictationpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localdictationpro.data.entities.Word
import com.example.localdictationpro.viewmodel.WordSelectionUiState
import com.example.localdictationpro.viewmodel.WordSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: WordSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val bookId = settings?.currentBookId

    LaunchedEffect(bookId) {
        if (bookId != null) {
            viewModel.loadBookData(bookId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择单词范围") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.applySelection()
                            onNavigateBack()
                        }
                    ) {
                        Text("确定")
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
            // 选择类型切换
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.selectedTabIndex == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("按单元") }
                )
                Tab(
                    selected = uiState.selectedTabIndex == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("按单词") }
                )
                Tab(
                    selected = uiState.selectedTabIndex == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("按序号") }
                )
            }

            when (uiState.selectedTabIndex) {
                0 -> UnitSelectionTab(viewModel, uiState)
                1 -> WordListTab(viewModel, uiState)
                2 -> RangeSelectionTab(viewModel, uiState)
            }
        }
    }
}

@Composable
fun UnitSelectionTab(
    viewModel: WordSelectionViewModel,
    uiState: WordSelectionUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.units) { unit ->
            val isSelected = unit in uiState.selectedUnits
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.toggleUnit(unit) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(unit)
                    Text("${uiState.unitWordCounts[unit] ?: 0} 词")
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { viewModel.toggleUnit(unit) }
                    )
                }
            }
        }
    }
}

@Composable
fun WordListTab(
    viewModel: WordSelectionViewModel,
    uiState: WordSelectionUiState
) {
    Column {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("搜索单词...") }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(uiState.filteredWords, key = { it.id }) { word ->
                val isSelected = word.word in uiState.selectedWords
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.toggleWord(word.word) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(word.word)
                            if (word.meaning.isNotEmpty()) {
                                Text(
                                    word.meaning,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleWord(word.word) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RangeSelectionTab(
    viewModel: WordSelectionViewModel,
    uiState: WordSelectionUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("总单词数: ${uiState.totalWords}")
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.rangeStart.toString(),
                onValueChange = { viewModel.updateRangeStart(it.toIntOrNull() ?: 1) },
                label = { Text("起始序号") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.rangeEnd.toString(),
                onValueChange = { viewModel.updateRangeEnd(it.toIntOrNull() ?: 1) },
                label = { Text("结束序号") },
                modifier = Modifier.weight(1f)
            )
        }
        Text("将选择 ${kotlin.math.max(0, uiState.rangeEnd - uiState.rangeStart + 1)} 个单词")

        if (uiState.rangePreview.isNotEmpty()) {
            Text("预览:", style = MaterialTheme.typography.titleSmall)
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.rangePreview) { word ->
                    Text("• $word")
                }
            }
        }
    }
}