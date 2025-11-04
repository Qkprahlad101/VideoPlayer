package com.example.videoplayer.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.nextUp
import kotlin.math.pow

/**
 * A full-screen video player that handles playback, UI states (buffering, errors),
 * orientation changes, and swipe-to-seek gestures.
 *
 * @param videoUri The URI of the video to be played (can be a local file path or a network URL).
 * @param navController The navigation controller used to handle back navigation.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(videoUri: String, navController: NavController) {
    val context = LocalContext.current

    var playerState by remember { mutableStateOf<PlayerState>(PlayerState.Idle) }
    var isOrientationSet by remember { mutableStateOf(false) }

    // State for swipe-to-seek gesture
    var isSeeking by remember { mutableStateOf(false) }
    var seekTime by remember { mutableLongStateOf(0L) }
    var initialSeekPosition by remember { mutableLongStateOf(0L) }

    // State for player controls visibility
    var areControlsVisible by remember { mutableStateOf(false) }
    val bandwidthMeter = remember { DefaultBandwidthMeter.Builder(context).build() }

    val exoPlayer = remember {
        // Set a custom User-Agent to avoid HTTP 403 Forbidden errors.
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(videoUri)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
    }

    val view = LocalView.current
    val window = (view.context as Activity).window

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

    LaunchedEffect(Unit) {
        hideSystemUi()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                playerState = when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        val speed = bandwidthMeter.bitrateEstimate
                        PlayerState.Buffering(formatBitrate(speed))
                    }
                    Player.STATE_READY -> PlayerState.Playing
                    Player.STATE_ENDED -> PlayerState.Ended
                    Player.STATE_IDLE -> PlayerState.Idle
                    else -> playerState
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                playerState = PlayerState.Error(error.message ?: "An unknown error occurred")
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (!isOrientationSet && videoSize.width > 0 && videoSize.height > 0) {
                    if (videoSize.width > videoSize.height) {
                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
                    } else {
                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
                    }
                    isOrientationSet = true
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    fun navigateBack() {
        showSystemUi()
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        navController.popBackStack()
    }

    BackHandler {
        navigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        if (playerState is PlayerState.Playing || playerState is PlayerState.Paused) {
                            change.consume()
                            // Map drag distance to seek time. A larger multiplier makes seeking faster.
                            val seekMultiplier = 200L
                            val seekDelta = dragAmount.nextUp().toLong() * seekMultiplier
                            val newPosition = (initialSeekPosition + seekDelta).coerceIn(0L, exoPlayer.duration)
                            seekTime = newPosition
                            isSeeking = true
                        }
                    },
                    onDragStart = {
                        if (playerState is PlayerState.Playing || playerState is PlayerState.Paused) {
                            initialSeekPosition = exoPlayer.currentPosition
                            isSeeking = true
                        }
                    },
                    onDragEnd = {
                        if (isSeeking) {
                            exoPlayer.seekTo(seekTime)
                            isSeeking = false
                        }
                    }
                )
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    // Listen to the visibility of the default controls
                    setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                        areControlsVisible = visibility == PlayerView.VISIBLE
                    })
                }
            }
        )

        AnimatedVisibility(
            visible = areControlsVisible || isSeeking,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            IconButton(
                modifier = Modifier
                    .padding(16.dp),
                onClick = { navigateBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Show buffering/error states
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
                else -> {}
            }

            // Show seek indicator UI when dragging
            if (isSeeking) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    val duration = exoPlayer.duration.coerceAtLeast(0)
                    Text(
                        text = "${formatDuration(seekTime)} / ${formatDuration(duration)}",
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun formatBitrate(bitsPerSecond: Long): String {
    if (bitsPerSecond < 1000) return "0 Kbps"
    val unit = 1000.0
    val exp = (ln(bitsPerSecond.toDouble()) / ln(unit)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %cbps", bitsPerSecond / unit.pow(exp.toDouble()), pre)
}

/**
 * Formats a duration in milliseconds into a human-readable string (e.g., "01:23" or "1:05:10").
 */
private fun formatDuration(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms)
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
