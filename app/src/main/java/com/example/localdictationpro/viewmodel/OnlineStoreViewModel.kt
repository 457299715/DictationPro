// viewmodel/OnlineStoreViewModel.kt
package com.example.localdictationpro.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.localdictationpro.data.AppDatabase
import com.example.localdictationpro.network.WordBankApi
import com.example.localdictationpro.network.models.*
import com.example.localdictationpro.utils.FileImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class OnlineStoreUiState(
    val categories: List<CategoryResponse> = emptyList(),
    val selectedCategoryId: Long? = null,
    val wordBanks: List<WordBankItem> = emptyList(),
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val error: String? = null,
    val importSuccess: Boolean = false,
    val currentPage: Int = 1,
    val totalItems: Int = 0
)

@HiltViewModel
class OnlineStoreViewModel @Inject constructor(
    application: Application,
    private val api: WordBankApi
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(OnlineStoreUiState())
    val uiState: StateFlow<OnlineStoreUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getCategories()
                if (response.code == 0 && response.data != null) {
                    _uiState.update {
                        it.copy(categories = response.data, isLoading = false)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message ?: "加载分类失败") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "网络错误：${e.message}") }
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, currentPage = 1) }
        loadWordBanks()
    }

    fun loadWordBanks(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getWordBankList(
                    categoryId = _uiState.value.selectedCategoryId,
                    page = page,
                    size = 20
                )
                if (response.code == 0 && response.data != null) {
                    _uiState.update {
                        it.copy(
                            wordBanks = if (page == 1) response.data.items
                            else it.wordBanks + response.data.items,
                            totalItems = response.data.total,
                            currentPage = page,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = response.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "网络错误：${e.message}") }
            }
        }
    }

    fun loadNextPage() {
        val nextPage = _uiState.value.currentPage + 1
        if (_uiState.value.wordBanks.size < _uiState.value.totalItems) {
            loadWordBanks(nextPage)
        }
    }

    // 下载并导入词库
    fun downloadAndImport(wordBankId: Long, wordBankName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, error = null) }
            try {
                val detailResp = api.getWordBankDetail(wordBankId)
                if (detailResp.code != 0 || detailResp.data == null) {
                    throw Exception(detailResp.message ?: "获取下载链接失败")
                }
                val fileUrl = detailResp.data.fileUrl
                val format = detailResp.data.fileFormat
                val version = detailResp.data.version

                val file: File
                if (fileUrl.startsWith("file:///android_asset/")) {
                    // 从 assets 复制到缓存目录
                    val assetPath = fileUrl.removePrefix("file:///android_asset/")
                    file = File(getApplication<Application>().cacheDir, wordBankName)
                    getApplication<Application>().assets.open(assetPath).use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    // 正常网络下载
                    file = downloadFile(fileUrl, wordBankName)
                }

                // 获取当前词典 ID
                val settings = database.settingsDao().getSettings().first()
                val dictId = settings?.currentDictionaryId

                // 使用 File 重载导入
                val bookId = withContext(Dispatchers.IO) {
                    FileImporter.importBook(
                        getApplication(),
                        file,          // 直接传 File
                        wordBankName,
                        dictId,
                        format
                    )
                }

                if (bookId != null) {
                    database.bookDao().updateRemoteInfo(bookId, wordBankId, version)
                    try { api.reportDownload(wordBankId) } catch (_: Exception) {}
                    _uiState.update { it.copy(isDownloading = false, importSuccess = true) }
                } else {
                    throw Exception("导入失败")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDownloading = false, error = "下载失败：${e.message}") }
            }
        }
    }

    // 检查已下载的词库是否有更新
    fun checkUpdateForBook(bookId: Long, remoteId: Long, localVersion: Int) {
        viewModelScope.launch {
            try {
                val response = api.checkUpdate(remoteId, localVersion)
                if (response.code == 0 && response.data?.hasUpdate == true) {
                    _uiState.update { it.copy(error = "词库有更新，最新版本 v${response.data.latestVersion}") }
                }
            } catch (e: Exception) {
                Log.e("OnlineStore", "检查更新失败", e)
            }
        }
    }

    private suspend fun downloadFile(url: String, fileName: String): File {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw Exception("下载失败：${response.code}")
            val body = response.body ?: throw Exception("响应体为空")

            val file = File(getApplication<Application>().cacheDir, fileName)

            // 显式获取输出流和输入流，避免 use 类型推断问题
            val outputStream = file.outputStream()
            val inputStream = body.byteStream()

            outputStream.use { fos ->
                inputStream.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Long = 0
                    val contentLength = body.contentLength()

                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        fos.write(buffer, 0, read)
                        bytesRead += read
                        val progress = if (contentLength > 0) bytesRead.toFloat() / contentLength else 0f
                        _uiState.update { it.copy(downloadProgress = progress) }
                    }
                }
            }
            file
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearImportSuccess() {
        _uiState.update { it.copy(importSuccess = false) }
    }
}