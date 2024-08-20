package me.him188.ani.app.videoplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.videoplayer.LibVlcAndroidPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import org.videolan.libvlc.util.VLCVideoLayout

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    playerState: PlayerState,
    modifier: Modifier
) {
    check(playerState is LibVlcAndroidPlayerState)
    val mediaPlayer = playerState.player
    val isPreviewing by rememberUpdatedState(LocalIsPreviewing.current)

    AndroidView(
        factory = { context ->
            VLCVideoLayout(context)
                .apply {
                    val layout = this
                    if (isPreviewing) {
                        return@apply // preview 时 set 会 ISE
                    }
                    mediaPlayer.attachViews(layout, null, true, false);
                }
        },
        modifier,
        onRelease = {
            mediaPlayer.detachViews()
        },
        update = {
            (playerState as? LibVlcAndroidPlayerState)?.player?.updateVideoSurfaces()
        },
    )


//    AndroidView(
//        factory = { context ->
//            PlayerView(context).apply {
//                val videoView = this
//                if (isPreviewing) {
//                    return@apply // preview 时 set 会 ISE
//                }
//                controllerAutoShow = false
//                useController = false
//                controllerHideOnTouch = false
//                subtitleView?.apply {
//                    this.setStyle(
//                        CaptionStyleCompat(
//                            Color.WHITE,
//                            0x000000FF,
//                            0x00000000,
//                            CaptionStyleCompat.EDGE_TYPE_OUTLINE,
//                            Color.BLACK,
//                            Typeface.DEFAULT,
//                        ),
//                    )
//                }
//                (playerState as? ExoPlayerState)?.let {
//                    player = it.player
//                    setControllerVisibilityListener(
//                        ControllerVisibilityListener { visibility ->
//                            if (visibility == View.VISIBLE) {
//                                videoView.hideController()
//                            }
//                        },
//                    )
//                }
//            }
//        },
//        modifier,
//        onRelease = {
//        },
//        update = { view ->
//            (playerState as? ExoPlayerState)?.let {
//                view.player = it.player
//            }
//        },
//    )
}