package com.example.localdictationpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localdictationpro.data.AppDatabase
import com.example.localdictationpro.data.entities.Settings
import com.example.localdictationpro.data.entities.Word
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordSelectionViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(WordSelectionUiState())
    val uiState: StateFlow<WordSelectionUiState> = _uiState.asStateFlow()

    val settings: StateFlow<Settings?> = database.settingsDao().getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var allWords: List<Word> = emptyList()

    fun loadBookData(bookId: Long) {
        viewModelScope.launch {
            // 获取单元列表
            val units = database.wordDao().getUnits(bookId)
            val unitWordCounts = units.associateWith { unit ->
                // 简化：实际应查询每个单元的单词数
                database.wordDao().getWordsByUnit(bookId, unit).first().size
            }

            // 获取所有单词
            database.wordDao().getWordsByBook(bookId).collect { words ->
                allWords = words
                _uiState.update {
                    it.copy(
                        units = units,
                        unitWordCounts = unitWordCounts,
                        totalWords = words.size,
                        allWords = words
                    )
                }
                updateRangePreview()
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun toggleUnit(unit: String) {
        _uiState.update { state ->
            val newSelected = if (unit in state.selectedUnits) {
                state.selectedUnits - unit
            } else {
                state.selectedUnits + unit
            }
            state.copy(selectedUnits = newSelected)
        }
    }

    fun toggleWord(word: String) {
        _uiState.update { state ->
            val newSelected = if (word in state.selectedWords) {
                state.selectedWords - word
            } else {
                state.selectedWords + word
            }
            state.copy(selectedWords = newSelected)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.allWords
            } else {
                state.allWords.filter { it.word.contains(query, ignoreCase = true) }
            }
            state.copy(searchQuery = query, filteredWords = filtered)
        }
    }

    fun updateRangeStart(start: Int) {
        _uiState.update { it.copy(rangeStart = start.coerceIn(1, it.totalWords)) }
        updateRangePreview()
    }

    fun updateRangeEnd(end: Int) {
        _uiState.update { it.copy(rangeEnd = end.coerceIn(1, it.totalWords)) }
        updateRangePreview()
    }

    private fun updateRangePreview() {
        val state = _uiState.value
        val start = state.rangeStart
        val end = state.rangeEnd
        val preview = if (start <= end) {
            allWords.subList(
                (start - 1).coerceAtLeast(0),
                end.coerceAtMost(allWords.size)
            ).map { it.word }
        } else emptyList()
        _uiState.update { it.copy(rangePreview = preview) }
    }

    fun applySelection() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedWordList = when (state.selectedTabIndex) {
                0 -> {
                    // 按单元：获取选中单元的所有单词
                    val bookId = settings.value?.currentBookId ?: return@launch
                    state.selectedUnits.flatMap { unit ->
                        database.wordDao().getWordsByUnit(bookId, unit).first()
                    }.map { it.word }
                }
                1 -> state.selectedWords.toList()
                2 -> state.rangePreview
                else -> emptyList()
            }

            settings.value?.let { currentSettings ->
                database.settingsDao().insertOrUpdate(
                    currentSettings.copy(
                        wordSelectionType = when (state.selectedTabIndex) {
                            0 -> "units"
                            1 -> "words"
                            2 -> "range"
                            else -> "all"
                        },
                        rangeStart = state.rangeStart,
                        rangeEnd = state.rangeEnd,
                        // 这里需要保存选中的单词列表，但Settings中没有对应字段
                        // 可以添加一个字段或使用其他方式存储
                    )
                )
            }
        }
    }
}

data class WordSelectionUiState(
    val selectedTabIndex: Int = 0,
    val units: List<String> = emptyList(),
    val unitWordCounts: Map<String, Int> = emptyMap(),
    val selectedUnits: Set<String> = emptySet(),
    val allWords: List<Word> = emptyList(),
    val filteredWords: List<Word> = emptyList(),
    val selectedWords: Set<String> = emptySet(),
    val searchQuery: String = "",
    val totalWords: Int = 0,
    val rangeStart: Int = 1,
    val rangeEnd: Int = 1,
    val rangePreview: List<String> = emptyList()
)