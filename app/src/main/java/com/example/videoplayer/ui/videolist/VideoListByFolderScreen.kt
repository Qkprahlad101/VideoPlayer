package com.example.videoplayer.ui.videolist

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.videoplayer.data.VideoItem
import com.example.videoplayer.viewmodel.VideoListViewModel

@Composable
fun VideosInFolderScreen(
    folderId: Long,
    viewModel: VideoListViewModel = viewModel(),
    onVideoClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val folder = uiState.videoFolders.find { it.id == folderId }

    if (folder != null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(folder.videos) { video ->
                VideoItemView(video = video, onClick = { onVideoClick(video.uri.toString()) })
            }
        }
    } else {
        // Handle case where folder is not found (e.g., after a process death)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Folder not found.")
        }
    }
}

@Composable
fun VideoItemView(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = video.name
) {
    val context = LocalContext.current
    val thumbnail = remember(video.uri) {
        val options = BitmapFactory.Options()
        options.inSampleSize = 1
        MediaStore.Video.Thumbnails.getThumbnail(
            context.contentResolver,
            ContentUris.parseId(video.uri),
            MediaStore.Video.Thumbnails.MINI_KIND,
            options
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        thumbnail?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Gray.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Video thumbnail",
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = video.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatDuration(video.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000).toInt()
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format("%d:%02d", minutes, remainingSeconds)
    }
}