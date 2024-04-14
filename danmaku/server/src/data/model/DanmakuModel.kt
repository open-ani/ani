package me.him188.ani.danmaku.server.data.model

import me.him188.ani.danmaku.protocol.DanmakuLocation
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID


data class DanmakuModel(
    @BsonId
    val id: UUID = UUID.randomUUID(),
    val senderId: String,
    val episodeId: String, // index
    val playTime: Double,
    val location: DanmakuLocation,
    val text: String,
    val color: Int
)