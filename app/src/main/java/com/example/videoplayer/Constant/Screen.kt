package com.example.videoplayer.Constant
sealed class Screen(val route: String) {
    object FolderList : Screen("folderList")
    object VideoList : Screen("videoList/{folderId}") {
        fun createRoute(folderId: Long) = "videoList/$folderId"
    }
    object Player : Screen("player/{videoUri}") {
        fun createRoute(videoUri: String) = "player/$videoUri"
    }
}