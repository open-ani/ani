package me.him188.ani.app.videoplayer.ui.guesture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.foundation.theme.aniLightColorTheme
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.videoplayer.ui.ControllerVisibility
import me.him188.ani.app.videoplayer.ui.VideoControllerState
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressSliderState
import kotlin.time.Duration.Companion.seconds

@Composable
fun GestureLock(
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
//    val background = aniDarkColorTheme().onSurface
//    SmallFloatingActionButton(
//        onClick = onClick,
//        modifier = modifier,
//        containerColor = background,
//    ) {
//        CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().contentColorFor(background)) {
//            if (isLocked) {
//                Icon(Icons.Outlined.LockOpen, contentDescription = "Lock screen")
//            } else {
//                Icon(Icons.Outlined.Lock, contentDescription = "Unlock screen")
//            }
//        }
//    }
    Surface(
        modifier,
        shape = RoundedCornerShape(16.dp),
        color = aniDarkColorTheme().background.copy(0.05f),
        border = BorderStroke(0.5.dp, aniLightColorTheme().outline.slightlyWeaken()),
    ) {
        IconButton(onClick) {
            val color = if (isLocked) {
                aniDarkColorTheme().primary
            } else {
                Color.White
            }
            CompositionLocalProvider(LocalContentColor provides color) {
                if (isLocked) {
                    Icon(Icons.Outlined.Lock, contentDescription = "UnLock screen")
                } else {
                    Icon(Icons.Outlined.LockOpen, contentDescription = "Lock screen")
                }
            }
        }
    }
//    Surface(
//        modifier,
//        shape = MaterialTheme.shapes.small,
//        shadowElevation = 1.dp,
//    ) {
//        IconButton(
//            onClick = onClick,
//        ) {
//            if (isLocked) {
//                Icon(Icons.Rounded.Lock, contentDescription = "Lock screen")
//            } else {
//                Icon(Icons.Rounded.LockOpen, contentDescription = "Unlock screen")
//            }
//        }
//    }
}

/**
 * Handles click events and auto-hide controller.
 *
 * @see LockableVideoGestureHost
 */
@Composable
fun LockedScreenGestureHost(
    controllerVisibility: () -> ControllerVisibility,
    setFullVisible: (visible: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clickable(
                remember { MutableInteractionSource() },
                indication = null,
                onClick = { setFullVisible(true) },
            ).fillMaxSize(),
    )

    if (controllerVisibility() == ControllerVisibility.Visible) {
        LaunchedEffect(true) {
            delay(2.seconds)
            setFullVisible(false)
        }
    }
    return
}


@Composable
fun LockableVideoGestureHost(
    controllerState: VideoControllerState,
    seekerState: SwipeSeekerState,
    progressSliderState: MediaProgressSliderState,
    indicatorState: GestureIndicatorState,
    fastSkipState: FastSkipState,
    locked: Boolean,
    enableSwipeToSeek: Boolean,
    modifier: Modifier = Modifier,
    onTogglePauseResume: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onExitFullscreen: () -> Unit = {},
) {
    if (locked) {
        LockedScreenGestureHost({ controllerState.visibility }, controllerState.setFullVisible, modifier)
    } else {
        VideoGestureHost(
            controllerState,
            seekerState,
            progressSliderState,
            indicatorState,
            fastSkipState,
            enableSwipeToSeek = enableSwipeToSeek,
            onTogglePauseResume = onTogglePauseResume,
            onToggleFullscreen = onToggleFullscreen,
            onExitFullscreen = onExitFullscreen,
        )
    }
}
