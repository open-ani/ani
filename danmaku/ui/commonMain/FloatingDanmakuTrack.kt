package me.him188.ani.danmaku.ui

import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable

/**
 * FloatingDanmakuTrack 中的弹幕在以下情况会移除:
 * - tick 中的逻辑帧检测
 * - 调用 [DanmakuTrack.clearAll]
 * 移除时必须调用 [onRemoveDanmaku] 避免内存泄露.
 */
@Stable
internal class FloatingDanmakuTrack<T : SizeSpecifiedDanmaku>(
    val trackIndex: Int,
    frameTimeNanosState: LongState,
    private val trackHeight: IntState,
    private val trackWidth: IntState,
    var speedPxPerSecond: Float,
    var safeSeparation: Float,
    // 放到这个轨道的弹幕里, 长度大于此基础长度才会加速弹幕运动, 等于此长度的弹幕速度为 100% [speedPxPerSecond]
    // var baseTextLength: Int,
    // val speedMultiplier: FloatState,
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    private val onRemoveDanmaku: (PositionedDanmakuState<T>) -> Unit
) : FrameTimeBasedDanmakuTrack<T>(frameTimeNanosState) {
    private val danmakuList: MutableList<FloatingDanmaku> = mutableListOf()

    /**
     * 检测是否有弹幕的右边缘坐标大于此弹幕的左边缘坐标.
     * 
     * 如果有那说明此弹幕放置后可能会与已有弹幕重叠.
     */
    override fun canPlace(danmaku: T, placeTimeNanos: Long): Boolean {
        if (trackWidth.value <= 0) return false
        if (danmakuList.isEmpty()) return true
        val upcomingDanmakuPosX = FloatingDanmaku(danmaku, placeTimeNanos).x
        for (d in danmakuList.asReversed()) {
            if (d.x + d.danmaku.danmakuWidth + safeSeparation > upcomingDanmakuPosX) return false
        }
        return true
    }
    
    override fun place(danmaku: T, placeTimeNanos: Long): PositionedDanmakuState<T> {
        return FloatingDanmaku(danmaku, placeTimeNanos).also { danmakuList.add(it) }
    }

    override fun clearAll() {
        danmakuList.removeAll {
            onRemoveDanmaku(it)
            true
        }
    }

    override fun tick() {
        if (danmakuList.isEmpty()) return
        danmakuList.removeAll { danmaku ->
            danmaku.isGone().also { if (it) onRemoveDanmaku(danmaku) }
        }
    }

    internal fun FloatingDanmaku.isGone(): Boolean {
        return x + danmaku.danmakuWidth.toFloat() < 0
    }

    internal fun FloatingDanmaku.isFullyVisible(): Boolean {
        return trackWidth.value.toFloat() - x >= danmaku.danmakuWidth + safeSeparation
    }
    
    @Stable
    inner class FloatingDanmaku(
        override val danmaku: T,
        override val placeFrameTimeNanos: Long,
    ) : PositionedDanmakuState<T>(
        calculatePosX = {
            val timeDiff = (frameTimeNanos - placeFrameTimeNanos) / 1_000_000_000f
            // val multiplier = speedMultiplier.value
            //     .pow(state.textWidth / (baseTextLength.toFloat() * 2f))
            //     .coerceAtLeast(1f)
            trackWidth.value - timeDiff * speedPxPerSecond // * multiplier
        },
        calculatePosY = { trackHeight.value.toFloat() * trackIndex }
    ) {
        override fun toString(): String {
            return "FloatingDanmaku(p=$x:$y, v=${isFullyVisible()}, g=${isGone()})"
        }
    }

    override fun toString(): String {
        return "FloatingTrack(index=${trackIndex}, " +
                "danmakuCount=${danmakuList.size}, " +
                "firstPlaceTimeMillis=${danmakuList.firstOrNull()?.placeFrameTimeNanos?.div(1_000_000)}, " +
                "lastPlaceTimeMillis=${danmakuList.lastOrNull()?.placeFrameTimeNanos?.div(1_000_000)})"
    }
}

