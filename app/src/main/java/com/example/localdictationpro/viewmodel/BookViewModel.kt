package com.example.localdictationpro.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localdictationpro.data.AppDatabase
import com.example.localdictationpro.data.entities.Book
import com.example.localdictationpro.data.entities.Word
import com.example.localdictationpro.utils.FileImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File

@HiltViewModel
class BookViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    val allBooks: StateFlow<List<Book>> = database.bookDao().getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(BookUiState())
    val uiState: StateFlow<BookUiState> = _uiState.asStateFlow()

    fun importBook(uri: Uri, fileName: String, dictionaryId: Long? = null, presetFormat: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val bookId = FileImporter.importBook(getApplication(), uri, fileName, dictionaryId, presetFormat)
                if (bookId != null) {
                    // 导入成功，可自动设置为当前书籍
                    database.settingsDao().updateCurrentBook(bookId)
                    _uiState.update { it.copy(isLoading = false, importSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "导入失败") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun importBookFromFile(file: File, fileName: String, dictionaryId: Long? = null, presetFormat: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val bookId = FileImporter.importBook(getApplication(), file, fileName, dictionaryId, presetFormat)
                if (bookId != null) {
                    database.settingsDao().updateCurrentBook(bookId)
                    _uiState.update { it.copy(isLoading = false, importSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "导入失败") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            database.bookDao().deleteBook(book)
        }
    }

    fun setCurrentBook(bookId: Long?) {
        viewModelScope.launch {
            database.settingsDao().updateCurrentBook(bookId)
        }
    }

    fun clearImportState() {
        _uiState.update { it.copy(importSuccess = false, error = null) }
    }
}

data class BookUiState(
    val isLoading: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null
)