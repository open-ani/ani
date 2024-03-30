package me.him188.ani.app.ui.subject.episode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.him188.ani.app.platform.AniBuildConfig
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.videoplayer.PlayerController
import me.him188.ani.app.videoplayer.VideoPlayerView
import me.him188.ani.app.videoplayer.togglePause
import me.him188.ani.app.videoplayer.ui.PlayerNavigationBar
import me.him188.ani.app.videoplayer.ui.PlayerProgressController
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import me.him188.ani.app.videoplayer.ui.VideoScaffold
import me.him188.ani.app.videoplayer.ui.guesture.VideoGestureHost
import me.him188.ani.app.videoplayer.ui.guesture.rememberSwipeSeekerState
import me.him188.ani.danmaku.ui.DanmakuHost
import me.him188.ani.danmaku.ui.DanmakuHostState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.seconds


/**
 * 剧集详情页面顶部的视频控件.
 * @param title 仅在全屏时显示的标题
 */
@Composable
internal fun EpisodeVideo(
    videoSourceSelected: Boolean,
    videoReady: Boolean,
    title: @Composable () -> Unit,
    playerController: PlayerController,
    danmakuHostState: DanmakuHostState,
    onClickFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = isInLandscapeMode(),
) {
    // Don't rememberSavable. 刻意让每次切换都是隐藏的
    var controllerVisible by remember { mutableStateOf(false) }

    VideoScaffold(
        controllersVisible = controllerVisible,
        topBar = {
            PlayerNavigationBar(
                title = if (isFullscreen) {
                    { title() }
                } else {
                    null
                },
            )
        },
        video = {
            VideoPlayerView(playerController, Modifier.matchParentSize())
        },
        danmakuHost = {
            DanmakuHost(danmakuHostState, Modifier.matchParentSize())
        },
        gestureHost = {
            VideoGestureHost(
                rememberSwipeSeekerState(constraints.maxWidth) {
                    playerController.seekTo(playerController.playedDuration.value + it.seconds)
                },
                onClickScreen = { controllerVisible = !controllerVisible },
                onDoubleClickScreen = { playerController.togglePause() },
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
        bottomBar = {
            PlayerProgressController(
                controller = playerController,
                isFullscreen = isFullscreen,
                onClickFullscreen = onClickFullScreen,
            )
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
