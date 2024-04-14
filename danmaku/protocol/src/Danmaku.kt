package me.him188.ani.danmaku.protocol

import kotlinx.serialization.Serializable

@Serializable
data class DanmakuPostRequest(
    val danmakuInfo: DanmakuInfo,
)

@Serializable
data class DanmakuGetResponse(
    val danmakuList: List<Danmaku>,
)

@Serializable
data class Danmaku(
    val id: String, // unique danmaku id
    val senderId: String, // unique sender id
    val danmakuInfo: DanmakuInfo,
)

@Serializable
data class DanmakuInfo(
    val playTime: Long, // in milliseconds
    val color: Int, // RGB
    val text: String,
    val location: DanmakuLocation,
)

@Serializable
enum class DanmakuLocation {
    TOP,
    BOTTOM,
    NORMAL,
}