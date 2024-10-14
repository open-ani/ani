/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.preference.FullscreenSwitchMode
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.domain.danmaku.protocol.DanmakuInfo
import me.him188.ani.app.domain.danmaku.protocol.DanmakuLocation
import me.him188.ani.app.tools.rememberUiMonoTasker
import me.him188.ani.app.ui.foundation.LocalIsPreviewing
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.TextWithBorder
import me.him188.ani.app.ui.foundation.effects.cursorVisibility
import me.him188.ani.app.ui.foundation.rememberDebugSettingsViewModel
import me.him188.ani.app.ui.settings.danmaku.DanmakuRegexFilterState
import me.him188.ani.app.ui.subject.episode.danmaku.DanmakuEditor
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceInfoProvider
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.statistics.VideoLoadingState
import me.him188.ani.app.ui.subject.episode.video.VideoDanmakuState
import me.him188.ani.app.ui.subject.episode.video.loading.EpisodeVideoLoadingIndicator
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsViewModel
import me.him188.ani.app.ui.subject.episode.video.sidesheet.EditDanmakuRegexFilterSideSheet
import me.him188.ani.app.ui.subject.episode.video.sidesheet.EpisodeSelectorSideSheet
import me.him188.ani.app.ui.subject.episode.video.sidesheet.EpisodeSelectorState
import me.him188.ani.app.ui.subject.episode.video.sidesheet.EpisodeVideoMediaSelectorSideSheet
import me.him188.ani.app.videoplayer.ui.VideoControllerState
import me.him188.ani.app.videoplayer.ui.VideoPlayer
import me.him188.ani.app.videoplayer.ui.VideoScaffold
import me.him188.ani.app.videoplayer.ui.guesture.GestureFamily
import me.him188.ani.app.videoplayer.ui.guesture.GestureLock
import me.him188.ani.app.videoplayer.ui.guesture.LevelController
import me.him188.ani.app.videoplayer.ui.guesture.LockableVideoGestureHost
import me.him188.ani.app.videoplayer.ui.guesture.ScreenshotButton
import me.him188.ani.app.videoplayer.ui.guesture.mouseFamily
import me.him188.ani.app.videoplayer.ui.guesture.rememberGestureIndicatorState
import me.him188.ani.app.videoplayer.ui.guesture.rememberPlayerFastSkipState
import me.him188.ani.app.videoplayer.ui.guesture.rememberSwipeSeekerState
import me.him188.ani.app.videoplayer.ui.progress.AudioSwitcher
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressIndicatorText
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressSliderState
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerBar
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults.SpeedSwitcher
import me.him188.ani.app.videoplayer.ui.progress.SubtitleSwitcher
import me.him188.ani.app.videoplayer.ui.rememberAlwaysOnRequester
import me.him188.ani.app.videoplayer.ui.state.PlayerState
import me.him188.ani.app.videoplayer.ui.state.SupportsAudio
import me.him188.ani.app.videoplayer.ui.state.togglePause
import me.him188.ani.app.videoplayer.ui.top.PlayerTopBar
import me.him188.ani.danmaku.ui.DanmakuHost
import me.him188.ani.danmaku.ui.DanmakuHostState
import me.him188.ani.utils.platform.annotations.TestOnly
import me.him188.ani.utils.platform.isDesktop
import me.him188.ani.utils.platform.isMobile
import kotlin.time.Duration.Companion.seconds

internal const val TAG_EPISODE_VIDEO_TOP_BAR = "EpisodeVideoTopBar"

internal const val TAG_DANMAKU_SETTINGS_SHEET = "DanmakuSettingsSheet"
internal const val TAG_SHOW_MEDIA_SELECTOR = "ShowMediaSelector"
internal const val TAG_SHOW_SETTINGS = "ShowSettings"

