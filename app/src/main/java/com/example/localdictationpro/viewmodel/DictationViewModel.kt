package com.example.localdictationpro.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localdictationpro.data.AppDatabase
import com.example.localdictationpro.data.entities.Settings
import com.example.localdictationpro.data.entities.Word
import com.example.localdictationpro.tts.TTSInitStatus
import com.example.localdictationpro.tts.TTSManager
import com.example.localdictationpro.tts.TTSEvent
import com.example.localdictationpro.utils.ExportHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DictationViewModel @Inject constructor(
    application: Application,
    private val ttsManager: TTSManager
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(DictationUiState())
    val uiState: StateFlow<DictationUiState> = _uiState.asStateFlow()

    val settings: StateFlow<Settings?> = database.settingsDao().getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var words: List<Word> = emptyList()
    private var timerJob: Job? = null

    private var ttsReady = false
    private val _ttsError = MutableStateFlow<String?>(null)
    val ttsError: StateFlow<String?> = _ttsError.asStateFlow()

    private var lastBookId: Long? = null  // 记录上次加载的书籍ID，避免重复加载

    init {
        // 初始化 TTS
        ttsManager.initialize { success, status ->
            ttsReady = success
            if (!ttsReady) {
                // 根据状态给出更具体的错误提示
                _ttsError.value = when (status) {
                    TTSInitStatus.NO_ENGINE_INSTALLED -> "未检测到TTS引擎，请在设置中安装并启用。"
                    TTSInitStatus.LANG_MISSING_DATA -> "缺少英文语音数据，请下载英文语音包。"
                    else -> "TTS未就绪: ${ttsManager.getInitErrorDescription()}"
                }
                _uiState.update { it.copy(error = _ttsError.value ?: "TTS未就绪") }
            } else {
                _ttsError.value = null
            }
        }

        // 监听设置变化
        viewModelScope.launch {
            settings.collect { settings ->
                settings?.let { s ->
                    ttsManager.setSpeechRate(s.speechRate)
                    _uiState.update { state ->
                        state.copy(
                            mode = s.mode,
                            randomOrder = s.randomOrder,
                            randomLanguage = s.randomLanguage,
                            displayMeaning = s.displayMeaning
                        )
                    }

                    // 仅当书籍ID真正变化时才重新加载书籍
                    val bookId = s.currentBookId
                    if (bookId != lastBookId) {
                        lastBookId = bookId
                        if (bookId != null) {
                            loadBook(bookId)
                        } else {
                            // 清空单词列表
                            words = emptyList()
                            _uiState.update {
                                it.copy(
                                    bookName = null,
                                    totalWords = 0,
                                    rangeInfo = "未选择书籍",
                                    currentIndex = 0,
                                    currentWord = "---",
                                    currentMeaning = "",
                                    progress = 0f,
                                    playedWords = emptyList()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadBook(bookId: Long) {
        viewModelScope.launch {
            val book = database.bookDao().getBookById(bookId)
            database.wordDao().getWordsByBook(bookId).collect { wordsList ->
                words = wordsList
                _uiState.update {
                    it.copy(
                        bookName = book?.name,
                        totalWords = words.size,
                        rangeInfo = "共 ${words.size} 个单词",
                        progress = 0f,
                        currentIndex = 0,
                        playedWords = emptyList()
                    )
                }
                updateCurrentWord()
            }
        }
    }

    private fun updateCurrentWord() {
        val index = _uiState.value.currentIndex
        if (words.isNotEmpty() && index < words.size) {
            val word = words[index]
            _uiState.update {
                it.copy(
                    currentWord = word.word,
                    currentMeaning = word.meaning,
                    progress = (index + 1).toFloat() / words.size
                )
            }
        }
    }

    fun start() {
        clearError()
        if (words.isEmpty()) {
            _uiState.update { it.copy(error = "没有单词可听写") }
            return
        }

        if (_uiState.value.isPlaying) return

        // 再次检查 TTS 就绪状态
        if (!ttsReady) {
            _ttsError.value = when {
                ttsManager.isLanguageDataMissing() -> "缺少英文语音数据，请下载英文语音包。"
                ttsManager.currentStatus == TTSInitStatus.NO_ENGINE_INSTALLED -> "未检测到TTS引擎，请在设置中安装并启用。"
                else -> "TTS未就绪: ${ttsManager.getInitErrorDescription()}"
            }
            _uiState.update { it.copy(error = _ttsError.value ?: "TTS未就绪") }
            return
        }

        _uiState.update { it.copy(isPlaying = true) }
        playCurrentWord()
    }

    private fun playCurrentWord() {
        val index = _uiState.value.currentIndex
        if (index >= words.size) {
            _uiState.update { it.copy(isPlaying = false, isCompleted = true) }
            return
        }

        val word = words[index]
        val settings = settings.value

        val language = if (settings?.randomLanguage == true) {
            val weight = listOf(settings.englishWeight, settings.chineseWeight)
            if (Random.nextInt(weight.sum()) < settings.englishWeight) "en" else "zh"
        } else {
            "en"
        }

        val textToSpeak = if (language == "en") word.word else word.meaning

        val playedWord = PlayedWord(
            word = word.word,
            meaning = word.meaning,
            language = language
        )

        _uiState.update { state ->
            val newList = state.playedWords + playedWord
            state.copy(playedWords = newList)
        }

        viewModelScope.launch {
            ttsManager.speak(textToSpeak).collect { event ->
                when (event) {
                    is TTSEvent.Done -> {
                        onTTSFinished()
                    }
                    is TTSEvent.Error -> {
                        _uiState.update { it.copy(error = event.message) }
                        _uiState.update { it.copy(isPlaying = false) }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun onTTSFinished() {
        val mode = _uiState.value.mode
        val settings = settings.value

        if (mode == "continuous" && _uiState.value.isPlaying) {
            timerJob = viewModelScope.launch {
                delay((settings?.intervalSeconds ?: 3) * 1000L)
                val nextIndex = _uiState.value.currentIndex + 1
                if (nextIndex < words.size) {
                    _uiState.update { it.copy(currentIndex = nextIndex) }
                    updateCurrentWord()
                    playCurrentWord()
                } else {
                    _uiState.update { it.copy(isPlaying = false, isCompleted = true) }
                }
            }
        } else if (mode == "single") {
            _uiState.update { it.copy(isPlaying = false) }
        }
    }

    fun pause() {
        timerJob?.cancel()
        ttsManager.stop()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun stop() {
        pause()
        _uiState.update {
            it.copy(
                currentIndex = 0,
                isPlaying = false,
                progress = 0f,
                playedWords = emptyList()
            )
        }
        updateCurrentWord()
    }

    fun nextWord() {
        if (_uiState.value.mode == "single" && !_uiState.value.isPlaying) {
            val nextIndex = _uiState.value.currentIndex + 1
            if (nextIndex < words.size) {
                _uiState.update { it.copy(currentIndex = nextIndex) }
                updateCurrentWord()
                start()
            }
        }
    }

    fun previousWord() {
        if (_uiState.value.mode == "single" && !_uiState.value.isPlaying) {
            val prevIndex = _uiState.value.currentIndex - 1
            if (prevIndex >= 0) {
                _uiState.update { it.copy(currentIndex = prevIndex) }
                updateCurrentWord()
                start()
            }
        }
    }

    fun setMode(mode: String) {
        viewModelScope.launch {
            settings.value?.let {
                database.settingsDao().insertOrUpdate(it.copy(mode = mode))
            }
        }
    }

    fun setRandomOrder(enabled: Boolean) {
        viewModelScope.launch {
            settings.value?.let {
                database.settingsDao().insertOrUpdate(it.copy(randomOrder = enabled))
            }
            if (enabled && words.isNotEmpty()) {
                words = words.shuffled()
                _uiState.update { it.copy(currentIndex = 0) }
                updateCurrentWord()
            }
        }
    }

    fun setRandomLanguage(enabled: Boolean) {
        viewModelScope.launch {
            settings.value?.let {
                database.settingsDao().insertOrUpdate(it.copy(randomLanguage = enabled))
            }
        }
    }

    fun setDisplayMeaning(enabled: Boolean) {
        viewModelScope.launch {
            settings.value?.let {
                database.settingsDao().insertOrUpdate(it.copy(displayMeaning = enabled))
            }
        }
    }

    fun setInterval(seconds: Int) {
        viewModelScope.launch {
            settings.value?.let {
                database.settingsDao().insertOrUpdate(it.copy(intervalSeconds = seconds))
            }
        }
    }

    fun exportAnswers() {
        viewModelScope.launch {
            val bookName = _uiState.value.bookName ?: "unknown"
            val listToExport = _uiState.value.playedWords
            val exportPath = settings.value?.exportPath
            val success = ExportHelper.exportAnswers(
                getApplication(),
                bookName,
                listToExport,
                exportPath
            )
            if (success) {
                _uiState.update { it.copy(error = null) }
            } else {
                _uiState.update { it.copy(error = "导出失败，请检查存储权限或路径") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun dismissCompletion() {
        _uiState.update { it.copy(isCompleted = false) }
    }

    fun retryTTSInit() {
        ttsManager.shutdown()
        ttsManager.initialize { success, status ->
            ttsReady = success
            if (!ttsReady) {
                _ttsError.value = when (status) {
                    TTSInitStatus.NO_ENGINE_INSTALLED -> "未检测到TTS引擎，请在设置中安装并启用。"
                    TTSInitStatus.LANG_MISSING_DATA -> "缺少英文语音数据，请下载英文语音包。"
                    else -> "TTS未就绪: ${ttsManager.getInitErrorDescription()}"
                }
                _uiState.update { it.copy(error = _ttsError.value ?: "TTS未就绪") }
            } else {
                _ttsError.value = null
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    fun getTTSInstallIntent(): Intent? = ttsManager.installTTSData()

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        timerJob?.cancel()
    }
}

data class DictationUiState(
    val bookName: String? = null,
    val totalWords: Int = 0,
    val currentIndex: Int = 0,
    val currentWord: String = "---",
    val currentMeaning: String = "",
    val progress: Float = 0f,
    val isPlaying: Boolean = false,
    val mode: String = "continuous",
    val randomOrder: Boolean = false,
    val randomLanguage: Boolean = false,
    val displayMeaning: Boolean = false,
    val rangeInfo: String = "准备开始听写",
    val isCompleted: Boolean = false,
    val error: String? = null,
    val playedWords: List<PlayedWord> = emptyList()
)