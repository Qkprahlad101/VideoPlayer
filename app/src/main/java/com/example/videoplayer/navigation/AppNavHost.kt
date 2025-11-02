package com.example.videoplayer.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.videoplayer.ui.folderlist.FolderListScreen
import com.example.videoplayer.ui.permissions.PermissionGatedContent
import com.example.videoplayer.ui.player.PlayerScreen
import com.example.videoplayer.ui.videolist.VideosInFolderScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavHost() {
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
                    PermissionGatedContent {
                        navController.navigate(Screen.VideoList.createRoute(it))
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
                            onVideoClick = {
                                navController.navigate(Screen.Player.createRoute(it))
                            }
                        )
                    }
                }
                composable(
                    route = Screen.Player.route,
                    arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encodedUri = backStackEntry.arguments?.getString("videoUri")
                    if (encodedUri != null) {
                        val videoUri = URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.toString())
                        PlayerScreen(videoUri = videoUri)
                    }
                }
            }
        }
    }
}
