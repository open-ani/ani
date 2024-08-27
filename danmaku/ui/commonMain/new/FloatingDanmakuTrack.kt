package me.him188.ani.danmaku.ui.new

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable

@Stable
class FloatingDanmakuTrack(
    private val currentTimeMillis: LongState,
    private val trackHeight: IntState,
    private val screenWidth: IntState,
    val trackIndex: Int,
    val speedPxPerSecond: Int,
    // 某个弹幕消失了
    override val onRemoveDanmaku: (DanmakuHostState.PositionedDanmakuState) -> Unit
) : DanmakuTrack {
    private val danmakuList: MutableList<FloatingDanmaku> = mutableListOf()

    /**
     * 在逻辑帧执行弹幕可见性检查
     */
    override fun tick() {
        if (danmakuList.isEmpty()) return
        // If it is already sorted. it doesn't cost time.
        danmakuList.sortBy { it.state.presentation.danmaku.playTimeMillis }
        danmakuList.removeAll { danmaku ->
            danmaku.isGone().also { if (it) onRemoveDanmaku(danmaku) }
        }
    }

    /**
     * check if this track can place danmaku now.
     */
    override fun canPlace(): Boolean {
        if (danmakuList.isEmpty()) return true
        // If it is already sorted. it doesn't cost time.
        danmakuList.sortBy { it.state.presentation.danmaku.playTimeMillis }
        return danmakuList.last().isFullyVisible()
    }

    /**
     * place a danmaku to the track
     * 
     * @return A positioned danmaku which can be placed on danmaku host.
     */
    override fun place(danmaku: DanmakuState): DanmakuHostState.PositionedDanmakuState {
        return FloatingDanmaku(danmaku).also { danmakuList.add(it) }
    }

    /**
     * try to place a danmaku, if this track can't place now, return null.
     */
    override fun tryPlace(danmaku: DanmakuState): DanmakuHostState.PositionedDanmakuState? {
        if (!canPlace()) return null
        return place(danmaku)
    }

    /**
     * remove all danmaku of this track.
     */
    override fun clearAll() {
        danmakuList.removeAll {
            onRemoveDanmaku(it)
            true
        }
    }

    private inline fun FloatingDanmaku.isGone(): Boolean {
        return calculatePosX() + state.textWidth.toFloat() < 0
    }

    private inline fun FloatingDanmaku.isFullyVisible(): Boolean {
        return screenWidth.value.toFloat() - calculatePosX() >= state.textWidth
    }
    
    @Immutable
    inner class FloatingDanmaku(
        override val state: DanmakuState,
    ) : DanmakuHostState.PositionedDanmakuState {
        override fun calculatePosX(): Float {
            val timeDiff = (currentTimeMillis.value - state.presentation.danmaku.playTimeMillis) / 1000f
            return screenWidth.value - timeDiff * speedPxPerSecond
        }

        override fun calculatePosY(): Float {
            return trackHeight.value.toFloat() * trackIndex
        }
    }
}

