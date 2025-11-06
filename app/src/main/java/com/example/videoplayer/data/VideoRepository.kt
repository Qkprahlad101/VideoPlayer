package com.example.videoplayer.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.videoplayer.domain.model.VideoFolder
import com.example.videoplayer.domain.model.VideoItem
import com.example.videoplayer.domain.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoRepositoryImpl(private val context: Context) : VideoRepository {

    override suspend fun getAllVideos(): List<VideoFolder> {
        return withContext(Dispatchers.IO) {
            val videoList = mutableListOf<VideoItem>()

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            )

            val selection = "${MediaStore.Video.Media.DURATION} >= ?"
            val selectionArgs = arrayOf("1000")
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getLong(durationColumn)
                    val folderName = cursor.getString(folderColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )

                    videoList.add(VideoItem(contentUri, name, duration, folderName))
                }
            }

            videoList.groupBy { it.folderName }
                .map { (folderName, videos) ->
                    VideoFolder(folderName.hashCode().toLong(), folderName, videos)
                }
        }
    }
}
