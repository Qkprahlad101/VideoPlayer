package com.example.videoplayer.ui.folderlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.videoplayer.data.VideoFolder
import com.example.videoplayer.viewmodel.VideoListViewModel

@Composable
fun FolderListScreen(onFolderClick: (Long) -> Unit) {
    val viewModel: VideoListViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
            uiState.videoFolders.isEmpty() -> {
                Text(text = "No video folders found.")
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.videoFolders) { folder ->
                        VideoFolderItem(folder = folder, onClick = { onFolderClick(folder.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun VideoFolderItem(folder: VideoFolder, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = folder.name, style = MaterialTheme.typography.titleMedium)
        Text(text = "${folder.videos.size} videos", style = MaterialTheme.typography.bodySmall)
    }
}
