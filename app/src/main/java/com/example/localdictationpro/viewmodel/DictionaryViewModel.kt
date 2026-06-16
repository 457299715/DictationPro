package com.example.localdictationpro.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localdictationpro.data.AppDatabase
import com.example.localdictationpro.data.entities.Dictionary
import com.example.localdictationpro.utils.FileImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    val allDictionaries: StateFlow<List<Dictionary>> = database.dictionaryDao().getAllDictionaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    fun importDictionary(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val dictId = FileImporter.importDictionary(getApplication(), uri, fileName)
                if (dictId != null) {
                    database.settingsDao().updateCurrentDictionary(dictId)
                    _uiState.update { it.copy(isLoading = false, importSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "导入失败") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteDictionary(dictionary: Dictionary) {
        viewModelScope.launch {
            database.dictionaryDao().deleteDictionary(dictionary)
        }
    }

    fun setCurrentDictionary(dictId: Long?) {
        viewModelScope.launch {
            database.settingsDao().updateCurrentDictionary(dictId)
        }
    }

    fun clearImportState() {
        _uiState.update { it.copy(importSuccess = false, error = null) }
    }
}

data class DictionaryUiState(
    val isLoading: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null
)