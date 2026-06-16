package com.example.localdictationpro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictionaries")
data class Dictionary(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String,
    var filePath: String,
    var entryCount: Int = 0,
    var importDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "dictionary_entries")
data class DictionaryEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var dictionaryId: Long,
    var word: String,
    var meaning: String
)