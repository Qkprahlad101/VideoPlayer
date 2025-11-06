package com.example.videoplayer.domain.repository

import com.example.videoplayer.domain.model.VideoFolder

interface VideoRepository {
    suspend fun getAllVideos(): List<VideoFolder>
}
