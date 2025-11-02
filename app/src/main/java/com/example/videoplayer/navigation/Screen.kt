package com.example.videoplayer.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Home : Screen("home") // The new main screen with tabs
    object VideoList : Screen("videoList/{folderId}") {
        fun createRoute(folderId: Long) = "videoList/$folderId"
    }
    object Player : Screen("player/{videoUri}") {
        fun createRoute(videoUri: String): String {
            val encodedUri = URLEncoder.encode(videoUri, StandardCharsets.UTF_8.toString())
            return "player/$encodedUri"
        }
    }
}
