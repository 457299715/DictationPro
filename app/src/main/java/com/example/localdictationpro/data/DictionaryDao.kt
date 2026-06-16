package com.example.localdictationpro.data

import androidx.room.*
import com.example.localdictationpro.data.entities.Dictionary
import com.example.localdictationpro.data.entities.DictionaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionaries ORDER BY name")
    fun getAllDictionaries(): Flow<List<Dictionary>>

    @Query("UPDATE dictionaries SET entryCount = :count WHERE id = :dictId")
    suspend fun updateEntryCount(dictId: Long, count: Int)

    @Query("SELECT * FROM dictionaries WHERE id = :id")
    suspend fun getDictionaryById(id: Long): Dictionary?

    @Insert
    suspend fun insertDictionary(dictionary: Dictionary): Long

    @Delete
    suspend fun deleteDictionary(dictionary: Dictionary)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<DictionaryEntry>)

    @Query("SELECT meaning FROM dictionary_entries WHERE dictionaryId = :dictId AND word = :word LIMIT 1")
    suspend fun getMeaning(dictId: Long, word: String): String?

    @Query("DELETE FROM dictionary_entries WHERE dictionaryId = :dictId")
    suspend fun deleteEntriesByDictionary(dictId: Long)
}