package me.him188.ani.danmaku.ui

import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue

/**
 * 弹幕轨道, 支持放置已知长度的弹幕. 
 * 
 * 这意味着[已经知道长度的弹幕][SizeSpecifiedDanmaku]一定可以计算是否可以放置到此轨道上.
 */
@Stable
interface DanmakuTrack<T : SizeSpecifiedDanmaku> {
    /**
     * place a danmaku to the track without check.
     *
     * @return A positioned danmaku which can be placed on danmaku host.
     */
    fun place(danmaku: T, placeTimeNanos: Long): PositionedDanmakuState<T>

    /**
     * check if this track can place danmaku.
     */
    fun canPlace(danmaku: T, placeTimeNanos: Long): Boolean

    /**
     * try to place a danmaku. there are reasons that the upcoming danmaku cannot be placed.
     * - [canPlace]
     */
    fun tryPlace(danmaku: T, placeTimeNanos: Long): PositionedDanmakuState<T>?

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
internal abstract class FrameTimeBasedDanmakuTrack<T : SizeSpecifiedDanmaku>(
    frameTimeNanosState: LongState
) : DanmakuTrack<T> {
    internal val frameTimeNanos: Long by frameTimeNanosState
    
    override fun tryPlace(danmaku: T, placeTimeNanos: Long): PositionedDanmakuState<T>? {
        if (!canPlace(danmaku, placeTimeNanos)) return null
        return place(danmaku, placeTimeNanos)
    }
}