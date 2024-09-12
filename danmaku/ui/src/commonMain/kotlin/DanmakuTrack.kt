package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Stable

/**
 * 弹幕轨道, 支持放置已知长度的弹幕. 
 * 
 * 这意味着[已经知道长度的弹幕][SizeSpecifiedDanmaku]一定可以计算是否可以放置到此轨道上.
 */
@Stable
interface DanmakuTrack<T : SizeSpecifiedDanmaku, D> {
    /**
     * 放置弹幕到此轨道, 无状态检查
     *
     * @return 返回已经放置的弹幕
     */
    fun place(danmaku: T, placeTimeNanos: Long = NOT_PLACED): D

    /**
     * 检测这条弹幕是否可以放置到此轨道中.
     * 
     * 目前的行为:
     * - [浮动弹幕][FloatingDanmakuTrack]: 如果放置后不会和已有弹幕重叠并且[放置时间][placeTimeNanos]对应的[位置][FloatingDanmaku.distanceX]不在轨道右侧外.
     * - [固定弹幕][FixedDanmakuTrack] : 当前没有正在显示的弹幕并且[放置时间][placeTimeNanos]距离当前时间在[显示时间][FixedDanmakuTrack.durationMillis]内.
     */
    fun canPlace(danmaku: T, placeTimeNanos: Long = NOT_PLACED): Boolean

    /**
     * 尝试放置弹幕
     * 
     * @return 无法放置返回 `null`, 可放置则返回已放置的弹幕.
     */
    fun tryPlace(
        danmaku: T, 
        placeTimeNanos: Long = NOT_PLACED
    ): D? {
        if (!canPlace(danmaku, placeTimeNanos)) return null
        return place(danmaku, placeTimeNanos)
    }

    /**
     * 清除当前轨道里的所有弹幕
     */
    fun clearAll()

    /**
     * 需要循环执行的逻辑帧.
     * 
     * 弹幕轨道上述的方法通常依赖时间来进行判断, 可执行此逻辑帧 tick 来进行判断.
     * 
     * 如果不需要判断则不需要实现此方法.
     * 
     * 目前的 [FloatingDanmakuTrack] 和 [FixedDanmakuTrack] 均实现了逻辑帧并进行以下行为:
     * - 基于帧时间判断是否需要移除轨道中的过期弹幕.
     * - 基于已有弹幕判断当前时间点是否可以放置一条新弹幕.
     */
    fun tick() { }
    
    companion object {
        const val NOT_PLACED = -1L
    }
}