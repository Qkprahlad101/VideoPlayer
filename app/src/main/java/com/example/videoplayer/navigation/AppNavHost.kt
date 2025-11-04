package com.example.videoplayer.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.videoplayer.feature_play_from_url.PlayFromUrlScreen
import com.example.videoplayer.ui.permissions.PermissionGatedContent
import com.example.videoplayer.ui.player.PlayerScreen
import com.example.videoplayer.ui.videolist.VideosInFolderScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                var tabState by remember { mutableIntStateOf(0) }
                val titles = listOf("Local Files", "Play from URL")

                Column {
                    PrimaryTabRow(selectedTabIndex = tabState) {
                        titles.forEachIndexed { index, title ->
                            Tab(
                                selected = tabState == index,
                                onClick = { tabState = index },
                                text = { Text(text = title) }
                            )
                        }
                    }
                    when (tabState) {
                        0 -> {
                            // Show Local Files Content
                            PermissionGatedContent { folderId ->
                                navController.navigate(Screen.VideoList.createRoute(folderId))
                            }
                        }
                        1 -> {
                            // Show Play from URL Content
                            PlayFromUrlScreen { videoUrl ->
                                navController.navigate(Screen.Player.createRoute(videoUrl))
                            }
                        }
                    }
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
                    // Pass the navController to the PlayerScreen for back navigation
                    PlayerScreen(
                        videoUri = videoUri,
                        navController = navController
                    )
                }
            }
        }
    }
}
