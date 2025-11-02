package com.example.videoplayer.ui.videolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(folder.videos) { video ->
                VideoItemView(video = video, onClick = { onVideoClick(video.uri.toString()) })
            }
        }
    } else {
        // Handle case where folder is not found (e.g., after a process death)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Folder not found.")
        }
    }
}

@Composable
fun VideoItemView(video: VideoItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = video.name, style = MaterialTheme.typography.titleMedium)
        // You can add more details here, like video duration or size
    }
}
