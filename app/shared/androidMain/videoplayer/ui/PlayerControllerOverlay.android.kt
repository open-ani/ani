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
import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.app.ProvideCompositionLocalsForPreview
import me.him188.ani.app.torrent.TorrentDownloadSession
import me.him188.ani.app.videoplayer.AbstractPlayerController
import me.him188.ani.app.videoplayer.PlayerState
import me.him188.ani.app.videoplayer.Video
import java.io.File

@Preview(widthDp = 160 * 4, heightDp = 90 * 4, showBackground = true)
@Composable
internal actual fun PreviewVideoControllerOverlay() {
    ProvideCompositionLocalsForPreview {
        val video = remember {
            object : Video {
                override val file: File get() = File("")
                override val totalBytes: Flow<Long> = MutableStateFlow(100)
                override val downloadedBytes: Flow<Long> = MutableStateFlow(70)
                override val downloadRate: StateFlow<Long> = MutableStateFlow(100)
                override val downloadProgress: Flow<Float> = MutableStateFlow(0.7f)
                override val length: StateFlow<Int> = MutableStateFlow(100)
                override val torrentSource: TorrentDownloadSession?
                    get() = null

                override fun close() {
                }
            }
        }
        val controller = remember {
            object : AbstractPlayerController() {
                override val state: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState.PLAYING)

                override val playedDuration: StateFlow<Int> = MutableStateFlow(30)

                override val playProgress: Flow<Float> = MutableStateFlow(0.5f)

                override fun pause() {
                    state.value = PlayerState.PAUSED
                }

                override fun resume() {
                    state.value = PlayerState.PLAYING
                }
            }
        }
        PlayerControllerOverlay(
            topBar = {

            },
            bottomBar = {
                PlayerControllerOverlayBottomBar(
                    video = video,
                    controller = controller,
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            },
        )
    }
}