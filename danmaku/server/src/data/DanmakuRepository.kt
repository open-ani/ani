package me.him188.ani.danmaku.server.data

import me.him188.ani.danmaku.protocol.Danmaku
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.server.data.model.DanmakuModel
import java.util.UUID

interface DanmakuRepository {
    fun add(episodeId: String, danmakuInfo: DanmakuInfo, userId: String): Boolean
    fun selectByEpisodeAndTime(episodeId: String, fromTime: Double, toTime: Double, maxCount: Int): List<Danmaku>
}

class InMemoryDanmakuRepositoryImpl : DanmakuRepository {
    private val danmakus = mutableListOf<DanmakuModel>()

    override fun add(episodeId: String, danmakuInfo: DanmakuInfo, userId: String): Boolean {
        danmakus.add(
            DanmakuModel(
                id = UUID.randomUUID().toString(),
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

    override fun selectByEpisodeAndTime(
        episodeId: String,
        fromTime: Double,
        toTime: Double,
        maxCount: Int
    ): List<Danmaku> {
        val actualToTime = if (toTime < 0) Double.MAX_VALUE else toTime
        return danmakus.filter {
            it.episodeId == episodeId && it.playTime in fromTime..actualToTime
        }.take(maxCount).map {
            Danmaku(
                id = it.id,
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

