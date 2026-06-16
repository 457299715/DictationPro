package com.example.localdictationpro.utils

import android.content.Context
import com.example.localdictationpro.viewmodel.PlayedWord
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ExportHelper {

    suspend fun exportAnswers(
        context: Context,
        bookName: String,
        playedWords: List<PlayedWord>,
        customPath: String? = null   // 新增可选参数
    ): Boolean {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "dictation_answers_${bookName}_$timestamp.txt"

            // 确定导出目录
            val exportDir = if (!customPath.isNullOrBlank()) {
                File(customPath)
            } else {
                File(context.getExternalFilesDir(null), "exports")
            }

            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val file = File(exportDir, fileName)

            file.bufferedWriter().use { writer ->
                writer.write("听写答案导出\n")
                writer.write("=".repeat(50) + "\n")
                writer.write("书籍: $bookName\n")
                writer.write("导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                writer.write("=".repeat(50) + "\n\n")

                playedWords.forEach { played ->
                    val langTag = if (played.language == "en") "[英文]" else "[中文]"
                    writer.write("${played.word}:${played.meaning} $langTag\n")
                }

                writer.write("\n" + "=".repeat(50) + "\n")
                writer.write("总计: ${playedWords.size} 个单词\n")
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}