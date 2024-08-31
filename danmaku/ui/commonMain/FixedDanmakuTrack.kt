package me.him188.ani.danmaku.ui

import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable

/**
 * FixedDanmakuTrack 中的弹幕在以下情况会移除:
 * - tick 中的逻辑帧检测
 * - [FixedDanmakuTrack.place] 覆盖了正在显示的弹幕
 * - 调用 [DanmakuTrack.clearAll]
 * 移除时必须调用 [onRemoveDanmaku] 避免内存泄露.
 */
@Stable
internal class FixedDanmakuTrack<T : WidthSpecifiedDanmaku>(
    val trackIndex: Int,
    val fromBottom: Boolean,
    frameTimeNanosState: LongState,
    private val trackHeight: IntState,
    private val hostWidth: IntState,
    private val hostHeight: IntState,
    // 顶部或底部弹幕的显示时间，现在还不能自定义
    private val durationMillis: LongState,
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    private val onRemoveDanmaku: (DanmakuHostState.PositionedDanmakuState<T>) -> Unit
) : FrameTimeBasedDanmakuTrack<T>(frameTimeNanosState) {
    private var currentDanmaku: FixedDanmaku? = null
    
    override fun place(danmaku: T, placeTimeNanos: Long): DanmakuHostState.PositionedDanmakuState<T> {
        val upcomingDanmaku = FixedDanmaku(danmaku, placeTimeNanos)
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = upcomingDanmaku
        return upcomingDanmaku
    }

    override fun canPlace(
        danmaku: T,
        placeTimeNanos: Long
    ): Boolean {
        if (currentDanmaku != null) return false
        // 当前没有正在显示的弹幕并且弹幕可以被显示
        return frameTimeNanos - placeTimeNanos < durationMillis.value
    }

    override fun clearAll() {
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = null
    }

    override fun tick() {
        val current = currentDanmaku ?: return
        val danmakuTime = current.placeFrameTimeNanos
        if (frameTimeNanos - danmakuTime >= durationMillis.value * 1_000_000) {
            onRemoveDanmaku(current)
            currentDanmaku = null
        }
    }

    @Stable
    inner class FixedDanmaku(
        override val danmaku: T,
        override val placeFrameTimeNanos: Long,
    ) : DanmakuHostState.PositionedDanmakuState<T>(
        calculatePosX = { (hostWidth.value - danmaku.danmakuWidth.toFloat()) / 2 },
        calculatePosY = {
            if (fromBottom) {
                hostHeight.value - (trackIndex + 1) * trackHeight.value.toFloat()
            } else {
                trackIndex * trackHeight.value.toFloat()
            }
        }
    ) {
        override fun toString(): String {
            return "FixedDanmaku(p=$x:$y, " +
                    "d=${placeFrameTimeNanos}..${placeFrameTimeNanos + durationMillis.value})"
        }
    }

    override fun toString(): String {
        return "FixedTrack(index=${trackIndex}, " +
                "placeTime=${currentDanmaku?.placeFrameTimeNanos?.div(1_000_000)})"
    }
}