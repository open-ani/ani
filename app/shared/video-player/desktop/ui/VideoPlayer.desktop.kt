package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.videoplayer.data.OpenFailures
import me.him188.ani.app.videoplayer.data.VideoProperties
import me.him188.ani.app.videoplayer.data.VideoSource
import me.him188.ani.app.videoplayer.data.VideoSourceOpenException
import me.him188.ani.app.videoplayer.torrent.FileVideoData
import me.him188.ani.app.videoplayer.torrent.FileVideoSource
import me.him188.ani.app.videoplayer.ui.VlcjVideoPlayerState.VlcjData
import me.him188.ani.app.videoplayer.ui.state.AbstractPlayerState
import me.him188.ani.app.videoplayer.ui.state.PlaybackState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.util.Locale


@Stable
class VlcjVideoPlayerState : PlayerState, AbstractPlayerState<VlcjData>() {
    val component = run {
        NativeDiscovery().discover()
        object : CallbackMediaPlayerComponent() {
            override fun mouseClicked(e: MouseEvent?) {
                super.mouseClicked(e)
                parent.dispatchEvent(e)
            }
        }
    }
    val player = component.mediaPlayer()

    override val state: MutableStateFlow<PlaybackState> = MutableStateFlow(PlaybackState.READY)

    class VlcjData(
        override val videoSource: FileVideoSource,
        override val videoData: FileVideoData,
        releaseResource: () -> Unit
    ) : Data(videoSource, videoData, releaseResource)

    override suspend fun openSource(source: VideoSource<*>): VlcjData {
        if (source !is FileVideoSource) {
            throw VideoSourceOpenException(
                OpenFailures.UNSUPPORTED_VIDEO_SOURCE,
                IllegalStateException("Unsupported video source: $source")
            )
        }

        val data = source.open()

        return VlcjData(
            source,
            data,
            releaseResource = {
                data.close()
            },
        )
    }

    override suspend fun startPlayer(data: VlcjData) {
        player.media().play/*OR .start*/(data.videoData.file.absolutePath)
    }

    override suspend fun cleanupPlayer() {
        player.controls().stop()
    }

    override val videoProperties: MutableStateFlow<VideoProperties?> = MutableStateFlow(null)
    override val currentPositionMillis: MutableStateFlow<Long> = MutableStateFlow(0)

    override fun getExactCurrentPositionMillis(): Long = player.status().time()

    override val bufferedPercentage = MutableStateFlow(0)
    override val playProgress: Flow<Float>
        get() = TODO("Not yet implemented")

    override fun pause() {
        player.controls().pause()
    }

    override fun resume() {
        player.controls().play()
    }

    override val playbackSpeed: MutableStateFlow<Float> = MutableStateFlow(1.0f)

    init {
        player.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
                currentPositionMillis.value = newTime
            }

            override fun lengthChanged(mediaPlayer: MediaPlayer, newLength: Long) {
                videoProperties.value = videoProperties.value?.copy(
                    durationMillis = newLength
                )
            }

            override fun playing(mediaPlayer: MediaPlayer) {
                state.value = PlaybackState.PLAYING
            }

            override fun paused(mediaPlayer: MediaPlayer) {
                state.value = PlaybackState.PAUSED
            }

            override fun stopped(mediaPlayer: MediaPlayer) {
                state.value = PlaybackState.FINISHED
            }

            override fun error(mediaPlayer: MediaPlayer) {
                state.value = PlaybackState.ERROR
            }
        })
    }

    override fun setPlaybackSpeed(speed: Float) {
        player.controls().setRate(speed)
        playbackSpeed.value = speed
    }

    override fun seekTo(positionMillis: Long) {
        player.controls().setTime(positionMillis)
    }

}

@Composable
actual fun VideoPlayer(
    playerState: PlayerState,
    modifier: Modifier,
) {
    check(playerState is VlcjVideoPlayerState)

    val mediaPlayer = playerState.player
    val isFullscreen = false
    LaunchedEffect(isFullscreen) {
        if (mediaPlayer is EmbeddedMediaPlayer) {
            /*
             * To be able to access window in the commented code below,
             * extend the player composable function from WindowScope.
             * See https://github.com/JetBrains/compose-jb/issues/176#issuecomment-812514936
             * and its subsequent comments.
             *
             * We could also just fullscreen the whole window:
             * `window.placement = WindowPlacement.Fullscreen`
             * See https://github.com/JetBrains/compose-multiplatform/issues/1489
             */
            // mediaPlayer.fullScreen().strategy(ExclusiveModeFullScreenStrategy(window))
            mediaPlayer.fullScreen().toggle()
        }
    }
//    DisposableEffect(Unit) { onDispose(mediaPlayer::release) }

    SwingPanel(
        factory = {
            playerState.component
        },
        background = Color.Transparent,
        modifier = modifier.fillMaxSize()
    )
    val surface = playerState.component.videoSurfaceComponent()

    // 转发鼠标事件到 Compose
    DisposableEffect(surface) {
        val listener = object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) = dispatchToCompose(e)
            override fun mousePressed(e: MouseEvent) = dispatchToCompose(e)
            override fun mouseReleased(e: MouseEvent) = dispatchToCompose(e)
            override fun mouseEntered(e: MouseEvent) = dispatchToCompose(e)
            override fun mouseExited(e: MouseEvent) = dispatchToCompose(e)
            override fun mouseDragged(e: MouseEvent) = dispatchToCompose(e)
            override fun mouseMoved(e: MouseEvent) = dispatchToCompose(e)
            override fun mouseWheelMoved(e: MouseWheelEvent) = dispatchToCompose(e)

            fun dispatchToCompose(e: MouseEvent) {
                playerState.component.parent.dispatchEvent(e)
            }
        }
        surface.addMouseListener(listener)
        onDispose {
            surface.removeMouseListener(listener)
        }
    }

    // 转发键盘事件到 Compose
    DisposableEffect(surface) {
        val listener = object : KeyAdapter() {
            override fun keyPressed(p0: KeyEvent) = dispatchToCompose(p0)
            override fun keyReleased(p0: KeyEvent) = dispatchToCompose(p0)
            override fun keyTyped(p0: KeyEvent) = dispatchToCompose(p0)
            fun dispatchToCompose(e: KeyEvent) {
                playerState.component.parent.dispatchEvent(e)
            }
        }
        surface.addKeyListener(listener)
        onDispose {
            surface.removeKeyListener(listener)
        }
    }
}

private fun isMacOS(): Boolean {
    val os = System
        .getProperty("os.name", "generic")
        .lowercase(Locale.ENGLISH)
    return "mac" in os || "darwin" in os
}
