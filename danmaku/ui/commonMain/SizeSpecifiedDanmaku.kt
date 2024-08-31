package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Immutable

/**
 * 已知大小长度的弹幕
 * 
 * 只有知道弹幕的大小才能将弹幕放到[轨道][DanmakuTrack]中.
 */
@Immutable
interface SizeSpecifiedDanmaku {
    val danmakuWidth: Int
    val danmakuHeight: Int
}