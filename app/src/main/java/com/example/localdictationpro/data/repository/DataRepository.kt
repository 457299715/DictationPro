package com.example.localdictationpro.data.repository

import android.content.Context
import com.example.localdictationpro.data.AppDatabase
import com.example.localdictationpro.data.entities.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val database: AppDatabase
) {
    // Books
    fun getAllBooks(): Flow<List<Book>> = database.bookDao().getAllBooks()
    suspend fun getBookById(id: Long): Book? = database.bookDao().getBookById(id)
    suspend fun insertBook(book: Book): Long = database.bookDao().insertBook(book)
    suspend fun deleteBook(book: Book) = database.bookDao().deleteBook(book)
    suspend fun updateWordCount(bookId: Long, count: Int) = database.bookDao().updateWordCount(bookId, count)

    // Words
    fun getWordsByBook(bookId: Long): Flow<List<Word>> = database.wordDao().getWordsByBook(bookId)
    fun getWordsByRange(bookId: Long, start: Int, end: Int): Flow<List<Word>> =
        database.wordDao().getWordsByRange(bookId, start, end)
    suspend fun getWordCount(bookId: Long): Int = database.wordDao().getWordCount(bookId)
    suspend fun getUnits(bookId: Long): List<String> = database.wordDao().getUnits(bookId)
    fun getWordsByUnit(bookId: Long, unit: String): Flow<List<Word>> =
        database.wordDao().getWordsByUnit(bookId, unit)
    suspend fun insertWords(words: List<Word>) = database.wordDao().insertWords(words)
    suspend fun deleteWordsByBook(bookId: Long) = database.wordDao().deleteWordsByBook(bookId)

    // Dictionaries
    fun getAllDictionaries(): Flow<List<Dictionary>> = database.dictionaryDao().getAllDictionaries()
    suspend fun getDictionaryById(id: Long): Dictionary? = database.dictionaryDao().getDictionaryById(id)
    suspend fun insertDictionary(dictionary: Dictionary): Long = database.dictionaryDao().insertDictionary(dictionary)
    suspend fun deleteDictionary(dictionary: Dictionary) = database.dictionaryDao().deleteDictionary(dictionary)
    suspend fun getMeaning(dictId: Long, word: String): String? = database.dictionaryDao().getMeaning(dictId, word)
    suspend fun insertDictionaryEntries(entries: List<DictionaryEntry>) =
        database.dictionaryDao().insertEntries(entries)
    suspend fun deleteEntriesByDictionary(dictId: Long) =
        database.dictionaryDao().deleteEntriesByDictionary(dictId)

    // Settings
    fun getSettings(): Flow<Settings?> = database.settingsDao().getSettings()
    suspend fun insertOrUpdateSettings(settings: Settings) = database.settingsDao().insertOrUpdate(settings)
    suspend fun updateCurrentBook(bookId: Long?) = database.settingsDao().updateCurrentBook(bookId)
    suspend fun updateCurrentDictionary(dictId: Long?) = database.settingsDao().updateCurrentDictionary(dictId)
}