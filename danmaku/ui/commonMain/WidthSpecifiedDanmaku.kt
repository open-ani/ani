package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable

/**
 * 已知长度的弹幕
 * 
 * 只有知道弹幕的长度才能将弹幕放到[轨道][DanmakuTrack]中.
 */
@Immutable
interface WidthSpecifiedDanmaku {
    val danmakuWidth: Int
}