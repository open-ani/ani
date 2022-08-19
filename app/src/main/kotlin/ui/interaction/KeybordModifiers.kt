package me.him188.animationgarden.desktop.ui.interaction

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent

inline fun Modifier.onEnterKeyEvent(crossinline action: (KeyEvent) -> Boolean) = onKeyEvent {
    if (it.key == Key.Enter || it.key == Key.NumPadEnter) {
        action(it)
    } else false
}