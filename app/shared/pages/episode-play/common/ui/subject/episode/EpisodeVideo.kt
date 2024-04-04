package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsViewModel
import me.him188.ani.app.ui.subject.episode.video.settings.VideoSettingsButton
import me.him188.ani.app.ui.subject.episode.video.topbar.EpisodeVideoTopBar
import me.him188.ani.app.videoplayer.PlayerState
import me.him188.ani.app.videoplayer.VideoPlayer
import me.him188.ani.app.videoplayer.togglePause
import me.him188.ani.app.videoplayer.ui.PlayerProgressController
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import me.him188.ani.app.videoplayer.ui.VideoScaffold
import me.him188.ani.app.videoplayer.ui.guesture.GestureLock
import me.him188.ani.app.videoplayer.ui.guesture.LockableVideoGestureHost
import me.him188.ani.app.videoplayer.ui.guesture.rememberSwipeSeekerState
import me.him188.ani.danmaku.ui.DanmakuConfig
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
    playerState: PlayerState,
    danmakuConfig: DanmakuConfig,
    danmakuHostState: DanmakuHostState,
    onClickFullScreen: () -> Unit,
    danmakuEnabled: Boolean,
    setDanmakuEnabled: (enabled: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = isInLandscapeMode(),
) {
    // Don't rememberSavable. 刻意让每次切换都是隐藏的
    var controllerVisible by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    VideoScaffold(
        modifier = modifier,
        controllersVisible = controllerVisible,
        gestureLocked = isLocked,
        topBar = {
            EpisodeVideoTopBar(
                title = if (isFullscreen) {
                    { title() }
                } else {
                    null
                },
            ) {
                VideoSettingsButton(onClick = { showSettings = true })
            }
        },
        video = {
            if (LocalIsPreviewing.current) {
                Text("预览模式")
            } else {
                VideoPlayer(playerState, Modifier.matchParentSize())
            }
        },
        danmakuHost = {
            AnimatedVisibility(
                danmakuEnabled,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                DanmakuHost(danmakuHostState, Modifier.matchParentSize(), danmakuConfig)
            }
        },
        gestureHost = {
            LockableVideoGestureHost(
                rememberSwipeSeekerState(constraints.maxWidth) {
                    playerState.seekTo(playerState.currentPosition.value + it.seconds)
                },
                controllerVisible = controllerVisible,
                locked = isLocked,
                setControllerVisible = { controllerVisible = it },
                onDoubleClickScreen = { playerState.togglePause() },
            )
        },
        floatingMessage = {
            Column {
                EpisodeVideoLoadingIndicator(playerState, videoSourceSelected, videoReady)
                if (AniBuildConfig.current().isDebug) {
                    playerState.videoSource.collectAsStateWithLifecycle().value?.let {
                        EpisodeVideoDebugInfo(
                            it,
                            Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        rhsBar = {
            if (isFullscreen) {
                GestureLock(isLocked = isLocked, onClick = { isLocked = !isLocked })
            }
        },
        bottomBar = {
            PlayerProgressController(
                controller = playerState,
                isFullscreen = isFullscreen,
                onClickFullscreen = onClickFullScreen,
                danmakuEnabled, setDanmakuEnabled
            )
        },
        rhsSideSheet = {
            if (showSettings) {
                EpisodeVideoSettingsSideSheet(
                    onDismissRequest = { showSettings = false }
                ) {
                    EpisodeVideoSettings(
                        rememberViewModel { EpisodeVideoSettingsViewModel() },
                        Modifier.padding(8.dp)
                    )
                }
            }
        },
        isFullscreen = isFullscreen
    )
}

@Composable
private fun EpisodeVideoLoadingIndicator(
    playerState: PlayerState,
    videoSourceSelected: Boolean,
    videoReady: Boolean,
    modifier: Modifier = Modifier,
) {
    val isBuffering by playerState.isBuffering.collectAsStateWithLifecycle(true)
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
