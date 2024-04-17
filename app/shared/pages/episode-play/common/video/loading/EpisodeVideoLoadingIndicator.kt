package me.him188.ani.app.ui.subject.episode.video.loading

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.datasources.api.topic.FileSize
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable // see preview
fun EpisodeVideoLoadingIndicator(
    playerState: PlayerState,
    mediaSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val isBuffering by playerState.isBuffering.collectAsStateWithLifecycle(true)

    val videoDataReady by remember(playerState) {
        playerState.videoData.map { it != null }
    }.collectAsStateWithLifecycle(false)

    val speed by remember(playerState) {
        playerState.videoData.filterNotNull().flatMapLatest { it.downloadSpeed }
    }.collectAsStateWithLifecycle(FileSize.Unspecified)

    if (isBuffering) {
        EpisodeVideoLoadingIndicator(
            EpisodeVideoLoadingState.deduceFrom(mediaSelected, videoDataReady),
            speedProvider = { speed },
            modifier,
        )
    }
}

enum class EpisodeVideoLoadingState {
    SELECTING_MEDIA,
    PREPARING,
    BUFFERING, ;

    companion object {
        fun deduceFrom(
            mediaSelected: Boolean,
            videoDataReady: Boolean,
        ): EpisodeVideoLoadingState = when {
            !mediaSelected -> SELECTING_MEDIA
            !videoDataReady -> PREPARING
            else -> BUFFERING
        }
    }
}

@Composable
fun EpisodeVideoLoadingIndicator(
    state: EpisodeVideoLoadingState,
    speedProvider: () -> FileSize,
    modifier: Modifier = Modifier,
) {
    VideoLoadingIndicator(
        showProgress = state != EpisodeVideoLoadingState.SELECTING_MEDIA,
        text = {
            when (state) {
                EpisodeVideoLoadingState.SELECTING_MEDIA -> {
                    Text("请选择数据源")
                }

                EpisodeVideoLoadingState.PREPARING -> {
                    Text("正在准备资源")
                }

                EpisodeVideoLoadingState.BUFFERING -> {
                    val speed by remember { derivedStateOf(speedProvider) }
                    val text by remember(speed) {
                        derivedStateOf {
                            buildString {
                                append("正在缓冲")
                                if (speed != FileSize.Unspecified) {
                                    appendLine()
                                    append(speed.toString())
                                    append("/s")
                                }
                            }
                        }
                    }

                    Text(text, textAlign = TextAlign.Center)
                }
            }
        },
        modifier,
    )
}
