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
     * 检测是否可以放置这条[弹幕][danmaku].
     * 
     * 无论如何弹幕都不可以放到轨道长度之外.
     */
    override fun canPlace(danmaku: T, placeTimeNanos: Long): Boolean {
        check(placeTimeNanos >= PositionedDanmakuState.NOT_PLACED) {
            "cannot set placeTimeNanos to negative since frameTimeNanos is always positive."
        }
        // 弹幕轨道宽度为 0 一定不能放
        if (trackWidth.value <= 0) return false
        
        val upcoming = FloatingDanmaku(danmaku, placeTimeNanos)
        // 弹幕缓存为空, 那就判断是否 gone 了, 如果 gone 了就不放置
        if (danmakuList.isEmpty()) return !upcoming.isGone()
        
        if (upcoming.x.isNaN()) {
            // 如果 upcoming 弹幕也是未放置的, 若缓存里也有 未放置的弹幕 或者 未显示完全的弹幕 时一定不能放置
            // reverse 加快高阶函数的判断循环
            return !danmakuList.asReversed().any { it.x.isNaN() || !it.isFullyVisible() }
        } else {
            // 无论如何弹幕都不可以放到轨道长度之外
            if (upcoming.x >= trackWidth.value) return false
            // 目前弹幕的速度都一样, 所以直接判断 overlapping 即可
            return upcoming.isNonOverlapping(danmakuList)
        }
    }
    
    override fun place(danmaku: T, placeTimeNanos: Long): PositionedDanmakuState<T> {
        check(placeTimeNanos >= PositionedDanmakuState.NOT_PLACED) {
            "cannot set placeTimeNanos to negative since frameTimeNanos is always positive."
        }
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

    /**
     * [list] should be sorted increasingly by range left.
     */
    private fun PositionedDanmakuState<T>.isNonOverlapping(list: List<PositionedDanmakuState<T>>): Boolean {
        fun PositionedDanmakuState<T>.left() = if (x.isNaN()) trackWidth.value.toFloat() else x
        fun PositionedDanmakuState<T>.right() = left() + danmaku.danmakuWidth + safeSeparation
        
        // fast path: 检查弹幕左侧是否比列表最后一个还大
        if (list.isEmpty() || left() >= list.last().right()) return true

        // 下面是 chatgpt 写的
        // Perform binary search to find the insertion point
        val insertionIndex = list.binarySearch { it.left().compareTo(left()) }
        val index = if (insertionIndex < 0) -insertionIndex - 1 else insertionIndex

        // Check for overlap with the range at the insertion point and the one before it
        if (index < list.size && right() >= list[index].left()) {
            return false
        }
        if (index > 0 && left() <= list[index - 1].right()) {
            return false
        }

        return true
    }

    override fun toString(): String {
        return "FloatingTrack(index=${trackIndex}, " +
                "danmakuCount=${danmakuList.size}, " +
                "firstPlaceTimeMillis=${danmakuList.firstOrNull()?.placeFrameTimeNanos?.div(1_000_000)}, " +
                "lastPlaceTimeMillis=${danmakuList.lastOrNull()?.placeFrameTimeNanos?.div(1_000_000)})"
    }
}