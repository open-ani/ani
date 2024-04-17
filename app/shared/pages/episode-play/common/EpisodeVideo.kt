package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map
import me.him188.ani.app.platform.AniBuildConfig
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.tools.rememberUiMonoTasker
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.subject.episode.video.loading.EpisodeVideoLoadingIndicator
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsViewModel
import me.him188.ani.app.ui.subject.episode.video.settings.VideoSettingsButton
import me.him188.ani.app.ui.subject.episode.video.topbar.EpisodeVideoTopBar
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.videoplayer.ui.VideoPlayer
import me.him188.ani.app.videoplayer.ui.VideoScaffold
import me.him188.ani.app.videoplayer.ui.guesture.GestureLock
import me.him188.ani.app.videoplayer.ui.guesture.LockableVideoGestureHost
import me.him188.ani.app.videoplayer.ui.guesture.rememberGestureIndicatorState
import me.him188.ani.app.videoplayer.ui.guesture.rememberSwipeSeekerState
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerBar
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults.SpeedSwitcher
import me.him188.ani.app.videoplayer.ui.progress.ProgressIndicator
import me.him188.ani.app.videoplayer.ui.progress.ProgressSlider
import me.him188.ani.app.videoplayer.ui.progress.rememberProgressSliderState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.togglePause
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuHost
import me.him188.ani.danmaku.ui.DanmakuHostState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


/**
 * 剧集详情页面顶部的视频控件.
 * @param title 仅在全屏时显示的标题
 */
@Composable
internal fun EpisodeVideo(
    videoSourceSelected: () -> Boolean,
    title: @Composable () -> Unit,
    playerState: PlayerState,
    danmakuConfig: () -> DanmakuConfig,
    danmakuHostState: DanmakuHostState,
    onClickFullScreen: () -> Unit,
    danmakuEnabled: () -> Boolean,
    setDanmakuEnabled: (enabled: Boolean) -> Unit,
    onSendDanmaku: (text: String) -> Unit,
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
                // Save the status bar height to offset the video player
                var statusBarHeight by rememberSaveable { mutableStateOf(0) }
                if (!isFullscreen) {
                    val insets = WindowInsets.systemBars
                    val density = LocalDensity.current
                    SideEffect {
                        statusBarHeight = insets.getTop(density)
                    }
                }

                VideoPlayer(
                    playerState,
                    Modifier
                        .offset(
                            x = if (isFullscreen) with(LocalDensity.current) {
                                -statusBarHeight.toDp() / 2
                            } else 0.dp,
                            y = 0.dp
                        )
                        .matchParentSize()
                )
            }
        },
        danmakuHost = {
            AnimatedVisibility(
                danmakuEnabled(),
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                DanmakuHost(danmakuHostState, Modifier.matchParentSize(), danmakuConfig())
            }
        },
        gestureHost = {
            val swipeSeekerState = rememberSwipeSeekerState(constraints.maxWidth) {
                playerState.seekTo(playerState.currentPositionMillis.value + it * 1000)
            }
            val indicatorTasker = rememberUiMonoTasker()
            val indicatorState = rememberGestureIndicatorState()
            LockableVideoGestureHost(
                swipeSeekerState,
                indicatorState,
                controllerVisible = controllerVisible,
                locked = isLocked,
                setControllerVisible = { controllerVisible = it },
                Modifier.padding(top = 100.dp),
                onDoubleClickScreen = {
                    if (playerState.state.value.isPlaying) {
                        indicatorTasker.launch {
                            indicatorState.showPausedLong()
                        }
                    } else {
                        indicatorTasker.launch {
                            indicatorState.showResumedLong()
                        }
                    }
                    playerState.togglePause()
                },
            )
        },
        floatingMessage = {
            Column {
                EpisodeVideoLoadingIndicator(playerState, videoSourceSelected())
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
            val progressSliderState = rememberProgressSliderState(
                playerState,
                onPreview = {
                    // not yet supported
                },
                onPreviewFinished = {
                    playerState.seekTo(it)
                }
            )
            PlayerControllerBar(
                startActions = {
                    val isPlaying by remember(playerState) { playerState.state.map { it.isPlaying } }
                        .collectAsStateWithLifecycle(false)
                    PlayerControllerDefaults.PlaybackIcon(
                        isPlaying = { isPlaying },
                        onClick = { playerState.togglePause() }
                    )

                    PlayerControllerDefaults.DanmakuIcon(
                        danmakuEnabled(),
                        onClick = { setDanmakuEnabled(!danmakuEnabled()) }
                    )
                },
                progressIndicator = {
                    ProgressIndicator(progressSliderState)
                },
                progressSlider = {
                    ProgressSlider(progressSliderState)
                },
                danmakuEditor = {
                    MaterialTheme(aniDarkColorTheme()) {
                        var text by rememberSaveable { mutableStateOf("") }
                        PlayerControllerDefaults.DanmakuTextField(
                            text,
                            onValueChange = { text = it },
                            onSend = {
                                onSendDanmaku(text)
                                text = ""
                            },
                            Modifier.weight(1f)
                        )
                    }
                },
                endActions = {
                    val speed by playerState.playbackSpeed.collectAsStateWithLifecycle()
                    SpeedSwitcher(
                        speed,
                        { playerState.setPlaybackSpeed(it) },
                    )
                    PlayerControllerDefaults.FullscreenIcon(
                        isFullscreen,
                        onClickFullscreen = onClickFullScreen,
                    )
                },
                expanded = isFullscreen,
            )
        },
        rhsSheet = {
            if (showSettings) {
                EpisodeVideoSettingsSideSheet(
                    onDismissRequest = { showSettings = false }
                ) {
                    EpisodeVideoSettings(
                        rememberViewModel { EpisodeVideoSettingsViewModel() },
                    )
                }
            }
        },
        isFullscreen = isFullscreen
    )
}
