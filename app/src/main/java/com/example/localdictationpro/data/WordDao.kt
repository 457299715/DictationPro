package com.example.localdictationpro.data

import androidx.room.*
import com.example.localdictationpro.data.entities.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE bookId = :bookId ORDER BY position")
    fun getWordsByBook(bookId: Long): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE bookId = :bookId AND position BETWEEN :start AND :end ORDER BY position")
    fun getWordsByRange(bookId: Long, start: Int, end: Int): Flow<List<Word>>

    @Query("SELECT COUNT(*) FROM words WHERE bookId = :bookId")
    suspend fun getWordCount(bookId: Long): Int

    @Query("SELECT DISTINCT unit FROM words WHERE bookId = :bookId AND unit IS NOT NULL ORDER BY unit")
    suspend fun getUnits(bookId: Long): List<String>

    @Query("SELECT * FROM words WHERE bookId = :bookId AND unit = :unit ORDER BY position")
    fun getWordsByUnit(bookId: Long, unit: String): Flow<List<Word>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Query("DELETE FROM words WHERE bookId = :bookId")
    suspend fun deleteWordsByBook(bookId: Long)
}