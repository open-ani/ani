package me.him188.ani.app.videoplayer.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.videoplayer.ExoPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerState

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    playerState: PlayerState,
    modifier: Modifier
) {
    val isPreviewing by rememberUpdatedState(me.him188.ani.app.ui.foundation.LocalIsPreviewing.current)

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                val videoView = this
                if (isPreviewing) {
                    return@apply // preview 时 set 会 ISE
                }
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
                        ),
                    )
                }
                (playerState as? ExoPlayerState)?.let {
                    player = it.player
                    setControllerVisibilityListener(
                        ControllerVisibilityListener { visibility ->
                            if (visibility == View.VISIBLE) {
                                videoView.hideController()
                            }
                        },
                    )
                }
            }
        },
        modifier,
        onRelease = {
        },
        update = { view ->
            (playerState as? ExoPlayerState)?.let {
                view.player = it.player
            }
        },
    )
}