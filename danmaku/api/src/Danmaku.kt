package me.him188.ani.danmaku.api

class Danmaku(
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
