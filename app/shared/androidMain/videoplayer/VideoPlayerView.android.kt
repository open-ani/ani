package me.him188.ani.app.videoplayer

import android.view.View
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayerView(
    playerController: PlayerController,
    modifier: Modifier
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                val videoView = this
                controllerAutoShow = false
                useController = false
                controllerHideOnTouch = false
                (playerController as? ExoPlayerController)?.let {
                    player = it.player
                    setControllerVisibilityListener(ControllerVisibilityListener { visibility ->
                        if (visibility == View.VISIBLE) {
                            videoView.hideController();
                        }
                    })
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
        },
    )
}