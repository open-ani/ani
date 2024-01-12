package me.him188.ani.app.videoplayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun VideoPlayerView(
    playerController: PlayerController,
    modifier: Modifier = Modifier,
)
