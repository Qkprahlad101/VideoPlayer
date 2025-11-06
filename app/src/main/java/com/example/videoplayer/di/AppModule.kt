package com.example.videoplayer.di

import com.example.videoplayer.data.VideoRepositoryImpl
import com.example.videoplayer.domain.repository.VideoRepository
import com.example.videoplayer.domain.use_case.GetVideosUseCase
import com.example.videoplayer.presentation.videolist.VideoListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for providing application-wide dependencies.
 * This setup replaces the Hilt/Dagger module.
 */
val appModule = module {

    // Provides a singleton instance of VideoRepository.
    // The 'single' keyword ensures there is only one instance throughout the app's lifecycle.
    single<VideoRepository> {
        VideoRepositoryImpl(get())
    }

    // Provides a factory for GetVideosUseCase.
    // The 'factory' keyword creates a new instance every time it is requested.
    factory {
        GetVideosUseCase(get())
    }

    // Provides a ViewModel instance for VideoListViewModel.
    // The 'viewModel' keyword links its lifecycle to a Composable screen or an Activity/Fragment.
    viewModel {
        VideoListViewModel(get())
    }
}
