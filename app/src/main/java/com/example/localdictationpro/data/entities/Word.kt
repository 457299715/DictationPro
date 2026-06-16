package com.example.localdictationpro.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Word(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var bookId: Long,
    var word: String,
    var meaning: String,
    var unit: String? = null,
    var position: Int = 0
)