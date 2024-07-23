package me.him188.ani.app.videoplayer.ui

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PHONE_LANDSCAPE
import me.him188.ani.app.ui.subject.episode.EpisodeVideoImpl
import me.him188.ani.app.ui.subject.episode.details.EpisodePlayMediaSelector
import me.him188.ani.app.ui.subject.episode.details.rememberTestMediaSelectorPresentation
import me.him188.ani.app.ui.subject.episode.mediaFetch.emptyMediaSourceResultsPresentation
import me.him188.ani.app.ui.subject.episode.statistics.VideoLoadingState
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet
import me.him188.ani.app.ui.subject.episode.video.topbar.EpisodePlayerTitle
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults
import me.him188.ani.app.videoplayer.ui.state.DummyPlayerState
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuHostState

@Preview("Landscape Fullscreen - Light", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_NO)
@Preview("Landscape Fullscreen - Dark", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewVideoScaffoldFullscreen() {
    PreviewVideoScaffoldImpl(expanded = true)
}

@Preview("Portrait - Light", heightDp = 300, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_NO)
@Preview("Portrait - Dark", heightDp = 300, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewVideoScaffold() {
    PreviewVideoScaffoldImpl(expanded = false)
}

@Preview("Landscape Fullscreen - Light", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_NO)
@Preview("Landscape Fullscreen - Dark", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewDetachedSliderFullscreen() {
    PreviewVideoScaffoldImpl(expanded = true, controllerVisibility = ControllerVisibility.DetachedSliderOnly)
}

@Preview("Portrait - Light", heightDp = 300, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_NO)
@Preview("Portrait - Dark", heightDp = 300, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewDetachedSlider() {
    PreviewVideoScaffoldImpl(expanded = false, controllerVisibility = ControllerVisibility.DetachedSliderOnly)
}

@Composable
private fun PreviewVideoScaffoldImpl(
    expanded: Boolean,
    controllerVisibility: ControllerVisibility = ControllerVisibility.Visible
) = ProvideCompositionLocalsForPreview {
    val playerState = remember {
        DummyPlayerState()
    }

    val controllerState = rememberVideoControllerState(initialVisibility = controllerVisibility)
    var isMediaSelectorVisible by remember { mutableStateOf(false) }
    var isEpisodeSelectorVisible by remember { mutableStateOf(false) }

    EpisodeVideoImpl(
        playerState = playerState,
        expanded = expanded,
        hasNextEpisode = true,
        onClickNextEpisode = {},
        videoControllerState = controllerState,
        title = {
            EpisodePlayerTitle(
                "28",
                "因为下次再见的时候就会很难为情",
                "葬送的芙莉莲",
            )
        },
        danmakuHostState = remember {
            DanmakuHostState()
        },
        videoLoadingState = { VideoLoadingState.Succeed(isBt = true) },
        danmakuConfig = { DanmakuConfig.Default },
        onClickFullScreen = { },
        onExitFullscreen = { },
        danmakuEditor = {
            val (value, onValueChange) = remember { mutableStateOf("") }
            PlayerControllerDefaults.DanmakuTextField(
                value = value,
                onValueChange = onValueChange,
                Modifier.weight(1f),
            )
        },
        configProvider = { VideoScaffoldConfig.Default },
        sideSheets = {
            if (isMediaSelectorVisible) {
                EpisodeVideoSettingsSideSheet(
                    onDismissRequest = { isMediaSelectorVisible = false },
                ) {
                    EpisodePlayMediaSelector(
                        rememberTestMediaSelectorPresentation(),
                        emptyMediaSourceResultsPresentation(),
                        onDismissRequest = { },
                        modifier = Modifier.fillMaxHeight(), // 防止添加筛选后数量变少导致 bottom sheet 高度变化
                    )
                }
            }
        },
        onShowMediaSelector = { isMediaSelectorVisible = true },
        onShowSelectEpisode = { isEpisodeSelectorVisible = true },
    )

//    VideoScaffold(
//        expanded = true,
//        modifier = Modifier,
//        controllersVisible = { controllerVisible },
//        gestureLocked = { isLocked },
//        topBar = {
//            EpisodeVideoTopBar(
//                title = {
//                    EpisodePlayerTitle(
//                        ep = "28",
//                        episodeTitle = "因为下次再见的时候会很难为情",
//                        subjectTitle = "葬送的芙莉莲"
//                    )
//                },
//
//                settings = {
//                    var config by remember {
//                        mutableStateOf(DanmakuConfig.Default)
//                    }
//                    var showSettings by remember { mutableStateOf(false) }
//                    if (showSettings) {
//                        EpisodeVideoSettingsSideSheet(
//                            onDismissRequest = { showSettings = false },
//                        ) {
//                            EpisodeVideoSettings(
//                                config,
//                                { config = it },
//                            )
//                        }
//                    }
//
//                }
//            )
//        },
//        video = {
////            AniKamelImage(resource = asyncPainterResource(data = "https://picsum.photos/536/354"))
//        },
//        danmakuHost = {
//        },
//        gestureHost = {
//            val swipeSeekerState = rememberSwipeSeekerState(constraints.maxWidth) {
//                playerState.seekTo(playerState.currentPositionMillis.value + it * 1000)
//            }
//            val indicatorState = rememberGestureIndicatorState()
//            val tasker = rememberUiMonoTasker()
//            val controllerState = rememberVideoControllerState()
//            LockableVideoGestureHost(
//                controllerState,
//                swipeSeekerState,
//                indicatorState,
//                fastSkipState = rememberPlayerFastSkipState(playerState, indicatorState),
//                locked = isLocked,
//                Modifier.padding(top = 100.dp),
//                onTogglePauseResume = {
//                    if (playerState.state.value.isPlaying) {
//                        tasker.launch {
//                            indicatorState.showPausedLong()
//                        }
//                    } else {
//                        tasker.launch {
//                            indicatorState.showResumedLong()
//                        }
//                    }
//                    playerState.togglePause()
//                },
//            )
//        },
//        floatingMessage = {
//            Column {
//                EpisodeVideoLoadingIndicator(VideoLoadingState.Succeed, speedProvider = { 233.kiloBytes })
//            }
//
//        },
//        rhsBar = {
//            GestureLock(isLocked = isLocked, onClick = { isLocked = !isLocked })
//        },
//        bottomBar = {
//            val progressSliderState =
//                rememberProgressSliderState(playerState = playerState, onPreview = {}, onPreviewFinished = {})
//            PlayerControllerBar(
//                startActions = {
//                    val playing = playerState.state.collectAsStateWithLifecycle()
//                    PlayerControllerDefaults.PlaybackIcon(
//                        isPlaying = { playing.value.isPlaying },
//                        onClick = { }
//                    )
//
//                    PlayerControllerDefaults.DanmakuIcon(
//                        true,
//                        onClick = { }
//                    )
//
//                },
//                progressIndicator = {
//                    ProgressIndicator(progressSliderState)
//                },
//                progressSlider = {
//                    ProgressSlider(progressSliderState)
//                },
//                danmakuEditor = {
//                    MaterialTheme(aniDarkColorTheme()) {
//                        var text by rememberSaveable { mutableStateOf("") }
//                        var sending by remember { mutableStateOf(false) }
//                        LaunchedEffect(key1 = sending) {
//                            if (sending) {
//                                delay(3.seconds)
//                                sending = false
//                            }
//                        }
//                        PlayerControllerDefaults.DanmakuTextField(
//                            text,
//                            onValueChange = { text = it },
//                            isSending = sending,
//                            onSend = {
//                                sending = true
//                                text = ""
//                            },
//                            modifier = Modifier.weight(1f)
//                        )
//                    }
//                },
//                endActions = {
//                    PlayerControllerDefaults.SubtitleSwitcher(playerState.subtitleTracks)
//                    val speed by playerState.playbackSpeed.collectAsStateWithLifecycle()
//                    SpeedSwitcher(
//                        speed,
//                        { playerState.setPlaybackSpeed(it) },
//                    )
//                    PlayerControllerDefaults.FullscreenIcon(
//                        isFullscreen,
//                        onClickFullscreen = {},
//                    )
//                },
//                expanded = isFullscreen,
//                Modifier.fillMaxWidth(),
//            )
//        },
//    )
}
