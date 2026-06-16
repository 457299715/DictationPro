package com.example.localdictationpro.di

import android.app.Application
import com.example.localdictationpro.tts.TTSManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TTSModule {

    @Provides
    @Singleton
    fun provideTTSManager(application: Application): TTSManager {
        return TTSManager(application)
    }
}