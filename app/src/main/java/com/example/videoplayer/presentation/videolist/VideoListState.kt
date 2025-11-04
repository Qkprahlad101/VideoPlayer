package com.example.videoplayer.presentation.videolist

import com.example.videoplayer.domain.model.VideoFolder

/**
 * Represents the state for the VideoListScreen.
 * It is self-contained within the 'presentation/videolist' feature package.
 */
data class VideoListState(
    val isLoading: Boolean = false,
    val videoFolders: List<VideoFolder> = emptyList(),
    val error: String? = null
)
