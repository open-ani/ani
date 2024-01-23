package me.him188.ani.app.videoplayer.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.AniTopAppBar
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.ui.theme.slightlyWeaken
import me.him188.ani.app.videoplayer.DummyPlayerController
import me.him188.ani.app.videoplayer.PlayerController
import me.him188.ani.datasources.bangumi.processing.fixToString
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.milliseconds


/**
 * 覆盖在视频播放器上层的控制器
 */
@Composable
fun PlayerControllerOverlay(
    topBar: @Composable RowScope.() -> Unit = {},
    floatingBox: @Composable BoxScope.() -> Unit = {},
    bottomBar: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxSize(), color = Color.Transparent) {
        Box {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth()) {
                    topBar()
                }

                Spacer(Modifier.weight(1f, fill = true))

                Row(Modifier.fillMaxWidth()) {
                    CompositionLocalProvider(LocalContentColor provides Color.White) {
                        bottomBar()
                    }
                }
            }

            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                    CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().onBackground.slightlyWeaken()) {
                        floatingBox()
                    }
                }
            }
        }
    }
}

@Composable
internal expect fun PreviewPlayerControllerOverlay()

@Preview
@Composable
internal fun PreviewPlayerControllerOverlayImpl() {
    ProvideCompositionLocalsForPreview {
        val controller = remember {
            DummyPlayerController()
        }
        Box(modifier = Modifier.background(Color.Black)) {
            PlayerControllerOverlay(
                topBar = {
                    PlayerControllerOverlayTopBar(
                        startActions = {
                            TopAppBarGoBackButton {}
                        },
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                },
                bottomBar = {
                    PlayerProgressController(
                        controller = controller,
                        onClickFullScreen = {},
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                },
            )
        }
    }
}

@Composable
fun PlayerProgressController(
    controller: PlayerController,
    onClickFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier, verticalAlignment = Alignment.CenterVertically
    ) {
        // 播放 / 暂停按钮
        val state by controller.state.collectAsStateWithLifecycle(null)
        Box(Modifier.padding(horizontal = 8.dp).size(32.dp)) {
            IconButton(
                onClick = {
                    if (state?.isPlaying == true) {
                        controller.pause()
                    } else {
                        controller.resume()
                    }
                },
            ) {
                if (state?.isPlaying == true) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        }

        val bufferProgress by controller.bufferProgress.collectAsStateWithLifecycle()
        val videoProperties by controller.videoProperties.collectAsStateWithLifecycle(null)
        val playedDuration by controller.playedDuration.collectAsStateWithLifecycle()
        val sliderPosition by controller.previewingOrPlayingProgress.collectAsStateWithLifecycle(0f)

        val playedDurationSeconds = remember(playedDuration) { playedDuration.inWholeSeconds }
        val totalDurationSeconds = remember(videoProperties) { videoProperties?.duration?.inWholeSeconds ?: 0L }
        val totalDurationMillis = remember(videoProperties) { videoProperties?.duration?.inWholeMilliseconds ?: 0L }

        Text(
            text = renderSeconds(playedDurationSeconds, totalDurationSeconds),
            Modifier.padding(end = 8.dp),
            style = MaterialTheme.typography.labelSmall,
        )

        Box(Modifier.weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
//            LinearProgressIndicator(
//                modifier = Modifier.padding(horizontal = 8.dp).matchParentSize(),
//                progress = bufferProgress,
//                color = aniDarkColorTheme().onSurface,
//                trackColor = aniDarkColorTheme().surface,
//                strokeCap = StrokeCap.Round,
//            )
//            LinearProgressIndicator(
//                modifier = Modifier.matchParentSize().alpha(0.8f),
//                progress = playProgress,
//                color = aniDarkColorTheme().primary,
//                trackColor = aniDarkColorTheme().surface,
//                strokeCap = StrokeCap.Round,
//            )
            Slider(
                value = sliderPosition,
                valueRange = 0f..1f,
                onValueChange = {
                    controller.setPreviewingProgress(it)
                    controller.seekTo((it * totalDurationMillis).toLong().milliseconds)
                },
                track = {
                    SliderDefaults.Track(
                        it,
                        colors = SliderDefaults.colors(
                            activeTrackColor = aniDarkColorTheme().secondary,
                            inactiveTrackColor = aniDarkColorTheme().surface,
                        )
                    )
                },
                modifier = Modifier.alpha(0.8f).matchParentSize(),
            )
        }

        Box(Modifier.padding(horizontal = 8.dp).size(32.dp)) {
            IconButton(
                onClick = onClickFullScreen,
            ) {
                Icon(Icons.Default.Fullscreen, contentDescription = null)
            }
        }
    }
}

@Stable
private fun renderSeconds(played: Long, length: Long?): String {
    if (length == null) {
        return "00:${played.fixToString(2)} / 00:00"
    }
    return if (played < 60 && length < 60) {
        "00:${played.fixToString(2)} / 00:${length.fixToString(2)}"
    } else if (played < 3600 && length < 3600) {
        val startM = (played / 60).fixToString(2)
        val startS = (played % 60).fixToString(2)
        val endM = (length / 60).fixToString(2)
        val endS = (length % 60).fixToString(2)
        """$startM:$startS / $endM:$endS"""
    } else {
        val startH = (played / 3600).fixToString(2)
        val startM = (played % 3600 / 60).fixToString(2)
        val startS = (played % 60).fixToString(2)
        val endH = (length / 3600).fixToString(2)
        val endM = (length % 3600 / 60).fixToString(2)
        val endS = (length % 60).fixToString(2)
        """$startH:$startM:$startS / $endH:$endM:$endS"""
    }
}


@Composable
fun PlayerControllerOverlayTopBar(
    startActions: @Composable (RowScope.() -> Unit),
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().onBackground) {
        AniTopAppBar(
            modifier
                .fillMaxWidth(),
            actions = startActions,
            containerColor = Color.Transparent,
            padding = PaddingValues(
                start = 4.dp,
                top = 2.dp,
                end = 4.dp,
                bottom = 2.dp
            )
        )
    }
}