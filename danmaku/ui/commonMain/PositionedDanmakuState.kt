package me.him188.ani.danmaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Stable

/**
 * DanmakuState which is positioned and can be placed on [Canvas].
 */
@Stable
abstract class PositionedDanmakuState<T : SizeSpecifiedDanmaku> {
    abstract val danmaku: T

    /**
     * `-1` represents this danmaku hasn't placed yet.
     */
    abstract var placeFrameTimeNanos: Long

    /**
     * `NaN` represents this position of danmaku hasn't calculated.
     */
    var x: Float = Float.NaN
    var y: Float = Float.NaN

    /**
     * 重新计算并缓存弹幕位置, 如果 [calculateX] 为 `false` 则不重新计算.
     */
    internal fun calculatePos(
        calculateX: Boolean = true, 
        calculateY: Boolean = true
    ) {
        // 如果当前没有缓存的坐标则总是尝试计算一次
        // 如果有那就由 calculateX 参数决定是否需要计算
        if (calculateX || x.isNaN()) x = calculatePosX()
        if (calculateY || y.isNaN()) y = calculatePosY()
    }

    abstract fun calculatePosX(): Float
    
    abstract fun calculatePosY(): Float
    
    companion object {
        internal const val NOT_PLACED = -1L
    }
}