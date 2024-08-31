package me.him188.ani.danmaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Stable


/**
 * DanmakuState which is positioned and can be placed on [Canvas].
 */
@Stable
abstract class PositionedDanmakuState<T : SizeSpecifiedDanmaku>(
    private val calculatePosX: () -> Float,
    private val calculatePosY: () -> Float
) {
    abstract val danmaku: T
    abstract val placeFrameTimeNanos: Long

    var x: Float = calculatePosX()
    var y: Float = calculatePosY()

    internal fun calculatePos() {
        x = calculatePosX()
        y = calculatePosY()
    }
}