package com.example.localdictationpro.utils

import android.content.Context
import android.net.Uri
import com.example.localdictationpro.data.AppDatabase
import com.example.localdictationpro.data.entities.Book
import com.example.localdictationpro.data.entities.Dictionary
import com.example.localdictationpro.data.entities.DictionaryEntry
import com.example.localdictationpro.data.entities.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.io.FileInputStream

object FileImporter {

    suspend fun importBook(
        context: Context,
        uri: Uri,
        fileName: String,
        dictionaryId: Long? = null,
        presetFormat: String? = null
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)

            val format = detectBookFormat(context, uri)

            val bookId = database.bookDao().insertBook(
                Book(
                    name = fileName,
                    filePath = uri.toString(),
                    format = format,
                    wordCount = 0
                )
            )

            var position = 1
            var currentUnit: String? = null
            val words = mutableListOf<Word>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.isEmpty()) continue

                    // 检测单元标记
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        currentUnit = trimmed.substring(1, trimmed.length - 1)
                        continue
                    }
                    if (trimmed.startsWith("# ")) {
                        currentUnit = trimmed.substring(2)
                        continue
                    }

                    // 解析单词
                    val (word, meaning) = parseLine(trimmed)
                    if (word.isNotEmpty()) {
                        words.add(
                            Word(
                                bookId = bookId,
                                word = word,
                                meaning = meaning.ifEmpty { "[未找到释义]" },
                                unit = currentUnit,
                                position = position++
                            )
                        )
                    }

                    // 分批插入
                    if (words.size >= 500) {
                        database.wordDao().insertWords(words)
                        words.clear()
                    }
                }
            }

            // 插入剩余单词
            if (words.isNotEmpty()) {
                database.wordDao().insertWords(words)
            }

            // 更新单词计数
            val count = database.wordDao().getWordCount(bookId)
            database.bookDao().updateWordCount(bookId, count)

            bookId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importBook(
        context: Context,
//        uri: Uri,
        file: File,
        fileName: String,
        dictionaryId: Long? = null,
        presetFormat: String? = null
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)

            val format = presetFormat ?: "key_value"

            val bookId = database.bookDao().insertBook(
                Book(
                    name = fileName,
                    filePath = file.absolutePath,
                    format = format,
                    wordCount = 0
                )
            )

            var position = 1
            var currentUnit: String? = null
            val words = mutableListOf<Word>()
            val reader = BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8"))
            reader.use { r ->
                var line: String?
                while (r.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.isEmpty()) continue

                    // 检测单元标记
                    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                        currentUnit = trimmed.substring(1, trimmed.length - 1)
                        continue
                    }
                    if (trimmed.startsWith("# ")) {
                        currentUnit = trimmed.substring(2)
                        continue
                    }

                    // 解析单词
                    val (word, meaning) = parseLine(trimmed)
                    if (word.isNotEmpty()) {
                        words.add(
                            Word(
                                bookId = bookId,
                                word = word,
                                meaning = meaning.ifEmpty { "[未找到释义]" },
                                unit = currentUnit,
                                position = position++
                            )
                        )
                    }

                    // 分批插入
                    if (words.size >= 500) {
                        database.wordDao().insertWords(words)
                        words.clear()
                    }
                }
            }

            // 插入剩余单词
            if (words.isNotEmpty()) {
                database.wordDao().insertWords(words)
            }

            // 更新单词计数
            val count = database.wordDao().getWordCount(bookId)
            database.bookDao().updateWordCount(bookId, count)

            bookId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun detectBookFormat(context: Context, uri: Uri): String {
        var keyValueCount = 0
        var englishOnlyCount = 0

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                var line: String?
                var linesRead = 0

                while (reader.readLine().also { line = it } != null && linesRead < 50) {
                    val trimmed = line!!.trim()
                    if (trimmed.isNotEmpty()) {
                        if (trimmed.contains(":") || trimmed.contains("：") || trimmed.contains("\t")) {
                            keyValueCount++
                        } else if (trimmed.matches(Regex("^[a-zA-Z\\s'-]+$"))) {
                            englishOnlyCount++
                        }
                        linesRead++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (keyValueCount > englishOnlyCount) "key_value" else "english_only"
    }

//    private fun detectBookFormat(context: Context, file: File): String {
//        var keyValueCount = 0
//        var englishOnlyCount = 0
//
//        try {
//            context.contentResolver.openInputStream(file)?.use { inputStream ->
//                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
//                var line: String?
//                var linesRead = 0
//
//                while (reader.readLine().also { line = it } != null && linesRead < 50) {
//                    val trimmed = line!!.trim()
//                    if (trimmed.isNotEmpty()) {
//                        if (trimmed.contains(":") || trimmed.contains("：") || trimmed.contains("\t")) {
//                            keyValueCount++
//                        } else if (trimmed.matches(Regex("^[a-zA-Z\\s'-]+$"))) {
//                            englishOnlyCount++
//                        }
//                        linesRead++
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        return if (keyValueCount > englishOnlyCount) "key_value" else "english_only"
//    }

    private fun parseLine(line: String): Pair<String, String> {
        val separators = listOf(":", "：", "\t")
        for (sep in separators) {
            val index = line.indexOf(sep)
            if (index > 0) {
                return Pair(
                    line.substring(0, index).trim(),
                    line.substring(index + 1).trim()
                )
            }
        }
        return Pair(line.trim(), "")
    }

    suspend fun importDictionary(
        context: Context,
        uri: Uri,
        fileName: String
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)

            val dictId = database.dictionaryDao().insertDictionary(
                Dictionary(
                    name = fileName,
                    filePath = uri.toString(),
                    entryCount = 0
                )
            )

            val entries = mutableListOf<DictionaryEntry>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

                    val (word, meaning) = parseLine(trimmed)
                    if (word.isNotEmpty() && meaning.isNotEmpty()) {
                        entries.add(
                            DictionaryEntry(
                                dictionaryId = dictId,
                                word = word.lowercase(),
                                meaning = meaning
                            )
                        )
                    }

                    if (entries.size >= 500) {
                        database.dictionaryDao().insertEntries(entries)
                        entries.clear()
                    }
                }
            }

            if (entries.isNotEmpty()) {
                database.dictionaryDao().insertEntries(entries)
            }

            // 更新词条计数
            val count = entries.size // 注意这是最后一批的大小，实际应查询总数
            database.dictionaryDao().updateEntryCount(dictId, count)

            dictId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}