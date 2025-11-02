package com.example.videoplayer.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import kotlin.math.ln
import kotlin.math.pow

/**
 * A full-screen video player that handles playback, UI states (buffering, errors),
 * and orientation changes.
 *
 * @param videoUri The URI of the video to be played (can be a local file path or a network URL).
 * @param navController The navigation controller used to handle back navigation.
 */
@Composable
fun PlayerScreen(videoUri: String, navController: NavController) {
    val context = LocalContext.current

    // State to hold the current player status (e.g., Buffering, Playing, Error).
    var playerState by remember { mutableStateOf<PlayerState>(PlayerState.Idle) }

    // State to track if we've already set the orientation. This prevents a rotation loop.
    var isOrientationSet by remember { mutableStateOf(false) }

    // A BandwidthMeter is used to estimate network bandwidth and report it during buffering.
    val bandwidthMeter = remember { DefaultBandwidthMeter.Builder(context).build() }

    // Create and remember the ExoPlayer instance.
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setBandwidthMeter(bandwidthMeter)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(videoUri)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true // Start playback automatically.
            }
    }

    // Get the current window and view to control system UI (full-screen) and orientation.
    val view = LocalView.current
    val window = (view.context as Activity).window

    // These effects are for managing the full-screen mode and orientation.
    fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun showSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
    }

    fun setOrientation(orientation: Int) {
        (view.context as Activity).requestedOrientation = orientation
    }

    // We use LaunchedEffect to enter full-screen mode once.
    LaunchedEffect(Unit) {
        hideSystemUi()
    }

    // This is the core logic for listening to player events and updating the UI state.
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                // Update our custom PlayerState based on ExoPlayer's state.
                playerState = when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        // Get the current network speed from the bandwidth meter.
                        val speed = bandwidthMeter.bitrateEstimate
                        PlayerState.Buffering(formatBitrate(speed))
                    }
                    Player.STATE_READY -> PlayerState.Playing
                    Player.STATE_ENDED -> PlayerState.Ended
                    Player.STATE_IDLE -> PlayerState.Idle
                    else -> playerState // Keep the current state if it's not one of the above
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                // If an error occurs, update the state to show an error message.
                playerState = PlayerState.Error(error.message ?: "An unknown error occurred")
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                // Auto-rotate the screen based on video dimensions, but only do it once
                // to prevent a rotation loop if the player state is unstable.
                if (!isOrientationSet && videoSize.width > 0 && videoSize.height > 0) {
                    if (videoSize.width > videoSize.height) {
                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                    } else {
                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
                    }
                    isOrientationSet = true // Mark that we've set the orientation.
                }
            }
        }

        exoPlayer.addListener(listener)

        // This onDispose block is crucial for cleanup.
        onDispose {
            // It is critical to release the player to free up memory, network, and hardware codecs.
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    // Centralized function to handle all back navigation logic.
    fun navigateBack() {
        // First, restore the system UI and reset the orientation.
        showSystemUi()
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        // Then, navigate back.
        navController.popBackStack()
    }

    // Handle the system back button press.
    BackHandler {
        navigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Black background for the player
    ) {
        // The AndroidView composable is used to embed the traditional Android PlayerView.
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { PlayerView(it).apply { player = exoPlayer } }
        )

        // A simple back button placed at the top-left corner.
        IconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            onClick = { navigateBack() } // Use the centralized back navigation function
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // This Box is for overlaying UI elements like buffering indicators or error messages.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Reactively show UI based on the player's state.
            when (val state = playerState) {
                is PlayerState.Buffering -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Buffering... ${state.speed}", color = Color.White)
                    }
                }
                is PlayerState.Error -> {
                    Text(text = state.message, color = Color.White, modifier = Modifier.padding(16.dp))
                }
                // For other states (Playing, Paused, etc.), show nothing in the overlay.
                else -> {}
            }
        }
    }
}

/**
 * A helper function to format a bitrate in bits per second into a human-readable string (e.g., "1.2 Mbps").
 */
private fun formatBitrate(bitsPerSecond: Long): String {
    if (bitsPerSecond < 1000) return "0 Kbps"
    val unit = 1000.0
    val exp = (ln(bitsPerSecond.toDouble()) / ln(unit)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %cbps", bitsPerSecond / unit.pow(exp.toDouble()), pre)
}
