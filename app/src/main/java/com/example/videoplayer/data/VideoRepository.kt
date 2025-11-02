package com.example.videoplayer.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class to fetch video files from the device's MediaStore.
 */
class VideoRepository(private val context: Context) {

    /**
     * Scans the device for all video files and groups them by folder.
     *
     * @return A list of [VideoFolder]s, each containing a list of [VideoItem]s.
     */
    suspend fun getAllVideos(): List<VideoFolder> {
        // Use withContext to switch to the IO dispatcher for this blocking operation
        return withContext(Dispatchers.IO) {
            val videoList = mutableListOf<VideoItem>()

            // 1. Define the columns we want to retrieve
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME // The name of the folder
            )

            // 2. Define the query selection and sort order
            val selection = "${MediaStore.Video.Media.DURATION} >= ?"
            val selectionArgs = arrayOf("1000") // Example: only videos longer than 1 second
            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            // 3. Execute the query
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                // 4. Get column indices
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val folderColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                // 5. Iterate over the cursor and create VideoItem objects
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

            // 6. Group the flat list of videos into folders
            videoList.groupBy { it.folderName }
                .map { (folderName, videos) ->
                    VideoFolder(folderName.hashCode().toLong(), folderName, videos)
                }
        }
    }
}
