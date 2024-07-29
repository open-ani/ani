package me.him188.ani.app.videoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.videoplayer.ui.state.PlayerState

/**
 * Displays a video player itself. There is no control bar or any other UI elements.
 *
 * The size of the video player is undefined by default. It may take the entire screen or vise versa.
 * Please apply a size [Modifier] to control the size of the video player.
 */
@Composable
actual fun VideoPlayer(
    playerState: PlayerState,
    modifier: Modifier
) {
    // TODO IOS VideoPlayer
}