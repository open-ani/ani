package me.him188.ani.app.videoplayer

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener

@androidx.annotation.OptIn(UnstableApi::class)
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
                subtitleView?.apply {
                    this.setStyle(
                        CaptionStyleCompat(
                            Color.WHITE,
                            0x000000FF,
                            0x00000000,
                            CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                            Color.BLACK,
                            Typeface.DEFAULT,
                        )
                    )
                }
                (playerController as? ExoPlayerController)?.let {
                    player = it.player
                    setControllerVisibilityListener(ControllerVisibilityListener { visibility ->
                        if (visibility == View.VISIBLE) {
                            videoView.hideController()
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