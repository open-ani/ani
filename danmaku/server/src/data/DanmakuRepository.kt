package me.him188.ani.danmaku.server.data

import me.him188.ani.danmaku.protocol.Danmaku
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.server.data.model.DanmakuModel
import java.util.UUID

interface DanmakuRepository {
    suspend fun add(episodeId: String, danmakuInfo: DanmakuInfo, userId: String): Boolean
    suspend fun selectByEpisodeAndTime(episodeId: String, fromTime: Long, toTime: Long, maxCount: Int): List<Danmaku>
}

class InMemoryDanmakuRepositoryImpl : DanmakuRepository {
    private val danmakus = mutableListOf<DanmakuModel>()

    override suspend fun add(episodeId: String, danmakuInfo: DanmakuInfo, userId: String): Boolean {
        danmakus.add(
            DanmakuModel(
                senderId = userId,
                episodeId = episodeId,
                playTime = danmakuInfo.playTime,
                location = danmakuInfo.location,
                text = danmakuInfo.text,
                color = danmakuInfo.color
            )
        )
        return true
    }

    override suspend fun selectByEpisodeAndTime(
        episodeId: String,
        fromTime: Long,
        toTime: Long,
        maxCount: Int
    ): List<Danmaku> {
        return danmakus.filter {
            it.episodeId == episodeId && it.playTime in fromTime..toTime
        }.take(maxCount).map {
            Danmaku(
                id = it.id.toString(),
                senderId = it.senderId,
                danmakuInfo = DanmakuInfo(
                    playTime = it.playTime,
                    location = it.location,
                    text = it.text,
                    color = it.color
                )
            )
        }
    }
}

