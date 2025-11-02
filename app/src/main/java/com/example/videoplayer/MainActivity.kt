package com.example.videoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.videoplayer.navigation.AppNavHost
import com.example.videoplayer.ui.theme.VIdeoPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VIdeoPlayerTheme {
                AppNavHost()
            }
        }
    }
}
