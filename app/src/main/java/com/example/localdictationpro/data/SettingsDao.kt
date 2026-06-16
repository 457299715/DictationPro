package com.example.localdictationpro.data

import androidx.room.*
import com.example.localdictationpro.data.entities.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: Settings)

    @Query("UPDATE settings SET speechRate = :rate WHERE id = 1")
    suspend fun updateSpeechRate(rate: Float)

    @Query("UPDATE settings SET theme = :theme WHERE id = 1")
    suspend fun updateTheme(theme: String)

    @Query("UPDATE settings SET currentBookId = :bookId WHERE id = 1")
    suspend fun updateCurrentBook(bookId: Long?)

    @Query("UPDATE settings SET currentDictionaryId = :dictId WHERE id = 1")
    suspend fun updateCurrentDictionary(dictId: Long?)

    @Query("UPDATE settings SET exportPath = :path WHERE id = 1")
    suspend fun updateExportPath(path: String?)
}