package com.example.localdictationpro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey
    var id: Int = 1,
    var speechRate: Float = 1.0f,
    var theme: String = "Light",
    var currentBookId: Long? = null,
    var currentDictionaryId: Long? = null,
    var intervalSeconds: Int = 3,
    var mode: String = "continuous",
    var randomOrder: Boolean = false,
    var randomLanguage: Boolean = false,
    var englishWeight: Int = 8,
    var chineseWeight: Int = 2,
    var fontSize: Int = 16,
    var displayMeaning: Boolean = false,
    var wordSelectionType: String = "all",
    var rangeStart: Int = 1,
    var rangeEnd: Int = 1,
    var exportPath: String? = null   // 新增导出路径，null 表示使用默认
) {
    constructor() : this(
        id = 1,
        speechRate = 1.0f,
        theme = "Light",
        currentBookId = null,
        currentDictionaryId = null,
        intervalSeconds = 3,
        mode = "continuous",
        randomOrder = false,
        randomLanguage = false,
        englishWeight = 8,
        chineseWeight = 2,
        fontSize = 16,
        displayMeaning = false,
        wordSelectionType = "all",
        rangeStart = 1,
        rangeEnd = 1,
        exportPath = null
    )
}