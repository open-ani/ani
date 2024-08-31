package me.him188.ani.danmaku.ui

import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue

/**
 * FixedDanmakuTrack 中的弹幕在以下情况会移除:
 * - tick 中的逻辑帧检测
 * - [FixedDanmakuTrack.place] 覆盖了正在显示的弹幕
 * - 调用 [DanmakuTrack.clearAll]
 * 移除时必须调用 [DanmakuTrack.onRemoveDanmaku] 避免内存泄露.
 */
@Stable
class FixedDanmakuTrack(
    val trackIndex: Int,
    val fromBottom: Boolean,
    frameTimeNanosState: LongState,
    private val trackHeight: IntState,
    private val screenWidth: IntState,
    private val screenHeight: IntState,
    // 顶部或底部弹幕的显示时间，现在还不能自定义
    private val durationMillis: LongState,
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    private val onRemoveDanmaku: (DanmakuHostState.PositionedDanmakuState) -> Unit
) : DanmakuTrack {
    private var currentDanmaku: FixedDanmaku? = null
    override val frameTimeNanos: Long by frameTimeNanosState
    
    override fun place(
        danmaku: DanmakuState,
        placeTimeNanos: Long
    ): DanmakuHostState.PositionedDanmakuState {
        val upcomingDanmaku = FixedDanmaku(danmaku, placeTimeNanos)
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = upcomingDanmaku
        return upcomingDanmaku
    }

    override fun canPlace(
        danmaku: DanmakuState,
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
        override val state: DanmakuState,
        override val placeFrameTimeNanos: Long,
    ) : DanmakuHostState.PositionedDanmakuState {
        override fun calculatePosX(): Float {
            return (screenWidth.value - state.textWidth.toFloat()) / 2
        }

        override fun calculatePosY(): Float {
            return if (fromBottom) {
                screenHeight.value - (trackIndex + 1) * trackHeight.value.toFloat()
            } else {
                trackIndex * trackHeight.value.toFloat()
            }
        }

        override fun toString(): String {
            return "FixedDanmaku(p=${calculatePosX()}:${calculatePosY()}, " +
                    "d=${placeFrameTimeNanos}..${placeFrameTimeNanos + durationMillis.value})"
        }
    }

    override fun toString(): String {
        return "FixedTrack(index=${trackIndex}, " +
                "placeTime=${currentDanmaku?.placeFrameTimeNanos?.div(1_000_000)})"
    }
}