package me.him188.ani.app.videoplayer.ui.guesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.videoplayer.ui.state.PlayerState

@Composable
fun rememberPlayerFastSkipState(
    playerState: PlayerState,
    gestureIndicatorState: GestureIndicatorState,
): FastSkipState {
    return remember(playerState) {
        PlayerFastSkipState(playerState, gestureIndicatorState).fastSkipState
    }
}

class PlayerFastSkipState(
    private val playerState: PlayerState,
    private val gestureIndicatorState: GestureIndicatorState,
) {
    private var originalSpeed = 0f
    private var gestureIndicatorTicket = 0
    val fastSkipState: FastSkipState = FastSkipState(
        onStart = { skipDirection ->
            originalSpeed = playerState.playbackSpeed.value
            playerState.setPlaybackSpeed(
                when (skipDirection) {
                    SkipDirection.FORWARD -> 3f
                    SkipDirection.BACKWARD -> error("Backward skipping is not supported")
                },
            )
            gestureIndicatorTicket = gestureIndicatorState.startFastForward()
        },
        onStop = {
            playerState.setPlaybackSpeed(originalSpeed)
            gestureIndicatorState.stopFastForward(gestureIndicatorTicket)
        },
    )
}

@Stable
class FastSkipState(
    private val onStart: (skipDirection: SkipDirection) -> Unit,
    private val onStop: () -> Unit,
) {
    var skippingDirection: SkipDirection? by mutableStateOf(null)

    var ticket: Int = 0

    fun startSkipping(direction: SkipDirection): Int {
        skippingDirection = direction
        onStart(direction)
        return ++ticket
    }

    fun stopSkipping(ticket: Int) {
        if (ticket == this.ticket) {
            skippingDirection = null
            onStop()
        }
    }
}

enum class SkipDirection {
    FORWARD, BACKWARD
}

fun Modifier.longPressFastSkip(
    state: FastSkipState,
    direction: SkipDirection,
): Modifier {
    var ticket = 0
    return detectLongPressGesture(
        onStart = {
            ticket = state.startSkipping(direction)
        },
        onEnd = {
            state.stopSkipping(ticket)
        },
    )
}
//    pointerInput(Unit) {
//    detectLongPressGesture()
////    detectTapGestures(
////        onPress = {
////            val ticket = state.startSkipping(direction)
////            awaitPointerEventScope {
////                var event = awaitPointerEvent()
////                while (event.changes.any { it.pressed }) {
////                    event = awaitPointerEvent()
////                }
////
////                state.stopSkipping(ticket)
////            }
////        }
////    )
//}

fun Modifier.detectLongPressGesture(
    onStart: () -> Unit,
    onEnd: () -> Unit,
    longPressTimeout: Long = 500L
): Modifier = pointerInput(Unit) {
    coroutineScope {
        val touchSlop = viewConfiguration.touchSlop
        var isLongPressDetected = false

        awaitEachGesture {
            val initialPosition = awaitFirstDown(requireUnconsumed = false).position
            // note: we don't consume the down event

            // Starts a job to mark long press detected if the user does not move the pointer, 
            // i.e. is holding at the same position for a certain time).
            val longPressJob = launch {
                delay(longPressTimeout)
                onStart()
                isLongPressDetected = true
            }

            var change = awaitPointerEvent()
            while (change.changes.any { it.pressed }) { // Pointer is still down
                val pointer = change.changes[0]
                if (isLongPressDetected) {
                    // Consume all events so that we won't trigger other gestures like swiping
                    change.changes.forEach { it.consume() }
                }
                if ((pointer.position - initialPosition).getDistance() > touchSlop) {
                    // User is swiping.
                    // Note, this can also happen if the long press has already been detected.
                    longPressJob.cancel() // Stop detecting long press if it hasn't been detected yet
                }
                change = awaitPointerEvent()
            }
            // Not pressing anymore
            if (isLongPressDetected) {
                // Consume the pointer up event
                change.changes.forEach { it.consume() }
            }

            longPressJob.cancel()
            if (isLongPressDetected) {
                onEnd()
                isLongPressDetected = false
            }
        }
    }
}