internal const val TAG_MEDIA_SELECTOR_SHEET = "MediaSelectorSheet"
internal const val TAG_EPISODE_SELECTOR_SHEET = "EpisodeSelectorSheet"

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
    danmakuEnabled: Boolean,
    onToggleDanmaku: () -> Unit,
    videoLoadingState: () -> VideoLoadingState,
    onClickFullScreen: () -> Unit,
    onExitFullscreen: () -> Unit,
    danmakuEditor: @Composable() (RowScope.() -> Unit),
    configProvider: () -> VideoScaffoldConfig,
    onClickScreenshot: () -> Unit,
    detachedProgressSlider: @Composable () -> Unit,
    progressSliderState: MediaProgressSliderState,
    mediaSelectorPresentation: MediaSelectorPresentation,
    mediaSourceResultsPresentation: MediaSourceResultsPresentation,
    episodeSelectorState: EpisodeSelectorState,
    mediaSourceInfoProvider: MediaSourceInfoProvider,
    audioController: LevelController,
    brightnessController: LevelController,
    leftBottomTips: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    maintainAspectRatio: Boolean = !expanded,
    danmakuRegexFilterState: DanmakuRegexFilterState,
    gestureFamily: GestureFamily = LocalPlatform.current.mouseFamily,
    contentWindowInsets: WindowInsets = WindowInsets(0.dp),
) {
    // Don't rememberSavable. 刻意让每次切换都是隐藏的
    var isLocked by remember { mutableStateOf(false) }
    var sideSheetState by remember { mutableStateOf(SideSheetState.NONE) }
    var isMediaSelectorVisible by remember { mutableStateOf(false) }
    var isEpisodeSelectorVisible by remember { mutableStateOf(false) }
    val config by remember(configProvider) { derivedStateOf(configProvider) }

    // auto hide cursor
    val videoInteractionSource = remember { MutableInteractionSource() }
    val isVideoHovered by videoInteractionSource.collectIsHoveredAsState()
    val showCursor by remember(videoControllerState) {
        derivedStateOf {
            !isVideoHovered || (videoControllerState.visibility.bottomBar
                    || videoControllerState.visibility.detachedSlider
                    || sideSheetState != SideSheetState.NONE)
        }
    }


    VideoScaffold(
        expanded = expanded,
        modifier = modifier
            .hoverable(videoInteractionSource)
            .cursorVisibility(showCursor),
        contentWindowInsets = contentWindowInsets,
        maintainAspectRatio = maintainAspectRatio,
        controllerState = videoControllerState,
        gestureLocked = { isLocked },
        topBar = {
            PlayerTopBar(
                Modifier.testTag(TAG_EPISODE_VIDEO_TOP_BAR),
                title = if (expanded) {
                    { title() }
                } else {
                    null
                },
                actions = {
                    if (expanded) {
                        IconButton({ isMediaSelectorVisible = true }, Modifier.testTag(TAG_SHOW_MEDIA_SELECTOR)) {
                            Icon(Icons.Rounded.DisplaySettings, contentDescription = "数据源")
                        }
                    }
                    IconButton({ sideSheetState = SideSheetState.SETTINGS }, Modifier.testTag(TAG_SHOW_SETTINGS)) {
                        Icon(Icons.Rounded.Settings, contentDescription = "设置")
                    }
                },
                windowInsets = contentWindowInsets,
            )
        },
        video = {
            if (LocalIsPreviewing.current) {
                Text("预览模式")
            } else {
                // Save the status bar height to offset the video player
                var statusBarHeight by rememberSaveable { mutableStateOf(0) }
                if (LocalPlatform.current.isMobile() && !expanded) {
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
                danmakuEnabled,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
            ) {
                DanmakuHost(danmakuHostState, Modifier.matchParentSize())
            }
        },
        gestureHost = {
            val swipeSeekerState = rememberSwipeSeekerState(constraints.maxWidth) {
                playerState.seekTo(playerState.currentPositionMillis.value + it * 1000)
            }
            val videoPropertiesState by playerState.videoProperties.collectAsState()
            val enableSwipeToSeek by remember {
                derivedStateOf {
                    videoPropertiesState?.let { it.durationMillis != 0L } ?: false
                }
            }

            val indicatorTasker = rememberUiMonoTasker()
            val indicatorState = rememberGestureIndicatorState()
            LockableVideoGestureHost(
                videoControllerState,
                swipeSeekerState,
                progressSliderState,
                indicatorState,
                fastSkipState = rememberPlayerFastSkipState(playerState = playerState, indicatorState),
                playerState,
                locked = isLocked,
                enableSwipeToSeek = enableSwipeToSeek,
                audioController = audioController,
                brightnessController = brightnessController,
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
                family = gestureFamily,
            )
        },
        floatingMessage = {
            Column {
                EpisodeVideoLoadingIndicator(
                    playerState,
                    videoLoadingState(),
                    optimizeForFullscreen = expanded, // TODO: 这对 PC 其实可能不太好
                )
                val debugViewModel = rememberDebugSettingsViewModel()
                @OptIn(TestOnly::class)
                if (debugViewModel.isAppInDebugMode && debugViewModel.debugSettings.value.showControllerAlwaysOnRequesters) {
                    TextWithBorder(
                        "Always on requesters: \n" +
                                videoControllerState.getAlwaysOnRequesters().joinToString("\n"),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
        rhsButtons = {
            if (expanded && LocalPlatform.current.isDesktop()) {
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
                        danmakuEnabled,
                        onClick = { onToggleDanmaku() },
                    )
                    if (expanded && playerState is SupportsAudio) {
                        val volumeState by playerState.volume.collectAsStateWithLifecycle()
                        val volumeMute by playerState.isMute.collectAsStateWithLifecycle()
                        PlayerControllerDefaults.AudioIcon(
                            volumeState,
                            isMute = volumeMute,
                            maxValue = playerState.maxValue,
                            onClick = {
                                playerState.toggleMute()
                            },
                            onchange = {
                                playerState.setVolume(it)
                            },
                            controllerState = videoControllerState,
                        )
                    }
                },
                progressIndicator = {
                    MediaProgressIndicatorText(progressSliderState)
                },
                progressSlider = {
                    PlayerControllerDefaults.MediaProgressSlider(
                        progressSliderState,
                        cacheProgressState = playerState.cacheProgress,
                        showPreviewTimeTextOnThumb = expanded,
                    )
                },
                danmakuEditor = danmakuEditor,
                endActions = {
                    if (expanded) {
                        PlayerControllerDefaults.SelectEpisodeIcon(
                            onClick = { isEpisodeSelectorVisible = true },
                        )

                        if (LocalPlatform.current.isDesktop()) {
                            PlayerControllerDefaults.AudioSwitcher(playerState.audioTracks)
                        }

                        PlayerControllerDefaults.SubtitleSwitcher(playerState.subtitleTracks)

                        val speed by playerState.playbackSpeed.collectAsStateWithLifecycle()
                        val alwaysOnRequester = rememberAlwaysOnRequester(videoControllerState, "speedSwitcher")
                        SpeedSwitcher(
                            speed,
                            { playerState.setPlaybackSpeed(it) },
                            onExpandedChanged = {
                                if (it) {
                                    alwaysOnRequester.request()
                                } else {
                                    alwaysOnRequester.cancelRequest()
                                }
                            },
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
        detachedProgressSlider = detachedProgressSlider,
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
            val alwaysOnRequester = rememberAlwaysOnRequester(videoControllerState, "sideSheets")
            val anySideSheetVisible by remember {
                derivedStateOf {
                    sideSheetState != SideSheetState.NONE || isMediaSelectorVisible || isEpisodeSelectorVisible
                }
            }
            if (anySideSheetVisible) {
                DisposableEffect(true) {
                    alwaysOnRequester.request()
                    onDispose {
                        alwaysOnRequester.cancelRequest()
                    }
                }
            }

            when (sideSheetState) {
                SideSheetState.EDIT_DANMAKU_REGEX_FILTER -> {
                    EditDanmakuRegexFilterSideSheet(
                        state = danmakuRegexFilterState,
                        onDismissRequest = {
                            sideSheetState = SideSheetState.NONE
                        },
                        expanded = expanded,
                    )
                }

                SideSheetState.SETTINGS -> {
                    EpisodeVideoSettingsSideSheet(
                        onDismissRequest = { sideSheetState = SideSheetState.NONE },
                        Modifier.testTag(TAG_DANMAKU_SETTINGS_SHEET),
                        title = { Text(text = "弹幕设置") },
                        closeButton = {
                            IconButton(onClick = { sideSheetState = SideSheetState.NONE }) {
                                Icon(Icons.Rounded.Close, contentDescription = "关闭")
                            }
                        },
                    ) {
                        EpisodeVideoSettings(
                            remember { EpisodeVideoSettingsViewModel() },
                            onManageRegexFilters = {
                                sideSheetState = SideSheetState.EDIT_DANMAKU_REGEX_FILTER
                            },
                        )
                    }
                }

                SideSheetState.NONE -> Unit // No sheet is displayed
            }

            if (isMediaSelectorVisible) {
                EpisodeVideoMediaSelectorSideSheet(
                    mediaSelectorPresentation,
                    mediaSourceResultsPresentation,
                    mediaSourceInfoProvider,
                    onDismissRequest = { isMediaSelectorVisible = false },
                )
            }
            if (isEpisodeSelectorVisible) {
                EpisodeSelectorSideSheet(
                    episodeSelectorState,
                    onDismissRequest = { isEpisodeSelectorVisible = false },
                )
            }
        },
        leftBottomTips = leftBottomTips,
    )
}


private enum class SideSheetState {
    NONE,
    SETTINGS,
    EDIT_DANMAKU_REGEX_FILTER
}

@Stable
object EpisodeVideoDefaults

@Composable
fun EpisodeVideoDefaults.DanmakuEditor(
    videoDanmakuState: VideoDanmakuState,
    danmakuTextPlaceholder: String,
    playerState: PlayerState,
    videoScaffoldConfig: VideoScaffoldConfig,
    videoControllerState: VideoControllerState,
    modifier: Modifier = Modifier,
) {
    val danmakuEditorRequester = rememberAlwaysOnRequester(videoControllerState, "danmakuEditor")

    val focusManager = LocalFocusManager.current
    /**
     * 是否设置了暂停
     */
    var didSetPaused by rememberSaveable { mutableStateOf(false) }
    Row(modifier = modifier) {
        DanmakuEditor(
            text = videoDanmakuState.danmakuEditorText,
            onTextChange = { videoDanmakuState.danmakuEditorText = it },
            isSending = videoDanmakuState.isSending,
            placeholderText = danmakuTextPlaceholder,
            onSend = { text ->
                videoDanmakuState.danmakuEditorText = ""
                videoDanmakuState.sendAsync(
                    DanmakuInfo(
                        playerState.getExactCurrentPositionMillis(),
                        text = text,
                        color = Color.White.toArgb(),
                        location = DanmakuLocation.NORMAL,
                    ),
                ) {
                    focusManager.clearFocus()
                }
            },
            modifier = Modifier.onFocusChanged {
                if (it.isFocused) {
                    if (videoScaffoldConfig.pauseVideoOnEditDanmaku && playerState.state.value.isPlaying) {
                        didSetPaused = true
                        playerState.pause()
                    }
                    danmakuEditorRequester.request()
                } else {
                    if (didSetPaused) {
                        didSetPaused = false
                        playerState.resume()
                    }
                    danmakuEditorRequester.cancelRequest()
                }
            }.weight(1f),
        )
    }
}

