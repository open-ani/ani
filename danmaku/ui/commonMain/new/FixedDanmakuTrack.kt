package me.him188.ani.danmaku.ui.new

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlin.math.absoluteValue

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
    private val currentTimeMillis: LongState,
    private val trackHeight: IntState,
    private val screenWidth: IntState,
    private val screenHeight: IntState,
    // 顶部或底部弹幕的显示时间，现在还不能自定义
    private val durationMillis: State<Long> = mutableStateOf(5000L),
    // 某个弹幕需要消失, 必须调用此函数避免内存泄漏.
    override val onRemoveDanmaku: (DanmakuHostState.PositionedDanmakuState) -> Unit
) : DanmakuTrack {
    internal var currentDanmaku: FixedDanmaku? = null
    
    override fun place(danmaku: DanmakuState): DanmakuHostState.PositionedDanmakuState {
        val upcomingDanmaku = FixedDanmaku(danmaku)
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = upcomingDanmaku
        return upcomingDanmaku
    }

    override fun canPlace(danmaku: DanmakuState): Boolean {
        if (currentDanmaku != null) return false
        // 当前没有正在显示的弹幕并且弹幕可以被显示
        val danmakuTime = danmaku.presentation.danmaku.playTimeMillis
        return currentTimeMillis.value - danmakuTime < durationMillis.value
    }

    override fun clearAll() {
        currentDanmaku?.let(onRemoveDanmaku)
        currentDanmaku = null
    }

    override fun tick() {
        val current = currentDanmaku ?: return
        val danmakuTime = current.state.presentation.danmaku.playTimeMillis
        if (currentTimeMillis.value - danmakuTime >= durationMillis.value) {
            onRemoveDanmaku(current)
            currentDanmaku = null
        }
    }

    @Immutable
    inner class FixedDanmaku(
        override val state: DanmakuState,
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
                    "d=${state.presentation.danmaku.playTimeMillis}..${state.presentation.danmaku.playTimeMillis + durationMillis.value})"
        }
    }
}