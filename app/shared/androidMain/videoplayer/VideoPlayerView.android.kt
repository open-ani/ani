package me.him188.ani.app.videoplayer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView

@Composable
actual fun VideoPlayerView(
    playerController: PlayerController,
    modifier: Modifier
) {
    val density = LocalDensity.current
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                (playerController as? ExoPlayerController)?.let {
                    player = it.player
                }
            }
        },
        modifier,
        onRelease = {
        },
        update = { view ->
            (playerController as? ExoPlayerController)?.let {
                view.player = it.player
            }
//                view.layoutParams = view.layoutParams.apply {
//                    width = maxWidth
//                    height = (maxWidth * 9 / 16).toInt()
//                }
        },
    )
}