package com.example.videoplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoplayer.data.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the main video list screen.
 * It fetches video folders from the repository and provides them to the UI.
 */
class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    private val videoRepository = VideoRepository(application)

    // Private mutable state flow that will be updated within the ViewModel.
    private val _uiState = MutableStateFlow(VideoListState())
    // Public immutable state flow that the UI will observe.
    val uiState: StateFlow<VideoListState> = _uiState.asStateFlow()

    // The init block is executed when the ViewModel is first created.
    init {
        loadVideoFolders()
    }

    private fun loadVideoFolders() {
        // Set the initial state to loading.
        _uiState.update { it.copy(isLoading = true) }

        // Launch a coroutine in the viewModelScope.
        // This scope is automatically cancelled when the ViewModel is cleared.
        viewModelScope.launch {
            try {
                val videoFolders = videoRepository.getAllVideos()
                _uiState.update {
                    it.copy(isLoading = false, videoFolders = videoFolders)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load videos: ${e.message}")
                }
            }
        }
    }
}
