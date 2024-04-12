package me.him188.ani.danmaku.protocol

import kotlinx.serialization.Serializable

@Serializable
data class DanmakuPostRequest(
    val episodeId: String,  // unique episode id
    val danmakuInfo: DanmakuInfo,
)

@Serializable
data class DanmakuGetRequest(
    val episodeId: String,  // unique episode id
    val maxCount: Int = 8000, // max count of danmaku to get, should not exceed 8000
    val fromTime: Double = 0.0, // in seconds
    val toTime: Double = -1.0, // in seconds, negative value means no limit
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
    val playTime: Double, // in seconds
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