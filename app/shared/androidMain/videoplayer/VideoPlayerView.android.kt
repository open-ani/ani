package me.him188.ani.app.videoplayer

import android.view.View
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.ControllerVisibilityListener
import me.him188.ani.app.app.AppLifeCycle

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
                (playerController as? ExoPlayerController)?.let {
                    player = it.player
                    setControllerVisibilityListener(ControllerVisibilityListener { visibility ->
                        if (visibility == View.VISIBLE) {
                            videoView.hideController()
                        }
                    })
                    // 暂停时保存进度
                    it.player.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (!isPlaying) playerController.updatePlayerPosition(it.player.currentPosition)
                        }
                    })
                    AppLifeCycle.addPausedListener("video_player") {
                        playerController.updatePlayerPosition(it.player.currentPosition)
                    }
                    AppLifeCycle.addDestroyListener("video_player") {
                        playerController.updatePlayerPosition(it.player.currentPosition)
                    }
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