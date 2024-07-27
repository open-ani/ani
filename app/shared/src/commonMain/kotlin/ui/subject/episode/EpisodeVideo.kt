package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DisplaySettings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.preference.FullscreenSwitchMode
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.platform.currentPlatform
import me.him188.ani.app.platform.isDesktop
import me.him188.ani.app.platform.isMobile
import me.him188.ani.app.tools.rememberUiMonoTasker
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.isInDebugMode
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.subject.episode.statistics.VideoLoadingState
import me.him188.ani.app.ui.subject.episode.video.loading.EpisodeVideoLoadingIndicator
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsViewModel
import me.him188.ani.app.ui.subject.episode.video.topbar.EpisodeVideoTopBar
import me.him188.ani.app.videoplayer.ui.VideoControllerState
import me.him188.ani.app.videoplayer.ui.VideoPlayer
import me.him188.ani.app.videoplayer.ui.VideoScaffold
import me.him188.ani.app.videoplayer.ui.guesture.GestureLock
import me.him188.ani.app.videoplayer.ui.guesture.LockableVideoGestureHost
import me.him188.ani.app.videoplayer.ui.guesture.ScreenshotButton
import me.him188.ani.app.videoplayer.ui.guesture.rememberGestureIndicatorState
import me.him188.ani.app.videoplayer.ui.guesture.rememberPlayerFastSkipState
import me.him188.ani.app.videoplayer.ui.guesture.rememberSwipeSeekerState
import me.him188.ani.app.videoplayer.ui.progress.AudioSwitcher
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressIndicatorText
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressSlider
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerBar
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults.SpeedSwitcher
import me.him188.ani.app.videoplayer.ui.progress.SubtitleSwitcher
import me.him188.ani.app.videoplayer.ui.progress.rememberMediaProgressSliderState
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.togglePause
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
internal fun EpisodeVideoImpl(
    playerState: PlayerState,
    expanded: Boolean,
    hasNextEpisode: Boolean,
    onClickNextEpisode: () -> Unit,
    videoControllerState: VideoControllerState,
    title: @Composable () -> Unit,
    danmakuHostState: DanmakuHostState,
    videoLoadingState: () -> VideoLoadingState,
    danmakuConfig: () -> DanmakuConfig,
    onClickFullScreen: () -> Unit,
    onExitFullscreen: () -> Unit,
    danmakuEditor: @Composable (RowScope.() -> Unit),
    configProvider: () -> VideoScaffoldConfig,
    sideSheets: @Composable () -> Unit,
    onShowMediaSelector: () -> Unit,
    onShowSelectEpisode: () -> Unit,
    onClickScreenshot: () -> Unit,
    modifier: Modifier = Modifier,
    maintainAspectRatio: Boolean = !expanded,
) {
    // Don't rememberSavable. 刻意让每次切换都是隐藏的
    var isLocked by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val config by remember(configProvider) { derivedStateOf(configProvider) }

    VideoScaffold(
        expanded = expanded,
        modifier = modifier,
        maintainAspectRatio = maintainAspectRatio,
        controllersVisible = { videoControllerState.isVisible },
        gestureLocked = { isLocked },
        topBar = {
            EpisodeVideoTopBar(
                title = if (expanded) {
                    { title() }
                } else {
                    null
                },
                actions = {
                    if (expanded) {
                        IconButton(onShowMediaSelector) {
                            Icon(Icons.Rounded.DisplaySettings, contentDescription = "数据源")
                        }
                    }
                    IconButton({ showSettings = true }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        video = {
            if (LocalIsPreviewing.current) {
                Text("预览模式")
            } else {
                // Save the status bar height to offset the video player
                var statusBarHeight by rememberSaveable { mutableStateOf(0) }
                if (currentPlatform.isMobile() && !expanded) {
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
                            x = if (expanded) with(LocalDensity.current) {
                                -statusBarHeight.toDp() / 2
                            } else 0.dp,
                            y = 0.dp,
                        )
                        .matchParentSize(),
                )
            }
        },
        danmakuHost = {
            AnimatedVisibility(
                videoControllerState.danmakuEnabled,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
            ) {
                DanmakuHost(danmakuHostState, Modifier.matchParentSize(), danmakuConfig)
            }
        },
        gestureHost = {
            val swipeSeekerState = rememberSwipeSeekerState(constraints.maxWidth) {
                playerState.seekTo(playerState.currentPositionMillis.value + it * 1000)
            }
            val indicatorTasker = rememberUiMonoTasker()
            val indicatorState = rememberGestureIndicatorState()
            LockableVideoGestureHost(
                videoControllerState,
                swipeSeekerState,
                indicatorState,
                fastSkipState = rememberPlayerFastSkipState(playerState = playerState, indicatorState),
                locked = isLocked,
                Modifier.padding(top = 100.dp),
                onTogglePauseResume = {
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
                onToggleFullscreen = {
                    onClickFullScreen()
                },
                onExitFullscreen = onExitFullscreen,
            )
        },
        floatingMessage = {
            Column {
                EpisodeVideoLoadingIndicator(
                    playerState,
                    videoLoadingState(),
                    optimizeForFullscreen = expanded, // TODO: 这对 PC 其实可能不太好
                )
            }
        },
        rhsButtons = {
            if (expanded && currentPlatform.isDesktop()) {
                ScreenshotButton(
                    onClick = onClickScreenshot,
                )
            }
        },
        gestureLock = {
            if (expanded) {
                GestureLock(isLocked = isLocked, onClick = { isLocked = !isLocked })
            }
        },
        bottomBar = {
            val progressSliderState = rememberMediaProgressSliderState(
                playerState,
                onPreview = {
                    // not yet supported
                },
                onPreviewFinished = {
                    playerState.seekTo(it)
                },
            )
            PlayerControllerBar(
                startActions = {
                    val isPlaying by remember(playerState) { playerState.state.map { it.isPlaying } }
                        .collectAsStateWithLifecycle(false)
                    PlayerControllerDefaults.PlaybackIcon(
                        isPlaying = { isPlaying },
                        onClick = { playerState.togglePause() },
                    )

                    if (hasNextEpisode && expanded) {
                        PlayerControllerDefaults.NextEpisodeIcon(
                            onClick = onClickNextEpisode,
                        )
                    }
                    PlayerControllerDefaults.DanmakuIcon(
                        videoControllerState.danmakuEnabled,
                        onClick = { videoControllerState.toggleDanmakuEnabled() },
                    )
                },
                progressIndicator = {
                    MediaProgressIndicatorText(progressSliderState)
                },
                progressSlider = {
                    MediaProgressSlider(
                        progressSliderState, playerState.cacheProgress,
                        downloadingColor = if (isInDebugMode()) Color.Yellow else aniDarkColorTheme().surface,
                    )
                },
                danmakuEditor = danmakuEditor,
                endActions = {
                    if (expanded) {
                        PlayerControllerDefaults.SelectEpisodeIcon(
                            onShowSelectEpisode,
                        )

                        if (currentPlatform.isDesktop()) {
                            PlayerControllerDefaults.AudioSwitcher(playerState.audioTracks)
                        }

                        PlayerControllerDefaults.SubtitleSwitcher(playerState.subtitleTracks)

                        val speed by playerState.playbackSpeed.collectAsStateWithLifecycle()
                        SpeedSwitcher(
                            speed,
                            { playerState.setPlaybackSpeed(it) },
                        )
                    }
                    PlayerControllerDefaults.FullscreenIcon(
                        expanded,
                        onClickFullscreen = onClickFullScreen,
                    )
                },
                expanded = expanded,
            )
        },
        floatingBottomEnd = {
            when (config.fullscreenSwitchMode) {
                FullscreenSwitchMode.ONLY_IN_CONTROLLER -> {}

                FullscreenSwitchMode.ALWAYS_SHOW_FLOATING -> {
                    PlayerControllerDefaults.FullscreenIcon(
                        expanded,
                        onClickFullscreen = onClickFullScreen,
                    )
                }

                FullscreenSwitchMode.AUTO_HIDE_FLOATING -> {
                    var visible by remember { mutableStateOf(true) }
                    LaunchedEffect(true) {
                        delay(5.seconds)
                        visible = false
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(snap()),
                        exit = fadeOut(),
                    ) {
                        PlayerControllerDefaults.FullscreenIcon(
                            expanded,
                            onClickFullscreen = onClickFullScreen,
                        )
                    }
                }
            }
        },
        rhsSheet = {
            if (showSettings) {
                EpisodeVideoSettingsSideSheet(
                    onDismissRequest = { showSettings = false },
                    title = { Text(text = "弹幕设置") },
                    closeButton = {
                        IconButton(onClick = { showSettings = false }) {
                            Icon(Icons.Rounded.Close, contentDescription = "关闭")
                        }
                    },
                ) {
                    EpisodeVideoSettings(
                        rememberViewModel { EpisodeVideoSettingsViewModel() },
                    )
                }
            }

            sideSheets()
        },
    )
}
