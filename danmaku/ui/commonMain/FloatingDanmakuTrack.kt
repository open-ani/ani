package me.him188.ani.danmaku.ui

import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable

/**
 * FloatingDanmakuTrack 中的弹幕在以下情况会移除:
 * - tick 中的逻辑帧检测
 * - 调用 [DanmakuTrack.clearAll]
 * 移除时必须调用 [DanmakuTrack.onRemoveDanmaku] 避免内存泄露.
 */
@Stable
class FloatingDanmakuTrack(
    val trackIndex: Int,
    override val frameTimeNanos: LongState,
    private val trackHeight: IntState,
    private val screenWidth: IntState,
    var speedPxPerSecond: Float,
    var safeSeparation: Float,
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    override val onRemoveDanmaku: (DanmakuHostState.PositionedDanmakuState) -> Unit
) : DanmakuTrack {
    internal val danmakuList: MutableList<FloatingDanmaku> = mutableListOf()
    
    // 检测是否有弹幕的右边缘坐标大于此弹幕的左边缘坐标
    // 如果有那说明此弹幕放置后可能会与已有弹幕重叠
    override fun canPlace(
        danmaku: DanmakuState,
        placeTimeNanos: Long
    ): Boolean {
        if (danmakuList.isEmpty()) return true
        val upcomingDanmakuPosX = FloatingDanmaku(danmaku, placeTimeNanos).calculatePosX()
        for (d in danmakuList.asReversed()) {
            if (d.calculatePosX() + d.state.textWidth + safeSeparation > upcomingDanmakuPosX) return false
        }
        return true
    }
    
    override fun place(
        danmaku: DanmakuState,
        placeTimeNanos: Long
    ): DanmakuHostState.PositionedDanmakuState {
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
        return calculatePosX() + state.textWidth.toFloat() < 0
    }

    internal fun FloatingDanmaku.isFullyVisible(): Boolean {
        return screenWidth.value.toFloat() - calculatePosX() >= state.textWidth + safeSeparation
    }
    
    @Stable
    inner class FloatingDanmaku(
        override val state: DanmakuState,
        override val placeFrameTimeNanos: Long,
    ) : DanmakuHostState.PositionedDanmakuState {
        override fun calculatePosX(): Float {
            val timeDiff = (frameTimeNanos.value - placeFrameTimeNanos) / 1_000_000_000f
            return screenWidth.value - timeDiff * speedPxPerSecond
        }

        override fun calculatePosY(): Float {
            return trackHeight.value.toFloat() * trackIndex
        }

        override fun toString(): String {
            return "FloatingDanmaku(p=${calculatePosX()}:${calculatePosY()}, v=${isFullyVisible()}, g=${isGone()})"
        }

        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other is DanmakuHostState.OverridePlaceTimeDanmakuState) {
                return other.state === this.state
            }
            return (other as? FloatingDanmaku) === this
        }

        override fun hashCode(): Int {
            var result = state.hashCode()
            result = 31 * result + placeFrameTimeNanos.hashCode()
            return result
        }
    }

    override fun toString(): String {
        return "FloatingTrack(index=${trackIndex}, " +
                "danmakuCount=${danmakuList.size}, " +
                "firstPlaceTimeMillis=${danmakuList.firstOrNull()?.placeFrameTimeNanos?.div(1_000_000)}, " +
                "lastPlaceTimeMillis=${danmakuList.lastOrNull()?.placeFrameTimeNanos?.div(1_000_000)})"
    }
}

