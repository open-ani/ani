package me.him188.ani.app.ui.foundation.effects

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager


/**
 * Moves focus to the next component when the [Key.Tab] or [Key.Enter] key is pressed.
 */
fun Modifier.moveFocusOnEnter(
    direction: FocusDirection = FocusDirection.Down
): Modifier = composed {
    val focusManager = LocalFocusManager.current
    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown && (keyEvent.key == Key.Tab || keyEvent.key == Key.Enter)) {
            true // Event consumed
        } else if (keyEvent.type == KeyEventType.KeyUp && (keyEvent.key == Key.Tab || keyEvent.key == Key.Enter)) {
            focusManager.moveFocus(direction)
            true // Event consumed
        } else {
            false // Event not consumed
        }
    }
}

/**
 * Request focus on this component by default.
 */
fun Modifier.defaultFocus(
    requester: FocusRequester = FocusRequester()
): Modifier = composed {
    LaunchedEffect(key1 = true) {
        requester.requestFocus()
    }
    focusRequester(requester)
}

typealias ComposeKey = Key

/**
 * Handles key event.
 */
fun Modifier.onKey(
    key: Key,
    onEnter: () -> Unit
): Modifier = onPreviewKeyEvent { keyEvent ->
    if (keyEvent.type == KeyEventType.KeyDown && (keyEvent.key == key)) {
        true // Consume event to prevent it from being handled by other components
    } else if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == key) {
        onEnter()
        true
    } else {
        false
    }
}

fun Modifier.onPointerEventMultiplatform(
    eventType: PointerEventType,
    pass: PointerEventPass = PointerEventPass.Main,
    onEvent: AwaitPointerEventScope.(event: PointerEvent) -> Unit
): Modifier = composed {
    val currentEventType by rememberUpdatedState(eventType)
    val currentOnEvent by rememberUpdatedState(onEvent)
    pointerInput(pass) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(pass)
                if (event.type == currentEventType) {
                    currentOnEvent(event)
                }
            }
        }
    }
}