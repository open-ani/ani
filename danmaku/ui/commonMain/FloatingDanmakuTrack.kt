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
    private val onRemoveDanmaku: (FloatingDanmaku<T>) -> Unit
) : DanmakuTrack<T, FloatingDanmaku<T>> {
    private val danmakuList: MutableList<FloatingDanmaku<T>> = mutableListOf()

    /**
     * 检测是否可以放置这条[弹幕][danmaku].
     * 
     * 无论如何弹幕都不可以放到轨道长度之外.
     */
    override fun canPlace(danmaku: T, placeTimeNanos: Long): Boolean {
        check(placeTimeNanos >= DanmakuTrack.NOT_PLACED) {
            "cannot set placeTimeNanos to negative since frameTimeNanos is always positive."
        }
        // 弹幕轨道宽度为 0 一定不能放
        if (trackWidth.value <= 0) return false
        // 无论如何都不能放置在轨道最右侧之外
        if (placeTimeNanos != DanmakuTrack.NOT_PLACED && frameTimeNanosState.value - placeTimeNanos < 0)
            return false
        
        // 如果指定了放置时间, 则需要计算划过的距离
        val upcomingDistanceX = if (placeTimeNanos == DanmakuTrack.NOT_PLACED) 0f else
            (frameTimeNanosState.value - placeTimeNanos) / 1_000_000_000 * speedPxPerSecond
        val upcoming = FloatingDanmaku(danmaku, upcomingDistanceX, trackIndex, trackHeight)
        
        // 弹幕缓存为空, 那就判断是否 gone 了, 如果 gone 了就不放置
        if (danmakuList.isEmpty()) return !upcoming.isGone()
        // 如果缓存不为空, 那就判断是否有重叠
        return upcoming.isNonOverlapping(danmakuList)
        
    }
    
    override fun place(danmaku: T, placeTimeNanos: Long): FloatingDanmaku<T> {
        val upcomingDistanceX = if (placeTimeNanos == DanmakuTrack.NOT_PLACED) 0f else
            (frameTimeNanosState.value - placeTimeNanos) / 1_000_000_000 * speedPxPerSecond
        return FloatingDanmaku(danmaku, upcomingDistanceX, trackIndex, trackHeight)
            .also { danmakuList.add(it) }
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

    private fun FloatingDanmaku<T>.isGone(): Boolean {
        return distanceX > trackWidth.value + danmaku.danmakuWidth
    }

    // internal fun FloatingDanmaku<T>.isFullyVisible(): Boolean {
    //     return distanceX >= danmaku.danmakuWidth + safeSeparation
    // }

    /**
     * [list] should be sorted increasingly by range left.
     */
    private fun FloatingDanmaku<T>.isNonOverlapping(list: List<FloatingDanmaku<T>>): Boolean {
        fun FloatingDanmaku<T>.left() = trackWidth.value.toFloat() - distanceX
        fun FloatingDanmaku<T>.right() = left() + danmaku.danmakuWidth + safeSeparation
        
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
        return "FloatingTrack(index=${trackIndex}, danmakuCount=${danmakuList.size})"
    }
}

/**
 * 一条浮动弹幕
 */
@Stable
internal class FloatingDanmaku<T : SizeSpecifiedDanmaku>(
    var danmaku: T,
    initialDistanceX: Float = 0f,
    private val trackIndex: Int,
    private val trackHeight: IntState,
) {
    /**
     * 弹幕在浮动轨道已滚动的距离, 是正数. 单位 px
     *
     * 例如, 如果弹幕现在在左侧刚被放置, 则等于 `0`.
     * 如果左边已滑倒轨道最左侧, 则等于轨道长度.
     */
    var distanceX: Float = initialDistanceX
        internal set

    /**
     * calculate pos y lazily in ui loop
     */
    var y = Float.NaN
        internal set

    internal fun calculatePosY(): Float {
        return trackHeight.value.toFloat() * trackIndex
    }

    override fun toString(): String {
        return "FloatingDanmaku(elapsedX=$distanceX, y=$y)"
    }
}