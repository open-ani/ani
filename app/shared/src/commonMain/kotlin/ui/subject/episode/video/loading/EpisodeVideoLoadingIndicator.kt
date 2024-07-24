package me.him188.ani.app.ui.subject.episode.video.loading

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import me.him188.ani.app.ui.foundation.TextWithBorder
import me.him188.ani.app.ui.subject.episode.statistics.VideoLoadingState
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import me.him188.ani.app.videoplayer.ui.state.PlaybackState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.datasources.api.topic.FileSize
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.seconds

@Composable // see preview
fun EpisodeVideoLoadingIndicator(
    playerState: PlayerState,
    videoLoadingState: VideoLoadingState,
    optimizeForFullscreen: Boolean,
    modifier: Modifier = Modifier,
) {
    val isBuffering by playerState.isBuffering.collectAsStateWithLifecycle(true)
    val state by playerState.state.collectAsStateWithLifecycle()

    val speed by remember(playerState) {
        playerState.videoData.filterNotNull().flatMapLatest { it.downloadSpeed }
    }.collectAsStateWithLifecycle(FileSize.Unspecified)

    if (isBuffering ||
        state == PlaybackState.PAUSED_BUFFERING || // 如果不加这个, 就会有一段时间资源名字还没显示出来, 也没显示缓冲中
        state == PlaybackState.ERROR ||
        videoLoadingState !is VideoLoadingState.Succeed
    ) {
        EpisodeVideoLoadingIndicator(
            videoLoadingState,
            speedProvider = { speed },
            optimizeForFullscreen = optimizeForFullscreen,
            modifier = modifier,
        )
    }
}

@Composable
fun EpisodeVideoLoadingIndicator(
    state: VideoLoadingState,
    speedProvider: () -> FileSize,
    optimizeForFullscreen: Boolean,
    playerError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    VideoLoadingIndicator(
        showProgress = state is VideoLoadingState.Progressing,
        text = {
            if (playerError) {
                TextWithBorder("播放失败, 请尝试重新进入页面", color = MaterialTheme.colorScheme.error)
                return@VideoLoadingIndicator
            }
            when (state) {
                VideoLoadingState.Initial -> {
                    if (optimizeForFullscreen) {
                        TextWithBorder("请在右上角选择数据源")
                    } else {
                        TextWithBorder("请选择数据源")
                    }
                }

                VideoLoadingState.ResolvingSource -> {
                    TextWithBorder("正在解析资源链接")
                }

                VideoLoadingState.DecodingData -> {
                    TextWithBorder("资源解析成功, 正在准备视频")
                }

                is VideoLoadingState.Succeed -> {
                    var tooLong by rememberSaveable {
                        mutableStateOf(false)
                    }
                    val speed by remember { derivedStateOf(speedProvider) }
                    if (speed == FileSize.Zero) {
                        LaunchedEffect(true) {
                            delay(10.seconds)
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
                                    if (state.isBt) {
                                        append("BT 初始缓冲耗时稍长, 请耐心等待半分钟")
                                    }
                                }
                            }
                        }
                    }

                    TextWithBorder(text, textAlign = TextAlign.Center)
                }

                is VideoLoadingState.Failed -> {
                    TextWithBorder(
                        "加载失败: ${renderCause(state)}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        modifier,
    )
}

fun renderCause(cause: VideoLoadingState.Failed): String = when (cause) {
    is VideoLoadingState.ResolutionTimedOut -> "解析超时"
    is VideoLoadingState.UnknownError -> "未知错误"
    is VideoLoadingState.UnsupportedMedia -> "不支持该文件类型"
    VideoLoadingState.NoMatchingFile -> "未找到可播放的文件"
    VideoLoadingState.Cancelled -> "已取消"
}
