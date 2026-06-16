package com.example.localdictationpro.data

import androidx.room.*
import com.example.localdictationpro.data.entities.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY name")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    @Insert
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("UPDATE books SET wordCount = :wordCount WHERE id = :bookId")
    suspend fun updateWordCount(bookId: Long, wordCount: Int)

    @Query("UPDATE books SET remoteId = :remoteId, remoteVersion = :version WHERE id = :bookId")
    suspend fun updateRemoteInfo(bookId: Long, remoteId: Long, version: Int)
}