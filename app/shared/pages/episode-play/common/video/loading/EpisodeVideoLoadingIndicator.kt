package me.him188.ani.app.ui.subject.episode.video.loading

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.him188.ani.app.ui.subject.episode.VideoSourceState
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.datasources.api.topic.FileSize
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.seconds

@Composable // see preview
fun EpisodeVideoLoadingIndicator(
    playerState: PlayerState,
    mediaSelected: Boolean,
    videoSourceState: () -> VideoSourceState,
    modifier: Modifier = Modifier,
) {
    val isBuffering by playerState.isBuffering.collectAsStateWithLifecycle(true)

    val videoDataReady by remember(playerState) {
        playerState.videoData.map { it != null }
    }.collectAsStateWithLifecycle(false)

    val speed by remember(playerState) {
        playerState.videoData.filterNotNull().flatMapLatest { it.downloadSpeed }
    }.collectAsStateWithLifecycle(FileSize.Unspecified)

    if (isBuffering || !videoDataReady) {
        EpisodeVideoLoadingIndicator(
            EpisodeVideoLoadingState.deduceFrom(mediaSelected, videoDataReady, videoSourceState()),
            speedProvider = { speed },
            modifier,
        )
    }
}

sealed class EpisodeVideoLoadingState {

    companion object {
        @Stable
        fun deduceFrom(
            mediaSelected: Boolean,
            videoDataReady: Boolean,
            videoSourceState: VideoSourceState = VideoSourceState.Initial,
        ): EpisodeVideoLoadingState {
            return when {
                !mediaSelected -> SelectingMedia
                videoSourceState == VideoSourceState.Resolving -> Resolving
                videoSourceState is VideoSourceState.Failed -> Failed(videoSourceState)
                !videoDataReady -> Preparing
                else -> Buffering
            }
        }
    }

    data object SelectingMedia : EpisodeVideoLoadingState()
    data object Preparing : EpisodeVideoLoadingState()
    data object Buffering : EpisodeVideoLoadingState()
    data object Resolving : EpisodeVideoLoadingState()
    data class Failed(val cause: VideoSourceState.Failed) : EpisodeVideoLoadingState()
}

@Composable
fun EpisodeVideoLoadingIndicator(
    state: EpisodeVideoLoadingState,
    speedProvider: () -> FileSize,
    modifier: Modifier = Modifier,
) {
    VideoLoadingIndicator(
        showProgress = state != EpisodeVideoLoadingState.SelectingMedia,
        text = {
            when (state) {
                EpisodeVideoLoadingState.SelectingMedia -> {
                    Text("请选择数据源")
                }

                EpisodeVideoLoadingState.Preparing -> {
                    Text("正在准备资源")
                }

                EpisodeVideoLoadingState.Buffering -> {
                    var tooLong by rememberSaveable {
                        mutableStateOf(false)
                    }
                    val speed by remember { derivedStateOf(speedProvider) }
                    if (speed == FileSize.Zero) {
                        LaunchedEffect(true) {
                            delay(5.seconds)
                            tooLong = true
                        }
                    }
                    val text by remember(speed) {
                        derivedStateOf {
                            buildString {
                                append("正在缓冲")
                                if (speed != FileSize.Unspecified) {
                                    appendLine()
                                    append(speed.toString())
                                    append("/s")
                                }

                                if (tooLong) {
                                    appendLine()
                                    append("检测到连接缓慢, 如有使用代理请尝试关闭后重启应用")
                                }
                            }
                        }
                    }

                    Text(text, textAlign = TextAlign.Center)
                }

                is EpisodeVideoLoadingState.Failed -> {
                    Text("加载失败: ${renderCause(state.cause)}")
                }

                EpisodeVideoLoadingState.Resolving -> {
                    Text("正在解析资源")
                }
            }
        },
        modifier,
    )
}

fun renderCause(cause: VideoSourceState.Failed): String = when (cause) {
    is VideoSourceState.ResolutionTimedOut -> "解析超时"
    is VideoSourceState.UnknownError -> "未知错误"
    is VideoSourceState.UnsupportedMedia -> "不支持该文件类型"
}
