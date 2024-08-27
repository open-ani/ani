package me.him188.ani.danmaku.ui.new

import androidx.compose.runtime.Stable

@Stable
interface DanmakuTrack {
    val onRemoveDanmaku: (DanmakuHostState.PositionedDanmakuState) -> Unit
    
    /**
     * place a danmaku to the track
     *
     * @return A positioned danmaku which can be placed on danmaku host.
     */
    fun place(danmakuState: DanmakuState): DanmakuHostState.PositionedDanmakuState

    /**
     * check if this track can place danmaku now.
     */
    fun canPlace(): Boolean

    /**
     * try to place a danmaku, if this track can't place now, return null.
     */
    fun tryPlace(danmakuState: DanmakuState): DanmakuHostState.PositionedDanmakuState?

    /**
     * clear all danmaku in this track
     */
    fun clearAll()

    /**
     * check for visibility of danmaku in this track at logical tick
     */
    fun tick()
}