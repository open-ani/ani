package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SpeakerNotesOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.videoplayer.PlayerController
import me.him188.ani.datasources.bangumi.processing.fixToString
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PlayerProgressController(
    controller: PlayerController,
    isFullscreen: Boolean,
    onClickFullscreen: () -> Unit,
    danmakuEnabled: Boolean,
    setDanmakuEnabled: (enabled: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clickable(remember { MutableInteractionSource() }, null, onClick = {}) // Consume touch event
            .padding(
                horizontal = if (isFullscreen) 8.dp else 4.dp,
                vertical = if (isFullscreen) 4.dp else 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 播放 / 暂停按钮
        val state by controller.state.collectAsStateWithLifecycle(null)
        Row {
            // Play / Pause
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
                    Icon(Icons.Rounded.Pause, contentDescription = "Pause", Modifier.size(36.dp))
                } else {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", Modifier.size(36.dp))
                }
            }

            // Enable / Disable Danmaku
            val danmakuEnabledState by rememberUpdatedState(setDanmakuEnabled)
            IconButton(
                onClick = { danmakuEnabledState(!danmakuEnabled) },
            ) {
                if (danmakuEnabled) {
                    Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = "Disable Danmaku")
                } else {
                    Icon(Icons.Rounded.SpeakerNotesOff, contentDescription = "Enable Danmaku")
                }
            }
        }
        val videoProperties by controller.videoProperties.collectAsStateWithLifecycle(null)
        val playedDuration by controller.playedDuration.collectAsStateWithLifecycle()
        val sliderPosition by controller.previewingOrPlayingProgress.collectAsStateWithLifecycle(0f)

        val playedDurationSeconds = remember(playedDuration) { playedDuration.inWholeSeconds }
        val totalDurationSeconds = remember(videoProperties) { videoProperties?.duration?.inWholeSeconds ?: 0L }
        val totalDurationMillis = remember(videoProperties) { videoProperties?.duration?.inWholeMilliseconds ?: 0L }

        Text(
            text = renderSeconds(playedDurationSeconds, totalDurationSeconds),
            Modifier,
            style = MaterialTheme.typography.labelSmall,
        )

        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
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
                            inactiveTrackColor = Color.DarkGray,
                        )
                    )
                },
                modifier = Modifier.alpha(0.8f).matchParentSize(),
            )
        }

        Box(Modifier) {
            IconButton(
                onClick = onClickFullscreen,
            ) {
                if (isFullscreen) {
                    Icon(Icons.Rounded.FullscreenExit, contentDescription = "Exit Fullscreen", Modifier.size(32.dp))
                } else {
                    Icon(Icons.Rounded.Fullscreen, contentDescription = "Enter Fullscreen", Modifier.size(32.dp))
                }
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
