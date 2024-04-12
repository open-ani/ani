package me.him188.ani.danmaku.server.data.model

import me.him188.ani.danmaku.protocol.DanmakuLocation


data class DanmakuModel(
    val id: String,
    val senderId: String,
    val episodeId: String,
    val playTime: Double,
    val location: DanmakuLocation,
    val text: String,
    val color: Int
)