package com.example.videoplayer.domain.use_case

import com.example.videoplayer.domain.model.VideoFolder
import com.example.videoplayer.domain.repository.VideoRepository

class GetVideosUseCase(private val repository: VideoRepository) {
    suspend operator fun invoke(): List<VideoFolder> {
        return repository.getAllVideos()
    }
}
