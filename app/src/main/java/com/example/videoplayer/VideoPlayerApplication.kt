package com.example.videoplayer

import android.app.Application
import android.util.Log
import com.example.videoplayer.di.appModule
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Custom Application class for the Video Player app.
 * This class is the entry point of the application and is used to initialize Koin and other libraries.
 */
class VideoPlayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize YoutubeDL and proactively update it in a background thread.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                YoutubeDL.getInstance().init(this@VideoPlayerApplication)
                // This is the proactive workaround to keep the extraction logic up-to-date.
                val updateResult = YoutubeDL.getInstance().updateYoutubeDL(this@VideoPlayerApplication)
                when (updateResult) {
                    YoutubeDL.UpdateStatus.DONE -> Log.d("VideoPlayerApplication", "YoutubeDL updated successfully.")
                    YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE -> Log.d("VideoPlayerApplication", "YoutubeDL already up to date.")
                    else -> Log.d("VideoPlayerApplication", "YoutubeDL update status: $updateResult")
                }
            } catch (e: YoutubeDLException) {
                Log.e("VideoPlayerApplication", "Failed to initialize or update YoutubeDL", e)
            }
        }

        // Start Koin dependency injection.
        startKoin {
            androidContext(this@VideoPlayerApplication)
            modules(appModule)
        }
    }
}
