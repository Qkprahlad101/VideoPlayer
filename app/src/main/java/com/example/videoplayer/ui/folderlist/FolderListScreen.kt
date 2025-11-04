package com.example.videoplayer.ui.folderlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.videoplayer.domain.model.VideoFolder
import com.example.videoplayer.presentation.videolist.VideoListViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * A screen that displays a list of video folders found on the device.
 */
@Composable
fun FolderListScreen(onFolderClick: (Long) -> Unit) {
    val viewModel: VideoListViewModel = koinViewModel()
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(8.dp)
            ) {

                Text(text = folder.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${folder.videos.size} videos",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
