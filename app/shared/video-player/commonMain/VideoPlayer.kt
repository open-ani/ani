package me.him188.ani.app.videoplayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


/**
 * Displays a video player itself. There is no control bar or any other UI elements.
 *
 * The size of the video player is undefined by default. It may take the entire screen or vise versa.
 * Please apply a size [Modifier] to control the size of the video player.
 */
@Composable
expect fun VideoPlayer(
    playerController: PlayerController,
    modifier: Modifier,
)
