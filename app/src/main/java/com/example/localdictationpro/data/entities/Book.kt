// Book.kt
package com.example.localdictationpro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String,
    var filePath: String,
    var format: String = "key_value",
    var wordCount: Int = 0,
    var importDate: Long = System.currentTimeMillis(),
    // 新增：在线词库来源ID（对应服务端 word_banks.id）
    var remoteId: Long? = null,
    // 新增：下载时的版本号
    var remoteVersion: Int? = null
)