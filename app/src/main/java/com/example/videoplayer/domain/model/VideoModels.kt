package com.example.videoplayer.domain.model

import android.net.Uri

/**
 * Represents a single video file on the device.
 *
 * @param uri The unique path to the video file.
 * @param name The display name of the video file.
 * @param duration The duration of the video in milliseconds.
 * @param folderName The name of the parent folder containing the video.
 */
data class VideoItem(
    val uri: Uri,
    val name: String,
    val duration: Long,
    val folderName: String
)

/**
 * Represents a folder containing a list of videos.
 *
 * @param name The name of the folder.
 * @param videos The list of [VideoItem]s within this folder.
 */
data class VideoFolder(
    val id: Long,
    val name: String,
    val videos: List<VideoItem>
)
