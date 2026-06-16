package com.example.localdictationpro.tts

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

/**
 * TTS初始化状态枚举
 */
enum class TTSInitStatus {
    SUCCESS,                // 完全就绪
    ERROR,                  // 通用初始化错误
    LANG_MISSING_DATA,      // 缺少语言数据（如英文语音包）
    LANG_NOT_SUPPORTED,     // 语言不支持
    NO_ENGINE_INSTALLED     // 没有安装任何TTS引擎
}

class TTSManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var speechRate = 1.0f
    private var initStatus: TTSInitStatus = TTSInitStatus.NO_ENGINE_INSTALLED

    // Google TTS 的包名
    private val googleTTSPackageName = "com.google.android.tts"

    /**
     * 公开当前初始化状态
     */
    val currentStatus: TTSInitStatus get() = initStatus

    /**
     * 初始化TTS引擎，自动选择系统默认引擎
     * @param callback 初始化完成回调，参数1：是否完全就绪，参数2：详细状态
     */
    fun initialize(callback: (Boolean, TTSInitStatus) -> Unit = { _, _ -> }) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                initStatus = when (result) {
                    TextToSpeech.LANG_MISSING_DATA -> TTSInitStatus.LANG_MISSING_DATA
                    TextToSpeech.LANG_NOT_SUPPORTED -> TTSInitStatus.LANG_NOT_SUPPORTED
                    else -> {
                        tts?.setSpeechRate(speechRate)
                        TTSInitStatus.SUCCESS
                    }
                }
                callback(initStatus == TTSInitStatus.SUCCESS, initStatus)
            } else {
                initStatus = TTSInitStatus.ERROR
                callback(false, initStatus)
            }
        }
    }

    /**
     * 获取所有已安装的TTS引擎列表
     */
    fun getInstalledEngines(): List<TextToSpeech.EngineInfo>? {
        return tts?.engines
    }

    /**
     * 获取当前默认引擎的包名
     */
    fun getDefaultEngine(): String? {
        return tts?.defaultEngine
    }

    /**
     * 判断Google TTS是否已安装
     */
    fun isGoogleTTsInstalled(): Boolean {
        return getInstalledEngines()?.any { it.name == googleTTSPackageName } ?: false
    }

    /**
     * 判断Google TTS是否为当前默认引擎
     */
    fun isGoogleTTsDefault(): Boolean {
        return getDefaultEngine() == googleTTSPackageName
    }

    /**
     * 设置语速
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    /**
     * 播放文本，返回Flow以监听播放事件
     */
    fun speak(text: String): Flow<TTSEvent> = callbackFlow {
        if (tts == null || initStatus != TTSInitStatus.SUCCESS) {
            trySend(TTSEvent.Error("TTS未就绪，状态: $initStatus"))
            close()
            return@callbackFlow
        }

        val utteranceId = UUID.randomUUID().toString()
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                trySend(TTSEvent.Start)
            }
            override fun onDone(utteranceId: String?) {
                trySend(TTSEvent.Done)
                close()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                trySend(TTSEvent.Error("TTS 播放错误"))
                close()
            }
        })

        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            trySend(TTSEvent.Error("播放失败，请重试"))
            close()
        }

        awaitClose {
            tts?.setOnUtteranceProgressListener(null)
            tts?.stop()
        }
    }

    /**
     * 停止播放
     */
    fun stop() = tts?.stop()

    /**
     * 关闭TTS并释放资源
     */
    fun shutdown() {
        tts?.shutdown()
        tts = null
        initStatus = TTSInitStatus.NO_ENGINE_INSTALLED
    }

    /**
     * 是否正在播放
     */
    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    /**
     * 是否完全就绪（引擎正常且语音数据完整）
     */
    fun isReady(): Boolean = initStatus == TTSInitStatus.SUCCESS

    /**
     * 是否缺少语言数据
     */
    fun isLanguageDataMissing(): Boolean = initStatus == TTSInitStatus.LANG_MISSING_DATA

    /**
     * 获取错误描述字符串
     */
    fun getInitErrorDescription(): String {
        return when (initStatus) {
            TTSInitStatus.ERROR -> "TTS引擎未启用或未安装。"
            TTSInitStatus.LANG_MISSING_DATA -> "缺少英文语音数据，请下载英文语音包。"
            TTSInitStatus.LANG_NOT_SUPPORTED -> "当前语言不支持。"
            TTSInitStatus.NO_ENGINE_INSTALLED -> "未检测到任何TTS引擎。"
            TTSInitStatus.SUCCESS -> ""
        }
    }

    /**
     * 引导安装TTS语音数据（如英文语音包）
     */
    fun installTTSData(): Intent? {
        return try {
            Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 打开系统TTS设置页面
     */
    fun openTTSSettings(): Intent {
        return Intent().apply {
            action = "com.android.settings.TTS_SETTINGS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * 跳转到指定应用的应用详情页（用于启用或下载Google TTS）
     */
    fun openAppInfo(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}

sealed class TTSEvent {
    object Start : TTSEvent()
    object Done : TTSEvent()
    data class Error(val message: String) : TTSEvent()
}