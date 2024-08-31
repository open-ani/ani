package me.him188.ani.danmaku.ui

import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue

@Stable
interface DanmakuTrack {
    /**
     * place a danmaku to the track without check.
     *
     * @return A positioned danmaku which can be placed on danmaku host.
     */
    fun place(danmaku: DanmakuState, placeTimeNanos: Long): DanmakuHostState.PositionedDanmakuState

    /**
     * check if this track can place danmaku.
     */
    fun canPlace(danmaku: DanmakuState, placeTimeNanos: Long): Boolean

    /**
     * try to place a danmaku. there are reasons that the upcoming danmaku cannot be placed.
     * - [canPlace]
     */
    fun tryPlace(danmaku: DanmakuState, placeTimeNanos: Long): DanmakuHostState.PositionedDanmakuState?

    /**
     * clear all danmaku in this track
     */
    fun clearAll()

    /**
     * check for visibility of danmaku in this track at logical tick
     */
    fun tick()
}

@Stable
internal abstract class FrameTimeBasedDanmakuTrack(
    frameTimeNanosState: LongState
) : DanmakuTrack {
    internal val frameTimeNanos: Long by frameTimeNanosState
    
    override fun tryPlace(danmaku: DanmakuState, placeTimeNanos: Long): DanmakuHostState.PositionedDanmakuState? {
        if (!canPlace(danmaku, placeTimeNanos)) return null
        return place(danmaku, placeTimeNanos)
    }
}