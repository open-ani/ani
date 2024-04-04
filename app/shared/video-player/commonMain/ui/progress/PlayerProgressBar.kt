package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Stable
object PlayerControllerDefaults {
    @Composable
    fun PlaybackIcon(
        isPlaying: () -> Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier,
        ) {
            if (isPlaying()) {
                Icon(Icons.Rounded.Pause, contentDescription = "Pause", Modifier.size(36.dp))
            } else {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", Modifier.size(36.dp))
            }
        }
    }

    @Composable
    fun DanmakuIcon(
        danmakuEnabled: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier,
        ) {
            if (danmakuEnabled) {
                Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = "Disable Danmaku")
            } else {
                Icon(Icons.Rounded.SpeakerNotesOff, contentDescription = "Enable Danmaku")
            }
        }
    }

    @Composable
    fun FullscreenIcon(
        isFullscreen: Boolean,
        onClickFullscreen: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClickFullscreen,
            modifier
        ) {
            if (isFullscreen) {
                Icon(Icons.Rounded.FullscreenExit, contentDescription = "Exit Fullscreen", Modifier.size(32.dp))
            } else {
                Icon(Icons.Rounded.Fullscreen, contentDescription = "Enter Fullscreen", Modifier.size(32.dp))
            }
        }
    }
}

/**
 * @see PlayerControllerDefaults
 * @param progressSlider see [ProgressSlider]
 */
@Composable
fun PlayerControllerBar(
    startActions: @Composable RowScope.() -> Unit,
    progressSlider: @Composable RowScope.() -> Unit,
    endActions: @Composable RowScope.() -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clickable(remember { MutableInteractionSource() }, null, onClick = {}) // Consume touch event
            .padding(
                horizontal = if (expanded) 8.dp else 4.dp,
                vertical = if (expanded) 4.dp else 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 播放 / 暂停按钮
        Row(verticalAlignment = Alignment.CenterVertically) {
            startActions()
        }

        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                progressSlider()
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            endActions()
        }
    }
}
