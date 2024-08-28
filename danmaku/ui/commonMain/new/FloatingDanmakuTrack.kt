package me.him188.ani.danmaku.ui.new

import androidx.compose.runtime.FloatState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

@Stable
class FloatingDanmakuTrack(
    val trackIndex: Int,
    private val currentTimeMillis: LongState,
    private val trackHeight: IntState,
    private val screenWidth: IntState,
    private val speedPxPerSecond: State<Float>,
    private val safeSeparation: State<Float>,
    
    // 某个弹幕消失了
    override val onRemoveDanmaku: (DanmakuHostState.PositionedDanmakuState) -> Unit
) : DanmakuTrack {
    internal val danmakuList: MutableList<FloatingDanmaku> = mutableListOf()
    
    override fun tick() {
        if (danmakuList.isEmpty()) return
        danmakuList.removeAll { danmaku ->
            danmaku.isGone().also { if (it) onRemoveDanmaku(danmaku) }
        }
    }
    
    // 检测是否有弹幕的右边缘坐标大于此弹幕的左边缘坐标
    // 如果有那说明此弹幕放置后可能会与已有弹幕重叠
    override fun canPlace(danmaku: DanmakuState): Boolean {
        if (danmakuList.isEmpty()) return true
        val upcomingDanmakuPosX = FloatingDanmaku(danmaku).calculatePosX()
        for (d in danmakuList.asReversed()) {
            if (d.calculatePosX() + d.state.textWidth + safeSeparation.value > upcomingDanmakuPosX) return false
        }
        return true
    }
    
    override fun place(danmaku: DanmakuState): DanmakuHostState.PositionedDanmakuState {
        return FloatingDanmaku(danmaku).also { danmakuList.add(it) }
    }
    
    override fun tryPlace(danmaku: DanmakuState): DanmakuHostState.PositionedDanmakuState? {
        if (!canPlace(danmaku)) return null
        return place(danmaku)
    }

    override fun clearAll() {
        danmakuList.removeAll {
            onRemoveDanmaku(it)
            true
        }
    }

    internal fun FloatingDanmaku.isGone(): Boolean {
        return calculatePosX() + state.textWidth.toFloat() < 0
    }

    internal fun FloatingDanmaku.isFullyVisible(): Boolean {
        return screenWidth.value.toFloat() - calculatePosX() >= state.textWidth + safeSeparation.value
    }
    
    @Immutable
    inner class FloatingDanmaku(
        override val state: DanmakuState,
    ) : DanmakuHostState.PositionedDanmakuState {
        override fun calculatePosX(): Float {
            val timeDiff = (currentTimeMillis.value - state.presentation.danmaku.playTimeMillis) / 1000f
            return screenWidth.value - timeDiff * speedPxPerSecond.value
        }

        override fun calculatePosY(): Float {
            return trackHeight.value.toFloat() * trackIndex
        }

        override fun toString(): String {
            return "FloatingDanmaku(p=${calculatePosX()}:${calculatePosY()}, v=${isFullyVisible()}, g=${isGone()})"
        }
    }
}

