package me.him188.ani.app.videoplayer

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.app.platform.Context
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
actual fun rememberPlayerController(videoSource: Flow<VideoSource<*>?>): PlayerController {
    TODO("Not yet implemented")
}

actual fun PlayerController(context: Context, videoSource: Flow<VideoSource<*>?>): PlayerController {
    return DesktopPlayerController()
}

private class DesktopPlayerController : PlayerController {
    override val state: StateFlow<PlayerState> = MutableStateFlow(PlayerState.PAUSED_BUFFERING)
    override val videoProperties: Flow<VideoProperties> = MutableStateFlow(
        VideoProperties(
            title = "Test Video",
            heightPx = 1080,
            widthPx = 1920,
            videoBitrate = 100,
            audioBitrate = 100,
            frameRate = 30f,
            duration = 100.seconds,
        )
    )
    override val isBuffering: Flow<Boolean> = MutableStateFlow(true)
    override val playedDuration: Flow<Duration> = MutableStateFlow(0.seconds)
    override val bufferProgress: Flow<Float> = MutableStateFlow(0f)
    override val playProgress: Flow<Float> = MutableStateFlow(0f)

    override fun pause() {
    }

    override fun resume() {
    }

    override fun setSpeed(speed: Float) {
    }
}