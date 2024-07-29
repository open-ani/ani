package me.him188.ani.app.platform.window

import androidx.compose.ui.geometry.Rect

actual class PlatformWindow(
    val windowHandle: Long
) {
    var nonFullscreenStyle: Long = 0
    var nonFullscreenExtStyle: Long = 0
    var nonFullscreenRect: Rect = Rect.Zero
}
