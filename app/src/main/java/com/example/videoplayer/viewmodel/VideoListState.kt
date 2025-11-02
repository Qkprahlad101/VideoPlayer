package com.example.videoplayer.viewmodel

import com.example.videoplayer.data.VideoFolder

/**
 * Represents the state of the video list screen.
 *
 * @param isLoading True if the video list is currently being loaded.
 * @param videoFolders The list of video folders to display.
 * @param error An optional error message if loading fails.
 */
data class VideoListState(
    val isLoading: Boolean = false,
    val videoFolders: List<VideoFolder> = emptyList(),
    val error: String? = null
)
