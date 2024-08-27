package me.him188.ani.app.platform.window

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Rect

actual class PlatformWindow(
    val windowHandle: Long,
    val composeWindow: ComposeWindow,
) {
    var nonFullscreenStyle: Long = 0
    var nonFullscreenExtStyle: Long = 0
    var nonFullscreenRect: Rect = Rect.Zero

    var didSetFullscreen by mutableStateOf(false)
}
