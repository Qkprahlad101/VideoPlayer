package com.example.videoplayer.ui.player

/**
 * A sealed class representing the various states of the video player.
 * This allows the UI to reactively display information like buffering indicators or error messages.
 */
sealed class PlayerState {
    /**
     * The player is idle or has not been prepared yet.
     */
    data object Idle : PlayerState()

    /**
     * The player is actively buffering content.
     *
     * @param speed A formatted string representing the current network download speed (e.g., "1.2 Mbps").
     */
    data class Buffering(val speed: String) : PlayerState()

    /**
     * The content is actively playing.
     */
    data object Playing : PlayerState()

    /**
     * The content is paused.
     */
    data object Paused : PlayerState()

    /**
     * The content has finished playing.
     */
    data object Ended : PlayerState()

    /**
     * An error occurred during playback.
     *
     * @param message A description of the error.
     */
    data class Error(val message: String) : PlayerState()
}
