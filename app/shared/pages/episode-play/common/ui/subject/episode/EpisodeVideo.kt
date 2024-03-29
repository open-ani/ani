package me.him188.ani.app.ui.subject.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.him188.ani.app.platform.AniBuildConfig
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.videoplayer.PlayerController
import me.him188.ani.app.videoplayer.VideoPlayerView
import me.him188.ani.app.videoplayer.ui.PlayerNavigationBar
import me.him188.ani.app.videoplayer.ui.PlayerProgressController
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import me.him188.ani.app.videoplayer.ui.VideoScaffold
import me.him188.ani.danmaku.ui.DanmakuHost
import me.him188.ani.danmaku.ui.DanmakuHostState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.seconds


/**
 * 剧集详情页面顶部的视频控件.
 */
@Composable
internal fun EpisodeVideo(
    videoSourceSelected: Boolean,
    videoReady: Boolean,
    videoTitle: String,
    playerController: PlayerController,
    danmakuHostState: DanmakuHostState,
    onClickFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = isInLandscapeMode(),
) {
    val controllerVisible by playerController.controllerVisible.collectAsStateWithLifecycle()

    VideoScaffold(
        controllersVisible = controllerVisible,
        onControllersVisibleChange = { playerController.setControllerVisible(it) },
        topBar = {
            PlayerNavigationBar(
                title = if (isFullscreen) {
                    { Text(videoTitle) }
                } else {
                    null
                },
            )
        },
        video = {
            VideoPlayerView(playerController, Modifier.matchParentSize())
        },
        bottomBar = {
            PlayerProgressController(
                controller = playerController,
                isFullscreen = isFullscreen,
                onClickFullscreen = onClickFullScreen,
            )
        },
        floatingMessage = {
            Column {
                EpisodeVideoLoadingIndicator(playerController, videoSourceSelected, videoReady)
                if (AniBuildConfig.current().isDebug) {
                    playerController.videoSource.collectAsStateWithLifecycle().value?.let {
                        EpisodeVideoDebugInfo(
                            it,
                            Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        danmakuHost = {
            DanmakuHost(danmakuHostState, Modifier.matchParentSize())
        },
        modifier = modifier,
        isFullscreen = isFullscreen
    )
}

@Composable
private fun EpisodeVideoLoadingIndicator(
    playerController: PlayerController,
    videoSourceSelected: Boolean,
    videoReady: Boolean,
    modifier: Modifier = Modifier,
) {
    val isBuffering by playerController.isBuffering.collectAsStateWithLifecycle(true)
    if (isBuffering) {
        var loadedTooLong by rememberSaveable { mutableStateOf(false) }
        VideoLoadingIndicator(
            showProgress = videoSourceSelected,
            text = {
                when {
                    !videoSourceSelected -> {
                        Text("请选择数据源")
                    }

                    !videoReady -> {
                        Text("正在下载种子")
                    }

                    loadedTooLong -> {
                        Text("资源较慢, 正在努力缓冲")
                    }

                    else -> {
                        //                                val speed by video.downloadRate.collectAsStateWithLifecycle(null)
                        Text("正在缓冲")
                        LaunchedEffect(true) {
                            delay(10.seconds)
                            loadedTooLong = true
                        }
                    }
                }
            },
            modifier,
        )
    }
}
