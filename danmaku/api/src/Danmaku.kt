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
) {
    override fun toString(): String {
        return "Danmaku(id='$id', time=$time, senderId='$senderId', location=$location, text='$text', color=$color)"
    }
}

enum class DanmakuLocation {
    TOP,
    BOTTOM,
    NORMAL,
}
