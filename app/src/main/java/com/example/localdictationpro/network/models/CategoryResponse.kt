// network/models/CategoryResponse.kt
package com.example.localdictationpro.network.models

data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String?
)

data class WordBankItem(
    val id: Long,
    val name: String,
    val description: String?,
    val author: String,
    val version: Int,
    val fileSize: Long,
    val wordCount: Int,
    val downloadCount: Int,
    val rating: Float?,
    val fileFormat: String,
    val createdAt: String
)

data class WordBankListResponse(
    val total: Int,
    val items: List<WordBankItem>
)

data class WordBankDetail(
    val id: Long,
    val name: String,
    val description: String?,
    val category: CategoryResponse?,
    val author: String,
    val version: Int,
    val fileUrl: String,          // 临时下载链接
    val fileFormat: String,
    val wordCount: Int,
    val downloadCount: Int,
    val changelog: String?
)

data class UpdateCheckResponse(
    val hasUpdate: Boolean,
    val latestVersion: Int?,
    val updateType: String?,      // "full" or "incremental"
    val downloadUrl: String?,
    val changeLog: String?
)

// 通用响应包装
data class ApiResponse<T>(
    val code: Int,
    val data: T?,
    val message: String?
)