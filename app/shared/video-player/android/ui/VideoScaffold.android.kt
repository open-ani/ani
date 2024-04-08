package me.him188.ani.app.videoplayer.ui

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.him188.ani.app.tools.rememberMonoTasker
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PHONE_LANDSCAPE
import me.him188.ani.app.ui.subject.episode.details.EpisodePlayerTitle
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettings
import me.him188.ani.app.ui.subject.episode.video.settings.EpisodeVideoSettingsSideSheet
import me.him188.ani.app.ui.subject.episode.video.topbar.EpisodeVideoTopBar
import me.him188.ani.app.ui.theme.aniDarkColorTheme
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
import me.him188.ani.app.videoplayer.ui.state.DummyPlayerState
import me.him188.ani.app.videoplayer.ui.state.togglePause
import me.him188.ani.danmaku.ui.DanmakuConfig
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Preview("Landscape Fullscreen - Light", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_NO)
@Preview("Landscape Fullscreen - Dark", device = PHONE_LANDSCAPE, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewVideoScaffoldFullscreen() {
    PreviewVideoScaffoldImpl(isFullscreen = true)
}

@Preview("Portrait - Light", heightDp = 300, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_NO)
@Preview("Portrait - Dark", heightDp = 300, device = Devices.PHONE, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewVideoScaffold() {
    PreviewVideoScaffoldImpl(isFullscreen = false)
}

@Composable
private fun PreviewVideoScaffoldImpl(
    isFullscreen: Boolean,
) = ProvideCompositionLocalsForPreview {
    val playerState = remember {
        DummyPlayerState()
    }

    var controllerVisible by remember {
        mutableStateOf(true)
    }
    var isLocked by remember { mutableStateOf(false) }

    VideoScaffold(
        modifier = Modifier,
        controllersVisible = controllerVisible,
        gestureLocked = isLocked,
        topBar = {
            EpisodeVideoTopBar(
                title = {
                    EpisodePlayerTitle(
                        ep = "28",
                        episodeTitle = "因为下次再见的时候会很难为情",
                        subjectTitle = "葬送的芙莉莲"
                    )
                },

                settings = {
                    var config by remember {
                        mutableStateOf(DanmakuConfig.Default)
                    }
                    var showSettings by remember { mutableStateOf(false) }
                    if (showSettings) {
                        EpisodeVideoSettingsSideSheet(
                            onDismissRequest = { showSettings = false },
                        ) {
                            EpisodeVideoSettings(
                                config,
                                { config = it },
                            )
                        }
                    }

                }
            )
        },
        video = {
//            AniKamelImage(resource = asyncPainterResource(data = "https://picsum.photos/536/354"))
        },
        danmakuHost = {
        },
        gestureHost = {
            val swipeSeekerState = rememberSwipeSeekerState(constraints.maxWidth) {
                playerState.seekTo(playerState.currentPositionMillis.value + it * 1000)
            }
            val indicatorState = rememberGestureIndicatorState()
            val tasker = rememberMonoTasker()
            LockableVideoGestureHost(
                swipeSeekerState,
                indicatorState,
                controllerVisible = controllerVisible,
                locked = isLocked,
                setControllerVisible = { controllerVisible = it },
                Modifier.padding(top = 100.dp),
                onDoubleClickScreen = {
                    if (playerState.state.value.isPlaying) {
                        tasker.launch {
                            indicatorState.showPausedLong()
                        }
                    } else {
                        tasker.launch {
                            indicatorState.showResumedLong()
                        }
                    }
                    playerState.togglePause()
                },
            )
        },
        floatingMessage = {
            Column {
                VideoLoadingIndicator(true, text = { Text(text = "正在缓冲") })
            }

        },
        rhsBar = {
            GestureLock(isLocked = isLocked, onClick = { isLocked = !isLocked })
        },
        bottomBar = {
            val progressSliderState =
                rememberProgressSliderState(playerState = playerState, onPreview = {}, onPreviewFinished = {})
            PlayerControllerBar(
                startActions = {
                    val playing = playerState.state.collectAsStateWithLifecycle()
                    PlayerControllerDefaults.PlaybackIcon(
                        isPlaying = { playing.value.isPlaying },
                        onClick = { }
                    )

                    PlayerControllerDefaults.DanmakuIcon(
                        true,
                        onClick = { }
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
                        onClickFullscreen = {},
                    )
                },
                expanded = isFullscreen,
                Modifier.fillMaxWidth(),
            )
        },
        isFullscreen = true,
    )
}
