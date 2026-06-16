package com.example.localdictationpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localdictationpro.data.AppDatabase
import com.example.localdictationpro.data.entities.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    val settings: StateFlow<Settings?> = database.settingsDao().getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateSpeechRate(rate: Float) {
        viewModelScope.launch {
            database.settingsDao().updateSpeechRate(rate)
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            database.settingsDao().updateTheme(theme)
        }
    }

    fun updateCurrentBook(bookId: Long?) {
        viewModelScope.launch {
            database.settingsDao().updateCurrentBook(bookId)
        }
    }

    fun updateCurrentDictionary(dictId: Long?) {
        viewModelScope.launch {
            database.settingsDao().updateCurrentDictionary(dictId)
        }
    }

    fun updateSettings(update: Settings.() -> Settings) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(update(current))
            }
        }
    }

    fun updateInterval(seconds: Int) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(current.copy(intervalSeconds = seconds))
            }
        }
    }

    fun updateMode(mode: String) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(current.copy(mode = mode))
            }
        }
    }

    fun updateRandomOrder(enabled: Boolean) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(current.copy(randomOrder = enabled))
            }
        }
    }

    fun updateRandomLanguage(enabled: Boolean) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(current.copy(randomLanguage = enabled))
            }
        }
    }

    fun updateWeights(englishWeight: Int, chineseWeight: Int) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(
                    current.copy(englishWeight = englishWeight, chineseWeight = chineseWeight)
                )
            }
        }
    }

    fun updateFontSize(size: Int) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(current.copy(fontSize = size))
            }
        }
    }

    fun updateDisplayMeaning(enabled: Boolean) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(current.copy(displayMeaning = enabled))
            }
        }
    }

    fun updateWordSelectionType(type: String) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(current.copy(wordSelectionType = type))
            }
        }
    }

    fun updateRange(start: Int, end: Int) {
        viewModelScope.launch {
            settings.value?.let { current ->
                database.settingsDao().insertOrUpdate(current.copy(rangeStart = start, rangeEnd = end))
            }
        }
    }

    fun updateExportPath(path: String?) {
        viewModelScope.launch {
            database.settingsDao().updateExportPath(path)
        }
    }
}