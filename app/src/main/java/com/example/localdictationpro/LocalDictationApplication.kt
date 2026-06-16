package com.example.localdictationpro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LocalDictationApplication : Application() {
    // 移除 TTS 初始化，交由 DictationViewModel 按需初始化
}