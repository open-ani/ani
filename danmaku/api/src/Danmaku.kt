package me.him188.ani.danmaku.api

import androidx.compose.runtime.Immutable

@Immutable
class Danmaku(
    val id: String,
    val time: Double, // xx.xx seconds
    val senderId: String,
    val location: DanmakuLocation,
    val text: String,
    val color: Int, // RGB
)

enum class DanmakuLocation {
    TOP,
    BOTTOM,
    NORMAL,
}
