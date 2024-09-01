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
    private val frameTimeNanosState: LongState,
    private val trackHeight: IntState,
    private val trackWidth: IntState,
    var speedPxPerSecond: Float,
    var safeSeparation: Float,
    // 放到这个轨道的弹幕里, 长度大于此基础长度才会加速弹幕运动, 等于此长度的弹幕速度为 100% [speedPxPerSecond]
    // var baseTextLength: Int,
    // val speedMultiplier: FloatState,
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    private val onRemoveDanmaku: (PositionedDanmakuState<T>) -> Unit
) : DanmakuTrack<T> {
    private val danmakuList: MutableList<FloatingDanmaku> = mutableListOf()

    /**
     * 检测是否有弹幕的右边缘坐标大于此弹幕的左边缘坐标.
     * 
     * 如果有那说明此弹幕放置后可能会与已有弹幕重叠.
     */
    override fun canPlace(danmaku: T, placeTimeNanos: Long): Boolean {
        // 弹幕轨道宽度为 0 一定不能放
        if (trackWidth.value <= 0) return false
        
        val upcomingDanmaku = FloatingDanmaku(danmaku, placeTimeNanos)
        // 弹幕缓存为空, 那就判断是否 gone 了, 如果 gone 了就不放置
        if (danmakuList.isEmpty()) return !upcomingDanmaku.isGone()
        
        // reverse 加快高阶函数的判断循环
        val reversedList = danmakuList.asReversed()
        if (upcomingDanmaku.x.isNaN()) {
            // 如果 upcoming 弹幕也是未放置的, 若缓存里也有 未放置的弹幕 或者 未显示完全的弹幕 时一定不能放置
            return !reversedList.any { it.x.isNaN() || !it.isFullyVisible() }
        } else {
            // 如果有未显示的弹幕, 那判断是否有未显示完整的弹幕. 如果有就不能放置, 会导致直接重叠.
            if (reversedList.any { it.x.isNaN() }) {
                return upcomingDanmaku.isFullyVisible()
            } else {
                // 所有缓存和 upcoming 都已经是放置的, 那就判断
                for (d in reversedList) {
                    if (d.x + d.danmaku.danmakuWidth + safeSeparation > upcomingDanmaku.x) return false
                }
                return true
            }
        }
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
        // 没放置的弹幕一定没有显示完
        if (placeFrameTimeNanos == PositionedDanmakuState.NOT_PLACED || x.isNaN()) return false
        return x + danmaku.danmakuWidth.toFloat() < 0
    }

    internal fun FloatingDanmaku.isFullyVisible(): Boolean {
        // 没放置的弹幕一定没有显示完整
        if (placeFrameTimeNanos == PositionedDanmakuState.NOT_PLACED || x.isNaN()) return false
        return trackWidth.value.toFloat() - x >= danmaku.danmakuWidth + safeSeparation
    }
    
    @Stable
    inner class FloatingDanmaku(
        override val danmaku: T,
        override var placeFrameTimeNanos: Long,
    ) : PositionedDanmakuState<T>() {
        init {
            // 对于已经指定时间的弹幕, 应该立刻计算位置
            // 在 [canPlace] 中需要立刻使用该弹幕的位置进行逻辑判断.
            if (placeFrameTimeNanos != NOT_PLACED) calculatePos()
        }
        
        override fun calculatePosX(): Float {
            // 要计算的弹幕必须已经放置了
            check(placeFrameTimeNanos != NOT_PLACED) {
                "Danmaku position which is not placed cannot be calculated."
            }
            
            val timeDiff = (frameTimeNanosState.value - placeFrameTimeNanos) / 1_000_000_000f
            // val multiplier = speedMultiplier.value
            //     .pow(state.textWidth / (baseTextLength.toFloat() * 2f))
            //     .coerceAtLeast(1f)
            return trackWidth.value - timeDiff * speedPxPerSecond // * multiplier
        }

        override fun calculatePosY(): Float {
            return trackHeight.value.toFloat() * trackIndex
        }
        
        override fun toString(): String {
            return "FloatingDanmaku(pox=[$x:$y], fullyVisible=${isFullyVisible()})"
        }
    }

    override fun toString(): String {
        return "FloatingTrack(index=${trackIndex}, " +
                "danmakuCount=${danmakuList.size}, " +
                "firstPlaceTimeMillis=${danmakuList.firstOrNull()?.placeFrameTimeNanos?.div(1_000_000)}, " +
                "lastPlaceTimeMillis=${danmakuList.lastOrNull()?.placeFrameTimeNanos?.div(1_000_000)})"
    }
}

