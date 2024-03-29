package me.him188.ani.app.videoplayer.ui.guesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.ui.theme.slightlyWeaken
import me.him188.ani.app.videoplayer.ui.guesture.SwipeSeekerState.Companion.swipeToSeek
import me.him188.ani.datasources.bangumi.processing.fixToString
import kotlin.math.absoluteValue

@Stable
private fun renderTime(seconds: Int): String {
    return "${(seconds / 60).fixToString(2)}:${(seconds % 60).fixToString(2)}"
}


/**
 * 展示当前快进/快退秒数的指示器.
 *
 * `<< 00:00` / `>> 00:00`
 */
@Composable
fun SeekPositionIndicator(
    deltaDuration: Int, // seconds
) {
    val shape = MaterialTheme.shapes.small
    val colors = aniDarkColorTheme()
    var lastDelta by remember { mutableIntStateOf(deltaDuration) }

    Surface(
        Modifier.shadow(1.dp, shape),
        color = colors.surface.slightlyWeaken(),
        shape = shape,
        contentColor = colors.onSurface,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 记忆变为 0 之前的 delta, 这样在快进/快退结束后, 会显示上一次的 delta, 而不是显示 0
            val duration = if (deltaDuration == 0) {
                lastDelta
            } else {
                deltaDuration.also {
                    lastDelta = deltaDuration
                }
            }

            val text = renderTime(duration.absoluteValue)
            Icon(
                if (duration > 0) {
                    Icons.Rounded.FastForward
                } else {
                    Icons.Rounded.FastRewind
                },
                null,
            )
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}


@Composable
fun VideoGestureHost(
    seekerState: SwipeSeekerState,
    onClickScreen: () -> Unit,
    onDoubleClickScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints {
        Row(Modifier.align(Alignment.TopCenter).padding(top = 80.dp)) {
            AnimatedVisibility(
                visible = seekerState.isSeeking,
                enter = fadeIn(tween(durationMillis = 100)),
                exit = fadeOut(tween(durationMillis = 500, delayMillis = 250))
            ) {
                SeekPositionIndicator(seekerState.deltaSeconds)
            }
        }

        Row(
            modifier
                .combinedClickable(
                    remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClickScreen,
                    onDoubleClick = onDoubleClickScreen,
                )
                .swipeToSeek(seekerState, Orientation.Horizontal)
                .fillMaxSize()
        ) {
            Box(
                Modifier.weight(1f)
//                    .draggable(state, Orientation.Vertical)
            )

            Box(Modifier.weight(1f))

            Box(
                Modifier.weight(1f)
            )
        }
    }
}

