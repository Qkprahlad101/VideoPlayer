package com.example.videoplayer

import android.app.Application
import com.example.videoplayer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Custom Application class for the Video Player app.
 * This class is the entry point of the application and is used to initialize Koin.
 */
class VideoPlayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin dependency injection.
        // This initializes Koin with the application context and the defined modules.
        startKoin {
            androidContext(this@VideoPlayerApplication)
            modules(appModule)
        }
    }
}
