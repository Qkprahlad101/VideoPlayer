package com.example.videoplayer.presentation.videolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoplayer.domain.use_case.GetVideosUseCase
// Correctly import the co-located state class
import com.example.videoplayer.presentation.videolist.VideoListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the video list screen.
 * This class is responsible for fetching the video folders and updating the UI state.
 */
class VideoListViewModel(
    private val getVideosUseCase: GetVideosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoListState())
    val uiState: StateFlow<VideoListState> = _uiState.asStateFlow()

    init {
        loadVideoFolders()
    }

    private fun loadVideoFolders() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val videoFolders = getVideosUseCase()
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
