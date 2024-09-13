package me.him188.ani.app.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

actual open class PlatformWindow(
    val windowHandle: Long,
) {
    internal var savedWindowState: SavedWindowState? = null

    var isUndecoratedFullscreen by mutableStateOf(false)
}

class SavedWindowState(
    val style: Int,
    val exStyle: Int,
    val rect: Rect,
    val maximized: Boolean,
)
