// network/FakeWordBankApi.kt
package com.example.localdictationpro.network

import com.example.localdictationpro.network.models.*
import kotlinx.coroutines.delay

class FakeWordBankApi : WordBankApi {

    // 模拟的分类
    private val categories = listOf(
        CategoryResponse(1, "中考", "初中升学考试词汇"),
        CategoryResponse(2, "高考", "高中升学考试词汇"),
        CategoryResponse(3, "四级", "大学英语四级"),
        CategoryResponse(4, "六级", "大学英语六级")
    )

    // 模拟的词库数据
    private val wordBanks = listOf(
        WordBankItem(
            id = 1, name = "中考英语核心词汇", description = "中考必考高频词，覆盖历年真题",
            author = "官方", version = 1, fileSize = 20480, wordCount = 500,
            downloadCount = 3280, rating = 4.5f, fileFormat = "key_value",
            createdAt = "2025-03-01T10:00:00Z"
        ),
        WordBankItem(
            id = 2, name = "高考英语3500词", description = "新课标高考大纲词汇",
            author = "官方", version = 2, fileSize = 45000, wordCount = 3500,
            downloadCount = 15200, rating = 4.8f, fileFormat = "key_value",
            createdAt = "2025-04-15T10:00:00Z"
        ),
        WordBankItem(
            id = 3, name = "四级高频词汇", description = "四级考试中高频出现的核心词汇",
            author = "社区用户@英语达人", version = 1, fileSize = 15000, wordCount = 800,
            downloadCount = 890, rating = 4.2f, fileFormat = "english_only",
            createdAt = "2025-05-10T10:00:00Z"
        )
    )

    // 模拟词库详情（包含下载链接）
    private val detailMap = mapOf(
        1L to WordBankDetail(
            id = 1, name = "中考英语核心词汇", description = "中考必考高频词",
            category = categories[0], author = "官方", version = 1,
            fileUrl = "file:///android_asset/demo_wordbank.txt",   // 指向 assets 中的测试文件
            fileFormat = "key_value", wordCount = 500,
            downloadCount = 3280, changelog = "首次发布"
        ),
        2L to WordBankDetail(
            id = 2, name = "高考英语3500词", description = "新课标高考大纲词汇",
            category = categories[1], author = "官方", version = 2,
            fileUrl = "file:///android_asset/demo_wordbank.txt",
            fileFormat = "key_value", wordCount = 3500,
            downloadCount = 15200, changelog = "v2: 增加2025新词"
        ),
        3L to WordBankDetail(
            id = 3, name = "四级高频词汇", description = "四级考试核心词汇",
            category = categories[2], author = "社区用户@英语达人", version = 1,
            fileUrl = "file:///android_asset/demo_wordbank.txt",
            fileFormat = "english_only", wordCount = 800,
            downloadCount = 890, changelog = "首次发布"
        )
    )

    override suspend fun getCategories(): ApiResponse<List<CategoryResponse>> {
        delay(300) // 模拟网络延迟
        return ApiResponse(0, categories, null)
    }

    override suspend fun getWordBankList(
        categoryId: Long?,
        page: Int,
        size: Int,
        keyword: String?,
        sort: String?
    ): ApiResponse<WordBankListResponse> {
        delay(500)
        var filtered = wordBanks
        if (categoryId != null) {
            filtered = filtered.filter {
                detailMap[it.id]?.category?.id == categoryId
            }
        }
        // 分页（简单模拟）
        val total = filtered.size
        val start = (page - 1) * size
        val end = minOf(start + size, total)
        val items = if (start < total) filtered.subList(start, end) else emptyList()
        return ApiResponse(
            0,
            WordBankListResponse(total, items),
            null
        )
    }

    override suspend fun getWordBankDetail(id: Long): ApiResponse<WordBankDetail> {
        delay(200)
        val detail = detailMap[id]
        return if (detail != null) {
            ApiResponse(0, detail, null)
        } else {
            ApiResponse(404, null, "词库不存在")
        }
    }

    override suspend fun checkUpdate(id: Long, localVersion: Int): ApiResponse<UpdateCheckResponse> {
        delay(100)
        val latest = detailMap[id]?.version ?: 0
        return ApiResponse(
            0,
            UpdateCheckResponse(
                hasUpdate = latest > localVersion,
                latestVersion = latest,
                updateType = "full",
                downloadUrl = detailMap[id]?.fileUrl,
                changeLog = "新版本内容"
            ),
            null
        )
    }

    override suspend fun reportDownload(id: Long, clientVersion: String): ApiResponse<Unit> {
        // 不执行实际操作
        return ApiResponse(0, Unit, null)
    }
}