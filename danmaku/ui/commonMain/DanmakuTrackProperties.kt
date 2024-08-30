package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
@Stable
class DanmakuTrackProperties(
    /**
     * Shift of the danmaku to be considered as fully out of the screen.
     */
    val visibilitySafeArea: Int = 0,
    /**
     * vertical padding of track, both top and bottom.
     */
    val verticalPadding: Int = 1,
    /**
     * speed multiplier for speed of floating danmaku.
     * represents a multiplier to speed that 2x length of danmaku text
     */
    val speedMultiplier: Float = 1.14f
) {
    companion object {
        val Default = DanmakuTrackProperties()
    }
}