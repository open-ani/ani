package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.videoplayer.AbstractPlayerController
import me.him188.ani.app.videoplayer.PlayerState
import me.him188.ani.app.videoplayer.VideoProperties
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Preview(widthDp = 160 * 4, heightDp = 90 * 4, showBackground = true)
@Composable
internal actual fun PreviewVideoControllerOverlay() {
    ProvideCompositionLocalsForPreview {
        val controller = remember {
            object : AbstractPlayerController() {
                override val state: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState.PLAYING)
                override val videoProperties: Flow<VideoProperties> = MutableStateFlow(
                    VideoProperties(
                        title = "Test Video",
                        heightPx = 1080,
                        widthPx = 1920,
                        videoBitrate = 100,
                        audioBitrate = 100,
                        frameRate = 30f,
                        duration = 100.milliseconds,
                    )
                )
                override val bufferProgress: Flow<Float> = MutableStateFlow(30f)
                override val playedDuration: Flow<Duration> = MutableStateFlow(0.seconds)
                override val playProgress: Flow<Float> = MutableStateFlow(0.5f)

                override fun pause() {
                    state.value = PlayerState.PAUSED
                }

                override fun resume() {
                    state.value = PlayerState.PLAYING
                }

                override fun setSpeed(speed: Float) {
                    TODO("Not yet implemented")
                }
            }
        }
        PlayerControllerOverlay(
            topBar = {

            },
            bottomBar = {
                PlayerControllerOverlayBottomBar(
                    controller = controller,
                    onClickFullScreen = {},
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            },
        )
    }
}