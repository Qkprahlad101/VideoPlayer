package com.example.videoplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.videoplayer.data.VideoFolder
import com.example.videoplayer.data.VideoItem
import com.example.videoplayer.ui.theme.VIdeoPlayerTheme
import com.example.videoplayer.viewmodel.VideoListState
import com.example.videoplayer.viewmodel.VideoListViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// 1. Define Navigation Routes for type safety
sealed class Screen(val route: String) {
    object FolderList : Screen("folderList")
    object VideoList : Screen("videoList/{folderId}") {
        fun createRoute(folderId: Long) = "videoList/$folderId"
    }
    object Player : Screen("player/{videoUri}") {
        fun createRoute(videoUri: String) = "player/$videoUri"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VIdeoPlayerTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Video Player",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            NavHost(navController = navController, startDestination = Screen.FolderList.route) {
                composable(Screen.FolderList.route) {
                    PermissionGatedContent { folderId ->
                        navController.navigate(Screen.VideoList.createRoute(folderId))
                    }
                }
                composable(
                    route = Screen.VideoList.route,
                    arguments = listOf(navArgument("folderId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val folderId = backStackEntry.arguments?.getLong("folderId")
                    if (folderId != null) {
                        VideosInFolderScreen(
                            folderId = folderId,
                            onVideoClick = { videoUri ->
                                val encodedUri = URLEncoder.encode(videoUri, StandardCharsets.UTF_8.toString())
                                navController.navigate(Screen.Player.createRoute(encodedUri))
                            }
                        )
                    }
                }
                composable(
                    route = Screen.Player.route,
                    arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val videoUri = backStackEntry.arguments?.getString("videoUri")
                    if (videoUri != null) {
                        PlayerScreen(videoUri = videoUri)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerScreen(videoUri: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
            }
        }
    )
}


@Composable
fun PermissionGatedContent(onFolderClick: (Long) -> Unit) {
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasPermission = isGranted }
    )

    LaunchedEffect(key1 = hasPermission) {
        if (!hasPermission) {
            launcher.launch(permission)
        }
    }

    if (hasPermission) {
        val viewModel: VideoListViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsState()
        VideoListScreen(uiState = uiState, onFolderClick = onFolderClick)
    } else {
        PermissionRationaleUI(onPermissionRequested = { launcher.launch(permission) })
    }
}

@Composable
fun VideoListScreen(uiState: VideoListState, onFolderClick: (Long) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
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

@Composable
fun PermissionRationaleUI(onPermissionRequested: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "This app needs to read video files from your device to display them.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onPermissionRequested) {
            Text("Request Permission")
        }
    }
}
